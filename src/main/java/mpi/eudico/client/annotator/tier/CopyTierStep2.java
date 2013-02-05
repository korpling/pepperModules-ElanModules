package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.util.TierTree;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * The second step in the reparent process: select the destination tier, or the
 * transcription itself when the tier should become a root tier.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class CopyTierStep2 extends StepPane implements TreeSelectionListener {
    private TranscriptionImpl transcription;
    private JTree tierTree;
    private String tierName;
    private TierImpl selTier;
    private JLabel tierLabel;
    private boolean copyMode = false;

    /**
     * Creates a new CopyTierStep2 instance.
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;
        if (multiPane.getStepProperty("CopyMode") != null) {
            copyMode = true;
        }
        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    public void initComponents() {
        // setPreferredSize
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        tierLabel = new JLabel();

        if (transcription != null) {
            //TierTree tree = new TierTree(transcription, null);
            TierTree tree = new TierTree(transcription);
            DefaultMutableTreeNode transNode = tree.getTree();
            transNode.setUserObject(ElanLocale.getString(
                    "MultiStep.Reparent.Transcription"));
            tierTree = new JTree(transNode);
        } else {
            tierTree = new JTree();
        }

        DefaultTreeSelectionModel model = new DefaultTreeSelectionModel();
        model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        model.addTreeSelectionListener(this);
        tierTree.setSelectionModel(model);
        tierTree.putClientProperty("JTree.lineStyle", "Angled");

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tierTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setBackgroundNonSelectionColor(Constants.DEFAULTBACKGROUNDCOLOR);

        //tierTree.setShowsRootHandles(false);
        tierTree.setRootVisible(true);
        tierTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        tierTree.setFont(tierTree.getFont().deriveFont((float) 14));

        JScrollPane scrollPane = new JScrollPane(tierTree);

        for (int i = 0; i < tierTree.getRowCount(); i++) {
            tierTree.expandRow(i);
        }

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(tierLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("MultiStep.Reparent.SelectParent") + " " +
                ElanLocale.getString("MultiStep.Reparent.SelectTrans") +
                "</html>"), gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("MultiStep.Reparent.SelectParent");
    }

    /**
     * Fetch the name of the tier that has been selected to move.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        tierName = (String) multiPane.getStepProperty("SelTier");

        if (tierName != null) {
            tierLabel.setText(ElanLocale.getString(
                    "MultiStep.Reparent.SelectedTier") + " " + tierName);
            selTier = (TierImpl) transcription.getTierWithId(tierName);
            if (copyMode) {
                if (selTier.hasParentTier()) {
                    String parentName = selTier.getParentTier().getName();
                    Enumeration en = ((DefaultMutableTreeNode) tierTree.getModel().getRoot()).breadthFirstEnumeration();
                    DefaultMutableTreeNode node = null;
                    
                    while (en.hasMoreElements()) {
                        node = (DefaultMutableTreeNode) en.nextElement();
                        if (parentName.equals((String) node.getUserObject())) {
                            tierTree.getSelectionModel().setSelectionPath(new TreePath(
                                    ((DefaultTreeModel)tierTree.getModel()).getPathToRoot(node)));
                            break;
                        }
                    }
                } else {
                    tierTree.getSelectionModel().setSelectionPath(new TreePath(tierTree.getModel().getRoot()));
                }
            }
            
        } else {
            // handle error
            tierName = "";
            selTier = null;
        }

        // if we have been here before check tree selection
        valueChanged(null);
    }

    /**
     * Enable the next button, a parent has been selected before.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * Store the name of the new parent (or the transcription) in the
     * properties map.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
        if (tierTree.getSelectionModel().getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;

                if (node.isRoot()) {
                    //multiPane.putStepProperty("HasParent", Boolean.FALSE);
                	multiPane.putStepProperty("SelNewParent", null);
                } else {
                    //multiPane.putStepProperty("HasParent", Boolean.TRUE);
                    String parentName = (String) node.getUserObject();
                    multiPane.putStepProperty("SelNewParent", parentName);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Always allowed.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    public boolean leaveStepBackward() {
        return true;
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    public boolean doFinish() {
        return true;
    }

    /**
     * Perform some checks on the selected parent. Enable the next button if a
     * valid parent has been selected: a tier can not be it's own parent  a
     * tier can not be assigned to its current parent and a tier that was
     * already a root tier can not be assigned to the  transcription again.
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
        if (tierTree.getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;

                String parentName = (String) node.getUserObject();
                if (!copyMode) {
	                if (parentName.equals(tierName)) {
	                    // cannot be it's own parent
	                    tierTree.setSelectionPath(null);
	
	                    return;
	                } else if ((selTier != null) &&
	                        (selTier.getParentTier() == null) && node.isRoot()) {
	                    // the tier was already an independent tier
	                    tierTree.setSelectionPath(null);
	
	                    return;
	                } else if ((selTier != null) &&
	                        (selTier.getParentTier() != null)) {
	                    String oldParent = selTier.getParentTier().getName();
	
	                    if (oldParent.equals(parentName)) {
	                        // the same parent as the old parent
	                        tierTree.setSelectionPath(null);
	
	                        return;
	                    }
	                }
                }
            }

            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
}
