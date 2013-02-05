/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * @author hennie
 *
 * Note: for historic reasons all getters take the file to be parsed as a 
 * parameter. This could be dropped in the future.
 * @version Dec 2006: added getter for the Annotator of a tier
 */
public abstract class Parser {
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getMediaFile(String fileName) {
		return null;
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract ArrayList getMediaDescriptors(String fileName);
	/**
	 * Returns the Linked File descriptors; only applicable to eaf
	 *
	 * @param fileName the file name
	 *
	 * @return a List of linked file descriptors
	 */
	public ArrayList getLinkedFileDescriptors(String fileName) {
	    return null;
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getSVGFile(String fileName) {
		return null;
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getAuthor(String fileName) {
		return null;
	}
	/**
	 * Returns a list of the document (header) properties.
	 * 
	 * @param fileName the file to be parsed
	 * 
	 * @return a list of the document (header) properties
	 */
	public ArrayList getTranscriptionProperties(String fileName) {
		return null;
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract ArrayList getLinguisticTypes(String fileName);
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract ArrayList getTimeOrder(String fileName);
	/**
	 * DOCUMENT ME!
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract HashMap getTimeSlots(String fileName);
	
	public HashMap getControlledVocabularies(String fileName) {
		return null;
	}

	/**
	 * Returns a map of id - ext ref objects pairs. 
	 * 
	 * @param fileName the transcription file
	 * @return a map with id - object mappings, null by default
	 */
	public Map getExternalReferences (String fileName) {
		return null;
	}
	
	/**
	 * Returns a map of id - services pairs
	 * 
	 * @param fileName the transcription file
	 * @return a map with id - services mappings
	 */
	public HashMap getLexiconServices(String fileName) {
		return null;
	}
	
	/**
	 * Returns the names of the Tiers that are present in the Transcription
	 * file
	 *
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract ArrayList getTierNames(String fileName);
	/**
	 * DOCUMENT ME!
	 *
	 * @param tierName DOCUMENT ME!
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract String getParticipantOf(String tierName, String fileName);
	/**
	 * Returns the annotator of a tier, will not be present in a lot of 
	 * file types.
	 *
	 * @param tierName the tier id
	 * @param fileName the file to parse
	 *
	 * @return the annotator
	 */
	public String getAnnotatorOf(String tierName, String fileName) {
		return null;
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param tierName DOCUMENT ME!
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract String getLinguisticTypeIDOf(
		String tierName,
		String fileName);
	/**
	 * DOCUMENT ME!
	 *
	 * @param tierName DOCUMENT ME!
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Locale getDefaultLanguageOf(
		String tierName,
		String fileName) {
			return new Locale("US", "en", "");
		}
	/**
	 * DOCUMENT ME!
	 *
	 * @param tierName DOCUMENT ME!
	 * @param fileName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract String getParentNameOf(String tierName, String fileName);
	/**
	 * Returns a List with the Annotations for this Tier. Each
	 * AnnotationRecord contains begin time, end time and text values
	 * 
	 * <p>
	 * MK:02/06/10<br> Elements of List are no CLOM/ACM Annotations but yet
	 * another List of String . The inner List is interpreted as variant
	 * record in DAFTranscriptionStore.loadTranscription
	 * </p>
	 *
	 * @param tierName DOCUMENT ME!
	 * @param fileName DOCUMENT ME!
	 *
	 * @return List of List of String
	 */
	public abstract ArrayList getAnnotationsOf(String tierName, String fileName);
	
	/**
	 * Sets the decoder info object for the parser. Empty implementation.
	 * 
	 * @param decoderInfo the decoderinfo object
	 */
	public void setDecoderInfo(DecoderInfo decoderInfo) {
	    // stub
	}
	
}