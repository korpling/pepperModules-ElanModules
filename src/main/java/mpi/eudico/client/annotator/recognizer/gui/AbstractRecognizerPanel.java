package mpi.eudico.client.annotator.recognizer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.xml.sax.SAXException;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.SegmentsToTiersCommand;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.HTMLViewer;
import mpi.eudico.client.annotator.recognizer.api.ParamPreferences;
import mpi.eudico.client.annotator.recognizer.api.RecogAvailabilityDetector;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.api.SharedRecognizer;
import mpi.eudico.client.annotator.recognizer.data.AudioSegment;
import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.io.CsvTierIO;
import mpi.eudico.client.annotator.recognizer.io.CsvTimeSeriesIO;
import mpi.eudico.client.annotator.recognizer.io.ParamIO;
import mpi.eudico.client.annotator.recognizer.io.RecTierWriter;
import mpi.eudico.client.annotator.recognizer.io.XmlTierIO;
import mpi.eudico.client.annotator.recognizer.io.XmlTimeSeriesReader;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.SystemReporting;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

public abstract class AbstractRecognizerPanel extends JComponent implements ActionListener, Runnable, 
RecognizerHost, ElanLocaleListener, ItemListener, ACMEditListener {
	protected ViewerManager2 viewerManager;
	// param panel provided by recognizer or created based on configuration file
	protected JPanel controlPanel;
	protected JPanel progressPanel;
	protected JPanel recognizerAndFilesPanel;
	protected JPanel paramPanel;
	protected JScrollPane jsp;
	protected JButton detachButton;
	protected ArrayList<String> mediaFilePaths;
	protected ArrayList<String> supportedMediaFiles;	
	protected JLabel recognizerLabel;
	protected JComboBox recognizerList;	
	protected HashMap<String, Recognizer> recognizers;
	protected Recognizer currentRecognizer;
	protected HashMap<String, Segmentation> segmentations;
	protected JProgressBar progressBar;
	protected JButton startStopButton;
	protected JButton reportButton;
	protected JButton createSegButton;
	protected JPanel paramButtonPanel;
	protected JButton saveParamsButton;
	protected JButton loadParamsButton;
	protected JButton helpButton;
	protected JButton configureButton;	
	protected boolean isRunning;
	protected int mode = Recognizer.AUDIO_TYPE;
	protected boolean notMono;
	protected boolean reduceFilePrompt = true;
	protected long lastStartTime = 0L;
	protected Timer elapseTimer;
	
	private boolean detatched = false;
	private ParamDialog detacthedDialog;	

	private HashMap<String, TierSelectionPanel> selPanelMap;

	/**
	 *  Initializes data structures and user interface components.
	 */
	public AbstractRecognizerPanel(ViewerManager2 viewerManager, ArrayList<String> mediaFilePaths) {
		super();

		this.viewerManager = viewerManager;
		this.viewerManager.connectListener(this);
		this.mediaFilePaths = mediaFilePaths;
		if (mediaFilePaths == null) {
			mediaFilePaths = new ArrayList<String>();
		}		
		supportedMediaFiles = new ArrayList<String>();		
		segmentations = new HashMap<String, Segmentation>();
		selPanelMap = new HashMap<String, TierSelectionPanel>();
		
		initComponents();
		initRecognizers();
	}
	
	/**
	 * Initialize interface components
	 */
	protected void initComponents() {
		
		Object val = Preferences.get("Recognizer.ReduceFilePrompts", null);
		if(val instanceof Boolean){
			reduceFilePrompt = ((Boolean)val).booleanValue();
		}
		setLayout(new GridBagLayout());

		recognizerLabel = new JLabel();
		recognizerList = new JComboBox();
		recognizerAndFilesPanel = new JPanel(new GridBagLayout());		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 2, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		recognizerAndFilesPanel.add(recognizerLabel, gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		recognizerAndFilesPanel.add(recognizerList, gbc);		
		
		// start/stop and progress information panel		
		JPanel progPanel = new JPanel();		
		progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.X_AXIS));
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progPanel.add(Box.createHorizontalStrut(10));
		progPanel.add(progressBar);
		progPanel.add(Box.createHorizontalStrut(10));
		startStopButton = new JButton(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
		startStopButton.addActionListener(this);
		progPanel.add(startStopButton);	
		reportButton = new JButton();
		reportButton.addActionListener(this);
		reportButton.setEnabled(false);
		progPanel.add(Box.createHorizontalStrut(15));
		progPanel.add(reportButton);
		createSegButton = new JButton();
		createSegButton.addActionListener(this);
		createSegButton.setEnabled(false);
		progPanel.add(Box.createHorizontalStrut(15));
		progPanel.add(createSegButton);
		
		progressPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		progressPanel.add(progPanel, gbc);
		
		elapseTimer = new Timer();
		
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		progressPanel.add(elapseTimer.getTimerPanel(), gbc);
		
		paramPanel = new JPanel(new GridBagLayout());
		paramButtonPanel = new JPanel(new GridBagLayout());	
		
		saveParamsButton = new JButton();
		saveParamsButton.addActionListener(this);
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource(
					"/toolbarButtonGraphics/general/SaveAs16.gif"));
			saveParamsButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			saveParamsButton.setText("S");
		}
		
		loadParamsButton = new JButton();
		loadParamsButton.addActionListener(this);
		try {
			ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
			loadParamsButton.setIcon(openIcon);
		} catch (Exception ex) {
			// catch any image loading exception
			loadParamsButton.setText("L");
		}
		
		detachButton = new JButton();
		detachButton.addActionListener(this);
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource(
					"/mpi/eudico/client/annotator/resources/Detach.gif"));
			detachButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			detachButton.setText("D");
		}
		
		configureButton = new JButton();
		configureButton.addActionListener(this);
		try {
			ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Configure16.gif"));
			configureButton.setIcon(openIcon);
		} catch (Exception ex) {
			// catch any image loading exception
			configureButton.setText("C");
		}	
		
		helpButton = new JButton();	
		helpButton.addActionListener(this);		
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help16.gif"));
			helpButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			helpButton.setText("H");
		}		 
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHEAST;	
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 2, 0, 2);	
		paramButtonPanel.add(loadParamsButton, gbc);
		
		gbc.gridx = 1;			
		paramButtonPanel.add(saveParamsButton, gbc);		
		
		gbc.gridx = 2;
		gbc.insets = new Insets(0, 10, 0, 2);
		paramButtonPanel.add(detachButton, gbc);	
		
		gbc.gridx = 3;
		gbc.insets = new Insets(0, 2, 0, 2);	
		paramButtonPanel.add(configureButton, gbc);
		
		gbc.gridx = 4;		
		paramButtonPanel.add(helpButton, gbc);
		
		// add components
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		add(recognizerAndFilesPanel, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;	
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(paramPanel, gbc);		
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;	
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		add(progressPanel, gbc);
	}
	
	/**
	 * Initializes recognizer and related ui elements, only once.
	 */
	protected void initRecognizers() {
		recognizers = getAvailableRecognizers();
		if (currentRecognizer != null) {
			paramPanel.removeAll();
			currentRecognizer = null;
		}

		recognizerList.removeItemListener(this);
		recognizerList.removeAllItems();
		Set<String> names = recognizers.keySet();
		Iterator<String> iter = names.iterator();
		while (iter.hasNext()) {
			recognizerList.addItem(iter.next());
		}
		
		recognizerList.setSelectedIndex(-1);
		
		if (recognizerList.getItemCount() == 0) {
			recognizerList.addItem(ElanLocale.getString("Recognizer.RecognizerPanel.No.Recognizers"));
			//paramPanel.add(new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.No.Parameters")), BorderLayout.CENTER);
			
			jsp = new JScrollPane(getParamPanel(null));
			jsp.setBackground(getBackground());
			jsp.getViewport().setBackground(getBackground());
			jsp.getVerticalScrollBar().setUnitIncrement(20);	
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
			
			startStopButton.setEnabled(false);
			recognizerList.setEnabled(false);
			createSegButton.setEnabled(false);
			paramButtonPanel.setVisible(false);
		} else {
			String lastActiveRecognizer = null;
			if(mode == Recognizer.AUDIO_TYPE){
				lastActiveRecognizer = (String) Preferences.get("ActiveAudioRecognizerName", viewerManager.getTranscription()); 
			} else if(mode == Recognizer.VIDEO_TYPE){
				lastActiveRecognizer = (String) Preferences.get("ActiveVideoRecognizerName", viewerManager.getTranscription());
			}
			
			if(lastActiveRecognizer != null && recognizers.containsKey(lastActiveRecognizer)){
				recognizerList.setSelectedItem(lastActiveRecognizer);
				setRecognizer(lastActiveRecognizer);
			} 
			
			if(recognizerList.getSelectedIndex() < 0){
				recognizerList.setSelectedIndex(0);
				setRecognizer((String) recognizerList.getSelectedItem());
				recognizerList.setEnabled(true);
			}
		}
		recognizerList.addItemListener(this);
	}
	
	/**
	 * To be implemented by subclasses. Depends on the type of recognizers, audio or video.
	 * 
	 * @return the currently available recognizers
	 */
	protected abstract HashMap<String, Recognizer> getAvailableRecognizers();
	
	/**
	 * Updates the media files that supported by the
	 * current recognizer
	 */
	protected void updateSupportedFiles(){
		// check the current media files	
		supportedMediaFiles.clear();
		for (int i = 0; i < mediaFilePaths.size(); i++) {
			if (currentRecognizer.canHandleMedia(mediaFilePaths.get(i))) {
				supportedMediaFiles.add(mediaFilePaths.get(i));
			} 
		}
	}
	
	/**
	 * Method called before closing the recognizer to
	 * store the preferences
	 */
	public void isClosing(){
		if (currentRecognizer != null) {
			// store preferences
			if (controlPanel instanceof ParamPreferences) {
				Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
				if (prefs != null) {
					Preferences.set(currentRecognizer.getName(), 
						prefs, viewerManager.getTranscription());
				}
			}
			if(mode == Recognizer.AUDIO_TYPE){
				Preferences.set("ActiveAudioRecognizerName", currentRecognizer.getName(), viewerManager.getTranscription());
			} else if(mode == Recognizer.VIDEO_TYPE){
				Preferences.set("ActiveVideoRecognizerName", currentRecognizer.getName(), viewerManager.getTranscription());
			}
			
			Preferences.set("Recognizer.ReduceFilePrompts", reduceFilePrompt, null);		
		}
	}
	
	/**
	 * Sets the recognizer, gets the parameter panel, updates the files list.
	 *  
	 * @param name the name of the recognizer
	 */
	protected void setRecognizer(String name) {
		if (currentRecognizer != null) {
			// store preferences
			if (controlPanel instanceof ParamPreferences) {
				Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
				if (prefs != null) {
					Preferences.set(currentRecognizer.getName(), 
						((ParamPreferences) controlPanel).getParamPreferences(), viewerManager.getTranscription());
				}
			}
			currentRecognizer.dispose();
			paramPanel.removeAll();
			segmentations.clear();
			createSegButton.setEnabled(false);
			reportButton.setEnabled(false);
		}
		
		selPanelMap.clear();
		elapseTimer.resetTimer();
		currentRecognizer = (Recognizer) recognizers.get(name);
		currentRecognizer.setRecognizerHost(this);
		
		updateSupportedFiles();	
		
		currentRecognizer.setMedia(supportedMediaFiles);		
		
		controlPanel = currentRecognizer.getControlPanel();
		if(controlPanel == null){			
			// check if there are parameters, create factory panel	
			controlPanel = getParamPanel(currentRecognizer);
		}		
		
		if(controlPanel != null){
			jsp = new JScrollPane(controlPanel);
			jsp.getVerticalScrollBar().setUnitIncrement(20);
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
				
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.NORTHEAST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			paramPanel.add(paramButtonPanel, gbc);
		}else {
			jsp = null;

			// label, no configurable params
			paramPanel.add(new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.No.Parameters")), new GridBagConstraints());
			paramPanel.repaint();
		}
			
		if (controlPanel instanceof ParamPreferences) {
			Object prefs = Preferences.get(currentRecognizer.getName(), viewerManager.getTranscription());
			if (prefs instanceof Map) {
				((ParamPreferences) controlPanel).setParamPreferences((HashMap<String, Object>) prefs);
			}
			loadParamsButton.setEnabled(true);
			saveParamsButton.setEnabled(true);
		} else {
			loadParamsButton.setEnabled(false);
			saveParamsButton.setEnabled(false);
		}
		
		if(RecogAvailabilityDetector.getHelpFile(currentRecognizer.getName()) == null){
			helpButton.setEnabled(false);
		}	 else {
			helpButton.setEnabled(true);
		}
		progressBar.setString("");
		progressBar.setValue(0);
		
		validate();
	}
	
	/**
	 * Returns a factory created user interface for setting parameters for the recognizer.
	 * 
	 * @param recognizer the recognizer
	 * 
	 * @return a parameter panel
	 */
	protected JPanel getParamPanel(Recognizer recognizer) {
		if (recognizer != null) {
			List<Param> params = RecogAvailabilityDetector.getParamList(recognizer.getName());
				
			if (params != null) {
				ParamPanelContainer ppc = new ParamPanelContainer(recognizer.getName(), params, supportedMediaFiles, viewerManager, mode);
				startStopButton.setEnabled(ppc.checkStartReg());
				ppc.validate();
				return ppc;
			}
		}
		return null;
	}	
	
	/**
	 * Called after a change in the set of linked media files.
	 */
	protected void updateMediaFiles(){
		if(currentRecognizer == null){
			return ;
		}
		
		updateSupportedFiles();		
		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateMediaFiles(supportedMediaFiles);
			startStopButton.setEnabled(((ParamPanelContainer)controlPanel).checkStartReg());			
		} else {
			if(supportedMediaFiles.size() <= 0){
				startStopButton.setEnabled(false);	
			} else {
				startStopButton.setEnabled(true);	
			}
		}
		
		Iterator<java.util.Map.Entry<String, TierSelectionPanel>> it = selPanelMap.entrySet().iterator();
		TierSelectionPanel panel = null;
		while(it.hasNext()){
			panel = it.next().getValue();
			if(panel != null){
			panel.updateMediaFiles(supportedMediaFiles);
			}
		}
		
		// update files list for the java plugin recognizers
		if(currentRecognizer != null){
			currentRecognizer.setMedia(supportedMediaFiles);
		}
	}
	
	/**
	 * Called after a change in the tier.
	 */
	protected void updateTiers(int event){
		if(currentRecognizer == null){
			return ;
		}				
		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateTiers(event);			
		} else {			
			Iterator<java.util.Map.Entry<String, TierSelectionPanel>> it = selPanelMap.entrySet().iterator();
			TierSelectionPanel panel = null;
			while(it.hasNext()){
				panel = it.next().getValue();
				if(panel != null){
					panel.updateTierNames(event);
				}	
			}
		}
	}	
	
	/**
	 * Handling of combobox selection events.
	 * @param e the event
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == recognizerList && e.getStateChange() == ItemEvent.SELECTED) {
			if (currentRecognizer != null && isBusy()) {
				// tell the user that the current recognizer is still running
				JOptionPane.showMessageDialog(this,
					    currentRecognizer.getName() + ": " + ElanLocale.getString("Recognizer.RecognizerPanel.Warning.Busy"),
					    currentRecognizer.getName() + " " + ElanLocale.getString("Recognizer.RecognizerPanel.Warning.Busy2"),
					    JOptionPane.PLAIN_MESSAGE);
				// restore the current recognizers name in the combo box
				recognizerList.setSelectedItem(currentRecognizer.getName());
				return;
			}
			if(detatched){
				detacthedDialog.dispose();
			}
			// reset the current segmentations
			//segmentations = new HashMap<String, Segmentation>();
			segmentations.clear();
			// remove current recognizer GUI
			paramPanel.removeAll();
			// set the new current recognizer
			setRecognizer((String) recognizerList.getSelectedItem());
			
		}
		
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source.equals(startStopButton)) {
			if (currentRecognizer == null) {
				return;
			}
			if (isRunning) {
				startStopButton.setText(ElanLocale.getString(
				"Recognizer.RecognizerPanel.Start"));
				stopRecognizers();
				if (progressBar.isIndeterminate()) {
					progressBar.setIndeterminate(false);
				}
				progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Canceled"));
				progressBar.setValue(0);
				//setProgress(0, ElanLocale.getString("Recognizer.RecognizerPanel.Canceled"));
				reportButton.setEnabled(true);
			} else {
				startRecognizer();
			}
		}  
		else if (source == reportButton) {
			if (currentRecognizer != null) {
				showReport(currentRecognizer.getReport());
			}
		} else if (source == configureButton) {
			showConfigureDialog();
		}else if (source == saveParamsButton) {
			saveParameterFile();
		} else if (source == loadParamsButton) {
			loadParameterFile();
		} else if (source == detachButton) {
			detachParamPanel();
		} else if(source == helpButton){
			showHelpDialog();
		}else if( source == createSegButton){
			if (isBusy()) {
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString(
                            "SegmentsToTierDialog.Warning.Busy"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
        		return;
        	}
            List segments = getSegmentations();

            if ((segments == null) || (segments.size() == 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "SegmentsToTierDialog.Warning.NoSegmentation"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }

            // needs the transcription
            Command cc = ELANCommandFactory.createCommand(viewerManager.getTranscription(),
                    ELANCommandFactory.SEGMENTS_2_TIER_DLG);
            cc.execute(viewerManager.getTranscription(),
                new Object[] { segments });
        
		}
	}
	
	protected void startRecognizer() {		
		if (isRunning) {
			return;
		}
		
		if(controlPanel instanceof ParamPanelContainer){				
			// get all the required input  && output parameters
			List<FileParamPanel> inputFPPS = new ArrayList<FileParamPanel>();
			List<FileParamPanel> outputFPPS = new ArrayList<FileParamPanel>();
			ParamPanelContainer ppc = (ParamPanelContainer) controlPanel;
			int numPanels = ppc.getNumPanels();
			AbstractParamPanel app;
			Object value;
			FileParamPanel ffp;
			int notFilled = 0;
			for (int i = 0; i < numPanels; i++) {
				app = ppc.getParamPanel(i);
				if(app instanceof FileParamPanel && !((FileParamPanel)app).isOptional()){
					ffp = (FileParamPanel)app;					
					
					if(ffp.isInputType()){
						if(!ffp.isValueFilled()){
							notFilled++;	
						}
						inputFPPS.add(ffp);	
					} else {
						outputFPPS.add(ffp);	
					}					
				}
			}
			
			// display a error message regarding the parameters that
			// doesn't have a value
			if(notFilled > 0){
				JOptionPane.showMessageDialog(this,									    
					ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyReqdParam"),	
				    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam"),
				    JOptionPane.ERROR_MESSAGE);	
				return;
			} 
			
			//validate all required input param values
			for(FileParamPanel panel : inputFPPS) {
				value = panel.getParamValue();
				String file = null;
				if(value instanceof HashMap){
					HashMap map = (HashMap)value;
					if(map.containsKey(TierSelectionPanel.SELECTIONS)){
						value = map.get(TierSelectionPanel.SELECTIONS);
					} else if(map.containsKey(TierSelectionPanel.TIER)){
						value = map.get(TierSelectionPanel.TIER);
					} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
						value = map.get(TierSelectionPanel.FILE_NAME);
					}
				} 
				
				if(value instanceof List){
					file = writeAndGetTierOrSelectionFile(panel, (List)value);
				}else if(value instanceof String){
					File tf = new File((String)value);
					if(tf != null && tf.exists()){
						file = value.toString();
					} else{
						JOptionPane.showMessageDialog(this, value.toString()+" file not found.",		
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}	
				
				if(file != null && file.length() > 0){
					currentRecognizer.setParameterValue(panel.getParamName(), file);
				} else {
//					JOptionPane.showMessageDialog(this, value.toString() +" file not found.",		
//							ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
//							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			
			//validate all required output param values
			for(FileParamPanel panel : outputFPPS) {	
				String val = (String) panel.getParamValue();
				if (val == null || val.length() == 0) {
					if (reduceFilePrompt) {
						val = autoCreateOutputFile(mediaFilePaths, 
								panel.getParamName(), panel.getContentType());							
					} else {
						// prompt file							
						FileChooser chooser = new FileChooser(this);
						List<String[]> extensions = panel.getFileTypeExtension();
						String[] mainFilterExt = null;
						if(extensions != null && extensions.size() > 0){
							mainFilterExt = extensions.get(0);
						}
						chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, extensions, mainFilterExt, "Recognizer.Path", null);
						val = chooser.getSelectedFile().getAbsolutePath();
					}
						
					// check again
					if (val == null || val.length() == 0)  {
						// prompt and return
						JOptionPane.showMessageDialog(this,									    
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam") + " \"" +
							    panel.description + "\" " +
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam2"),
							    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.EmptyParam"),
							    JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						panel.setParamValue(val);		
					}
				} else {
					// check if file exists and set access
					createOutputFile(val);
				}
					
				if(val != null && val.length() > 0){
					currentRecognizer.setParameterValue(panel.getParamName(), val);
				}  else {
					return;
				}
			}			
			
			// set other available parameters to the recognizer
			for (int i = 0; i < numPanels; i++) {
				app = ppc.getParamPanel(i);
				value = app.getParamValue();
				if(app instanceof FileParamPanel && !((FileParamPanel)app).isOptional()){
					continue;
				}
				
				if(app instanceof FileParamPanel){
					String file = null;
					ffp = (FileParamPanel)app;	
					
					//if outputparam
					if(!ffp.isInputType()){
						if(value != null){							
							file = value.toString();
						}	
					}else {
						if(value instanceof HashMap){
							HashMap map = (HashMap)value;
							if(map.containsKey(TierSelectionPanel.SELECTIONS)){
								value = map.get(TierSelectionPanel.SELECTIONS);
							} else if(map.containsKey(TierSelectionPanel.TIER)){
								value = map.get(TierSelectionPanel.TIER);
							} else if(map.containsKey(TierSelectionPanel.FILE_NAME)){
								value = map.get(TierSelectionPanel.FILE_NAME);
							}
						} 
											
						
						if(value instanceof List){
							file = writeAndGetTierOrSelectionFile(ffp, (List)value);			
						} else if(value instanceof String){
							File tf = new File((String)value);
							if(tf != null &&tf.exists()){
								file = value.toString();
							}
						}
					}
					
					if(file != null && file.length() > 0){
						currentRecognizer.setParameterValue(ffp.getParamName(), file);
					} 
				} else	if (value instanceof Float) {
					currentRecognizer.setParameterValue(app.paramName, ((Float) value).floatValue());
				} else if (value instanceof Double) {
					currentRecognizer.setParameterValue(app.paramName, ((Double) value).floatValue());
				} else if (value instanceof String) {
					currentRecognizer.setParameterValue(app.paramName, (String) value);
				}
			}
		} else {
			// java plug-in
		}
		
		 try {
				currentRecognizer.validateParameters();
			} catch (RecognizerConfigurationException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(),		
					    ElanLocale.getString("Recognizer.RecognizerPanel.Warning.InValidParam"),
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
		
		// store preferences
		if (controlPanel instanceof ParamPreferences) {
//			Map<String, Object> prefs = ((ParamPreferences) controlPanel).getParamPreferences();
//			if (prefs != null) {
//				Preferences.set(currentRecognizer.getName(), 
//					((ParamPreferences) controlPanel).getParamPreferences(), null);
//			}
		}
		// clear the list of segmentations created by a previous run
		segmentations.clear();

		progressBar.setValue(0);
		progressBar.setString("");
		
		startStopButton.setText(ElanLocale.getString("Button.Cancel"));
		reportButton.setEnabled(false);
		isRunning = true;
		progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Recognizing"));
		new Thread(this).start();	
		elapseTimer.start();
	}

	/**
	 * Prompts the user to specify a location where to store the selections.
	 * @param contentType FileParam.TIER or FileParam.CSV_TIER
	 * 
	 * @return the path or null if canceled
	 */
	private String promptForTierFile(String title, int contentType, String startingPoint) {
		String prefPath = null;
		if (startingPoint != null && startingPoint.length() > 0) {
			prefPath = startingPoint;
		} 
		
		String[] extensions = null;
		if (contentType == FileParam.CSV_TIER) {
			extensions = FileExtension.CSV_EXT;
		} else {
			extensions = FileExtension.XML_EXT;
		}
		FileChooser chooser = new FileChooser(this);		
		
		chooser.createAndShowFileDialog(title, FileChooser.SAVE_DIALOG,  extensions, "Recognizer.Path");
		
		File f = chooser.getSelectedFile();
		if (f != null) {			
			return f.getAbsolutePath();
		} else {
			return null;
		}		
	}
	
	/**
	 * Creates a filename based on the first media file and the parameter identifier.
	 * Instead of prompting the user to specify a path. 
	 * The file is also created if it does not exist.
	 * 
	 * @param mediaFiles the selected media files
	 * @param paramName the name/identifier of a parameter
	 * @param contentType csv or xml or other
	 * 
	 * @return the path to a file
	 */
	private String autoCreateOutputFile(List<String> mediaFiles, String paramName, int contentType) {
		if (mediaFiles != null && mediaFiles.size() > 0) {
			String firstMed = mediaFiles.get(0);
			int dirIndex = firstMed.lastIndexOf(File.separator);// '/'?
			StringBuilder dirPath = null;
			
			if (dirIndex > -1 && dirIndex < firstMed.length() - 1) {
				dirPath = new StringBuilder(firstMed.substring(0, dirIndex + 1));
			} else {
				// no valid last path separator
				int index = firstMed.lastIndexOf('.');
				if (index > -1 && index < firstMed.length() - 1) {
					dirPath = new StringBuilder(firstMed.substring(0, index + 1));
				} else {
					dirPath = new StringBuilder(firstMed);
				}
			}
			
			if (dirPath != null) {
				dirPath.append(paramName.replace(' ', '_'));
				if (contentType == FileParam.CSV_TIER || contentType == FileParam.CSV_TS) {
					dirPath.append(".csv");
				} else if (contentType == FileParam.TIER || contentType == FileParam.MULTITIER || contentType == FileParam.TIMESERIES) {
					dirPath.append(".xml");
				}
				try {
					File out = new File(dirPath.toString());
					if (!out.exists()) {
						out.createNewFile();
						if (currentRecognizer instanceof SharedRecognizer) {
							try {
								changeFileAccess(out);
							} catch (Exception ex) {// just catch any exception that can occur
								ClientLogger.LOG.warning("Cannot change the file permissions: " + ex.getMessage());
							}
						}
					}
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Cannot create the file: " + ioe.getMessage());
				} catch (SecurityException se) {
					ClientLogger.LOG.warning("Cannot create the file: " + se.getMessage());
				}
				
				return dirPath.toString();
			}
		}
		
		return null;
	}
	
	/**
	 * Create an output file for a recognizer based on a user specified path 
	 * and set the access rights.
	 * 
	 * @param path the path to the file
	 */
	private void createOutputFile(String path) {
		if (path == null) {
			return;
		}
		try {
			File out = new File(path);
			if (!out.exists()) {
				out.createNewFile();
				if (currentRecognizer instanceof SharedRecognizer) {
					try {
						changeFileAccess(out);
					} catch (Exception ex) {// just catch any exception that can occur
						ClientLogger.LOG.warning("Cannot change the file permissions: " + ex.getMessage());
					}
				}
			}
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Cannot create the file: " + ioe.getMessage());
		} catch (SecurityException se) {
			ClientLogger.LOG.warning("Cannot create the file: " + se.getMessage());
		}
	}
	
	/**
	 * Tries to make the output file readable, writable and executable for all users.
	 * 
	 * @param f the file
	 */
	private void changeFileAccess(File f) {
		if (f == null) {
			return;
		}
		// java 1.6 method via reflection, on Windows Vista this doesn't change anything even if true is returned
		/*
		Class<?>[] params = new Class[2];
		params[0] = boolean.class;
		params[1] = boolean.class;
		Object[] values = new Object[2];
		values[0] = Boolean.TRUE;
		values[1] = Boolean.FALSE;
		try {
			Method m = f.getClass().getMethod("setExecutable", params);
			Object result = m.invoke(f, values);
			if (result instanceof Boolean) {
				ClientLogger.LOG.info("Set executable: " + result);
			}
			m = f.getClass().getMethod("setWritable", params);
			result = m.invoke(f, values);
			if (result instanceof Boolean) {
				ClientLogger.LOG.info("Set writable: " + result);
			}
		} catch (NoSuchMethodException nsme) {
		*/
			// no java 1.6, try system tools
			ArrayList<String> coms = new ArrayList<String>(5);
			
			if (SystemReporting.isWindows()) {				
				coms.add("CACLS");
				coms.add("\"" + f.getAbsolutePath() + "\"");
				coms.add("/E");
				coms.add("/G");
				coms.add("Everyone:f"); //avatech fails "No mapping between account names and security IDs was done."			
			} else {// MacOS, Linux
				coms.add("chmod");
				coms.add("a+rwx");
				coms.add("\"" + f.getAbsolutePath() + "\"");
			}
			
			ProcessBuilder pb = new ProcessBuilder(coms);
			pb.redirectErrorStream(true);
			try {
				Process proc = pb.start();
				int exit = proc.exitValue();
				if (exit != 0) {
					ClientLogger.LOG.warning("Could not set the file access attributes via using native tool, error: " + exit);
				}
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Cannot set the file access attributes via native tool");
			}
		/*	
		} catch (Exception ex) {
			// any other exception
			ClientLogger.LOG.warning("Cannot set the file access attributes");
		}
		*/
	}
	
	/**
	 * When the recognizer has finished, check if there is output like 
	 * tier or timeseries files and ask if the segments and 
	 * the tracks should be loaded. 
	 */
	private void checkOutput() {
		if (isRunning) {
			return;
		}
		boolean loadTS = false;
		boolean loadTiers = false;
		boolean tsAvailable = false;
		boolean tierFileAvailable = false;

		if (controlPanel instanceof ParamPanelContainer) {			
			ParamPanelContainer ppc = (ParamPanelContainer) controlPanel;
			int numPanels = ppc.getNumPanels();
			AbstractParamPanel app;
			FileParamPanel fpp;
			// first loop for prompt
			for (int i = 0; i < numPanels; i++) {
				app = ppc.getParamPanel(i);
				if (app instanceof FileParamPanel) {
					fpp = (FileParamPanel) app;
					if (!fpp.isInputType() && 
							(fpp.getContentType() == FileParam.CSV_TS || fpp.getContentType() == FileParam.TIMESERIES)) {
						Object file = fpp.getParamValue();
						if (file instanceof String) {
							File f = new File((String) file);
							
							if (f.exists() && f.canRead() && f.lastModified() > lastStartTime) {
								tsAvailable = true;					
							}
						}	
					} else if (!fpp.isInputType() && 
							(fpp.getContentType() == FileParam.TIER || fpp.getContentType() == FileParam.MULTITIER || fpp.getContentType() == FileParam.CSV_TIER)) {
						Object file = fpp.getParamValue();
						if (file instanceof String) {
							File f = new File((String) file);
							
							if (f.exists() && f.canRead() && f.lastModified() > lastStartTime) {
								tierFileAvailable = true;
								//break;						
							}
						}	
					}
				}
			}
			
			boolean tiersAvailable = segmentations.size() > 0;
			if (!tiersAvailable && !tsAvailable && !tierFileAvailable) {
				JOptionPane.showMessageDialog(this, 
						ElanLocale.getString("Recognizer.RecognizerPanel.Warning.NoOutput"), 
						ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			// ask the user whether tracks and tiers should be loaded
			List<SelectableObject> resources = new ArrayList<SelectableObject>(4);

			if (tiersAvailable || tierFileAvailable) {
				boolean sel = true;
				Object pref = Preferences.get("Recognizer.RecognizerPanel.Tiers", null);

				if (pref instanceof Boolean) {
					sel = ((Boolean) pref).booleanValue();
				}
				resources.add(new SelectableObject(
						ElanLocale.getString("Recognizer.RecognizerPanel.Tiers"), sel));

			}
			if (tsAvailable) {
				boolean sel = true;
				Object pref = Preferences.get("Recognizer.RecognizerPanel.TimeSeries", null);

				if (pref instanceof Boolean) {
					sel = ((Boolean) pref).booleanValue();
				}
				resources.add(new SelectableObject(
						ElanLocale.getString("Recognizer.RecognizerPanel.TimeSeries"), sel));
			}
			
			LoadOutputPane lop = new LoadOutputPane(resources);
			int option = JOptionPane.showConfirmDialog(this, 
					lop,
					"", 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option != JOptionPane.YES_OPTION) {
				return;
			} else {

				SelectableObject selObj;
				for (int i = 0; i < resources.size(); i++) {
					selObj = resources.get(i);
					if (selObj.getValue().equals(ElanLocale.getString("Recognizer.RecognizerPanel.Tiers"))) {
						loadTiers = selObj.isSelected();
						Preferences.set("Recognizer.RecognizerPanel.Tiers", new Boolean(loadTiers), null, false, false);
					}
					else if (selObj.getValue().equals(ElanLocale.getString("Recognizer.RecognizerPanel.TimeSeries"))) {
						loadTS = selObj.isSelected();
						Preferences.set("Recognizer.RecognizerPanel.TimeSeries", new Boolean(loadTS), null, false, false);
					}
				}
			}
			
			if (loadTiers) {
				if (tierFileAvailable && !tiersAvailable) {
					// load them from file if possible
					List<File> csvFiles = new ArrayList<File>(4);
					List<File> xmlFiles = new ArrayList<File>(4);
					for (int i = 0; i < numPanels; i++) {
						app = ppc.getParamPanel(i);
						if (app instanceof FileParamPanel) {
							fpp = (FileParamPanel) app;
							if (!fpp.isInputType() && 
									(fpp.getContentType() == FileParam.CSV_TIER || fpp.getContentType() == FileParam.TIER)) {
								Object file = fpp.getParamValue();
								if (file instanceof String) {
									File f = new File((String) file);
									if (f.exists() && f.canRead() && f.lastModified() > lastStartTime) {
										if (fpp.getContentType() == FileParam.CSV_TIER) {
											csvFiles.add(f);
										} else {
											xmlFiles.add(f);
										}
									}
								}
							}
						}
					}
					
					for (File csvFile : csvFiles) {
						if (csvFile.exists() && csvFile.canRead() && csvFile.lastModified() > lastStartTime) {
							CsvTierIO cio = new CsvTierIO();
							List<Segmentation> segm = cio.read(csvFile);
							if (segm != null && segm.size() > 0) {
								for (Segmentation s : segm) {
									addSegmentation(s);
								}
							}
						}
					}
					
		        	StringBuffer mesBuf = new StringBuffer();  					
					for (File xmlFile : xmlFiles) {
						if (xmlFile.exists() && xmlFile.canRead() && xmlFile.lastModified() > lastStartTime) {
							XmlTierIO xio = new XmlTierIO(xmlFile);
							List<Segmentation> segm = null;
							try{
								segm = xio.parse();
							} catch (Exception e){
								mesBuf.append(xmlFile.getAbsolutePath() + " : " + e.getMessage()  + "\n");
							}
							 
							if (segm != null && segm.size() > 0) {
								for (Segmentation s : segm) {
									addSegmentation(s);
								}
							}
						}
					}
					
					if(mesBuf.length() > 0) {
						JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), 
								mesBuf, ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
						
						if(getSegmentations().size() == 0){
							return;
						}
	                }
					
				}
	            // needs the transcription
				HashMap<String, List<AnnotationDataRecord>> segmentationMap = 
					new HashMap<String, List<AnnotationDataRecord>>();
				Segmentation seg;
		        Segment segment;
		        ArrayList segments = null;
		        ArrayList<AnnotationDataRecord> records = null;
		        ArrayList<Segmentation> segs = getSegmentations();
		        for (int i = 0; i < segs.size(); i++) {
		            seg = (Segmentation) segs.get(i);

		            if (seg == null) {
		                continue;
		            }

		            segments = seg.getSegments();
		            records = new ArrayList<AnnotationDataRecord>();

		            for (int j = 0; j < segments.size(); j++) {
		                segment = (Segment) segments.get(j);
		                records.add(new AnnotationDataRecord("", segment.label,
		                        segment.beginTime, segment.endTime));
		            }

		            segmentationMap.put(seg.getName(), records);
		        }

		        // create command
		        Command com = (SegmentsToTiersCommand) ELANCommandFactory.createCommand(
		        		viewerManager.getTranscription(),
		                ELANCommandFactory.SEGMENTS_2_TIER);
		        //com.addProgressListener(this);
		        //progressBar.setIndeterminate(false);
		        //progressBar.setValue(0);
		        com.execute(viewerManager.getTranscription(), new Object[] { segmentationMap });

			}
			
			if (loadTS) {
				List<File> csvFiles = new ArrayList<File>(4);
				List<File> xmlFiles = new ArrayList<File>(4);
				for (int i = 0; i < numPanels; i++) {
					app = ppc.getParamPanel(i);
					if (app instanceof FileParamPanel) {
						fpp = (FileParamPanel) app;
						if (!fpp.isInputType() && 
								(fpp.getContentType() == FileParam.CSV_TS || fpp.getContentType() == FileParam.TIMESERIES)) {
							Object file = fpp.getParamValue();
							if (file instanceof String) {
								File f = new File((String) file);
								if (f.exists() && f.canRead() && f.lastModified() > lastStartTime) {
									if (fpp.getContentType() == FileParam.CSV_TS) {
										csvFiles.add(f);
									} else {
										xmlFiles.add(f);
									}
								}
							}
						}
					}
				}
				
				List<Object> tracks = new ArrayList<Object>(10);
				for (File f : csvFiles) {
					CsvTimeSeriesIO csvIO = new CsvTimeSeriesIO(f);
					List<Object> result = csvIO.getAllTracks();
					if (result != null) {
						tracks.addAll(result);
					}
				}
				for (File f : xmlFiles) {
					XmlTimeSeriesReader xmlIO = new XmlTimeSeriesReader(f);
					try {
						List<Object> result = xmlIO.parse();
						if (result != null) {
							tracks.addAll(result);
						}
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(this, 
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed") + "\n" + 
								ioe.getMessage(), 
								ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
					} catch (SAXException sax) {
						JOptionPane.showMessageDialog(this, 
								ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed") + "\n" + 
								sax.getMessage(), 
								ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
					}
				}
				
				Command com = ELANCommandFactory.createCommand(viewerManager.getTranscription(), 
						ELANCommandFactory.ADD_TRACK_AND_PANEL);
				com.execute(viewerManager, new Object[]{tracks});
			}
		}
	}
	
	/**
	 * This method will run in a separate Thread to decouple the recognizer from ELAN
	 */
	public void run() {
		if (currentRecognizer != null) {
			currentRecognizer.start();
			lastStartTime = System.currentTimeMillis();
		}
	}

	/**
	 * Takes care of giving running recognizers a chance to stop gracefully
	 * ELAN should call this method before it quits
	 *
	 */
	public void stopRecognizers() {
		if (currentRecognizer != null) {
			elapseTimer.stop();
			currentRecognizer.stop();			
			isRunning = false;
		}
	}

	/**
	 * Tells if there is one or more recognizer busy
	 * 
	 * @return true if there is some recognizing going on
	 */
	public boolean isBusy() {
		return isRunning;
	}

	/**
	 * Sets the localized labels for ui elements.
	 */
	public void updateLocale() {		
		createSegButton.setText(ElanLocale.getString(
                "Recognizer.SegmentationsPanel.Make.Tier"));
		if (currentRecognizer != null) {
			currentRecognizer.updateLocale(ElanLocale.getLocale());
		}
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).updateLocale();
		}
		helpButton.setToolTipText(ElanLocale.getString("Button.Help.ToolTip"));
		startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
		reportButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Report"));
		recognizerLabel.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Recognizer"));			
		progressPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.RecognizerPanel.Progress")));
		paramPanel.setBorder(new TitledBorder(ElanLocale.getString("Recognizer.RecognizerPanel.Parameters")));
		detachButton.setToolTipText(ElanLocale.getString("Detachable.detach"));
		configureButton.setToolTipText(ElanLocale.getString("MetadataViewer.Configure"));		
		saveParamsButton.setToolTipText(ElanLocale.getString("Recognizer.RecognizerPanel.SaveParameters"));
		loadParamsButton.setToolTipText(ElanLocale.getString("Recognizer.RecognizerPanel.LoadParameters"));
		
	}
	
	//
	// RecognizerHost interface implementation
	//
	
	/**
	 * Add a segmentation 
	 */
	public void addSegmentation(Segmentation segmentation) {
		segmentations.put(segmentation.getName(), segmentation);
		createSegButton.setEnabled(true);
	}
	
	/**
	 * 
	 */
	public ArrayList<Segmentation> getSegmentations() {
		return new ArrayList<Segmentation>(segmentations.values());
	}
	
	/**
	 * By calling this method a recognizer gives information about the progress of its recognition task
	 * 
	 * @param progress  a float between 0 and 1 with 1 meaning that the task is completed
	 */
	public void setProgress(float progress) {	
		int progPercent = (int) (100 * progress);
		setProgress(progPercent, null);
		/*
		if (progress < 0) {
			if (!progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(true);
			}
		} else {
			if (progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(false);
			}
			
			int progPercent = (int) (100 * progress);
			progressBar.setValue(progPercent);	
			
			if (progress >= 1) {
				progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Ready"));
				startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
				isRunning = false;
				reportButton.setEnabled(true);
				// get report??
				// check for any output
				elapseTimer.stop();
				checkOutput();
			}else {
				String message = progressBar.getString();
				if(message != null){
					message = message.trim();
					int index = message.indexOf('%');
					if(index > -1){
						message = progPercent + message.substring(index);
					}else{
						message = progPercent + "%";
					}
				} else{
					message = progPercent + "%";
				}
				progressBar.setString(message);
				elapseTimer.recognizerUpdate();
			}
		}
		*/
	}

	/**
	 * By calling this method a recognizer gives information about the progress of its recognition task
	 * 
	 * @param progress  a float between 0 and 1 with 1 meaning that the task is completed
	 * @param message a progress message
	 */
	public void setProgress(float progress, String message) {
		int progPercent = (int) (100 * progress);
		setProgress(progPercent, message);
		/*
		setProgress(progress);
		if (isRunning) {// still running?
			if (message != null) {
				String info = progressBar.getString();
				int index = info.indexOf('%');
				info = info.substring(0, index+1) + " " + message;				
				progressBar.setString(info);
			} else {
				progressBar.setString("");
			}
		}
		*/
	}	
	
	/**
	 * For internal use only, called from {@link #setProgress(float)} or {@link #setProgress(float, String)}.
	 * 
	 * @param percentage an integer value between 1 and 100, inclusive. 100 means completed
	 * @param message a message string or null
	 */
	private void setProgress(int percentage, String message) {
		if (percentage < 0) {
			if (!progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(true);
			}
			if (message != null) {
				progressBar.setString(message);
			} else {
				progressBar.setString("");
			}
			elapseTimer.recognizerUpdate();//??
		} else {
			if (progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(false);
			}
			
			progressBar.setValue(percentage);
			
			if (percentage >= 100) {
				progressBar.setString(ElanLocale.getString("Recognizer.RecognizerPanel.Ready"));
				startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
				isRunning = false;
				reportButton.setEnabled(true);
				// get report??
				// check for any output
				elapseTimer.stop();
				checkOutput();
			} else {
				if (isRunning) {
					if (message != null) {
						progressBar.setString(String.valueOf(percentage) + "% " + message);
					} else {
						progressBar.setString(String.valueOf(percentage) + "%");
					}
				}
				elapseTimer.recognizerUpdate();
			}
		}
		
	}

	/**
	 * Called by the recognizer to signal that a fatal error occurred.
	 * 
	 * @param message a description of the error
	 */
	public void errorOccurred(String message) {
		elapseTimer.stop();
		JOptionPane.showMessageDialog(this, ElanLocale.getString("Recognizer.RecognizerPanel.Error") + "\n" + message, 
				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
		// just to be sure
		if (isRunning) {			
			currentRecognizer.stop();
		}
		setProgress(1f);
	}
	
	/**
	 * Returns a tier selection panel for the given parameter
	 * 
	 * @param paramName - name of the parameter
	 */
	public TierSelectionPanel getSelectionPanel(String paramName) {
		if(paramName == null || paramName.trim().length()==0){
			paramName = "DEFAULT";
		}
		TierSelectionPanel panel = selPanelMap.get(paramName);		
		if(panel == null){
			panel = new TierSelectionPanel(mode, supportedMediaFiles, viewerManager);
			selPanelMap.put(paramName, panel);
			
		}
		return panel;
	}
	
	/**
	 * Preliminary!
	 * Attaches the parameter panel to the main panel again.
	 */
	public void attachParamPanel(JComponent paramComp) {
		// is the param scrollpane
		if (paramComp == controlPanel || paramComp == jsp) {
			if(paramComp == controlPanel && controlPanel instanceof ParamPanelContainer){
				((ParamPanelContainer)controlPanel).doLayout(false);
				jsp.setViewportView(controlPanel);
			}
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			paramPanel.add(jsp, gbc);
			paramButtonPanel.setVisible(true);
			
			detatched = false;
			detacthedDialog = null;
			
//			if(controlPanel){
//			//	startStopButton.setEnabled(true);
//			}
		}
	}
	
	private void showConfigureDialog(){		
		ConfigWindow cw = new ConfigWindow(ELANCommandFactory.getRootFrame(
				viewerManager.getTranscription()));
		cw.pack();
		Dimension dim = cw.getPreferredSize();
		Point p = configureButton.getLocationOnScreen();
		cw.setBounds(p.x - dim.width, p.y, dim.width, dim.height);
		cw.setVisible(true);
	}
	
	/**
	 * Preliminary!
	 * Detaches the parameter panel from the main panel.
	 */
	public void detachParamPanel() {
		paramPanel.remove(jsp);
		paramPanel.repaint();
		paramButtonPanel.setVisible(false);		
		if(controlPanel instanceof ParamPanelContainer){
			((ParamPanelContainer)controlPanel).doLayout(true);
			detacthedDialog = new ParamDialog(ELANCommandFactory.getRootFrame(
					viewerManager.getTranscription()), this, controlPanel);
		} else {
			detacthedDialog = new ParamDialog(ELANCommandFactory.getRootFrame(
					viewerManager.getTranscription()), this, jsp);
		}
		
		detacthedDialog.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle curRect = detacthedDialog.getBounds();
		// add some extra width for a possible scrollbar in the scrollpane
		detacthedDialog.setBounds(screen.width / 2, 10, 
				Math.min(screen.width / 2 - 20, curRect.width), Math.min(screen.height - 30, curRect.height));
		detacthedDialog.setVisible(true);
		detatched = true;
		//startStopButton.setEnabled(false);
	}
	
	/**
	 * Checks if help is available for the current 
	 * recognizer
	 * 
	 * @return
	 */
	public boolean isHelpAvailable(){
		return helpButton.isEnabled();
	}
	
	/**
	 * Show a new Help dialog
	 * 
	 */
	public void showHelpDialog(){
		try {
			String fileName = RecogAvailabilityDetector.getHelpFile(currentRecognizer.getName());
			HTMLViewer helpViewer = new HTMLViewer(fileName, false, ElanLocale.getString("Recognizer.RecognizerPanel.Help"));
    		JDialog dialog = helpViewer.createHTMLDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()));
    		dialog.pack();
    		dialog.setSize(500, 600);
    		dialog.setVisible(true);
    	} catch (IOException ioe) {
    		// message box
    		JOptionPane.showMessageDialog(this, (ElanLocale.getString("Message.LoadHelpFile")+ " "   + ioe.getMessage()), 
    				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE, null);
    	}
	}
	
	/**
	 * Loads the parameter settings from a PARAM XML file and applies them to the current recognizer. 
	 */
	protected void loadParameterFile() {
		if (currentRecognizer == null || controlPanel == null || !(controlPanel instanceof ParamPreferences)) {
			return;
		}
		
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(null, FileChooser.OPEN_DIALOG, FileExtension.XML_EXT, "Recognizer.Path");
		File selFile = chooser.getSelectedFile();
		if (selFile != null && selFile.canRead()) {
			try {
				ParamIO pio = new ParamIO();
				Map<String, Object> parMap = pio.read(selFile);
				((ParamPreferences) controlPanel).setParamPreferences(parMap);
			} catch (IOException ioe) {
				// 
				JOptionPane.showMessageDialog(this, ElanLocale.getString(
					"Recognizer.RecognizerPanel.Warning.LoadFailed")  + ioe.getMessage(), 
					ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Writes the current parameter settings to a PARAM XML file. 
	 */
	protected void saveParameterFile() {
		if (currentRecognizer == null || controlPanel == null || !(controlPanel instanceof ParamPreferences)) {
			return;
		}
		
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.XML_EXT, "Recognizer.Path");	
		File f = chooser.getSelectedFile();
		if(f != null){	
			ParamIO pio = new ParamIO();
			// hier... or get parameters from the recognizer?
			Map<String, Object> paramMap = ((ParamPreferences) controlPanel).getParamPreferences();
			
			try {
				pio.writeParamFile(paramMap, f);
			} catch (IOException ioe) {
				// message
				JOptionPane.showMessageDialog(this, ElanLocale.getString(
						"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			}			
		}
	}
	
	private String writeAndGetTierOrSelectionFile(FileParamPanel panel, List selList){
		String filePath = null;
		// ask for file name					
		if (reduceFilePrompt) {
			filePath = autoCreateOutputFile(mediaFilePaths, panel.getParamName(), panel.getContentType());
		} else {						
			filePath = promptForTierFile(panel.description, panel.getContentType(), null);
		}
		
		if(filePath == null){
			return null;	
		}		
	 		
		File tf = new File(filePath);
		try { 			
			if(tf.exists()){
				writeFile(filePath, selList, panel.getContentType());
			} else {
				JOptionPane.showMessageDialog(this, "File not exists.", 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
				return null;
			}
		} catch (IOException ioe) {
				// show message
			JOptionPane.showMessageDialog(this, ElanLocale.getString(
					"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
					ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
			return null ;
		}	catch (Exception ex) {// any exception
			return null;
		}					
		 
		return filePath;
	}
	
	/**
	 * Writes the selections from the selection panel to a csv or xml file.
	 * It is assumed that a check has been performed on the list of selections and that
	 * the file is save to save to.
	 * 
	 * @param filepath the path to the file
	 * @param contentType CSV_TIER or TIER (xml)
	 * @throws IOException any io error that can occur during writing
	 */
	protected void writeFile(String filepath, Object value, int contentType) throws IOException {		
		List valList = new ArrayList();
		if(value instanceof List){
			valList = (List)value;
		} else if(value instanceof String){
			// create segments			
			TierImpl tier = (TierImpl) viewerManager.getTranscription().getTierWithId(value.toString());			
			if (tier!= null) {					
				List anns = tier.getAnnotations();
				valList = new ArrayList<RSelection>(anns.size());
				AbstractAnnotation aa;
				for (int j = 0; j < anns.size(); j++) {
					aa = (AbstractAnnotation) anns.get(j);
					valList.add(new AudioSegment(aa.getBeginTimeBoundary(), aa.getEndTimeBoundary(), 
							aa.getValue()));
				}				
			}
		}
		RecTierWriter xTierWriter = new RecTierWriter();
		File f = new File(filepath);
		xTierWriter.write(f, valList, true);
		//xTierWriter.write(f, selectionPanel.getSelections());
	}
	
	/**
	 * Creates a dialog with a text area containing the report.
	 * 
	 * @param report the report
	 */
	protected void showReport(String report) {
		if (report != null) {
			JDialog rD = new JDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), true);
			JTextArea ta = new JTextArea(report);
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			ta.setEditable(false);
			rD.getContentPane().setLayout(new BorderLayout());
			rD.getContentPane().add(new JScrollPane(ta), BorderLayout.CENTER);
			rD.pack();
			// set minimum size
			rD.setSize(Math.max(rD.getWidth(), 400), Math.max(rD.getHeight(), 300));
			// set maximum size
			rD.setSize(Math.min(rD.getWidth(), 800), Math.min(rD.getHeight(), 600));
			rD.setLocationRelativeTo(this);
			
			rD.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, ElanLocale.getString("Recognizer.RecognizerPanel.No.Report"), 
				ElanLocale.getString("Message.Warning"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (currentRecognizer != null) {
			if (isRunning) {
				currentRecognizer.stop();
				elapseTimer.stop();
				currentRecognizer.dispose();
			}
			currentRecognizer = null;
		}
		super.finalize();
	}
	
	public void ACMEdited(ACMEditEvent e){
		switch (e.getOperation()){			
			case ACMEditEvent.ADD_TIER:	
			case ACMEditEvent.REMOVE_TIER:	
			case ACMEditEvent.CHANGE_TIER:		
				updateTiers(e.getOperation());
			break;
		}
	}
	
	/**
	 * Class to update the elapsed time and time since
	 * the last update from the recognizer
	 *  
	 * @author aarsom
	 *
	 */
	private class Timer{		
		private Thread internalThread;
		private boolean stopUpdate;
		
		private JPanel timerPanel;
		private JLabel elapseTimeLabel;
		private JLabel lastUpdateLabel;
		
		private long lastUpdateTime;		
		
		SimpleDateFormat df = new SimpleDateFormat("mm:ss");
		
		public Timer() {
		}
		
		public JPanel getTimerPanel(){
			if(timerPanel == null){
				elapseTimeLabel = new JLabel("00:00");
				lastUpdateLabel = new JLabel("00:00");
				
				JLabel elapseLabel = new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.Timer.ElapseTime")+ " ");
				JLabel updateLabel = new JLabel(ElanLocale.getString("Recognizer.RecognizerPanel.Timer.UpdateTime")+ " ");
				
				Font font = new Font(elapseTimeLabel.getFont().getFontName(), Font.PLAIN, 10);				
				elapseTimeLabel.setFont(font);
				lastUpdateLabel.setFont(font);
				elapseLabel.setFont(font);
				updateLabel.setFont(font);
				
				timerPanel = new JPanel();
				timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(elapseLabel);
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(elapseTimeLabel);				
				timerPanel.add(Box.createHorizontalStrut(15));
				timerPanel.add(updateLabel);				
				timerPanel.add(Box.createHorizontalStrut(10));
				timerPanel.add(lastUpdateLabel);
			}
			
			return timerPanel;
		}
		
		public void resetTimer(){
			elapseTimeLabel.setText("00:00");
			lastUpdateLabel.setText("00:00");
			stopUpdate = false;
			repaint();
		}
		
		public void recognizerUpdate(){
			lastUpdateTime = System.currentTimeMillis();;
		}
		
		public void start(){
			resetTimer();
			
		    internalThread = new Thread("ElapseTimer"){
		    	public void run() {
					try {						
		    			long startTime = System.currentTimeMillis();
		    			lastUpdateTime = System.currentTimeMillis();
		    	
		    	        while ( !stopUpdate) {
		    	        	internalThread.sleep(1000);
		    	        	long currTime = System.currentTimeMillis();		    	       
			    	        	
			    	        elapseTimeLabel.setText(df.format(new Date(currTime - startTime)));
			    	        lastUpdateLabel.setText(df.format(new Date(currTime - lastUpdateTime)));
		    	        }
					} catch ( InterruptedException x ) {
			            //x.printStackTrace();
		             }
				}
		    };
			internalThread.start();
		}			
	
	    public void stop(){
	    	stopUpdate = true;
	    	if(internalThread != null && internalThread.isAlive()){
	    		internalThread.interrupt();
	    	}
	    }
	}
	
	/**
	 * Popup window for configuring properties 
	 * for the recognizer 
	 * 
	 * @author Aarthy Somasundaram
	 */
	private class ConfigWindow extends JWindow implements ActionListener{
		private JPanel compPanel;
		private JButton closeButton;
		private JCheckBox reduceFilePromptCB;
		
		/**
		 * @param owner
		 */
		public ConfigWindow(Window owner) {
			super(owner);
			initComponents();
		}
		
		private void initComponents() {
			Icon icon = null;
			String text = null;
			try {
				icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Close16.gif"));
			} catch (Exception ex) {// any
				text = "X";
			}
			compPanel = new JPanel(new GridBagLayout());
			compPanel.setBorder(new CompoundBorder(new LineBorder(Constants.SHAREDCOLOR6, 1), 
					new EmptyBorder(2, 4, 2, 2)));

			closeButton = new JButton(text, icon);// load icon...
			closeButton.setToolTipText(ElanLocale.getString("Button.Close"));
			closeButton.setBorderPainted(false);
			closeButton.setPreferredSize(new Dimension(16, 16));
			closeButton.addActionListener(this);
			
			reduceFilePromptCB = new JCheckBox(ElanLocale.getString("Recognizer.RecognizerPanel.ReduceFilePrompt"));
			reduceFilePromptCB.setSelected(reduceFilePrompt);
			reduceFilePromptCB.addActionListener(this);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridwidth = 2;
			compPanel.add(closeButton, gbc);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridwidth = 1;
			gbc.gridy = 1;
			compPanel.add(reduceFilePromptCB, gbc);		
			
			add(compPanel);
		}

		/**
		 * Action events.
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == closeButton) {
				close();
			}	else if(e.getSource() == reduceFilePromptCB){
				reduceFilePrompt = reduceFilePromptCB.isSelected();
			}		
		}		
		
		/**
		 * Closes the window.
		 */
		private void close() {			
			setVisible(false);
			dispose();
		}

	}
}
