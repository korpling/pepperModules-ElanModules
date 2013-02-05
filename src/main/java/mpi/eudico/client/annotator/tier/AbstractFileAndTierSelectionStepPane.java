package mpi.eudico.client.annotator.tier;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;


import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * Abstract Step pane for selecting multiple tiers from single or multiple files
 * 
 * Does not use the advance tierSelectionPanel
 * 
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2010
 */
public abstract class AbstractFileAndTierSelectionStepPane extends StepPane{	
	
	private TranscriptionImpl transcription;
	private JTable tierTable;	
	private Set<String> tierSet;	
	
	private JRadioButton currentlyOpenedFileRB, selectedFilesFromDiskRB, filesFromDomainRB;
	private JPanel fileSelectionPanel,tierSelectionPanel, buttonPanel;
	private ButtonGroup buttonGroup;
	private JButton selectFilesBtn, selectDomainBtn, selectAllButton, selectNoneButton;	
	private JScrollPane tierTableScrollPane;
	
	private List<String> openedFileList;	
	private List<String> rootTierTypeNamesList;
	private List<String> childTierTypeNamesList;
	//used only in multiple file mode
	private List<String> completeTypeNamesList;
	
	private Insets globalInset;

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcripton
	 */
	public AbstractFileAndTierSelectionStepPane(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp);
		this.transcription = transcription;		
		globalInset = new Insets(5, 10, 5, 10);
		
		//create set of tier names
		if(transcription != null){
			tierSet = new TreeSet<String>();
			Vector<TierImpl> tiers = transcription.getTiers();
			for( TierImpl tier : tiers )
				tierSet.add(tier.getName());
		}
				
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
	public abstract String getStepTitle();

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
		
		//retrieve selected tier names	
		multiPane.putStepProperty("SelectedTiers", model.getSelectedValues());		
		multiPane.putStepProperty("OpenedFiles", openedFileList);
		multiPane.putStepProperty("RootTierTypes", rootTierTypeNamesList);
		multiPane.putStepProperty("ChildTierTypes", childTierTypeNamesList);
		multiPane.putStepProperty("CompleteTierTypes", completeTypeNamesList);		
		multiPane.putStepProperty("AllTiers", tierSet);		
		
		int transcriptionMode;
		if( currentlyOpenedFileRB.isSelected() )
			transcriptionMode = 0;
		else
			transcriptionMode = 1;
		
		multiPane.putStepProperty("TranscriptionMode", transcriptionMode);
		return true;
	}
    
	/**
	 * Initializes the upper part containing file selection
	 */
	private void initFileSelectionPanel(){			
		//panel
		fileSelectionPanel = new JPanel(new GridBagLayout());
		fileSelectionPanel.setBorder(new TitledBorder( ElanLocale.getString("FileAndTierSelectionStepPane.Panel.Title.FileSelection")));
		
		//create all radio buttons
		RadioButtonHandler radioButtonListener = new RadioButtonHandler();
		currentlyOpenedFileRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.CurrentlyOpenedFile"));
		currentlyOpenedFileRB.addActionListener(radioButtonListener);
		selectedFilesFromDiskRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromFileBrowser"));
		selectedFilesFromDiskRB.addActionListener(radioButtonListener);
		filesFromDomainRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromDomain"));;
		filesFromDomainRB.addActionListener(radioButtonListener);
		
		//add radio buttons to button group
		buttonGroup = new ButtonGroup();
		buttonGroup.add(currentlyOpenedFileRB);
		buttonGroup.add(selectedFilesFromDiskRB);
		buttonGroup.add(filesFromDomainRB);
		
		//create all buttons
		ButtonHandler buttonHandler = new ButtonHandler();
		
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(buttonHandler);
		
		selectDomainBtn = new JButton(ElanLocale.getString("FileAndTierSelectionStepPane.Button.Domain"));
		selectDomainBtn.addActionListener(buttonHandler);
		selectDomainBtn.setEnabled(false);
		
		//handle multiple file case vs. single file case
		if( transcription == null ){
			//MULTIPLE_FILES: disable all radio buttons dealing with single file
			currentlyOpenedFileRB.setEnabled(false);
			selectedFilesFromDiskRB.setEnabled(true);
			selectedFilesFromDiskRB.setSelected(true);
			selectFilesBtn.setEnabled(true);
		}else{
			//SINGLE FILES: disable all multiple file functionality
			selectedFilesFromDiskRB.setEnabled(false);
			filesFromDomainRB.setEnabled(false);
			selectFilesBtn.setEnabled(false);
			selectDomainBtn.setEnabled(false);
			
			currentlyOpenedFileRB.setSelected(true);
		}
		
		//add buttons to panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 30, 0, 10);			
		fileSelectionPanel.add(currentlyOpenedFileRB, gbc);
		
		//files from disk
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		fileSelectionPanel.add(selectedFilesFromDiskRB, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectFilesBtn, gbc);
		
		//files from domain
		gbc.gridx = 0;
		gbc.gridy = 2;
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
		globalInset = new Insets(5, 10, 5, 10);
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("FileAndTierSelectionStepPane.Panel.Title.TierSelection")));		
		
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
		
		//first menu depends on multiple files or single files
		if( transcription != null ){
			Object[] tierArray = tierSet.toArray();
			String[] stereotypeArray = new String[tierArray.length];
			String[] linguisticArray = new String[tierArray.length];
			
			for( int i=0; i<tierArray.length; i++ ){
				TierImpl tier = (TierImpl)transcription.getTierWithId( (String)tierArray[i] );
				linguisticArray[i] = tier.getLinguisticType().getLinguisticTypeName();
				stereotypeArray[i] = "";
				
				if( tier.getLinguisticType().hasConstraints() )
					stereotypeArray[i] = Constraint.stereoTypes[tier.getLinguisticType().getConstraints().getStereoType()];
			}
			
			SelectableContentTableModel model = new SelectableContentTableModel(tierTable, tierArray, linguisticArray, stereotypeArray );
			model.addActionListener(new ModelClickedHandler());
			
			tierTable.setModel(model);
			tierTable.getColumnModel().getColumn(0).setHeaderValue(null);
			tierTable.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
			tierTable.getColumnModel().getColumn(2).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.LinguisticType") );
			tierTable.getColumnModel().getColumn(3).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.Stereotype") );
			tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
		}else{
			DisplayableContentTableModel model = new DisplayableContentTableModel(ElanLocale.getString("FileAndTierSelectionStepPane.Message1"));
			tierTable.setModel(model);
			tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));
			
			selectFilesBtn.setEnabled(true);
			selectDomainBtn.setEnabled(false);
		}
		
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
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInset;
		tierSelectionPanel.add(tierTableScrollPane, gbc);
		
		//add button panel
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		Insets insets = new Insets(0, globalInset.left, 0, globalInset.right);
		gbc.insets = insets;
		tierSelectionPanel.add(buttonPanel, gbc);
	}
	
	/**
	 * Shows a multiple file chooser dialog, checks if every selected file exists
	 * and stores the selected files in private variable eafFiles
	 * @return boolean to indicate if file selection went successful
	 */
	private List<String> showMultiFileChooser(){
		List<String> fileNames = null;	
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.GENERIC,
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
    protected void addFileNames(File dir, List<String> files) {
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
					MFDomainDialog domainDialog = new MFDomainDialog(ELANCommandFactory.getRootFrame(transcription), true);
					domainDialog.setVisible(true);
					
					//when domain is selected, get the search paths
					List<String> searchPaths = domainDialog.getSearchPaths();
					List<String> searchDirs = domainDialog.getSearchDirs();
					
					File f;
				    for (int i = 0; i < searchDirs.size(); i++) {
				    	String fileName = searchDirs.get(i);
				        	f = new File(fileName);
				        	if (f.isFile() && f.canRead() && !searchPaths.contains(fileName)) {
				        		searchPaths.add(fileName);
				        	} else if (f.isDirectory() && f.canRead()) {
				        		addFileNames(f, searchPaths);
				        	}
				        }					
					
					//check if domain contains files
					if( !searchPaths.isEmpty() ){
						//load the files in the selected domain
						Thread t = new OpenFilesThread(searchPaths, tierTable, new ModelClickedHandler());
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
			
			if( rb == currentlyOpenedFileRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}
				
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(false);
				
				Object[] tierArray = tierSet.toArray();
				String[] stereotypeArray = new String[tierArray.length];
				String[] linguisticArray = new String[tierArray.length];
				
				for( int i=0; i<tierArray.length; i++ ){
					TierImpl tier = (TierImpl)transcription.getTierWithId( (String)tierArray[i] );
					linguisticArray[i] = tier.getLinguisticType().getLinguisticTypeName();
					stereotypeArray[i] = "";
					
					if( tier.getLinguisticType().hasConstraints() )
						stereotypeArray[i] = Constraint.stereoTypes[tier.getLinguisticType().getConstraints().getStereoType()];
				}
				
				SelectableContentTableModel model = new SelectableContentTableModel(tierTable, tierArray, linguisticArray, stereotypeArray );;
				model.addActionListener(new ModelClickedHandler());
				tierTable.setModel(model);
				tierTable.getColumnModel().getColumn(0).setHeaderValue(null);
				tierTable.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
				tierTable.getColumnModel().getColumn(2).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.LinguisticType") );
				tierTable.getColumnModel().getColumn(3).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.Stereotype") );
				tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
				
			}else if( rb == selectedFilesFromDiskRB ){
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
	
	private class OpenFilesThread extends Thread{
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
			//Set<String> uniqueTierNames = new TreeSet<String>();
			tierSet = new TreeSet<String>();
			openedFileList = new ArrayList<String>();
			rootTierTypeNamesList = new ArrayList<String>();
			childTierTypeNamesList = new ArrayList<String>();	
			completeTypeNamesList = new ArrayList<String>();

			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			
			FrameManager manager = FrameManager.getInstance();
			ElanFrame2 frame;
			TranscriptionImpl transImpl;
			
			
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
					JOptionPane.showMessageDialog(AbstractFileAndTierSelectionStepPane.this, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE, null);
				}
				
				if(transImpl == null){				
					transImpl = new TranscriptionImpl( filenames.get(i) );
				}
				openedFileList.add(filenames.get(i));
				
				//get tiers
				Vector<TierImpl> tiersInFile = transImpl.getTiers();
				
				//store tiers
				for( TierImpl tier : tiersInFile ){
					tierSet.add(tier.getName());
				}
				model.updateMessage(1, (i+1) + " " + ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part2") + " " + filenames.size() + " " + 
						ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part3") + " (" + Math.round((i+1)/((float)filenames.size())*100.0f) + "%)");
			
				// get Types
				Vector<LinguisticType> typesInFile = transImpl.getLinguisticTypes();
				
				//store types
				for( LinguisticType type : typesInFile ){
					if(type.getConstraints() == null){
						if(!rootTierTypeNamesList.contains(type.getLinguisticTypeName())){
							rootTierTypeNamesList.add(type.getLinguisticTypeName());
						}
					}else if(type.getConstraints().getStereoType() == Constraint.INCLUDED_IN){
						//don't add linguistic types that are using controlled vocabulary
						if( type.isUsingControlledVocabulary() )
							continue;
						
						if(!childTierTypeNamesList.contains(type.getLinguisticTypeName())){
							childTierTypeNamesList.add(type.getLinguisticTypeName());
						}
					}
					
					if(!completeTypeNamesList.contains(type.getLinguisticTypeName())){
						completeTypeNamesList.add(type.getLinguisticTypeName());
					}
				}			
			}
			
			//update table
			if( tierSet.isEmpty() ){
				//if there are no tiers to be loaded (all files do not contain tiers)
				openedFileList.clear();
				DisplayableContentTableModel model = (DisplayableContentTableModel) tierTable.getModel();
				//tierTable.setModel(dataModel)
				
				model.setValueAt( ElanLocale.getString("FileAndTierSelectionStepPane.Message3"), 0, 0);
			}else{
				SelectableContentTableModel model = new SelectableContentTableModel(tierSet, tierTable);
				model.addActionListener(listener);
				tierTable.setModel(model);
				tierTable.getColumnModel().getColumn(0).setHeaderValue(null);
				tierTable.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
				tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
				tierTable.repaint();
			}
		}
	}
}
