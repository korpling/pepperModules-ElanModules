package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import mpi.eudico.util.ControlledVocabulary;


public abstract class AbstractEditCVDialog extends JDialog
    implements ActionListener, ItemListener {
    private static final int DEFAULT_MINIMUM_HEIGHT = 500;
    private static final int DEFAULT_MINIMUM_WIDTH = 550;
    
    protected EditCVPanel cvEditorPanel;
    protected JButton addCVButton;
    protected JButton changeCVButton;
    protected JButton closeDialogButton;
    protected JButton deleteCVButton;
    protected JComboBox cvComboBox;
    protected JLabel currentCVLabel;
    protected JLabel cvDescLabel;
    protected JLabel cvNameLabel;
    protected JLabel titleLabel;
    protected JPanel cvButtonPanel;
    protected JPanel cvPanel;
    protected JTextArea cvDescArea;
    protected JTextField cvNameTextField;
    protected String cvContainsEntriesMessage = "contains entries.";
    protected String cvInvalidNameMessage = "Invalid name.";
    protected String cvNameExistsMessage = "Name exists already.";
    protected String deleteQuestion = "delete anyway?";
    protected String oldCVDesc;

    // internal caching fields
    protected String oldCVName;
    protected int minimumHeight;
    protected int minimumWidth;
    private final boolean multipleCVs;

    /**
     * Constructor with standard EditCVPanel
     * @param parent parent window
     * @param modal modality
     * @param multipleCVs if true, user can edit more than one CV
     */
    public AbstractEditCVDialog(Frame parent, boolean modal, boolean multipleCVs) {
        this(parent, modal, multipleCVs, new EditCVPanel());
    }

    /**
     *
     * @param parent parent window
     * @param modal modality
     * @param multipleCVs if true, user can edit more than one CV
     * @param cvEditorPanel panel which might have already controlled vocabulary
     */
    public AbstractEditCVDialog(Frame parent, boolean modal,
        boolean multipleCVs, EditCVPanel cvEditorPanel) {
        super(parent, modal);
        this.cvEditorPanel = cvEditorPanel;
        this.multipleCVs = multipleCVs;
        minimumHeight = DEFAULT_MINIMUM_HEIGHT;
        minimumWidth = DEFAULT_MINIMUM_WIDTH;
        makeLayout();
    }

    /**
     * The button actions.
     *
     * @param actionEvent the actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check source equality
        if (source == closeDialogButton) {
            closeDialog();
        } else if (source == addCVButton) {
            addCV();
        } else if (source == changeCVButton) {
            changeCV();
        } else if (source == deleteCVButton) {
            deleteCV();
        }
    }

    /**
     * Handles a change in the cv selection.
     *
     * @param ie the item event
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == cvComboBox) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                cvEditorPanel.setControlledVocabulary((ControlledVocabulary) cvComboBox.getSelectedItem());
            }

            updateCVButtons();
        }
    }

    /**
     * test method with an cv list of size 0
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        AbstractEditCVDialog dialog = new AbstractEditCVDialog(frame, false,
                false) {
                protected List getCVList() {
                    return new java.util.ArrayList();
                }
            };

        dialog.updateComboBox();
        dialog.pack();
        dialog.setVisible(true);
    }

    protected abstract List getCVList();

    /**
     * Pack, size and set location.
     */
    protected void setPosition() {
        pack();
        setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH),
            Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
        setLocationRelativeTo(getParent());
    }

    /**
     * check if name is valid
     *
     */
    protected void addCV() {
        String name = cvNameTextField.getText();

        name = name.trim();

        if (name.length() == 0) {
            showWarningDialog(cvInvalidNameMessage);

            return;
        }

        if (cvExists(name)) {
            // cv with that name already exists, warn
            showWarningDialog(cvNameExistsMessage);

            return;
        }

        addCV(name);
    }

    /**
     * Creates a new ControlledVocabulary when there isn't already one with the
     * same name and adds it to the List.
     *
     * @param name name of new CV
     */
    protected void addCV(String name) {
        ControlledVocabulary cv = new ControlledVocabulary(name, "");
        cvComboBox.addItem(cv);
        cvEditorPanel.setControlledVocabulary(cv);
    }

    /**
     * Checks whether name is valid and unique.
     */
    protected void changeCV() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (cv == null) {
            return;
        }

        String name = cvNameTextField.getText();
        String desc = cvDescArea.getText();

        if (name != null) {
            name = name.trim();

            if (name.length() < 1) {
                showWarningDialog(cvInvalidNameMessage);
                cvNameTextField.setText(oldCVName);

                return;
            }
        }

        if ((oldCVName != null) && !oldCVName.equals(name)) {
            // check if there is already a cv with the new name
            if (cvExists(name)) {
                // cv with that name already exists, warn
                showWarningDialog(cvNameExistsMessage);

                return;
            }

            changeCV(cv, name, desc);
        } else if (((oldCVDesc == null) && (desc != null) &&
                (desc.length() > 0)) ||
                ((oldCVDesc != null) &&
                ((desc == null) || (desc.length() == 0))) ||
                (!oldCVDesc.equals(desc))) {
            changeCV(cv, null, desc);
        }
    }

    /**
     * changes name and description in specified ControlledVocabulary
     * @param cv ControlledVocabulary to be changed
     * @param name new name (may be null -> no change of name!)
     * @param description new description
     */
    protected void changeCV(ControlledVocabulary cv, String name,
        String description) {
        cv.setDescription(description);

        if (name != null) {
            cv.setName(name);
            cvEditorPanel.setControlledVocabulary(cv);
        }
    }

    /**
    * Closes the dialog
    */
    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     *
     * @param name
     * @return true if ControlledVocabulary with specified name is in the list
     */
    protected boolean cvExists(String name) {
        boolean nameExists = false;

        for (int i = 0; i < cvComboBox.getItemCount(); i++) {
            if (((ControlledVocabulary) cvComboBox.getItemAt(i)).getName()
                     .equals(name)) {
                nameExists = true;

                break;
            }
        }

        return nameExists;
    }

    /**
     * If cv not empty, ask the user for confirmation.
     */
    protected void deleteCV() {
        ControlledVocabulary conVoc = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (conVoc.getEntries().length > 0) {
            String mes = cvContainsEntriesMessage + " " + deleteQuestion;

            if (!showConfirmDialog(mes)) {
                return;
            }
        }

        deleteCV(conVoc);
    }

    /**
     * Deletes controlled vocabulary from the list
     * @param cv ControlledVocabulary to be deleted
     */
    protected void deleteCV(ControlledVocabulary cv) {
        cvComboBox.removeItem(cv);

        if (cvComboBox.getItemCount() > 0) {
            cvComboBox.setSelectedIndex(0);
        } else {
            cvEditorPanel.setControlledVocabulary(null);
        }
    }

    /**
    * makes layout
    */
    protected void makeLayout() {
        JPanel closeButtonPanel;
        JPanel titlePanel;

        GridBagConstraints gridBagConstraints;

        cvPanel = new JPanel();
        currentCVLabel = new JLabel();
        cvComboBox = new JComboBox();
        cvNameLabel = new JLabel();
        cvNameTextField = new JTextField();
        cvDescLabel = new JLabel();
        cvDescArea = new JTextArea();
        cvButtonPanel = new JPanel();
        addCVButton = new JButton();
        changeCVButton = new JButton();
        changeCVButton.setEnabled(false);
        deleteCVButton = new JButton();
        deleteCVButton.setEnabled(false);

        closeButtonPanel = new JPanel();
        closeDialogButton = new JButton();
        titlePanel = new JPanel();
        titleLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    closeDialog();
                }
            });

        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        cvPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(currentCVLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        cvPanel.add(cvNameTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(cvDescLabel, gridBagConstraints);
        cvDescArea.setLineWrap(true);
        cvDescArea.setWrapStyleWord(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(new JScrollPane(cvDescArea), gridBagConstraints);

        cvButtonPanel.setLayout(new GridLayout(0, 1, 6, 6));

        addCVButton.addActionListener(this);
        cvButtonPanel.add(addCVButton);

        changeCVButton.addActionListener(this);
        cvButtonPanel.add(changeCVButton);

        deleteCVButton.addActionListener(this);
        cvButtonPanel.add(deleteCVButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvButtonPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;

        if (multipleCVs) {
            getContentPane().add(cvPanel, gridBagConstraints);
        }

        //
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(cvEditorPanel, gridBagConstraints);

        closeButtonPanel.setLayout(new GridLayout(1, 1, 0, 2));

        closeDialogButton.addActionListener(this);
        closeButtonPanel.add(closeDialogButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(closeButtonPanel, gridBagConstraints);

        InputMap iMap = ((JComponent) getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = ((JComponent) getContentPane()).getActionMap();

        if ((iMap != null) && (aMap != null)) {
            final String esc = "Esc";
            final String enter = "Enter";
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), esc);
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
            aMap.put(esc, new EscapeAction());
            aMap.put(enter, new EnterAction());
        }
    }

    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the messsage to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message, "Warning",
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning",
            JOptionPane.WARNING_MESSAGE);
    }

    protected void updateCVButtons() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
        changeCVButton.setEnabled(cv != null);
        deleteCVButton.setEnabled(cv != null);
        cvNameTextField.setText((cv != null) ? cv.getName() : "");
        cvDescArea.setText((cv != null) ? cv.getDescription() : "");
        oldCVName = (cv != null) ? cv.getName() : null;
        oldCVDesc = (cv != null) ? cv.getDescription() : null;
    }

    /**
     * Extracts the CV's from the transcription and fills the cv combobox.
     */
    protected void updateComboBox() {
        cvComboBox.removeItemListener(this);

        // extract
        List v = getCVList();
        cvComboBox.removeAllItems();

        for (int i = 0; i < v.size(); i++) {
            cvComboBox.addItem(v.get(i));
        }

        if (v.size() > 0) {
            cvComboBox.setSelectedIndex(0);
            cvEditorPanel.setControlledVocabulary((ControlledVocabulary) cvComboBox.getItemAt(
                    0));
        }

        updateCVButtons();

        cvComboBox.addItemListener(this);
    }

    /**
     * Since this dialog is meant to be modal a Locale change while this dialog
     * is open  is not supposed to happen. This will set the labels etc. using
     * the current locale  strings.
     */
    protected void updateLabels() {
        closeDialogButton.setText("Close");
        deleteCVButton.setText("Delete");
        changeCVButton.setText("Change");
        addCVButton.setText("Add");
        cvNameLabel.setText("Name");
        cvDescLabel.setText("Description");
        currentCVLabel.setText("Current");
    }

    /**
     * An action to put in the dialog's action map and that is being performed
     * when the enter key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EnterAction extends AbstractAction {
        /**
         * The action that is performed when the enter key has been hit.
         *
         * @param ae the action event
         */
        public void actionPerformed(ActionEvent ae) {
            Component com = AbstractEditCVDialog.this.getFocusOwner();

            if (com instanceof JButton) {
                ((JButton) com).doClick();
            }
        }
    }

    ////////////
    // action classes for handling escape and enter key.
    ////////////

    /**
     * An action to put in the dialog's action map and that is being performed
     * when the escape key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EscapeAction extends AbstractAction {
        /**
         * The action that is performed when the escape key has been hit.
         *
         * @param ae the action event
         */
        public void actionPerformed(ActionEvent ae) {
            AbstractEditCVDialog.this.closeDialog();
        }
    }
}
