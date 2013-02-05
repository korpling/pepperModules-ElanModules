package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TreeViewable;


/**
 * DOCUMENT ME!
 * $Id: LanguageResourceNode.java 6043 2006-04-20 09:37:36Z klasal $
 * @author $Author$
 * @version $Revision$
 */
public interface LanguageResourceNode extends MetaDataNode, TreeViewable {
    /**
     * DOCUMENT ME!
     *
     * @param metaDataItems DOCUMENT ME!
     */
    public void addMetaData(MetaDataNode[] metaDataItems);

    /**
     * DOCUMENT ME!
     *
     * @param fmt DOCUMENT ME!
     */
    public void setFormat(String fmt);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFormat();
}
