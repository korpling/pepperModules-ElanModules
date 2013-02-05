package mpi.eudico.client.util;

import java.awt.Frame;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ECVStore;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/** 
 * Utility class for loading externally defined controlled vocabularies 
 * and updating annotation values, if needed.
 * 
 * @author Han Sloetjes
 */
public class TranscriptionECVLoader {

	public TranscriptionECVLoader() {
		super();
	}
	
	/**
	 * Checks the transcription and loads CV's. 
	 * The parent frame reference is only used for error/warning messages.
	 * 
	 * @param transcription the transcription to check
	 * @param parent the parent frame or null
	 */
	public void loadExternalCVs(TranscriptionImpl transcription, Frame parent) {
        int numCV = transcription.getControlledVocabularies().size();
        if (numCV > 0) {
        	int numLoadedExternal = 0;
        	ControlledVocabulary cv = null;
        	ECVStore ecvStore = null;
        	HashMap<String, List<ExternalCV>> urlMap = new HashMap<String, List<ExternalCV>>(4);
        	// collect all CV's that stem from the same url and then load them in one action
        	for (int i = 0; i < numCV; i++) {
        		cv = (ControlledVocabulary) transcription.getControlledVocabularies().get(i);
        		if (cv instanceof ExternalCV) {
        			ExternalCV ecv = (ExternalCV) cv;
        			if (!ecv.isLoadedFromURL() && !ecv.isLoadedFromCache()) {
        				// try to load from URL
        				Object extRef = ecv.getExternalRef();
        				if (extRef instanceof ExternalReference) {
        					if (((ExternalReference) extRef).getReferenceType() == ExternalReference.EXTERNAL_CV) {
        						String urlString = ((ExternalReference) extRef).getValue();
        						if (urlString != null) {
        							if (urlMap.containsKey(urlString)) {
        								urlMap.get(urlString).add(ecv);
        							} else {
        								ArrayList<ExternalCV> ecvList = new ArrayList<ExternalCV>(4);
        								ecvList.add(ecv);
        								urlMap.put(urlString, ecvList);
        							}
        						}
        					}
        				}
        			}
        		}
        	}
        	
        	Iterator<String> keyIt = urlMap.keySet().iterator();
        	String urlStr = null;
        	List<ExternalCV> ecvList = null;
        	String cachedCV = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "CVCACHE";
        	
        	while (keyIt.hasNext()) {
        		urlStr = keyIt.next();
        		ecvList = urlMap.get(urlStr);
        		if (ecvStore == null) {
        			ecvStore = new ECVStore();
        		}
        		
        		try {
        			ecvStore.loadExternalCVS(ecvList, urlStr);
        			for (ExternalCV excv : ecvList) {
        				excv.setLoadedFromURL(true);
        			}
        			numLoadedExternal += ecvList.size();
        			// update cache
        			ecvStore.storeExternalCVS(ecvList, cachedCV, urlStr);
        		} catch (Exception exc) {
        			// load local
        			try {
            			URL url = new URL(urlStr);
						String urlHost = url.getHost();
						if(urlHost.equals("")) {
							urlHost = "localfile_system";
						}
						cachedCV += Constants.FILESEPARATOR + urlHost;
						String pathElements[] = url.getPath().substring(1).split("/");
						for(int j = 0; j < (pathElements.length); j++) {
							cachedCV += Constants.FILESEPARATOR + pathElements[j];
						}
						ecvStore.loadExternalCVS(ecvList, cachedCV);
            			for (ExternalCV excv : ecvList) {
            				excv.setLoadedFromCache(true);
            			}
						String message = ElanLocale.getString("LoadExternalCV.Message.Readerror") 
							+ " " + urlStr + "\n"
							+ ElanLocale.getString("LoadExternalCV.Message.LoadedFromCache");
						if (parent != null) {
							JOptionPane.showMessageDialog(parent, message,
								ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
						} else {
							ClientLogger.LOG.warning("Warning: " + message);
						}
        			} catch (Exception ex) {// Malformed URL, Parse, IO, Security etc. Exception
        				String message = ElanLocale.getString("LoadExternalCV.Message.Readerror") 
						+ " " + urlStr + "\n"
						+ ElanLocale.getString("LoadExternalCV.Message.NotLoadedFromCache");
        				if (parent != null) {
        					JOptionPane.showMessageDialog(parent, message,
								ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
        				} else {
        					ClientLogger.LOG.warning("Warning: " + message);
        				}
        			}
        		}
        	}
        	
        	if (numLoadedExternal > 0) {
        		transcription.checkAnnotECVConsistency();
        	}
        }
    }

}
