package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TreeViewable;

import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: MDItemNode.java 6043 2006-04-20 09:37:36Z klasal $
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public class MDItemNode implements MetaDataNode,
    TreeViewable {
    private String tag;
    private String info;
    private String description;
    private MetaDataNode[] mdNodes;

    /**
     * Creates a new MDItemNode instance
     *
     * @param theTag DOCUMENT ME!
     * @param theInfo DOCUMENT ME!
     * @param theDescription DOCUMENT ME!
     */
    public MDItemNode(String theTag, String theInfo, String theDescription) {
        tag = theTag;
        info = theInfo;
        description = theDescription;
    }

    // implementation of MetaDataNode interface
    public MetaDataNode[] getMetaDataNodes() {
        return mdNodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasMetaData() {
        if (mdNodes != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public LanguageResourceNode[] getLRNodes() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasLRs() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTag() {
        return tag;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getInfo() {
        return info;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isMetaData() {
        return true;
    }

    // implementation of TreeViewable interface

    /**
     * Returns the name of the TreeViewable Object as it must be used in the
     * Tree.
     *
     * @return the name of the TreeViewable
     */
    public String getNodeName() {
        return getTag();
    }

    /**
     * Informs whether this TreeViewable has TreeViewable children.
     *
     * @return true if this TreeViewable has TreeViewable children, false
     *         otherwise
     */
    public boolean isTreeViewableLeaf() {
        return !hasMetaData();
    }

    /**
     * Returns a Vector with the TreeViewable children of this TreeViewable. If
     * this TreeViewable has no children an empty Vector must be returned
     * instead of null.
     *
     * @return the TreeViewable children
     */
    public Vector getChildren() {
        return new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param metaDataItems DOCUMENT ME!
     */
    public void addMetaData(MetaDataNode[] metaDataItems) {
        mdNodes = metaDataItems;
    }
}
