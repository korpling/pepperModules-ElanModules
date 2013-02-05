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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class CreateAnnsOnDependentTiersStep1 extends StepPane implements MouseListener, ListSelectionListener {
	
	private TranscriptionImpl transcription;
	
	// ui elements
	
    private JTable tierTable;
    private TierExportTableModel model;  
    
    /**
     * Constructor.
     *
     * @param multiPane the enclosing MultiStepPane
     * @param trans a current transcription
     */
    public CreateAnnsOnDependentTiersStep1(MultiStepPane multiPane,
        TranscriptionImpl trans) {
        super(multiPane);          
        transcription = trans;
        initComponents();
        extractTiers();           
    }

    /**
     * Initializes ui components.
     */
    public void initComponents() {
    	 setLayout(new GridBagLayout());
         setBorder(new EmptyBorder(12, 12, 12, 12));   
         
         model = new TierExportTableModel();
         model.setColumnCount(2);
         tierTable = new JTable(model);

         DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
         tierTable.getColumnModel().getColumn(0).setCellEditor(cellEd);
         tierTable.getColumnModel().getColumn(0)
                  .setCellRenderer(new CheckBoxTableCellRenderer());
         tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
         tierTable.setShowVerticalLines(false);
         tierTable.setTableHeader(null);  
         
         tierTable.addMouseListener(this);
         tierTable.getSelectionModel().addListSelectionListener(this);
        
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
    }
    
    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
        if (transcription != null) {
            Vector v = transcription.getTiers();
            TierImpl t;
            boolean selectFirstTier = false;

            for (int i = 0; i < v.size(); i++) {
                t = (TierImpl) v.get(i);                
                if (t.getChildTiers().size() > 0){
                	// selects the first tier in the list
                	if (!selectFirstTier) {
                		model.addRow(new Object[] { Boolean.TRUE, t.getName() });
                		selectFirstTier = true;
                	} else{
                		model.addRow(new Object[] { Boolean.FALSE, t.getName() });
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
    private List getSelectedTiers() {
        List tiers = new ArrayList();
        Object selObj = null;
        Object nameObj = null;
        
        for (int i = 0; i < tierTable.getRowCount(); i++) {
            selObj = tierTable.getValueAt(i, 0);

            if (selObj == Boolean.TRUE) {
                nameObj = tierTable.getValueAt(i, 1);
                TierImpl t = (TierImpl) transcription.getTierWithId(nameObj.toString());
                if (nameObj != null && t !=null ) {         
                	if(t.hasParentTier()){
                		if(!tiers.contains(t.getParentTier().getName()))
                    		tiers.add(nameObj);
                	} else {
                		tiers.add(nameObj);
                	}
                }
            }
        }

        return tiers;
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("CreateAnnsOnDependentTiersDlg.Title");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        // the next button is already disabled   
    	List selectedTiers = getSelectedTiers();
    	if(selectedTiers.size() > 0){
    		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    	}
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
    }

    /**     * 
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {    	
    	multiPane.putStepProperty("SelectedParentTiers", getSelectedTiers());   
        return true;
    }
    
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {	
	}

	public void mouseReleased(MouseEvent e) {
		List selectedTiers = getSelectedTiers();
    	if(selectedTiers.size() > 0){
    		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    	}   else {
    		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
    	}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {	
	}

	public void valueChanged(ListSelectionEvent lse) {
		if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = 0;
            
            for (int i = b; i <= e; i++) {
                if (tierTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
		
	}
       

}
