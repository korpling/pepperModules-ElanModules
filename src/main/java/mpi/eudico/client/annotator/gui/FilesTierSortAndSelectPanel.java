package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

public class FilesTierSortAndSelectPanel extends AbstractTierSortAndSelectPanel implements ActionListener,
ListSelectionListener, ChangeListener, ItemListener, TableModelListener {
	protected ArrayList<File> files;
	
	protected Map<String, ArrayList<String>> tierTypeMap;
	protected Map<String, ArrayList<String>> tierParticipantMap;
	protected Map<String, ArrayList<String>> tierAnnotatorMap;
	
	protected List<String> rootTiers;
	protected List<String> rootTypes;
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers
	 * 
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files) {
		this(files, null, null, true, false);
	}
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers, but with the option to specify the tier mode
	 * 
	 * @param transcription
	 * @param tierMode the tier mode for the panel one of the Modes enum
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, Modes tierMode) {
		this(files, null, null, true, false, tierMode);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, List<String> tierOrder,
			List<String> selectedTiers) {
		this(files, tierOrder, selectedTiers, true, false);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering) {
		this(files, tierOrder, selectedTiers, allowReordering, false);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		this(files, tierOrder, selectedTiers, allowReordering, allowSorting, Modes.ALL_TIERS);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
			Modes tierMode) {
		super();
		this.files = files;
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
		
        tierTypeMap = new LinkedHashMap<String, ArrayList<String>>();
    	tierParticipantMap = new LinkedHashMap<String, ArrayList<String>>();
    	tierAnnotatorMap = new LinkedHashMap<String, ArrayList<String>>();
    	rootTiers = new ArrayList<String>();
    	rootTypes = new ArrayList<String>();
    	
		initComponents();
	}
	
	
	
	/**
	 * Populates the tables with data from the transcription
	 */
	protected void initTables() {
        if (allTierNames == null) {
        	allTierNames = new ArrayList<String>();
        }
        //allTierNames.clear();  
    	tierTypeMap.clear();
    	tierParticipantMap.clear();
    	tierAnnotatorMap.clear();
		
        ArrayList pts = null;
        EAFSkeletonParser parser = null;
        File file;
        String path;

        for (int i = 0; i < files.size(); i++) {
        	file = (File) files.get(i);
        	if (file == null) {
        		continue;
        	}
        	path = file.getAbsolutePath();

            try {
            	parser = new EAFSkeletonParser(path);
                parser.parse();
                pts = parser.getTiers();

                TierImpl tier;
                String value;
                List<String> list;
                String tierName;

                for (int j = 0; j < pts.size(); j++) {
                	tier = (TierImpl) pts.get(j);
                	tierName = tier.getName();

                    if (!allTierNames.contains(tierName)) {
                    	allTierNames.add(tierName);
                    }
                    // store the root tiers separately
                    if (tier.getParentTier() == null) {
                    	if (!rootTiers.contains(tierName)) {
                    		rootTiers.add(tierName);
                    	}
                    }
                    value = tier.getParticipant();
            		if (value.length() == 0) {
            			value = NOT_SPECIFIED;
            		}
                    
            		if (tierParticipantMap.get(value) == null) {
            			tierParticipantMap.put(value, new ArrayList<String>());
            		}
        			
            		list = tierParticipantMap.get(value);
            		if (!list.contains(tierName)) {
            			list.add(tierName);
            		}
            		
            		value = tier.getAnnotator();
        			if (value.length() == 0) {
        				value = NOT_SPECIFIED;
        			}
        			
        			if (tierAnnotatorMap.get(value) == null) {
            			tierAnnotatorMap.put(value, new ArrayList<String>());
            		}
        			
        			list = tierAnnotatorMap.get(value);
            		if (!list.contains(tierName)) {
            			list.add(tierName);
            		}    		
           			
           			value = tier.getLinguisticType().getLinguisticTypeName();
           			if (tierTypeMap.get(value) == null) {
           				tierTypeMap.put(value, new ArrayList<String>());
            		}
           			
           			list = tierTypeMap.get(value);
            		if (!list.contains(tierName)) {
            			list.add(tierName);
            		}
            		// store the types for root tiers separately
            		if (tier.getParentTier() == null) {
            			if (!rootTypes.contains(value)) {
            				rootTypes.add(value);
            			}
            		}
                }
            } catch (ParseException pe) {
                ClientLogger.LOG.warning(pe.getMessage());
                    //pe.printStackTrace();
            } catch (Exception ex) {
                ClientLogger.LOG.warning("Could not load file: " + path);
            }
        }
        
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
	    	model.removeRow(i);
	    }

	    for (int i = 0; i < allTierNames.size(); i++) {
	    	if(selectedTierNames.size() > 0){
	    		if(selectedTierNames.contains(allTierNames.get(i))){
	    			model.addRow(new Object[] { Boolean.TRUE, allTierNames.get(i)});
	    		} else {
	    			model.addRow(new Object[] { Boolean.FALSE, allTierNames.get(i)});
	    		}
	    	} else if(i ==0){
	    		model.addRow(new Object[] { Boolean.TRUE, allTierNames.get(i)});
	    		selectedTierNames.add((String) allTierNames.get(i));
	    	} else {
	    		model.addRow(new Object[] { Boolean.FALSE, allTierNames.get(i)});
	    	}
	    }
	    
	    if (model.getRowCount() > 0) {
	    	tierTable.setRowSelectionInterval(0, 0);
	    }
	    
	    Iterator<String> typeIter = tierTypeMap.keySet().iterator();
	    String key;
	    while (typeIter.hasNext()) {
	    	key = typeIter.next();
	    	typeModel.addRow(new Object[] {Boolean.FALSE, key});
	    }
	    
	    Iterator<String> partIter = tierParticipantMap.keySet().iterator();
	    while (partIter.hasNext()) {
	    	key = partIter.next();
	    	partModel.addRow(new Object[] {Boolean.FALSE, key});
	    }
	    
	    Iterator<String> annotIter = tierAnnotatorMap.keySet().iterator();
	    while (annotIter.hasNext()) {
	    	key = annotIter.next();
	    	annotModel.addRow(new Object[] {Boolean.FALSE, key});
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

		    	if (!rootTiers.contains(name)) {
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
			
		    for (int i = typeModel.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) typeModel.getValueAt(i, nameCol);
		    	
		    	if (!rootTypes.contains(name)) {
		    		typeModel.removeRow(i);
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
    	    List<String> tierList;
    	    
    		for (int i = 0; i < typeModel.getRowCount(); i++) {
    			include = typeModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				typeName = (String) typeModel.getValueAt(i, nameCol);
    				
    				tierList = tierTypeMap.get(typeName);
    				
    				for (String s : tierList) {
    					if (!selectedTierNames.contains(s)) {
    						selectedTierNames.add(s);
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
    	    List<String> tierList;
    	    
    		for (int i = 0; i < partModel.getRowCount(); i++) {
    			include = partModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				participant = (String) partModel.getValueAt(i, nameCol);
    				
    				tierList = tierParticipantMap.get(participant);
    				for (String s : tierList) {
    					if (!selectedTierNames.contains(s)) {
    						selectedTierNames.add(s);
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
    	    List<String> tierList;
    	    
    		for (int i = 0; i < annotModel.getRowCount(); i++) {
    			include = annotModel.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				annotator = (String) annotModel.getValueAt(i, nameCol);
    				
    				tierList = tierAnnotatorMap.get(annotator);
    				for (String s : tierList) {
    					if (!selectedTierNames.contains(s)) {
    						selectedTierNames.add(s);
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
