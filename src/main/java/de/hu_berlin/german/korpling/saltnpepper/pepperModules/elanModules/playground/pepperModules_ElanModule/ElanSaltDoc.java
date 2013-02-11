package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.pepperModules_ElanModule;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.EnumSet;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimeline;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ElanSaltDoc {

	private Transcription eaf;;
	private SDocument doc;
	private STextualDS primaryText;
	private STimeline sTimeline;
	private SLayer defaultLayer;
	private TierImpl tokenTier;
	
	public ElanSaltDoc(String path, String docname, SCorpus corp) {
	    // initialize the eaf parsing
        eaf = new TranscriptionImpl(path);
        System.out.println(path);
        
        doc= SaltFactory.eINSTANCE.createSDocument();
        doc.setSName(docname);
        //adding document to the graph
        SCorpusGraph sCorpGraph = corp.getSCorpusGraph();
        sCorpGraph.addSNode(doc);
        //creating relation (edge)
        SCorpusDocumentRelation sRelation= SaltFactory.eINSTANCE.createSCorpusDocumentRelation();
        //adding source (the corpus) to relation
        sRelation.setSCorpus(corp);
        //adding target (the document) to relation
        sRelation.setSDocument(doc);
        //adding the relation to the graph
        sCorpGraph.addSRelation(sRelation);
        doc.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		
	}
	
	public TierImpl getTokenTier(){
		return tokenTier;
	}
	
	public SDocument getSDocument(){
		return doc;
	}

	public void setPrimaryText(String pt){
		primaryText = SaltFactory.eINSTANCE.createSTextualDS();
        primaryText.setSText(pt);
        doc.getSDocumentGraph().addSNode(primaryText);
	}
	
	public TierImpl getTierByName(String name){
        Collection<TierImpl> tiers = eaf.getTiers();
        TierImpl out = null;
        for (TierImpl tier : tiers){
        	if (tier.getName().equals(name)){
        		out = tier;
        	}
        }
		return out;
    }

    public String getValuesFromTier(String name){
    	String values = new String("");
    	TierImpl pttier = this.getTierByName(name);
		Collection<AbstractAnnotation> annos = pttier.getAnnotations();
        for (AbstractAnnotation anno : annos)
        {
          String wert = anno.getValue();
          values = values + wert;
        }
        return values;
	}

	public void createDefaultLayer() {
        defaultLayer = SaltFactory.eINSTANCE.createSLayer();
        defaultLayer.setSName("defaultLayer");
        doc.getSDocumentGraph().addSLayer(defaultLayer);
	}

	public void setSTokens(TierImpl tokenizationTier) {
		this.tokenTier = tokenizationTier;
		Collection<AbstractAnnotation> annos = tokenizationTier.getAnnotations();
        for (AbstractAnnotation anno : annos){
        	String value = anno.getValue();
        	Integer start = (int) anno.getBeginTimeBoundary();
        	Integer stop = (int) anno.getEndTimeBoundary();
        	SToken tok = doc.getSDocumentGraph().createSToken(primaryText, start, stop);
        	defaultLayer.getSNodes().add(tok);
        	tok.createSAnnotation(null, "tok", value);
        }
	}

	public void addAnnotation(String annotationlevel) {
		System.out.print("adding annotation " + annotationlevel + ": ");
		TierImpl tier = this.getTierByName(annotationlevel);
		ElanSaltTier estier = new ElanSaltTier(tier);
		boolean hasSpans = estier.hasSpans(this);
		if (!hasSpans){
			addTokenAnnotation(annotationlevel, tier);
		}
		if (hasSpans){
			addSpanAnnotation(annotationlevel, tier);
		}
	}
	
	public SToken getSTokenAtTime(long annoStart){ 
		Collection<AbstractAnnotation> tokens = this.getTokenTier().getAnnotations();
		int i = 0;
		for (AbstractAnnotation token : tokens){
			if (token.getBeginTimeBoundary() == annoStart){
				break;
			}
			i = i + 1;
		}
		SToken currentSToken = this.doc.getSDocumentGraph().getSTokens().get(i); 
		return currentSToken;
	}
	
	public void addTokenAnnotation(String annoLevel, TierImpl tier){
		System.out.println(annoLevel + " is estimated to be token-based");
		Collection<AbstractAnnotation> annos = tier.getAnnotations();
		for (AbstractAnnotation anno : annos){
			String annoValue = anno.getValue().trim();
			String annoValueHex = String.format("%x", new BigInteger(1, annoValue.getBytes()));
			if (annoValueHex.length() > 4){
				long annoStart = anno.getBeginTimeBoundary();
				SToken currentSToken = getSTokenAtTime(annoStart);
				currentSToken.createSAnnotation(null, annoLevel, annoValue);
			}
		}
	}
	
	public void addSpanAnnotation(String annotationlevel, TierImpl tier){
		System.out.println(annotationlevel + " is estimated to be span-based");
		Collection<AbstractAnnotation> targets = tier.getAnnotations();
        for (AbstractAnnotation target : targets){
        	EList<SToken> overlappingTokens = new BasicEList<SToken>();
        	String targetValue = target.getValue();
        	long targetStart = target.getBeginTimeBoundary();
        	long targetStop = target.getEndTimeBoundary();
        	Annotation tok = this.tokenTier.getAnnotationAtTime(targetStart);
        	int tokStart = (int) tok.getBeginTimeBoundary();
        	int tokStop = (int) tok.getEndTimeBoundary();
        	SToken elanTok = getSTokenAtTime(tokStart);
    		overlappingTokens.add(elanTok);
    		while (tokStop < targetStop){
    			tok = this.tokenTier.getAnnotationAfter(tokStop);
    			tokStart = (int) tok.getBeginTimeBoundary();
    			if (tokStop != tokStart){
    				System.out.println("\timprecision found at " + Milliseconds2HumanReadable(tokStop));
    			}
    			tokStop = (int) tok.getEndTimeBoundary();
    			elanTok = getSTokenAtTime(tokStart);
    			overlappingTokens.add(elanTok);
    		}
    		SSpan newSpan = doc.getSDocumentGraph().createSSpan(overlappingTokens);
    		newSpan.createSAnnotation(null, annotationlevel, targetValue);
        }
	}
	
	public String Milliseconds2HumanReadable(int millis){
		return String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);
	}
}
