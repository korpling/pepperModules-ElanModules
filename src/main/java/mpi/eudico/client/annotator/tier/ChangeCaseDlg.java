package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.client.annotator.gui.ClosableDialog;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * A dialog to select tiers the annotations of which need to be converted to
 * upper- or lowercase.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ChangeCaseDlg extends ClosableDialog implements ActionListener,
    ListSelectionListener, CellEditorListener {
    private TranscriptionImpl transcription;

    // ui elements
    private JPanel titlePanel;
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
    
    private JTable tierTable;
    private TierExportTableModel model;
    
    private JRadioButton upperCaseRB;
    private JRadioButton lowerCaseRB;
    private JCheckBox beginCapCheckBox;

    /**
     * Creates a new LabelAndNumberDlg instance
     *
     * @param transcription the transcription that hold the tiers
     */
    public ChangeCaseDlg(TranscriptionImpl transcription, Frame frame) {
        super(frame);
        this.transcription = transcription;
        initComponents();
        extractTiers();
        postInit();
    }

    private void initComponents() {
        titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        titleLabel = new JLabel();

        model = new TierExportTableModel();
        model.setColumnCount(2);
        tierTable = new JTable(model);

        DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
        tierTable.getColumnModel().getColumn(0).setCellEditor(cellEd);
        tierTable.getColumnModel().getColumn(0)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
        tierTable.getSelectionModel()
                 .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);

        JScrollPane tierScroll = new JScrollPane(tierTable);
        tierScroll.setPreferredSize(new Dimension(100, 100));

        upperCaseRB = new JRadioButton();
        upperCaseRB.setSelected(true);
        lowerCaseRB = new JRadioButton();
        beginCapCheckBox = new JCheckBox();
        beginCapCheckBox.setEnabled(false);
        
        ButtonGroup group = new ButtonGroup();
        group.add(upperCaseRB);
        group.add(lowerCaseRB);

        setModal(true);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints;

        titlePanel.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleLabelPanel = new JPanel();
        titleLabelPanel.add(titleLabel);
        titlePanel.add(titleLabelPanel, BorderLayout.NORTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        tierPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        tierPanel.add(tierScroll, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierPanel, gridBagConstraints);

        optionsPanel.setLayout(new GridBagLayout());
        //insets.bottom = 3;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(upperCaseRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(lowerCaseRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 20, 2, 6);;
        optionsPanel.add(beginCapCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 6, 2, 6);
        getContentPane().add(optionsPanel, gridBagConstraints);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        startButton.addActionListener(this);
        buttonPanel.add(startButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
        updateLocale();

        // addItemListener etc
        upperCaseRB.addActionListener(this);
        lowerCaseRB.addActionListener(this);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        int minimalWidth = 500;
        int minimalHeight = 400;
        setSize((getSize().width < minimalWidth) ? minimalWidth : getSize().width,
            (getSize().height < minimalHeight) ? minimalHeight : getSize().height);
        setLocationRelativeTo(getParent());
        //setResizable(false);
    }

    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
        if (transcription != null) {
            Vector v = transcription.getTiers();
            TierImpl t;

            for (int i = 0; i < v.size(); i++) {
                t = (TierImpl) v.get(i);

                if (i == 0) {
                    model.addRow(new Object[] { Boolean.TRUE, t.getName() });
                } else {
                    model.addRow(new Object[] { Boolean.FALSE, t.getName() });
                }
            }

            tierTable.getSelectionModel().addListSelectionListener(this);
            tierTable.getColumnModel().getColumn(0).getCellEditor()
                     .addCellEditorListener(this);
        }
    }

    private void updateLocale() {
        setTitle(ElanLocale.getString("ChangeCaseDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ChangeCaseDialog.Title"));
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Tier")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Options")));
        upperCaseRB.setText(ElanLocale.getString(
                "ChangeCaseDialog.UpperCase"));
        lowerCaseRB.setText(ElanLocale.getString(
                "ChangeCaseDialog.LowerCase"));
        beginCapCheckBox.setText(ElanLocale.getString(
                "ChangeCaseDialog.Capital"));
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));

    }

   

    /**
     * Checks the current settings and creates a Command.
     */
    private void startOperation() {
        List<String> tierNames = null;

        tierNames = getSelectedTiers();

        if (tierNames.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("LabelAndNumberDialog.Warning.NoTier"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }
       
        //closeDialog(); // to give the command the possibility of showing a monitor??
        Object[] args = new Object[] {
                tierNames, new Boolean(upperCaseRB.isSelected()), new Boolean(beginCapCheckBox.isSelected())
            };
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.CHANGE_CASE);

        command.execute(transcription, args);
    }

    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedTiers() {
        List<String> tiers = new ArrayList<String>();
        Object selObj = null;
        Object nameObj = null;

        for (int i = 0; i < tierTable.getRowCount(); i++) {
            selObj = tierTable.getValueAt(i, 0);

            if (selObj == Boolean.TRUE) {
                nameObj = tierTable.getValueAt(i, 1);

                if (nameObj instanceof String) {
                    tiers.add((String)nameObj);
                }
            }
        }

        return tiers;
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == startButton) {
            startOperation();
        } else if (source == closeButton) {
            closeDialog();
        } else if (ae.getSource() == upperCaseRB || ae.getSource() == lowerCaseRB) {
        	beginCapCheckBox.setEnabled(lowerCaseRB.isSelected());
        }
    }


    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            if (tierTable.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                int i = tierTable.getSelectedRow();

                if (i > -1) {
                    for (int j = 0; j < tierTable.getRowCount(); j++) {
                        if (j != i) {
                            tierTable.setValueAt(Boolean.FALSE, j, 0);
                        } else {
                            tierTable.setValueAt(Boolean.TRUE, j, 0);
                        }
                    }
                    tierTable.revalidate();
                }
            } else {
                int b = lse.getFirstIndex();
                int e = lse.getLastIndex();
                int col = 0;

                for (int i = b; i <= e; i++) {
                    if (tierTable.isRowSelected(i)) {
                        model.setValueAt(Boolean.TRUE, i, col);
                    }
                }
            }
        }

    }

    /**
     * Ensures that only one checkbox is selected in 'single tier' mode.
     *
     * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
     */
    public void editingStopped(ChangeEvent e) {
        if (tierTable.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
            int i = tierTable.getSelectedRow();

            if (i > -1) {
                for (int j = 0; j < tierTable.getRowCount(); j++) {
                    if (j != i) {
                        tierTable.setValueAt(Boolean.FALSE, j, 0);
                    }
                }
            }

        }
    }

    /**
     * Ignored (for the editor is a checkbox)
     *
     * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
     */
    public void editingCanceled(ChangeEvent e) {
        // stub
    }
}
