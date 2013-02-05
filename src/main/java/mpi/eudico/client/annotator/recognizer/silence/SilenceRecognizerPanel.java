package mpi.eudico.client.annotator.recognizer.silence;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.gui.TierSelectionPanel;

/**
 * A panel for setting the minimal silence and non-silence durations.
 * Optionally the noise levels for left and right channels can be set, instead of using selections.
 * 
 * @author albertr
 * @updated aarsom, Sep 2012
 */
public class SilenceRecognizerPanel extends JPanel implements ChangeListener, ParamPreferences {
	private JLabel minimalSilenceDurationLabel;
	private JSlider minimalSilenceDuration;
	private JLabel minimalNonSilenceDurationLabel;
	private JSlider minimalNonSilenceDuration;
	
	private JComboBox mediaFilesComboBox;
	
	private JPanel settingsPanel;
	private TierSelectionPanel selectionPanel;
	
	private ArrayList<String> mediaFilesList;
	
	/**
	 * Constructor. Initializes the components.
	 */
	public SilenceRecognizerPanel(TierSelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
		this.selectionPanel.setDefaultOption(TierSelectionPanel.SELECTIONS);
		
		initComponents();
	}
	
	private void initComponents(){
		
		mediaFilesComboBox = new JComboBox();
		
		JPanel filePanel = new JPanel(new GridBagLayout());			
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		filePanel.add(new JLabel("Files List :"), gbc);
		
		gbc.gridx = 1;	
		gbc.weightx = 1.0;
		filePanel.add(mediaFilesComboBox, gbc);
		
		JPanel selPanel = new JPanel(new GridBagLayout());
		selPanel.setBorder(new TitledBorder("Selection Panel"));
		gbc = new GridBagConstraints();			
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.weightx = 1.0;
		selPanel.add(selectionPanel, gbc);		
		
		initializeSettingsPanel();
		JPanel settingPanel = new JPanel(new GridBagLayout());
		settingPanel.setBorder(new TitledBorder("Settings"));
		gbc = new GridBagConstraints();			
		gbc.anchor = GridBagConstraints.NORTHWEST;			
		gbc.weightx = 1.0;
		gbc.insets = new Insets(1, 1, 1, 1);
		settingPanel.add(settingsPanel, gbc);				
		
		setLayout(new GridBagLayout());		
		
		gbc = new GridBagConstraints();
		gbc.gridy = 0;		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 2, 4, 2);
		add(filePanel, gbc);
		
		gbc.gridy = 1;		
		add(selPanel, gbc);
		
		gbc.gridy = 2;			
		add(settingPanel, gbc);
		//add(settingsPanel, gbc);
		
		gbc.gridy = 3;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(new JPanel(), gbc);			
	}
	
	/**
	 * Initialize settings Panel
	 */
	private void initializeSettingsPanel(){		
		int initialSilenceDuration = SilenceRecognizer.DEFAULT_SILENCE_DURATION;
		int initialNonSilenceDuration = SilenceRecognizer.DEFAULT_NON_SILENCE_DURATION;
		
		settingsPanel = new JPanel();
		//settingsPanel.setBorder(new TitledBorder("Settings"));
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(Box.createVerticalStrut(4));
		
		minimalSilenceDurationLabel = new JLabel(
				ElanLocale.getString("Recognizer.Silence.MinimalSilenceDuration") + 
				" " + initialSilenceDuration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
		settingsPanel.add(minimalSilenceDurationLabel);
		
		minimalSilenceDuration = new JSlider(JSlider.HORIZONTAL, 0, 1000, initialSilenceDuration);
		minimalSilenceDuration.setMajorTickSpacing(200);
		minimalSilenceDuration.setMinorTickSpacing(25);
		minimalSilenceDuration.setPaintTicks(true);
		minimalSilenceDuration.setPaintLabels(true);
		minimalSilenceDuration.addChangeListener(this);
		settingsPanel.add(minimalSilenceDuration);
		settingsPanel.add(Box.createVerticalStrut(4));
		
		minimalNonSilenceDurationLabel = new JLabel(
				ElanLocale.getString("Recognizer.Silence.MinimalNonSilenceDuration") + 
				" " + initialNonSilenceDuration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
		settingsPanel.add(minimalNonSilenceDurationLabel);
		
		minimalNonSilenceDuration = new JSlider(JSlider.HORIZONTAL, 0, 1000, initialNonSilenceDuration);
		minimalNonSilenceDuration.setMajorTickSpacing(200);
		minimalNonSilenceDuration.setMinorTickSpacing(25);
		minimalNonSilenceDuration.setPaintTicks(true);
		minimalNonSilenceDuration.setPaintLabels(true);
		minimalNonSilenceDuration.addChangeListener(this);
		settingsPanel.add(minimalNonSilenceDuration);
	}
	
	/**
	 * Change event handling.
	 * 
	 * @param e the change event
	 */
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
    	int duration = (int)source.getValue();
        
        if (source == minimalSilenceDuration) {
        	minimalSilenceDurationLabel.setText(
        			ElanLocale.getString("Recognizer.Silence.MinimalSilenceDuration") + 
        			" " + duration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
        } else if (source == minimalNonSilenceDuration) {
        	minimalNonSilenceDurationLabel.setText(
        			ElanLocale.getString("Recognizer.Silence.MinimalNonSilenceDuration") +
        			" " + duration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
        }
    }

	/**
	 * Returns the current minimal silence duration slider value.
	 * 
	 * @return the current minimal silence duration value
	 */
	public int getMinimalSilenceDuration() {
		return minimalSilenceDuration.getValue();
	}
	
	/**
	 * Returns the current minimal non-silence duration slider value.
	 * 
	 * @return the current minimal non-silence duration value
	 */
	public int getMinimalNonSilenceDuration() {
		return minimalNonSilenceDuration.getValue();
	}
	
	/**
	 * Updates the user interface elements.
	 */
	public void updateLocale() {
		int duration = minimalSilenceDuration.getValue();
    	minimalSilenceDurationLabel.setText(
    			ElanLocale.getString("Recognizer.Silence.MinimalSilenceDuration") + 
    			" " + duration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
    	duration = minimalNonSilenceDuration.getValue();
    	minimalNonSilenceDurationLabel.setText(
    			ElanLocale.getString("Recognizer.Silence.MinimalNonSilenceDuration") +
    			" " + duration + " " + ElanLocale.getString("PlayAroundSelDialog.Ms"));
    	selectionPanel.updateLocale();
	}
	
	/**
	 * Updates the media files that are
	 * supported by this recognizer
	 * 
	 * @param mediaFiles
	 */
	public void updateMediaFiles(List<String> mediaFiles) {
		if(mediaFilesComboBox == null ){
			mediaFilesComboBox = new JComboBox();
		}
		
		if(mediaFilesList == null){
			mediaFilesList = new ArrayList<String>();
		}
		
		mediaFilesComboBox.removeAllItems();
		mediaFilesList.clear();
		if(mediaFiles != null && mediaFiles.size() > 0){
			List<String> fileNameList = new ArrayList<String>();
			for(String media : mediaFiles){
				String fileName = fileNameFromPath(media);
				if(fileNameList.contains(fileName)){
					mediaFilesComboBox.addItem(media);
				} else{
					mediaFilesComboBox.addItem(fileName);
					fileNameList.add(fileName);
				}
				mediaFilesList.add(media);
			}			
			mediaFilesComboBox.setSelectedIndex(0);
		}
	}
	
	/**
	 * Extracts the file name from a path.
	 * 
	 * @param path the file path
	 * @return the file name
	 */
	private String fileNameFromPath(String path) {
		if (path == null) {
			return "Unknown";
		}
		// assume all paths have forward slashes
		int index = path.lastIndexOf('/');
		if (index > -1 && index < path.length() - 1) {
			return path.substring(index + 1);
		}
		
		return path;
	}

	/**
	 * Returns the current settings.
	 */
	public Map<String, Object> getParamPreferences() {
		Map <String, Object> sps = new HashMap<String, Object>(4);
		sps.put("MinimalSilenceDuration", new Integer(minimalSilenceDuration.getValue()));
		sps.put("MinimalNonSilenceDuration", new Integer(minimalNonSilenceDuration.getValue()));
		sps.put("SelectionPanelPref", selectionPanel.getStorableParamPreferencesMap(selectionPanel.getParamValue()));
		
		return sps;
	}

	/**
	 * Restores the last used settings.
	 */
	public void setParamPreferences(Map<String, Object> storedPrefs) {
		if (storedPrefs != null) {
			Object val;
			val = storedPrefs.get("MinimalSilenceDuration");
			if (val instanceof Integer) {
				minimalSilenceDuration.setValue((Integer) val);
			}
			val = storedPrefs.get("MinimalNonSilenceDuration");
			if (val instanceof Integer) {
				minimalNonSilenceDuration.setValue((Integer) val);
			}
			
			val = storedPrefs.get("SelectionPanelPref");
			if (val instanceof HashMap) {
				selectionPanel.setParamValue((HashMap) val);
			}
		}		
	}

	/**
	 * Returns the selected media file 
	 * 
	 * @return
	 */
	public String getSelectedMediaFile() {		
		return mediaFilesList.get(mediaFilesComboBox.getSelectedIndex());
	}
	
	public void validateParameters() throws RecognizerConfigurationException {
		if(getSelections() == null){
			throw new RecognizerConfigurationException(ElanLocale.getString("Recognizer.RecognizerPanel.Warning.Selection"));
		}
	}
	
	/**
	 * Returns the selections made in the selection panel
	 * 
	 * @return
	 */
	public ArrayList<RSelection> getSelections(){
		Object value = selectionPanel.getSelectionedValue();		
		
		if(value instanceof ArrayList){
			return ((ArrayList<RSelection>)value);
		} 	
		
		return null;
	}
}