package mpi.eudico.client.annotator.linkedmedia;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A panel for adding and removing of secondary linked files, 
 * additional files not containing a primary source of annotation.
 * 
 * @author Han Sloetjes
 */
public class SecLinkedFilesPanel extends JPanel implements ActionListener,
	ListSelectionListener, TableModelListener {
	private TranscriptionImpl transcription;
	
	/** empty value string */
	private final String NO_SOURCE = "-";

	// ui stuff
	private JScrollPane linkScrollPane;
	private JTable linkTable;
	private JPanel linkInfoPanel;
	private JLabel linkInfoLabel;
	private JButton addMB;
	private JButton removeMB;
	private JButton updateMB;
	private JButton associateJB;
	private JPanel linkButtonPanel;
	// data
	private Vector descCopy;
	
	public SecLinkedFilesPanel(Transcription trans) {
		this.transcription = (TranscriptionImpl) trans;
		
		if (transcription != null) {
			Vector orgLFD = transcription.getLinkedFileDescriptors();
			descCopy = new Vector(orgLFD.size());

			LinkedFileDescriptor lfd;
			LinkedFileDescriptor cloneLFD;

			for (int i = 0; i < orgLFD.size(); i++) {
				lfd = (LinkedFileDescriptor) orgLFD.get(i);
				cloneLFD = (LinkedFileDescriptor) lfd.clone();

				if (cloneLFD != null) {
					descCopy.add(cloneLFD);
				}
			}
		}
		
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * dialog.
	 */
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
		ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
		CheckBoxTableCellRenderer cbRenderer = new CheckBoxTableCellRenderer();
		cbRenderer.setIcon(untickIcon);
		cbRenderer.setSelectedIcon(tickIcon);
		cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				
		linkScrollPane = new JScrollPane();
		linkTable = new JTable();
		linkInfoPanel = new JPanel();
		linkInfoLabel = new JLabel();
		linkButtonPanel = new JPanel();
		addMB = new JButton();
		removeMB = new JButton();
		updateMB = new JButton();
		associateJB = new JButton();
		
		setLayout(new GridBagLayout());

		Insets insets = new Insets(2, 6, 2, 6);

		linkScrollPane.setMinimumSize(new Dimension(100, 100));
		linkScrollPane.setPreferredSize(new Dimension(550, 100));
		
		LFDescriptorTableModel model = new LFDescriptorTableModel(descCopy);
		linkTable.setModel(model);

		linkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		linkTable.getSelectionModel().addListSelectionListener(this);
		linkTable.getModel().addTableModelListener(this);

		for (int i = 0; i < linkTable.getColumnCount(); i++) {
			if (linkTable.getModel().getColumnClass(i) != String.class) {
				linkTable.getColumn(linkTable.getModel().getColumnName(i))
						  .setPreferredWidth(35);
			}

			if (linkTable.getModel().getColumnClass(i) == Boolean.class) {
				linkTable.getColumn(linkTable.getModel().getColumnName(i))
						  .setCellRenderer(cbRenderer);
			}
		}
		
		linkScrollPane.setViewportView(linkTable);
		linkScrollPane.getViewport().setBackground(linkTable.getBackground());
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = insets;
		add(linkScrollPane, gridBagConstraints);

		linkInfoPanel.setLayout(new BorderLayout());
		linkInfoLabel.setFont(linkInfoLabel.getFont().deriveFont(Font.PLAIN, 10));
		fillInfoPanel(0);
		linkInfoPanel.add(linkInfoLabel, BorderLayout.WEST);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = insets;
		add(linkInfoPanel, gridBagConstraints);

		linkButtonPanel.setLayout(new GridLayout(2, 2, 6, 2));

		addMB.addActionListener(this);
		linkButtonPanel.add(addMB);

		removeMB.setEnabled(false);
		removeMB.addActionListener(this);
		linkButtonPanel.add(removeMB);

		associateJB.setEnabled(false);
		associateJB.addActionListener(this);
		linkButtonPanel.add(associateJB);
		
		updateMB.setEnabled(false);
		updateMB.addActionListener(this);
		linkButtonPanel.add(updateMB);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.insets = insets;
		add(linkButtonPanel, gridBagConstraints);

		updateLocale();
	}

	/**
	 * Applies localized strings to the ui elements.
	 */
	private void updateLocale() {
		linkInfoPanel.setBorder(new TitledBorder(ElanLocale.getString(
					"LinkedFilesDialog.Label.LinkInfo")));
		addMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Add"));
		removeMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Remove"));
		updateMB.setText(ElanLocale.getString("LinkedFilesDialog.Button.Update"));
		associateJB.setText(ElanLocale.getString("LinkedFilesDialog.Button.AssociatedWith"));
	}
		
	/**
	 * Checks whether changes have been made to the set of linked media files
	 * and, if any, creates a command that replaces the  media descriptors.
	 * The dialog is then closed.
	 */
	void applyChanges() {

		if (hasChanged()) {
			Command c = ELANCommandFactory.createCommand(transcription,
					ELANCommandFactory.CHANGE_LINKED_FILES);
			c.execute(transcription, new Object[] { new Vector(descCopy), Boolean.FALSE });
		}
		
		// update the copy vector and the table
		Vector orgLFD = transcription.getLinkedFileDescriptors();

		LinkedFileDescriptor lfd, clfd;
		LinkedFileDescriptor cloneLFD;
outerloop:
		for (int i = 0; i < orgLFD.size(); i++) {
			lfd = (LinkedFileDescriptor) orgLFD.get(i);
			
			for (int j = 0; j < descCopy.size(); j++) {
				clfd = (LinkedFileDescriptor) descCopy.get(j);
				if (lfd.linkURL.equals(clfd.linkURL)) {
					continue outerloop;
				}
			}
			// not yet in the copy
			cloneLFD = (LinkedFileDescriptor) lfd.clone();

			if (cloneLFD != null) {
				descCopy.add(cloneLFD);
				// the table model has a reference to the same vector of link descriptors
				((LFDescriptorTableModel) linkTable.getModel()).rowDataChanged();
			}
		}
	}

	/**
	 * Checks whether anything has changed in the linked files setup. 
	 * 
	 * @return whether anything has been changed in the linked files setup
	 */
	boolean hasChanged() {
		boolean anyChange = false;
		
		Vector orgMD = transcription.getLinkedFileDescriptors();
		LinkedFileDescriptor olddesc;
		LinkedFileDescriptor newdesc;

		// first compare the size of the vectors
		if (orgMD.size() != descCopy.size()) {
			anyChange = true;
		}

		// if the size is the same check if all elements are the same
		if (!anyChange) {
		outerloop: 
			for (int i = 0; i < orgMD.size(); i++) {
				olddesc = (LinkedFileDescriptor) orgMD.get(i);

				for (int j = 0; j < descCopy.size(); j++) {
					newdesc = (LinkedFileDescriptor) descCopy.get(j);

					if ((olddesc != null) && olddesc.equals(newdesc)) {
						// check on change in master media and let the order be important
						//if ((i == 0 && j > 0) || (i > 0 && j == 0)) { master change
						if (i != j) {
							anyChange = true;

							break outerloop;
						}

						continue outerloop;
					}
				}

				// if we come here something has changed
				anyChange = true;

				break outerloop;
			}
		}
				
		return anyChange;
	}
	
	/**
	 * Prompts the user to select a file and creates a descriptor for the file.
	 */
	private void addDescriptor() {
		String file = chooseFile();
		
		if (file == null) {
			return;
		}
		
		LinkedFileDescriptor lfd = LinkedFileDescriptorUtil.createLFDescriptor(file);
		
		for (int i = 0; i < descCopy.size(); i++) {
			LinkedFileDescriptor otherLFD = (LinkedFileDescriptor) descCopy.get(i);

			if (otherLFD.linkURL.equals(lfd.linkURL)) {
				showWarningDialog(ElanLocale.getString(
						"LinkedFilesDialog.Message.AlreadyLinked"));

				return;
			}
		}
		
		descCopy.add(lfd);

		// the table model has a reference to the same vector of link descriptors
		((LFDescriptorTableModel) linkTable.getModel()).rowDataChanged();

		linkTable.getSelectionModel().setLeadSelectionIndex(descCopy.size() -
			1);
	}
	
	/**
	 * Removes the selected linkdescriptor from the list/table. It thereby is
	 * also removed from the vector of copied descriptors. Other descriptors
	 * may have to be changed or removed, e.g. when there is an linkdescriptor with an
	 * associated with field pointing to the removed descriptor.
	 */
	private void removeDescriptor() {
		int row = linkTable.getSelectedRow();

		if (row >= 0) {
			// the row number is the same as the position of the LinkedFileDescriptor 
			LinkedFileDescriptor lfd = (LinkedFileDescriptor) descCopy.get(row);
			descCopy.remove(row);

			for (int i = descCopy.size() - 1; i >= 0; i--) {
				LinkedFileDescriptor desc = (LinkedFileDescriptor) descCopy.get(i);

				if (lfd.linkURL.equals(desc.associatedWith)) {
					desc.associatedWith = null;
					//descCopy.remove(i);
				}
			}
			// the table model has a reference to the same vector of link descriptors
			((LFDescriptorTableModel) linkTable.getModel()).rowDataChanged();
		}
	}
	
	/** 
	 * Presents a selection box with possible files/descriptors to associate this 
	 * file/descriptor with.
	 */
	private void setAssociatedWith() {
		int row = linkTable.getSelectedRow();

		if (row >= 0) {
			LinkedFileDescriptor updateLFD = (LinkedFileDescriptor) descCopy.get(row);
			String ref = selectAssociation(updateLFD);
			
			if (ref != null) {
				if (ref == NO_SOURCE) {
					updateLFD.associatedWith = null;
				} else {
					updateLFD.associatedWith = ref;
				}
			}
			
			((LFDescriptorTableModel) linkTable.getModel()).rowDataChanged();
			linkTable.getSelectionModel().setLeadSelectionIndex(row);
		}
	}
	
	/**
	 * Updates an existing descriptor e.g. when a file has been renamed or moved.
	 * Descriptors associated with this file will be updated as well. Time offset 
	 * can be maintained if the user chooses so.
	 */
	private void updateDescriptor() {
		int row = linkTable.getSelectedRow();

		if (row >= 0) {
			LinkedFileDescriptor updateLFD = (LinkedFileDescriptor) descCopy.get(row);
			String file = chooseFile();

			if (file == null) {
				return;
			}

			LinkedFileDescriptor lfd = LinkedFileDescriptorUtil.createLFDescriptor(file);			

			// it should not be exactly the same file
			if (lfd.linkURL.equals(updateLFD.linkURL)) {
				showWarningDialog(ElanLocale.getString(
						"LinkedFilesDialog.Message.SameFile"));

				return;
			}

			// should the updated file be of the same mime-type?
			for (int i = 0; i < descCopy.size(); i++) {
				if (i == row) {
					continue;
				}

				LinkedFileDescriptor otherLFD = (LinkedFileDescriptor) descCopy.get(i);

				// check whether the file was already linked
				if (otherLFD.linkURL.equals(lfd.linkURL)) {
					showWarningDialog(ElanLocale.getString(
							"LinkedFilesDialog.Message.AlreadyLinked"));

					return;
				}

				// update descriptors that were associated with this one ?
				if (updateLFD.linkURL.equals(otherLFD.associatedWith)) {
					otherLFD.associatedWith = lfd.linkURL;
				}

				if (updateLFD.associatedWith.equals(otherLFD.linkURL)) {
					lfd.associatedWith = otherLFD.linkURL;
				}
			}

			if (updateLFD.timeOrigin != 0) {
				// prompt user whether or not to maintain the offset
				if (showOptionDialog(ElanLocale.getString(
								"LinkedFilesDialog.Question.UpdateKeepOffset"))) {
					lfd.timeOrigin = updateLFD.timeOrigin;
				}
			}

			// finally replace the descriptor
			descCopy.remove(row);
			descCopy.add(row, lfd);

			// the table model has a reference to the same vector of media descriptors
			((LFDescriptorTableModel) linkTable.getModel()).rowDataChanged();
			linkTable.getSelectionModel().setLeadSelectionIndex(row);
		}		
	}

	/**
	 * Enables/disables buttons after a change in table data or table
	 * selection.
	 */
	private void updateUIComponents() {
		int row = linkTable.getSelectedRow();

		if ((row >= 0) && (row < descCopy.size())) {
			removeMB.setEnabled(true);
			updateMB.setEnabled(true);
			associateJB.setEnabled(true);
		} else {
			removeMB.setEnabled(false);
			updateMB.setEnabled(false);
			associateJB.setEnabled(false);
		}

		fillInfoPanel(row);
	}
		
	/**
	 * Sets the contents of the media file info panel. A JLabel with html
	 * formatting is used for the info strings, which are a kind of key-value
	 * pairs.
	 *
	 * @param row the source Descriptor for the info,  when null empty
	 *        value strings are used
	 */
	private void fillInfoPanel(int row) {
		TableModel model = linkTable.getModel();

		Object linkedObj = model.getValueAt(row, 5);

		boolean isLinked = (linkedObj instanceof Boolean)
			? ((Boolean) linkedObj).booleanValue() : false;

		linkInfoLabel.setText("<html><table>" + "<tr><td>" +
			model.getColumnName(0) + "</td><td>" +
			((model.getValueAt(row, 0) != null) ? model.getValueAt(row, 0) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(1) + "</td><td>" +
			((model.getValueAt(row, 1) != null) ? model.getValueAt(row, 1) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(2) + "</td><td>" +
			((model.getValueAt(row, 2) != null) ? model.getValueAt(row, 2) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(3) + "</td><td>" +
			((model.getValueAt(row, 3) != null) ? model.getValueAt(row, 3) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(4) + "</td><td>" +
			((model.getValueAt(row, 4) != null) ? model.getValueAt(row, 4) : "") +
			"</td></tr>" + "<tr><td>" + model.getColumnName(5) + "</td><td>" +
			((model.getValueAt(row, 5) != null)
			? (isLinked
			? ElanLocale.getString("LinkedFilesDialog.Label.StatusLinked")
			: ElanLocale.getString("LinkedFilesDialog.Label.StatusMissing")) : "") +
			"</td></tr>" + "</table></html>");
	}

	/**
	 * Shows a FileChooser for a mediafile.
	 *
	 * @param mediaType DOCUMENT ME!
	 *
	 * @return the full path to a mediafile as a String, or null
	 */
	private String chooseFile() {
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("Button.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString(
			"LinkedFilesDialog.SelectMediaDialog.Approve"),	null, null, true, "LinkedFileDir", FileChooser.FILES_ONLY, null);
		
		File selected = chooser.getSelectedFile();	
		if (selected != null){
			return selected.getAbsolutePath();
		}
		return null;
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
	 * Shows a yes-no option dialog with the specified question.
	 *
	 * @param question the question
	 *
	 * @return true if the user's answer is confirmative, false otherwise
	 */
	private boolean showOptionDialog(String question) {
		int option = JOptionPane.showOptionDialog(this, question,
				ElanLocale.getString("LinkedFilesDialog.Title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new String[] {
					ElanLocale.getString("Button.Yes"),
					ElanLocale.getString("Button.No")
				}, ElanLocale.getString("Button.Yes"));

		return (option == JOptionPane.YES_OPTION);
	}
	
	/**
	 * Presents a dialog with a list of all media files and other linked files, 
	 * except for the file represented by the specified descriptor.
	 * 
	 * @param descriptor the file that should be associated with another file
	 * @return the selcted url or null (dialog cancelled)
	 */
	private String selectAssociation(LinkedFileDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		String ref = null;
		
		List candidates = new ArrayList();
		candidates.add(NO_SOURCE);
		
		Vector mediaDesc = transcription.getMediaDescriptors();
		MediaDescriptor md;
		for (int i = 0; i < mediaDesc.size(); i++) {
			 md = (MediaDescriptor) mediaDesc.get(i);
			 candidates.add(md.mediaURL);
		}
		LinkedFileDescriptor lfd;
		for (int i = 0; i < descCopy.size(); i++) {
			lfd = (LinkedFileDescriptor) descCopy.get(i);
			if (lfd != descriptor) {
				candidates.add(lfd.linkURL);
			}
		}
		ref = (String) JOptionPane.showInputDialog(this,
				ElanLocale.getString("LinkedFilesDialog.Question.SelectAssocaition"),
				ElanLocale.getString("LinkedFilesDialog.Title"),
				JOptionPane.QUESTION_MESSAGE, null, candidates.toArray(), NO_SOURCE);
				
		return ref;
	}
			
	/**
	 * The action performed method.
	 *
	 * @param actionEvent the event
	 */
	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();

		if (source == addMB) {
			addDescriptor();
		} else if (source == removeMB) {
			removeDescriptor();
		} else if (source == updateMB) {
			updateDescriptor();
		} else if (source == associateJB) {
			setAssociatedWith();
		}
	}
		
	/**
	 * Updates some buttons after a change in the selected row.
	 *
	 * @param lse the event
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent lse) {
		if (!lse.getValueIsAdjusting()) {
			updateUIComponents();
		}
	}

	/**
	 * Updates some buttons after a change in the table model.
	 *
	 * @param tme the event
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void tableChanged(TableModelEvent tme) {
		//updateUIComponents();
	}
	
}
