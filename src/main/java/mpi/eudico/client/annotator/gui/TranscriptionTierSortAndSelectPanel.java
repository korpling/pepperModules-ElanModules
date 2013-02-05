package mpi.eudico.client.annotator.gui;


import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

public class TranscriptionTierSortAndSelectPanel extends AbstractTierSortAndSelectPanel 
	implements ActionListener, ListSelectionListener, ChangeListener, ItemListener, TableModelListener {
  protected List<String> allTypeNames;
  protected List<String> allPartNames;
  protected List<String> allAnnNames;
	
	private Transcription transcription;
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers
	 * 
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription) {
		this(transcription, null, null, true, false);
	}
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers, but with the option to specify the tier mode
	 * 
	 * @param transcription
	 * @param tierMode the tier mode for the panel one of the Modes enum
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription, Modes tierMode) {
		this(transcription, null, null, true, false, tierMode);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription, List<String> tierOrder,
			List<String> selectedTiers) {
		this(transcription, tierOrder, selectedTiers, true, false);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering) {
		this(transcription, tierOrder, selectedTiers, allowReordering, false);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		this(transcription, tierOrder, selectedTiers, allowReordering, allowSorting, Modes.ALL_TIERS);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(Transcription transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
			Modes tierMode) {
		super();
		this.transcription = transcription;
		allTierNames = tierOrder;
		if (selectedTiers  != null) {
			selectedTierNames = selectedTiers;
			initialSelectedTiersProvided = true;
		} else {
			selectedTierNames = new ArrayList<String>();
			initialSelectedTiersProvided = false;
		}
		this.allowReordering = allowReordering;
		this.allowSorting = allowSorting;
		mode = tierMode;
		tabIndices = new HashMap<Integer, String>(4);
		tabIndices.put(TIER_INDEX, BY_TIER);
		tabIndices.put(TYPE_INDEX, BY_TYPE);
		tabIndices.put(PART_INDEX, BY_PART);
		tabIndices.put(ANN_INDEX, BY_ANN);
		initComponents();
	}
	
	/**
	 * Populates the tables with data from the transcription
	 */
	protected void initTables() {
        allTypeNames = new ArrayList<String>();          
    	allPartNames = new ArrayList<String>();          
    	allAnnNames = new ArrayList<String>();  
		TierImpl tier;
        String value;
        
		if (allTierNames == null) {
			allTierNames = new ArrayList<String>();
			List tiers = transcription.getTiers();
			
			for (int i = 0; i < tiers.size(); i++) {
				tier = (TierImpl) tiers.get(i);
				switch (mode) {
				case ALIGNABLE_TIERS:
					if (tier.isTimeAlignable()) {
						allTierNames.add(tier.getName());
					}
					break;
				case ROOT_TIERS:
					if (tier.getParentTier() == null) {
						allTierNames.add(tier.getName());
					}
					break;
				case ROOT_W_INCLUDED:
					if (tier.getParentTier() == null || 
							(tier.getLinguisticType().getConstraints() != null && 
									tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
						allTierNames.add(tier.getName());
					}
					break;
					default:
					allTierNames.add(tier.getName());	
				}				
			}
		} else {
			List tiers = transcription.getTiers();
			
			for (int i = 0; i < tiers.size(); i++) {
				tier = (TierImpl) tiers.get(i);
				switch (mode) {
				case ALIGNABLE_TIERS:
					if (tier.isTimeAlignable()) {
						if (!allTierNames.contains(tier.getName())) {
							allTierNames.add(tier.getName());
						}
					} else {
						if (allTierNames.contains(tier.getName())) {
							allTierNames.remove(tier.getName());
						}
					}
					break;
				case ROOT_TIERS:
					if (tier.getParentTier() == null) {
						if (!allTierNames.contains(tier.getName())) {
							allTierNames.add(tier.getName());
						}
					} else {
						if (allTierNames.contains(tier.getName())) {
							allTierNames.remove(tier.getName());
						}
					}
					break;
				case ROOT_W_INCLUDED:
					if (tier.getParentTier() == null || 
							(tier.getLinguisticType().getConstraints() != null && 
									tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
						if (!allTierNames.contains(tier.getName())) {
							allTierNames.add(tier.getName());
						}
					} else {
						if (allTierNames.contains(tier.getName())) {
							allTierNames.remove(tier.getName());
						}
					}
					break;
					default:
						if (!allTierNames.contains(tier.getName())) {
							allTierNames.add(tier.getName());	
						}
				}				
			}
		}
        // this only adds the types, participants and annotators of (selected) tiers
        if (allTierNames != null) {
            for (String name : allTierNames) {
                if (initialSelectedTiersProvided) {
                    if (selectedTierNames.contains(name)) {
                        model.addRow(new Object[] { Boolean.TRUE, name });
                    } else {
                        model.addRow(new Object[] { Boolean.FALSE, name });
                    }
                } else {
                    model.addRow(new Object[] { Boolean.TRUE, name });// default is selected
                }              
                
                tier = (TierImpl) transcription.getTierWithId(name);  
               
        		value = tier.getParticipant();
        		if(value.length()==0){
        			value = NOT_SPECIFIED;
        		}
                
        		if(!allPartNames.contains(value)){
        			allPartNames.add(value);
        			partModel.addRow(new Object[] { Boolean.FALSE, value });
        		}	
        		
        		value =tier.getAnnotator();
    			if(value.length() == 0){
    				value = NOT_SPECIFIED;
    			}
    			
    			if(!allAnnNames.contains(value)){
        			allAnnNames.add(value);
        			annotModel.addRow(new Object[] { Boolean.FALSE, value });
        		}
       			
       			value =tier.getLinguisticType().getLinguisticTypeName();
       			if(!allTypeNames.contains(value)){
       				allTypeNames.add(value);
       				typeModel.addRow(new Object[] { Boolean.FALSE, value });
        		}
            }
            
            if (model.getRowCount() == 1) {
            	model.setValueAt(Boolean.TRUE, 0, 0);
            	String name = (String) model.getValueAt(0, model.findColumn(TIER_NAME_COLUMN));
            	
            	if (!selectedTierNames.contains(name)) {
            		selectedTierNames.add(name);
            	}
            }
        }
	}
	
	/**
	 * Switches dynamically between a view where only root tier are shown
	 * and the list of tiers for the mode of the panel.
	 *  
	 * @param rootsOnly
	 */
	protected void toggleRootsOnly(boolean rootsOnly) {
		if (rootsOnly) {// from all tiers for mode to root only
			// store current lists and selections for tiers and types
		   	int includeCol = model.findColumn(SELECT_COLUMN);
		    int nameCol = model.findColumn(TIER_NAME_COLUMN);
		    unfilteredTiers = new LinkedHashMap<String, Boolean>(model.getRowCount());
			for (int i = 0; i < model.getRowCount(); i++) {
		    	unfilteredTiers.put((String) model.getValueAt(i, nameCol), 
		    			(Boolean) model.getValueAt(i, includeCol));
		    }
		    model.removeTableModelListener(this);
		    String name;
		    TierImpl t;
		    for (int i = model.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) model.getValueAt(i, nameCol);
		    	t = (TierImpl) transcription.getTierWithId(name);
		    	if (t != null && t.hasParentTier()) {
		    		model.removeRow(i);
		    	}
		    }
		    
		    model.addTableModelListener(this);
		    pendingChanges = true;
		    
		    unfilteredTypes = new LinkedHashMap<String, Boolean>(typeModel.getRowCount());
		    includeCol = typeModel.findColumn(SELECT_COLUMN);
		    nameCol = typeModel.findColumn(TIER_NAME_COLUMN);
			for (int i = 0; i < typeModel.getRowCount(); i++) {
				unfilteredTypes.put((String) typeModel.getValueAt(i, nameCol), 
		    			(Boolean) typeModel.getValueAt(i, includeCol));
		    }
			
			LinguisticType type;
			List allTypes = transcription.getLinguisticTypes();
		    for (int i = typeModel.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) typeModel.getValueAt(i, nameCol);
		    	
		    	for (int j = 0; j < allTypes.size(); j++) {
		    		type = (LinguisticType) allTypes.get(j);
		    		if (type.getLinguisticTypeName().equals(name)) {
		    			if (type.getConstraints() != null) {
		    				typeModel.removeRow(i);
		    			}
		    			break;
		    		}
		    	}
		    }
		} else {// from root only to all tiers for the mode. Try to maintain changes in the current order
			if (unfilteredTiers == null) {
				return;
			}
			model.removeTableModelListener(this);
		   	int includeCol = model.findColumn(SELECT_COLUMN);
		    int nameCol = model.findColumn(TIER_NAME_COLUMN);
			// check if the unfiltered lists are there
			LinkedHashMap<String, Boolean> filteredTiers = new LinkedHashMap<String, Boolean>(model.getRowCount());
			for (int i = 0; i < model.getRowCount(); i++) {
				filteredTiers.put((String) model.getValueAt(i, nameCol), 
		    			(Boolean) model.getValueAt(i, includeCol));
		    }

			int insertAfter = -1;
			Iterator<String> keyIter = unfilteredTiers.keySet().iterator();
			String key;
			String name;
			while (keyIter.hasNext()) {
				key = keyIter.next();
				boolean shouldInsert = !filteredTiers.containsKey(key);
				if (shouldInsert) {
					if (insertAfter == -1) {
						model.insertRow(0, new Object[]{unfilteredTiers.get(key), key});
						insertAfter = 0;
					} else if (insertAfter >= model.getRowCount() - 1) {// add to end
						model.addRow(new Object[]{unfilteredTiers.get(key), key});
						insertAfter = model.getRowCount() - 1;
					} else {
						model.insertRow(insertAfter + 1, new Object[]{unfilteredTiers.get(key), key});
						insertAfter++;
					}					
				} else {// find index in current, filtered list
					for (int i = 0; i < model.getRowCount(); i++) {
						name = (String) model.getValueAt(i, nameCol);
						if (name.equals(key)) {
							insertAfter = i;
							break;
						}
				    }
				}				
			}
			
			model.addTableModelListener(this);
			pendingChanges = true;
			// do same for types
			LinkedHashMap<String, Boolean> filteredTypes = new LinkedHashMap<String, Boolean>(typeModel.getRowCount());
			for (int i = 0; i < typeModel.getRowCount(); i++) {
				filteredTypes.put((String) typeModel.getValueAt(i, nameCol), 
		    			(Boolean) typeModel.getValueAt(i, includeCol));
		    }
			
			insertAfter = -1;
			keyIter = unfilteredTypes.keySet().iterator();
			key = null;
			name = null;
			
			while (keyIter.hasNext()) {
				key = keyIter.next();
				boolean shouldInsert = !filteredTypes.containsKey(key);
				if (shouldInsert) {
					if (insertAfter == -1) {
						typeModel.insertRow(0, new Object[]{unfilteredTypes.get(key), key});
						insertAfter = 0;
					} else if (insertAfter >= typeModel.getRowCount() - 1) {// add to end
						typeModel.addRow(new Object[]{unfilteredTypes.get(key), key});
						insertAfter = typeModel.getRowCount() - 1;
					} else {
						typeModel.insertRow(insertAfter + 1, new Object[]{unfilteredTypes.get(key), key});
						insertAfter++;
					}					
				} else {// find index in current, filtered list
					for (int i = 0; i < typeModel.getRowCount(); i++) {
						name = (String) typeModel.getValueAt(i, nameCol);
						if (name.equals(key)) {
							insertAfter = i;
							break;
						}
				    }
				}				
			}
		}
	}
    
	 /**
     * 
     */
    protected void showTiersTab(){     	
    	int includeCol = model.findColumn(SELECT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        model.removeTableModelListener(this);
        
    	for (int i = 0; i < model.getRowCount(); i++) {
    		Object value = model.getValueAt(i, nameCol);
    		if(selectedTierNames.contains(value.toString())){
    			model.setValueAt(Boolean.TRUE, i, includeCol);
    		} else {
    			model.setValueAt(Boolean.FALSE, i, includeCol);
    		}
    	}

    	model.addTableModelListener(this);
    }
    
    
    
    /**
     * Update the types tab changes
     */
    protected void updateLinguisticTypes(){
    	if (pendingChanges) {
    		selectedTierNames.clear();
    		int includeCol = typeModel.findColumn(SELECT_COLUMN);
    	    int nameCol = typeModel.findColumn(TIER_NAME_COLUMN);
    	    String typeName;
    	    Object include;
    		for (int i = 0; i < typeModel.getRowCount(); i++) {
    			include = typeModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				typeName = (String) typeModel.getValueAt(i, nameCol);
    				Vector visibleTiers = transcription.getTiersWithLinguisticType(typeName);
            		if(visibleTiers != null){
            			for(int x = 0; x < visibleTiers.size(); x++){        				
            				selectedTierNames.add(((TierImpl)visibleTiers.get(x)).getName());
            			}
            		}
    			}
    		}
    		hiddenTiers.clear();
    		for (String s : allTierNames) {
    			if (!selectedTierNames.contains(s)) {
    				hiddenTiers.add(s);
    			}
    		}
    	}
    	
    }
    
    /**
     * Update the participants tab changes
     */
    protected void updateParticipants(){
    	if (pendingChanges) {
    		selectedTierNames.clear();
        	// update based on table model
    	   	int includeCol = partModel.findColumn(SELECT_COLUMN);
    	    int nameCol = partModel.findColumn(TIER_NAME_COLUMN);
    	    String participant;
    	    Object include;
    	    
    		for (int i = 0; i < partModel.getRowCount(); i++) {
    			include = partModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				participant = (String) partModel.getValueAt(i, nameCol);
    				
    				for (String s : allTierNames) {
    					TierImpl tier = (TierImpl) transcription.getTierWithId(s);
    	             	String partName = tier.getParticipant();           	
    	             	if ( (partName == null || partName.length() == 0) && participant == NOT_SPECIFIED){
    	             		selectedTierNames.add(tier.getName());
    	             	} else if (participant.equals(partName)) {
    	             		selectedTierNames.add(tier.getName());
    	             	}
    				}
    			}
    		} 
    		hiddenTiers.clear();
    		for (String s : allTierNames) {
    			if (!selectedTierNames.contains(s)) {
    				hiddenTiers.add(s);
    			}
    		}
    	}
    	
    }
    
    /**
     * Update the annotator tab changes
     */
    protected void updateAnnotators(){
    	if (pendingChanges) {
    		selectedTierNames.clear();
        	// update based on table model
    	   	int includeCol = annotModel.findColumn(SELECT_COLUMN);
    	    int nameCol = annotModel.findColumn(TIER_NAME_COLUMN);
    	    String annotator;
    	    Object include;
    	    
    		for (int i = 0; i < annotModel.getRowCount(); i++) {
    			include = annotModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				annotator = (String) annotModel.getValueAt(i, nameCol);
    				
    				for (String s : allTierNames) {
    					TierImpl tier = (TierImpl) transcription.getTierWithId(s);
    	             	String annotName = tier.getAnnotator();           	
    	             	if ( (annotName == null || annotName.length() == 0) && annotator == NOT_SPECIFIED){
    	             		selectedTierNames.add(tier.getName());
    	             	} else if (annotator.equals(annotName)) {
    	             		selectedTierNames.add(tier.getName());
    	             	}
    				}
    			}
    		}
    		
    		hiddenTiers.clear();
    		for (String s : allTierNames) {
    			if (!selectedTierNames.contains(s)) {
    				hiddenTiers.add(s);
    			}
    		}
    	}	
    }
    
}
