package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.util.SharedDataObject;


/**
 * TierSharedInfo consists of attributes that can be shared by different Tiers
 * in different Transcriptions. For example, a TierSharedInfo attribute is the
 * Tier's CodeGroup, describing legal values for all fields in the Tier's
 * Tags. The purpose of TierSharedInfo is to facilitate reuse of Tier setup
 * information for a group of similarly coded Transcriptions.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 11-Sep-1998
 */
public interface TierSharedInfo extends SharedDataObject {
    /**
     * Returns the name of the Tier. Tier names can be shared among similarly
     * coded Transcriptions.
     *
     * @return the Tier's name
     */
    public String getTierName();

    /**
     * DOCUMENT ME!
     *
     * @param theName DOCUMENT ME!
     *
     */
    public void setTierName(String theName);

    /**
     * Gives the Participant with whom the Tier is associated. Several
     * participants can be involved in the recorded event that is represented
     * in the Tier's Transcription.
     *
     * @return the Tier's participant
     */
    public String getParticipant();

}
