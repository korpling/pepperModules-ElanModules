package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ElanWrapper {
	
	private TranscriptionImpl elan;
	private String primTierName= "tok";
	
	
	public void setPrimTierName(String primTierName) {
		this.primTierName = primTierName;
	}

	public String getPrimTierName() {
		return primTierName;
	}
	

	public ElanWrapper (String file){
		TranscriptionImpl eaf = new TranscriptionImpl(file);
		this.elan = eaf;
		
	}

	public String getPrimaryText() {
		StringBuffer primText = new StringBuffer();
		TierImpl charTier = (TierImpl) elan.getTierWithId(this.getPrimTierName());
		for (Object obj : charTier.getAnnotations()){
			AbstractAnnotation charAnno = (AbstractAnnotation) obj;
			primText.append(charAnno.getValue());
		}
		return(primText.toString());
	}

	public Collection<ElanToken> getElanTokens() {
		Collection<ElanToken> out = new ArrayList<ElanToken>();

		String primtextchangeable = getPrimaryText();
		
		TierImpl tokTier = (TierImpl) elan.getTierWithId(this.getPrimTierName());
				
		int offset = 0;
        for (Object obj : tokTier.getAnnotations()){
        	AbstractAnnotation token = (AbstractAnnotation) obj;
        	String value = token.getValue();
        	Integer start =  primtextchangeable.indexOf(value);
        	Integer stop = start + value.length();
        	primtextchangeable = primtextchangeable.substring(stop);
        	int corstart = offset + start;
        	int corstop = offset + stop;
        	offset = offset + stop;
        	
        	ElanToken curElanToken = new ElanToken(value, token.getBeginTimeBoundary(), token.getEndTimeBoundary(), corstart, corstop);
        	out.add(curElanToken);
        }
		
		return out;
	}

	public Collection<ElanToken> getElanTokens(long startTime, long endTime){
		Collection<ElanToken> out = new ArrayList();
		ArrayList<ElanToken> ets = (ArrayList<ElanToken>) getElanTokens();
		for (int i = 0; i < ets.size(); i++){
			if (startTime < endTime){
				if (ets.get(i).getBeginTime() == startTime){
					out.add(ets.get(i));
					if (i+1 < ets.size()){
						startTime = ets.get(i+1).getBeginTime();
					}
				}
			}
		}
		return out;
	}
	
	public String getValueInTier(String curTier, long startTime, long stopTime) {
		String out = null;
		TierImpl tier = (TierImpl) elan.getTierWithId(curTier);
		Annotation anno = tier.getAnnotationAtTime(startTime);
		if (anno != null){
			out = anno.getValue();
		}
		return out;
	}

	public String Milliseconds2HumanReadable(long millis){
		return String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);
	}

	public ElanToken getElanToken(int startChar, int stopChar) {
		ElanToken out = null;
		Collection<ElanToken> elanTokens = getElanTokens();
		for (ElanToken et : elanTokens){
			if (startChar == et.getBeginChar() & stopChar == et.getEndChar()){
				out = et;
				break;
			}
		}
		return out;
	}
	
	public ElanToken getElanToken(long startTime, long stopTime){
		ElanToken out = null;
		Collection<ElanToken> elanTokens = getElanTokens();
		for (ElanToken et : elanTokens){
			if (startTime == et.getBeginTime() & stopTime == et.getEndTime()){
				out = et;
				break;
			}
		}
		return out;
	}

	public Collection<ElanSpan> getElanSpans(String curTierName) {
		Collection<ElanSpan> out = new ArrayList();
		TierImpl curTier = (TierImpl) elan.getTierWithId(curTierName);
		Collection<ElanToken> elanTokens = getElanTokens();
        for (Object obj : curTier.getAnnotations()){
        	AbstractAnnotation anno = (AbstractAnnotation) obj;
        	Collection<ElanToken> ets = getElanTokens(anno.getBeginTimeBoundary(), anno.getEndTimeBoundary());
        	if (ets.size() > 0){
        		ElanSpan es = new ElanSpan(ets, curTierName, anno.getValue());
        		out.add(es);
        	}
        }
		return out;
	}

	public ArrayList<String> getTierNames() {
		ArrayList<String> out = new ArrayList<String>();
		for (Object obj : elan.getTiers()){
			TierImpl tier = (TierImpl) obj;
			out.add(tier.getName());
		}
		return out;
	}
}
