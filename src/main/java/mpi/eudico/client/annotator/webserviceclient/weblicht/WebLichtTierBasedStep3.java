package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

public class WebLichtTierBasedStep3 extends StepPane {
	private JList serviceList;
	private DefaultListModel model;
	private String contentType;
	
	public WebLichtTierBasedStep3(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	protected void initComponents() {
		super.initComponents();
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(2, 0, 2, 0);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		add(new JLabel("Select a service"), gbc);
		
		model = new DefaultListModel();
		serviceList = new JList(model);
		JScrollPane scrollPane = new JScrollPane(serviceList);
		scrollPane.setPreferredSize(new Dimension(100, 80));
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(scrollPane, gbc);
	}

	public void enterStepForward() {
		super.enterStepForward();
		
		if (contentType == null) {
			contentType = (String) multiPane.getStepProperty("ContentType");
			fillListForType(contentType);
		} else {
			String oldContentType = contentType;
			contentType = (String) multiPane.getStepProperty("ContentType");
			if (contentType != null && !contentType.equals(oldContentType)) {
				fillListForType(contentType);
			}
		}
	}
	
	public String getStepTitle() {
		return "WebLicht: select a web service or service chain";
	}

	private void fillListForType(String type) {
		model.removeAllElements();
		// add services
	}
}
