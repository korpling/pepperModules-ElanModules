package mpi.eudico.server.corpora.location;

/**
 * DataLocatorList is implemented by all Corpus data objects that can be shared
 * by multiple Identities. For each Identity there exists a DataLocator
 * reference in the object's DataLocatorList. If appropriate, a
 * DataLocatorList can create a DataLocator for an Identity in
 * 'getDataLocator'. For example, GestureCorpus creates DataLocators that are
 * used for all it's GestureTranscriptions.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 25-May-1998
 * @version Aug 2005 Identity removed
 */
public interface DataLocatorList {
    /**
     * Adds a DataLocator to the implementing object's list
     *
     * @param theLocator the DataLocator to be added
     */
    public void attachDataLocator(DataLocator theLocator);

    /**
     * Removes a DataLocator from the implementing object's list
     *
     * @param theLocator theDataLocator to be removed
     */
    public void detachDataLocator(DataLocator theLocator);

    /**
     * Gives the DataLocator that is associated with a given Identity from the
     * implementing object's list, if such a DataLocator exists. Some
     * implementations may implement this method as a Factory method, meaning
     * that a DataLocator is created for an Identity, if it does not yet
     * exist.
     *
     * @return the DataLocator
     */
    public DataLocator getDataLocator();
}
