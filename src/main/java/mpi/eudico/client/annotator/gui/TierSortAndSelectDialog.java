package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A dialog for sorting and selecting tiers.
 * Does not work on a Transcription object but rather on lists of tiers
 * @author Han Sloetjes
 */
public class TierSortAndSelectDialog extends ClosableDialog implements ActionListener,
		ListSelectionListener, MouseListener {
	
    /** A constant for unspecified participant or linguistic type */
    protected final String NOT_SPECIFIED = "not specified";
    
    protected final String BY_TIER = ElanLocale.getString("ExportDialog.Tab.Tier") ;
    protected final String BY_TYPE = ElanLocale.getString("ExportDialog.Tab.Type");
    protected final String BY_PART = ElanLocale.getString("ExportDialog.Tab.Participant");
    protected final String BY_ANN = ElanLocale.getString("ExportDialog.Tab.Annotators") ;
	
    /** panel for start and close buttons (bottom component) */
    protected JPanel buttonPanel;
    /** close button */
    private JButton cancelButton;

    /** start export button */
    private JButton okButton;
	protected JPanel tierButtonPanel;
	protected JButton downButton;
	protected JButton upButton;
	protected JButton allButton;
	protected JButton noneButton;
    /** table for tiers */
    protected DefaultTableModel model;
    /** panel for a tier table */
    protected JPanel tierSelectionPanel;
    
    protected final JTabbedPane selectTiersTabPane = new JTabbedPane();  

    /** table ui */
    protected JTable tierTable;
    /** column id for the include in export checkbox column, invisible */
    protected final String SELECT_COLUMN = "select";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";
    
    private List<String> returnedTiers = null;

	private int currentTabIndex = 0;
	private List<String> allTierNames;		   
	private List<String> allTypeNames;
	private List<String> allPartNames;
	private List<String> allAnnNames;
	
	private List<String> selectedTypeNames;
	private List<String> selectedTierNames;
	private List<String> selectedParts;
	private List<String> selectedAnns;
	
	private Transcription transcription;
	
	private List<String> hiddenTiers;	
    
    /**
     * Constructor.
     * @param owner the parent window
     * @param tierOrder current, initial tier order
     * @param selectedTiers current, initial selected tiers
     * @throws HeadlessException
     */
	public TierSortAndSelectDialog(Frame owner, Transcription trans, List<String> tierOrder,
			List<String> selectedTiers) throws HeadlessException {
		super(owner);
		allTierNames = tierOrder;
		selectedTierNames = new Vector<String>(selectedTiers);		
		transcription = trans;
		initComponents();
	}

	/**
     * Constructor.
     * @param owner the parent window
     * @param modal the modal flag
     * @param tierOrder current, initial tier order
     * @param selectedTiers current, initial selected tiers
	 * @throws HeadlessException
	 */
	public TierSortAndSelectDialog(Frame owner, boolean modal, Transcription trans, List<String> tierOrder,
			List<String> selectedTiers)
			throws HeadlessException {
		super(owner, modal);
		allTierNames = tierOrder;
		selectedTierNames = selectedTiers;
		transcription = trans;
		initComponents();
	}

	/**
     * Constructor.
     * @param owner the parent window
     * @param tierOrder current, initial tier order
     * @param selectedTiers current, initial selected tiers
	 * @throws HeadlessException
	 */
	public TierSortAndSelectDialog(Dialog owner, Transcription trans, List<String> tierOrder,
			List<String> selectedTiers) throws HeadlessException {
		super(owner);
		allTierNames = tierOrder;
		selectedTierNames = selectedTiers;
		transcription = trans;
		initComponents();
	}

	/**
     * Constructor.
     * @param owner the parent window
     * @param modal the modal flag
     * @param tierOrder current, initial tier order
     * @param selectedTiers current, initial selected tiers
	 * @throws HeadlessException
	 */
	public TierSortAndSelectDialog(Dialog owner, boolean modal, Transcription trans, List<String> tierOrder,
			List<String> selectedTiers)
			throws HeadlessException {
		super(owner, modal);
		allTierNames = tierOrder;
		selectedTierNames = selectedTiers;
		transcription = trans;
		initComponents();
	}

	/**
	 * Initializes panes, buttons and tier table 
	 */
	private void initComponents() {		   
        selectedTypeNames = new Vector<String>();        
        selectedParts = new Vector<String>();
        selectedAnns = new Vector<String>();
        allTypeNames = new Vector<String>();          
    	allPartNames = new Vector<String>();          
    	allAnnNames = new Vector<String>();   
    	hiddenTiers = new Vector();
		
		getContentPane().setLayout(new GridBagLayout());
		Insets insets = new Insets(4, 6, 4, 6);
		
		model = new TierExportTableModel();
		tierTable = new JTable(model);
		
        model.setColumnIdentifiers(new String[] { SELECT_COLUMN, TIER_NAME_COLUMN });
        tierTable.getColumn(SELECT_COLUMN)
                 .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        tierTable.getColumn(SELECT_COLUMN)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumn(SELECT_COLUMN).setMaxWidth(30);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);
        tierTable.getSelectionModel().addListSelectionListener(this);
        
        TierImpl tier;
        String value;
        
        if (allTierNames != null) {
            for (String name : allTierNames) {
                if (selectedTierNames != null) {
                    if (selectedTierNames.contains(name)) {
                        model.addRow(new Object[] { Boolean.TRUE, name });
                    } else {
                        model.addRow(new Object[] { Boolean.FALSE, name });
                    }
                } else {
                    model.addRow(new Object[] { Boolean.TRUE, name });
                }
                
                tier = (TierImpl) transcription.getTierWithId(name);  
               
        		value = tier.getParticipant();
        		if(value.length()==0){
        			value = NOT_SPECIFIED;
        		}
                
        		if(!allPartNames.contains(value)){
        			allPartNames.add(value);
        		}	
        		
        		value =tier.getAnnotator();
    			if(value.length() == 0){
    				value = NOT_SPECIFIED;
    			}
    			
    			if(!allAnnNames.contains(value)){
        			allAnnNames.add(value);
        		}
       			
       			value =tier.getLinguisticType().getLinguisticTypeName();
       			if(!allTypeNames.contains(value)){
       				allTypeNames.add(value);
        		}
            }
        }
        
        Dimension tableDim = new Dimension(50, 150);
        JScrollPane tierScrollPane = new JScrollPane(tierTable);        
        selectTiersTabPane.setPreferredSize(tableDim);        
        selectTiersTabPane.addMouseListener(this); 
        selectTiersTabPane.add(BY_TIER, tierScrollPane);
        
        JPanel checkboxPanel = new JPanel();
        JCheckBox checkbox;
        BoxLayout box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);         
        for (int i = 0; i < allTypeNames.size(); i++) {        	
        	checkbox = new JCheckBox(allTypeNames.get(i));
        	checkboxPanel.add(checkbox);
        }
        selectTiersTabPane.add(BY_TYPE ,new JScrollPane(checkboxPanel));
    
        checkboxPanel = new JPanel();    
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);     
        for (int i = 0; i < allPartNames.size(); i++) {                
        	checkbox = new JCheckBox(allPartNames.get(i));
        	checkboxPanel.add(checkbox);
        }
        selectTiersTabPane.addTab(BY_PART , new JScrollPane(checkboxPanel));
  
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box); 
        for (int i = 0; i < allAnnNames.size(); i++) {                
        	checkbox = new JCheckBox(allAnnNames.get(i));
       		checkboxPanel.add(checkbox);
        }
        selectTiersTabPane.addTab(BY_ANN, new JScrollPane(checkboxPanel));    
        
		tierSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tierSelectionPanel.add(selectTiersTabPane, gridBagConstraints);      
        

        // add more elements to this panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierSelectionPanel, gridBagConstraints);
		
        // add more 
        upButton = new JButton();
        downButton = new JButton();
        allButton = new JButton(ElanLocale.getString("Button.SelectAll"));
        noneButton = new JButton(ElanLocale.getString("Button.SelectNone"));
        tierButtonPanel = new JPanel(new GridBagLayout());
        
        try {
            ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
            ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
            upButton.setIcon(upIcon);
            downButton.setIcon(downIcon);
        } catch (Exception ex) {
            upButton.setText("Up");
            downButton.setText("Down");
        }
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        allButton.addActionListener(this);
        noneButton.addActionListener(this);
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(upButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(downButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(4, 20, 4, 2);
        tierButtonPanel.add(allButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(noneButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(tierButtonPanel, gridBagConstraints);
        
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
        
        pack();
        int minW = 300;
        int minH = 400;
        setSize(Math.max(minW, getWidth()), Math.max(minH, getHeight()));
	}
	    
    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers
     */
    public List<String> getSelectedTiers() {
        return returnedTiers;
    }
    
    /**
     * Returns all tiers in the current order.
     * 
     * @return the list of all tiers
     */
    public List<String> getTierOrder() {
    	int nameCol = model.findColumn(TIER_NAME_COLUMN);
    	ArrayList<String> orderedTiers = new ArrayList<String>();
    	
        for (int i = 0; i < model.getRowCount(); i++) {
        	orderedTiers.add((String) model.getValueAt(i, nameCol));
        }
        
    	return orderedTiers;
    }
    
    /**
     * Returns the list of hidden ties of a 
     * certain group(type /participants/ annotators)
     * 
     * @return
     */
    public List getHiddenTiers(){    	
    	Vector<String> hiddenTiers = new Vector<String>();
    	if(selectedTypeNames.size() > 0){
    		Vector tiers = new Vector();
    		for(int i=0; i< selectedTypeNames.size(); i++){
    			tiers.addAll(transcription.getTiersWithLinguisticType(selectedTypeNames.get(i)));
    		}
    		
    		TierImpl t;
    		for(int i=0; i < tiers.size(); i++){
    			t = (TierImpl) tiers.get(i);
    			if(!selectedTierNames.contains(t.getName())){
    				hiddenTiers.add(t.getName());
    			}
    		}
    	} else if(selectedParts.size() > 0){    		
    		for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId((String) allTierNames.get(i));            	
            	String partName = tier.getParticipant();            	
            	if(partName.length() == 0){
            		partName = NOT_SPECIFIED;
            	}
            	if(selectedParts.contains(partName)){
            		if(!selectedTierNames.contains(tier.getName())){
            			hiddenTiers.add(tier.getName());
            		}
            	}
            }
    	} else if(selectedAnns.size() > 0){
    		for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId((String) allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(selectedAnns.contains(annName)){
            		if(!selectedTierNames.contains(tier.getName())){
            			hiddenTiers.add(tier.getName());
            		}
            	}
            } 
    	}
    	
    	if(hiddenTiers.size() > 0){
    		return hiddenTiers;
    	}      	
    	return null;
    }
    
    /** 
     *Sets and removes the hidden tiers from selection
     * 
     * @param hiddenTiers
     */
    private void setHiddenTiers(List hiddenTiers){   
    	if(hiddenTiers == null){
    		return;
    	}
    	
    	this.hiddenTiers = hiddenTiers;
    	
    	TierImpl t;
    	String value;
    	String selectionMode = this.selectTiersTabPane.getTitleAt(currentTabIndex);
    	if(selectionMode.equals(BY_ANN)){
    		selectedAnns.clear();
    		for(int i=0; i< selectedTierNames.size(); i++){
    			t = (TierImpl) transcription.getTierWithId(selectedTierNames.get(i));
    			value = t.getAnnotator();
    			
                if(value.length() == 0){
                	value = NOT_SPECIFIED;
                }                
                if(!selectedAnns.contains(value)){
                	selectedAnns.add(value);
                }
    		}    		
    		
         	selectedTierNames.clear();
         	for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId(allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(selectedAnns.contains(annName) && !hiddenTiers.contains(tier.getName())){
            		selectedTierNames.add(tier.getName());
            	}
            } 
            selectedTypeNames.clear();  
            selectedParts.clear(); 
    	}else if(selectionMode.equals(BY_TYPE)){
    		selectedTypeNames.clear();
    		for(int i=0; i< selectedTierNames.size(); i++){
    			t = (TierImpl) transcription.getTierWithId(selectedTierNames.get(i));
    			value = t.getLinguisticType().getLinguisticTypeName();
                
                if(!selectedTypeNames.contains(value)){
                	selectedTypeNames.add(value);
                }
    		}
    		
    		selectedTierNames.clear();
        	Vector visibleTiers;
        	TierImpl tier ;
        	for(int i=0; i< selectedTypeNames.size(); i++){
        		String typeName = selectedTypeNames.get(i);
        		visibleTiers = transcription.getTiersWithLinguisticType(typeName);
        		if(visibleTiers != null){
        			for(int x=0; x< visibleTiers.size(); x++){   
        				tier = (TierImpl)visibleTiers.get(x);        				
        				if(!hiddenTiers.contains(tier.getName())){
        					selectedTierNames.add(tier.getName());
        				}  
        			}
        		}
        	}
            selectedAnns.clear();  
            selectedParts.clear(); 
    	}else if(selectionMode.equals(BY_PART)){
    		selectedParts.clear();
    		for(int i=0; i< selectedTierNames.size(); i++){
    			t = (TierImpl) transcription.getTierWithId(selectedTierNames.get(i));
    			value = t.getParticipant();
    			
                if(value.length() == 0){
                	value = NOT_SPECIFIED;
                }
                
                if(!selectedParts.contains(value)){
                	selectedParts.add(value);
                }
    		}
    		
    		selectedTierNames.clear();        	
            for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId(allTierNames.get(i));            	
            	String partName = tier.getParticipant();            	
            	if(partName.length() == 0){
            		partName = NOT_SPECIFIED;
            	}
            	if(selectedParts.contains(partName) && !hiddenTiers.contains(tier.getName())){
            		selectedTierNames.add(tier.getName());
            	}
            }
            selectedTypeNames.clear();  
        	selectedAnns.clear();
        	
    		currentTabIndex = selectTiersTabPane.indexOfTab(BY_PART);    
    	} else {
    		//currentTabIndex = selectTiersTabPane.indexOfTab(BY_TIER); 
    		return;
    	}
    	updateTabAtIndex(currentTabIndex);
    }
    
    /**
     * Sets the selection mode and also the tiers hidden in that mode
     * 
     * @param setSelectType
     */
    public void setSelectedMode(String selectionMode, List hiddenTiers){     
    	if(selectionMode == null){
    		return;
    	}
    	if(selectionMode.equals(BY_ANN)){  
            currentTabIndex = selectTiersTabPane.indexOfTab(BY_ANN);    		
    	}else if(selectionMode.equals(BY_TYPE)){    		
            currentTabIndex = selectTiersTabPane.indexOfTab(BY_TYPE);    
    	}else if(selectionMode.equals(BY_PART)){    		
        	
    		currentTabIndex = selectTiersTabPane.indexOfTab(BY_PART);    
    	} else {
    		currentTabIndex = selectTiersTabPane.indexOfTab(BY_TIER);     		
    	}    	
    	selectTiersTabPane.setSelectedIndex(currentTabIndex);
    	setHiddenTiers(hiddenTiers);
    }
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators)
     * 
     * @return
     */
    public String getSelectionMode(){
    	if(selectedTypeNames.size() > 0){
    		return BY_TYPE;
    	} else if(selectedParts.size() > 0){
    		return BY_PART;
    	} else if(selectedAnns.size() > 0){
    		return this.BY_ANN;
    	}
    	return BY_TIER;
    }
    
    
    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveDown() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (model.getRowCount() - 1)) &&
                    !tierTable.isRowSelected(row + 1)) {
                model.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveUp() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = 0; i < selected.length; i++) {
            int row = selected[i];

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
                model.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }

    /**
     * The action event handling.
     */
	public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == allButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.TRUE, i, 0);
                }
            }
        } else if (ae.getSource() == noneButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.FALSE, i, 0);
                }
            }
        } else if (ae.getSource() == upButton) {
            moveUp();
        } else if (ae.getSource() == downButton) {
            moveDown();
        } else if (ae.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (ae.getSource() == okButton) {
        	updateChanges(currentTabIndex);
            returnedTiers = this.selectedTierNames;
            setVisible(false);
            dispose();
        } else {
        	updateChanges(currentTabIndex);
        }
	}

	/**
	 * Selection of rows in the table.
	 */
	public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model.findColumn(SELECT_COLUMN);

            for (int i = b; i <= e; i++) {
                if (tierTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
	}
	
	 /**
     * 
     */
    private void showTiersTab(){     	
    	 int includeCol = model.findColumn(SELECT_COLUMN);
         int nameCol = model.findColumn(TIER_NAME_COLUMN);
         
    	for (int i = 0; i < model.getRowCount(); i++) {
    		Object value = model.getValueAt(i, nameCol);
    		if(selectedTierNames.contains(value.toString())){
    			model.setValueAt(Boolean.TRUE, i, includeCol);
    		} else {
    			model.setValueAt(Boolean.FALSE, i, includeCol);
    		}
    	}
    	
    	if (model.getRowCount() > 1) {
            upButton.setEnabled(true);
            downButton.setEnabled(true);
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }
    
    /**
     * 
     */
    private void showTypesTab(){
    	int index = selectTiersTabPane.indexOfTab(BY_TYPE);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(selectedTypeNames.contains(((JCheckBox) boxes[i]).getText())){
            		((JCheckBox) boxes[i]).setSelected(true);
            	} else {
            		((JCheckBox) boxes[i]).setSelected(false);
            	}
            }
        }        
        upButton.setEnabled(false);
        downButton.setEnabled(false);
    }
    
    /**
     * 
     */
    private void showParticipantsTab(){
    	int index = selectTiersTabPane.indexOfTab(BY_PART);
   		JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
   	
   		Component[] boxes = checkboxPanel.getComponents();
   		for (int i = 0; i < boxes.length; i++) {
   			if (boxes[i] instanceof JCheckBox) {
   				if(selectedParts.contains(((JCheckBox) boxes[i]).getText())){
        	 		 ((JCheckBox) boxes[i]).setSelected(true);
           		} else {
           				((JCheckBox) boxes[i]).setSelected(false);
           		}
   			}
   		}
   		
   		upButton.setEnabled(false);
        downButton.setEnabled(false);
   	}
   
    /**
     * 
     */
    private void showAnnotatorsTab(){
    	int index = selectTiersTabPane.indexOfTab(BY_ANN);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
	   	
    	Component[] boxes = checkboxPanel.getComponents();
    	for (int i = 0; i < boxes.length; i++) {
    		if (boxes[i] instanceof JCheckBox) {
    			if(selectedAnns.contains(((JCheckBox) boxes[i]).getText())){
    				((JCheckBox) boxes[i]).setSelected(true);
    			} else {
    				((JCheckBox) boxes[i]).setSelected(false);
    			}
    		}
    	}
    	
    	upButton.setEnabled(false);
        downButton.setEnabled(false);
   	}
    
    /**
     * Update the tier tab changes
     */
    private void updateTiers(){      	
    	int includeCol = model.findColumn(SELECT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        
    	Vector<String> oldselectedTierNames = new Vector<String>();
    	oldselectedTierNames.addAll(selectedTierNames);
    	selectedTierNames.clear();
    	boolean valueChanged =false;
    	
    	String tierName; 
        for (int i = 0; i < model.getRowCount(); i++) {
        	int x = model.getRowCount();
        	Boolean t = (Boolean)model.getValueAt(i, includeCol);
        	if((Boolean) model.getValueAt(i, includeCol)){
        		tierName =  (String) model.getValueAt(i, nameCol) ;
        		selectedTierNames.add(tierName);
        		if(!oldselectedTierNames.contains(tierName)){
        			if(hiddenTiers.contains(tierName)){
        				hiddenTiers.remove(tierName);
        				if(!valueChanged)
        					valueChanged = false;
        			} else{
        				valueChanged = true;
        			}
          		}
        	}
        }
        	
        if(valueChanged){
        	hiddenTiers.clear();
        	selectedTypeNames.clear();    	
            selectedParts.clear();  
            selectedAnns.clear();
        } else {
        	for(int i=0; i < oldselectedTierNames.size(); i++){
        		if(!selectedTierNames.contains(oldselectedTierNames.get(i))){
        			if(!hiddenTiers.contains(oldselectedTierNames.get(i))){
        				hiddenTiers.add(oldselectedTierNames.get(i));
        			}
        		}
        	}
        }
    }
    
    /**
     * Update the types tab changes
     */
    private void updateLinguisticTypes(){
    	int index = selectTiersTabPane.indexOfTab(BY_TYPE);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedTypes = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String typeName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedTypes.contains(typeName)){
            			selectedTypes.add(typeName);
            		}
            	} 
            }
        } 
        
        if(selectedTypes.size() != selectedTypeNames.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedTypes.size(); i++){
        		if(!selectedTypeNames.contains(selectedTypes.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }            
        
        if(changed){
        	 if(selectedTypeNames.size() == 0){
             	hiddenTiers.clear();
             }
        	selectedTypeNames = selectedTypes;
        	selectedTierNames.clear();
        	Vector visibleTiers;
        	for(int i=0; i< selectedTypeNames.size(); i++){
        		String typeName = selectedTypeNames.get(i);        		
        		visibleTiers = transcription.getTiersWithLinguisticType(typeName);
        		if(visibleTiers != null){
        			for(int x=0; x< visibleTiers.size(); x++){        				
        				selectedTierNames.add(((TierImpl)visibleTiers.get(x)).getName());
        			}
        		}
        	}        	
        	for(int i= 0; i< hiddenTiers.size(); i++){
        		if(selectedTierNames.contains(hiddenTiers.get(i))){
        			selectedTierNames.remove(hiddenTiers.get(i));
        		}
        	}
            selectedAnns.clear();  
            selectedParts.clear();                
        }
    }
    
    /**
     * Update the participants tab changes
     */
    private void updateParticipants(){
    	int index = selectTiersTabPane.indexOfTab(BY_PART);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedPart = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String partName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedPart.contains(partName)){
            			selectedPart.add(partName);
            		}
            	} 
            }
        }    
        
        if(selectedPart.size() != selectedParts.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedPart.size(); i++){
        		if(!selectedParts.contains(selectedPart.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }
        
        if(changed){
        	if(selectedParts.size() == 0){
            	hiddenTiers.clear();
            }
        	selectedTierNames.clear();
        	selectedParts = selectedPart;
        	 for(int i=0; i< allTierNames.size(); i++){
             	TierImpl tier = (TierImpl) transcription.getTierWithId((String) allTierNames.get(i));           	
             	String partName = tier.getParticipant();            	
             	if(partName.length() == 0){
             		partName = NOT_SPECIFIED;
             	}
             	if(selectedParts.contains(partName)){
             		selectedTierNames.add(tier.getName());
             	}
             }
        	for(int i= 0; i< hiddenTiers.size(); i++){
        		if(selectedTierNames.contains(hiddenTiers.get(i))){
        			selectedTierNames.remove(hiddenTiers.get(i));
        		}
        	}
            selectedTypeNames.clear();  
        	selectedAnns.clear();
        }
    }
    
    /**
     * Update the annotator tab changes
     */
    private void updateAnnotators(){
    	int index = selectTiersTabPane.indexOfTab(BY_ANN);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) selectTiersTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedAnn = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String annName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedAnn.contains(annName)){
            			selectedAnn.add(annName);
            		}
            	} 
            }
        }
        
        if(selectedAnn.size() != selectedAnns.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedAnn.size(); i++){
        		if(!selectedAnns.contains(selectedAnn.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }              
        
        if(changed){
        	if(selectedAnns.size() == 0){
            	hiddenTiers.clear();
            }
         	selectedAnns = selectedAnn;
         	selectedTierNames.clear();
         	for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) transcription.getTierWithId((String) allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(selectedAnns.contains(annName)){
            		selectedTierNames.add(tier.getName());
            	}
            }         	
         	for(int i= 0; i< hiddenTiers.size(); i++){
        		if(selectedTierNames.contains(hiddenTiers.get(i))){
        			selectedTierNames.remove(hiddenTiers.get(i));
        		}
        	}
            selectedTypeNames.clear();  
            selectedParts.clear();                
        }
    }   
    
    private void updateChanges(int index){
    	String tabName = selectTiersTabPane.getTitleAt(index);
    	
    	if(tabName.equals(BY_TYPE)){
    		updateLinguisticTypes();
    	} else if(tabName.equals(BY_TIER)){
    		updateTiers();
    	} else if(tabName.equals(BY_PART)){
    		updateParticipants();
    	} else if(tabName.equals(BY_ANN)){
    		updateAnnotators();
    	} 
    }
    
    private void updateTabAtIndex(int index){
    	String tabName = selectTiersTabPane.getTitleAt(index);
    	
    	if(tabName.equals(BY_TYPE)){
    		showTypesTab();
    	} else if(tabName.equals(BY_TIER)){
    		showTiersTab();
    	} else if(tabName.equals(BY_PART)){
    		showParticipantsTab();
    	} else if(tabName.equals(BY_ANN)){
    		showAnnotatorsTab();
    	} 
    }
    
    public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() == selectTiersTabPane) {			
			updateChanges(currentTabIndex);
		}	
	}

	public void mouseReleased(MouseEvent e) {	
		if (e.getSource() == selectTiersTabPane) {
			currentTabIndex = selectTiersTabPane.getSelectedIndex();
			updateTabAtIndex(currentTabIndex);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}

}
