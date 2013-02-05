package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesUser;
import mpi.eudico.client.annotator.TierOrder;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.CommandAction;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.TierDependenciesCA;

import mpi.eudico.client.annotator.gui.*;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.util.TierMenuStringFormatter;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * JPanel that shows Tier labels for a MultiTierViewer The labels can be hidden
 * and replaced.
 */
public class MultiTierControlPanel extends JPanel implements MouseListener,
    MouseMotionListener, MouseWheelListener, ActionListener, ComponentListener, ElanLocaleListener, PreferencesUser {
    /** Holds value of property default width */
    public final static int MIN_WIDTH = 120;

    /** the horizontal margins of the component */
    public final static int MARGIN = 5;

    /** Indentation for each hierarchical tier level */
    public final static int LEVEL_INDENT = 10;

    /** The size of the node expand/collapse marker */
    public static final int MARKER_SIZE = 6;
    
    /** Holds value of the unsorted sorting property */
    private final int UNSORTED = 0;

    /** Holds value of the sort by hierarchy sorting property */
    private final int SORT_BY_HIERARCHY = 1;

    /** Holds value of the sort by participant sorting property */
    private final int SORT_BY_PARTICIPANT = 2;

    /** Holds value of the sort by linguistic type sorting property */
    private final int SORT_BY_LINGUISTIC_TYPE = 3;

    /** Holds value of the sort by annotator sorting property */
    private final int SORT_BY_ANNOTATOR = 4;   
    
    /** A constant for unspecified participant or linguistic type */
    private final String NOT_SPECIFIED = "not specified";
    
    /**
     * Add a marker to the action command of a tier to avoid collisions in  the
     * handling of menuitem's action events
     */
    private final String TIER_AC = "@&@";
    
    /** popup menu and menu items */
    private JPopupMenu popup;
    private JMenu viewerMenu;
    private JMenu visTiersMenu;
    private JMenuItem showHideMoreMI;
    private JMenu sortTiersMenu;
    private JMenuItem hideAllMI;
    private JMenuItem showAllMI;    
    private JMenuItem collapseAllMI;
    private JMenuItem expandAllMI;
    private JMenuItem hideMI;
    private JMenuItem changeMI;
    private JMenuItem applyMI;
    private JMenuItem deleteMI;
    private JMenuItem colOrExpMI;
    private JMenuItem showHierMI;
    private JCheckBoxMenuItem numAnnosMI;
    private JRadioButtonMenuItem timelineMI;
    private JRadioButtonMenuItem interlinearMI;
    private JRadioButtonMenuItem sortByHierarchMI;
    private JRadioButtonMenuItem sortByTypeMI;
    private JRadioButtonMenuItem sortByPartMI;
    private JRadioButtonMenuItem sortByAnnotMI;
    private JRadioButtonMenuItem unsortedMI;    
    private JCheckBoxMenuItem sortAlphabeticallyMI;
    
    private Dimension dimension;
    private Transcription transcription;
    
    /** Holds all the tiers of this transcription - unsorted*/
    private Vector tiers;
    
    /** Holds all the tiers of this transcription - sorted*/
    private Vector<TierImpl> sortedTiers; 
    
    /** Holds all the tiers that are visible*/
    private Vector<TierImpl> visibleTiers; 
    
    /** Holds all the tiers that are visible and diaplayable*/
    private Vector<TierImpl> displayableTiers; 
    
    /**
     * A flattened view on the tier tree. It contains tier nodes that are
     * displayable and that are not a child of a collapsed node.
     */
    private Vector<TierTreeNode> displayableTierNodes;
    
    
    /** Holds pair of < tierName, its tierTreeNode>*/
    private HashMap<String, TierTreeNode> tierTreeNodeMap;
    
    /** Holds a pair of <tier, tierNames> */
    private Hashtable<TierImpl, String> tierNames;    
    
    //ordering
    private DefaultMutableTreeNode sortedRootNode;
    private TierOrder tierOrder;
    
    //sorting
    /** Holds the last used sorting mode */
    private int oldSortMode;
    
    /** Holds the current sorting mode */
    private int sortMode;
   
    //painting 
    private BufferedImage bi;
    private Graphics2D big2d;
    private FontMetrics fontMetrics;
    private Font boldFont;
    private FontMetrics boldMetrics;
    
    //dragging 
    private boolean dragging;
    private int dragX;
    private int dragY;
    private int dragIndex;
    private String dragLabel;
    
    private MultiTierViewer viewer;
    private Vector<MultiTierViewer> viewers;
    private int[] tierPositions; 
    private Tier activeTier;
    private TierImpl rightClickTier;
    private int[] colorValues;
    
    private HashMap prefTierColors;
    private int tierHeight;
    private HashMap numAnnosPerTier;
    
    private JComponent resizer = null;
    
    private final Color demarcColor = new Color(200, 200, 200);
    
    private boolean showNumberOfAnnotations = true;
    private boolean reducedTierHeight = false;    
    private boolean disableInterlinearMI = false; 
    private boolean sortAlphabetically = false;
    private boolean hideAllTiers = false;
    private boolean preferenceChanged = false;    
    private boolean sortingChanged = false;    
    
    /**
     * PROBLEMS WHEN MORE THAN ONE MULTI TIER VIEWER IS VISIBLE
     *
     * @param transcription DOCUMENT ME!
     */
    public MultiTierControlPanel(Transcription transcription, TierOrder tierOrder) {
    	setLayout(null);
        dimension = new Dimension();
        visibleTiers = new Vector<TierImpl>();   
        displayableTiers = new Vector<TierImpl>();
        sortedTiers = new Vector<TierImpl>();
        displayableTierNodes = new Vector<TierTreeNode>();        
        viewers = new Vector<MultiTierViewer>();       
        this.tierOrder = tierOrder;
        setFont(Constants.DEFAULTFONT);

        sortMode = UNSORTED;
        oldSortMode = sortMode;

        //colorValues = new int[]{0, 128, 255};
        colorValues = new int[] { 0, 90, 160 };
        setTranscription(transcription);        		

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        setOpaque(true);
        setDoubleBuffered(true);
    }

    /**
     * Overrides <code>JComponent.setFont(Font)</code> by creating a bold
     * derivative  and <code>FontMetrics</code> objects for both Font objects.
     *
     * @param f DOCUMENT ME!
     */
    public void setFont(Font f) {
        super.setFont(f);
        tierHeight = 3 * f.getSize(); // first guess
        fontMetrics = getFontMetrics(getFont());
        boldFont = getFont().deriveFont(Font.BOLD);
        boldMetrics = getFontMetrics(boldFont);
    }
    
    public void setResizeComponent(JComponent comp) {
    	if (resizer != null) {
    		remove(resizer);
    	}
    	resizer = comp;
    	if (resizer != null) {
    		add(resizer);
    		resizer.setBounds(getWidth() - resizer.getWidth(), 1, resizer.getWidth(), resizer.getHeight());
    	}
    	repaint();
    }
    
    /**Enables/disables the Show Interlinear Viewer radio button
     * 
     * @param val true/ false
     */
    public void disableShowInterlinearViewer(boolean val){    	
    	disableInterlinearMI = val;
    	
    	if(val){
    		if(interlinearMI != null){
    			interlinearMI.setEnabled(false);
    		}    	
    		if(timelineMI != null){
    			timelineMI.setSelected(true);
    		}
    	} else{
    		if(interlinearMI != null){
    			interlinearMI.setEnabled(true);
    		}       		
    	}
    }

    /**
     * Set the viewer that must be controlled
     *
     * @param viewer DOCUMENT ME!
     */
    public void setViewer(MultiTierViewer viewer) {
        this.viewer = viewer;
        viewer.setMultiTierControlPanel(this);

        if (viewer instanceof TimeLineViewer) {
        	if (timelineMI != null) {
        		timelineMI.setSelected(true);	
        	}
        } else if (viewer instanceof InterlinearViewer) {
        	if (interlinearMI != null) {
        		interlinearMI.setSelected(true);
        	}            
        }
        
        // notify the viewer about the current displayable tiers
        // this results in a callback to setTierPositions
        if (displayableTiers != null) {
            setVisibleTiers(displayableTiers);           
        }

        if (activeTier != null) {
            setActiveTier(activeTier);
        }
    }

    /**
     * Add a viewer that must be controlled
     *
     * @param viewer DOCUMENT ME!
     */
    public void addViewer(MultiTierViewer viewer) {
    	
        viewers.add(viewer);
        viewer.setMultiTierControlPanel(this);

        // notify the viewer about the current displayable tiers
        // this results in a callback to setTierPositions
        if (displayableTiers != null) {
            setVisibleTiers(displayableTiers);
        }

        if (activeTier != null) {
            setActiveTier(activeTier);
        }
    }

    /**
     * Remove a viewer that must be controlled
     *
     * @param viewer DOCUMENT ME!
     */
    public void removeViewer(MultiTierViewer viewer) {
        viewer.setMultiTierControlPanel(null);
        viewers.remove(viewer);
    }

    /**
     * DOCUMENT ME!
     */
    public void updateLocale() {
        if (popup == null) {
            return;
        }

        hideAllMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.HideAllTiers"));
        showAllMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ShowAllTiers"));       
        showHideMoreMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ShowHideMore"));
        viewerMenu.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.Viewer"));
        visTiersMenu.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.VisibleTiers"));
        sortTiersMenu.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.SortTiers"));
        expandAllMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ExpandAll"));
        collapseAllMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.CollapseAll"));
        unsortedMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.Unsorted"));
        sortByHierarchMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.SortByHierarchy"));
        sortByTypeMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.SortByType"));
        sortByPartMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.SortByParticipant"));        
        sortByAnnotMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.SortByAnnotator"));
        sortAlphabeticallyMI.setText(ElanLocale.getString(
        		"MultiTierControlPanel.Menu.SortAlpabetically"));
        hideMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ThisTier.Hide"));
        changeMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ThisTier.Change"));
        applyMI.setText(ElanLocale.getString(
        	"MultiTierControlPanel.Menu.ThisTier.Apply"));
        deleteMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ThisTier.Delete"));
        colOrExpMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ThisTier.CollapseExpand"));
        showHierMI.setText(ElanLocale.getString(
                "MultiTierControlPanel.Menu.ThisTier.ShowInHierarchy"));
        numAnnosMI.setText(ElanLocale.getString(
        		"MultiTierControlPanel.Menu.NumberAnnotations"));
    }

    /**
     * Set the transcription and initialize all the components
     * 
     * @param transcription
     */
    private void setTranscription(Transcription transcription) {
        this.transcription = transcription;
        
        try {
            tiers = transcription.getTiers();
            tierNames = new Hashtable<TierImpl, String>();  
            prefTierColors = new HashMap();
            numAnnosPerTier = new HashMap();

            popup = new JPopupMenu("");

            //popup.setLightWeightPopupEnabled(true);
            viewerMenu = new JMenu();
            popup.add(viewerMenu);
            popup.addSeparator();

            visTiersMenu = new JMenu();
            
            MenuScroller scroller = MenuScroller.setScrollerFor(visTiersMenu);
            // 2 - hideAllMI, showAllMI           
            scroller.setTopFixedCount(2);
            
            hideAllMI = new JMenuItem("");
            hideAllMI.setActionCommand("hideAll");
            hideAllMI.addActionListener(this);
            visTiersMenu.add(hideAllMI);
            showAllMI = new JMenuItem("");
            showAllMI.setActionCommand("showAll");
            showAllMI.addActionListener(this);
            visTiersMenu.add(showAllMI);              
                       
            String tierName;
            JMenuItem menuItem;

            for (int i = 0; i < tiers.size(); i++) {
                TierImpl tier = (TierImpl) tiers.elementAt(i);
                tierName = tier.getName();
                tierNames.put(tier, tierName);                
                
                if (tier.getParentTier() == null) {
                    addToRootColors(tier);
                }
                
                menuItem = new JCheckBoxMenuItem(tierName);
                menuItem.setSelected(true);

                menuItem.setActionCommand(TIER_AC + tierName);

                // use an index because maybe a tier name is not unique?
                // a tiername should be unique...
                //menuItem.setActionCommand(Integer.toString(i));
                menuItem.addActionListener(this);
                visTiersMenu.add(menuItem);

                visibleTiers.add(tier);
            }  
            
            popup.add(visTiersMenu);
            
            showHideMoreMI = new JMenuItem();
            showHideMoreMI.setActionCommand("showHideMore");
            showHideMoreMI.addActionListener(this);
            popup.add(showHideMoreMI);
            
            sortedTiers.clear();
            sortedTiers.addAll(visibleTiers);
            
            displayableTiers.clear();
            displayableTiers.addAll(visibleTiers);            

            sortTiersMenu = new JMenu();

            ButtonGroup sortGroup = new ButtonGroup();
            unsortedMI = new JRadioButtonMenuItem();
            unsortedMI.setActionCommand("unsorted");
            unsortedMI.addActionListener(this);
            sortGroup.add(unsortedMI);
            sortTiersMenu.add(unsortedMI);
            sortByHierarchMI = new JRadioButtonMenuItem();
            sortByHierarchMI.setActionCommand("sortHier");
            sortByHierarchMI.addActionListener(this);
            sortGroup.add(sortByHierarchMI);
            sortTiersMenu.add(sortByHierarchMI);
            sortByTypeMI = new JRadioButtonMenuItem();
            sortByTypeMI.setActionCommand("sortType");
            sortByTypeMI.addActionListener(this);
            sortGroup.add(sortByTypeMI);
            sortTiersMenu.add(sortByTypeMI);     
            sortByPartMI = new JRadioButtonMenuItem();
            sortByPartMI.setActionCommand("sortPart");
            sortByPartMI.addActionListener(this);
            sortGroup.add(sortByPartMI);
            sortTiersMenu.add(sortByPartMI);
            sortByAnnotMI = new JRadioButtonMenuItem();
            sortByAnnotMI.setActionCommand("sortAnn");
            sortByAnnotMI.addActionListener(this);
            sortGroup.add(sortByAnnotMI);
            sortTiersMenu.add(sortByAnnotMI);
            sortTiersMenu.addSeparator();            
            sortAlphabeticallyMI = new JCheckBoxMenuItem();
            sortAlphabeticallyMI.addActionListener(this);
            sortTiersMenu.add(sortAlphabeticallyMI);
            
            switch (sortMode) {
            case UNSORTED:
                unsortedMI.setSelected(true);

                break;

            case SORT_BY_HIERARCHY:
                sortByHierarchMI.setSelected(true);

                break;

            case SORT_BY_PARTICIPANT:
                sortByPartMI.setSelected(true);

                break;

            case SORT_BY_LINGUISTIC_TYPE:
                sortByTypeMI.setSelected(true);

                break;
            }

            popup.add(sortTiersMenu);
            
            expandAllMI = new JMenuItem();
            expandAllMI.setActionCommand("expandAll");
            expandAllMI.addActionListener(this);
            popup.add(expandAllMI);
            collapseAllMI = new JMenuItem();
            collapseAllMI.setActionCommand("collapseAll");
            collapseAllMI.addActionListener(this);
            popup.add(collapseAllMI);            
            
            popup.addSeparator();

            hideMI = new JMenuItem();
            hideMI.setActionCommand("hideThis");
            hideMI.addActionListener(this);
            hideMI.setEnabled(false);
            popup.add(hideMI);
            changeMI = new JMenuItem();
            changeMI.setActionCommand("changeThis");
            changeMI.addActionListener(this);
            changeMI.setEnabled(false);
            popup.add(changeMI);
            applyMI = new JMenuItem();
            applyMI.setActionCommand("applyThis");
            applyMI.addActionListener(this);
            applyMI.setEnabled(false);
            popup.add(applyMI);
            deleteMI = new JMenuItem();
            deleteMI.setActionCommand("deleteThis");
            deleteMI.addActionListener(this);
            deleteMI.setEnabled(false);
            popup.add(deleteMI);
            colOrExpMI = new JMenuItem();
            colOrExpMI.setActionCommand("toggleExpandThis");
            colOrExpMI.addActionListener(this);
            colOrExpMI.setEnabled(false);
            popup.add(colOrExpMI);
            showHierMI = new JMenuItem();
            showHierMI.setActionCommand("showThis");
            showHierMI.addActionListener(this);
            showHierMI.setEnabled(false);
            popup.add(showHierMI);
            popup.addSeparator();
            numAnnosMI = new JCheckBoxMenuItem();
            numAnnosMI.setActionCommand("numAnnos");
            numAnnosMI.addActionListener(this);
            numAnnosMI.setSelected(true);
            popup.add(numAnnosMI);
            
            updateLocale();
           
            createSortedTree(false);

            if (visibleTiers.size() > 0 && Preferences.get("MultiTierViewer.ActiveTierName", 
    				transcription) == null) {
                setActiveTier((Tier) visibleTiers.get(0));
            }

            annotationsChanged();
            
            paintBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a node hierarchy depending on the current sorting mode.
     */
    private void createSortedTree(boolean storeTierOrder) {
    	if(sortAlphabetically){    		
    		Collections.sort(visibleTiers, new TierComparer());    		
    	}
    	
        // create a list based on the current preferred order
        ArrayList tierList = new ArrayList(sortedTiers);

//        for (int i = 0; i < tiers.size(); i++) {
//            TierImpl tier = (TierImpl) tiers.elementAt(i);
//
//            if (!tierList.contains(tier)) {
//                tierList.add(tier);
//            }
//        }

        sortedRootNode = new DefaultMutableTreeNode("sortRoot");
        tierTreeNodeMap = new HashMap<String, TierTreeNode>();

        switch (sortMode) {
        case SORT_BY_HIERARCHY:

            HashMap nodes = new HashMap();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);
                TierTreeNode n = new TierTreeNode(tier);

                if ((activeTier != null) && (activeTier == tier)) {
                    n.setActive(true);
                }

                if (!visibleTiers.contains(tier)) {                	
                    n.setVisible(false);
                }

                tierTreeNodeMap.put(tier.getName(), n);
                nodes.put(tier, n);
            }

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                if (tier.getParentTier() == null) {
                    sortedRootNode.add((TierTreeNode) nodes.get(tier));
                } else {
                    ((TierTreeNode) nodes.get(tier.getParentTier())).add((TierTreeNode) nodes.get(
                            tier));
                }
            }
            
            

            break;

        case SORT_BY_PARTICIPANT:
        	
        	HashMap participantMap = new HashMap();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                TierTreeNode n = new TierTreeNode(tier);

                if ((activeTier != null) && (activeTier == tier)) {
                    n.setActive(true);
                }

                if (!visibleTiers.contains(tier)) {
                    n.setVisible(false);
                }

                String part = tier.getParticipant();

                if (part.length() == 0) {
                    part = NOT_SPECIFIED;
                }

                if (participantMap.get(part) == null) {
                    ArrayList list = new ArrayList();
                    list.add(n);
                    participantMap.put(part, list);
                } else {
                    ((ArrayList) participantMap.get(part)).add(n);
                }
                
                tierTreeNodeMap.put(tier.getName(), n);
            }

            if (participantMap.size() > 0) {
                Set keys = participantMap.keySet();
                List names = new ArrayList(keys);
                Collections.sort(names);

                for (int j = 0; j < names.size(); j++) {
                    ArrayList pList = (ArrayList) participantMap.get(names.get(
                                j));

                    for (int k = 0; k < pList.size(); k++) {
                        sortedRootNode.add((TierTreeNode) pList.get(k));
                    }
                }
            }

            break;

        case SORT_BY_ANNOTATOR:

            HashMap annotMap = new HashMap();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                TierTreeNode n = new TierTreeNode(tier);

                if ((activeTier != null) && (activeTier == tier)) {
                    n.setActive(true);
                }

                if (!visibleTiers.contains(tier)) {
                    n.setVisible(false);
                }

                String annot = tier.getAnnotator();

                if ((annot == null) || (annot.length() == 0)) {
                    annot = NOT_SPECIFIED;
                }

                if (annotMap.get(annot) == null) {
                    ArrayList list = new ArrayList();
                    list.add(n);
                    annotMap.put(annot, list);
                } else {
                    ((ArrayList) annotMap.get(annot)).add(n);
                }
                
                tierTreeNodeMap.put(tier.getName(), n);
            }

            if (annotMap.size() > 0) {
                Set keys = annotMap.keySet();
                List names = new ArrayList(keys);
                Collections.sort(names);

                for (int j = 0; j < names.size(); j++) {
                    ArrayList pList = (ArrayList) annotMap.get(names.get(j));

                    for (int k = 0; k < pList.size(); k++) {
                        sortedRootNode.add((TierTreeNode) pList.get(k));
                    }
                }
            }

            break;

        case SORT_BY_LINGUISTIC_TYPE:

            HashMap typeMap = new HashMap();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                TierTreeNode n = new TierTreeNode(tier);

                if ((activeTier != null) && (activeTier == tier)) {
                    n.setActive(true);
                }

                if (!visibleTiers.contains(tier)) {
                    n.setVisible(false);
                }

                LinguisticType type = tier.getLinguisticType();

                if (type == null) {
                    type = new LinguisticType(NOT_SPECIFIED);
                }

                if (typeMap.get(type) == null) {
                    ArrayList list = new ArrayList();
                    list.add(n);
                    typeMap.put(type, list);
                } else {
                    ((ArrayList) typeMap.get(type)).add(n);
                }
                
                tierTreeNodeMap.put(tier.getName(), n);
            }

            if (typeMap.size() > 0) {
                //Set vals = typeMap.entrySet();
                Iterator typeIt = typeMap.values().iterator();

                while (typeIt.hasNext()) {
                    ArrayList typeList = (ArrayList) typeIt.next();

                    for (int k = 0; k < typeList.size(); k++) {
                        sortedRootNode.add((TierTreeNode) typeList.get(k));
                    }
                }
            }

            break;

        case UNSORTED:
        // fallthrough default order
        default:

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                TierTreeNode n = new TierTreeNode(tier);

                if ((activeTier != null) && (activeTier == tier)) {
                    n.setActive(true);
                }

                 if (!visibleTiers.contains(tier)) {
                    n.setVisible(false);
                }
                 tierTreeNodeMap.put(tier.getName(), n);
                sortedRootNode.add(n);
            }
        }
        
        if(storeTierOrder){
        	storeTierOrder();
        }
        
        // update the visibletiers and displayablenodes vector
        updateDisplayableTiers();
    }

    /**
     * Updates the displayableTiers vector and the Vector with the (flattened) tier
     * nodes.
     */
    private void updateDisplayableTiers() {
        Vector dispTiers = new Vector();       
        displayableTierNodes.clear();
        displayableTiers.clear();
        if ((sortMode == SORT_BY_HIERARCHY) &&
                (sortedRootNode.getChildCount() > 0)) {
            int level = -1;
            TierTreeNode curNode = (TierTreeNode) sortedRootNode.getFirstChild();
            DefaultMutableTreeNode parent = null;
mainloop: 
            while (level != 0) {
                if (curNode.isVisible()) {
                    //visibleTiers.add(curNode.getTier());
                    displayableTierNodes.add(curNode);
                    dispTiers.add(curNode.getTier());

                    if ((curNode.getChildCount() > 0) && curNode.isExpanded()) {
                        curNode = (TierTreeNode) curNode.getFirstChild();
                        level = curNode.getLevel();

                        continue mainloop;
                    } else {
                        // no children or children are not displayable because node is collapsed,
                        // traverse up
                        parent = (DefaultMutableTreeNode) curNode.getParent();
                    }
                } else {
                    // not visible; skip the children of this node, traverse up
                    parent = (DefaultMutableTreeNode) curNode.getParent();
                }

uploop: 
                while (true) {
                    if (parent.getChildAfter(curNode) != null) {
                        curNode = (TierTreeNode) parent.getChildAfter(curNode);

                        continue mainloop;
                    } else if (parent == sortedRootNode) {
                        // the rootnode does not have more children
                        break mainloop;
                    } else {
                        curNode = (TierTreeNode) parent;
                        level = curNode.getLevel();
                        parent = (DefaultMutableTreeNode) curNode.getParent();

                        continue uploop;
                    }
                }
            }
        } else {
            Enumeration nodeEnum = sortedRootNode.children();
            TierTreeNode cn = null;

            while (nodeEnum.hasMoreElements()) {
                cn = (TierTreeNode) nodeEnum.nextElement();

                if (cn.isVisible()) {
                    //visibleTiers.add(cn.getTier());
                    displayableTierNodes.add(cn);
                    dispTiers.add(cn.getTier());
                }
            }
        }
        
//        if(preferenceChanged){
//        	if(!hideAllTiers && dispTiers.size()==0){    		    	
//        		if (visibleTiers.size() > 0) {
//        			String message ="Cant sort the selected tiers by this sort type.";
//        			JOptionPane.showMessageDialog(this.getRootPane(), message,
//        					ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);	
//        			undoSorting();
//        		} else {    	        			
//        			updateVisibleTiersMenu(dispTiers);
//       	     		setVisibleTiers(dispTiers);       	     		
//        		}
//        	} else {
//    		 updateVisibleTiersMenu(dispTiers);
//    	     setVisibleTiers(dispTiers);     	     
//    		}  
//        }
        
        displayableTiers.addAll(dispTiers);
        updateVisibleTiersMenu(dispTiers);
	    setVisibleTiers(dispTiers); 
    }

    /**
     * Adds the MultiTier Viewer items to the popup (when neccessary) and
     * enables / disables menu items depending on the characteristics of  the
     * specified tier.
     *
     * @param tier the tier
     */
    private void updatePopup(TierImpl tier) {
        if (timelineMI == null) {
            ButtonGroup multitierViewerGroup = new ButtonGroup();
            timelineMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                        transcription, ELANCommandFactory.SHOW_TIMELINE));
            interlinearMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                        transcription, ELANCommandFactory.SHOW_INTERLINEAR));

            // default
            timelineMI.setSelected(true);
            multitierViewerGroup.add(timelineMI);
            multitierViewerGroup.add(interlinearMI);
            viewerMenu.add(timelineMI);
            viewerMenu.add(interlinearMI);   

            if (viewer instanceof TimeLineViewer) {
                timelineMI.setSelected(true);
            } else if (viewer instanceof InterlinearViewer) {
                interlinearMI.setSelected(true);
            }
        }
        
        if(disableInterlinearMI){
        	interlinearMI.setEnabled(false);
        	timelineMI.setSelected(disableInterlinearMI);        	
        }

        if (tier != null) {
            String name = tier.getName();

            if ((name != null) && (name.length() > 0)) {
                hideMI.setText("<html>" +
                    ElanLocale.getString(
                        "MultiTierControlPanel.Menu.ThisTier.Hide") + " " +
                    "<i>" + name + "</i></html>");
                changeMI.setText("<html>" +
                    ElanLocale.getString(
                        "MultiTierControlPanel.Menu.ThisTier.Change") + " " +
                    "<i>" + name + "</i></html>");
                applyMI.setText("<html>" +
                        ElanLocale.getString(
                            "MultiTierControlPanel.Menu.ThisTier.Apply") + " " +
                        "<i>" + name + "</i></html>");
                deleteMI.setText("<html>" +
                    ElanLocale.getString(
                        "MultiTierControlPanel.Menu.ThisTier.Delete") + " " +
                    "<i>" + name + "</i></html>");

                if (sortMode == SORT_BY_HIERARCHY) {
                    colOrExpMI.setText("<html>" +
                        ElanLocale.getString(
                            "MultiTierControlPanel.Menu.ThisTier.CollapseExpand") +
                        " " + "<i>" + name + "</i></html>");
                    colOrExpMI.setEnabled(true);
                } else {
                    colOrExpMI.setText(ElanLocale.getString(
                            "MultiTierControlPanel.Menu.ThisTier.CollapseExpand"));
                    colOrExpMI.setEnabled(false);
                }

                hideMI.setEnabled(true);
                changeMI.setEnabled(true);
                applyMI.setEnabled(true);
                deleteMI.setEnabled(true);
                showHierMI.setEnabled(true);
            }
        } else {
            hideMI.setText(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ThisTier.Hide"));
            changeMI.setText(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ThisTier.Change"));
            applyMI.setText(ElanLocale.getString(
            		"MultiTierControlPanel.Menu.ThisTier.Apply"));
            deleteMI.setText(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ThisTier.Delete"));
            colOrExpMI.setText(ElanLocale.getString(
                    "MultiTierControlPanel.Menu.ThisTier.CollapseExpand"));
            hideMI.setEnabled(false);
            changeMI.setEnabled(false);
            applyMI.setEnabled(false);
            deleteMI.setEnabled(false);
            colOrExpMI.setEnabled(false);
            showHierMI.setEnabled(false);
        }

        if (sortMode == SORT_BY_HIERARCHY) {
            expandAllMI.setEnabled(true);
            collapseAllMI.setEnabled(true);
        } else {
            expandAllMI.setEnabled(false);
            collapseAllMI.setEnabled(false);
        }
    }

    /**
     * Sets the y positions of the visible tiers.
     *
     * @param tierPositions the y positions of the tiers
     */
    public void setTierPositions(int[] tierPositions) {
        this.tierPositions = tierPositions;

        if (tierPositions.length > 1) {
            tierHeight = tierPositions[1] - tierPositions[0];
            if (tierHeight > 0) {//initially the tier height is sometimes < 0
	            if (tierHeight < 2.5 * getFont().getSize()) {
	            	reducedTierHeight = true;
	            } else {
	            	reducedTierHeight = false;
	            }
            }
        }

        paintBuffer();
    }
    
    /**
 	* Updates the annotation count in the menu's (except for the time line viewer)
 	*/
	private void updateAnnotationCounts(){			
		if( transcription != null ){
			//initialize the tier menu string formatter with the known tiers
			TierMenuStringFormatter.InitializeWithTierList(transcription.getTiers());
			
			for(int i=0; i<visTiersMenu.getMenuComponentCount(); i++){
				Component c = visTiersMenu.getMenuComponent(i);
			
				if( c instanceof JCheckBoxMenuItem ){
					JCheckBoxMenuItem mi = (JCheckBoxMenuItem) c;
				
					//retrieve the tier name
					String tierName = mi.getText().trim();
				
					//the regular expression to test whether the string ends with brackets
					String regExp = ".*\\[\\d*\\]";
				
					if( Pattern.matches(regExp, tierName) )
						//filter away the brackets (to get the plain tiername)
						tierName = tierName.substring(0, tierName.lastIndexOf("[")).trim();
					else if( tierName.startsWith("<html>") ){
						int endIndex = tierName.indexOf("</td>");
						int beginIndex = tierName.substring(0, endIndex).lastIndexOf('>');
						tierName = tierName.substring(beginIndex+1, endIndex);
					}
					//now add the new annotation count behind it
					//System.out.println("size = " + t.);
					TierImpl t = (TierImpl) transcription.getTierWithId(tierName);
					//if( t == null )
					//	System.out.println("Tier == null. Searched for " + tierName);
					
					if(t != null){
						int size = t.getNumberOfAnnotations();					
						mi.setText( TierMenuStringFormatter.GetFormattedString(tierName, "[" + size + "]") );
					}
				}
			}
		}
	}

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            return;
        }

        Point p = e.getPoint();

        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() > 1)) {
            // define the active tier
            int index = getClosestTierIndexForMouseY(p.y);
            TierTreeNode node = (TierTreeNode) displayableTierNodes.get(index);
            setActiveTier(node.getTier());
        }

        paintBuffer();
    }

    /**
     * Start dragging a tier.
     *
     * @param e the mousePressed event
     */
    public void mousePressed(MouseEvent e) {
        //Point p = e.getPoint();
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            // present a menu to set visible tiers
            // maybe the menu drops out of the screen, correct it if needed
            //SwingUtilities.convertPointToScreen(p, this);
            int y = e.getPoint().y;

            int tierIndex = getClosestTierIndexForMouseY(y);

            if (!(tierIndex >= displayableTierNodes.size()) &&
                    (displayableTierNodes.size() > 0)) {
                TierTreeNode node = (TierTreeNode) displayableTierNodes.get(tierIndex);
                TierImpl tier = null;

                if (node != null) {
                    tier = node.getTier();
                    rightClickTier = tier;
                }

                // set the menu right from the panel so it does not hide the labels
                // when this panel is created LayoutManager maybe null
                // the viewer action access the LayoutManager
                updatePopup(tier);
            } else {
                rightClickTier = null;
                updatePopup(null);
            }

            popup.show(this, e.getPoint().x, y);

            return;
        }

        dragIndex = getClosestTierIndexForMouseY(e.getPoint().y);

        if ((dragIndex == 0) && (displayableTierNodes.size() == 0)) {
            return;
        } else {
            dragLabel = (String) tierNames.get(((TierTreeNode) displayableTierNodes.elementAt(
                        dragIndex)).getTier());
            dragging = true;
        }
    }

    /**
     * Drag a tier label.
     *
     * @param e the mouseDragged event
     */
    public void mouseDragged(MouseEvent e) {
        // this part should only be done once

        /*
           if (!dragging) {
               dragging = true;
        
                       dragIndex = getClosestTierIndexForMouseY(e.getPoint().y);
        
                       if ((dragIndex == 0) && (displayableTierNodes.size() == 0)) {
                           dragLabel = "";
                       } else {
                           dragLabel = (String) tierNames.get( ((TierTreeNode)displayableTierNodes.elementAt(dragIndex)).getTier() );
                       }
                   }
         */
        if (dragging) {
            dragX = e.getPoint().x - 40;
            dragY = e.getPoint().y;
            repaint();
        }
    }

    /**
     * When this is the end of a drag operation, move a tier or a group of
     * tiers,  depending on the current sorting mode.
     *
     * @param e the mouse release event
     */
    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            if (displayableTierNodes.size() <= 1) {
                dragging = false;
                dragLabel = "";
                repaint();

                return;
            }

            // re-order the visible tiers
            // insert the dragged tier at the release index
            int index = getInsertionIndexForMouseY(e.getPoint().y);

            if (index >= displayableTierNodes.size()) {
                index = displayableTierNodes.size() - 1;
            }

            if ((index != dragIndex) && ((index - 1) != dragIndex)) {
                TierTreeNode node = (TierTreeNode) displayableTierNodes.elementAt(dragIndex);
                TierTreeNode insertBeforeNode = (TierTreeNode) displayableTierNodes.get(index);
                TierTreeNode insertAfterNode = null;

                if ((index - 1) >= 0) {
                    insertAfterNode = (TierTreeNode) displayableTierNodes.get(index -
                            1);
                }

                if (index == (displayableTierNodes.size() - 1)) {
                    insertBeforeNode = null;
                    insertAfterNode = (TierTreeNode) displayableTierNodes.get(index);
                }

                /*
                   System.out.print("drag tier: " + node.getTierName());
                   if (insertAfterNode != null) System.out.print(" - insertaftertier: " + insertAfterNode.getTierName());
                       else System.out.print(" insertaftertier: null");
                   if (insertBeforeNode != null) System.out.println(" - insertbeforetier: " + insertBeforeNode.getTierName()http://www.ammerlaan.demon.nl/foreignstudents.htm);
                       else System.out.println(" insertbeforetier: null");
                   System.out.print("drag parent: " + node.getParent());
                   if (insertAfterNode != null) System.out.print(" - parent after: " + insertAfterNode.getParent());
                   if (insertBeforeNode != null)System.out.println(" - parent before: " + insertBeforeNode.getParent());
                   System.out.println("");
                 */

                //System.out.println("Node: " + node.getTierName() + " after: " + insertAfterNode.getTierName() + " before: " + 
                //(insertBeforeNode == null ? "null" : insertBeforeNode.getTierName()));
                switch (sortMode) {
                case SORT_BY_HIERARCHY:

                    if ((insertAfterNode != null) &&
                            (node.getParent() == insertAfterNode.getParent())) {
                        node.removeFromParent();

                        int afterIndex = insertAfterNode.getParent()
                                                        .getIndex(insertAfterNode);
                        ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                            afterIndex + 1);
                    } else if ((insertBeforeNode != null) &&
                            (node.getParent() == insertBeforeNode.getParent())) {
                        int beforeIndex = insertBeforeNode.getParent()
                                                          .getIndex(insertBeforeNode);
                        ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                            beforeIndex);
                    } else if ((insertAfterNode != null) &&
                            (node.getParent() == insertAfterNode.getSharedAncestor(
                                node))) {
                        int curLevel = node.getLevel();
                        DefaultMutableTreeNode sameLevelNode = insertAfterNode;

                        while (sameLevelNode.getLevel() != curLevel) {
                            sameLevelNode = (DefaultMutableTreeNode) sameLevelNode.getParent();
                        }

                        node.removeFromParent();

                        int afterIndex = sameLevelNode.getParent()
                                                      .getIndex(sameLevelNode);
                        ((DefaultMutableTreeNode) sameLevelNode.getParent()).insert(node,
                            afterIndex + 1);
                    }

                    break;

                case SORT_BY_PARTICIPANT:

                    // first try within group
                    if ((insertAfterNode != null) &&
                            (insertBeforeNode != null)) {
                        if (insertAfterNode.getTier().getParticipant()
                                               .equals(node.getTier()
                                                               .getParticipant())) {
                            node.removeFromParent();

                            int afterIndex = insertAfterNode.getParent()
                                                            .getIndex(insertAfterNode);
                            ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                                afterIndex + 1);

                            break;
                        } else if (insertBeforeNode.getTier().getParticipant()
                                                       .equals(node.getTier()
                                                                       .getParticipant())) {
                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);
                            ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                                beforeIndex);

                            break;
                        }
                    }

                    if (insertAfterNode != null) {
                        if (insertAfterNode.getTier().getParticipant()
                                               .equals(node.getTier()
                                                               .getParticipant())) {
                            node.removeFromParent();

                            int afterIndex = insertAfterNode.getParent()
                                                            .getIndex(insertAfterNode);
                            ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                                afterIndex + 1);
                        } else {
                            // move group
                            String afterPartName = insertAfterNode.getTier()
                                                                  .getParticipant();
                            String dragPartName = node.getTier().getParticipant();
                            TierTreeNode lastNode = insertAfterNode;

                            while ((lastNode.getNextNode() != null) &&
                                    ((TierTreeNode) lastNode.getNextNode()).getTier()
                                         .getParticipant().equals(afterPartName)) {
                                lastNode = (TierTreeNode) lastNode.getNextNode();
                            }

                            ArrayList moveGroup = new ArrayList();
                            Enumeration en = sortedRootNode.preorderEnumeration();
                            en.nextElement();

                            TierTreeNode nn;

                            while (en.hasMoreElements()) {
                                nn = (TierTreeNode) en.nextElement();

                                if (nn.getTier().getParticipant()
                                          .equals(dragPartName)) {
                                    moveGroup.add(nn);
                                }
                            }

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) moveGroup.get(i)).removeFromParent();
                            }

                            int afterIndex = lastNode.getParent()
                                                     .getIndex(lastNode);

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) lastNode.getParent()).insert((DefaultMutableTreeNode) moveGroup.get(
                                        i), afterIndex + 1);
                            }
                        }
                    } else if (insertBeforeNode != null) {
                        if (insertBeforeNode.getTier().getParticipant()
                                                .equals(node.getTier()
                                                                .getParticipant())) {
                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);
                            ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                                beforeIndex);
                        } else {
                            // move group , before node should be the first node
                            String dragPartName = node.getTier().getParticipant();
                            ArrayList moveGroup = new ArrayList();
                            Enumeration en = sortedRootNode.preorderEnumeration();
                            en.nextElement();

                            TierTreeNode nn;

                            while (en.hasMoreElements()) {
                                nn = (TierTreeNode) en.nextElement();

                                if (nn.getTier().getParticipant()
                                          .equals(dragPartName)) {
                                    moveGroup.add(nn);
                                }
                            }

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) moveGroup.get(i)).removeFromParent();
                            }

                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert((DefaultMutableTreeNode) moveGroup.get(
                                        i), beforeIndex);
                            }
                        }
                    }

                    break;

                case SORT_BY_LINGUISTIC_TYPE:

                    // first try to move within group
                    if ((insertAfterNode != null) &&
                            (insertBeforeNode != null)) {
                        if (insertAfterNode.getTier().getLinguisticType()
                                               .equals(node.getTier()
                                                               .getLinguisticType())) {
                            node.removeFromParent();

                            int afterIndex = insertAfterNode.getParent()
                                                            .getIndex(insertAfterNode);
                            ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                                afterIndex + 1);

                            break;
                        } else if (insertBeforeNode.getTier().getLinguisticType()
                                                       .equals(node.getTier()
                                                                       .getLinguisticType())) {
                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);
                            ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                                beforeIndex);

                            break;
                        }
                    }

                    if (insertAfterNode != null) {
                        if (insertAfterNode.getTier().getLinguisticType()
                                               .equals(node.getTier()
                                                               .getLinguisticType())) {
                            // move within group 
                            node.removeFromParent();

                            int afterIndex = insertAfterNode.getParent()
                                                            .getIndex(insertAfterNode);
                            ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                                afterIndex + 1);
                        } else {
                            // move group
                            LinguisticType insertType = insertAfterNode.getTier()
                                                                       .getLinguisticType();
                            LinguisticType dragType = node.getTier()
                                                          .getLinguisticType();
                            TierTreeNode lastNode = insertAfterNode;

                            while ((lastNode.getNextNode() != null) &&
                                    ((TierTreeNode) lastNode.getNextNode()).getTier()
                                         .getLinguisticType().equals(insertType)) {
                                lastNode = (TierTreeNode) lastNode.getNextNode();
                            }

                            ArrayList moveGroup = new ArrayList();
                            Enumeration en = sortedRootNode.preorderEnumeration();
                            en.nextElement();

                            TierTreeNode nn;

                            while (en.hasMoreElements()) {
                                nn = (TierTreeNode) en.nextElement();

                                if (nn.getTier().getLinguisticType()
                                          .equals(dragType)) {
                                    moveGroup.add(nn);
                                }
                            }

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) moveGroup.get(i)).removeFromParent();
                            }

                            int afterIndex = lastNode.getParent()
                                                     .getIndex(lastNode);

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) lastNode.getParent()).insert((DefaultMutableTreeNode) moveGroup.get(
                                        i), afterIndex + 1);
                            }
                        }
                    } else if (insertBeforeNode != null) {
                        if (insertBeforeNode.getTier().getLinguisticType()
                                                .equals(node.getTier()
                                                                .getLinguisticType())) {
                            // move within group
                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);
                            ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                                beforeIndex);
                        } else {
                            // move group , before node should be the first node
                            LinguisticType dragType = node.getTier()
                                                          .getLinguisticType();
                            ArrayList moveGroup = new ArrayList();
                            Enumeration en = sortedRootNode.preorderEnumeration();
                            en.nextElement();

                            TierTreeNode nn;

                            while (en.hasMoreElements()) {
                                nn = (TierTreeNode) en.nextElement();

                                if (nn.getTier().getLinguisticType()
                                          .equals(dragType)) {
                                    moveGroup.add(nn);
                                }
                            }

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) moveGroup.get(i)).removeFromParent();
                            }

                            int beforeIndex = insertBeforeNode.getParent()
                                                              .getIndex(insertBeforeNode);

                            for (int i = moveGroup.size() - 1; i >= 0; i--) {
                                ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert((DefaultMutableTreeNode) moveGroup.get(
                                        i), beforeIndex);
                            }
                        }
                    }

                    break;

                case UNSORTED:

                    if (insertAfterNode != null) {
                        node.removeFromParent();

                        int afterIndex = insertAfterNode.getParent()
                                                        .getIndex(insertAfterNode);
                        ((DefaultMutableTreeNode) insertAfterNode.getParent()).insert(node,
                            afterIndex + 1);
                    } else if (insertBeforeNode != null) {
                        int beforeIndex = insertBeforeNode.getParent()
                                                          .getIndex(insertBeforeNode);
                        ((DefaultMutableTreeNode) insertBeforeNode.getParent()).insert(node,
                            beforeIndex);
                    }

                    break;

                default:
                    return;
                }

                updateDisplayableTiers();
            }
//            sortAlphabetically = false;
//        	sortAlphabeticallyMI.setSelected(false);
        	
            if(sortAlphabetically){
            	setSorting(sortMode);
            }
            dragging = false;
            dragLabel = "";
            paintBuffer();
            storeTierOrder();
        }
    }

    private int getClosestTierIndexForMouseY(int y) {
        if ((tierPositions.length == 0) || (displayableTierNodes.size() == 0)) {
            return 0;
        }

        int index;

        for (index = 0; index < displayableTierNodes.size(); index++) {
            if (y < tierPositions[index]) {
                break;
            }
        }

        if (index > 0) {
            index--;

            if ((index == displayableTierNodes.size()) && (index != 0)) {
                index--;
            }
        }

        // determine to which label the mouse is closest
        float d1 = Math.abs(y - tierPositions[index]);
        float d2 = Float.MAX_VALUE;

        if ((index + 1) < displayableTierNodes.size()) {
            d2 = Math.abs(y - tierPositions[index + 1]);
        }

        if (d2 < d1) {
            index++;
        }

        return index;
    }

    private int getInsertionIndexForMouseY(int y) {
        int index = 0;

        if (displayableTierNodes.size() > 0) {
            for (index = 0; index < displayableTierNodes.size(); index++) {
                if (y < tierPositions[index]) {
                    break;
                }
            }
        }

        return index;
    }
    
    /**
     * Handle action events from menu's.
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if(e.getSource() == sortAlphabeticallyMI){        	
        	int sortMode = getSorting();
        	if(sortAlphabeticallyMI.isSelected()){
        		sortAlphabetically = true;
        	}else {
        		sortAlphabetically = false;
        	}  
        	setPreference("MultiTierViewer.SortAlpabetically", 
        			sortAlphabetically, transcription);        	
        	setSorting(sortMode);        	
        } else  if (command.equals("hideAll")) {
            hideAllTiers();
        } else if (command.equals("showAll")) {
            showAllTiers();
        } else if (command.equals("showHideMore")) {
        	ShowHideMoreTiersDlg dialog = new ShowHideMoreTiersDlg(transcription, visibleTiers, this);    
        	dialog.setVisible(true);
        	if(dialog.isValueChanged()){
        		updateVisibleTiers(dialog.getVisibleTierNames());
        	}
        } else if (command.equals("unsorted")) {
        	sortingChanged = true;
            setSorting(UNSORTED);
        } else if (command.equals("sortHier")) {
        	sortingChanged = true;
            setSorting(SORT_BY_HIERARCHY);
        } else if (command.equals("sortPart")) {
        	sortingChanged = true;
            setSorting(SORT_BY_PARTICIPANT);           
        } else if (command.equals("sortType")) {
        	sortingChanged = true;
        	setSorting(SORT_BY_LINGUISTIC_TYPE);        	
        } else if (command.equals("sortAnn")) {
        	sortingChanged = true;
            setSorting(SORT_BY_ANNOTATOR);
        } else if (command.equals("expandAll")) {
            expandAllNodes();
        } else if (command.equals("collapseAll")) {
            collapseAllNodes();
        } else if (command.equals("hideThis")) {
            hideTier(rightClickTier);
        } else if (command.equals("changeThis")) {
            changeTier(rightClickTier);
        } else if (command.equals("applyThis")) {
            applyTier(rightClickTier);
        } else if (command.equals("deleteThis")) {
            deleteTier(rightClickTier);
        } else if (command.equals("toggleExpandThis")) {
            toggleExpandedState(rightClickTier);
        } else if (command.equals("showThis")) {
            showInTierDependencyFrame(rightClickTier);
        } else if (command.equals("numAnnos")) {
        		showNumberOfAnnotations = numAnnosMI.isSelected();
        		setPreference("MultiTierViewer.ShowNumberOfAnnotations", new Boolean(showNumberOfAnnotations), 
        				transcription);
        		paintBuffer();
        } else if (command.startsWith(TIER_AC)) {
            // a tier visibility checkbox menu item           
            String tierName = command.substring(TIER_AC.length());
            boolean visible = ((JCheckBoxMenuItem)e.getSource()).isSelected();
            TierTreeNode node = tierTreeNodeMap.get(tierName);
            if (node != null) {
            	node.setVisible(visible);
            	if(visible){
                	if(!visibleTiers.contains(node.getTier())){
                		visibleTiers.add(node.getTier());
                	}
                } else {
                	visibleTiers.remove(node.getTier());
                }
            }
               
            if(visibleTiers.size() == 0){
    			hideAllTiers = true;
    		}else {
    			hideAllTiers = false;
    		}
            
            
            int numberOfDisplayableTiers = displayableTiers.size();
            updateDisplayableTiers();
            
            if(visible){
            	if(!displayableTiers.contains(node.getTier())){ //((JCheckBoxMenuItem)e.getSource()).getForeground() == Color.GRAY)     
            		String message = ElanLocale.getString("MultiTierControlPanel.SelectedTier.SingleTier.Part1") + tierName + 
            			ElanLocale.getString("MultiTierControlPanel.SelectedTier.SingleTier.Part2") + " " + this.getSortModeDescription(sortMode);
            		checkForNonDisplayableVisibleTiers(false, message, node.getTier());	       
            	}
            } else {
            	//making a tier insvisible can also hide its visible child tiers
            	 if((numberOfDisplayableTiers-1) > displayableTiers.size()) {   
            		 JPanel panel = new JPanel(new GridLayout(2,1));
            		 panel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.DeselectedTier.Message.Part1")+ tierName + 
            				 ElanLocale.getString("MultiTierControlPanel.DeselectedTier.Message.Part2") + 
            				 this.getSortModeDescription(sortMode) +"."));
            		 panel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.DeselectedTier.MakeItVisible.Part1")+ tierName +
            				 ElanLocale.getString("MultiTierControlPanel.DeselectedTier.MakeItVisible.Part2")));
            		 
            		int selectedValue = JOptionPane.showConfirmDialog(null, panel, ElanLocale.getString("Message.Warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                  	if(selectedValue == JOptionPane.OK_OPTION){
                  		node.setVisible(true);
                  		visibleTiers.add(node.getTier());
                  		updateDisplayableTiers();
                 	}else{
                 		return;
                 	}
            	}
            }
            
            storeHiddenTiers();
        }  
    }  
    
    /**
     * Inform the viewers about the new visible tiers and determine the
     * activeTierIndex.
     *
     * @param tiers the displayable tiers
     */
    private void setVisibleTiers(List tiers){
        //this.visibleTiers = tiers;
        //		viewer.setVisibleTiers(tiers);
    	
        for (int i = 0; i < viewers.size(); i++) {
            ((MultiTierViewer) viewers.elementAt(i)).setVisibleTiers(tiers);
        }

        paintBuffer();
    }

    /**
     * Returns the Vector of currently visible tiers. The ordering of the Tiers
     * in the Vector  reflects the order they appear in the
     * MultiTierControlPanel and the connected Viewer.
     *
     * @return a Vector containing the visible Tiers
     */
    public List getVisibleTiers() {
        return visibleTiers;
    }
    
    /**
     * Returns a vector containing the tier names in the order they appear in
     * the panel.
     *
     * @return tier names in a vector in the order they appear on the screen
     */
    public List getTierOrder() {
    	
    	if(tierOrder != null){
    		return tierOrder.getTierOrder();
    	}
    	
        ArrayList visibleTierNames = new ArrayList();

        for (int i = 0; i < visibleTiers.size(); i++) {
            visibleTierNames.add(((Tier) visibleTiers.elementAt(i)).getName());
        }
        
        return visibleTierNames;
    }

    /**
     * Sets the tiers in a specific order.
     *
     * @param tierNames a List containing the ordered names of the tiers
     */
    private void setTierOrder(List tierNames) {    	
    	
        sortMode = UNSORTED; // change the mode to unordered

        if (!unsortedMI.isSelected()) {
            unsortedMI.setSelected(true);
        }
        
        updateVisibleTierMenu();
        createSortedTree(false);
    }

    /**
	 * Sets the active tier.
	 *
	 * @param name the name of the active tier
	 */
	public void setActiveTierForName(String name) {
	    if (name == null) {
	        return;
	    }
	
	    for (int i = 0; i < tiers.size(); i++) {
	        if (name.equals(((Tier) tiers.elementAt(i)).getName())) {
	            setActiveTier((Tier) tiers.elementAt(i));
	        }
	    }
	}

	/**
     * Returns the name of the active tier.
     *
     * @return the name of the active tier or null
     */
    public String getActiveTierName() {
        if (activeTier == null) {
            return null;
        }

        return activeTier.getName();
    }

    /**
     * Sets the mode for the sorting of the tiers.
     *
     * @param mode the sorting mode
     */
    public void setSorting(int mode) {
    	if(sortAlphabetically){
    		sortAlphabeticallyMI.setSelected(true);
    	} else{
    		sortAlphabeticallyMI.setSelected(false);
    	}
        
    	oldSortMode = sortMode;

        if ((mode < UNSORTED) || (mode > SORT_BY_ANNOTATOR)) {
            sortMode = UNSORTED;
        } else {
            sortMode = mode;
        }

        // update the menuitems, if necessary
        switch (sortMode) {
        case UNSORTED:

            if (!unsortedMI.isSelected()) {
                unsortedMI.setSelected(true);
            }  
            break;

        case SORT_BY_HIERARCHY:

            if (!sortByHierarchMI.isSelected()) {
                sortByHierarchMI.setSelected(true);
            }
            break;

        case SORT_BY_LINGUISTIC_TYPE:
        	
            if (!sortByTypeMI.isSelected()) {
                sortByTypeMI.setSelected(true);
            }
            break;

        case SORT_BY_PARTICIPANT:

            if (!sortByPartMI.isSelected()) {
                sortByPartMI.setSelected(true);
            }
            break;

        case SORT_BY_ANNOTATOR:

            if (!sortByAnnotMI.isSelected()) {
                sortByAnnotMI.setSelected(true);
            }
        }

        setPreference("MultiTierViewer.TierSortingMode", new Integer(sortMode), 
        		transcription);
        
        createSortedTree(true);
        
        if(preferenceChanged && sortingChanged){
        	String message;
        	if(displayableTierNodes.size() == 0){
        		message = ElanLocale.getString("MultiTierControlPanel.SortingChanged.NoTierDisplayed");
        	} else {
        		message = ElanLocale.getString("MultiTierControlPanel.SortingChanged.FewTiersNotDisplayed");
        	}
        	message = message + " " + getSortModeDescription(sortMode);        		
        	checkForNonDisplayableVisibleTiers(true, message, null);	
        }
        
        sortingChanged = false;
    }
    
    /**
     * Undo sorting - tries to sort in the last 
     * used sorting mode , else sorts to unsorted
     * 
     */
    private void undoSorting(){  
    	if(oldSortMode == sortMode){
    		setSorting(UNSORTED);
    	}else {
    		setSorting(oldSortMode);
    	}  
    }
    
    
    private String getSortModeDescription(int mode){      
    	String sortMode = null;
    	switch(mode){
    	case UNSORTED:
    		sortMode = ElanLocale.getString(
                    "MultiTierControlPanel.Menu.Unsorted");
    		break;
    	case SORT_BY_HIERARCHY:
    		sortMode = ElanLocale.getString(
                    "MultiTierControlPanel.Menu.SortByHierarchy");
    		break;
    	case SORT_BY_PARTICIPANT:
    		sortMode = ElanLocale.getString(
                    "MultiTierControlPanel.Menu.SortByParticipant");
    		break;
    	case SORT_BY_LINGUISTIC_TYPE:
    		sortMode = ElanLocale.getString(
                    "MultiTierControlPanel.Menu.SortByType");
    		break;
    	case SORT_BY_ANNOTATOR:
    		sortMode = ElanLocale.getString(
                    "MultiTierControlPanel.Menu.SortByAnnotator");
    		break;
    		
//    		default:
//    			sortMode = ElanLocale.getString(
//                        "MultiTierControlPanel.Menu.Unsorted");
    	}
		return sortMode;
    }

    /**
     * Returns the sorting mode.<br>
     * One of <code>UNSORTED</code>,  <code>SORT_BY_HIERARCHY</code>,
     * <code>SORT_BY_PARTICIPANT</code> or
     * <code>SORT_BY_LINGUISTIC_TYPE</code>.
     *
     * @return the sorting mode
     */
    public int getSorting() {
        return sortMode;
    }

    /**
     * The MultiTierControlPanel is not an ACMEditListener but gets informed of
     * relevant changes by the connected viewer.<br>
     * When a tier is added this panel should update it's list of visible
     * tiers taking into account the current ordering of the tiers and notify
     * the viewer.
     *
     * @param tier the new tier
     */
    public void tierAdded(TierImpl tier) {
        String name;
        JMenuItem menuItem;

        name = tier.getName();

        if (!tierNames.contains(tier)) {
            tierNames.put(tier, name);
            
            TierTreeNode node = new TierTreeNode(tier);
            
            if (tier.getParentTier() == null) {
                addToRootColors(tier);               
            }

            menuItem = new JCheckBoxMenuItem(name);
            menuItem.setSelected(true);
            menuItem.addActionListener(this);
            menuItem.setActionCommand(TIER_AC + name);
            visTiersMenu.add(menuItem);  
            tierTreeNodeMap.put(name, node);
            
            switch (sortMode) {
            case UNSORTED:
                sortedRootNode.add(node);
                break;

            case SORT_BY_HIERARCHY:

                if (tier.getParentTier() == null) {
                    sortedRootNode.add(node);
                } else {
                    Enumeration en = sortedRootNode.preorderEnumeration();

                    // skip root node
                    en.nextElement();

                    TierTreeNode nextNode;

                    while (en.hasMoreElements()) {
                        nextNode = (TierTreeNode) en.nextElement();

                        if (nextNode.getTier() == tier.getParentTier()) {
                            nextNode.add(node);

                            break;
                        }
                    }
                }

                break;

            case SORT_BY_PARTICIPANT:

                String parti = tier.getParticipant();

                // append to the participants group, or to the root
                boolean groupFound = false;
                Enumeration partEnum = sortedRootNode.children();
                TierTreeNode nextNode;
                TierTreeNode lastInGroupNode = null;

                while (partEnum.hasMoreElements()) {
                    nextNode = (TierTreeNode) partEnum.nextElement();
                    if (nextNode.getTier().getParticipant().equals(parti)) {
                        groupFound = true;
                        lastInGroupNode = nextNode;
                    } else{
                    	if(groupFound){
                            break;
                    	}
                    }
                }

                if (!groupFound) {
                    sortedRootNode.add(node);
                }else{
                	int index = sortedRootNode.getIndex(lastInGroupNode);
                	if(index < sortedRootNode.getChildCount()-1){
                		sortedRootNode.insert(node, index + 1);
                	}else{
                		 sortedRootNode.add(node);
                	}
                }

                break;

            case SORT_BY_LINGUISTIC_TYPE:

                LinguisticType type = tier.getLinguisticType();

                // append to the participants group, or to the root
                boolean typesFound = false;               
                Enumeration typesEnum = sortedRootNode.children();
                TierTreeNode nextTypeNode;
                TierTreeNode lastInTypeGroupNode = null;

                while (typesEnum.hasMoreElements()) {
                    nextTypeNode = (TierTreeNode) typesEnum.nextElement();

                    if (((type == null) &&
                            (nextTypeNode.getTier().getLinguisticType() == null)) ||
                            type.equals(nextTypeNode.getTier()
                                                        .getLinguisticType())) {
                        typesFound = true;   
                        lastInTypeGroupNode = nextTypeNode;                       
                    } else {
                    	if(typesFound){                    		
                             break;
                    	}
                    }
                }

                if (!typesFound) {                	
                    sortedRootNode.add(node);
                }else{
                	int index = sortedRootNode.getIndex(lastInTypeGroupNode);
                	if(index < sortedRootNode.getChildCount()-1){
                		sortedRootNode.insert(node, index + 1);
                	}else{
                		 sortedRootNode.add(node);
                	}
                }

                break;
                
            case SORT_BY_ANNOTATOR:
            	String annotator = tier.getAnnotator();

                // append to the annotators group, or to the root
                groupFound = false;
                partEnum = sortedRootNode.children();
                nextNode = null;
                lastInGroupNode = null;

                while (partEnum.hasMoreElements()) {
                    nextNode = (TierTreeNode) partEnum.nextElement();
                    if (nextNode.getTier().getParticipant().equals(annotator)) {
                        groupFound = true;
                        lastInGroupNode = nextNode;
                    } else{
                    	if(groupFound){
                            break;
                    	}
                    }
                }

                if (!groupFound) {
                    sortedRootNode.add(node);
                }else{
                	int index = sortedRootNode.getIndex(lastInGroupNode);
                	if(index < sortedRootNode.getChildCount()-1){
                		sortedRootNode.insert(node, index + 1);
                	}else{
                		 sortedRootNode.add(node);
                	}
                }

                break;
            }
            
            visibleTiers.add(tier);           
            annotationsChanged();
            storeTierOrder();
            updateDisplayableTiers();   
            
            if(sortAlphabetically){
            	setSorting(sortMode);
            }
            // if this is the first tier, make it active
            if (visibleTiers.size() == 1) {
                setActiveTier(tier);
            }
        }
    }

    /**
     * The MultiTierControlPanel is not an ACMEditListener but gets informed of
     * relevant changes by the connected viewer.<br>
     * When a tier is removed this panel should update it's list of visible
     * tiers and notify the viewer.
     *
     * @param tier the removed tier
     */
    public void tierRemoved(TierImpl tier) {
        if (tierNames.containsKey(tier)) {
            String name = (String) tierNames.get(tier);
            MenuElement menu = visTiersMenu.getSubElements()[0];
            MenuElement[] items = menu.getSubElements();

            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof JCheckBoxMenuItem) {
                    if (((JCheckBoxMenuItem) items[i]).getText().equals(name)) {
                        visTiersMenu.remove((JCheckBoxMenuItem) items[i]);

                        break;
                    }
                }
            }           
            
            tierNames.remove(tier);
            prefTierColors.remove(tier);
            numAnnosPerTier.remove(name);
            visibleTiers.remove(tier);
            
            
//            Enumeration nodeEnum = sortedRootNode.preorderEnumeration();
//
//            // skip root node
//            nodeEnum.nextElement();

            TierTreeNode node = tierTreeNodeMap.get(tier.getName());
            ((DefaultMutableTreeNode) node.getParent()).remove(node);
//            while (nodeEnum.hasMoreElements()) {
//                node = (TierTreeNode) nodeEnum.nextElement();
//
//                if (node.getTier() == tier) {
//                    ((DefaultMutableTreeNode) node.getParent()).remove(node);
//
//                    break;
//                }
//            }
            
            //sortedRootNode.remove(tierTreeNodeMap.get(tier.getName()));
            tierTreeNodeMap.remove(tier.getName());
           
            updateDisplayableTiers();
            storeTierOrder();
            storeHiddenTiers();
            
            if(sortAlphabetically){
            	setSorting(sortMode);
            }
            if (activeTier == tier) {
                if (visibleTiers.size() > 0) {
                    setActiveTier((Tier) visibleTiers.get(0));
                } else {
                	setActiveTier(null);
                }
            }
        }
    }

    /**
     * The MultiTierControlPanel is not an ACMEditListener but gets informed of
     * relevant changes by the connected viewer.<br>
     * When a tier is changed this panel should update it's list of visible
     * tiers and notify the viewer. The control panel seems to responsible of
     * finding out what has changed and whether the change has consequences
     * for the panel. Important are changes in: the name, the parent or the
     * participant.
     *
     * @param tier the tier that has been changed
     */
    public void tierChanged(TierImpl tier) {
        // for now only the tier name matters
        String name = tier.getName();
        String oldName = (String) tierNames.get(tier);

        if (!name.equals(oldName)) {
            //update popup
            MenuElement menu = visTiersMenu.getSubElements()[0];
            MenuElement[] items = menu.getSubElements();

            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof JCheckBoxMenuItem) {
                    if (((JCheckBoxMenuItem) items[i]).getText().equals(oldName)) {
                        ((JCheckBoxMenuItem) items[i]).setText(name);
                        ((JCheckBoxMenuItem) items[i]).setActionCommand(TIER_AC +
                            name);

                        break;
                    }
                }
            }
         
            TierTreeNode node = tierTreeNodeMap.get(oldName);            
            if (node != null) {
                node.setTierName(name);
            }
            
            tierTreeNodeMap.put(name, node);
            tierTreeNodeMap.remove(oldName);

            
            // replace the name string
            tierNames.put(tier, name);
            annotationsChanged();
            if(sortAlphabetically){
            	setSorting(sortMode);
            }
            paintBuffer();
            storeHiddenTiers();
            storeTierOrder();
        } else {
            // the name of the tier did not change, maybe parent or participant
            createSortedTree(false);
        }
    }

    /**
     * Tells the ControlPanel which is the new active tier.<br>
     * The panel updates it's own state and notifies the attached viewer.
     *
     * @param tier the new active tier
     */
    public void setActiveTier(Tier tier) {
        activeTier = tier;

        Enumeration e = sortedRootNode.preorderEnumeration();

        // skip the root node
        e.nextElement();

        while (e.hasMoreElements()) {
            TierTreeNode node = (TierTreeNode) e.nextElement();

            if (node.getTier() == activeTier) {
                node.setActive(true);
            } else {
                node.setActive(false);
            }
        }

        // notify viewers
        for (int j = 0; j < viewers.size(); j++) {
            ((MultiTierViewer) viewers.elementAt(j)).setActiveTier(tier);
        }

        paintBuffer();
        if (activeTier != null) {
            setPreference("MultiTierViewer.ActiveTierName", activeTier.getName(), 
            		transcription);
        } else {
            setPreference("MultiTierViewer.ActiveTierName", null, 
            		transcription);
        }
    }

    /**
     * Returns the currently active tier.
     *
     * @return the active Tier, can be null
     */
    public Tier getActiveTier() {
        return activeTier;
    }

    /**
     * Sets the next or previous tier active, relative to the current active
     * tier. If there is no tier active, the first displayable tier is
     * activated.
     *
     * @param next if true, set the tier below the current tier active,
     *        otherwise set the  tier above the current tier active
     */
    public void setNextActiveTier(boolean next) {
        if (displayableTierNodes.size() == 0) {
            return;
        }

        if (activeTier == null) {
            if (displayableTierNodes.size() > 0) {
                TierTreeNode node = (TierTreeNode) displayableTierNodes.get(0);
                setActiveTier(node.getTier());
            }
        } else {
            // if the current active tier is displayable, move to the next/previous
            // displayable tier
            TierTreeNode node = null;
            Tier tier = null;

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);
                tier = node.getTier();

                if (tier == activeTier) {
                    if (!next) {
                        if (i > 0) {
                            node = (TierTreeNode) displayableTierNodes.get(i -
                                    1);
                            setActiveTier(node.getTier());
                        }

                        return;
                    } else {
                        if (i < (displayableTierNodes.size() - 1)) {
                            node = (TierTreeNode) displayableTierNodes.get(i +
                                    1);
                            setActiveTier(node.getTier());
                        }

                        return;
                    }
                }
            }

            // if we get here the active tier is not displayable, 
            // jump to the next displayable tier
            Enumeration e = sortedRootNode.preorderEnumeration();
            // skip the root node
            e.nextElement();

            while (e.hasMoreElements()) {
                node = (TierTreeNode) e.nextElement();

                if (node.getTier() == activeTier) {
                    if (!next) {
                        // traverse up to a displayable tier
                        while ((node.getParent() != null) &&
                                !((DefaultMutableTreeNode) node.getParent()).isRoot()) {
                            node = (TierTreeNode) node.getParent();

                            if (displayableTierNodes.contains(node)) {
                                setActiveTier(node.getTier());

                                return;
                            }
                        }
                    } else {
                        // traverse down to the next displayable tier
                        while (e.hasMoreElements()) {
                            node = (TierTreeNode) e.nextElement();

                            if (displayableTierNodes.contains(node)) {
                                setActiveTier(node.getTier());

                                return;
                            }
                        }
                    }

                    return;
                }
            }
        }
    }

    /**
     * Creates a edit tier dialog for the specified tier.
     *
     * @param tier the tier to edit
     */
    private void changeTier(TierImpl tier) {
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EDIT_TIER);

        Object[] args = new Object[] { new Integer(EditTierDialog2.CHANGE), tier };

        c.execute(transcription, args);
    }
    
    /**
     * Creates a edit tier dialog for the specified tier.
     *
     * @param tier the tier to edit
     */
    private void applyTier(TierImpl tier) {
    	AdvancedAttributeSettingOptionDialog dialog = new AdvancedAttributeSettingOptionDialog(
    			ELANCommandFactory.getRootFrame(transcription),
    			ElanLocale.getString("EditTierDialog.Title.Apply"), transcription, tier.getName());
    	dialog.setVisible(true);
    }

    /**
     * Creates a single edit dialog to delete the specified tier.
     *
     * @param tier the tier to delete.
     */
    private void deleteTier(TierImpl tier) {
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EDIT_TIER);

        Object[] args = new Object[] { new Integer(EditTierDialog2.DELETE), tier };

        c.execute(transcription, args);
    }

    /**
     * Shows the tier dependency frame and tries to select the specified tier.
     *
     * @param tier the Tier to select in the dependency tree.
     */
    private void showInTierDependencyFrame(TierImpl tier) {
        CommandAction ca = ELANCommandFactory.getCommandAction(transcription,
                ELANCommandFactory.TIER_DEPENDENCIES);
        ca.actionPerformed(null);

        String name = tier.getName();
        if(!visibleTiers.contains(tier)){
        	visibleTiers.add(tier);
        }

        if (ca instanceof TierDependenciesCA) {
            JTree tree = ((TierDependenciesCA) ca).getTree();
            if (tree != null) {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                                                                           .getRoot();
                Enumeration en = root.preorderEnumeration();
                DefaultMutableTreeNode target;

                while (en.hasMoreElements()) {
                    target = (DefaultMutableTreeNode) en.nextElement();

                    if ((target.getUserObject() != null) &&
                            target.getUserObject().equals(name)) {
                        TreeNode[] path = target.getPath();
                        tree.setSelectionPath(new TreePath(path));
                    }
                }
            }
        }
    }

    /**
     * Makes all tiers visible.<br> Does not change ordering.
     */
    private void showAllTiers() {
    		
        for (int i = 0; i < tiers.size(); i++) {
        	if (!visibleTiers.contains(tiers.get(i))) {
        		visibleTiers.add((TierImpl) tiers.get(i));
            }
        }        
         
        MenuElement menu = visTiersMenu.getSubElements()[0];
        MenuElement[] items = menu.getSubElements();

        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem) items[i]).setSelected(true);
            }
        }

        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();
            node.setVisible(true);
        }

        hideAllTiers = false;
        updateDisplayableTiers();
        storeHiddenTiers();
    }

    /**
     * Hides all tiers.<br>
     */
    private void hideAllTiers() {
    	//visibleTiers.clear();
        MenuElement menu = visTiersMenu.getSubElements()[0];
        MenuElement[] items = menu.getSubElements();

        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem) items[i]).setSelected(false);
            }
        }

        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();
            node.setVisible(false);
        }
        visibleTiers.clear();
        hideAllTiers = true;
        updateDisplayableTiers();
        storeHiddenTiers();        
    }

    /**
     * Saves the explicitely hidden tiers in the preferences file. This is 
     * to prevent that tiers that have been deleted from a transcription, without
     * the transcription being saved, are invisible when the transcription is 
     * reloaded. Based on the menus.
     */
    private void storeHiddenTiers() {
    	if (sortedRootNode != null) {
    		ArrayList tNames = new ArrayList(tiers.size());
	        Enumeration en = sortedRootNode.preorderEnumeration();
	        en.nextElement();
	
	        TierTreeNode nn;
	
	        while (en.hasMoreElements()) {
	            nn = (TierTreeNode) en.nextElement();
	            if (!nn.isVisible()) {
	            	tNames.add(nn.getTierName());
	            }	            
	        }
	        setPreference("MultiTierViewer.HiddenTiers", tNames, transcription);	     
    	}
    }
    
    /**
     * Stores the order of all the tiers.
     */
    private void storeTierOrder() {    	
    	if (sortedRootNode != null) {
    		ArrayList tNames = new ArrayList(tiers.size());
	        Enumeration en = sortedRootNode.preorderEnumeration();
	        en.nextElement();
	
	        TierTreeNode nn;
	
	        while (en.hasMoreElements()) {
	            nn = (TierTreeNode) en.nextElement();
	            tNames.add(nn.getTierName());
	        }	        
	        setPreference("MultiTierViewer.TierOrder", tNames, transcription);
	        if(tierOrder != null){
	        	tierOrder.setTierOrder(tNames);	 
	        	updateVisibleTierMenu();
	        	
	        	sortedTiers.clear();
				for(int i=0; i< tNames.size(); i++){
					TierImpl tier = (TierImpl) transcription.getTierWithId(tNames.get(i).toString());
					if(tier != null){
						sortedTiers.add(tier);
					}
				}
	        }
    	}
    } 
    
    
    /**
     * Stores the tiers that are collapsed in the hierarchical view.
     */
    private void storeCollapsedTiers() {
    	if (sortedRootNode != null) {
    		ArrayList tNames = new ArrayList(tiers.size());
	        Enumeration en = sortedRootNode.preorderEnumeration();
	        en.nextElement();
	
	        TierTreeNode nn;
	
	        while (en.hasMoreElements()) {
	            nn = (TierTreeNode) en.nextElement();
	            if (!nn.isLeaf() && !nn.isExpanded()) {
	            	tNames.add(nn.getTierName());
	            }	            
	        }
	        setPreference("MultiTierViewer.CollapsedTiers", tNames, transcription);
    	}
    }
    
    /**
     * Updates the order of the tiers in 
     * the visible tiers menu 
     */
    private void updateVisibleTierMenu() {
    	
        JMenuItem menuItem;
        
        if(tierOrder != null){
        	List tierOrderList = tierOrder.getTierOrder();
        	if(tierOrderList != null){
        		MenuElement menu = visTiersMenu.getSubElements()[0];
        	    MenuElement[] items = menu.getSubElements();
            	if(items !=null){
            		for(int i=0; i < items.length; i++){
            			String commandName = ((JMenuItem)items[i]).getActionCommand();
            			if(commandName.startsWith((TIER_AC))){
            				visTiersMenu.remove(((JMenuItem)items[i]));
            			}
            		}
            	}
            	
        		for(int i=0; i < tierOrderList.size(); i++){
        			String tierName = (String) tierOrderList.get(i);
        			menuItem = new JCheckBoxMenuItem(tierName);
        			if(visibleTiers.contains(transcription.getTierWithId(tierName))){
        				menuItem.setSelected(true);
        			} else{
        				menuItem.setSelected(false);
        			}
        			menuItem.setActionCommand(TIER_AC + tierName);
        			menuItem.addActionListener(this);
        			visTiersMenu.add(menuItem);
        		}
        	}
        }
        
	}
    
    /**
     * Updates the visible tiers in the visible tiers menu
     * 
     * @param visTiers, list of displayable tiers
     */
    private void updateVisibleTiersMenu(Vector visTiers) {
    	// update the menu's
        MenuElement menu = visTiersMenu.getSubElements()[0];
        MenuElement[] items = menu.getSubElements();
        JCheckBoxMenuItem checkBoxMI;

        for (int i = 0; i < items.length; i++) {
        	if (items[i] instanceof JCheckBoxMenuItem) {
        		checkBoxMI = (JCheckBoxMenuItem) items[i];
        		String tierName = checkBoxMI.getText();        		
        		if (visTiers.contains(transcription.getTierWithId(tierName))){
        			checkBoxMI.setSelected(true);
        			checkBoxMI.setForeground(Color.BLACK);
                } else if(visibleTiers.contains(transcription.getTierWithId(tierName))){
                	checkBoxMI.setSelected(true);
                	checkBoxMI.setForeground(Color.GRAY);
                	checkBoxMI.setToolTipText(ElanLocale.getString("MultiTierControlPanel.NonDisplayableTiers.ToolTipText"));
                } else {
                	checkBoxMI.setSelected(false);
                	checkBoxMI.setForeground(Color.BLACK);
                }
        	}
        }
    }
    
    /**
     * Updates the visible tiers and tiers menu items. Called from a dialog
     * that  gives the user the possibility to change the visibility of more
     * tiers at a time.
     *
     * @param visTierNames a Vector with the names of the visible tiers
     */
    protected void updateVisibleTiers(Vector visTierNames) {
    	if(visTierNames ==null){
    		return;
    	}
    	
    	visibleTiers.clear();
        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();

            if (visTierNames.contains(node.getTierName())) {
                node.setVisible(true);
                visibleTiers.add(node.getTier());
            } else {
                node.setVisible(false);
            }
        }
        
        if(visibleTiers.size() == 0){
			hideAllTiers = true;
		}else {
			hideAllTiers = false;
		}
        
        storeTierOrder();
        updateDisplayableTiers();
        storeHiddenTiers();    
       
        String message;
       	if(displayableTierNodes.size() == 0){
    		message = ElanLocale.getString("MultiTierControlPanel.SelectedTier.NoTierDisplayed");
    	} else {
    		message = ElanLocale.getString("MultiTierControlPanel.SelectedTier.FewTiersNotDisplayed");
    	}
    	message = message + " " + getSortModeDescription(sortMode);        		
    	checkForNonDisplayableVisibleTiers(false, message, null);	
    }
    
    /**
     * Checks for tiers that are visible but cannot be displayed
     * in the current sorting mode
     * 
     * @param message,  message which explains about the non-displayable 
     * 					tier if any      
     */
    private void checkForNonDisplayableVisibleTiers(boolean isSortingChanged, String message, TierImpl tier){
    	 if(visibleTiers.size() > displayableTiers.size()) {           	
         	JRadioButton lastSortTypeRB = null;
         	JRadioButton makeItVisibleRB = null;
         	JRadioButton doNothingRB = null;
         	JCheckBox makeItVisibleCB = null;
         	
         	JPanel panel = new JPanel();
         	panel.setLayout(new GridBagLayout());         	  	
         	
         	GridBagConstraints gbc = new GridBagConstraints();
         	gbc.gridx= 0;
         	gbc.gridy = 1;
         	gbc.anchor = gbc.NORTHWEST;
         	gbc.fill = gbc.NONE;
         	gbc.insets = new Insets(2,0,2,0);
         	panel.add(new JLabel(message), gbc);         	
         	         	
         	if(isSortingChanged){
         		int oldMode = oldSortMode;
         	
         		if(oldSortMode == sortMode){
         			oldMode = UNSORTED;
         		}
         		lastSortTypeRB = new JRadioButton(ElanLocale.getString("MultiTierControlPanel.SortingChanged.SwitchToLastSortMode") + " " + this.getSortModeDescription(oldMode), true);
             	gbc.gridy = gbc.RELATIVE;
         		gbc.insets = new Insets(4,6,4,2);
         		panel.add(lastSortTypeRB, gbc);
         	
         		makeItVisibleRB = new JRadioButton(ElanLocale.getString("MultiTierControlPanel.SortingChanged.MakeitVisible"), true);
         		gbc.gridy = gbc.RELATIVE;
         		panel.add(makeItVisibleRB,gbc);
         		makeItVisibleRB.setSelected(false);
         		
         		gbc.gridy = gbc.RELATIVE;
         		gbc.insets = new Insets(0,35,0,0);
         		panel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.MakeitVisible.part2")), gbc);
         		
         		doNothingRB = new JRadioButton(ElanLocale.getString("MultiTierControlPanel.SortingChanged.DoNothing"), true);
         		gbc.gridy = gbc.RELATIVE;
         		gbc.insets = new Insets(4,6,4,2);
         		panel.add(doNothingRB,gbc);
         		doNothingRB.setSelected(false);         		
         		
         		ButtonGroup group = new ButtonGroup();
             	group.add(lastSortTypeRB);
             	group.add(makeItVisibleRB);   
             	group.add(doNothingRB);   
         	}else {
         		makeItVisibleCB = new JCheckBox(ElanLocale.getString("MultiTierControlPanel.SelectedTier.MakeitVisible"), true);
         		gbc.gridy = gbc.RELATIVE;
         		panel.add(makeItVisibleCB, gbc);
         		
         		gbc.gridy = gbc.RELATIVE;
         		gbc.insets = new Insets(0,35,0,0);
         		panel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.MakeitVisible.part2")), gbc);
         	}
     		     		     	
         	int selectedValue = JOptionPane.showConfirmDialog(null, panel, ElanLocale.getString("Message.Warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
         	if(selectedValue != JOptionPane.OK_OPTION){
         		return;
         	}
         	
         	if(isSortingChanged){
         		if(lastSortTypeRB.isSelected()){
             		undoSorting();
             	}else if(makeItVisibleRB.isSelected()){
             		makeNonDisplayableTiersVisible(tier);
             	} else if(doNothingRB.isSelected()){
             		return;
             	}
         	}else {
         		if(makeItVisibleCB.isSelected()){
             		makeNonDisplayableTiersVisible(tier);
             	}
         	}
         }
    }   
    
    /**
     * Displays all the non-displayable tiers
     * by making all its parent tier visible
     */
    private void makeNonDisplayableTiersVisible(TierImpl tier){    
    	
    	ArrayList<TierImpl> nonDisplayableTiers = new ArrayList<TierImpl>();
    	
    	if(tier != null){
    		nonDisplayableTiers.add(tier);
    	}else{
    		// get all the tiers that cannot be displayed
        	for(int i=0; i < visibleTiers.size(); i++){
        		if(!displayableTiers.contains(visibleTiers.get(i))){
        			nonDisplayableTiers.add(visibleTiers.get(i));
        		}
        	}
    	}    	
    	
    	// make all the parent tiers visible
    	TierImpl t;
    	for(int i=0; i < nonDisplayableTiers.size(); i++){
    		t = nonDisplayableTiers.get(i);
    		while (t.hasParentTier()){
    			t = (TierImpl) t.getParentTier();
    			if(!visibleTiers.contains(t)){
    				visibleTiers.add(t);
    				tierTreeNodeMap.get(t.getName()).setVisible(true);
    			}
    		}
    	}
    	  
    	updateDisplayableTiers();
    	storeHiddenTiers();   
    }

    /**
     * Sets a single tier to invisible.
     *
     * @param tier the tier to set invisible
     */
    private void hideTier(TierImpl tier) {
        if (tier == null) {
            return;
        }
        
        if( visibleTiers.contains(tier)){
        	visibleTiers.remove(tier);
        }

        // toggle the menuitem
        String name = tier.getName();

        MenuElement menu = visTiersMenu.getSubElements()[0];
        MenuElement[] items = menu.getSubElements();

        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JCheckBoxMenuItem) {
                if (((JCheckBoxMenuItem) items[i]).getText().equals(name)) {
                    ((JCheckBoxMenuItem) items[i]).setSelected(false);

                    break;
                }
            }
        }

        TierTreeNode node = tierTreeNodeMap.get(tier.getName());
        
        if (node!= null) {
        	node.setVisible(false);
        }


        if(visibleTiers.contains(tier)){
        	visibleTiers.remove(tier);
        }
        
        updateDisplayableTiers();
        storeHiddenTiers();
    }

    /**
     * Sets all tier-nodes to expanded.
     */
    private void expandAllNodes() {
        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();
            node.setExpanded(true);
        }

        updateDisplayableTiers();
        storeCollapsedTiers();
    }

    /**
     * Sets all tier-nodes to collapsed.
     */
    private void collapseAllNodes() {
        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();
            node.setExpanded(false);
        }

        updateDisplayableTiers();
        storeCollapsedTiers();
    }

    /**
     * Toggles the expanded state of a single tier.
     *
     * @param tier the tier-node to expand or collapse
     */
    private void toggleExpandedState(TierImpl tier) {
        if (tier == null) {
            return;
        }

        Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

        // skip root node
        nodeEnum.nextElement();

        TierTreeNode node;

        while (nodeEnum.hasMoreElements()) {
            node = (TierTreeNode) nodeEnum.nextElement();

            if (node.getTier() == tier) {
                node.setExpanded(!node.isExpanded());

                break;
            }
        }

        updateDisplayableTiers();
        storeCollapsedTiers();
    }

    /**
     * Adds a tier - color pair to the table of root tier colors.<br>
     * Tries to create unique colors as much as possible.
     *
     * @param t the TierImpl to add to the table
     */
    private void addToRootColors(TierImpl t) {
        int i = 0;
        if(prefTierColors.get(t) != null){
        	return;
        }
        Color c = getRandomColor();

        while (true) {
            if ((c.getRed() == c.getGreen()) && (c.getRed() == c.getBlue())) {
                // || (c.getRed() + c.getGreen() + c.getBlue() > 2 * colorValues[2] + colorValues[1] - 1)
                //don't permit gray colors
                c = getRandomColor();
                i++;

                continue;
            }

            if (!prefTierColors.values().contains(c)) {
            	prefTierColors.put(t, c);
            	storeColorPref(t.getName(), c);
                break;
            } else if (i > 50) {
            	prefTierColors.put(t, c);     
            	storeColorPref(t.getName(), c);
                break; //prevent an endless loop
            }

           else {
                c = getRandomColor();
                i++;
            }
        }
    }
    
    private void storeColorPref(String tierName, Color c){
    	Object val = Preferences.get("TierColors", transcription);
    	HashMap map = null;
    	if(val == null || !(val instanceof HashMap)){
    		map = new HashMap();
    	} else{
    		map = (HashMap)val;
    	}
    	if(map.get(tierName) == null){
    		map.put(tierName, c);
    		Preferences.set("TierColors", map, transcription);
    	}
    }

    /**
     * Create a truncated String of a label to display in the panel.
     *
     * @param string the label's value
     * @param width the available width for the String
     * @param fMetrics the font metrics
     *
     * @return the truncated String
     */
    private String truncateString(String string, int width, FontMetrics fMetrics) {
        String line = string;

        if (fMetrics != null) {
            int stringWidth = fMetrics.stringWidth(line);

            if (stringWidth > (width - 4)) { // truncate

                int i = 0;
                String s = "";
                int size = line.length();

                while (i < size) {
                    if (fMetrics.stringWidth(s) > (width - 4)) {
                        break;
                    } else {
                        s = s + line.charAt(i++);
                    }
                }

                if (!s.equals("")) {
                    line = s.substring(0, s.length() - 1);
                } else {
                    line = s;
                }
            }
        }

        return line;
    }
    
    /**
     * Stores the number of annotations per tier.
     */
    public void annotationsChanged() {
    	if (transcription != null) {
    		TierImpl ti = null;
    		int numTiers = transcription.getTiers().size();
    		
    		for (int i = 0; i < numTiers; i++) {
    			ti = (TierImpl) transcription.getTiers().get(i);
    			numAnnosPerTier.put(ti.getName(), "[" + String.valueOf(ti.getAnnotations().size()) + "]");
    		}    			
    		
    		updateAnnotationCounts();
    			
    		paintBuffer();
    	}
    }

    /*
     * Swing method, maybe our own layoutable methods will make this one obsolete
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getMinimumSize() {
        dimension.setSize(MultiTierControlPanel.MIN_WIDTH, ((JComponent) viewer).getHeight());

        return dimension;
    }

    /*
     * Swing method, maybe our own layoutable methods will make this one obsolete
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPreferredSize() {
        dimension.setSize(3 * MultiTierControlPanel.MIN_WIDTH, ((JComponent) viewer).getHeight());

        return dimension;
    }

    /**
     * Not very useful...
     *
     * @return DOCUMENT ME!
     */
//    public int getWidth() {
//        return WIDTH;
//    }

    /**
	 * Create a semi-random color based on three predefined color values.<br>
	 * Based on the restrictions in this method there are 16 legal colors.
	 *
	 * @return a new color
	 */
	private Color getRandomColor() {
	    int r = colorValues[(int) (Math.random() * 3)];
	    int g = colorValues[(int) (Math.random() * 3)];
	    int b = colorValues[(int) (Math.random() * 3)];
	
	    return new Color(r, g, b);
	}

	/**
     * Creates a BufferedImage when necessary and paints the tierlabels in this
     * buffer.
     */
    private void paintBuffer() {        	
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if ((displayableTierNodes == null) || (tierPositions == null) ||
                (displayableTierNodes.size() > tierPositions.length)) {
            return;
        }

        if ((bi == null) || (bi.getWidth() != getWidth()) ||
                (bi.getHeight() != getHeight())) {
            bi = new BufferedImage(getWidth(), getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        }

        big2d = bi.createGraphics();
        if (SystemReporting.antiAliasedText) {
	        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        big2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        if (tierPositions.length > 1) {
            big2d.setColor(demarcColor);

            int y = tierPositions[0] + (tierHeight / 2);

            for (int i = 0; i < tierPositions.length; i++) {
                big2d.drawLine(0, y, bi.getWidth(), y);
                y += tierHeight;
            }
        }

        int panelWidth = getWidth();
        int availableLabelWidth = panelWidth - (2 * MARGIN);
        TierTreeNode node;

        switch (sortMode) {
        case SORT_BY_HIERARCHY:

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);
                paintLabelHierarchically(big2d, node, i, availableLabelWidth);
            }

            break;

        case SORT_BY_PARTICIPANT:

            boolean startNewGroup = true;
            TierTreeNode prevNode;

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);

                if (i > 0) {
                    prevNode = (TierTreeNode) displayableTierNodes.get(i - 1);

                    if (node.getTier().getParticipant()
                                .equals(prevNode.getTier().getParticipant())) {
                        startNewGroup = false;
                    } else {
                        startNewGroup = true;
                    }
                }

                paintLabelInBlock(big2d, node, i, availableLabelWidth,
                    startNewGroup);
            }

            break;

        case SORT_BY_ANNOTATOR:

            boolean startAnnGroup = true;
            TierTreeNode prevAnnNode;

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);

                if (i > 0) {
                    prevAnnNode = (TierTreeNode) displayableTierNodes.get(i -
                            1);

                    if (node.getTier().getAnnotator()
                                .equals(prevAnnNode.getTier().getAnnotator())) {
                        startAnnGroup = false;
                    } else {
                        startAnnGroup = true;
                    }
                }

                paintLabelInBlock(big2d, node, i, availableLabelWidth,
                    startAnnGroup);
            }

            break;

        case SORT_BY_LINGUISTIC_TYPE:

            boolean startLinGroup = true;
            TierTreeNode prevLinNode;

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);

                if (i > 0) {
                    prevLinNode = (TierTreeNode) displayableTierNodes.get(i -
                            1);

                    if (node.getTier().getLinguisticType()
                                .equals(prevLinNode.getTier().getLinguisticType())) {
                        startLinGroup = false;
                    } else {
                        startLinGroup = true;
                    }
                }

                paintLabelInBlock(big2d, node, i, availableLabelWidth,
                    startLinGroup);
            }

            break;

        case UNSORTED:
        // default, fall through
        default:

            for (int i = 0; i < displayableTierNodes.size(); i++) {
                node = (TierTreeNode) displayableTierNodes.get(i);

                paintLabelInBlock(big2d, node, i, availableLabelWidth, false);
            }
        }

        repaint();
    }

    /**
     * Draw the buffered tier names and eventually the label of 
     * the tier that is being dragged.
     *
     * @param g the graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        if (SystemReporting.antiAliasedText) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        //g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        //g2d.fillRect(0, 0, getWidth(), getHeight());
        if (bi != null) {
        	g2d.drawImage(bi, 0, 0, this);
        }       

        g2d.setColor(Constants.SELECTIONCOLOR);
        g2d.drawLine(0, 0, getWidth() - 1, 0);
        g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        if (dragging) {
            g2d.setFont(boldFont);
            g2d.setColor(Color.blue);
            g2d.drawString(dragLabel, dragX, dragY);
        }
        if (resizer != null) {
        	resizer.repaint();
        }
    }

    /**
     * Paints a tier label in a tree-like structure. Angled lines show the tree
     * hierarchy  and for tiers with children there is a marker to indicate
     * the expanded state  of the branche.
     *
     * @param g the Graphics object for rendering
     * @param node the tier's node to paint
     * @param positionIndex the index in the array of displayable tiers
     * @param availableLabelWidth the availbale width for tier label plus
     *        decorations
     */
    private void paintLabelHierarchically(Graphics g, TierTreeNode node,
        int positionIndex, int availableLabelWidth) {
        int level = node.getLevel();
        boolean hasChildren = (node.getChildCount() == 0) ? false : true;

        boolean atLeastOneChildVisible = false;

        if (node.getChildCount() > 0) {
            Enumeration en = node.children();

            while (en.hasMoreElements()) {
                if (((TierTreeNode) en.nextElement()).isVisible()) {
                    atLeastOneChildVisible = true;

                    break;
                }
            }
        }

        boolean[] isParentLastChild = new boolean[level];

        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        isParentLastChild[level - 1] = (parent.getLastChild() == node);

        DefaultMutableTreeNode upNode;

        for (int i = level - 2; i >= 0; i--) {
            upNode = parent;
            parent = (DefaultMutableTreeNode) upNode.getParent();
            isParentLastChild[i] = (parent.getLastChild() == upNode);
        }

        int widthForLabel = availableLabelWidth -
            ((level * LEVEL_INDENT) + (LEVEL_INDENT / 2));
        int actualLabelWidth = widthForLabel;
        String label = "";

        if (node.isActive()) {
            label = truncateString(node.getTierName(), widthForLabel,
                    boldMetrics);
            actualLabelWidth = SwingUtilities.computeStringWidth(boldMetrics,
                    label);
        } else {
            label = truncateString(node.getTierName(), widthForLabel,
                    fontMetrics);
            actualLabelWidth = SwingUtilities.computeStringWidth(fontMetrics,
                    label);
        }
        
        // start painting, vertical lines
        g.setColor(Color.DARK_GRAY);

        int y = tierPositions[positionIndex];
        int minY = y - (tierHeight / 2);
        int minX = MARGIN + (LEVEL_INDENT / 2);
        int levelCount = 1;

        for (int i = levelCount; i < level; i++) {
            if (!isParentLastChild[i - 1]) {
                g.drawLine(minX, minY, minX, minY + tierHeight);
            }

            minX += LEVEL_INDENT;
        }

        int x = (MARGIN + (level * LEVEL_INDENT)) - (LEVEL_INDENT / 2);

        if (isParentLastChild[level - 1]) {
            g.drawLine(x, minY, x, y);
        } else {
            g.drawLine(x, minY, x, minY + tierHeight);
        }

        // horizontal line
        g.drawLine(x, y, x + LEVEL_INDENT, y);

        if (hasChildren && node.isExpanded()) {
            if (atLeastOneChildVisible) {
                g.drawLine(x + LEVEL_INDENT, y, x + LEVEL_INDENT,
                    y + (tierHeight / 2));
            }

            // paint expanded marker
            g.drawRect(x - (MARKER_SIZE / 2), y - (MARKER_SIZE / 2),
                MARKER_SIZE, MARKER_SIZE);
        } else if (hasChildren && !node.isExpanded()) {
            // paint collapsed marker
            g.fillRect(x - (MARKER_SIZE / 2), y - (MARKER_SIZE / 2),
                MARKER_SIZE, MARKER_SIZE);
        }

        x += (LEVEL_INDENT + (LEVEL_INDENT / 2));
        y += (boldFont.getSize() / 2);

        if (node.isActive()) {
            g.setColor(Constants.CROSSHAIRCOLOR);
            g.setFont(boldFont);
            g.drawLine(x, y + 3, x + actualLabelWidth, y + 3);
            g.drawString(label, x, y);
            if (showNumberOfAnnotations) {   
            	if (!reducedTierHeight) {
	                g.setFont(Constants.SMALLFONT);
	                g.drawString((String) numAnnosPerTier.get(node.getTierName()), x, (minY + tierHeight - 1));
            	} else {
    	            int numW = getFontMetrics(Constants.SMALLFONT).stringWidth(
    	            		(String) numAnnosPerTier.get(node.getTierName()));
    	            if (actualLabelWidth + numW < widthForLabel - 3) {
    	            	g.setFont(Constants.SMALLFONT);
    	            	g.drawString((String) numAnnosPerTier.get(node.getTierName()), x + actualLabelWidth + 3, y);
    	            }
            	}
            }
        } else {
        	Color col = (Color) prefTierColors.get(node.getTier());
        	if (col == null) {        	
	            TierImpl root = node.getTier().getRootTier();
	
	            if (root != null) {
	                g.setColor((Color) prefTierColors.get(root));
	            }
        	} else {
        		g.setColor(col);
        	}

            g.setFont(getFont());
            g.drawString(label, x, y);
            if (showNumberOfAnnotations) {
            	if (!reducedTierHeight) {
	                g.setFont(Constants.SMALLFONT);
	                g.drawString((String) numAnnosPerTier.get(node.getTierName()), x, (minY + tierHeight - 2));
            	} else {
    	            int numW = getFontMetrics(Constants.SMALLFONT).stringWidth(
    	            		(String) numAnnosPerTier.get(node.getTierName()));
    	            if (actualLabelWidth + numW < widthForLabel - 3) {
    	            	g.setFont(Constants.SMALLFONT);
    	            	g.drawString((String) numAnnosPerTier.get(node.getTierName()), x + actualLabelWidth + 3, y);
    	            }
            	}
            }
        }
    }

    /**
     * Paints a tier label in a flattened tree; i.e. all branches are at the
     * same level,  but there are groups separated by horizontal dividing
     * lines.
     *
     * @param g the Graphics object for rendering
     * @param node the tier's node to paint
     * @param positionIndex the index in the array of displayable tiers
     * @param availableLabelWidth the availbale width for the tier label
     * @param startNewGroup true indicates that a horizontal line should be
     *        drawn above this node
     */
    private void paintLabelInBlock(Graphics g, TierTreeNode node,
        int positionIndex, int availableLabelWidth, boolean startNewGroup) {
        int y = tierPositions[positionIndex];
        int minY = y - (tierHeight / 2);

        if ((sortMode != UNSORTED) && ((positionIndex == 0) || startNewGroup)) {
            g.setColor(Color.DARK_GRAY);
            g.drawLine(MARGIN, minY, MARGIN + availableLabelWidth, minY);
        }

        if ((sortMode != UNSORTED) &&
                (positionIndex == (displayableTierNodes.size() - 1))) {
            g.setColor(Color.DARK_GRAY);
            g.drawLine(MARGIN, minY + tierHeight, MARGIN + availableLabelWidth,
                minY + tierHeight);
        }

        int actualLabelWidth = availableLabelWidth;
        String label = "";

        if (node.isActive()) {
            label = truncateString(node.getTierName(), availableLabelWidth,
                    boldMetrics);
            actualLabelWidth = SwingUtilities.computeStringWidth(boldMetrics,
                    label);

            int x = (MARGIN + availableLabelWidth) - actualLabelWidth;
            y += (getFont().getSize() / 2);
            g.setColor(Constants.CROSSHAIRCOLOR);
            //g.setFont(boldFont);
            //g.drawLine(x, y + 3, x + actualLabelWidth, y + 3); defer
            //g.drawString(label, x, y);
            
            if (showNumberOfAnnotations) {
	            //g.setColor(Color.DARK_GRAY);
	            g.setFont(Constants.SMALLFONT);
	            int numW = getFontMetrics(Constants.SMALLFONT).stringWidth(
	            		(String) numAnnosPerTier.get(node.getTierName()));
	            //g.drawString((String) numAnnosPerTier.get(node.getTierName()), (x + numW < getWidth() ? x : getWidth() - MARGIN - numW), 
	            	//	(minY + tierHeight - 2));
	            if (!reducedTierHeight) {// paint underneath the label
		            g.drawString((String) numAnnosPerTier.get(node.getTierName()), (getWidth() - MARGIN - numW), 
		            		(minY + tierHeight - 1));
	            } else { // check if the number of annotations can be appended completely
	            	int xx = x - 3 - numW;
	            	if (xx >= MARGIN) {// enough space left
	            		x = xx;
			            g.drawString((String) numAnnosPerTier.get(node.getTierName()), (getWidth() - MARGIN - numW), 
			            		y);
	            	}
	            }
            }
            g.setFont(boldFont);
            g.drawLine(x, y + 3, x + actualLabelWidth, y + 3);
            g.drawString(label, x, y);
        } else {
            label = truncateString(node.getTierName(), availableLabelWidth,
                    fontMetrics);
            actualLabelWidth = SwingUtilities.computeStringWidth(fontMetrics,
                    label);

            int x = (MARGIN + availableLabelWidth) - actualLabelWidth;
            y += (getFont().getSize() / 2);
        	Color col = (Color) prefTierColors.get(node.getTier());
        	if (col == null) {        	
	            TierImpl root = node.getTier().getRootTier();
	
	            if (root != null) {
	                g.setColor((Color) prefTierColors.get(root));
	            }
        	} else {
        		g.setColor(col);
        	}
            
            if (showNumberOfAnnotations) {
	            //g.setColor(Color.DARK_GRAY);
	            g.setFont(Constants.SMALLFONT);
	            int numW = getFontMetrics(Constants.SMALLFONT).stringWidth(
	            		(String) numAnnosPerTier.get(node.getTierName()));
	            //g.drawString((String) numAnnosPerTier.get(node.getTierName()), (x + numW < getWidth() ? x : getWidth() - MARGIN - numW), 
	            	//	(minY + tierHeight - 2));
	            if (!reducedTierHeight) {
	            	g.drawString((String) numAnnosPerTier.get(node.getTierName()), (getWidth() - MARGIN - numW), 
	            		(minY + tierHeight - 2));
	            } else {
	            	int xx = x - 3 - numW;
	            	if (xx >= MARGIN) {
		            	x = xx;
			            g.drawString((String) numAnnosPerTier.get(node.getTierName()), (getWidth() - MARGIN - numW), 
			            		y);
	            	}
	            }
            }
            
            g.setFont(getFont());
            g.drawString(label, x, y);
        }
    }

    /**
     * MouseMotionListener method. Creates a formatted tooltip that displays
     * information  on the tier at the mouse position.
     *
     * @param e the mouse event
     */
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        int tierIndex = getClosestTierIndexForMouseY(p.y);

        if (!(tierIndex < displayableTierNodes.size())) {
            return;
        }

        TierTreeNode tierNode = (TierTreeNode) displayableTierNodes.get(tierIndex);
        TierImpl tier = tierNode.getTier();

        if (tier == null) {
            return;
        }

        StringBuffer tooltip = new StringBuffer("<html><table>");

        // tier name
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString("EditTierDialog.Label.TierName"));
        tooltip.append("</b></td><td>");
        tooltip.append(tier.getName());
        tooltip.append("</td></tr>");

        // tier parent
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString("EditTierDialog.Label.Parent"));
        tooltip.append("</b></td><td>");

        if (tier.hasParentTier()) {
            tooltip.append(tier.getParentTier().getName());
        } else {
            tooltip.append("-");
        }

        tooltip.append("</td></tr>");

        // tier participant
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString("EditTierDialog.Label.Participant"));
        tooltip.append("</b></td><td>");
        tooltip.append(tier.getParticipant());
        tooltip.append("</td></tr>");

        // tier annotator
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString("EditTierDialog.Label.Annotator"));
        tooltip.append("</b></td><td>");
        tooltip.append(tier.getAnnotator());
        tooltip.append("</td></tr>");

        // tier lin. type
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString(
                "EditTierDialog.Label.LinguisticType"));
        tooltip.append("</b></td><td>");

        if (tier.getLinguisticType() != null) {
            tooltip.append(tier.getLinguisticType().getLinguisticTypeName());
        }

        tooltip.append("</td></tr>");

        // tier language
        tooltip.append("<tr><td><b>");
        tooltip.append(ElanLocale.getString("EditTierDialog.Label.Language"));
        tooltip.append("</b></td><td>");
        Locale defLoc = tier.getDefaultLocale();
        if (defLoc != null) {
            tooltip.append(defLoc.getDisplayName());
        }

        tooltip.append("</td></tr>");

        tooltip.append("</table></html>");
        setToolTipText(tooltip.toString());
    }

    // not used interface methods
    /**
     * Empty
     *
     * @param e event
     */
    public void mouseEntered(MouseEvent e) {
    }

    /*
     * MouseListener method, not used here
     */
    /**
     * Empty
     *
     * @param e event
     */
    public void mouseExited(MouseEvent e) {
    }

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (viewer instanceof MouseWheelListener) {
			((MouseWheelListener) viewer).mouseWheelMoved(e);
		}		
	}

    /**
     * Sets: active tier, tier order, hidden tiers, sorting mode
     * 
     */
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}		
	}
	
	/**
	 * Update the panel after a change in preferences, or after loading a document
	 */
	public void preferencesChanged() {
		// visible and hidden tiers, tierorder
		List hiddenTiers = (List) Preferences.get("MultiTierViewer.HiddenTiers", 
				transcription);		
		
		List collTiers = (List) Preferences.get("MultiTierViewer.CollapsedTiers", 
				transcription);
		
		Object useTyp = Preferences.get("MultiTierViewer.SortAlpabetically", transcription);
		if(useTyp instanceof Boolean){
			sortAlphabetically = (Boolean)useTyp;
		}
		
		List tierOrderList = tierOrder.getTierOrder();
		//setTierOrder(tierOrderList);
		sortedTiers.clear();
		visibleTiers.clear();
		for(int i=0; i< tierOrderList.size(); i++){
			TierImpl tier = (TierImpl) transcription.getTierWithId(tierOrderList.get(i).toString());
			if(tier != null){
				sortedTiers.add(tier);
			}
		}
		visibleTiers.addAll(sortedTiers);
				
		// HS reuse the list to update the visible tiers menu
		if (hiddenTiers != null && hiddenTiers.size() > 0) {
			Tier tier;
			for (int i = visibleTiers.size() - 1; i >= 0; i--) {
				tier = (Tier) visibleTiers.get(i);
				if (hiddenTiers.contains(tier.getName())) {
					visibleTiers.remove(i);
				}
			}
		}
			
		if(visibleTiers.size() == 0){
			hideAllTiers = true;
		}

		// active tier	
		String atName = (String) Preferences.get("MultiTierViewer.ActiveTierName", 
				transcription);
		if (atName != null) {
			setActiveTierForName(atName);
		}
		// finally sort mode
		Integer sortMode = (Integer) Preferences.get("MultiTierViewer.TierSortingMode", 
				transcription);
		
		if (sortMode != null) {
			setSorting(sortMode.intValue());
		}else {
			createSortedTree(true);
		}
		
		if (collTiers != null && collTiers.size() > 0) {
	    	if (sortedRootNode != null) {
		        Enumeration en = sortedRootNode.preorderEnumeration();
		        en.nextElement();
		
		        TierTreeNode nn;
		
		        while (en.hasMoreElements()) {
		            nn = (TierTreeNode) en.nextElement();
		            if (collTiers.contains(nn.getTierName())) {
		            	nn.setExpanded(false);
		            }	            
		        }
	    	}
	    	
	    	updateDisplayableTiers();
		}
		
		
				
		// preferred tier colors
		//prefTierColors.clear();
		Object colorsObj = Preferences.get("TierColors", transcription);
		if (colorsObj instanceof Map) {
			Map colors = (Map) colorsObj;
			Iterator keyIt = colors.keySet().iterator();
			String name;
			TierImpl t;
			Color col;
			while (keyIt.hasNext()) {
				name = (String) keyIt.next();
				col = (Color) colors.get(name);
				t = (TierImpl) transcription.getTierWithId(name);
				if (t != null) {					
					prefTierColors.put(t, col);
				}
			}
		}
		// number of annotations indication
		Boolean numAnn = (Boolean) Preferences.get("MultiTierViewer.ShowNumberOfAnnotations", 
				transcription);
		if (numAnn != null) {
			showNumberOfAnnotations = numAnn.booleanValue();
			numAnnosMI.setSelected(showNumberOfAnnotations);
		}
		
		// display a warning message with alternate options
		// which can make some tiers visible
		if(displayableTierNodes.size() == 0){
    		sortingChanged = true;
    	}
		preferenceChanged = true;
		
		//updateDisplayableTiers();
		updateAnnotationCounts();
	}
	
	 /**
     * Called when the viewer will become invisible e.g when switching to an other working mode.
     */
    public void isClosing(){		
		HashMap<String, Color> prefMap = new HashMap<String, Color>();
		
		Iterator keyIt = prefTierColors.keySet().iterator();
		String name;
		TierImpl t;
		Color col;
		while (keyIt.hasNext()) {
			t = (TierImpl) keyIt.next();
			col = (Color) prefTierColors.get(t);
			prefMap.put(t.getName(), col);
		}
		
		Preferences.set("TierColors", prefMap, transcription, false, true);
    }
    
	public void componentHidden(ComponentEvent e) {		
	}

	public void componentMoved(ComponentEvent e) {		
	}

	public void componentResized(ComponentEvent e) {
		if (resizer != null) {
			resizer.setBounds(getWidth() - resizer.getWidth(), 1, resizer.getWidth(), resizer.getHeight());
		}
		paintBuffer();
	}

	public void componentShown(ComponentEvent e) {
		
	}
	
    //**********************************************************************
    //* internal classes
    //**********************************************************************
    /**
     * A class to paint the name of a tier in the <code>MultiTierControlPanel </code>.<br>
     * It has fields to hold the visibility and active state of the  Tier and
     * the expanded state of the node.
     *
     * @author Han Sloetjes
     * @version 1.0 21 Apr 2004
     */
    public class TierTreeNode extends DefaultMutableTreeNode {
        private TierImpl tier;
        private String tierName;
        private String label;
        private boolean visible = true;
        private boolean active = false;
        private boolean expanded = true;

        /**
         * Creates a new TierTreeNode instance
         *
         * @param tier DOCUMENT ME!
         */
        public TierTreeNode(TierImpl tier) {
            this.tier = tier;
            tierName = tier.getName();
            label = tierName;
        }

        /**
         * Returns the active state of this tier.
         *
         * @return true if this is the active tier, false otherwise
         */
        public boolean isActive() {
            return active;
        }

        /**
         * Returns the visibility of this tier.
         *
         * @return true if this tier is visible, false otherwise
         */
        public boolean isVisible() {
            return visible;
        }

        /**
         * Sets the active state.
         *
         * @param active the active state
         */
        public void setActive(boolean active) {
            this.active = active;
        }

        /**
         * Sets the visibility of this tier.
         *
         * @param visible the visibility of this tier
         */
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        /**
         * Returns the expanded state.
         *
         * @return true if this node is expanded, false otherwise
         */
        public boolean isExpanded() {
            return expanded;
        }

        /**
         * Sets the expanded state of this node.
         *
         * @param expanded the expanded state
         */
        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        /**
         * Returns the tier that is the userobject of this node.
         *
         * @return the tier
         */
        public TierImpl getTier() {
            return tier;
        }

        /**
         * Sets the userobject of this node.
         *
         * @param tier the userobject of this node
         */
        public void setTier(TierImpl tier) {
            this.tier = tier;
        }

        /**
         * Returns the name of the tier.
         *
         * @return the name of the tier
         */
        public String getTierName() {
            return tierName;
        }

        /**
         * Sets the name of the tier.
         *
         * @param tierName the new name of the tier
         */
        public void setTierName(String tierName) {
            this.tierName = tierName;
        }

        /**
         * Returns the name of the tier.
         *
         * @return the name of the tier
         */
        public String toString() {
            return tierName;
        }
    }
    
    public class TierComparer implements Comparator<TierImpl>{
		public int compare(TierImpl o1, TierImpl o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
    }
}

