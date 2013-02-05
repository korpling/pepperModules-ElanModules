package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.util.ACMEditableObject;

import java.util.Vector;


/**
 * A Tag is EUDICO's basic unit of transcription. It has an (optional) begin
 * time and end time, as well as several textual fields. The order and types
 * of these fields are determined by the CodeGroup that is associated with the
 * Tag's Tier.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 21-Apr-1999
 */
public interface Tag extends Comparable, ACMEditableObject {
    /**
     * Gives the Tag's begin time in milliseconds.
     *
     * @return begin time in milliseconds
     */
    public long getBeginTime();

    /**
     * Gives the Tag's end time in milliseconds.
     *
     * @return end time in milliseconds
     */
    public long getEndTime();

    /**
     * Returns a list of textual values for the Tag's time interval, in the
     * order specified by a CodeGroup.
     *
     * @return a list of textual values
     */
    public Vector getValues();

    /**
     * Returns the Tier of which this Tag is a component.
     *
     * @return the Tag's Tier
     */
    public Tier getTier();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIndex();

    /**
     * Adjusts index of Tag in the Transcription. Should only be called by an
     * entity that is responsible for mananging these indices (usually the
     * Transcription's MetaTime.
     */
    public void setIndex(int theIndex);

    /**
     * Returns true if the Tag is aligned with the time axis of Transcription's
     * media. An unaligned Tag has zero begin and end times.
     *
     * @return DOCUMENT ME!
     */
    public boolean isTimeAligned();

    /**
     * Returns true if this Tag comes after theTag. Implementation specific Tag
     * ordering is handled here (used when adding to MetaTime).
     * Comparable.compareTo orders Tags on index.
     *
     * @return DOCUMENT ME!
     */
    public boolean isAfter(Tag theTag);
}
