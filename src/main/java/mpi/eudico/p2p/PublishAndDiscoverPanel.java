package mpi.eudico.p2p;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.ElanLocale;

/**
 * A panel containing input fields for information needed for the
 * participation in a p2p/collaboration session.
 * @author Han Sloetjes
 */
public class PublishAndDiscoverPanel extends JPanel {
	private String name;
	private String email;
	private String documentName;
	private JTextField docNameField;
	private JTextField partNameField;
	private JTextField partEmailField;
	private int mode = DISCOVER_MODE;
	
	public static final int PUBLISH_MODE = 0;
	public static final int DISCOVER_MODE = 1;

	/**
	 * Constructs a panel with input fields, mode is DISCOVER_MODE.
	 */
	public PublishAndDiscoverPanel() {
		super();
		initPanel();
	}
	
	/**
	 * Constructs a panel with input fields, mode is DISCOVER_MODE.
	 * @param mode one of DISCOVER_MODE or PUBLISH_MODE
	 */
	public PublishAndDiscoverPanel(int mode) {
		super();
		this.mode = mode;
		initPanel();
	}
	
	private void initPanel() {
		setLayout(new GridBagLayout());
		JLabel docNameLabel = new JLabel(ElanLocale.getString("P2P.PublishAndDiscoverPanel.Label.DocumentName"));
		JLabel nameLabel = new JLabel(ElanLocale.getString("P2P.PublishAndDiscoverPanel.Label.ParticipantName"));
		JLabel mailLabel = new JLabel(ElanLocale.getString("P2P.PublishAndDiscoverPanel.Label.ParticipantMail"));
		docNameField = new JTextField(24);
		if (mode == PUBLISH_MODE) {
			docNameField.setEditable(false);
		}
		partNameField = new JTextField(24);
		partEmailField = new JTextField(24);
		
		Insets insets = new Insets(2, 2, 2, 8);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = insets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		add(docNameLabel, gbc);
		gbc.gridy = 1;
		add(nameLabel, gbc);
		gbc.gridy = 2;
		add(mailLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		add(docNameField, gbc);
		gbc.gridy = 1;
		add(partNameField, gbc);
		gbc.gridy = 2;
		add(partEmailField, gbc);
	}

	/**
	 * Returns the document name.
	 * @return the document name from the input field or null
	 */
	public String getDocumentName() {
		if (docNameField.getText() != null) {
			documentName = docNameField.getText().trim();
		}
		
		return documentName;
	}

	/**
	 * Returns the email address.
	 * @return the email address from the input field or null
	 */
	public String getEmail() {
		if (partEmailField.getText() != null) {
			email = partEmailField.getText().trim();
		}
		
		return email;
	}

	/**
	 * Returns the name.
	 * @return the name from the input field or null
	 */
	public String getName() {
		if (partNameField.getText() != null) {
			name = partNameField.getText().trim();
		}
		
		return name;
	}

	/**
	 * Sets the initial document name.
	 * @param documentName the name of the document
	 */
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
		docNameField.setText(documentName);
	}

	/**
	 * Sets the initial email address of the participant.
	 * @param email the email address of the participant
	 */
	public void setEmail(String email) {
		this.email = email;
		partEmailField.setText(email);
	}

	/**
	 * Sets the initial name of the participant.
	 * @param name the name of the participant
	 */
	public void setName(String name) {
		this.name = name;
		partNameField.setText(name);
	}

}
