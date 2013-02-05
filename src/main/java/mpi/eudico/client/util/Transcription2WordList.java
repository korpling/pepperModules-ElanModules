package mpi.eudico.client.util;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.charset.UnsupportedCharsetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * Extracts unique words from a selection of tiers and writes the results to a
 * text file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class Transcription2WordList implements ClientLogger {
    final private String NEWLINE = "\n";
    private String delimiters = " \t\n\r\f.,!?\"\'";

    /**
     * Creates a new Transcription2WordList instance
     */
    public Transcription2WordList() {
        super();
    }

    /**
     * Exports the unique words from a selection of tiers.<br>
     * Note: test shoulds be done with respect to performance (use of a
     * TreeSet instead of an ArrayList followed by a sort.
     *
     * @param transcription the transcription containing the tiers
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     * @param countOccurrences if true the frequency of the words/annotations is also exported
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWords(Transcription transcription, List<String> tierNames,
        File exportFile, String charEncoding, String delimiters, boolean countOccurrences)
        throws IOException {
    	if (countOccurrences) {
    		exportWordsAndCount(transcription, tierNames, exportFile, charEncoding, delimiters);
    		return;
    	}
    	
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if (transcription == null) {
            LOG.severe("No transcription specified for wordlist");

            return;
        }

        if (tierNames == null) {
            LOG.warning("No tiers specified for the wordlist: using all tiers");
            tierNames = new ArrayList(transcription.getTiers().size());

            for (int i = 0; i < transcription.getTiers().size(); i++) {
                tierNames.add(((TierImpl) (transcription.getTiers().get(i))).getName());
            }
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        ArrayList uniqueWords = getUniqueWords(transcription, tierNames);
        Collections.sort(uniqueWords);

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        for (int i = 0; i < uniqueWords.size(); i++) {
            writer.write((String) uniqueWords.get(i));
            writer.write(NEWLINE);
        }

        writer.close();
    }

    /**
     * Exports the unique words from a selection of tiers from a number of files.<br>
     * Note: tests should be done with respect to performance (use of a
     * TreeSet instead of an ArrayList followed by a sort.
     *
     * @param files a list of eaf files
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     * @param countOccurrences if true the frequency of the words/annotations is also exported
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWords(List<File> files, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters, boolean countOccurrences) throws IOException {
    	if (countOccurrences) {
    		exportWordsAndCount(files, tierNames, exportFile, charEncoding, delimiters);
    		return;
    	}
    	
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            LOG.warning("No files specified for export");
            throw new IOException("No files specified for export");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        ArrayList uniqueWords = new ArrayList();

        ArrayList transWords;
        File file;
        TranscriptionImpl trans;

        for (int i = 0; i < files.size(); i++) {
            file = (File) files.get(i);

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());
                transWords = getUniqueWords(trans, tierNames);

                Object wo;

                for (int j = 0; j < transWords.size(); j++) {
                    wo = transWords.get(j);

                    if (!uniqueWords.contains(wo)) {
                        uniqueWords.add(wo);
                    }
                }
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                LOG.severe("Could not handle file: " + file.getAbsolutePath());
                LOG.severe(ex.getMessage());
            }
        }

        Collections.sort(uniqueWords);

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        for (int i = 0; i < uniqueWords.size(); i++) {
            writer.write((String) uniqueWords.get(i));
            writer.write(NEWLINE);
        }

        writer.close();
    }

    /**
     * Creates a list of unique words in the specified tiers from the specified
     * transcription.
     *
     * @param transcription the transcription
     * @param tierNames the tiers
     *
     * @return the words
     */
    private ArrayList getUniqueWords(Transcription transcription, List tierNames) {
        ArrayList uniqueWords = new ArrayList();

        if (transcription == null) {
            LOG.severe("No transcription specified to extract words from");

            return uniqueWords;
        }

        TierImpl t;
        ArrayList annos = new ArrayList();
        Annotation ann;
        String token;
        StringTokenizer tokenizer;

        if (tierNames != null) {
            for (int i = 0; i < tierNames.size(); i++) {
                t = (TierImpl) transcription.getTierWithId((String) tierNames.get(
                            i));

                if (t != null) {
                    annos.addAll(t.getAnnotations());
                } else {
                    LOG.warning("No tier with name: " + tierNames.get(i));
                }
            }
        } else {
            List tiers = transcription.getTiers();

            for (int i = 0; i < tiers.size(); i++) {
                t = (TierImpl) tiers.get(i);

                if (t != null) {
                    annos.addAll(t.getAnnotations());
                } else {
                    LOG.warning("No tier with name: " + tierNames.get(i));
                }
            }
        }

        for (int i = 0; i < annos.size(); i++) {
            ann = (Annotation) annos.get(i);

            if (ann != null) {
                if (ann.getValue().length() > 0) {
                    if (delimiters.length() > 0) {
                        tokenizer = new StringTokenizer(ann.getValue(),
                                delimiters);

                        while (tokenizer.hasMoreTokens()) {
                            token = tokenizer.nextToken();

                            if (!uniqueWords.contains(token)) {
                                uniqueWords.add(token);
                            }
                        }
                    } else {
                        if (!uniqueWords.contains(ann.getValue())) {
                            uniqueWords.add(ann.getValue());
                        }
                    }
                }
            } else {
                LOG.warning("Annotation is null");
            }
        }

        return uniqueWords;
    }

    /**
     * Exports the unique words and their frequencies from a selection of tiers from a number of files.
     *
     * @param transcription a transcription
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWordsAndCount(Transcription transcription, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters) throws IOException {
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }
        
        if (transcription == null) {
            LOG.severe("No transcription specified for wordlist");

            return;
        }      

        if (tierNames == null) {
            LOG.warning("No tiers specified for the wordlist: using all tiers");
            tierNames = new ArrayList<String> (transcription.getTiers().size());

            for (int i = 0; i < transcription.getTiers().size(); i++) {
                tierNames.add(((TierImpl) (transcription.getTiers().get(i))).getName());
            }
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }
        
        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();
        addUniqueWordsAndCount(uniqueWords, transcription, tierNames);
        
        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        Iterator<String> keyIt = uniqueWords.keySet().iterator();
        String key = null;
        
        while (keyIt.hasNext()) {
        	key = keyIt.next();
        	writer.write(key);
        	writer.write("\t" + uniqueWords.get(key).intValue);
            writer.write(NEWLINE);
        }

        writer.close();
    }
        
    /**
     * Exports the unique words and their frequencies from a selection of tiers from a number of files.
     *
     * @param files a list of eaf files
     * @param tierNames a list of the selected tier names
     * @param exportFile the file to export to
     * @param charEncoding the encoding to use for the export file
     * @param delimiters the token delimiters
     *
     * @throws IOException if no file has been passed or when writing to the
     *         file fails
     */
    public void exportWordsAndCount(List<File> files, List<String> tierNames, File exportFile,
        String charEncoding, String delimiters) throws IOException {
        if (exportFile == null) {
            LOG.warning("No destination file specified for export");
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            LOG.warning("No files specified for export");
            throw new IOException("No files specified for export");
        }

        if (delimiters != null) {
            this.delimiters = delimiters;
        }

        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();

        File file;
        TranscriptionImpl trans;

        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());
                addUniqueWordsAndCount(uniqueWords, trans, tierNames);

            } catch (Exception ex) {
                // catch any exception that could occur and continue
                LOG.severe("Could not handle file: " + file.getAbsolutePath());
                LOG.severe(ex.getMessage());
            }
        }

        // write the words
        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        Iterator<String> keyIt = uniqueWords.keySet().iterator();
        String key = null;
        
        while (keyIt.hasNext()) {
        	key = keyIt.next();
        	writer.write(key);
        	writer.write("\t" + uniqueWords.get(key).intValue);
            writer.write(NEWLINE);
        }

        writer.close();
    }

    
    /**
     * Adds the (unique) words in the specified tiers from the specified
     * transcription to the specified map and updates their frequency.
     *
     * @param uniqueWords a map containing word - frequency pairs
     * @param transcription the transcription
     * @param tierNames the tiers
     */
    private void addUniqueWordsAndCount(Map<String, MutableInt> uniqueWords,Transcription transcription, 
    		List<String> tierNames) {
    	
        if (transcription == null) {
            LOG.severe("No transcription specified to extract words from");

            return;
        }

        TierImpl t;
        Annotation ann;
        List annos;
        String token;
        StringTokenizer tokenizer;
        List<String> actTierNames = null;
        
        if (tierNames == null || tierNames.size() == 0) {
        	actTierNames = new ArrayList<String>();
            List tiers = transcription.getTiers();

            for (int i = 0; i < tiers.size(); i++) {
                t = (TierImpl) tiers.get(i);

                if (t != null) {
                    actTierNames.add(t.getName());
                } else {
                    LOG.warning("Tier is null: " + i);
                }
            }
        } else {
        	actTierNames = tierNames;
        }

        if (actTierNames.size() > 0) {
            for (int j = 0; j < actTierNames.size(); j++) {
                t = (TierImpl) transcription.getTierWithId(actTierNames.get(j));

                if (t != null) {
                	annos = t.getAnnotations();
                	int numAnnos = annos.size();
                	
                	for (int i = 0; i < numAnnos; i++) {
                        ann = (Annotation) annos.get(i);

                        if (ann != null) {
                            if (ann.getValue().length() > 0) {
                                if (delimiters.length() > 0) {
                                    tokenizer = new StringTokenizer(ann.getValue(),
                                            delimiters);

                                    while (tokenizer.hasMoreTokens()) {
                                        token = tokenizer.nextToken();

                                        if (!uniqueWords.containsKey(token)) {
                                        	uniqueWords.put(token, new MutableInt(1));
                                        } else {
                                        	uniqueWords.get(token).intValue++;
                                        }
                                    }
                                } else {
                                    if (!uniqueWords.containsKey(ann.getValue())) {
                                        uniqueWords.put(ann.getValue(), new MutableInt(1));
                                    } else {
                                    	uniqueWords.get(ann.getValue()).intValue++;
                                    }
                                }
                            }
                        } else {
                            LOG.warning("Annotation is null");
                        }
                    }
                } else {
                    LOG.warning("No tier with name: " + actTierNames.get(j));
                }
            }
        }
    }
}
