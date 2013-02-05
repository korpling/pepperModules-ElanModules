package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TierSharedInfo;
import mpi.eudico.server.corpora.location.LocatorManager;

/**
 * TierSharedInfoImpl consists of attributes that can be shared by different
 * Tiers in different Transcriptions. For example, a TierSharedInfo attribute
 * is the Tier's CodeGroup, describing legal values for all fields in the
 * Tier's Tags. The purpose of TierSharedInfo is to facilitate reuse of
 * Tier setup information for a group of similarly coded Transcriptions.
 *
 * <p>MK:02/06/12<br>
 * Since the Tier constructor ignores the name variable,
 * you have to store the name of tier in this class yourself.
 * When using only one transcripition, shared info does not make much sense.
 * As tiernames are used as IDs, this means that
 * two tiers with the same name are identical.
 * Which means that only identical ties can share attributes.
 * </p>
 *
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 6-May-1999
 * @version Aug 2005 Identity removed
 */
public class TierSharedInfoImpl implements TierSharedInfo {

	/**
	 * The Tier's name.
	 */
	protected String tierName;

	/**
	 * Participant's name.
	 */
	protected String participantName;
	protected LocatorManager locatorManager;
	protected Tier tier;

	// Constructors
	public TierSharedInfoImpl(String theName, LocatorManager theLocatorMgr,
			Tier theTier) {
		tierName = theName;

		tier = theTier;		// specific to CHATTierSharedInfo, it is not really shared among Tiers

		locatorManager = theLocatorMgr;
		
		// HS aug 2005: don't create an object if it isn't used, see getCodeGroup
		// codeGroup = new CodeGroupImpl();
	}

	// TierSharedInfo interface methods

	/**
	 * Returns the name of the Tier. Tier names can be shared
	 * among similarly coded Transcriptions.
	 *
	 * @return	the Tier's name
	 */
	public String getTierName() {
		return tierName;
	}


	public void setTierName(String theName) {
		tierName = theName;
	}


	/**
	     * Returns the participant with whom this Tier is associated.
	     *
	     * @return DOCUMENT ME!
	     */
	public String getParticipant() {
	    return participantName;
	}

}

