package mpi.eudico.client.annotator;

import java.awt.EventQueue;

import java.io.File;
import javax.swing.JPopupMenu;
//import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mpi.eudico.client.annotator.update.ExternalUpdaterThread;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;


/**
 * The main class for ELAN. Main performs some initialization and creates the
 * first frame. Holds version information, major, minor and micro.
 */
public class ELAN implements ClientLogger {
    /** the major version value */
    public static int major = 4;

    /** the minor version value */
    public static int minor = 5;

    /** the micro (bug fix) version value */
    public static int micro = 0;

    /**
     * Creates a new ELAN instance
     */
    private ELAN() {
    }

    /**
     * Main method, initialization and first frame.
     *
     * @param args the arguments, path to an eaf file
     */
    public static void main(final String[] args) {
    	LOG.info("");
    	System.out.println("\n@ELAN Launched\n");
    	LOG.info("ELAN " + getVersionString());
    	LOG.info("Java home: " + System.getProperty("java.home"));
    	LOG.info("Java version: " +
                System.getProperty("java.version"));
    	LOG.info("Runtime version: " +
                System.getProperty("java.runtime.version"));
    	LOG.info("OS name: " + System.getProperty("os.name"));
    	LOG.info("OS version: " + System.getProperty("os.version"));
    	LOG.info("User language: " + System.getProperty("user.language"));
    	LOG.info("User home: " + System.getProperty("user.home"));
    	LOG.info("User dir: " + System.getProperty("user.dir"));
    	LOG.info("Classpath: " + System.getProperty("java.class.path"));
    	LOG.info("Library path: " + System.getProperty("java.library.path"));
        // make sure the directory for Elan data exists, could move to preferences?
        try {
            /* HS May 2008: copy files to the new ELAN data folder. Do this only once. */
            if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
            	File dataFolder = new File(Constants.ELAN_DATA_DIR);
            	if (!dataFolder.exists()) {
            		dataFolder.mkdir();
            		File oldDataFolder = new File(Constants.USERHOME + Constants.FILESEPARATOR + ".elan_data");
            		if (oldDataFolder.exists()) {
            			// copy files
            			File[] files = oldDataFolder.listFiles();
            			File inFile = null;
            			File outFile = null;
            			for (int i = 0; i < files.length; i++) {
            				inFile = files[i];
            				if (inFile.isFile()) {
            					outFile = new File(dataFolder.getAbsolutePath() + Constants.FILESEPARATOR + inFile.getName());
            					FileUtility.copyToFile(inFile, outFile);
            				}
            			}
            		}
            	}
            	boolean screenBar = false;//default
            	Object val = Preferences.get("OS.Mac.useScreenMenuBar", null);
            	if (val instanceof Boolean) {
            		screenBar = ((Boolean) val).booleanValue();
            		System.setProperty("apple.laf.useScreenMenuBar", String.valueOf(screenBar));
            	}
            	// using the screen menu bar implies the default Mac OS L&F
            	if (!screenBar) {
            		val = Preferences.get("UseMacLF", null);
            		
            		if (val instanceof Boolean) {
            			boolean macLF = ((Boolean) val).booleanValue();
            			if (!macLF) {
                            try {
                                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                                JPopupMenu.setDefaultLightWeightPopupEnabled(false);//??
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
            			}
            		}
            	}
            	//System.setProperty("apple.awt.brushMetalLook", "true"); 
            	// media framework: if no framework specified, check the user's stored preference
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		val = Preferences.get("Mac.PrefMediaFramework", null);
            		if (val instanceof String) {
            			System.setProperty("PreferredMediaFramework", (String) val);
            		}
            	}

            }// end mac initialization
            else if (SystemReporting.isWindows()) {// windows user preferred media framework
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		Object val = Preferences.get("Windows.PrefMediaFramework", null);
            		if (val instanceof String) {
            			System.setProperty("PreferredMediaFramework", (String) val);
            		}
            	}

            	Object val = Preferences.get("UseWinLF", null);
            	if (val instanceof Boolean) {
            		if ((Boolean) val) {
                    	try {
                        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception ex) {
                        	ClientLogger.LOG.warning("Could not set the Look and Feel");
                        }
            		}
            	}
            }
            
            File dataDir = new File(Constants.ELAN_DATA_DIR);

            if (!dataDir.exists()) {
                dataDir.mkdir();
            }

            // temporary, clean up old crap
            File oldCrap = new File(Constants.STRPROPERTIESFILE);
            oldCrap.delete();
            oldCrap = new File(Constants.USERHOME + Constants.FILESEPARATOR +
                    ".elan.pfs");
            oldCrap.delete();
            
        } catch (Exception ex) {
            // catch any
        	LOG.warning("Could not create ELAN's data directory: " + ex.getMessage());
        }
        
        FrameManager.getInstance().setExitAllowed(true);
        
        // create the frame on the event dispatch thread
        
        if ((args != null) && (args.length > 0) && (args[0].length() != 0)) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                		File argFile = new File(args[0]); 
                		//System.out.println("F " + argFile.getAbsolutePath());
                		//System.out.println("A " + argFile.isAbsolute());
                    	// HS July 2008: check if the argument (filepath to eaf) is a relative
                    	// path. If so let the jvm resolve it relative to the current directory 
                    	// (where ELAN is launched from)
                    	if (!argFile.isAbsolute()) { 
                    		//System.out.println("F " + argFile.getAbsolutePath());
                    		FrameManager.getInstance().createFrame(argFile.getAbsolutePath());
                    	} else {
                    		FrameManager.getInstance().createFrame(args[0]);
                    	}
                    }
                });
        } else {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        FrameManager.getInstance().createEmptyFrame();
                    }
                });
        }

        // external launcher, currently only accepts imdi files to open an eaf
        // from another application/VM
        // HS 02-2012 has not been used for years
        //mpi.eudico.client.annotator.integration.ExternalLauncher.start();
        
        // automatic check for version update
        Object val = Preferences.get("AutomaticUpdate", null);		
    	if(val == null  ||( val instanceof Boolean && (Boolean)val)){
    		ExternalUpdaterThread updater = new ExternalUpdaterThread();
            updater.start();
    	}           
        
        // create one Mac Application handler per jvm
        if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
            try {
            	Class macHandler = null;
            	try {
            		macHandler = Class.forName("mpi.eudico.client.mac.MacAppHandler2");
            		System.out.println("Loading new Apple integration 2");
            	} catch (Throwable anyCause) {
            		macHandler = Class.forName(
                    "mpi.eudico.client.mac.MacAppHandler");	
            		System.out.println("Loading Apple integration 1");
            		//anyCause.printStackTrace();
            	}
                
                Class macList = Class.forName(
                        "mpi.eudico.client.mac.MacApplicationListener");
                java.lang.reflect.Constructor con = macHandler.getConstructor(new Class[] {
                            macList
                        });
                ElanMacApplication elanMacApp = new ElanMacApplication();
                con.newInstance(new Object[] { elanMacApp });
            } catch (Throwable ex) {
                System.out.println("Could not load Mac application handler.");
                //ex.printStackTrace();
            }
        }
        /*
        Properties props = System.getProperties();
        Iterator prIt = props.keySet().iterator();
        String key, value;
        while (prIt.hasNext()) {
        	key = (String) prIt.next();
        	value = props.getProperty(key);
        	System.out.println("K: " + key + " V: " + value);
        }
        */
    }

    /**
     * Returns the current version information as a string.
     *
     * @return the current version
     */
    public static String getVersionString() {
        return major + "." + minor + "." + micro;
    }
    
    /**
     * Prints the UI defaults of the current platform to System.out.
     */
    /*
    private void printUIDefaults() {
        Hashtable uid = UIManager.getDefaults();
        Iterator keyIt = uid.keySet().iterator();
        Object uiKey, val;
        while (keyIt.hasNext()) {
        	uiKey = keyIt.next();
        	val = uid.get(uiKey);
        	System.out.println("Key: " + uiKey + "\n\tValue: " + val);
        }
    }
    */
    
}
