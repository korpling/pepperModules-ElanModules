package mpi.eudico.client.annotator.tier;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * StepPane to select/specify the values of the annotations on the new tier
 * 
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011 
 */
public abstract class AbstractDestTierAnnValueSpecStepPane extends StepPane implements ActionListener, KeyListener, ListSelectionListener{
	protected JRadioButton durationRadioButton, specificValueRB, tierValueRadioButton, smpteRB, palRB, ntscRB;
	protected JRadioButton msecRB, secRB, hrRB;	
	protected JRadioButton concatValuesRB, sortByTimeRB, sortBySelectionRB;
	protected JTextField specificValueTF;
	protected JPanel tierValuePanel, buttonPanel;
	protected JScrollPane tierTableScrollPane;
	protected JButton moveUpButton, moveDownButton;	
	protected ButtonGroup valueKindRadioGroup, concatValueRadioGroup, timeRadioGroup, smpteRadioGroup;
	
	protected JComboBox tierSelectBox;
	protected JTable sortTierTable;
	protected DefaultTableModel model;	
	
	public static final int ANN_VALUE_TIME_FORMAT = 0;
	public static final int ANN_VALUE_SPECIFIC_VALUE = 1;
	public static final int ANN_VALUE_FROM_TIER = 2;
	public static final int ANN_VALUE_CONCAT_BASED_ON_TIME = 3;
	public static final int ANN_VALUE_CONCAT_BASED_ON_TIERORDER = 4;
	
	/**
	 * Constructor
	 * 
	 * @param mp
	 */
	public AbstractDestTierAnnValueSpecStepPane(MultiStepPane mp){
		super(mp);
		initComponents();
	}

	/**
	 * Initializes the ui components
	 */
	protected void initComponents(){	
		Insets globalInset = new Insets(5, 10, 5, 10);
		Insets singleTabInset = new Insets(0, 30, 0, 10);
		Insets doubleTabInset = new Insets(0, 50, 0, 10);
		Insets firstSingleTabInset = new Insets(5, 10, 0, 10);
		
		//create radio buttons for annotation value
		durationRadioButton = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.DurationValue"), true);
		specificValueRB = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.SpecificValue"));
		tierValueRadioButton = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.SpecificTier"));	
		concatValuesRB = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.ConcatValues"));
		
		durationRadioButton.addActionListener(this);
		specificValueRB.addActionListener(this);	
		tierValueRadioButton.addActionListener(this);
		concatValuesRB.addActionListener(this);
		
		valueKindRadioGroup = new ButtonGroup();
		valueKindRadioGroup.add(durationRadioButton);
		valueKindRadioGroup.add(tierValueRadioButton);
		valueKindRadioGroup.add(specificValueRB);
		valueKindRadioGroup.add(concatValuesRB);	
				
		//radio buttons for the time formats
		msecRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.MilliSec"), true);
		secRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.Seconds"));
		hrRB = new JRadioButton( ElanLocale.getString("TimeCodeFormat.TimeCode") );
		smpteRB = new JRadioButton( ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE") );
		palRB = new JRadioButton( ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL"), true);
		ntscRB = new JRadioButton( ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC"));
		
		msecRB.addActionListener(this);
		secRB.addActionListener(this);
		hrRB.addActionListener(this);
		smpteRB.addActionListener(this);
		
		palRB.setEnabled(false);
		ntscRB.setEnabled(false);
		
		timeRadioGroup = new ButtonGroup();
		timeRadioGroup.add(msecRB);
		timeRadioGroup.add(secRB);
		timeRadioGroup.add(hrRB);
		timeRadioGroup.add(smpteRB);
		
		smpteRadioGroup = new ButtonGroup();
		smpteRadioGroup.add(palRB);
		smpteRadioGroup.add(ntscRB);
		
		//create radio buttons for concatinating annotation value		
		sortByTimeRB = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.ConcatValues.SortByAnnotationTime"), true );
		sortBySelectionRB = new JRadioButton( ElanLocale.getString("DestTierAnnValue.Radio.ConcatValues.SortByTierOrderSelection") );
				
		sortByTimeRB.setEnabled(false);		
		sortBySelectionRB.setEnabled(false);
				
		sortBySelectionRB.addActionListener(this);
		sortByTimeRB.addActionListener(this);
				
		concatValueRadioGroup = new ButtonGroup();
		concatValueRadioGroup.add(sortByTimeRB);
		concatValueRadioGroup.add(sortBySelectionRB);

		//create text field
		specificValueTF = new JTextField();
		specificValueTF.setEnabled(false);
		specificValueTF.addKeyListener(this);
		
		//create combobox
		tierSelectBox = new JComboBox();
		tierSelectBox.setEnabled(false);
		
		//create table
		model = new DefaultTableModel();			
		sortTierTable = new JTable(model );
		model.addColumn(ElanLocale.getString("DestTierAnnValue.Column.SelectedTiers"));
		sortTierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
		sortTierTable.getSelectionModel().addListSelectionListener(this);				
		
		//add table to scroll pane
		tierTableScrollPane = new JScrollPane(sortTierTable);
		tierTableScrollPane.setSize(200,100);
		tierTableScrollPane.setEnabled(false);
		
		//buttons		
		moveUpButton = new JButton();
		moveDownButton = new JButton();
		
		
		moveUpButton.addActionListener(this);
		moveDownButton.addActionListener(this);
		
		moveUpButton.setEnabled(false);
		moveDownButton.setEnabled(false);
		
		try {
		   	ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
		    ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
		    moveUpButton.setIcon(upIcon);
		    moveDownButton.setIcon(downIcon);
		} catch (Exception ex) {
		  	moveUpButton.setText("Up");
		   	moveDownButton.setText("Down");
		}
		
		buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		buttonPanel.add(moveUpButton, gbc);

		gbc.gridx = 1;		   
		buttonPanel.add(moveDownButton, gbc);
		
		//layout components
		tierValuePanel = new JPanel(new GridBagLayout());
		tierValuePanel.setBorder(new TitledBorder(ElanLocale.getString("DestTierAnnValue.Panel.Title.TierValue")));
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = firstSingleTabInset;
		tierValuePanel.add(durationRadioButton, gbc);
		
		gbc.insets = singleTabInset;
		gbc.gridy = 1;
		tierValuePanel.add(msecRB, gbc);
		
		gbc.gridy = 2;
		tierValuePanel.add(secRB, gbc);
		
		gbc.gridy = 3;
		tierValuePanel.add(hrRB, gbc);
		
		gbc.gridy = 4;
		tierValuePanel.add(smpteRB, gbc);
		
		gbc.gridy = 5;
		gbc.insets = doubleTabInset;
		tierValuePanel.add(palRB, gbc);
		
		gbc.gridy = 6;
		tierValuePanel.add(ntscRB, gbc);
		
		gbc.gridy = 7;
		gbc.insets = globalInset;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		tierValuePanel.add(specificValueRB, gbc);			
		
		gbc.gridx = 1;
		gbc.weightx = 1.0;			
		tierValuePanel.add(specificValueTF, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 8;			
		tierValuePanel.add(tierValueRadioButton, gbc);
		
		gbc.gridx = 1;
		tierValuePanel.add(tierSelectBox, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy = 9;		
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		tierValuePanel.add(concatValuesRB, gbc);
		
		gbc.gridy = 10;
		gbc.insets = singleTabInset;			
		//gbc.weightx = 1.0;
		tierValuePanel.add(sortByTimeRB, gbc);	
		
		gbc.gridy = 11;
		gbc.insets = singleTabInset;			
		tierValuePanel.add(sortBySelectionRB, gbc);	
		
		gbc.gridx = 0;
		gbc.gridy = 12;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(10, 50, 0, 10);		
		tierValuePanel.add(tierTableScrollPane, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 13;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 50, 0, 10);	
		tierValuePanel.add(buttonPanel, gbc);	
		
		//add panels to screen
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = globalInset;			
		add(tierValuePanel, gbc);	
	}
	
	public abstract String getStepTitle();
	
	/**     
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	public void enterStepForward(){		
		//Updates the combobox with tier names that are selected in the table in step 1
		Vector tierList = (Vector) multiPane.getStepProperty("SelectedTiers");
		tierSelectBox.removeAllItems();		
		//add tier names that are selected in tier table in step 1
		for( int i=0; i<tierList.size(); i++ )
			tierSelectBox.addItem( tierList.get(i).toString() );
		
		//updates the selectional model table		
		while(this.model.getRowCount() > 0){
			this.model.removeRow(0);
		}
		
		if(!sortBySelectionRB.isSelected()){
			sortTierTable.setForeground(Color.LIGHT_GRAY);
			sortTierTable.setEnabled(false);
		}
		
		for( int i=0; i< tierList.size(); i++ ){				
			this.model.addRow( new String[] {(String) tierList.get(i)});			
		}
		updateButtonStates();
	}	
	
	public boolean leaveStepBackward(){	
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
		return true;
	}
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
    	List tierOrder = null;
		//retrieve annotation value type
		int annotationValueType = -1;
		if( durationRadioButton.isSelected() )
			annotationValueType = ANN_VALUE_TIME_FORMAT;
		else if( specificValueRB.isSelected() )
			annotationValueType = ANN_VALUE_SPECIFIC_VALUE;
		else if( tierValueRadioButton.isSelected() )
			annotationValueType = ANN_VALUE_FROM_TIER;
		else if(concatValuesRB.isSelected()){
			if(sortByTimeRB.isSelected()){
				annotationValueType = ANN_VALUE_CONCAT_BASED_ON_TIME;
			} else if(sortBySelectionRB.isSelected()){
				annotationValueType = ANN_VALUE_CONCAT_BASED_ON_TIERORDER;
				tierOrder = new ArrayList();
				for(int i =0; i< sortTierTable.getRowCount(); i++){
					tierOrder.add(sortTierTable.getValueAt(i,0));
				}
			}
		}
		
		multiPane.putStepProperty("AnnotationValueType", annotationValueType);
		multiPane.putStepProperty("TierOrder", tierOrder);
		
		//store time format
		String timeFormat = null;
		boolean usePalFormat = true;
		
		if( msecRB.isSelected() ) 
			timeFormat = Constants.MS_STRING; //msec
		else if( secRB.isSelected() ) 
			timeFormat = Constants.SSMS_STRING; //ss:msec
		else if( hrRB.isSelected() ) 
			timeFormat = Constants.HHMMSSMS_STRING; //hh:mm:ss.ms
		else if( smpteRB.isSelected() && palRB.isSelected() )
		{
			timeFormat = Constants.HHMMSSFF_STRING; //SMPTE (PAL)
			usePalFormat = true;
		}
		else if( smpteRB.isSelected() && ntscRB.isSelected() )
		{
			timeFormat = Constants.HHMMSSFF_STRING; //SMPTE (NTSC)
			usePalFormat = false;
		}
		
		multiPane.putStepProperty("TimeFormat", timeFormat);
		multiPane.putStepProperty("UsePalFormat", usePalFormat);
		
		//retrieve annotation value	
		multiPane.putStepProperty("AnnotationValue", specificValueTF.getText().trim());
		
		//annotation from specific tier name
		multiPane.putStepProperty("AnnFromTier", (String)tierSelectBox.getSelectedItem());

        return true;
    }
	
	/**
	 * Set the button states appropriately, according to constraints
	 */
	public void updateButtonStates(){
		boolean b = durationRadioButton.isSelected();
		msecRB.setEnabled(b);
		secRB.setEnabled(b);
		hrRB.setEnabled(b);
		smpteRB.setEnabled(b);
		
		palRB.setEnabled(b && smpteRB.isSelected());
		ntscRB.setEnabled(b && smpteRB.isSelected());
		
		b = concatValuesRB.isSelected();
		sortByTimeRB.setEnabled(b);
		sortBySelectionRB.setEnabled(b);
		
		sortTierTable.setEnabled( b && sortBySelectionRB.isSelected());
		
		tierSelectBox.setEnabled(tierValueRadioButton.isSelected());		
		
		specificValueTF.setEnabled(specificValueRB.isSelected());
		
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		
		if( specificValueRB.isSelected() )
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, specificValueTF.getText().trim().length() > 0);
		else
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}
	
	 /**
     * Moves selected tier up in the list of tiers.
     */
    protected void moveDown() {
    	if ((sortTierTable == null) || (model == null)){	               
            return;
        }

        int row = sortTierTable.getSelectedRow();
        model.moveRow(row, row, row + 1);
        sortTierTable.changeSelection(row+1, 0, false, false);	    
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
     protected void moveUp() {
        if ((sortTierTable == null) || (model == null)){	               
            return;
        }

        int row = sortTierTable.getSelectedRow();
        model.moveRow(row, row, row - 1);	        
        sortTierTable.changeSelection(row-1, 0, false, false);
    }
	
	/**
	 * Action handler for buttons and radio buttons in this panel
	 */
	public void actionPerformed(ActionEvent e) {			
		updateButtonStates();		
		if(e.getSource() == moveUpButton){
			moveUp();
		} else if(e.getSource() == moveDownButton){
			moveDown();
		} else if (e.getSource() == sortBySelectionRB){
			sortTierTable.setForeground(Color.BLACK);	
		}
	}

	/**
	 * Not used
	 */
	public void keyPressed(KeyEvent e) {}

	/**
	 * Key release handler that updates the button states
	 */
	public void keyReleased(KeyEvent e) {
		updateButtonStates();
	}

	/**
	 * Key typed handler that updates the button states
	 */
	public void keyTyped(KeyEvent e) {
		updateButtonStates();
	}

	public void valueChanged(ListSelectionEvent e) {	
		if(sortTierTable.getSelectedRow() == 0){
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(true);				
		} else 	if(sortTierTable.getSelectedRow() == sortTierTable.getRowCount()-1 ){
			moveUpButton.setEnabled(true);
			moveDownButton.setEnabled(false);
		} else {
			if(sortTierTable.getSelectedRow() > 0){
				moveUpButton.setEnabled(true);
				moveDownButton.setEnabled(true);
			} else {
				moveUpButton.setEnabled(false);
				moveDownButton.setEnabled(false);
			}
		}
	}
}


