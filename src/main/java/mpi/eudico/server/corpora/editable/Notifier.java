package mpi.eudico.server.corpora.editable;

/**
 * A Runnable object which only task it is to update a RemoteObserver If for
 * some reason the RemoteObserver can not be updated he is removed  from the
 * RemoteObservable's observer administration.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public class Notifier implements Runnable {
    private RemoteObserver observer;
    private RemoteObservable observable;
    private Object arg;

    /**
     * Construct the Notifier for a RemoteObservable RemoteObserver pair.
     *
     * @param observer the RemoteObserver that must be notified of an update
     * @param observable rhe RemoteObservable that wants to notify the
     *        RemoteObserver
     * @param arg a parameter that can contain extra update information
     */
    public Notifier(RemoteObserver observer, RemoteObservable observable,
        Object arg) {
        this.observer = observer;
        this.observable = observable;
        this.arg = arg;
    }

    /**
     * Try to update the RemoteObserver. If the RemoteObserver somehow is slow
     * in handling this update method it is no problem for other processes
     * because this update takes place in a separate Thread. If the update
     * fails due to some sort of an Exception the RemoteObserver is removed
     * from theRemoteObservables observer administration.
     */
    public void run() {
        try {
            observer.update(observable, arg);
        } catch (Exception e) {
            //	System.out.println("Lost contact with " + observer);
            try {
                observable.removeObserver(observer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
