package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		// create the primary text - DONE
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
	public static void createPrimaryData(SDocument sDocument){
		if (sDocument== null)
			throw new ELANImporterException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph()== null)
			throw new ELANImporterException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		{//creating the primary text
			String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
			ElanWrapper ew = new ElanWrapper(filename);
			sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a string
			sTextualDS.setSText(ew.getPrimaryText());
			//adding the text to the document-graph
			sDocument.getSDocumentGraph().addSNode(sTextualDS);
		}//creating the primary text
	}

	/**
	 * gets the textual relation that glues together a salt document and the salt token that is taking place for the elan token
	 * @param sDocument relevant document
	 * @param et Elan token that we want to find in the document
	 * @return
	 */
	public static STextualRelation getSTextualRelation(SDocument sDocument, ElanToken et){
		STextualRelation out = null;
		List<STextualRelation> sTextRels = Collections.synchronizedList(sDocument.getSDocumentGraph().getSTextualRelations());
		for (STextualRelation st : sTextRels){
			if (st.getSStart() == et.getBeginChar() & st.getSEnd() == et.getEndChar()){
				out = st;
				break;
			}
		}
		return out;
	}
	
	/**
	 * function to traverse an elan document (going through tiers, then going through the annotations in the tiers)
	 * the retrieved annotations are mapped to salt, either by adding them to an existing salt token or salt span
	 * or by creating a new salt token or span.
	 * @param sDocument from the meta annotation in the salt document, the actual elan file is retrieved.
	 */
	public static void traverseElanDocument(SDocument sDocument){
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		ArrayList<String> tierNames = ew.getTierNames();
		for (String tierName : tierNames){
			System.out.println("working on " + filename + ", adding tier " + tierName);
			for (ElanSpan es : ew.getElanSpans(tierName)){
// dirty hack to make everything as a span, to solve issue with token annotations always being represented as kwic in annis
// TODO change something in ANNIS so that				
//				if (es.size() == 1){
//					ElanToken et = es.getElanToken(0);
//					setSaltToken(sDocument, et, es.getName(), es.getValue());
//				}
//				if (es.size() > 1){
					setSaltSpan(sDocument, es, es.getName(), es.getValue());
//				}
			}
		}
	}
	
	/**
	 * the elan span in the input is added (or created) to the salt document, with an annotation that consist of name and value 
	 * @param sDocument the salt document in which this span should be added or created
	 * @param es the elan span
	 * @param name annotation name
	 * @param value annotation value
	 */
	private static void setSaltSpan(SDocument sDocument, ElanSpan es, String name, String value) {
		// dirty hack to check for weird symbols in the value that we do not want.
		if (value.trim().length() != 0){
			if (String.format("%040x", new BigInteger(value.getBytes())).startsWith("-")){
				value = "";
			}
		}
		if (value.trim().length() != 0){
			SSpan sSpan= null;
			SSpanningRelation sSpanRel= null;
			SAnnotation sAnno = null;
			sSpan= SaltFactory.eINSTANCE.createSSpan();
			//sDocument.getSDocumentGraph().addSNode(sSpan);
			for (int i= 0; i< es.size(); i++)
			{
				sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
				sSpanRel.setSSpan(sSpan);
				ElanToken et = es.getElanToken(i);
				STextualRelation str = getSTextualRelation(sDocument, et);
				SToken st = null;
				if (str == null){
					st = SaltFactory.eINSTANCE.createSToken();
					sDocument.getSDocumentGraph().addSNode(st);
					if (sDocument.getSDocumentGraph().getSLayerByName("annotations").size() == 0){
						SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
						annoLayer.setSName("annotations");
						sDocument.getSDocumentGraph().addSLayer(annoLayer);
					}
					SLayer layer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
					layer.getSNodes().add(st);
					STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
					sTextRel.setSToken(st);
					sTextRel.setSTextualDS(sDocument.getSDocumentGraph().getSTextualDSs().get(0));
					sTextRel.setSStart(et.getBeginChar());
					sTextRel.setSEnd(et.getEndChar());
					sDocument.getSDocumentGraph().addSRelation(sTextRel);
				}
				if (str != null){
					st = str.getSToken();
				}
				sSpanRel.setSToken(st);
	//			sDocument.getSDocumentGraph().addSRelation(sSpanRel);
			}
			
			// TODO this does not seem to work
			boolean find = false;
			for (SSpan sp : sDocument.getSDocumentGraph().getSSpans()){
				if (sSpan.equals(sp)){
					sSpan = sp;
					find = true;
					break;
				}
			}
			
			if (find == true){
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				//setting the name of the annotation
				sAnno.setSName(name);
				//setting the value of the annotation
				sAnno.setSValue(value.trim());
				//adding the annotation to the placeholder span
				sSpan.addSAnnotation(sAnno);
			}
			
			if (find == false){
				sSpan= null;
				sSpanRel= null;
				sSpan= SaltFactory.eINSTANCE.createSSpan();
				sDocument.getSDocumentGraph().addSNode(sSpan);
				for (int i= 0; i< es.size(); i++)
				{
					sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
					sSpanRel.setSSpan(sSpan);
					ElanToken et = es.getElanToken(i);
					SToken st = getSTextualRelation(sDocument, et).getSToken();
					sSpanRel.setSToken(st);
					sDocument.getSDocumentGraph().addSRelation(sSpanRel);
				}
				
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				//setting the name of the annotation
				sAnno.setSName(name);
				//setting the value of the annotation
				sAnno.setSValue(value.trim());
				//adding the annotation to the placeholder span
				sSpan.addSAnnotation(sAnno);
			}
		}
	}

	/**
	 * the elan token in the input is added (or created) to the salt document, with an annotation that consists of the tier name and the value
	 * @param sDocument salt document to which this token is added
	 * @param et the elan token
	 * @param curTierName the name of the annotation to be added
	 * @param curValue the value of the annotation to be added
	 */
	private static void setSaltToken(SDocument sDocument, ElanToken et, String curTierName, String curValue) {
		STextualRelation curTextRel = getSTextualRelation(sDocument, et);
		String filename = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		ElanWrapper ew = new ElanWrapper(filename);
		SAnnotation sAnno = null;
		if (curTextRel != null){
			sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(curTierName);
			int startChar = curTextRel.getSStart();
			int stopChar = curTextRel.getSEnd();
			String check = curTextRel.getSTextualDS().getSText().substring(startChar, stopChar);
			String value = ew.getValueInTier(curTierName, et.getBeginTime(), et.getEndTime());
			if (!check.equals(et.getTok())){
				System.out.println("error: " + check + " != " + et.getTok() + " at time " + et.getBeginTime());
			}
			if (value != null){
				sAnno.setSValue(value.trim());
				sAnno.setSNS("elan");
				curTextRel.getSToken().addSAnnotation(sAnno);
			}
		}
		if (curTextRel == null){
			STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
			//as a means to group elements, layers (SLayer) can be used. here, a layer
			//named "morphology" is created and the tokens will be added to it
			SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
			annoLayer.setSName("annotations");
			sDocument.getSDocumentGraph().addSLayer(annoLayer);
			
			SToken sToken= SaltFactory.eINSTANCE.createSToken();
			sDocument.getSDocumentGraph().addSNode(sToken);
			SLayer layer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
			layer.getSNodes().add(sToken);
			STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
			sTextRel.setSToken(sToken);
			sTextRel.setSTextualDS(sTextualDS);
			sTextRel.setSStart(et.getBeginChar());
			sTextRel.setSEnd(et.getEndChar());
			sDocument.getSDocumentGraph().addSRelation(sTextRel);
			
			sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(curTierName);
			sAnno.setSValue(curValue.trim());
			sAnno.setSNS("elan");
			sTextRel.getSToken().addSAnnotation(sAnno);
		}
	}
}