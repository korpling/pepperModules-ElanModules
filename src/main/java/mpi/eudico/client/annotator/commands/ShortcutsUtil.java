package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.prefs.PreferencesReader;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * A singleton class for handling keyboard shortcuts and their mapping to
 * actions. Any new action that potentially can be triggered via a keyboard
 * shortcut should be added to 1 of the categories in the private method
 * fillActionsMap().
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ShortcutsUtil {
    private static ShortcutsUtil shortcutsUtil;

    /** annotation editing category */
    public static final String ANN_EDIT_CAT = "Frame.ShortcutFrame.Sub.AnnotationEdit";

    /** annotation navigation category */
    public static final String ANN_NAVIGATION_CAT = "Frame.ShortcutFrame.Sub.AnnotationNavigation";

    /** tier and type category */
    public static final String TIER_TYPE_CAT = "Frame.ShortcutFrame.Sub.TierType";

    /** selection category */
    public static final String SELECTION_CAT = "Frame.ShortcutFrame.Sub.Selection";

    /** media navigation category */
    public static final String MEDIA_CAT = "Frame.ShortcutFrame.Sub.MediaNavigation";

    /** document and file i/o category */
    public static final String DOCUMENT_CAT = "Frame.ShortcutFrame.Sub.Document";

    /** miscellaneous category */
    public static final String MISC_CAT = "Frame.ShortcutFrame.Sub.Misc";  
    
    private static final String PREF_FILEPATH = Constants.ELAN_DATA_DIR +
    	System.getProperty("file.separator") + "shortcuts.pfsx";
    
    private static final String NEW_PREF_FILEPATH = Constants.ELAN_DATA_DIR +
		System.getProperty("file.separator") + "shortcuts1.pfsx";
  
    /** shortcuttable actions for each mode
     *  structure Map< modeName, Map<category, List<all actions>>>*/ 
    private Map<String, Map<String, List<String>>> shortcuttableActionsMap;
    
    /** shortcut keystrokes for each mode
     *  structure Map< modeName, Map<actionName, keystroke>>*/ 
    private Map<String,Map<String, KeyStroke>> shortcutKeyStrokesMap;
    
    private boolean shortcutClash = false;

    /**
     * Creates the single ShortcutsUtil instance
     */
    private ShortcutsUtil() {
        shortcuttableActionsMap = new LinkedHashMap<String, Map<String, List<String>>>(8);
        shortcutKeyStrokesMap = new LinkedHashMap<String,Map<String, KeyStroke>>(80);
        
        fillActionsMap();
        fillShortcutMap();
    }
    
    /**
     * Returns the single instance of this class
     *
     * @return the single instance of this class
     */
    public static ShortcutsUtil getInstance() {
        if (shortcutsUtil == null) {
            shortcutsUtil = new ShortcutsUtil();
        }

        return shortcutsUtil;
    }
    
    /**
     * Adds the constant identifiers of actions that potentially can be invoked
     * by a keyboard shortcut, to one of the categories of actions. Any new
     * action that is created has to be added here as well, if it has a
     * default shortcut it should be added to {@link #loadDefaultShortcuts()}.
     */
    private void fillActionsMap() {    	
    	shortcuttableActionsMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeShortcuttableActionsMap());
    	shortcuttableActionsMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeShortcuttableActionsMap());
    }
    
    /**
     * Read from stored preferences or use defaults.
     */
    private void fillShortcutMap() {
        if (!readCurrentShortcuts()) {
            loadDefaultShortcuts();
        }
    }
    
    /**
     * Load default shortcuts.
     */
    private void loadDefaultShortcuts() {
        // defaults...
    	shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
       
        //add all actions that have no default keystroke
        addActionsWithoutShortcut();
    }  

    private Map<String, List<String>> getAnnotationModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.ANNOTATION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category
    	editActions.add(ELANCommandFactory.NEW_ANNOTATION);
    	editActions.add(ELANCommandFactory.NEW_ANNOTATION_BEFORE);
        editActions.add(ELANCommandFactory.NEW_ANNOTATION_AFTER);
        editActions.add(ELANCommandFactory.KEY_CREATE_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_ANNOTATION_TREE);      
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_HERE);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE);
        editActions.add(ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE);
        editActions.add(ELANCommandFactory.DUPLICATE_ANNOTATION);
        editActions.add(ELANCommandFactory.COPY_TO_NEXT_ANNOTATION);        
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME);
        editActions.add(ELANCommandFactory.MODIFY_ANNOTATION_DC);        
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_RIGHT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT);
        editActions.add(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_RIGHT);        
        editActions.add(ELANCommandFactory.REMOVE_ANNOTATION_VALUE);        
        editActions.add(ELANCommandFactory.DELETE_ANNOTATION);
        editActions.add(ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION);
        editActions.add(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
       
        // actions with no default shortcut key
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        editActions.add(ELANCommandFactory.REGULAR_ANNOTATION_DLG);        
        editActions.add(ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);
        editActions.add(ELANCommandFactory.DELETE_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.DELETE_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.DELETE_ALL_ANNOS_RIGHT_OF);        
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOTATIONS);
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ALL_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_IN_SELECTION);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_LEFT_OF);
        editActions.add(ELANCommandFactory.SHIFT_ANNOS_RIGHT_OF);
        editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);        
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);
        
        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION);
        navActions.add(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION);
        navActions.add(ELANCommandFactory.NEXT_ANNOTATION_EDIT);
        navActions.add(ELANCommandFactory.ANNOTATION_UP);
        navActions.add(ELANCommandFactory.ANNOTATION_DOWN);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
        
        List<String> tierActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "tier and type" category
        tierActions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
        tierActions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
       
        List<String> selActions = new ArrayList<String>();        
        selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.CLEAR_SELECTION_AND_MODE);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);
        selActions.add(ELANCommandFactory.SELECTION_MODE);
        selActions.add(ELANCommandFactory.CENTER_SELECTION);
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>();
        
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);        
        medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.NEXT_SCROLLVIEW);
        medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
        medNavActions.add(ELANCommandFactory.GO_TO_END);
        medNavActions.add(ELANCommandFactory.GOTO_DLG);
        medNavActions.add(ELANCommandFactory.LOOP_MODE);
        //actions with no defalut shortcut key
       // medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG);
        medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);
        shortcuttableActions.put(MEDIA_CAT, medNavActions);
        
        List<String> miscActions = new ArrayList<String>();         
        miscActions.add(ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
        miscActions.add(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);  
        miscActions.add("MultiTierViewer.ShiftToolTip");
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    }
    
    // default shortcuts for annotation mode
    private Map<String, KeyStroke> getAnnotationModeDefaultShortcutsMap(){    	
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_BEFORE,
 	    	KeyStroke.getKeyStroke(KeyEvent.VK_N,
 	        	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
 	            ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_ANNOTATION_AFTER,
    	 	KeyStroke.getKeyStroke(KeyEvent.VK_N,
    	    	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.KEY_CREATE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
    	 
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_ANNOTATION,
    	 	KeyStroke.getKeyStroke(KeyEvent.VK_C,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    	 
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_ANNOTATION_TREE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_C,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
    	        ActionEvent.ALT_MASK));
    	        
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_HERE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
    	    	ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_TREE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
    	        ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE,
    		KeyStroke.getKeyStroke(KeyEvent.VK_V,
    	     	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
    	        ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.DUPLICATE_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_D,
    	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_TO_NEXT_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_D,
    	    	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION,
    		KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME,
        	 	KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
        	    	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));     
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MODIFY_ANNOTATION_DC,
    		KeyStroke.getKeyStroke(KeyEvent.VK_M,
    	    	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK)); 
    	        
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT,
    	 	KeyStroke.getKeyStroke( KeyEvent.VK_J, 
    	    	ActionEvent.CTRL_MASK));
    	       
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_RIGHT,
    	 	KeyStroke.getKeyStroke( KeyEvent.VK_U, 
    	    	ActionEvent.CTRL_MASK));
    	        
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT,
    		KeyStroke.getKeyStroke( KeyEvent.VK_J, 
    	    	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK ));
      	
      	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_RIGHT,
    	  	KeyStroke.getKeyStroke( KeyEvent.VK_U, 
    	     	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK ));
      	
      	shortcutKeyStrokes.put(ELANCommandFactory.REMOVE_ANNOTATION_VALUE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.ALT_MASK));
    	       
        shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ACTIVE_ANNOTATION_EDIT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION,
        		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ANNOTATION_EDIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.ALT_MASK /*+ ActionEvent.SHIFT_MASK*/));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_UP,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.ANNOTATION_DOWN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ACTIVE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_C,
            	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION_AND_MODE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            	ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                	ActionEvent.ALT_MASK ));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_MODE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_K,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

       	shortcutKeyStrokes.put(ELANCommandFactory.CENTER_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_A, 
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
       	
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
            	ActionEvent.CTRL_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
            	/*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_STEP_AND_REPEAT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 
             	ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
             	ActionEvent.SHIFT_MASK));
           
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
         	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_SCROLLVIEW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_SCROLLVIEW,
         	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_B,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
        	KeyStroke.getKeyStroke(KeyEvent.VK_E,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
         	KeyStroke.getKeyStroke(KeyEvent.VK_G,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        shortcutKeyStrokes.put(ELANCommandFactory.LOOP_MODE,
           	 KeyStroke.getKeyStroke(KeyEvent.VK_L,
                 	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));        
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_RATE_TOGGLE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_R,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
             	ActionEvent.ALT_MASK));

        shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_R,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK));
        
        return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getTranscModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.TRANSCRIPTION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();
    	// add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation editing" category 
    	
    	editActions.add(ELANCommandFactory.COMMIT_CHANGES); 
        editActions.add(ELANCommandFactory.CANCEL_CHANGES);
        editActions.add(ELANCommandFactory.DELETE_ANNOTATION); 
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);  
        
        List<String> navActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "annotation navigation" category
        navActions.add(ELANCommandFactory.MOVE_UP);        
        navActions.add(ELANCommandFactory.MOVE_DOWN);        
        navActions.add(ELANCommandFactory.MOVE_LEFT);
        navActions.add(ELANCommandFactory.MOVE_RIGHT);
        shortcuttableActions.put(ANN_NAVIGATION_CAT, navActions);
    	
    	List<String> selActions = new ArrayList<String>();    	 
    	selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);

        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>(); 
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_FROM_START);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);  
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);        
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.LOOP_MODE);        
        shortcuttableActions.put(MEDIA_CAT, medNavActions); 
        
        List<String> miscActions = new ArrayList<String>(); 
        // has no default shortcuts
        miscActions.add(ELANCommandFactory.EDIT_IN_ANN_MODE);
        miscActions.add(ELANCommandFactory.FREEZE_TIER);  
        miscActions.add(ELANCommandFactory.HIDE_TIER);  
        shortcuttableActions.put(MISC_CAT, miscActions);    	
        
        return shortcuttableActions;
    }
    
    private Map<String, KeyStroke> getTranscModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes  = new LinkedHashMap<String, KeyStroke>(80);
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.COMMIT_CHANGES,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CANCEL_CHANGES,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.SHIFT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WB,
            	KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_UP,
            	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_DOWN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_LEFT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MOVE_RIGHT,
            	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));	
    	    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_C,
                	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                   	ActionEvent.ALT_MASK ));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE, 
            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_FROM_START, 
            	KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
          	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
               	/*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
           		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));  
       
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                	ActionEvent.SHIFT_MASK));
              
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                   ActionEvent.SHIFT_MASK));
           
       shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
    	   KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
               
       shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
            	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       
       shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
               
           shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
      shortcutKeyStrokes.put(ELANCommandFactory.LOOP_MODE,
    	 KeyStroke.getKeyStroke(KeyEvent.VK_L,
          	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getSegmentModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.SEGMENTATION_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	
    	List<String> editActions = new ArrayList<String>();    	
    	editActions.add(ELANCommandFactory.DELETE_ANNOTATION); 
    	editActions.add(ELANCommandFactory.SEGMENT); 
    	editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
        editActions.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
        
    	// actions with no default shortcut key    
        editActions.add(ELANCommandFactory.SPLIT_ANNOTATION);        
        shortcuttableActions.put(ANN_EDIT_CAT, editActions);
        
        List<String> tierActions = new ArrayList<String>();
        // add all actions that potentially can be invoked by means of
        // a keyboard shortcut and that belongs to the "tier and type" category
        tierActions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
        tierActions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
        
    	List<String> selActions = new ArrayList<String>();    	 
    	selActions.add(ELANCommandFactory.CLEAR_SELECTION);
        selActions.add(ELANCommandFactory.SELECTION_BOUNDARY);
        selActions.add(ELANCommandFactory.SELECTION_CENTER);
        shortcuttableActions.put(SELECTION_CAT, selActions);
        
        List<String> medNavActions = new ArrayList<String>();
        medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
        medNavActions.add(ELANCommandFactory.PLAY_SELECTION);
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION);   
        medNavActions.add(ELANCommandFactory.PLAY_STEP_AND_REPEAT);
        medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
        medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
        medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
        medNavActions.add(ELANCommandFactory.NEXT_FRAME);
        
        medNavActions.add(ELANCommandFactory.SECOND_LEFT);
        medNavActions.add(ELANCommandFactory.SECOND_RIGHT);
        medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
        medNavActions.add(ELANCommandFactory.GO_TO_END);
        medNavActions.add(ELANCommandFactory.GOTO_DLG);
        
        medNavActions.add(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG);
        medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);
        shortcuttableActions.put(MEDIA_CAT, medNavActions);
        
        List<String> miscActions = new ArrayList<String>();         
        miscActions.add(ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
        miscActions.add(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);
        //actions with no default shortcuts
        miscActions.add(ELANCommandFactory.BULLDOZER_MODE);
        miscActions.add(ELANCommandFactory.TIMEPROP_NORMAL);
        miscActions.add(ELANCommandFactory.SHIFT_MODE);
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    }
    
    private Map<String, KeyStroke> getSegmentModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_ANNOTATION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0 ));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.SEGMENT,
    			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WN,
            	KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.MERGE_ANNOTATION_WB,
            	KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER,
    			KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.NEXT_ACTIVE_TIER,
    			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));     
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.CLEAR_SELECTION,
            	KeyStroke.getKeyStroke(KeyEvent.VK_C,
                	ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_BOUNDARY,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            
        shortcutKeyStrokes.put(ELANCommandFactory.SELECTION_CENTER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                   	ActionEvent.ALT_MASK ));
        
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
                	ActionEvent.CTRL_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_SELECTION,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_AROUND_SELECTION,
          	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
               	/*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
           		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));        
            
       shortcutKeyStrokes.put(ELANCommandFactory.PLAY_STEP_AND_REPEAT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 
               	ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
            
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
               	ActionEvent.SHIFT_MASK));
               
       shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
           
       shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                
       shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       
      shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
    	  KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
          
      shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
    	  KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));      
      
      shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
      	KeyStroke.getKeyStroke(KeyEvent.VK_B,
           	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
      	KeyStroke.getKeyStroke(KeyEvent.VK_E,
          	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      
      shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
       	KeyStroke.getKeyStroke(KeyEvent.VK_G,
           	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));      
      
      shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_RATE_TOGGLE,
      	KeyStroke.getKeyStroke(KeyEvent.VK_R,
          	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
           	ActionEvent.ALT_MASK));

      shortcutKeyStrokes.put(ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE,
      	KeyStroke.getKeyStroke(KeyEvent.VK_R,
          	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
          	ActionEvent.SHIFT_MASK));
       
      return shortcutKeyStrokes;
    }
    
    private Map<String, List<String>> getSyncModeShortcuttableActionsMap(){
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.SYNC_MODE);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);
    	 List<String> medNavActions = new ArrayList<String>();
    	 medNavActions.add(ELANCommandFactory.PLAY_PAUSE);
         medNavActions.add(ELANCommandFactory.PIXEL_LEFT);
         medNavActions.add(ELANCommandFactory.PIXEL_RIGHT);
         medNavActions.add(ELANCommandFactory.PREVIOUS_FRAME);
         medNavActions.add(ELANCommandFactory.NEXT_FRAME);
         medNavActions.add(ELANCommandFactory.SECOND_LEFT);
         medNavActions.add(ELANCommandFactory.SECOND_RIGHT);         
         medNavActions.add(ELANCommandFactory.GO_TO_BEGIN);
         medNavActions.add(ELANCommandFactory.GO_TO_END);
         medNavActions.add(ELANCommandFactory.GOTO_DLG);         
         //actions without default shortcut key
         medNavActions.add(ELANCommandFactory.PLAYBACK_TOGGLE_DLG);         
         shortcuttableActions.put(MEDIA_CAT, medNavActions);
         return shortcuttableActions;
    
    }
    
    private Map<String, KeyStroke> getSyncModeDefaultShortcutsMap(){
    	Map<String, KeyStroke> shortcutKeyStrokes = new LinkedHashMap<String, KeyStroke>(80);
    	
        shortcutKeyStrokes.put(ELANCommandFactory.PLAY_PAUSE,
            	KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, /*Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()*/
                	ActionEvent.CTRL_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_LEFT,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
              
        shortcutKeyStrokes.put(ELANCommandFactory.PIXEL_RIGHT,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
            
        shortcutKeyStrokes.put(ELANCommandFactory.PREVIOUS_FRAME,
           	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                
        shortcutKeyStrokes.put(ELANCommandFactory.NEXT_FRAME,
          	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
               	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_LEFT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.SECOND_RIGHT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_BEGIN,
        	KeyStroke.getKeyStroke(KeyEvent.VK_B,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GO_TO_END,
        	KeyStroke.getKeyStroke(KeyEvent.VK_E,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        shortcutKeyStrokes.put(ELANCommandFactory.GOTO_DLG,
         	KeyStroke.getKeyStroke(KeyEvent.VK_G,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            
        return shortcutKeyStrokes;       
    }
    
    private Map<String, List<String>> getCommonShortcuttableActionsMap(){   
    	Map<String, List<String>>  shortcuttableActions = shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	if(shortcuttableActions != null){    		
    		return shortcuttableActions;
    	}
    	
    	shortcuttableActions = new LinkedHashMap<String, List<String>> (8);  
    	
    	List<String> tierActions = new ArrayList<String>();
        tierActions.add(ELANCommandFactory.ADD_TIER);
        tierActions.add(ELANCommandFactory.DELETE_TIER);
        tierActions.add(ELANCommandFactory.ADD_TYPE);        
     // actions with no default shortcut key
        tierActions.add(ELANCommandFactory.CHANGE_TIER);
        tierActions.add(ELANCommandFactory.REPARENT_TIER);
        tierActions.add(ELANCommandFactory.TOKENIZE_DLG);
        tierActions.add(ELANCommandFactory.FILTER_TIER);
        tierActions.add(ELANCommandFactory.COPY_TIER);
        tierActions.add(ELANCommandFactory.ANN_FROM_OVERLAP);
        tierActions.add(ELANCommandFactory.MERGE_TIERS);
        tierActions.add(ELANCommandFactory.ANN_FROM_GAPS);
        tierActions.add(ELANCommandFactory.CHANGE_CASE);
        tierActions.add(ELANCommandFactory.COMPARE_ANNOTATORS_DLG);
        tierActions.add(ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES);
        tierActions.add(ELANCommandFactory.ANN_ON_DEPENDENT_TIER);
        tierActions.add(ELANCommandFactory.LABEL_AND_NUMBER);
        tierActions.add(ELANCommandFactory.TIER_DEPENDENCIES);
        tierActions.add(ELANCommandFactory.CHANGE_TYPE);
        tierActions.add(ELANCommandFactory.DELETE_TYPE);        
        shortcuttableActions.put(TIER_TYPE_CAT, tierActions);
        
        List<String> docActions = new ArrayList<String>();
        docActions.add(ELANCommandFactory.NEW_DOC);
        docActions.add(ELANCommandFactory.OPEN_DOC);
        docActions.add(ELANCommandFactory.SAVE);
        docActions.add(ELANCommandFactory.SAVE_AS);
        docActions.add(ELANCommandFactory.SAVE_AS_TEMPLATE);        
        docActions.add(ELANCommandFactory.PRINT);
        docActions.add(ELANCommandFactory.PREVIEW);
        docActions.add(ELANCommandFactory.PAGESETUP);
        docActions.add(ELANCommandFactory.NEXT_WINDOW);
        docActions.add(ELANCommandFactory.PREV_WINDOW);
        docActions.add(ELANCommandFactory.CLOSE);
        docActions.add(ELANCommandFactory.EXIT);
        //actions with no default shortcut key
        docActions.add(ELANCommandFactory.SAVE_SELECTION_AS_EAF);
        docActions.add(ELANCommandFactory.MERGE_TRANSCRIPTIONS);
        docActions.add(ELANCommandFactory.IMPORT_SHOEBOX);
        docActions.add(ELANCommandFactory.IMPORT_TOOLBOX);
        docActions.add(ELANCommandFactory.IMPORT_FLEX);
        docActions.add(ELANCommandFactory.IMPORT_CHAT);
        docActions.add(ELANCommandFactory.IMPORT_TRANS);
        docActions.add(ELANCommandFactory.IMPORT_TAB);
        docActions.add(ELANCommandFactory.IMPORT_PRAAT_GRID);
        docActions.add(ELANCommandFactory.IMPORT_PREFS);
        docActions.add(ELANCommandFactory.IMPORT_TIERS);
        docActions.add(ELANCommandFactory.IMPORT_TYPES);
        docActions.add(ELANCommandFactory.EXPORT_TAB_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_ANNLIST_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_WORDLIST_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_TIERS_MULTI);
        docActions.add(ELANCommandFactory.EXPORT_FILMSTRIP);
        docActions.add(ELANCommandFactory.EXPORT_HTML);
        docActions.add(ELANCommandFactory.EXPORT_IMAGE_FROM_WINDOW);
        docActions.add(ELANCommandFactory.EXPORT_INTERLINEAR);
        docActions.add(ELANCommandFactory.EXPORT_MEDIA);
        docActions.add(ELANCommandFactory.EXPORT_PRAAT_GRID);
        docActions.add(ELANCommandFactory.EXPORT_PREFS);
        docActions.add(ELANCommandFactory.EXPORT_QT_SUB);
        docActions.add(ELANCommandFactory.EXPORT_SHOEBOX);
        docActions.add(ELANCommandFactory.EXPORT_SMIL_RT);               
        docActions.add(ELANCommandFactory.EXPORT_SMIL_QT);
        docActions.add(ELANCommandFactory.EXPORT_SUBTITLES);
        docActions.add(ELANCommandFactory.EXPORT_TAB);
        docActions.add(ELANCommandFactory.EXPORT_RECOG_TIER);
        docActions.add(ELANCommandFactory.EXPORT_TIGER);
        docActions.add(ELANCommandFactory.EXPORT_TOOLBOX);
        docActions.add(ELANCommandFactory.EXPORT_TRAD_TRANSCRIPT);
        docActions.add(ELANCommandFactory.EXPORT_WORDS);
        shortcuttableActions.put(DOCUMENT_CAT, docActions);
        
        List<String> miscActions = new ArrayList<String>();
        miscActions.add(ELANCommandFactory.UNDO);
        miscActions.add(ELANCommandFactory.REDO);
        miscActions.add(ELANCommandFactory.SEARCH_DLG);
        miscActions.add(ELANCommandFactory.SEARCH_MULTIPLE_DLG);
        miscActions.add(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG);
        miscActions.add(ELANCommandFactory.LINKED_FILES_DLG);
        miscActions.add(ELANCommandFactory.EDIT_CV_DLG);
        miscActions.add(ELANCommandFactory.HELP);
        miscActions.add(ELANCommandFactory.COPY_CURRENT_TIME);
        //actions with no default shortcut key        
        miscActions.add(ELANCommandFactory.REPLACE_MULTIPLE);        
        miscActions.add(ELANCommandFactory.EDIT_LEX_SRVC_DLG);
        miscActions.add(ELANCommandFactory.EDIT_PREFS);
        miscActions.add(ELANCommandFactory.EDIT_SHORTCUTS);
        miscActions.add(ELANCommandFactory.SET_AUTHOR);
        miscActions.add(ELANCommandFactory.FONT_BROWSER);
        miscActions.add(ELANCommandFactory.SHORTCUTS);
        miscActions.add(ELANCommandFactory.SPREADSHEET);
        miscActions.add(ELANCommandFactory.STATISTICS);        
        miscActions.add(ELANCommandFactory.ABOUT);      
        // category to be checked
        miscActions.add(ELANCommandFactory.ANNOTATION_MODE);
        miscActions.add(ELANCommandFactory.SYNC_MODE);
        miscActions.add(ELANCommandFactory.TRANSCRIPTION_MODE);
        miscActions.add(ELANCommandFactory.SEGMENTATION_MODE);
        miscActions.add(ELANCommandFactory.INTERLINEARIZATION_MODE);        
        shortcuttableActions.put(MISC_CAT, miscActions);
        
        return shortcuttableActions;
    	
    }
    
    private Map<String, KeyStroke> getCommonDefaultShortcutsMap(){ 
    	Map<String, KeyStroke> shortcutKeyStrokes  = new LinkedHashMap<String, KeyStroke>(80);
    	
    	shortcutKeyStrokes.put(ELANCommandFactory.ADD_TIER,
                KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.DELETE_TIER,
        	KeyStroke.getKeyStroke(KeyEvent.VK_T,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.ADD_TYPE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_T,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.NEW_DOC,
        	KeyStroke.getKeyStroke(KeyEvent.VK_N,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.OPEN_DOC,
        	KeyStroke.getKeyStroke(KeyEvent.VK_O,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE_AS,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK));
           
    	shortcutKeyStrokes.put(ELANCommandFactory.SAVE_AS_TEMPLATE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_S,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.PRINT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            
    	shortcutKeyStrokes.put(ELANCommandFactory.PREVIEW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.ALT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.PAGESETUP,
        	KeyStroke.getKeyStroke(KeyEvent.VK_P,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.NEXT_WINDOW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.SHIFT_MASK));
           
    	shortcutKeyStrokes.put(ELANCommandFactory.PREV_WINDOW,
        	KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.CLOSE,
        	KeyStroke.getKeyStroke(KeyEvent.VK_W,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.EXIT,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.UNDO,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.REDO,
        	KeyStroke.getKeyStroke(KeyEvent.VK_Y,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SEARCH_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.SEARCH_MULTIPLE_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_F,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.SHIFT_MASK + ActionEvent.ALT_MASK));

        
    	shortcutKeyStrokes.put(ELANCommandFactory.LINKED_FILES_DLG,
          	KeyStroke.getKeyStroke(KeyEvent.VK_L,
             	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
               	ActionEvent.ALT_MASK));        
        
    	shortcutKeyStrokes.put(ELANCommandFactory.EDIT_CV_DLG,
        	KeyStroke.getKeyStroke(KeyEvent.VK_C,
            	Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
            	ActionEvent.SHIFT_MASK));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.HELP,
            KeyStroke.getKeyStroke(KeyEvent.VK_H,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
    	shortcutKeyStrokes.put(ELANCommandFactory.COPY_CURRENT_TIME,
        	KeyStroke.getKeyStroke(KeyEvent.VK_G, 
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                ActionEvent.ALT_MASK));
    	
    	return shortcutKeyStrokes;
    }
    
    /**
     * adds all actions that are shortcuttable but are missing from shortcutKeyStrokes to it
     */
    private void addActionsWithoutShortcut() {
    	Iterator it = shortcuttableActionsMap.entrySet().iterator();
    	while(it.hasNext()){    
    		String modeName = (String) ((Map.Entry) it.next()).getKey();    			
    		Map<String, KeyStroke> shortcutKeyStrokes = shortcutKeyStrokesMap.get(modeName);
    		Iterator OuterIt = shortcuttableActionsMap.get(modeName).entrySet().iterator();
    		while (OuterIt.hasNext())
    		{
    			Map.Entry kvpair = (Map.Entry) OuterIt.next();
        		List<String> actionList = (List<String>) kvpair.getValue();
        		for(int i=0; i< actionList.size(); i++){
        			String action = actionList.get(i);
        			if (!(shortcutKeyStrokes.containsKey(action)))
        			{
        				shortcutKeyStrokes.put(action, null);
        			}
        		}
    		}
    	}
	}
    

	/**
     * Returns the KeyStroke for the specified action identifier.
     *
     * @param actionID the identifier, one of the constants in {@link
     *        ELANCommandFactory}.
     *
     * @return a KeyStroke or null
     */
    public KeyStroke getKeyStrokeForAction(String actionID, String modeName) {
    	KeyStroke ks = null;
        if (actionID != null) { 
        	if(modeName == null){
        		if(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).get(actionID);
        		}
        	}else{
        		if(shortcutKeyStrokesMap.get(modeName).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(modeName).get(actionID);
        		} else if(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).containsKey(actionID)){
    				ks = shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).get(actionID);
        		}
        			
        	}
        }
        return ks;
    }

    /**
     * Returns the current id- keystroke mappings.
     *
     * @return a map containing id to keystroke mappings
     */
    public Map<String, KeyStroke> getCurrentShortcuts(String modeName) {
        Map<String, KeyStroke> currentShortcutKeyStrokes = new HashMap<String, KeyStroke> ();
        currentShortcutKeyStrokes.putAll(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS));
        if(modeName != null){
        	currentShortcutKeyStrokes.putAll(shortcutKeyStrokesMap.get(modeName));
        }
        
		return currentShortcutKeyStrokes ;
    }

    /**
     * Returns a map of all action that can have a shortcut. The keys are
     * category names, the values are lists of action identifiers.
     *
     * @return a mapping of all actions, grouped per category
     */
    public Map<String, List<String>> getShortcuttableActions(String modeName) {
    	if(modeName == null){
    		return shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	}
        return shortcuttableActionsMap.get(modeName);
    }    
  
    /**
     * Returns all the shortcuts used in the give mode
     * 
     * @param modeName, name of the mode
     * @return Map with all the shortcuts associated with this mode
     */
    public Map<String, KeyStroke> getShortcutKeysOnlyIn(String modeName) {
    	if(modeName == null){
    		return shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    	}
        return shortcutKeyStrokesMap.get(modeName);
    }

    /**
     * Returns a (friendly) description of the action (by default the tooltip
     * description). This is independent of any transcription or instantiated
     * actions.
     *
     * @param actionID the id
     *
     * @return a description or the empty string
     */
    public String getDescriptionForAction(String actionID) {
        if (actionID == null) {
            return "";
        }

        String desc = ElanLocale.getString(actionID + "ToolTip");

        if ((desc == null) || (desc.length() == 0)) {
            desc = ElanLocale.getString(actionID);
        }

        return desc;
    }
    
    
    /**
     * Returns the category this action belongs to
     *
     * @param actionID the id
     *
     * @return a category name or the empty string
     */
    public String getCategoryForAction(String modeName, String actionID)
    {
        if (actionID == null) {
            return "";
        }
        
        Iterator it;
        
        if(modeName == null){
        	it = shortcuttableActionsMap.get(ELANCommandFactory.COMMON_SHORTCUTS).entrySet().iterator(); 
    	} else{
    		it = shortcuttableActionsMap.get(modeName).entrySet().iterator();
    	}
        
        while (it.hasNext())
        {
        		Map.Entry pairs = (Map.Entry) it.next();
        		String cat = (String) pairs.getKey();
        		List<String> actionList = (ArrayList<String>) pairs.getValue();
        		if (actionList.contains(actionID))
        		{
        			return cat;
        		}
        }
        return "";
    }
    
    /**
     * Returns a user readable, platform specific, description of the key and
     * the modifiers.
     *
     * @param ks the keystroke
     *
     * @return the description
     */
    public String getDescriptionForKeyStroke(KeyStroke ks) {
        if (ks == null) {
            return "";
        }

        String nwAcc = "";

        if (SystemReporting.isMacOS()) {
            int modifier = ks.getModifiers();

            if ((modifier & InputEvent.CTRL_MASK) != 0) {
                nwAcc += "\u2303";
            }
            
            if ((modifier & InputEvent.SHIFT_MASK) != 0) {
                nwAcc += "\u21E7";
            }

            if ((modifier & InputEvent.ALT_MASK) != 0) {
                nwAcc += "\u2325";
            }
            
            if ((modifier & InputEvent.META_MASK) != 0) {
                nwAcc += "\u2318";
            }

            if (ks.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
            	if(ks.getKeyCode() == KeyEvent.VK_DELETE){
            		nwAcc += "Delete";
            	} else if( ks.getKeyCode() == KeyEvent.VK_PAGE_UP){
            		nwAcc += "PageUp";
            	} else if( ks.getKeyCode() == KeyEvent.VK_PAGE_DOWN){
            		nwAcc += "PageDown";
            	} else if( ks.getKeyCode() == KeyEvent.VK_SPACE){
            		nwAcc += "Space";
//            	} else if( ks.getKeyCode() == KeyEvent.VK_ENTER){
//            		nwAcc += "Return";
            	} else {
            		nwAcc += KeyEvent.getKeyText(ks.getKeyCode());
            	}
            } else {
                nwAcc += String.valueOf(ks.getKeyChar());
            }
        } else {
            int modifier = ks.getModifiers();

            if ((modifier & InputEvent.CTRL_MASK) != 0) {
                nwAcc += "Ctrl+";
            }

            if ((modifier & InputEvent.ALT_MASK) != 0) {
                nwAcc += "Alt+";
            }

            if ((modifier & InputEvent.SHIFT_MASK) != 0) {
                nwAcc += "Shift+";
            }

            if (ks.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {            	
                nwAcc += KeyEvent.getKeyText(ks.getKeyCode());
            } else {
                nwAcc += String.valueOf(ks.getKeyChar());
            }
        }

        return nwAcc;
    }

    /**
     * Restores the default keyboard shortcuts.
     */   
    public void restoreDefaultShortcutsForthisMode(String modeName){
    	if(modeName == null || modeName.equals(ELANCommandFactory.COMMON_SHORTCUTS)){
    		shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	} else 	if(modeName.equals(ELANCommandFactory.ANNOTATION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getAnnotationModeDefaultShortcutsMap());
    	} 
    	else if(modeName.equals(ELANCommandFactory.SYNC_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getSyncModeDefaultShortcutsMap());
    	}
    	else if(modeName.equals(ELANCommandFactory.TRANSCRIPTION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getTranscModeDefaultShortcutsMap());
    	}
    	else if(modeName.equals(ELANCommandFactory.SEGMENTATION_MODE)){
    		shortcutKeyStrokesMap.put(modeName, getSegmentModeDefaultShortcutsMap());
    	}   
    	
    	JOptionPane.showMessageDialog( null,ElanLocale.getString("Shortcuts.Message.Restored") + " "+ ElanLocale.getString(modeName));
    }
    
    public void restoreAll(){
    	shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap());
    	shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());
    	
    	JOptionPane.showMessageDialog( null,ElanLocale.getString("Shortcuts.Message.RestoredAll"));
    }
    
    /**
     * Reads stored shortcuts form file
     *
     * @return true if stored mappings have been loaded, false otherwise
     */
    public boolean readCurrentShortcuts() 
    {    	
    	PreferencesReader	xmlPrefsReader = new PreferencesReader();
    	Map<String,Map<String, KeyStroke>> shortcutKeyMap = new HashMap<String,Map<String, KeyStroke>>();     	    	
    	HashMap shortcutMapRaw = null;
    	
    	try{
    		File file = new File(NEW_PREF_FILEPATH);
    		if(file.exists()){
    			shortcutMapRaw = (HashMap) xmlPrefsReader.parse(NEW_PREF_FILEPATH);
    		} else {
    			shortcutMapRaw = (HashMap) xmlPrefsReader.parse(PREF_FILEPATH);
    		}
    		
    	}catch (Exception ex) {
            ClientLogger.LOG.warning("Could not load the keyboard shortcut preferences file");
        }    	
    	
    	if (shortcutMapRaw != null && (!shortcutMapRaw.isEmpty()))
  	 	{    		
    		if(shortcutMapRaw.values().iterator().hasNext()){    			
          		Object val = shortcutMapRaw.values().iterator().next();   
          		// if the preferences file is in the old format
				if (val instanceof ArrayList) 
          		{
					Iterator it = shortcutMapRaw.entrySet().iterator();
					HashMap<String, KeyStroke> shortcutMap = new HashMap<String, KeyStroke>();
	          	 	while (it.hasNext())
	          	 	{            	 		
	          	 		Map.Entry pair = (Map.Entry) it.next();
	          	 		String actionName = (String) pair.getKey();
	              		val = pair.getValue();            	 		
	    	
	              		if (val instanceof ArrayList) 
	              		{
	              			ArrayList<String> codes = (ArrayList<String>) val;
							if (codes.isEmpty())
	              			{
	              				shortcutMap.put(actionName, null);
	              			}
	              			else
	              			{
	    	            		int keycode = Integer.parseInt(codes.get(0));
	    	            		int modcode = Integer.parseInt(codes.get(1));
	    	            		KeyStroke aks = KeyStroke.getKeyStroke(keycode, modcode);
	    	            		shortcutMap.put(actionName, aks);
	              			}
	              		}
	          	 	}
	          	 	
	          	 	// covert the old preference format to the new format
	          	 	shortcutKeyMap.putAll(covertToNewShortCutMap(shortcutMap));	  
          		} 
				
				// if the preferences file is in the new format based on different modes
				else if(val instanceof Map) {  
          			Iterator it = shortcutMapRaw.entrySet().iterator();         	
        			while (it.hasNext())
                 	{            	 		
                 		Map.Entry pair = (Map.Entry) it.next();
                 		String modeName = (String) pair.getKey();
                   		val = pair.getValue();   
                   		Map<String, KeyStroke> shortcutMap = null;
                   		if(val instanceof Map){
                   			shortcutMap = new HashMap<String, KeyStroke>();
                   			Iterator it1 = ((Map<String, List<String>>)val).entrySet().iterator();
                   			while (it1.hasNext())
                         	{            	
                   				pair = (Map.Entry) it1.next();
                   				String actionName = (String) pair.getKey();
                   				val = pair.getValue();     
                   				if (val instanceof ArrayList) 
                   				{
                   					ArrayList<String> codes = (ArrayList<String>) val;
                   					if (codes.isEmpty())
                   					{
                   						shortcutMap.put(actionName, null);
                   					}
                   					else
                   					{
                   						int keycode = Integer.parseInt(codes.get(0));
                   						int modcode = Integer.parseInt(codes.get(1));
                   						KeyStroke aks = KeyStroke.getKeyStroke(keycode,modcode);
                   						shortcutMap.put(actionName, aks);
                   					}
                   				}
                         	}
                   		}           		
                   		shortcutKeyMap.put(modeName, shortcutMap); 
                 	}
          		}
				
				shortcutKeyStrokesMap.clear();
        		shortcutKeyStrokesMap.putAll(shortcutKeyMap);
        		
        		if(checkForNewShortcuts()){
        			PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    		
        			try 
        			{            		
        				xmlPrefsWriter.encodeAndSave(getStorableShortcutMap(shortcutKeyStrokesMap), NEW_PREF_FILEPATH);    		
        			}  catch (Exception ex) {
        				ClientLogger.LOG.warning("Error while updating the shortcuts file. File not created.");
        			} 
        		}
    		}
    		
    		// ensure that all shortcuttable Actions are in the hash
    		addActionsWithoutShortcut();
    		return true;
  	 	} else {
  	 		return false;    	 		
  	 	} 
    }
    
    public HashMap<String, HashMap<String, List<String>>> getStorableShortcutMap(Map<String,Map<String, KeyStroke>> shortcutKeyStrokesMap){
    	//overwrite the shortcut preferences file in new format
    	HashMap<String ,HashMap<String, List<String>>> shortcutModeMap = new HashMap<String, HashMap<String, List<String>>>(); 
    	Iterator it = shortcutKeyStrokesMap.entrySet().iterator();
    	while(it.hasNext()){
    		Map.Entry pair = (Map.Entry) it.next();
     		String modeName = (String) pair.getKey();
     		Map<String, KeyStroke> valMap = (Map<String, KeyStroke>)pair.getValue();   
    		Iterator it1 = valMap.entrySet().iterator();
    		HashMap<String, List<String>> map = new HashMap<String, List<String>>();  
    		while(it1.hasNext()){
    			pair = (Map.Entry) it1.next();
         		String actionName = (String) pair.getKey();
         		KeyStroke ks = (KeyStroke)pair.getValue(); 
         		ArrayList<String> codes = new ArrayList<String>(2);
         		if(ks != null){
         			codes.add(String.valueOf(ks.getKeyCode()));
         			codes.add(String.valueOf(ks.getModifiers()));
         		}
      			map.put(actionName,codes);
        		
    		}
    		shortcutModeMap.put(modeName, map);
    	}
    	return shortcutModeMap;
    }
    	
    private HashMap<String,Map<String, KeyStroke>> covertToNewShortCutMap(HashMap<String, KeyStroke> shortcutMap) {
    	
    	List<String> generalActions = new ArrayList<String>();
    	List<String> annModeActions = new ArrayList<String>();
    	List<String> transModeActions = new ArrayList<String>(); ;
    	List<String> syncModeActions = new ArrayList<String>();
    	List<String> segMentActions = new ArrayList<String>();
    	
    	Map<String, KeyStroke> annModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> transModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> syncModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> segmentModeMap = new HashMap<String, KeyStroke>();
    	Map<String, KeyStroke> commonModeMap = new HashMap<String, KeyStroke>();
    	
    	Iterator it = getCommonShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		generalActions.addAll((List<String>)it.next());
    	}
    	
    	it = getAnnotationModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		annModeActions.addAll((List<String>)it.next());
    	}
    	
    	it = getTranscModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		transModeActions.addAll((List<String>)it.next());
    	}
    	
    	it = getSyncModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		syncModeActions.addAll((List<String>)it.next());
    	}
    	
    	it = getSegmentModeShortcuttableActionsMap().values().iterator();
    	while(it.hasNext()){
    		segMentActions.addAll((List<String>)it.next());
    	}   
    	
    	// assign the old shortcuts to its revelant mode
    	it = shortcutMap.entrySet().iterator();
    	while(it.hasNext()){
    		Map.Entry pair = (Map.Entry) it.next();
     		String actionName = (String) pair.getKey();
     		KeyStroke val = (KeyStroke)pair.getValue();   
     		if(generalActions.contains(actionName) && val != null){
     			commonModeMap.put(actionName, val);
     		} else {
     			if(annModeActions.contains(actionName) && val != null){
     				annModeMap.put(actionName, val);
         		}
     			
     			if(transModeActions.contains(actionName) && val != null){
     				transModeMap.put(actionName, val);
     			}
     			
     			if(syncModeActions.contains(actionName) && val != null){
     				syncModeMap.put(actionName, val);
         		}
     			
     			if(segMentActions.contains(actionName) && val != null){
     				segmentModeMap.put(actionName, val);
         		}
     		}
    	}   
    	
    	
    	// New Modes have different shortcut keys for the actions in old shortcut file
    	// update all new shortcut keys(default) for the new modes
    	// only when shortcuts.pfsx file is found (versions till 4.1.2)
    	
    	/// Transcription mode special shortcuts
    	
    	KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.COMMIT_CHANGES,	ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.CANCEL_CHANGES, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.DELETE_ANNOTATION, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_UP, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_DOWN, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_LEFT, ks);
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.MOVE_RIGHT, ks);	
    	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.PLAY_PAUSE, ks);
        
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK);
    	if(transModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	transModeMap.put(ELANCommandFactory.PLAY_FROM_START, ks);

    	// SegementationMode special shortcuts
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.DELETE_ANNOTATION, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.SEGMENT, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER, ks);
     	
    	ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    	if(segmentModeMap.containsValue(ks) || commonModeMap.containsValue(ks)){
    		shortcutClash = true;
    	}
    	segmentModeMap.put(ELANCommandFactory.NEXT_ACTIVE_TIER, ks); 
    	
    	HashMap<String,Map<String, KeyStroke>> shortcutKeysMap = new HashMap<String,Map<String, KeyStroke>>();    	
    	shortcutKeysMap.put(ELANCommandFactory.COMMON_SHORTCUTS, commonModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.ANNOTATION_MODE,annModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.SYNC_MODE, syncModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, transModeMap);
    	shortcutKeysMap.put(ELANCommandFactory.SEGMENTATION_MODE, segmentModeMap);    	
		return shortcutKeysMap;
	}
    
    /**
     * Check is made for each version for new shortcuts added
     * 
     * @return
     */
    private boolean checkForNewShortcuts(){
    	Object val= Preferences.get("ShortcutKeyUpdateVersion", null);
    	String version = ELAN.getVersionString();
    	if(val == null || !((String)val).equals(version)){ 
    		addNewShortcuts(ELANCommandFactory.COMMON_SHORTCUTS, getCommonDefaultShortcutsMap()); 
    		addNewShortcuts(ELANCommandFactory.ANNOTATION_MODE, getAnnotationModeDefaultShortcutsMap()); 
    		addNewShortcuts(ELANCommandFactory.TRANSCRIPTION_MODE, getTranscModeDefaultShortcutsMap()); 
    		addNewShortcuts(ELANCommandFactory.SYNC_MODE, getSyncModeDefaultShortcutsMap()); 
    		addNewShortcuts(ELANCommandFactory.SEGMENTATION_MODE, getSegmentModeDefaultShortcutsMap());     		
    		Preferences.set("ShortcutKeyUpdateVersion", version, null);
    		
    		if(shortcutClash){
    			// display a message
    			String message = ElanLocale.getString("Shortcuts.Warning.Clashes") + System.getProperty("line.separator") +
    					ElanLocale.getString("Shortcuts.Warning.Edit");
    	        	JOptionPane.showMessageDialog(null, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    	        
    		}
    		return true;
    	}    	
    	return false;
    }
    	
    private void addNewShortcuts(String modeName, Map<String, KeyStroke> defaultShorcutMap){
    	Map<String, KeyStroke> currentShorcutMapForThisMode = shortcutKeyStrokesMap.get(modeName); 
    	
    	Map<String, KeyStroke> currentlyUsedShorcutMap = new HashMap<String, KeyStroke>(); 
    	currentlyUsedShorcutMap.putAll(shortcutKeyStrokesMap.get(modeName));      	
    	if(!modeName.equals(ELANCommandFactory.COMMON_SHORTCUTS)){    	
    		currentlyUsedShorcutMap.putAll(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS));
    	}
    	
		Iterator shortcutIt = defaultShorcutMap.entrySet().iterator();
		Map.Entry pair;
		String actionName;
		KeyStroke ks;		
		
		while(shortcutIt.hasNext()){
    		pair = (Map.Entry) shortcutIt.next();
    		actionName = (String) pair.getKey();
    		if(!currentShorcutMapForThisMode.containsKey(actionName)){
    			ks = (KeyStroke)pair.getValue();       			
    			// shortcuts clashes
    			currentShorcutMapForThisMode.put(actionName, ks);
    			
    			if(currentlyUsedShorcutMap.containsValue(ks)){
    				shortcutClash = true;
    			}    		
    			currentlyUsedShorcutMap.put(actionName, ks);
    			
    		}
		}
		shortcutKeyStrokesMap.put(modeName, currentShorcutMapForThisMode);    		
    }
	
    /**
     * Saves the user defined keyboard shortcut to action mappings.
     */
    public void saveCurrentShortcuts(HashMap<String, HashMap<String, List<String>>> shortcutMap) 
    {
    	PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    		
    	try 
    	{
    		xmlPrefsWriter.encodeAndSave(shortcutMap, NEW_PREF_FILEPATH);    		
    		JOptionPane.showMessageDialog( null, ElanLocale.getString("Shortcuts.Message.Saved"));
        }  catch (Exception ex) {
           ClientLogger.LOG.warning("Could not save the keyboard shortcut preferences file");
       	  	JOptionPane.showMessageDialog( null, ElanLocale.getString("Shortcuts.Message.NotSaved"));
        } 
    } 
}
