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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.elanModules.playground.salt.elan2salt.ElanImporterMain;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps data coming from the ELAN model to a Salt model.
 * 
 * @author Florian Zipser
 * @author Tom Ruette
 */
public class Elan2SaltMapper extends PepperMapperImpl implements PepperMapper {
	private static final Logger logger = LoggerFactory.getLogger(Elan2SaltMapper.class);
	/** namespace of annotations */
	private String annoNS = null;
	/** folder of metadata files */
	private String mdDir = null;

	// variables that I want to keep track of in the whole class, but that are
	// not set initially
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
	protected TranscriptionImpl elan = null;

	/**
	 * returns the {@link PepperModuleProperties} as
	 * {@link ElanImporterProperties}
	 **/
	public ElanImporterProperties getProps() {
		return ((ElanImporterProperties) getProperties());
	}

	public Map<Long, Integer> getTime2Char() {
		return (time2char);
	}

	public Map<Integer, Long> getChar2Time() {
		return (char2time);
	}

	public void setElanModel(String fullFilename) {
		this.elan = new TranscriptionImpl(fullFilename);
	}

	public TranscriptionImpl getElanModel() {
		return elan;
	}

	public String getMINIMAL_SEGMENTATION_TIER_NAME() {
		return this.MINIMAL_SEGMENTATION_TIER_NAME;
	}

	public void setMINIMAL_SEGMENTATION_TIER_NAME(String mINIMAL_SEGMENTATION_TIER_NAME) {
		this.MINIMAL_SEGMENTATION_TIER_NAME = mINIMAL_SEGMENTATION_TIER_NAME;
	}

	public void setDocumentMetaAnnotation(String key, String value) {
		getDocument().createMetaAnnotation(null, key, value);
	}

	/**
	 * {@inheritDoc PepperMapper#setDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		// read properties
		annoNS = this.getProps().getAnnotationNamespace();
		mdDir = this.getProps().getMetadataFolderPath();
		// set the elan document
		setElanModel(this.getResourceURI().toFileString());
		if (getDocument().getDocumentGraph() == null)
			getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());

		// create the primary text
		createPrimaryData(getDocument());

		// goes through the elan document, and makes all the elan tiers into
		// salt tiers
		traverseElanDocument(getDocument());
		return (DOCUMENT_STATUS.COMPLETED);
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
			throw new PepperModuleException(this, "Cannot create example, because the given sDocument is empty.");
		if (sDocument.getDocumentGraph() == null)
			throw new PepperModuleException(this, "Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		{// creating the primary text
			TierImpl primtexttier = (TierImpl) this.getElanModel().getTierWithId(this.getProps().getPrimTextTierName());
			StringBuffer primText = new StringBuffer();
			if (primtexttier == null) {
				throw new PepperModuleException(this, "Cannot import data, no primary text tier was found. Please use customization property '" + ElanImporterProperties.PROP_PRIMARY_TEXT_TIER_NAME + "'. ");
			}
			for (Object obj : primtexttier.getAnnotations()) {
				AbstractAnnotation charAnno = (AbstractAnnotation) obj;
				// TODO assumption, the value of the anno is exactly is it is
				// supposed to be (so also with spaces and so), so that
				// everything can just be concatenated
				primText.append(charAnno.getValue());
			}
			sTextualDS = SaltFactory.createSTextualDS();
			// ew.getPrimaryText gets the text from the elan document as a
			// string
			sTextualDS.setText(primText.toString());
			// adding the text to the document-graph
			getDocument().getDocumentGraph().addNode(sTextualDS);
		}// creating the primary text
	}

	/**
	 * function to go through the elan document and create the mapped salt
	 * document
	 * 
	 * @param sDocument
	 *            from the meta annotation in the salt document, the actual elan
	 *            file is retrieved.
	 */
	public void traverseElanDocument(SDocument sDocument) {

		// set the segments for the segmentation tiers
		createSegmentationForMainTiers();

		// go through the elan document and add annotations
		addAnnotations();

		// if there are meta data in attribute value form in the metadata folder (set by property, default: ./meta), add them to
		// the document
		try {
			addMetaAnnotations();
		} catch (IOException e) {
			logger.error("An error occured reading the metadata. No metadata will be available.");
		}

		// add a linked elan file
		try {
			String filename = this.getResourceURI().toFileString().substring(this.getResourceURI().toFileString().lastIndexOf("/"));
			addLinkedElan(this.getProps().getLinkedFolder() + filename);
		} catch (Exception e) {

		}
	}

	/**
	 * function to add a linked elan file designed for DDD gloss corpora
	 */
	public void addLinkedElan(String pathToFile) throws IOException {
		// read in gloss file
		TranscriptionImpl gloss = new TranscriptionImpl(pathToFile);
		// 1. play around with the tokens so that the glosses will fit
		for (String glossid : AddLinkedElan.getGlossIDs(this.getElanModel())) {
			TranscriptionImpl glosspart = AddLinkedElan.getEafPartForGlossID(gloss, glossid);
			// establish how many tokens need to be added to the main file to
			// accomodate the gloss
			List<SToken> stokens = (List<SToken>) AddLinkedElan.getSTokensForGloss(getDocument(), glossid);
			int numberOfMainTokens = stokens.size();
			int numberOfGlossTokens = AddLinkedElan.calcTokensNeeded(AddLinkedElan.getEafPartForGlossID(glosspart, glossid), this.getProps());
			// if there are more gloss tokens than main tokens, insert tokens
			// after last token, grow the annotations, and shift the rest
			if (numberOfGlossTokens > numberOfMainTokens) {
				STextualDS curSTextualDS = getDocument().getDocumentGraph().getTextualDSs().get(0);
				int insertPos = AddLinkedElan.getStopFromSToken(stokens.get(stokens.size() - 1));
				List<String> placeholders = new ArrayList<String>();
				for (int i = 0; i < numberOfGlossTokens - numberOfMainTokens; i++) {
					placeholders.add("placeholder");
				}

				List<SToken> placeholderTokens = getDocument().getDocumentGraph().insertTokensAt(curSTextualDS, insertPos, placeholders, true);
				Collection<SSpan> spans = AddLinkedElan.getSpansContaintingToken(stokens.get(stokens.size() - 1));
				for (SSpan span : spans) {
					addSTokensToSpan(placeholderTokens, span);
				}
			}
		}

		// 2. assuming that there are at least enough tokens, initialize the
		// timeline, create a salt model for the gloss and reference it to the
		// main model
		getDocument().getDocumentGraph().sortTokenByText();
		getDocument().getDocumentGraph().createTimeline();

		for (String glossid : AddLinkedElan.getGlossIDs(this.getElanModel())) {
			TranscriptionImpl glosspart = AddLinkedElan.getEafPartForGlossID(gloss, glossid);

			// get the stokens (now they should include the added tokens!
			List<SToken> stokens = (List<SToken>) AddLinkedElan.getSTokensForGloss(getDocument(), glossid);

			// find the timelinerelations that can be used for the gloss
			// annotation
			List<STimelineRelation> timelineRelationsForGloss = new ArrayList<STimelineRelation>();
			for (STimelineRelation curTimelineRelation : getDocument().getDocumentGraph().getTimelineRelations()) {
				SToken curToken = curTimelineRelation.getSource();
				if (stokens.contains(curToken)) {
					timelineRelationsForGloss.add(curTimelineRelation);
				}
			}

			// now, reuse the methods for the main elan file, modified so that
			// they do not create segmentations
			// and they also immediately set the timeline properties
			// first, establish how many timelinerelations can be used per gloss
			// token
			int numberOfMainTokens = stokens.size();
			int numberOfGlossTokens = AddLinkedElan.calcTokensNeeded(AddLinkedElan.getEafPartForGlossID(glosspart, glossid), this.getProps());
			int posPerToken = (int) Math.floor((float) numberOfMainTokens / (float) numberOfGlossTokens);
			GlossEnricher ge = new GlossEnricher(getDocument(), glosspart, timelineRelationsForGloss, posPerToken, this.getProps());
			this.setDocument(ge.getEnrichedDocument());
		}
	}

	private void addSTokensToSpan(List<SToken> placeholderTokens, SSpan span) {
		SSpanningRelation spanRel = null;
		for (SToken sToken : placeholderTokens) {
			spanRel = SaltFactory.createSSpanningRelation();
			spanRel.setTarget(sToken);
			spanRel.setSource(span);
			span.getGraph().addRelation(spanRel);
		}
	}

	/**
	 * function that searches for attr/val meta annotations and adds them to the
	 * sdoc
	 * 
	 * @throws IOException
	 */
	public void addMetaAnnotations() throws IOException {
		String path = this.getResourceURI().toFileString();
		// get to the metadata
		String[] segments = path.split(File.separator);
		if (segments.length > 2) {
			String target = segments[segments.length - 2];
			path = path.replace(target, mdDir);
		}
		if (segments.length > 1) {
			String fname = segments[segments.length - 1];
			path = path.replace(fname, fname.split("_")[0]);
		}

		path = path + ".txt";
		path = path.replaceAll(".eaf", "");

		File metaFile = new File(path);
		if (!metaFile.exists()) {
			logger.warn("Cannot read meta data file '" + metaFile.getAbsolutePath() + "'.");
		} else {
			BufferedReader br = new BufferedReader(new FileReader(path));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					String attr = line.split("=")[0];
					String val = "NA";
					try {
						val = line.split("=")[1];
						if (val.equals("null")) {
							val = "NA";
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						continue;
					}
					getDocument().createMetaAnnotation(null, attr, val);
				}
			} finally {
				br.close();
			}
		}
	}

	/**
	 * function to go through elan document, and add the elan annos to salt
	 * tokens and spans
	 */
	public void addAnnotations() {

		// fetch the primary text
		int amountOfTextualDSs = getDocument().getDocumentGraph().getTextualDSs().size();
		STextualDS sTextualDS = getDocument().getDocumentGraph().getTextualDSs().get(amountOfTextualDSs - 1);

		// go through the tiers in elan
		for (Object obj : this.getElanModel().getTiers()) {
			TierImpl tier = (TierImpl) obj;
			// we do not want to make annotations to the tokens, nor to the
			// tiers that have already been made for segmentation, nor to the
			// ignore tiers
			// TODO make it, so that the segmentation tiers are created here as
			// well
			if (!this.getProps().getSegmentationTierNames().contains(tier.getName()) & !this.getProps().getIgnoreTierNames().contains(tier.getName())) {

				// and go through the individual annotations on this tier
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
						throw new PepperModuleException(this, "something wrong at " + beginTime + " up to " + endTime + "in file " + this.getElanModel().getFullPath());
					}

					// if there is something interesting in the value, grab
					// everything you can get about this anno
					if (!value.isEmpty()) {
						// create a sequence that we can use to search for a
						// related span
						DataSourceSequence sequence = new DataSourceSequence();
						sequence.setDataSource(sTextualDS);
						sequence.setStart((int) beginChar);
						sequence.setEnd((int) endChar);

						// this span variable will receive the span which will
						// be annotated
						SSpan sSpan = null;

						// Let's see if there are already some spans in the
						// sDocument that fit the bill
						List<SSpan> sSpansInSDoc = getDocument().getDocumentGraph().getSpans();
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
						List<DataSourceSequence> sequences = 
                            getDocument().getDocumentGraph().getOverlappedDataSourceSequence(sp, SALT_TYPE.STEXT_OVERLAPPING_RELATION);

							// grab the start en end of the first sequence for
							// comparison with the anno start and end
							int startSeq = (Integer)sequences.get(0).getStart();
							int endSeq = (Integer)sequences.get(0).getEnd();

							// check to see if this is the right span...
							if (startSeq == beginChar & endSeq == endChar) {
								sSpan = sp;
								lastSpanIndex = i; // so that the next loop
													// through this tier is
													// quicker.
								break;
							}
						}

						// if we found a span that fits, add the annotation to
						// it
						if (sSpan != null) {
							sSpan.createAnnotation(annoNS, tier.getName(), value);
						}

						// if there was no span yet, create a new one and add
						// the anno
						if (sSpan == null) {
							// find the tokens that are covered by the
							// annotation
							List<SToken> sNewTokens = getDocument().getDocumentGraph().getTokensBySequence(sequence);

							if ((sNewTokens != null) && (sNewTokens.size() > 0)) {
								// given these tokens, create a span and add the
								// annotation
								SSpan newSpan = getDocument().getDocumentGraph().createSpan(sNewTokens);
								newSpan.createAnnotation(annoNS, tier.getName(), value);
							}
						}
					}
				}
			}
		}
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
	public void createSegmentationForMainTiers() {

		// find the tier with the smallest segmentation for the tokens
		// TODO not every user has a character level, make this general by going
		// through all layers, and keep track of annotation ends
		// TODO it is not a good idea to make separate spans per segmentation
		// layer, this gets confusing. Let us move everything to the add
		// annotations part.
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
		int amountOfTextualDSs = getDocument().getDocumentGraph().getTextualDSs().size();
		STextualDS primaryText = getDocument().getDocumentGraph().getTextualDSs().get(amountOfTextualDSs - 1);

		// because we need to calculate the positions of the tokens in the
		// primary text, we need these two things
		String primtextchangeable = primaryText.getText();
		int offset = 0;

		// go through the annotations of the tier with the smallest subdivision
		ArrayList<Integer> startStopValues = new ArrayList<Integer>();
		// TODO this should not be a loop through a specific annotation layer,
		// but rather through a list of start and stop values
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
				throw new PepperModuleException(this, "token was not found in primarytext: (" + name + ", " + value + ") (primtext:" + primtextchangeable + ")");
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
				getDocument().getDocumentGraph().createToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
				startStopValues.removeAll(startStopValues);
			}

			// if an anno starts here, but nothing ends, then that means that
			// the previous segments have to form a token, and that the current
			// starts a new list
			if (endToken == false & startToken == true) {
				if (startStopValues.size() > 0) {
					getDocument().getDocumentGraph().createToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
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
					getDocument().getDocumentGraph().createToken(primaryText, startStopValues.get(0), startStopValues.get(startStopValues.size() - 1));
				}
				getDocument().getDocumentGraph().createToken(primaryText, corstart, corstop);
				startStopValues.removeAll(startStopValues);
			}

			// if no anno ends here, then this segment can be safely added to
			// the list without further action
			if (endToken == false & startToken == false) {
				startStopValues.add(corstart);
				startStopValues.add(corstop);
			}
		}
		// now, we have a single token for every possible annotation

		// now make arching spans for the other maintiers
		// TODO this is the part that should go away, and left to add
		// annotations
		Collection<String> segtiers = this.getProps().getSegmentationTierNames();
		for (String tiername : segtiers) {
			TierImpl tier = (TierImpl) this.getElanModel().getTierWithId(tiername);
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
					DataSourceSequence sequence = null;
					sequence = new DataSourceSequence();
					sequence.setDataSource(primaryText);
					sequence.setStart((int) beginChar);
					sequence.setEnd((int) endChar);

					// find the relevant tokens
					List<SToken> sNewTokens = null;
					sNewTokens = getDocument().getDocumentGraph().getTokensBySequence(sequence);
					// create the span
					newSpan = getDocument().getDocumentGraph().createSpan(sNewTokens);
					newSpan.createAnnotation(annoNS, tiername, anno.getValue());
				} catch (Exception e) {
					throw new PepperModuleException(this, "something wrong at " + beginTime + " up to " + endTime + "in file " + this.getElanModel().getFullPath());
				}
			}
		}
	}
}