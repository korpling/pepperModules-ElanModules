package mpi.eudico.client.annotator.transcriptionMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A class which extractors a list of possible linguistic types 
 * for the next column in the transcription table based on the
 * selections made for other columns
 * 
 * @author aarsom
 */
public class PossibleTypesExtractor{
	private List<String> linkedTypes;   
    private List<TierImpl> parentTierList;  
    /** structure : HashMap< top level parent tiers, 
     * 				map< typeName, List< number of tiers of this type under this toplevel tier>>
     */
 	private HashMap<TierImpl, HashMap<String, Integer>> typeTierMap;
    private Transcription transcription;
	
	public PossibleTypesExtractor(Transcription transcription){
		// initialize all the variables
        typeTierMap = new HashMap<TierImpl, HashMap<String, Integer>>();	
		parentTierList = new ArrayList<TierImpl>();   
		linkedTypes = new ArrayList<String>();
		this.transcription = transcription;
	}
	
	/**
     * Returns a hashmap with the available typeNames
     * and the number of tiers(as a value) of each type related to the given
     * reference tier
     * 
     * @param tier, the top level parent tier
     * @return tierTypeMap, a map of the available types and the number of tiers of that types
     */
    private HashMap<String, Integer> getTierTypeMap(TierImpl tier){    	   	
    	HashMap<String, Integer> tierTypeMap = new HashMap<String, Integer>();	    	
    	List childTiers = tier.getChildTiers();    	
    		
    	for(int x=0; x < childTiers.size(); x++){
			TierImpl childTier = (TierImpl) childTiers.get(x);
			String typeName = null ;
			if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
				// get all the types of the tiers depending on this child tier
				List dependentTiers = childTier.getDependentTiers();
				for(int y=0; y < dependentTiers.size(); y++) {
					TierImpl dependantTier = (TierImpl) dependentTiers.get(y);	
					typeName = dependantTier.getLinguisticType().getLinguisticTypeName();
					if(dependantTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
						if(tierTypeMap.containsKey(typeName)){
							tierTypeMap.put(typeName, tierTypeMap.get(typeName)+1);
						} else {
							tierTypeMap.put(typeName, 1);
						}
						
						if(!linkedTypes.contains(dependantTier.getLinguisticType().getLinguisticTypeName())){
							linkedTypes.add(dependantTier.getLinguisticType().getLinguisticTypeName());
						}
					}
				}						
				typeName = childTier.getLinguisticType().getLinguisticTypeName();
				
				if(tierTypeMap.containsKey(typeName)){
					tierTypeMap.put(typeName, tierTypeMap.get(typeName)+1);
				} else {
					tierTypeMap.put(typeName, 1);
				}
				
				if(!linkedTypes.contains(childTier.getLinguisticType().getLinguisticTypeName())){
					linkedTypes.add(childTier.getLinguisticType().getLinguisticTypeName());
				}
			}
		}    	
    	return tierTypeMap;   	
    }
    
    /**
     * Get all the types linked with the first selected type
     */
    private void getLinkedTypes(String refType){ 
    	linkedTypes.clear();
    	parentTierList.clear();
    	
    	List tierList = transcription.getTiersWithLinguisticType(refType);	
    	List<TierImpl> parentTiers = new ArrayList<TierImpl>();
    	
		for(int i= 0 ; i < tierList.size(); i++){
			TierImpl tier = (TierImpl) tierList.get(i);		
			
			//if type selected for the first column is not symbolic associated type
			if(tier.getLinguisticType().getConstraints() == null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION ){	
				if(parentTierList.contains(tier)){
		    		continue;
		    	} 
		    	parentTierList.add(tier);
		    	typeTierMap.put(tier, getTierTypeMap(tier));				
			} else {				
				TierImpl parentTier = (TierImpl) tier.getParentTier();
				while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
					parentTier = (TierImpl) parentTier.getParentTier();
				}	
				
				if(parentTierList.contains(parentTier)){
		    		continue;
		    	} 
				parentTierList.add(parentTier);				
				
				typeTierMap.put(parentTier, getTierTypeMap(parentTier));	
			}
		}
    }
    
    /**
     * Returns the list of all possible types for the next column
     * 
     * @param column, the column number
     * @return possibleTypes, a list of all possible types for this column
     */
    public List<String> getPossibleTypesForColumn(int column, List<String> columnTypeList){
    	
    	
    	List<String> possibleTypes = new ArrayList<String>();
    	
    	if(column == 1){ 
    		// for first column any linguistic type can be selected
    		List types = transcription.getLinguisticTypes();		    
    		for(int i = 0; i < types.size(); i++){
    			LinguisticType type = (LinguisticType)types.get(i);	
    			Vector tierList = transcription.getTiersWithLinguisticType(type.getLinguisticTypeName());
    			if( tierList!= null && tierList.size() > 0){
    				possibleTypes.add(type.getLinguisticTypeName());
    			}
    		}
    		return possibleTypes;
    	}
    	
    	if(columnTypeList == null){
    		return possibleTypes;
    	}
    	
    	if(column == 2 ){
    		// for other columns 
    		if(columnTypeList.size() > 0){
    			getLinkedTypes(columnTypeList.get(0));
    		}else {
    			return possibleTypes;
    		}
    	}
    	
    	for(int x=0; x < parentTierList.size(); x++){
    		HashMap <String,Integer> _map = typeTierMap.get(parentTierList.get(x));
    		for(int i=0; i < linkedTypes.size(); i++){
    			String typeName = linkedTypes.get(i);
    			if (_map.containsKey(typeName)){    				
    				int noOfTiers = _map.get(typeName).intValue();
    				if(noOfTiers > 0){
    					for(int c=0; c < columnTypeList.size(); c++){    						
    						if(c == column-1){
    							break;
    						}
    						if(typeName.equals(columnTypeList.get(c))){
    							noOfTiers = noOfTiers -1;
    							if(noOfTiers <= 0){
    								noOfTiers = 0;
    								break;
    							}
    						}
    					}    					
    				}
    				
    				if(noOfTiers > 0){
    					if(!possibleTypes.contains(typeName)){
        					possibleTypes.add(typeName);
        				}
    				}
    			}
    		}
    	}    	
    	return possibleTypes;	
    }  
}
