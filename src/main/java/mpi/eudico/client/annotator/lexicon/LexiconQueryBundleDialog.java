package mpi.eudico.client.annotator.lexicon;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;

/**
 * Lets the user select an existing Lexicon Link and select a Lexical Entry Field
 * of that Lexicon Link and combines that to a LexiconQueryBundle
 * @author Micha Hulsbosch
 */
public class LexiconQueryBundleDialog extends ClosableDialog implements
		ActionListener, ListSelectionListener {
	
	private JLabel lexiconLinkLabel;
	private JComboBox lexiconLinkComboBox;
	private JTable lexiconEntryFieldTable;
	private JScrollPane entryFieldScroller;
	private JButton okButton;
	private JButton cancelButton;
	private TranscriptionImpl transcription;
	private LexiconQueryBundle2 oldQueryBundle;
	private boolean canceled;
	/** value for no connection! */
    private String none;
    
    private HashMap<String, LexiconEntryTableModel> fieldLists;
	private JLabel titleLabel;
	private JPanel lexiconEntryFieldPanel;
	
	private Component parent;

	public LexiconQueryBundleDialog(Dialog dialog, boolean modal, Transcription trans) {
		super(dialog, modal);
		this.parent = dialog;
		this.transcription = (TranscriptionImpl) trans;
		if (!transcription.isLexcionServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				ClientLogger.LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
			}
		}
		fieldLists = new HashMap<String, LexiconEntryTableModel>();
		canceled = true;
		initComponents();
		postInit();
	}

	public LexiconQueryBundleDialog(Dialog dialog, boolean modal, Transcription trans,
			LexiconQueryBundle2 queryBundle) {
		super(dialog, modal);
		this.parent = dialog;
		this.transcription = (TranscriptionImpl) trans;
		if (!transcription.isLexcionServicesLoaded()) {
			try {
				new LexiconClientFactoryLoader().loadLexiconClientFactories(transcription);
			} catch (Exception exc) {//just any exception
				ClientLogger.LOG.warning("Error while loading lexicon service clients: " + exc.getMessage());
			}
		}
		this.oldQueryBundle = queryBundle;
		fieldLists = new HashMap<String, LexiconEntryTableModel>();
		canceled = true;
		initComponents();
		postInit();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
			public void windowOpened(WindowEvent evt) {
				fillEntryFieldTable();
			}
		});
		
		titleLabel = new JLabel();
		lexiconLinkLabel = new JLabel();
		lexiconLinkComboBox = new JComboBox();
		lexiconLinkComboBox.addActionListener(this);
		lexiconEntryFieldPanel = new JPanel();
		lexiconEntryFieldPanel.setLayout(new GridBagLayout());
		lexiconEntryFieldPanel.setBorder(new TitledBorder(""));
		lexiconEntryFieldTable = new JTable();
		lexiconEntryFieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lexiconEntryFieldTable.getSelectionModel().addListSelectionListener(this);
		
		entryFieldScroller = new JScrollPane(lexiconEntryFieldTable);
		
		okButton = new JButton();
		okButton.addActionListener(this);
		cancelButton = new JButton();
		cancelButton.addActionListener(this);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Insets insets = new Insets(6, 6, 6, 6);
		
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = insets;
        c.weightx = 1.0;
        c.gridwidth = 2;
        this.add(titleLabel, c);
		
        c.fill = GridBagConstraints.NONE;
		c.insets = insets;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		this.add(lexiconLinkLabel, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 1;
		this.add(lexiconLinkComboBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		lexiconEntryFieldPanel.add(entryFieldScroller, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		this.add(lexiconEntryFieldPanel, c);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		c.insets = insets;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.SOUTH;
		this.add(buttonPanel, c);

	}

	private void postInit() {
		updateLocale();
		fillLexiconLinkCombo();
		setLocationRelativeTo(getParent());
		updateButtons();
		
		pack();
		
		int w = 450;
	    int h = 550;
	    setSize((getSize().width < w) ? w : getSize().width,
	        (getSize().height < h) ? h : getSize().height);
	    setLocationRelativeTo(parent);
	}

	private void updateLocale() {
		this.setTitle(ElanLocale.getString("EditQueryBundle.Title"));
		titleLabel.setText(getTitle());
		lexiconLinkLabel.setText(ElanLocale.getString("EditQueryBundle.Label.Links"));
		lexiconEntryFieldPanel.setBorder(new TitledBorder(ElanLocale.getString("EditQueryBundle.Label.Entryfield")));
		okButton.setText(ElanLocale.getString("Button.OK"));
		cancelButton.setText(ElanLocale.getString("Button.Cancel"));
		none = ElanLocale.getString("EditQueryBundle.None");
		if (lexiconLinkComboBox.getItemCount() > 0) {
			lexiconLinkComboBox.removeItemAt(0);
		}
		lexiconLinkComboBox.insertItemAt(none, 0);
	}

	/**
	 * Checks whether a Lexical Entry Field is selected in the table
	 * and en/disables the button accordingly
	 * @author Micha Hulsbosch
	 */
	private void updateButtons() {
		if((lexiconLinkComboBox.getSelectedIndex() > -1 && lexiconEntryFieldTable.getSelectedRow() > -1) ||
				(lexiconLinkComboBox.getSelectedIndex() == 0)) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	/**
	 * Fills the Lexicon Link Combobox with all Lexicon Links of the Transcription and the option
	 * to select none
	 * @author Micha Hulsbosch
	 */
	private void fillLexiconLinkCombo() {
		ArrayList<LexiconLink> lexiconLinks = new ArrayList(transcription.getLexiconLinks().values());
		lexiconLinkComboBox.removeActionListener(this);
		lexiconLinkComboBox.removeAllItems();
		lexiconLinkComboBox.addItem(none);
		for(LexiconLink link : lexiconLinks) {
			lexiconLinkComboBox.addItem(link.getName());
		}
		
		if(oldQueryBundle != null) {
			String linkName = oldQueryBundle.getLinkName();
			for(int i = 0; i < lexiconLinkComboBox.getItemCount(); i++) {
				if(linkName.equals(lexiconLinkComboBox.getItemAt(i))) {
					lexiconLinkComboBox.setSelectedIndex(i);
					break;
				}
			}
		} else if(lexiconLinkComboBox.getItemCount() > 0) {
			lexiconLinkComboBox.setSelectedIndex(0);
		}

		lexiconLinkComboBox.addActionListener(this);
	}

	/** 
	 * Fills the Entry Field Table with the entry fields that belong to 
	 * the selected Lexicon Link
	 * If the entry fields are loaded before during the life-cycle of this 
	 * dialog, the entry fields are loaded from memory
	 * @author Micha Hulsbosch
	 */
	private void fillEntryFieldTable() {
		lexiconEntryFieldTable.setModel(new LexiconEntryTableModel());
		if (lexiconLinkComboBox.getSelectedIndex() > 0) {
			ArrayList<LexiconLink> lexiconLinks = new ArrayList(
					transcription.getLexiconLinks().values());
			LexiconLink link = null;
			for(LexiconLink lnk2 : lexiconLinks) {
				if (lnk2.getName().equals(
						lexiconLinkComboBox.getSelectedItem())) {
					link = lnk2;
					break;
				}
			}
			if (link == null || link.getSrvcClient() == null) {
				JOptionPane.showMessageDialog(this, ElanLocale.getString("LexiconLink.NoClient"), 
						"Warning", JOptionPane.WARNING_MESSAGE);
			} else {
				if (!fieldLists.containsKey(lexiconLinkComboBox.getSelectedItem())) {
					boolean tryGetFieldIds = true;
					while (tryGetFieldIds) {
						try {
							ArrayList<LexicalEntryFieldIdentification> fldIds = link
							.getSrvcClient()
							.getLexicalEntryFieldIdentifications(link.getLexId());
							Collections.sort(fldIds);
							LexiconEntryTableModel tmpModel = new LexiconEntryTableModel();
							for (LexicalEntryFieldIdentification fldId : fldIds) {
								tmpModel.addRow(fldId);
							}
							lexiconEntryFieldTable
							.setModel(tmpModel);
							fieldLists.put(
									(String) lexiconLinkComboBox
									.getSelectedItem(),
									tmpModel);
							tryGetFieldIds = false;
						} catch (LexiconServiceClientException e) {
							if (e.getMessage().equals(LexiconServiceClientException.NO_USERNAME_OR_PASSWORD)
									|| e.getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
								LexiconLoginDialog loginDialog = new LexiconLoginDialog(
										this, link);
								loginDialog.setVisible(true);
								if (loginDialog.isCanceled()) {
									tryGetFieldIds = false;
								}
							} else {
								String title = ElanLocale.getString("LexiconLink.Action.Error");
								String message = title
								+ "\n"
								+ ElanLocale
								.getString("LexiconServiceClientException.Cause")
								+ " "
								+ e.getMessageLocale();
								JOptionPane.showMessageDialog(this,
										message, title,
										JOptionPane.ERROR_MESSAGE);
								tryGetFieldIds = false;
							}
						}
					}
					if (oldQueryBundle != null) {
						String fldIdName = oldQueryBundle.getFldId().getName();
						for (int i = 0; i < lexiconEntryFieldTable.getModel()
								.getRowCount(); i++) {
							if (fldIdName.equals(((LexiconEntryTableModel) lexiconEntryFieldTable
											.getModel()).getFldIdAtRow(i).getName())) {
								lexiconEntryFieldTable.setRowSelectionInterval(
										i, i);
								break;
							}
						}
					}
				} else {
					lexiconEntryFieldTable.setModel(fieldLists
							.get(lexiconLinkComboBox.getSelectedItem()));
				}
			}
		} 
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == okButton) {
			setCanceled(false);
			closeDialog();
		} else if (ae.getSource() == lexiconLinkComboBox) {
			fillEntryFieldTable();
			updateButtons();
		} else if (ae.getSource() == cancelButton){
			closeDialog();
		}
	}


	protected void closeDialog() {
		setVisible(false);
		dispose();
	}
	
	private void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return this.canceled;
	}
	
	public void valueChanged(ListSelectionEvent arg0) {
		updateButtons();
	}

	/**
	 * Returns the Lexicon Query Bundle containing the name of the
	 * Lexicon Link and the Lexical Entry Field ID
	 * @return the Lexicon Query Bundle
	 * @author Micha Hulsbosch
	 */
	public LexiconQueryBundle2 getBundle() {
		if (lexiconLinkComboBox.getSelectedIndex() > 0 &&
				lexiconEntryFieldTable.getSelectedRow() > -1) {
			LexiconLink theLink = null;
			String linkName = (String) lexiconLinkComboBox.getSelectedItem();
			ArrayList<LexiconLink> links = new ArrayList(transcription.getLexiconLinks().values());
			for(LexiconLink link : links) {
				if(link.getName().equals(linkName)) {
					theLink = link;
				}
			}
			
			LexicalEntryFieldIdentification theFldId = ((LexiconEntryTableModel) lexiconEntryFieldTable.getModel()).getFldIdAtRow(lexiconEntryFieldTable.getSelectedRow());
			return new LexiconQueryBundle2(theLink, theFldId);
			
		}
		return null;
	}

	private class LexiconEntryTableModel extends AbstractTableModel {
		
		ArrayList<LexicalEntryFieldIdentification> entryFields;
		private String[] columnNames;
		
		public LexiconEntryTableModel() {
			entryFields = new ArrayList<LexicalEntryFieldIdentification>();
			columnNames = new String[2];
			columnNames[0] = "Name";
			columnNames[1] = "Description";
		}
		
		public void addRow(LexicalEntryFieldIdentification entryField) {
			entryFields.add(entryField);
			fireTableRowsInserted(entryFields.size() - 1, entryFields.size() - 1);
		}
		
		public String getColumnName(int col) {
	        return columnNames[col];
	    }
	
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}
	
		public int getRowCount() {
			return entryFields.size();
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			LexicalEntryFieldIdentification entryField = entryFields.get(rowIndex);
			if(columnIndex == 0) {
				return entryField.getName();
			} else if (columnIndex == 1) {
				return entryField.getDescription();
			}
			return null;
		}
		
		public LexicalEntryFieldIdentification getFldIdAtRow(int rowNumber) {
			return entryFields.get(rowNumber);
		}
		
	}

}
