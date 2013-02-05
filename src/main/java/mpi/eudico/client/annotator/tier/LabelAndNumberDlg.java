package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.client.annotator.gui.ClosableDialog;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * A dialog for selection of tiers and customization of the format for the  new
 * labels (prefix + index).
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class LabelAndNumberDlg extends ClosableDialog implements ActionListener,
    ChangeListener, ItemListener, ListSelectionListener, CellEditorListener {
    private TranscriptionImpl transcription;

    // ui elements
    private JPanel titlePanel;
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
    private JRadioButton singleTierRB;
    private JRadioButton multiTierRB;
    private JTable tierTable;
    private TierExportTableModel model;
    private JCheckBox labelCheckBox;
    private JTextField labelTextField;
    private JCheckBox delimiterCheckBox;
    private JRadioButton spaceRadioButton;
    private JSpinner spaceSpinner;
    private JRadioButton otherDelRadioButton;
    private JTextField otherDelTextField;
    private JCheckBox includeNumberCheckBox;
    private JComboBox numberFormatComboBox;
    private JCheckBox leadingZerosCheckBox;
    private JRadioButton minIntDigitsRadioButton;
    private JSpinner numZerosSpinner;
    private JRadioButton minIntDigitsDependsRadioButton;
    private JLabel startLabel;
    private JSpinner startSpinner;
    private JLabel incrementLabel;
    private JSpinner incrementSpinner;
    private JLabel exampleLabel;
    private String intFormat = "Integer";
    private String decFormat = "Decimal";

    /**
     * Creates a new LabelAndNumberDlg instance
     *
     * @param transcription the transcription that hold the tiers
     */
    public LabelAndNumberDlg(TranscriptionImpl transcription, Frame frame) {
        super(frame);
        this.transcription = transcription;
        initComponents();
        extractTiers();
        postInit();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });
        titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        titleLabel = new JLabel();
        singleTierRB = new JRadioButton();
        multiTierRB = new JRadioButton();

        ButtonGroup tierGroup = new ButtonGroup();
        tierGroup.add(singleTierRB);
        tierGroup.add(multiTierRB);
        model = new TierExportTableModel();
        model.setColumnCount(2);
        tierTable = new JTable(model);

        DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
        tierTable.getColumnModel().getColumn(0).setCellEditor(cellEd);
        tierTable.getColumnModel().getColumn(0)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
        tierTable.getSelectionModel()
                 .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);

        JScrollPane tierScroll = new JScrollPane(tierTable);
        tierScroll.setPreferredSize(new Dimension(100, 100));

        labelCheckBox = new JCheckBox();
        labelTextField = new JTextField(6);
        delimiterCheckBox = new JCheckBox();
        spaceRadioButton = new JRadioButton();
        spaceSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        otherDelRadioButton = new JRadioButton();
        otherDelTextField = new JTextField(4);
        includeNumberCheckBox = new JCheckBox();
        numberFormatComboBox = new JComboBox();
        leadingZerosCheckBox = new JCheckBox();
        minIntDigitsRadioButton = new JRadioButton();
        numZerosSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        minIntDigitsDependsRadioButton = new JRadioButton();
        startLabel = new JLabel();
        startSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        incrementLabel = new JLabel();
        incrementSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        exampleLabel = new JLabel();
        exampleLabel.setFont(exampleLabel.getFont().deriveFont(Font.ITALIC));
        exampleLabel.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
        exampleLabel.setBorder(new LineBorder(Constants.ACTIVEANNOTATIONCOLOR));
        // defaults
        singleTierRB.setSelected(true);
        labelCheckBox.setSelected(true);
        spaceRadioButton.setSelected(true);
        delimiterCheckBox.setSelected(true);
        otherDelRadioButton.setSelected(false);
        otherDelTextField.setEnabled(false);

        ButtonGroup delGroup = new ButtonGroup();
        delGroup.add(spaceRadioButton);
        delGroup.add(otherDelRadioButton);
        includeNumberCheckBox.setSelected(true);
        intFormat = ElanLocale.getString("LabelAndNumberDialog.Label.IntFormat");
        decFormat = ElanLocale.getString(
                "LabelAndNumberDialog.Label.IntAndFractionFormat");
        numberFormatComboBox.addItem(intFormat);
        numberFormatComboBox.addItem(decFormat);
        leadingZerosCheckBox.setSelected(true);
        minIntDigitsRadioButton.setSelected(false);
        numZerosSpinner.setEnabled(false);
        minIntDigitsDependsRadioButton.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(minIntDigitsRadioButton);
        group.add(minIntDigitsDependsRadioButton);

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
        tierPanel.add(singleTierRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierPanel.add(multiTierRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        tierPanel.add(tierScroll, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        //tierPanel.add(selectTierComboBox, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierPanel, gridBagConstraints);

        optionsPanel.setLayout(new GridBagLayout());
        insets.bottom = 3;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(labelCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(labelTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(delimiterCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(spaceRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,6,4,6);
        optionsPanel.add(spaceSpinner, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);;
        optionsPanel.add(otherDelRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,6,4,6);
        optionsPanel.add(otherDelTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(includeNumberCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,6,4,6);
        optionsPanel.add(numberFormatComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(leadingZerosCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets =  new Insets(4,22,4,6);
        optionsPanel.add(minIntDigitsRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4,6,4,6);;
        optionsPanel.add(numZerosSpinner, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets =  new Insets(4,22,4,6);
        optionsPanel.add(minIntDigitsDependsRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(startLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(startSpinner, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(incrementLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2,80,2,6);
        optionsPanel.add(incrementSpinner, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = insets;
        optionsPanel.add(exampleLabel, gridBagConstraints);

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
        singleTierRB.addActionListener(this);
        multiTierRB.addActionListener(this);
        labelCheckBox.addChangeListener(this);
        delimiterCheckBox.addChangeListener(this);
        spaceRadioButton.addChangeListener(this);
        otherDelRadioButton.addChangeListener(this);
        includeNumberCheckBox.addChangeListener(this);
        numberFormatComboBox.addItemListener(this);
        leadingZerosCheckBox.addChangeListener(this);
        minIntDigitsRadioButton.addChangeListener(this);
        minIntDigitsDependsRadioButton.addChangeListener(this);
        spaceSpinner.addChangeListener(this);
        numZerosSpinner.addChangeListener(this);
        startSpinner.addChangeListener(this);
        incrementSpinner.addChangeListener(this);

        KeyAdapter keyList = new KeyAdapter() {
                public void keyReleased(KeyEvent ke) {
                    updateExample();
                }
            };

        labelTextField.addKeyListener(keyList);
        otherDelTextField.addKeyListener(keyList);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        //setSize((getSize().width < minimalWidth) ? minimalWidth : getSize().width,
        //    (getSize().height < minimalHeight) ? minimalHeight : getSize().height);
        setLocationRelativeTo(getParent());
        setResizable(false);
        updateExample();
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
        setTitle(ElanLocale.getString("LabelAndNumberDialog.Title"));
        titleLabel.setText(ElanLocale.getString("LabelAndNumberDialog.Title"));
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Tier")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Options")));
        singleTierRB.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.Single"));
        multiTierRB.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.Multi"));
        labelCheckBox.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.IncludeLabel"));
        delimiterCheckBox.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.InsertDelimiter"));
        spaceRadioButton.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.InsertSpace"));
        otherDelRadioButton.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.InsertOther"));
        includeNumberCheckBox.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.IncludeNumber"));
        leadingZerosCheckBox.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.LeadingZeros"));
        minIntDigitsRadioButton.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.MinIntDigits"));
        minIntDigitsDependsRadioButton.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.MinDigitsDepends"));
        startLabel.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.Start"));
        incrementLabel.setText(ElanLocale.getString(
                "LabelAndNumberDialog.Label.Increment"));
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
        exampleLabel.setText("nnn");
    }

    /**
     * Formats an exampple label showing the output of the first label
     * according to the current settings.
     */
    private void updateExample() {
        StringBuffer sb = new StringBuffer();

        if (labelCheckBox.isSelected()) {
            sb.append(labelTextField.getText());
        }

        if (delimiterCheckBox.isSelected()) {
            if (spaceRadioButton.isSelected() && spaceRadioButton.isEnabled()) {
                int spaces = 1;
                Object so = spaceSpinner.getValue();

                if (so instanceof Integer) {
                    spaces = ((Integer) so).intValue();

                    for (int i = 0; i < spaces; i++) {
                        sb.append(" ");
                    }
                }
            } else if (otherDelRadioButton.isSelected() &&
                    otherDelRadioButton.isEnabled()) {
                sb.append(otherDelTextField.getText());
            }
        }

        if (includeNumberCheckBox.isSelected()) {
            if (leadingZerosCheckBox.isSelected()) {
                int numDig = 1;

                if (minIntDigitsRadioButton.isSelected()) {
                    Object nd = numZerosSpinner.getValue();

                    if (nd instanceof Integer) {
                        numDig = ((Integer) nd).intValue();
                    }
                } else {
                    List tierNames = getSelectedTiers();
                    TierImpl tier = null;
                    int count = 0;

                    for (int i = 0; i < tierNames.size(); i++) {
                        tier = (TierImpl) transcription.getTierWithId((String) tierNames.get(
                                    i));

                        if (tier != null) {
                            count += tier.getAnnotations().size();
                        }
                    }

                    Object startObj = startSpinner.getValue();
                    Object increObj = incrementSpinner.getValue();

                    if (startObj instanceof Integer) {
                        int start = ((Integer) startObj).intValue();
                        count = start +
                            ((count - 1) * ((Integer) increObj).intValue());
                    } else {
                        double startd = ((Double) startObj).doubleValue();
                        count = (int) (startd +
                            ((count - 1) * ((Double) increObj).doubleValue()));
                    }

                    while (count >= 10) {
                        numDig++;
                        count = count / 10;
                    }
                }

                Object stObj = startSpinner.getValue();

                if (stObj != null) {
                    String stVal = stObj.toString();
                    int index = stVal.indexOf('.');

                    if (index < 0) {
                        index = stVal.length();
                    }

                    for (int i = 0; i < (numDig - index); i++) {
                        sb.append("0");
                    }

                    sb.append(stVal);
                }
            } else {
                Object stObj = startSpinner.getValue();

                if (stObj != null) {
                    sb.append(stObj.toString());
                }
            }
        }

        if (sb.length() == 0) {
            exampleLabel.setText(" ");
        } else {
            exampleLabel.setText(sb.toString());
        }
    }

    /**
     * Checks the current settings and creates a Command.
     */
    private void startOperation() {
        List tierNames = null;
        String prefix = null;
        Object format = null;
        Integer numDigits = null;
        Object startVal = null;
        Object incrementVal = null;

        tierNames = getSelectedTiers();

        if (tierNames.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("LabelAndNumberDialog.Warning.NoTier"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        if (labelCheckBox.isSelected()) {
            StringBuffer buf = new StringBuffer();
            buf.append(labelTextField.getText());

            if (delimiterCheckBox.isSelected()) {
                if (spaceRadioButton.isSelected() &&
                        spaceRadioButton.isEnabled()) {
                    int spaces = 1;
                    Object so = spaceSpinner.getValue();

                    if (so instanceof Integer) {
                        spaces = ((Integer) so).intValue();

                        for (int i = 0; i < spaces; i++) {
                            buf.append(" ");
                        }
                    }
                } else if (otherDelRadioButton.isSelected() &&
                        otherDelRadioButton.isEnabled()) {
                    buf.append(otherDelTextField.getText());
                }
            }

            prefix = buf.toString();
        }

        if (includeNumberCheckBox.isSelected()) {
            if (decFormat.equals(numberFormatComboBox.getSelectedItem())) {
                format = new Double(0);
            } else {
                format = new Integer(0);
            }

            if (leadingZerosCheckBox.isSelected()) {
                int numDig = 1;

                if (minIntDigitsRadioButton.isSelected()) {
                    Object nd = numZerosSpinner.getValue();

                    if (nd instanceof Integer) {
                        numDig = ((Integer) nd).intValue();
                    }
                } else {
                    String name = null;
                    TierImpl tier = null;
                    int count = 0;

                    for (int i = 0; i < tierNames.size(); i++) {
                        name = (String) tierNames.get(i);
                        tier = (TierImpl) transcription.getTierWithId(name);

                        if (tier != null) {
                            count += tier.getAnnotations().size();
                        }
                    }

                    Object startObj = startSpinner.getValue();
                    Object increObj = incrementSpinner.getValue();

                    if (startObj instanceof Integer) {
                        int start = ((Integer) startObj).intValue();
                        count = start +
                            ((count - 1) * ((Integer) increObj).intValue());
                    } else {
                        double startd = ((Double) startObj).doubleValue();
                        count = (int) (startd +
                            ((count - 1) * ((Double) increObj).doubleValue()));
                    }

                    while (count >= 10) {
                        numDig++;
                        count = count / 10;
                    }
                }

                numDigits = new Integer(numDig);
            } else {
                numDigits = new Integer(0);
            }

            if (format instanceof Integer) {
                startVal = (Integer) startSpinner.getValue();
                incrementVal = (Integer) incrementSpinner.getValue();
            } else {
                startVal = (Double) startSpinner.getValue();
                incrementVal = (Double) incrementSpinner.getValue();
            }
        } else {
            numDigits = new Integer(0);
        }

        Object[] args = new Object[] {
                tierNames, prefix, format, numDigits, startVal, incrementVal
            };
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.LABEL_AND_NUMBER);

        command.execute(transcription, args);
    }

    /**
     * Returns the tiers that heve been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List getSelectedTiers() {
        List tiers = new ArrayList();
        Object selObj = null;
        Object nameObj = null;

        for (int i = 0; i < tierTable.getRowCount(); i++) {
            selObj = tierTable.getValueAt(i, 0);

            if (selObj == Boolean.TRUE) {
                nameObj = tierTable.getValueAt(i, 1);

                if (nameObj != null) {
                    tiers.add(nameObj);
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
    private void closeDialog(WindowEvent evt) {
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
            closeDialog(null);
        } else if (ae.getSource() == singleTierRB) {
            boolean oneSel = false;
            int selRow = -1;
            tierTable.getSelectionModel()
                     .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // make sure that only one tier is selected
            for (int i = 0; i < tierTable.getRowCount(); i++) {
                if (!oneSel) {
                    if (tierTable.getValueAt(i, 0) == Boolean.TRUE) {
                        oneSel = true;
                        selRow = i;
                    }
                } else {
                    tierTable.setValueAt(Boolean.FALSE, i, 0);
                }
            }

            if (selRow > -1) {
                tierTable.getSelectionModel()
                         .setSelectionInterval(selRow, selRow);
            }
        } else if (ae.getSource() == multiTierRB) {
            tierTable.getSelectionModel()
                     .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
    	if (e.getSource() == labelCheckBox) {
            if (labelCheckBox.isSelected()) {
                labelTextField.setEnabled(true);
                delimiterCheckBox.setEnabled(true);
                spaceRadioButton.setEnabled(true);

                if (spaceRadioButton.isSelected()) {
                    spaceSpinner.setEnabled(true);
                }
            } else {
                labelTextField.setEnabled(false);
                delimiterCheckBox.setEnabled(false);
                spaceRadioButton.setEnabled(false);
                spaceSpinner.setEnabled(false);
            }
        } else if (e.getSource() == delimiterCheckBox) {
            boolean sel = delimiterCheckBox.isSelected();
            spaceRadioButton.setEnabled(sel);

            if (sel && spaceRadioButton.isSelected()) {
                spaceSpinner.setEnabled(true);
            } else {
                spaceSpinner.setEnabled(false);
            }

            otherDelRadioButton.setEnabled(sel);

            if (sel && otherDelRadioButton.isSelected()) {
                otherDelTextField.setEnabled(true);
            } else {
                otherDelTextField.setEnabled(false);
            }
        } else if (e.getSource() == spaceRadioButton) {
            if (spaceRadioButton.isSelected()) {
                spaceSpinner.setEnabled(true);
                otherDelTextField.setEnabled(false);
            } else {
                spaceSpinner.setEnabled(false);
                otherDelTextField.setEnabled(true);
            }
        } else if (e.getSource() == otherDelRadioButton) {
            if (otherDelRadioButton.isSelected()) {
                spaceSpinner.setEnabled(false);
                otherDelTextField.setEnabled(true);
            } else {
                spaceSpinner.setEnabled(true);
                otherDelTextField.setEnabled(false);
            }
        } else if (e.getSource() == includeNumberCheckBox) {
            boolean select = includeNumberCheckBox.isSelected();

            if (labelCheckBox.isSelected()) {
                //delimiterCheckBox.setSelected(select);
                delimiterCheckBox.setEnabled(select);
                spaceRadioButton.setEnabled(select);

                if (select && spaceRadioButton.isSelected()) {
                    spaceSpinner.setEnabled(true);
                } else {
                    spaceSpinner.setEnabled(false);
                }
            }

            numberFormatComboBox.setEnabled(select);
            leadingZerosCheckBox.setEnabled(select);
            minIntDigitsDependsRadioButton.setEnabled(select);
            minIntDigitsRadioButton.setEnabled(select);

            if (minIntDigitsRadioButton.isSelected() && select) {
                numZerosSpinner.setEnabled(true);
            } else {
                numZerosSpinner.setEnabled(false);
            }

            startSpinner.setEnabled(select);
            incrementSpinner.setEnabled(select);
        } else if (e.getSource() == leadingZerosCheckBox) {
            boolean select = leadingZerosCheckBox.isSelected();
            minIntDigitsRadioButton.setEnabled(select);
            minIntDigitsDependsRadioButton.setEnabled(select);

            if (minIntDigitsRadioButton.isSelected() && select) {
                numZerosSpinner.setEnabled(true);
            } else {
                numZerosSpinner.setEnabled(false);
            }
        } else if (e.getSource() == minIntDigitsRadioButton) {
            numZerosSpinner.setEnabled(minIntDigitsRadioButton.isSelected());
        } else if (e.getSource() == minIntDigitsDependsRadioButton) {
            numZerosSpinner.setEnabled(!minIntDigitsDependsRadioButton.isSelected());
        }

        updateExample();
    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        if ((e.getSource() == numberFormatComboBox) &&
                (e.getStateChange() == ItemEvent.SELECTED)) {
            String val = (String) numberFormatComboBox.getSelectedItem();
            startSpinner.removeChangeListener(this);
            incrementSpinner.removeChangeListener(this);

            if (decFormat.equals(val)) {
                Object spinVal = startSpinner.getValue();
                startSpinner.setModel(new SpinnerNumberModel(1.0d, 0.0d,
                        1000.0d, 1.0d));
                startSpinner.setValue(new Double(((Integer) spinVal).intValue()));
                spinVal = incrementSpinner.getValue();
                incrementSpinner.setModel(new SpinnerNumberModel(1.0d, 0.0d,
                        100.0d, 0.1d));
                incrementSpinner.setValue(new Double(
                        ((Integer) spinVal).intValue()));
            } else {
                Object spinVal = startSpinner.getValue();
                startSpinner.setModel(new SpinnerNumberModel(1, 0, 1000, 1));
                startSpinner.setValue(new Integer(
                        (int) ((Double) spinVal).doubleValue()));
                spinVal = incrementSpinner.getValue();
                incrementSpinner.setModel(new SpinnerNumberModel(1, 0, 100, 1));
                incrementSpinner.setValue(new Integer(
                        (int) ((Double) spinVal).doubleValue()));
            }

            startSpinner.addChangeListener(this);
            incrementSpinner.addChangeListener(this);
        }

        updateExample();
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

        updateExample();
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

            updateExample();
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
