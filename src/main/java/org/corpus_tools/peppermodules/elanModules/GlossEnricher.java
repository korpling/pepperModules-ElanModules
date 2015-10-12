/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.elanModules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import org.corpus_tools.peppermodules.elanModules.playground.salt.elan2salt.ElanImporterMain;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimeline;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimelineRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

public class GlossEnricher {

	protected SDocument curSDoc;
	protected TranscriptionImpl curElan;
	protected Map<Long, Integer> time2char = new HashMap<Long, Integer>(); // solve
																			// this
																			// by
																			// using
																			// STimeline?
	protected Map<Integer, Long> char2time = new HashMap<Integer, Long>(); // solve
																			// this
																			// by
																			// using
																			// STimeline?
	protected String MINIMAL_SEGMENTATION_TIER_NAME = null;
	protected ElanImporterProperties curProps;
	protected ArrayList<SSpan> glosspans = new ArrayList<SSpan>();
	protected ArrayList<SToken> glosstokens = new ArrayList<SToken>();

	public ElanImporterProperties getProps() {
		return curProps;
	}

	public void setProps(ElanImporterProperties props) {
		this.curProps = props;
	}

	public Map<Long, Integer> getTime2Char() {
		return (time2char);
	}

	public Map<Integer, Long> getChar2Time() {
		return (char2time);
	}

	public ArrayList<SSpan> getGlossSpans() {
		return glosspans;
	}

	public void addToGlossTokens(SToken newToken) {
		glosstokens.add(newToken);
	}

	public void addToGlossSpans(SSpan newSpan) {
		glosspans.add(newSpan);
	}

	public ArrayList<SToken> getGlossTokens() {
		return glosstokens;
	}

	public TranscriptionImpl getElanModel() {
		return curElan;
	}

	public void setElanModel(TranscriptionImpl elanModel) {
		this.curElan = elanModel;
	}

	public SDocument getSDocument() {
		return this.curSDoc;
	}

	public void setSDocument(SDocument sDoc) {
		this.curSDoc = sDoc;
	}

	public STimeline getSTimeline() {
		return curSDoc.getSDocumentGraph().getSTimeline();
	}

	public GlossEnricher(SDocument sDoc, TranscriptionImpl glossElanModel, EList<STimelineRelation> availableTimelineRelations, int posPerToken, ElanImporterProperties props) {
		setSDocument(sDoc);
		setElanModel(glossElanModel);
		setProps(props);
		createPrimaryData(this.getSDocument());
		makeSTokensForGloss(this.getElanModel(), availableTimelineRelations, posPerToken);
		addAnnotations();
	}

	public SDocument getEnrichedDocument() {
		return this.curSDoc;
	}

	/**
	 * Creates a {@link STextualDS} object containing the primary text
	 * {@link ElanImporterMain#PRIMARY_TEXT} and adds the object to the
	 * {@link SDocumentGraph} being contained by the given {@link SDocument}
	 * object.
	 * 
	 * @param sDocument
	 *            the document, to which the created {@link STextualDS} object
	 *            will be added
	 */
	public void createPrimaryData(SDocument sDocument) {
		if (sDocument == null)
			throw new PepperModuleException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph() == null)
			throw new PepperModuleException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		{// creating the primary text
			TierImpl primtexttier = (TierImpl) this.getElanModel().getTierWithId(this.getProps().getPrimTextTierName());
			StringBuffer primText = new StringBuffer();
			for (Object obj : primtexttier.getAnnotations()) {
				AbstractAnnotation charAnno = (AbstractAnnotation) obj;
				// TODO assumption, the value of the anno is exactly is it is
				// supposed to be (so also with spaces and so), so that
				// everything can just be concatenated
				primText.append(charAnno.getValue());
			}
			sTextualDS = SaltFactory.eINSTANCE.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a
			// string
			sTextualDS.setSText(primText.toString());
			// adding the text to the document-graph
			this.getSDocument().getSDocumentGraph().addSNode(sTextualDS);
		}// creating the primary text
	}

	/**
	 * function to go through the elan maintiers and set salt tokens for the
	 * segmentations in these tiers
	 * 
	 * @param maintiers
	 *            the main tiers in elan, for which you want to create salt
	 *            segmentations
	 */
	@SuppressWarnings("unchecked")
	public void makeSTokensForGloss(TranscriptionImpl glossElan, EList<STimelineRelation> availableTimelineRelations, int posPerToken) {
		// timelinerelationstarts and ends init
		int startPos = availableTimelineRelations.get(0).getSStart();
		int endPos = availableTimelineRelations.get(posPerToken - 1).getSEnd();

		// find the tier with the smallest segmentation for the tokens
		int l = -1;
		String minimalTierName = null;
		for (Object tierobj : this.getElanModel().getTiers()) {
			TierImpl tier = (TierImpl) tierobj;
			if (tier.getNumberOfAnnotations() > l) {
				l = tier.getNumberOfAnnotations();
				minimalTierName = tier.getName();
				MINIMAL_SEGMENTATION_TIER_NAME = minimalTierName;
			}
		}
		// set the tokens for the minimal Tier
		TierImpl smallestTier = (TierImpl) this.getElanModel().getTierWithId(minimalTierName);
		int amountOfTextualDSs = this.getSDocument().getSDocumentGraph().getSTextualDSs().size();
		STextualDS primaryText = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(amountOfTextualDSs - 1);

		// because we need to calculate the positions of the tokens in the
		// primary text, we need these two things
		String primtextchangeable = primaryText.getSText();
		int offset = 0;

		// go through the annotations of the tier with the smallest subdivision
		ArrayList<Integer> startStopValues = new ArrayList<Integer>();
		for (Object annoObj : smallestTier.getAnnotations()) {
			Annotation anno = (Annotation) annoObj;
			String name = smallestTier.getName();
			String value = anno.getValue();

			// get the begin and end time
			long beginTime = anno.getBeginTimeBoundary();
			long endTime = anno.getEndTimeBoundary();

			// the start value is the position of value in primtextchangeable
			int start = primtextchangeable.indexOf(value);
			if (start < 0) {
				throw new PepperModuleException("token was not found in primarytext: (" + name + ", " + value + ") (primtext:" + primtextchangeable + ")");
			}

			// the stop value is the start value plus the length of the value
			int stop = start + value.length();

			// but because we cut something of the primary text (the amount in
			// offset) we have to add this
			int corstart = offset + start;
			int corstop = offset + stop;
			// we keep a map of beginTimes and beginChars, and of endTimes and
			// endChars
			if (!this.getTime2Char().containsKey(beginTime)) {
				this.getTime2Char().put(beginTime, corstart);
			}
			if (!this.getTime2Char().containsKey(endTime)) {
				this.getTime2Char().put(endTime, corstop);
			}

			if (!this.getChar2Time().containsKey(corstart)) {
				this.getChar2Time().put(corstart, beginTime);
			}
			if (!this.getChar2Time().containsKey(corstop)) {
				this.getChar2Time().put(corstop, endTime);
			}

			// update the offset and primary text
			offset = offset + stop;
			primtextchangeable = primtextchangeable.substring(stop);

			SToken newToken = null;
			// check if this segment has a starting anno somewhere in the elan
			// model
			boolean endToken = false;
			for (Tier tierabstr : (Collection<Tier>) this.getElanModel().getTiers()) {
				TierImpl tier = (TierImpl) tierabstr;
				Annotation curAnno = tier.getAnnotationAtTime(beginTime);
				if (curAnno != null & !tier.getName().equals(minimalTierName) & !this.getProps().getIgnoreTierNames().contains(tier.getName())) {
					if (curAnno.getEndTimeBoundary() == endTime) {
						endToken = true;
					}
				}
			}
			boolean startToken = false;
			for (Tier tierabstr : (Collection<Tier>) this.getElanModel().getTiers()) {
				TierImpl tier = (TierImpl) tierabstr;
				Annotation curAnno = tier.getAnnotationAtTime(beginTime);
				if (curAnno != null & !tier.getName().equals(minimalTierName) & !this.getProps().getIgnoreTierNames().contains(tier.getName())) {
					if (curAnno.getBeginTimeBoundary() == beginTime) {
						startToken = true;
					}
				}
			}

			// if an anno ends at this segment, but no anno starts here, then
			// that means that the previous and current segments have to form a
			// token
			if (endToken == true & startToken == false) {
				startStopValues.add(corstart);
				startStopValues.add(corstop);
				newToken = this.getSDocument().getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
				startStopValues.removeAll(startStopValues);
			}

			// if an anno starts here, but nothing ends, then that means that
			// the previous segments have to form a token, and that the current
			// starts a new list
			if (endToken == false & startToken == true) {
				if (startStopValues.size() > 0) {
					newToken = this.getSDocument().getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
				}
				startStopValues.removeAll(startStopValues);
				startStopValues.add(corstart);
				startStopValues.add(corstop);
			}

			// if an anno starts and stops here, then the previous segments
			// become a token, the current segments becomes a token, and the
			// list is resetted
			if (endToken == true & startToken == true) {
				if (startStopValues.size() > 0) {
					newToken = this.getSDocument().getSDocumentGraph().createSToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
				}
				newToken = this.getSDocument().getSDocumentGraph().createSToken(primaryText, corstart, corstop);
				startStopValues.removeAll(startStopValues);
			}

			// if no anno ends here, then this segment can be safely added to
			// the list without further action
			if (endToken == false & startToken == false) {
				startStopValues.add(corstart);
				startStopValues.add(corstop);
			}

			// add token to timeline and to glosstokens
			if (newToken != null) {
				addTokenToTimeline(newToken, startPos, endPos); // adjust
																// startPos and
																// endPos to
																// match tokens
																// in main and
																// gloss
				addToGlossTokens(newToken);
				startPos = startPos + 1;
				endPos = endPos + posPerToken;
			}
		}

		// now make arching spans for the other maintiers
		Collection<String> segtiers = this.getProps().getSegmentationTierNames();
		for (String tiername : segtiers) {
			TierImpl tier = (TierImpl) glossElan.getTierWithId(tiername);
			// and go through the individual annotations
			for (Object segObj : tier.getAnnotations()) {
				Annotation anno = (Annotation) segObj;
				long beginTime = anno.getBeginTimeBoundary();
				long endTime = anno.getEndTimeBoundary();

				// grab everything you can get about this anno
				// get the positions in the primary text
				int beginChar = -1;
				int endChar = -1;
				SSpan newSpan = null;
				try {
					beginChar = this.getTime2Char().get(beginTime);
					endChar = this.getTime2Char().get(endTime);

					// create a sequence that we can use to search for a related
					// token
					SDataSourceSequence sequence = null;
					sequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
					sequence.setSSequentialDS(primaryText);
					sequence.setSStart((int) beginChar);
					sequence.setSEnd((int) endChar);

					// find the relevant tokens
					EList<SToken> sNewTokens = null;
					sNewTokens = this.getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
					// create the span
					newSpan = this.getSDocument().getSDocumentGraph().createSSpan(sNewTokens);
					newSpan.createSAnnotation("gloss", tiername, anno.getValue());
					// add span to glossspans
					addToGlossSpans(newSpan);

				} catch (Exception e) {
					throw new PepperModuleException("something wrong at " + beginTime + " up to " + endTime);
				}
			}
		}
	}

	private void addTokenToTimeline(SToken newToken, int startPos, int endPos) {
		STimelineRelation sTimeRel = SaltFactory.eINSTANCE.createSTimelineRelation();
		sTimeRel.setSTimeline(this.getSTimeline());
		sTimeRel.setSToken(newToken);
		sTimeRel.setSStart(startPos);
		sTimeRel.setSEnd(endPos);
		this.getSDocument().getSDocumentGraph().addSRelation(sTimeRel);
	}

	/**
	 * function to go through elan document, and add the elan annos to salt
	 * tokens and spans
	 */
	public void addAnnotations() {

		// fetch the primary text
		int amountOfTextualDSs = this.getSDocument().getSDocumentGraph().getSTextualDSs().size();
		STextualDS sTextualDS = this.getSDocument().getSDocumentGraph().getSTextualDSs().get(amountOfTextualDSs - 1);

		// go through the tiers in elan
		for (Object obj : this.getElanModel().getTiers()) {
			TierImpl tier = (TierImpl) obj;
			if (!this.getProps().getSegmentationTierNames().contains(tier.getName()) & !this.getProps().getIgnoreTierNames().contains(tier.getName())) { // we
																																							// do
																																							// not
																																							// want
																																							// to
																																							// make
																																							// annotations
																																							// to
																																							// the
																																							// tokens
				// and go through the individual annotations
				int lastSpanIndex = 0; // variable to speed up some checks below
				for (Object annoObj : tier.getAnnotations()) {
					Annotation anno = (Annotation) annoObj;
					String value = anno.getValue().trim();
					long beginTime = anno.getBeginTimeBoundary();
					long endTime = anno.getEndTimeBoundary();

					// get the positions in the primary text
					int beginChar = 0;
					int endChar = 0;
					try {
						beginChar = this.getTime2Char().get(beginTime);
						endChar = this.getTime2Char().get(endTime);
					} catch (Exception ex) {
						throw new PepperModuleException("something wrong at " + beginTime + " up to " + endTime);
					}

					// if there is something interesting in the value, grab
					// everything you can get about this anno
					if (!value.isEmpty()) {
						// create a sequence that we can use to search for a
						// related span
						SDataSourceSequence sequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
						sequence.setSSequentialDS(sTextualDS);
						sequence.setSStart((int) beginChar);
						sequence.setSEnd((int) endChar);

						// this span will receive the span which will be
						// annotated
						SSpan sSpan = null;

						// Let's see if there are already some spans in the
						// sDocument that fit the bill
						ArrayList<SSpan> sSpansInSDoc = getGlossSpans();
						for (int i = lastSpanIndex; i < sSpansInSDoc.size(); i++) { // start
																					// at
																					// lastspanIndex,
																					// because
																					// previous
																					// spans
																					// are
																					// not
																					// possible
							// init the current span
							SSpan sp = sSpansInSDoc.get(i);

							// find the related DSSequence
							EList<STYPE_NAME> rels = new BasicEList<STYPE_NAME>();
							rels.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
							EList<SDataSourceSequence> sequences = this.getSDocument().getSDocumentGraph().getOverlappedDSSequences(sp, rels);

							// grab the primtext part with the elan anno start
							// and end info
							String checkPrimAnno = sTextualDS.getSText().substring(beginChar, endChar).trim();
							// grab the primtext part with the dssequence start
							// and end info
							String checkPrimSeq = sTextualDS.getSText().substring(sequences.get(0).getSStart(), sequences.get(0).getSEnd()).trim();
							int startSeq = sequences.get(0).getSStart();
							int endSeq = sequences.get(0).getSEnd();

							// check to see if this is the right span...
							if (checkPrimAnno.equals(checkPrimSeq)) {
								if (startSeq == beginChar & endSeq == endChar) {
									// add check for layer here too!
									sSpan = sp;
									lastSpanIndex = i; // so that the next
														// through this tier is
														// quicker.
									break;
								}
							}
						}

						if (sSpan != null) {
							sSpan.createSAnnotation("gloss", tier.getName(), value);
						}
						// ok, last chance, perhaps there was no span yet, so we
						// have to create one
						if (sSpan == null) {
							ArrayList<SToken> sNewTokens = getGlossTokens();
							EList<SToken> sNewTokensEList = new BasicEList<SToken>();
							sNewTokensEList.addAll(sNewTokens);
							if (sNewTokens.size() > 0) {
								EList<STYPE_NAME> rels = new BasicEList<STYPE_NAME>();
								rels.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);

								int firstTokenStart = this.getSDocument().getSDocumentGraph().getOverlappedDSSequences(sNewTokens.get(0), rels).get(0).getSStart();
								int lastTokenEnd = this.getSDocument().getSDocumentGraph().getOverlappedDSSequences(sNewTokens.get(sNewTokens.size() - 1), rels).get(0).getSEnd();
								if (firstTokenStart == beginChar & lastTokenEnd == endChar) {
									SSpan newSpan = this.getSDocument().getSDocumentGraph().createSSpan(sNewTokensEList);
									newSpan.createSAnnotation("gloss", tier.getName(), value);
								}
							}
						}
					}
				}
			}
		}
	}
}
