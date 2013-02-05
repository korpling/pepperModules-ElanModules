package mpi.eudico.client.annotator.search.model;

import java.io.File;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.ContentQuery;

import mpi.search.model.ProgressListener;
import mpi.search.model.SearchEngine;
import mpi.search.query.model.Query;


/**
 * $Id: EAFMultipleFileSearchEngine.java 8348 2007-03-09 09:43:13Z klasal $
 * $Author$ $Version$
 *
 */
public class EAFMultipleFileSearchEngine implements SearchEngine {
    private final ProgressListener progressListener;

    /**
     * Creates a new EAFMultipleFileSearchEngine object.
     *
     * @param progressListener DOCUMENT ME!
     */
    public EAFMultipleFileSearchEngine(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     *
     *
     * @param regex DOCUMENT ME!
     * @param files DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static ContentQuery createQuery(String regex, File[] files)
        throws Exception {
        AnchorConstraint ac = new AnchorConstraint("", regex, 0L, 0L, "", true,
                false, null);
        ContentQuery query = new ContentQuery(ac, new EAFType(), files);

        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query
     *
     * @throws Exception DOCUMENT ME!
     */
    public void executeThread(ContentQuery query) throws Exception {
        EAFMultipleFileSearchHandler handler = new EAFMultipleFileSearchHandler(query);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        File[] files = query.getFiles();

        try {
            SAXParser saxParser = factory.newSAXParser();

            // iterate over the EAF Files to do the searching stuf
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                handler.newFile(file);

                try {
                    saxParser.parse(file, handler);
                } catch (SAXException e) {
                    throw new SAXException(file.toString() + ":\n" +
                        e.getMessage());
                }

                if (progressListener != null) {
                    progressListener.setProgress((int) (((i + 1) * 100.0) / files.length));
                }
            }
        }
        // stop of thread can cause ConcurrentModificationException
        // (will be ignored since it has no further consequences)
        catch (ConcurrentModificationException e) {
        }
    }

    /**
    	 *
    	 */
    public static void main(String[] args) {
        List dirs = new ArrayList();
        List paths = new ArrayList();

        // look on lux02 in /srv/testcorpus/eafs/ for the dobes data
        dirs.add(System.getProperty("user.dir") + "/resources/testdata/elan");

        // dirs.add("D:/Data/eafs/Kuikuro");
        // dirs.add("D:/Data/eafs/TOFA");
        // paths.add("D:/Data/eafs/Trumai/Data/Linguistic/NaturalUse/monological/recorded/description/Media/RG01Photos.eaf");
        // paths.add("niet bestaande troep");

        /*
                        final File[] files = EAFMultipleFileUtilities.getUniqueEAFFilesIn(dirs,
                                        paths);
                        EAFMultipleFileSearchEngine eafGoogle = new EAFMultipleFileSearchEngine(
                                        new SearchListener() {
                                                public void executionStarted() {
                                                }

                                                public void executionStopped() {
                                                }

                                                public void searchInterrupted() {
                                                }

                                                public void handleException(Exception e) {
                                                }
                                        });

                        String regexp = "ye.*"; // ".*";//

                        try {
                                eafGoogle.execute(createQuery(regexp, files));

                                ContentResult result = (ContentResult) eafGoogle.getResult();
                                for (int i = 1; i <= result.getMatchCount(); i++) {
                                        EAFMultipleFileMatch match = (EAFMultipleFileMatch) result
                                                        .getMatch(i);
                                        System.out.println("file:        " + match.getFileName());
                                        System.out.println("tier:        " + match.getTierName());
                                        System.out.println("left context:  " + match.getLeftContext());
                                        System.out.println("value:     " + match.getValue());
                                        System.out.println("time:    " + match.getBeginTimeBoundary()
                                                        + " - " + match.getEndTimeBoundary());
                                        System.out.println("ann index:   " + match.getIndex());
                                        System.out.println("right context:   "
                                                        + match.getRightContext());

                                        System.out.println("");

                                        // limit printing to 5 results
                                        if (i > 5) {
                                                break;
                                        }
                                }

                                for (int i = 0; i < eafGoogle.tierNames.size(); i++) {
                                        // System.out.println("tierName " + tierNames.elementAt(i));
                                }
                        } catch (Exception e) {
                                e.printStackTrace();
                        } */
    }

    /**
     *
     *
     * @param query DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }
}
