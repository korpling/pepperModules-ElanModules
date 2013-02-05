/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.Transcription2Tiger;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * $Id: ExportTigerDialog.java 27113 2011-10-31 13:53:04Z aarsom $ Dialog
 * for selecting tiers whose annotations will be exported into the Tiger
 * Syntax Format (as leaf nodes) In "Tiger-terminology": annotations will
 * become feature values of terminal nodes
 *
 * @author $Author$
 * @version $Revision$
 */
public class ExportTigerDialog extends AbstractTierExportDialog
    implements ListSelectionListener {
    private JPanel textFieldPanel;
    private JTextField[] featureTextFields = new JTextField[0];

    /**
     * Creates a new ExportTigerDialog object.
     *
     * @param parent DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param transcription DOCUMENT ME!
     * @param selection DOCUMENT ME!
     */
    public ExportTigerDialog(Frame parent, boolean modal,
        Transcription transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        updateFeatureTextFields();
        postInit();
    }

    /**
     * for debugging
     *
     * @param args no args
     */
    public static void main(String[] args) {
        String filename = "resources/testdata/elan/elan-example2.eaf";
        Transcription transcription = new TranscriptionImpl(filename);

        JFrame frame = new JFrame();
        javax.swing.JDialog dialog = new ExportTigerDialog(frame, false,
                transcription, null);
        dialog.setVisible(true);
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model.findColumn(EXPORT_COLUMN);

            for (int i = b; i <= e; i++) {
                if (tierTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }

            updateFeatureTextFields();
        }
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {
                List v = getSentenceTiers(transcription);
                Tier t;

                for (int i = 0; i < v.size(); i++) {
                    t = (TierImpl) v.get(i);

                    // add all
                    model.addRow(new Object[] { Boolean.TRUE, t.getName() });
                }
            }
            
          //read Preferences
            Object useTyp = Preferences.get("ExportTigerDialog.selectedTiers", transcription);
            if (useTyp instanceof ArrayList) {
            	loadTierPreferences((ArrayList)useTyp);        	
          	 } 
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void makeLayout() {
        super.makeLayout();
        
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        
        model.setColumnIdentifiers(new String[] { EXPORT_COLUMN, TIER_NAME_COLUMN });
        tierTable.getColumn(EXPORT_COLUMN).setCellEditor(new DefaultCellEditor(
                new JCheckBox()));
        tierTable.getColumn(EXPORT_COLUMN).setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumn(EXPORT_COLUMN).setMaxWidth(30);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        optionsPanel.add(new JSeparator(), gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        optionsPanel.add(new JLabel("Features:"), gridBagConstraints);

        textFieldPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        optionsPanel.add(textFieldPanel, gridBagConstraints);

        tierTable.getSelectionModel().addListSelectionListener(this);
        setPreferredSetting();
        updateLocale();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected boolean startExport() throws IOException {
    	
        List selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        long selectionBT = 0L;
        long selectionET = Long.MAX_VALUE;

        if (restrictCheckBox.isSelected()) {
            selectionBT = selection.getBeginTime();
            selectionET = selection.getEndTime();
        }

        String[] features = new String[featureTextFields.length];

        for (int i = 0; i < featureTextFields.length; i++) {
            features[i] = featureTextFields[i].getText();
        }

        // tiers should be presented in ordered form -> LinkedHashMap
        HashMap sentenceTierHash = new LinkedHashMap();

        // add selected tiers in the right order
        for (int i = 0; i < selectedTiers.size(); i++) {
            Tier sentenceTier = transcription.getTierWithId((String) selectedTiers.get(
                        i));
            List featureTiers = getFeatureTiers(sentenceTier);
            HashMap featureHash = new LinkedHashMap();

            for (int j = 0; j < featureTiers.size(); j++) {
                featureHash.put(featureTiers.get(j), features[j]);
            }

            sentenceTierHash.put(sentenceTier, featureHash);
        }

        //show only if there is a choice
        if (features.length > 1) {
            if (ExportTigerFeatureCheckPane.showFeatureCheckPane(this,
                        sentenceTierHash, features) == ExportTigerFeatureCheckPane.CANCEL_OPTION) {
                return false;
            }
        }

        //prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                    "Export.TigerDialog.title"), null,
                    FileExtension.TIGER_EXT, true);

        if (exportFile == null) {
            return false;
        }

        Transcription2Tiger.exportTiers(transcription, sentenceTierHash,
            exportFile, encoding, selectionBT, selectionET);

        return true;
    }

    /**
     * DOCUMENT ME!
     */
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportTigerDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTigerDialog.TitleLabel"));
    }

    /**
     * DOCUMENT ME!
     *
     * @param sentenceTier
     *
     * @return ordered HashMap of dependent Tiers with Constraint
     *         TIME_SUBDIVISION resp. SYMBOLIC_ASSOCIATION, containing (empty)
     *         features as values;
     */
    private static List getFeatureTiers(Tier sentenceTier) {
        List featureTiers = new ArrayList();
        List childTiers = ((TierImpl) sentenceTier).getChildTiers();

        for (int j = 0; j < childTiers.size(); j++) {
            Tier childTier = (Tier) childTiers.get(j);

            if (((TierImpl) childTier).getLinguisticType().getConstraints()
                     .getStereoType() == Constraint.TIME_SUBDIVISION) {
                featureTiers.add(childTier);

                addDescendantFeatureTiers(featureTiers, childTier);
            }
        }

        return featureTiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param transcription
     *
     * @return list of all tiers that have no constraints and a child with
     *         constraint TIME_SUBDIVISION
     */
    private static List getSentenceTiers(Transcription transcription) {
        //tiers without a constraint
        List noConstraintTiers = new ArrayList();

        //tiers without a constraint that have a child with constraint TIME_SUBDIVISION
        List sentenceTiers = new ArrayList();

        List lingTypes = transcription.getLinguisticTypes();

        for (int i = 0; i < lingTypes.size(); i++) {
            LinguisticType lingType = (LinguisticType) lingTypes.get(i);

            if (lingType.getConstraints() == null) {
                noConstraintTiers.addAll(transcription.getTiersWithLinguisticType(
                        ((LinguisticType) lingTypes.get(i)).getLinguisticTypeName()));
            }
        }

        for (int i = 0; i < noConstraintTiers.size(); i++) {
            List childTiers = ((TierImpl) noConstraintTiers.get(i)).getChildTiers();

            boolean containsWordTier = false;

            for (int j = 0; j < childTiers.size(); j++) {
                Tier childTier = (Tier) childTiers.get(j);
                Constraint constraint = ((TierImpl) childTier).getLinguisticType()
                                         .getConstraints();

                if ((constraint != null) &&
                        (constraint.getStereoType() == Constraint.TIME_SUBDIVISION)) {
                    containsWordTier = true;

                    break;
                }
            }

            if (containsWordTier) {
                sentenceTiers.add(noConstraintTiers.get(i));
            }
        }

        return sentenceTiers;
    }

    /**
     * adds descendent tiers with constraint SYMBOLIC ASSOCIATION to hashMap
     * (with empty features as values)
     *
     * @param featureTiers
     * @param tier
     */
    private static void addDescendantFeatureTiers(List featureTiers, Tier tier) {
        List childTiers = ((TierImpl) tier).getChildTiers();

        for (int k = 0; k < childTiers.size(); k++) {
            Tier childTier = (Tier) childTiers.get(k);

            if (((TierImpl) childTier).getLinguisticType().getConstraints()
                     .getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                featureTiers.add(childTier);

                addDescendantFeatureTiers(featureTiers, childTier);
            }
        }
    }

    /**
     * depending on which sentence tiers are selected, the number possible
     * features may vary.
     */
    private void updateFeatureTextFields() {
        int includeCol = model.findColumn(EXPORT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        int maxFeatures = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                try {
                    Tier sentenceTier = transcription.getTierWithId((String) model.getValueAt(
                                i, nameCol));
                    maxFeatures = Math.max(maxFeatures,
                            getFeatureTiers(sentenceTier).size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (maxFeatures != featureTextFields.length) {
            textFieldPanel.removeAll();
            featureTextFields = new JTextField[maxFeatures];

            for (int i = 0; i < maxFeatures; i++) {
                featureTextFields[i] = new JTextField((i < Transcription2Tiger.defaultFeatureNames.length)
                        ? Transcription2Tiger.defaultFeatureNames[i] : "", 10);
                textFieldPanel.add(featureTextFields[i]);
            }

            featureTextFields[0].setEnabled(false);
            textFieldPanel.revalidate();
        }
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    
    private void setPreferredSetting()
    {
    	Object useTyp = Preferences.get("ExportTigerDialog.restrictCheckBox", null);    
    	if(useTyp != null){
    		restrictCheckBox.setSelected((Boolean)useTyp); 
    	}	
     
    	useTyp = Preferences.get("ExportTigerDialog.featureTextFields", transcription);
    	if(useTyp instanceof ArrayList){
    		//if (((List) useTyp).size() == featureTextFields.length) {
    			textFieldPanel.removeAll();
    			featureTextFields = new JTextField[((List) useTyp).size()];
    			for (int i = 0; i < ((List) useTyp).size(); i++) {
    				featureTextFields[i] = new JTextField((String) ((List)useTyp).get(i));                    
    				textFieldPanel.add(featureTextFields[i]);
    			}
    		//}  
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
    */    
    private void savePreferences(){
    	Preferences.set("ExportTigerDialog.restrictCheckBox", restrictCheckBox.isSelected(), null); 
    	
    	List features = new ArrayList();
    	for (int i = 0; i < featureTextFields.length; i++) {
            features.add( featureTextFields[i].getText());            
        }
    	Preferences.set("ExportTigerDialog.featureTextFields", features, transcription);
    	Preferences.set("ExportTigerDialog.selectedTiers", getSelectedTiers(), transcription);
    }
}
