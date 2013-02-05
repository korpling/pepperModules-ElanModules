package mpi.eudico.client.annotator.prefs.gui;

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
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A panel for changing settings concerning preferences files.
 *
 * @author Han Sloetjes
 */
public class GeneralPrefsPanel extends JPanel implements PreferenceEditor,
    ActionListener {
    private String curGenPrefsLocation = "-";
    private JLabel setDirLabel;
    private JLabel curDirLabel;
    private JButton defaultDirButton;
    private JButton resetDirButton;
    private JComboBox nrOfBuFilesCB;
    private Integer origNumBuFiles = 1;
    private JCheckBox checkForUpdatesCB;
    private boolean origCheckUpdates = true;

    /**
     * Constructor. Reads the current preferences and creates the ui.
     */
    public GeneralPrefsPanel() {
        super();
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Object val = Preferences.get("DefaultPreferencesLocation", null);

        if (val instanceof String) {
            curGenPrefsLocation = (String) val;
        }
        
        val = Preferences.get("NumberOfBackUpFiles", null);
        
        if (val instanceof Integer) {
			origNumBuFiles = (Integer) val;
		}
        
        val = Preferences.get("AutomaticUpdate", null);
        
        if (val instanceof Boolean) {
        	origCheckUpdates = (Boolean) val;
		}
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 0, 2, 0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        gbc.gridwidth = 3;
        add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.Location")),
            gbc);

        setDirLabel = new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Prefs.DefaultLoc"));
        setDirLabel.setFont(setDirLabel.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        add(setDirLabel, gbc);
        curDirLabel = new JLabel(curGenPrefsLocation);
        curDirLabel.setFont(curDirLabel.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 2;
        add(curDirLabel, gbc);

        defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(defaultDirButton, gbc);
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
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 10, 2, 0);
        add(resetDirButton, gbc);
        resetDirButton.addActionListener(this);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(12, 0, 2, 0);
        add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.NumBackUp")), gbc);
        
        Integer[] nrOfBuItemsList = { 1, 2, 3, 4, 5 };
        nrOfBuFilesCB = new JComboBox(nrOfBuItemsList);
        nrOfBuFilesCB.setSelectedItem(origNumBuFiles);
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 0, 2, 0);
        add(nrOfBuFilesCB, gbc);
        
        checkForUpdatesCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Prefs.AutoUpdate"), origCheckUpdates);
        gbc.gridy = 5;
        gbc.insets = new Insets(12, 0, 2, 0);
        add(checkForUpdatesCB, gbc);
        
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        
        add(new JPanel(), gbc); // filler
    }

    /**
     * Returns the changed preferences.  return a map of changed preferences
     *
     * @return a map with changed preferences
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(2);

            if ((curDirLabel.getText() != null) &&
                    !curDirLabel.getText().equals("-")) {
                chMap.put("DefaultPreferencesLocation", curDirLabel.getText());
            } else {
            	chMap.put("DefaultPreferencesLocation", null);
            }
            
            if (origNumBuFiles != nrOfBuFilesCB.getSelectedItem()) {
            	chMap.put("NumberOfBackUpFiles", nrOfBuFilesCB.getSelectedItem());
            }
            
            if (origCheckUpdates != checkForUpdatesCB.isSelected()) {
            	chMap.put("AutomaticUpdate", checkForUpdatesCB.isSelected());
            }

            return chMap;
        }

        return null;
    }

    /**
     * Returns whether or not anything changed.
     *
     * @return true if anything changed, false otherwise
     */
    public boolean isChanged() {
        return !curGenPrefsLocation.equals(curDirLabel.getText()) || origNumBuFiles != nrOfBuFilesCB.getSelectedItem() ||
        		origCheckUpdates != checkForUpdatesCB.isSelected();
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

            File startDir = new File(System.getProperty("user.home"));
            if (curGenPrefsLocation.length() > 1) {
                File dir = new File(FileUtility.urlToAbsPath(
                            curGenPrefsLocation));

                if (dir.exists() && dir.isDirectory()) {
                    startDir = dir;
                }
            }

            chooser.setCurrentDirectory(startDir.getAbsolutePath());            
            chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
            		null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
            File selFile = chooser.getSelectedFile();
            if (selFile != null) {
                curDirLabel.setText(selFile.getAbsolutePath());
                curDirLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        } else if (e.getSource() == resetDirButton) {
        	curDirLabel.setText("-");
        }
    }
}
