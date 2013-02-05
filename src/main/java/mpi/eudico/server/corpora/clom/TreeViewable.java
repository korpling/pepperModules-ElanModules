package mpi.eudico.server.corpora.clom;

import java.util.Vector;


/**
 * If an Object implements the TreeViewable interface it can be  used as a
 * content Object of a DynamicTreeNode.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 06-Mai-1998
 */
public interface TreeViewable {
    /**
     * Returns the name of the TreeViewable Object as it must be used in the
     * Tree.
     *
     * @return the name of the TreeViewable
     */
    public String getNodeName();

    /**
     * Informs whether this TreeViewable has TreeViewable children.
     *
     * @return true if this TreeViewable has TreeViewable children, false
     *         otherwise
     */
    public boolean isTreeViewableLeaf();

    /**
     * Returns a Vector with the TreeViewable children of this TreeViewable. If
     * this TreeViewable has no children an empty Vector must be returned
     * instead of null.
     *
     * @return the TreeViewable children
     */
    public Vector getChildren();
}
