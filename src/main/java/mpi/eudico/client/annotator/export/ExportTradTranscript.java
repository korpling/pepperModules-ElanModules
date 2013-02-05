package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.charset.UnsupportedCharsetException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.util.TimeRelation;

import mpi.eudico.util.TimeFormatter;


/**
 * An export dialog for exporting tiers in a 'traditional' transcription style.
 * This class will probably be obsolete by the time the full-featured text
 * export  function is fully implemented.
 *
 * @author Han Sloetjes
 */
public class ExportTradTranscript extends AbstractExtTierExportDialog
    implements  ItemListener {
    
    /** ui elements */
    //private JCheckBox rootTiersCB;
	private JCheckBox labelsCB;
	private JTextField labelWidthTF;
    private JCheckBox labelWidthCB;
    private JCheckBox selectionCB;
    private JCheckBox timeCodeCB;
    private JCheckBox silenceCB;
    private JTextField minDurSilTF;
    private JLabel minDurSilLabel;
    private JComboBox silenceDecimalComboBox;
    private JLabel silDecimalLabel;
    private JCheckBox wrapLinesCB;
    private JCheckBox emptyLineCB;
    private JLabel charPerLineLabel;
    private JTextField numCharTF;
	private JCheckBox mergeAnnCB;
	private JTextField mergeDurTF;
    private JLabel mergeDurLabel;

    /** new line string */
    private final String NEW_LINE = "\n";

    // some strings
    // not visible in the table header
    /** white space string */
    private final String SPACE = " ";

    /** string to separate time codes */
    private final String TIME_SEP = " - ";

    /** new line char */
    private final char NL_CHAR = '\n';

    /** white space char */
    private final char SPACE_CHAR = ' ';

    /** space between label and contents */
    private final int LABEL_VALUE_MARGIN = 3;

    /** default line width */
    private final int NUM_CHARS = 80;
    
    /** default tier label width */
    private final int LABEL_WIDTH = 5;
    
    /** default merge duration limit*/
    private final int MERGE_DUR = 50;
    
    /** default minimal silence duration  */
    private final int MIN_SILENCE = 20;
    
    /**
     * Constructor.
     *
     * @param parent parent frame
     * @param modal the modal/blocking attribute
     * @param transcription the transcription to export from
     * @param selection the current selection
     */
    public ExportTradTranscript(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == wrapLinesCB) {
            if (wrapLinesCB.isSelected()) {
                numCharTF.setEnabled(true);
                numCharTF.setBackground(Constants.SHAREDCOLOR4);

                if ((numCharTF.getText() != null) ||
                        (numCharTF.getText().length() == 0)) {
                    numCharTF.setText("" + NUM_CHARS);
                }

                numCharTF.requestFocus();
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (ie.getSource() == silenceCB) {
            if (silenceCB.isSelected()) {
                minDurSilTF.setEnabled(true);
                minDurSilTF.setBackground(Constants.SHAREDCOLOR4);
                silenceDecimalComboBox.setEnabled(true);
                if (minDurSilTF.getText() == null || minDurSilTF.getText().length() == 0) {
                    minDurSilTF.setText("" + MIN_SILENCE);
                }
                
                minDurSilTF.requestFocus();
            } else {
                minDurSilTF.setEnabled(false);
                silenceDecimalComboBox.setEnabled(false);
                minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if(ie.getSource() == labelsCB){
        	labelWidthCB.setEnabled(labelsCB.isSelected());
        	if(labelWidthCB.isEnabled()){
        		if (labelWidthCB.isSelected()) {
            		labelWidthTF.setEnabled(true);
           		 	labelWidthTF.setBackground(Constants.SHAREDCOLOR4);
                    if (labelWidthTF.getText() == null || labelWidthTF.getText().length() == 0) {
                   	 	labelWidthTF.setText("" + LABEL_WIDTH);
                    }                
                    labelWidthTF.requestFocus();
                } else {
               	 labelWidthTF.setEnabled(false);                
               	 labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                }
        	} else {
        		labelWidthTF.setEnabled(false);                
              	labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        	}
        } else if(ie.getSource() == labelWidthCB){
        	if (labelWidthCB.isSelected() && labelWidthCB.isEnabled()) {
        		labelWidthTF.setEnabled(true);
       		 	labelWidthTF.setBackground(Constants.SHAREDCOLOR4);
                if (labelWidthTF.getText() == null || labelWidthTF.getText().length() == 0) {
               	 	labelWidthTF.setText("" + LABEL_WIDTH);
                }                
                labelWidthTF.requestFocus();
            } else {
           	 labelWidthTF.setEnabled(false);                
           	 labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        }        
        else if(ie.getSource() == mergeAnnCB){
       	 	if (mergeAnnCB.isSelected()) {
       	 		mergeDurTF.setEnabled(true);
       	 		mergeDurTF.setBackground(Constants.SHAREDCOLOR4);
       	 		if (mergeDurTF.getText() == null || mergeDurTF.getText().length() == 0) {
       	 			mergeDurTF.setText("" + MERGE_DUR);
       	 		}
             
       	 		mergeDurTF.requestFocus();
       	 	} else {
       	 		mergeDurTF.setEnabled(false);                
       	 		mergeDurTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
       	 	}
        }
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {       
    	Object useTyp;
    	
    	useTyp = Preferences.get("ExportTradTranscript.TierOrder", transcription);
    	
    	if (useTyp instanceof List) {
    		setTierOrder((List) useTyp);
    	} else {
    		super.extractTiers(true);
    	}
    	
        useTyp = Preferences.get("ExportTradTranscript.selectedTiers", transcription);
        if (useTyp instanceof List) {
        	setSelectedTiers((List) useTyp);
        } 
        
        useTyp = Preferences.get("ExportTradTranscript.SelectTiersMode", transcription);
        if (useTyp instanceof String) {
        	//List list = (List) Preferences.get("ExportTradTranscript.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);        	
        	setSelectionMode((String)useTyp);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals((String) useTyp) ) {
            	// call this after! the mode has been set
            	Object selItems  = Preferences.get("ExportTradTranscript.LastSelectedItems", transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems((List) selItems);
            	}
        	}
         }	
    }

    /**
     * Initializes UI elements.
     */
    protected void makeLayout() {
        super.makeLayout();
        charPerLineLabel = new JLabel();
        wrapLinesCB = new JCheckBox();
        numCharTF = new JTextField(4);
        timeCodeCB = new JCheckBox();
        silenceCB = new JCheckBox();
        minDurSilLabel = new JLabel();
        minDurSilTF = new JTextField(4);
        silDecimalLabel = new JLabel();        
        silenceDecimalComboBox = new JComboBox();
        silenceDecimalComboBox.addItem(Constants.ONE_DIGIT);
        silenceDecimalComboBox.addItem(Constants.TWO_DIGIT);
        silenceDecimalComboBox.addItem(Constants.THREE_DIGIT);
        silenceDecimalComboBox.setSelectedItem(Constants.TWO_DIGIT);
        labelsCB = new JCheckBox();
        labelWidthCB = new JCheckBox();
        labelWidthTF = new JTextField(4);
        selectionCB = new JCheckBox();
        emptyLineCB = new JCheckBox();
        emptyLineCB.setSelected(true);
        mergeAnnCB = new JCheckBox();
        mergeDurLabel = new JLabel();
        mergeDurTF = new JTextField(4);

        GridBagConstraints gbc;

        optionsPanel.setLayout(new GridBagLayout());
        
        JPanel labelWidthPanel = new JPanel(new GridBagLayout());
        
        labelWidthCB.addItemListener(this);      
        labelWidthCB.setEnabled(false);  
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        labelWidthPanel.add(labelWidthCB, gbc);
        
        labelWidthTF.setEnabled(false);
        labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);  
        gbc.gridx = 1;
        labelWidthPanel.add(labelWidthTF, gbc);

        wrapLinesCB.addItemListener(this);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        optionsPanel.add(wrapLinesCB, gbc);

        JPanel fill = new JPanel();
        Dimension fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);       
        gbc.gridy = gbc.gridy +1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        optionsPanel.add(fill, gbc);
        
        numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gbc.gridx = 1;        
        optionsPanel.add(numCharTF, gbc);
        
        gbc.gridx = 2;        
        optionsPanel.add(charPerLineLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridwidth = 3;    
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(timeCodeCB, gbc);
        
        mergeAnnCB.addItemListener(this);
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(mergeAnnCB, gbc);
        
        fill = new JPanel();
        fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gbc.gridy = gbc.gridy +1;
        gbc.gridwidth = 1;       
        optionsPanel.add(fill, gbc);

        mergeDurTF.setEnabled(false);
        mergeDurTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);       
        gbc.gridx = 1;
        optionsPanel.add(mergeDurTF, gbc);     
        
        gbc.gridx = 2;       
        optionsPanel.add(mergeDurLabel, gbc);  

        labelsCB.addItemListener(this);
        gbc.gridx = 0;
        gbc.gridwidth = 3;   
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(labelsCB, gbc);
        
        fill = new JPanel();
        fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gbc.gridy = gbc.gridy +1;
        gbc.gridwidth = 1;       
        optionsPanel.add(fill, gbc);
        
        gbc.gridx = 1;     
        gbc.gridwidth = 2;
        optionsPanel.add(labelWidthPanel, gbc); 
        
        gbc.gridx = 0;
        gbc.gridwidth = 3;   
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(emptyLineCB, gbc);

        silenceCB.addItemListener(this);
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(silenceCB, gbc);
 
        fill = new JPanel();
        fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gbc.gridy = gbc.gridy +1;
        gbc.gridwidth = 1;       
        optionsPanel.add(fill, gbc);

        minDurSilTF.setEnabled(false);
        minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);       
        gbc.gridx = 1;
        optionsPanel.add(minDurSilTF, gbc);
        
        gbc.gridx = 2;       
        optionsPanel.add(minDurSilLabel, gbc); 
        
        fill = new JPanel();
        fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(fill, gbc);
        
        gbc.gridx = 1;
        optionsPanel.add(silenceDecimalComboBox, gbc);
      
        gbc.gridx = 2;       
        optionsPanel.add(silDecimalLabel, gbc); 
        
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy +1;
        gbc.gridwidth = 3;        
        optionsPanel.add(selectionCB, gbc);

        setPreferredSetting();
        updateLocale();
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false oherwise
     *
     * @throws IOException DOCUMENT ME!
     */
    protected boolean startExport(){
        List selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }
 
        // check the chars per line value
        int charsPerLine = Integer.MAX_VALUE;

        if (wrapLinesCB.isSelected()) {
            String textValue = numCharTF.getText().trim();

            try {
                charsPerLine = Integer.parseInt(textValue);
                if(charsPerLine <= 10){
           		 	showWarningDialog(ElanLocale.getString(
           	                "ExportTradTranscript.Message.InvalidNumber"));
           		 	numCharTF.selectAll();
           		 	numCharTF.requestFocus();
  	                return false;                
            	}
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                        "ExportTradTranscript.Message.InvalidNumber"));
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }
        
     // check the minimal silence duration      
        int labelWidth = LABEL_WIDTH;
        if (labelsCB.isSelected() && labelWidthCB.isSelected()) {
            String textValue = labelWidthTF.getText().trim();
            
            try {
            	labelWidth = Integer.parseInt(textValue);
            	if(labelWidth <= 0){
            		 showWarningDialog(ElanLocale.getString(
            	                "ExportTradTranscript.Message.InvalidNumber3"));
            		 labelWidthTF.selectAll();
            	     labelWidthTF.requestFocus();
   	                 return false;                
            	}
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                "ExportTradTranscript.Message.InvalidNumber3"));
                labelWidthTF.selectAll();
                labelWidthTF.requestFocus();

                return false;                
            }
        }
        
     // check the merge duration    
        int mergeValue = MERGE_DUR;
        if (mergeAnnCB.isSelected()) {
            String textValue = mergeDurTF.getText().trim();            
            
            try {
            	mergeValue = Integer.parseInt(textValue);
            	if(mergeValue <= 0){
           		 	showWarningDialog(ElanLocale.getString(
           	                "ExportTradTranscript.Message.InvalidNumber4"));
           		 	mergeDurTF.selectAll();
           		 	mergeDurTF.requestFocus();
  	                return false;                
            	}
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                "ExportTradTranscript.Message.InvalidNumber4"));
                mergeDurTF.selectAll();
                mergeDurTF.requestFocus();

                return false;                
            }
        }
        
        // check the minimal silence duration
        int minSilence = MIN_SILENCE;
        if (silenceCB.isSelected()) {
            String textValue = minDurSilTF.getText().trim();
            
            try {
                minSilence = Integer.parseInt(textValue);
                if(minSilence <= 0){
           		 	showWarningDialog(ElanLocale.getString(
           	                "ExportTradTranscript.Message.InvalidNumber2"));
           		 	minDurSilTF.selectAll();
           		 	minDurSilTF.requestFocus();
  	                return false;                
            	}
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                "ExportTradTranscript.Message.InvalidNumber2"));
                minDurSilTF.selectAll();
                minDurSilTF.requestFocus();

                return false;                
            }
        }

        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                    "ExportTradTranscript.Title"), null, FileExtension.TEXT_EXT, true);

        if (exportFile == null) {
            return false;
        }

        // export....
        return doExport(exportFile, selectedTiers, charsPerLine, minSilence, mergeValue, labelWidth);
    }

    /**
     * Applies localized strings to the ui elements. For historic reasons the
     * string identifiers start with "TokenizeDialog"
     */
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportTradTranscript.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTradTranscript.Title"));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.Options")));
        wrapLinesCB.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.WrapLines"));
        charPerLineLabel.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.NumberChars"));
        timeCodeCB.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.IncludeTimeCode"));
        labelsCB.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.IncludeTierLabels"));
        silenceCB.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.IncludeSilence"));
        minDurSilLabel.setText(ElanLocale.getString(
                "ExportTradTranscript.Label.MinSilenceDuration"));
        selectionCB.setText(ElanLocale.getString("ExportDialog.Restrict"));
        silDecimalLabel.setText(ElanLocale.getString(
        		"InterlinearizerOptionsDlg.NumberofDigits"));
        emptyLineCB.setText(ElanLocale.getString(
        		"ExportTradTranscript.Label.IncludeEmptyLines"));
        labelWidthCB.setText(ElanLocale.getString(
        		"ExportTradTranscript.Label.MaxLabelWidth"));
        mergeAnnCB.setText(ElanLocale.getString(
        		"ExportTradTranscript.Label.MergeAnnotations"));
        mergeDurLabel.setText(ElanLocale.getString(
        		"ExportTradTranscript.Label.MergeDuration"));
    }

    /**
     * Creates a label string of length <code>numchars</code>. A number of
     * space characters will be added to the input string  to make it the
     * right length.
     *
     * @param name the input string
     * @param numchars the new length
     *
     * @return the input string with the right number of space characters added
     *         to it
     */
    private String getMarginString(String name, int numchars, final int fixedLabelWidth) {
    	int nameLength = 0;
        if (name != null) {
        	if( name.length() > fixedLabelWidth){
        		name = name.substring(0, fixedLabelWidth);
        	}
        	
        	nameLength = name.length();
        }

        StringBuffer bf = new StringBuffer(name);

        for (int i = 0; i < (numchars - nameLength); i++) {
            bf.append(SPACE);
        }

        return bf.toString();
    }

    /**
     * Split a string into an array of substrings, each not longer than  the
     * max annotation length.
     *
     * @param val the string to split
     * @param maxAnnotationLength the maximum length of the substrings
     *
     * @return an array of substrings
     */
    private String[] breakValue(String val, int maxAnnotationLength) {
        if (val == null) {
            return new String[] {  };
        }

        if ((val.indexOf(SPACE) < 0) || (val.length() < maxAnnotationLength)) {
            return new String[] { val };
        }

        ArrayList vals = new ArrayList();
        String sub = null;

        while (val.length() > maxAnnotationLength) {
            sub = val.substring(0, maxAnnotationLength);

            int breakSpace = sub.lastIndexOf(SPACE_CHAR);

            if (breakSpace < 0) {
                breakSpace = val.indexOf(SPACE_CHAR);

                if (breakSpace < 0) {
                    vals.add(val);

                    break;
                } else {
                    vals.add(val.substring(0, breakSpace + 1));
                    val = val.substring(breakSpace + 1);
                }
            } else {
                vals.add(sub.substring(0, breakSpace + 1));
                val = val.substring(breakSpace + 1);
            }

            if (val.length() <= maxAnnotationLength) {
                vals.add(val);

                break;
            }
        }

        return (String[]) vals.toArray(new String[] {  });
    }

    //******************************
    // actual export methods from here, for the time being
    //******************************

    /**
     * The actual writing. If this class is to survive alot of the export stuff
     * should go to another  class.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1
     * @param charsPerLine num of chars per line if linewrap is selected
     *
     * @return true if all went well, false otherwise
     */
    private boolean doExport(final File exportFile, final List orderedTiers,
        final int charsPerLine, final int minSilence, final int  mergeValue, final int fixedLabelWidth) {
        boolean selectionOnly = selectionCB.isSelected();
        boolean wrapLines = wrapLinesCB.isSelected();
        boolean includeTimeCodes = timeCodeCB.isSelected();
        boolean includeLabels = labelsCB.isSelected();
        boolean includeSilence = silenceCB.isSelected();
        boolean insertEmptyLine = emptyLineCB.isSelected();
        boolean mergeAnn = mergeAnnCB.isSelected();
      
        int labelMargin = 0;
        int labelWidth = 0;
        Hashtable marginStrings = null;

        String tcLabel = "TC";
        String emptyLabel = "empty";
        int maxAnnotationLength = charsPerLine;
        
        String name;

        if (includeLabels) {
            marginStrings = new Hashtable();
            if(labelWidthCB.isSelected()){
            	labelWidth = fixedLabelWidth;
            	labelMargin = labelWidth + LABEL_VALUE_MARGIN;
            } else {
            	for (int i = 0; i < orderedTiers.size(); i++) {
            		name = (String) orderedTiers.get(i);

            		if (name.length() > labelMargin) {
            			labelMargin = name.length();
            		}
            	}  
            	labelWidth = labelMargin;
            	labelMargin += LABEL_VALUE_MARGIN;
            }

            for (int i = 0; i < orderedTiers.size(); i++) {
                name = (String) orderedTiers.get(i);
                marginStrings.put(name, getMarginString(name, labelMargin, labelWidth));
            }

            // add timecode label
            if (includeTimeCodes) {
                if (!marginStrings.containsKey(tcLabel)) {
                    marginStrings.put(tcLabel,
                        getMarginString(tcLabel, labelMargin, labelWidth));
                } else {
                    String tcl;

                    for (int count = 1; count < 100; count++) {
                        tcl = tcLabel + "-" + count;

                        if (!marginStrings.containsKey(tcl)) {
                            tcLabel = tcl;
                            marginStrings.put(tcLabel,
                                getMarginString(tcLabel, labelMargin, labelWidth));

                            break;
                        }
                    }
                }
            }

            // add empty string
            if (!marginStrings.containsKey(emptyLabel)) {
                marginStrings.put(emptyLabel, getMarginString("", labelMargin, labelWidth));
            } else {
                String tcl;

                for (int count = 1; count < 100; count++) {
                    tcl = emptyLabel + "-" + count;

                    if (!marginStrings.containsKey(tcl)) {
                        emptyLabel = tcl;
                        marginStrings.put(emptyLabel,
                            getMarginString("", labelMargin, labelWidth));

                        break;
                    }
                }
            }
        }

        if (wrapLines && includeLabels) {
            maxAnnotationLength = charsPerLine - labelMargin;
        }

        long bb = 0;
        long eb = Long.MAX_VALUE;

        if (selectionOnly && (selection != null)) {
            bb = selection.getBeginTime();
            eb = selection.getEndTime();
        }

        // the parameters are set, create an ordered set of Annotation records
        TreeSet records = new TreeSet();
        String tierName;
        TierImpl t;
        Annotation ann;

        for (int i = 0; i < orderedTiers.size(); i++) {
            tierName = (String) orderedTiers.get(i);

            t = (TierImpl) transcription.getTierWithId(tierName);

            if (t == null) {
                continue;
            }

            Vector v = t.getAnnotations();

            for (int j = 0; j < v.size(); j++) {
                ann = (Annotation) v.get(j);

                if (TimeRelation.overlaps(ann, bb, eb)) {
                    records.add(new IndexedExportRecord(ann, i));
                }

                if (ann.getBeginTimeBoundary() > eb) {
                    break;
                }
            }
        }
        // if silence indicators should be part of the output, calculate them here 
        if (includeSilence) {
            IndexedExportRecord rec1 = null;
            IndexedExportRecord rec2 = null;
            long dur;
            long endTime = 0;
            
            Iterator recIter = records.iterator();
            while (recIter.hasNext()) {
                rec2 = (IndexedExportRecord) recIter.next();
                if (rec1 != null) {
                    // set the silence after of rec 1
                	dur = rec2.getBeginTime() - endTime;
                    if (dur >= minSilence) {
                           rec1.setSilenceAfter(formatSilenceString(dur));
                       }
                	
                }
            
            	if(endTime < rec2.getEndTime()){
            		 endTime = rec2.getEndTime();
            	}            	
                rec1 = rec2;   
            }
        }
        
        // merge value in seconds
        float mergeVal = mergeValue/1000f;
        
        // create output stream
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(exportFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);

            // do the writing
            Iterator recIter = records.iterator();
            IndexedExportRecord record;
            IndexedExportRecord nextRec;
            String val;
            String[] valLines;
            if(mergeAnn){
            	if (recIter.hasNext()) {
            		record = (IndexedExportRecord) recIter.next();            		
            		val = record.getValue();
            		long beginTime = record.getBeginTime();            		
            		float silDur = -1f;           		
            		
            		while(recIter.hasNext()){
            			nextRec = (IndexedExportRecord) recIter.next();
            			silDur = -1f;
                		if(record.getSilenceAfter() != null){
                			try{
                				silDur = Float.parseFloat(record.getSilenceAfter());
                    		}
                			catch(NumberFormatException e){
                				silDur = -1f;
                			}
                		}
                		
            			if(record.getTierName().equals(nextRec.getTierName()) && 
            					silDur > 0 && silDur <= mergeVal){
             					// check for silence duration
             					if (includeSilence) {                                 
             						val = val +" (" + silDur + ")";
                				}
             					val = val + " "+ nextRec.getValue();
             					record = nextRec;
            			} else {
            				// write the record
            				 val = val.replace(NL_CHAR, SPACE_CHAR);

                             if (includeLabels) {
                                 writer.write((String) marginStrings.get(
                                         record.getTierName()));
                             }

                             if (!wrapLines) {
                                 writer.write(val);                                
                             } else {
                                 if (!(val.length() > maxAnnotationLength)) {
                                     writer.write(val);
                                 } else {
                                     valLines = breakValue(val, maxAnnotationLength);

                                     for (int i = 0; i < valLines.length; i++) {
                                         if (i != 0 && includeLabels) {
                                             writer.write((String) marginStrings.get(
                                                     emptyLabel));
                                         }

                                         writer.write(valLines[i]);

                                         if (i != (valLines.length - 1)) {
                                             writer.write(NEW_LINE);
                                         }
                                     }
                                 }
                             }    
                             
                             writer.write(NEW_LINE);

                             if (includeTimeCodes) {
                                 if (includeLabels) {
                                     writer.write((String) marginStrings.get(tcLabel));
                                 }                   
                                 writer.write(TimeFormatter.toString(beginTime));
                                 writer.write(TIME_SEP);
                                 writer.write(TimeFormatter.toString(record.getEndTime()));
                                 writer.write(NEW_LINE);
                                 if (includeSilence && record.getSilenceAfter() != null) {
                                     if (includeLabels) {
                                         writer.write((String) marginStrings.get(
                                                 emptyLabel));    
                                     }
                                     writer.write("(" + record.getSilenceAfter() + ")");
                                     writer.write(NEW_LINE);
                                 }
                                 if (insertEmptyLine){
                                 	writer.write(NEW_LINE);
                                 }
                             } else if (includeSilence) {                             	
                                 if (record.getSilenceAfter() != null) {
             	                    if (includeLabels) {
             	                        writer.write((String) marginStrings.get(
             	                                emptyLabel));                       
             	                    }
             	                    writer.write("(" + record.getSilenceAfter() + ")");
             	                    writer.write(NEW_LINE);
                                 }
                                 if (insertEmptyLine){
                                 	writer.write(NEW_LINE);
                                 }
                             } else if (insertEmptyLine){
                             	writer.write(NEW_LINE);
                             }
                             
                             record = nextRec;
                             val = record.getValue();
                             beginTime = record.getBeginTime();
            			}
            		}
            		
            		// write the last record     
   				 	val = val.replace(NL_CHAR, SPACE_CHAR);

                    if (includeLabels) {
                        writer.write((String) marginStrings.get(
                                record.getTierName()));
                    }

                    if (!wrapLines) {
                        writer.write(val);
                    } else {
                        if (!(val.length() > maxAnnotationLength)) {
                            writer.write(val);
                        } else {
                            valLines = breakValue(val, maxAnnotationLength);

                            for (int i = 0; i < valLines.length; i++) {
                                if (i != 0 && includeLabels) {
                                    writer.write((String) marginStrings.get(
                                            emptyLabel));
                                }

                                writer.write(valLines[i]);

                                if (i != (valLines.length - 1)) {
                                    writer.write(NEW_LINE);
                                }
                            }
                        }
                    } 
                    
                    writer.write(NEW_LINE);

                    if (includeTimeCodes) {
                    	if (includeLabels) {
                            writer.write((String) marginStrings.get(tcLabel));
                        }                   
                        writer.write(TimeFormatter.toString(beginTime));
                        writer.write(TIME_SEP);
                        writer.write(TimeFormatter.toString(record.getEndTime()));
                        writer.write(NEW_LINE);
                        if (includeSilence && record.getSilenceAfter() != null) {
                            if (includeLabels) {
                                writer.write((String) marginStrings.get(
                                        emptyLabel));    
                            }
                            writer.write("(" + record.getSilenceAfter() + ")");
                        }
                        if (insertEmptyLine){
                        	writer.write(NEW_LINE);
                        }
                    } else if (includeSilence) {                    	
                        if (record.getSilenceAfter() != null) {
    	                    if (includeLabels) {
    	                        writer.write((String) marginStrings.get(
    	                                emptyLabel));                       
    	                    }
    	                    writer.write("(" + record.getSilenceAfter() + ")");
                        }
                        if (insertEmptyLine){
                        	writer.write(NEW_LINE);
                        }
                    } else if (insertEmptyLine){
                    	writer.write(NEW_LINE);
                    }
            	}
        	} else {
        		while (recIter.hasNext()) {
                	
                    record = (IndexedExportRecord) recIter.next();
                    val = record.getValue().replace(NL_CHAR, SPACE_CHAR);

                    if (includeLabels) {
                        writer.write((String) marginStrings.get(
                                record.getTierName()));
                    }

                    if (!wrapLines) {
                        writer.write(val);                        
                    } else {
                        if (!(val.length() > maxAnnotationLength)) {
                            writer.write(val);
                        } else {
                            valLines = breakValue(val, maxAnnotationLength);

                            for (int i = 0; i < valLines.length; i++) {
                                if (i != 0 && includeLabels) {
                                    writer.write((String) marginStrings.get(
                                            emptyLabel));
                                }

                                writer.write(valLines[i]);

                                if (i != (valLines.length - 1)) {
                                    writer.write(NEW_LINE);
                                }
                            }                           
                        }
                    }  
                    
                    writer.write(NEW_LINE);

                    if (includeTimeCodes) {
                    	if (includeLabels) {
                            writer.write((String) marginStrings.get(tcLabel));
                        }                   
                        writer.write(TimeFormatter.toString(record.getBeginTime()));
                        writer.write(TIME_SEP);
                        writer.write(TimeFormatter.toString(record.getEndTime()));
                        writer.write(NEW_LINE);
                        if (includeSilence && record.getSilenceAfter() != null) {
                            if (includeLabels) {
                                writer.write((String) marginStrings.get(
                                        emptyLabel));    
                            }
                            writer.write("(" + record.getSilenceAfter() + ")");
                            writer.write(NEW_LINE);
                        }
                        if (insertEmptyLine){
                        	writer.write(NEW_LINE);
                        }
                    } else if (includeSilence) {                    	
                        if (record.getSilenceAfter() != null) {
    	                    if (includeLabels) {
    	                        writer.write((String) marginStrings.get(
    	                                emptyLabel));                       
    	                    }
    	                    writer.write("(" + record.getSilenceAfter() + ")");
    	                    writer.write(NEW_LINE);
                        }
                        if (insertEmptyLine){
                        	writer.write(NEW_LINE);
                        }
                    } else if (insertEmptyLine){
                    	writer.write(NEW_LINE);
                    }
                }
        		
        	}
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            // FileNotFound, IO, Security, Null etc
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportDialog.Message.Error"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception ee) {
            }
        }

        return true;
    }

    /**
     * Formats a long value in ms as a string in seconds with 2 decimals.
     *  
     * @param dur the duration in ms
     * @return a string in seconds
     */
    private String formatSilenceString(long dur) {
        if (dur <= 0) {
            return null;
        }
        
        String silDur = null;
        
        int decimal = ((Integer) silenceDecimalComboBox.getSelectedItem()).intValue();
        
        if(decimal == Constants.ONE_DIGIT){     			              			  
		   silDur = String.valueOf(Math.round(dur/100f) / 10f); 
		} else if (decimal == Constants.TWO_DIGIT){
		   silDur = String.valueOf(Math.round(dur/10) / 100f);              				   
		} else if (decimal ==Constants. THREE_DIGIT){
		   silDur = String.valueOf(Math.round(dur) / 1000f);              				   
		} 
        
        return silDur;
    }

    //***********************
    // inner classes
    //***********************	

    /**
     * A class that extends AnnotationDataRecord with an index,  that denotes
     * its position in the tier output order  and that implements Comparable.<br>
     * Note: this class has a natural ordering that is inconsistent with
     * equals.
     * @version Apr 2007 added field for the 'silence after this annotation' value
     * @author Han Sloetjes
     */
    private class IndexedExportRecord extends AnnotationDataRecord
        implements Comparable {
        private int index;
        private String silAfter;
        private boolean childAnn;

        /**
         * Constructor.
         *
         * @param annotation the annotation
         * @param index the index in the tier order
         */
        IndexedExportRecord(Annotation annotation, int index) {
        	super(annotation);
            this.index = index;         
        }       

        /**
         * Returns the index in the tier order.
         *
         * @return the index in the tier order
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the duration of silence between this record and the next one.
         * 
         * @param value the duration between this and the next annotation
         */
        public void setSilenceAfter(String value) {
            silAfter = value;
        }
        
        /**
         * Returns the duration of silence between this record and the next one.
         * 
         * @return the duration of silence between this record and the next one
         */
        public String getSilenceAfter() {
            return silAfter;
        }
        
        /**
         * Performs a two step comparison: <br>
         * - compare the begintimes - when they are the same, compare the
         * index
         *
         * @param o the object to compare with
         *
         * @return a negative integer, zero, or a positive integer as this
         *         object is less than,  equal to, or greater than the
         *         specified object
         *
         * @throws ClassCastException if the parameter is not an
         *         IndexedExportRecord
         */
        public int compareTo(Object o) throws ClassCastException {
            if (!(o instanceof IndexedExportRecord)) {
                throw new ClassCastException(
                    "Object is not an IndexedExportRecord");
            }

            IndexedExportRecord other = (IndexedExportRecord) o;

            if (this.getBeginTime() < other.getBeginTime()) {
                return -1;
            } else if (this.getBeginTime() > other.getBeginTime()) {
                return 1;
            }  
            
            if(this.getEndTime() == other.getEndTime()){
            	 if (this.index < other.getIndex()) {
                     return -1;
                 } else if (this.index > other.getIndex()) {
                     return 1;
                 }
            }
            
            //if begin time is same child if it is a child annotation child ann
            TierImpl thisTier = (TierImpl) transcription.getTierWithId(this.getTierName());
            TierImpl otherTier = (TierImpl) transcription.getTierWithId(other.getTierName());
            
            //check if thisTier is a child of otherTier
           List childTiers = thisTier.getChildTiers();
           if(childTiers != null && childTiers.contains(otherTier)){
        	   return -1;
           }
           
           childTiers = otherTier.getChildTiers();
           if(childTiers != null && childTiers.contains(thisTier)){
        	   return 1;
           }

            if (this.getEndTime() < other.getEndTime()) {
                return -1;
            } else if (this.getEndTime() > other.getEndTime()) {
                return 1;
            }
            
            return 0;
        }
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Object useTyp = Preferences.get("ExportTradTranscript.rootTiersCB", null);
    
    	if(useTyp != null){
    		setRootTiersOnly((Boolean)useTyp); 
    	}
    	
    	useTyp = Preferences.get("ExportTradTranscript.wrapLinesCB", null);
    	if(useTyp != null){
    		wrapLinesCB.setSelected((Boolean)useTyp); 
    	}
    	
    	//the preference string is changed from minimalDurTF to numCharPerLine
    	useTyp = Preferences.get("ExportTradTranscript.minimalDurTF", null);
    	if(useTyp != null){
    		Preferences.set("ExportTradTranscript.numCharPerLine", useTyp.toString(), null);
    		Preferences.set("ExportTradTranscript.minimalDurTF", null, null);
    	} 
    	
    	useTyp = Preferences.get("ExportTradTranscript.numCharPerLine", null);
    	if(useTyp != null){
    		numCharTF.setText(useTyp.toString()); 
    	}
    	
    	useTyp = Preferences.get("ExportTradTranscript.timeCodeCB", null);
    	if(useTyp != null){
    		timeCodeCB.setSelected((Boolean)useTyp); 
    	}
    	
    	 useTyp = Preferences.get("ExportTradTranscript.MergeAnnotations", null);
     	if(useTyp != null){
     		mergeAnnCB.setSelected((Boolean)useTyp); 
     	}
         
        useTyp = Preferences.get("ExportTradTranscript.MergeDurationValue", null);
     	if(useTyp != null){
     		mergeDurTF.setText(useTyp.toString()); 
     	}
     	
     	useTyp = Preferences.get("ExportTradTranscript.labelsCB", null);
    	if(useTyp != null){
    		labelsCB.setSelected((Boolean)useTyp); 
    	}
    	
    	useTyp = Preferences.get("ExportTradTranscript.FixedLabelWidth", null);
    	if(useTyp != null){
    		labelWidthCB.setSelected((Boolean)useTyp); 
    	}
    	
    	useTyp = Preferences.get("ExportTradTranscript.FixedLabelWidthValue", null);
    	if(useTyp != null){
    		labelWidthTF.setText(useTyp.toString()); 
    	}  
    	
    	useTyp = Preferences.get("ExportTradTranscript.IncludeEmptyLines", null);
    	if(useTyp != null){
    		emptyLineCB.setSelected((Boolean)useTyp); 
    	}
    	     
    	useTyp = Preferences.get("ExportTradTranscript.silenceCB", null);
    	if(useTyp != null){
    		silenceCB.setSelected((Boolean)useTyp); 
    	}
    	
    	useTyp = Preferences.get("ExportTradTranscript.minDurSilTF", null);
    	if(useTyp != null){
    		minDurSilTF.setText(useTyp.toString()); 
    	}
    	
    	useTyp = Preferences.get("NumberOfDecimalDigits", null);    	
        if (useTyp instanceof Integer) {
        	silenceDecimalComboBox.setSelectedItem(((Integer) useTyp).intValue());
        }
        
    	useTyp = Preferences.get("ExportTradTranscript.selectionCB", null);
    	if(useTyp != null){
    		selectionCB.setSelected((Boolean)useTyp); 
    	}
       
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportTradTranscript.rootTiersCB", isRootTiersOnly(), null); 
    	Preferences.set("ExportTradTranscript.wrapLinesCB", wrapLinesCB.isSelected(), null);
    	if (numCharTF.getText() != null){
    		Preferences.set("ExportTradTranscript.numCharPerLine", numCharTF.getText().trim(), null);
    	}
    	Preferences.set("ExportTradTranscript.timeCodeCB", timeCodeCB.isSelected(), null);
    	Preferences.set("ExportTradTranscript.MergeAnnotations", mergeAnnCB.isSelected(), null);    	
    	if (mergeDurTF.getText() != null){
    		Preferences.set("ExportTradTranscript.MergeDurationValue", mergeDurTF.getText(), null);
    	}    
    	Preferences.set("ExportTradTranscript.labelsCB", labelsCB.isSelected(), null);
    	Preferences.set("ExportTradTranscript.FixedLabelWidth", labelWidthCB.isSelected(), null);
    	if (labelWidthTF.getText() != null){
    		Preferences.set("ExportTradTranscript.FixedLabelWidthValue", labelWidthTF.getText().trim(), null);
    	}
    	Preferences.set("ExportTradTranscript.IncludeEmptyLines", emptyLineCB.isSelected(), null);
    	Preferences.set("ExportTradTranscript.silenceCB",silenceCB.isSelected(), null);    	
    	if (minDurSilTF.getText() != null){
    		Preferences.set("ExportTradTranscript.minDurSilTF", minDurSilTF.getText().trim(), null);
    	}    	    	
    	Preferences.set("NumberOfDecimalDigits", silenceDecimalComboBox.getSelectedItem(), null);   
    	Preferences.set("ExportTradTranscript.selectionCB", selectionCB.isSelected(), null);
    	
    	Preferences.set("ExportTradTranscript.selectedTiers", getSelectedTiers(), transcription);   
    	Preferences.set("ExportTradTranscript.SelectTiersMode", getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set("ExportTradTranscript.LastSelectedItems", getSelectedItems(), transcription);
    	}
    	Preferences.set("ExportTradTranscript.HiddenTiers", getHiddenTiers(), transcription);
    	
    	List tierOrder = getTierOrder();
    	Preferences.set("ExportTradTranscript.TierOrder", tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
        		if (rootTiersCB.isSelected()) {
        			Preferences.set("ExportTradTranscript.ParentTierOrder", currentTierOrder, transcription);
        		}
        		else {
        			Preferences.set("ExportTradTranscript.TierOrder", currentTierOrder, transcription);
        		}
        		break;
    		}
    	}
    	*/    	
    }
}
