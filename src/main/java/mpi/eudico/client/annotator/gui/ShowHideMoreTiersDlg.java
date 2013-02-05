package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A dialog that gives the user the possibility to change the visibility of
 * more tiers at a time.
 *
 * @author Han Sloetjes
 */
public class ShowHideMoreTiersDlg extends ClosableDialog
    implements ActionListener, MouseListener {
	
    /** A constant for unspecified participant or linguistic type */
    private final String NOT_SPECIFIED = "not specified";
    
    private final String SHOW_TIERS = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowTiers") ;
    private final String SHOW_TYPES = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowLinguisticType");
    private final String SHOW_PART = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowParticipant");
    private final String SHOW_ANN = ElanLocale.getString(
							"MultiTierControlPanel.Menu.ShowAnnotator") ;

    // components
    private JPanel checkboxPanel;   
    private JButton showAllButton;
    private JButton hideAllButton;
    private JButton applyButton;
    private JButton cancelButton;    
    private JButton sortButton;
    private JButton sortDefaultButton;
    private JTabbedPane showTabPane;
    
    private Transcription trans;   
    private List allTierNames;
    private Vector<String> allLinTypeNames;
    private Vector<String> allParticipants;
    private Vector<String> allAnnotators;
       
    private Vector<String> visibleTypeNames;
    private Vector<String> visibleTierNames;
    private Vector<String> visibleParts;
    private Vector<String> visibleAnns;
    private int currentTabIndex = 0;    

    private boolean rootTiersOnly = false;
    
    private boolean valueChanged = false;

	private List hiddenTiers;
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers   at a time.
     *
     * @param trans the transcription
     * @param visibleTiers the currently visible tiers
     */
    public ShowHideMoreTiersDlg(Transcription trans, Vector visibleTiers) {    	
    	this(trans,visibleTiers, null);
    }
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers at a time.
     *
     * @param trans the transcription
     * @param visibleTiers the currently visible tiers
     * @param component, the component used to set the location of the this dialog
     */
    public ShowHideMoreTiersDlg(Transcription trans, Vector visibleTiers, Component component) {
       this(trans, visibleTiers, component, false);
    }
    
    public ShowHideMoreTiersDlg(Transcription trans, Vector visibleTiers, Component component, boolean rootTiersOnly) {
        this(trans, null, visibleTiers, component, rootTiersOnly);
     }    

    public ShowHideMoreTiersDlg(Transcription trans, List tierOrder, Vector visibleTiers, Component component) {
       this(trans, tierOrder, visibleTiers, component, false);
    }
    
    /**
     * Creates and shows a modal dialog, displaying checkbox items for each
     * tier in the transcription. The user can change the visibility of
     * more tiers at a time.
     *
     * @param trans the transcription
     * @param tierOrder current order of tiers
     * @param visibleTiers the currently visible tiers
     * @param component, the component used to set the location of the this dialog
     * @param rootTiersOnly show only root tiers in table
     */
    public ShowHideMoreTiersDlg(Transcription trans, List tierOrder, Vector visibleTiers, Component component, boolean rootTiersOnly) {
        super(ELANCommandFactory.getRootFrame(trans),
            ElanLocale.getString("MultiTierControlPanel.Menu.VisibleTiers"),
            true);
        
        this.trans = trans;       
        this.rootTiersOnly = rootTiersOnly;
        
        initialize(tierOrder, visibleTiers);
        initDialog();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocation(component);       
    }
    
    private void initialize(List tierOrder, Vector visibleTiers){    	
    	if(tierOrder == null){    		
        	allTierNames = ELANCommandFactory.getViewerManager(trans).getTierOrder().getTierOrder();
        	
        } else {
        	TierImpl tier;
        	allTierNames = tierOrder;
        }   
    	
    	if(rootTiersOnly){
    		TierImpl tier;
        	int i=0;
        	while (i < allTierNames.size()){
        		tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));
        		if(tier.hasParentTier()){
        			allTierNames.remove(i);
        		}else{
        			i++;
        		}
        	}
    	}
    	
        visibleTierNames = new Vector<String>();
        if(visibleTiers != null){
        	TierImpl tier = null;
        	Object obj = null;
    		for(int i=0; i< visibleTiers.size(); i++){
    			obj = visibleTiers.get(i);
    			if(obj instanceof TierImpl){
    				tier = (TierImpl) visibleTiers.get(i);
    				if(allTierNames.contains(tier.getName()) && !visibleTierNames.contains(tier.getName())){
    					visibleTierNames.add(tier.getName());
    				}
    			} else if (obj instanceof String){
    				if(allTierNames.contains((String)obj) && !visibleTierNames.contains((String)obj)){
    					visibleTierNames.add((String)obj);
    				}    				
//    				visibleTierNames.addAll(visibleTiers);
//    				break;
    			}
    		}
    	}   
        allLinTypeNames = new Vector<String>();       
        allParticipants = new Vector<String>();
        allAnnotators = new Vector<String>();            
        visibleTypeNames = new Vector<String>();
        visibleParts = new Vector<String>();
        visibleAnns = new Vector<String>();
        
        hiddenTiers = new Vector();
    }
    

    /**
     * Create components and add them to the content pane.
     */
    private void initDialog() {
        showAllButton = new JButton(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ShowAllTiers"));
        showAllButton.addActionListener(this);
        hideAllButton = new JButton(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.HideAllTiers"));
        hideAllButton.addActionListener(this);
        applyButton = new JButton(ElanLocale.getString("Button.OK"));
        applyButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        
        sortButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Sort"));
        sortButton.addActionListener(this);
        
        sortDefaultButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Default"));
        sortDefaultButton.addActionListener(this);
        
        showTabPane = new JTabbedPane(); 
        
        JPanel buttonPanel1 = new JPanel(new GridBagLayout());
        buttonPanel1.add(sortButton);
        buttonPanel1.add(sortDefaultButton);
        buttonPanel1.add(showAllButton);
        buttonPanel1.add(hideAllButton);            

        GridLayout gl = new GridLayout(1, 2, 6, 2);
        JPanel buttonPanel2 = new JPanel(gl);          
        buttonPanel2.add(applyButton);
        buttonPanel2.add(cancelButton);
        

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(600,400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.gridy = 1;
        gbc.gridx = 0;
        getContentPane().add(buttonPanel1, gbc);
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        getContentPane().add(buttonPanel2, gbc);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        getContentPane().add(showTabPane, gbc);
        getRootPane().setDefaultButton(applyButton);

        
        checkboxPanel = new JPanel();
        BoxLayout box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        JCheckBox checkbox;
        TierImpl tier;
        String value;

        for (int i = 0; i < allTierNames.size(); i++) {
            tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));
            checkbox = new JCheckBox(tier.getName());            
            checkboxPanel.add(checkbox);
            
            value =tier.getParticipant();
            if(value.length()==0){
            	value = NOT_SPECIFIED;
            }
            
            if(!allParticipants.contains(value)){
            	allParticipants.add(value);
            }

            value =tier.getAnnotator();
            if(value.length() == 0){
            	value = NOT_SPECIFIED;
            }
            
            if(!allAnnotators.contains(value)){
            	allAnnotators.add(value);
            }
            
            value = tier.getLinguisticType().getLinguisticTypeName();
            if(!allLinTypeNames.contains(value)){
            	allLinTypeNames.add(value);
            }
        }
       
        showTabPane.add(SHOW_TIERS, new JScrollPane(checkboxPanel));
    
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);      
        for (int i = 0; i < allLinTypeNames.size(); i++) {        	
        	checkbox = new JCheckBox(allLinTypeNames.get(i));
        	checkboxPanel.add(checkbox);
        }
        showTabPane.add(SHOW_TYPES ,new JScrollPane(checkboxPanel));
    
        checkboxPanel = new JPanel();    
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        for (int i = 0; i < allParticipants.size(); i++) {                
        	checkbox = new JCheckBox(allParticipants.get(i));
        	checkboxPanel.add(checkbox);
        }
        showTabPane.addTab(SHOW_PART , new JScrollPane(checkboxPanel));
  
        checkboxPanel = new JPanel();
        box = new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS);
        checkboxPanel.setLayout(box);
        for (int i = 0; i < allAnnotators.size(); i++) {                
        	checkbox = new JCheckBox(allAnnotators.get(i));
       		checkboxPanel.add(checkbox);
        }
        showTabPane.addTab(SHOW_ANN, new JScrollPane(checkboxPanel));  
        
        showTabPane.addMouseListener(this);
        
        showTiers();
    }
    
    
    private void updateTab(int index, List sortedList){
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	checkboxPanel.removeAll();
    	
    	for (int i = 0; i < sortedList.size(); i++) {
       	 checkboxPanel.add( new JCheckBox((String)sortedList.get(i)));
       }
       showTabPane.repaint(); 
       updateTabAtIndex(index);
    }
    
    private void updateShowTiersTab(){
    	int index = showTabPane.indexOfTab(SHOW_TIERS);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	checkboxPanel.removeAll();
    	
        for (int i = 0; i < allTierNames.size(); i++) {
        	 checkboxPanel.add( new JCheckBox((String)allTierNames.get(i)));
        }
        showTabPane.repaint();            
        showTiers();
    }
    
    private void updateShowTypesTab(){
    	int index = showTabPane.indexOfTab(SHOW_TYPES);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	checkboxPanel.removeAll();
    	
        for (int i = 0; i < allLinTypeNames.size(); i++) {
        	 checkboxPanel.add( new JCheckBox(allLinTypeNames.get(i)));
        }
        showTabPane.repaint();            
        showTypes();
    }
    
    private void updateShowPartTab(){
    	int index = showTabPane.indexOfTab(SHOW_PART);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	checkboxPanel.removeAll();
    	
        for (int i = 0; i < allParticipants.size(); i++) {
        	 checkboxPanel.add( new JCheckBox(allParticipants.get(i)));
        }
        showTabPane.repaint();            
        showParticipants();
    }
    
    private void updateShowAnnTab(){
    	int index = showTabPane.indexOfTab(SHOW_ANN);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	checkboxPanel.removeAll();
    	
        for (int i = 0; i < allAnnotators.size(); i++) {
        	 checkboxPanel.add( new JCheckBox(allAnnotators.get(i)));
        }
        showTabPane.repaint();            
        showAnnotators();
    }
    
    /**
     * Position the dialog relative to the given component.
     * 
     * @param component
     */
    private void setLocation(Component component) { 
    	if(component instanceof mpi.eudico.client.annotator.viewer.MultiTierControlPanel){
    		Point p = new Point(0, 0);
    		SwingUtilities.convertPointToScreen(p, component);

    		int windowHeight = SwingUtilities.getWindowAncestor(component)
                                         .getHeight();

    		if (this.getHeight() > windowHeight) {
    			// don't let the dialog be higher than the window
    			this.setSize(this.getWidth(), windowHeight);
    		}

    		p.x += component.getWidth();

    		// line at bottom
    		p.y -= (this.getHeight() - component.getHeight());
    		setLocation(p);     		
    	} else{
    		setLocationRelativeTo(getParent());
    	} 
    }
    
    public Vector<String> getVisibleTierNames(){  
    	return visibleTierNames;
    }
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators)
     * 
     * @return
     */
    public String getSelectionMode(){
    	if(visibleTypeNames.size() > 0){
    		return SHOW_TYPES;
    	} else if(visibleParts.size() > 0){
    		return SHOW_PART;
    	} else if(visibleAnns.size() > 0){
    		return this.SHOW_ANN;
    	}
    	return SHOW_TIERS;
    }
    
    /** 
     *Sets and removes the hidden tiers from selection
     * 
     * @param hiddenTiers
     */
    private void setHiddenTiers(List hiddenTiers){   
    	if(hiddenTiers == null){
    		return;
    	}
    	
    	this.hiddenTiers = hiddenTiers;
    	
    	TierImpl t;
    	String value;
    	String selectionMode = showTabPane.getTitleAt(currentTabIndex);
    	if(selectionMode.equals(SHOW_ANN)){
    		visibleAnns.clear();
    		for(int i=0; i< visibleTierNames.size(); i++){
    			t = (TierImpl) trans.getTierWithId(visibleTierNames.get(i));
    			value = t.getAnnotator();
    			
                if(value.length() == 0){
                	value = NOT_SPECIFIED;
                }                
                if(!visibleAnns.contains(value)){
                	visibleAnns.add(value);
                }
    		}    		
    		
    		visibleTierNames.clear();
         	for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(visibleAnns.contains(annName) && !hiddenTiers.contains(tier.getName())){
            		visibleTierNames.add(tier.getName());
            	}
            } 
         	visibleTypeNames.clear();  
         	visibleParts.clear();   
    	}else if(selectionMode.equals(SHOW_TYPES)){
    		visibleTypeNames.clear();
    		for(int i=0; i< visibleTierNames.size(); i++){
    			t = (TierImpl) trans.getTierWithId(visibleTierNames.get(i));
    			value = t.getLinguisticType().getLinguisticTypeName();
                
                if(!visibleTypeNames.contains(value)){
                	visibleTypeNames.add(value);
                }
    		}
    		
    		visibleTierNames.clear();
        	Vector visibleTiers;
        	TierImpl tier ;
        	for(int i=0; i< visibleTypeNames.size(); i++){
        		String typeName = visibleTypeNames.get(i);
        		visibleTiers = trans.getTiersWithLinguisticType(typeName);
        		if(visibleTiers != null){
        			for(int x=0; x< visibleTiers.size(); x++){   
        				tier = (TierImpl)visibleTiers.get(x);        				
        				if(!hiddenTiers.contains(tier.getName())){
        					visibleTierNames.add(tier.getName());
        				}  
        			}
        		}
        	}
        	visibleAnns.clear();  
        	visibleParts.clear();  
    	}else if(selectionMode.equals(SHOW_PART)){
    		visibleParts.clear();
    		for(int i=0; i< visibleTierNames.size(); i++){
    			t = (TierImpl) trans.getTierWithId(visibleTierNames.get(i));
    			value = t.getParticipant();
    			
                if(value.length() == 0){
                	value = NOT_SPECIFIED;
                }
                
                if(!visibleParts.contains(value)){
                	visibleParts.add(value);
                }
    		}
    		
    		visibleTierNames.clear();        	
            for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));            	
            	String partName = tier.getParticipant();            	
            	if(partName.length() == 0){
            		partName = NOT_SPECIFIED;
            	}
            	if(visibleParts.contains(partName) && !hiddenTiers.contains(tier.getName())){
            		visibleTierNames.add(tier.getName());
            	}
            }
            visibleTypeNames.clear();  
            visibleAnns.clear();
        	
    		currentTabIndex = showTabPane.indexOfTab(SHOW_PART);    
    	} else {
    		//currentTabIndex = showTabPane.indexOfTab(SHOW_TIER); 
    		return;
    	}
    	updateTabAtIndex(currentTabIndex);
    }
    
    /**
     * Sets the selection mode and also the tiers hidden in that mode
     * 
     * @param setSelectType
     */
    public void setSelectedMode(String selectionMode, List hiddenTiers){      
    	if(selectionMode == null){
    		return;
    	}
    	if(selectionMode.equals(SHOW_ANN)){  
            currentTabIndex = showTabPane.indexOfTab(SHOW_ANN);    		
    	}else if(selectionMode.equals(SHOW_TYPES)){    		
            currentTabIndex = showTabPane.indexOfTab(SHOW_TYPES);    
    	}else if(selectionMode.equals(SHOW_PART)){    		        	
    		currentTabIndex = showTabPane.indexOfTab(SHOW_PART);    
    	} else {
    		currentTabIndex = showTabPane.indexOfTab(SHOW_TIERS);     		
    	}    	
    	showTabPane.setSelectedIndex(currentTabIndex);
    	setHiddenTiers(hiddenTiers);
    }
    
    /**
     * Returns the list of hidden ties of a 
     * certain group(type /participants/ annotators)
     * 
     * @return
     */
    public List getHiddenTiers(){
    	if(trans == null){
    		return null;
    	}
    	Vector<String> hiddenTiers = new Vector<String>();
    	if(visibleTypeNames.size() > 0){
    		Vector tiers = new Vector();
    		for(int i=0; i< visibleTypeNames.size(); i++){
    			tiers.addAll(trans.getTiersWithLinguisticType(visibleTypeNames.get(i)));
    		}
    		
    		TierImpl t;
    		for(int i=0; i < tiers.size(); i++){
    			t = (TierImpl) tiers.get(i);
    			if(!visibleTierNames.contains(t.getName())){
    				hiddenTiers.add(t.getName());
    			}
    		}
    	} else if(visibleParts.size() > 0){    		
    		for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));            	
            	String partName = tier.getParticipant();            	
            	if(partName.length() == 0){
            		partName = NOT_SPECIFIED;
            	}
            	if(visibleParts.contains(partName)){
            		if(!visibleTierNames.contains(tier.getName())){
            			hiddenTiers.add(tier.getName());
            		}
            	}
            }
    	} else if(visibleAnns.size() > 0){
    		for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(visibleAnns.contains(annName)){
            		if(!visibleTierNames.contains(tier.getName())){
            			hiddenTiers.add(tier.getName());
            		}
            	}
            } 
    	}
    	
    	if(hiddenTiers.size() > 0){
    		return hiddenTiers;
    	}  
    	
    	return null;
    }
    
    /**
     * 
     */
    private void showTiers(){
    	int index = showTabPane.indexOfTab(SHOW_TIERS);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView(); 
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
        	if (boxes[i] instanceof JCheckBox) {
        		if(visibleTierNames.contains(((JCheckBox) boxes[i]).getText())){
                	((JCheckBox) boxes[i]).setSelected(true);
                } else {
                	((JCheckBox) boxes[i]).setSelected(false);
                }
            }
    	}
    }
    
    /**
     * 
     */
    private void showTypes(){
    	int index = showTabPane.indexOfTab(SHOW_TYPES);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(visibleTypeNames.contains(((JCheckBox) boxes[i]).getText())){
            		((JCheckBox) boxes[i]).setSelected(true);
            	} else {
            		((JCheckBox) boxes[i]).setSelected(false);
            	}
            }
        }
    }
    
   /**
    * 
    */
   private void showParticipants(){
   	int index = showTabPane.indexOfTab(SHOW_PART);
   	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
   	
   	Component[] boxes = checkboxPanel.getComponents();
       for (int i = 0; i < boxes.length; i++) {
           if (boxes[i] instanceof JCheckBox) {
           	if(visibleParts.contains(((JCheckBox) boxes[i]).getText())){
           		((JCheckBox) boxes[i]).setSelected(true);
           	} else {
           		((JCheckBox) boxes[i]).setSelected(false);
           	}
           }
       }
   }
   
   /**
   * 
   */
   private void showAnnotators(){
	   int index = showTabPane.indexOfTab(SHOW_ANN);
	   JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
	   	
	   Component[] boxes = checkboxPanel.getComponents();
	   for (int i = 0; i < boxes.length; i++) {
		   if (boxes[i] instanceof JCheckBox) {
			   if(visibleAnns.contains(((JCheckBox) boxes[i]).getText())){
				   ((JCheckBox) boxes[i]).setSelected(true);
			   } else {
				   ((JCheckBox) boxes[i]).setSelected(false);
			   }
		   }
	   }
   	}
    
    /**
     * 
     */
    private void updateTiers(){
    	int index = showTabPane.indexOfTab(SHOW_TIERS);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
    	Vector<String> oldVisibleTierNames = new Vector<String>();
    	oldVisibleTierNames.addAll(visibleTierNames);
    	boolean valueChanged =false;
    	
    	visibleTierNames.clear();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String tierName = ((JCheckBox) boxes[i]).getText();                		
            		visibleTierNames.add(tierName);
            		if(!oldVisibleTierNames.contains(tierName)){
            			if(hiddenTiers.contains(tierName)){
            				hiddenTiers.remove(tierName);
            				if(!valueChanged)
            					valueChanged = false;
            			} else{
            				valueChanged = true;
            			}
            		}
            	}
            }
        }
        
        if(valueChanged){
        	hiddenTiers.clear();
        	visibleTypeNames.clear();    	
            visibleParts.clear();  
            visibleAnns.clear();
        } else {
        	for(int i=0; i < oldVisibleTierNames.size(); i++){
        		if(!visibleTierNames.contains(oldVisibleTierNames.get(i))){
        			if(!hiddenTiers.contains(oldVisibleTierNames.get(i))){
        				hiddenTiers.add(oldVisibleTierNames.get(i));
        			}
        		}
        	}
        }
    }
    
    /**
     * 
     */
    private void updateLinguisticTypes(){
    	int index = showTabPane.indexOfTab(SHOW_TYPES);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedTypes = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String typeName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedTypes.contains(typeName)){
            			selectedTypes.add(typeName);
            		}
            	} 
            }
        }  
        
        if(selectedTypes.size() != visibleTypeNames.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedTypes.size(); i++){
        		if(!visibleTypeNames.contains(selectedTypes.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }            
        
        if(changed){
        	if(visibleTypeNames.size() == 0){
            	hiddenTiers.clear();
            }
        	visibleTypeNames = selectedTypes;
        	visibleTierNames.clear();
        	Vector visibleTiers;
        	for(int i=0; i< visibleTypeNames.size(); i++){
        		String typeName = visibleTypeNames.get(i);        		
        		visibleTiers = trans.getTiersWithLinguisticType(typeName);
        		if(visibleTiers != null){
        			for(int x=0; x< visibleTiers.size(); x++){        				
        				visibleTierNames.add(((TierImpl)visibleTiers.get(x)).getName());
        			}
        		}
        	}        	
        	for(int i= 0; i< hiddenTiers.size(); i++){
         		if(visibleTierNames.contains(hiddenTiers.get(i))){
         			visibleTierNames.remove(hiddenTiers.get(i));
         		}
         	}
            visibleAnns.clear();  
            visibleParts.clear();                
        }
    }
    
    public JTabbedPane getTabPane(){       	
    	return showTabPane;
    }
    
    /**
     * 
     */
    private void updateParticipants(){
    	int index = showTabPane.indexOfTab(SHOW_PART);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedPart = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String partName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedPart.contains(partName)){
            			selectedPart.add(partName);
            		}
            	} 
            }
        }    
        
        if(selectedPart.size() != visibleParts.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedPart.size(); i++){
        		if(!visibleParts.contains(selectedPart.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }
        
        if(changed){
        	if(visibleParts.size() == 0){
            	hiddenTiers.clear();
            }
        	visibleTierNames.clear();
        	visibleParts = selectedPart;
            for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));           	
            	String partName = tier.getParticipant();            	
            	if(partName.length() == 0){
            		partName = NOT_SPECIFIED;
            	}
            	if(visibleParts.contains(partName)){
            		visibleTierNames.add(tier.getName());
            	}
            }
            
            for(int i= 0; i< hiddenTiers.size(); i++){
        		if(visibleTierNames.contains(hiddenTiers.get(i))){
        			visibleTierNames.remove(hiddenTiers.get(i));
        		}
        	}
            visibleTypeNames.clear();  
        	visibleAnns.clear();
        }
            
    }
    
    /**
     * 
     */
    private void updateAnnotators(){
    	int index = showTabPane.indexOfTab(SHOW_ANN);
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
    	boolean changed = false;        	
    	Vector<String> selectedAnn = new Vector<String>();
    	
    	Component[] boxes = checkboxPanel.getComponents();
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
            	if(((JCheckBox) boxes[i]).isSelected()){
            		String annName = ((JCheckBox) boxes[i]).getText();                		
            		if(!selectedAnn.contains(annName)){
            			selectedAnn.add(annName);
            		}
            	} 
            }
        }
        
        if(selectedAnn.size() != visibleAnns.size() ){
        	changed = true;
        } else {
        	for(int i=0; i< selectedAnn.size(); i++){
        		if(!visibleAnns.contains(selectedAnn.get(i))){
        			changed = true;
        			break;
        		}
        	}
        }              
        
        if(changed){
        	if(visibleAnns.size() == 0){
            	hiddenTiers.clear();
            }
         	visibleAnns = selectedAnn;
         	visibleTierNames.clear();
            for(int i=0; i< allTierNames.size(); i++){
            	TierImpl tier = (TierImpl) trans.getTierWithId((String) allTierNames.get(i));            	
            	String annName = tier.getAnnotator();            	
            	if(annName.length() == 0){
            		annName = NOT_SPECIFIED;
            	}
            	if(visibleAnns.contains(annName)){
            		visibleTierNames.add(tier.getName());
            	}
            } 
            
            for(int i= 0; i< hiddenTiers.size(); i++){
        		if(visibleTierNames.contains(hiddenTiers.get(i))){
        			visibleTierNames.remove(hiddenTiers.get(i));
        		}
        	}
            visibleTypeNames.clear();  
            visibleParts.clear();                
        }
    }


    /**
     * Check or uncheck all tier checkboxes.
     *
     * @param selected if true select all checkboxes, unselect all
     *        otherwise
     */
    private void setAllSelected(boolean selected) {
    	int index = showTabPane.getSelectedIndex();
    	JPanel checkboxPanel = (JPanel) ((JScrollPane) showTabPane.getComponentAt(index)).getViewport().getView();
    	
        Component[] boxes = checkboxPanel.getComponents();

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] instanceof JCheckBox) {
                ((JCheckBox) boxes[i]).setSelected(selected);
            }
        }
    }

    /**
     * Disposes this dialog.
     */
    private void close() {
        setVisible(false);
        dispose();
    }
    
    public boolean isValueChanged(){
    	return valueChanged;
    }

    /**
     * Actions following a button click.
     *
     * @param ae the Action Event
     */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource(); 
        if (source == showAllButton) {
            setAllSelected(true);
        } else if (source == hideAllButton) {
            setAllSelected(false);
        } else if (source == applyButton) { 
        	valueChanged = true;
        	updateChanges(showTabPane.getSelectedIndex());           
            close();
        } else if (source == cancelButton) {
        	valueChanged = false;
            close();
        }else if(source ==sortButton){        	
        	int index = showTabPane.getSelectedIndex();
        	updateChanges(index);
        	sortAlphabetically(index);
        } else if(source ==sortDefaultButton){        
        	int index = showTabPane.getSelectedIndex();
        	updateChanges(index);
        	sortInDefaultOrder(index);
        }
    }
    
    private void sortInDefaultOrder(int index){
    	String tabName = showTabPane.getTitleAt(index);        	
    	if(tabName.equals(SHOW_TYPES)){  
    		updateTab(index, allLinTypeNames);
    	} else if(tabName.equals(SHOW_TIERS)){
    		updateTab(index, allTierNames);
    	} else if(tabName.equals(SHOW_PART)){
    		updateTab(index, allParticipants);
    	} else if(tabName.equals(SHOW_ANN)){  
    		updateTab(index, allAnnotators);
    	}         	
    }
    
    private void sortAlphabetically(int index){    	
    	String tabName = showTabPane.getTitleAt(index);        	
    	if(tabName.equals(SHOW_TYPES)){        		
    		Object[] array = (Object[]) allLinTypeNames.toArray();
    		Arrays.sort(array);
    		Vector<String> sortedLinTypeNames = new Vector<String>();
    		for(int i=0; i<array.length; i++){
    			sortedLinTypeNames.add((String)array[i]);
    		}  
    		updateTab(index, sortedLinTypeNames);
    	} else if(tabName.equals(SHOW_TIERS)){
    		Object[] array = (Object[]) allTierNames.toArray();
    		Arrays.sort(array);
    		Vector<String> sortedTierNames = new Vector<String>();
    		for(int i=0; i<array.length; i++){
    			sortedTierNames.add((String)array[i]);
    		}
    		updateTab(index, sortedTierNames);
    	} else if(tabName.equals(SHOW_PART)){
    		Object[] array = (Object[]) allParticipants.toArray();
    		Arrays.sort(array);
    		Vector<String> sortedParticipants = new Vector<String>();
    		for(int i=0; i<array.length; i++){
    			sortedParticipants.add((String)array[i]);
    		}  
    		updateTab(index, sortedParticipants);
    	} else if(tabName.equals(SHOW_ANN)){        		
    		Object[] array = (Object[]) allAnnotators.toArray();
    		Arrays.sort(array);
    		Vector<String> sortedAnnotators = new Vector<String>();
    		for(int i=0; i<array.length; i++){
    			sortedAnnotators.add((String)array[i]);
    		}  
    		updateTab(index, sortedAnnotators);
    	} 
    }
    
    private void updateChanges(int index){
    	String tabName = showTabPane.getTitleAt(index);
    	
    	if(tabName.equals(SHOW_TYPES)){
    		updateLinguisticTypes();
    	} else if(tabName.equals(SHOW_TIERS)){
    		updateTiers();
    	} else if(tabName.equals(SHOW_PART)){
    		updateParticipants();
    	} else if(tabName.equals(SHOW_ANN)){
    		updateAnnotators();
    	} 
    }
    
    private void updateTabAtIndex(int index){
    	String tabName = showTabPane.getTitleAt(index);
    	
    	if(tabName.equals(SHOW_TYPES)){
    		showTypes();
    	} else if(tabName.equals(SHOW_TIERS)){
    		showTiers();
    	} else if(tabName.equals(SHOW_PART)){
    		showParticipants();
    	} else if(tabName.equals(SHOW_ANN)){
    		showAnnotators();
    	} 
    }

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() == showTabPane) {
			//int index = showTabPane.getSelectedIndex();
			updateChanges(currentTabIndex);
		}	
	}

	public void mouseReleased(MouseEvent e) {	
		if (e.getSource() == showTabPane) {
			currentTabIndex = showTabPane.getSelectedIndex();
			updateTabAtIndex(currentTabIndex);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
}
