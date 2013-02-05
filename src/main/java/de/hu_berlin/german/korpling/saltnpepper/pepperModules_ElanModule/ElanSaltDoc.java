package de.hu_berlin.german.korpling.saltnpepper.pepperModules_ElanModule;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import java.math.BigInteger;
import java.util.Collection;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules_ElanModule.exceptions.ElanModulesException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ElanSaltDoc {

	private Transcription eaf;
	private SaltProject sp;
	private SDocument doc;
	private SCorpus corp;
	private STextualDS primaryText;
	private SLayer defaultLayer;
	private TierImpl tokenTier;
	
	public ElanSaltDoc(String path) {
	    // initialize the eaf parsing
        eaf = new TranscriptionImpl(path);
        
        // initialize the Salt stuff
  	    sp = SaltFactory.eINSTANCE.createSaltProject();
        SCorpusGraph sCorpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
        sp.getSCorpusGraphs().add(sCorpGraph);
        corp= SaltFactory.eINSTANCE.createSCorpus();
        corp.setSName("corpus-name");
        sCorpGraph.addSNode(corp);
        //creates a meta annotation on the corpus sampleCorpus
        corp.createSMetaAnnotation(null, "annotator", "tom");
        doc= SaltFactory.eINSTANCE.createSDocument();
        doc.setSName("document-name");
        //adding document to the graph
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

	public void setTokens(TierImpl tokenizationTier) {
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
	
	public void save(URI saveloc) {
		sp.saveSaltProject_DOT(saveloc);
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
        	try{
        		SToken elanTok = getSTokenAtTime(tokStart);
        		while (tokStop <= targetStop){
        			overlappingTokens.add(elanTok);
        			tok = this.tokenTier.getAnnotationAtTime(tokStop);
        			tokStart = (int) tok.getBeginTimeBoundary();
        			tokStop = (int) tok.getEndTimeBoundary();
        			elanTok = getSTokenAtTime(tokStart);
        		}
        		SSpan newSpan = doc.getSDocumentGraph().createSSpan(overlappingTokens);
        		newSpan.createSAnnotation(null, annotationlevel, targetValue);
        	} catch (NullPointerException nothingFound) {
        		throw new ElanModulesException("\tError: No matching tokens found for span between " + targetStart + " and " + targetStop + ".");
        	} finally {
        		continue;
        	}
        }
	}
}
