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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SOrderRelation;
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
		// TODO is this the nicest way of getting the elan path?
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
			// TODO properties
			TierImpl primtexttier = (TierImpl) elan.getTierWithId("tok");
			StringBuffer primText = new StringBuffer();
			for (Object obj : primtexttier.getAnnotations()){
				AbstractAnnotation charAnno = (AbstractAnnotation) obj;
				primText.append(charAnno.getValue().trim());
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
	 * function to go through the elan document and create the mapped salt document
	 * @param sDocument from the meta annotation in the salt document, the actual elan file is retrieved.
	 */
	public void traverseElanDocument(SDocument sDocument){
		ArrayList<String> maintiers = new ArrayList<String>();
		// TODO properties
		maintiers.add("tok");
		maintiers.add("character");
		maintiers.add("txt");
		// set the tokens from the maintiers
		createSegmentationForMainTiers(maintiers);
		// go through the elan document and add annotations
		addAnnotations();
	}
	
	
	public static final String NAMESPACE_ELAN="elan";
	
	/**
	 * function to go through elan document, and add the elan annos to salt tokens and spans
	 */
	public void addAnnotations(){
		// fetch the primary text
		STextualDS sTextualDS = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(0);
		// go through the tiers in elan
		for (Object obj : this.getElanModel().getTiers()){
			TierImpl tier = (TierImpl) obj;
			System.out.println(tier.getName());
			// and go through the individual annotations
			for (Object annoObj : tier.getAnnotations()){
				Annotation anno = (Annotation) annoObj;
				String value = anno.getValue().trim();
				long beginTime = anno.getBeginTimeBoundary();
				long endTime = anno.getEndTimeBoundary();
				// get the positions in the primary text
				int beginChar = time2char.get(beginTime);
				int endChar = time2char.get(endTime);
				// if there is something interesting in the value, grab everything you can get about this anno
				if (!value.isEmpty()){
					try{
						// create a sequence that we can use to search for a related token
				        SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
				        sequence.setSSequentialDS(sTextualDS);
				        sequence.setSStart((int) beginChar);
				        sequence.setSEnd((int) endChar);
				        
				        // use this sequence to find the related tokens
				        EList<SToken> sTokens = this.getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
				        System.out.println("found tokens: " + sTokens);
				        
				        // initialize the wanted token
				        SToken sToken = null;
				        // go through the retrieved tokens, and only keep the one whose beginning and ending are exact matches
				        for (SToken st : sTokens){
				        	// weird salt way of getting the textrel of an stoken
				        	// TODO make this a function
				        	EList<STYPE_NAME> relTypes= new BasicEList<STYPE_NAME>();
				            relTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
				            EList<SDataSourceSequence> sTextRels = sDocument.getSDocumentGraph().getOverlappedDSSequences(st, relTypes);
				            // but here you can grab the send and sstart (assuming there is one hit)
				            int sStart = sTextRels.get(0).getSStart();
				            int sStop = sTextRels.get(0).getSEnd();
				        
				            // do the test
				            if (sStart == beginChar & sStop == endChar){
				            	sToken = st;
				            	break;
				            }
				        }
	
				        // if we found a matching SToken, then add the annotation
				        if (sToken != null){
				        	System.out.println("token found");
				        	sToken.createSAnnotation(NAMESPACE_ELAN, tier.getName(), value.trim());	
				        }
				        
				        // if we did not find a matching SToken, then it is perhaps a span?
				        if (sToken == null){
				        	// Let's see if there are already some spans in the sDocument that fit the bill
					        EList<SSpan> sSpans = sDocument.getSDocumentGraph().getSSpanBySequence(sequence);
					        // initialize the wanted token
					        SSpan sSpan = null;
					        // go through the retrieved spans, and only keep the one whose beginning and ending are exact matches
					        for (SSpan sp : sSpans){
					        	// weird salt way of getting the spanning relation of an sspan
					        	// TODO make this a function, so we can use it above, too
					        	EList<STYPE_NAME> relTypes= new BasicEList<STYPE_NAME>();
					            relTypes.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
					            EList<SDataSourceSequence> sSpanRels = sDocument.getSDocumentGraph().getOverlappedDSSequences(sp, relTypes);
					            // but here you can grab the send and sstart!
					            int sStart = sSpanRels.get(0).getSStart();
					            int sStop = sSpanRels.get(0).getSEnd();
					            // do the test
					            if (sStart == beginChar & sStop == endChar){
					            	sSpan = sp;
					            	break;
					            }
					        }
					        if (sSpan != null){
					        	System.out.println("span found");
					        	sSpan.createSAnnotation(NAMESPACE_ELAN, tier.getName(), value);	
					        }
					        // ok, last chance, perhaps there was no span yet, so we have to create one
					        if (sSpan == null){
					        	System.out.println("Creating new span");
						        EList<SToken> sNewTokens = this.getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
						        // TODO test if the beginning and end of the sNewTokens fits the elan annotation begin and ending, use the function
						        SSpan newSpan = sDocument.getSDocumentGraph().createSSpan(sNewTokens);
						        newSpan.createSAnnotation(NAMESPACE_ELAN, tier.getName(), value);
					        }
				        }
					}catch (NullPointerException noppes){
						throw new ELANImporterException("This token could not be annotated: " + tier.getName() + ", " + value + ", " + beginTime);
					}
				} 
			}
		}
	}
	
	protected Map<Long,Integer> time2char = new HashMap();
	
	/**
	 * function to go through the elan maintiers and set salt tokens for the segmentations in these tiers
	 * @param maintiers the main tiers in elan, for which you want to create salt segmentations
	 */
	public void createSegmentationForMainTiers(ArrayList<String> maintiers){
		// find the tier with the smallest segmentation for the tokens
		int l = -1;
		String minimalTierName = null;
		for (String tierName : maintiers){
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tierName);
			if (tier.getAnnotations().size() > l){
				l = tier.getAnnotations().size();
				minimalTierName = tierName;
			}
		}
		System.out.println("smallest tier is " + minimalTierName);
		// remove this tier from the maintiers, because it now has become the tokentier
		maintiers.remove(minimalTierName);

		// set the tokens for the minimal Tier
		TierImpl smallestTier = (TierImpl) this.getElanModel().getTierWithId(minimalTierName);
		STextualDS primaryText = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
		// because we need to calculate the positions of the tokens in the primary text, we need these two things
		String primtextchangeable = primaryText.getSText();
		int offset = 0;
		// go through the annotations if it is a maintier
		SToken lastSToken= null;
		for (Object annoObj : smallestTier.getAnnotations()){
			Annotation anno = (Annotation) annoObj;
			String name = smallestTier.getName();
			String value = anno.getValue().trim();
	
			// get the begin and end time
			long beginTime = anno.getBeginTimeBoundary();
			long endTime = anno.getEndTimeBoundary();
			
			// the start value is the position of value in primtextchangeable
			int start =  primtextchangeable.indexOf(value);
			// this start value should not be larger than 3 (small number), because otherwise there is something wrong
			if (start < 0 | start > 2){
				throw new ELANImporterException("token was not found in primarytext: (" + name + ", " + value + ") (primtext:" + primtextchangeable + ")");
			}

			// the stop value is the start value plus the length of the value
        	int stop = start + value.length();
        	
        	// but because we cut something of the primary text (the amount in offset) we have to add this
        	int corstart = offset + start;
        	int corstop = offset + stop;
        	
        	// we keep a map of beginTimes and beginChars, and of endTimes and endChars
        	if (!time2char.containsKey(beginTime)){
        		time2char.put(beginTime, corstart);
        	}
        	if (!time2char.containsKey(endTime)){
        		time2char.put(endTime, corstop);
        	}
        	
        	// update the offset and primary text
        	offset = offset + stop;
        	primtextchangeable = primtextchangeable.substring(stop);
			
        	// create the token
        	SToken sToken = sDocument.getSDocumentGraph().createSToken(primaryText, corstart, corstop);
			
        	if (lastSToken!= null)
        	{// create SOrderRelation between current and last token (if exists)
        		SOrderRelation sOrderRel= SaltFactory.eINSTANCE.createSOrderRelation();
        		sOrderRel.setSource(lastSToken);
        		sOrderRel.setSTarget(sToken);
        		sOrderRel.addSType(name);
        		sDocument.getSDocumentGraph().addSRelation(sOrderRel);
        		lastSToken = sToken;
        	}// create SOrderRelation between current and last token (if exists)
        	
        	// add the token to the layer
        	// TODO properties, parameterize this?
        	SLayer curSLayer = sDocument.getSDocumentGraph().getSLayerByName("annotations").get(0);
			curSLayer.getSNodes().add(sToken);
		}
			
		// now make arching spans for the other maintiers
		for (String tiername : maintiers){
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tiername);
			System.out.println(tier.getName());
			// and go through the individual annotations
			SSpan lastSSpan = null;
			for (Object segObj : tier.getAnnotations()){
				Annotation anno = (Annotation) segObj;
				String value = anno.getValue().trim();
				long beginTime = anno.getBeginTimeBoundary();
				long endTime = anno.getEndTimeBoundary();
				// if there is something interesting in the value, grab everything you can get about this anno
				if (!value.isEmpty()){
						try{
							// get the positions in the primary text
							int beginChar = time2char.get(beginTime);
							int endChar = time2char.get(endTime);
							
							// create a sequence that we can use to search for a related token
					        SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
					        sequence.setSSequentialDS(primaryText);
					        sequence.setSStart((int) beginChar);
					        sequence.setSEnd((int) endChar);
					        
					        // find the relevant tokens
					        EList<SToken> sNewTokens = sDocument.getSDocumentGraph().getSTokensBySequence(sequence);
					        System.out.println(sNewTokens);
					        // TODO test if the beginning and end of the sNewTokens fits the elan annotation begin and ending, use the function
					        SSpan newSpan = sDocument.getSDocumentGraph().createSSpan(sNewTokens);
					        System.out.println("span created: " + newSpan);
				        	System.out.println("span found: " + sDocument.getSDocumentGraph().getSSpanBySequence(sequence));
					        
				        	if (lastSSpan!= null)
				        	{// create SOrderRelation between current and last token (if exists)
				        		SOrderRelation sOrderRel= SaltFactory.eINSTANCE.createSOrderRelation();
				        		sOrderRel.setSource(lastSSpan);
				        		sOrderRel.setSTarget(newSpan);
				        		sOrderRel.addSType(tier.getName());
				        		sDocument.getSDocumentGraph().addSRelation(sOrderRel);
				        		lastSSpan = newSpan;
				        	}// create SOrderRelation between current and last token (if exists)
				        	
				        }catch (NullPointerException noppes){
							throw new ELANImporterException("This token could not be annotated: " + tier.getName() + ", " + value + ", " + beginTime);
						}
				}
			}
		}
	}
}
