package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.svg.SVGParserAndStore;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationEncoder;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.MonitoringLogger;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;

import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;


/**
 * Saves a transcription as an .eaf or .etf (template) file, either creating a new 
 * file (Save As) or overwriting an existing file (Save).<br>
 * 
 * @version Nov 2007 added support for relative media paths
 * @author Hennie Brugman
 */
public class StoreCommand implements Command {
    private String commandName;

    /**
     * Creates a new StoreCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public StoreCommand(String name) {
        commandName = name;
    }

    //arguments:
    //[0]: TranscriptionStore eafTranscriptionStore
    //[1]: Boolean saveAsTemplate
    //[2]: Boolean saveNewCopy
    //[3]: Vector visibleTiers
    public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;
        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];
        boolean saveAsTemplate = ((Boolean) arguments[1]).booleanValue();
        boolean saveNewCopy = ((Boolean) arguments[2]).booleanValue();
        List visibleTiers;

        if (arguments[3] != null) {
            visibleTiers = (List) arguments[3];
        } else {
        	if (ELANCommandFactory.getViewerManager(tr)
                                             .getMultiTierControlPanel() != null) {
	            visibleTiers = ELANCommandFactory.getViewerManager(tr)
	                                             .getMultiTierControlPanel()
	                                             .getVisibleTiers();
        	} else {
        		visibleTiers = new ArrayList(0);//just to be on the save side, should be ignored in most cases
        	}
        }

        if (saveNewCopy) {
            // prompt for new file name         
            // open dialog at directory of original eaf file
            FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(tr));

            if (saveAsTemplate) {              
                chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Template.Title"), FileChooser.SAVE_DIALOG, 
                		FileExtension.TEMPLATE_EXT, "LastUsedEAFDir"); 
            } else {            	
            	String fileName = tr.getName();
            	
            	if(!fileName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME)){
            		fileName = fileName.substring(0,fileName.lastIndexOf('.')) + ".eaf";
            		chooser.setCurrentDirectory( FileUtility.urlToAbsPath(tr.getFullPath()));    
            		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, null,
                			FileExtension.EAF_EXT, null,fileName); 
            	}else {            	
            		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, 
                			FileExtension.EAF_EXT, "LastUsedEAFDir"); 
            	}            	
            }
            
            File f = chooser.getSelectedFile();
            if (f != null) {
                // make sure pathname finishes with .eaf or .etf extension
                String pathName = f.getAbsolutePath();  
                if (saveAsTemplate) {
                    try {
                    eafTranscriptionStore.storeTranscriptionAsTemplateIn(tr,
                        visibleTiers, pathName);
                    
                    // HS Nov 2009: save a preferences file alongside the template
                    String templatePrefPath = pathName.substring(0, pathName.length() - 3) + "pfsx";
                    Preferences.exportPreferences(tr, templatePrefPath);
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                "Unable to save the template file: " +
                                "(" + ioe.getMessage() + ")",
                                ElanLocale.getString("Message.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // checks if there are any tiers allowing graphical annotations...
                    boolean saveSVG = false;

                    if (!saveSVG) {
                        Vector tiers = tr.getTiers();
                        TierImpl tier;

                        for (int i = 0; i < tiers.size(); i++) {
                            tier = (TierImpl) tiers.get(i);

                            if (tier.getLinguisticType()
                                        .hasGraphicReferences()) {
                                saveSVG = true;

                                break;
                            }
                        }
                    }

                    if (!saveSVG) {
                         ((TranscriptionImpl) tr).setSVGFile(null);
                    }
                    else {
                        int index = pathName.lastIndexOf(".eaf");
                        String svgFileName = pathName.substring(0, index) +
                            ".svg";
                        ((TranscriptionImpl) tr).setSVGFile(svgFileName);
                        //SVGParserAndStore.storeSVG(tr);
                    }
                    String name = pathName;
                    int lastSlashPos = name.lastIndexOf(System.getProperty(
                                "file.separator"));

                    if (lastSlashPos >= 0) {
                        name = name.substring(lastSlashPos + 1);
                    }

                    //System.out.println("nm " + name);
                    tr.setName(name);

                    //tr.setName(pathName);
                    if (tr instanceof TranscriptionImpl) {
                        ((TranscriptionImpl) tr).setPathName(pathName);
                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
                            tr.getName());
                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame(tr), 
                        		pathName);
                    } else {
                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
                            name);
                    }
                    
                    // check, copy and update linked files, configuration and svg files 
                    Vector linkedFiles = tr.getLinkedFileDescriptors();
                    String svgExt = ".svg";
                    String confExt = "_tsconf.xml";
                    String curExt;
                    if (linkedFiles.size() > 0) {
                    	LinkedFileDescriptor lfd;
                    	for (int i = 0; i < linkedFiles.size(); i++) {
                    		curExt = null;
                    		lfd = (LinkedFileDescriptor) linkedFiles.get(i);
                    		if (lfd.linkURL.toLowerCase().endsWith(confExt)) {
                    			curExt = confExt;
                    		} else if (lfd.linkURL.toLowerCase().endsWith(svgExt)) {
                    			curExt = svgExt;
                    		}
                    		if (curExt != null) {
                    			// ELAN generated configuration file, copy
                    			String url = pathName.substring(0, pathName.length() - 4) +
                    				curExt;
                    			System.out.println("New conf: " + url);                     			
                    			// copy conf or svg
                    			try {
                    				File source = null, dest = null;
                    				if (lfd.linkURL.startsWith("file:")) {
                    					source = new File(lfd.linkURL.substring(5));
                    				} else {
                    					source = new File(lfd.linkURL);
                    				}
                    				if (url.startsWith("file:")) {
                    					dest = new File(url.substring(5));
                    				} else {
                    					dest = new File(url);
                    				}
                    				if (source.exists() && source.compareTo(dest) != 0) {
                    					FileUtility.copyToFile(source, dest);
                    				} else {                       					
                    					TSConfigurationEncoder enc = new TSConfigurationEncoder();
                    					enc.encodeAndSave((TranscriptionImpl) tr, 
                    							ELANCommandFactory.getTrackManager(tr).getConfigs());
                    				}
                    			} catch (Exception ex) {
                    				System.out.println("Could not copy the configuration file.");
                    			}
                    			lfd.linkURL = FileUtility.pathToURLString(url);
                    			tr.setChanged();
                    		}
                    	}
                    }
                    
                    // update relative media paths
                    // make sure the eaf path is treated the same way as media files,
                    // i.e. it starts with file:/// or file://
                    String fullEAFURL = FileUtility.pathToURLString(pathName);
                    Vector mediaDescriptors = tr.getMediaDescriptors();
                    MediaDescriptor md;
                    String relUrl;
                    
                    for (int i = 0; i < mediaDescriptors.size(); i++) {
                        md = (MediaDescriptor) mediaDescriptors.elementAt(i);
                        relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
                        md.relativeMediaURL = relUrl;
                    }
                    // linked other files 
                    if (linkedFiles.size() > 0) {
                    	LinkedFileDescriptor lfd;
                    	for (int i = 0; i < linkedFiles.size(); i++) {
                    		lfd = (LinkedFileDescriptor) linkedFiles.get(i);
                    		relUrl = FileUtility.getRelativePath(fullEAFURL,lfd.linkURL);
                    		lfd.relativeLinkURL = relUrl;
                    	}
                    }
                    // save
                    try {
                        eafTranscriptionStore.storeTranscriptionIn(tr, null,
                                visibleTiers, pathName, TranscriptionStore.EAF);
                        if(MonitoringLogger.isInitiated()){
                        	MonitoringLogger.getLogger(tr).log(MonitoringLogger.SAVE_FILE);
                        }
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                "Unable to save the transcription file: " +
                                "(" + ioe.getMessage() + ")",
                                ElanLocale.getString("Message.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
//                    String name = pathName;
//                    int lastSlashPos = name.lastIndexOf(System.getProperty(
//                                "file.separator"));
//
//                    if (lastSlashPos >= 0) {
//                        name = name.substring(lastSlashPos + 1);
//                    }
//
//                    //System.out.println("nm " + name);
//                    tr.setName(name);
//
//                    //tr.setName(pathName);
//                    if (tr instanceof TranscriptionImpl) {
//                        ((TranscriptionImpl) tr).setPathName(pathName);
//                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
//                            tr.getName());
//                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame(tr), 
//                        		pathName);
//                    } else {
//                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
//                            name);
//                    }

                    tr.setUnchanged();
                    
                    // save svg
                   if (saveSVG) {
                        SVGParserAndStore.storeSVG(tr);
                    }

                    // create a new backup timer
                    if (tr instanceof TranscriptionImpl) {
                        ((BackupCA) ELANCommandFactory.getCommandAction(tr,
                            ELANCommandFactory.BACKUP)).setFilePath(pathName);
                    }
                }
            }             
        } else if (tr.isChanged()) {
            // check svg
            boolean saveSVG = false;
            Vector tiers = tr.getTiers();
            TierImpl tier;

            for (int i = 0; i < tiers.size(); i++) {
                tier = (TierImpl) tiers.get(i);

                if (tier.getLinguisticType().hasGraphicReferences()) {
                    saveSVG = true;

                    break;
                }
            }
            
            if (!saveSVG) {
                ((TranscriptionImpl) tr).setSVGFile(null);
            }

            String svgFileName = ((TranscriptionImpl) tr).getSVGFile();

            if ((svgFileName == null) && saveSVG) {
                String pathName = ((TranscriptionImpl) tr).getPathName();
                int index = pathName.lastIndexOf(".eaf");
                String newSvgFileName = pathName.substring(0, index) + ".svg";
                ((TranscriptionImpl) tr).setSVGFile(newSvgFileName);
            }
            // check if relative media paths have to be generated or updated
            // make sure the eaf path is treated the same way as media files,
            // i.e. it starts with file:/// or file://
            String fullEAFURL = FileUtility.pathToURLString(tr.getFullPath());
            Vector mediaDescriptors = tr.getMediaDescriptors();
            MediaDescriptor md;
            String relUrl;
            for (int i = 0; i < mediaDescriptors.size(); i++) {
                md = (MediaDescriptor) mediaDescriptors.elementAt(i);
                relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
                md.relativeMediaURL = relUrl;
            }
            
            // linked other files 
            Vector linkedFiles = tr.getLinkedFileDescriptors();
            if (linkedFiles.size() > 0) {
            	LinkedFileDescriptor lfd;
            	for (int i = 0; i < linkedFiles.size(); i++) {
            		lfd = (LinkedFileDescriptor) linkedFiles.get(i);
            		relUrl = FileUtility.getRelativePath(fullEAFURL,lfd.linkURL);
            		lfd.relativeLinkURL = relUrl;
            	}
            }
            
            try {
                eafTranscriptionStore.storeTranscription(tr, null, visibleTiers,
                		TranscriptionStore.EAF);
                if(MonitoringLogger.isInitiated()){
                	MonitoringLogger.getLogger(tr).log(MonitoringLogger.SAVE_FILE);
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                        //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "Unable to save the transcription file: " +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);
            }

            if ((svgFileName != null) || saveSVG) {
                SVGParserAndStore.storeSVG(tr);
            }

            //SVGParserAndStore.storeSVG(tr, null);    
            tr.setUnchanged();
        }// hier.. check if there are sec. linked files and store the config file
        else {
        	
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }
}
