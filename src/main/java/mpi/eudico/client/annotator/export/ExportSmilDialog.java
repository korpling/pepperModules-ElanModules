package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.EAF2SMIL;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


public class ExportSmilDialog extends AbstractExtTierExportDialog 
	implements ChangeListener {
   
	private JCheckBox minimalDurCB;
    private JTextField minimalDurTF;
    private JCheckBox correctTimesCB;
    private JCheckBox recalculateTimesCB;
	
	private JButton fontSettingsButton;
	private HashMap fontSettingHashMap;
	
     /**
     * 
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     */
    public ExportSmilDialog (Frame parent, boolean modal,
        Transcription transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        this.makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * Initializes UI elements.
     */
    protected void makeLayout() {
        super.makeLayout();       
        // options
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);
        
        recalculateTimesCB = new JCheckBox();  
        recalculateTimesCB.setEnabled(false);
        gridBagConstraints.gridy = 1;     
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(recalculateTimesCB, gridBagConstraints);

        correctTimesCB = new JCheckBox();
        correctTimesCB.setSelected(true);
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        optionsPanel.add(correctTimesCB, gridBagConstraints);

        minimalDurCB = new JCheckBox();
        minimalDurCB.setSelected(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(minimalDurCB, gridBagConstraints);
        
        minimalDurTF = new JTextField(6);
        minimalDurTF.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(minimalDurTF, gridBagConstraints);
        
        fontSettingsButton = new JButton();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fontSettingsButton, gridBagConstraints);
        fontSettingsButton.addActionListener(this);
        
        restrictCheckBox.addChangeListener(this);
        minimalDurCB.addChangeListener(this);
        
        setPreferredSetting();
        updateLocale();
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	Object useTyp = Preferences.get("ExportSmilDialog.TierOrder", transcription);
    	if (useTyp instanceof List) {
    		setTierOrder((List)useTyp);        	
        } else {
        	super.extractTiers(false);	
        }
    	
        useTyp = Preferences.get("ExportSmilDialog.selectedTiers", transcription);
        if (useTyp instanceof List) {
        	setSelectedTiers((List) useTyp);
        }
        
        useTyp = Preferences.get("ExportSmilDialog.SelectTiersMode", transcription);
        if (useTyp instanceof String) {
//        	List list = (List) Preferences.get("ExportSmilDialog.HiddenTiers", transcription);
//        	setSelectedMode((String)useTyp, list);
        	setSelectionMode((String)useTyp);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals((String) useTyp) ) {
            	// call this after! the mode has been set
            	Object selItems  = Preferences.get("ExportSmilDialog.LastSelectedItems", transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems((List) selItems);
            	}
        	}
         }
    }

    
    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportSmilDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportSmilDialog.TitleLabel"));
        fontSettingsButton.setText(ElanLocale.getString("ExportQtSubtitleDialog.Button.FontSetting"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        minimalDurCB.setText(ElanLocale.getString(
                "ExportDialog.Label.MinimalDur"));        
        recalculateTimesCB.setText(ElanLocale.getString("ExportDialog.RecalculateTimes"));
     }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#startExport()
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
        
        int minimalDur = 0;

        if (minimalDurCB.isSelected()) {
            String dur = minimalDurTF.getText();

            if ((dur == null) || (dur.length() == 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                minimalDurTF.requestFocus();

                return false;
            }

            try {
                minimalDur = Integer.parseInt(dur);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                minimalDurTF.requestFocus();

                return false;
            }
        }

 
        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                "Export.TigerDialog.title"), null, FileExtension.SMIL_EXT, false);
        
        if (exportFile == null) {
            return false;
        }
        
        long offset = 0L;

        if (correctTimesCB.isSelected()) {
            Vector mediaDescriptors = transcription.getMediaDescriptors();

            if (mediaDescriptors.size() > 0) {
                offset = ((MediaDescriptor) mediaDescriptors.get(0)).timeOrigin;
            }
        }

        // export....
        String[] tierNames = (String[]) selectedTiers.toArray(new String[] {  });
        String mediaURL = "";
        if ( ((TranscriptionImpl) transcription).getMediaDescriptors().size() > 0) {
            mediaURL = ((MediaDescriptor) ((TranscriptionImpl) transcription).getMediaDescriptors().
            get(0)).mediaURL;
        }
        try {	        
		    if (selection != null && restrictCheckBox.isSelected()) {
				EAF2SMIL.export2SMIL(
					 transcription,
					exportFile,
					tierNames,
					mediaURL,
					selection.getBeginTime(),
					selection.getEndTime(), offset,  minimalDur,recalculateTimesCB.isSelected(), fontSettingHashMap);
		    } else {
				EAF2SMIL.export2SMIL(
					new File(((TranscriptionImpl) transcription).getPathName()), 
					exportFile, tierNames, mediaURL,offset,  minimalDur, fontSettingHashMap);
		    }
        } catch (TransformerException te) {
            // this is ugly
            throw new IOException("TransformerException: " + te.getMessage());
        }
        
        
        return true;
    }
       
    /**
     * Enables/disables the recalculate Time interval Check box
     *
     * @param e the change event
     */
    public void stateChanged(ChangeEvent e) {
    	if (e.getSource() == minimalDurCB) {
            minimalDurTF.setEnabled(minimalDurCB.isSelected());
        }else if (e.getSource() == restrictCheckBox){
        	recalculateTimesCB.setEnabled(restrictCheckBox.isSelected());
        }
    }
    
    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
    	super.actionPerformed(ae);
    	if(ae.getSource() == fontSettingsButton){
    		this.setName("realPlayer");
    		setNewFontSetting(DisplaySettingsPane.getNewFontSetting(this, ElanLocale.getString("DisplaySettingsPane.Title"))); 
    		}
    	}
    
    
	private void setNewFontSetting(HashMap newSetting){
		if (newSetting != null){
			fontSettingHashMap = new HashMap(); 
			fontSettingHashMap = newSetting;
			}
	}
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Object useTyp = Preferences.get("ExportSmilDialog.restrictCheckBox", null);
    
    	if(useTyp != null){
    		restrictCheckBox.setSelected((Boolean)useTyp); 
    	}	
    	
    	useTyp = Preferences.get("ExportSmilDialog.minimalDurCB", null);
    	if(useTyp != null){
    		minimalDurCB.setSelected((Boolean)useTyp); 
    	}
     
    	useTyp = Preferences.get("ExportSmilDialog.correctTimesCB", null);
    	if(useTyp != null){
    		correctTimesCB.setSelected((Boolean)useTyp); 
    	}  
    	
    	useTyp = Preferences.get("ExportSmilDialog.minimalDurTF", null);
    	if(useTyp != null){
    		minimalDurTF.setText(useTyp.toString()); 
    	}
    	
    	useTyp = Preferences.get("ExportSmilDialog.recalculateTimesCB", null);
    	if(useTyp != null){
    		recalculateTimesCB.setSelected((Boolean)useTyp);    		
    	}

    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportSmilDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);
    	Preferences.set("ExportSmilDialog.selectedTiers", getSelectedTiers(), transcription);
    	Preferences.set("ExportSmilDialog.minimalDurCB", minimalDurCB.isSelected(), null);
    	Preferences.set("ExportSmilDialog.correctTimesCB", correctTimesCB.isSelected(), null);    	
    	Preferences.set("ExportSmilDialog.recalculateTimesCB", recalculateTimesCB.isSelected(), null);
    	if (minimalDurTF.getText() != null){
    		Preferences.set("ExportSmilDialog.minimalDurTF", minimalDurTF.getText(), null);  
    	}
    	
    	Preferences.set("ExportSmilDialog.SelectTiersMode", getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set("ExportSmilDialog.LastSelectedItems", getSelectedItems(), transcription);
    	}
    	Preferences.set("ExportSmilDialog.HiddenTiers", getHiddenTiers(), transcription);
    	
    	List tierOrder = getTierOrder();
    	Preferences.set("ExportSmilDialog.TierOrder", tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set("ExportSmilDialog.TierOrder", currentTierOrder, transcription);
    			break;
    		}
    	}    	
    	*/
    }
    
}
