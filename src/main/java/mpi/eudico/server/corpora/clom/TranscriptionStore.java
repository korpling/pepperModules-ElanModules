package mpi.eudico.server.corpora.clom;

import java.io.IOException;

import java.util.List;


/**
 * First attempt to abstract persistent storage. If this works for DOBES
 * minimal and DOBES-ATLAS implementations, generalization to ACM will be
 * done.
 *
 * @version Aug 2005 Identity removed
 */
public interface TranscriptionStore {
    /** constant for EAF format */
    public static final int EAF = 0;

    /** constant for the CHAT format */
    public static final int CHAT = 1;

    /** constant for the Shoebox/Toolbox format */
    public static final int SHOEBOX = 2;

    /** constant for the Transcriber format */
    public static final int TRANSCRIBER = 3;

    /**
     * Saves a transcription.
     *
     * @param theTranscription the transcription document
     * @param encoderInfo information for the encoding process
     * @param tierOrder optional list of the (preferred) tier order
     * @param format the format in which to store the document, one of the
     *        TranscriptionStore's format constants
     *
     * @throws IOException any io exception
     */
    public void storeTranscription(Transcription theTranscription,
        EncoderInfo encoderInfo, List tierOrder, int format)
        throws IOException;

    /**
     * Saves a transcription.
     *
     * @param theTranscription the transcription document
     * @param encoderInfo information for the encoding process
     * @param tierOrder optional list of the (preferred) tier order
     * @param pathName the path to the file to store the transcription in
     * @param format the format in which to store the document, one of the
     *        TranscriptionStore's format constants
     *
     * @throws IOException any io exception
     */
    public void storeTranscription(Transcription theTranscription,
        EncoderInfo encoderInfo, List tierOrder, String pathName, int format)
        throws IOException;

    /**
     * Writes to the file specified by given path, unless the field
     * <code>fileToWriteXMLinto</code> is not null.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param encoderInfo information for the encoding process
     * @param tierOrder the preferred ordering of the tiers
     * @param path the path to the file to use for storage
     * @param format the format in which to store the document, one of the
     *        TranscriptionStore's format constants
     *
     * @throws IOException any io exception
     */
    public void storeTranscriptionIn(Transcription theTranscription,
        EncoderInfo encoderInfo, List tierOrder, String path, int format)
        throws IOException;

    /**
     * Creates a template file using the given path, unless the field
     * <code>fileToWriteXMLinto</code> is not null.
     *
     * @param theTranscription the Transcription to use for the template (not
     *        null)
     * @param tierOrder the preferred ordering of the tiers
     * @param path the path to the file to use for storage
     *
     * @throws IOException any io exception
     */
    public void storeTranscriptionAsTemplateIn(Transcription theTranscription,
        List tierOrder, String path) throws IOException;
    
    /**
     * Loads a transcription
     *
     * @param theTranscription the transcription
     */
    public void loadTranscription(Transcription theTranscription);

    /**
     * Loads the Transcription from an eaf file.
     *
     * @param theTranscription the transcription to load
     * @param decoderInfo the info object for the decoder or parser
     */
    public void loadTranscription(Transcription theTranscription,
        DecoderInfo decoderInfo);
}
