package mpi.eudico.server.corpora.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: LocalDirectoryTree.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public class LocalDirectoryTree implements DirectoryTree {
    private File topNode;

    /**
     * Creates a new LocalDirectoryTree instance
     *
     * @param theTopNode DOCUMENT ME!
     */
    public LocalDirectoryTree(File theTopNode) {
        topNode = theTopNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getDirectories(File subDirectory) {
        Vector subDirectories = new Vector();
        File absolutePath = new File(topNode, subDirectory.getPath());

        //	File[] listing = absolutePath.listFiles();
        String[] listing = absolutePath.list();

        if (listing != null) {
            for (int i = 0; i < listing.length; i++) {
                //	File file = (File) listing[i];
                File file = new File(absolutePath, (String) listing[i]);

                if (file.isDirectory()) {
                    subDirectories.add(file);
                }
            }
        }

        return subDirectories;
    }

    /**
     * DOCUMENT ME!
     *
     * @param subDirectory DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getFiles(File subDirectory) {
        Vector files = new Vector();
        File absolutePath = new File(topNode, subDirectory.getPath());

        //	File[] listing = absolutePath.listFiles();
        String[] listing = absolutePath.list();

        if (listing != null) {
            for (int i = 0; i < listing.length; i++) {
                //	File file = (File) listing[i];
                File file = new File(absolutePath, (String) listing[i]);

                if (file.isFile()) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOwner(File theFile) {
        // not implemented yet
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
        // not implemented yet
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
        boolean containsDirs = false;

        File absolutePath = new File(topNode, subDirectory.getPath());

        //	File[] listing = absolutePath.listFiles();
        String[] listing = absolutePath.list();

        if (listing != null) {
            for (int i = 0; i < listing.length; i++) {
                //	File file = (File) listing[i];
                File file = new File(absolutePath, (String) listing[i]);

                if (file.isDirectory()) {
                    containsDirs = true;

                    break;
                }
            }
        }

        return containsDirs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPath() {
        return topNode.getPath();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierNames(File theFile) {
        Vector tierNames = new Vector();

        // find @Participants: line in theFile
        File file = new File(topNode, theFile.getPath());
        String participantLine = getParticipantLine(file);

        // parse this line, find name mnemonics
        StringTokenizer st = new StringTokenizer(participantLine, ":,\n");

        if (st.hasMoreTokens()) { // 'eat' Participants label
            st.nextToken();
        }

        while (st.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st.nextToken().trim());

            if (st2.hasMoreTokens()) {
                String tierName = st2.nextToken();
                tierNames.add(tierName);
            }
        }

        return tierNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     * @param theTierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getParticipant(File theFile, String theTierName) {
        String participant = null;

        // find @Participants: line in theFile
        File file = new File(topNode, theFile.getPath());
        String participantLine = getParticipantLine(file);

        // parse this line, find full description for mnemonic theTierName
        return participant;
    }

    private String getParticipantLine(File theFile) {
        String line = null;
        String pLine = null;
        boolean recording = false;

        try {
            FileReader fr = new FileReader(theFile);
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                if (line.startsWith("@Participants:")) {
                    recording = true;
                    pLine = line;
                } else if (recording == true) {
                    if (!(line.startsWith("@") || line.startsWith("*") ||
                            line.startsWith("%"))) {
                        // continuation of participants line
                        pLine += line;
                    } else { // new header line or block line, end recording
                        recording = false;

                        break;
                    }
                }
            }
        } catch (FileNotFoundException fex) {
            fex.printStackTrace();
        } catch (IOException iex) {
            iex.printStackTrace();
        }

        return pLine;
    }

    /**
     * Helper method to avoid copy and paste
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private final BufferedReader file2br(File file) throws IOException {
        /*
           A file is opened from the operating system.
           This stream of bytes could be a UTF-8 encoded unicode stream.
           If a file interpreted as UTF-8 contains isolatin-1, the file
           cannot be read. An Exception is thrown.
           Therefore, special care has to be taken when reading in UTF-8.
           As a first measure, the filename is used to decide if to read as UTF-8.
           This has to be changend in a future version.
           This is just done in order to include Unicode characters into Eudico.
         */
        Reader filereader;

        if (-1 != file.getName().lastIndexOf(".utf8.")) { // this means 'contains'
            filereader = new InputStreamReader(new FileInputStream(file),
                    "UTF-8");
        } else {
            // use the locale encoding.
            filereader = new FileReader(file);
        }

        BufferedReader br = new BufferedReader(filereader);

        return br;
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
        boolean recording = false;
        String line = null;
        String previousLine = null;
        Vector blocks = new Vector();
        HashMap chatBlock = null;

        try {
            File file = new File(topNode, theFile.getPath());
            BufferedReader br = file2br(file);

            while ((line = br.readLine()) != null) {
                if ((line.startsWith("*")) || (line.startsWith("@"))) { // new block

                    if (recording == true) {
                        // stop recording and output
                        recording = false;

                        if (chatBlock != null) {
                            blocks.add(chatBlock);
                        }
                    }

                    if (line.startsWith("*" + theTierName)) {
                        // start new recording
                        recording = true;

                        chatBlock = new HashMap();

                        // add line to new recording
                        previousLine = line;
                        addLineToBlock(line, chatBlock);
                    }
                } else { // other lines

                    if (recording == true) {
                        if ((line.startsWith("%")) || (line.startsWith("*"))) { // || (line.startsWith("@"))) {
                            previousLine = line;
                        } else { // no label, continuation of previous line
                            line = previousLine + line;
                        }

                        addLineToBlock(line, chatBlock);
                    }
                }
            }
        } catch (FileNotFoundException fex) {
            fex.printStackTrace();
        } catch (IOException iex) {
            iex.printStackTrace();
        }

        return blocks;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getCHATBlocks(File theFile) {
        boolean recording = false;
        String line = null;
        String previousLine = null;
        Vector blocks = new Vector();
        HashMap chatBlock = null;

        try {
            File file = new File(topNode, theFile.getPath());
            BufferedReader br = file2br(file);

            while ((line = br.readLine()) != null) {
                if (line.startsWith("*")) { // new block

                    // output
                    if (chatBlock != null) {
                        blocks.add(chatBlock);
                    }

                    // start new recording
                    chatBlock = new HashMap();

                    // add line to new recording
                    previousLine = line;
                    addLineToBlock(line, chatBlock);
                } else { // other lines

                    if (line.startsWith("%")) {
                        previousLine = line;
                        addLineToBlock(line, chatBlock);
                    } else if (!line.startsWith("@")) { // no label, continuation of previous line
                        line = previousLine + line;
                        addLineToBlock(line, chatBlock);
                    }
                }
            }

            // output last block
            if (chatBlock != null) {
                blocks.add(chatBlock);
            }
        } catch (FileNotFoundException fex) {
            fex.printStackTrace();
        } catch (IOException iex) {
            iex.printStackTrace();
        }

        return blocks;
    }

    private void addLineToBlock(String theLine, HashMap theBlock) {
        String label = null;
        String value = null;

        label = getLabelPart(theLine);
        value = getValuePart(theLine);

        if ((label != null) && (value != null)) {
            theBlock.put(label, value);
        }
    }

    private String getLabelPart(String theLine) {
        String label = null;

        int index = theLine.indexOf(':');

        if (index > 0) {
            label = theLine.substring(0, index);
        }

        return label;
    }

    private String getValuePart(String theLine) {
        String value = null;

        int index = theLine.indexOf(':');

        if (index < (theLine.length() - 2)) {
            value = theLine.substring(index + 1).trim();
        }

        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theFile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMediaFileName(File theFile) {
        String line = null;
        String mediaFileName = null;

        try {
            File file = new File(topNode, theFile.getPath());

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                if (line.startsWith("%snd:")) {
                    // parse this line, second token is media file name.
                    StringTokenizer st = new StringTokenizer(line);

                    if (st.hasMoreTokens()) { // 'eat' %snd label
                        st.nextToken();
                    }

                    if (st.hasMoreTokens()) {
                        mediaFileName = st.nextToken();
                    }

                    // strip off possible double quotes
                    if (mediaFileName.startsWith("\"")) {
                        mediaFileName = mediaFileName.substring(1);
                    }

                    if (mediaFileName.endsWith("\"")) {
                        mediaFileName = mediaFileName.substring(0,
                                mediaFileName.length() - 1);
                    }

                    //	if (mediaFileName.endsWith(".sd")) {
                    //		mediaFileName = mediaFileName.substring(0, mediaFileName.length() - 3) + ".wav";
                    //	}
                }
            }
        } catch (FileNotFoundException fex) {
            fex.printStackTrace();
        } catch (IOException iex) {
            iex.printStackTrace();
        }

        return mediaFileName;
    }
}
