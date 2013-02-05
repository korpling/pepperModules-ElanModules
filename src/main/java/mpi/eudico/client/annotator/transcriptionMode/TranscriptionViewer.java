package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.SelectionPanel;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.gui.AdvancedTierOptionsDialog;
import mpi.eudico.client.annotator.layout.TranscriptionManager;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.util.TableSubHeaderObject;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Viewer for the transcription table in the transcription mode layout
 * with added functions, which allows easy transcription for the selected type of tiers
 * 
 * @author aarsom
 *
 */
public class TranscriptionViewer  extends AbstractViewer implements ListSelectionListener, ACMEditListener{	

    /** Holds value of property DOCUMENT ME! */
    public static final String CREATE_ANN = "create";
    
    private TranscriptionManager layoutManager;
    private ViewerManager2 viewerManager;
    //private ElanMediaPlayer viewerManager.getMasterMediaPlayer();
    private SignalViewer signalViewer;
    
    private JScrollPane scroller;
    private TranscriptionTable table;
    private TranscriptionTableModel tableModel;

 	private HashMap<String,Integer> columnOrder;
 	private HashMap<String,Integer> columnWidth;
 	
 	private HashMap<TierImpl, List<TierImpl>> tierMap;
 	
 	private List<String> hiddenTiersList;  
 	private List<String> nonEditableTiersList;  	
 	private List<String> columnTypeList;  	
 	private List<Color> tierColorsList;
 	
 	private JPopupMenu popupMenu;
	private JMenuItem nonEditableTierMI;
	private JMenuItem hideAllTiersMI;
	private JMenuItem showHideMoreMI;
	private JMenuItem changeColorMI;
 	
 	private boolean merge = false;
 	private boolean showTierNames = true;
 	private boolean autoPlayBack = true; 	
 	
 	private int playAroundSelection = 500;

	private ArrayList<KeyStroke> keyStrokesList; 	 
 	
 	/**
 	 * Creates a instance of TranscriptionViewer
 	 * 
 	 * @param viewerManager
 	 * @param transManager
 	 */
    public TranscriptionViewer(ViewerManager2 viewerManager){     	
    	this.viewerManager = viewerManager; 
    	//viewerManager.getMasterMediaPlayer() = viewerManager.getMasterMediaPlayer();
   	 	signalViewer = viewerManager.getSignalViewer(); 
   	 	if(signalViewer != null){
   	 		signalViewer.setRecalculateInterval(false);
   	 	}
    }    
    
    /**
     * Initializes this viewer with the necessary components
     * 
     * @param transManager
     */
    public void intializeViewer(TranscriptionManager transManager){
    	layoutManager = transManager;	
    	tierColorsList = new ArrayList<Color>();
    	hiddenTiersList = new ArrayList<String>();
		tierMap = new HashMap<TierImpl,List<TierImpl>>();
		hiddenTiersList = new ArrayList<String>();
		nonEditableTiersList = new ArrayList<String>();
		columnOrder = new HashMap<String, Integer>();	
		columnWidth = new HashMap<String, Integer>();	
		
		keyStrokesList = new ArrayList<KeyStroke>();        
	    Iterator it = ShortcutsUtil.getInstance().getShortcutKeysOnlyIn(ELANCommandFactory.TRANSCRIPTION_MODE).values().iterator();
	    KeyStroke ks;
	    while(it.hasNext()){
	    	ks = (KeyStroke) it.next();
	      	if(ks != null){
	       		keyStrokesList.add(ks);
	       	}
	    }
		initializeTable();
		preferencesChanged();
    }
    
    /**
     * Creates a popup menu.
     */
    private void createPopUpMenu(){
		ActionListener actionLis = new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				if(e.getSource() == nonEditableTierMI){
					editOrNoneditableTier();
				} else if(e.getSource() == hideAllTiersMI){					
					hideTiers();	
				} else if( e.getSource() == showHideMoreMI) {
					showHideMoreTiers();
				} else if(e.getSource() == changeColorMI){					
					showChangeColorDialog(table.getTierName(table.getCurrentRow(),table.getCurrentColumn()));
				}
			}
		};
		
		popupMenu = new JPopupMenu("HideTier");
		nonEditableTierMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.EditableTier"));	
		nonEditableTierMI.addActionListener(actionLis);
		
		hideAllTiersMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.HideLinkedTiers"));
		hideAllTiersMI.addActionListener(actionLis);
		
		showHideMoreMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.ShoworHideTiers"));
		showHideMoreMI.addActionListener(actionLis);
		
		changeColorMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.ChangeColorForThisTier"));
		changeColorMI.addActionListener(actionLis);
		
		//updatePopUpShortCuts();
		
		popupMenu.add(changeColorMI);	
		popupMenu.add(nonEditableTierMI);	
		popupMenu.addSeparator();
		popupMenu.add(hideAllTiersMI);
		popupMenu.add(showHideMoreMI);			
    }
    
    public void showChangeColorDialog(String tierName){
    	AdvancedTierOptionsDialog dialog = new AdvancedTierOptionsDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()),
				ElanLocale.getString("EditTierDialog.Title.Change"), viewerManager.getTranscription(), tierName);
        dialog.setVisible(true); 
        
        setPreferredFontAndColorSettings();
    }
    
    /**
     * Returns whether the given tier is editable or not
     * 
     * @param tierName, the tier which has to be checked
     * @return true, if it is a editable tier else return false
     */
    public boolean isEditableTier(String tierName){
    	return !nonEditableTiersList.contains(tierName);
	}
    
    private void editOrNoneditableTier(){    	
    	String tierName = table.getTierName(table.getCurrentRow(),table.getCurrentColumn());
		editOrNoneditableTier(tierName);
    }
    
    /**
     * Changes the given tier either as editable/non-editable
     * 
     * @param tierName, the given tier name
     */
    public void editOrNoneditableTier(String tierName){    	
    	if(!nonEditableTiersList.contains(tierName)){
			nonEditableTiersList.add(tierName);			
		} else {
			nonEditableTiersList.remove(tierName);
		}
    	
    	table.setNoneditableTiers(nonEditableTiersList); 
    	if(tierMap.size() >=1 && columnTypeList.size() >1){
    		Iterator it = tierMap.entrySet().iterator();
    		Object keyObj;
    		while(it.hasNext()){
    			keyObj = it.next();
    			if(tierMap.get(keyObj) != null && tierMap.get(keyObj).size() >1){
    				table.startEdit(null);
    		    	if(!table.isEditing()){
    		    		table.goToNextEditableCell();
    		    	    					
    				}
    			}
    		}    		
    	}
    	  	
    }
    
    public void setNoneditableTier(List<String> tierList){
    	if(tierList != null){
    		nonEditableTiersList = tierList;
    		table.setNoneditableTiers(nonEditableTiersList);
    	}
    	
    }
    
    public void hideTiers(){    
    	int row = table.getCurrentRow();
		int column = table.getCurrentColumn();
		hideTiersLinkedWith(table.getTierName(row,column));	
    }
    
    /**
     * Hides all the tiers which are linked with the given tier 
     * 
     * @param tierName
     */
    public void hideTiersLinkedWith(String tierName){    
    	if(table.isEditing()){
			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
		}
    	
    	TierImpl linkedTier = (TierImpl) viewerManager.getTranscription().getTierWithId(tierName);
		if(linkedTier != null){		
    		List<TierImpl> tierList = null;
    		TierImpl keyObj;
    		Iterator<TierImpl> keyIt = tierMap.keySet().iterator();

    		while (keyIt.hasNext()) {
    			keyObj = keyIt.next();	
    			if(tierMap.get(keyObj) instanceof List){
    				tierList =  tierMap.get(keyObj);
    				if(tierList.contains(linkedTier)){
    					if(!hiddenTiersList.contains(keyObj.getName())){
    						hiddenTiersList.add(keyObj.getName());    						
    						loadTable();
    						break;
    					}
    				}
    			}
    		}
		}
    }
    
    /**
     * opens the select tiers dialog to show or hide more 
     */
    public void showHideMoreTiers(){
    	if(table.isEditing()){
			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
		}
    	SelectChildTiersDlg dialog = new SelectChildTiersDlg(layoutManager.getElanLayoutManager(), tierMap, hiddenTiersList, columnTypeList);
		dialog.setVisible(true);
		
		if(dialog.isValueChanged()){
			if( dialog.getHiddenTiers() != null){
				setHiddenTiersList( dialog.getHiddenTiers());
			}			
			setTierMap(dialog.getTierMap());
			loadTable();
		}
    }
    
    /**
     * 
     * @return
     */
    public Transcription getTranscription(){
    	return viewerManager.getTranscription();
    }
    
    public void setTierMap(HashMap<TierImpl,List<TierImpl>> map){
    	if(map != null){
    		this.tierMap = map;  
    	} else {
    		tierMap.clear();
    	}
    }
    
    public void setHiddenTiersList(List<String> list){
    	if(list != null){
    		this.hiddenTiersList = list;    		    		
    	} else {
    		hiddenTiersList.clear();
    	}
    }
    
    

    public HashMap<TierImpl, List<TierImpl>> getTierMap(){    	
    	return tierMap;
    }
    
    public List<String> getHiddenTiers(){    	
    	return this.hiddenTiersList;
    }
    
    /**
	 * Initialize the table
	 */
	private void initializeTable() {
		tableModel = new TranscriptionTableModel();	 
		table = new TranscriptionTable(); 
		table.setModel(tableModel);
	    table.setDefaultEditor(Object.class, new TranscriptionTableCellEditor(this)); 
	    table.setDefaultRenderer(Object.class, new TranscriptionTableCellRenderer(getTranscription()));
	    table.getColumnModel().getColumn(0).setMinWidth(40);
	    table.getColumnModel().getColumn(0).setPreferredWidth(40);
	    table.getColumnModel().getColumn(0).setMaxWidth(40);
	    
	    scroller = new JScrollPane(table);
	    
		table.addMouseListener(new MouseAdapter() {		    
	        public void mouseReleased(MouseEvent e) {	        			
	        	if ( javax.swing.SwingUtilities.isRightMouseButton(e)){	  
	        		
	        		if(popupMenu == null){
	        			createPopUpMenu();
	        		}
	        		int r = table.rowAtPoint(e.getPoint());
	        		int c = table.columnAtPoint(e.getPoint());		        		
	        		if(c==0){	        			
	        			nonEditableTierMI.setEnabled(false);
	        			changeColorMI.setEnabled(false);
	        			popupMenu.show(table, e.getX(), e.getY());
	        			popupMenu.setVisible(true);	        			        			
	        			return;
	        		} else if (table.getValueAt(r,c) instanceof TableSubHeaderObject){	
	        			if(table.isEditing()){
		        			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
		        		}
	        			table.changeSelection(r, c, false, false);
	        			String tierName = table.getTierName(r, c);
	            		if(tierName != null){
	            			nonEditableTierMI.setEnabled(true);
	            			changeColorMI.setEnabled(true);
	            			if(nonEditableTiersList.contains(tierName)){
	            				nonEditableTierMI.setText(ElanLocale.getString("TranscriptionTable.Label.EditableTier"));
	            			} else {
	            				nonEditableTierMI.setText(ElanLocale.getString("TranscriptionTable.Label.NonEditableTier"));
	            			}
	            			
	            			popupMenu.show(table, e.getX(), e.getY());
		        			popupMenu.setVisible(true);	        			        			
		        			return;
	            		}
	        		}
	        		
	     		if (r >= 0 && r < table.getRowCount()) {
	        			if(table.isEditing()){
		        			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
		        		}
	        			table.changeSelection(r, c, false, false);	
	        			table.startEdit(null);
			        	if(table.isEditing()){
			        		((TranscriptionTableCellEditor)table.getCellEditor()).showPopUp(table, e.getX(), e.getY());
			        	} else {
			        		String tierName = table.getTierName(r, c);
		            		if(tierName != null){
		            			nonEditableTierMI.setEnabled(true);
		            			if(nonEditableTiersList.contains(tierName)){
		            				nonEditableTierMI.setText(ElanLocale.getString("TranscriptionTable.Label.EditableTier"));
		            			} else {
		            				nonEditableTierMI.setText(ElanLocale.getString("TranscriptionTable.Label.NonEditableTier"));
		            			}
		            			
		            			popupMenu.show(table, e.getX(), e.getY());
			        			popupMenu.setVisible(true);	        			        			
			        			return;
		            		}
			        	}
		            }        	
	        	}
//	        	else if( javax.swing.SwingUtilities.isLeftMouseButton(e)){
//	        		int r = table.rowAtPoint(e.getPoint());
//	        		int c = table.columnAtPoint(e.getPoint());	
//	        		if(table.isEditing()){
//	        			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
//	        		}
//	        		table.changeSelection(r, c, false, false);	
//	        		table.startEdit(null);
//	        	}
	        }
		});	
		
//		table.addKeyListener(new KeyAdapter(){
//			 public void keyPressed(KeyEvent e) {
//				 
//				 int c = table.getCurrentColumn();     		
//		         if(c==0){	
//		        	return;
//		         }
//		         
//		         System.out.println(c);
//		        	
//				 KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);   
//			    	
//			     if(ks == null || !keyStrokesList.contains(ks)){  
//			    	return;
//			     }   			
//	        	
//	        	 int r = table.getCurrentRow();
//	        	
//	        	 if (table.getValueAt(r,c) instanceof TableSubHeaderObject){	
//	        			if(table.isEditing()){
//		        			((TranscriptionTableCellEditor)table.getCellEditor()).commitChanges();
//		        		}	        			
//	        			String tierName = table.getTierName(r, c);
//	            		if(tierName != null){
//	            			// make tier editable/ non- editable
//	       			     if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.FREEZE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE)) { 
//	       			    		editOrNoneditableTier(tierName);
//	       			    	} 
//	       			    	
//	       			        
//	       			        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.HIDE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
//	       			          	hideTiersLinkedWith(tierName);	
//	       			        }
//	            		}
//	        		}
//			 }
//		});
	    
	    scroller.addComponentListener(new ComponentAdapter() {			
			public void componentResized(ComponentEvent e) {	
				//table.reCalculateRowHeight();
				long selectionBeginTime = 0L;
				long selectionEndTime = 0L;				
				
				boolean playback = isAutoPlayBack();
				setAutoPlayBack(false);
				long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
				table.startEdit(null);
				table.scrollIfNeeded();
				setAutoPlayBack(playback);
				viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime);	
				
				if(viewerManager.getSelection() != null){
					selectionBeginTime = viewerManager.getSelection().getBeginTime();
					selectionEndTime = viewerManager.getSelection().getEndTime();
				}
				
				if(selectionBeginTime > 0L && selectionEndTime > 0L && signalViewer != null){
					//signalViewer.repaint();
					signalViewer.setSelection(selectionBeginTime, selectionEndTime);
					((SelectionPanel) layoutManager.getTranscriptionModePlayerController().getSelectionPanel()).setBegin(selectionBeginTime);
					((SelectionPanel) layoutManager.getTranscriptionModePlayerController().getSelectionPanel()).setEnd(selectionEndTime);
				}
			}
		});
	    	   
		setLayout(new BorderLayout());		
		add(scroller, BorderLayout.CENTER);	
	    table.getSelectionModel().addListSelectionListener(this);	
	    
	    setPreferredFontAndColorSettings();
	}
	
	private void commitTableChanges(){
		if(table.isEditing()){
			TranscriptionTableCellEditor editor = (TranscriptionTableCellEditor)table.getCellEditor(table.getCurrentRow(), table.getCurrentColumn());
			editor.commitChanges();
		}
	}
	
	
	private void reloadColumns(){	
		commitTableChanges();
		storeColumnOrder();
		storeColumnWidth();
		table.setStoreColumnOrder(false);
		int count = table.getColumnCount()-1;
		if( count != columnTypeList.size()){
			while(table.getColumnCount() > 1){
				table.removeColumn(table.getColumnModel().getColumn(table.getColumnCount()-1));
			}
		}		
		tableModel.updateModel(columnTypeList);
		table.setStoreColumnOrder(true);
	}
	
	/**set the preferred for the scroll pane
	 * 
	 * @param width
	 * @param height
	 */
	public void setScrollerSize(int width, int height) {
		scroller.setPreferredSize(new Dimension(width, height));		
	}
	
	/**
	 * Sets the preferred font and color setting for the tiers
	 *                         							
	 */
	private void setPreferredFontAndColorSettings(){			
		//preferred Font Color
		Preferences.set("TranscriptionMode.Temp.TierColors", null, viewerManager.getTranscription());
		
		Object colorMap = Preferences.get("TierColors", viewerManager.getTranscription());
		if (colorMap instanceof HashMap) {
			table.clearColorPrefernces();
			table.setFontColorForTiers((HashMap) colorMap);	
			
			if(tierMap != null){
				Iterator<List<TierImpl>> it = tierMap.values().iterator();
				List<TierImpl> tierList;
				while(it.hasNext()){
					tierList = it.next();
					if(tierList != null){
						for(int i=0; i < tierList.size(); i++){
							if(tierList.get(i) != null){
								checkColorForTier(tierList.get(i).getName());
							}
						}
					}
				}
			}
		}
		
		//preferred fonts
		Object fo = Preferences.get("TierFonts", viewerManager.getTranscription());
		if (fo instanceof HashMap) {
			table.setFontsForTiers((HashMap) fo);
		}
		
		table.repaint();
	}
	
	/**
	 * Sets the font size of the table
	 * 
	 * @param size
	 */
	public void setFontSize(Integer size) {
		table.setFont(new Font(table.getFont().getFontName(), table.getFont().getStyle(), size));
		table.reCalculateRowHeight();
	}
	
	/**
	 * Returns the current font size of the table
	 * @return
	 */
	public Integer getFontSize() {
		// TODO Auto-generated method stub
		return table.getFontSize();
	}
	
	/**
	 * Sets s flag, whether "enter" moves via column or not
	 * 
	 * @param selected, if true moves via column
	 * 		if false, moves by row in the current column
	 */
	public void moveViaColumn(boolean selected) {
		table.moveViaColumn(selected);			
	}
	
	/**
	 * Sets s flag, whether the editing cell should 
	 * always be in the center or not
	 * 
	 * @param selected, if true scrolls the current editing cell to the center of the table
	 * 		if false, has the default behaviour of the table
	 */
	public void scrollActiveCellInCenter(boolean selected) {
		table.scrollActiveCellInCenter(selected);
	}
	
	/**
	 * Sets whether the media should automatically
	 * played when start editing a cell
	 * 
	 * @param selected, if true, the autoplaymaode is set true
	 */
	public void setAutoPlayBack(boolean selected) {
		autoPlayBack = selected;		
	}
	
	public void autoCreateAnnotations(boolean create){
		table.setAutoCreateAnnotations(create);
	}
	
	public boolean isAnnotationsCreatedAutomatically(){
		return table.isAnnotationsCreatedAutomatically();
	}
	
	/**
	 * Returns whether the autoplayBack mode is on or off
	 * @return
	 */
	public boolean isAutoPlayBack() {
		return autoPlayBack;
	}
	
	public boolean isTierNamesShown() {
		return showTierNames;
	}
	
	public void showColorOnlyOnNoColumn(boolean selected) {
		((TranscriptionTableCellRenderer)table.getDefaultRenderer(Object.class)).showColorOnlyOnNoColumn(selected);
		if(!layoutManager.isInitialized() || table.getRowCount() <= 0){
			return;
		} 
		
		table.revalidate();
		table.repaint();
	}
	
	/**
	 * Method to show/ hide the tier names from the table
	 * 
	 * @param selected, if true, shows the tierNames in the table,
	 * 					if false, then the tierNames are hidden
	 */
	public void showTierNames(boolean selected) {
		showTierNames = selected;
		((TranscriptionTableCellRenderer)table.getDefaultRenderer(Object.class)).setShowTierNames(showTierNames);
		
		if(!layoutManager.isInitialized() || table.getRowCount() <= 0){
			return;
		}
		
		int currentRow = table.getCurrentRow();
		int currentColumn = table.getCurrentColumn();
		int annNumber = 0;
		boolean plaBack = isAutoPlayBack();	
		setAutoPlayBack(false);
		if(currentRow < 0 || currentRow >= table.getRowCount()){
			annNumber = 1;
		}else{
			Object val = table.getValueAt(currentRow, 0);
			if(val instanceof Integer){ 
				annNumber = ((Integer)val).intValue();
			}
		}		
		
		if(layoutManager.isInitialized()){
			loadTable();
		}
		
		if(showTierNames){
			int n = 0;
			for(int i=0; i < table.getRowCount(); i++){
				Object val = table.getValueAt(i, 0);
				if(val instanceof Integer){
					n = ((Integer)val).intValue();
					if(n == annNumber){						
						table.changeSelection(i, currentColumn, false, false);
						if(table.editCellAt(i, currentColumn)){
							table.startEdit(null);
							table.scrollIfNeeded();
						}
						break;
					}
				}
			}
		}else{			
			if(annNumber > 0 && (table.getRowCount()-1) >= (annNumber-1)){					
				table.changeSelection(annNumber-1, currentColumn, false, false);
				if(table.editCellAt(annNumber-1, currentColumn)){
					table.startEdit(null);	
					table.scrollIfNeeded();
				}
			}
		}
		
		table.scrollIfNeeded();
		setAutoPlayBack(plaBack);
	}
	
	
	public void setColumnTypeList(List<String> types){
		columnTypeList = types;
		reloadColumns();
	}
	
	public List<String> getColumnTypes(){
		return columnTypeList;
	}
		
	/**
	 * Checks whether merging of annotations is 
	 * possible for the selected type of parent
	 * tiers
	 */
	public void checkForMerge(){
		merge = false;
		if(columnTypeList != null && columnTypeList.size() > 0){
			Vector tierList = viewerManager.getTranscription().getTiersWithLinguisticType(columnTypeList.get(0));
			if(tierList != null && tierList.size() > 0){
				TierImpl tier = (TierImpl)tierList.get(0);
				if(tier.hasParentTier() && tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){						
					TierImpl parentTier = (TierImpl) tier.getParentTier();
					while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
						parentTier = (TierImpl) parentTier.getParentTier();
					}
					
					if(parentTier.getLinguisticType().getConstraints() == null){
						merge = true;							
					}	
				} else if(tier.getLinguisticType().getConstraints() == null){
					merge = true;
				}
			}
		}
	}			
	
	/**
	 * Focuses the table
	 */
	public void focusTable(){
		if(table.isEditing()){
			((TranscriptionTableCellEditor)table.getCellEditor()).getEditorComponent().grabFocus();
		}else{
			table.requestFocusInWindow();
		}
	}
	
	/**
	 * Return whether merging is possible of not
	 * 
	 * @return merge
	 */
	public boolean getMerge(){
		return merge;
	}
	
//	/**
//	 * Clears the table
//	 */
//	public void clearTable() {
//		table.getSelectionModel().removeListSelectionListener(this);
//		table.clearRows();
//		table.repaint();
//		table.getSelectionModel().addListSelectionListener(this);
//	}

	/**
	 * Switch on/off the loop mode
	 */
	public void toggleLoopMode() {
		layoutManager.getTranscriptionModePlayerController().toggleLoopMode();		
	}
	
	/**
	 * stores the column order in the map
	 */
	public void storeColumnOrder(){	
		
		if(!table.getStoreColumnOrder()){
			return;
		}
		
		if(table.getColumnCount()-1 != columnOrder.size()){
			columnOrder.clear();
		}

		for(int i=1; i< table.getColumnCount(); i++){			
			columnOrder.put(TranscriptionTableModel.COLUMN_PREFIX+getColumnNumber(i), i);
		}				
		setPreference("TranscriptionTable.ColumnOrder", columnOrder, viewerManager.getTranscription());
	}
	
	private int getColumnNumber(int column){		
		String currentColumn = table.getColumnName(column);		
		for(int i=1; i< table.getColumnCount(); i++){
			String  columnName= TranscriptionTableModel.COLUMN_PREFIX + i;
			if(currentColumn.startsWith(columnName)){
				column = i;
				break;
			}			
		}		
		return column;		
	}
	
	private void storeColumnWidth(){
		if(!table.getStoreColumnOrder()){
			return;
		}
		
		if(table.getColumnCount()-1 != columnWidth.size()){
			columnWidth.clear();
		}
		
		for(int i=1; i< table.getColumnCount(); i++){	
			columnWidth.put(TranscriptionTableModel.COLUMN_PREFIX+getColumnNumber(i), table.getColumnModel().getColumn(i).getWidth());
		}			
		setPreference("TranscriptionTable.ColumnWidth", columnWidth, viewerManager.getTranscription());
	}
	
	private void restoreColumnWidth(){		
		String[] columnNames = tableModel.getColumnIdentifiers();
		
		if(columnWidth.size() <= 0){
			return;
		}
		
		if((columnNames.length-1) != columnWidth.size()){
			columnWidth.clear();
			return;
		}
		
		for(int i=1; i< table.getColumnCount(); i++){	
			int columnIndex = table.getColumnModel().getColumnIndex(columnNames[i]);	
			int width = columnWidth.get(TranscriptionTableModel.COLUMN_PREFIX+i);
			if(width >=0 && columnIndex < table.getColumnCount()){
				table.getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
			}
		}	
	}
	
	private void restorePrefferedOrder(){		
		
		table.setStoreColumnOrder(false);
		String[] columnNames = tableModel.getColumnIdentifiers();
		
		if(columnOrder.size() <= 0){
			return;
		}
		
		if((columnNames.length-1) != columnOrder.size()){
			columnOrder.clear();
			return;
		}
		
		for(int i=1; i< table.getColumnCount(); i++){
			String columnName = columnNames[i];				
			int columnIndex = table.getColumnModel().getColumnIndex(columnName);	
			int targetColumnIndex = columnOrder.get(TranscriptionTableModel.COLUMN_PREFIX+i);
			if(targetColumnIndex >=0 && targetColumnIndex < table.getColumnCount()){
				table.moveColumn(columnIndex, targetColumnIndex);		
			}
		}	
		restoreColumnWidth();
		
		table.setStoreColumnOrder(true);
	}
	
	private List<TierImpl> getLinkedTiersOfType(TierImpl refTier, String type){
    	List childTiers = refTier.getChildTiers();
    	List<TierImpl> linkedTiers = new ArrayList<TierImpl>();
    	for(int i=0; i < childTiers.size(); i++){
			TierImpl childTier = (TierImpl) childTiers.get(i);
			if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
				if(childTier.getLinguisticType().getLinguisticTypeName().equals(type)){	
					linkedTiers.add(childTier);
				}
				// get all the types of the tiers depending on this child tier
				List dependentTiers = childTier.getDependentTiers();
				for(int y=0; y < dependentTiers.size(); y++) {
					TierImpl dependantTier = (TierImpl) dependentTiers.get(y);	
					if(dependantTier.getLinguisticType().getLinguisticTypeName().equals(type)){								
						linkedTiers.add(dependantTier);	
					}
				}
			}
		}
    	return linkedTiers;	   
    }
	
	/**
	 * load the table with values
	 */
	public void loadTable(){		
		commitTableChanges();
		table.getSelectionModel().removeListSelectionListener(this);
		table.clearRows();		
		
		List tiers  = new ArrayList();
		List <TierImpl> parentTierListType = new ArrayList<TierImpl>();	 	
		List annotationsList = new ArrayList();
		
		if(columnTypeList != null && columnTypeList.size() >= 1){
			tiers = viewerManager.getTranscription().getTiersWithLinguisticType(columnTypeList.get(0));	
		} else{
			return;
		}
		
		List<String> types = new ArrayList<String>();
		for(int i=0; i< columnTypeList.size(); i++){
			if(!types.contains(columnTypeList.get(i))){
				types.add(columnTypeList.get(i));
			}
		}
		
		if(tiers != null && tiers.size() > 0){
			TierImpl tierC1 = (TierImpl)tiers.get(0);	
			
			// if columnType1 is symbolic Associatedtype
	 		if(tierC1.getLinguisticType().getConstraints() != null && tierC1.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){ 
	 			for(int x = 0; x < tiers.size(); x++){						
					TierImpl tier = (TierImpl)tiers.get(x);	
					TierImpl parentTier = (TierImpl) tier.getParentTier();
					while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
						parentTier = (TierImpl) parentTier.getParentTier();
					}
					
					if(parentTierListType.contains(parentTier)){
						continue;
					} 
					
					if(hiddenTiersList.contains(parentTier.getName())){
						continue;
					}
					
					parentTierListType.add(parentTier);
					List<TierImpl> linkedTiers = tierMap.get(parentTier);
					List<TierImpl> matchedTiersType = new ArrayList<TierImpl>(); 					
					
					if(linkedTiers !=null){						
						if(linkedTiers.size() < columnTypeList.size()){
							for(int i= linkedTiers.size(); i < columnTypeList.size(); i++){
								linkedTiers.add(null);
							}
						} else if(linkedTiers.size() > columnTypeList.size()){
							for(int i= linkedTiers.size(); i > columnTypeList.size(); i--){
								linkedTiers.remove(i-1);
							}
						}
					} else {
						linkedTiers = new ArrayList<TierImpl>();		
						for(int i= 0; i< columnTypeList.size(); i++){
							linkedTiers.add(null);
						}
					}	
					
					for(int c=0; c< types.size() ; c++){
						matchedTiersType.clear();
						matchedTiersType.addAll(getLinkedTiersOfType(parentTier, types.get(c)));						
						
						
						if(types.size() != columnTypeList.size()){
							for(int i= c; i < columnTypeList.size();i++){
								if(columnTypeList.get(i).equals(types.get(c))){
									//int index = columnTypeList.indexOf(columnTypeList.get(i));									
									
									if(linkedTiers.get(i) == null || !matchedTiersType.contains(linkedTiers.get(i))){
										if(matchedTiersType.size() >=1){
											linkedTiers.set(i,matchedTiersType.get(0));
											matchedTiersType.remove(matchedTiersType.get(0));
										} else {
											linkedTiers.set(i,null);
										}
									}else if(matchedTiersType.contains(linkedTiers.get(i))){
										matchedTiersType.remove(linkedTiers.get(i));
									}
								}
							}						
						} else {
							int index = columnTypeList.indexOf(columnTypeList.get(c));
							if(linkedTiers.get(index) == null || !matchedTiersType.contains(linkedTiers.get(index))){
								if(matchedTiersType.size() >=1){
									linkedTiers.set(index,matchedTiersType.get(0));
									matchedTiersType.remove(matchedTiersType.get(0));
								} else {
									linkedTiers.set(index,null);
								}
							}else if(matchedTiersType.contains(linkedTiers.get(index))){
								matchedTiersType.remove(linkedTiers.get(index));
							}
						}
					}					
					tierMap.put(parentTier,linkedTiers);
	 			}
	 		} else {		
	 			
	 			List<TierImpl> parentTiers = new ArrayList<TierImpl>();
	 			for(int x = 0; x < tiers.size(); x++){
	 				TierImpl tier = (TierImpl)tiers.get(x);
	 				
	 				if(hiddenTiersList.contains(tier.getName())){
						continue;
					}
	 				
	 				parentTierListType.add(tier); 	
	 				
	 				List<TierImpl> linkedTiers = tierMap.get(tier);
					List<TierImpl> matchedTiersType = new ArrayList<TierImpl>(); 				
					
					if(linkedTiers !=null){						
						if(linkedTiers.size() < columnTypeList.size()){							
							for(int i= linkedTiers.size(); i < columnTypeList.size(); i++){
								linkedTiers.add(null);
							}
						}else if(linkedTiers.size() > columnTypeList.size())		{
							for(int i= linkedTiers.size(); i > columnTypeList.size(); i--){
								linkedTiers.remove(i-1);
							}
						}
					}else {
						linkedTiers = new ArrayList<TierImpl>();		
						for(int i= 0; i< columnTypeList.size(); i++){
							linkedTiers.add(null);
						}
					}	
					linkedTiers.set(0, tier);
					
					for(int c=1; c< types.size() ; c++){
						matchedTiersType.clear();
						matchedTiersType.addAll(getLinkedTiersOfType(tier, types.get(c)));
						
						if(types.size() != columnTypeList.size()){
							for(int i= c; i < columnTypeList.size();i++){
								if(columnTypeList.get(i).equals(types.get(c))){
									//int index = columnTypeList.indexOf(columnTypeList.get(i));									
									
									if(linkedTiers.get(i) == null || !matchedTiersType.contains(linkedTiers.get(i))){
										if(matchedTiersType.size() >=1){
											linkedTiers.set(i,matchedTiersType.get(0));
											matchedTiersType.remove(matchedTiersType.get(0));
										} else {
											linkedTiers.set(i,null);
										}
									} else if(matchedTiersType.contains(linkedTiers.get(i))){
										matchedTiersType.remove(linkedTiers.get(i));
									}
								}
							}						
						} else {
							int index = columnTypeList.indexOf(columnTypeList.get(c));
							if(linkedTiers.get(index) == null || !matchedTiersType.contains(linkedTiers.get(index))){
								if(matchedTiersType.size() >=1){
									linkedTiers.set(index,matchedTiersType.get(0));
									matchedTiersType.remove(matchedTiersType.get(0));
								} else {
									linkedTiers.set(index,null);
								}
							} else if(matchedTiersType.contains(linkedTiers.get(index))){
								matchedTiersType.remove(linkedTiers.get(index));
							}
						}
					}
					
					tierMap.put(tier,linkedTiers);
	 			}
	 		}
		}
		
		for(int x = 0; x < parentTierListType.size(); x++){
			TierImpl tier = (TierImpl)parentTierListType.get(x);	
			if(tier.getLinguisticType().getConstraints()== null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION){
				if(tierMap.get(tier) != null){
					annotationsList.addAll(tier.getAnnotations());
				}
			}
	 	}
		
	 	if(columnTypeList != null  && columnTypeList.size() >=1 && annotationsList.size() ==0 && hiddenTiersList.size() == 0){
	 		String message = ElanLocale.getString("TranscriptionManager.Message.NoSegments");
			JOptionPane.showMessageDialog(table, message,
			            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);	
			return;
	 	}
	 	
		AnnotationComparator annComparator = new AnnotationComparator();
		Collections.sort(annotationsList, annComparator);
		
		HashMap<AbstractAnnotation, List<Object>> annotationMap = new HashMap<AbstractAnnotation, List<Object>>();
		
		for(int i=0; i < annotationsList.size(); i++){	
			AbstractAnnotation ann = (AbstractAnnotation) annotationsList.get(i);			
			TierImpl parentTier = (TierImpl) ann.getTier();
			List<TierImpl> linkedTiers = tierMap.get(parentTier);
			
			List<Object> linkedAnn = new ArrayList<Object>();
			
			for (int y=0; y < linkedTiers.size(); y++){
				linkedAnn.add(getLinkedAnnotation(ann, linkedTiers.get(y)));
			}
			annotationMap.put(ann, linkedAnn);
		}
			
		int n = 0;
		String tierName =  null;
		String parentTierName =  null;
		
		int rowIndex = -1;
		
		// loads all the annotations in the table
		TranscriptionTableModel tableModel = (TranscriptionTableModel) table.getModel();		
		for(int i=0; i < annotationsList.size(); i++){	
			Annotation ann = (Annotation) annotationsList.get(i);			
			
			List<Object> objList = annotationMap.get(ann);
			List<TierImpl> tierList = tierMap.get(ann.getTier());
			
			if(showTierNames){
				if(i == 0){
					parentTierName = ann.getTier().getName();
					tableModel.addRow(new Object[]{new TableSubHeaderObject("")});	
					rowIndex++;										
				} else if(!parentTierName.equals(ann.getTier().getName())){							
					tableModel.addRow(new Object[]{new TableSubHeaderObject("")});	
					rowIndex++;
				}
			}
			
			n++;
			tableModel.addRow(new Object[] {n});
			rowIndex++;
			
			for(int x=0; x < objList.size(); x++){
				Object obj = objList.get(x);	
				TierImpl tier = tierList.get(x); 				
				
				if(tier != null){				
					tierName = tier.getName();		
					checkColorForTier(tierName);				
				} else{
					tierName = null;
				}
				
				if(showTierNames){
					if(i == 0){
						parentTierName = ann.getTier().getName();
						tableModel.setValueAt(new TableSubHeaderObject(tierName), rowIndex-1, x+1);								
					} else if(!parentTierName.equals(ann.getTier().getName())){		
						tableModel.setValueAt(new TableSubHeaderObject(tierName), rowIndex-1, x+1);	
					}
				}
				
				tableModel.setValueAt(obj,rowIndex, x+1);
			}			
			parentTierName = ann.getTier().getName();			
		}
		
		// sets the width of the column "No" according to the number of annotations
		if(n > 999){
			table.getColumnModel().getColumn(0).setMaxWidth(50);	 
			table.getColumnModel().getColumn(0).setMinWidth(50);
		}else if( n > 99){
			table.getColumnModel().getColumn(0).setMaxWidth(45);	
			table.getColumnModel().getColumn(0).setMinWidth(45);
		} else if(n < 100){
			table.getColumnModel().getColumn(0).setMinWidth(40);
			table.getColumnModel().getColumn(0).setMaxWidth(40);
		}
		
		restorePrefferedOrder();

		table.reCalculateRowHeight();		
		table.revalidate();
		table.requestFocusInWindow();
		table.getSelectionModel().addListSelectionListener(this);
		
		table.setStoreColumnOrder(true);
	} 
	
	private Object getLinkedAnnotation(AbstractAnnotation refAnnotation, TierImpl tier){
		Annotation annotation = null;
		if(tier != null){
			if(tier.hasParentTier() && tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){		
				TierImpl refTier = (TierImpl) refAnnotation.getTier();		
				List childAnnotations = refAnnotation.getParentListeners();	
				String create_Ann =  CREATE_ANN + ";" + tier.getName()+";"+ refAnnotation.getBeginTimeBoundary() + " : " + refAnnotation.getEndTimeBoundary();	
				TierImpl parentTier = (TierImpl) tier.getParentTier();
				if(parentTier != refTier){
					AbstractAnnotation parentAnn = (AbstractAnnotation) parentTier.getAnnotationAtTime(refAnnotation.getBeginTimeBoundary());
					if(parentAnn != null){
						childAnnotations = parentAnn.getParentListeners();	
						for(int j=0; j< childAnnotations.size();j++){
							if(((Annotation)childAnnotations.get(j)).getTier() == tier){
								annotation = (Annotation)childAnnotations.get(j);							
								break;
							}
						}						
					} else {					
						return null;
					}
				} else {
					for(int j=0; j< childAnnotations.size();j++){
						if(((Annotation)childAnnotations.get(j)).getTier() == tier){
							annotation = (Annotation)childAnnotations.get(j);							
							break;
						}
					}
				}				
				
				if(annotation == null){
					return create_Ann;
				}else {
					return annotation;					
				}
			} else {
				return refAnnotation;
			}
		} else {
			return null;
		}
	}
	/**
	 * Checks if there is a preferred color for the give tier.
	 * Else calls the setColorForTier method
	 * 
	 * @param tierName, tier to be checked
	 */	
	private void checkColorForTier(String tierName){
		Color c = table.getFontColorForTier(tierName);
		if(c == null){
			TierImpl tier = (TierImpl) viewerManager.getTranscription().getTierWithId(tierName);
			if(tier.hasParentTier()){
				TierImpl parentTier = (TierImpl) tier.getParentTier();
				c = table.getFontColorForTier(parentTier.getName());
				if( c==null ){
					setColorForTier(parentTier.getName());
				}else {
					HashMap map = new HashMap();
					map.put(tierName, c);
					table.setFontColorForTiers(map);
				}
			} else {
				setColorForTier(tierName);
			}
		} else {
			tierColorsList.add(c);
		}
	}
	
	/**
	 * Sets a random color for the given Tier
	 * 
	 * @param tierName
	 */
	private void setColorForTier(String tierName) {		
		while(true){
			int r = (int)(Math.random()*255);
			int g = (int)(Math.random()*255);
			int b = (int)(Math.random()*255);
			 
			Color c = new Color(r,g,b);
			if(c == Color.BLACK || c == Color.WHITE ||
					c == new Color(238,238,238) || c == TranscriptionTableCellRenderer.NO_ANN_BG){
				continue;
			}
			
			if(!tierColorsList.contains(c)){
				tierColorsList.add(c);
				HashMap map = new HashMap();
				map.put(tierName, c);
				table.setFontColorForTiers(map);
				break;
			}
		}
	}
	
	public void updateSignalViewer(SignalViewer viewer){		
		if(viewer != null){
			signalViewer = viewer;
			signalViewer.setRecalculateInterval(false);
			if(table.isEditing()){
				signalViewer.setEnabled(true);
				Object obj = table.getValueAt(table.getCurrentRow(), table.getCurrentColumn());
				if(obj instanceof Annotation){
					long begin = ((Annotation)obj).getBeginTimeBoundary();
					long end = ((Annotation)obj).getEndTimeBoundary();	
					updateMedia(begin, end);
				}
			}
		}
		
	}
	
	/**
	 * Updates the signal viewer, the players and the 
	 *
	 * 
	 * @param begin
	 * @param end
	 */
	public void updateMedia(long begin, long end){	
		
		clearSelection();
		
		if(viewerManager.getMasterMediaPlayer() !=null){ 
			if(layoutManager.getTranscriptionModePlayerController().getLoopMode()){
				layoutManager.getTranscriptionModePlayerController().stopLoop();
			}			
			
			if( viewerManager.getMasterMediaPlayer().isPlaying()){
				viewerManager.getMasterMediaPlayer().stop();
			}
			viewerManager.getMasterMediaPlayer().setMediaTime(begin);
		}	
		
		// updates the signal viewer to show the wave signal for the given time interval
		if(signalViewer != null){			
			signalViewer.updateInterval(begin,end);
		}	
		
		((SelectionPanel) layoutManager.getTranscriptionModePlayerController().getSelectionPanel()).setBegin(begin);
		((SelectionPanel) layoutManager.getTranscriptionModePlayerController().getSelectionPanel()).setEnd(end);
	}	

	/**
	 * Start playing the media
	 */
	public void playMedia() {	
		if(table.isEditing() ){
			table.getEditorComponent().requestFocusInWindow();
			Annotation ann = (Annotation) table.getValueAt(table.getEditingRow(),table.getEditingColumn());	
			playInterval(ann.getBeginTimeBoundary(),ann.getEndTimeBoundary());		
					
		}else{
			//table.startEdit();
			if(table.getSelectedRow() > -1 && table.getSelectedColumn() > -1){
				Annotation ann = (Annotation) table.getValueAt(table.getSelectedRow(),table.getSelectedColumn());
				updateMedia(ann.getBeginTimeBoundary(),ann.getEndTimeBoundary());		
				playInterval(ann.getBeginTimeBoundary(),ann.getEndTimeBoundary());	
			}
			table.requestFocusInWindow();
		}
	}
	
	/**
	 * Start playing the media
	 */
	public void playSelection() {	
		Selection sel = viewerManager.getSelection();
		if( sel != null ){
			table.getEditorComponent().requestFocusInWindow();			
			playInterval(sel.getBeginTime(), sel.getEndTime());	
		}
	}
	
	private boolean isValidMediaTime(long beginTime, long endTime){		
		long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
    	if(mediaTime < beginTime){
    		return false;
    	}
    	
    	if(mediaTime > endTime){
    		return false;
    	}
    	
    	return true;
	}
	
	public void goToOnepixelForwardOrBackward(String commandName, long beginTime, long endTime){		
		long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
		
		Command c = ELANCommandFactory.createCommand(viewerManager.getTranscription(), commandName);	
		Object[] args = new Object[1];
        args[0] = viewerManager.getTimeScale();
    	c.execute(viewerManager.getMasterMediaPlayer(), args);   
    	
    	if(!isValidMediaTime(beginTime, endTime)){
    		viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime);
    	}
    	
    	
	}
	
	public void goToPreviousOrNextFrame(String commandName, long beginTime, long endTime){	
		long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
		Command c = ELANCommandFactory.createCommand(viewerManager.getTranscription(), commandName);
    	c.execute(viewerManager.getMasterMediaPlayer(), null);   
    	
    	if(!isValidMediaTime(beginTime, endTime)){
    		viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime);
    	}
	}
	
	public void goToOneSecondForwardOrBackward(String commandName, long beginTime, long endTime){	
		long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
		
		Command c = ELANCommandFactory.createCommand(viewerManager.getTranscription(), commandName);		
    	c.execute(viewerManager.getMasterMediaPlayer(), null);
    	
    	if(!isValidMediaTime(beginTime, endTime)){
    		viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime);
    	}
	}
	
	public void playAroundSelection(long beginTime, long endTime){		
		
		Selection s = viewerManager.getSelection();		
		
		if( s != null ){
			long selBeginTime = s.getBeginTime();
			long selEndTime = s.getEndTime();
			boolean timeChanged = false;
			if(s.getBeginTime() != s.getEndTime()){				
				if(selBeginTime < beginTime){
					selBeginTime = beginTime;
					timeChanged = true;
				}
				
				if(selEndTime > endTime){
					selBeginTime = endTime;
					timeChanged = true;
				}
				
				if(timeChanged){
					s.setSelection(selBeginTime, selEndTime);
				}
				
				if(selBeginTime < selEndTime && selBeginTime >= beginTime  && selEndTime <= endTime){	
					beginTime = selBeginTime;
					endTime = selEndTime;
				}
			}
		} else {
			//clearSelection();
		}
				
		
		beginTime = beginTime - playAroundSelection;
		if(beginTime < 0){
			beginTime = 0;
		}
		
		endTime = endTime+ playAroundSelection;
		if(endTime > viewerManager.getMasterMediaPlayer().getMediaDuration()){
			endTime = viewerManager.getMasterMediaPlayer().getMediaDuration();
		}
		playInterval(beginTime, endTime);	
		
	}
	
	public void playIntervalFromBeginTime(long beginTime, long endTime){
		if(!layoutManager.isInitialized()){
			return;
		}		
			
		if (viewerManager.getMasterMediaPlayer() == null) {
	       return;
	    }
    	 viewerManager.getMasterMediaPlayer().playInterval(beginTime, endTime);		
	}
	
	public void stopPlayer() {
        if (viewerManager.getMasterMediaPlayer() == null &&
        		viewerManager.getMasterMediaPlayer().isPlaying()) {
            return;
        }

        viewerManager.getMasterMediaPlayer().stop();
        layoutManager.getTranscriptionModePlayerController().stopLoop();
    }
	
	/**
	 * Plays the media for the given interval
	 * 
	 * @param beginTime, the start time of the media
	 * @param endTime, the end time of the media
	 */
	public void playInterval(long beginTime, long endTime){						
		
		if(!layoutManager.isInitialized()){
			return;
		}		
			
		if (viewerManager.getMasterMediaPlayer() == null) {
	       return;
	    }
		boolean isPlaying = viewerManager.getMasterMediaPlayer().isPlaying();
			
	    //stop if a player is being played
	    if (isPlaying) {
	    	viewerManager.getMasterMediaPlayer().stop();	
	    	layoutManager.getTranscriptionModePlayerController().stopLoop();
	    	return;
	     }	    
	    
	     long mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();	
	     
	     
	     //if not playing, start playing
	     if (!isPlaying  && (mediaTime > beginTime) &&
	                (mediaTime < endTime-5)) {
	    	 viewerManager.getMasterMediaPlayer().playInterval(mediaTime, endTime);
	    	 if (layoutManager.getTranscriptionModePlayerController().getLoopMode()) {		    		 
	    		 delayedStartLoop(beginTime, endTime);
		     }
	    	 return;
	      }
	     
	     if (layoutManager.getTranscriptionModePlayerController().getLoopMode()) {	
	    	 layoutManager.getTranscriptionModePlayerController().startLoop(beginTime, endTime);
	     } else {	        	
	    	 viewerManager.getMasterMediaPlayer().playInterval(beginTime, endTime);
	     }
	}	
	
	public void clearSelection(){
		Selection sel = viewerManager.getSelection();
		if(sel != null && (sel.getBeginTime() != 0 || sel.getEndTime() != 0)){			
			sel.clear();
			if (signalViewer != null) {
				signalViewer.updateSelection();				
			}
		}
	}
	
	/**
	 * Merge the given annotation with the annotation
	 * before it
	 * 
	 * @param annotation, the annotation to be merged
	 */
	void mergeBeforeAnn(Annotation annotation){
		if(viewerManager.getMasterMediaPlayer()!= null && viewerManager.getMasterMediaPlayer().isPlaying()){
			viewerManager.getMasterMediaPlayer().stop();
		}
		
		setPreference("TranscriptionTable.LastActiveRow", table.getCurrentRow()-1, viewerManager.getTranscription());
		setPreference("TranscriptionTable.LastActiveColumn", table.getColumnName(table.getCurrentColumn()), viewerManager.getTranscription());
   		
   		Transcription transcription = (Transcription)annotation.getTier().getParent();   		
    	Command c = ELANCommandFactory.createCommand(transcription,ELANCommandFactory.MERGE_ANNOTATION_WB);    	
    	Object[] args = new Object[] { annotation, false };
    	c.execute(transcription, args);
	}
   		
	/**
	 * Merge the given annotation with the annotation
	 * next to it
	 * 
	 * @param annotation, the annotation to be merged
	 */	
	void mergeNextAnn(Annotation annotation){
		if(viewerManager.getMasterMediaPlayer()!= null && viewerManager.getMasterMediaPlayer().isPlaying()){
			viewerManager.getMasterMediaPlayer().stop();
		}	
		setPreference("TranscriptionTable.LastActiveRow", table.getCurrentRow(), viewerManager.getTranscription());
		setPreference("TranscriptionTable.LastActiveColumn", table.getColumnName(table.getCurrentColumn()), viewerManager.getTranscription());
		
   		Command c = ELANCommandFactory.createCommand(viewerManager.getTranscription(),ELANCommandFactory.MERGE_ANNOTATION_WN);    	
    	Object[] args = new Object[] { annotation, true };
    	c.execute(viewerManager.getTranscription(), args); 
	}
	
	/**
	 * Deletes the given annotation
	 *
	 * @param annotation, the annotation to be deleted
	 */	
	void deleteAnnotation(Annotation annotation){
		
		if(viewerManager.getMasterMediaPlayer()!= null && viewerManager.getMasterMediaPlayer().isPlaying()){
			viewerManager.getMasterMediaPlayer().stop();
		}
		
		int leadRow = table.getSelectionModel().getLeadSelectionIndex();
   		int leadColumn = table.getColumnModel().getSelectionModel().
	                   getLeadSelectionIndex();
   		
   		if(table.getValueAt(leadRow, leadColumn) instanceof TableSubHeaderObject){
   			return;
   		}
   		
   		Transcription transcription = (Transcription)annotation.getTier().getParent();  
   		Command c = ELANCommandFactory.createCommand(transcription,ELANCommandFactory.DELETE_ANNOTATION);    	
    	Object[] args = new Object[] { viewerManager, annotation};
    	c.execute((TierImpl) annotation.getTier(), args);  
	}
	
	/**
	 * Tries to select the last active cell
	 */
	public void updateTable(){
		Integer lastActiveRow = (Integer) Preferences.get("TranscriptionTable.LastActiveRow", viewerManager.getTranscription());
		String lastActiveColumnName = (String) Preferences.get("TranscriptionTable.LastActiveColumn", viewerManager.getTranscription());
		int lastActiveColumn = -1;
		
		if(lastActiveRow != null && lastActiveColumnName !=null){
			int i = tableModel.findColumn(lastActiveColumnName);
			if(i>=0){			
				lastActiveColumn = table.getColumnModel().getColumnIndex(lastActiveColumnName);	
			}
			if(lastActiveRow > -1 && lastActiveColumn > -1){
				table.changeSelection(lastActiveRow, lastActiveColumn, false, false);
				table.scrollIfNeeded();
			}
		}		
		//table.scrollIfNeeded();
	}
	
	public void valueChanged(ListSelectionEvent e) {
		int currentRow = table.getSelectionModel().getLeadSelectionIndex();
		int currentColumn = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();

		setPreference("TranscriptionTable.LastActiveRow", currentRow, viewerManager.getTranscription());
		setPreference("TranscriptionTable.LastActiveColumn", table.getColumnName(currentColumn), viewerManager.getTranscription());
			
		if(currentRow > -1 && currentColumn > -1){
			Object obj = null;
			if(table.getRowCount() > currentRow){
				obj =  table.getValueAt(currentRow, currentColumn);
			}
			
			if(obj instanceof Annotation || obj instanceof String ){
				layoutManager.getTranscriptionModePlayerController().enableButtons(true);	
				if(signalViewer  != null){
					//SignalViewer viewer = signalViewer;
					signalViewer.setEnabled(true);					
				}
			} else{				
				layoutManager.getTranscriptionModePlayerController().enableButtons(false);	
				if(signalViewer  != null){
					signalViewer.setEnabled(false);
				}
				updateMedia(0L,0L);
			}
		}
	}
	
	public void reValidateTable(){
		table.repaint();
	}
	
	public void loadPreferences(){		
		HashMap<String, Integer> newColumnOrder = (HashMap<String, Integer>) Preferences.get("TranscriptionTable.ColumnOrder", viewerManager.getTranscription());
		if(newColumnOrder != null){			
				columnOrder = newColumnOrder;			
		}
		
		HashMap<String, Integer> newColumnWidth = (HashMap<String, Integer>) Preferences.get("TranscriptionTable.ColumnWidth", viewerManager.getTranscription());
		if(newColumnWidth != null){			
			columnWidth = newColumnWidth;			
		}	
	}
	
	
	
//	private void updatePopUpShortCuts(){
//		if(popupMenu == null){
//			return;
//		}
//		final String modeName = ELANCommandFactory.TRANSCRIPTION_MODE;	
//		nonEditableTierMI.setAccelerator(ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.FREEZE_TIER, modeName));	
//		hideAllTiersMI.setAccelerator(ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.HIDE_TIER, modeName));	
//	}
	
	public List<KeyStroke> getKeyStrokeList(){
		return keyStrokesList;
	}
	
	public void shortcutsChanged() {	
		keyStrokesList.clear();
	    Iterator it = ShortcutsUtil.getInstance().getShortcutKeysOnlyIn(ELANCommandFactory.TRANSCRIPTION_MODE).values().iterator();
	    KeyStroke ks = null;
	    while(it.hasNext()){
	    	ks = (KeyStroke) it.next();
	      	if(ks != null){
	       		keyStrokesList.add(ks);
	       	}
	    }
		
		//updatePopUpShortCuts();
		TranscriptionTableCellEditor editor = (TranscriptionTableCellEditor) table.getDefaultEditor(Object.class);
		if(editor != null){
			TranscriptionTableEditBox editBox = editor.getEditorComponent();
			if(editBox != null){
				editBox.updateShortCuts();
			}
		}
	}	
	
	public void isClosing(){
		if(table.isEditing()){	
			TranscriptionTableCellEditor editor = (TranscriptionTableCellEditor)table.getCellEditor(table.getCurrentRow(), table.getCurrentColumn());
    		editor.commitChanges();
		}
		storePreferences();
	}
	
	private void storePreferences() {			
		storeColumnWidth();
		setPreference("TranscriptionTable.ColumnTypes", columnTypeList, viewerManager.getTranscription());	
		setPreference("TranscriptionMode.Temp.TierColors", table.getFontColorTierMap(), viewerManager.getTranscription());	
		setPreference("TranscriptionTable.TierMap", changeToStorableMap(tierMap), viewerManager.getTranscription());
		setPreference("TranscriptionTable.HiddenTiers", hiddenTiersList, viewerManager.getTranscription());
		setPreference("TranscriptionTable.NonEditableTiers", nonEditableTiersList, viewerManager.getTranscription());
	}	
	
	/**
     * Changes the format of the given map to a 
     * new format such that the map can be stored by the 
     * PrefernceWriter
     * 
     * @param map, map which is can be stored
     * @return
     */
    private HashMap<String, List<String>> changeToStorableMap(HashMap<TierImpl, List<TierImpl>> map){
    	if(map != null){
    		HashMap<String, List<String>> newMap = new HashMap();    
    		List<TierImpl> tierList = null;
    		List<String> tierNamesList = null;
    		
    		TierImpl keyObj;
    		Iterator<TierImpl> keyIt = map.keySet().iterator();

    		while (keyIt.hasNext()) {
    			keyObj = keyIt.next();
    			
    			tierList = map.get(keyObj);
    			tierNamesList = new ArrayList<String>();
    			
    			for(int i=0; i< tierList.size(); i++){
    				TierImpl tier = tierList.get(i);
    				if(tier == null){
    					tierNamesList.add("No tier");    					
    				} else {
    					tierNamesList.add(tier.getName());    	
    				}
    			}    			
    			newMap.put(keyObj.getName(), tierNamesList);
    		}
    		return newMap;
    	}    	
    	return null;
    }
	
	public void controllerUpdate(ControllerEvent event) {
		 if (event instanceof StopEvent) {
			 layoutManager.getTranscriptionModePlayerController().setPlayPauseButton(true);
	     }

	     if (event instanceof StartEvent) {
	    	 layoutManager.getTranscriptionModePlayerController().setPlayPauseButton(false);
	    }
	}
	
	private void validateColumns(){		
		TierImpl keyObj;
		List<TierImpl>tierList;		
		ArrayList<Integer> nullValueIndex = new ArrayList<Integer>();
		Iterator<List<TierImpl>> it;
		
		for(int i=0; i < columnTypeList.size(); i++){
			int numberofnullValues = 0;
			it = tierMap.values().iterator();	
			while(it.hasNext()){				
				if(it.next().get(i) == null){
					numberofnullValues = numberofnullValues+1;
				}
			}
			
			if(numberofnullValues == tierMap.size()){
				nullValueIndex.add(i);
			}
		}
		
		List<String> types = new ArrayList<String>();
		types.addAll(columnTypeList);
		for(int i = (nullValueIndex.size()-1) ; i >= 0; i--){
			int index = nullValueIndex.get(i);
			types.remove(index);
			it = tierMap.values().iterator();	
			while(it.hasNext()){				
				it.next().remove(index);				
			}
		}
		
		int columnwidth0 = table.getColumnModel().getColumn(0).getPreferredWidth();
		System.out.println("columnwidth0 :" + columnwidth0);
				
		if(nullValueIndex.size() > 0){
			setColumnTypeList(types);
			it = tierMap.values().iterator();	
			while(it.hasNext()){	
				tierList = it.next();
				for(int i = (nullValueIndex.size()-1) ; i >= 0; i--){
					tierList.remove(nullValueIndex.get(i));
				}
			}
			
			table.getColumnModel().getColumn(0).setMinWidth(columnwidth0);
			table.getColumnModel().getColumn(0).setMaxWidth(columnwidth0);			
			table.repaint();
		}
	}
	
	public void ACMEdited(ACMEditEvent e) {	
		int currentRow = table.getCurrentRow();
		int currentColumn = table.getCurrentColumn();
		switch (e.getOperation()){		
			case ACMEditEvent.CHANGE_ANNOTATION_VALUE:					
				boolean inEditMode = table.isEditing();
				if(((TranscriptionTableCellEditor)table.getCellEditor()).getEditorComponent() != null){
					if(((TranscriptionTableCellEditor)table.getCellEditor()).getEditorComponent().isCommitChanges()){						
						return;
					}
				}
				
				loadTable();
				if(currentRow < table.getRowCount()){
					table.changeSelection(currentRow, currentColumn, false, false);			
				} else {
					table.changeSelection(table.getRowCount()-1, currentColumn, false, false);	
				}				
				
				if(inEditMode)				
					table.startEdit(null);				
				break;				
			case ACMEditEvent.ADD_ANNOTATION_HERE:	
			case ACMEditEvent.CHANGE_ANNOTATIONS:
			case ACMEditEvent.REMOVE_ANNOTATION:
				loadTable();
				if(currentRow < table.getRowCount()){
					table.changeSelection(currentRow, currentColumn, false, false);			
				} else {
					table.changeSelection(table.getRowCount()-1, currentColumn, false, false);			
					
				}
				//table.startEdit(null);
			break;
				
			case ACMEditEvent.ADD_TIER:	
				setPreferredFontAndColorSettings();
				//table.repaint();
				Object obj  = e.getModification();
				if(obj instanceof TierImpl){
					TierImpl tier = (TierImpl) obj;
					String type = tier.getLinguisticType().getLinguisticTypeName();
					if(columnTypeList != null && this.columnTypeList.contains(type)){
						loadTable();
					}	
				}	
				break;	
			
			case ACMEditEvent.REMOVE_TIER:	
				obj  = e.getModification();
				if(obj instanceof TierImpl){
					TierImpl tier = (TierImpl) obj;
					String type = tier.getLinguisticType().getLinguisticTypeName();
					if(columnTypeList != null && this.columnTypeList.contains(type)){
						TierImpl keyObj;
						Iterator<TierImpl> keyIt = tierMap.keySet().iterator();
			    		while (keyIt.hasNext()) {
			    			keyObj = keyIt.next();			    			
			    			List<TierImpl>tierList = tierMap.get(keyObj);
			    			if( tierList.contains(tier)){
			    				tierList.set(tierList.indexOf(tier), null);
								loadTable();		
								validateColumns();
								if(currentRow < table.getRowCount()){
									table.changeSelection(currentRow, currentColumn, false, false);			
								} else {
									table.changeSelection(table.getRowCount()-1, currentColumn, false, false);
								}
								break;
							}
						}
					}						
				}	
				break;	

			case ACMEditEvent.CHANGE_TIER:	
				obj  = e.getSource();			
				if(obj instanceof TierImpl){
					setPreferredFontAndColorSettings();
					
					TierImpl tier = (TierImpl) obj;
					List tiers = viewerManager.getTranscription().getTiers();
					if(tiers.contains(tier)){							
						TierImpl keyObj;
						List<TierImpl>tierList;
						Iterator<TierImpl> keyIt = tierMap.keySet().iterator();			
						
						boolean changesApplied = false;
						
						//check if tier is in the table
						while (keyIt.hasNext()) {
							tierList = tierMap.get(keyIt.next());								
							if(tierList != null && tierList.contains(tier)){								
								loadTable();
								validateColumns();
								changesApplied = true;
								if(currentRow < table.getRowCount()){
									table.changeSelection(currentRow, currentColumn, false, false);			
								} else {
									table.changeSelection(table.getRowCount()-1, currentColumn, false, false);
								}
								System.out.println("tier changed");
								break;
							} 
						}
						
						if(!changesApplied){						
							//if tier is not in the table
							keyIt = tierMap.keySet().iterator();
							TierImpl parentTier = (TierImpl) tier.getParentTier();
							while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
								parentTier = (TierImpl) parentTier.getParentTier();
							}
							// check if its parent tier is in the table
							if(parentTier != null){
								while (keyIt.hasNext()) {
									keyObj = keyIt.next();
									if(keyObj == parentTier){										
										loadTable();
										validateColumns();
										if(currentRow < table.getRowCount()){
											table.changeSelection(currentRow, currentColumn, false, false);			
										} else {
											table.changeSelection(table.getRowCount()-1, currentColumn, false, false);
										}
										System.out.println("parent tier is in the table");
										break;
									}
								}
							} else {
								// check if its type is loaded in the table
								String type = tier.getLinguisticType().getLinguisticTypeName();
								if(this.columnTypeList!= null && this.columnTypeList.contains(type)){									
									loadTable();
									validateColumns();
									if(currentRow < table.getRowCount()){
										table.changeSelection(currentRow, currentColumn, false, false);			
									} else {
										table.changeSelection(table.getRowCount()-1, currentColumn, false, false);
									}
									System.out.println("type is in the table");
									break;
								}
							}
						}
					}
				}
				break;
			
			case ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY:
				obj = e.getModification();
				if(obj instanceof ControlledVocabulary){
					ControlledVocabulary cv = (ControlledVocabulary) obj;
					if(table.isEditing()){
						if(((TranscriptionTableCellEditor)table.getCellEditor()).getEditorComponent().isUsingControlledVocabulary()){
							((TranscriptionTableCellEditor)table.getCellEditor()).getEditorComponent().cvChanged(cv);
						}
					}
				}
				break;
		}							
	}
	
	/**
	 * Updates the selection.
	 */
	public void updateSelection() {
		Selection s = viewerManager.getSelection();
		
		if(table != null){
			if(table.isEditing()){	
				Annotation ann = (Annotation) table.getValueAt(table.getEditingRow(), table.getEditingColumn());
				long beginTime = ann.getBeginTimeBoundary();
				long endTime = ann.getEndTimeBoundary();				
		    
				if( s != null && s.getBeginTime() != s.getEndTime() ){
					if(s.getBeginTime() >= beginTime  && s.getEndTime() <= endTime){
						return;
					} 			
					if(s.getBeginTime() >= beginTime){
						if(signalViewer  != null){		
							signalViewer .setSelection(s.getBeginTime(), endTime);
							signalViewer .repaint();
						}
						return;
					} else if(s.getEndTime() <= endTime){
						if(signalViewer  != null){		
							signalViewer .setSelection(beginTime, s.getEndTime());
							signalViewer .repaint();
						}
						return;
					}
				}
			} else {
				table.requestFocusInWindow();
			}
		} 
	}

	
	public void updateActiveAnnotation() {		
	}
	
	public void editInAnnotationMode(){
		layoutManager.editInAnnotationMode();
	}

	public void setActiveAnnotation() {
		if(table.getCurrentRow() > 0 && table.getCurrentColumn() > 0){
			Object val = table.getValueAt(table.getCurrentRow(), table.getCurrentColumn());
			if(val instanceof Annotation){
				TierImpl tier = (TierImpl) ((Annotation)val).getTier();
				if(tier.getLinguisticType().getConstraints() !=null && tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					Annotation parentAnn = ((Annotation)val).getParentAnnotation();
					if(parentAnn != null){
						setActiveAnnotation(parentAnn);
					}else {
						setActiveAnnotation((Annotation)val);
					}
				} else {
					setActiveAnnotation((Annotation)val);
				}
			}
		}		
	}

	public void updateLocale() {
	}
	
	public void preferencesChanged() {	
		// update values in play around selection actions
        Object val = Preferences.get("PlayAroundSelection.Mode", null);
        boolean msMode = true;
        if (val instanceof String) {
        	if ("frames".equals((String)val)) {
        		msMode = false;
        	}
        }
        val = Preferences.get("PlayAroundSelection.Value", null);
        if (val instanceof Integer) {
        	int playaroundVal = ((Integer) val).intValue();
        	if (!msMode) {
        		playaroundVal *= viewerManager.getMasterMediaPlayer().getMilliSecondsPerSample();
        		playAroundSelection =  playaroundVal;
        	}
        }
	}	
	
	private void delayedStartLoop(long begin, long end) {
        LoopThread loopthread = new LoopThread(begin, end);
        loopthread.start();
    }

    /**
     * Calls the media player controllers startloop method after a first, partial selection playback has finished.
     */
    private class LoopThread extends Thread {
    	
    	 private long beginTime;
         private long endTime;
         /**
          * Creates a new LoopThread instance
          *
          * @param begin the interval begin time
          * @param end the interval endtime
          */
         LoopThread(long begin, long end) {
             this.beginTime = begin;
             this.endTime = end;
             setName("delayed Loop Thread");
         }

        /**
         * DOCUMENT ME!
         */
        public void run() {
            if ( layoutManager.getTranscriptionModePlayerController().getLoopMode() == true) {
	            try {// give player time to start
	            	Thread.sleep(200);
	            } catch (InterruptedException ie) {
	            	
	            }
	            while (viewerManager.getMasterMediaPlayer().isPlaying()) {// wait until stopped
	            	try {
	            		Thread.sleep(50);
	            	} catch (InterruptedException ie) {
	            		
	            	}
	            }
	            
	            // then start the loop, if player not yet stopped
	           try {
	           		Thread.sleep(500);
	           	} catch (InterruptedException ie) {}
	           	
	           layoutManager.getTranscriptionModePlayerController().startLoop(beginTime, endTime);
            }
       }
    }
     //end of LoopThread
	
	/**
	 * Class to compare or sort the annotations
	 * according to the start time
	 * 
	 * @author aarsom
	 *
	 */
	private class AnnotationComparator implements Comparator<Annotation>{			
		public int compare(Annotation o1, Annotation o2) {
			Long bt1 = o1.getBeginTimeBoundary();
			Long bt2 = o2.getBeginTimeBoundary();
			if(bt1 != bt2){
				return bt1.compareTo(bt2);
			} else {
				Long et1 = o1.getEndTimeBoundary();
				Long et2 = o2.getEndTimeBoundary();
				return et1.compareTo(et2);	
			}
		}				
	}
}
