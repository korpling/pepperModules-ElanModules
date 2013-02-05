package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TierUnsharedInfo;
import mpi.eudico.server.corpora.event.NotImplementedException;



/**
 * TierUnsharedInfoImpl consists of attributes that are unique the Tier's
 * Transcription.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 6-May-1999
 * @version Aug 2005 Identity removed
 */
public class TierUnsharedInfoImpl implements TierUnsharedInfo {
    private Tier tier;

    /** Holds value of property DOCUMENT ME! */
    protected String transcriber;

    /** Holds value of property DOCUMENT ME! */
    protected String quality;

    /**
     * Creates a new TierUnsharedInfoImpl instance
     *
     * @param theTier DOCUMENT ME!
     */
    public TierUnsharedInfoImpl(Tier theTier) {
        tier = theTier;
    }

    /**
     * Gives the name of the person who has transcribed this Tier. This method
     * is implemented using a 'proxy' approach.
     *
     * @return the Transcriber's name
     *
     * @exception NotImplementedException is thrown when a specific
     *            implementation     class is not able to return a Transcriber
     */
    public String getTranscriber()
        throws NotImplementedException {
        	return transcriber;
        }

    /**
     * Returns a string describing the exactness of time alignment of the
     * Tier's Tags. This method is implemented using a 'proxy' approach.
     *
     * @return the quality of the tier's Tag alignment
     *
     * @exception NotImplementedException thrown when a specific implementation
     *            class is not able to determine a quality
     */
    public String getQuality()
        throws NotImplementedException {
        	return quality;
        }
}
