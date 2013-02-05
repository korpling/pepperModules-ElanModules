package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.io.RecTierWriter;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeRelation;

/**
 * A dialog to export a selection of tiers to the AVATecH TIER format.
 * 
 * @author Han Sloetjes
 */
public class ExportRecogTiersDialog extends AbstractExtTierExportDialog 
    implements  ItemListener{
    private JCheckBox selectionCB;
    
    /** pref key */
    final String prefSelectedTiers = "ExportRecogTiersDialog.selectedTiers";
    
    /** pref key */
    final String prefTierOrder = "ExportRecogTiersDialog.TierOrder";
    
    /** pref key */
    final String prefParentTierOrder = "ExportRecogTiersDialog.ParentTierOrder";
    
    /** pref key */
    final String prefSelectTiersMode = "ExportRecogTiersDialog.SelectTiersMode";
    
    /** pref key */
    final String prefLastSelectedItems = "ExportRecogTiersDialog.LastSelectedItems";

    /** pref key */
    final String prefHiddenTiers = "ExportRecogTiersDialog.HiddenTiers";
    
    /** pref key */
    final String prefRootTiersOnly = "ExportRecogTiersDialog.ShowOnlyRootTiers";
    
    /** pref key */
    final String prefSelectionOnly = "ExportRecogTiersDialog.SelectionOnly";
    
    /**
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the transcription
     */
    public ExportRecogTiersDialog(Frame parent, boolean modal,
            Transcription transcription, Selection selection) {
        super(parent, modal, transcription, selection);

        makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    public void itemStateChanged(ItemEvent ie) {
        extractTiers();
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {    
    	 
    	Object useTyp;

    	useTyp = Preferences.get(prefTierOrder, transcription);
    	if (useTyp instanceof List) {
    		setTierOrder((List)useTyp);        	
        } else {
        	super.extractTiers(true);
        }
    	
        useTyp = Preferences.get(prefSelectedTiers, transcription);
        if (useTyp instanceof List) {
        	setSelectedTiers((List)useTyp);
        } 
        
        useTyp = Preferences.get(this.prefSelectTiersMode, transcription);
        if (useTyp instanceof String) {
        	//List list = (List) Preferences.get(this.prefHiddenTiers, transcription);
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
     * Initializes UI elements.
     */
    protected void makeLayout() {
        super.makeLayout();
        selectionCB = new JCheckBox();
        
        GridBagConstraints gridBagConstraints;
    
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        optionsPanel.add(selectionCB, gridBagConstraints);

        setPreferredSetting();
        updateLocale();
    }
    
    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false otherwise
     * @see mpi.eudico.client.annotator.export.AbstractBasicExportDialog#startExport()
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

        String fileName = promptForFileName();
        if (fileName == null) {
        	return false;
        }
		File tf = new File(fileName);
		try { 
			if (tf.exists()) {
                int answer = JOptionPane.showConfirmDialog(this,
                        ElanLocale.getString("Message.Overwrite"),
                        ElanLocale.getString("SaveDialog.Message.Title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (answer == JOptionPane.NO_OPTION) {
                    return false;
                }
			}
		} catch (Exception ex) {// any exception
			return false;
		}
		
        long begin = 0l;
        long end = Long.MAX_VALUE;
        if (selectionCB.isSelected()) {
            if (selection != null && selection.getBeginTime() < selection.getEndTime()) {
                begin = selection.getBeginTime();
                end = selection.getEndTime();
            }
        }
        
        List<Object> segmentations = new ArrayList<Object>(selectedTiers.size());
		TierImpl ti;
		String name;
		List anns;
		
		for (int i = 0; i < selectedTiers.size(); i++) {
			name = (String) selectedTiers.get(i);
			ti = (TierImpl) transcription.getTierWithId(name);
			
			if (ti != null) {					
				anns = ti.getAnnotations();
				ArrayList<RSelection> segments = new ArrayList<RSelection>(anns.size());
				AbstractAnnotation aa;
				for (int j = 0; j < anns.size(); j++) {
					aa = (AbstractAnnotation) anns.get(j);
					if (TimeRelation.overlaps(aa, begin, end)) {
						segments.add(new Segment(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary(), 
							aa.getValue()));
					}
					if (aa.getBeginTimeBoundary() > end) {
						break;
					}
				}
				Segmentation segmentation = new Segmentation(name, segments, "");// pass the master media path?
				segmentations.add(segmentation);
			}
		}
        
		try {
			RecTierWriter xTierWriter = new RecTierWriter();
			xTierWriter.write(tf, segmentations, false);
		} catch (IOException ioe) {
			// show message
			JOptionPane.showMessageDialog(this, ElanLocale.getString(
					"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
					ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
        return true;
    }
    
    /**
     * Applies localized strings to the ui elements.
     */
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportDialog.RecogTiers.Title"));
        titleLabel.setText(ElanLocale.getString("ExportDialog.RecogTiers.Title"));
        selectionCB.setText(ElanLocale.getString("ExportDialog.Restrict"));
    }
    
    protected String promptForFileName() {
    	ArrayList<String[]> extensions = new ArrayList<String[]>(); 
    	extensions.add(FileExtension.XML_EXT);
    	extensions.add(FileExtension.CSV_EXT);
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, extensions, FileExtension.XML_EXT, "Recognizer.Path", null);

		File f = chooser.getSelectedFile();
		if (f != null) {
			return f.getAbsolutePath();
		} else {
			return null;
		}	
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Object useTyp = Preferences.get(prefRootTiersOnly, null);    
    	if(useTyp == null){
    		useTyp = Preferences.get("ExportRecogTiersDialog.rootTiersCB", null);
    		Preferences.set("ExportRecogTiersDialog.rootTiersCB", null, null);
    	}
    	if(useTyp != null){ 
    		setRootTiersOnly((Boolean)useTyp);
    	}	
     
    	useTyp = Preferences.get(prefSelectionOnly, null);
    	if(useTyp == null){
    		useTyp = Preferences.get("ExportRecogTiersDialog.selectionCB", null);
    		Preferences.set("ExportRecogTiersDialog.selectionCB", null, null);
    	}
    	if(useTyp != null){
    		selectionCB.setSelected((Boolean)useTyp); 
    	} 
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set(this.prefRootTiersOnly, isRootTiersOnly(), null);    
    	Preferences.set(this.prefSelectionOnly, selectionCB.isSelected(), null);
    	Preferences.set(this.prefSelectedTiers, getSelectedTiers(), transcription);  
    	Preferences.set(this.prefSelectTiersMode, getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set(prefLastSelectedItems, getSelectedItems(), transcription);
    	}
    	Preferences.set(this.prefHiddenTiers, getHiddenTiers(), transcription);
    	
    	List tierOrder = getTierOrder();
    	Preferences.set(this.prefTierOrder, tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
        		if (rootTiersCB.isSelected()) {
        			Preferences.set(this.prefParentTierOrder, currentTierOrder, transcription);
        		}
        		else {
        			Preferences.set(this.prefTierOrder, currentTierOrder, transcription);
        		}
        		break;
    		}
    	}
    	*/    	
    }
}
