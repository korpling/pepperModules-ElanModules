package mpi.eudico.server.corpora.clom;


/**
 * DataTreeNode is implemented to administer the hierarchical structure of
 * Corpus data objects in the EUDICO hierarchical database. Knowledge about
 * this structure is needed for several purposes, for example, to be able to
 * delete a child of a specific node, or to pass on requests for information
 * to objects higher in the hierarchy.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 25-May-1998
 */
public interface DataTreeNode {
    /**
     * Gives the parent of this node in the Corpus data object hierarchy.
     *
     * @return the parent DataTreeNode
     */
    public DataTreeNode getParent();

    /**
     * Removes a child in the Corpus data hierarchy by deleting it's reference
     * to that child. The garbage collector will then actually delete the
     * child object.
     *
     * @param theChild the child node to be deleted
     */
    public void removeChild(DataTreeNode theChild);
}
