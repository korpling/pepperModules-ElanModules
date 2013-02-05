package mpi.eudico.server.corpora.clomdefault;

import mpi.eudico.server.corpora.location.DataLocator;
import mpi.eudico.server.corpora.location.DirectoryTree;
import mpi.eudico.server.corpora.location.LocatorManager;


/**
 * Extends abstract class LocatorManager Encapsulates a DirectoryTree and could
 * restrict access to it (does not).
 * @version Aug 2005 Identity removed
 */
public class DefaultLocatorManager extends LocatorManager {
    private DefaultDataLocator locator;

    /**
     * Uses a DefaultDataLocator for the DirectoryTree
     *
     * @param theTree ???
     */
    public DefaultLocatorManager(DirectoryTree theTree) {
        locator = new DefaultDataLocator(theTree);
    }

    /**
     * Implementing abstract method from LocatorManager.
     *
     * @return DOCUMENT ME!
     */
    public DataLocator getDataLocator() {
        return this.locator;
    }
}
