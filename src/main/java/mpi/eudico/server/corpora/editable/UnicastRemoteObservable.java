package mpi.eudico.server.corpora.editable;

import java.util.Iterator;
import java.util.Vector;


/**
 * An Object that can be remotely observed
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public class UnicastRemoteObservable implements RemoteObservable {
    private Vector observers;

    /**
     * Construct the remote observable and initialize the observer
     * administration
     */
    public UnicastRemoteObservable() {
        observers = new Vector();
    }

    /**
     * Add a RemoteObserver to the observer administration.
     *
     * @param observer the RemoteObserver that wants to observe this
     *        RemoteObservable
     */
    public synchronized void addObserver(RemoteObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove a RemoteObserver from the observer administration.
     *
     * @param observer the RemoteObserver that no longer wants to observe this
     *        RemoteObservable
     */
    public synchronized void removeObserver(RemoteObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    /**
     * Notify all interested RemoteObservers. Each RemoteObserver is updated in
     * a separate Thread to prevent an unreponsive RemoteObserver blocking the
     * update to the other  RemoteObservers.
     */
    public synchronized void notifyObservers() {
        Iterator observerIterator = observers.iterator();

        while (observerIterator.hasNext()) {
            (new Thread(new Notifier((RemoteObserver) observerIterator.next(),
                    this, null))).start();
        }
    }

    /**
     * Notify all interested RemoteObservers. Each RemoteObserver is updated in
     * a separate Thread to prevent an unreponsive RemoteObserver blocking the
     * update to the other  RemoteObservers.
     *
     * @param arg some parameter that gives extra information to the
     *        RemoteObserver
     */
    public synchronized void notifyObservers(Object arg) {
        Iterator observerIterator = observers.iterator();

        while (observerIterator.hasNext()) {
            (new Thread(new Notifier((RemoteObserver) observerIterator.next(),
                    this, arg))).start();
        }
    }
}
