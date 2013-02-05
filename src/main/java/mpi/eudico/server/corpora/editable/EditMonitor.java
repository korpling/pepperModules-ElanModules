package mpi.eudico.server.corpora.editable;


/**
 * Interface for EditMonitor functionality
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public interface EditMonitor {
    /**
     * Check if the RMI connection to the RemoteEditor still exists
     */
    public void isCallable();

    /**
     * inform the caller if the RemoteEditor is still in edit mode
     *
     * @return DOCUMENT ME!
     */
    abstract public boolean isInEditMode();
}
