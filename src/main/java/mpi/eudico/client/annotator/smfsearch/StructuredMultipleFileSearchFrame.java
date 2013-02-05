package mpi.eudico.client.annotator.smfsearch;

//import nl.mpi.annex.swingsearch.SearchApplication;
//import nl.mpi.annex.swingsearch.SearchApplicationMediator;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.prefs.MultipleFileDomains;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFileUtilities;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import mpi.search.SearchLocale;
import nl.mpi.annot.search.mfsearch.SearchApplication;
import nl.mpi.annot.search.mfsearch.SearchApplicationMediator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;


/**
 * A frame for structured search in multiple local annotation (eaf) files.
 * This is the ELAN version of Annex search. This class implements Annex'
 * SearchApplication interface to communicate with Annex functionality.
 * A separate class that implements SearchApplication instead of this Frame 
 * might be created later.
 * 
 * @author HS
 * @version 1.0
 */
public class StructuredMultipleFileSearchFrame extends ClosableFrame
    implements SearchApplication {
    /** prefs key for directories */
    protected static final String PREFERENCES_DIRS_KEY = "MultipleFileSearchDirs";

    /** prefs key for annotation files */
    protected static final String PREFERENCES_PATHS_KEY = "MultipleFileSearchPaths";
    protected static final String PREFERENCES_LAST_DOMAIN = "LastUsedMFSearchDomain";
    private ArrayList searchDirs;
    private ArrayList searchPaths;
    private File[] searchFiles;
    private JComponent defPanel;
    //private JPanel resPanel;

    /**
     * Creates a new StructuredMultipleFileSearchFrame instance
     *
     * @param elanFrame the parent frame
     */
    public StructuredMultipleFileSearchFrame(ElanFrame2 elanFrame) {
        super(SearchLocale.getString("MultipleFileSearch.Title"));

        ImageIcon icon = new ImageIcon(this.getClass()
                                           .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));

        if (icon != null) {
            setIconImage(icon.getImage());
        } else {
            setIconImage(null);
        }

        ArrayList curDomain = loadDomain();

        if (curDomain == null) {
            return;
        }

        // initialize mediator and panels 
        SearchApplicationMediator mediator = new SearchApplicationMediator(this,
                curDomain);
        defPanel = mediator.getSearchComponent();
        //resPanel = mediator.getSearchResultPanel();

        initComponents();
        pack();
        postInit();
        setLocationRelativeTo(elanFrame);
        //setVisible(true);
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 2, 2, 2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        getContentPane().add(defPanel, gbc);
    }
    
    /**
     * Adjust the size of the frame if necessary.
     */
    private void postInit() {
    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((getSize().width > dim.width - 40) ? dim.width - 40 : getSize().width,
            (getSize().height > dim.height - 40) ? dim.height - 40 : getSize().height);
    }

    /**
     * Loads the stored files and folders. If no domain has been specified
     * before the user will  be prompted to add files to the domain. The ui
     * will not be instantiated if there are no files to be searched.
     *
     * @return a list of eaf files
     */
    private ArrayList loadDomain() {
    	// check the last used domain
    	Object val = Preferences.get(PREFERENCES_LAST_DOMAIN, null);
    	
    	if (val instanceof String) {
    		String domainName = (String) val;
    		
            Map<String, List<String>> domain = MultipleFileDomains.getInstance()
            		.getDomain(domainName);

			if (domain != null) {
				List<String> dirs = domain.get(domainName +
						MultipleFileDomains.DIR_SUF);
				
				if (dirs != null) {
					searchDirs = (ArrayList) dirs;
					//dirs = new ArrayList<String>(0);
				}
				
				List<String> paths = domain.get(domainName +
						MultipleFileDomains.PATH_SUF);
				
				if (paths != null) {
					searchPaths = (ArrayList) paths;
					//paths = new ArrayList<String>(0);
				}
			}
    	}
    	
    	if (searchDirs == null && searchPaths == null) {
	        // initialize lists for directories and files from preferences
	        searchDirs = (Preferences.get(PREFERENCES_DIRS_KEY, null) != null)
	            ? (ArrayList) Preferences.get(PREFERENCES_DIRS_KEY, null)
	            : new ArrayList(0);
	        searchPaths = (Preferences.get(PREFERENCES_PATHS_KEY, null) != null)
	            ? (ArrayList) Preferences.get(PREFERENCES_PATHS_KEY, null)
	            : new ArrayList(0);
    	} else {
    		if (searchDirs == null) {
    			searchDirs = new ArrayList(0);
    		}
    		if (searchPaths == null) {
    			searchPaths = new ArrayList(0);
    		}
    	}

        searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                searchPaths);

        if (searchFiles.length == 0) {
            EAFMultipleFileUtilities.specifyDomain(this, searchDirs, searchPaths);
            searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                    searchPaths);

            if (searchFiles.length == 0) {
                return null;
            }
        }

        ArrayList domain = new ArrayList(searchFiles.length);

        for (int i = 0; i < searchFiles.length; i++) {
            domain.add(searchFiles[i]); // or add the path?
        }

        return domain;
    }

    /**
     * Opens the specified file in the viewer application (i.e. ELAN)
     * activating the annotation at the specified time, on the specified tier.
     *
     * @param filePath the file path
     * @param tierName the name of the tier
     * @param beginTime begin time of the annotation
     * @param endTime end time of the annotation
     */
    public void showInViewer(String filePath, final String tierName,
        final long beginTime, final long endTime) {
        if (filePath != null) {
            final ElanFrame2 newElanFrame = FrameManager.getInstance()
                                                        .getFrameFor(filePath);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (newElanFrame != null) {
                if (newElanFrame.getViewerManager() != null || !newElanFrame.isFullyInitialized()) {
                    newElanFrame.getViewerManager().getSelection()
                                .setSelection(beginTime, endTime);
                    newElanFrame.getViewerManager().getMasterMediaPlayer()
                                .setMediaTime(beginTime);

                    TierImpl t = (TierImpl) newElanFrame.getViewerManager()
                                                        .getTranscription()
                                                        .getTierWithId(tierName);

                    if (t != null) {
                        Annotation ann = t.getAnnotationAtTime(beginTime);

                        if (ann != null) {
                            newElanFrame.getViewerManager().getActiveAnnotation()
                                        .setAnnotation(ann);
                        }
                    }
                } else {

                    new Thread(new Runnable() {// new thread, this doesn't work on the eventqueue with invokeLater
                            public void run() {        
                            	  // check initialization of frame, use a time out period
                            	  long timeOut = System.currentTimeMillis() + 30000;
                            	  while (!newElanFrame.isFullyInitialized() && System.currentTimeMillis() < timeOut) {
                            		  try {
                            			  Thread.sleep(200);
                            		  } catch (InterruptedException ie) {
                            			  
                            		  }
                            	  }

                                newElanFrame.getViewerManager().getSelection()
                                            .setSelection(beginTime, endTime);
                                newElanFrame.getViewerManager()
                                            .getMasterMediaPlayer()
                                            .setMediaTime(beginTime);

                                TierImpl t = (TierImpl) newElanFrame.getViewerManager()
                                                                    .getTranscription()
                                                                    .getTierWithId(tierName);

                                if (t != null) {
                                    Annotation ann = t.getAnnotationAtTime(beginTime);

                                    if (ann != null) {
                                        newElanFrame.getViewerManager()
                                                    .getActiveAnnotation()
                                                    .setAnnotation(ann);
                                    }
                                }
                            }
                        }).start();
                }

                newElanFrame.toFront();
                this.toFront();
            }
        }
    }

    /**
     * Returns a list of annotation files (File objects).
     * 
     * @version June 2009 if there are stored domains first prompt whether to 
     * load an existing domain or create a new one.
     *   
     * @return a list of file objects or null if no annotation file is in the
     *         domain. When null is returned this is interpreted as no change
     *         to the search domain
     */
    public ArrayList getDomain() {
    	// prompt with a list of domains
    	// if one is picked load that domain, otherwise continue with 
    	// "new domain prompt"
    	MFDomainDialog mfDialog = new MFDomainDialog(this, 
    			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
    	mfDialog.setSearchDirs(searchDirs);
    	mfDialog.setSearchPaths(searchPaths);
    	mfDialog.setVisible(true);
    	searchDirs = (ArrayList) mfDialog.getSearchDirs();
    	searchPaths = (ArrayList) mfDialog.getSearchPaths();
    	
        //EAFMultipleFileUtilities.specifyDomain(this, searchDirs, searchPaths);
        searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                searchPaths);

        if (searchFiles.length == 0) {
            return null;
        }
        
        ArrayList domain = new ArrayList(searchFiles.length);

        for (int i = 0; i < searchFiles.length; i++) {
        	if (searchFiles[i] != null) {
        		domain.add(searchFiles[i]);
        	}          
        }

        if (domain.size() == 0) {
        	return null;
        }
        
        return domain;
    }

    /**
     * Return the default background color.
     *
     * @return the default background color
     */
    public Color getBackgroundColor() {
        return UIManager.getColor("Panel.background");
    }
    
    /**
     * Store a key/value preference pair in a persistent manner
     * 
     * @param key the key
     * @param value the value
     * @see #getPersistent(String)
     */
    public void putPersistent(String key, String value) {
    	// maybe these preferences should be added to a map that in
    	// turn is added to the preferences
    	Preferences.set(key, value, null);
    }
    
    /**
     * Get a value String for a key from the persistent store used by putPersistent
     * 
     * @param key the key
     * @return the value String, null if it does not exist
     * @see #putPersistent(String, String)
     */
    public String getPersistent(String key) {
    	// maybe the string should be retrieved from a map (that is retrieved
    	// from the preferences) rather than from the preferences directly
    	return (String) Preferences.get(key, null);
    }
    
    /**
     * Delete a key/value pair from the persistent store used by putPersistent
     * 
     * @param key the key
     */
    public void deletePersistent(String key) {
    	Preferences.set(key, null, null);//??
    }
}
