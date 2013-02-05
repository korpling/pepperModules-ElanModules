package mpi.eudico.client.annotator.webserviceclient;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep1;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep2;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtTierBasedStep2;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtTierBasedStep3;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Dialog to select and access web services. Access to and interaction with a
 * service is guided by a wizard.
 * 
 * @author keeloo
 */

public class WebServicesDialog extends ClosableDialog implements
	ActionListener, TreeSelectionListener {
    // id generated by integrated development environment
    private static final long serialVersionUID = 1L;

    private JLabel titleLabel;
    private JScrollPane treeScrollPane;
    private JPanel prefCatPanel;
    private JButton cancelButton;
    private JPanel buttonPanel;
    private JTree catTree;
    private HashMap<String, String> catKeyMap;
    private HashMap<String, JComponent> activatedPanels;
    private CardLayout cardLayout;
    private JComponent currentEditPanel = null;
    private TranscriptionImpl trans;

    /**
     * Create a new WebServiceDialog instance. Invoked by WebServicesDlgCommand.
     * 
     * @throws HeadlessException
     */
    public WebServicesDialog() throws HeadlessException {
	initComponents();
    }

    /**
     * Create a WebServiceDialog instance. Invoked by WebServicesDlgCommand.
     * 
     * @param owner
     *            the parent
     * @param modal
     *            modal flag
     * 
     * @throws HeadlessException
     */
    public WebServicesDialog(Frame owner, boolean modal)
	    throws HeadlessException {
	this(owner, "", modal);
    }

    /**
     * Create a new WebServicesDialog instance
     * 
     * @param owner
     *            the parent
     * @param title
     *            the dialog title
     * @param modal
     *            modal flag
     * 
     * @throws HeadlessException
     */
    public WebServicesDialog(Frame owner, String title, boolean modal)
	    throws HeadlessException {
	super(owner, title, modal);
	initComponents();
    }

    /**
     * Finish initialization of the dialog pane by setting it's size and
     * location.
     */
    private void postInit() {
	pack();

	int w = 720;
	int h = 450;
	setSize((getSize().width < w) ? w : getSize().width,
		(getSize().height < h) ? h : getSize().height);
	setLocationRelativeTo(getParent());
    }

    /**
     * Associate a transcription with the dialog.
     */
    public void setTranscription(TranscriptionImpl trans) {
	this.trans = trans;
    }

    /**
     * Add a title panel, a preferences category tree pane, a place holder panel
     * for category specific panels and a button panel to the new dialog pane.
     */
    private void initComponents() {

	catKeyMap = new HashMap<String, String>();
	activatedPanels = new HashMap<String, JComponent>();
	getContentPane().setLayout(new GridBagLayout());

	Insets insets = new Insets(2, 6, 2, 6);

	titleLabel = new JLabel();
	titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
	titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

	// add title
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.insets = new Insets(6, 6, 10, 6);
	gbc.weightx = 1.0;
	gbc.gridwidth = 2;
	getContentPane().add(titleLabel, gbc);

	// add tree as treeScrollPane
	catTree = new JTree(new DefaultMutableTreeNode(""));
	catTree.getSelectionModel().setSelectionMode(
		TreeSelectionModel.SINGLE_TREE_SELECTION);

	((DefaultTreeCellRenderer) catTree.getCellRenderer()).setLeafIcon(null);
	((DefaultTreeCellRenderer) catTree.getCellRenderer()).setOpenIcon(null);
	((DefaultTreeCellRenderer) catTree.getCellRenderer())
		.setClosedIcon(null);

	treeScrollPane = new JScrollPane(catTree);

	Dimension dim = new Dimension(180, 300);
	treeScrollPane.setPreferredSize(dim);
	treeScrollPane.setMinimumSize(dim);
	treeScrollPane.setBackground(Color.WHITE);

	gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.VERTICAL;
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.insets = insets;
	gbc.weighty = 1.0;
	gbc.gridy = 1;
	getContentPane().add(treeScrollPane, gbc);

	/*
	 * Add a place holder panel. The steps associated with a key in the tree
	 * will be stored in a card layout. Later on, one of the cards will be
	 * shown in the panel.
	 */
	cardLayout = new CardLayout();
	prefCatPanel = new JPanel(cardLayout);
	prefCatPanel.add(
		new JLabel(ElanLocale
			.getString("WebServicesDialog.SelectService")),
		"Intro_xxx");
	cardLayout.show(prefCatPanel, "Intro_xxx");

	gbc = new GridBagConstraints();
	gbc.anchor = GridBagConstraints.NORTHWEST;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = insets;
	gbc.gridy = 1;
	gbc.gridx = 1;

	// add the panel containing the card layout to the pane
	getContentPane().add(prefCatPanel, gbc);

	// add cancel button
	buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
	cancelButton = new JButton();
	cancelButton.addActionListener(this);
	buttonPanel.add(cancelButton);

	gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.SOUTH;
	gbc.insets = insets;
	gbc.gridy = 2;
	gbc.weightx = 0.0;
	gbc.gridwidth = 2;
	getContentPane().add(buttonPanel, gbc);

	updateLocale();
	postInit();

	// register listener associated with the pane's category tree
	catTree.addTreeSelectionListener(this);
    }

    /**
     * Supply components in the dialog pane with names from ElanLocale.
     */
    private void updateLocale() {
	setTitle(ElanLocale.getString("WebServicesDialog.Title"));
	titleLabel.setText(ElanLocale.getString("WebServicesDialog.Title"));
	cancelButton.setText(ElanLocale.getString("Button.Cancel"));

	/*
	 * Add web service names to the key map associated with the tree in the
	 * pane. For the moment, use strings directly, not via the locale.
	 */
	String val = ElanLocale
		.getString("WebServicesDialog.WebService.TypeCraft");
	catKeyMap.put(val, "WebServicesDialog.WebService.TypeCraft");
	((DefaultMutableTreeNode) catTree.getModel().getRoot())
		.add(new DefaultMutableTreeNode(val));
	val = ElanLocale.getString("WebServicesDialog.WebService.WebLicht");
	catKeyMap.put(val, "WebServicesDialog.WebService.WebLicht");
	((DefaultMutableTreeNode) catTree.getModel().getRoot())
		.add(new DefaultMutableTreeNode(val));

	catTree.setEditable(false);
	catTree.expandRow(0);
    }

    /**
     * Create a component for holding the panels that are associated with steps
     * taken with the wizard that accmpanies the service chosen from the tree.
     * 
     * @param key
     *            the service
     * 
     * @return the pane
     */
    private JComponent getPanelsForKey(String key) {
	if (key == null) {
	    return null;
	}

	if (key.equals("WebServicesDialog.WebService.TypeCraft")) {

	    // create wizard dialog component
	    MultiStepPane pane = new MultiStepPane(
		    ElanLocale.getResourceBundle());

	    // add the login step as a panel in the component
	    StepPane stepOne = new WebServicesDialogTCStepOne(pane);
	    pane.addStep(stepOne);

	    // add a panel for the step of chosing between download and upload
	    StepPane choseStep = new WebServicesDialogTCInOut(pane);
	    pane.addStep(choseStep);

	    // add a panel containing the step to handle download
 	    StepPane stepTwo = new WebServicesDialogTCStepTwo(pane);
 	    stepTwo.setName("download");
 	    pane.addStep(stepTwo);

	    // add a panel for the upload step
	    StepPane stepThree = new WebServicesDialogTCStepThree(pane);
	    stepThree.setName("upload");
	    pane.addStep(stepThree);

	    pane.putStepProperty("transcription", trans);
	    pane.setButtonVisible(MultiStepPane.ALL_BUTTONS, false);

	    // return the component containing the panels
	    return pane;

	} else if (key.equals("WebServicesDialog.WebService.WebLicht")) {
	    /*
	     * Similar to the TypeCraft case, create a wizard dialog and the
	     * steps to be included in it.
	     */
	    MultiStepPane pane = new MultiStepPane(
		    ElanLocale.getResourceBundle());
	    StepPane step1 = new WebLichtStep1(pane);
	    pane.addStep(step1);
	    step1.setName("TextOrTierStep1");
	    pane.setButtonVisible(MultiStepPane.CANCEL_BUTTON, false);

	    StepPane step2 = new WebLichtStep2(pane);
	    step2.setName("TextStep2");
	    pane.addStep(step2);
		
		StepPane stTier2 = new WebLichtTierBasedStep2(pane);
		stTier2.setName("TierStep2");
		pane.addStep(stTier2);
		
		StepPane stTier3 = new WebLichtTierBasedStep3(pane);
		stTier3.setName("TierStep3");
		pane.addStep(stTier3);
		
	    pane.putStepProperty("transcription", trans);

	    return pane;
	}

	return null;
    }

    /**
     * Release resources that belong to the web services dialog.
     */
    private void closeDialog() {
	dispose();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
	if (event.getSource() == cancelButton) {
	    setVisible(false);
	    closeDialog();
	}
    }

    /**
     * Implementation of the abstract method from TreeSelectionListener. This
     * method handles selection changes in the web services tree.
     * 
     * @param e
     *            the event
     */
    public void valueChanged(TreeSelectionEvent e) {

	// get selection from the tree
	Object selNode = e.getPath().getLastPathComponent();
	if (selNode instanceof DefaultMutableTreeNode) {
	    String key = (String) ((DefaultMutableTreeNode) selNode)
		    .getUserObject();
	    String val = (String) catKeyMap.get(key);
	    // val holds the current selection in the tree

	    if ((currentEditPanel != null)
		    && (activatedPanels.get(val) == currentEditPanel)) {
		/*
		 * Selection in the tree has not been changed, and the
		 * corresponding wizard panel has been build. Nothing needs to
		 * be done in this case.
		 */
		return;
	    } else if (activatedPanels.get(val) != null) {
		/*
		 * Selection in the tree has been changed, but the wizard panel
		 * indicated is already in the list of panels that have been
		 * build. In this case, the designated component in the dialog
		 * pane already holds the cards associated with the wizard step
		 * indicated by the key selected from the tree. Show them.
		 */
		cardLayout.show(prefCatPanel, val);

		// keep track of the panel currently being edited in
		currentEditPanel = (JComponent) activatedPanels.get(val);
	    } else {
		/*
		 * The panel holding the the wizard steps indicated by the key
		 * has not been build yet. Create it now.
		 */
		JComponent keyPanel = getPanelsForKey(val);

		if (keyPanel != null) {
		    // add the panel to the designated component in the dialog
		    // pane
		    prefCatPanel.add(keyPanel, val);

		    // show the card layout in the panel
		    cardLayout.show(prefCatPanel, val);

		    // remember the panel currently being edited
		    currentEditPanel = keyPanel;

		    /*
		     * Remember the panel as a panel already build. Panels are
		     * to be build only once.
		     */
		    activatedPanels.put(val, keyPanel);
		}
	    }
	}
    }
}
