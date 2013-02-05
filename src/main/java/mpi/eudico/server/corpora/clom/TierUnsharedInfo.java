package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.event.NotImplementedException;

/**
 * TierUnsharedInfo consists of attributes that are unique the Tier's
 * Transcription. Some of this interface's methods can not be implemented by
 * all Corpora and will throw a NotImplementedException.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 26-May-1998
 * @version Aug 2005 Identity removed
 */
public interface TierUnsharedInfo {
    /**
     * Gives the name of the person who has transcribed this Tier.
     *
     *
     * @return the Transcriber's name
     *
     * @exception NotImplementedException is thrown when a specific
     *            implementation     class is not able to return a Transcriber
     */
    public String getTranscriber()
        throws NotImplementedException;

    /**
     * Returns a string describing the exactness of time alignment of the
     * Tier's Tags. Is probably only implemented by a limited number of
     * Corpora.
     *
     * @return the quality of the tier's Tag alignment
     *
     * @exception NotImplementedException thrown when a specific implementation
     *            class is not able to determine a quality
     */
    public String getQuality()
        throws NotImplementedException;
}
