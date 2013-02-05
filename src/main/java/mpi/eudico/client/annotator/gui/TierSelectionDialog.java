package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.tier.TierExportTableModel;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * A dialog to select tier names from a list (table) of tier names.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TierSelectionDialog extends ClosableDialog
    implements ListSelectionListener, ActionListener {
    private List<String> allTier;
    private List<String> selectedTier;
    private List<String> returnedTiers = null;
    private DefaultTableModel model;
    private JTable tierTable;
    private JButton selAllButton;
    private JButton deselAllButton;
    private JButton okButton;
    private JButton cancelButton;

    /** column id for the include in export checkbox column, invisible */
    protected final String EXPORT_COLUMN = "export";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";

    /**
     * Creates a new TierSelectionDialog instance
     *
     * @param owner parent dialog
     * @param allTier the list of tiers
     * @param selectedTier the list of selected tiers
     *
     * @throws HeadlessException he
     */
    public TierSelectionDialog(Dialog owner, List<String> allTier,
        List<String> selectedTier) throws HeadlessException {
        super(owner, true);
        this.allTier = allTier;
        this.selectedTier = selectedTier;
        returnedTiers = selectedTier;
        initComponents();
    }

    /**
     * Creates a new TierSelectionDialog instance
     *
     * @param owner parent dialog
     * @param allTier the list of tiers
     * @param selectedTier the list of selected tiers
     *
     * @throws HeadlessException
     */
    public TierSelectionDialog(Frame owner, List<String> allTier,
        List<String> selectedTier) throws HeadlessException {
        super(owner, true);
        this.allTier = allTier;
        this.selectedTier = selectedTier;
        returnedTiers = selectedTier;
        initComponents();
    }

    private void initComponents() {
        JPanel cp = new JPanel(new GridBagLayout());
        cp.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.SelectTiers")));

        model = new TierExportTableModel();
        tierTable = new JTable(model);

        model.setColumnIdentifiers(new String[] { EXPORT_COLUMN, TIER_NAME_COLUMN });
        tierTable.getColumn(EXPORT_COLUMN)
                 .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        tierTable.getColumn(EXPORT_COLUMN)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumn(EXPORT_COLUMN).setMaxWidth(30);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);
        tierTable.getSelectionModel().addListSelectionListener(this);

        if (allTier != null) {
            for (String name : allTier) {
                if (selectedTier != null) {
                    if (selectedTier.contains(name)) {
                        model.addRow(new Object[] { Boolean.TRUE, name });
                    } else {
                        model.addRow(new Object[] { Boolean.FALSE, name });
                    }
                } else {
                    model.addRow(new Object[] { Boolean.TRUE, name });
                }
            }
        }

        Insets insets = new Insets(4, 6, 4, 6);
        Dimension tableDim = new Dimension(120, 200);

        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        cp.add(tierScrollPane, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 6, 4));
        selAllButton = new JButton(ElanLocale.getString(
                    "ExportDialog.Label.SelectAll"));
        selAllButton.addActionListener(this);
        buttonPanel.add(selAllButton);

        deselAllButton = new JButton(ElanLocale.getString(
                    "ExportDialog.Label.DeselectAll"));
        deselAllButton.addActionListener(this);
        buttonPanel.add(deselAllButton);

        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        cp.add(buttonPanel, gbc);
        getContentPane().add(cp);

        pack();

        if (getParent() != null) {
            setLocationRelativeTo(getParent());
        }
    }

    private List<String> getSelectedTiers() {
        int includeCol = model.findColumn(EXPORT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);

        ArrayList<String> selectedTiers = new ArrayList<String>();

        // add selected tiers in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                selectedTiers.add((String) model.getValueAt(i, nameCol));
            }
        }

        return selectedTiers;
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == selAllButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.TRUE, i, 0);
                }
            }
        } else if (ae.getSource() == deselAllButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.FALSE, i, 0);
                }
            }
        } else if (ae.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (ae.getSource() == okButton) {
            returnedTiers = getSelectedTiers();
            setVisible(false);
            dispose();
        }
    }

    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers
     */
    public List<String> getValue() {
        return returnedTiers;
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model.findColumn(EXPORT_COLUMN);

            for (int i = b; i <= e; i++) {
                if (tierTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
    }
}
