package mpi.eudico.server.corpora.editable;


/**
 * Interface for an Object that can be edited
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public interface Editable {
    /**
     * DOCUMENT ME!
     *
     * @param status DOCUMENT ME!
     * @param identity DOCUMENT ME!
     * @param editMonitor DOCUMENT ME!
     *
     * @throws LockedException DOCUMENT ME!
     * @throws NotLockedException DOCUMENT ME!
     */
    public void setLocked(boolean status, EditIdentity identity,
        EditMonitor editMonitor)
        throws LockedException, NotLockedException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isLocked();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public EditIdentity getOwnerIdentity();

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     * @param identity DOCUMENT ME!
     *
     * @throws LockedException DOCUMENT ME!
     * @throws NotLockedException DOCUMENT ME!
     */
    public void setEditableDataValue(Object value, EditIdentity identity)
        throws LockedException, NotLockedException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getEditableDataValue();
}
