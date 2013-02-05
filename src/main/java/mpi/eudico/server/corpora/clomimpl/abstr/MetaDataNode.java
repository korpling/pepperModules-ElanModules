package mpi.eudico.server.corpora.clomimpl.abstr;


/**
 * DOCUMENT ME!
 * $Id: MetaDataNode.java 4255 2005-08-17 13:34:41Z hasloe $
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public interface MetaDataNode {
    // methods to access meta data structure
    public MetaDataNode[] getMetaDataNodes();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasMetaData();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public LanguageResourceNode[] getLRNodes();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasLRs();

    // methods that provide access to meta data items themselves
    public String getTag();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getInfo();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isMetaData();
}
