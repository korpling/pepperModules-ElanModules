package mpi.eudico.server.corpora.util;


/**
 * RemoteListIterator is identical to java's ListIterator, except for the
 * throws clause. RemoteListIterator is implemented by the RemoteObject
 * implementing MetaTime.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 16-Apr-1999
 */
public interface RemoteListIterator {
    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     */
    public void add(Object o);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasNext();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasPrevious();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object next();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int nextIndex();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object previous();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int previousIndex();

    /**
     * DOCUMENT ME!
     */
    public void remove();

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     */
    public void set(Object o);
}
