package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.prefs.PreferenceEditor;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A panel for changing UI related settings.
 * 
 * @author Mark Blokpoel
  */
public class UIPrefsPanel extends JPanel implements PreferenceEditor {
    private JComboBox nrOfRecentItemsCBox;
    private Integer origNrRecentItems = 5;
    private JCheckBox tooltipCB;
    private boolean origToolTipEnabled = true;
    private JCheckBox showAnnotationCountCB;
    private boolean origShowAnnotationCount = false; 

    /**
     * Creates a new PlatformPanel instance
     */
    public UIPrefsPanel() {
        super();
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Object val = Preferences.get("UI.RecentItems", null);

        if (val instanceof Integer) {
            origNrRecentItems = (Integer) val;
        }
        
        val = Preferences.get("UI.ToolTips.Enabled", null);
        
        if (val instanceof Boolean) {
        	origToolTipEnabled = (Boolean) val;
        }
        
        val = Preferences.get("UI.MenuItems.ShowAnnotationCount", null);
    	if (val instanceof Boolean)
    		origShowAnnotationCount = (Boolean) val;
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 0, 2, 0);
        Integer[] nrOfRecentItemsList = { 5, 10, 15, 20, 25, 30 };
        nrOfRecentItemsCBox = new JComboBox(nrOfRecentItemsList);
        nrOfRecentItemsCBox.setSelectedItem(origNrRecentItems);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.RecentItems")),
            gbc);

        gbc.gridy = 1;

        add(nrOfRecentItemsCBox, gbc);
        /*
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
        nrOfRecentItemsCBox.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Relaunch.Tooltip"));

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        add(relaunchLabel, gbc);
        */

        
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 2, 0);
        add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.ToolTip")), gbc);
        
        tooltipCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.ToolTip.Enabled"));
        tooltipCB.setSelected(origToolTipEnabled);
        tooltipCB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 3;
        gbc.insets = insets;
        add(tooltipCB, gbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 0, 2, 0);
        add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.MenuOptions")), gbc);
        
        showAnnotationCountCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.MenuOptions.ShowAnnotationCount"));
        showAnnotationCountCB.setSelected(origShowAnnotationCount);
        showAnnotationCountCB.setFont(showAnnotationCountCB.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = 5;
        gbc.insets = insets;
        add(showAnnotationCountCB, gbc);
        
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        add(new JPanel(), gbc); // filler
    }
    
    /**
     * Returns
     *
     * @return a map containing the changes
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map chMap = new HashMap(2);

            if (nrOfRecentItemsCBox.getSelectedItem() != origNrRecentItems) {
                chMap.put("UI.RecentItems",
                    nrOfRecentItemsCBox.getSelectedItem());
            }
            // will be handled by ElanLayoutManager (arbitrary choice)
            if (tooltipCB.isSelected() != origToolTipEnabled) {
            	chMap.put("UI.ToolTips.Enabled",
                        new Boolean(tooltipCB.isSelected()));
            }
            if(showAnnotationCountCB.isSelected() != origShowAnnotationCount){
        		chMap.put( "UI.MenuItems.ShowAnnotationCount", new Boolean(showAnnotationCountCB.isSelected()) );
        	}
            return chMap;
        }

        return null;
    }

    /**
     * Returns whether any of the settings has changed
     *
     * @return whether anything changed
     */
    public boolean isChanged() {
        if (nrOfRecentItemsCBox.getSelectedItem() != origNrRecentItems || 
        		tooltipCB.isSelected() != origToolTipEnabled || 
        		showAnnotationCountCB.isSelected() != origShowAnnotationCount) {
            return true;
        }

        return false;
    }
}
