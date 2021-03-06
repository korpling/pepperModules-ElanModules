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
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;

public class AddLinkedElan {

	public static ArrayList<String> getGlossIDs(TranscriptionImpl main) {
		ArrayList<String> out = new ArrayList<String>();
		TierImpl glosstier = (TierImpl) main.getTierWithId("Glosse");
		for (Object annoObj : glosstier.getAnnotations()) {
			Annotation anno = (Annotation) annoObj;
			out.add(anno.getValue());
		}
		return out;
	}

	public static TranscriptionImpl getEafPartForGlossID(TranscriptionImpl gloss, String glossid) {
		long start = -1;
		long stop = -1;
		TranscriptionImpl out = null;
		TierImpl glossidtier = (TierImpl) gloss.getTierWithId("Glosse");
		for (Object annoObj : glossidtier.getAnnotations()) {
			Annotation anno = (Annotation) annoObj;
			if (glossid.equals(anno.getValue())) {
				start = anno.getBeginTimeBoundary();
				stop = anno.getEndTimeBoundary();
				out = getEafBetween(gloss, start, stop);
				break;
			}
		}
		return out;
	}

	public static TranscriptionImpl getEafBetween(TranscriptionImpl in, long beginTime, long endTime) {
		TranscriptionImpl out = new TranscriptionImpl();
		for (Object tierobj : in.getTiers()) {
			TierImpl tier = (TierImpl) tierobj;
			// create new tier
			TierImpl newTier = new TierImpl(tier.getName(), null, out, new LinguisticType("main-tier"));
			// add selected annotations to new tier
			for (Object annoobj : tier.getAnnotations()) {
				Annotation anno = (Annotation) annoobj;
				if (anno.getBeginTimeBoundary() >= beginTime && anno.getEndTimeBoundary() <= endTime) {
					newTier.addAnnotation(anno);
				}
			}
			// add new tier to out transcription
			out.addTier(newTier);
		}
		return out;
	}

	public static Collection<SToken> getSTokensForGloss(SDocument sDocument, String glossid) {
		Collection<SToken> out = null;
		for (SSpan span : sDocument.getDocumentGraph().getSpans()) {
			for (SAnnotation sAnno : span.getAnnotations()) {
				if (sAnno.getName().equals("Glosse")) {
					String annoValue = sAnno.getValue().toString();
					if (annoValue.equals(glossid)) {
						out = getSTokensFromSSpan(span);
						break;
					}
				}
			}
		}
		return out;
	}

	public static int calcTokensNeeded(TranscriptionImpl glosspart, ElanImporterProperties props) {
		int count = 0;
		for (Object tierobj : glosspart.getTiers()) {
			TierImpl tier = (TierImpl) tierobj;
			if (!props.getIgnoreTierNames().contains(tier.getName())) {
				int annoCount = tier.getAnnotations().size();
				if (annoCount > count) {
					count = annoCount;
				}
			}
		}
		return count;
	}

	private static STextualRelation getTextualRelationFromSToken(SToken stok) {
		STextualRelation out = null;
		for (STextualRelation stextrel : stok.getGraph().getTextualRelations()) {
			if (stextrel.getSource().equals(stok)) {
				out = stextrel;
				break;
			}
		}
		return out;
	}

	public static int getStopFromSToken(SToken sToken) {
		int out = -1;
		for (STextualRelation stextrel : sToken.getGraph().getTextualRelations()) {
			if (stextrel.getSource().equals(sToken)) {
				out = stextrel.getEnd();
				break;
			}
		}
		return out;
	}

	public static int getStartFromSToken(SToken sToken) {
		int out = -1;
		for (STextualRelation stextrel : sToken.getGraph().getTextualRelations()) {
			if (stextrel.getSource().equals(sToken)) {
				out = stextrel.getStart();
				break;
			}
		}
		return out;
	}

	public static ArrayList<SToken> getGlossTokens(SDocument sDocument, SToken sToken, int tokensNeeded) {
		ArrayList<SToken> out = new ArrayList<SToken>();
		int start = getStartFromSToken(sToken);
		int stop = start + tokensNeeded;
		for (STextualRelation sTextRel : sDocument.getDocumentGraph().getTextualRelations()) {
			if (sTextRel.getStart() >= start && sTextRel.getEnd() <= stop) {
				out.add(sTextRel.getSource());
			}
		}
		return out;
	}

	public static List<SToken> getSTokensFromSSpan(SSpan span) {
		SDocumentGraph docGraph = span.getGraph();
		List<SSpanningRelation> spanningRelations = docGraph.getSpanningRelations();

		List<SToken> spanTokens = new ArrayList<SToken>();
		for (SSpanningRelation spanningRel : spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				spanTokens.add(token);
			}
		}
		return spanTokens;
	}

	public static int getEndFromSSpan(SSpan span, SDocumentGraph docGraph) {
		List<SSpanningRelation> spanningRelations = docGraph.getSpanningRelations();
		int out = -1;
		for (SSpanningRelation spanningRel : spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				int newStop = getStopFromSToken(token);
				if (newStop > out) {
					out = newStop;
				}
			}
		}
		return out;
	}

	public static int getStartFromSSpan(SSpan span, SDocumentGraph docGraph) {
		List<SSpanningRelation> spanningRelations = docGraph.getSpanningRelations();
		int out = 999999999;
		for (SSpanningRelation spanningRel : spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				int newStart = getStartFromSToken(token);
				if (newStart < out) {
					out = newStart;
				}
			}
		}
		return out;
	}

	public static Collection<SSpan> getSpansContaintingToken(SToken sToken) {
		Collection<SSpan> out = new ArrayList<SSpan>();
		SDocumentGraph curSDocGraph = sToken.getGraph();

		Collection<SSpan> spans = curSDocGraph.getSpans();
		for (SSpan span : spans) {
			// get tokens in the span
			List<SSpanningRelation> spanningRelations = curSDocGraph.getSpanningRelations();
			List<SToken> spanTokens = new ArrayList<SToken>();
			for (SSpanningRelation spanningRel : spanningRelations) {
				if (spanningRel.getSource() == span) {
					SToken token = (SToken) spanningRel.getTarget();
					spanTokens.add(token);
				}
			}
			if (spanTokens.contains(sToken)) {
				out.add(span);
			}
		}
		return out;
	}
}