package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
	public static final ArrayList<String> SEGMENTATION_TIERNAMES= new ArrayList<String>();
	public static final ArrayList<String> IGNORE_TIERNAMES= new ArrayList<String>();
	
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
		// set the segmentation tiers
		SEGMENTATION_TIERNAMES.add("segm");
		SEGMENTATION_TIERNAMES.add("txt");
		SEGMENTATION_TIERNAMES.add("character");
		
		// which tiers from elan should be ignored
		IGNORE_TIERNAMES.add("segm");
		IGNORE_TIERNAMES.add("txt");
		IGNORE_TIERNAMES.add("character");
		IGNORE_TIERNAMES.add("vergleich");
				
		// set the elan document
		// TODO is this the nicest way of getting the elan path?
		String fname = sDocument.getSMetaAnnotation("elan::origFile").getValueString();
		System.out.println("filename: " +fname);
		this.elan = new TranscriptionImpl(fname);

		// create the primary text
		createPrimaryData(sDocument);

		// goes through the elan document, and makes all the elan tiers into salt tiers
		traverseElanDocument(sDocument);
		
		// reset the variables
		SEGMENTATION_TIERNAMES.removeAll(SEGMENTATION_TIERNAMES);
		IGNORE_TIERNAMES.removeAll(IGNORE_TIERNAMES);
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
		
		// set the segments for the segmentation tiers
		createSegmentationForMainTiers(SEGMENTATION_TIERNAMES);
		
		// go through the elan document and add annotations
		addAnnotations(IGNORE_TIERNAMES);
	}
	
	/**
	 * function to go through elan document, and add the elan annos to salt tokens and spans
	 */
	public void addAnnotations(ArrayList<String> maintiers){
		
		// fetch the primary text
		STextualDS sTextualDS = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(0);
		
		// go through the tiers in elan
		for (Object obj : this.getElanModel().getTiers()){
			TierImpl tier = (TierImpl) obj;
			if (!maintiers.contains(tier.getName())){
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
	
	// TODO this can probably be captured by STimeline?
	protected Map<Long,Integer> time2char = new HashMap<Long,Integer>();
	protected Map<Integer,Long> char2time = new HashMap<Integer,Long>();
	
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
		
		// set the tokens for the minimal Tier
		TierImpl smallestTier = (TierImpl) this.getElanModel().getTierWithId(minimalTierName);
		STextualDS primaryText = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
	
		// because we need to calculate the positions of the tokens in the primary text, we need these two things
		String primtextchangeable = primaryText.getSText();
		int offset = 0;
		
		// go through the annotations of the tier with the smallest subdivision
		ArrayList<Integer> startStopValues = new ArrayList<Integer>();
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
        	if (value.length() == 0 & (endTime - beginTime) != 0){
        		// kind of error: if the value of the annotation is zero, but the endtime minus begintime is not zero, then something is wrong
        		// TODO turn this into an error, and actually it will not occur anymore with STimeline
        		System.out.println("at " + beginTime + " there is something wrong!");
        	}
        	
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
        	
        	SToken nt = null;
        	// check if this segment has a starting anno somewhere in the elan model
        	boolean endToken = false;
        	for (Tier tierabstr : (Collection<Tier>) elan.getTiers()){
        		TierImpl tier = (TierImpl) tierabstr;
        		Annotation curAnno = tier.getAnnotationAtTime(beginTime);
        		if (curAnno != null & !tier.getName().equals(minimalTierName)){
        			if (curAnno.getEndTimeBoundary() == endTime){
        				endToken = true;
        			}
        		}
        	}
        	boolean startToken = false;
        	for (Tier tierabstr : (Collection<Tier>) elan.getTiers()){
        		TierImpl tier = (TierImpl) tierabstr;
        		Annotation curAnno = tier.getAnnotationAtTime(beginTime);
        		if (curAnno != null & !tier.getName().equals(minimalTierName)){
        			if (curAnno.getBeginTimeBoundary() == beginTime){
        				startToken = true;
        			}
        		}
        	}
        	// if an anno ends at this segment, but no anno starts here, then that means that the previous and current segments have to form a token
        	if (endToken == true & startToken == false){
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        		nt = sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		startStopValues.removeAll(startStopValues);
        	}
        	
        	// if an anno starts here, but nothing ends, then that means that the previous segments have to form a token, and that the current starts a new list
        	if (endToken == false & startToken == true){
        		if (startStopValues.size() > 0){
        			sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		}
        		startStopValues.removeAll(startStopValues);
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        	}
        	
        	// if an anno starts and stops here, then the previous segments become a token, the current segments becomes a token, and the list is resetted
        	if (endToken == true & startToken == true){
        		if (startStopValues.size() > 0){
        			sDocument.getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
        		}
        		sDocument.getSDocumentGraph().createSToken(primaryText, corstart, corstop);
        		startStopValues.removeAll(startStopValues);
        	}
        	
        	// if no anno ends here, then this segment can be safely added to the list without further action
        	if (endToken == false & startToken == false){
        		startStopValues.add(corstart);
        		startStopValues.add(corstop);
        	}        	
		}
			
		// now make arching spans for the other maintiers
		maintiers.remove(minimalTierName);
		for (String tiername : maintiers){
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tiername);
			System.out.println("making spans for maintier: " + tier.getName());
			
			// and go through the individual annotations
			for (Object segObj : tier.getAnnotations()){
				Annotation anno = (Annotation) segObj;
				String value = anno.getValue();
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
			        // and add an annotation
			        // TODO when adding the annotation, pepper gets into a loop, when no annotation, error is produced without consequences
			        newSpan.createSAnnotation(NAMESPACE_ELAN, tiername, value);

			        // TODO if orderrelation is to be handled within this module, then add it here

			}
		}
	}
}