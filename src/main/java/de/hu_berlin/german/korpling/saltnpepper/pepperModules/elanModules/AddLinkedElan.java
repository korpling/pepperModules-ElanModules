/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules;

import java.util.ArrayList;
import java.util.Collection;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;

public class AddLinkedElan {
	
	public static ArrayList<String> getGlossIDs(TranscriptionImpl main){
		ArrayList<String> out = new ArrayList<String>();
		TierImpl glosstier = (TierImpl) main.getTierWithId("Glosse");
		for (Object annoObj : glosstier.getAnnotations()){
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
		for (Object annoObj : glossidtier.getAnnotations()){
			Annotation anno = (Annotation) annoObj;
			if (glossid.equals(anno.getValue())){
				start = anno.getBeginTimeBoundary();
				stop = anno.getEndTimeBoundary();
				out = getEafBetween(gloss, start, stop);
				break;
			}
		}
		return out;
	}
	
	public static TranscriptionImpl getEafBetween(TranscriptionImpl in, long beginTime, long endTime){
		TranscriptionImpl out = new TranscriptionImpl();
		for (Object tierobj : in.getTiers()){
		  TierImpl tier = (TierImpl) tierobj;
		  // create new tier
		  TierImpl newTier = new TierImpl(tier.getName(), null, out, new LinguisticType("main-tier"));
		  // add selected annotations to new tier
		  for (Object annoobj : tier.getAnnotations()){
			  Annotation anno = (Annotation) annoobj;
			  if (anno.getBeginTimeBoundary() >= beginTime && anno.getEndTimeBoundary() <= endTime){
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
		for (SSpan span : sDocument.getSDocumentGraph().getSSpans()){
		  for (SAnnotation sAnno : span.getSAnnotations()){
			if (sAnno.getSName().equals("Glosse")){
			  String annoValue = sAnno.getValue().toString();
			  if (annoValue.equals(glossid)){
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
		for (Object tierobj : glosspart.getTiers()){
			TierImpl tier = (TierImpl) tierobj;
			if (!props.getIgnoreTierNames().contains(tier.getName())){
				int annoCount = tier.getAnnotations().size();
				if (annoCount > count){
					count = annoCount;
				}
			}
		}
		return count;
	}

	private static STextualRelation getSTextualRelationFromSToken(SToken stok){
		STextualRelation out = null;
		for (STextualRelation stextrel : stok.getSDocumentGraph().getSTextualRelations()){
			if (stextrel.getSToken().equals(stok)){
			  out = stextrel;
			  break;
			}
		}
		return out;
	}
	
	public static int getStopFromSToken(SToken sToken) {
		int out = -1;
		for (STextualRelation stextrel : sToken.getSDocumentGraph().getSTextualRelations()){
		  if (stextrel.getSToken().equals(sToken)){
			  out = stextrel.getSEnd();
			  break;
		  }
		}
		return out;
	}

	public static int getStartFromSToken(SToken sToken) {
		int out = -1;
		for (STextualRelation stextrel : sToken.getSDocumentGraph().getSTextualRelations()){
		  if (stextrel.getSToken().equals(sToken)){
			  out = stextrel.getSStart();
			  break;
		  }
		}
		return out;
	}

	public static ArrayList<SToken> getGlossTokens(SDocument sDocument, SToken sToken, int tokensNeeded) {
		ArrayList<SToken> out = new ArrayList<SToken>();
		int start = getStartFromSToken(sToken);
		int stop = start + tokensNeeded;
		for (STextualRelation sTextRel : sDocument.getSDocumentGraph().getSTextualRelations()){
			if (sTextRel.getSStart() >= start && sTextRel.getSEnd() <= stop){
				out.add(sTextRel.getSToken());
			}
		}
		return out;
	}

	public static EList<SToken> getSTokensFromSSpan(SSpan span) {
		SDocumentGraph docGraph = span.getSDocumentGraph();
		EList<SSpanningRelation> spanningRelations = docGraph.getSSpanningRelations();
		
		EList<SToken> spanTokens = new BasicEList<SToken>();
		for (SSpanningRelation spanningRel: spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				spanTokens.add(token);
			}
		}
		return spanTokens;
	}
	
	public static int getEndFromSSpan(SSpan span, SDocumentGraph docGraph) {
		EList<SSpanningRelation> spanningRelations = docGraph.getSSpanningRelations();
		int out = -1;
		for (SSpanningRelation spanningRel: spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				System.out.println("token in span: " + token);
				int newStop = getStopFromSToken(token);
				System.out.println("newStop: " + newStop);
				if( newStop > out){
					out = newStop;
				}
			}
		}
		return out;
	}

	public static int getStartFromSSpan(SSpan span, SDocumentGraph docGraph) {
		EList<SSpanningRelation> spanningRelations = docGraph.getSSpanningRelations();
		int out = 999999999;
		for (SSpanningRelation spanningRel: spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				int newStart = getStartFromSToken(token);
				if( newStart < out){
					out = newStart;
				}
			}
		}
		return out;
	}

	public static Collection<SSpan> getSpansContaintingToken(SToken sToken) {
		Collection<SSpan> out = new ArrayList<SSpan>();
		SDocumentGraph curSDocGraph = sToken.getSDocumentGraph();
		
		Collection<SSpan> spans = curSDocGraph.getSSpans();
		for (SSpan span : spans){			
			// get tokens in the span
			EList<SSpanningRelation> spanningRelations = curSDocGraph.getSSpanningRelations();
			EList<SToken> spanTokens = new BasicEList<SToken>();
			for (SSpanningRelation spanningRel: spanningRelations) {
				if (spanningRel.getSource() == span) {
					SToken token = (SToken) spanningRel.getTarget();
					spanTokens.add(token);
				}
			}
			if (spanTokens.contains(sToken)){
				out.add(span);
			}
		}
		return out;
	}	
}