package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.md.imdi.ImdiFileServiceProvider;
import mpi.eudico.client.annotator.md.imdi.ImdiKeyRenderer;

import mpi.eudico.client.annotator.prefs.PreferenceEditor;

import mpi.eudico.client.annotator.tier.TierExportTableModel;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * A preference editor for visualization of IMDI metadata values.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class MetadataPanel extends JPanel implements PreferenceEditor,
    ListSelectionListener {
    private ImdiFileServiceProvider provider;
    private List origKeys = null;
    private List<String> afterKeys = null;
    private DefaultTableModel model;
    private JTable keyTable;
    private final String SEL_COLUMN = "Select";
    private final String KEY_COLUMN = "Key";

    /**
     * Constructor.
     */
    public MetadataPanel() {
        super();
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Object val = Preferences.get("Metadata.IMDI.Defaults", null);

        if (val instanceof List) {
            origKeys = (List) val;
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
        add(new JLabel(ElanLocale.getString("PreferencesDialog.Metadata.IMDI")),
            gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridy = 1;

        provider = new ImdiFileServiceProvider();

        URL imdiUrl = this.getClass()
                          .getResource("/mpi/eudico/client/annotator/resources/Session.imdi");
        
        provider.setMetadataFile(imdiUrl.toString()); // uses an empty imdi session file
        provider.initialize(); // should all be save
        model = new TierExportTableModel(); //reuse
        keyTable = new JTable(model);

        model.setColumnIdentifiers(new String[] { SEL_COLUMN, KEY_COLUMN });
        keyTable.getColumn(SEL_COLUMN)
                .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        keyTable.getColumn(SEL_COLUMN)
                .setCellRenderer(new CheckBoxTableCellRenderer());
        keyTable.getColumn(SEL_COLUMN).setMaxWidth(30);
        keyTable.getColumn(KEY_COLUMN).setCellRenderer(new ImdiKeyRenderer());
        keyTable.setShowVerticalLines(false);
        keyTable.setTableHeader(null);
        keyTable.getSelectionModel().addListSelectionListener(this);
        keyTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);

        // fill model
        if ((provider != null) && (provider.getKeys() != null)) {
            String key;

            for (int i = 0; i < provider.getKeys().size(); i++) {
                key = (String) provider.getKeys().get(i);

                if ((origKeys != null) && origKeys.contains(key)) {
                    model.addRow(new Object[] { Boolean.TRUE, key });
                } else {
                    model.addRow(new Object[] { Boolean.FALSE, key });
                }
            }
        }

        add(new JScrollPane(keyTable), gbc);
    }

    /**
     * Returns a map of (changed) preferences. The method isChanged() is called 
     * first by the enclosing dialog, this is crucial!
     *
     * @return a map of (changed) preferences
     */
    public Map getChangedPreferences() {
        // rely on the fact that isChanged has been called
        if (afterKeys != null) {
            Map<String, List<String>> prefs = new HashMap<String, List<String>>(1);
            prefs.put("Metadata.IMDI.Defaults", afterKeys);

            return prefs;
        }

        return null;
    }

    /**
     * Compares the current selected items with the initial selected items.
     *
     * @return true if anything changed
     */
    public boolean isChanged() {
        afterKeys = new ArrayList<String>();

        int includeCol = model.findColumn(SEL_COLUMN);
        int nameCol = model.findColumn(KEY_COLUMN);

        // add selected keys in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                afterKeys.add((String) model.getValueAt(i, nameCol));
            }
        }

        if ((origKeys == null) && (afterKeys.size() > 0)) {
            return true;
        }

        if ((origKeys != null) && (afterKeys.size() != origKeys.size())) {
            return true;
        }

        // check whether there is at least one key not present in the both lists
        if (origKeys != null) {
            for (Object key : origKeys) {
                if (!afterKeys.contains(key)) {
                    return true;
                }
            }

            for (Object key : afterKeys) {
                if (!origKeys.contains(key)) {
                    return true;
                }
            }
        }

        afterKeys = null;

        return false;
    }

    /**
     * Updates the checked state of the selection checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model.findColumn(SEL_COLUMN);

            for (int i = b; i <= e; i++) {
                if (keyTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
    }
}
