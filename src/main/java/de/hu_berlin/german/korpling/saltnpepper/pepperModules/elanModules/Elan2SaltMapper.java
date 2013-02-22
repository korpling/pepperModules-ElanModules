package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.exceptions.ELANImporterException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt.ElanImporterMain;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.helper.modules.SDocumentDataEnricher;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SOrderRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

/**
 * This class maps data coming from the ELAN model to a Salt model.
 * @author Florian Zipser
 *
 */
public class Elan2SaltMapper 
{
	// properties to be set, I guess
	public static final String NAMESPACE_ELAN="elan";
	public static final String PRIMARY_TEXT_TIER_NAME="character";
	public static final List<String> SEGMENTATION_TIERNAMES= Arrays.asList("character", "segm", "txt");
	public static final List<String> IGNORE_TIERNAMES= Arrays.asList("vergleich");
	public static final boolean addOrderRelation = new Boolean(false);

	// properties that I want to keep track of in the whole class, but that are not set initially
	protected Map<Long,Integer> time2char = new HashMap<Long,Integer>(); // solve this by using STimeline?
	protected Map<Integer,Long> char2time = new HashMap<Integer,Long>(); // solve this by using STimeline?
	protected String MINIMAL_SEGMENTATION_TIER_NAME = null;
	protected TranscriptionImpl elan = null;
	
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
	
	public TranscriptionImpl setElanModel(String fullFilename){
		return new TranscriptionImpl(fullFilename);
	}
	
	public TranscriptionImpl getElanModel(){
		return elan;
	}
	
	public String getMINIMAL_SEGMENTATION_TIER_NAME() {
		return this.MINIMAL_SEGMENTATION_TIER_NAME;
	}

	public void setMINIMAL_SEGMENTATION_TIER_NAME(
			String mINIMAL_SEGMENTATION_TIER_NAME) {
		this.MINIMAL_SEGMENTATION_TIER_NAME = mINIMAL_SEGMENTATION_TIER_NAME;
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
		// TODO is this the nicest way of getting the elan path? Probably we can handle this from the superclass with setElanModel
		String fname = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		System.out.println("working on " + fname);
		this.elan = new TranscriptionImpl(fname);

		// create the primary text
		createPrimaryData(sDocument);

		// goes through the elan document, and makes all the elan tiers into salt tiers
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
			TierImpl primtexttier = (TierImpl) elan.getTierWithId(PRIMARY_TEXT_TIER_NAME);
			StringBuffer primText = new StringBuffer();
			for (Object obj : primtexttier.getAnnotations()){
				AbstractAnnotation charAnno = (AbstractAnnotation) obj;
				// TODO assumption, the value of the anno is exactly is it is supposed to be (so also with spaces and so), so that everything can just be concatenated
				primText.append(charAnno.getValue());
			}
			sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a string
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
		
		// set the segments for the segmentation tiers
		createSegmentationForMainTiers(SEGMENTATION_TIERNAMES);
		
		// go through the elan document and add annotations
		addAnnotations(IGNORE_TIERNAMES);
	}
	
	/**
	 * function to go through elan document, and add the elan annos to salt tokens and spans
	 */
	public void addAnnotations(List<String> maintiers){
		
		// fetch the primary text
		STextualDS sTextualDS = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(0);
		
		// go through the tiers in elan
		for (Object obj : this.getElanModel().getTiers()){
			TierImpl tier = (TierImpl) obj;
			if (!tier.getName().equals(MINIMAL_SEGMENTATION_TIER_NAME) & !IGNORE_TIERNAMES.contains(tier.getName())){ // we do not want to make annotations to the tokens
				System.out.println(tier.getName());
		
				// and go through the individual annotations
				int lastSpanIndex = 0; // variable to speed up some checks below
				for (Object annoObj : tier.getAnnotations()){
					Annotation anno = (Annotation) annoObj;
					String value = anno.getValue().trim();
					long beginTime = anno.getBeginTimeBoundary();
					long endTime = anno.getEndTimeBoundary();
				
					// TODO this is perhaps better handled by STimeline?
					// get the positions in the primary text
					int beginChar = time2char.get(beginTime);
					int endChar = time2char.get(endTime);
					
					// if there is something interesting in the value, grab everything you can get about this anno
					if (!value.isEmpty()){
						// create a sequence that we can use to search for a related span
				        SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
				        sequence.setSSequentialDS(sTextualDS);
				        sequence.setSStart((int) beginChar);
				        sequence.setSEnd((int) endChar);
				        
				        // this span will receive the span which will be annotated
				        SSpan sSpan = null;

			        	// Let's see if there are already some spans in the sDocument that fit the bill
				        // TODO change this to the more efficient getSSpanBySequence method when Florian fixes the bug					    
				        // EList<SSpan> sSpans = sDocument.getSDocumentGraph().getSSpanBySequence(sequence);
			        	EList<SSpan> sSpansInSDoc = sDocument.getSDocumentGraph().getSSpans();
			        	for (int i = lastSpanIndex; i < sSpansInSDoc.size(); i++){ // start at lastspanIndex, because previous spans are not possible
			        		// init the current span
			        		SSpan sp = sSpansInSDoc.get(i);
			        		
			        		// find the related DSSequence
			        		EList<STYPE_NAME> rels= new BasicEList<STYPE_NAME>();
			        		rels.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
			        		EList<SDataSourceSequence> sequences= sDocument.getSDocumentGraph().getOverlappedDSSequences(sp, rels);
			        		
			        		// grab the primtext part with the elan anno start and end info
			        		String checkPrimAnno = sTextualDS.getSText().substring(beginChar, endChar).trim();
			        		// grab the primtext part with the dssequence start and end info
			        		String checkPrimSeq = sTextualDS.getSText().substring(sequences.get(0).getSStart(), sequences.get(0).getSEnd()).trim();
			        		int startSeq = sequences.get(0).getSStart();
			        		int endSeq = sequences.get(0).getSEnd();
			        		
			        		// check to see if this is the right span...
			        		if (checkPrimAnno.equals(checkPrimSeq)){
			        			if (startSeq == beginChar & endSeq == endChar){
			        				sSpan = sp;
				        			lastSpanIndex = i; // so that the next through this tier is quicker.
				        			break;
				        		}
			        		}
			        	}
			        	
				        if (sSpan != null){
				        	sSpan.createSAnnotation(NAMESPACE_ELAN, tier.getName(), value);	
				        }
				        // ok, last chance, perhaps there was no span yet, so we have to create one
				        if (sSpan == null){
					        EList<SToken> sNewTokens = this.getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
					        if (sNewTokens.size() > 0){
					        	EList<STYPE_NAME> rels= new BasicEList<STYPE_NAME>();
					        	rels.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
					        	
					        	int firstTokenStart = sDocument.getSDocumentGraph().getOverlappedDSSequences(sNewTokens.get(0), rels).get(0).getSStart();
					        	int lastTokenEnd = sDocument.getSDocumentGraph().getOverlappedDSSequences(sNewTokens.get(sNewTokens.size()-1), rels).get(0).getSEnd();
					        	if (firstTokenStart == beginChar & lastTokenEnd == endChar){
					        		SSpan newSpan = sDocument.getSDocumentGraph().createSSpan(sNewTokens);
					        		newSpan.createSAnnotation(NAMESPACE_ELAN, tier.getName(), value);
					        	}
					        }
				        }
					}
				}
			}
		}
	}
	
	/**
	 * function to go through the elan maintiers and set salt tokens for the segmentations in these tiers
	 * @param maintiers the main tiers in elan, for which you want to create salt segmentations
	 */
	public void createSegmentationForMainTiers(List<String> mtiers){
		
		// cast mtiers to ArrayList
		ArrayList<String> maintiers = new ArrayList<String>(mtiers);
		
		// find the tier with the smallest segmentation for the tokens
		int l = -1;
		String minimalTierName = null;
		for (String tierName : maintiers){
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tierName);
			if (tier.getAnnotations().size() > l){
				l = tier.getAnnotations().size();
				minimalTierName = tierName;
				MINIMAL_SEGMENTATION_TIER_NAME = minimalTierName;
			}
		}
		
		// set the tokens for the minimal Tier
		TierImpl smallestTier = (TierImpl) this.getElanModel().getTierWithId(minimalTierName);
		STextualDS primaryText = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
	
		// because we need to calculate the positions of the tokens in the primary text, we need these two things
		String primtextchangeable = primaryText.getSText();
		int offset = 0;
		
		// go through the annotations of the tier with the smallest subdivision
		ArrayList<Integer> startStopValues = new ArrayList<Integer>();
		SToken lastToken = null;
		for (Object annoObj : smallestTier.getAnnotations()){
			Annotation anno = (Annotation) annoObj;
			String name = smallestTier.getName();
			String value = anno.getValue();

			// get the begin and end time
			long beginTime = anno.getBeginTimeBoundary();
			long endTime = anno.getEndTimeBoundary();
			
			// the start value is the position of value in primtextchangeable
			int start =  primtextchangeable.indexOf(value);
			if (start < 0){
				throw new ELANImporterException("token was not found in primarytext: (" + name + ", " + value + ") (primtext:" + primtextchangeable + ")");
			}

			// the stop value is the start value plus the length of the value
			// TODO perhaps this is an unnecessary assumption that can be superseded by using STimeline?
        	int stop = start + value.length();
        	
        	// but because we cut something of the primary text (the amount in offset) we have to add this
       		int corstart = offset + start;
        	int corstop = offset + stop;
        	
        	// we keep a map of beginTimes and beginChars, and of endTimes and endChars
        	// TODO this is handled by STimeline?
        	if (!time2char.containsKey(beginTime)){
        		time2char.put(beginTime, corstart);
        	}
        	if (!time2char.containsKey(endTime)){
        		time2char.put(endTime, corstop);
        	}

        	if (!char2time.containsKey(corstart)){
        		char2time.put(corstart, beginTime);
        	}
        	if (!char2time.containsKey(corstop)){
        		char2time.put(corstop, endTime);
        	}
        	
        	// update the offset and primary text
        	offset = offset + stop;
        	primtextchangeable = primtextchangeable.substring(stop);
        	
        	SToken newToken = null;
        	// check if this segment has a starting anno somewhere in the elan model
        	boolean endToken = false;
        	for (Tier tierabstr : (Collection<Tier>) elan.getTiers()){
        		TierImpl tier = (TierImpl) tierabstr;
        		Annotation curAnno = tier.getAnnotationAtTime(beginTime);
        		if (curAnno != null & !tier.getName().equals(minimalTierName) & !IGNORE_TIERNAMES.contains(tier.getName())){
        			if (curAnno.getEndTimeBoundary() == endTime){
        				endToken = true;
        			}
        		}
        	}
        	boolean startToken = false;
        	for (Tier tierabstr : (Collection<Tier>) elan.getTiers()){
        		TierImpl tier = (TierImpl) tierabstr;
        		Annotation curAnno = tier.getAnnotationAtTime(beginTime);
        		if (curAnno != null & !tier.getName().equals(minimalTierName) & !IGNORE_TIERNAMES.contains(tier.getName())){
        			if (curAnno.getBeginTimeBoundary() == beginTime){
        				startToken = true;
        			}
        		}
        	}
        	// if an anno ends at this segment, but no anno starts here, then that means that the previous and current segments have to form a token
        	if (endToken == true & startToken == false){
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        		newToken = sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		startStopValues.removeAll(startStopValues);
        	}
        	
        	// if an anno starts here, but nothing ends, then that means that the previous segments have to form a token, and that the current starts a new list
        	if (endToken == false & startToken == true){
        		if (startStopValues.size() > 0){
        			newToken = sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		}
        		startStopValues.removeAll(startStopValues);
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        	}
        	
        	// if an anno starts and stops here, then the previous segments become a token, the current segments becomes a token, and the list is resetted
        	if (endToken == true & startToken == true){
        		if (startStopValues.size() > 0){
        			newToken = sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		}
        		newToken = sDocument.getSDocumentGraph().createSToken(primaryText, corstart, corstop);
        		startStopValues.removeAll(startStopValues);
        	}
        	
        	// if no anno ends here, then this segment can be safely added to the list without further action
        	if (endToken == false & startToken == false){
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        	}        	
        	
        	if (addOrderRelation == true){
        		if (lastToken != null){
        			if (newToken != null){
        				SOrderRelation orderRelToken = SaltFactory.eINSTANCE.createSOrderRelation();
        				orderRelToken.setSSource(lastToken);
        				orderRelToken.setSTarget(newToken);
        				orderRelToken.addSType(minimalTierName);
        				sDocument.getSDocumentGraph().addSRelation(orderRelToken);
        			}
	        	}
    	        lastToken = newToken;
	        }
		}
			
		// now make arching spans for the other maintiers
		maintiers.remove(minimalTierName);
		SSpan lastSpan = null;
		for (String tiername : maintiers){
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tiername);
			System.out.println("making spans for maintier: " + tier.getName());
			
			// and go through the individual annotations
			for (Object segObj : tier.getAnnotations()){
				Annotation anno = (Annotation) segObj;
				long beginTime = anno.getBeginTimeBoundary();
				long endTime = anno.getEndTimeBoundary();
				
				// grab everything you can get about this anno
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
		        // create the span
		        SSpan newSpan = sDocument.getSDocumentGraph().createSSpan(sNewTokens);

		        // add the order relation
		        if (addOrderRelation == true){
		        	if (lastSpan != null){
		        		SOrderRelation orderRel = SaltFactory.eINSTANCE.createSOrderRelation();
		        		orderRel.setSSource(lastSpan);
		        		orderRel.setSTarget(newSpan);
		        		orderRel.addSType(tiername);
		        		sDocument.getSDocumentGraph().addSRelation(orderRel);
		        	}
		        	lastSpan = newSpan;
		        }
			}
		}
	}
}