package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * The first step in contacting a WebLicht service
 */
public class WebLichtStep1 extends StepPane {
	private JRadioButton fromScratchRB;
	private JRadioButton uploadTiersRB;
//	private JButton fromScratchB;
//	private JButton uploadTiersB;
	
	public WebLichtStep1(MultiStepPane multiPane) {
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
    	fromScratchRB = new JRadioButton("uploading plain text", true);
    	uploadTiersRB = new JRadioButton("uploading tier(s)");
    	ButtonGroup buttonGroup = new ButtonGroup();
    	buttonGroup.add(fromScratchRB);
    	buttonGroup.add(uploadTiersRB);
//    	fromScratchB = new JButton("Uploading plain text");
//    	fromScratchB.addActionListener(this);
//    	uploadTiersB = new JButton("Uploading tier(s)");
//    	uploadTiersB.addActionListener(this);
    	
    	JLabel label = new JLabel("Start WebLicht processing by");
    	GridBagConstraints gbc = new GridBagConstraints();
    	Insets insets = new Insets(2, 2, 2, 0);
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	add(label, gbc);
    	
    	gbc.gridy = 1;
    	gbc.insets = new Insets(2, 20, 2, 0);
    	add(fromScratchRB, gbc);
//    	add(fromScratchB, gbc);
    	gbc.gridy = 2;
    	add(uploadTiersRB, gbc);
//    	add(uploadTiersB, gbc);
    	
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;
    	add(new JPanel(), gbc);
    }

    /**
     * Returns the title
     */
	public String getStepTitle() {
		return "WebLicht processing";
	}

	/**
	 * Store which radio button is selected.
	 * 
	 *  @return true
	 */
	public boolean leaveStepForward() {
		if (fromScratchRB.isSelected()) {
			multiPane.putStepProperty("UploadContents", "Plain Text");
		} else {
			multiPane.putStepProperty("UploadContents", "Tiers");
		}
		
		return true;
	}

	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	public void enterStepForward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	
	/**
	 * The next step depends on the selected radio button.
	 */
	public String getPreferredNextStep() {
		if (fromScratchRB.isSelected()) {
			return "TextStep2";
		} else {
			return "TierStep2";
		}
	}
    
    
}
