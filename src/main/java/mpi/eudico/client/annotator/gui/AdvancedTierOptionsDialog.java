package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.util.FavoriteColorPanel;
import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;


/**
 * A dialog to set or change some more tier attributes.  At this moment thes
 * are a few user preferences.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class AdvancedTierOptionsDialog extends JDialog implements ActionListener {
    private HashMap tierProperties;
    
    private JPanel colorPreviewPanel;
    private JButton colorButton;
    private JButton resetColorButton;
   
    private JPanel highlightColorPreviewPanel;
    private JButton highlightColorButton;
    private JButton resetHighlightColorButton;
   
    private JTextField fontTextField;
    private JButton fontButton;
    private JButton resetFontButton;
   
    private JButton changeButton;
    private JButton cancelButton;
    
    private String tierName;
    private Color fontColor;
    private Color highlightColor;
    private Font font;
    
    private boolean applySettings = false;

	private Transcription transcription;

    /**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param modal modal flag
     * @param tierProperties a map containing current properties key-value pairs
     *
     * @throws HeadlessException
     */
    public AdvancedTierOptionsDialog(Dialog owner, boolean modal,
        HashMap tierProperties) throws HeadlessException {
        this(owner, "", modal, tierProperties);
    }
    
    /**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param title the dialog title
     * @param modal modal flag 
     * @param tierProps a map containing current properties key-value pairs
     *
     * @throws HeadlessException
     * @throws IllegalArgumentException if tierproperties are null or if no tier name
     * property is found
     */
    public AdvancedTierOptionsDialog(Dialog owner, String title, boolean modal,
            HashMap tierProps ) throws HeadlessException {
    	 super(owner, title, modal);         
         initialize(tierProps);  
    }    
    
    public AdvancedTierOptionsDialog(Frame owner, String title, Transcription transcription, String tierName) {     
		super(owner, title, true);
		this.setTitle(title);
		this.tierName = tierName;
		applySettings = true;
		this.transcription = transcription;
		
		HashMap tierProps = new HashMap();
		tierProps.put("TierName", tierName);
		
		HashMap map = (HashMap) Preferences.get("TierColors", transcription);
		if (map != null) {
			tierProps.put("TierColor", map.get(tierName));
		}
		
		map = (HashMap) Preferences.get("TierHighlightColors", transcription);
		if (map != null) {
			tierProps.put("TierHighlightColor", map.get(tierName));
		}
		
		map = (HashMap) Preferences.get("TierFonts", transcription);
		if (map != null) {
			tierProps.put("TierFont", map.get(tierName));
		}
		
		initialize(tierProps);  
	}
    
    private void initialize(HashMap tierProps){
    	 if ((tierProps == null) || (tierProps.get("TierName") == null)) {
             throw new IllegalArgumentException("Insufficient tier properties.");
         }        
    
         this.tierProperties = new HashMap(tierProps.size());
         tierProperties.putAll(tierProps);
         initComponents();
         postInit();
    }

    /**
     * Initializes the ui components.
     */
    private void initComponents() {
        fontColor = (Color) tierProperties.get("TierColor");
        if ( fontColor == null) {
        	fontColor = Color.WHITE;
        }
        
        highlightColor = (Color) tierProperties.get("TierHighlightColor");
        if ( highlightColor == null) {
        	highlightColor = Color.WHITE;
        }
        
        font = (Font) tierProperties.get("TierFont");
    	
        tierName = (String) tierProperties.get("TierName");
    	
    	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        JPanel prefPanel = new JPanel();

        JLabel titleLabel = new JLabel();
        titleLabel.setText(ElanLocale.getString("EditTierDialog.Label.TierName") +
            ": " + tierName);
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);

        prefPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.Label.TierPreferences")));
        prefPanel.setLayout(new GridBagLayout());

        //font color panel
        JLabel colorLabel = new JLabel(ElanLocale.getString(
                    "EditTierDialog.Label.TierColor"));
        colorButton = new JButton(ElanLocale.getString("Button.Browse"));
        colorButton.addActionListener(this);
        resetColorButton = new JButton(ElanLocale.getString("Button.Default"));
        resetColorButton.addActionListener(this);
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
        colorPreviewPanel.setBackground(fontColor);
        colorPreviewPanel.setPreferredSize(new Dimension(colorButton.getPreferredSize().height, 
        		colorButton.getPreferredSize().height));
        colorPreviewPanel.setMinimumSize(new Dimension(colorButton.getPreferredSize().height, 
        		colorButton.getPreferredSize().height));   
        
		//highlight color panel
        JLabel highlightColorLabel = new JLabel(ElanLocale.getString(
        			"EditTierDialog.Label.TierHighlightColor"));
        highlightColorButton = new JButton(ElanLocale.getString("Button.Browse"));
        highlightColorButton.addActionListener(this);
        resetHighlightColorButton = new JButton(ElanLocale.getString("Button.Default"));
        resetHighlightColorButton.addActionListener(this);
        highlightColorPreviewPanel = new JPanel();
        highlightColorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
        highlightColorPreviewPanel.setBackground(highlightColor);       
        highlightColorPreviewPanel.setPreferredSize(new Dimension(
        		highlightColorButton.getPreferredSize().height,
        		highlightColorButton.getPreferredSize().height));
        highlightColorPreviewPanel.setMinimumSize(new Dimension(
        		highlightColorButton.getPreferredSize().height,
        		highlightColorButton.getPreferredSize().height));
        
        
        // font name panel
        JLabel fontLabel = new JLabel(ElanLocale.getString(
                    "EditTierDialog.Label.TierFont"));
        fontTextField = new JTextField(20);
        fontTextField.setEditable(false);
        if (font != null) {
            fontTextField.setText(font.getName());
        }

        fontButton = new JButton(ElanLocale.getString("Button.Browse"));
        fontButton.addActionListener(this);
        resetFontButton = new JButton(ElanLocale.getString("Button.Default"));
        resetFontButton.addActionListener(this);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        prefPanel.add(colorLabel, gbc);
       
        gbc.gridx = 1;
        prefPanel.add(colorPreviewPanel, gbc);
        
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
       
        gbc.weightx = 0;
        gbc.weighty = 0;
        prefPanel.add(colorButton, gbc);
      
        gbc.gridx = 4;
        prefPanel.add(resetColorButton, gbc);
       
        gbc.gridx = 0;
        gbc.gridy = 1;
        prefPanel.add(highlightColorLabel, gbc);
       
        gbc.gridx = 1;
        prefPanel.add(highlightColorPreviewPanel, gbc);
       
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        prefPanel.add(highlightColorButton, gbc);
        
        gbc.gridx = 4;
        prefPanel.add(resetHighlightColorButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        prefPanel.add(fontLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        prefPanel.add(fontTextField, gbc);
        
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        prefPanel.add(fontButton, gbc);
        
        gbc.gridx = 4;
        prefPanel.add(resetFontButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(prefPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        changeButton = new JButton();
        changeButton.setText(ElanLocale.getString("Button.Apply"));
        changeButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);

        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent we) {
        		doClose();
        	}
        });
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        /*
           int w = 300;
           int h = 200;
           setSize((getSize().width < w) ? w : getSize().width,
               (getSize().height < h) ? h : getSize().height);
         */
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    /**
     * Creates a customized color chooses, which includes a panel for (persistent) favorite
     * colors.
     * 
     * @param oldColor the color to start with
     * @return a new color or null
     */
    private Color chooseColor(final Color oldColor) {
    	Color newColor = null;
    	
    	final JColorChooser chooser = new JColorChooser(oldColor);
    	AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
    	AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
    	FavoriteColorPanel fcp = new FavoriteColorPanel();
    	panels2[0] = fcp;
    	
    	for (int i = 0; i < panels.length; i++) {
    		panels2[i + 1] = panels[i];
    	}
    	
    	chooser.setChooserPanels(panels2);
    	// read stored favorite colors
    	Map oldColors = null;
    	Object stored = Preferences.get("FavoriteColors", null);
    	if (stored instanceof Map) {
    		oldColors = (Map) stored;
    		//Color[] favColors = new Color[fcp.NUM_COLS * fcp.NUM_ROWS];
    		Color[] favColors = fcp.getColors();// use the array of the panel
    		Iterator coIt = oldColors.keySet().iterator();
    		String key;
    		Color val;
    		while (coIt.hasNext()) {
    			key = (String) coIt.next();
    			val = (Color) oldColors.get(key);
    			try {
    				int index = Integer.valueOf(key);
        			if (index < favColors.length) {
        				favColors[index] = val;
        			}
    			} catch (NumberFormatException nfe) {
    				// ignore
    			}
    		}
    		//fcp.setColors(favColors);
    	}
    	
    	// have to provide an "OK" action listener...
    	AbstractAction aa = new AbstractAction() {
    			
			public void actionPerformed(ActionEvent e) {
				putValue(Action.DEFAULT, chooser.getColor());				
			}};
			
        JDialog cd = JColorChooser.createDialog(this, ElanLocale.getString("ColorChooser.Title"), 
        		true, chooser, aa, null);
        cd.setVisible(true);
        
        // if necessary store the current favorite colors
        HashMap<String, Color> colMap = new HashMap<String, Color>();
        Color[] colors = fcp.getColors();
        for (int i = 0; i < colors.length; i++) {
        	if (colors[i] != null) {
        		colMap.put(String.valueOf(i), colors[i]);
        	}
        }
        
        if (colMap.size() > 0 || oldColors != null) {
        	Preferences.set("FavoriteColors", colMap, null);
        }
        
        newColor = (Color) aa.getValue(Action.DEFAULT);
        
    	return newColor;
    }
    
    private void selectTierColor() {    	
    	Color newColor = chooseColor(fontColor);
    
        if (newColor != null && !newColor.equals(fontColor)) {
        	fontColor = newColor;
            colorPreviewPanel.setBackground(fontColor);
        }
    }    
    
    private void selectHighlightColor() {    	
    	Color newColor = chooseColor(highlightColor);      

        if (newColor != null && !newColor.equals(highlightColor)) {
        	highlightColor = newColor;
            highlightColorPreviewPanel.setBackground(highlightColor);           
        }
    }
    
    private void selectFont() {
    	JFontChooser jfc = new JFontChooser();

    	Font f = jfc.showDialog(this, true, font);
    	if (f != null) {    	
    		fontTextField.setText(f.getName());
    		font = f;
    	}
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * The action event handling
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeButton) {
        	AdvancedAttributeSettingOptionDialog dialog;
        	tierProperties.put("TierColor", fontColor);
        	tierProperties.put("TierHighlightColor", highlightColor);
            tierProperties.put("TierFont", font);  
        	if(applySettings){
        		applySettingsToCurrentTier();
        		dialog = new AdvancedAttributeSettingOptionDialog(this, this.getTitle(),transcription, tierName);
        	} else {
                dialog = new AdvancedAttributeSettingOptionDialog(this, this.getTitle(), tierProperties);
        	}
        	dialog.setVisible(true);
        	tierProperties = dialog.getTierProperties();
            doClose();
        } else if (e.getSource() == cancelButton) {
            tierProperties = null;
            doClose();
        } else if (e.getSource() == colorButton) {
            selectTierColor();
        } else if (e.getSource() == resetColorButton) {
        	 fontColor = Color.WHITE;
             colorPreviewPanel.setBackground(fontColor);
        } else if (e.getSource() == highlightColorButton) { 
        	selectHighlightColor();
        } else if (e.getSource() == resetHighlightColorButton) {
        	highlightColor = Color.WHITE;
        	highlightColorPreviewPanel.setBackground(highlightColor);        	
        } else if (e.getSource() == fontButton) {		
            selectFont();
        } else if (e.getSource() == resetFontButton) {
        	    fontTextField.setText("");
    		    font = null;
        }
    }
    
    private void applySettingsToCurrentTier(){ 
		
    	if(tierProperties.get("TierColor") != null){
			HashMap colorMap = (HashMap) Preferences.get("TierColors", transcription);
			if (colorMap == null) {
				colorMap = new HashMap();
			}
			colorMap.put(tierName, tierProperties.get("TierColor"));		
			Preferences.set("TierColors", colorMap, transcription);
		}
		
		if(tierProperties.get("TierHighlightColor") != null){
			HashMap highlightMap = (HashMap) Preferences.get("TierHighlightColors", transcription);
			if (highlightMap == null) {
				highlightMap = new HashMap();
			}
			highlightMap.put(tierName, tierProperties.get("TierHighlightColor"));		
			Preferences.set("TierHighlightColors", highlightMap, transcription);
		}
		
		
		if(tierProperties.get("TierFont") != null){
			HashMap fontMap = (HashMap) Preferences.get("TierFonts", transcription);
			if (fontMap == null) {
				fontMap = new HashMap();
			}
			fontMap.put(tierName, tierProperties.get("TierFont"));		
			Preferences.set("TierFonts", fontMap, transcription);
		}
	}

    /**
     * Returns the, possibly modified, properties.
     *
     * @return the properties
     */
    public HashMap getTierProperties() {
        return tierProperties;
    }
}
