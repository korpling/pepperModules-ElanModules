package mpi.eudico.client.annotator.md.imdi;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;

import mpi.eudico.client.annotator.tier.TierExportTableModel;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * A panel for configuration / selection of imdi metadata fields.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiConfigurationPanel extends MDConfigurationPanel
    implements ListSelectionListener, ActionListener {
    private ImdiFileServiceProvider provider;
    private DefaultTableModel model;
    private JTable keyTable;
    private final String SEL_COLUMN = "Select";
    private final String KEY_COLUMN = "Key";

    /**
     * Creates a new ImdiConfigurationPanel instance
     *
     * @param provider the provider to configure
     */
    public ImdiConfigurationPanel(ImdiFileServiceProvider provider) {
        super();
        this.provider = provider;
        initComponents();
    }

    private void initComponents() {
    	setLayout(new GridBagLayout());
        setBorder(new TitledBorder(ElanLocale.getString("MetadataViewer.SelectKeys")));

        model = new TierExportTableModel();//reuse
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
        if (provider != null && provider.getKeys() != null) {
            String key;

            for (int i = 0; i < provider.getKeys().size(); i++) {
                key = (String) provider.getKeys().get(i);

                if (provider.getSelectedKeys().contains(key)) {
                    model.addRow(new Object[] { Boolean.TRUE, key });
                } else {
                    model.addRow(new Object[] { Boolean.FALSE, key });
                }
            }
        }

        JScrollPane keyScrollPane = new JScrollPane(keyTable);
        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        add(keyScrollPane, gbc);

        //setPreferredSize(new Dimension(300, 460));
    }

    /**
     * Applies changes made to the selection.
     */
    @Override
    public void applyChanges() {
        List<String> nextSelected = new ArrayList<String>();
        int includeCol = model.findColumn(SEL_COLUMN);
        int nameCol = model.findColumn(KEY_COLUMN);

        // add selected keys in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                nextSelected.add((String) model.getValueAt(i, nameCol));
            }
        }

        if (provider != null) {
            provider.setSelectedKeys(nextSelected);
        }
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
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
