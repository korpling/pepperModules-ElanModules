package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.server.corpora.util.SharedDataObject;

import java.util.Vector;


/**
 * A Tier represents one 'stream' of consecutive labels of time intervals.
 * Transcriptions can contain multiple Tiers. Tiers contain Tags, that
 * possibly contain multiple codes referring to the same time interval. Tier
 * attributes are divided in two groups, one group, TierUnsharedInfo, consists
 * of attributes that are unique the Tier's Transcription, the other group,
 * TierSharedInfo, consists of attributes that can be shared by different
 * Tiers in different Transcriptions. For example, a TierSharedInfo attribute
 * is the Tier's CodeGroup, describing legal values for all fields in the
 * Tier's Tags.
 * <h1>
 * 
 * <p>
 * MK:02/06/20<br> GOAL: For all tiers in one transcription, the members (Name,
 * Participant) are unique.<br>
 * STATE: For all tiers in one transcription, the member (Name) is unique.<br>
 * </p>
 * return the participant of 'this' tier     public String getParticipant()
 * 
 * <p>
 * MK:02/06/20<br> GOAL: replace Tags by Annotations<br>
 * </p>
 * return the Annotations of 'this' tier     public Vector getAnnotations()
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 1-Jun-1999
 * @version Aug 2005 Identity removed
 */
public interface Tier extends SharedDataObject, DataTreeNode,
    ACMEditableObject {
    /**
     * Gives the Tier's name.
     * 
     * <p>
     * MK:02/06/20<br>name is unique, should be name/participant pair.
     * </p>
     *
     * @return the name of the Tier
     */
    public String getName();

    /**
     * Returns a group of Tier attributes that can be shared among
     * Transcriptions.
     * 
     * <p>
     * MK:02/06/20<br>remove.
     * </p>
     *
     * @return the TierSharedInfo attribute group
     */
    public TierSharedInfo getTierSharedInfo();

    /**
     * Return a group of Tier attributes that is unique for the Tier's
     * Transcription.
     * 
     * <p>
     * MK:02/06/20<br>remove.
     * </p>
     *
     * @return the TierUnsharedInfo attribute group
     */
    public TierUnsharedInfo getTierUnsharedInfo();

    /**
     * Returns a list of a Tier's Tags, sorted by begin time.
     * 
     * <p>
     * MK:02/06/20<br>replace by getAnnotations().
     * </p>
     *
     * @return list of Tags, sorted by begin time
     */
    // HS Nov 2009 unused, removed
    //public Vector getTags();

    /**
     * Adds a Tag to the Tier.
     * 
     * <p>
     * MK:02/06/20<br>replace by addAnnotation().
     * </p>
     */
    //public void addTag(Tag theTag);

    /**
     * Defines the time scale of the annotations on this Tier. The basic time
     * units of the Tier's Tags are milliseconds. TimeScale gives a
     * multiplication factor that e.g. can be used when creating Tags.
     * Example: PAL video frames last 40 msec. If the original video
     * annotations  on the Tier use frame counts, the time scale is 40.0.
     *
     * @return DOCUMENT ME!
     */
    public double getTimeScale();
}
