package mpi.eudico.server.corpora.location;

import java.io.File;

import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: DirectoryTree.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public interface DirectoryTree {
    /**
     * DOCUMENT ME!
     *
     * @param theTopNode DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getDirectories(File theTopNode);

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getFiles(File subDirectory);

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOwner(File theFile);

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAccessRights(File theFile);

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean containsDirectories(File subDirectory);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPath();

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierNames(File theFile);

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     * @param theTierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getCHATBlocks(File theFile, String theTierName);

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getCHATBlocks(File theFile);

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMediaFileName(File theFile);
}
