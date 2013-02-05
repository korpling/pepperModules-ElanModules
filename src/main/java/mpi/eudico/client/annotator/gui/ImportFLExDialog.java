package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.FileExtension;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexDecoderInfo;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * A dialog for configuring the import of FieldWorks Language Explorer, FLEx,
 * format.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImportFLExDialog extends ClosableDialog implements ActionListener,
    ItemListener {
    private JPanel filePanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JTextField flexFileField;
    private JTextField mediaFileField;
    private JButton selectFlexButton;
    private JButton selectMediaButton;
    private JCheckBox includeITCB;
    private JCheckBox includeParagrCB;
    private JComboBox unitsCombo;
    private JRadioButton fileDurationRB;
    private JRadioButton unitDurationRB;
    private JTextField fileDurTextField;
    private JTextField unitTextField;
    private JLabel msPerUnitLabel;
    private JButton okButton;
    private JButton cancelButton;
    private final String[] elements = new String[] {
            FlexConstants.IT, FlexConstants.PARAGR, FlexConstants.PHRASE,
            FlexConstants.WORD
        };
    private List<String> tempMediaPaths;
    private FlexDecoderInfo decoderInfo = null;

    /**
     * Constructor.
     *
     * @param owner
     *
     * @throws HeadlessException
     */
    public ImportFLExDialog(Frame owner) throws HeadlessException {
        super(owner, true);
        initComponents();
        postInit();
    }

    /**
     * Returns the created decoder info object, or null in case the dialog was
     * canceled.
     *
     * @return the decoder info storing the user's selections for the parser
     */
    public FlexDecoderInfo getDecoderInfo() {
        return decoderInfo;
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("Menu.File.Import.FLEx"));
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(new TitledBorder(""));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        filePanel.add(new JLabel(ElanLocale.getString("ImportDialog.Flex.File")),
            gbc);
        gbc.gridy = 1;
        filePanel.add(new JLabel(ElanLocale.getString(
                    "ImportDialog.Label.Media")), gbc);

        flexFileField = new JTextField("", 20);
        flexFileField.setEditable(false);
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filePanel.add(flexFileField, gbc);

        mediaFileField = new JTextField("", 20);
        mediaFileField.setEditable(false);
        gbc.gridy = 1;
        filePanel.add(mediaFileField, gbc);

        selectFlexButton = new JButton("...");
        selectFlexButton.addActionListener(this);
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        filePanel.add(selectFlexButton, gbc);
        selectMediaButton = new JButton("...");
        selectMediaButton.addActionListener(this);
        gbc.gridy = 1;
        filePanel.add(selectMediaButton, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(filePanel, gbc);

        // options panel
        optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ImportDialog.Label.Options")));
        includeITCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludeIT"));
        includeITCB.setSelected(true);
        includeParagrCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludePara"));
        includeParagrCB.setSelected(true);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        optionsPanel.add(includeITCB, gbc);
        gbc.gridy = 1;
        optionsPanel.add(includeParagrCB, gbc);

        JLabel smallestLabel = new JLabel(ElanLocale.getString(
                    "ImportDialog.Flex.SmallestTimeAlignable"));
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(12, 6, 2, 6);
        optionsPanel.add(smallestLabel, gbc);

        unitsCombo = new JComboBox(elements);
        unitsCombo.setSelectedItem(FlexConstants.PHRASE);
        unitsCombo.addItemListener(this);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        optionsPanel.add(unitsCombo, gbc);

        JLabel durLabel = new JLabel(ElanLocale.getString("Player.duration"));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(12, 6, 2, 6);
        optionsPanel.add(durLabel, gbc);

        ButtonGroup group = new ButtonGroup();
        fileDurationRB = new JRadioButton(ElanLocale.getString(
                    "ImportDialog.Flex.FileDuration"));
        fileDurationRB.addActionListener(this);
        fileDurationRB.setSelected(true);
        group.add(fileDurationRB);
        gbc.gridy = 4;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 2;
        optionsPanel.add(fileDurationRB, gbc);

        fileDurTextField = new JTextField("", 8);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        optionsPanel.add(fileDurTextField, gbc);

        unitDurationRB = new JRadioButton(ElanLocale.getString(
                    "ImportDialog.Flex.UnitDuration"));
        unitDurationRB.addActionListener(this);
        group.add(unitDurationRB);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = insets;
        gbc.gridwidth = 3;
        optionsPanel.add(unitDurationRB, gbc);

        unitTextField = new JTextField("", 8);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 26, 2, 6);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        optionsPanel.add(unitTextField, gbc);

        msPerUnitLabel = new JLabel(ElanLocale.getString(
                    "ImportDialog.Flex.MsPer") + "   < " +
                unitsCombo.getSelectedItem() + " >");
        gbc.gridx = 1;
        gbc.insets = insets;
        gbc.gridwidth = 2;
        optionsPanel.add(msPerUnitLabel, gbc);

        unitTextField.setEnabled(false);
        msPerUnitLabel.setEnabled(false);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(optionsPanel, gbc);

        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(buttonPanel, gbc);
    }

    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true); //blocks
    }

    private void enableFileDuration(boolean enabled) {
        fileDurTextField.setEnabled(enabled);
        unitTextField.setEnabled(!enabled);
        msPerUnitLabel.setEnabled(!enabled);
    }

    private void selectFlexFile() {
        FileChooser chooser = new FileChooser((Frame)getParent());
        chooser.createAndShowFileDialog(ElanLocale.getString("Button.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.XML_EXT, false, "LastUsedFlexDir", FileChooser.FILES_ONLY, null);
	    File f = chooser.getSelectedFile();
        if (f != null) {
        	flexFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void selectMediaFiles() {
    	FileChooser chooser = new FileChooser((Frame)getParent());
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("Button.Select"), FileChooser.MEDIA);       
        Object[] files = chooser.getSelectedFiles();
        
        if (files != null) {    
            if (files.length > 0) {
                if (tempMediaPaths == null) {
                    tempMediaPaths = new ArrayList<String>(4);
                } else {
                    tempMediaPaths.clear();
                }

                String filePaths = "";

                for (int i = 0; i < files.length; i++) {
                    tempMediaPaths.add(((File) files[i]).getAbsolutePath());
                    filePaths += (files[i] + ", ");
                }

                mediaFileField.setText(filePaths);
            }
        }
    }

    /**
     * Checks the fields for valid selections and prompts if anything is
     * missing.
     *
     * @return true if enough information has been provided to continue with
     *         the import, false otherwise
     */
    private boolean checkFields() {
        String path = flexFileField.getText();

        if ((path == null) || (path.length() == 0)) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ImportDialog.Flex.Message.NoFlex"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            return false;
        } else {
            File f = new File(path);

            if (!f.exists() || f.isDirectory()) {
                String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
                strMessage += path;
                strMessage += ElanLocale.getString("Menu.Dialog.Message2");

                String strError = ElanLocale.getString("Message.Error");
                JOptionPane.showMessageDialog(this, strMessage, strError,
                    JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }

        // replace all backslashes by forward slashes
        path = path.replace('\\', '/');

        long durationVal = -1;

        if (fileDurationRB.isSelected()) {
            try {
                durationVal = Long.parseLong(fileDurTextField.getText());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ImportDialog.Flex.Message.DurFile"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return false;
            }
        } else {
            try {
                durationVal = Long.parseLong(unitTextField.getText());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ImportDialog.Flex.Message.DurElement"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }

        // if we get here create the decoder object 
        decoderInfo = new FlexDecoderInfo(path);
        decoderInfo.smallestWithTimeAlignment = (String) unitsCombo.getSelectedItem();
        decoderInfo.inclITElement = includeITCB.isSelected();
        decoderInfo.inclParagraphElement = includeParagrCB.isSelected();

        if (fileDurationRB.isSelected()) {
            decoderInfo.totalDurationSpecified = true;
            decoderInfo.totalDuration = durationVal;
        } else {
            decoderInfo.totalDurationSpecified = false;
            decoderInfo.perElementDuration = durationVal;
        }

        if ((tempMediaPaths != null) && (tempMediaPaths.size() > 0)) {
            ArrayList<MediaDescriptor> descriptors = new ArrayList<MediaDescriptor>(tempMediaPaths.size());

            for (String medPath : tempMediaPaths) {
                MediaDescriptor descriptor = MediaDescriptorUtil.createMediaDescriptor(medPath);

                if (descriptor != null) {
                    descriptors.add(descriptor);
                }
            }

            decoderInfo.setMediaDescriptors(descriptors);
        }

        return true;
    }

    /**
     * Handling of action events fired by the buttons.
     *
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectFlexButton) {
            selectFlexFile();
        } else if (e.getSource() == selectMediaButton) {
            selectMediaFiles();
        } else if (e.getSource() == fileDurationRB) {
            enableFileDuration(true);
        } else if (e.getSource() == unitDurationRB) {
            enableFileDuration(false);
        } else if (e.getSource() == okButton) {
            if (checkFields()) { // creates a decoder info object
                setVisible(false);
            }
        } else if (e.getSource() == cancelButton) {
            decoderInfo = null;
            setVisible(false);
        }
    }

    /**
     * Handling of combobox selection events.
     *
     * @param e event
     */
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == unitsCombo) {
            msPerUnitLabel.setText(ElanLocale.getString(
                    "ImportDialog.Flex.MsPer") + "   < " +
                unitsCombo.getSelectedItem() + " >");
        }
    }
}
