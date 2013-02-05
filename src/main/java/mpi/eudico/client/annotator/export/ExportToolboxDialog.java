package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.ToolboxEncoder;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.SelectableObject;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.MarkerRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxTypFile;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * An export dialog for exporting tiers to a Shoebox/Toolbox file.
 *
 * @author Han Sloetjes
 */
public class ExportToolboxDialog extends AbstractTierExportDialog
    implements  ItemListener, ListSelectionListener {
    private JButton typButton;
    private JCheckBox blankLineCB;
    //private JCheckBox allUnicodeCB;
    private JCheckBox correctTimesCB;
    private JCheckBox wrapBlocksCB;
    private JCheckBox wrapLinesCB;
    private JCheckBox includeEmptyLinesCB;

    /** ui elements */
    private JLabel charPerLineLabel;
    private JLabel toolboxDBTypeLabel;
    private JLabel recordMarkerLabel;
    private JLabel timeFormatLabel;
    private JPanel markerPanel;
    private JPanel outerPanel;
    private JScrollPane outerScrollPane;
    private JRadioButton hhMMSSMSFormatRB;
    private JRadioButton specRB;
    private JRadioButton ssMSFormatRB;
    private JRadioButton typeRB;
    private JRadioButton wrapNextLineRB;
    private JRadioButton wrapAfterBlockRB;
    private JRadioButton detectedRMRB;
    private JRadioButton defaultRMRB;
    private JRadioButton customRMRB;
    private JTextField dbTypField;
    private JTextField numCharTF;
    private JTextField typField;
    private JTextField markerTF;
    private JCheckBox mediaMarkerCB;
    private JLabel mediaMarkerNameLabel;
    private JTextField mediaMarkerNameTF;
    private JComboBox mediaFilesCombo;
    private JRadioButton absFilePathRB;
    private JRadioButton relFilePathRB;

    // some strings
    // not visible in the table header

    /** default line width */
    private final int NUM_CHARS = 80;
    private List markers;

    // fields for the encoder
    private String databaseType;
    private String exportFileName;
    
    // count the number of root tiers after 'collapsing' or 'merging' all 
    // 'marker@part' tiers to 'marker'
    private int numRootTiers = 1;
    private String recordMarker = "";
    private ArrayList mergedTiers;
    private ArrayList markersWithBlankLine;
    
    private final String elanBeginLabel = Constants.ELAN_BEGIN_LABEL;
    private final String elanEndLabel = Constants.ELAN_END_LABEL;
    private final String elanParticipantLabel = Constants.ELAN_PARTICIPANT_LABEL;

    /**
     * Constructor.
     *
     * @param parent parent frame
     * @param modal the modal/blocking attribute
     * @param transcription the transcription to export from
     */
    public ExportToolboxDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription) {
        super(parent, modal, transcription, null);
        mergedTiers = new ArrayList();
        markersWithBlankLine = new ArrayList(5);
        makeLayout();
        extractTiers();
        postInit();
        typField.requestFocus();
    }

    /**
     *
     *
     * @param tier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierTree(TierImpl tier) {
        Vector tierTree = new Vector();
        Vector tierTrees = new Vector();

        Vector children = tier.getChildTiers();

        tierTree.add(tier);

        for (int j = 0; j < children.size(); j++) {
            TierImpl child = (TierImpl) children.elementAt(j);
            tierTrees.add(getTierTree(child));
        }

        Collections.sort(tierTrees, new VectorComparator());

        for (int j = 0; j < tierTrees.size(); j++) {
            tierTree.addAll((Vector) tierTrees.elementAt(j));
        }

        return tierTree;
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == typButton) {
            chooseTyp();
            typeRB.setSelected(true);
        } else{
        	super.actionPerformed(ae);
        }
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == wrapBlocksCB) {
            if (wrapBlocksCB.isSelected()) {
                setDefaultNumOfChars();
                numCharTF.requestFocus();
                wrapLinesCB.setEnabled(true);
            	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
            	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                wrapLinesCB.setEnabled(false);
            	wrapNextLineRB.setEnabled(false);
            	wrapAfterBlockRB.setEnabled(false);
            }
        } else if (ie.getSource() == wrapLinesCB) {
        	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
        	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
        } else if (ie.getSource() == typeRB) {
        	enableTypComponents(true);
        } else if (ie.getSource() == specRB) {
        	enableTypComponents(false);
        } else if (ie.getSource() == blankLineCB) {
        	int row = tierTable.getSelectedRow();
        	if (row > -1) {
        		Object val = model.getValueAt(row, model.findColumn(TIER_NAME_COLUMN));
        		if (val instanceof SelectableObject) {
        			((SelectableObject) val).setSelected(blankLineCB.isSelected());
        			tierTable.repaint();
        		}
        	}
        } else if (ie.getSource() == detectedRMRB || ie.getSource() == defaultRMRB) {
        	markerTF.setEnabled(false);
        } else if (ie.getSource() == customRMRB) {
        	markerTF.setEnabled(true);
        } else if (ie.getSource() == mediaMarkerCB) {
        	mediaFilesCombo.setEnabled(mediaMarkerCB.isSelected());
        	mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
        	absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        	relFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        }
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int col = model.findColumn(EXPORT_COLUMN);
            int row  = tierTable.getSelectedRow();
            
            if (row > -1) {
                if (tierTable.isRowSelected(row)) {
                    model.setValueAt(Boolean.TRUE, row, col);
                }
            	
	            Object val = model.getValueAt(row, model.findColumn(TIER_NAME_COLUMN));
	            if (val instanceof SelectableObject) {
	            	blankLineCB.setSelected(((SelectableObject) val).isSelected());
	            }
            }
        }
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {            	
            	TierImpl t;
            	List tierOrderList = mpi.eudico.client.annotator.commands.ELANCommandFactory.getViewerManager(transcription).getTierOrder().getTierOrder(); 
            	Vector<TierImpl> v = new Vector<TierImpl>();
            	for (int i = 0; i < tierOrderList.size(); i++) {
            		t = (TierImpl) transcription.getTierWithId(tierOrderList.get(i).toString());
            		if(t != null){
            			v.add(t);
            		}
            	}
            	
//            	List tierOrder = (List) Preferences.get("ExportToolbox.TierOrder", transcription);
//            	if(tierOrder == null){
//            		tierOrder = mpi.eudico.client.annotator.commands.ELANCommandFactory.getViewerManager(transcription).getTierOrder().getTierOrder();
//            	}            	
//            	
//            	if(tierOrder != null && tierOrder.size() > 0){
//            		v = new Vector();
//            		for (int i = 0; i < tierOrder.size(); i++) {
//            			t = (TierImpl) transcription.getTierWithId(tierOrder.get(i).toString());
//            			if(t != null){
//            				v.add(t);
//            			}
//            		}
//            		
//            		Vector tiers = transcription.getTiers();
//            		for(int i=0; i < tiers.size(); i++){
//            			if(!v.contains(tiers.get(i))){
//            				v.add(tiers.get(i));                     
//            			}
//            		}
//            	} else {            		
//            		v = transcription.getTiers();
//            	}
            	
                ArrayList rootTiers = new ArrayList(5);
                String tName;
                String markName;
                
                for (int i = 0; i < v.size(); i++) {
                    t = (TierImpl) v.get(i);
                    tName = t.getName();
                    int atIndex = tName.indexOf('@');
                    if (atIndex > -1) {
                    	markName = tName.substring(0, atIndex);
                    	if (!mergedTiers.contains(markName)) {
                    		mergedTiers.add(markName);
                    	}
                    	if (!t.hasParentTier()) {
                    		if (!rootTiers.contains(markName)) {
                    			rootTiers.add(markName);
                    		}
                    	}
                    } else {
                    	mergedTiers.add(tName);
                    	if (!t.hasParentTier()) {
                    		rootTiers.add(tName);
                    	}
                    }
                }
                
                numRootTiers = rootTiers.size();
                if (numRootTiers == 1) {
                	recordMarker = (String) rootTiers.get(0);
                	int index = mergedTiers.indexOf(recordMarker);
                	if (index != 0) {
                		mergedTiers.remove(index);
                		mergedTiers.add(0, recordMarker);
                	}
                	if (detectedRMRB != null) {
                		detectedRMRB.setEnabled(true);
                		detectedRMRB.setText(detectedRMRB.getText() + 
                				" (\\" + recordMarker + ")");
                	}
                } else {
                	detectedRMRB.setEnabled(false);
                	defaultRMRB.setSelected(true);
                }
                
            	List tierOrder = (List) Preferences.get("ExportToolbox.TierOrder", transcription);
            	if(tierOrder != null){
            		mergedTiers.add(elanBeginLabel);               	
                	mergedTiers.add(elanEndLabel);
                	mergedTiers.add(elanParticipantLabel); 
                	
                	// remove deleted/ changed tiers from the list
//                	for(int i=0; i < tierOrder.size(); i++){
//                		if(!mergedTiers.contains(tierOrder.get(i))){
//                			tierOrder.remove(i);
//                		}
//                	}
                	
                	// remove all the tiers not in the current tier list
                	int i=0;
                	while(i < tierOrder.size()){
                		if(!mergedTiers.contains(tierOrder.get(i))){
                    		tierOrder.remove(i);
                    	} else {
                    		i++;
                    	}
                	}
                	
                	// add all the new/changed tiers, if any
                	if(tierOrder.size() != mergedTiers.size()){
                		i=0;
                		while(mergedTiers.size() !=  tierOrder.size()){
                			if(!tierOrder.contains(mergedTiers.get(i))){
                    			tierOrder.add(mergedTiers.get(i));                    			
                    		}
                        	i++;
                    	}
//                		for(int x=0; x < mergedTiers.size(); x++ ){
//                    		if(!tierOrder.contains(mergedTiers.get(x))){
//                    			tierOrder.add(mergedTiers.get(x));
//                    			
//                    		}
//                    	}
                	}
                	
                	mergedTiers = new ArrayList(tierOrder);
            	} else{        
            		if(mergedTiers.size() >1){
            			mergedTiers.add(1, elanParticipantLabel);
                       	mergedTiers.add(1, elanEndLabel);
                       	mergedTiers.add(1, elanBeginLabel);  
            		} else{
            			mergedTiers.add(elanParticipantLabel);
            			mergedTiers.add(elanEndLabel);
            			mergedTiers.add(elanBeginLabel);  
            		}
                   
            	}
            	
//                if (mergedTiers.size() > 1) {
//                	mergedTiers.add(1, elanParticipantLabel);
//                	mergedTiers.add(1, elanEndLabel);
//                	mergedTiers.add(1, elanBeginLabel);
//                } else {
//                	mergedTiers.add(elanBeginLabel);               	
//                	mergedTiers.add(elanEndLabel);
//                	mergedTiers.add(elanParticipantLabel);              	
//                }
                
                for (int i = 0; i < mergedTiers.size(); i++) {                     
                    model.addRow(new Object[] { Boolean.TRUE, 
                    		new SelectableObject(mergedTiers.get(i), false)});
                }
                
              //read Preferences
                Object useTyp = Preferences.get("ExportToolbox.selectedTiers", transcription);                   
               	if (useTyp != null && useTyp instanceof ArrayList) {
               		 List tierList = (ArrayList)useTyp;
               	     if(tierList.size() >0 ){    	
               	    	 int includeCol = model.findColumn(EXPORT_COLUMN);
               	    	 int nameCol = model.findColumn(TIER_NAME_COLUMN);
        		 
               	    	 for(int i=0; i< model.getRowCount(); i++){
               	    		 SelectableObject obj = (SelectableObject) model.getValueAt(i, nameCol);
               	    		 if( tierList.contains(obj.getValue())){
               	    			  model.setValueAt(true, i, includeCol);
               	    		 } else {
               	    			 model.setValueAt(false, i, includeCol);
               	    		 }
               	    	 }
               	     }
               	}
            }

            if (model.getRowCount() > 1) {
                upButton.setEnabled(true);
                downButton.setEnabled(true);
            } else {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }

    /**
     * Initializes UI elements.
     */
    protected void makeLayout() {
        super.makeLayout();
        charPerLineLabel = new JLabel();
        wrapBlocksCB = new JCheckBox();
        wrapBlocksCB.setSelected(true);
        numCharTF = new JTextField(4);
        timeFormatLabel = new JLabel();
        ssMSFormatRB = new JRadioButton();
        hhMMSSMSFormatRB = new JRadioButton();
        correctTimesCB = new JCheckBox();
        blankLineCB = new JCheckBox();
        blankLineCB.addItemListener(this);
        wrapLinesCB = new JCheckBox();
        wrapLinesCB.setSelected(true);
        wrapNextLineRB = new JRadioButton();
        wrapAfterBlockRB = new JRadioButton();
        wrapNextLineRB.setSelected(true);
        ButtonGroup wrapGroup = new ButtonGroup();
        wrapGroup.add(wrapNextLineRB);
        wrapGroup.add(wrapAfterBlockRB);
        includeEmptyLinesCB = new JCheckBox();
        includeEmptyLinesCB.setSelected(true);

        toolboxDBTypeLabel = new JLabel();
        typField = new JTextField("", 23);
        typButton = new JButton("...");
        dbTypField = new JTextField("", 14);

        ButtonGroup buttonGroup = new ButtonGroup();
        typeRB = new JRadioButton();
        typeRB.setSelected(true);
        typeRB.addItemListener(this);
        specRB = new JRadioButton();
        specRB.addItemListener(this);
        buttonGroup.add(typeRB);
        buttonGroup.add(specRB);
        recordMarkerLabel = new JLabel();
        detectedRMRB = new JRadioButton();
        detectedRMRB.setSelected(true);
        defaultRMRB = new JRadioButton();
        customRMRB = new JRadioButton();
        ButtonGroup rmGroup = new ButtonGroup();
        rmGroup.add(detectedRMRB);
        rmGroup.add(defaultRMRB);
        rmGroup.add(customRMRB);
        markerTF = new JTextField("", 6);
        markerTF.setEnabled(false);
        mediaMarkerCB = new JCheckBox("");
        mediaMarkerCB.addItemListener(this);
        mediaMarkerNameLabel = new JLabel();
        mediaMarkerNameTF = new JTextField("", 6);
        mediaMarkerNameTF.setEnabled(false);
        mediaFilesCombo = new JComboBox();
        mediaFilesCombo.setEnabled(false);
        absFilePathRB = new JRadioButton();
        absFilePathRB.setSelected(true);
        relFilePathRB = new JRadioButton();
        ButtonGroup fileGroup = new ButtonGroup();
        fileGroup.add(absFilePathRB);
        fileGroup.add(relFilePathRB);

        model.setColumnIdentifiers(new String[] { EXPORT_COLUMN, TIER_NAME_COLUMN });
        tierTable.getColumn(EXPORT_COLUMN).setCellEditor(new DefaultCellEditor(
                new JCheckBox()));
        tierTable.getColumn(EXPORT_COLUMN).setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumn(TIER_NAME_COLUMN).setCellRenderer(new MarkerCellRenderer());
        tierTable.getColumn(EXPORT_COLUMN).setMaxWidth(30);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);
        tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tierTable.getSelectionModel().addListSelectionListener(this);

        outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        outerScrollPane = new JScrollPane(outerPanel);
        outerScrollPane.setBorder(null);
        
        GridBagConstraints gridBagConstraints;
        Insets vertInsets = new Insets(0, 2, 2, 2);
        Insets leftVertIndent = new Insets(0, 26, 2, 2);
        Insets innerInsets = new Insets(4, 2, 4, 2);
        JPanel updownPanel = new JPanel(new GridBagLayout());
 
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = innerInsets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        updownPanel.add(new JPanel(), gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = innerInsets;
        updownPanel.add(blankLineCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tierSelectionPanel.add(updownPanel, gridBagConstraints);
        
        getContentPane().remove(tierSelectionPanel);
        getContentPane().remove(optionsPanel);
        optionsPanel.setLayout(new GridBagLayout());
        JPanel wrapPanel = new JPanel(new GridBagLayout());
        JPanel timePanel = new JPanel(new GridBagLayout());

        wrapBlocksCB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapBlocksCB, gridBagConstraints);

        numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(numCharTF, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        wrapPanel.add(charPerLineLabel, gridBagConstraints);

        wrapLinesCB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapLinesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(wrapNextLineRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(wrapAfterBlockRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        wrapPanel.add(includeEmptyLinesCB, gridBagConstraints);
        
        // time
        ButtonGroup group = new ButtonGroup();
        group.add(ssMSFormatRB);
        ssMSFormatRB.setSelected(true);
        group.add(hhMMSSMSFormatRB);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(timeFormatLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(hhMMSSMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(ssMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(correctTimesCB, gridBagConstraints);

        // add to options panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        optionsPanel.add(wrapPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(new JPanel(), gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        optionsPanel.add(timePanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = innerInsets;
        outerPanel.add(tierSelectionPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = innerInsets;       
        outerPanel.add(optionsPanel, gridBagConstraints);

        markerPanel = new JPanel();
        markerPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(toolboxDBTypeLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(typeRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(typField, gridBagConstraints);

        typButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(typButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(specRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(dbTypField, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = innerInsets;
        markerPanel.add(recordMarkerLabel, gridBagConstraints);
     
        detectedRMRB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(detectedRMRB, gridBagConstraints);
        
        defaultRMRB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(defaultRMRB, gridBagConstraints);
        
        customRMRB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(customRMRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(markerTF, gridBagConstraints);
        
        // add media marker elements
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(mediaMarkerCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(mediaFilesCombo, gridBagConstraints);
        
        JPanel mediaPanel = new JPanel(new GridLayout(1, 2));
        mediaPanel.add(absFilePathRB);
        mediaPanel.add(relFilePathRB);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        markerPanel.add(mediaPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        markerPanel.add(mediaMarkerNameLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        markerPanel.add(mediaMarkerNameTF, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = innerInsets;
        gridBagConstraints.gridy = 2;
        outerPanel.add(markerPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(outerScrollPane, gridBagConstraints);

        setDefaultNumOfChars();

        setShoeboxMarkerRB();
        
        loadMediaFileNames();

        updateLocale();
        
        loadPreferences();
        addComponentListener(new SizeListener());
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    
    private void savePreferences(){
    	Preferences.set("ExportToolbox.WrapBlocks", wrapBlocksCB.isSelected(), null);
    	Preferences.set("ExportToolbox.CharacterPerBlocks", numCharTF.getText(), null);
    	Preferences.set("ExportToolbox.wrapLinesCB", wrapLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.wrapNextLineRB", wrapNextLineRB.isSelected(), null);
    	Preferences.set("ExportToolbox.includeEmptyLinesCB", includeEmptyLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.ssMSFormatRB", ssMSFormatRB.isSelected(), null);
    	Preferences.set("ExportToolbox.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.detectedRMRB", detectedRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.defaultRMRB", defaultRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.markerTF", markerTF.getText(), null);    	
    	Preferences.set("ExportToolbox.blankLineCB", blankLineCB.isSelected(), null);    	
    	
    	Preferences.set("ExportToolbox.selectedTiers", getSelectedTiers(), transcription);
    	
    	List prefferedTierOrder = getCurrentTierOrder();
    	//List tierOrder = mpi.eudico.client.annotator.commands.ELANCommandFactory.getViewerManager(transcription).getTierOrder().getTierOrder();    	
    	
    	for(int i=0; i< prefferedTierOrder.size(); i++){
    		if(prefferedTierOrder.get(i) != mergedTiers.get(i)){
    			Preferences.set("ExportToolbox.TierOrder", prefferedTierOrder, transcription);
    			break;
    		}
    	}
    	if (specRB.isSelected()) {
    		Preferences.set("ExportToolbox.ManualDBName", dbTypField.getText(), null);
    	}
    	Preferences.set("ExportToolbox.exportMediaMarker", mediaMarkerCB.isSelected(), null);
    	
    	if (mediaMarkerCB.isSelected()) {
    		Preferences.set("ExportToolbox.mediaMarkerName", mediaMarkerNameTF.getText(), null);
    		Preferences.set("ExportToolbox.fileForMediaMarker", mediaFilesCombo.getSelectedItem(), transcription);
    		Preferences.set("ExportToolbox.absoluteMediaFileName", absFilePathRB.isSelected(), null);
    	}
    }
    
    private void loadPreferences(){      	
    	Object useTyp = Preferences.get("ExportToolbox.WrapBlocks", null);
    	if(useTyp != null){
    		wrapBlocksCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportShoebox.numCharTF", null);
    	if(useTyp != null){
    		numCharTF.setText((String)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportShoebox.wrapLinesCB", null);
    	if(useTyp != null){
    		wrapLinesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.wrapNextLineRB", null);
    	if(useTyp != null){
    		wrapNextLineRB.setSelected((Boolean)useTyp);
    		wrapAfterBlockRB.setSelected(!(Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.includeEmptyLinesCB", null);
    	if(useTyp != null){
    		includeEmptyLinesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.ssMSFormatRB", null);
    	if(useTyp != null){
    		ssMSFormatRB.setSelected((Boolean)useTyp);
    		hhMMSSMSFormatRB.setSelected(!(Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.correctTimesCB", null);
    	if(useTyp != null){
    		correctTimesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.detectedRMRB", null);
    	if(useTyp != null){
    		detectedRMRB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.defaultRMRB", null);
    	if(useTyp != null){
    		defaultRMRB.setSelected((Boolean)useTyp);
    	}
    	
    	if(defaultRMRB.isSelected() || detectedRMRB.isSelected()){
    		customRMRB.setSelected(false);
    	}else {
    		customRMRB.setSelected(true);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.markerTF", null);
    	if(useTyp != null){
    		markerTF.setText((String)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.blankLineCB", null);
    	if(useTyp != null){
    		blankLineCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.ManualDBName", null);
    	if (useTyp != null) {
    		dbTypField.setText((String) useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.exportMediaMarker", null);
    	if(useTyp != null){
    		mediaMarkerCB.setSelected((Boolean)useTyp);// will this fire an event?
    		mediaFilesCombo.setEnabled(mediaMarkerCB.isSelected());
    		mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
    		absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
    		relFilePathRB.setEnabled(mediaMarkerCB.isSelected());
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.mediaMarkerName", null);
    	if(useTyp != null){
    		mediaMarkerNameTF.setText((String) useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.fileForMediaMarker", transcription);
    	if(useTyp != null){
    		mediaFilesCombo.setSelectedItem((String) useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.absoluteMediaFileName", null);
    	if(useTyp != null){
    		absFilePathRB.setSelected((Boolean) useTyp);
    		relFilePathRB.setSelected(!((Boolean) useTyp));
    	}
    }

    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false oherwise
     */
    protected boolean startExport() {
    	savePreferences();
    	
        if (!checkFields()) {
            return false;
        }

        List selectedTiers = getSelectedTiers();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        
        if(selectedTiers.contains(recordMarker)){
        	if(!selectedTiers.get(0).equals(recordMarker)){
        		selectedTiers.remove(recordMarker);
        		selectedTiers.add(0, recordMarker);
        	}
        }
        // check the chars per line value
        int charsPerLine = Integer.MAX_VALUE;

        if (wrapBlocksCB.isSelected()) {
            String textValue = numCharTF.getText().trim();

            try {
                charsPerLine = Integer.parseInt(textValue);
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                        "ExportShoebox.Message.InvalidNumber"));
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }

        int timeFormat = Interlinear.SSMS;

        if (hhMMSSMSFormatRB.isSelected()) {
            timeFormat = Interlinear.HHMMSSMS;
        }

        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                    "ExportShoebox.Title.Toolbox"), null, FileExtension.TOOLBOX_TEXT_EXT, false);

        if (exportFile == null) {
            return false;
        }
        exportFileName = exportFile.getPath();       

        // export....
        boolean success = doExport(exportFileName, selectedTiers, charsPerLine,
                timeFormat, correctTimesCB.isSelected());

        return success;
    }

    /**
     * Applies localized strings to the ui elements. For historic reasons the
     * string identifiers start with "TokenizeDialog"
     */
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportShoebox.Title.Toolbox"));
        titleLabel.setText(ElanLocale.getString("ExportShoebox.Title.Toolbox"));
        blankLineCB.setText(ElanLocale.getString("ExportShoebox.Button.BlankLineAfter"));
        markerPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportShoebox.Label.ToolboxOptions")));
        wrapBlocksCB.setText(ElanLocale.getString(
                "ExportShoebox.Label.WrapBlocks"));
        charPerLineLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.NumberChars"));
        wrapLinesCB.setText(ElanLocale.getString(
                "ExportShoebox.Label.WrapLines"));
        wrapAfterBlockRB.setText(ElanLocale.getString(
                "ExportShoebox.Label.WrapEndOfBlock"));
        wrapNextLineRB.setText(ElanLocale.getString(
        		"ExportShoebox.Label.WrapNextLine"));
        includeEmptyLinesCB.setText(ElanLocale.getString(
        		"ExportShoebox.Label.IncludeEmpty"));
        timeFormatLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.Format"));
        hhMMSSMSFormatRB.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.TimeCode"));
        ssMSFormatRB.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.Seconds"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        toolboxDBTypeLabel.setText(ElanLocale.getString(
        		"ExportShoebox.Label.ToolboxBDName"));
        toolboxDBTypeLabel.setToolTipText("e.g. \\_sh v3.0  400 Text");
        typeRB.setText(ElanLocale.getString("ExportShoebox.Label.Type"));
        //allUnicodeCB.setText(ElanLocale.getString(
        //        "ExportShoebox.CheckBox.AllUnicode"));
        //fieldSpecButton.setText(ElanLocale.getString(
        //        "ExportShoebox.Button.FieldSpec"));
        specRB.setText(ElanLocale.getString(
                "ExportShoebox.Label.SpecifyType"));
        //tierNamesLabel.setText(ElanLocale.getString(
        //        "ExportShoebox.Label.UseTierNames"));
        //generateMarkersCB.setText(ElanLocale.getString(
        //        "ExportShoebox.CheckBox.AutoGenerateMarkers"));
        recordMarkerLabel.setText(ElanLocale.getString(
        		"ExportShoebox.Label.RecordMarker"));
        detectedRMRB.setText(ElanLocale.getString(
        		"ExportShoebox.Label.Detected"));
        defaultRMRB.setText(ElanLocale.getString(
        		"ExportShoebox.Label.DefaultMarker") + " (\\block)");
        customRMRB.setText(ElanLocale.getString(
        		"ExportShoebox.Label.CustomMarker"));
        mediaMarkerCB.setText(ElanLocale.getString("ExportShoebox.Label.IncludeMediaMarker"));
        mediaMarkerNameLabel.setText(ElanLocale.getString("ExportShoebox.Label.MediaMarkerName"));
        absFilePathRB.setText(ElanLocale.getString("ExportShoebox.Label.AbsoluteMediaFile"));
        relFilePathRB.setText(ElanLocale.getString("ExportShoebox.Label.RelMediaFile"));
    }
    
    /**
     * Extracts the media file names from the media descriptors and adds them to
     * the combobox.
     */
    private void loadMediaFileNames() {
    	List mds = transcription.getMediaDescriptors();
    	if (mds != null && mds.size() > 0) {
    		MediaDescriptor md = null;
    		String name;
    		for (int i = 0; i < mds.size(); i++) {
    			md = (MediaDescriptor) mds.get(i);
    			name = FileUtility.fileNameFromPath(md.mediaURL);
    			if (name != null) {
    				mediaFilesCombo.addItem(name);
    			}
    		}
    	} else {
    		// disable some options
    		mediaMarkerCB.setSelected(false);
    		mediaMarkerCB.setEnabled(false);
    	}
    }

    private void setDefaultNumOfChars() {
        numCharTF.setEnabled(true);
        numCharTF.setBackground(Constants.SHAREDCOLOR4);

        if ((numCharTF.getText() != null) ||
                (numCharTF.getText().length() == 0)) {
            numCharTF.setText("" + NUM_CHARS);
        }
    }
    
    private List getMarkersWithBlankLines() {
    	List mbl = new ArrayList();
        int nameCol = model.findColumn(TIER_NAME_COLUMN);

        // add selected tiers in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            SelectableObject sob = (SelectableObject) model.getValueAt(i, nameCol);

            if (sob.isSelected()) {
                mbl.add(sob.getValue());
            }
        }
    	return mbl;
    }
/*
    private void setEnabledAllUnicode(boolean enable) {
        allUnicodeCB.setSelected(false);
        allUnicodeCB.setEnabled(enable);
    }

    private void setEnabledAutoGenerate(boolean enable) {
        generateMarkersCB.setSelected(false);
        generateMarkersCB.setEnabled(enable);
    }
*/
    private Vector getHierarchicallySortedTiers(TranscriptionImpl transcription) {
        // for each root tier, find dependency tree.
        // store in a Vector with Vectors, one for each root.
        // take the largest tier tree first, this is likely to be the interlinear tree
        Vector tierTrees = new Vector();
        Vector sortedTiers = new Vector();

        Vector topTiers = transcription.getTopTiers();

        for (int i = 0; i < topTiers.size(); i++) {
            TierImpl topTier = (TierImpl) topTiers.elementAt(i);
            tierTrees.add(getTierTree(topTier));
        }

        Collections.sort(tierTrees, new VectorComparator());

        for (int j = 0; j < tierTrees.size(); j++) {
            sortedTiers.addAll((Vector) tierTrees.elementAt(j));
        }

        return sortedTiers;
    }

    private void setShoeboxMarkerRB() {
        Object useTyp = Preferences.get("LastUsedShoeboxExport", null);

        if (useTyp == null || (useTyp instanceof String &&
                ((String) useTyp).equalsIgnoreCase("typ"))) {
            typeRB.setSelected(true);

            Object luTypFile = Preferences.get("LastUsedShoeboxTypFile", null);

            if (luTypFile instanceof String) {
                typField.setText((String) luTypFile);
            }
            enableTypComponents(true);
        } else {
        	specRB.setSelected(true);
        	enableTypComponents(false);
        }
    }

    private void enableTypComponents(boolean enable) {  	
    	typField.setEnabled(enable);
    	typButton.setEnabled(enable);
    	dbTypField.setEnabled(!enable);
    }
    
    private void autoGenerateMarkerFile() {
        // generate marker records for each tier.
        // only marker, parent marker and stereotype have to be set, rest is default
        Vector markerRecords = new Vector();

        try {
            Vector tiers = transcription.getTiers();

            for (int i = 0; i < tiers.size(); i++) {
                TierImpl t = (TierImpl) tiers.elementAt(i);

                MarkerRecord mkrRecord = new MarkerRecord();
                mkrRecord.setMarker(t.getName());

                if (t.hasParentTier()) {
                    mkrRecord.setParentMarker(t.getParentTier().getName());

                    if (t.getLinguisticType() != null) {
                        int stereotype = t.getLinguisticType().getConstraints()
                                          .getStereoType();

                        if ((stereotype == Constraint.SYMBOLIC_SUBDIVISION) ||
                                (stereotype == Constraint.TIME_SUBDIVISION) ||
                                (stereotype == Constraint.INCLUDED_IN)) {
                            //mkrRecord.setStereoType(Constraint.publicStereoTypes[2]);
                            mkrRecord.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
                        } else if (stereotype == Constraint.SYMBOLIC_ASSOCIATION) {
                            //mkrRecord.setStereoType(Constraint.publicStereoTypes[3]);
                            mkrRecord.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
                        }
                    }
                }

                mkrRecord.setCharset(MarkerRecord.UNICODESTRING);
                mkrRecord.setParticipantMarker(false);
                mkrRecord.setExcluded(false);

                markerRecords.add(mkrRecord);
            }

            // store in mkr file with name of transcription, next to eaf
            // dec 2006 HS: by default the .mkr file will now be saved next to the export file
            String fileName = ((TranscriptionImpl) transcription).getPathName();

            if (exportFileName != null) {
                fileName = exportFileName.substring(0,
                        exportFileName.lastIndexOf("."));
            } else if (fileName.toLowerCase().endsWith(".eaf")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }

            fileName += ".mkr";

            final File newSaveFile = new File(fileName);

            if (newSaveFile != null) {
                if (newSaveFile.exists()) {
                    int answer = JOptionPane.showConfirmDialog(null,
                            ElanLocale.getString("Message.Overwrite") + "\n" +
                            fileName,
                            ElanLocale.getString("SaveDialog.Message.Title"),
                            JOptionPane.YES_NO_OPTION);

                    if (answer == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                FileOutputStream out = new FileOutputStream(newSaveFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            out, "UTF-8"));

                Iterator markerIter = markerRecords.iterator();

                while (markerIter.hasNext()) {
                    writer.write(((MarkerRecord) markerIter.next()).toString());
                }

                writer.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the contents of marker input fields and next the existence of the
     * designated files.
     *
     * @return true if the files exist, false otherwise
     */
    private boolean checkFields() {
    	// database type check
        if (typeRB.isSelected() &&
                ((typField.getText() == null) ||
                (typField.getText().length() == 0))) {
            showError(ElanLocale.getString("ImportDialog.Message.SpecifyType"));

            return false;
        }

        if (typeRB.isSelected()) {
            File tf = new File(typField.getText());

            if (!tf.exists()) {
                showError(ElanLocale.getString("ImportDialog.Message.NoType"));

                return false;
            } else {
                try {
                    ShoeboxTypFile typFile = new ShoeboxTypFile(tf);
                    databaseType = typFile.getDatabaseType();
                } catch (Exception e) {
                }
            }
        } else {
            databaseType = dbTypField.getText();
            if (databaseType == null || databaseType.trim().length() == 0) {
            	showError(ElanLocale.getString("ExportShoebox.Message.NoType"));
            	dbTypField.requestFocus();
            	
            	return false;
            }
        }
        // record marker test
        if (customRMRB.isSelected()) {
        	String custRM = markerTF.getText();
        	if (custRM == null || custRM.trim().length() == 0) {
        		showError(ElanLocale.getString("ExportShoebox.Message.NoRecordMarker"));
        		markerTF.requestFocus();
        		
        		return false;
        	} else {
        		recordMarker = custRM.trim();
        	}
        } else if (defaultRMRB.isSelected()) {
        	recordMarker = "block"; // should be a constant from elsewhere
        } // otherwise the record marker has been detected from the transcription
        
        //media marker 
        if(mediaMarkerCB.isSelected()){
        	String mediaMarker = mediaMarkerNameTF.getText();
    		if(mediaMarker == null
    				|| mediaMarker.trim().length() <= 0){
    			showError(ElanLocale.getString("ExportShoebox.Message.NoMediaMarker"));
    			mediaMarkerNameTF.requestFocus();
        		return false;
    		}
    	}

        return true;
    }

    private void chooseTyp() {      
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
        		null, FileExtension.SHOEBOX_TYP_EXT, false, "LastUsedShoeboxTypDir", FileChooser.FILES_ONLY, null);
        File f = chooser.getSelectedFile();
        if (f != null) {
            typField.setText(f.getAbsolutePath());
        }
    }

    //******************************
    // actual export methods from here, for the time being
    //******************************

    /**
     * The actual writing.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1
     * @param charsPerLine num of chars per line if linewrap is selected
     * @param timeFormat the time format, a constant from Interlinear
     * @param correctTimes if true the master media time offset will be 
     * added to all time values
     *
     * @return true if all went well, false otherwise
     */
    private boolean doExport(final String fileName, final List orderedTiers,
        final int charsPerLine, final int timeFormat, final boolean correctTimes) {
        int markerSource = ToolboxEncoderInfo.TIERNAMES; // default

        if (typeRB.isSelected()) {
            markerSource = ToolboxEncoderInfo.TYPFILE;
            Preferences.set("LastUsedShoeboxExport", "typ", null);
            Preferences.set("LastUsedShoeboxTypFile", typField.getText(), null);
        } else {
            Preferences.set("LastUsedShoeboxExport", "", null);
        }

        ToolboxEncoderInfo tbEncoderInfo = new ToolboxEncoderInfo(charsPerLine,
                markerSource, timeFormat);
        tbEncoderInfo.setCorrectAnnotationTimes(correctTimes);

        if (databaseType != null) {
            tbEncoderInfo.setDatabaseType(databaseType);
        }
        // the new options
        if (charsPerLine != Integer.MAX_VALUE) {
	        tbEncoderInfo.setWrapLines(wrapLinesCB.isSelected());
	        if (wrapLinesCB.isSelected()) {
	        	if (wrapNextLineRB.isSelected()) {
	        		tbEncoderInfo.setLineWrapStyle(Interlinear.NEXT_LINE);
	        	} else {
	        		tbEncoderInfo.setLineWrapStyle(Interlinear.END_OF_BLOCK);
	        	}
	        } else {
	        	tbEncoderInfo.setLineWrapStyle(Interlinear.NO_WRAP);
	        }
        } else {
        	// no block and no line wrapping
        	tbEncoderInfo.setWrapLines(false);
        	tbEncoderInfo.setLineWrapStyle(Interlinear.NO_WRAP);
        }
        
        if (correctTimesCB.isSelected()) {
            List mds = transcription.getMediaDescriptors();

            if ((mds != null) && (mds.size() > 0)) {
                long mediaOffset = ((MediaDescriptor) mds.get(0)).timeOrigin;
                tbEncoderInfo.setTimeOffset(mediaOffset);
            }        	
        }
        tbEncoderInfo.setIncludeEmptyMarkers(includeEmptyLinesCB.isSelected());
        tbEncoderInfo.setRecordMarker(recordMarker);
        tbEncoderInfo.setOrderedVisibleTiers(orderedTiers);
        tbEncoderInfo.setMarkersWithBlankLines(getMarkersWithBlankLines());
        
        boolean includeMediaMarker = mediaMarkerCB.isSelected();
        if (includeMediaMarker) {
        	tbEncoderInfo.setIncludeMediaMarker(true);
        	tbEncoderInfo.setMediaMarker(mediaMarkerNameTF.getText());
        
        	String selFileName = (String) mediaFilesCombo.getSelectedItem();
        	if (relFilePathRB.isSelected()) {
        		tbEncoderInfo.setMediaFileName(selFileName);
        	} else {
	        	List mds = transcription.getMediaDescriptors();
	        	MediaDescriptor md;
	        	// or use the index of the selected media file to get the descriptor?
	        	for (int i = 0; i < mds.size(); i++) {
	        		md = (MediaDescriptor) mds.get(i);
	        		if (md.mediaURL != null && md.mediaURL.endsWith(selFileName)) {
	        			String fileURL = FileUtility.urlToAbsPath(md.mediaURL);
	        			int numSlash = 0;
	        			for (int j = 0; j < fileURL.length(); j++) {
	        				if (fileURL.charAt(j) == '/') {
	        					numSlash++;
	        				} else {
	        					break;
	        				}
	        			}
	        			if (numSlash != 0 && numSlash !=2 ) {
	        				fileURL = fileURL.substring(numSlash);
	        			}
	        			tbEncoderInfo.setMediaFileName(fileURL.replace('/', '\\'));
	        			break;
	        		}
	        	}
        	}
        }
        
        if (fileName != null) {
            try {
            	ToolboxEncoder encoder = new ToolboxEncoder();
                encoder.encodeAndSave(transcription,
                        tbEncoderInfo, orderedTiers, fileName);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);   
            }
        }

        return true;
    }    
    
    protected List getSelectedTiers() {
        int includeCol = model.findColumn(EXPORT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);

        ArrayList selectedTiers = new ArrayList();

        // add selected tiers in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                selectedTiers.add(((SelectableObject) model.getValueAt(i, nameCol)).getValue());
            }
        }

        return selectedTiers;
    }
    
    /**
     * Returns the current tierOrder of this export
     * 
     * @ return  tierOrder, list<string> 
     */
    private List getCurrentTierOrder() {
        int includeCol = model.findColumn(EXPORT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);

        ArrayList tierOrder = new ArrayList();
      
        //tiers in the right order
        for (int i = 0; i < model.getRowCount(); i++) { 
        	tierOrder.add(((SelectableObject) model.getValueAt(i, nameCol)).getValue());
        }
        return tierOrder;
    }
    
    /**
     * Shows an error dialog.
     *
     * @param message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    }
/*
    private void specifyFieldSpecs() {
        ShoeboxMarkerDialog smd = new ShoeboxMarkerDialog(null, true);
        smd.setVisible(true);
        markers = smd.getMarkers();
    }
*/    

    //***********************
    // inner classes
    //***********************	
    class VectorComparator implements Comparator {
        /**
         * Compares Vectors, on basis of their size. The largest one comes
         * first
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {
            Vector v0 = (Vector) arg0;
            Vector v1 = (Vector) arg1;

            if (v0.size() < v1.size()) {
                return 1;
            }

            if (v0.size() > v1.size()) {
                return -1;
            }

            return 0;
        }
    }
    
    /**
     * Renderer class that uses a different foreground color for selected objects.
     * @author Han Sloetjes
     */
    class MarkerCellRenderer extends DefaultTableCellRenderer {

		/**
		 * Highlight the markers that should be followed by a whit line.
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			if (value instanceof SelectableObject) {
				if (((SelectableObject) value).isSelected()) {
					c.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
				} else {
					if (!isSelected) {
						c.setForeground(table.getForeground());
					}					
				}
			}
			return c;
		}
    	
    }
    
    /**
     * A class that adds or removes a border to the outer scrollpane,
     * depending on whether one or both the of scrollbars are visible or not.
     * 
     * @author Han Sloetjes
     */
    class SizeListener implements ComponentListener {

		public void componentHidden(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {	
		}

		public void componentResized(ComponentEvent e) {
			if (outerScrollPane != null) {
				if (outerScrollPane.getHorizontalScrollBar().isVisible() || 
						outerScrollPane.getVerticalScrollBar().isVisible()) {
					if (outerScrollPane.getBorder() == null) {
						outerScrollPane.setBorder(new LineBorder(Color.GRAY, 1));
					}
				} else {
					if (outerScrollPane.getBorder() != null) {
						outerScrollPane.setBorder(null);
					}
				}
			}
		}

		public void componentShown(ComponentEvent e) {
			componentResized(e);
		}   	
    }
}
