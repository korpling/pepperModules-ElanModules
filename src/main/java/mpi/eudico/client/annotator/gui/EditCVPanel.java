package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;


public class EditCVPanel extends JPanel implements ActionListener,
    ListSelectionListener {
    /** Empty string to fill UI elements when values/description are empty. */
    protected static final String EMPTY = "";
    private static final int MOVE_BUTTON_SIZE = 24;
    private static final int MINIMAL_ENTRY_PANEL_WIDTH = 240;
    protected BasicControlledVocabulary cv;
    
    private boolean ascending = false;
    private boolean descending = true;

    // internal caching fields
    protected CVEntry currentEntry;
    protected JButton addEntryButton;
    protected JButton changeEntryButton;
    protected JButton deleteEntryButton;
    protected JButton moveDownButton;
    protected JButton moveToBottomButton;
    protected JButton moveToTopButton;
    protected JButton moveUpButton;
    protected JButton redoButton;
    protected JButton undoButton;
    protected JButton ascendingButton;
    protected JButton descendingButton;
    protected JLabel entryDescLabel;
    protected JLabel entryValueLabel;
    protected JLabel titleLabel;
    protected JList entryList;
    protected JTextField entryDescTextField;
    protected JTextField entryValueTextField;
    protected String invalidValueMessage = "Invalid value";
    protected String valueExistsMessage = "Value exists";
    private DefaultListModel entryListModel;
    // dcr 
    protected JPanel dcrPanel;
    protected JLabel dcrLabel;
    protected JTextField dcrField;
    protected JTextField dcIdField;
    protected JButton dcrButton;
    // more options
    boolean enableMoreOptions = false;
    protected JButton moreOptionsButton;
    
    // ui elements
    private UndoManager undoManager;

    /**
     * opens panel with no cv
     *
     */
    public EditCVPanel() {
        this(null);
    }

    /**
     * opens panel with no cv and add more options button
     * 
     * @param enableMoreOptions if true, enables more options for entries
     */
    public EditCVPanel(boolean enableMoreOptions) {
        this(null, enableMoreOptions);
    }
    
    /**
     * opens panel with cv
     * @param cv Controlled Vocabulary
     */
    public EditCVPanel(BasicControlledVocabulary cv) {
        this(cv, false);
    }

    /**
     * opens panel with cv and adds more options (for preferences)
     * 
     * @param cv Controlled Vocabulary
     * @param enableMoreOptions if true, enables more options for entries
     */
    public EditCVPanel(BasicControlledVocabulary cv, boolean enableMoreOptions) {
        undoManager = new UndoManager() {
                    public void undoableEditHappened(UndoableEditEvent e) {
                        super.undoableEditHappened(e);
                        updateUndoRedoButtons();
                    }
                };
        this.enableMoreOptions = enableMoreOptions;
        makeLayout();
        entryList.addListSelectionListener(this);
        setControlledVocabulary(cv);       
    }    
   
  
    
    /**
     * sets (new) cv
     * @param cv Controlled Vocabulary
     */
    public void setControlledVocabulary(BasicControlledVocabulary cv) {
        this.cv = cv;
        undoManager.discardAllEdits();
        updateLabels();
        resetViewer();
        entryValueTextField.setEnabled(cv != null);
        entryDescTextField.setEnabled(cv != null);

        if (cv instanceof ControlledVocabulary) {
            ((ControlledVocabulary) cv).addUndoableEditListener(undoManager);
            undoButton.setVisible(true);
            redoButton.setVisible(true);
            dcrButton.setEnabled(true);
            
            if (cv instanceof ExternalCV) {
				addEntryButton.setEnabled(false);
				changeEntryButton.setEnabled(false);
				deleteEntryButton.setEnabled(false);
				moveDownButton.setEnabled(false);
				moveToBottomButton.setEnabled(false);
				moveToTopButton.setEnabled(false);
				moveUpButton.setEnabled(false);
				redoButton.setEnabled(false);
				undoButton.setEnabled(false);
				entryDescTextField.setEnabled(false);
				entryValueTextField.setEnabled(false);
				dcrButton.setEnabled(false);
				moreOptionsButton.setEnabled(true);
				ascendingButton.setEnabled(false);
				descendingButton.setEnabled(false);
			}
        } else {
            undoButton.setVisible(false);
            redoButton.setVisible(false);
        }
    }

    /**
     * The button actions.
     *
     * @param actionEvent the actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check source equality
        if (source == entryValueTextField) {
            entryDescTextField.requestFocus();
        } else if ((source == addEntryButton) ||
                (source == entryDescTextField)) {
            addEntry();
        } else if (source == changeEntryButton) {
            changeEntry();
        } else if (source == deleteEntryButton) {
            deleteEntries();
        } else if (source == moveToTopButton) {
            moveEntries(BasicControlledVocabulary.MOVE_TO_TOP);
        } else if (source == moveUpButton) {
            moveEntries(BasicControlledVocabulary.MOVE_UP);
        } else if (source == moveDownButton) {
            moveEntries(BasicControlledVocabulary.MOVE_DOWN);
        } else if (source == moveToBottomButton) {
            moveEntries(BasicControlledVocabulary.MOVE_TO_BOTTOM);
        } else if (source == undoButton) {
            undo();
        } else if (source == redoButton) {
            redo();
        } else if (source == ascendingButton) {
        	ascending = true;        	
        	sortEntries();
        } else if (source == descendingButton) {
        	descending = true;
        	sortEntries();
        }
    }

    /**
     * for test purposes. opens frame with this panel and a test controlled vocabulary
     * @param args no arguments
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        ControlledVocabulary cv = new ControlledVocabulary("Name 1",
                "Description 1");
        cv.addEntry(new CVEntry("Entry 1", "Entry description 1"));
        cv.addEntry(new CVEntry("Entry 2", "Entry description 2"));
        cv.addEntry(new CVEntry("Entry 3", "Entry description 3"));

        JPanel p = new EditCVPanel(cv);
        frame.getContentPane().add(p);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Handles a change in the selection in the entry list.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getSource() == entryList) {
            updateEntryButtons();            
            updateTextFields();
            updateSortButtons();
        }        
        
    }

    /**
     * Adds an entry to the current CV. When checking the uniqueness of the
     * entry  value, values are compared case sensitive.
     */
    protected void addEntry() {
        if (cv == null) {
            return;
        }

        String entry = entryValueTextField.getText();

        entry = entry.trim();

        if (entry.length() == 0) {
            showWarningDialog(invalidValueMessage);

            return;
        }

        if (cv.containsValue(entry)) {
            showWarningDialog(valueExistsMessage);
        } else {
            String desc = entryDescTextField.getText();

            if (desc != null) {
                desc = desc.trim();
            }

            CVEntry newEntry = new CVEntry(entry, desc);
            cv.addEntry(newEntry);
            updateList();

            //make text fields free for next input!
            setSelectedEntry(null);
        }
    }

    /**
     * Changes the value and/or description of an existing entry. Checks
     * whether  the specified value is unique within the current
     * ControlledVocabulary.
     */
    protected void changeEntry() {
        if (cv == null) {
            return;
        }

        String newValue = entryValueTextField.getText().trim();

        if (newValue.length() == 0) {
            showWarningDialog(invalidValueMessage);
            entryValueTextField.setText((currentEntry != null)
                ? currentEntry.getValue() : "");

            return;
        }

        String newDescription = entryDescTextField.getText().trim();

        if (newValue.equals(currentEntry.getValue())) {
            if ((newDescription != null) &&
                    !newDescription.equals(currentEntry.getDescription())) {
                CVEntry newEntry = new CVEntry(newValue, newDescription);
                cv.replaceEntry(currentEntry, newEntry);
                updateList();
                setSelectedEntry(newEntry);
            }

            return;
        }

        // entry value has changed...
        if (cv.containsValue(newValue)) {
            showWarningDialog(valueExistsMessage);
        } else {
            CVEntry newEntry = new CVEntry(newValue, newDescription);
            cv.replaceEntry(currentEntry, new CVEntry(newValue, newDescription));
            updateList();
            setSelectedEntry(newEntry);
        }
    }

    /**
     * Deletes the selected entry/entries from the current
     * ControlledVocabulary.
     */
    protected void deleteEntries() {
        Object[] selEntries = entryList.getSelectedValues();

        if (selEntries.length == 0) {
            return;
        }

        CVEntry[] entries = new CVEntry[selEntries.length];

        for (int i = 0; i < entries.length; i++) {
            entries[i] = (CVEntry) selEntries[i];
        }

        cv.removeEntries(entries);
        updateList();
        setSelectedEntry(null);
    }

    /**
    * This method is called from within the constructor to initialize the
    * dialog's components.
    */
    protected void makeLayout() {
        JPanel moveEntriesPanel;
        JPanel sortEntriesPanel;

        GridBagConstraints gridBagConstraints;

        ImageIcon topIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Top16.gif"));
        ImageIcon bottomIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Bottom16.gif"));
        ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
        ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
        ImageIcon redoIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Redo16.gif"));
        ImageIcon undoIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Undo16.gif"));

        entryListModel = new DefaultListModel();
        entryList = new JList(entryListModel);
        entryValueLabel = new JLabel();
        entryValueTextField = new JTextField();
        addEntryButton = new JButton();
        addEntryButton.setEnabled(false);
        changeEntryButton = new JButton();
        changeEntryButton.setEnabled(false);
        deleteEntryButton = new JButton();
        deleteEntryButton.setEnabled(false);

        titleLabel = new JLabel();
        entryDescLabel = new JLabel();
        entryDescTextField = new JTextField();
        
        dcrPanel = new JPanel(new GridBagLayout());
        dcrLabel = new JLabel();
        dcrField = new JTextField();
        dcIdField = new JTextField();
        dcIdField.setEditable(false);
        dcrField.setEditable(false);
        //dcrField.setEnabled(false);
        dcrButton = new JButton();
		
        moveEntriesPanel = new JPanel();
        moveUpButton = new JButton(upIcon);
        moveUpButton.setEnabled(false);
        moveToTopButton = new JButton(topIcon);
        moveToTopButton.setEnabled(false);
        moveDownButton = new JButton(downIcon);
        moveDownButton.setEnabled(false);
        moveToBottomButton = new JButton(bottomIcon);
        moveToBottomButton.setEnabled(false);
        undoButton = new JButton(undoIcon);
        undoButton.setEnabled(false);
        redoButton = new JButton(redoIcon);
        redoButton.setEnabled(false);
        

        Dimension prefDim = new Dimension(MINIMAL_ENTRY_PANEL_WIDTH, MOVE_BUTTON_SIZE);
        Dimension buttonDimension = new Dimension(MOVE_BUTTON_SIZE, MOVE_BUTTON_SIZE);

        Insets insets = new Insets(2, 6, 2, 6);

        // entry sorting buttons
        moveEntriesPanel.setLayout(new GridBagLayout());

        moveToTopButton.addActionListener(this);
        moveToTopButton.setPreferredSize(buttonDimension);
        moveToTopButton.setMaximumSize(buttonDimension);
        moveToTopButton.setMinimumSize(buttonDimension);
        moveUpButton.addActionListener(this);
        moveUpButton.setPreferredSize(buttonDimension);
        moveUpButton.setMaximumSize(buttonDimension);
        moveUpButton.setMinimumSize(buttonDimension);
        moveDownButton.addActionListener(this);
        moveDownButton.setPreferredSize(buttonDimension);
        moveDownButton.setMaximumSize(buttonDimension);
        moveDownButton.setMinimumSize(buttonDimension);
        moveToBottomButton.addActionListener(this);
        moveToBottomButton.setPreferredSize(buttonDimension);
        moveToBottomButton.setMaximumSize(buttonDimension);
        moveToBottomButton.setMinimumSize(buttonDimension);
        undoButton.addActionListener(this);
        undoButton.setPreferredSize(buttonDimension);
        undoButton.setMaximumSize(buttonDimension);
        undoButton.setMinimumSize(buttonDimension);
        redoButton.addActionListener(this);
        redoButton.setPreferredSize(buttonDimension);
        redoButton.setMaximumSize(buttonDimension);
        redoButton.setMinimumSize(buttonDimension);        
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        moveEntriesPanel.add(moveToTopButton, gridBagConstraints);
        moveEntriesPanel.add(moveUpButton, gridBagConstraints);
        moveEntriesPanel.add(moveDownButton, gridBagConstraints);
        moveEntriesPanel.add(moveToBottomButton, gridBagConstraints);
        moveEntriesPanel.add(undoButton, gridBagConstraints);
        moveEntriesPanel.add(redoButton, gridBagConstraints);
        
        //sort entries button 
        
        sortEntriesPanel = new JPanel();
        sortEntriesPanel.setLayout(new GridBagLayout());
        
        ascendingButton = new JButton("Sort A-Z");
        ascendingButton.setEnabled(false);
        ascendingButton.addActionListener(this);
        descendingButton = new JButton("Sort Z-A");
        descendingButton.setEnabled(false);
        descendingButton.addActionListener(this);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        sortEntriesPanel.add(ascendingButton, gridBagConstraints);
        sortEntriesPanel.add(descendingButton, gridBagConstraints);        

        // dcr
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        dcrPanel.add(dcrLabel, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 0, 6);
        dcrPanel.add(dcrField, gridBagConstraints);
        // add the id field?
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.5;
        dcrPanel.add(dcIdField, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        dcrPanel.add(dcrButton, gridBagConstraints);
        
        //other subcomponents        
        JScrollPane entryPane = new JScrollPane(entryList);
        entryPane.setPreferredSize(prefDim);
        entryPane.setMinimumSize(prefDim);

        entryValueTextField.setPreferredSize(prefDim);
        entryValueTextField.setMinimumSize(prefDim);

        entryDescTextField.setPreferredSize(prefDim);
        entryDescTextField.setMinimumSize(prefDim);

        //main layout
        setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        add(entryPane, gridBagConstraints);
        
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = insets;
        add(sortEntriesPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = insets;
        add(entryValueLabel, gridBagConstraints);

        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        add(entryValueTextField, gridBagConstraints);
        add(entryDescLabel, gridBagConstraints);
        add(entryDescTextField, gridBagConstraints);
        add(dcrPanel, gridBagConstraints);
        add(addEntryButton, gridBagConstraints);
        add(changeEntryButton, gridBagConstraints);
        add(deleteEntryButton, gridBagConstraints);
        if (enableMoreOptions) {
        	moreOptionsButton = new JButton();
        	add(moreOptionsButton, gridBagConstraints);
        }
        add(moveEntriesPanel, gridBagConstraints);
        
        //add(sortEntriesPanel, gridBagConstraints);
        
        

        undoButton.setToolTipText(undoManager.getUndoPresentationName());

        entryValueTextField.addActionListener(this);
        entryDescTextField.addActionListener(this);
        addEntryButton.addActionListener(this);
        changeEntryButton.addActionListener(this);
        deleteEntryButton.addActionListener(this);
        dcrButton.addActionListener(this);
        dcrPanel.setVisible(false);//default
        if (enableMoreOptions) {
        	moreOptionsButton.addActionListener(this);
        }
    }

    /**
     * Updates some UI fields after a change in the selected CV.
     *
     */
    protected void resetViewer() {
        // reset some fields
        entryListModel.clear();

        if (cv != null) {
            CVEntry[] entries = cv.getEntries();
            currentEntry = null;

            for (int i = 0; i < entries.length; i++) {
                entryListModel.addElement(entries[i]);

                if (i == 0) {
                    entryList.setSelectedIndex(0);
                    currentEntry = entries[0];
                }
            }	
            addEntryButton.setEnabled(true);
        } else {
            cv = null;
            addEntryButton.setEnabled(false);
        }
       
        updateEntryButtons();
        updateTextFields();
        updateSortButtons();
    }

    /**
     * Since this dialog is meant to be modal a Locale change while this dialog
     * is open  is not supposed to happen. This will set the labels etc. using
     * the current locale  strings.
     */
    protected void updateLabels() {
        moveToTopButton.setToolTipText("Top");
        moveUpButton.setToolTipText("Up");
        moveDownButton.setToolTipText("Down");
        moveToBottomButton.setToolTipText("Bottom");
        deleteEntryButton.setText("Delete");
        changeEntryButton.setText("Change");
        addEntryButton.setText("Add");
        entryDescLabel.setText("Description");
        entryValueLabel.setText("Value");
        dcrLabel.setText("ISO Data Category");
        dcrButton.setText("Browse...");
        setBorder(new TitledBorder("Entries"));
        undoButton.setToolTipText(undoManager.getUndoPresentationName());
        redoButton.setToolTipText(undoManager.getRedoPresentationName());
        ascendingButton.setToolTipText("Ascending order");
        descendingButton.setToolTipText("Descending order");
        if (enableMoreOptions) {
        	moreOptionsButton.setText("More Options...");
        }
    }

    /**
     * Reextracts the entries from the current CV after an add, change or
     * delete entry operation on the CV.
     *
     */
    protected void updateList() {
        if (cv != null) {
            entryList.removeListSelectionListener(this);
            entryListModel.clear();

            CVEntry[] entries = cv.getEntries();

            for (int i = 0; i < entries.length; i++) {
                entryListModel.addElement(entries[i]);
            }

            entryList.addListSelectionListener(this);
        }
    }
    
    /**
     * Re-extracts the entries from the current CV after an add, change or
     * delete entry operation on the CV.
     *
     */
    protected void sortEntries() {
    	if (cv != null) {            
            entryListModel.clear();  

            CVEntry[] entries = null;
            
            if(ascending){
            	entries = cv.getEntriesSortedByAlphabet();
            	ascending = false;            	
            } else if (descending){
            	entries = cv.getEntriesSortedByReverseAlphabetOrder();
            	descending = false;            	
            }
            if(entries != null){
            	for (int i = 0; i < entries.length; i++) {
            		entryListModel.addElement(entries[i]);            		
                
            		if (i == 0) {
            			entryList.setSelectedIndex(0);
            			currentEntry = entries[0];
            		}
            	}
            } 
        }     	
    	
    }

    protected void setSelectedEntries(CVEntry[] entries) {
        currentEntry = null;

        if ((entries != null) && (entries.length > 0)) {
            entryList.removeListSelectionListener(this);

            for (int i = 0; i < entryListModel.getSize(); i++) {
                for (int j = 0; j < entries.length; j++) {
                    if (entryListModel.getElementAt(i).equals(entries[j])) {
                        entryList.addSelectionInterval(i, i);
                    }
                }
            }

            entryList.addListSelectionListener(this);
        }

        updateEntryButtons();
        updateTextFields();
    }

    protected void setSelectedEntry(CVEntry entry) {
        if (entry != null) {
            setSelectedEntries(new CVEntry[] { entry });
        } else {
            setSelectedEntries(null);
        }
    }

    /**
     * Moves the selected entries to the bottom of the entry list.
     * @param the type of the move (up, down, etc.) as defined in BasicControlledVocabulary
     */
    private void moveEntries(int moveType) {
        if (cv == null) {
            return;
        }

        Object[] selEntries = entryList.getSelectedValues();

        if (selEntries.length == 0) {
            return;
        }

        CVEntry[] entriesToBeMoved = new CVEntry[selEntries.length];

        for (int i = 0; i < selEntries.length; i++) {
            entriesToBeMoved[i] = (CVEntry) selEntries[i];
        }

        cv.moveEntries(entriesToBeMoved, moveType);
        updateList();
        setSelectedEntries(entriesToBeMoved);
    }

    /**
     * Invokes the redo method of the <code>UndoManager</code>.
     */
    private void redo() {
        try {
            undoManager.redo();

            updateList();
            setSelectedEntry(null);
        } catch (CannotRedoException cre) {
            //LOG.warning(LogUtil.formatStackTrace(cre));
        }

        updateUndoRedoButtons();
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

    /**
     * Invokes the undo method of the <code>UndoManager</code>.
     */
    private void undo() {
        try {
            undoManager.undo();

            updateList();
            setSelectedEntry(null);
        } catch (CannotUndoException cue) {
            // LOG.warning(LogUtil.formatStackTrace(cue));
        }

        updateUndoRedoButtons();
    }

    /**
    * Enables or disables buttons depending on the selected entries.
    */
    private void updateEntryButtons() {
        if ((entryList == null) || (entryList.getSelectedIndex() == -1)) {
            changeEntryButton.setEnabled(false);
            deleteEntryButton.setEnabled(false);
            moveToTopButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            moveToBottomButton.setEnabled(false);
            currentEntry = null;
            if (moreOptionsButton != null) {
            	moreOptionsButton.setEnabled(false);
            }
        } else if (cv instanceof ExternalCV) {
			changeEntryButton.setEnabled(false);
			deleteEntryButton.setEnabled(false);
			moveToTopButton.setEnabled(false);
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
			moveToBottomButton.setEnabled(false);
			if (moreOptionsButton != null) {
				if ((entryList == null) || (entryList.getSelectedIndex() == -1)) {
					moreOptionsButton.setEnabled(false);
				} else {
					moreOptionsButton.setEnabled(true);
				}
			}
			currentEntry = (CVEntry) entryList.getSelectedValue();
		} else {
            int firstIndex = entryList.getSelectedIndices()[0];
            int numSelected = entryList.getSelectedIndices().length;
            int lastIndex = entryList.getSelectedIndices()[numSelected - 1];
            changeEntryButton.setEnabled(true);
            deleteEntryButton.setEnabled(true);
            if (moreOptionsButton != null) {
            	moreOptionsButton.setEnabled(true);
            }

            if (firstIndex > 0) {
                moveToTopButton.setEnabled(true);
                moveUpButton.setEnabled(true);
            } else {
                moveToTopButton.setEnabled(false);
                moveUpButton.setEnabled(false);
            }

            if (lastIndex < (entryList.getModel().getSize() - 1)) {
                moveDownButton.setEnabled(true);
                moveToBottomButton.setEnabled(true);
            } else {
                moveDownButton.setEnabled(false);
                moveToBottomButton.setEnabled(false);
            }

            currentEntry = (CVEntry) entryList.getSelectedValue();
        }
    } 
    
    /**
     * Enables or disables buttons depending on the selected entries.
     */
     private void updateSortButtons() {
         if (entryList != null && entryList.getModel().getSize() > 1) {
             ascendingButton.setEnabled(true);
             descendingButton.setEnabled(true);             
         }
     } 

    protected void updateTextFields() {
        if (entryList.getSelectedIndex() == -1) {
            entryValueTextField.setText(EMPTY);
            entryDescTextField.setText(EMPTY);
        } else {
            //put the first selected entry into text fields
            CVEntry selEntry = (CVEntry) entryList.getSelectedValue();
            entryValueTextField.setText(selEntry.getValue());
            entryDescTextField.setText(selEntry.getDescription());
        }

        if (entryValueTextField.isEnabled()) {
            entryValueTextField.requestFocus();
        }
    }

    /**
     * Enables or disables the undo/redo buttons.
     */
    private void updateUndoRedoButtons() {
        undoButton.setEnabled(undoManager.canUndo());
        redoButton.setEnabled(undoManager.canRedo());
    }
}
