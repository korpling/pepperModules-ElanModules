package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxTypFile;

/**
 * Panel for step 2: Export/Toolbox options
 * 
 * Set toolbox options for the files
 * those are going to be exported
 * 
 * @author aarsom
 * @version Feb, 2012
 */
public class MultipleFileToolBoxExportStep2 extends StepPane implements ItemListener, ActionListener{

	private JCheckBox wrapBlocksCB;
	private JCheckBox correctTimesCB;
	private JCheckBox wrapLinesCB;
	private JCheckBox includeEmptyLinesCB;
	private JCheckBox mediaMarkerCB;	
	
	private JTextField numCharTF;
	private JTextField typField;
	private JTextField dbTypField;
	private JTextField markerTF;
	private JTextField mediaMarkerNameTF;
	
	private JLabel mediaMarkerNameLabel;
	private JLabel mediaTypeLabel;
	
	private JRadioButton ssMSFormatRB;
	private JRadioButton hhMMSSMSFormatRB;
	private JRadioButton wrapNextLineRB;
	private JRadioButton wrapAfterBlockRB;
	private JRadioButton typeRB;
	private JRadioButton specRB;
	private JRadioButton detectedRMRB;
	private JRadioButton defaultRMRB;
	private JRadioButton customRMRB;
	private JRadioButton videoRB;
	private JRadioButton audioRB;
	
	private JRadioButton absFilePathRB;
	private JRadioButton relFilePathRB;
	
	private JButton typButton;
	
	private JComboBox recordMarkerCB;
	
	private JPanel outputOptionsPanel;
	private JPanel toolboxOptionsPanel;
	
	private JScrollPane outerScrollPane;
	
	List<String> recordMarkerList = null;
	
	/** default line width */
    private final int NUM_CHARS = 80;
    
    Insets insets = new Insets(4, 6, 4, 6);
    Insets vertInsets = new Insets(0, 2, 2, 2);
    Insets leftVertIndent = new Insets(0, 16, 2, 2);
    Insets innerInsets = new Insets(4, 2, 4, 2);
	
    /**
     * Constructor
     * 
     * @param multiPane
     */
	public MultipleFileToolBoxExportStep2(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();		
	}
	
	/**
	 * Initialize the ui components
	 */
	protected void initComponents(){
		initOutputOptionsPanel();		
		initToolboxOptionsPanel();
		
		 JPanel outerPanel = new JPanel();
	     outerPanel.setLayout(new GridBagLayout());
	     outerScrollPane = new JScrollPane(outerPanel);
	     outerScrollPane.setBorder(null);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		Insets globalInset = new Insets(5, 10, 5, 10);		
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		outerPanel.add(outputOptionsPanel, gbc);		
	
		gbc.gridy = 1;
		outerPanel.add(toolboxOptionsPanel, gbc);
		
		gbc.gridy = 2;	
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		outerPanel.add(new JPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
        add(outerScrollPane, gbc);
        
        TextFieldHandler tfHandler = new TextFieldHandler();
        
        
        numCharTF.addKeyListener(tfHandler);
    	typField.addKeyListener(tfHandler);
    	dbTypField.addKeyListener(tfHandler);
    	markerTF.addKeyListener(tfHandler);
    	mediaMarkerNameTF.addKeyListener(tfHandler);
	
		setDefaultNumOfChars();

	    setShoeboxMarkerRB();

	    loadPreferences();
	}
	
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileExportToolbox.Title.Step2Title");
	}	
	
	
	
	public void enterStepForward(){
		
		boolean repaint = false;
		
		detectedRMRB.setEnabled(false);
     	defaultRMRB.setSelected(true);
     	detectedRMRB.setText(ElanLocale.getString(
         		"ExportShoebox.Label.Detected"));
     	if(recordMarkerCB != null){
     		toolboxOptionsPanel.remove(recordMarkerCB); 
     		recordMarkerCB = null;
     		
     		repaint = true;
     	}
		
		recordMarkerList = (List<String>) multiPane.getStepProperty("RecordMarkersList");	
    	if(recordMarkerList != null && recordMarkerList.size() > 0){
    		updateDetectedRecordMarker();
    		
    		repaint = true;
        } 	
			
		mediaMarkerCB.setSelected(false);
		mediaMarkerCB.setEnabled(false);
		
		boolean mediaDetected = (Boolean) multiPane.getStepProperty("EnableMediaMarker");			
		if (mediaDetected){
			mediaMarkerCB.setEnabled(mediaDetected);
			mediaMarkerCB.setSelected(mediaDetected);
			
			Boolean bothMediaDetected = (Boolean) multiPane.getStepProperty("BothMediaDetected");	
			if(!bothMediaDetected){
				audioRB.setEnabled(false);
				videoRB.setEnabled(false);
				mediaTypeLabel.setEnabled(false);
			}		
			repaint = true;
		} 
		
		if(repaint){
			repaint();
		}
			
		updateButtonStates();				
	}			
	
	/**
	 * Updates the button states
	 */
	public void updateButtonStates(){
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		
		if(wrapBlocksCB.isSelected()){
			if(numCharTF.getText() == null || 
				numCharTF.getText().trim().length() <= 0){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);	
				return;
			}
		}		
		
		if(typeRB.isSelected()){
			if(typField.getText() == null || 
					typField.getText().trim().length() <= 0){
					multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
					multiPane.setButtonToolTipText(MultiStepPane.NEXT_BUTTON, 
							ElanLocale.getString("ImportDialog.Message.SpecifyType"));
					return;
			}
		} else if(specRB.isSelected()){
			if(dbTypField.getText() == null || 
					dbTypField.getText().trim().length() <= 0){
					multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);	
					multiPane.setButtonToolTipText(MultiStepPane.NEXT_BUTTON, 
							ElanLocale.getString("ExportShoebox.Message.NoType"));
					return;
			}
		}
		
		if(customRMRB.isSelected()){
			if(markerTF.getText() == null || 
				markerTF.getText().trim().length() <= 0){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
				multiPane.setButtonToolTipText(MultiStepPane.NEXT_BUTTON, 
						ElanLocale.getString("ExportShoebox.Message.NoRecordMarker"));
				return;
			}
		}
		
		if(mediaMarkerCB.isSelected()){
			if(mediaMarkerNameTF.getText() == null || 
				mediaMarkerNameTF.getText().trim().length() <= 0){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);		
				multiPane.setButtonToolTipText(MultiStepPane.NEXT_BUTTON, 
						ElanLocale.getString("ExportShoebox.Message.NoMediaMarker"));
				return;
			}
		}
	}		
		
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {    	
    	int charsPerLine = Integer.MAX_VALUE;
    	
    	if (wrapBlocksCB.isSelected()) {
            String textValue = numCharTF.getText().trim();
            try {
                charsPerLine = Integer.parseInt(textValue);
            } catch (NumberFormatException nfe) {
            	JOptionPane.showMessageDialog(this, ElanLocale.getString("ExportShoebox.Message.InvalidNumber"),
                        ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }    
    	
    	String databaseType = null;
    	
    	if (typeRB.isSelected()) {
            File tf = new File(typField.getText());

            if (!tf.exists()) {
            	JOptionPane.showMessageDialog(this, ElanLocale.getString("ImportDialog.Message.NoType"),
                        ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

                return false;
            } else {
                try {
                    ShoeboxTypFile typFile = new ShoeboxTypFile(tf);
                    databaseType = typFile.getDatabaseType();
                } catch (Exception e) {
                }
            }
        } else {
            databaseType = dbTypField.getText();
        }
    	
    	int timeFormat = Interlinear.SSMS;

        if (hhMMSSMSFormatRB.isSelected()) {
            timeFormat = Interlinear.HHMMSSMS;
        }
        
        String recordMarker = null;
        
        // record marker test
        if (customRMRB.isSelected()) {
        	recordMarker = markerTF.getText().trim();
        } else if (defaultRMRB.isSelected()) {
        	recordMarker = "block"; 
        } else {
        	if(recordMarkerList.size() == 1){
        		recordMarker = recordMarkerList.get(0);
        	}else {
        		recordMarker = recordMarkerCB.getSelectedItem().toString();
        	}
        }
    	
    	//store the values 
    	multiPane.putStepProperty("CharsPerLine", charsPerLine);		
    	multiPane.putStepProperty("TimeFormat", timeFormat);    	
    	multiPane.putStepProperty("CorrectTimes", correctTimesCB.isSelected());
    	multiPane.putStepProperty("TypeFileSelected", typeRB.isSelected());
    	multiPane.putStepProperty("DatabaseType", databaseType);
    	multiPane.putStepProperty("WrapLines", wrapLinesCB.isSelected());
    	multiPane.putStepProperty("WrapNextLine", wrapNextLineRB.isSelected());
    	multiPane.putStepProperty("IncludeEmptyLines", includeEmptyLinesCB.isSelected());
    	multiPane.putStepProperty("UseDetectedRecordMarker", detectedRMRB.isSelected());
    	multiPane.putStepProperty("RecordMarker", recordMarker);
    	multiPane.putStepProperty("IncludeMediaMarkerCB", mediaMarkerCB.isSelected());
    	String mediaMarkerName = mediaMarkerNameTF.getText();
    	if(mediaMarkerName != null){
    		multiPane.putStepProperty("MediaMarkerName", mediaMarkerName.trim());
    	} 	
    	multiPane.putStepProperty("AudiofileType", audioRB.isSelected());
    	multiPane.putStepProperty("UseRelFilePath", relFilePathRB.isSelected());
    	
    	savePreferences();         
   		return true;       
    }    
    
    /**
     * Updates the detected record marker label
     */
    private void updateDetectedRecordMarker(){
    	detectedRMRB.setEnabled(true);
   		detectedRMRB.setSelected(true);
    	if(recordMarkerList.size() == 1){
       		detectedRMRB.setText(detectedRMRB.getText() + 
         		" (\\" + recordMarkerList.get(0) + ")");         		
       	 } else if(recordMarkerList.size() > 1){
       		 recordMarkerCB = new JComboBox();
       		 for(String marker: recordMarkerList){
       			 recordMarkerCB.addItem(marker);
       		 }        		 
       		 recordMarkerCB.setSelectedIndex(0);
       		 
       		GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 2, 2, 2);
            toolboxOptionsPanel.add(recordMarkerCB, gridBagConstraints);            
       	 }    	
    }
	
	/**
	 * Initializes the output options
	 */
	private void initOutputOptionsPanel(){			
		//panel
		outputOptionsPanel = new JPanel(new GridBagLayout());
		outputOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "ExportDialog.Label.Options")));
		
		//create all components
		wrapBlocksCB = new JCheckBox(ElanLocale.getString(
                "ExportShoebox.Label.WrapBlocks"));	
		wrapBlocksCB.setSelected(true);
		wrapBlocksCB.addItemListener(this);
	    		
		
		JLabel charPerLineLabel = new JLabel(ElanLocale.getString(
                "ExportShoebox.Label.NumberChars"));    
		
		numCharTF = new JTextField(4);
		numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
       
        JLabel timeFormatLabel = new JLabel(ElanLocale.getString(
                "ExportShoebox.Label.Format"));
		
		ssMSFormatRB = new JRadioButton(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.Seconds"));
		ssMSFormatRB.setSelected(true);
		
		hhMMSSMSFormatRB = new JRadioButton(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.TimeCode"));
       
		correctTimesCB = new JCheckBox(ElanLocale.getString("ExportDialog.CorrectTimes"));
       
		wrapLinesCB = new JCheckBox(ElanLocale.getString(
                "ExportShoebox.Label.WrapLines"));
		wrapLinesCB.setSelected(true);    
		wrapLinesCB.addItemListener(this);
       
		wrapNextLineRB = new JRadioButton(ElanLocale.getString(
        		"ExportShoebox.Label.WrapNextLine"));
		wrapNextLineRB.setSelected(true);
		
		wrapAfterBlockRB = new JRadioButton(ElanLocale.getString(
                "ExportShoebox.Label.WrapEndOfBlock"));
		
		includeEmptyLinesCB = new JCheckBox(ElanLocale.getString(
        		"ExportShoebox.Label.IncludeEmpty"));
		includeEmptyLinesCB.setSelected(true);
		
		//add radio buttons to button group
		ButtonGroup timeGroup = new ButtonGroup();
		timeGroup.add(ssMSFormatRB);
	    timeGroup.add(hhMMSSMSFormatRB);
	        
	    ButtonGroup wrapGroup = new ButtonGroup();
	    wrapGroup.add(wrapNextLineRB);
	    wrapGroup.add(wrapAfterBlockRB);	
	    
	    GridBagConstraints gridBagConstraints;
	    //wrap panel
	    JPanel wrapPanel = new JPanel(new GridBagLayout());
      
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapBlocksCB, gridBagConstraints);
     
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(numCharTF, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        wrapPanel.add(charPerLineLabel, gridBagConstraints);
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapLinesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(wrapNextLineRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        gridBagConstraints.gridwidth = 2;
        wrapPanel.add(wrapAfterBlockRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        wrapPanel.add(includeEmptyLinesCB, gridBagConstraints);
        
        //time panel
        JPanel timePanel = new JPanel(new GridBagLayout());
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(timeFormatLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(hhMMSSMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(ssMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(correctTimesCB, gridBagConstraints);
        
        // add to options panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        outputOptionsPanel.add(wrapPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        outputOptionsPanel.add(new JPanel(), gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        outputOptionsPanel.add(timePanel, gridBagConstraints);
	}	
	
	/**
	 * Initializes the toolbox options
	 * @param mediaMarkerCB 
	 */
	private void initToolboxOptionsPanel(){
		//panel
		toolboxOptionsPanel = new JPanel(new GridBagLayout());
		toolboxOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "ExportShoebox.Label.ToolboxOptions")));
		
		//initialize components
		 JLabel toolboxDBTypeLabel = new JLabel(ElanLocale.getString(
	        		"ExportShoebox.Label.ToolboxBDName"));
		 toolboxDBTypeLabel.setToolTipText("e.g. \\_sh v3.0  400 Text");
		 
		 typeRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.Type"));
	     typeRB.setSelected(true);
	     typeRB.addItemListener(this);
	     
	     typField = new JTextField("", 23);
	     
		 typButton = new JButton("...");
		 typButton.addActionListener(this);
	     
	     specRB = new JRadioButton(ElanLocale.getString(
	                "ExportShoebox.Label.SpecifyType"));
	     specRB.addItemListener(this);
	     
         dbTypField = new JTextField("", 14);
         
         JLabel recordMarkerLabel = new JLabel(ElanLocale.getString(
         		"ExportShoebox.Label.RecordMarker"));
         
         detectedRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.Detected"));
         detectedRMRB.setSelected(true);
         detectedRMRB.addItemListener(this);
         
         defaultRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.DefaultMarker") + " (\\block)");
         defaultRMRB.addItemListener(this);
         
         customRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.CustomMarker"));
         customRMRB.addItemListener(this);
         
         markerTF = new JTextField("", 6);
         markerTF.setEnabled(false);
         
         mediaMarkerCB = new JCheckBox(ElanLocale.getString("ExportShoebox.Label.IncludeMediaMarker"));
         mediaMarkerCB.addItemListener(this);
         
         audioRB = new JRadioButton(ElanLocale.getString("MultiFileExportToolbox.useAudioFile"));
  		 audioRB.setSelected(true);
  		 audioRB.addItemListener(this);
  		
  		 videoRB = new JRadioButton(ElanLocale.getString("MultiFileExportToolbox.useVideoFile"));
  		 videoRB.addItemListener(this);
         
         absFilePathRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.AbsoluteMediaFile"));
         absFilePathRB.setSelected(true);
         
         relFilePathRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.RelMediaFile"));
         
         mediaMarkerNameLabel = new JLabel(ElanLocale.getString("ExportShoebox.Label.MediaMarkerName"));
         
         mediaTypeLabel = new JLabel(ElanLocale.getString("MultiFileExportToolbox.SelectMediaType"));
         
         mediaMarkerNameTF = new JTextField("", 6);
         mediaMarkerNameTF.setEnabled(false);   

         ButtonGroup mediaGroup = new ButtonGroup();
  		 mediaGroup.add(videoRB);
  		 mediaGroup.add(audioRB);
  		
         ButtonGroup buttonGroup = new ButtonGroup();
         buttonGroup.add(typeRB);
         buttonGroup.add(specRB);       
        
         ButtonGroup rmGroup = new ButtonGroup();
         rmGroup.add(detectedRMRB);
         rmGroup.add(defaultRMRB);
         rmGroup.add(customRMRB);
         
         ButtonGroup fileGroup = new ButtonGroup();
         fileGroup.add(absFilePathRB);
         fileGroup.add(relFilePathRB);         
          
         GridBagConstraints gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = insets;
         toolboxOptionsPanel.add(toolboxDBTypeLabel, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(typeRB, gridBagConstraints);

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(typField, gridBagConstraints);

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(typButton, gridBagConstraints);

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(specRB, gridBagConstraints);

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(dbTypField, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = innerInsets;
         toolboxOptionsPanel.add(recordMarkerLabel, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(detectedRMRB, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(defaultRMRB, gridBagConstraints);
        
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(customRMRB, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(markerTF, gridBagConstraints);
         
         // add media marker elements
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(mediaMarkerCB, gridBagConstraints);   
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(mediaMarkerNameLabel, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(mediaMarkerNameTF, gridBagConstraints);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(mediaTypeLabel, gridBagConstraints);  
         
         JPanel mediaPanel = new JPanel(new GridLayout(1, 2));
         mediaPanel.add(audioRB);
         mediaPanel.add(videoRB);
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 9;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = innerInsets;
         toolboxOptionsPanel.add(mediaPanel, gridBagConstraints);  	
         
         JPanel fileNamePanel = new JPanel(new GridLayout(1, 2));
         fileNamePanel.add(absFilePathRB);
         fileNamePanel.add(relFilePathRB);         

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = innerInsets;
         toolboxOptionsPanel.add(fileNamePanel, gridBagConstraints);     		  
	}
	
	/**
	 * sets the value in the
	 * numCharTF 
	 */
	private void setDefaultNumOfChars() {
        numCharTF.setEnabled(true);
        numCharTF.setBackground(Constants.SHAREDCOLOR4);

        if ((numCharTF.getText() != null) ||
                (numCharTF.getText().length() == 0)) {
            numCharTF.setText("" + NUM_CHARS);
        }
    }
	
	private void setShoeboxMarkerRB() {
        Object useTyp = Preferences.get("LastUsedShoeboxExport", null);

        if (useTyp == null || (useTyp instanceof String &&
                ((String) useTyp).equalsIgnoreCase("typ"))) {
            typeRB.setSelected(true);

            Object luTypFile = Preferences.get("LastUsedShoeboxTypFile", null);

            if (luTypFile instanceof String) {
                typField.setText((String) luTypFile);
            }
            enableTypComponents(true);
        } else {
        	specRB.setSelected(true);
        	enableTypComponents(false);
        }
    }
	
	 private void enableTypComponents(boolean enable) {  	
	    	typField.setEnabled(enable);
	    	typButton.setEnabled(enable);
	    	dbTypField.setEnabled(!enable);
	    }
	
	private void chooseTyp() {      
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
        		null, FileExtension.SHOEBOX_TYP_EXT, false, "LastUsedShoeboxTypDir", FileChooser.FILES_ONLY, null);
        File f = chooser.getSelectedFile();
        if (f != null) {
            typField.setText(f.getAbsolutePath());
        }
    }

	/**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == wrapBlocksCB) {
            if (wrapBlocksCB.isSelected()) {
                setDefaultNumOfChars();
                numCharTF.requestFocus();
                wrapLinesCB.setEnabled(true);
            	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
            	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                wrapLinesCB.setEnabled(false);
            	wrapNextLineRB.setEnabled(false);
            	wrapAfterBlockRB.setEnabled(false);
            }
        } else if (ie.getSource() == wrapLinesCB) {
        	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
        	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
        } 
        else if (ie.getSource() == typeRB) {
        	enableTypComponents(true);
        } else if (ie.getSource() == specRB) {
        	enableTypComponents(false);
        	dbTypField.requestFocus();
        } else if (ie.getSource() == detectedRMRB || ie.getSource() == defaultRMRB) {
        	markerTF.setEnabled(false);
        } else if (ie.getSource() == customRMRB) {
        	markerTF.setEnabled(true);
        	markerTF.requestFocus();
        } else if (ie.getSource() == mediaMarkerCB) {
        	mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
        	absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        	relFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        	audioRB.setEnabled(mediaMarkerCB.isSelected());
    		videoRB.setEnabled(mediaMarkerCB.isSelected());
    		mediaTypeLabel.setEnabled(mediaMarkerCB.isSelected());  
         	mediaMarkerNameLabel.setEnabled(mediaMarkerCB.isSelected());  
        }
        
        updateButtonStates();
    }
    
    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == typButton) {
            chooseTyp();
            typeRB.setSelected(true);
        } 
    }
    
    private void loadPreferences(){      	
    	Object useTyp = Preferences.get("ExportToolbox.WrapBlocks", null);
    	if(useTyp != null){
    		wrapBlocksCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportShoebox.numCharTF", null);
    	if(useTyp != null){
    		numCharTF.setText((String)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportShoebox.wrapLinesCB", null);
    	if(useTyp != null){
    		wrapLinesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.wrapNextLineRB", null);
    	if(useTyp != null){
    		wrapNextLineRB.setSelected((Boolean)useTyp);
    		wrapAfterBlockRB.setSelected(!(Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.includeEmptyLinesCB", null);
    	if(useTyp != null){
    		includeEmptyLinesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.ssMSFormatRB", null);
    	if(useTyp != null){
    		ssMSFormatRB.setSelected((Boolean)useTyp);
    		hhMMSSMSFormatRB.setSelected(!(Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.correctTimesCB", null);
    	if(useTyp != null){
    		correctTimesCB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.detectedRMRB", null);
    	if(useTyp != null){
    		detectedRMRB.setSelected((Boolean)useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.defaultRMRB", null);
    	if(useTyp != null){
    		defaultRMRB.setSelected((Boolean)useTyp);
    	}
    	
    	if(defaultRMRB.isSelected() || detectedRMRB.isSelected()){
    		customRMRB.setSelected(false);
    	}else {
    		customRMRB.setSelected(true);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.markerTF", null);
    	if(useTyp != null){
    		markerTF.setText((String)useTyp);
    	}    	
    	
    	useTyp = Preferences.get("ExportToolbox.ManualDBName", null);
    	if (useTyp != null) {
    		dbTypField.setText((String) useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.exportMediaMarker", null);
    	if(useTyp != null){
    		mediaMarkerCB.setSelected((Boolean)useTyp);// will this fire an event?
    		mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
    		absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
    		relFilePathRB.setEnabled(mediaMarkerCB.isSelected());   
    		audioRB.setEnabled(mediaMarkerCB.isSelected());
         	videoRB.setEnabled(mediaMarkerCB.isSelected());  
         	mediaTypeLabel.setEnabled(mediaMarkerCB.isSelected());  
         	mediaMarkerNameLabel.setEnabled(mediaMarkerCB.isSelected());  
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.mediaMarkerName", null);
    	if(useTyp != null){
    		mediaMarkerNameTF.setText((String) useTyp);
    	}
    	
    	useTyp = Preferences.get("ExportToolbox.absoluteMediaFileName", null);
    	if(useTyp != null){
    		absFilePathRB.setSelected((Boolean) useTyp);
    		relFilePathRB.setSelected(!((Boolean) useTyp));
    	}
    	
    	Object val = Preferences.get("ExportToolbox.useAudioFile", null);
 		if(val != null && val instanceof Boolean){ 			
 			videoRB.setSelected((Boolean) useTyp);
 			videoRB.setSelected(!((Boolean) useTyp)); 			
 		} 		
    }
    
    private void savePreferences(){
    	Preferences.set("ExportToolbox.WrapBlocks", wrapBlocksCB.isSelected(), null);
    	Preferences.set("ExportToolbox.CharacterPerBlocks", numCharTF.getText(), null);
    	Preferences.set("ExportToolbox.wrapLinesCB", wrapLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.wrapNextLineRB", wrapNextLineRB.isSelected(), null);
    	Preferences.set("ExportToolbox.includeEmptyLinesCB", includeEmptyLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.ssMSFormatRB", ssMSFormatRB.isSelected(), null);
    	Preferences.set("ExportToolbox.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.detectedRMRB", detectedRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.defaultRMRB", defaultRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.markerTF", markerTF.getText(), null); 
    	
   
    	if (specRB.isSelected()) {
    		Preferences.set("ExportToolbox.ManualDBName", dbTypField.getText(), null);
    	}
    	Preferences.set("ExportToolbox.exportMediaMarker", mediaMarkerCB.isSelected(), null);
    	
    	if (mediaMarkerCB.isSelected()) {
    		Preferences.set("ExportToolbox.mediaMarkerName", mediaMarkerNameTF.getText(), null);    		
    		Preferences.set("ExportToolbox.absoluteMediaFileName", absFilePathRB.isSelected(), null);  
    		if(audioRB != null){
    			Preferences.set("ExportToolbox.useAudioFile", audioRB.isSelected(), null);
    		}    		
    	}
    	
    	if (typeRB.isSelected()) {
            Preferences.set("LastUsedShoeboxExport", "typ", null);
            Preferences.set("LastUsedShoeboxTypFile", typField.getText(), null);
        } else {
            Preferences.set("LastUsedShoeboxExport", "", null);
        }
    }
    
	private class TextFieldHandler implements KeyListener{
		public void keyPressed(KeyEvent e) {}

		public void keyReleased(KeyEvent e) {
			updateButtonStates();
		}

		public void keyTyped(KeyEvent e) {}
		
	}	
}
