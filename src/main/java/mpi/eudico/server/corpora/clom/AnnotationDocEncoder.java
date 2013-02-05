/*
 * Created on Oct 12, 2004
 *
 */
package mpi.eudico.server.corpora.clom;

import java.io.IOException;
import java.util.List;

/**
 * To be implemented for every annotation document format that is to
 * be written. Used by TranscriptionStore to encode and write to the
 * desired output format.
 * 
 * @author hennie
 *
 */
public interface AnnotationDocEncoder {

	public void encodeAndSave(Transcription theTranscription, EncoderInfo theEncoderInfo, 
	        List tierOrder, String path) throws IOException;
}
