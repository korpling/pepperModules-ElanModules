package mpi.eudico.server.corpora.editable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;


/**
 * An abstract class that implements all functionality for a remote editable
 * Object except the set and get EditableDataValue methods that must be
 * implemented by the classes that extend from this abstract class.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public abstract class RemoteEditable extends UnicastRemoteObservable
    implements Editable, ActionListener {
    private EditIdentity ownerIdentity;
    private EditMonitor ownerEditMonitor;
    private Timer livelinessTimer;
    private long timeOfLastSaveAction;
    private boolean locked;

    /**
     * Construct the RemoteEditable
     */
    public RemoteEditable() {
        ownerIdentity = null;
        ownerEditMonitor = null;
        locked = false;
        livelinessTimer = new Timer(EditPreferences.LIVELINESS_CHECK_PERIOD,
                this);
    }

    /**
     * return the Identity of the current owner of this RemoteEditable.
     *
     * @return DOCUMENT ME!
     */
    public synchronized EditIdentity getOwnerIdentity() {
        return ownerIdentity;
    }

    /**
     * This method must be implemented by the editable data item class that
     * extends this abstract class. The method is protected to prevent
     * unwanted changes of the data value by unidentified callers.
     */
    abstract protected void setEditableDataValue(Object value);

    /**
     * This method must be implemented by the editable data item class that
     * extends this abstract class
     *
     * @return DOCUMENT ME!
     */
    abstract public Object getEditableDataValue();

    /**
     * By calling this method a caller can set the data value if the callers
     * Identity is equal to the Identity of the current owner of this
     * RemoteEditable.
     *
     * @param value the new data value
     * @param identity the identity of the person that wants to change the data
     *        value
     *
     * @throws LockedException DOCUMENT ME!
     * @throws NotLockedException DOCUMENT ME!
     */
    public synchronized void setEditableDataValue(Object value,
        EditIdentity identity)
        throws LockedException, NotLockedException {
        if (!locked) {
            throw new NotLockedException();
        } else if (!identity.equals(ownerIdentity)) {
            throw new LockedException();
        } else {
            setEditableDataValue(value);
            notifyObservers("ValueChange");
            timeOfLastSaveAction = System.currentTimeMillis();
        }
    }

    /**
     * returns the lock status of this RemoteEditable
     *
     * @return DOCUMENT ME!
     */
    public synchronized boolean isLocked() {
        return locked;
    }

    /**
     * Sets the lock status of this RemoteEditable. The lock is only succesfull
     * if the object is not already locked by another Identity. The unlock is
     * only succesfull if the current owners Identity is equal to the Identity
     * parameter.
     *
     * @param mustBeLocked true = set locked,  false = set unlocked
     * @param identity the Identity of the person that wants to lock/unlock
     * @param editMonitor an object that must be provided by the editor to
     *        check his liveliness
     *
     * @throws LockedException DOCUMENT ME!
     */
    public synchronized void setLocked(boolean mustBeLocked,
        EditIdentity identity, EditMonitor editMonitor)
        throws LockedException {
        if (mustBeLocked) {
            if ((identity != null) && (editMonitor != null) &&
                    (ownerIdentity == null)) {
                locked = true;
                ownerIdentity = identity;
                ownerEditMonitor = editMonitor;
                notifyObservers("StateChange");
                timeOfLastSaveAction = System.currentTimeMillis();
                livelinessTimer.start();
            } else if (ownerIdentity.equals(identity)) {
                if (!locked) {
                    locked = true;
                    notifyObservers("StateChange");
                }
            } else {
                throw new LockedException();
            }
        } else {
            if (locked) {
                if (ownerIdentity.equals(identity)) {
                    locked = false;
                    ownerIdentity = null;
                    ownerEditMonitor = null;
                    notifyObservers("StateChange");
                    livelinessTimer.stop();
                } else {
                    throw new LockedException();
                }
            }
        }
    }

    /**
     * This method is periodically called by the livelinessTimer. It checks
     * through the  owners EditMonitor if the owner is still in edit mode.
     * This is done to prevent  an indefinite lock of the RemoteEditable by an
     * owner that stopped editing without properly notifying this
     * RemoteEditable that it can be unlocked.
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
        if (ownerEditMonitor != null) {
            System.out.println("CHECK LIVELINESS OF " +
                ownerIdentity.getName() + " (" + ownerIdentity.getNumber() +
                ")");

            try {
                ownerEditMonitor.isCallable();

                long now = System.currentTimeMillis();

                if ((now - timeOfLastSaveAction) > EditPreferences.MAX_TIME_BETWEEN_SAVE_ACTIONS) {
                    System.out.println("CHECK EDIT MODE OF " +
                        ownerIdentity.getName() + " (" +
                        ownerIdentity.getNumber() + ")");
                    livelinessTimer.stop();

                    if (!ownerEditMonitor.isInEditMode()) {
                        locked = false;
                        ownerEditMonitor = null;
                        ownerIdentity = null;
                        notifyObservers("StateChange");
                    } else {
                        timeOfLastSaveAction = System.currentTimeMillis();
                        livelinessTimer.start();
                    }
                }
            } catch (Exception ex) {
                locked = false;
                ownerEditMonitor = null;
                ownerIdentity = null;
                notifyObservers("StateChange");
            }
        }
    }
}
