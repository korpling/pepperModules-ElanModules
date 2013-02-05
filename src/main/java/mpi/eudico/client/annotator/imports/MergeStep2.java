package mpi.eudico.client.annotator.imports;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.MergeTranscriptionsCommand;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.util.ProgressListener;

import mpi.eudico.client.util.CheckboxTreeCellEditor;
import mpi.eudico.client.util.CheckboxTreeCellRenderer;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.client.util.TierTree;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * Second step in the merge process; loading of first source (=dest
 * transcription) or copying of first transcription, loading of second
 * source, presentation of  available tiers for selection, etc.
 */
public class MergeStep2 extends StepPane implements ProgressListener, ActionListener {
    private TranscriptionImpl firstTrans;
    private TranscriptionImpl secTrans;
    private TranscriptionImpl destTrans;
    private boolean addLinkedFiles;
    private boolean appendAnnsWithMedia;
    private boolean appendAnnsWithGivenTime;
    private boolean appendAnnsWithLastAnns;
    private long givenTimeFrame;
    private Object firstSource;
    private String secondSource;
    private String destFileName;
    private Insets insets;
    private JPanel progressPanel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JPanel treePanel;
    private JTable firstTable;
    private JTable secTable;
    private JTree firstTree;
    private JTree secTree;
    private JScrollPane firstScroll;
    private JScrollPane secScroll;
    private JLabel firstLabel;
    private JLabel secLabel;
    private JButton allButton;
    private JButton noneButton;
    private JCheckBox overwriteCB;

    // the Command
    private Command com;

    /**
     * Creates a new MergeStep2 instance.
     *
     * @param multiPane the enclosing MultiStepPane
     */
    public MergeStep2(MultiStepPane multiPane) {
        super(multiPane);
        initComponents();
    }

    /**
     * Initializes the components of the step ui.
     */
    public void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        insets = new Insets(4, 6, 4, 6);

        // progress panel
        progressPanel = new JPanel(new GridBagLayout());
        progressLabel = new JLabel("Loading...");
        progressBar = new JProgressBar();

        progressPanel.setPreferredSize(new Dimension(50, 80));
        progressLabel.setFont(Constants.SMALLFONT);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(progressLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(progressBar, gridBagConstraints);

        // tree panel
        treePanel = new JPanel(new GridBagLayout());
        firstLabel = new JLabel(ElanLocale.getString(
                    "MergeTranscriptionDialog.Label.TiersSource1"));
        secLabel = new JLabel(ElanLocale.getString(
                    "MergeTranscriptionDialog.Label.SelectTiers"));
        overwriteCB = new JCheckBox(ElanLocale.getString(
                    "MergeTranscriptionDialog.Label.Overwrite"));

        // try trees instead of tables
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        treePanel.add(firstLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        firstScroll = new JScrollPane(firstTable);
        treePanel.add(firstScroll, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        treePanel.add(secLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        secScroll = new JScrollPane(secTable);
        treePanel.add(secScroll, gridBagConstraints);

        allButton = new JButton(ElanLocale.getString("Button.SelectAll"));
        allButton.addActionListener(this);
        noneButton = new JButton(ElanLocale.getString("Button.SelectNone"));
        noneButton.addActionListener(this);
        
        JPanel butPanel = new JPanel(new GridLayout(1, 2));
        butPanel.add(allButton);
        butPanel.add(noneButton);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        treePanel.add(butPanel, gridBagConstraints);

        gridBagConstraints.gridy = 5;
        treePanel.add(overwriteCB, gridBagConstraints);
    }

	/**
	 * Adds the progress bar ui to this pane.
	 */
    private void setProgressUI() {
        removeAll();
        treePanel.setVisible(false);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(progressPanel, gridBagConstraints);
        progressPanel.setVisible(true);
        revalidate();
    }

	/**
	 * Adds the tier selection ui to this pane.
	 */
    private void setTierTreeUI() {
        removeAll();
        progressPanel.setVisible(false);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(treePanel, gridBagConstraints);
        treePanel.setVisible(true);
        revalidate();
    }

	/**
	 * Fills the tree overview of both transcriptions.
	 * The second tree has checkboxes to represent the selected
	 * state of each tier.
	 */
    private void fillTrees() {
        if (destTrans != null) {
            TierTree tree = new TierTree(destTrans);
            DefaultMutableTreeNode transNode = tree.getTree();

            //transNode.setUserObject(ElanLocale.getString(
            //        "MultiStep.Reparent.Transcription"));
            firstTree = new JTree(transNode);

            DefaultTreeSelectionModel model = new DefaultTreeSelectionModel();
            model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            firstTree.setSelectionModel(model);
            firstTree.putClientProperty("JTree.lineStyle", "Angled");

            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) firstTree.getCellRenderer();
            renderer.setLeafIcon(null);
            renderer.setOpenIcon(null);
            renderer.setClosedIcon(null);
            renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

            firstTree.setShowsRootHandles(true);
            firstTree.setRootVisible(false);
            firstTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            firstScroll.setViewportView(firstTree);

            for (int i = 0; i < firstTree.getRowCount(); i++) {
                firstTree.expandRow(i);
            }
        }

        if (secTrans != null) {
            // tree
            TierTree tree = new TierTree(secTrans);
            DefaultMutableTreeNode transNode = tree.getTree();

            // replace the UserObject Strings by SelectableObject's
            DefaultMutableTreeNode node = null;
            Enumeration en = transNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                node = (DefaultMutableTreeNode) en.nextElement();

                if (node != transNode) {
                    node.setUserObject(new SelectableObject(
                            node.getUserObject(), true));
                }
            }

            secTree = new JTree(transNode);

            DefaultTreeSelectionModel model = new DefaultTreeSelectionModel();
            model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            secTree.setSelectionModel(model);
            secTree.putClientProperty("JTree.lineStyle", "Angled");

            CheckboxTreeCellRenderer render = new CheckboxTreeCellRenderer();
            render.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

            CheckboxTreeCellEditor editor = new CheckboxTreeCellEditor();
            editor.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            secTree.setCellRenderer(render);
            secTree.setCellEditor(editor);

            secTree.setRootVisible(false);
            secTree.setShowsRootHandles(true);
            secTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            secScroll.setViewportView(secTree);

            for (int i = 0; i < secTree.getRowCount(); i++) {
                secTree.expandRow(i);
                checkSelection((DefaultMutableTreeNode) secTree.getPathForRow(i).getLastPathComponent());
            }

            secTree.setEditable(true);

            secTree.addMouseListener(new TreeMouseListener());
        }
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("MergeTranscriptionDialog.Title");
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        if ((firstSource != null) && (secondSource != null) &&
                (destFileName != null)) {
            // we have been here before: check if anything has been changed in step 1
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);

            Object fs = multiPane.getStepProperty("Source1");
            String ss = (String) multiPane.getStepProperty("Source2");
            destFileName = (String) multiPane.getStepProperty("Destination");

            boolean fsChanged = false;
            boolean ssChanged = false;

            if (fs instanceof String) {
                if (!firstSource.equals(fs)) {
                    fsChanged = true;
                    firstSource = (String) fs;
                }
            } else if (fs != firstSource) {
                fsChanged = true;
                firstSource = fs;
            }

            if (!secondSource.equals(ss)) {
                ssChanged = true;
                secondSource = (String) ss;
            }

            //System.out.println("First Source changed: " + fsChanged);
            //System.out.println("Second Source changed: " + ssChanged);
            if (fsChanged || ssChanged) {
                multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
                multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, false);
                setProgressUI();
                new LoadThread(fsChanged, ssChanged).start();
            } else {
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
                multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
                multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
            }
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
            setProgressUI();

            // start loading, set progressbar visible
            firstSource = multiPane.getStepProperty("Source1");
            secondSource = (String) multiPane.getStepProperty("Source2");
            destFileName = (String) multiPane.getStepProperty("Destination");            
            appendAnnsWithMedia = (Boolean) multiPane.getStepProperty("appendAnnsWithMedia");
            appendAnnsWithGivenTime = (Boolean) multiPane.getStepProperty("appendAnnsWithGivenTime");
            appendAnnsWithLastAnns = (Boolean) multiPane.getStepProperty("appendAnnsLastAnns");
            addLinkedFiles = (Boolean) multiPane.getStepProperty("AddLinkedFiles");
            
            if(appendAnnsWithGivenTime){
            	givenTimeFrame = (Long) multiPane.getStepProperty("givenTimeFrame");
            }
            multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
            multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, false);
            new LoadThread().start();
        }
    }

    /**
     * Notification that this step will become the active step, moving down.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
    }

    /**
     * Notification that this step will no longer be the active step, moving
     * up.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
        return true;
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    public boolean leaveStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);

        return true;
    }

    /**
     * Store selected tiers, check the overwrite checkbox, create a command,
     * register as listener and activate the progress ui.
     *
     * @return false; a new thread is started, the ui waits for its completion
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    public boolean doFinish() {
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        setProgressUI();

        boolean overwrite = overwriteCB.isSelected();

        ArrayList tiersToAdd = new ArrayList();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) secTree.getModel()
                                                                      .getRoot();
        DefaultMutableTreeNode node = null;
        SelectableObject so = null;

        Enumeration en = root.preorderEnumeration();
        en.nextElement();

        while (en.hasMoreElements()) {
            node = (DefaultMutableTreeNode) en.nextElement();
            so = (SelectableObject) node.getUserObject();

            if (so.isSelected()) {
                tiersToAdd.add(so.getValue());
            }
        }
        
        //
        com = new MergeTranscriptionsCommand("MergeTranscriptions");
        ((MergeTranscriptionsCommand) com).addProgressListener(this);
        
        if(appendAnnsWithMedia){
        	long mediaDur = ELANCommandFactory.getViewerManager(firstTrans).getMasterMediaPlayer()
						.getMediaDuration();
        	secTrans.shiftAllAnnotations(mediaDur);
        } else if (appendAnnsWithLastAnns){
        	long d =firstTrans.getLatestTime();
        	secTrans.shiftAllAnnotations(d);
        } else if(appendAnnsWithGivenTime){
        	secTrans.shiftAllAnnotations(givenTimeFrame);
        } 
        
        // receiver is destination transcrption
        Object[] args = new Object[] {
                secTrans, destFileName, tiersToAdd, new Boolean(overwrite), new Boolean(addLinkedFiles) };
        com.execute(destTrans, args);

        // new Thread is started, return false
        return false;
    }
    
    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressUpdated(java.lang.Object,
     *      int, java.lang.String)
     */
    public void progressUpdated(Object source, int percent, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);

            if (percent < 0) {
                percent = 0;
            } else if (percent > 100) {
                percent = 100;
            }

            progressBar.setValue(percent);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressCompleted(java.lang.Object,
     *      java.lang.String)
     */
    public void progressCompleted(Object source, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);
            progressBar.setValue(100);

            if (com != null) { // merge process complete
                Object standalone = multiPane.getStepProperty("Standalone");
                int option = JOptionPane.showOptionDialog(this,
                        (message + "\nOpen new transcription in ELAN?"), null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null,
                        new Object[] {
                            ElanLocale.getString("Button.Yes"),
                            ElanLocale.getString("Button.No")
                        }, ElanLocale.getString("Button.Yes"));

                if (option == JOptionPane.YES_OPTION) {
                    //could open from transcription, but use the file to be sure...
                    //new ElanFrame2(destFileName);
                	FrameManager.getInstance().createFrame(destFileName);
//                  check if we are standalone
                    if (standalone != null) {
                        // no need to check the actual value of standalone
                        multiPane.putStepProperty("CanQuit", Boolean.FALSE);
                    } 
                    multiPane.close();
                } else {
                    if (standalone == null) {
                        multiPane.close();
                    } else {
                        // ask wether more merging should be done.
                        multiPane.previousStep();
                    }
                }
            }
        }
    }

    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressInterrupted(java.lang.Object,
     *      java.lang.String)
     */
    public void progressInterrupted(Object source, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);

            if (com != null) {
                showWarningDialog("An error occured: " + message);
                multiPane.close();
            }
        }
    }

    /**
     * Button action events.
     */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == allButton) {
			setAllSelected(true);
		} else if (e.getSource() == noneButton) {
			setAllSelected(false);
		}
		
	}
	
    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Copies all parts of a Transcription to a new transcription.
     *
     * @param inTrans the source transcription
     *
     * @return a copy of the transcription
     */
    private TranscriptionImpl copyTranscription(TranscriptionImpl inTrans) {
        TranscriptionImpl destTr = new TranscriptionImpl();
        destTr.setNotifying(false);

        if (inTrans == null) {
            return destTr;
        }

        TranscriptionCopier copier = new TranscriptionCopier();
        copier.copyTranscription(inTrans, destTr);

        return destTr;
    }

    /**
     * Checks whether tiers should be selected or deselected based on
     * dependencies  and available tiers in the destination after a change in
     * the selected state  of a tier.
     *
     * @param node the node containing the selected tier
     */
    private void checkSelection(DefaultMutableTreeNode node) {
        if (node == null) {
            return;
        }

        Object obj = node.getUserObject();

        if (!(obj instanceof SelectableObject)) {
            return;
        }

        boolean selected = ((SelectableObject) obj).isSelected();
        String tierName = ((SelectableObject) obj).toString();

        DefaultMutableTreeNode nextNode = null;
        SelectableObject so = null;
        String nextName = null;
        TierImpl t1;
        TierImpl t2 = null;

        if (!selected) { // check selected children

            Enumeration en = node.breadthFirstEnumeration();
            en.nextElement();

            while (en.hasMoreElements()) {
                nextNode = (DefaultMutableTreeNode) en.nextElement();
                so = (SelectableObject) nextNode.getUserObject();

                if (so.isSelected()) {
                    nextName = (String) so.getValue();
                    t1 = (TierImpl) secTrans.getTierWithId(nextName);
                    t2 = (TierImpl) destTrans.getTierWithId(nextName);

                    if (t2 == null) {
                        if ((destTrans.getTierWithId(t1.getParentTier().getName()) == null) ||
                                !lingTypeCompatible(
                                    (TierImpl) destTrans.getTierWithId(
                                        t1.getParentTier().getName()),
                                    (TierImpl) t1.getParentTier())) {
                            so.setSelected(false);
                        }
                    } else if (!lingTypeCompatible(t1, t2)) {
                        so.setSelected(false);
                    }
                }
            }
        } else {
            // selected
            if (node.getLevel() > 1) {
                // check this node and if necessary its ancestors
                nextNode = node;

                while (!nextNode.isRoot()) {
                    so = (SelectableObject) nextNode.getUserObject();
                    tierName = (String) so.getValue();
                    t1 = (TierImpl) secTrans.getTierWithId(tierName);
                    t2 = (TierImpl) destTrans.getTierWithId(tierName);

                    if ((t2 != null)) {
                        if (lingTypeCompatible(t1, t2)) {
                            break; // don't need to change anything   
                        } else {
                            so.setSelected(false);
                            break;
                        }                      
                    } else {
                        so.setSelected(true);
                    }

                    nextNode = (DefaultMutableTreeNode) nextNode.getParent();
                }
            }
        }

        secTree.repaint();
    }

    /**
     * Check whether the LinguisticTypes of the tiers have the same stereotype.
     * This is a loose check, other attributes could also be checked; name, cv
     * etc.
     *
     * @param t the first tier for the comparison
     * @param t2 the second tier
     *
     * @return true if the Linguistic Type of the tiers have the same stereotype
     */
    private boolean lingTypeCompatible(TierImpl t, TierImpl t2) {
        // check linguistic type
        LinguisticType lt = t.getLinguisticType();
        LinguisticType lt2 = t2.getLinguisticType();

        // losely check the linguistic types
        if ( /*lt.getLinguisticTypeName().equals(lt2.getLinguisticTypeName()) &&*/
            lt.hasConstraints() == lt2.hasConstraints()) {
            if (lt.getConstraints() != null) {
                if (lt.getConstraints().getStereoType() == lt2.getConstraints()
                                                                  .getStereoType()) {
                    return true;
                } else {
                    // LOG.warning("Incompatible tier types in source and destination: " + t.getName());
                    return false;
                }
            } else {
                // both toplevel tiers
                return true;
            }
        }

        return false;
    }
    
    /**
     * The select all/none action.
     * 
     * @param select if true all nodes in the second tree will be set selected, otherwise
     * they will be deselected.
     */
    private void setAllSelected(boolean select) {
    	if (secTree != null) {
    		DefaultMutableTreeNode node;
    		Object selObj;
    		
    		Object root = secTree.getModel().getRoot();
    		if (root instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) root;
				Enumeration<Object> en = rootNode.breadthFirstEnumeration();
				
				while (en.hasMoreElements()) { 
					node = (DefaultMutableTreeNode)en.nextElement();
					selObj = node.getUserObject();
					
					if (selObj instanceof SelectableObject) {
						((SelectableObject) selObj).setSelected(select);
					}
				}				
			}
    		secTree.repaint();
    	}
    }

    /**
     * Listen for a change in the selected state of a tier tree node.
     */
    class TreeMouseListener extends MouseAdapter {
        /**
         * The mouse clicked event.
         *
         * @param me the mouse event
         */
        public void mouseClicked(MouseEvent me) {
            int selRow = secTree.getRowForLocation(me.getX(), me.getY());
            TreePath path = secTree.getPathForLocation(me.getX(), me.getY());

            if (selRow != -1) {
                checkSelection((DefaultMutableTreeNode) path.getLastPathComponent());
            }
        }
    }

    /**
     * The loading of the transcription is done in a separate thread.
     * 
     * @author Han Sloetjes
     * @version 1.0
     */
    class LoadThread extends Thread {
        private boolean loadFirst = true;
        private boolean loadSec = true;

        /**
         * Creates a new LoadThread instance
         */
        LoadThread() {
        }

        /**
         * Creates a new LoadThread instance
         *
         * @param loadFirst if true the first source should be loaded/copied
         * @param loadSec if true the second source should be loaded
         */
        LoadThread(boolean loadFirst, boolean loadSec) {
            this.loadFirst = loadFirst;
            this.loadSec = loadSec;
        }

        /**
         * The run method; loads and updates the progress ui.
         */
        public void run() {
            try {
                progressUpdated(this, 5, "Loading first transcription...");

                if (loadFirst) {
                    if (firstSource instanceof String) {
                        firstTrans = new TranscriptionImpl((String) firstSource);
                        destTrans = firstTrans;
                    } else {
                        firstTrans = (TranscriptionImpl) firstSource;
                        destTrans = copyTranscription(firstTrans);
                    }
                }

                progressUpdated(this, 40, "Loading second transcription...");

                if (loadSec) {
                    secTrans = new TranscriptionImpl(secondSource);
                }

                progressUpdated(this, 80, "Fetching tiers...");

                // init two tier trees
                progressCompleted(this, "");
                setTierTreeUI();
                
                multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
                multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
                fillTrees();
            } catch (Exception ex) {
                progressInterrupted(this,
                    "An error occurred: " + ex.getMessage());
            }
        }
    }
}
