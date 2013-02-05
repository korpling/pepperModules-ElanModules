package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;


/**
 * A class with a single method that returns the current ACM TranscriptionStore.
 * To be used when no specific version is required, it returns the latest version.
 * <br>
 * This way there will be a single location to be changed when a new version of 
 * the transcription store becomes available.
 *  
 * @author Han Sloetjes
 * @version 1.0
  */
public class ACMTranscriptionStore {
    /**
     * Creates a new ACMTranscriptionStore instance
     */
    private ACMTranscriptionStore() {
        // not to be instantiated
    }

    /**
     * Returns the current version of ACM Transcription Store.
     * Note: this methods creates a new instance of the transcription store 
     * for each call
     *
     * @return the current version of the ACM Transcription Store
     */
    public static final TranscriptionStore getCurrentTranscriptionStore() {
        return new ACM27TranscriptionStore();
    }
    
    /**
     * Returns the current (latest) parser for .eaf files.
     * 
     * @return the current (latest) parser for .eaf files
     */
    public static final Parser getCurrentEAFParser() {
    	return new EAF27Parser();
    }
    
    /**
     * Returns the path to the current (latest) local version of the EAF schema.
     * Local means the location in the source tree.
     * 
     * @return the path to the current EAF schema
     */
    public static final String getCurrentEAFSchemaLocal() {
    	return "/mpi/eudico/resources/EAFv2.7.xsd";
    }
    
    /**
     * Returns the path to the current (latest) remote version of the EAF schema.
     * Remote means the (official) URL of the EAF schema.
     * 
     * @return the URL (as a string) to the current EAF schema
     */
    public static final String getCurrentEAFSchemaRemote() {
    	return "http://www.mpi.nl/tools/elan/EAFv2.7.xsd";
    }
    
}
