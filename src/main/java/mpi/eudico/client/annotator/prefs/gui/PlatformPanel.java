package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A panel for OS specific preference settings.
 *
 * @author Han Sloetjes
 */
public class PlatformPanel extends JPanel implements PreferenceEditor, ChangeListener {
    private JCheckBox macScreenBarCB;
    private boolean origMacUseScreenBar = true;
    private JCheckBox macLAndFCB;
    private boolean origMacLF = true;
    private JCheckBox macFileDialogCB;
    private boolean origMacFileDialog = true;
    private JRadioButton cocoaQTB;
    private JRadioButton qtB;
    private String origMacPrefFramework = "CocoaQT";
    private boolean origPermDetached = false;
    private JCheckBox permDetachedCB;

    // windows
    private JRadioButton jdsRB;
    private JCheckBox jmmfCB;
    private JRadioButton dsShowRB;
    private JRadioButton winQTRB;
    private JRadioButton jmfRB;
    private String origWinPrefFramework = "JDS";
    private JCheckBox winLAndFCB;
    private boolean origWinLF = false;
    private boolean origJMMFEnabled = true;
    private JCheckBox correctAtPauseCB;
    private boolean origCorrectAtPause = true;

    /**
     * Creates a new PlatformPanel instance
     */
    public PlatformPanel() {
        super();
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
    	Object val = null;
    	if (SystemReporting.isMacOS()) {
    		val = Preferences.get("OS.Mac.useScreenMenuBar", null);

            if (val instanceof Boolean) {
                origMacUseScreenBar = ((Boolean) val).booleanValue();
            }

            val = Preferences.get("UseMacLF", null);

            if (val instanceof Boolean) {
                origMacLF = ((Boolean) val).booleanValue();
            }
            
            val = Preferences.get("UseMacFileDialog", null);

            if (val instanceof Boolean) {
            	origMacFileDialog = ((Boolean) val).booleanValue();
            }

            val = Preferences.get("Mac.PrefMediaFramework", null);

            if (val instanceof String) {
                origMacPrefFramework = (String) val;
            }

            val = Preferences.get("PreferredMediaWindow", null);

            if (val instanceof Boolean) {
                origPermDetached = ((Boolean) val).booleanValue();
            }

    	} else if (SystemReporting.isWindows()) {
            val = Preferences.get("Windows.PrefMediaFramework", null);

            if (val instanceof String) {
                origWinPrefFramework = (String) val;
            }
            
            val = Preferences.get("UseWinLF", null);

            if (val instanceof Boolean) {
                origWinLF = ((Boolean) val).booleanValue();
            }
            
            val = Preferences.get("Windows.JMMFEnabled", null);
            
            if (val instanceof Boolean) {
            	origJMMFEnabled = ((Boolean) val).booleanValue();
            }
            
            val = Preferences.get("Windows.JMMFPlayer.CorrectAtPause", null);
            
            if (val instanceof Boolean) {
            	origCorrectAtPause = ((Boolean) val).booleanValue();
            }
    	}
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 0, 2, 0);
        GridBagConstraints gbc = new GridBagConstraints();
        Font plainFont = null;
        int gy = 0;
        
        if (SystemReporting.isMacOS()) {	        	
	        macScreenBarCB = new JCheckBox(ElanLocale.getString(
	                    "PreferencesDialog.OS.Mac.ScreenMenuBar"));
	        macScreenBarCB.setSelected(origMacUseScreenBar);
	
	        plainFont = macScreenBarCB.getFont().deriveFont(Font.PLAIN);
	        macScreenBarCB.setFont(plainFont);
	
	        //JLabel macLabel = new JLabel(ElanLocale.getString("PreferencesDialog.OS.Mac"));
	        
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridwidth = 2;
	        gbc.gridy = gy++;
	        gbc.insets = insets;
	        add(new JLabel(ElanLocale.getString("PreferencesDialog.OS.Mac")), gbc);
	
	        gbc.gridy = gy++;
	        gbc.gridwidth = 1;
	        //gbc.fill = GridBagConstraints.NONE;
	        //gbc.weightx = 0.0;
	        add(macScreenBarCB, gbc);
	
	        JLabel relaunchLabel = new JLabel();
	        ImageIcon relaunchIcon = null;
	
	        // add relaunch icon
	        try {
	            relaunchIcon = new ImageIcon(this.getClass()
	                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	            relaunchLabel.setIcon(relaunchIcon);
	        } catch (Exception ex) {
	            relaunchLabel.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        macScreenBarCB.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	
	        gbc.gridx = 1;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        add(relaunchLabel, gbc);
	
	        macLAndFCB = new JCheckBox(ElanLocale.getString(
	                    "PreferencesDialog.OS.Mac.LF"));
	        macLAndFCB.setSelected(origMacLF);
	        macLAndFCB.setFont(plainFont);
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        add(macLAndFCB, gbc);
	
	        JLabel relaunchLabel2 = new JLabel();
	
	        if (relaunchIcon != null) {
	            relaunchLabel2.setIcon(relaunchIcon);
	        } else {
	            relaunchLabel2.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel2.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        macLAndFCB.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	
	        gbc.gridx = 1;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        add(relaunchLabel2, gbc);
	        
	        
	        macFileDialogCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.OS.Mac.FileDialog"));
	        macFileDialogCB.setSelected(origMacFileDialog);
	        macFileDialogCB.setFont(plainFont);
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        add(macFileDialogCB, gbc);     
	
	        JLabel frameworkLabel = new JLabel(ElanLocale.getString(
	                    "Player.Framework"));
	        frameworkLabel.setFont(plainFont);
	        cocoaQTB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.Cocoa"));
	        cocoaQTB.setFont(plainFont);
	        qtB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.QTJ"));
	        qtB.setFont(plainFont);
	
	        if (origMacPrefFramework.equals("CocoaQT")) {
	            cocoaQTB.setSelected(true);
	        } else {
	            qtB.setSelected(true);
	        }
	
	        ButtonGroup gr = new ButtonGroup();
	        gr.add(cocoaQTB);
	        gr.add(qtB);
	
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.weightx = 1.0;
	        gbc.insets = new Insets(12, 0, 2, 0);
	        add(frameworkLabel, gbc);
	
	        gbc.gridy = gy++;
	        gbc.insets = insets;
	        add(cocoaQTB, gbc);
	        gbc.gridy = gy++;
	        add(qtB, gbc);
	
	        permDetachedCB = new JCheckBox(ElanLocale.getString(
	                    "PreferencesDialog.OS.Mac.DetachedMedia"), origPermDetached);
	        permDetachedCB.setFont(plainFont);
	
	        gbc.gridy = gy++;
	        add(permDetachedCB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weighty = 1.0;
	        add(new JPanel(), gbc); // filler
        } else if (SystemReporting.isWindows()) {
	        // add Windows stuff
	        gbc = new GridBagConstraints();
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridwidth = 2;
	        gbc.gridy = gy++;
	        gbc.insets = new Insets(12, 0, 2, 0);
	        add(new JLabel(ElanLocale.getString("PreferencesDialog.OS.Windows")),
	            gbc);
	
	        gbc.insets = insets;
	        //gbc.gridy = 8;
	
	        ButtonGroup winBG = new ButtonGroup();
	        jdsRB = new JRadioButton(ElanLocale.getString(
	        		"PreferencesDialog.Media.JDS"), true);
	        jmmfCB = new JCheckBox(ElanLocale.getString(
    				"PreferencesDialog.Media.JMMF"), origJMMFEnabled);
	        correctAtPauseCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.JMMF.CorrectAtPause"), origCorrectAtPause);
	        dsShowRB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.WMP"));
	        winQTRB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.QTJ"));
	        jmfRB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.JMF"));
	        winBG.add(jdsRB);
	        winBG.add(dsShowRB);
	        winBG.add(winQTRB);
	        winBG.add(jmfRB);
	        
	        plainFont = dsShowRB.getFont().deriveFont(Font.PLAIN);
	        JLabel winMedia = new JLabel(ElanLocale.getString("Player.Framework"));
	        winMedia.setFont(plainFont);
	        gbc.gridy = gy++;
	        add(winMedia, gbc);
	
	        if (origWinPrefFramework.equals("NativeWindows")) {
	        	dsShowRB.setSelected(true);
	        } else if (origWinPrefFramework.equals("QT")) {
	            winQTRB.setSelected(true);
	        } else if (origWinPrefFramework.equals("JMF")) {
	            jmfRB.setSelected(true);
	        }
	
	        jdsRB.setFont(plainFont);
	        jmmfCB.setFont(plainFont);
	        dsShowRB.setFont(plainFont);
	        winQTRB.setFont(plainFont);
	        jmfRB.setFont(plainFont);
	        correctAtPauseCB.setFont(plainFont);
	
	        gbc.insets = insets;
	        gbc.gridy = gy++;
	        add(jdsRB, gbc);
	        Insets ins = new Insets(insets.top, insets.left + 16, insets.bottom, insets.right);
	        gbc.gridy = gy++;
	        gbc.insets = ins;
	        add(jmmfCB, gbc);
	        gbc.gridy = gy++;
	        add(correctAtPauseCB, gbc);
	        gbc.insets = insets;
	        gbc.gridy = gy++;
	        add(dsShowRB, gbc);
	        gbc.gridy = gy++;
	        add(winQTRB, gbc);
	        gbc.gridy = gy++;
	        add(jmfRB, gbc);
	
	        // look and feel
	        // add relaunch icon
	        JLabel relaunchLabel = new JLabel();
	        ImageIcon relaunchIcon = null;
	        try {
	            relaunchIcon = new ImageIcon(this.getClass()
	                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	            relaunchLabel.setIcon(relaunchIcon);
	        } catch (Exception ex) {
	            relaunchLabel.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        
	        winLAndFCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.OS.Windows.LF"), origWinLF);
	        winLAndFCB.setFont(plainFont);
	        gbc.insets = new Insets(12, 0, 2, 0);
	        gbc.gridwidth = 1;
	        gbc.gridy = gy++;
	        add(winLAndFCB, gbc);
	        
	        gbc.gridx = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        add(relaunchLabel, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weighty = 1.0;
	        add(new JPanel(), gbc); // filler
	        
	        if (SystemReporting.isWindows7OrHigher() || SystemReporting.isWindowsVista()) {
	        	jdsRB.addChangeListener(this);
	        	dsShowRB.addChangeListener(this);
	        	jmfRB.addChangeListener(this);
	        	jmmfCB.setEnabled(jdsRB.isSelected());
	        	correctAtPauseCB.setEnabled(jdsRB.isSelected());
	        } else {
	        	jmmfCB.setEnabled(false);//??
	        	jmmfCB.setVisible(false);
	        	correctAtPauseCB.setVisible(false);
	        }
        }
    }

    /**
     * @see mpi.eudico.client.annotator.prefs.PreferenceEditor#getChangedPreferences()
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(4);

        	if (SystemReporting.isMacOS()) {
        		if (macScreenBarCB.isSelected() != origMacUseScreenBar) {
                    chMap.put("OS.Mac.useScreenMenuBar",
                        new Boolean(macScreenBarCB.isSelected()));
                }

                if (macLAndFCB.isSelected() != origMacLF) {
                    chMap.put("UseMacLF", new Boolean(macLAndFCB.isSelected()));
                }
                
                if (macFileDialogCB.isSelected() != origMacFileDialog) {
                    chMap.put("UseMacFileDialog", new Boolean(macFileDialogCB.isSelected()));
                }

                String tmp = "CocoaQT";

                if (qtB.isSelected()) {
                    tmp = "QT";
                }

                if (!origMacPrefFramework.equals(tmp)) {
                    chMap.put("Mac.PrefMediaFramework", tmp);
                    //apply immediately
                    System.setProperty("PreferredMediaFramework", tmp);
                }

                if (origPermDetached != permDetachedCB.isSelected()) {
                    chMap.put("PreferredMediaWindow",
                        new Boolean(permDetachedCB.isSelected()));
                }
        	} else if (SystemReporting.isWindows()) {               
                String winTmp = "JDS";

                if (dsShowRB.isSelected()) {
                	winTmp = "NativeWindows";
                } else if (winQTRB.isSelected()) {
                    winTmp = "QT";
                } else if (jmfRB.isSelected()) {
                    winTmp = "JMF";
                }

                if (!origWinPrefFramework.equals(winTmp)) {
                    chMap.put("Windows.PrefMediaFramework", winTmp);
                    //apply immediately
                    System.setProperty("PreferredMediaFramework", winTmp);
                }
                
                if (origWinLF != winLAndFCB.isSelected()) {
                	chMap.put("UseWinLF", winLAndFCB.isSelected());
                }
                
                if (origJMMFEnabled != jmmfCB.isSelected()) {
                	chMap.put("Windows.JMMFEnabled", jmmfCB.isSelected());
                }
                
                if (origCorrectAtPause != correctAtPauseCB.isSelected()) {
                	chMap.put("Windows.JMMFPlayer.CorrectAtPause", correctAtPauseCB.isSelected());
                }
        	}
        	
            return chMap;
        }

        return null;
    }

    /**
     * @see mpi.eudico.client.annotator.prefs.PreferenceEditor#isChanged()
     */
    public boolean isChanged() {
    	if (SystemReporting.isMacOS()) {
    		if ((macScreenBarCB.isSelected() != origMacUseScreenBar) ||
                    (macLAndFCB.isSelected() != origMacLF) ||
                    (macFileDialogCB.isSelected() != origMacFileDialog) ||
                    (permDetachedCB.isSelected() != origPermDetached)) {
                return true;
            }

            String tmp = "CocoaQT";

            if (qtB.isSelected()) {
                tmp = "QT";
            }

            if (!origMacPrefFramework.equals(tmp)) {
                return true;
            }
    	} else if (SystemReporting.isWindows()) {
    		String winTmp = "JDS";

            if (dsShowRB.isSelected()) {
            	winTmp = "NativeWindows";
            } else if (winQTRB.isSelected()) {
                winTmp = "QT";
            } else if (jmfRB.isSelected()) {
                winTmp = "JMF";
            }

            if (!origWinPrefFramework.equals(winTmp)) {
                return true;
            }
            if (origWinLF != winLAndFCB.isSelected()) {
            	return true;
            }
            if (origJMMFEnabled != jmmfCB.isSelected()) {
            	return true;
            }
            if (origCorrectAtPause != correctAtPauseCB.isSelected()) {
            	return true;
            }
    	}
        
        return false;
    }

	public void stateChanged(ChangeEvent ce) {
		jmmfCB.setEnabled(jdsRB.isSelected());
		correctAtPauseCB.setEnabled(jdsRB.isSelected());
	}
}
