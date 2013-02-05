package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;

import mpi.eudico.client.annotator.util.FileUtility;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel showing option to change the media navigation setting: <br>
 * - frame forward/backward jumps to the begin of next/previous frame (default
 * it jumps with the amount of ms of the duration of a single frame)  - a
 * default location (directory) to search for media files (after the
 * "traditional" location)
 *
 * @author Han Sloetjes
 * @version 2.0, Dec 2008
 */
public class MediaNavPanel extends JPanel implements PreferenceEditor, ChangeListener,
    ActionListener {
    private boolean origFrameStepToFrameBegin = false;
    private String curMediaLocation = "-";
    private boolean origVideoSameSize = false;
    private boolean videoInCentre = false;
    private boolean origAltMediaLocSetsDirty = true;
    private String origTimeFormat = Constants.MS_STRING;    
    private boolean origPromptForFilename = true;
    private boolean origOnlyClipFirstMediaFile = false;
    private boolean origClipInParallel = true;
    private JCheckBox frameStepCB;
    private JCheckBox videosSameSizeCB;
    private JCheckBox videosInCentreCB;
    private JLabel setDirLabel;
    private JLabel curDirLabel;
    private JButton defaultDirButton;
    private JButton resetDirButton;
    private JCheckBox changedMediaLocCB;
    private JLabel timeFormatLabel;
    private JComboBox timeFormatComboBox;
    private JCheckBox promptForFilenameCB;
    private JCheckBox onlyClipFirstMediaFileCB;
    private JCheckBox clipInParallelCB; 
   
    private String HH_MM_SS_MS = ElanLocale.getString("TimeCodeFormat.Hours");
    private String SS_MS = ElanLocale.getString("TimeCodeFormat.Seconds");
    private String MS = ElanLocale.getString("TimeCodeFormat.MilliSec");   
    private String NTSC = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC");   
    private String PAL = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL");
    
    private Map<String, String> tcMap;
    private int origTimeFormatIndex;
    
    /**
     * Creates a new MediaNavPanel instance
     */
    public MediaNavPanel() {
        super();
        tcMap = new HashMap<String, String>(5);
        tcMap.put(HH_MM_SS_MS, Constants.HHMMSSMS_STRING);
        tcMap.put(SS_MS, Constants.SSMS_STRING);
        tcMap.put(MS, Constants.MS_STRING);
        tcMap.put(NTSC, Constants.NTSC_STRING);
        tcMap.put(PAL, Constants.PAL_STRING);
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Object val = Preferences.get("MediaNavigation.FrameStepToFrameBegin",
                null);

        if (val instanceof Boolean) {
            origFrameStepToFrameBegin = ((Boolean) val).booleanValue();
        }

        val = Preferences.get("DefaultMediaLocation", null);

        if (val instanceof String) {
            curMediaLocation = (String) val;
        }

        val = Preferences.get("Media.VideosSameSize", null);

        if (val instanceof Boolean) {
            origVideoSameSize = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("Media.VideosCentre", null);

        if (val instanceof Boolean) {
            videoInCentre = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("MediaLocation.AltLocationSetsChanged", null);
        
        if (val instanceof Boolean) {
        	origAltMediaLocSetsDirty = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("CurrentTime.Copy.TimeFormat", null);
        
        if (val instanceof String) {
        	// take into account possible older localized stored preferences
        	String storedPref = (String) val;
        	if (tcMap.containsKey(storedPref)) {
        		origTimeFormat = tcMap.get(storedPref);
        	} else {
        		if (tcMap.values().contains(storedPref)) {
        			origTimeFormat = storedPref;
        		}
        		// if not, it might be string from yet another language  
        	}
        	//origTimeFormat should now be a non localized string
        }

        val = Preferences.get("Media.PromptForFilename", null);

        if (val instanceof Boolean) {
            origPromptForFilename = ((Boolean) val).booleanValue();
        }

        val = Preferences.get("Media.OnlyClipFirstMediaFile", null); 

        if (val instanceof Boolean) {
            origOnlyClipFirstMediaFile = ((Boolean) val).booleanValue();
        }

        val = Preferences.get("Media.ClipInParallel", null); 

        if (val instanceof Boolean) {
            origClipInParallel = ((Boolean) val).booleanValue();
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        
	JPanel mediaNavPanel = new JPanel(new GridBagLayout());	

        Insets insets = new Insets(2, 0, 2, 0);
        frameStepCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.MediaNav.FrameBegin"),
                origFrameStepToFrameBegin);
        frameStepCB.setFont(frameStepCB.getFont().deriveFont(Font.PLAIN));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        gbc.gridwidth = 3;
        mediaNavPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Category.MediaNav")), gbc);

        gbc.gridy = 1;
        mediaNavPanel.add(frameStepCB, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 2, 0);
        mediaNavPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Media.VideoDisplay")), gbc);
        videosSameSizeCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Media.VideoSize"), origVideoSameSize);
        gbc.gridy = 3;
        gbc.insets = insets;
        videosSameSizeCB.setFont(videosSameSizeCB.getFont()
                                                 .deriveFont(Font.PLAIN));
        mediaNavPanel.add(videosSameSizeCB, gbc);
        
        videosInCentreCB = new JCheckBox(ElanLocale.getString(
        	"PreferencesDialog.Media.VideoCentre"), videoInCentre );
        //videosInCentreCB.addChangeListener(this);
        videosInCentreCB.addActionListener(this);
        gbc.gridy = 4;
        gbc.insets = insets;
        videosInCentreCB.setFont(videosInCentreCB.getFont().deriveFont(Font.PLAIN));
        mediaNavPanel.add(videosInCentreCB, gbc);
        
        gbc.gridy = 5;
        gbc.insets = new Insets(12, 0, 2, 0);
        mediaNavPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Media.Location")),
            gbc);
        
        setDirLabel = new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Media.DefaultLoc"));
        setDirLabel.setFont(setDirLabel.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = insets;
        mediaNavPanel.add(setDirLabel, gbc);

        curDirLabel = new JLabel(curMediaLocation);
        curDirLabel.setFont(curDirLabel.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 7;
        mediaNavPanel.add(curDirLabel, gbc);

        defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridheight = 2; // don't forget to reset
        gbc.anchor = GridBagConstraints.NORTHEAST;
        mediaNavPanel.add(defaultDirButton, gbc);
        defaultDirButton.addActionListener(this);

        resetDirButton = new JButton();

        ImageIcon resetIcon = null;

        // add reset icon
        try {
            resetIcon = new ImageIcon(this.getClass()
                                          .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
            resetDirButton.setIcon(resetIcon);
        } catch (Exception ex) {
            resetDirButton.setText("X");
        }

        resetDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetDirButton.setPreferredSize(new Dimension(
                resetDirButton.getPreferredSize().width,
                defaultDirButton.getPreferredSize().height));
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 10, 2, 0);
        mediaNavPanel.add(resetDirButton, gbc);
        resetDirButton.addActionListener(this);
        
        changedMediaLocCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.SaveAltLocation"));
        changedMediaLocCB.setFont(changedMediaLocCB.getFont().deriveFont(Font.PLAIN));
        changedMediaLocCB.setSelected(origAltMediaLocSetsDirty);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        mediaNavPanel.add(changedMediaLocCB, gbc); 
        
        timeFormatLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Media.TimeFormat"));
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 0, 2, 0);
        mediaNavPanel.add(timeFormatLabel, gbc);
        
        timeFormatComboBox = new JComboBox();
        timeFormatComboBox.addItem(HH_MM_SS_MS);   
        timeFormatComboBox.addItem(SS_MS);
        timeFormatComboBox.addItem(MS);    
        timeFormatComboBox.addItem(NTSC);    
        timeFormatComboBox.addItem(PAL);
        
        boolean prefRestored = false;
        Iterator<String> tcIt = tcMap.keySet().iterator();
        String key;
        String tcConst = null;
        while (tcIt.hasNext()) {
        	key = tcIt.next();
        	tcConst = tcMap.get(key);
        	if (tcConst.equals(origTimeFormat)) {
        		timeFormatComboBox.setSelectedItem(key);
        		prefRestored = true;
        		break;
        	}
        }
        if (!prefRestored) {
        	timeFormatComboBox.setSelectedItem(MS);
        }
        origTimeFormatIndex = timeFormatComboBox.getSelectedIndex();
        
        gbc.gridx=1;
        gbc.gridwidth = 2;
        mediaNavPanel.add(timeFormatComboBox, gbc);
        
        gbc.weightx = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 1; // reset height from previous change, increase rows by one again
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.gridx = 0;
        gbc.gridy = 12;
        mediaNavPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Media.Clipping")), gbc); 
        
        promptForFilenameCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.PromptForFilename"), 
        	origPromptForFilename);
        gbc.gridy = 13;
        promptForFilenameCB.setFont(promptForFilenameCB.getFont().deriveFont(Font.PLAIN));
        mediaNavPanel.add(promptForFilenameCB, gbc); 
       
        onlyClipFirstMediaFileCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.OnlyClipFirstMediaFile"), 
        	origOnlyClipFirstMediaFile);
        gbc.gridy = 14;
        onlyClipFirstMediaFileCB.setFont(onlyClipFirstMediaFileCB.getFont().deriveFont(Font.PLAIN));
        mediaNavPanel.add(onlyClipFirstMediaFileCB, gbc); 
       
        clipInParallelCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.ClipInParallel"),
        	origClipInParallel);
        gbc.gridy = 15;
        clipInParallelCB.setFont(clipInParallelCB.getFont().deriveFont(Font.PLAIN));
        mediaNavPanel.add(clipInParallelCB, gbc); 
       
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        mediaNavPanel.add(new JPanel(), gbc); // filler
        
        
        JScrollPane scrollPane = new JScrollPane(mediaNavPanel);
        scrollPane.setBorder(new TitledBorder(ElanLocale.getString("PreferencesDialog.Category.Media")));   
        scrollPane.setBackground(mediaNavPanel.getBackground());

        gbc = new GridBagConstraints();
        gbc.insets = insets;       
        gbc.weighty = 1.0;   
        gbc.weightx = 1.0;  
        gbc.fill = gbc.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(scrollPane , gbc);

    }

    /**
     * Returns the changed pref.
     *
     * @return a map with the changed pref, or null
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map chMap = new HashMap(4);

            if (frameStepCB.isSelected() != origFrameStepToFrameBegin) {
                chMap.put("MediaNavigation.FrameStepToFrameBegin",
                    new Boolean(frameStepCB.isSelected()));
            }

            if (videosSameSizeCB.isSelected() != origVideoSameSize) {
                chMap.put("Media.VideosSameSize",
                    new Boolean(videosSameSizeCB.isSelected()));
            }
            
            if (videosInCentreCB.isSelected() != videoInCentre) {
                chMap.put("Media.VideosCentre",
                    new Boolean(videosInCentreCB.isSelected()));
            }

            if ((curDirLabel.getText() != null) &&
                    !curDirLabel.getText().equals("-")) {
                chMap.put("DefaultMediaLocation", curDirLabel.getText());
            } else {
                chMap.put("DefaultMediaLocation", null);
            }
            
            if (changedMediaLocCB.isSelected() != origAltMediaLocSetsDirty) {
            	chMap.put("MediaLocation.AltLocationSetsChanged", 
            			new Boolean(changedMediaLocCB.isSelected()));
            }   
            
            //if(!timeFormatComboBox.getSelectedItem().toString().equals(origTimeFormat)){
            if (origTimeFormatIndex != timeFormatComboBox.getSelectedIndex()) {
            	chMap.put("CurrentTime.Copy.TimeFormat", 
            			tcMap.get(timeFormatComboBox.getSelectedItem()));
            }
            
            if (promptForFilenameCB.isSelected() != origPromptForFilename) {
                chMap.put("Media.PromptForFilename",
                    new Boolean(promptForFilenameCB.isSelected()));
            } 

            if (onlyClipFirstMediaFileCB.isSelected() != origOnlyClipFirstMediaFile) {
                chMap.put("Media.OnlyClipFirstMediaFile",
                    new Boolean(onlyClipFirstMediaFileCB.isSelected()));
            } 

            if (clipInParallelCB.isSelected() != origClipInParallel) {
                chMap.put("Media.ClipInParallel",
                    new Boolean(clipInParallelCB.isSelected()));
            } 

            return chMap;
        }

        return null;
    }

    /**
     * Returns whether anything has changed.
     *
     * @return whether anything has changed
     */
    public boolean isChanged() {
        return ((frameStepCB.isSelected() != origFrameStepToFrameBegin) ||
        !curMediaLocation.equals(curDirLabel.getText()) ||
        (videosSameSizeCB.isSelected() != origVideoSameSize) ||
        (videosInCentreCB.isSelected() != videoInCentre) ||
        (changedMediaLocCB.isSelected() != origAltMediaLocSetsDirty) || 
        //(!origTimeFormat.equals(timeFormatComboBox.getSelectedItem().toString()))) ||
        (origTimeFormatIndex != timeFormatComboBox.getSelectedIndex()) ||
        (promptForFilenameCB.isSelected() != origPromptForFilename) ||
        (onlyClipFirstMediaFileCB.isSelected() != origOnlyClipFirstMediaFile) ||
        (clipInParallelCB.isSelected() != origClipInParallel));
    }

    /**
     * Action event handling
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == defaultDirButton) {
            // show a folder file chooser, set the current def. location
            FileChooser chooser = new FileChooser(this);
            if (curMediaLocation.length() > 1) {
                File dir = new File(FileUtility.urlToAbsPath(curMediaLocation));

                if (dir.exists() && dir.isDirectory()) {
                	 chooser.setCurrentDirectory(dir.getAbsolutePath());
                }
            }
           chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		   null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
            //chooser.setMultiSelectionEnabled(false);
           
           File selFile = chooser.getSelectedFile();
           if (selFile != null) {
               curDirLabel.setText(selFile.getAbsolutePath());
               curDirLabel.setText(FileUtility.pathToURLString(
                       selFile.getAbsolutePath()));
           } 
            
        } else if (e.getSource() == resetDirButton) {
            curDirLabel.setText("-");
        } else if (e.getSource() == videosInCentreCB) {
        	Preferences.set("Media.VideosCentre.Temporary", videosInCentreCB.isSelected(), null);
        }
    }


	public void stateChanged(ChangeEvent e) {// on Windows this is triggered by mouse hover etc. 
		if(e.getSource() == videosInCentreCB ){
			Preferences.set("Media.VideosCentre.Temporary", videosInCentreCB.isSelected(), null);	
		}		
	}
	
}
