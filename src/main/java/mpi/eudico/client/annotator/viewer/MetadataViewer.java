package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesUser;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.md.DefaultMDViewerComponent;
import mpi.eudico.client.annotator.md.MDConfigurationDialog;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDServiceRegistry;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * A viewer that is able to display metadata information relevant for the
 * current transcription document.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class MetadataViewer extends JPanel implements PreferencesUser,
    ElanLocaleListener, Viewer, ActionListener {
    private ViewerManager2 viewerManager;
    private String metadataPath;
    private MDServiceProvider mdProvider;
    private MDViewerComponent viewerPanel;

    // ui elements
    private JButton selectMDButton;
    private JButton configureMDButton;
    private JLabel mdPathLabel;
    private JPanel viewerContainer;

    /** constant for metadata source */
    public final String MD_SOURCE = "MetadataSource";

    /** constant for metadata keys */
    public final String MD_KEYS = "MetadataKeys";

    /**
     * Creates a new MetadataViewer instance
     *
     * @param viewerManager the viewer manager
     */
    public MetadataViewer(ViewerManager2 viewerManager) {
        super();
        this.viewerManager = viewerManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        selectMDButton = new JButton();
        mdPathLabel = new JLabel();
        mdPathLabel.setFont(mdPathLabel.getFont().deriveFont(10f));
        configureMDButton = new JButton();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(selectMDButton, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 6, 0, 0);
        add(mdPathLabel, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(configureMDButton, gbc);

        updateLocale();
        selectMDButton.addActionListener(this);
        configureMDButton.addActionListener(this);
        configureMDButton.setEnabled(false);
        mdPathLabel.setText(ElanLocale.getString(
                "MetadataViewer.NoMetadataSource"));

        viewerContainer = new JPanel(new GridBagLayout());
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 0, 0, 0);
        add(viewerContainer, gbc);
    }

    /**
     * Provides the means to select a source for the metadata.
     */
    private void selectMDSource() {
        FileChooser chooser = new FileChooser(this);
        

        String dir = (String) Preferences.get("MetadataFileDir", viewerManager.getTranscription());

        if (dir == null) {
            // start in the directory of the eaf file
            String eafPath = ((TranscriptionImpl) viewerManager.getTranscription()).getFullPath();

            if ((eafPath != null) &&
                    (eafPath.indexOf(TranscriptionImpl.UNDEFINED_FILE_NAME) == -1)) {
                if (eafPath.startsWith("file:")) {
                    eafPath = eafPath.substring(5);
                }

                int lastSep = eafPath.lastIndexOf(File.separator);

                if (lastSep > 0) {
                    eafPath = eafPath.substring(0, lastSep);
                }
                dir = eafPath;
            } 
        }
        chooser.setCurrentDirectory(dir);
        chooser.createAndShowFileDialog(ElanLocale.getString("Button.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.IMDI_EXT, false, "MetadataFileDir", FileChooser.FILES_ONLY, null);

        File selected = chooser.getSelectedFile();
        if ((selected != null) ) {
            String selectedFilePath = selected.getAbsolutePath();
            mdPathLabel.setText(selectedFilePath);
           
            if (!selectedFilePath.equals(metadataPath)) {
                metadataPath = selectedFilePath;
                setPreference(MD_SOURCE, metadataPath,
                	viewerManager.getTranscription());
                mdProvider = MDServiceRegistry.getInstance()
                                              .getProviderForMDFile(metadataPath);
                
                if (mdProvider != null) {
                    configureMDButton.setEnabled(true);
                    MDViewerComponent oldPanel = viewerPanel;
                    viewerPanel = mdProvider.getMDViewerComponent();

                    if (viewerPanel == null) {
                        viewerPanel = new DefaultMDViewerComponent(mdProvider);
                    }
                    // remove the old one
                    if (oldPanel instanceof Component) {
                    	viewerContainer.remove((Component) oldPanel);
                    }
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.anchor = GridBagConstraints.NORTHWEST;
                    gbc.fill = GridBagConstraints.BOTH;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;
                    viewerContainer.add((Component) viewerPanel, gbc);
                    
                 // check if there are default settings, special IMDI case
                    if ("IMDI".equals(mdProvider.getMDFormatDescription())) {
                        Object val = Preferences.get("Metadata.IMDI.Defaults", null);
                        if (val instanceof List) {
                        	List prefKeys = (List) val;
                        	mdProvider.setSelectedKeys(prefKeys);
                            Map allSelKeysVals = mdProvider.getSelectedKeysAndValues();

                            if (allSelKeysVals != null) {
                                if (viewerPanel != null) {
                                    viewerPanel.setSelectedKeysAndValues(allSelKeysVals);
                                }
                            }
                        }
                    }
                }
            }
        }
    }             

    /**
     * Allows selection of metadata keys and values to display.
     */
    private void configureMD() {
        if (mdProvider == null) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("MetadataViewer.NoMetadataSource"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }

        // show configure dialog
        MDConfigurationDialog dialog = new MDConfigurationDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), 
        		mdProvider.getConfigurationPanel());
        dialog.setVisible(true);

        List selKeys = mdProvider.getSelectedKeys();

        if ((selKeys == null) || (selKeys.size() == 0)) {
            return;
        }

        // store preferences
        setPreference(MD_KEYS, selKeys, viewerManager.getTranscription());

        /*
           int rowCount = model.getRowCount();
           for (int i = rowCount - 1; i >= 0; i--) {
               model.removeRow(i);
           }
         */
        Map allSelKeysVals = mdProvider.getSelectedKeysAndValues();

        if (allSelKeysVals != null) {
            if (viewerPanel != null) {
                viewerPanel.setSelectedKeysAndValues(allSelKeysVals);
            }
        }

        /*
           Iterator keyIt = allSelKeysVals.keySet().iterator();
           Object key, val;
           while (keyIt.hasNext()) {
               key = keyIt.next();
               // hier... store key in preferences
               val = allSelKeysVals.get(key);
               //System.out.println("K: " + key + "\n\tV: " + val);
               model.addRow(new Object[]{key, val});
           }
         */

        /*
           List allVals;
           String key;
           for (int i = 0; i < selKeys.size(); i++) {
               key = (String) selKeys.get(i);
               System.out.println("K: " + key);
               allVals = mdProvider.getValues(key);
               if (allVals != null) {
                   for (int j = 0; j < allVals.size(); j++) {
                       System.out.println("\tV: " + allVals.get(j));
                   }
               }
           }
         */
    }

    /**
     * Stores preferences, such as which metadata fields and values to display.
     *
     * @param key the preference key
     * @param value the value
     * @param document the transcription
     */
    public void setPreference(String key, Object value, Object document) {
        if (document instanceof Transcription) {
            Preferences.set(key, value, (Transcription) document, false, false);
        } else {
            Preferences.set(key, value, null, false, false);
        }
    }

    /**
     * Notification of a change in the preferences. Also called after opening a
     * file to restore state.
     */
    public void preferencesChanged() {
        Object pref = Preferences.get(MD_SOURCE,
                viewerManager.getTranscription());
        
        if (pref != null) {
            metadataPath = (String) pref;

            if (metadataPath.startsWith(".")) {
            	metadataPath = metadataPath.replace("\\", "/");
            	String eafPath = ((TranscriptionImpl)viewerManager.getTranscription()).getFullPath();
            	if (eafPath.startsWith("file:")) {
            		eafPath = eafPath.substring(5);
            	}
            	eafPath = eafPath.replace("\\", "/");
            	ClientLogger.LOG.info(eafPath);
            	ClientLogger.LOG.info(metadataPath);
            	metadataPath = FileUtility.getAbsolutePath(eafPath, metadataPath);
            }
            /* --- END --- */
            if ((mdProvider == null) ||
                    !metadataPath.equals(mdProvider.getMetadataFile())) {
                mdProvider = MDServiceRegistry.getInstance()
                                              .getProviderForMDFile(metadataPath);

                if (mdProvider == null) {
                    return;
                }

                MDViewerComponent oldPanel = viewerPanel;
                configureMDButton.setEnabled(true);
                mdPathLabel.setText(metadataPath);
                viewerPanel = mdProvider.getMDViewerComponent();

                if (viewerPanel == null) {
                    viewerPanel = new DefaultMDViewerComponent(mdProvider);
                }
                // remove the old one
                if (oldPanel instanceof Component) {
                	viewerContainer.remove((Component) oldPanel);
                }
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                viewerContainer.add((Component) viewerPanel, gbc);
            }

            // only look for defined keys if there is a provider
            pref = Preferences.get(MD_KEYS, viewerManager.getTranscription());

            if (pref == null) {
                // try globally specified keys, this has a different key in the preferences
                pref = Preferences.get("Metadata.IMDI.Defaults", null);
            }

            if (pref instanceof List) {
                mdProvider.setSelectedKeys(new ArrayList((List) pref));
                
                Map allSelKeysVals = mdProvider.getSelectedKeysAndValues();

                if (allSelKeysVals != null) {
                    if (viewerPanel != null) {
                        viewerPanel.setSelectedKeysAndValues(allSelKeysVals);
                    }
                }
            }
        }
    }

    /**
     * Notification of a change in ui language.
     */
    public void updateLocale() {
        selectMDButton.setText(ElanLocale.getString(
                "MetadataViewer.SelectSource"));
        configureMDButton.setText(ElanLocale.getString(
                "MetadataViewer.Configure"));
        if (viewerPanel != null) {
        	viewerPanel.setResourceBundle(ElanLocale.getResourceBundle());
        }
    }

    /**
     * Returns the viewer manager.
     *
     * @return the viewer manager
     */
    public ViewerManager2 getViewerManager() {
        return viewerManager;
    }

    /**
     * Sets the viewer manager.
     *
     * @param viewerManager the viewer manager
     */
    public void setViewerManager(ViewerManager2 viewerManager) {
        this.viewerManager = viewerManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectMDButton) {
            selectMDSource();
        } else if (e.getSource() == configureMDButton) {
            configureMD();
        }
    }
}
