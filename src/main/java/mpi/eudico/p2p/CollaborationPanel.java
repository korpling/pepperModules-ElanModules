package mpi.eudico.p2p;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;


/**
 * A panel that show information on  and show UI elements to interact with
 * a p2p/collaborative annotation session.
 * @author Han Sloetjes
 */
public class CollaborationPanel extends JComponent implements ActionListener,
	TableModelListener, ElanLocaleListener {
    private JLabel sessionNameLabel;
    private JLabel chairNameLabel;
    private JLabel docNameLabel;
    private JLabel sessionValueLabel;
    private JLabel chairValueLabel;
    private JLabel docValueLabel;
    private JTable sessionTable;

    private DefaultTableModel sessionTableModel;
    private JButton requestControlButton;
    private JButton byeButton;

    private String controllingParticipantMail;
    private String localParticipantName;
    private String localParticipantMail;
	private ElanP2P elanP2P;

	private static Logger logger = Logger.getLogger(CollaborationPanel.class.getName());

    /**
     * Creates a new CollaborationPanel instance.
     */
    public CollaborationPanel() {
        initPanel();
        populate(this);
    }

	/**
	 * Creates a new CollaborationPanel instance. The ElanP2P object
	 * handles the communication between this panel and other Elan objects
	 * and the P2P server.
	 * @param elanP2P the ElanP2P object
	 */
	public CollaborationPanel(ElanP2P elanP2P) {
		this.elanP2P = elanP2P;
		initPanel();
	//	populate(this);
	}

    private void initPanel() {
        setLayout(new GridBagLayout());

        Insets insets = new Insets(3, 5, 3, 5);
        sessionNameLabel = new JLabel();
        chairNameLabel = new JLabel();
        docNameLabel = new JLabel();
        sessionValueLabel = new JLabel();
        chairValueLabel = new JLabel();
        docValueLabel = new JLabel();
        sessionTableModel = new CollaborativeTableModel();
        //sessionTableModel.addTableModelListener(this);
        sessionTable = new JTable(sessionTableModel);

        JScrollPane tableScrollPane = new JScrollPane(sessionTable);
        requestControlButton = new JButton();
        requestControlButton.setActionCommand("control");
        requestControlButton.addActionListener(this);
        byeButton = new JButton();
        byeButton.addActionListener(this);
        byeButton.setActionCommand("bye");

        // add to gridlayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;

        add(sessionNameLabel, gbc);
        gbc.gridx = 1;
        add(sessionValueLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(chairNameLabel, gbc);
        gbc.gridx = 1;
        add(chairValueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(docNameLabel, gbc);
        gbc.gridx = 1;
        add(docValueLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(tableScrollPane, gbc);

		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		add(requestControlButton, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(byeButton, gbc);

        updateLocale();
		sessionTable.getColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Control")).setMaxWidth(60);
		//RadioButtonCellEditor controlEditor = new RadioButtonCellEditor();
		//sessionTable.getColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Control")).setCellEditor(controlEditor);
		sessionTable.getColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Control")).setCellRenderer(new RadioButtonTableCellRenderer());

    }

	/**
	 * Leave a collaborative annotation session.
	 */
    private void logOff() {
    	logger.info("Signing off...");
    	elanP2P.leaveSession();
		for (int i = sessionTableModel.getRowCount() - 1; i >= 0; i--) {
			sessionTableModel.removeRow(i);
		}
		final String empty = "";
		setSessionName(empty);
		setChairName(empty);
		setSharedDocumentName(empty);
    }

    private void requestControl() {
    	if (localParticipantMail == null) {
    		return;
    	}
    	if (localParticipantMail.equals(controllingParticipantMail)) {
    		return;
    	}
		elanP2P.requestControl();
    	setControllingParticipant(localParticipantMail);
		logger.info("Setting controlling participant to: " + localParticipantMail);
    }

	/**
	 * Returns the email address of the local participant.
	 * @return the email address of the local participant
	 */
	public String getLocalParticipantMail() {
		return localParticipantMail;
	}

	/**
	 * Returns the name of the local participant.
	 * @return the name of the local participant
	 */
	public String getLocalParticipantName() {
		return localParticipantName;
	}

	/**
	 * Sets the email address of the local participant.
	 * @param localParticipantMail the email address of the local participant
	 */
	public void setLocalParticipantMail(String localParticipantMail) {
		this.localParticipantMail = localParticipantMail;
	}

	/**
	 * Sets the name of the local participant.
	 * @param localParticipantName the name of the local participant
	 */
	public void setLocalParticipantName(String localParticipantName) {
		this.localParticipantName = localParticipantName;
	}

    /**
     * Sets the name of the p2p session.
     * @param name the name of the session
     */
    public void setSessionName(String name) {
    	if (name != null) {
			sessionValueLabel.setText(name);
    	}
    }

    /**
     * Returns the name of the current session.
     * @return the name of the current session
     */
    public String getSessionName() {
    	return sessionValueLabel.getText();
    }

	/**
	 * Sets the name of the chairman (m/w) of the session.
	 * @param name the name of the chairman (m/w)
	 */
    public void setChairName(String name) {
    	if (name != null) {
    		chairValueLabel.setText(name);
    	}
    }

    /**
     * Returns the name of the chairman (m/w) of the session, i.e.
     * the publisher.
     * @return the name of the chairman (m/w)
     */
    public String getChairName() {
    	return chairValueLabel.getText();
    }

	/**
	 * Sets the name of the shared document of the session.
	 * @param name the name of the shared document
	 */
    public void setSharedDocumentName(String name) {
    	if (name != null) {
    		docValueLabel.setText(name);
    	}
    }

	/**
	 * Returns the name of the shared document of the session.
	 * @return the name of the shared document
	 */
    public String getSharedDocumentName() {
    	return docValueLabel.getText();
    }

    /**
     * Adds a participant to the table.<br>
     * For the time being only a name and email need to be supplied.
     * Only the name should be unique, for now.
     * The new participant initially does not have the control.
     * Eventually a unique id should probably be passed.
     * @param name the name of the participant
     * @param email the email address
     */
    public void addParticipant(String name, String email) {
    	if (name == null) {
    		logger.warning("Illegal Argument: A participant without a name is not allowed");
    		throw new IllegalArgumentException("A participant without a name is not allowed");
    	}
    	if (email == null) {
			logger.warning("Illegal Argument: A participant without a valid email address is not allowed");
			throw new IllegalArgumentException("A participant without a valid email address is not allowed");
    	}
    	int nameColumn = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Participant"));
    	int emailColumn = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Mail"));
    	for (int i = 0; i < sessionTableModel.getRowCount(); i++) {
    		String nm = (String)sessionTableModel.getValueAt(i, nameColumn);
    		if (name.equals(nm)) {
				logger.warning("Illegal Argument: A participant with this name already exists");
				throw new IllegalArgumentException("A participant with this name already exists");
    		}
    		String mail = (String)sessionTableModel.getValueAt(i, emailColumn);
    		if (email.equals(mail)) {
				logger.warning("Illegal Argument: A participant with this email address already exists");
				throw new IllegalArgumentException("A participant with this email address already exists");
    		}
    	}
    	sessionTableModel.addRow(new String[]{"false", name, email});
    }

	/**
	 * Removes a participant from the table.<br>
	 * @param name the name of the participant
	 * @param email the email address of the participant
	 */
    public void removeParticipant(String email) {
		int column = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Mail"));
		for (int i = 0; i < sessionTableModel.getRowCount(); i++) {
			String n = (String)sessionTableModel.getValueAt(i, column);
			if (email.equals(n)) {
				sessionTableModel.removeRow(i);
				if (controllingParticipantMail.equals(email)) {
					controllingParticipantMail = null;
					// should we give one of the remaining participants control?
				}
				return;
			}
		}
    }

    /**
     * Sets the participant that has control.
     * @param mail the email address of the participant that has gained control
     */
    public void setControllingParticipant(String mail) {
		int mailColumn = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Mail"));
		int controlColumn = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Control"));
		//sessionTableModel.removeTableModelListener(this);
		for (int i = 0; i < sessionTableModel.getRowCount(); i++) {
			String n = (String)sessionTableModel.getValueAt(i, mailColumn);
			if (mail.equals(n)) {
				sessionTableModel.setValueAt("true", i, controlColumn);
				controllingParticipantMail = mail;
			} else {
				sessionTableModel.setValueAt("false", i, controlColumn);
			}
		}
		//sessionTableModel.addTableModelListener(this);
    }

    /**
     * Returns the email address of the participant that currently has control.
     * @return the email address of the participant that has control or null
     */
    public String getControllingParticipant() {
    	return controllingParticipantMail;
    }

    /**
     * A preliminary implementation for the use of Locales.
     */
    public void updateLocale() {
        sessionNameLabel.setText(ElanLocale.getString(
                "P2P.CollaborationPanel.Label.Session"));
        chairNameLabel.setText(ElanLocale.getString(
                "P2P.CollaborationPanel.Label.Chair"));
        docNameLabel.setText(ElanLocale.getString(
                "P2P.CollaborationPanel.Label.Document"));
        sessionValueLabel.setText("");
        chairValueLabel.setText("");
        docValueLabel.setText("");
        sessionTableModel.setColumnIdentifiers(new String[] {
                ElanLocale.getString("P2P.CollaborationPanel.Header.Control"),
                ElanLocale.getString("P2P.CollaborationPanel.Header.Participant"),
                ElanLocale.getString("P2P.CollaborationPanel.Header.Mail")
            });
        byeButton.setText(ElanLocale.getString("P2P.CollaborationPanel.Button.Bye"));
		requestControlButton.setText(ElanLocale.getString("P2P.CollaborationPanel.Button.Control"));
    }

    /**
     * Implement ActionListener. Handles button presses etc.
     *
     * @param event the ActionEvent
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("bye")) {
            logOff();
        } else if (event.getActionCommand().equals("control")) {
        	requestControl();
        }
    }

	/**
	 * Performs a check on a change of the table model.
	 * @param tme the tablemodelevent
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent tme) {
		if (tme.getType() == TableModelEvent.UPDATE) {
			String oldControllingPartMail = controllingParticipantMail;
			sessionTableModel.removeTableModelListener(this);
			int row = tme.getFirstRow();
			int column = tme.getColumn(); //should be the "Control" column
			// check if the row was already the controller row
			TableModel tm = (TableModel)tme.getSource();

			for (int i = 0; i < tm.getRowCount(); i++) {
				if (i != row) {
					tm.setValueAt("false", i, column);
				} else {
					int mailColumn = sessionTableModel.findColumn(ElanLocale.getString("P2P.CollaborationPanel.Header.Mail"));
					controllingParticipantMail = (String)tm.getValueAt(i, mailColumn);
					if (!controllingParticipantMail.equals(oldControllingPartMail)) {
						// notify group
						// elanP2P.setControllingParticipant(controllingParticipant);
						logger.info("Setting controlling participant to: " + controllingParticipantMail);
					}
				}
			}
			sessionTableModel.addTableModelListener(this);
		}
	}

	/**
	 * A temporary main method for testing purposes.
	 *
	 * @param args args
	 */
	public static void main(String[] args) {
		final CollaborationPanel cp = new CollaborationPanel();

		// temp, show in separate frame
		showFrame(cp);
		populate(cp);
	}

    private static void showFrame(CollaborationPanel cp) {
        JFrame frame = new JFrame("Collaboration");
        frame.getContentPane().add(cp);
        frame.setSize(400, 300);
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent we) {
                    System.exit(0);
                }
            });
        frame.setVisible(true);
    }

    private static void populate(final CollaborationPanel cp) {
		new Thread(new Runnable(){
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
				}
				cp.setSessionName("Test session");
				cp.setChairName("Kofi Annan");
				cp.setSharedDocumentName("test document");
		//		cp.addParticipant("hopi", "hopi@hetnet.nl");
		//		cp.addParticipant("solana", "solana@webos.es");
		//		cp.addParticipant("lubbi", "lubbi@demon.nl");
		//		cp.setControllingParticipant("solana@webos.es");
		//		cp.setLocalParticipantName("hopi");
		//		cp.setLocalParticipantMail("hopi@hetnet.nl");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
				}
				//cp.removeParticipant("solana@webos.es");
			}
		}).start();
    }

    //********************************************************
    // internal classes
    //********************************************************

    class CollaborativeTableModel extends DefaultTableModel {
		/**
		 * Returns true for the "Control" column, false for the other columns.
		 *
 		 * @param   row       the row whose value is to be queried
 		 * @param   column    the column whose value is to be queried
		 * @return  true true for the "Control" column, false otherwise
		 * @see #setValueAt
		 */
		public boolean isCellEditable(int row, int column) {
			/*
			String head = getColumnName(column);
			if (head != null && head.equals(ElanLocale.getString("P2P.CollaborationPanel.Header.Control"))) {
				return true;
			}
			*/
			return false;
		}

    }


    /**
     * A table cell editor that uses a JRadioButton as the editor component.
     * @author Han Sloetjes
     */
    class RadioButtonCellEditor extends AbstractCellEditor
    		implements TableCellEditor, ActionListener, Serializable {
		private JRadioButton radioButton;

		/**
		 * No-arg constructor.
		 */
		RadioButtonCellEditor() {
			radioButton = new JRadioButton();
			radioButton.addActionListener(this);
		}

		/**
		 * Returns the JRadioButton editor component.
		 * @param table the enclosing table
		 * @param value the current value
		 * @param isSelected the selected state of the table cell
		 * @param row the rowindex
		 * @param column the column index
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(
			JTable table, Object value, boolean isSelected,
			int row, int column) {
			setValue(value);
			if(isSelected){
				radioButton.setBackground(table.getSelectionBackground());
			} else {
				radioButton.setBackground(table.getBackground());
			}
			radioButton.setHorizontalAlignment(SwingConstants.CENTER);
			return radioButton;
		}

		/**
		 * Sets the selected state of the radiobutton.
		 * @param value a Boolean or String
		 */
		private void setValue(Object value) {
			boolean selected = false;
			if (value instanceof Boolean) {
				selected = ((Boolean)value).booleanValue();
			} else  if (value instanceof String) {
				selected = ((String)value).equalsIgnoreCase("true");
			}
			radioButton.setSelected(selected);
		}

		/**
		 * Called when the editing has been canceled.
		 * @see javax.swing.CellEditor#cancelCellEditing()
		 */
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		/**
		 * Called when the editing has been stopped. Always returns true
		 * because the new value is always accepted without validation.
		 * @see javax.swing.CellEditor#stopCellEditing()
		 */
		public boolean stopCellEditing() {
			fireEditingStopped();
			return true;
		}

		/**
		 * Returns the current value of the radio button.
		 * @return true if the radiobutton is selected, false otherwise
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {
			return new Boolean(radioButton.isSelected());
		}

		/**
		 * Returns true.
		 * @return always returns true
		 * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
		 */
		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		/**
		 * Returns false.
		 * @param anEvent the event
		 * @return always returns false
		 * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
		 */
		public boolean shouldSelectCell(EventObject anEvent) {
			return false;
		}

		/**
		 * Implement ActionListener by firing editingStopped
		 * @param ae the actionevent
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			if (!radioButton.isSelected()) {
				radioButton.setSelected(true);
			} else {
				// apparently a change in the participant that has control occurred
				//System.out.println("control change");
			}
			RadioButtonCellEditor.this.stopCellEditing();
		}

    }

    /**
     * A table cell renderer that uses a JRadioButton to render boolean values.
     * @author Han Sloetjes
     */
    class RadioButtonTableCellRenderer extends JRadioButton
    		implements TableCellRenderer, Serializable {

		RadioButtonTableCellRenderer() {
			super();
			setOpaque(true);
		}

		/**
		 * Returns a radio button for the specified cell to render
		 * the boolean value.
		 * @param table the enclosing table
		 * @param value the current value
		 * @param isSelected the selected state of the table cell
		 * @param row the rowindex
		 * @param column the column index
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				super.setBackground(table.getSelectionBackground());
			} else {
				super.setBackground(table.getBackground());
			}

			setValue(value);
			setHorizontalAlignment(SwingConstants.CENTER);
			return this;
		}

		/**
		 * Overridden for performance reasons.
		 */
		public void validate() {}

		/**
		 * Overridden for performance reasons.
		 */
		public void revalidate() {}

		/**
		 * Overridden for performance reasons.
		 */
		public void repaint(long tm, int x, int y, int width, int height) {}

		/**
		 * Overridden for performance reasons.
		 */
		public void repaint(Rectangle r) { }

		/**
		 * Sets the <code>Boolean</code> value for the cell being rendered to
		 * <code>value</code>.
		 *
		 * @param value  the Boolean value for this cell; if value is
		 *		<code>null</code> it sets the value to <code>false</code>
		 * @see JRadioButton#setSelected
		 */
		protected void setValue(Object value) {
			boolean selected = false;
			if (value instanceof Boolean) {
				selected = ((Boolean)value).booleanValue();
			} else  if (value instanceof String) {
				selected = ((String)value).equalsIgnoreCase("true");
			}
			super.setSelected(selected);
		}

    }

}
