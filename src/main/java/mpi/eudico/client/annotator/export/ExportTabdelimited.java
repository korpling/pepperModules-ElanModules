package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.util.CVEntry;
import mpi.eudico.util.TimeRelation;

import mpi.eudico.util.TimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.charset.UnsupportedCharsetException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * A class for exporting a or multiple transcriptions as tab delimited text,
 * whereby separate columns are created for each tier and the value of
 * spanning annotations can be repeated in rows of spanned annotations.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportTabdelimited {
    // defaults
    /** flags whether the begin time should be included in the output */
    public boolean includeBeginTime = true;

    /** flags whether the end time should be included in the output */
    public boolean includeEndTime = true;

    /** flags whether the duration should be included in the output */
    public boolean includeDuration = true;

    /** flags whether the time should be formatted as hh:mm:ss.ms */
    public boolean includeHHMM = true;

    /** flags whether the time should be formatted as ss.ms */
    public boolean includeSSMS = true;

    /** flags whether the time should be in milliseconds */
    public boolean includeMS = false;

    /** flags whether the time should be in SMPTE code */
    public boolean includeSMPTE = false;

    /** flags whether the time should be in PAL code */
    public boolean palFormat = false;
    
    /** flags whether the file name should be included */
    public boolean includeFileName = false;
    
    /** flags whether the file path should be included */
    public boolean includeFilePath = false;

    /** the offset to add to all begin and end times */
    public long mediaOffset = 0L; 
    
    /** flags whether the cv description should be included in the output*/
    public boolean includeCVDescrip = true;    

    /**
     * flags whether values of annotations spanning other annotations should be
     * repeated
     */
    public boolean repeatValues = true;

    /**
     * flags whether annotations of different "blocks" should be combined in
     * the same row  and if values should be repeated
     */
    public boolean combineBlocks = true;
    private final String TAB = "\t";
    private final String NEWLINE = "\n";
    private boolean multipleFileExport = false;

    /**
     * Creates a new ExportTabdelimited instance
     */
    public ExportTabdelimited() {
        super();
    }

    /**
     * Exports a single transcription. With a separate column for each selected tier.<br>
     * Note: first set the boolean's for inclusions and formats.
     *
     * @param transcription the transcription
     * @param tierNames the name of the tiers to include
     * @param exportFile the file to save to
     * @param charEncoding the encoding for the file, default utf-8
     * @param beginTime the begintime of the selection
     * @param endTime the end time of the selection
     *
     * @throws IOException if the export file is null
     */
    public void exportTiersColumnPerTier(Transcription transcription,
        String[] tierNames, File exportFile, String charEncoding,
        long beginTime, long endTime) throws IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(exportFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, charEncoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
        } catch (Exception ex) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + ex.getMessage());
        }

        List<String> includedTiers = new ArrayList<String>();

        if (tierNames == null) {
            // use all
            List tiers = transcription.getTiers();
            Tier t = null;

            for (int i = 0; i < tiers.size(); i++) {
                t = (Tier) tiers.get(i);
                includedTiers.add(t.getName());
            }
        } else {
            for (String name : tierNames) {
                includedTiers.add(name);
            }
        }

        writeHeaders(writer, includedTiers);

        writeTiersColumnPerTier(writer, transcription, includedTiers,
            beginTime, endTime);

        try {
            writer.close();
        } catch (IOException iioo) {
            iioo.printStackTrace();
        }
    }

    /**
     * Exports the selected tiers from the selected files to one tab delimited
     * file. There are separate columns per tier and the files are treated
     * sequentially; no attempts are made to put annotations from different
     * files into one row.  <br>
     * Note: first set the boolean's for inclusions and formats.
     *
     * @param files the list of transcription files
     * @param tierNames the name of the tiers to include
     * @param exportFile the file to save to
     * @param charEncoding the encoding for the file, default utf-8
     * @param beginTime the begintime of the selection
     * @param endTime the end time of the selection
     *
     * @throws IOException if the export file is null
     */
    public void exportTiersColumnPerTier(List<File> files, String[] tierNames,
        File exportFile, String charEncoding, long beginTime, long endTime)
        throws IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            throw new IOException("No files specified for export");
        }

        if ((tierNames == null) || (tierNames.length == 0)) {
            throw new IOException("No tiers specified for export");
        }

        multipleFileExport = true;

        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(exportFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, charEncoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
        } catch (Exception ex) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + ex.getMessage());
        }

        List<String> includedTiers = new ArrayList<String>();

        for (String name : tierNames) {
            includedTiers.add(name);
        }

        writeHeaders(writer, includedTiers);

        // create trancscriptions of files and write
        File file;
        TranscriptionImpl trans;

        for (int i = 0; i < files.size(); i++) {
            file = (File) files.get(i);

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());

                writeTiersColumnPerTier(writer, trans, includedTiers,
                    beginTime, endTime);
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                ClientLogger.LOG.warning("Could not handle file: " +
                    file.getAbsolutePath());
            }
        }

        try {
            writer.close();
        } catch (IOException iioo) {
            iioo.printStackTrace();
        }
    }

    /**
     * Note: first set the boolean's for inclusions and formats.
     *
     * @param writer the writer to write the results to
     * @param transcription the transcription
     * @param includedTiers the names of the tiers to include
     * @param beginTime the begin time of the selection
     * @param endTime the end time of the selection
     *
     * @throws IOException if there is no valid writer object
     * @throws NullPointerException if the transcription is null
     */
    public void writeTiersColumnPerTier(BufferedWriter writer,
        Transcription transcription, List<String> includedTiers,
        long beginTime, long endTime) throws IOException {
        if (transcription == null) {
            throw new NullPointerException("The transcription is null");
        }

        if (writer == null) {
            throw new IOException("No writer supplied to write to");
        }

        if (includedTiers == null) {
            includedTiers = new ArrayList<String>();

            // use all
            List tiers = transcription.getTiers();
            Tier t = null;

            for (int i = 0; i < tiers.size(); i++) {
                t = (Tier) tiers.get(i);
                includedTiers.add(t.getName());
            }
        }

        // first create "fully filled" blocks of annotations in the same tree,
        // but without deleting the rows only containing the toplevel tier 
        // (+ symbolically associated tiers)

        // create a tree
        DefaultMutableTreeNode rootNode = createTree(includedTiers,
                transcription);
        List<MinimalTabExportTableModel> allBlocks = new ArrayList<MinimalTabExportTableModel>();

        // loop over the "toplevel" tiers, find annotations within the selected interval
        // add the depending annotations to each and create a "filled" table
        DefaultMutableTreeNode node;

        // loop over the "toplevel" tiers, find annotations within the selected interval
        // add the depending annotations to each and create a "filled" table
        DefaultMutableTreeNode chNode;
        List<Annotation> allAnnotations = new ArrayList<Annotation>(100);      
        
        String tierName;
        TierImpl tier;
        

        for (int i = 0; i < rootNode.getChildCount(); i++) {
        	HashMap<String, String> cvEntryMap = null;
        	String cvName = null;
            CVEntry[] entries = null;
             
            node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            tierName = (String) node.getUserObject();
            tier = (TierImpl) transcription.getTierWithId(tierName);
                        
            if (tier != null) {
            	if(includeCVDescrip){
            		cvEntryMap = new HashMap<String, String>();
                    cvName = tier.getLinguisticType().getControlledVocabylaryName();
                	if(cvName != null){
                		entries = ((TranscriptionImpl)transcription).getControlledVocabulary(cvName).getEntries();
                		for(CVEntry cv : entries){
                        	cvEntryMap.put(cv.getValue(), cv.getDescription());
                		}
                	}
            	}
            	
                List annos = tier.getAnnotations();
                Annotation ann;
                Annotation ann2;

                for (int j = 0; j < annos.size(); j++) {
                    allAnnotations.clear();

                    ann = (Annotation) annos.get(j);

                    // create a block per toplevel annotation
                    if (ann != null) {
                        if (TimeRelation.overlaps(ann, beginTime, endTime)) {
                            allAnnotations.add(ann);

                            long b = ann.getBeginTimeBoundary();
                            long e = ann.getEndTimeBoundary();

                            Enumeration nodeEn = node.depthFirstEnumeration();

                            //Enumeration nodeEn = node.breadthFirstEnumeration();
                            while (nodeEn.hasMoreElements()) {
                                chNode = (DefaultMutableTreeNode) nodeEn.nextElement();

                                if (chNode == node) {
                                    continue;
                                }

                                tierName = (String) chNode.getUserObject();
                                tier = (TierImpl) transcription.getTierWithId(tierName);

                                if (tier != null) {
                                    List annos2 = tier.getAnnotations();

                                    for (int k = 0; k < annos2.size(); k++) {
                                        ann2 = (Annotation) annos2.get(k);

                                        if (ann2 != null) {
                                            if (TimeRelation.overlaps(ann2, b, e)) {
                                                allAnnotations.add(ann2);
                                            }

                                            if (ann2.getBeginTimeBoundary() > e) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            // with the current list of annotations fill a tablemodel
                            MinimalTabExportTableModel tm = null;
                            
                            if (repeatValues && combineBlocks) {
                            	tm = new MinimalTabExportTableModel(includedTiers,
                                        allAnnotations, cvEntryMap, true, true);
                            } else {
                            	tm = new MinimalTabExportTableModel(includedTiers,
                                        allAnnotations, cvEntryMap);
                            }

                            if (multipleFileExport) {
                            	if(includeFileName){
                            		tm.setFileName(transcription.getName());
                            	}
                            	if(includeFilePath){
                            		tm.setFilePath(transcription.getFullPath());
                            	}
                            }

                            if (combineBlocks) {
                                tm.setSpan(new long[] { b, e });
                                allBlocks.add(tm);
                            } else {
                                writeBlock(writer, tm);
                            }
                        }

                        if (ann.getBeginTimeBoundary() > endTime) {
                            break;
                        }
                    }
                }
            }
        }

        if (combineBlocks) {
            List<MinimalTabExportTableModel> removableBlocks = new ArrayList<MinimalTabExportTableModel>();

            // combine
            MinimalTabExportTableModel mtm1;

            // combine
            MinimalTabExportTableModel mtm2;
            long[] span1;
            long[] span2;

            for (int i = allBlocks.size() - 1; i >= 0; i--) {
                mtm2 = allBlocks.get(i);
                span2 = mtm2.getSpan();

                for (int j = 0; j < allBlocks.size(); j++) {
                    if (j == i) {
                        continue;
                    }

                    mtm1 = allBlocks.get(j);
                    span1 = mtm1.getSpan();

                    // full overlap, or partial overlap, consider merging rows
                    if ((span1[0] <= span2[0]) && (span1[1] >= span2[1])) {
                        boolean empty = mergeTables(mtm1, mtm2);

                        if (empty) {
                            removableBlocks.add(mtm2);
                        }
                    }
                }
            }

            if (removableBlocks.size() > 0) {
                for (int i = allBlocks.size() - 1; i >= 0; i--) {
                    if (removableBlocks.contains(allBlocks.get(i))) {
                        allBlocks.remove(i);
                    }
                }
            }

            // write the whole bunch
            for (int i = 0; i < allBlocks.size(); i++) {
                writeBlock(writer, allBlocks.get(i));
            }
        }
    }

    /**
     * Merges the rows from the second table with the first table (the first
     * table is the  destination). Removes merged rows from the second table.
     * This can lead to loss of a direct relation between rows.
     *
     * @param mtm1 the first table
     * @param mtm2 the second table
     *
     * @return true if all rows have been merged (second table is empty) false
     *         other wise
     */
    private boolean mergeTables(MinimalTabExportTableModel mtm1, MinimalTabExportTableModel mtm2) {
        // merge rows with identical begin and end time
        List curRow;

        // merge rows with identical begin and end time
        List otherRow;
        long l1;
        long l2;
        long l3;
        long l4;
        List<Integer> removals = new ArrayList<Integer>();
        Object val;

outerloop: 
        for (int i = 0; i < mtm2.getRows().size(); i++) {
            curRow = (List) mtm2.getRows().get(i);
            l1 = ((Long) curRow.get(1)).longValue();
            l2 = ((Long) curRow.get(2)).longValue();

            for (int j = 0; j < mtm1.getRows().size(); j++) {
                otherRow = (List) mtm1.getRows().get(j);
                l3 = ((Long) otherRow.get(1)).longValue();
                l4 = ((Long) otherRow.get(2)).longValue();

                if ((l1 == l3) && (l2 == l4)) {
                    removals.add(i);

                    for (int k = 3; k < curRow.size(); k++) {
                        val = curRow.get(k);

                        if (val != null) {
                            otherRow.set(k, val);
                        }
                    }

                    continue outerloop;
                } else if ((l3 <= l1) && (l4 >= l2)) {
                    for (int k = 3; k < otherRow.size(); k++) {
                        val = otherRow.get(k);

                        if (val != null) {
                            curRow.set(k, val);
                        }
                    }
                }
            }
        }

        //remove the "normal" removables, after merging
        for (int i = removals.size() - 1; i >= 0; i--) {
        	//System.out.println("Remove row: " + ((Integer) removals.get(i)).intValue());
            mtm2.getRows().remove(((Integer) removals.get(i)).intValue());
        }

        return mtm2.getRows().size() == 0;
    }

    /**
     * Writes the column headers; first time columns, then the tier columns and
     * optionally the file name column.
     *
     * @param writer the writer to write to
     * @param includedTiers the list of selected tiers
     *
     * @throws IOException any io exception
     */
    public void writeHeaders(BufferedWriter writer, List<String> includedTiers)
        throws IOException {
        if (includeBeginTime) {
            if (includeHHMM) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode") + TAB);
            }

            if (includeSSMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds") + TAB);
            }

            if (includeMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec") + TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        if (includeEndTime) {
            if (includeHHMM) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode") + TAB);
            }

            if (includeSSMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds") + TAB);
            }

            if (includeMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnEndTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec") + TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        if (includeDuration) {
            if (includeHHMM) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode") + TAB);
            }

            if (includeSSMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds") + TAB);
            }

            if (includeMS) {
                writer.write(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec") + TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    writer.write(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        for (String name : includedTiers) {
            writer.write(name);
            writer.write(TAB);
        }

        if(includeCVDescrip){
        	writer.write(ElanLocale.getString("EditCVDialog.Label.CVDescription"));
            writer.write(TAB);
        }
        
        if (multipleFileExport) {
        	if(includeFileName){
        		writer.write(ElanLocale.getString("Frame.GridFrame.ColumnFileName") + TAB);
        	}
        	if(includeFilePath){
        		writer.write(ElanLocale.getString("Frame.GridFrame.ColumnFilePath"));
        	}
        }

        writer.write(NEWLINE);
    }

    /**
     * Writes the contents of a block of annotations. If the first cell of a
     * row contains the Hidden marker, it is ignored.
     *
     * @param writer the writer to write to
     * @param tm the table model holding the data
     *
     * @throws IOException any
     */
    private void writeBlock(BufferedWriter writer, MinimalTabExportTableModel tm)
        throws IOException {
        if (tm == null) {
            ClientLogger.LOG.warning("No table model provided");
        }

        List<List> rows = tm.getRows();
        List row = null;
        long bt;
        long et;
        Object value;

        for (int i = 0; i < rows.size(); i++) {
            row = rows.get(i);
            
            if (row.get(0) == tm.HIDDEN) {
            	continue;
            }
            
            bt = ((Long) row.get(1)).longValue() + mediaOffset;
            et = ((Long) row.get(2)).longValue() + mediaOffset;

            if (includeBeginTime) {
                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(bt) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(bt / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(bt + TAB);
                }

                if (includeSMPTE) {
                    if (palFormat) {
                        writer.write(TimeFormatter.toTimecodePAL(bt) + TAB);
                    } else {
                        writer.write(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                    }
                }
            }

            if (includeEndTime) {
                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(et) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(et / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(et + TAB);
                }

                if (includeSMPTE) {
                    if (palFormat) {
                        if (palFormat) {
                            writer.write(TimeFormatter.toTimecodePAL(et) + TAB);
                        } else {
                            writer.write(TimeFormatter.toTimecodeNTSC(et) +
                                TAB);
                        }
                    }
                }
            }

            if (includeDuration) {
                long d = et - bt;

                if (includeHHMM) {
                    writer.write(TimeFormatter.toString(d) + TAB);
                }

                if (includeSSMS) {
                    writer.write(Double.toString(d / 1000.0) + TAB);
                }

                if (includeMS) {
                    writer.write(d + TAB);
                }
            }

            // write annotations in the columns
            for (int j = 3; j < row.size(); j++) {
                value = row.get(j);

                if (value != null) {
                    if (value instanceof String) {
                        writer.write(((String) value).replace(NEWLINE, " "));
                    } else {
                        writer.write(value.toString());
                    }
                }

                if (j != (row.size() - 1)) {
                    writer.write(TAB);
                }
            }

            if (tm.getFileName() != null) {
                writer.write(TAB + tm.getFileName());
            }
            
            if(tm.getFilePath() != null){
            	writer.write(TAB + tm.getFilePath());
            }

            writer.write(NEWLINE);
        }

        //writer.write(NEWLINE); 
    }

    /**
     * Creates a tree of t he included tiers.
     *
     * @param includedTiers the included tiers names
     * @param transcription the transcription
     *
     * @return the root of a tree
     */
    private DefaultMutableTreeNode createTree(List<String> includedTiers,
        Transcription transcription) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        List<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>(includedTiers.size());

        for (String name : includedTiers) {
            nodeList.add(new DefaultMutableTreeNode(name));
        }

        String tierName;
        String parentName;
        TierImpl tier;
        TierImpl parentTier;

        for (DefaultMutableTreeNode node : nodeList) {
            tierName = (String) node.getUserObject();
            tier = (TierImpl) transcription.getTierWithId(tierName);

            if (tier != null) {
                parentTier = (TierImpl) tier.getParentTier();

                if (parentTier == null) {
                    rootNode.add(node);
                } else {
                    parentName = parentTier.getName();

                    if (!includedTiers.contains(parentName)) {
                        // look for ancestors
                        while (true) {
                            parentTier = (TierImpl) parentTier.getParentTier();

                            if (parentTier == null) {
                                parentName = null;

                                break;
                            }

                            parentName = parentTier.getName();

                            if (includedTiers.contains(parentName)) {
                                break;
                            }
                        }
                    }

                    if (parentName == null) {
                        rootNode.add(node);
                    } else {
                        // find the parent node
                        for (DefaultMutableTreeNode node2 : nodeList) {
                            if (parentName.equals(node2.getUserObject())) {
                                node2.add(node);

                                break;
                            }
                        }
                    }
                }

                // check
                if (node.getParent() == null) {
                    ClientLogger.LOG.warning("Tier " + tierName +
                        " could not be added to a parent");
                    rootNode.add(node);
                }
            }
        } // tree created

        return rootNode;
    }
}
