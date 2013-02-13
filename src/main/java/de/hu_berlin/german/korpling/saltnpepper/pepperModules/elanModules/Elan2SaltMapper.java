package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.exceptions.ELANImporterException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.importer.ElanSpan;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.importer.ElanToken;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.importer.ElanWrapper;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt.ElanImporterMain;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

/**
 * This class maps data coming from the ELAN model to a Salt model.
 * @author Florian Zipser
 *
 */
public class Elan2SaltMapper 
{
	//TODO remove when inheriting from PepperMapper
	protected SDocument sDocument= null;
	//TODO remove when inheriting from PepperMapper
	public SDocument getSDocument() {
		return(sDocument);
	}
	//TODO remove when inheriting from PepperMapper
	public void setSDocument(SDocument sDocument) {
		this.sDocument= sDocument;
	}
	//TODO remove when inheriting from PepperMapper
	protected SCorpus sCorpus= null;
	//TODO remove when inheriting from PepperMapper
	public SCorpus getSCorpus() {
		return(sCorpus);
	}
	//TODO remove when inheriting from PepperMapper
	public void setSCorpus(SCorpus sCorpus) {
		this.sCorpus= sCorpus;
	}
	//TODO remove when inheriting from PepperMapper
	protected URI resourceURI= null;
	//TODO remove when inheriting from PepperMapper
	public URI getResourceURI() {
		return(resourceURI);
	}
	
	protected TranscriptionImpl elan = null;
	public TranscriptionImpl getElanModel(){
		return elan;
	}
	
	//TODO remove when inheriting from PepperMapper
	public void setResourceURI(URI resourceURI) {
		this.resourceURI= resourceURI;
	}
	//TODO remove when inheriting from PepperMapper
	private PepperModuleProperties props=null;
	//TODO remove when inheriting from PepperMapper
	public void setProps(PepperModuleProperties props) {
		this.props = props;
	}
	//TODO remove when inheriting from PepperMapper
	public PepperModuleProperties getProps() {
		return props;
	}
	
	//TODO set @override
	public void mapSCorpus()
	{
		
	}
	
	//TODO set @override
	public void mapSDocument()
	{
		// set the elan document
		String fname = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		System.out.println("filename: " +fname);
		this.elan = new TranscriptionImpl(fname);
		// create the requested layers
		SLayer morphLayer = SaltFactory.eINSTANCE.createSLayer();
		morphLayer.setSName("annotations");
		sDocument.getSDocumentGraph().addSLayer(morphLayer);
		// create the primary text
		createPrimaryData(sDocument);
		// goes through the elan document, and makes all the elan tiers into salt tiers
		// TODO add an option to ignore certain elan tiers
		traverseElanDocument(sDocument);
	}
	
	/**
	 * Creates a {@link STextualDS} object containing the primary text {@link ElanImporterMain#PRIMARY_TEXT} and adds the object
	 * to the {@link SDocumentGraph} being contained by the given {@link SDocument} object.
	 * 
	 * @param sDocument the document, to which the created {@link STextualDS} object will be added
	 */
	public void createPrimaryData(SDocument sDocument){
		if (sDocument== null)
			throw new ELANImporterException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph()== null)
			throw new ELANImporterException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		{//creating the primary text
			TierImpl primtexttier = (TierImpl) elan.getTierWithId("tok");
			StringBuffer primText = new StringBuffer();
			for (Object obj : primtexttier.getAnnotations()){
				AbstractAnnotation charAnno = (AbstractAnnotation) obj;
				primText.append(charAnno.getValue());
			}
			sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a string
			System.out.println(primText.toString());
			sTextualDS.setSText(primText.toString());
			//adding the text to the document-graph
			sDocument.getSDocumentGraph().addSNode(sTextualDS);
		}//creating the primary text
	}
	
	/**
	 * function to traverse an elan document (going through tiers, then going through the annotations in the tiers)
	 * the retrieved annotations are mapped to salt, either by adding them to an existing salt token or salt span
	 * or by creating a new salt token or span.
	 * @param sDocument from the meta annotation in the salt document, the actual elan file is retrieved.
	 */
	public void traverseElanDocument(SDocument sDocument){
		ArrayList<String> maintiers = new ArrayList<String>();
		maintiers.add("tok");
		maintiers.add("character");
		maintiers.add("txt");
		createSTokensForMainTiers(maintiers);
		addAnnotationsToTokens();
	}
	
	public void addAnnotationsToTokens(){
		STextualDS sTextualDS = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(0);
		for (Object obj : this.getElanModel().getTiers()){
			TierImpl tier = (TierImpl) obj;
			for (Object annoObj : tier.getAnnotations()){
				Annotation anno = (Annotation) annoObj;
				String value = anno.getValue();
				if (!value.trim().isEmpty()){
					long beginTime = anno.getBeginTimeBoundary();
					long endTime = anno.getEndTimeBoundary();
					int beginChar = time2char.get(beginTime);
					int endChar = time2char.get(endTime);
			        SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
			        sequence.setSSequentialDS(sTextualDS);
			        sequence.setSStart((int) beginChar);
			        sequence.setSEnd((int) endChar);
			        EList<SToken> sTokens = this.getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
			        SToken sToken = sTokens.get(0);
			        sToken.createSAnnotation("elan", tier.getName(), value.trim());
				}
			}
		}
	}
	
	protected Map<Long,Integer> time2char = new HashMap();
	
	public void createSTokensForMainTiers(ArrayList<String> maintiers){
		for (Object obj : this.getElanModel().getTiers()){
			TierImpl tier = (TierImpl) obj;
			STextualDS primaryText = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
			String primtextchangeable = primaryText.getSText();
			int offset = 0;
			if (maintiers.contains(tier.getName())){
				for (Object annoObj : tier.getAnnotations()){
					Annotation anno = (Annotation) annoObj;
					String name = tier.getName();
					String value = anno.getValue();
					if (!value.trim().isEmpty()){						
						long beginTime = anno.getBeginTimeBoundary();
						long endTime = anno.getEndTimeBoundary();
						int start =  primtextchangeable.indexOf(value);
						if (start < 0 | start > 1){
							throw new ELANImporterException("token was not found in primarytext: (" + value + ") (primtext:" + primtextchangeable + ")");
						}
			        	int stop = start + value.length();
			        	int corstart = offset + start;
			        	int corstop = offset + stop;
			        	if (!time2char.containsKey(beginTime)){
			        		time2char.put(beginTime, corstart);
			        	}
			        	if (!time2char.containsKey(endTime)){
			        		time2char.put(endTime, corstop);
			        	}
			        	offset = offset + stop;
			        	primtextchangeable = primtextchangeable.substring(stop);
						SToken sToken = sDocument.getSDocumentGraph().createSToken(primaryText, corstart, corstop);
						SLayer curSLayer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
						curSLayer.getSNodes().add(sToken);
					}
				}
			}
		}
	}
}