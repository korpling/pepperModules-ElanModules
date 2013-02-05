package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;

import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.Transcription2TabDelimitedText;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * A dialog for exporting a set of tiers to a tab delimited text file. Provides
 * ui elements to customize the output.
 *
 * @author Han Sloetjes
 */
public class ExportTabDialog extends AbstractExtTierExportDialog
    implements ChangeListener {
    private JCheckBox btCheckBox;
    private JCheckBox correctTimesCB;
    private JCheckBox suppressNamesCB;
    private JCheckBox suppressParticipantsCB;
    private JCheckBox colPerTierCB;
    private JCheckBox repeatValuesCB;
    private JCheckBox repeatOnlyWithinCB;
    private JCheckBox durCheckBox;
    private JCheckBox etCheckBox;
    private JCheckBox hhmmssmsCheckBox;
    private JCheckBox msCheckBox;
    private JCheckBox ssmsCheckBox;
    private JCheckBox timecodeCB;
    private JLabel timeCodesLabel;
    private JLabel timeFormatLabel;
    private JRadioButton ntscTimecodeRB;
    private JRadioButton palTimecodeRB;    
    private JCheckBox includeFileNameCB;
    private JCheckBox includeFilePathCB;
    private JCheckBox includeCVEntryDesCB;
    
    private Insets insets = new Insets(2, 4, 2, 4);    

    /**
     * Creates a new ExportTabDialog2 instance
     *
     * @param parent the parent frame
     * @param modal the modal property
     * @param transcription the transcription to export
     * @param selection the selection object
     */
    public ExportTabDialog(Frame parent, boolean modal,
        Transcription transcription, Selection selection) {
        super(parent, modal, transcription, selection);       
        makeLayout();
        extractTiers();
        postInit();      
    }

    /**
     * Creates a new ExportTabDialog2 instance
     *
     * @param parent the parent frame
     * @param modal the modal property
     * @param files the eaf files to export
     */
    public ExportTabDialog(Frame parent, boolean modal,
    		ArrayList<File> files) {
        super(parent, modal, files);
        makeLayout();
        extractTiersFromFiles();
        postInit();      
    }
    
    /**
     * Enables / disables PAL and NTSC radio buttons.
     *
     * @param ce change event
     */
    public void stateChanged(ChangeEvent ce) {
    	if (ce.getSource() == timecodeCB) {
	        palTimecodeRB.setEnabled(timecodeCB.isSelected());
	        ntscTimecodeRB.setEnabled(timecodeCB.isSelected());
    	} else if (ce.getSource() == colPerTierCB) {
    		repeatValuesCB.setEnabled(colPerTierCB.isSelected());
    		// update include tier name and tier participant checkboxes
    		suppressNamesCB.setEnabled(!colPerTierCB.isSelected());
    		suppressParticipantsCB.setEnabled(!colPerTierCB.isSelected());
    		if (!colPerTierCB.isSelected()) {
    			repeatOnlyWithinCB.setEnabled(false);
    		}
    	} else if (ce.getSource() == repeatValuesCB) {
    		repeatOnlyWithinCB.setEnabled(repeatValuesCB.isSelected());
    	}
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	Object useTyp = null;
    	
    	useTyp = Preferences.get("ExportTabDialog.TierOrder", transcription);
    	if (useTyp instanceof List) {
    		setTierOrder((List)useTyp);        	
        } 
    	/*else {
        	super.extractTiers(false);
        }*/
    	
        useTyp = Preferences.get("ExportTabDialog.selectedTiers", transcription);
        if (useTyp instanceof List) {
        	setSelectedTiers((List)useTyp);
        }

        useTyp = Preferences.get("ExportTabDialog.SelectTiersMode", transcription);
        if (useTyp instanceof String) {
        	//List list = (List) Preferences.get("ExportTabDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);
        	setSelectionMode((String)useTyp);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals((String) useTyp) ) {
            	// call this after! the mode has been set
            	Object selItems  = Preferences.get("ExportTabDialog.LastSelectedItems", transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems((List) selItems);
            	}
        	}
         }
    }
    
    /**
     * Restore some global preferences
     */
	protected void extractTiersFromFiles() {
		super.extractTiersFromFiles();
		
        // in case of multiple files, transcription is null and  the global preference will be loaded
        Object useTyp = Preferences.get("ExportTabDialog.SelectTiersMode", null);
        if (useTyp instanceof String) {
        	//List list = (List) Preferences.get("ExportTabDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);
        	setSelectionMode((String)useTyp);
         }
	}

	/**
     * Initializes UI elements.
     */
    protected void makeLayout() {
        super.makeLayout();

        // add more 
        timeCodesLabel = new JLabel();
        timeFormatLabel = new JLabel();
        btCheckBox = new JCheckBox();
        etCheckBox = new JCheckBox();
        durCheckBox = new JCheckBox();
        hhmmssmsCheckBox = new JCheckBox();
        ssmsCheckBox = new JCheckBox();
        msCheckBox = new JCheckBox();
        timecodeCB = new JCheckBox();
        palTimecodeRB = new JRadioButton();
        ntscTimecodeRB = new JRadioButton();
        includeFileNameCB = new JCheckBox();
        includeFilePathCB = new JCheckBox();
        includeCVEntryDesCB = new JCheckBox();

        ButtonGroup group = new ButtonGroup();
        correctTimesCB = new JCheckBox();
        suppressNamesCB = new JCheckBox();
        suppressParticipantsCB = new JCheckBox();
        colPerTierCB = new JCheckBox();
        repeatValuesCB = new JCheckBox();
        colPerTierCB.addChangeListener(this);
        repeatValuesCB.setSelected(true);
        repeatValuesCB.setEnabled(false);
        repeatValuesCB.addChangeListener(this);
        repeatOnlyWithinCB = new JCheckBox();
        repeatOnlyWithinCB.setSelected(false);
        repeatOnlyWithinCB.setEnabled(false);
       
        // options
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(correctTimesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(suppressNamesCB, gridBagConstraints);

        // add suppress participant checkbox
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(suppressParticipantsCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(colPerTierCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);              	
        optionsPanel.add(repeatValuesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        optionsPanel.add(repeatOnlyWithinCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(includeCVEntryDesCB, gridBagConstraints);
        
        if(!multipleFileExport){        
        	JPanel fill = new JPanel();
        	Dimension fillDim = new Dimension(30, 10);
        	fill.setPreferredSize(fillDim);
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 8;
        	gridBagConstraints.gridwidth = 3;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(fill, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 9;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timeCodesLabel, gridBagConstraints);

        	JPanel filler = new JPanel();
        	filler.setPreferredSize(fillDim);
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 1;
        	gridBagConstraints.gridy = 9;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(filler, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 9;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timeFormatLabel, gridBagConstraints);       

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 10;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(btCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 10;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(hhmmssmsCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 11;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(etCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 11;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(ssmsCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 12;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(durCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 12;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(msCheckBox, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 13;
        	gridBagConstraints.gridwidth = 2;
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = insets;
        	optionsPanel.add(timecodeCB, gridBagConstraints);

        	JPanel smptePanel = new JPanel();        	
        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 2;
        	gridBagConstraints.gridy = 14;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.gridheight = 2;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	gridBagConstraints.fill = GridBagConstraints.NONE;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.weightx = 0.0;
        	optionsPanel.add(smptePanel, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 0;
        	gridBagConstraints.gridy = 0;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.weightx = 10.0;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	smptePanel.add(palTimecodeRB, gridBagConstraints);

        	gridBagConstraints = new GridBagConstraints();
        	gridBagConstraints.gridx = 1;
        	gridBagConstraints.gridy = 0;
        	gridBagConstraints.gridwidth = 1;
        	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        	gridBagConstraints.weightx = 10.0;
        	gridBagConstraints.anchor = GridBagConstraints.WEST;
        	gridBagConstraints.insets = new Insets(2, 22, 2, 4);
        	smptePanel.add(ntscTimecodeRB, gridBagConstraints);
        }
        else {
        	gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(includeFileNameCB, gridBagConstraints);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 9;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;;
            optionsPanel.add(includeFilePathCB, gridBagConstraints);            
            
            JPanel fill = new JPanel();
            Dimension fillDim = new Dimension(30, 10);
            fill.setPreferredSize(fillDim);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 10;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(fill, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 11;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timeCodesLabel, gridBagConstraints);

            JPanel filler = new JPanel();
            filler.setPreferredSize(fillDim);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 11;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(filler, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 11;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timeFormatLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 12;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(btCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 12;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(hhmmssmsCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 13;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(etCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 13;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(ssmsCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(durCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 14;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(msCheckBox, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 15;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(timecodeCB, gridBagConstraints);

            JPanel smptePanel = new JPanel();            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 16;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 2;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.0;
            optionsPanel.add(smptePanel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 10.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            smptePanel.add(palTimecodeRB, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 10.0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(2, 22, 2, 4);
            smptePanel.add(ntscTimecodeRB, gridBagConstraints);
        
        }
            
        group.add(palTimecodeRB);
        group.add(ntscTimecodeRB);
        timecodeCB.addChangeListener(this);		
        
        setPreferencesOrDefaultSettings();
        
        if (transcription == null) {
        		correctTimesCB.setEnabled(false);
        		//colPerTierCB.setEnabled(false);
        		restrictCheckBox.setEnabled(false);
        }       
        updateLocale();
    }

    /**
     * Starts the actual exporting process.
     *
     * @return true if export succeeded
     *
     * @throws IOException can occur when writing to the file
     * @throws NullPointerException DOCUMENT ME!
     */
    protected boolean startExport() throws IOException {
        List selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }
   
        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString("ExportTabDialog.Title"), 
        		null, FileExtension.TEXT_EXT, true);

        if (exportFile == null) {
            return false;
        }
 
        // export....
        String[] tierNames = (String[]) selectedTiers.toArray(new String[] {  });

        long selectionBT = 0L;
        long selectionET = Long.MAX_VALUE;

        long mediaOffset = 0L;

        if (transcription != null) {
        	// this option only applies to single file export currently
            if (restrictCheckBox.isSelected()) {
                selectionBT = selection.getBeginTime();
                selectionET = selection.getEndTime();
            }
            if (correctTimesCB.isSelected()) {
                Vector mds = transcription.getMediaDescriptors();

                if ((mds != null) && (mds.size() > 0)) {
                    mediaOffset = ((MediaDescriptor) mds.get(0)).timeOrigin;
                }
            }
	        if (colPerTierCB.isSelected()) {	        	
	        	if (!repeatValuesCB.isSelected()) {	        	
	        		Transcription2TabDelimitedText.exportTiersColumnPerTier(transcription, tierNames,
	                    exportFile  , encoding,  includeCVEntryDesCB.isSelected(), selectionBT, selectionET,
	                    btCheckBox.isSelected(), etCheckBox.isSelected(),
	                    durCheckBox.isSelected(), hhmmssmsCheckBox.isSelected(),
	                    ssmsCheckBox.isSelected(), msCheckBox.isSelected(),
	                    timecodeCB.isSelected(), palTimecodeRB.isSelected(), mediaOffset);	            
	        	} else {
		            ExportTabdelimited et = new ExportTabdelimited();
		            et.includeCVDescrip = includeCVEntryDesCB.isSelected();
		            et.includeBeginTime = btCheckBox.isSelected();
		            et.includeEndTime = etCheckBox.isSelected();
		            et.includeDuration = durCheckBox.isSelected();
		            et.includeHHMM = hhmmssmsCheckBox.isSelected();
		            et.includeSSMS = ssmsCheckBox.isSelected();
		            et.includeMS = msCheckBox.isSelected();
		            et.includeSMPTE = timecodeCB.isSelected();
		            et.palFormat = palTimecodeRB.isSelected();
		            et.mediaOffset = mediaOffset;
		            et.repeatValues = true;
		            et.combineBlocks = !repeatOnlyWithinCB.isSelected();		            
		            et.exportTiersColumnPerTier(transcription, tierNames, exportFile, 
		            		encoding, selectionBT, selectionET);
	        	}
	            
	        } else {       
		        Transcription2TabDelimitedText.exportTiers(transcription, tierNames,
		            exportFile, encoding, selectionBT, selectionET,
		            includeCVEntryDesCB.isSelected(), btCheckBox.isSelected(), etCheckBox.isSelected(),
		            durCheckBox.isSelected(), hhmmssmsCheckBox.isSelected(),
		            ssmsCheckBox.isSelected(), msCheckBox.isSelected(),
		            timecodeCB.isSelected(), palTimecodeRB.isSelected(), mediaOffset,
		            !suppressNamesCB.isSelected(), !suppressParticipantsCB.isSelected());
	        }
        } else {
        	if (colPerTierCB.isSelected()) {
	        	if (!repeatValuesCB.isSelected()) {	        		
	        		Transcription2TabDelimitedText.exportTiersColumnPerTier(files, tierNames,
	                    exportFile, encoding,  includeCVEntryDesCB.isSelected(),
	                    btCheckBox.isSelected(), etCheckBox.isSelected(),
	                    durCheckBox.isSelected(), hhmmssmsCheckBox.isSelected(),
	                    ssmsCheckBox.isSelected(), msCheckBox.isSelected(),
	                    timecodeCB.isSelected(), palTimecodeRB.isSelected(), includeFileNameCB.isSelected(), includeFilePathCB.isSelected());	
	                               
	        	} else {
		            ExportTabdelimited et = new ExportTabdelimited();
		            et.includeCVDescrip = includeCVEntryDesCB.isSelected();
		            et.includeBeginTime = btCheckBox.isSelected();
		            et.includeEndTime = etCheckBox.isSelected();
		            et.includeDuration = durCheckBox.isSelected();
		            et.includeHHMM = hhmmssmsCheckBox.isSelected();
		            et.includeSSMS = ssmsCheckBox.isSelected();
		            et.includeMS = msCheckBox.isSelected();
		            et.includeSMPTE = timecodeCB.isSelected();
		            et.palFormat = palTimecodeRB.isSelected();
		            et.includeFileName = includeFileNameCB.isSelected();
		            et.includeFilePath = includeFilePathCB.isSelected();
		            et.mediaOffset = mediaOffset;
		            et.repeatValues = true;
		            et.combineBlocks = !repeatOnlyWithinCB.isSelected();
		            et.exportTiersColumnPerTier(files, tierNames, exportFile, 
		            		encoding, 0L, Long.MAX_VALUE);
	        	}
        	} else {
        		Transcription2TabDelimitedText.exportTiers(files, tierNames, exportFile, encoding, 
        		includeCVEntryDesCB.isSelected(), btCheckBox.isSelected(), etCheckBox.isSelected(),
		        durCheckBox.isSelected(), hhmmssmsCheckBox.isSelected(),
		        ssmsCheckBox.isSelected(), msCheckBox.isSelected(),
		        timecodeCB.isSelected(), palTimecodeRB.isSelected(),
		        !suppressNamesCB.isSelected(), !suppressParticipantsCB.isSelected(), 
		        includeFileNameCB.isSelected(), includeFilePathCB.isSelected());
        	}
        } 
        
        return true;
    }

    /**
     * Set the localized text on ui elements.
     *
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportTabDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTabDialog.TitleLabel"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        suppressNamesCB.setText(ElanLocale.getString(
        "ExportTabDialog.Label.SuppressNames"));
        suppressParticipantsCB.setText(ElanLocale.getString("ExportTabDialog.Label.SuppressParticipants"));
        colPerTierCB.setText(ElanLocale.getString("ExportTabDialog.Label.ColPerTier"));
        repeatValuesCB.setText(ElanLocale.getString("ExportTabDialog.Label.RepeatValues"));
        repeatOnlyWithinCB.setText(ElanLocale.getString("ExportTabDialog.Label.RepeatWithinBlock"));
        includeFileNameCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeFileName"));
        includeFilePathCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeFilePath"));        
        timeCodesLabel.setText(ElanLocale.getString(
                "ExportTabDialog.Label.Columns"));
        timeFormatLabel.setText(ElanLocale.getString(
                "ExportTabDialog.Label.Formats"));
        btCheckBox.setText(ElanLocale.getString(
                "Frame.GridFrame.ColumnBeginTime"));
        etCheckBox.setText(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"));
        durCheckBox.setText(ElanLocale.getString(
                "Frame.GridFrame.ColumnDuration"));
        hhmmssmsCheckBox.setText(ElanLocale.getString("TimeCodeFormat.TimeCode"));
        ssmsCheckBox.setText(ElanLocale.getString("TimeCodeFormat.Seconds"));
        msCheckBox.setText(ElanLocale.getString("TimeCodeFormat.MilliSec"));
        timecodeCB.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE"));
        ntscTimecodeRB.setText(ElanLocale.getString(
                "TimeCodeFormat.TimeCode.SMPTE.NTSC"));
        palTimecodeRB.setText(ElanLocale.getString(
                "TimeCodeFormat.TimeCode.SMPTE.PAL"));
        includeCVEntryDesCB.setText(ElanLocale.getString("ExportTabDialog.Label.IncludeCVDescription"));

     }  
    
    private void setPreferencesOrDefaultSettings()
    {
    	 Object useTyp = Preferences.get("ExportTabDialog.restrictCheckBox", null);
         if(useTyp != null){
         	restrictCheckBox.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.correctTimesCB", null);
         if(useTyp != null){
        	 correctTimesCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.suppressNamesCB", null);
         if(useTyp != null){
        	 suppressNamesCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.suppressParticipantsCB", null);
         if(useTyp instanceof Boolean){
        	 suppressParticipantsCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.colPerTierCB", null);
         if(useTyp != null){
        	 colPerTierCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.repeatValuesCB", null);
         if(useTyp != null){
        	 repeatValuesCB.setSelected((Boolean)useTyp); 
         }
         else{
        	 repeatValuesCB.setSelected(true);
         }
         
         useTyp = Preferences.get("ExportTabDialog.repeatOnlyWithinCB", null);
         if(useTyp != null){
        	 repeatOnlyWithinCB.setSelected((Boolean)useTyp); 
         }
         
         
         useTyp = Preferences.get("ExportTabDialog.btCheckBox", null);
         if(useTyp != null){
        	 btCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 btCheckBox.setSelected(true);
         }
                  
         useTyp = Preferences.get("ExportTabDialog.etCheckBox", null);
         if(useTyp != null){
        	 etCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 etCheckBox.setSelected(true);
         }
         
         useTyp = Preferences.get("ExportTabDialog.durCheckBox", null);
         if(useTyp != null){
        	 durCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 durCheckBox.setSelected(true);
         }
         
         useTyp = Preferences.get("ExportTabDialog.hhmmssmsCheckBox", null);
         if(useTyp != null){
        	 hhmmssmsCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 hhmmssmsCheckBox.setSelected(true);
         }
         
         useTyp = Preferences.get("ExportTabDialog.ssmsCheckBox", null);
         if(useTyp != null){
        	 ssmsCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 ssmsCheckBox.setSelected(true);
         }
         
         useTyp = Preferences.get("ExportTabDialog.msCheckBox", null);
         if(useTyp != null){
        	 msCheckBox.setSelected((Boolean)useTyp); 
         }else
         {
        	 msCheckBox.setSelected(false);
         }       
         
         useTyp = Preferences.get("ExportTabDialog.timecodeCB", null);
         if(useTyp != null){
        	 timecodeCB.setSelected((Boolean)useTyp); 
         }else
         {
        	 timecodeCB.setSelected(false);  
         }
         
         useTyp = Preferences.get("ExportTabDialog.ntscTimecodeRB", null);
         if(useTyp != null){
        	 ntscTimecodeRB.setSelected((Boolean)useTyp); 
         }
          	
         useTyp = Preferences.get("ExportTabDialog.palTimecodeRB", null);
         if(useTyp != null){
        	 palTimecodeRB.setSelected((Boolean)useTyp); 
         }else
         { 
        	 palTimecodeRB.setSelected(true);
        	 }
         
         useTyp = Preferences.get("ExportTabDialog.includeFileNameCB", null);
         if(useTyp != null){
        	 includeFileNameCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.IncludeCVDescription", null);
         if(useTyp != null){
        	 includeCVEntryDesCB.setSelected((Boolean)useTyp); 
         }
         
         useTyp = Preferences.get("ExportTabDialog.includeFilePathCB", null);
         if(useTyp != null){
        	 includeFilePathCB.setSelected((Boolean)useTyp); 
         }
         else
        	 includeFilePathCB.setSelected(true); 
         
         if( timecodeCB.isSelected())
         {
        	 palTimecodeRB.setEnabled(true);
             ntscTimecodeRB.setEnabled(true); 
         }else{        	 
        	 palTimecodeRB.setEnabled(false);
             ntscTimecodeRB.setEnabled(false); 
         }
    }
    
    private void savePreferences() {    		
    	Preferences.set("ExportTabDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);    
    	Preferences.set("ExportTabDialog.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.suppressNamesCB", suppressNamesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.suppressParticipantsCB", suppressParticipantsCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.colPerTierCB", colPerTierCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.repeatValuesCB", repeatValuesCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.repeatOnlyWithinCB", repeatOnlyWithinCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.btCheckBox", btCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.etCheckBox", etCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.durCheckBox", durCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.hhmmssmsCheckBox", hhmmssmsCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.ssmsCheckBox", ssmsCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.msCheckBox", msCheckBox.isSelected(), null);
    	Preferences.set("ExportTabDialog.timecodeCB", timecodeCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.ntscTimecodeRB", ntscTimecodeRB.isSelected(), null);
    	Preferences.set("ExportTabDialog.palTimecodeRB", palTimecodeRB.isSelected(), null);
    	Preferences.set("ExportTabDialog.includeFileNameCB", includeFileNameCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.includeFilePathCB", includeFilePathCB.isSelected(), null);
    	Preferences.set("ExportTabDialog.IncludeCVDescription", includeCVEntryDesCB.isSelected(), null);
    	
    	if(!multipleFileExport){
    		Preferences.set("ExportTabDialog.selectedTiers", getSelectedTiers(), transcription);      		
    		Preferences.set("ExportTabDialog.SelectTiersMode", getSelectionMode(), transcription);
        	// save the selected list in case on non-tier tab
        	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
        		Preferences.set("ExportTabDialog.LastSelectedItems", getSelectedItems(), transcription);
        	}
    		Preferences.set("ExportTabDialog.HiddenTiers", getHiddenTiers(), transcription);
    	
    		List tierOrder = getTierOrder();
    		Preferences.set("ExportTabDialog.TierOrder", tierOrder, transcription);
    		/*
        	List currentTierOrder = getCurrentTierOrder();    	    	
        	for(int i=0; i< currentTierOrder.size(); i++){
        		if(currentTierOrder.get(i) != tierOrder.get(i)){
        			Preferences.set("ExportTabDialog.TierOrder", currentTierOrder, transcription);
        			break;
        		}
        	}
        	*/   		
    	} else {
    		// set a global preference
    		Preferences.set("ExportTabDialog.SelectTiersMode", getSelectionMode(), null);
    	}
    }
}

    
    