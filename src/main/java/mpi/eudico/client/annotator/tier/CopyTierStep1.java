package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.util.TierTree;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;


/**
 * The first pane for the 'reparent tier' task. Select the tier that is to be
 * moved.
 *
 * @author Han Sloetjes
 */
public class CopyTierStep1 extends StepPane implements TreeSelectionListener {
    private TranscriptionImpl transcription;
    private JTree tierTree;
    private JCheckBox depTiersCB;
    private boolean copyMode = false;

    /**
     * Creates a new CopyTierStep1 instance
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep1(MultiStepPane multiPane,
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

        if (transcription != null) {
            //TierTree tree = new TierTree(transcription, null);
            TierTree tree = new TierTree(transcription);
            tierTree = new JTree(tree.getTree());
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
        //tierTree.setRootVisible(false);
        tierTree.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        tierTree.setFont(tierTree.getFont().deriveFont((float) 14));

        JScrollPane scrollPane = new JScrollPane(tierTree);

        //scrollPane.setViewportView(tierTree);
        for (int i = 0; i < tierTree.getRowCount(); i++) {
            tierTree.expandRow(i);
        }

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        if (copyMode) {
            add(new JLabel("<html>" +
                    ElanLocale.getString("MultiStep.Copy.SelectTier") + 
                    "</html>"), gbc);
        } else {
            add(new JLabel("<html>" +
                ElanLocale.getString("MultiStep.Reparent.SelectTier") + " " +
                ElanLocale.getString("MultiStep.Reparent.Depending") +
                "</html>"), gbc);
        }
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);
        
        if (copyMode) {
            depTiersCB = new JCheckBox(ElanLocale.getString("MultiStep.Copy.Depending"));
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            add(depTiersCB, gbc);
        }
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        if (copyMode) {
            return ElanLocale.getString("MultiStep.Copy.SelectTier");
        }
        return ElanLocale.getString("MultiStep.Reparent.SelectTier");
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        // the next button is already disabled
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * If a tier has been selected store it in the properties map for use by
     * other steps and return true.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
        if (tierTree.getSelectionModel().getSelectionCount() > 0) {
            Object o = tierTree.getLastSelectedPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                multiPane.putStepProperty("SelTier",
                    ((DefaultMutableTreeNode) o).getUserObject());

                //System.out.println("selected obj: " + o);
            }
            if (copyMode) {
                multiPane.putStepProperty("IncludeDepTiers", new Boolean(depTiersCB.isSelected()));
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    public boolean leaveStepBackward() {
        return true;
    }

    /**
     * Nothing to do. Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    public boolean doFinish() {
        return true;
    }

    /**
     * When a tier has been selected in the tree, enable the 'next' button.
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
        if ((tierTree.getSelectionCount() > 0) &&
                (tierTree.getSelectionModel().getMinSelectionRow() > 0)) {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
}
