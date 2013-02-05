package mpi.eudico.client.annotator.prefs.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.gui.ColorDialog;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.util.ButtonCellEditor;
import mpi.eudico.client.util.ButtonTableCellRenderer;
import mpi.eudico.client.util.RadioButtonCellEditor;
import mpi.eudico.client.util.RadioButtonTableCellRenderer;
import mpi.eudico.client.util.SelectEnableObject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder; 

import java.awt.event.ActionListener; 

/**
 * A panel for viewer related preferences.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class ViewerPanel extends JPanel implements PreferenceEditor, MouseListener, ActionListener {
    private int origNumSubtitles = 4;
    private boolean origActiveAnnBold = false;
    private boolean origReducedTierHeight = false;
  
    private boolean videoInCentre = false; 
    
    private JComboBox numSubCB;
    private JCheckBox aaBoldCB;
    private JCheckBox redTierHeightCB;    
    
    private Color origSymAnnColor = Constants.SHAREDCOLOR1; 
    private Color symAnnColor = origSymAnnColor;
    private JPanel colorPreviewPanel; 
    private JButton colorButton;
    private JButton resetColorButton;
    private JLabel colorTextLabel;
    
    public ColorDialog dialog;
     
    private JButton downButton;
    private JButton upButton;   
	
    private JTable viewerTable; 
    private boolean sortOrderChanged = false;
    

    private final String  GRID_VIEWER = ElanLocale.getString(ELANCommandFactory.GRID_VIEWER);
    private final String  TEXT_VIEWER = ElanLocale.getString(ELANCommandFactory.TEXT_VIEWER);
    private final String  SUBTITLE_VIEWER = ElanLocale.getString(ELANCommandFactory.SUBTITLE_VIEWER);
    private final String  LEXICON_VIEWER = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);
    private final String  AUDIO_RECOGNIZER = ElanLocale.getString(ELANCommandFactory.AUDIO_RECOGNIZER);
    private final String  VIDEO_RECOGNIZER = ElanLocale.getString(ELANCommandFactory.VIDEO_RECOGNIZER);
    private final String  METADATA_VIEWER = ElanLocale.getString(ELANCommandFactory.METADATA_VIEWER);

    private final List<String> viewersList = new ArrayList<String>(
    		Arrays.asList(GRID_VIEWER, TEXT_VIEWER, SUBTITLE_VIEWER, LEXICON_VIEWER, AUDIO_RECOGNIZER, VIDEO_RECOGNIZER, METADATA_VIEWER ));
    
    private List<String> viewerSortOrder;
    
    private JSpinner scrollSpeedSpinner;
    private int origScrollSpeed = 10;
    private final int MIN_SCROLL = 5;
    private final int MAX_SCROLL = 50;

    /**
     * Creates a new ViewerPanel instance
     */
    public ViewerPanel() {
        super();   
        readPrefs();
        initComponents(); 
    }
    
    /**
     * Reads stored preferences.
     *
     */
    private void readPrefs() {
    	
    	Object val =  Preferences.get("Media.VideosCentre", null);
    		
    	if (val instanceof Boolean) {
    		videoInCentre = ((Boolean) val).booleanValue();
    	}        
        
    	val = Preferences.get("NumberOfSubtitleViewers", null);

    	if (val instanceof Integer) {
    		origNumSubtitles = ((Integer) val).intValue();
    	}
        
    	val = Preferences.get("TimeLineViewer.ActiveAnnotationBold", null);
    	if (val instanceof Boolean) {
    		origActiveAnnBold = ((Boolean) val).booleanValue();
    	}
        
    	val = Preferences.get("TimeLineViewer.ReducedTierHeight", null);
    	if (val instanceof Boolean) {
    		origReducedTierHeight = ((Boolean) val).booleanValue();
    	}
        
    	val = Preferences.get("PreferencesDialog.Viewer.SortOrder", null);
    	if(val instanceof List){
    		viewerSortOrder = (List<String>)val;
    	} else {
    		viewerSortOrder = viewersList;
    	}   
    	
    	val = Preferences.get("Preferences.SymAnnColor", null);
    	if (val instanceof Color) {
		origSymAnnColor= new Color (
			((Color)val).getRed(),((Color)val).getGreen(),((Color)val).getBlue()) ;
    	}
    	
    	val = Preferences.get("Preferences.TimeLine.HorScrollSpeed", null);
    	if (val instanceof Integer) {
    		origScrollSpeed = (Integer) val;
    		if (origScrollSpeed < MIN_SCROLL) {
    			origScrollSpeed = MIN_SCROLL;
    		} else if (origScrollSpeed > MAX_SCROLL) {
    			origScrollSpeed = MAX_SCROLL;
    		}
    	}

    }
    
    
    /**
     * Reads stored viewer preferences.
     *
     */
    private void readViewerPref(){
    	for(int x=0; x< viewerTable.getRowCount(); x++){	
    		if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
    			SelectEnableObject seo = (SelectEnableObject) viewerTable.getModel().getValueAt(x, 1);
    			boolean bool = getPrefValue((String) seo.getValue());
    			seo.setSelected(bool);
    			((SelectEnableObject) viewerTable.getModel().getValueAt(x, 0)).setSelected(!bool);
    		}
    	}       
    }          
    
    /**
     * Gets the references value to store the preferences of the viewer
     * 
     * @param viewer, the viewer
     * @return string, the reference string
     */
    private String getRefValue(String viewer){
    	String val = null;
    	
    	if(viewer.equals(this.GRID_VIEWER)){
    		val = "PreferencesDialog.Viewer.Grid.Right";
    	}else if(viewer.equals(this.TEXT_VIEWER)){
    		val = "PreferencesDialog.Viewer.Text.Right";
    	} else if(viewer.equals(this.SUBTITLE_VIEWER)){
    		val = "PreferencesDialog.Viewer.Subtitle.Right";
    	} else if(viewer.equals(this.LEXICON_VIEWER)){
    		val = "PreferencesDialog.Viewer.Lexicon.Right";
    	} else if(viewer.equals(this.AUDIO_RECOGNIZER)){
    		val = "PreferencesDialog.Viewer.Audio.Right";
    	} else if(viewer.equals(this.VIDEO_RECOGNIZER)){
    		val = "PreferencesDialog.Viewer.Video.Right";
    	} else if(viewer.equals(this.METADATA_VIEWER)){
    		val = "PreferencesDialog.Viewer.MetaData.Right";
    	}
    	
    	return val;
    }
    
   /**
    * Gets the preference value of the given viewer
    * 
    * @param viewer the viewer for which the value is required
    * @return boolean if true, the given viewer is in the right pane of the video
    * 				if false , then it is on the left pane of the video
    */
    private boolean getPrefValue(String viewer){
    	
    	boolean bool = true;
    	Object val = null;
    	
    	val = Preferences.get(getRefValue(viewer), null);
		if(val instanceof Boolean) {
			bool = ((Boolean) val).booleanValue();  
		} 
		return bool;
    }

    /**
     * Initializes the ui components
     */
    private void initComponents() {
    	
        //column headers
        String coulmnHeader1 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.LeftofVideo");
    	String coulmnHeader2 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.RightofVideo");
    	String coulmnHeader3 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.MoveUp");
    	String coulmnHeader4 = ElanLocale.getString("PreferencesDialog.Viewer.ColumnHeader.MoveDown");
    	
    	DefaultTableModel dm = new DefaultTableModel();
	    dm.setColumnIdentifiers(new String[] { coulmnHeader1, coulmnHeader2, coulmnHeader3, coulmnHeader4 });
	    
	    viewerTable = new JTable(dm) ;  
	    viewerTable.getColumn(coulmnHeader1).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));               
	    viewerTable.getColumn(coulmnHeader1).setCellRenderer(new RadioButtonTableCellRenderer());	   
	    
	    viewerTable.getColumn(coulmnHeader2).setCellEditor(new RadioButtonCellEditor(new JCheckBox()));               
	    viewerTable.getColumn(coulmnHeader2).setCellRenderer(new RadioButtonTableCellRenderer());	   
	    
	    viewerTable.getColumn(coulmnHeader3).setCellRenderer(new ButtonTableCellRenderer());
	    viewerTable.getColumn(coulmnHeader3).setCellEditor(new ButtonCellEditor(new JCheckBox()));
	    viewerTable.getColumn(coulmnHeader3).setMaxWidth(70);	    
	    
	    viewerTable.getColumn(coulmnHeader4).setCellRenderer(new ButtonTableCellRenderer());
	    viewerTable.getColumn(coulmnHeader4).setCellEditor(new ButtonCellEditor(new JCheckBox()));
	    viewerTable.getColumn(coulmnHeader4).setMaxWidth(70);
	    
	    viewerTable.setGridColor(Color.BLACK);	    
	    viewerTable.setRowHeight(20);
	    viewerTable.addMouseListener(this);
	    
	    ImageIcon upIcon = null;
        ImageIcon downIcon = null;        
        String upButtonLabel = null;
        String downButtonLabel= null;
        
        try {
            upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
            downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));            
        } catch (Exception ex) {
        	upButtonLabel ="Up";
        	downButtonLabel = "Down";
        }     
        
	    for (int i=0; i< viewerSortOrder.size(); i++){
	    	SelectEnableObject leftObj = new SelectEnableObject(viewerSortOrder.get(i), false, false);
	    	SelectEnableObject rightObj = new SelectEnableObject(viewerSortOrder.get(i), true, true);
	    	
	    	upButton = new JButton();
	        downButton = new JButton();
	        upButton.setToolTipText(ElanLocale.getString("PreferencesDialog.Viewer.SortButtonToolTip"));
	        downButton.setToolTipText(ElanLocale.getString("PreferencesDialog.Viewer.SortButtonToolTip"));
	        
		    if(upIcon !=null && downIcon !=null){
		    	upButton.setIcon(upIcon);
		    	downButton.setIcon(downIcon);
		    }else{
		    	upButton.setText(upButtonLabel);
		    	downButton.setText(downButtonLabel);
		    }
		    
		    dm.addRow(new Object[] { leftObj, rightObj, upButton, downButton });	
	    }
        
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 0, 2, 0);
        Insets rightColInsets = new Insets(2, 4, 2, 0);
        Font plainFont;
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 4;
        
        gbc.insets = insets;
        add(new JLabel(ElanLocale.getString("Tab.Subtitles")), gbc);

        JLabel numLabel = new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Viewer.NumSubtitles"));
        plainFont = numLabel.getFont().deriveFont(Font.PLAIN);
        numLabel.setFont(plainFont);
        gbc.insets = new Insets(2,22,2,0);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        add(numLabel, gbc);

        numSubCB = new JComboBox(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        numSubCB.setSelectedItem(origNumSubtitles);  
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 2;
        gbc.gridy = 1;        
        gbc.insets = rightColInsets;
        add(numSubCB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 4;
        gbc.insets = insets;
        gbc.gridy = 2;        
        add(new JLabel(ElanLocale.getString("TimeLineViewer.Name")), gbc);
        
        aaBoldCB = new JCheckBox(ElanLocale.getString("TimeLineViewer.ActiveAnnotationBold"));
        aaBoldCB.setFont(aaBoldCB.getFont().deriveFont(Font.PLAIN));
        aaBoldCB.setSelected(origActiveAnnBold);
        gbc.gridy = 3;
        gbc.insets = insets;
        add(aaBoldCB, gbc);
        
        redTierHeightCB = new JCheckBox(ElanLocale.getString("TimeLineViewer.ReducedTierHeight"));
        redTierHeightCB.setFont(plainFont);
        redTierHeightCB.setSelected(origReducedTierHeight);
        gbc.gridy = 4;
        add(redTierHeightCB, gbc); 
        
        SpinnerNumberModel spinModel = new SpinnerNumberModel(origScrollSpeed, 5, 50, 5);
        scrollSpeedSpinner = new JSpinner(spinModel);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 6;        
        gbc.insets = insets;
        JLabel scrollLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Viewer.HorizontalScrollSpeed"));
        scrollLabel.setFont(plainFont);
        add(scrollLabel, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.insets = rightColInsets;
        add(scrollSpeedSpinner, gbc);
                
        colorTextLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Viewer.ColorTextLabel"));
        colorTextLabel.setFont(plainFont);
        colorButton = new JButton(ElanLocale.getString("Button.Browse"));
        colorButton.addActionListener(this);
        resetColorButton = new JButton(ElanLocale.getString("Button.Default"));
        resetColorButton.addActionListener(this);

        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
        colorPreviewPanel.setPreferredSize(new Dimension(colorButton.getPreferredSize().height, 
		colorButton.getPreferredSize().height));
        colorPreviewPanel.setMinimumSize(new Dimension(colorButton.getPreferredSize().height, 
		colorButton.getPreferredSize().height));
        colorPreviewPanel.setBackground(origSymAnnColor);
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 1;  
        gbc.insets = new Insets(10, 0, 2, 0);  
        add(colorTextLabel, gbc);   

        gbc.gridx = 1;
        gbc.insets = new Insets(6, 6, 2, 0);   
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        add(colorPreviewPanel, gbc);
        gbc.insets = rightColInsets;   
        gbc.gridx = 2;
        add(colorButton, gbc);   
        gbc.gridx = 3;
        gbc.insets = insets;
        add(resetColorButton, gbc);   
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
   
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.insets = new Insets(10,0,2,0);
        JLabel videoLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Viewer.Label.Video"));
        //videoLabel.setFont(videoLabel.getFont().deriveFont(Font.PLAIN));        
        add(videoLabel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(viewerTable);
        
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6,10,2,0);
        add(scrollPane, gbc); 
        
        updateViewerSelectionInTable();  
    }

    /**
     * Returns a map of changed key-value pairs.
     *
     * @return a map of changed key-value pairs
     */
    public Map getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(1);

            if (origNumSubtitles != (Integer) numSubCB.getSelectedItem()) {
                chMap.put("NumberOfSubtitleViewers",
                    (Integer) numSubCB.getSelectedItem());
            }
            
            if (origActiveAnnBold != aaBoldCB.isSelected()) {
            	chMap.put("TimeLineViewer.ActiveAnnotationBold", new Boolean(aaBoldCB.isSelected()));
            }
            
            if (origReducedTierHeight != redTierHeightCB.isSelected()) {
            	chMap.put("TimeLineViewer.ReducedTierHeight", new Boolean(redTierHeightCB.isSelected()));
            }
            
            if(videoInCentre){
            	for(int x=0; x< viewerTable.getRowCount(); x++){	
            		if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
            			SelectEnableObject seo = (SelectEnableObject) viewerTable.getModel().getValueAt(x, 1);
            			String refValue = getRefValue((String) seo.getValue());
            			chMap.put(refValue, new Boolean(seo.isSelected()));
            		}
            	}      
            }
            
            if (symAnnColor != origSymAnnColor){
            	chMap.put("Preferences.SymAnnColor", new Color(symAnnColor.getRed(),symAnnColor.getGreen(),symAnnColor.getBlue()));        	
            }
            
            if(sortOrderChanged){            	 
            	 chMap.put("PreferencesDialog.Viewer.SortOrder" , getNewViewerSortOrder());             	
            }
            
            int curScrollSpeed = (Integer) scrollSpeedSpinner.getValue();
            
            if (curScrollSpeed != origScrollSpeed) {
            	chMap.put("Preferences.TimeLine.HorScrollSpeed", new Integer(curScrollSpeed));
            }
            return chMap;
        }
        return null;
    } 
    
    /**
     * Returns whether any preference item has been changed.
     *
     * @return true if anything has changed.
     */
    public boolean isChanged() {     	
    	
    	int count = (Integer) numSubCB.getSelectedItem();
    	
    	List<String> newSortOrder = getNewViewerSortOrder();
        
        for(int i=0; i< viewerSortOrder.size(); i++){
        	if(viewerSortOrder.get(i).equals(newSortOrder.get(i)))
        		continue;
        	else{        		
        		sortOrderChanged = true;
        		return true;
        	}
        }
        
        if (count != origNumSubtitles) {
            return true;
        }
        
        if (origActiveAnnBold != aaBoldCB.isSelected()) {
        	return true;
        }
        
        if (origReducedTierHeight != redTierHeightCB.isSelected()) {
        	return true;
        }
        
        if(videoInCentre){        	
        	return true;
        }
        
        if(origSymAnnColor != symAnnColor){
            return true;
        }
        
        int curScrollSpeed = (Integer) scrollSpeedSpinner.getValue();
        
        if (curScrollSpeed != origScrollSpeed && (curScrollSpeed >= MIN_SCROLL && curScrollSpeed <= MAX_SCROLL)) {
        	return true;
        }
        
        return false;
    }    
    
    private List<String> getNewViewerSortOrder(){
    	List<String> newSortOrder = new ArrayList<String>();
    	int row = 0;
    	while(row < viewerTable.getRowCount()){     		
    		newSortOrder.add(   (String) ((SelectEnableObject)viewerTable.getModel().getValueAt(row, 0)).getValue());
    		row++;
    	}
    	
    	return newSortOrder;
    }

    /**
	 * updates the videoInCentre value
	 * 
	 * @param  val the new value
	 */
	public void updateVideoInCentre(Boolean val){
		if(val != videoInCentre){
			videoInCentre = val;
			updateViewerSelectionInTable();		
		}
	}
	
	/**
	 * updates the viewer selection panel with 	  
	 */
	private void updateViewerSelectionInTable(){		
		if(!videoInCentre){ 
			for(int x=0; x< viewerTable.getRowCount(); x++){	
				if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
					SelectEnableObject leftRB= (SelectEnableObject) viewerTable.getModel().getValueAt(x, 0);
					SelectEnableObject rightRB= (SelectEnableObject) viewerTable.getModel().getValueAt(x, 1);
					leftRB.setEnabled(false);
					leftRB.setSelected(false);
					rightRB.setEnabled(true);
					rightRB.setSelected(true);
				}
			}
		} else {
			readViewerPref();
			for(int x=0; x< viewerTable.getRowCount(); x++){	
				if( viewerTable.getModel().getValueAt(x, 0) instanceof SelectEnableObject){
					SelectEnableObject rb = (SelectEnableObject) viewerTable.getModel().getValueAt(x, 0);
					rb.setEnabled(true);
				}
			}	
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		int selectedRowIndex = viewerTable.getSelectedRow();
		int selectedColumnIndex = viewerTable.getSelectedColumn();
		
		if( viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex) instanceof JButton){	
			if(selectedColumnIndex == 2 ){
				Object row1 = viewerTable.getModel().getValueAt(selectedRowIndex, 0);
				Object row11 = viewerTable.getModel().getValueAt(selectedRowIndex, 1);
				if(selectedRowIndex > 0){
					Object row2 = viewerTable.getModel().getValueAt(selectedRowIndex-1, 0);
					Object row21 = viewerTable.getModel().getValueAt(selectedRowIndex-1, 1);
					
					viewerTable.getModel().setValueAt(row1, selectedRowIndex-1, 0);
					viewerTable.getModel().setValueAt(row11, selectedRowIndex-1, 1);
					
					viewerTable.getModel().setValueAt(row2, selectedRowIndex, 0);
					viewerTable.getModel().setValueAt(row21, selectedRowIndex, 1);
				}
			} else if(selectedColumnIndex == 3 ){
				Object row1 = viewerTable.getModel().getValueAt(selectedRowIndex, 0);
				Object row11 = viewerTable.getModel().getValueAt(selectedRowIndex, 1);
				if(selectedRowIndex < viewerTable.getRowCount()-1){
					Object row2 = viewerTable.getModel().getValueAt(selectedRowIndex+1, 0);
					Object row21 = viewerTable.getModel().getValueAt(selectedRowIndex+1, 1);
					
					viewerTable.getModel().setValueAt(row1, selectedRowIndex+1, 0);
					viewerTable.getModel().setValueAt(row11, selectedRowIndex+1, 1);
					
					viewerTable.getModel().setValueAt(row2, selectedRowIndex, 0);
					viewerTable.getModel().setValueAt(row21, selectedRowIndex, 1);
				}
			}
		}
		viewerTable.repaint();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		int selectedRowIndex = viewerTable.getSelectedRow();
		int selectedColumnIndex = viewerTable.getSelectedColumn();
		
		if( viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex) instanceof SelectEnableObject){			
			SelectEnableObject seo1 = (SelectEnableObject) viewerTable.getValueAt(selectedRowIndex, selectedColumnIndex);
			if(seo1.isSelected()){
				SelectEnableObject seo2 = null;
				if(selectedColumnIndex==0){
					seo2 = (SelectEnableObject) viewerTable.getValueAt(selectedRowIndex, 1);
				} else if(selectedColumnIndex==1) {
					seo2 = (SelectEnableObject) viewerTable.getValueAt(selectedRowIndex, 0);
				}
				if(seo2 != null && seo2.isEnabled()){
					seo2.setSelected(false);
					//seo2.setSelected(!seo1.isSelected());
				}
			} else {
				seo1.setSelected(true);
			}
		}
		viewerTable.repaint();
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void actionPerformed(ActionEvent e) {
	    
	    Color newColor = null;

	    if (e.getSource() == colorButton) {
		
		// symAnnColor is suggested in the dialog
		
		dialog = new ColorDialog (this, origSymAnnColor);  
		
		newColor = dialog.chooseColor(); 
		if (newColor == null){
		    // no color selected, keep current
		}
		else {
		    symAnnColor = newColor;
		    colorPreviewPanel.setBackground(symAnnColor);
		}
	    }
	    else if (e.getSource() == resetColorButton) {
	            colorPreviewPanel.setBackground(Constants.SHAREDCOLOR1); 
	    }
	}
}


