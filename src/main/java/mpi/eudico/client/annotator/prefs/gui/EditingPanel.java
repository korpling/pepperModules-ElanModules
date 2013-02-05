package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.Constants;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Panel showing options for:<br>
 * - flag to set that deselecting the edit box commits changes (default is cancel)<br>
 * - flag to set that Enter commits the changes (default is adding a new line
 * char)<br>
 * - flag to set that the selection should be cleared after creation of a new annotation,
 * or modifying the time alignement etc.
 * 
 * @author Han Sloetjes
 */
public class EditingPanel extends JPanel implements PreferenceEditor, ChangeListener {
    /** deselect commits preference, default is false */
    private JCheckBox deselectCB;

    /** enter commits (in addition to Ctrl + Enter), default false */
    private JCheckBox enterCommitsCB;
    /** clear the selection after creation or modification of a new annotation */
    private JCheckBox clearSelectionCB;
    private JCheckBox clearSelectionOnSingleClickCB;
    
    private JCheckBox createDependAnnCB;
    private JCheckBox snapAnnCB;
    private JLabel snapAnnLabel;
    private JCheckBox stickToFramesCB;
    private JTextField snapAnnTextField;
    private JCheckBox editInCenterCB;
    private JLabel copyOptionLabel;
    private JComboBox copyOptionComboBox;
    
    private JCheckBox suggestEntryContainsCB;
    private JCheckBox suggestSearchDescCB;
    private JCheckBox suggestIgnoreCaseCB;
    
    private boolean origDeselectFlag = false;
    private boolean origEnterFlag = false;
    private boolean origClearSelFlag = false;
    private boolean origClearSelOnSingleClickFlag = true; 
    private boolean oriCreateDependAnnFlag = false;
    
    private boolean oriSnapAnnotationsFlag = false;
    private boolean oriStickToFramesFlag = false;
    private long oriSnapValue = 100L;
    private long newSnapValue; // initialized on reading preferences
    private boolean oriAnnInCenterFlag = true;
     
    private static String TEXTANDTIME = ElanLocale.getString("PreferencesDialog.Edit.CopyAll");  
    private static String TEXT = ElanLocale.getString("PreferencesDialog.Edit.CopyTextOnly");
    
    /* to be implemented later on
     * 
     * private static String URL = ElanLocale.getString("PreferencesDialog.Edit.CopyHyperlink");*/
    

    private Map<String, String> tcMap;
    // tcMod is to be a map with locale strings as key and English strings from Constants as values
    
    private int origCopyOptionIndex;
    private String oriCopyOption = Constants.TEXTANDTIME_STRING;
    
    private boolean oriSuggestSearchMethodFlag = false;
    private boolean oriSuggestSearchInDescFlag = false;
    private boolean oriSuggestIgnoreCaseFlag = false;
   
    /**
     * Creates a new EditingPanel instance
     */
    public EditingPanel() {
	super();
        tcMap = new HashMap<String, String>(2);
        tcMap.put(TEXTANDTIME, Constants.TEXTANDTIME_STRING);
        tcMap.put(TEXT, Constants.TEXT_STRING);
        /* to be implemented later on
         *
         * tcMap.put(URL, Constants.URL_STRING); to be implemented later on
         */
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Object val = Preferences.get("InlineEdit.DeselectCommits", null);

        if (val instanceof Boolean) {
            origDeselectFlag = ((Boolean) val).booleanValue();
        }

        val = Preferences.get("InlineEdit.EnterCommits", null);

        if (val instanceof Boolean) {
            origEnterFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("ClearSelectionAfterCreation", null);
        
        if (val instanceof Boolean) {
        	origClearSelFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("ClearSelectionOnSingleClick", null); 
        
        if (val instanceof Boolean) {
        	origClearSelOnSingleClickFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("CreateDependingAnnotations", null);
        
        if (val instanceof Boolean) {
        	oriCreateDependAnnFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("SnapAnnotations", null);
        
        if (val instanceof Boolean) {
        	oriSnapAnnotationsFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("SnapAnnotationsValue", null);
        
        if (val instanceof Long) {
        	oriSnapValue = ((Long) val).longValue();
        }
        newSnapValue = oriSnapValue; 
        
        val = Preferences.get("StickAnnotationsWithVideoFrames", null);
        
        if (val instanceof Boolean) {
        	oriStickToFramesFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("SuggestPanel.EntryContains", null);
        
        if (val instanceof Boolean) {
        	oriSuggestSearchMethodFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("SuggestPanel.SearchDescription", null);
        
        if (val instanceof Boolean) {
        	oriSuggestSearchInDescFlag = ((Boolean) val).booleanValue();
        }

        val = Preferences.get("SuggestPanel.IgnoreCase", null);
        
        if (val instanceof Boolean) {
        	oriSuggestIgnoreCaseFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("EditingPanel.ActiveAnnotationInCenter", null);
        
        if (val instanceof Boolean) {
        	oriAnnInCenterFlag = ((Boolean) val).booleanValue();
        }
        
        val = Preferences.get("EditingPanel.CopyOption", null);
        
        if (val instanceof String) {
    	// take into account possible older localized stored preferences
    	String storedPref = (String) val;
    	if (tcMap.containsKey(storedPref)) {
    		oriCopyOption = tcMap.get(storedPref);
	    } else {
		if (tcMap.values().contains(storedPref)) {
		    oriCopyOption = storedPref;
		}
		// if not, it might be string from yet another language
	    }
	    // oriCopyOption should now be a non localized string
	}

    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        
        Insets insets = new Insets(2, 0, 2, 0);
        deselectCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Edit.Deselect"), origDeselectFlag);
        enterCommitsCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Edit.EnterCommits"), origEnterFlag);
        clearSelectionCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Edit.ClearSelection"), origClearSelFlag);
        clearSelectionOnSingleClickCB = new JCheckBox(ElanLocale.getString(
		"PreferencesDialog.Edit.ClearSelectionOnSingleClick"), origClearSelOnSingleClickFlag);
        createDependAnnCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.CreateDependAnn"),oriCreateDependAnnFlag);
        snapAnnCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SnapAnnotations"),oriSnapAnnotationsFlag);
        snapAnnCB.addChangeListener(this);
        snapAnnLabel = new JLabel(ElanLocale.getString(
				"PreferencesDialog.Edit.SnapAnnotations.Label"));
        snapAnnTextField = new JTextField(Long.toString(oriSnapValue));  
        snapAnnTextField.setEnabled(oriSnapAnnotationsFlag);
        stickToFramesCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.StickToVideoFrames"),oriStickToFramesFlag);
        editInCenterCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.ActiveAnnotationInCenter"), oriAnnInCenterFlag);
        
        deselectCB.setFont(deselectCB.getFont().deriveFont(Font.PLAIN));
        enterCommitsCB.setFont(deselectCB.getFont());
        clearSelectionCB.setFont(deselectCB.getFont());
        clearSelectionOnSingleClickCB.setFont(deselectCB.getFont());
        createDependAnnCB.setFont(deselectCB.getFont());
        snapAnnCB.setFont(deselectCB.getFont());
        snapAnnLabel.setFont(deselectCB.getFont());
        snapAnnTextField.setFont(deselectCB.getFont());
        stickToFramesCB.setFont(deselectCB.getFont());
        editInCenterCB.setFont(deselectCB.getFont());
                
        suggestEntryContainsCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Edit.SuggestEntryContains"), oriSuggestSearchMethodFlag);
	suggestSearchDescCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SuggestSearchDesc"), oriSuggestSearchInDescFlag);
	suggestIgnoreCaseCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SuggestIgnoreCase"), oriSuggestIgnoreCaseFlag);
	suggestEntryContainsCB.setFont(deselectCB.getFont());
	suggestSearchDescCB.setFont(deselectCB.getFont());
	suggestIgnoreCaseCB.setFont(deselectCB.getFont());
	
        copyOptionLabel = new JLabel(ElanLocale.getString(
				"PreferencesDialog.Edit.CopyOptionLabel"));
        copyOptionLabel.setFont(deselectCB.getFont());
	
        copyOptionComboBox = new JComboBox();
        copyOptionComboBox.addItem(TEXTANDTIME);        
        copyOptionComboBox.addItem(TEXT);

        /* to be implemented later on
         * 
         * copyOptionComboBox.addItem(URL); */   
                
        boolean prefRestored = false;
        Iterator<String> tcIt = tcMap.keySet().iterator();
        String key;
        String tcConst = null;
        while (tcIt.hasNext()) {
        	key = tcIt.next();
        	tcConst = tcMap.get(key);
        	if (tcConst.equals(oriCopyOption)) {
        		copyOptionComboBox.setSelectedItem(key);
        		prefRestored = true;
        		break;
        	}
        }
        if (!prefRestored) {
        	copyOptionComboBox.setSelectedItem(TEXTANDTIME); // default
        }
        origCopyOptionIndex = copyOptionComboBox.getSelectedIndex();
        
        copyOptionComboBox.setSelectedItem(oriCopyOption);
        copyOptionComboBox.setFont(deselectCB.getFont());
        
	GridBagConstraints gbc = new GridBagConstraints();
	
	// create panel for max snap value entry
		
	JPanel snapPanel = new JPanel(new GridBagLayout());	
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.gridx = 0;
	gbc.fill = GridBagConstraints.NONE; 
	gbc.insets = new Insets(2,35,2,0); 
    	snapPanel.add(snapAnnLabel, gbc);
    	
    	gbc.gridx =1;
    	gbc.fill = GridBagConstraints.HORIZONTAL; 
    	gbc.insets = new Insets(2,6,2,0); 
    	gbc.weightx = 1.0;
    	snapPanel.add(snapAnnTextField, gbc); 
    	
    	// create separate panel for copy options
		
	JPanel copyOptionPanel = new JPanel(new GridBagLayout());	
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.fill = GridBagConstraints.NONE; 
	gbc.gridx = 0;
	gbc.insets = new Insets(5,0,2,0); 
    	copyOptionPanel.add(copyOptionLabel, gbc);
    	
    	gbc.gridx = 0; 
    	gbc.gridy = 1; 
    	gbc.insets = new Insets(2,2,2,0); 
    	copyOptionPanel.add(copyOptionComboBox, gbc);
    	
    	// integrate both in panel for editing options
		
	JPanel editingPanel = new JPanel(new GridBagLayout());		
	gbc = new GridBagConstraints();
	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.HORIZONTAL; 
    	gbc.insets = insets;
    	editingPanel.add(deselectCB, gbc);

        gbc.gridy = 2;
        editingPanel.add(enterCommitsCB, gbc);
        
        gbc.gridy = 3;
        editingPanel.add(clearSelectionCB, gbc);
        
        gbc.gridy = 4;       
        editingPanel.add(clearSelectionOnSingleClickCB, gbc);
       
        gbc.gridy = 5;       
        editingPanel.add(createDependAnnCB, gbc);
        
        gbc.gridy = 6;
        editingPanel.add(stickToFramesCB, gbc);
        
        gbc.gridy = 7;        
        editingPanel.add(snapAnnCB, gbc);       
        
        gbc.gridy = 8;
        editingPanel.add(snapPanel, gbc);   
       
        gbc.gridy = 9; 
        gbc.insets = insets;   
        gbc.fill = GridBagConstraints.NONE;         
        editingPanel.add(editInCenterCB, gbc);    

        gbc.gridy = 10; 
    	gbc.insets = new Insets(15,2,5,0); 
    	editingPanel.add(copyOptionPanel, gbc);

        gbc.gridy = 11;       
        editingPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Edit.SuggestPanel")),
                gbc);

        gbc.insets = insets;
        gbc.gridy = 12;
        editingPanel.add(suggestEntryContainsCB, gbc);
        
        gbc.gridy = 13;
        editingPanel.add(suggestSearchDescCB, gbc);
        
        gbc.gridy = 14;
        editingPanel.add(suggestIgnoreCaseCB, gbc);
        
        gbc.gridy = 15; 
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = gbc.BOTH;
        editingPanel.add(new JPanel(), gbc); 
        
        JScrollPane scrollPane = new JScrollPane(editingPanel);
        scrollPane.setBorder(new TitledBorder(ElanLocale.getString("PreferencesDialog.Category.Edit")));   
        scrollPane.setBackground(editingPanel.getBackground());

        gbc = new GridBagConstraints();
        gbc.insets = insets;       
        gbc.weighty = 1.0;   
        gbc.weightx = 1.0;  
        gbc.fill = gbc.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(scrollPane , gbc);

    }
    
    /**
     * Returns the changed prefs.
     *
     * @return a map containing the changed preferences, key-value pairs, or null
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map chMap = new HashMap(3);

            if (deselectCB.isSelected() != origDeselectFlag) {
                chMap.put("InlineEdit.DeselectCommits",
                    new Boolean(deselectCB.isSelected()));
            }

            if (enterCommitsCB.isSelected() != origEnterFlag) {
                chMap.put("InlineEdit.EnterCommits",
                    new Boolean(enterCommitsCB.isSelected()));
            }

            if (clearSelectionCB.isSelected() != origClearSelFlag) {
            	chMap.put("ClearSelectionAfterCreation", 
            		new Boolean(clearSelectionCB.isSelected()));
            }
            
            if (clearSelectionOnSingleClickCB.isSelected() != origClearSelOnSingleClickFlag) {
            	chMap.put("ClearSelectionOnSingleClick", 
            		new Boolean(clearSelectionOnSingleClickCB.isSelected()));
            }
            
            if (createDependAnnCB.isSelected() != oriCreateDependAnnFlag) {
            	chMap.put("CreateDependingAnnotations", 
            		new Boolean(createDependAnnCB.isSelected()));
            }
            
            if (snapAnnCB.isSelected() != oriSnapAnnotationsFlag) {
            	chMap.put("SnapAnnotations", 
            		new Boolean(snapAnnCB.isSelected()));
            }
            
            if (snapAnnCB.isSelected()){ 
            	chMap.put("SnapAnnotationsValue", new Long(newSnapValue));
            }
            
            if (stickToFramesCB.isSelected() != oriStickToFramesFlag) {
            	chMap.put("StickAnnotationsWithVideoFrames", 
            		new Boolean(stickToFramesCB.isSelected()));
            }
            if (suggestEntryContainsCB.isSelected() != oriSuggestSearchMethodFlag) {
            	chMap.put("SuggestPanel.EntryContains", 
            		new Boolean(suggestEntryContainsCB.isSelected()));
            }
            if (suggestSearchDescCB.isSelected() != oriSuggestSearchInDescFlag) {
            	chMap.put("SuggestPanel.SearchDescription", 
            		new Boolean(suggestSearchDescCB.isSelected()));
            }
            if (suggestIgnoreCaseCB.isSelected() != oriSuggestIgnoreCaseFlag) {
            	chMap.put("SuggestPanel.IgnoreCase", 
            		new Boolean(suggestIgnoreCaseCB.isSelected()));
            } 
            if (editInCenterCB.isSelected() != oriAnnInCenterFlag) {
            	chMap.put("EditingPanel.ActiveAnnotationInCenter", 
            		new Boolean(editInCenterCB.isSelected()));
            }
            
            String string = copyOptionComboBox.getSelectedItem().toString(), 
        	    nonLocaleString = Constants.TEXTANDTIME_STRING;

	    // Use the non-locale interpretation of the string in the comboBox. 
           
            nonLocaleString = tcMap.get(string);
                        
            if(!nonLocaleString.equals(oriCopyOption)){
            	chMap.put("EditingPanel.CopyOption", nonLocaleString);
            }
            
            /* kj: instead of this, look at the index
             * language can be changed in the meantime
             */
           
            return chMap;
        }
        return null;
    }

    /**
     * Returns whether anything has changed.
     *
     * @return whether anything has changed
     */
    public boolean isChanged() {
        if (deselectCB.isSelected() != origDeselectFlag ||
                enterCommitsCB.isSelected() != origEnterFlag ||
                clearSelectionCB.isSelected() != origClearSelFlag||
                clearSelectionOnSingleClickCB.isSelected() != origClearSelOnSingleClickFlag|| 
                createDependAnnCB.isSelected() != oriCreateDependAnnFlag ||
                snapAnnCB.isSelected() != oriSnapAnnotationsFlag || 
                newSnapValue != oriSnapValue || 
                stickToFramesCB.isSelected() != oriStickToFramesFlag ||
                suggestEntryContainsCB.isSelected() != oriSuggestSearchMethodFlag ||
                suggestSearchDescCB.isSelected() != oriSuggestSearchInDescFlag ||
                suggestIgnoreCaseCB.isSelected() != oriSuggestIgnoreCaseFlag ||
                editInCenterCB.isSelected() != oriAnnInCenterFlag ||
                origCopyOptionIndex != copyOptionComboBox.getSelectedIndex()) {
            return true;
        }

        return false;
    }
    
    /**
     * Validates the value given for snap annotations
     * 
     * @return true, if the input is valid, if not false;
     */
    public boolean checkSnapValue(){
    	if(snapAnnCB.isSelected()){
    		if (snapAnnTextField.getText() !=null){           	
        		try{
        			newSnapValue = Long.parseLong(snapAnnTextField.getText().trim());    
        			return true;
        		} catch (NumberFormatException e){        			
        			return false;        			
        		}
        	}
    	}
    	return true;
    } 
    
    public void focusSnapValue(){
    	snapAnnTextField.requestFocus();
    }

	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == snapAnnCB){
			snapAnnTextField.setEnabled(snapAnnCB.isSelected());
			if(snapAnnCB.isSelected()){
				snapAnnTextField.requestFocus();
			}		
		}
	}  
}
