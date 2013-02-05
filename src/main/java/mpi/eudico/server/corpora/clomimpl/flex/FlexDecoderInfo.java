package mpi.eudico.server.corpora.clomimpl.flex;

import mpi.eudico.server.corpora.clom.DecoderInfo;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An decoder information class for the FLEx file format parser.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class FlexDecoderInfo implements DecoderInfo {
    /** a flag whether or not to include the "interlinear-text" element */
    public boolean inclITElement = true;

    /** a flag whether or not to include the "paragraph" element */
    public boolean inclParagraphElement = true;

    /** a flag indicating whether a total media duration has been specified */
    public boolean totalDurationSpecified = false;

    /** the total duration for the transcription */
    public long totalDuration = -1;

    /** the duration per smallest alignable element */
    public long perElementDuration = 200;

    /** the element for which the duration has been specified */

    //public String elementWithSpecifiedDuration = FlexConstants.WORD;
    /**
     * the "smallest" element that is time aligned and for which a
     * "perElementDuration" is specified or calculated
     */
    public String smallestWithTimeAlignment = FlexConstants.WORD;

    /**
     * a map containing element name to subdivision type
     * (TIME_SUBDIVISION/INCLUDED_IN) mappings. Not supported yet.
     */

    //public Map<String, String> subdivTypes = new HashMap<String, String>(5);
    /**
     * a flag to specify whether items of the same "type" but different "lang"
     * should be placed on the same tier (concatenated)
     */
    public boolean collapseLanguages = false;
    private String sourcePath = "";
    private List<MediaDescriptor> mediaDescriptors = null;
    /** a mapping of annotation id's of the form "a_<guid>" to a long array 
     * containing begin and end time */
    private HashMap<String, long[]> storedAlignment = null;

    /**
     * Creates a decoder info instance with default values.
     */
    public FlexDecoderInfo() {
        super();

        //fillSubDivTypes();
    }

    /**
     * Constructor with the source file path as parameter
     *
     * @param sourcePath the FLEx source file
     */
    public FlexDecoderInfo(String sourcePath) {
        super();
        this.sourcePath = sourcePath;

        //fillSubDivTypes();
    }

    /*
       private void fillSubDivTypes() {
           subdivTypes.put(FlexConstants.PARAGR,
               Constraint.stereoTypes[Constraint.INCLUDED_IN]);
           subdivTypes.put(FlexConstants.PHRASE,
               Constraint.stereoTypes[Constraint.INCLUDED_IN]);
           subdivTypes.put(FlexConstants.WORD,
               Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
           subdivTypes.put(FlexConstants.MORPH,
               Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
       }
     */
    /**
     * Returns the path to the source file
     *
     * @return the source file path or null
     */
    public String getSourceFilePath() {
        return sourcePath;
    }

    /**
     * Returns a list containing the media descriptors
     *
     * @return the list of media descriptors
     */
    public List<MediaDescriptor> getMediaDescriptors() {
        return mediaDescriptors;
    }

    /**
     * Sets the list of media descriptors.
     *
     * @param mediaDescriptors the list of media descriptors
     */
    public void setMediaDescriptors(List<MediaDescriptor> mediaDescriptors) {
        this.mediaDescriptors = mediaDescriptors;
    }

	/**
	 * Returns previous time alignment extracted from an eaf file.
	 * 
	 * @return the storedAlignment, a mapping of annotation guid-id to an array long[] {begin time, end time}
	 */
	public HashMap<String, long[]> getStoredAlignment() {
		return storedAlignment;
	}

	/**
	 * Sets the time alignment as extracted from an eaf file.
	 * 
	 * @param storedAlignment the storedAlignment to set
	 */
	public void setStoredAlignment(HashMap<String, long[]> storedAlignment) {
		this.storedAlignment = storedAlignment;
	}
    
}
