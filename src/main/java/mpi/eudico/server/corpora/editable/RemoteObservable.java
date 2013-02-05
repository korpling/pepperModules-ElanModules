package mpi.eudico.server.corpora.editable;


/**
 * Interface for an Object that can be remotely observed
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public interface RemoteObservable {
    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     */
    public void addObserver(RemoteObserver observer);

    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     */
    public void removeObserver(RemoteObserver observer);

    /**
     * DOCUMENT ME!
     */
    public void notifyObservers();

    /**
     * DOCUMENT ME!
     *
     * @param arg DOCUMENT ME!
     */
    public void notifyObservers(Object arg);
}
