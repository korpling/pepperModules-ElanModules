package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.tier.DisplayableContentTableModel;
import mpi.eudico.client.annotator.tier.SelectableContentTableModel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1: File and tier selection
 * 
 * Select multiple files and tiers that are to be exported as 
 * toolbox file
 * 
 * @author aarsom
 * @version Feb, 2012
 */
public class MultipleFileToolBoxExportStep1 extends StepPane implements ItemListener{	
	
	private JTable tierTable;	
	private JCheckBox blankLineCB;
	private JRadioButton selectedFilesFromDiskRB, filesFromDomainRB;
	private JPanel fileSelectionPanel,tierSelectionPanel, buttonPanel;
	private ButtonGroup buttonGroup;
	private JButton selectFilesBtn, selectDomainBtn, selectAllButton, selectNoneButton;	
	private JScrollPane tierTableScrollPane;
	
	private List<String> openedFileList;	
	private List<String> recordMarkerList;	
	private Set<String> tierSet;	
	HashMap<String, String> recordMarkerMap;
	HashMap<String, List<String>> tiersMap;
	
	private final String elanBeginLabel = Constants.ELAN_BEGIN_LABEL;
	private final String elanEndLabel = Constants.ELAN_END_LABEL;
	private final String elanParticipantLabel = Constants.ELAN_PARTICIPANT_LABEL;
	
	private Insets globalInset = new Insets(2, 4, 2, 4);
	
	private boolean bothMediaDetected = false;
	private boolean mediaDetected = false;
	
	/**
	 * Constructor
	 * 
	 * @param multiPane
	 */
	public MultipleFileToolBoxExportStep1(MultiStepPane multiPane) {
		super(multiPane);	
		initComponents();
	}
		
	/**
	 * Initialize the ui components
	 */
	protected void initComponents(){			
		//initialize 
		initFileSelectionPanel();
		initTierSelectionPanel();		
	
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(fileSelectionPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(tierSelectionPanel, gbc);
	}		
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileExportToolbox.Title.Step1Title");
	}				

	/**
	 * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
	 */
	public void enterStepForward(){
		updateButtonStates();
	}
	
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	public boolean leaveStepForward(){
		//retrieve selected tier names
		SelectableContentTableModel model = (SelectableContentTableModel) tierTable.getModel();
		
		Vector tiers = model.getSelectedValues();
		
		List selectedTiers = new ArrayList();
		for(Object t : tiers){
			selectedTiers.add(t.toString());
		}
		
		//retrieve selected tier names	
		multiPane.putStepProperty("SelectedTiers", selectedTiers);		
		multiPane.putStepProperty("OpenedFiles", openedFileList);
		multiPane.putStepProperty("TierListMap", tiersMap);
		multiPane.putStepProperty("RecordMarkersList", recordMarkerList);
		multiPane.putStepProperty("RecordMarkersMap", recordMarkerMap);
		multiPane.putStepProperty("MarkersWithBlankLinesList", getMarkersWithBlankLines());
		multiPane.putStepProperty("BothMediaDetected", bothMediaDetected);	
		multiPane.putStepProperty("EnableMediaMarker", mediaDetected);			
		return true;
	}
	
	private List getMarkersWithBlankLines() {
    	List mbl = new ArrayList();

        // add selected tiers in the right order
        for (int i = 0; i < tierTable.getModel().getRowCount(); i++) {
            SelectableObject sob = (SelectableObject) tierTable.getModel().getValueAt(i, 1);

            if (sob.isSelected()) {
                mbl.add(sob.getValue());
            }
        }
    	return mbl;
    }
    
	/**
	 * Initializes the upper part containing file selection
	 */
	private void initFileSelectionPanel(){			
		//panel
		fileSelectionPanel = new JPanel(new GridBagLayout());
		fileSelectionPanel.setBorder(new TitledBorder( ElanLocale.getString("MultiFileExport.Panel.Title.FileSelection")));
		
		//create all radio buttons
		RadioButtonHandler radioButtonListener = new RadioButtonHandler();
		selectedFilesFromDiskRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromFileBrowser"));
		selectedFilesFromDiskRB.addActionListener(radioButtonListener);
		selectedFilesFromDiskRB.setSelected(true);
		
		filesFromDomainRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromDomain"));;
		filesFromDomainRB.addActionListener(radioButtonListener);
		
		//add radio buttons to button group
		buttonGroup = new ButtonGroup();
		buttonGroup.add(selectedFilesFromDiskRB);
		buttonGroup.add(filesFromDomainRB);
		
		//create all buttons
		ButtonHandler buttonHandler = new ButtonHandler();
		
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(buttonHandler);
		
		selectDomainBtn = new JButton(ElanLocale.getString("FileAndTierSelectionStepPane.Button.Domain"));
		selectDomainBtn.addActionListener(buttonHandler);		
		selectDomainBtn.setEnabled(false);
		
		//add buttons to panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = globalInset;		
		fileSelectionPanel.add(selectedFilesFromDiskRB, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectFilesBtn, gbc);
		
		//files from domain
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		fileSelectionPanel.add(filesFromDomainRB, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectDomainBtn, gbc);
	}
	
	/**
	 * Intializes tier table pane
	 */
	private void initTierSelectionPanel(){
		//panel
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tierSelectionPanel = new JPanel(new GridBagLayout());		
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("MultiFileExport.Panel.Title.TierSelection")));		
		
		//
		blankLineCB = new JCheckBox(ElanLocale.getString("ExportShoebox.Button.BlankLineAfter"));
		blankLineCB.addItemListener(this);
		blankLineCB.setEnabled(false);
		
		//buttons
		selectAllButton = new JButton(ElanLocale.getString("Button.SelectAll"));
		selectNoneButton = new JButton(ElanLocale.getString("Button.SelectNone"));
		
		ButtonHandler buttonHandler = new ButtonHandler();
		selectAllButton.addActionListener(buttonHandler);
		selectNoneButton.addActionListener(buttonHandler);
		
		//create table to show tiers in
		tierTable = new JTable();
		tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierTable.setShowVerticalLines(true);
		
			
		DisplayableContentTableModel model = new DisplayableContentTableModel(ElanLocale.getString("FileAndTierSelectionStepPane.Message1"));
		tierTable.setModel(model);
		tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));
		
		//--- common layout code ----
		//add table to scroll pane
		tierTableScrollPane = new JScrollPane(tierTable);
		tierTableScrollPane.setColumnHeaderView(null);
		
		//add buttons
		buttonPanel.add(selectAllButton);
		buttonPanel.add(selectNoneButton);
		
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInset;
		tierSelectionPanel.add(tierTableScrollPane, gbc);
		
		//add button panel
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		Insets insets = new Insets(0, globalInset.left, 0, globalInset.right);
		gbc.insets = insets;
		tierSelectionPanel.add(buttonPanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;		
		gbc.insets = insets;
		tierSelectionPanel.add(blankLineCB, gbc);
	}
	
	/**
	 * The item state changed handling.
	 *
	 * @param ie the ItemEvent
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == blankLineCB) {
			int row = tierTable.getSelectedRow();
        	if (row > -1) {
        		Object val = tierTable.getValueAt(row, 1);
        		if (val instanceof SelectableObject) {
        			((SelectableObject) val).setSelected(blankLineCB.isSelected());
        			tierTable.repaint();
        		}
        	}
		}
	}

	/**
	 * Shows a multiple file chooser dialog, checks if every selected file exists
	 * and stores the selected files in private variable eafFiles
	 * @return boolean to indicate if file selection went successful
	 */
	private List<String> showMultiFileChooser(){
		List<String> fileNames = null;	
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("ExportDialog.Multi"), FileChooser.GENERIC,
        		FileExtension.EAF_EXT, "LastUsedEAFDir");
        
        Object[] objects = chooser.getSelectedFiles();     

		if (objects != null) {	  
			if (objects.length > 0) {           
				fileNames = new ArrayList<String>();
                for (int i = 0; i < objects.length; i++) {
                    if (fileNames.contains(objects[i]) == false) {
                        fileNames.add("" + objects[i]);
                    }
                }
            }
		}		
		return fileNames;
	}

	/**
	 * Updates the button states according to some constraints (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){
		try{
			SelectableContentTableModel model = (SelectableContentTableModel) tierTable.getModel();
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, model.getSelectedValues().size() > 1);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
		}catch(ClassCastException e){
			//if there is no selection model, then no selection is made
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
	}
	
	 /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    private void addFileNames(File dir, List<String> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
            	addFileNames(allSubs[i], files);
            } else {
                if (allSubs[i].canRead()) {
                    if (allSubs[i].getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                    	
                    	if(!files.contains(allSubs[i].getAbsolutePath()))
                    		files.add(allSubs[i].getAbsolutePath());
                    }
                }
            }
        }
    }

	
	private class ButtonHandler implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			updateButtonStates();
			if( e != null ){
				JButton button = (JButton) e.getSource();
				
				if( button == selectFilesBtn ){
					List<String> filenames = showMultiFileChooser();
					if( filenames != null && !filenames.isEmpty() ){
						Thread t = new OpenFilesThread( filenames, tierTable, new ModelClickedHandler() );
						t.start();
					}
				}else if( button == selectDomainBtn ){
					//create domain dialog
					MFDomainDialog domainDialog = new MFDomainDialog(ELANCommandFactory.getRootFrame(null), ElanLocale.getString("ExportDialog.Multi"), true);
					domainDialog.setVisible(true);
					
					//when domain is selected, get the search paths
					List<String> searchPaths = domainDialog.getSearchPaths();
					List<String> searchDirs = domainDialog.getSearchDirs();
										
					List<String> fileNames = new ArrayList<String>();
					
					//check if domain contains files
					if( !searchPaths.isEmpty() ){
						fileNames.addAll(searchPaths);
						
					}
					
					//check if domain contains files
					if( !searchDirs.isEmpty() ){
						
					    File f;
					    for (int i = 0; i < searchDirs.size(); i++) {
					      	f = new File(searchDirs.get(i));
					       	if (f.isFile() && f.canRead()) {
					       		if(!fileNames.contains(searchDirs.get(i))){
					       			fileNames.add(searchDirs.get(i));	
					       		}
					       	} else if (f.isDirectory() && f.canRead()) {
					       		addFileNames(f, fileNames);
					       	}
					    }	
					}
					
					if(fileNames.size() >0){
						Thread t = new OpenFilesThread(fileNames, tierTable, new ModelClickedHandler());
						t.start();
					}
				}else if( button == selectAllButton ){
					try{
						SelectableContentTableModel model = (SelectableContentTableModel)tierTable.getModel();
						model.selectAll();
						updateButtonStates();
					}catch(ClassCastException exception){
						//do nothing
					}
				}else if( button == selectNoneButton ){
					try{
						SelectableContentTableModel model = (SelectableContentTableModel)tierTable.getModel();
						model.selectNone();
						updateButtonStates();
					}catch(ClassCastException exception){
						//do nothing
					}
				}
			}				
		}		
	}
	
	private class ModelClickedHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			updateButtonStates();
		}
	}
	
	private class RadioButtonHandler implements ActionListener{
		private JRadioButton previouslySelectedRadioButton;
		
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			
			if( rb == selectedFilesFromDiskRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}
				
				DisplayableContentTableModel model = new DisplayableContentTableModel(ElanLocale.getString("FileAndTierSelectionStepPane.Message1"));
				tierTable.setModel(model);
				tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));
				
				selectFilesBtn.setEnabled(true);
				selectDomainBtn.setEnabled(false);
			}else if( rb == filesFromDomainRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}
				
				DisplayableContentTableModel model = new DisplayableContentTableModel(ElanLocale.getString("FileAndTierSelectionStepPane.Message3"));
				tierTable.setModel(model);
				tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));
				
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(true);
			}
			
			updateButtonStates();
		}
	}
	
	protected class OpenFilesThread extends Thread implements ListSelectionListener{
		private List<String> filenames;
		private JTable tierTable;
		private DisplayableContentTableModel model;
		private ActionListener listener;
			
		
		public OpenFilesThread( List<String> filenames, JTable tierTable, ActionListener listener ){
			this.filenames = filenames;
			this.tierTable = tierTable;
			this.listener = listener;
			model = new DisplayableContentTableModel( new String[]{ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part1"), "0 " + 
					ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part2") + " " + filenames.size() + " " + 
					ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part3") + " (0%)"} );
			model.connectTable(this.tierTable);
			this.tierTable.setModel(model);
		}
		
		@Override
		public void run(){
			tierSet	 = new TreeSet<String>();
			Set<SelectableObject> selectableObjectSet = new TreeSet<SelectableObject>(new SelectableObjectComparator());
			
			openedFileList = new ArrayList<String>();
			recordMarkerList = new ArrayList<String>();		
			recordMarkerMap = new HashMap<String, String>();
			tiersMap = new HashMap<String, List<String>>();
			
			FrameManager manager = FrameManager.getInstance();
			ElanFrame2 frame;
			TranscriptionImpl transImpl;
			List<String> markersInthisFile;
			
			
			for( int i=0; i<filenames.size(); i++ ){
				//open file and store in list with transcription implementations
				transImpl =null;
				frame = manager.getFrameFor(filenames.get(i), false);
				String message = null;
				if(frame != null){
					message = "\' " + filenames.get(i) + ElanLocale.getString("FileAndTierSelectionStepPane.Message3.Part1");
					if(frame.getViewerManager().getTranscription().isChanged()){
						message += "\\n" + ElanLocale.getString("FileAndTierSelectionStepPane.Message3.Part2");
					}
					message += "\\n" + ElanLocale.getString("FileAndTierSelectionStepPane.Message3.Part3");
					JOptionPane.showMessageDialog(MultipleFileToolBoxExportStep1.this, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE, null);
				}
				
				if(transImpl == null){				
					transImpl = new TranscriptionImpl( filenames.get(i) );
				}
				openedFileList.add(filenames.get(i));
				
				checkForMediaFiles(transImpl);
				
				//get tiers
				Vector<TierImpl> tiersInFile = transImpl.getTiers();
				markersInthisFile = new ArrayList<String>();
				
				//store tiers
				ArrayList rootTiers = new ArrayList(5);
                String tName;
                String markName;
				for( TierImpl t : tiersInFile ){
                    tName = t.getName();
                    int atIndex = tName.indexOf('@');
                    if (atIndex > -1) {
                    	markName = tName.substring(0, atIndex);
                    	if (!tierSet.contains(markName)) {
                    		if(tierSet.size() == 1){				
                    			tierSet.add(elanBeginLabel);  
            					selectableObjectSet.add(new SelectableObject(elanBeginLabel, false));
            					tierSet.add(elanEndLabel);
            					selectableObjectSet.add(new SelectableObject(elanEndLabel, false));
            					tierSet.add(elanParticipantLabel);
            					selectableObjectSet.add(new SelectableObject(elanParticipantLabel, false));  
                    		}                     		
                    		tierSet.add(markName);
                    		selectableObjectSet.add(new SelectableObject(markName, false));
                    	}
                    	if(!markersInthisFile.contains(markName)){
                    		markersInthisFile.add(markName);
                    	}
                    	if (!t.hasParentTier()) {
                    		if (!rootTiers.contains(markName)) {
                    			rootTiers.add(markName);
                    		}
                    	}
                    } else {
                    	if(tierSet.size() == 1){				
                			tierSet.add(elanBeginLabel);  
        					selectableObjectSet.add(new SelectableObject(elanBeginLabel, false));
        					tierSet.add(elanEndLabel);
        					selectableObjectSet.add(new SelectableObject(elanEndLabel, false));
        					tierSet.add(elanParticipantLabel);
        					selectableObjectSet.add(new SelectableObject(elanParticipantLabel, false));  
                		}      
                    	selectableObjectSet.add(new SelectableObject(tName, false));
                    	tierSet.add(tName);
                    	markersInthisFile.add(tName);                    	
                    	if (!t.hasParentTier()) {
                    		rootTiers.add(tName);
                    	}
                    }
                }
				
				tiersMap.put(filenames.get(i), markersInthisFile);
				
				if (rootTiers.size() == 1) {
                	String recordMarker = (String) rootTiers.get(0);                 	
                	recordMarkerMap.put(filenames.get(i), recordMarker);
                	if(!recordMarkerList.contains(recordMarker)){
                		recordMarkerList.add(recordMarker);
                	}
				}
				model.updateMessage(1, (i+1) + " " + ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part2") + " " + filenames.size() + " " + 
						ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part3") + " (" + Math.round((i+1)/((float)filenames.size())*100.0f) + "%)");
			}
			
			//update table
			if( tierSet.isEmpty() ){
				//if there are no tiers to be loaded (all files do not contain tiers)
				openedFileList.clear();
				DisplayableContentTableModel model = (DisplayableContentTableModel) tierTable.getModel();
				//tierTable.setModel(dataModel)
				
				model.setValueAt( ElanLocale.getString("FileAndTierSelectionStepPane.Message3"), 0, 0);
			}else{				
				if(tierSet.size() == 1){				
        			tierSet.add(elanBeginLabel);  
					selectableObjectSet.add(new SelectableObject(elanBeginLabel, true));
					tierSet.add(elanEndLabel);
					selectableObjectSet.add(new SelectableObject(elanEndLabel, true));
					tierSet.add(elanParticipantLabel);
					selectableObjectSet.add(new SelectableObject(elanParticipantLabel, true));  
        		}      
				final SelectableContentTableModel model = new SelectableContentTableModel(selectableObjectSet, tierTable);
				model.addActionListener(listener);
				model.selectAll();
				tierTable.setModel(model);
				tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		        tierTable.getSelectionModel().addListSelectionListener(this);
				
				tierTable.getColumnModel().getColumn(0).setHeaderValue(null);
				tierTable.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
				tierTable.getColumnModel().getColumn(0).setMaxWidth(30);				
				tierTable.getColumnModel().getColumn(1).setCellRenderer(new MarkerCellRenderer());					
				tierTable.repaint();				
				blankLineCB.setEnabled(true);
				updateButtonStates();
			}
		}
		
	    private void checkForMediaFiles(TranscriptionImpl transcription) {
	    	if(bothMediaDetected){
	    		return;
	    	}
	    	List mds = transcription.getMediaDescriptors();
	    	boolean video = false;
	    	boolean audio = false;
	    	if (mds != null && mds.size() > 0) {
	    		MediaDescriptor md = null;	    		
	    		for (int i = 0; i < mds.size(); i++) {
	    			
	    			if(video && audio){
	    				mediaDetected = true;
	    				bothMediaDetected = true;
	    				break;
	    			}
	    			md = (MediaDescriptor) mds.get(i);	    			
	    			if(MediaDescriptorUtil.isVideoType(md)){
	    				video = true;
	    				mediaDetected = true;
	    				continue;
	    			} else if(md.mimeType!= null){
	    				if(md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) || 
	    						md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)){
	    					audio = true;
	    					mediaDetected = true;
		    				continue;
	    				}
	    			}
	    		}
	    	}
	    	
	    	if(video && audio){
	    		mediaDetected = true;
				bothMediaDetected = true;				
			}
    	} 	    

		/**
	     * Updates the checked state of the export checkboxes.
	     *
	     * @param lse the list selection event
	     */
	    public void valueChanged(ListSelectionEvent lse) {
	        if ((model != null) && lse.getValueIsAdjusting()) {
	            int col = 0;
	            int row  = tierTable.getSelectedRow();
	            
	            if (row > -1) {
	                if (tierTable.isRowSelected(row)) {
	                	tierTable.setValueAt(Boolean.TRUE, row, col);
	                }
	            	
		            Object val = tierTable.getValueAt(row, 1);
		            if (val instanceof SelectableObject) {
		            	blankLineCB.setSelected(((SelectableObject) val).isSelected());
		            }
	            }
	        }
	    }

	}
	
	class SelectableObjectComparator implements Comparator<SelectableObject>{
		public  int compare(SelectableObject o1, SelectableObject o2){
			if(o1.getValue().equals(o2.getValue())){
				return 0;
			}
			return 1;
			
		}
	}
	
	/**
     * Renderer class that uses a different foreground color for selected objects.
     * @author Han Sloetjes
     */
    class MarkerCellRenderer extends DefaultTableCellRenderer {

    	/**
		 * Highlight the markers that should be followed by a whit line.
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			if (value instanceof SelectableObject) {
				if (((SelectableObject) value).isSelected()) {
					c.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
				} else {
					if (!isSelected) {
						c.setForeground(table.getForeground());
					}					
				}
			}
			return c;
		}
    	
    }
}