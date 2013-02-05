package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.lexicon.LexiconClientFactoryLoader;
import mpi.eudico.client.annotator.lexicon.LexiconLoginDialog;
import mpi.eudico.client.annotator.lexicon.ValueChooseDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
//import mpi.eudico.client.util.BrowserLaunch;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.lexicon.EntryElement;
import mpi.eudico.server.corpora.lexicon.Lexicon;
import mpi.eudico.server.corpora.lexicon.LexiconEntry;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 * This viewer lets the user perform a query on a lexicon service. The Lexicon Query Bundle of the Type
 * of the Tier of the selected Annotation is used.
 * @author Micha Hulsbosch
 *
 */
public class LexiconEntryViewer extends AbstractViewer implements 
		ACMEditListener, ActionListener, ElanLocaleListener, TreeSelectionListener {

	private JPanel mainPanel;
	private JTextField activeAnnotationValue;
	private JLabel activeAnnotationLabel;
	private JTree lexiconResponseTree;

	private JButton getLexiconEntryButton;
	
	private String activeAnnotationText;
	private Tier tierOfActiveAnnotation;
	
	private LexiconServiceClient currentClient;
	private JLabel tierOfActiveAnnotationLabel;
	private JTextField tierOfActiveAnnotationValue;
	private JLabel constraintsLabel;
	private JComboBox constraintsComboBox;
	private DefaultMutableTreeNode resultsNode;
	private DefaultTreeModel resultsTreeModel;
	private LexiconQueryBundle2 currentLexiconQueryBundle;
	private JScrollPane lexiconResponseTextScroller;
	private JLabel message;
	private Lexicon searchResultLexicon;
	private JButton changeAnnotationButton;
	private Annotation activeAnnotation;
	private ArrayList<String> selectedEntryValues;
	
	private boolean clientLoadStateChecked = false;
	
	
	public LexiconEntryViewer() {
		super();
		initComponents();
	}

	private void initComponents() {
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
	
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(0,5,0,0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		activeAnnotationLabel = new JLabel();
		searchPanel.add(activeAnnotationLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 0;
		
		activeAnnotationValue = new JTextField();
		searchPanel.add(activeAnnotationValue, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 0;
		gbc.gridy = 1;
		
		tierOfActiveAnnotationLabel = new JLabel();
		searchPanel.add(tierOfActiveAnnotationLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 1;
		
		tierOfActiveAnnotationValue = new JTextField();
		tierOfActiveAnnotationValue.setEditable(false);
		searchPanel.add(tierOfActiveAnnotationValue, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 0;
		gbc.gridy = 2;
		
		constraintsLabel = new JLabel();
		searchPanel.add(constraintsLabel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 2;
		
		constraintsComboBox = new JComboBox();
		searchPanel.add(constraintsComboBox, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 3;
		
		getLexiconEntryButton = new JButton();
		getLexiconEntryButton.setEnabled(false);
		getLexiconEntryButton.addActionListener(this);
		searchPanel.add(getLexiconEntryButton, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 4;
		
		changeAnnotationButton = new JButton();
		changeAnnotationButton.setEnabled(false);
		changeAnnotationButton.addActionListener(this);
		searchPanel.add(changeAnnotationButton, gbc);
		
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new GridBagLayout());
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		
		message = new JLabel();
		message.setBackground(Color.WHITE);
		
		resultsNode = new DefaultMutableTreeNode("Results");
		resultsTreeModel = new DefaultTreeModel(resultsNode);
		lexiconResponseTree = new JTree(resultsTreeModel);
		// HS change the rendering of the tree
        ((DefaultTreeCellRenderer) lexiconResponseTree.getCellRenderer()).setOpenIcon(null);
        ((DefaultTreeCellRenderer) lexiconResponseTree.getCellRenderer()).setClosedIcon(null);
        ((DefaultTreeCellRenderer) lexiconResponseTree.getCellRenderer()).setLeafIcon(null);
        ((DefaultTreeCellRenderer) lexiconResponseTree.getCellRenderer()).setTextNonSelectionColor(Color.BLACK);
        ((DefaultTreeCellRenderer) lexiconResponseTree.getCellRenderer()).setTextSelectionColor(Color.BLACK);
		lexiconResponseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		lexiconResponseTree.addTreeSelectionListener(this);
		lexiconResponseTree.setRootVisible(true);
		lexiconResponseTextScroller = new JScrollPane(lexiconResponseTree);
		resultsPanel.add(lexiconResponseTextScroller, gbc);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		mainPanel.add(searchPanel, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0,5,0,0);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		
		mainPanel.add(resultsPanel, gbc);
		
		setLayout(new GridBagLayout());
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		
		add(mainPanel, gbc);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == getLexiconEntryButton) {
			doSearch();
		} else if (e.getSource() == changeAnnotationButton) {
			changeAnnotation();
		}
	}

	/**
	 * Performs an actual search
	 */
	private void doSearch() {
		String searchString = activeAnnotationValue.getText();
		if(currentClient != null && !searchString.equals("")) {
			lexiconResponseTree.removeTreeSelectionListener(this);
			boolean trySearch = true;
			while (trySearch) {
				// Do a search
				try {
					String resultsStr = ElanLocale.getString("LexiconEntryViewer.Results");
					String lexiconStr = ElanLocale.getString("LexiconEntryViewer.Lexicon");
					String fieldStr = ElanLocale.getString("LexiconEntryViewer.Field");
					String constraintsStr = ElanLocale.getString("LexiconEntryViewer.Constraints");
					String searchStringStr = ElanLocale.getString("LexiconEntryViewer.SearchString");
					String performingStr = ElanLocale.getString("LexiconEntryViewer.PerformingMessage");
					String rootText = "(" + lexiconStr + ": <i>" + currentLexiconQueryBundle.getLink().getLexId().getName()
					+ "</i>; " + fieldStr + ": <i>" + currentLexiconQueryBundle.getFldId().getName() 
					+ "</i>; " +  constraintsStr + ": <i>" + constraintsComboBox.getSelectedItem()
					+ "</i>; " +  searchStringStr + ": <i>" + searchString + "</i>)</html>";
					
					message.setText("<html><b>" + performingStr + "</b> " + rootText);
					lexiconResponseTextScroller.setViewportView(message);
					
					searchResultLexicon = currentClient.search(
							currentLexiconQueryBundle.getLink().getLexId(),
							currentLexiconQueryBundle.getFldId(),
							(String) constraintsComboBox.getSelectedItem(),
							searchString);
					
					searchResultLexicon.setName("<html><b>" + resultsStr + "</b> " + rootText);
					lexiconResponseTree.setModel(searchResultLexicon);
					lexiconResponseTextScroller.setViewportView(lexiconResponseTree);
					trySearch = false;
				} catch (LexiconServiceClientException ex) {
					if (ex.getMessage().equals(LexiconServiceClientException.NO_USERNAME_OR_PASSWORD)
							|| ex.getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
						// Let user enter username/password
						Object parent = this.getRootPane().getParent();
						if (parent instanceof Frame) {
							LexiconLoginDialog loginDialog = new LexiconLoginDialog((Frame) parent, 
									currentLexiconQueryBundle.getLink());
							loginDialog.setVisible(true);
							if(loginDialog.isCanceled()) {
								trySearch = false;
							}
						}
					} else {
						// Show why the user couldn't do a search
						String title = ElanLocale.getString("LexiconLink.Action.Error");
						String message = title + "\n" + ElanLocale.getString("LexiconServiceClientException.Cause") + 
							" " + ex.getMessageLocale();
						JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
						trySearch = false;
					}
				}
				message.setText("");
			}
			lexiconResponseTree.addTreeSelectionListener(this);
		}
	}

	private void changeAnnotation() {
		String newAnnotationValue = "";
		if(selectedEntryValues.size() == 1) {
			newAnnotationValue = selectedEntryValues.get(0);
		} else if (selectedEntryValues.size() > 1) {
			Object parent = this.getRootPane().getParent();
			ValueChooseDialog chooser = new ValueChooseDialog((Frame) parent, selectedEntryValues);
			chooser.setVisible(true);
			if(!chooser.isCanceled()) {
				newAnnotationValue = chooser.getSelectedValue();
			} else {
				return;
			}
		}
		
		LinguisticType type = ((TierImpl) tierOfActiveAnnotation).getLinguisticType();
		Object extRef = null;
        
		if(type.isUsingControlledVocabulary()) {
			TranscriptionImpl trans = (TranscriptionImpl) tierOfActiveAnnotation.getParent();
            ControlledVocabulary cv = trans.getControlledVocabulary(((TierImpl) tierOfActiveAnnotation)
            		.getLinguisticType().getControlledVocabylaryName());
            CVEntry cvEntry = cv.getEntryWithValue(newAnnotationValue);
            if(cvEntry != null) {
            	extRef = cvEntry.getExternalRef();
            } 
		}
		Command c = ELANCommandFactory.createCommand(((Transcription) tierOfActiveAnnotation.getParent()),
    			ELANCommandFactory.MODIFY_ANNOTATION);
    	Object[] args = new Object[] { activeAnnotation.getValue(), newAnnotationValue, extRef };
    	c.execute(activeAnnotation, args);
	}

	@Override
	public void controllerUpdate(ControllerEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preferencesChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateActiveAnnotation() {
		doUpdate();
	}

	@Override
	public void updateLocale() {
		activeAnnotationLabel.setText(ElanLocale.getString("LexiconEntryViewer.AnnotationLabel"));
		tierOfActiveAnnotationLabel.setText(ElanLocale.getString("LexiconEntryViewer.Tier"));
		constraintsLabel.setText(ElanLocale.getString("LexiconEntryViewer.Constraints"));
		getLexiconEntryButton.setText(ElanLocale.getString("LexiconEntryViewer.GetEntriesButton"));
		changeAnnotationButton.setText(ElanLocale.getString("LexiconEntryViewer.ChangeAnnotation"));
	}

	@Override
	public void updateSelection() {
		// TODO Auto-generated method stub
		
	}

	public void ACMEdited(ACMEditEvent e) {
		doUpdate();
	}

	/**
	 * Does an update of the UI elements for the newly selected annotation (or no selected annotation).
	 */
	private void doUpdate() {
		activeAnnotationValue.setText("");
		tierOfActiveAnnotationValue.setText("");
		constraintsComboBox.removeAllItems();
		getLexiconEntryButton.setEnabled(false);
		
		activeAnnotation = getActiveAnnotation();
		if(activeAnnotation != null) {
			activeAnnotationText = activeAnnotation.getValue();
			activeAnnotationValue.setText(activeAnnotationText);
			
			tierOfActiveAnnotation = activeAnnotation.getTier();
			tierOfActiveAnnotationValue.setText(tierOfActiveAnnotation.getName());
			LinguisticType linguisticTypeOfTier = ((TierImpl) tierOfActiveAnnotation).getLinguisticType();
			currentLexiconQueryBundle = linguisticTypeOfTier.getLexiconQueryBundle();
			if (currentLexiconQueryBundle != null) {
				if (!clientLoadStateChecked) {
					if (!((TranscriptionImpl) tierOfActiveAnnotation.getParent()).isLexcionServicesLoaded()) {
						new LexiconClientFactoryLoader().loadLexiconClientFactories(
							(TranscriptionImpl) tierOfActiveAnnotation.getParent());
						((TranscriptionImpl) tierOfActiveAnnotation.getParent()).setLexcionServicesLoaded(true);
					}
					clientLoadStateChecked = true;
				}
				
				currentClient = currentLexiconQueryBundle.getLink().getSrvcClient();
				if (currentClient != null) {
					getLexiconEntryButton.setEnabled(true);

					ArrayList<String> constraints = currentClient.getSearchConstraints();
					for (int i = 0; i < constraints.size(); i++) {
						constraintsComboBox.addItem(constraints.get(i));
					}
				}
			} 
		}
	}

	public void valueChanged(TreeSelectionEvent event) {
		changeAnnotationButton.setEnabled(false);
		TreePath path = lexiconResponseTree.getSelectionPath();
		
		if (path != null) {
			Object node = path.getLastPathComponent();
			if (node instanceof LexiconEntry) {
				selectedEntryValues = ((LexiconEntry) node).getFocusFieldValues();
				changeAnnotationButton.setEnabled(true);
			} else if (node instanceof EntryElement) {
				String value = ((EntryElement) node).getValue();
				if (value.toLowerCase().startsWith("http")) {
					try {
						URL url = new URL(value);
						openURL(value);
					} catch (MalformedURLException mue) {
						// Apparently the value was not a URL; Do nothing
					}
				}
			}
		}
	}
	
	/**
     * Opens the web page in the default web browser of the system.
     * Temporarily copied from WebMA...
     */
    private void openURL(String url) {
        if (url == null) {
            return;
        }

        if (SystemReporting.isMacOS()) {
        	String[] command = new String[] { "open", url };
            
            String error = execCommand(command);
            if (error != null) {
            	errorMessage(error);
            }
        } else if (SystemReporting.isWindows()) {
        	//String[] command = new String[] { "cmd.exe", "/c", "start", url };
        	String[] command = new String[] { "rundll32", "url.dll", "FileProtocolHandler", url };
            String error = execCommand(command);
            if (error != null) {
            	errorMessage(error);
            }
        } else {// linux, try multiple variants
        	String[][] commands = new String[][] {
        		new String[] { "xdg-open", url },
        		new String[] { "gnome-open", url },
        		new String[] { "kde-open", url },
                new String[] { "firefox", url }
        	};
        	String error = "";
        	for (int i = 0; i < commands.length; i++) {
        		String nextError = execCommand(commands[i]);
        		if (nextError == null) {
        			return;
        		} else {
        			error = error + ", " + nextError;
        		}
        	}
        	errorMessage(error);
        }
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(this,
            (ElanLocale.getString("Message.Web.NoConnection") + ": " + message),
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Executes the command.
     * 
     * @param command the command string array
     * @return null if the command was executed successfully, an error message otherwise
     */
    private String execCommand(String[] command) {
        try {
            Process proc = Runtime.getRuntime().exec(command);
            
            try {
            	Thread.sleep(100);
            } catch (InterruptedException ie) {
            	// ignore
            }
            //proc.destroy();//doesn't work on Windows
            return null;
        } catch (SecurityException se) {
            ClientLogger.LOG.warning("No connection: " + se.getMessage());
            return se.getMessage();
        } catch (IOException ioe) {
            ClientLogger.LOG.warning("No connection: " + ioe.getMessage());
            return ioe.getMessage();
        }
    }

}
