package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.webserviceclient.WsClientRest;
import mpi.eudico.webserviceclient.weblicht.WebLichtWsClient;

/**
 * A step in the interaction with WebLicht.
 * 
 * @author Han Sloetjes
 */
public class WebLichtStep2 extends StepPane implements ActionListener {
	private JTextArea textArea;
	private JLabel textLabel;
	private JButton uploadButton;

	public WebLichtStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

    /**
     * Initialize the panel.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    public void initComponents() {
    	setLayout(new GridBagLayout());
    	
    	textLabel = new JLabel("Type or paste text into the text field");
    	textArea = new JTextArea();
    	textArea.setWrapStyleWord(true);
    	textArea.setLineWrap(true);
    	uploadButton = new JButton("Upload");
    	uploadButton.addActionListener(this);
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = new Insets(6, 0, 6, 0);
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	add(textLabel, gbc);
    	
    	gbc.gridy = 1;
    	gbc.insets = new Insets(2, 0, 2, 0);
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1.0;
    	gbc.weighty = 1.0;
    	add(new JScrollPane(textArea), gbc);
    	
    	gbc.gridy = 2;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0;
    	gbc.weighty = 0.0;
    	add(uploadButton, gbc);
    }

	/**
	 * The button event handling
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == uploadButton) {
			String text = textArea.getText();
			if (text != null && text.length() > 0) {
				WebLichtWsClient wsClient = new WebLichtWsClient();
				// hier configure
				String tcf = wsClient.convertPlainText(text); 
				// immediately upload the contents to a sentence splitter
				if (tcf != null) {
					String sentenceTokenUrl = "service-opennlp-1_5/tcf/detect-sentences/tokenize";
					
					if (sentenceTokenUrl != null) {
						// convert to tiers or upload to next service?
						String tcf2 = wsClient.callWithTCF(sentenceTokenUrl, tcf);
						//System.out.println(tcf2);
					}
				}
			}
		}
		
	}

	public String getStepTitle() {
		return "WebLicht text upload";
	}
}
