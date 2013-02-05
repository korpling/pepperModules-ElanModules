package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;

import mpi.eudico.client.annotator.util.FileExtension;

import mpi.eudico.client.util.Transcription2QtSubtitle;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;


/**
 * DOCUMENT ME! $Id: ExportQtSubtitleDialog.java 28855 2012-01-17 16:14:58Z hasloe $
 *
 * @author $Author: ericauer $
 * @version $Revision: 1.3 $
 */
public class ExportQtSubtitleDialog extends AbstractExtTierExportDialog
    implements  ChangeListener {
    private JCheckBox minimalDurCB;
    private JTextField minimalDurTF;
    private JCheckBox correctTimesCB;
    private JCheckBox mergeTiersCB;
    private JCheckBox recalculateTimesCB;
    
    private JButton fontSettingsButton;
    private HashMap fontSettingHashMap;
    
    private boolean smilExport;
    
    // preferences keys
    
    final String prefSmilPrefix = "ExportQtSMILDialog";
    
    final String prefSubtitlePrefix = "ExportQtSubtitleDialog";
    
    String prefStringPrefix = prefSubtitlePrefix;
    
    /** pref key */
    final String prefSelectionOnly = prefStringPrefix + ".SelectionOnly";
    /** pref key */
    final String prefSelectedTiers = prefStringPrefix + ".selectedTiers";    
    /** pref key */
    final String prefTierOrder = prefStringPrefix + ".TierOrder";    
    /** pref key */
    final String prefSelectTiersMode = prefStringPrefix + ".SelectTiersMode";
    /** pref key */
    final String prefLastSelectedItems = prefStringPrefix + ".LastSelectedItems";
    /** pref key */
    final String prefHiddenTiers = prefStringPrefix + ".HiddenTiers";   
    /** pref key */
    final String prefAddOffsetTime = prefStringPrefix + ".AddOffsetTime";
    /** pref key */
    final String prefMergeTiers = prefStringPrefix + ".MergeTiers";
    /** pref key */
    final String prefRecalculateTime = prefStringPrefix + ".RecalculateTimeFromZero";
    /** pref key */
    final String prefMinDur = prefStringPrefix + ".MinimumDuration";
    /** pref key */
    final String prefMinDurValue = prefStringPrefix + ".MinimumDurationValue";   	

    /**
     * DOCUMENT ME!
     *
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     */
    public ExportQtSubtitleDialog(Frame parent, boolean modal,
        Transcription transcription, Selection selection) {
    	this(parent,modal,transcription, selection, false);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     * @param smilExport
     */
    public ExportQtSubtitleDialog(Frame parent, boolean modal,
        Transcription transcription, Selection selection, boolean smilExport) {    	
    	super(parent, modal, transcription, selection);
    	this.smilExport = smilExport;  
    	if(smilExport){
    		prefStringPrefix = prefSmilPrefix;
    	}else{
    		prefStringPrefix = prefSubtitlePrefix;
    	}
        makeLayout();
        extractTiers();
        postInit();
        restrictCheckBox.requestFocus();        
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	
    	Object useTyp = Preferences.get(prefTierOrder, transcription);
    	if (useTyp instanceof List) {
    		setTierOrder((List)useTyp);        	
        } else {
        	super.extractTiers(false);
        }
    	
    	useTyp = Preferences.get(prefSelectedTiers, transcription);
        if (useTyp instanceof List) {
        	setSelectedTiers((List)useTyp);
        }
       
        useTyp = Preferences.get(prefSelectTiersMode, transcription);
        if (useTyp instanceof String) {
        	setSelectionMode((String)useTyp);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals((String) useTyp) ) {
            	// call this after! the mode has been set
            	Object selItems  = Preferences.get(prefLastSelectedItems, transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems((List) selItems);
            	}
        	}
         } 
    }

    /**
     * Initializes UI elements. Note: (for the time being) the checkbox column
     * indicating the selected state has been removed. It isn't that useful in
     * a single selection mode. The table could be replaced by a JList. When
     * the checkbox column would  be added again one of two things should
     * happen: either a custom tablecelleditor should be written for the
     * checkbox column or the valueChanged method should iterate over all rows
     * and uncheck all boxes, except the one for the selected roe.
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

        mergeTiersCB = new JCheckBox();
        mergeTiersCB.setSelected(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(mergeTiersCB, gridBagConstraints);        
        
        fontSettingsButton = new JButton();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fontSettingsButton, gridBagConstraints);
        fontSettingsButton.addActionListener(this); 
        
        minimalDurCB.addChangeListener(this);
        restrictCheckBox.addChangeListener(this);
        setPreferredSetting();

        updateLocale();
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractExtTierExportDialog#startExport()
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
        File exportFile;
        if(smilExport){        	
            exportFile = promptForFile(ElanLocale.getString(
                    "ExportQtSmilDialog.Title"), null, FileExtension.SMIL_EXT, false);
            
            if (exportFile == null) {
                return false;
            }
        
        } else{        
          exportFile = promptForFile(ElanLocale.getString(
                    "ExportQtSubtitleDialog.Title"), null, FileExtension.TEXT_EXT, false);
        }

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

        boolean merge = mergeTiersCB.isSelected();
        long mediaDur = ELANCommandFactory.getViewerManager(transcription)
                                          .getMasterMediaPlayer()
                                          .getMediaDuration();

        String[] tierNames = (String[]) selectedTiers.toArray(new String[0]);
        long b = 0L;
        long e = Long.MAX_VALUE;

        if (restrictCheckBox.isSelected()) {
            b = selection.getBeginTime();
            e = selection.getEndTime();
        } 
        
        String mediaURL = "";        
        if ( ((TranscriptionImpl) transcription).getMediaDescriptors().size() > 0) {
            mediaURL = ((MediaDescriptor) ((TranscriptionImpl) transcription).getMediaDescriptors().
            get(0)).mediaURL;
            
        }    

        if (!merge) {
             Transcription2QtSubtitle.exportTiers(transcription, tierNames,
                exportFile, b, e, offset, minimalDur, mediaDur, recalculateTimesCB.isSelected(), fontSettingHashMap);            
        } else {
            Transcription2QtSubtitle.exportTiersMerged(transcription,
                tierNames, exportFile, b, e, offset, minimalDur, mediaDur, recalculateTimesCB.isSelected(), fontSettingHashMap);
        }  
        
        // .sml output for Smil Quicktime Export
        if(smilExport){
        	
        	 String smilFile = exportFile.getAbsolutePath();
             int index = smilFile.lastIndexOf('.');
             if (index > 0) {
             	smilFile = smilFile.substring(0, index);
             }             
             smilFile += "." + FileExtension.SMIL_EXT[1];
             
             String mediaPath ="";
             if(mediaURL.length() > 0){ 
             	index = mediaURL.lastIndexOf("/");
             	mediaPath = mediaURL.substring(index+1);       	
             }
        
             try {	        
            	 if (selection != null && restrictCheckBox.isSelected()) {            		 
            		 ExportQtSmilDialog.export2SMILQt(	new File(((TranscriptionImpl) transcription).getPathName()),
            					 new File(smilFile), tierNames,	mediaPath, b+offset, e+offset, recalculateTimesCB.isSelected(), merge, fontSettingHashMap);
            	 } else {
            		 ExportQtSmilDialog.export2SMILQt(	new File(((TranscriptionImpl) transcription).getPathName()), 
						new File(smilFile), tierNames, mediaPath, mediaDur, merge, fontSettingHashMap);
            	 }
            	 return true;
             }
             catch (TransformerException te) {
            	 // this is ugly
            	 throw new IOException("TransformerException: " + te.getMessage());        	
             }
        	}
        
        	return true;
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExtExportDialog#updateLocale()
     */
    protected void updateLocale() {
        super.updateLocale();
        if(smilExport){
     	   setTitle(ElanLocale.getString("ExportQtSmilDialog.Title"));
            titleLabel.setText(ElanLocale.getString(
                    "ExportQtSmilDialog.TitleLabel"));   	   
     	   
        } else {
        	 setTitle(ElanLocale.getString("ExportQtSubtitleDialog.Title"));
             titleLabel.setText(ElanLocale.getString(
                     "ExportQtSubtitleDialog.TitleLabel"));
        }
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        minimalDurCB.setText(ElanLocale.getString(
                "ExportDialog.Label.MinimalDur"));
        mergeTiersCB.setText(ElanLocale.getString(
                "ExportQtSubtitleDialog.Label.Merge"));
        fontSettingsButton.setText(ElanLocale.getString(
        			"ExportQtSubtitleDialog.Button.FontSetting"));  
        recalculateTimesCB.setText(ElanLocale.getString("ExportDialog.RecalculateTimes"));
    }
    

    /**
     * Enables/disables the minimal duration textfield and the recalculate time check box
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
    		setNewFontSetting(DisplaySettingsPane.getNewFontSetting(this, ElanLocale.getString("DisplaySettingsPane.Title")));    		
    		}
    	}
    
    
	protected void setNewFontSetting(HashMap newSetting){
		if (newSetting != null){
			fontSettingHashMap = new HashMap(); 
			fontSettingHashMap = newSetting;
		}
	}
    /**
     * 
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    protected void setPreferredSetting()
    {
    	Object useTyp = Preferences.get(prefSelectionOnly, null);    	
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".restrictCheckBox", null); 
    		Preferences.set(prefStringPrefix+".restrictCheckBox", null, null);   
    	}
    	if(useTyp != null){
    		restrictCheckBox.setSelected((Boolean)useTyp); 
    	}	
     
    	useTyp = Preferences.get(prefMinDur, null);
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".minimalDurCB", null); 
    		Preferences.set(prefStringPrefix+".minimalDurCB", null, null);   
    	}
    	if(useTyp != null){
    		minimalDurCB.setSelected((Boolean)useTyp); 
    	}
     
    	useTyp = Preferences.get(prefAddOffsetTime, null);
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".correctTimesCB", null); 
    		Preferences.set(prefStringPrefix+".correctTimesCB", null, null);   
    	}
    	if(useTyp != null){
    		correctTimesCB.setSelected((Boolean)useTyp); 
    	}
     
    	useTyp = Preferences.get(prefMergeTiers, null);
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".mergeTiersCB", null); 
    		Preferences.set(prefStringPrefix+".mergeTiersCB", null, null);   
    	}
    	if(useTyp != null){
    		mergeTiersCB.setSelected((Boolean)useTyp); 
    	}
    	
    	useTyp = Preferences.get(prefMinDurValue, null);
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".minimalDurTF", null); 
    		Preferences.set(prefStringPrefix+".minimalDurTF", null, null);   
    	}
    	if(useTyp != null){
    		minimalDurTF.setText(useTyp.toString()); 
    	}
    	
    	useTyp = Preferences.get(prefRecalculateTime, null);
    	if(useTyp == null){
    		useTyp = Preferences.get(prefStringPrefix+".recalculateTimesCB", null); 
    		Preferences.set(prefStringPrefix+".recalculateTimesCB", null, null);   
    	}
    	if(useTyp != null){
    		recalculateTimesCB.setSelected((Boolean)useTyp);    		
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    protected void savePreferences(){
    	Preferences.set(prefSelectionOnly, restrictCheckBox.isSelected(), null);    
    	Preferences.set(prefMinDur, minimalDurCB.isSelected(), null);
    	Preferences.set(prefAddOffsetTime, correctTimesCB.isSelected(), null);
    	Preferences.set(prefMergeTiers, mergeTiersCB.isSelected(), null);   
    	Preferences.set(prefRecalculateTime, recalculateTimesCB.isSelected(), null);
    	if (minimalDurTF.getText() != null){
    		Preferences.set(prefMinDurValue, minimalDurTF.getText(), null);    	
    	}
    	Preferences.set(prefSelectedTiers, getSelectedTiers(), transcription);    	
    	Preferences.set(prefSelectTiersMode, getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set(prefLastSelectedItems, getSelectedItems(), transcription);
    	}
    	Preferences.set(prefHiddenTiers, getHiddenTiers(), transcription);
    	
    	List tierOrder = getTierOrder();
    	Preferences.set(prefTierOrder, tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set(prefTierOrder, currentTierOrder, transcription);
    			break;
    		}
    	} 
    	*/
    }
}
