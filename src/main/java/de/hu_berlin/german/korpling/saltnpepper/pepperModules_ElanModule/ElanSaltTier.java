package de.hu_berlin.german.korpling.saltnpepper.pepperModules_ElanModule;

import java.util.Collection;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

public class ElanSaltTier {
	
	private TierImpl estier;
	
	public ElanSaltTier(TierImpl tier){
		this.estier = tier;
	}
	
	public boolean hasSpans(ElanSaltDoc doc){
		boolean out = false;
		Collection<AbstractAnnotation> nodes = this.estier.getAnnotations();
		for (AbstractAnnotation node : nodes){
			long nodeStart = node.getBeginTimeBoundary();
			long nodeStop = node.getEndTimeBoundary();
			Annotation token = doc.getTokenTier().getAnnotationAtTime(nodeStart);
			if (token.getEndTimeBoundary() < (nodeStop)){
				out = true;
			}
		}
		return out;
	}
}
