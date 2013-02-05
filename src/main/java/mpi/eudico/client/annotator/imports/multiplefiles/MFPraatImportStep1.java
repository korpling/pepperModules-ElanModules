package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

public class MFPraatImportStep1 extends AbstractMFImportStep1 {
	
	private JComboBox encodingComboBox;
	
	private String[] encodings = null;

	public MFPraatImportStep1(MultiStepPane mp) {
		super(mp);
		
	}
	
	/**
	 * Initialize the ui components
	 */
	protected void initComponents(){	
		super.initComponents();
		
		remove(removeFilesBtn);
		
		encodings = new String[]{ElanLocale.getString("Button.Default"), 
	    		FileChooser.UTF_8, FileChooser.UTF_16};
		
		encodingComboBox = new JComboBox();
		for(int i=0; i < encodings.length; i++){
			encodingComboBox.addItem(encodings[i]);
		}
		encodingComboBox.setSelectedItem(FileChooser.UTF_8);
		
		JPanel panel = new JPanel();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = globalInset;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		add(removeFilesBtn, gbc);

		gbc.gridy = 3;	
		gbc.anchor = GridBagConstraints.WEST;
		add(panel, gbc);		
		
		
		panel.setLayout(new GridBagLayout());
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(ElanLocale.getString("FileChooser.Mac.Label.Encoding")), gbc);
		
		gbc.gridx = 1;
		panel.add(encodingComboBox, gbc);		

	}
	
	public boolean leaveStepForward(){	
			
		multiPane.putStepProperty("Encoding", encodingComboBox.getSelectedItem().toString());	
		
		return super.leaveStepForward();
	}
	
	protected Object[] getMultipleFiles() {
    	Object[] files = getMultipleFiles(ElanLocale.getString("MultiFileImport.Praat.Select"),
		FileExtension.PRAAT_TEXTGRID_EXT, "LastUsedPraatDir", FileChooser.FILES_AND_DIRECTORIES);    	

    	if ((files == null) || (files.length == 0)) {
    		return null;
    	}

    	ArrayList<String> extensions = new ArrayList<String>();
    	for(String ext: FileExtension.PRAAT_TEXTGRID_EXT){
    		if(!extensions.contains(ext.toLowerCase())){
    			extensions.add(ext.toLowerCase());
    		}
    	}

    	ArrayList<File> validFiles = new ArrayList<File>();
    	File file;
    	for(Object f: files){
    		file = (File)f;
    		if(file.isFile()){
    			if(file.canRead() && !validFiles.contains(file))
    				validFiles.add(file);
    		} else if(file.isDirectory()){
    			chooser.addFiles(file, validFiles, extensions);
    		}
    	}
    	
		return validFiles.toArray();
	}

}
