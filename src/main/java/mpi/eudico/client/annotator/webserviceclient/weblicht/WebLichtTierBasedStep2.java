package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.TierTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class WebLichtTierBasedStep2 extends StepPane implements ItemListener, ListSelectionListener {
	private TierTableModel ttm;
	private JTable tierTable;
	private ComboBoxModel typeModel;
	private JComboBox typeCB;
	private boolean tiersLoaded = false;
	
	public WebLichtTierBasedStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}


	protected void initComponents() {
		setLayout(new GridBagLayout());
		
		JLabel selectLabel = new JLabel("Select the tier to upload.");
		JLabel typeLabel = new JLabel("Specify the content type of the tier to upload");
		ttm = new TierTableModel();
		// load the tiers the first time this pane becomes active
		tierTable = new JTable(ttm);
		JScrollPane tierScroll = new JScrollPane(tierTable);
		typeModel = new DefaultComboBoxModel(); 
		typeCB = new JComboBox(typeModel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(2, 0, 2, 0);
		gbc.insets = insets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 2;
		
		add(selectLabel, gbc);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(tierScroll, gbc);
		
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		add(typeLabel, gbc);
		
		gbc.gridx = 1;
		add(typeCB, gbc);
	}

	/**
	 * The first time this pane is entered fill the tier table.
	 */
	public void enterStepForward() {
		if (!tiersLoaded) {
			TranscriptionImpl trans = (TranscriptionImpl) multiPane.getStepProperty("transcription");
			
			if (trans != null) {
				List tiers = trans.getTiers();
				
				for (int i = 0; i < tiers.size(); i++) {
					ttm.addRow((TierImpl) tiers.get(i));
				}
			}
			tierTable.getSelectionModel().addListSelectionListener(this);
			tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			typeCB.addItem("Unknown");
			typeCB.addItem("Sentence");
			typeCB.addItem("Word/token");
			typeCB.addItem("POS");
			typeCB.addItemListener(this);
			
			tiersLoaded = true;
		}
		// set enabled, warn if conditions are not met
		if (tierTable.getSelectedRow() > -1 && typeCB.getSelectedIndex() != 0) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}
	
	/**
	 * (Re)enable the next button.
	 */
	public void enterStepBackward() {
		super.enterStepBackward();
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}
	
	/**
	 * Return to the step of the choice between plain text and tier upload.
	 * 
	 * @return the id of the text/tier decision step
	 */
	public String getPreferredPreviousStep() {
		return "TextOrTierStep1";
	}

	/**
     * Returns the title
     */
	public String getStepTitle() {
		return "WebLicht processing: select a tier and a content type";
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			String selected = (String) typeCB.getSelectedItem();
			if ("Unknown".equals(selected)) {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			} else {
				if (tierTable.getSelectedRow() < 0) {
					multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
				} else {
					multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
				}
			}
		}		
	}

	/**
	 * Checks whether a tier and a type are selected.
	 */
	public boolean leaveStepForward() {
		if (tierTable.getSelectedRow() < 0) {
			// show message
			JOptionPane.showMessageDialog(this, "Please select a tier to upload", "Warning", 
					JOptionPane.WARNING_MESSAGE, null);
			
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			return false;
		}
		if (typeCB.getSelectedIndex() == 0) {
			// show message
			JOptionPane.showMessageDialog(this, "Please select the type of content to be uploaded.", "Warning", 
					JOptionPane.WARNING_MESSAGE, null);
			
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			return false;
		}
		multiPane.putStepProperty("Tier", tierTable.getValueAt(tierTable.getSelectedRow(), 0));
		multiPane.putStepProperty("ContentType", typeCB.getSelectedItem());
		return true;
	}


	public void valueChanged(ListSelectionEvent e) {
		int row = tierTable.getSelectedRow();
		if (row < 0) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		} else {
			String selected = (String) typeCB.getSelectedItem();
			if ("Unknown".equals(selected)) {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			} else {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
			}
		}
		
	}
	
	
}
