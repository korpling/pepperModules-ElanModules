package mpi.eudico.client.annotator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * 
 * 
 * @author aarsom
 *
 */
public class AnnotationSlicer {
	
	/**
	 * Returns a sorted list of begin time and end time values
	 * from the list of given tiers
	 * 
	 * @param tierList, list of tiers from which the time values
	 * 					to be extracted
	 * @return ArrayList<Long>
	 */
	public static ArrayList<Long> getTimeValues(List<TierImpl> tierList){
		if(tierList == null){
			return null;
		}
		
		ArrayList<Long> timeSlotList = new ArrayList<Long>();
		TierImpl tier = null;
		Vector annotations;
		long time;
		for(int i= 0; i < tierList.size(); i++){
			tier = tierList.get(i);
			annotations  = tier.getAnnotations();
			for(int a=0; a < annotations.size(); a++){
				time = ((Annotation)annotations.get(a)).getBeginTimeBoundary();
				if(!timeSlotList.contains(time)){
					timeSlotList.add(time);
				}
				
				time = ((Annotation)annotations.get(a)).getEndTimeBoundary();
				if(!timeSlotList.contains(time)){
					timeSlotList.add(time);
				}
			}
		}
		
		Collections.sort(timeSlotList);	
		return timeSlotList;
	}
	
    /** Returns a map <timeValue, List<annotations at this time value>>
     * 
     * @param timeValuesList, list of time values for which the annotations
     * 						 have to be extracted
     * @param tierList, tier list from which the annotations
     * 					have to extracted
     * 
     * @return HashMap<Long, ArrayList<Annotation>>
     */
	public static HashMap<Long, ArrayList<Annotation>> getAnnotationMap(ArrayList<Long> timeValuesList, ArrayList<TierImpl> tierList){
		HashMap<Long, ArrayList<Annotation>> map = new HashMap<Long, ArrayList<Annotation>>();
		ArrayList<Annotation> annList;
		Annotation ann;
		long currentTimeValue;
		for(int i = 0; i < timeValuesList.size(); i++){
			currentTimeValue = timeValuesList.get(i);
			annList  = new ArrayList<Annotation>();
			
			for(int t=0; t < tierList.size(); t++){				
				ann = tierList.get(t).getAnnotationAtTime(currentTimeValue);
				if(ann != null){	
					annList.add(ann);
				}
			}	
			
			map.put(currentTimeValue, annList);
		}		
		return map;
	}
}
