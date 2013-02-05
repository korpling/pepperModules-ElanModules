package mpi.eudico.server.corpora.location;

import java.util.HashMap;


/**
 * DOCUMENT ME!
 * $Id: LocatorManager.java 4255 2005-08-17 13:34:41Z hasloe $
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public abstract class LocatorManager {
    /** The list of DataLocators for all Identities using a Corpus. */
    protected HashMap dataLocatorList;

    /**
     * Creates a new LocatorManager instance
     */
    public LocatorManager() {
        dataLocatorList = new HashMap();
    }

    /**
     * Gives the DataLocator that is associated with a given Identity, if such
     * a  DataLocator exists. This method is to be implemented as a Factory
     * method, meaning that a DataLocator is created for an Identity, if it
     * does not yet exist.
     *
     * @return the DataLocator
     */
    public abstract DataLocator getDataLocator();

}
