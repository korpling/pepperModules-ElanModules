package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.CopyTierCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.client.annotator.gui.EditTypeDialog2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;

import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;

import mpi.eudico.client.annotator.util.ProgressListener;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * The third step in the reparent process:  allow or, in some cases, force to
 * choose another linguistic type.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class CopyTierStep3 extends ProgressStepPane implements ListSelectionListener,
    ActionListener, ProgressListener {
    private TranscriptionImpl transcription;
    private JLabel tierOverview;
    private JTable typeTable;
    private JScrollPane scrollPane;
    private String tierName;
    private String newParentName;
    private TierImpl selTier;
    private TierImpl newParent;
    private LinguisticType curType;
    private String selTypeName;
    private LinguisticTypeTableModel model;
    private String[] columns;
    private Vector types;
    private JButton typButton;
    private GridBagLayout layout;
    private CopyTierCommand com;
    private boolean copyMode = false;

    /**
     * Creates a new CopyTierStep3 instance.
     *
     * @param multiPane the enclosing container for the steps
     * @param transcription the transcription
     */
    public CopyTierStep3(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;

        types = this.transcription.getLinguisticTypes();

        if (multiPane.getStepProperty("CopyMode") != null) {
            copyMode = true;
        }

        initComponents();
    }

    /**
     * Initialise components.
     */
    public void initComponents() {   
        // setPreferredSize
        layout = new GridBagLayout();
        setLayout(layout);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        tierOverview = new JLabel();

        ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
        ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
        CheckBoxTableCellRenderer cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setIcon(untickIcon);
        cbRenderer.setSelectedIcon(tickIcon);
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // create a tablemodel for linguistic types
        if (types != null) {
            columns = new String[] {
                    LinguisticTypeTableModel.NAME,
                    LinguisticTypeTableModel.STEREOTYPE,
                    LinguisticTypeTableModel.CV_NAME,
                    LinguisticTypeTableModel.TIME_ALIGNABLE,
                    LinguisticTypeTableModel.GRAPHICS
                };
            model = new LinguisticTypeTableModel(types, columns);
        } else {
            model = new LinguisticTypeTableModel();
        }

        typeTable = new JTable(model);
        typeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeTable.getSelectionModel().addListSelectionListener(this);

        for (int i = 0; i < typeTable.getColumnCount(); i++) {
            if (typeTable.getModel().getColumnClass(i) != String.class) {
                typeTable.getColumn(typeTable.getModel().getColumnName(i))
                         .setPreferredWidth(35);
            }

            if (typeTable.getModel().getColumnClass(i) == Boolean.class) {
                typeTable.getColumn(typeTable.getModel().getColumnName(i))
                         .setCellRenderer(cbRenderer);
            }
        }

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(typeTable);

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(tierOverview, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        typButton = new JButton(ElanLocale.getString(
                    "Menu.Type.AddNewType"));
        typButton.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        gbc.weightx = 0.0;
        add(typButton, gbc);
    }

    /**
     * Remove the table and linguistictype button and add a progressbar  and a
     * message label.
     */
    private void adjustComponents() {
        JPanel progressPanel = new JPanel(new GridBagLayout());
        JPanel filler = new JPanel();
        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        progressPanel.add(filler, gbc);

        progressLabel = new JTextArea("...");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        progressPanel.add(progressLabel, gbc);
        
        progressLabel.setEditable(false);
        progressLabel.setBackground(progressPanel.getBackground());

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setValue(0);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        progressPanel.add(progressBar, gbc);

        remove(scrollPane);
        remove(typButton);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(progressPanel, gbc);

        revalidate();
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("MultiStep.Reparent.SelectType");
    }

    /**
     * Get the selected tier and new parent and fill the linguistic type table.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        tierName = (String) multiPane.getStepProperty("SelTier");

        selTier = (TierImpl) transcription.getTierWithId(tierName);

        if (selTier != null) {
            curType = selTier.getLinguisticType();
        }

        Object par = multiPane.getStepProperty("SelNewParent");

        if (par != null) {
            newParentName = (String) par;

            newParent = (TierImpl) transcription.getTierWithId(newParentName);
        } else {
        	newParent = null;
            newParentName = "-";
        }

        tierOverview.setText("<html><table><tr><td>" +
            ElanLocale.getString("MultiStep.Reparent.SelectedTier") + " " +
            "</td><td>" + tierName + "</td></tr>" + "<tr><td>" +
            ElanLocale.getString("MultiStep.Reparent.SelectedParent") + " " +
            "</td><td>" + newParentName + "</td></tr>");

        Constraint con = null;

        if (newParent != null) {
            con = newParent.getLinguisticType().getConstraints();
        }

        if (newParent == null) {
            model.showOnlyStereoTypes(new int[] { -1 }); // -1 == no constraints
        } else if ((con == null) ||
                (con.getStereoType() == Constraint.TIME_SUBDIVISION) ||
                (con.getStereoType() == Constraint.INCLUDED_IN)) {
            // parent is root or time subdivision
            model.showOnlyStereoTypes(new int[] {
                    Constraint.TIME_SUBDIVISION, Constraint.INCLUDED_IN,
                    Constraint.SYMBOLIC_SUBDIVISION, Constraint.SYMBOLIC_ASSOCIATION
                });
        } else {
            // parent is symbolic subdivision or association
            model.showOnlyStereoTypes(new int[] {
                    Constraint.SYMBOLIC_SUBDIVISION,
                    Constraint.SYMBOLIC_ASSOCIATION
                });
        }

        int col = model.findColumn(LinguisticTypeTableModel.NAME);

        for (int i = 0; i < model.getRowCount(); i++) {
            Object o = model.getValueAt(i, col);

            if (o instanceof String) {
                if (curType.getLinguisticTypeName().equals((String) o)) {
                    selTypeName = (String) o;
                    typeTable.setRowSelectionInterval(i, i);
                    multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);

                    break;
                }
            }
        }
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
        // n.a.
    }

    /**
     * Never called.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
        // this is the last step, cannot go to next
        return false;
    }

    /**
     * Disable the finish button and allow a step back.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    public boolean leaveStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);

        return true;
    }

    /**
     * No cleanup necessary.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#cancelled()
     */
    public void cancelled() {
    }

    /**
     * No cleanup necessary.
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#finished()
     */
    public void finished() {
    }

    /**
     * This is the last step. When all conditions have been met create a
     * command and register as progress listener. Disable buttons.
     *
     * @return true if the process has been finished successfully
     *
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    public boolean doFinish() {
        // the actual reparenting/copying of the tier(s)
        // disable all buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        //System.out.println("T: " + tierName + " P: " + newParentName + " LT: " +
         //   selTypeName);

        // create a command passing the selected tier, the selected new parent and the
        // (new) linguistic type for selected tier
        // add this panel as progress listener
        adjustComponents();

        if ((tierName != null) && (newParentName != null) &&
                (selTypeName != null)) {
            Boolean includeDepTiers = Boolean.TRUE;

            if (copyMode) {
                com = (CopyTierCommand) ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.COPY_TIER);

                Object include = multiPane.getStepProperty("IncludeDepTiers");

                if (include instanceof Boolean) {
                    includeDepTiers = (Boolean) include;
                }
            } else {
                com = (CopyTierCommand) ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.REPARENT_TIER);
            }

            ((CopyTierCommand) com).addProgressListener(this);
            com.execute(transcription,
                new Object[] {
                    tierName, newParentName, selTypeName, includeDepTiers
                });

            return false;
        } else {
            progressLabel.setText("Illegal selection");
            multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
            multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);

            return false;
        }

        //System.out.println("T: " + tierName + " P: " + newParentName + " LT: " + selTypeName);
        //return false;
    }

    /**
     * Enable the finish button once a valid type has been selected.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int row = typeTable.getSelectedRow();

            if (row > -1) {
                int col = model.findColumn(LinguisticTypeTableModel.NAME);
                String typeName = (String) model.getValueAt(row, col);

                for (int i = 0; i < types.size(); i++) {
                    LinguisticType t = (LinguisticType) types.get(i);

                    if (t.getLinguisticTypeName().equals(typeName)) {
                        selTypeName = typeName;

                        break;
                    }
                }

                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
            } else {
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
            }
        }
    }

    /**
     * Show the "add new linguistic type" dialog, then rescan the types to add
     * to  the table.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // store current selection, if any
        String typeName = null;

        if (typeTable.getRowCount() > 0) {
            int row = typeTable.getSelectedRow();

            if (row > -1) {
                int col = model.findColumn(LinguisticTypeTableModel.NAME);
                typeName = (String) model.getValueAt(row, col);
            }
        }

        List<String> curTypes = new ArrayList<String>();
        int col = model.findColumn(LinguisticTypeTableModel.NAME);
        
        for (int i = 0; i < model.getRowCount(); i++) {
        	curTypes.add( (String) model.getValueAt(i, col) );
        }
        
        new EditTypeDialog2(null, true, transcription, EditTypeDialog2.ADD).setVisible(true);
        // if the new type dialog is used then check whether any type has changed
        // rebuild the model. Check if new type has been added; if so select it
        model.removeAllRows();
        
        for (int i = 0; i < types.size(); i++) {
            model.addLinguisticType((LinguisticType) types.get(i));
        }
        
    	// try to detect the (first) new type, select it
    	int firstNewOrChanged = -1;
    	//int totalNew = 0;
        String tname = null;

        for (int i = 0; i < model.getRowCount(); i++) {
            tname = (String) model.getValueAt(i, col);
        
            if (!curTypes.contains(tname)) {
            	//totalNew++;
            	//if (firstNewOrChanged == -1) {
            		firstNewOrChanged = i;
            		break;
            	//}
            }
        }
        
        if (firstNewOrChanged > -1) {
            typeTable.setRowSelectionInterval(firstNewOrChanged, firstNewOrChanged);
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);	
        } else if (typeName != null) {// this selects the previously selected.
            tname = null;

            for (int i = 0; i < model.getRowCount(); i++) {
                tname = (String) model.getValueAt(i, col);

                if (typeName.equals(tname)) {
                    typeTable.setRowSelectionInterval(i, i);
                    multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);

                    break;
                }
            }
        }
    }
    
    /**
     * Unregister as a progress listener and close the pane.
     */
	protected void endOfProcess() {
        if (com != null) {
            ((CopyTierCommand) com).removeProgressListener(this);
        }

        multiPane.close();
	}

}
