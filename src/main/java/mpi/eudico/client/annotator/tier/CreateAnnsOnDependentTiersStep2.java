package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.RadioButtonCellEditor;
import mpi.eudico.client.util.RadioButtonTableCellRenderer;
import mpi.eudico.client.util.SelectEnableObject;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class CreateAnnsOnDependentTiersStep2 extends StepPane implements MouseListener{
	private TranscriptionImpl transcription;
	
    // the Command
    private Command com;
    
    private JTable tierTable;
    private DefaultTableModel model;   
    
    private JCheckBox overWriteCB;   
    
    private List selectedParentTiers;    
    private List emptyAnnTierList;
	private List annWithValTierList;
    
       /** column id for the tier name column */
    private final String TIER_NAME_COLUMN = "Tiers";
    
    /** column id for the tier name column */
    private final String EMPTY_ANNOTATION_COLUMN = "Empty Annotations";
    
    /** column id for the tier name column */
    private final String ANNOTATION_WITH_VAL_COLUMN = "Annotation With Value of Parent";

    /**
     * Creates a new MergeStep2 instance.
     *
     * @param multiPane the enclosing MultiStepPane
     */
    public CreateAnnsOnDependentTiersStep2(MultiStepPane multiPane, TranscriptionImpl trans) {
        super(multiPane);    
        emptyAnnTierList = new  ArrayList();
    	annWithValTierList = new  ArrayList();
        transcription = trans;
        initComponents();                
    }
    /**
     * Initializes the components of the step ui.
     */
    public void initComponents() {        
        
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));   
        
        model = new TierExportTableModel();        		
        model.setColumnCount(4);        
        model.setColumnIdentifiers(new String[] { "", TIER_NAME_COLUMN, EMPTY_ANNOTATION_COLUMN, ANNOTATION_WITH_VAL_COLUMN });        
       
        tierTable = new JTable(model);

        DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
        tierTable.getColumnModel().getColumn(0).setCellEditor(cellEd);
        tierTable.getColumnModel().getColumn(0)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
        
        tierTable.getColumn(EMPTY_ANNOTATION_COLUMN).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));
        tierTable.getColumn(EMPTY_ANNOTATION_COLUMN).setCellRenderer(new RadioButtonTableCellRenderer());
        tierTable.getColumn(EMPTY_ANNOTATION_COLUMN).setWidth(75);
       
        tierTable.getColumn(ANNOTATION_WITH_VAL_COLUMN).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));
        tierTable.getColumn(ANNOTATION_WITH_VAL_COLUMN).setCellRenderer(new RadioButtonTableCellRenderer());
        tierTable.getColumn(ANNOTATION_WITH_VAL_COLUMN).setWidth(150);
        
        tierTable.addMouseListener(this);
        
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();  
       
        Dimension tableDim = new Dimension(450, 100);
        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tierScrollPane, gridBagConstraints);  
        
        overWriteCB = new JCheckBox(ElanLocale.getString("CreateAnnsOnDependentTiersDlg.Label.Overwrite"));
        overWriteCB.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        add(overWriteCB, gridBagConstraints);  
    }
    
    
    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
    	model.setRowCount(0);
        if (transcription != null) {        	
        	for(int i = 0; i < selectedParentTiers.size(); i++){
        		TierImpl t = (TierImpl) transcription.getTierWithId((String)selectedParentTiers.get(i));   
        		Vector v = t.getDependentTiers();
        		
        		for (int x = 0; x < v.size(); x++) {
        			 t = (TierImpl) v.get(x); 
        			 SelectEnableObject emptySEO = new SelectEnableObject("", true , false);
        			 SelectEnableObject withValSEO = new SelectEnableObject("", false , false);  
        			 if (i == 0 && x==0) { 
        				 emptySEO.setEnabled(true);
        				 withValSEO.setEnabled(true);
                       	model.addRow(new Object[] { Boolean.TRUE, t.getName(), emptySEO, withValSEO });
                     } else{
                     	model.addRow(new Object[] { Boolean.FALSE, t.getName(), emptySEO, withValSEO });
                     }
        		}
        	}
        }                     	
    }    
    
    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private void updateSelectedTierList() {  
    	Object selObj = null;
    	Object nameObj = null;   
    	
    	emptyAnnTierList.clear();
    	annWithValTierList.clear();
    	for (int i = 0; i < tierTable.getRowCount(); i++) {
            selObj = tierTable.getValueAt(i, 0);

            if (selObj == Boolean.TRUE) {
            	SelectEnableObject emptyAnnSEO = (SelectEnableObject) tierTable.getValueAt(i, 2);
            	nameObj = tierTable.getValueAt(i, 1);   
            	if (nameObj != null) {
            		if(emptyAnnSEO.isSelected()){
            			emptyAnnTierList.add(nameObj);
                    }else{
                    	annWithValTierList.add(nameObj);
                    }
            	}
            }
        }
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("CreateAnnsOnDependentTiersDlg.Title");
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
    	selectedParentTiers = (List) multiPane.getStepProperty("SelectedParentTiers");
    	extractTiers();   
    	multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
    	multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
	    multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
	    multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);    	
    }

    /**
     * Notification that this step will become the active step, moving down.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
    }

    /**
     * Notification that this step will no longer be the active step, moving
     * up.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
        return true;
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    public boolean leaveStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        overWriteCB.setEnabled(false);
        return true;
    }

    /**
     * Store selected tiers, check the overwrite checkbox, create a command,
     * register as listener and activate the progress ui.
          
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    public boolean doFinish() {
    	updateSelectedTierList();
    	
        Object[] args = new Object[] { emptyAnnTierList, annWithValTierList, overWriteCB.isSelected() };
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ANN_ON_DEPENDENT_TIER);

        command.execute(transcription, args);
        return true;
    }
       
	public void mouseClicked(MouseEvent e) {
		updateSelectedTierList();
    	if(emptyAnnTierList.size() > 0 || annWithValTierList.size() >0){
    		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
    	}   else {
    		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
    	}
    	if(annWithValTierList.size() > 0){
    		overWriteCB.setEnabled(true);    	
    	} else {
    		overWriteCB.setEnabled(false);    	
    	}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		int selectedRowIndex =tierTable.getSelectedRow();
		int selectedColumnIndex = tierTable.getSelectedColumn();
		if(selectedColumnIndex == 0){		
			if((Boolean)model.getValueAt(selectedRowIndex,0) ){
				((SelectEnableObject)model.getValueAt(selectedRowIndex,2)).setEnabled(true);
				((SelectEnableObject)model.getValueAt(selectedRowIndex,3)).setEnabled(true);
			} else {
				((SelectEnableObject)model.getValueAt(selectedRowIndex,2)).setEnabled(false);
				((SelectEnableObject)model.getValueAt(selectedRowIndex,3)).setEnabled(false);				
			}
		}
		
		if( tierTable.getValueAt(selectedRowIndex, selectedColumnIndex) instanceof SelectEnableObject){			
			SelectEnableObject seo1 = (SelectEnableObject) tierTable.getValueAt(selectedRowIndex, selectedColumnIndex);
			if(seo1.isSelected()){
				SelectEnableObject seo2 = null;
				if(selectedColumnIndex==2){
					seo2 = (SelectEnableObject) tierTable.getValueAt(selectedRowIndex, 3);
				} else if(selectedColumnIndex==3) {
					seo2 = (SelectEnableObject) tierTable.getValueAt(selectedRowIndex, 2);
				}
				if(seo2 != null && seo2.isEnabled()){
					seo2.setSelected(false);
					//seo2.setSelected(!seo1.isSelected());
				}
			} else {
				seo1.setSelected(true);
			}
		}
		
		tierTable.repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
}

