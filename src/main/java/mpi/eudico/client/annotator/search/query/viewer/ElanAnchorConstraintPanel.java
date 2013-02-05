package mpi.eudico.client.annotator.search.query.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.TierSortAndSelectDialog2;
import mpi.eudico.client.annotator.search.model.ElanType;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.search.SearchLocale;

import mpi.search.content.model.CorpusType;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.RestrictedAnchorConstraint;

import mpi.search.content.query.viewer.AbstractConstraintPanel;
import mpi.search.content.query.viewer.AnchorConstraintPanel;
import mpi.search.content.query.viewer.AttributeConstraintPanel;
import mpi.search.content.query.viewer.RelationPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;


/**
 * Elan subclass with additional font support.
 * 
 * @author HS
 * @version Aug 2008
  */
public class ElanAnchorConstraintPanel extends AnchorConstraintPanel {
    /**
     * Creates a new ElanAnchorConstraintPanel instance
     *
     * @param constraint 
     * @param treeModel 
     * @param type 
     * @param startAction 
     */
    public ElanAnchorConstraintPanel(AnchorConstraint constraint,
        DefaultTreeModel treeModel, CorpusType type, Action startAction) {
        super(constraint, treeModel, type, startAction);
    }

    /**
     * Creates the ui elements and layout.
     */
    protected void makeLayout() {
        setFont(Constants.DEFAULTFONT);
        //RegExPanel
        patternPanel = new ElanPatternPanel(type, tierComboBox, regExCheckBox,
                constraint, startAction, getFont());

        //RelationPanel
        relationPanel = new RelationPanel(type, constraint);

        //OptionPanel
        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
        regExCheckBox.setFont(getFont().deriveFont(Font.PLAIN, 9f));
        caseCheckBox.setFont(getFont().deriveFont(Font.PLAIN, 9f));
        checkBoxPanel.add(regExCheckBox);
        checkBoxPanel.add(caseCheckBox);
        optionPanel.add(checkBoxPanel, BorderLayout.WEST);

        //InputPanel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        inputPanel.add(patternPanel);
        inputPanel.add(relationPanel);

        //AttributePanel
        if (type.hasAttributes()) {
            attributePanel = new AttributeConstraintPanel(type);
            optionPanel.add(attributePanel, BorderLayout.CENTER);
            attributePanel.setTier(getTierName());
        }

        //FramedPanel
        JPanel specificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                    0, 1));
        specificationPanel.add(inputPanel);
        specificationPanel.add(optionPanel);
        framedPanel.add(specificationPanel, "");
        framedPanel.setBorder(blueBorder);
        framedPanelLayout.show(framedPanel, "");

        //this
        setLayout(new BorderLayout());
        add(titleComponent, BorderLayout.NORTH);
        add(framedPanel, BorderLayout.CENTER);

        tierComboBox.addItemListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        Action addConstraintAction = new AbstractAction(SearchLocale.getString(
                    "Search.Query.Add")) {
                public void actionPerformed(ActionEvent e) {
                    addConstraint();
                }
            };

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK);
        addConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

        JButton addButton = new JButton(addConstraintAction);
        addButton.setFont(getFont().deriveFont(11f));
        buttonPanel.add(addButton);

        if ((constraint.getParent() != null) &&
                !(constraint.getParent() instanceof RestrictedAnchorConstraint)) {
            Action deleteConstraintAction = new AbstractAction(SearchLocale.getString(
                        "Search.Query.Delete")) {
                    public void actionPerformed(ActionEvent e) {
                        deleteConstraint();
                    }
                };

            ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
            deleteConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

            JButton deleteButton = new JButton(deleteConstraintAction);
            deleteButton.setFont(getFont().deriveFont(11f));
            buttonPanel.add(deleteButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        try {
            Class popupMenu = type.getInputMethodClass();
            popupMenu.getConstructor(new Class[] {
                    Component.class, AbstractConstraintPanel.class
                })
                     .newInstance(new Object[] {
                    patternPanel.getDefaultInputComponent(),
                    ElanAnchorConstraintPanel.this
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Transfers the focus to the textfield of the pattern panel.
     */
	public void grabFocus() {
		patternPanel.grabFocus();
	}

	/**
     * Shows an extended tier selection dialog that allows for tier
     * selection based on type, participant or annotator
     */
	protected void selectCustomTierSet() {
    	if ( !(type instanceof ElanType) ) {
    		return; // message??
    	}
		// popup an extended tier selection window
    	Window w = SwingUtilities.getWindowAncestor(this);
    	List<String> allTiers = new ArrayList<String>();
    	String[] tierNames = type.getTierNames();// all tiers
    	for (String s : tierNames) {
    		allTiers.add(s);
    	}
    	Transcription trans = ((ElanType) type).getTranscription();
    	List<String> sTiers = new ArrayList<String>();
    	if (selectedTiers != null && selectedTiers.size() > 0) {
    		sTiers.addAll(selectedTiers);
    	} else {
    		String[] curTiers = getTierNames();
    		if (curTiers.length == 1 && curTiers[0] == Constraint.ALL_TIERS) {// default
    	    	Object oldSelTiers = Preferences.get("Search.SelectedTiers", trans);
    	    	if (oldSelTiers instanceof List) {
    	    		List oldList = (List) oldSelTiers;
    	    		for (int i = 0; i < oldList.size(); i++) {
    	    			sTiers.add((String) oldList.get(i)); 
    	    		}
    	    	} else {
    	    		sTiers.addAll(allTiers);
    	    	}   			
    		} else {
    			for (String s: curTiers) {
    				sTiers.add(s);
    			}
    		}
    	}
    	
    	TierSortAndSelectDialog2 dialog = null;
    	if (w instanceof Dialog) {
    		dialog = new TierSortAndSelectDialog2((Dialog) w, 
    				trans, allTiers, sTiers);
    	} else if (w instanceof Frame) {
    		dialog = new TierSortAndSelectDialog2((Frame) w, 
    				trans, allTiers, sTiers);
    	}
    	if (dialog == null) {
    		return;
    	}
    	// read preferences
    	
    	Object modeObj = Preferences.get("Search.TierSelectionMode", trans);
    	Object itemObj = Preferences.get("Search.HiddenItems", trans);
    	if (modeObj instanceof String) {
    		String mode = (String) modeObj;
    		dialog.setSelectionMode(mode, (List) itemObj);
    	}
		
		// hier read hidden tiers? store selected tiers??
    	dialog.setTitle(ElanLocale.getString("TranscriptionManager.SelectTierDlg.Title"));
    	dialog.setLocationRelativeTo(this);
    	dialog.setVisible(true);
    	
    	
    	List<String> selTiers = dialog.getSelectedTiers();
    	if (selTiers != null) {
    		if (selectedTiers == null) {
    			selectedTiers = new ArrayList<String>(selTiers);
    		} else {
    			selectedTiers.clear();
    			selectedTiers.addAll(selTiers);
    		}
    		
    		String mode = dialog.getSelectionMode();
    		List<String> items = dialog.getUnselectedItems();

    		Preferences.set("Search.SelectedTiers", selTiers, trans);
    		Preferences.set("Search.TierSelectionMode", mode, trans);
    		Preferences.set("Search.HiddenItems", items, trans);
    		
    	} else {
    		// nothing changed
    	}
	}
    
}
