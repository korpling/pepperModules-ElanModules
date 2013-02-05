package mpi.eudico.server.corpora.clom;


/**
 * TierSetupDatabase is intended to provide a single access point for tier
 * setup information as TierBundles, TierSharedInfo objects and CodeTypes that
 * are available for a specific corpus. These objects can exist independent of
 * the existence of objects in the Data Tree  (DataTreeNodes). Because
 * TierSetupDatabase is a single access point, it has to be implemented
 * (possibly for each implementing corpus) as a Singleton.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 28-May-1998
 */
public interface TierSetupDatabase {
    //	public Set getCodeTypes();
}
