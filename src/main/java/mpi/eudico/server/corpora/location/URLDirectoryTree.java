package mpi.eudico.server.corpora.location;

import java.io.File;

import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: URLDirectoryTree.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public class URLDirectoryTree implements DirectoryTree {
    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getDirectories(File subDirectory) {
        return new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getFiles(File subDirectory) {
        return new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOwner(File theFile) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAccessRights(File theFile) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean containsDirectories(File subDirectory) {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPath() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierNames(File theFile) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     * @param theTierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getCHATBlocks(File theFile, String theTierName) {
        return new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getCHATBlocks(File theFile) {
        return new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMediaFileName(File theFile) {
        return null;
    }
}
