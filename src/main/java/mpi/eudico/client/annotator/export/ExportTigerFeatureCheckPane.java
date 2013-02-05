package mpi.eudico.client.annotator.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.server.corpora.clom.Tier;


/**
 * $Id: ExportTigerFeatureCheckPane.java 15028 2009-05-07 11:26:09Z hasloe $
 *
 * @author $author$
 * @version $Revision$
 */
public class ExportTigerFeatureCheckPane extends JDialog {
    /** Return value from class method if YES is chosen. */
    public static final int YES_OPTION = 0;

    /** Return value from class method if CANCEL is chosen. */
    public static final int CANCEL_OPTION = 2;
    
    /** stores value of option */
    private int option = CANCEL_OPTION;

    /**
     * Creates a new ExportTigerFeatureCheckPane.
     *
     * @param parent DOCUMENT ME!
     * @param sentenceTierHash DOCUMENT ME!
     * @param features DOCUMENT ME!
     */
    private ExportTigerFeatureCheckPane(JDialog parent,
        final HashMap sentenceTierHash, final String[] features) {
        super(parent, true);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
                new TitledBorder(ElanLocale.getString("ExportTigerDialog.FeaturePane.Title"))));

        for (Iterator it1 = sentenceTierHash.keySet().iterator();
                it1.hasNext();) {
            Tier sentenceTier = (Tier) it1.next();
            HashMap featureHash = (HashMap) sentenceTierHash.get(sentenceTier);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 15, 5, 15);

            JLabel sentenceTierLabel = new JLabel(sentenceTier.getName() +
                    ": ");
            c.gridx = 0;
            c.weightx = 0.25;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.BOTH;
            mainPanel.add(sentenceTierLabel, c);

            HashMap tierComboBoxHash = new HashMap();
            int count = 0;

            for (Iterator it2 = featureHash.keySet().iterator(); it2.hasNext();) {
                Tier tier = (Tier) it2.next();
                JLabel tierLabel = new JLabel(tier.getName());
                c.gridx = 1;
                c.insets = new Insets(5, 5, 5, 5);
                mainPanel.add(tierLabel, c);

                c.weightx = 0;
                c.gridx = 2;
                mainPanel.add(new JLabel("->"), c);

                JComboBox featureComboBox = new JComboBox(features);
                featureComboBox.setSelectedItem(features[count]);
                featureHash.put(tier, features[count]);
                count++;

                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 3;
                mainPanel.add(featureComboBox, c);

                featureComboBox.addItemListener(new MyItemListener(
                        featureHash, tierComboBoxHash));
                tierComboBoxHash.put(tier, featureComboBox);
            }

            if (it1.hasNext()) {
                c.gridx = 0;
                c.weightx = 1.0;
                c.gridwidth = 4;
                mainPanel.add(new JSeparator(), c);
            }
        }

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        JButton startButton = new JButton(ElanLocale.getString("Button.OK"));
        startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        for (Iterator it = sentenceTierHash.values().iterator();
                                it.hasNext();) {
                            if (!((HashMap) it.next()).values().contains(features[0])) {
                                JOptionPane.showMessageDialog(ExportTigerFeatureCheckPane.this,
                                    ElanLocale.getString(
                                        "ExportTigerDialog.FeaturePane.ErrorMessagePart1") +
                                    " '" + features[0] + "' "+
                                    ElanLocale.getString(
                                        "ExportTigerDialog.FeaturePane.ErrorMessagePart2")+"!",
                                    ElanLocale.getString("Message.Error"),
                                    JOptionPane.ERROR_MESSAGE);

                                return;
                            }
                        }

                        option = YES_OPTION;
                        dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        JButton closeButton = new JButton(ElanLocale.getString("Button.Cancel"));
        closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 0));
        JPanel bufferPanel = new JPanel();
        bufferPanel.add(buttonPanel);
        buttonPanel.add(startButton);
        buttonPanel.add(closeButton);

        getContentPane().add(bufferPanel, BorderLayout.SOUTH);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * Functions like JOptionPane
     *
     * @param parent DOCUMENT ME!
     * @param sentenceTierHash DOCUMENT ME!
     * @param features DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int showFeatureCheckPane(JDialog parent,
        HashMap sentenceTierHash, String[] features) {
        ExportTigerFeatureCheckPane pane = new ExportTigerFeatureCheckPane(parent,
                sentenceTierHash, features);
        pane.setVisible(true);

        return pane.option;
    }

    /**
     * actualizes model (HashMaps) when item changed; 
     * assures that no two JComboBoxes have the same selected item.
     * 
     * @author klasal
     *
     */
    class MyItemListener implements ItemListener {
        private final HashMap tierComboBoxHash;
        private final HashMap tierFeatureHash;
        private String deselectedFeature;

        public MyItemListener(HashMap tierFeatureHash, HashMap tierComboBoxHash) {
            this.tierFeatureHash = tierFeatureHash;
            this.tierComboBoxHash = tierComboBoxHash;
        }

        /**
         * update other comboboxes in order to avoid features being selected
         * twice
         *
         * @param e DOCUMENT ME!
         */
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                deselectedFeature = (String) e.getItem();
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
                if (deselectedFeature != null) {
                    for (Iterator it = tierFeatureHash.keySet().iterator();
                            it.hasNext();) {
                        Object tier = it.next();
                        JComboBox comboBox = (JComboBox) tierComboBoxHash.get(tier);

                        if (comboBox == e.getSource()) {
                            tierFeatureHash.put(tier, e.getItem());
                        } else {
                            if (comboBox.getSelectedItem().equals(e.getItem())) {
                                ItemListener[] its = comboBox.getItemListeners();
                                comboBox.removeItemListener(its[0]);
                                comboBox.setSelectedItem(deselectedFeature);
                                comboBox.addItemListener(its[0]);
                                tierFeatureHash.put(tier, deselectedFeature);
                            }
                        }
                    }
                }
            }
        }
    }
}
