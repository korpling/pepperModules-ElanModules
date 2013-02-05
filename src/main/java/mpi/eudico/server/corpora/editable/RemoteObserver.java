package mpi.eudico.server.corpora.editable;


/**
 * Interface for an Object that acts as a RemoteObserver. A RemoteObserver must
 * be updatable by the Object that is being observed.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public interface RemoteObserver {
    /**
     * DOCUMENT ME!
     *
     * @param observable DOCUMENT ME!
     * @param arg DOCUMENT ME!
     *
     */
    public void update(RemoteObservable observable, Object arg);
}
