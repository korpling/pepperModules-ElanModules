package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

public class AdvancedAttributeSettingOptionDialog extends JDialog implements ActionListener, ChangeListener{
	
	private JCheckBox typeCB;
	private JCheckBox dependentTiersCB;
	private JCheckBox participantsCB;
	private JCheckBox tierColorCB;
	private JCheckBox tierHighLightColorCB;
	private JCheckBox tierFontCB;
	private JButton okButton;
	
	private HashMap tierProperties;	
	private String tierName;
    private Transcription transcription;
    
    // flag which says whether it is called from tier dialog or
    // a direct call to apply the attributes of the current tier
    private boolean appleAttributesDlg = false;
    
	public AdvancedAttributeSettingOptionDialog(Dialog owner, String title, HashMap tierProps) {
        super(owner, title, true);
        this.tierProperties = tierProps;
        initComponents();
        postInit();
	}
	
	public AdvancedAttributeSettingOptionDialog(Dialog owner, String title,Transcription transcription, String tierName) {     
		super(owner, title, true);
		
		initialize(transcription, tierName);
	}
	
	public AdvancedAttributeSettingOptionDialog(Frame owner, String title,Transcription transcription, String tierName) {     
		super(owner, title, true);
		
		initialize(transcription, tierName);

	}
	
	private void initialize(Transcription transcription, String tierName){
		appleAttributesDlg = true;
		this.tierName = tierName;
		this.transcription = transcription;
        initComponents();
        postInit();
	}
	
	private void initComponents(){		
		typeCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.Type"));
		typeCB.addChangeListener(this);
		
		dependentTiersCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.DependentTiers"));
		dependentTiersCB.addChangeListener(this);
		
		participantsCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.Participants"));
		participantsCB.addChangeListener(this);
		
		tierColorCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierColor"),true);
		tierColorCB.addChangeListener(this);
		
		tierHighLightColorCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierHighlightColor"), true);
		tierHighLightColorCB.addChangeListener(this);
		
		tierFontCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierFont"), true);
		tierFontCB.addChangeListener(this);
						
		getContentPane().setLayout(new GridBagLayout());
        
        JPanel optionsPanel = new JPanel();  
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.AdvancedSetting.Label.Options")));
        optionsPanel.setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;        
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;      
        gbc.insets = insets;
        optionsPanel.add(typeCB, gbc);
        
        gbc.gridy = 1;      
        optionsPanel.add(dependentTiersCB, gbc);
        
        gbc.gridy = 2;      
        optionsPanel.add(participantsCB, gbc);
        
        JPanel settingsPanel = new JPanel();  
        settingsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.AdvancedSetting.Label.Setting")));
        settingsPanel.setLayout(new GridBagLayout());
        
        okButton = new JButton();
        okButton.setText(ElanLocale.getString("Button.Apply"));
        okButton.addActionListener(this);
        
        if(appleAttributesDlg){
        	okButton.setEnabled(false);
        }
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;        
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;      
        gbc.insets = insets;
        settingsPanel.add(tierColorCB, gbc);
        
        gbc.gridy = 1;      
        settingsPanel.add(tierHighLightColorCB, gbc);
        
        gbc.gridy = 2;      
        settingsPanel.add(tierFontCB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(settingsPanel, gbc);        
        
        gbc.gridy = 1;
        getContentPane().add(optionsPanel, gbc);        

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(okButton, gbc);   
	}
	
	 /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();        
        setResizable(false);
        setLocationRelativeTo(getParent());
    }
	
	private void doClose() {
        setVisible(false);
        dispose();
    }
	
	/**
     * Returns the, possibly modified, properties.
     *
     * @return the properties
     */
    public HashMap getTierProperties() {
        return tierProperties;
    }

	public void actionPerformed(ActionEvent e) {		

		// if can one of the options are selected then applychanges
		if(tierColorCB.isSelected() || tierHighLightColorCB.isSelected() || tierFontCB.isSelected()){
			if(typeCB.isSelected() || dependentTiersCB.isSelected() || participantsCB.isSelected()){
				if(tierProperties !=null){
					applyNewPropertyChanges(); 
				}else {
					applyAttributeSettings();
				}
			}
		}
		
		doClose();
	}
	
	private void applyNewPropertyChanges(){		
		tierProperties.put("SameType", typeCB.isSelected());   
		tierProperties.put("DependingTiers", dependentTiersCB.isSelected());  
		tierProperties.put("SameParticipants", participantsCB.isSelected());  
		tierProperties.put("Color", tierColorCB.isSelected());   
		tierProperties.put("HighLightColor", tierHighLightColorCB.isSelected());  
		tierProperties.put("Font", tierFontCB.isSelected());
	}
	
	private void applyAttributeSettings(){
		HashMap colors = (HashMap) Preferences.get("TierColors", transcription);
		if (colors == null) {
			colors = new HashMap();
			Preferences.set("TierColors", colors, transcription);
		}
        
		HashMap highlightColors = (HashMap) Preferences.get("TierHighlightColors", transcription);
        if(highlightColors == null) {
        	highlightColors = new HashMap();
        	Preferences.set("TierHighlightColors", highlightColors, transcription);
        }        

        HashMap fonts = (HashMap) Preferences.get("TierFonts", transcription);
		if (fonts == null) {
			fonts = new HashMap();
			Preferences.set("TierFonts", fonts, transcription);
		}
    	
    	TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);		
		
    	
		Color nextColor = (Color) colors.get(tierName);
        Color nextHighlightColor = (Color) highlightColors.get(tierName);       
		Font fo = (Font) fonts.get(tierName);
		
    	Vector tierList = new Vector();
		if(typeCB.isSelected()){
			if(tier.getLinguisticType() != null){
				if(transcription.getTiersWithLinguisticType(tier.getLinguisticType().getLinguisticTypeName()) !=null){
					tierList.addAll(transcription.getTiersWithLinguisticType(tier.getLinguisticType().getLinguisticTypeName()));
				}
			}
		}
		
		if(dependentTiersCB.isSelected()){
			if(tier.getDependentTiers() != null){
				tierList.addAll(tier.getDependentTiers());
			}
		}  
		
		if(participantsCB.isSelected() ){
			if(tier.getParticipant() != null){
				Vector allTiers = transcription.getTiers();
				for(int i= 0; i<allTiers.size(); i++ ){
					TierImpl t = (TierImpl) allTiers.get(i);
					if(t.getParticipant()!= null){
						if(t.getParticipant().equals(tier.getParticipant())){
							if(!tierList.contains(t)){
								tierList.add(t);
							}
						}
					}
				}
			}				
		}
		
		for(int i=0; i< tierList.size(); i++){
			TierImpl t = (TierImpl) tierList.get(i);
			if (tierColorCB.isSelected() && nextColor != null && !nextColor.equals(Color.WHITE) ) {
				((Map) colors).put(t.getName(), nextColor); 
        	}
			
			if (tierHighLightColorCB.isSelected() && (nextHighlightColor != null) && !nextHighlightColor.equals(Color.WHITE)) {
				((Map) highlightColors).put(t.getName(), nextHighlightColor);
            } 
		
			if (tierFontCB.isSelected() && fo != null) {
				((Map) fonts).put(t.getName(), fo);
        	}        			
		}
		
		if(nextColor != null || nextHighlightColor != null || fo != null ){
			// notify
			Preferences.set("TierHighlightColors", highlightColors, transcription, true);
			Preferences.set("TierColors", colors, transcription, true);
			Preferences.set("TierFonts", fonts, transcription, true);
		} else {
			JOptionPane.showMessageDialog(this,"No attribute settings avaible for the selected tier.",                
	                ElanLocale.getString("Message.Warning"),
	                JOptionPane.WARNING_MESSAGE);
		}		
	}
	
	public void stateChanged(ChangeEvent e) {		
		if(tierColorCB.isSelected() || tierHighLightColorCB.isSelected() || tierFontCB.isSelected()){
			typeCB.setEnabled(true);
			dependentTiersCB.setEnabled(true);
			participantsCB.setEnabled(true);
			 if(appleAttributesDlg){
				 if(typeCB.isSelected()|| dependentTiersCB.isSelected() || participantsCB.isSelected()){
		        	okButton.setEnabled(true);
				 } else {
		        	okButton.setEnabled(false);
		         }
			 }
		}else {
			typeCB.setEnabled(false);
			dependentTiersCB.setEnabled(false);
			participantsCB.setEnabled(false);
			 if(appleAttributesDlg){
		        	okButton.setEnabled(false);
		        }
		}	
		
		
	}
	
}
