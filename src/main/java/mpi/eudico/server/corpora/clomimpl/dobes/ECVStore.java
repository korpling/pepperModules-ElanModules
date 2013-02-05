package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/**
 * Stores and load an External CV
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECVStore {
	
	/**
	 * Loads an External CV from a url. This is not optimal for the case where 
	 * an external file contains multiple CV's.
	 * 
	 * @param cv
	 * @param url
	 * @param theTranscription
	 * @throws ParseException
	 */
	public void loadExternalCV(ExternalCV cv, String url) 
		throws ParseException {
		ECV01Parser ecvParser = null;
        try {
        	ecvParser = new ECV01Parser(url);
        	ecvParser.parse();
        } catch (ParseException pe) {
        	System.out.println("Parse failed " + url);
        	throw(pe);
        }
        
        // get the ext refs mappings
		Map extReferences = ecvParser.getExternalReferences();
        
        ArrayList allCVs = ecvParser.getControlledVocabularies();

        ExternalCV cvFromUrl = null;
        for (int i = 0; i < allCVs.size(); i++) {
        	cvFromUrl = (ExternalCV) allCVs.get(i);
        	if(cvFromUrl.getName().equals(cv.getName())) {
        		cv.addAll(cvFromUrl.getEntries());
        	}
        } 
	}
	
	/**
	 * Loads all entries for External Controlled Vocabularies from the specified url.
	 * The ECV objects have been created beforehand (e.g. when parsing an eaf file).
	 * 
	 * @param ecvList the list of ECV objects, should not be null
	 * @param url the url of the file containing the controlled vocabularies
	 * 
	 * @throws ParseException
	 */
	public void loadExternalCVS(List<ExternalCV> ecvList, String url) throws ParseException {
		if (ecvList == null || ecvList.size() == 0) {
			return;// return silently
		}
		ECV01Parser ecvParser = null;
        try {
        	ecvParser = new ECV01Parser(url);
        	ecvParser.parse();
        } catch (ParseException pe) {
        	ServerLogger.LOG.severe("Parse failed " + url);
        	throw(pe);
        }
        
        // get the ext refs mappings
		Map<String, ExternalReference> extReferences = ecvParser.getExternalReferences();
        
        ArrayList<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();

        ExternalCV cvFromUrl = null;
        ExternalCV cvFromList = null;
        for (int j = 0; j < ecvList.size(); j++) {
        	cvFromList = ecvList.get(j);
        	
	        for (int i = 0; i < allCVs.size(); i++) {
	        	cvFromUrl = (ExternalCV) allCVs.get(i);
	        	// checking equality by name might fail if the name has been changed there where
	        	// it is used, by the "client"
	        	if(cvFromUrl.getName().equals(cvFromList.getName())) {
	        		cvFromList.addAll(cvFromUrl.getEntries());
	        		break;
	        	}
	        }
        }
	}
	
	/**
	 * Stores an External CV (not implemented yet)
	 * 
	 * @see mpi./eudico/server/corpora/clomimpl/dobes/ECV01Encoder
	 *  
	 * @param cv a single external CV
	 * @param cachePath the cache base folder
	 * @param urlString the location of the source file
	 */
	public void storeExternalCV(ExternalCV cv, String cachePath, String urlString) {
		if (cv == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV provided.");
			return;
		}
		List<ExternalCV> list = new ArrayList<ExternalCV>(1);
		storeExternalCVS(list, cachePath, urlString);
	}
	
	/**
	 * Creates a cached version of the controlled vocabularies loaded from the same
	 * external source.
	 * 
	 * @param ecvList the list of controlled vocabularies
	 * @param cachePath the path to the cache base folder
	 * @param urlString the source file
	 */
	public void storeExternalCVS(List<ExternalCV> ecvList, String cachePath, String urlString) {
		if (ecvList == null || ecvList.size() == 0) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV's provided.");
			return; // return silently
		}
		if (cachePath == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no cache folder specified.");
			return;
		}
		if (cachePath == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no source URL specified.");
			return;
		}
		
		ExternalReferenceImpl eri = new ExternalReferenceImpl(urlString, ExternalReference.EXTERNAL_CV);
		try {
			ECV01Encoder encoder = new ECV01Encoder();
			encoder.encodeAndSave(ecvList, cachePath, eri);
		} catch (Throwable thr) {// catch anything that can go wrong, caching is not crucial
			ServerLogger.LOG.severe("Could not create a cached version: " + thr.getMessage());
		}
	}
}
