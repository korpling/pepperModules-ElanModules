package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A dialog for the tokenization of annotations on one (parent) tier  on
 * another (dependent) subdivision tier.<br>
 * Tokenization means that for  every single token (word) in one parent
 * annotation, a new annotation on a  a dependent tier is being created.
 * e.g.<br>
 * <pre>
 * |this is a test|<br>
 * |this|is|a|test|
 * </pre>
 * The default delimiters are space, tab, newline, (carriagereturn and
 * formfeed). The user can specify other delimiters.
 *
 * @author Han Sloetjes
 * @version jul 2004
 * @version Aug 2005 Identity removed
 */
public class TokenizeDialog extends AbstractTwoTierOpDialog
    implements ActionListener, ItemListener, ChangeListener {
    private JRadioButton customDelimRB;
    private JLabel tokenDelimLabel;
    private JPanel extraOptionsPanel;
    private JRadioButton defaultDelimRB;
    private JTextField customDelimField;
    private ButtonGroup delimButtonGroup;

    /** Holds value of property DOCUMENT ME! */
    private final char[] DEF_DELIMS = new char[] { '\t', '\n', '\r', '\f' };

    /**
     * Creates a new tokenizer dialog.
     *
     * @param transcription the transcription
     */
    public TokenizeDialog(Transcription transcription) {
        super(transcription);

        //initComponents();
        initOptionsPanel();
        updateLocale();
        loadPreferences();
        //extractSourceTiers();
        postInit();
    }

    /**
     * Extracts the candidate destination tiers for the currently selected
     * source tier.<br>
     * The destination tier must be a direct child of the source  and must be
     * of type time-subdivision or symbolic-subdivision.
     */
    protected void extractDestinationTiers() {
        destTierComboBox.removeAllItems();
        destTierComboBox.addItem(EMPTY);

        if ((sourceTierComboBox.getSelectedItem() != null) &&
                (sourceTierComboBox.getSelectedItem() != EMPTY)) {
            String name = (String) sourceTierComboBox.getSelectedItem();
            TierImpl source = (TierImpl) transcription.getTierWithId(name);

            Vector depTiers = source.getDependentTiers();
            Iterator tierIt = depTiers.iterator();
            TierImpl dest = null;
            LinguisticType lt = null;

            while (tierIt.hasNext()) {
                dest = (TierImpl) tierIt.next();
                lt = dest.getLinguisticType();

                if ((dest.getParentTier() == source) &&
                        ((lt.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) ||
                        (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION))) {
                    destTierComboBox.addItem(dest.getName());
                }
            }
            if (destTierComboBox.getItemCount() > 1) {
            	destTierComboBox.removeItem(EMPTY);
            }
        }
    }

    /**
     * Performs some checks and starts the tokenization process.
     */
    protected void startOperation() {
        // do some checks, spawn warning messages
        String sourceName = (String) sourceTierComboBox.getSelectedItem();
        String destName = (String) destTierComboBox.getSelectedItem();
        String delimsText = null;
        boolean preserveExisting = preserveRB.isSelected();
        boolean createEmptyAnnotations = emptyAnnCheckBox.isSelected();

        if ((sourceName == EMPTY) || (destName == EMPTY)) {
            //warn and return...
            showWarningDialog(ElanLocale.getString(
                    "TokenizeDialog.Message.InvalidTiers"));

            return;
        }

        if (customDelimRB.isSelected()) {
            // check if there is a valid tokenizer
            delimsText = customDelimField.getText();

            if ((delimsText == null) || (delimsText.length() == 0)) {
                showWarningDialog(ElanLocale.getString(
                        "TokenizeDialog.Message.NoDelimiter"));

                return;
            }

            // be sure tab and newline characters are part of the delimiter
            delimsText = checkDelimiters(delimsText);
        }
        storePreferences();
        // if we get here we can start working...
        //need a command because of undo / redo mechanism
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.TOKENIZE_TIER);
        Object[] args = new Object[5];
        args[0] = sourceName;
        args[1] = destName;
        args[2] = delimsText;
        args[3] = new Boolean(preserveExisting);
        args[4] = new Boolean(createEmptyAnnotations);
        com.execute(transcription, args);
    }

    /**
     * Ensures that some default characters are part of the delimiter string.
     *
     * @param delim the string to check
     *
     * @return new delimiter string
     */
    private String checkDelimiters(String delim) {
        StringBuffer buffer = new StringBuffer(delim);

        for (int i = 0; i < DEF_DELIMS.length; i++) {
            if (delim.indexOf(DEF_DELIMS[i]) < 0) {
                buffer.append(DEF_DELIMS[i]);
            }
        }

        return buffer.toString();
    }

    /**
     * Initializes UI elements.
     */
    protected void initOptionsPanel() {
        GridBagConstraints gridBagConstraints;

        extraOptionsPanel = new JPanel();
        delimButtonGroup = new ButtonGroup();
        tokenDelimLabel = new JLabel();
        defaultDelimRB = new JRadioButton();
        customDelimRB = new JRadioButton();
        customDelimField = new JTextField();

        Insets insets = new Insets(2, 0, 2, 6);

        extraOptionsPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(tokenDelimLabel, gridBagConstraints);

        defaultDelimRB.setSelected(true);
        defaultDelimRB.addChangeListener(this);
        delimButtonGroup.add(defaultDelimRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(defaultDelimRB, gridBagConstraints);

        customDelimRB.addChangeListener(this);
        delimButtonGroup.add(customDelimRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customDelimRB, gridBagConstraints);

        customDelimField.setEnabled(false);
        customDelimField.setColumns(6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customDelimField, gridBagConstraints);

        addOptionsPanel(extraOptionsPanel);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("TokenizeDialog.Title"));
        titleLabel.setText(ElanLocale.getString("TokenizeDialog.Title"));

        //explanatoryTA.setText(ElanLocale.getString("TokenizeDialog.Explanation"));
        tokenDelimLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.TokenDelimiter"));
        defaultDelimRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Default"));
        customDelimRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Custom"));
    }
    
    /**
     * Stores choices as preferences.
     */
    private void storePreferences() {
    	if (defaultDelimRB.isSelected()) {
    		Preferences.set("TokenizeDialog.DefaultDelimiter", true, null, false, false);
    	} else {
    		Preferences.set("TokenizeDialog.DefaultDelimiter", false, null, false, false);
    		Preferences.set("TokenizeDialog.CustomDelimiter", customDelimField.getText(), null, false, false);
    	}
    	Preferences.set("TokenizeDialog.Overwrite", overwriteRB.isSelected(), null, false, false);
    	Preferences.set("TokenizeDialog.ProcessEmptyAnnotations", emptyAnnCheckBox.isSelected(), null, false, false);
    }
    
    /**
     * Restores choices as preferences.
     */
    private void loadPreferences() {
    	Object val = null;
    	
    	val = Preferences.get("TokenizeDialog.DefaultDelimiter", null);
    	if (val instanceof Boolean) {
    		boolean defde = ((Boolean) val).booleanValue();
    		if (!defde) {
    			customDelimRB.setSelected(true);
    			val = Preferences.get("TokenizeDialog.CustomDelimiter", null);
    			if (val instanceof String) {
    				customDelimField.setText((String) val);
    			}
    		}
    	}
    	val = Preferences.get("TokenizeDialog.Overwrite", null);
    	if (val instanceof Boolean) {
    		boolean overwr = ((Boolean) val).booleanValue();
    		if (overwr) {
    			overwriteRB.setSelected(true);
    		} else {
    			preserveRB.setSelected(true);
    		}
    	}
    	val = Preferences.get("TokenizeDialog.ProcessEmptyAnnotations", null);
    	if (val instanceof Boolean) {
    		emptyAnnCheckBox.setSelected((Boolean) val);
    	}
    }

    /**
     * The state changed event handling.
     *
     * @param ce the change event
     */
    public void stateChanged(ChangeEvent ce) {
        if (defaultDelimRB.isSelected()) {
            customDelimField.setEnabled(false);
        } else {
            customDelimField.setEnabled(true);
            customDelimField.requestFocus();
        }
    }
}
