package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import mpi.eudico.server.corpora.clom.DecoderInfo;

import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TimeSlotRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TimeSlotRecordComparator;

import mpi.eudico.server.corpora.util.ServerLogger;

import mpi.eudico.util.TimeFormatter;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A Parser for comma separated values or tab-delimited text files. Annotations
 * are extracted based on information on the structure of the  file. This
 * structure is stored in a decoder info object.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DelimitedTextParser extends Parser implements ServerLogger {
    private DelimitedTextDecoderInfo decoderInfo;
    private DelimitedTextReader reader;
    private String lastParsedFile;
    private final String TS_ID_PREFIX = "ts";
    private final String ANN_PREFIX = "a";
    private ArrayList lingTypeRecords = new ArrayList();
    private ArrayList tierNames = new ArrayList();
    private ArrayList timeOrder = new ArrayList(); // in the end a list of timeslot id's
    private ArrayList timeSlots = new ArrayList(); // of TimeSlotRecords
    private HashMap recordsPerTier = new HashMap();

    /**
     * Creates a new DelimitedTextParser instance
     */
    public DelimitedTextParser() {
        super();
    }

    /**
     * Creates a new DelimitedTextParser instance
     *
     * @param decoderInfo the decoder info object containing information on how
     *        to interpret the rows
     */
    public DelimitedTextParser(DelimitedTextDecoderInfo decoderInfo) {
        super();
        this.decoderInfo = decoderInfo;
        parse(decoderInfo.getSourceFilePath());
    }

    /**
     * Reads the delimited text file (specified in the decoder info object) and
     * creates  annotation and tier objects etc. based on the information in
     * the decoder info object.
     *
     * @param fileName the file to parse
     */
    public void parse(String fileName) {
        if ((fileName == null) || fileName.equals(lastParsedFile)) {
            return;
        }

        // reset
        lingTypeRecords.clear();
        tierNames.clear();
        timeOrder.clear();
        timeSlots.clear();
        recordsPerTier.clear();

        try {
            reader = new DelimitedTextReader(fileName);

            if (decoderInfo == null) {
            } else {
                reader.setDelimiter(decoderInfo.getDelimiter()); // default tab

                if (decoderInfo.isSingleAnnotationPerRow()) {
                    parseSingleAnnColumn();
                    sortTimeSlots();
                } else {
                    parseMultiAnnColumns();
                    sortTimeSlots();
                }
            }
        } catch (FileNotFoundException fnfe) {
            LOG.warning("The file is not found: " + fileName);

            return;
        }

        lastParsedFile = fileName;
    }

    /**
     * Parse a table file with 1 annotation per row.
     */
    private void parseSingleAnnColumn() {
        int[] annColumn = decoderInfo.getAnnotationColumns(); // should be length 1 in this case
        int annIndex = -1;

        if ((annColumn != null) && (annColumn.length >= 1)) {
            annIndex = annColumn[0];
        }

        int tierIndex = decoderInfo.getTierColumnIndex();
        int btIndex = decoderInfo.getBeginTimeColumn();
        int etIndex = decoderInfo.getEndTimeColumn();
        int durIndex = decoderInfo.getDurationColumn();

        int[] inclColumns = decoderInfo.getIncludedColumns();
        Map colToTierName = decoderInfo.getColumnsWithTierNames();
        String defTier = "Tier-1";

        // check if the tiername is in the header
        if ((annIndex > -1) && (colToTierName != null)) {
            Iterator indexIt = colToTierName.keySet().iterator();
            Integer keyInt;

            while (indexIt.hasNext()) {
                keyInt = (Integer) indexIt.next();

                if (keyInt.intValue() == annIndex) {
                    Object tName = colToTierName.get(keyInt);

                    if (tName instanceof String) {
                        defTier = (String) tName;
                    }
                }
            }
        }

        // check the indices and calculate result indices on the basis of the included columns array
        for (int i = 0; i < inclColumns.length; i++) {
            if (inclColumns[i] == annIndex) {
                annIndex = i;
            } else if (inclColumns[i] == tierIndex) {
                tierIndex = i;
            } else if (inclColumns[i] == btIndex) {
                btIndex = i;
            } else if (inclColumns[i] == etIndex) {
                etIndex = i;
            } else if (inclColumns[i] == durIndex) {
                durIndex = i;
            }
        }

        try {
            List rows = reader.getRowDataForColumns(decoderInfo.getFirstRowIndex(),
                    inclColumns);
            String[] row = null;
            long bt;
            long et;
            long dur;
            String ann;
            String curTier = defTier;
            int tsCount = 1;
            int annCount = 1;
            AnnotationRecord annRec;
            TimeSlotRecord tsRec;

            for (int i = 0; i < rows.size(); i++) {
                row = (String[]) rows.get(i);

                if ((btIndex > -1) && (btIndex < row.length)) {
                    bt = TimeFormatter.toMilliSeconds(row[btIndex]);
                } else {
                    bt = -1;
                }

                if ((etIndex > -1) && (etIndex < row.length)) {
                    et = TimeFormatter.toMilliSeconds(row[etIndex]);
                } else if ((durIndex > -1) && (durIndex < row.length)) {
                    // use duration
                    dur = TimeFormatter.toMilliSeconds(row[durIndex]);

                    if ((dur == -1) || (bt == -1)) {
                        et = -1; // if there is no begintime but there is a duration, ignore it
                    } else {
                        et = bt + dur;
                    }
                } else {
                    et = -1;
                }

                if ((annIndex > -1) && (annIndex < row.length)) {
                    ann = row[annIndex];
                } else {
                    ann = "";
                }

                if ((tierIndex > -1) && (tierIndex < row.length)) {
                    curTier = row[tierIndex];

                    if (curTier.length() == 0) {
                        curTier = defTier;
                    }
                } else {
                    curTier = defTier;
                }

                if (!tierNames.contains(curTier)) {
                    tierNames.add(curTier);
                    recordsPerTier.put(curTier, new ArrayList());
                }

                annRec = new AnnotationRecord();
                annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
                annRec.setValue(ann);
                annRec.setAnnotationId(ANN_PREFIX + annCount++);
                tsRec = new TimeSlotRecord(tsCount++, bt);
                annRec.setBeginTimeSlotRecord(tsRec);
                timeSlots.add(tsRec);
                tsRec = new TimeSlotRecord(tsCount++, et);
                annRec.setEndTimeSlotRecord(tsRec);
                timeSlots.add(tsRec);

                //annotationRecords.add(annRec);
                ((List) recordsPerTier.get(curTier)).add(annRec);
            }
        } catch (IOException ioe) {
            LOG.severe("Error retrieving rowdata: " + ioe.getMessage());
        }

        //
        List records;
        Iterator valIt = recordsPerTier.values().iterator();
        AnnRecordComparator comp = new AnnRecordComparator();

        while (valIt.hasNext()) {
            records = (List) valIt.next();
            Collections.sort(records, comp);
        }

        // calculate unaligned slots per tier, then reorder all slots and regenerate the id's.
        String tierName;
        Iterator tierIt = recordsPerTier.keySet().iterator();

        while (tierIt.hasNext()) {
            tierName = (String) tierIt.next();
            records = (List) recordsPerTier.get(tierName);
            calculateUnalignedSlots(records);
        }

        // correct overlaps??
        valIt = recordsPerTier.values().iterator();

        while (valIt.hasNext()) {
            records = (List) valIt.next();
            correctOverlaps(records);
        }
    }

    /**
     * Parse a table file with more than 1 annotation per row, with, possibly,
     * the tier name  in the 'header' or first row.
     */
    private void parseMultiAnnColumns() {
        // check if all info that is expected is availble
        // int[] annColumns = decoderInfo.getAnnotationColumns();
        int[] inclColumns = decoderInfo.getIncludedColumns();
        Map orgColsToTierName = decoderInfo.getColumnsWithTierNames();
        Map colsToTierName = new HashMap(orgColsToTierName.size());

        int btIndex = decoderInfo.getBeginTimeColumn();
        int etIndex = decoderInfo.getEndTimeColumn();
        int durIndex = decoderInfo.getDurationColumn();

        // check the indices and calculate result indices on the basis of the included columns array
        Integer colIndex;
        String tierName;
        Iterator colIter = orgColsToTierName.keySet().iterator();

        while (colIter.hasNext()) {
            colIndex = (Integer) colIter.next();

            for (int i = 0; i < inclColumns.length; i++) {
                if (inclColumns[i] == colIndex.intValue()) {
                    tierName = (String) orgColsToTierName.get(colIndex);
                    colsToTierName.put(new Integer(i), tierName);

                    if (!tierNames.contains(tierName)) {
                        tierNames.add(tierName);
                        recordsPerTier.put(tierName, new ArrayList());
                    }
                }
            }
        }

        Integer[] tierCols = new Integer[colsToTierName.size()];
        colIter = colsToTierName.keySet().iterator();

        int z = 0;

        while (colIter.hasNext()) {
            tierCols[z] = (Integer) colIter.next();
            z++;
        }

        for (int i = 0; i < inclColumns.length; i++) {
            if (inclColumns[i] == btIndex) {
                btIndex = i;
            } else if (inclColumns[i] == etIndex) {
                etIndex = i;
            } else if (inclColumns[i] == durIndex) {
                durIndex = i;
            }
        }

        // start parsing rows
        Map slotToSlots = new HashMap();
        String refTier = null; // for single calculation of timeslots

        try {
            List rows = reader.getRowDataForColumns(decoderInfo.getFirstRowIndex(),
                    inclColumns);
            String[] row = null;
            long bt;
            long et;
            long dur;
            String ann;
            String curTier;
            int tsCount = 1;
            int annCount = 1;
            AnnotationRecord annRec;
            TimeSlotRecord tsRec;
            TimeSlotRecord curRefBTTS;
            TimeSlotRecord curRefETTS;

            for (int i = 0; i < rows.size(); i++) {
                curRefBTTS = null;
                curRefETTS = null;
                row = (String[]) rows.get(i);

                if ((btIndex > -1) && (btIndex < row.length)) {
                    bt = TimeFormatter.toMilliSeconds(row[btIndex]);
                } else {
                    bt = -1;
                }

                if ((etIndex > -1) && (etIndex < row.length)) {
                    et = TimeFormatter.toMilliSeconds(row[etIndex]);
                } else if ((durIndex > -1) && (durIndex < row.length)) {
                    // use duration
                    dur = TimeFormatter.toMilliSeconds(row[durIndex]);

                    if ((dur == -1) || (bt == -1)) {
                        et = -1; // if there is no begintime but there is a duration, ignore it
                    } else {
                        et = bt + dur;
                    }
                } else {
                    et = -1;
                }

                for (int j = 0; j < tierCols.length; j++) {
                    if ((tierCols[j].intValue() > -1) &&
                            (tierCols[j].intValue() < row.length)) {
                        ann = row[tierCols[j].intValue()];
                    } else {
                        ann = "";
                    }

                    curTier = (String) colsToTierName.get(tierCols[j]);

                    if ((j == 0) && (refTier == null)) {
                        refTier = curTier;
                    }

                    /*
                       if (!tierNames.contains(curTier)) {
                           tierNames.add(curTier);
                           recordsPerTier.put(curTier, new ArrayList());
                       }
                     */
                    annRec = new AnnotationRecord();
                    annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
                    annRec.setValue(ann);
                    annRec.setAnnotationId(ANN_PREFIX + annCount++);
                    tsRec = new TimeSlotRecord(tsCount++, bt);
                    annRec.setBeginTimeSlotRecord(tsRec);
                    timeSlots.add(tsRec);
                    tsRec = new TimeSlotRecord(tsCount++, et);
                    annRec.setEndTimeSlotRecord(tsRec);
                    timeSlots.add(tsRec);

                    //annotationRecords.add(annRec);
                    ((List) recordsPerTier.get(curTier)).add(annRec);

                    if (j == 0) {
                        curRefBTTS = annRec.getBeginTimeSlotRecord();
                        curRefETTS = annRec.getEndTimeSlotRecord();
                        slotToSlots.put(curRefBTTS, new ArrayList(5));
                        slotToSlots.put(curRefETTS, new ArrayList(5));
                    } else {
                        ((ArrayList) slotToSlots.get(curRefBTTS)).add(annRec.getBeginTimeSlotRecord());
                        ((ArrayList) slotToSlots.get(curRefETTS)).add(annRec.getEndTimeSlotRecord());
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.severe("Error retrieving rowdata: " + ioe.getMessage());
        }

        // calculate unaligned slots for the ref tier, update 'depending' time slots
        List records = (List) recordsPerTier.get(refTier);
        // sort records
        Collections.sort(records, new AnnRecordComparator());

        calculateUnalignedSlots(records);
        // correct overlaps
        correctOverlaps(records);

        // now update the referring time slots
        ArrayList refSlots;
        TimeSlotRecord keyRec;
        TimeSlotRecord valRec;
        Iterator slotIter = slotToSlots.keySet().iterator();

        while (slotIter.hasNext()) {
            keyRec = (TimeSlotRecord) slotIter.next();
            refSlots = (ArrayList) slotToSlots.get(keyRec);

            for (int i = 0; i < refSlots.size(); i++) {
                valRec = (TimeSlotRecord) refSlots.get(i);
                valRec.setValue(keyRec.getValue());
            }
        }
    }

    /**
     * Sorts the time slot records using a TimeSlotRecordComparator and then
     * regenerates the id's.
     */
    private void sortTimeSlots() {
        if ((timeSlots != null) && (timeSlots.size() > 1)) {
            Collections.sort(timeSlots, new TimeSlotRecordComparator());

            TimeSlotRecord tsr;

            for (int i = 0; i < timeSlots.size(); i++) {
                tsr = (TimeSlotRecord) timeSlots.get(i);
                tsr.setId(i + 1);
            }
        }
    }

    /**
     * Calculates time values for (series of) unaligned time slots for the
     * annotation records of one tier.
     *
     * @param records a list containing AnnotationRecord objects
     */
    private void calculateUnalignedSlots(List records) {
        if ((records == null) || (records.size() == 0)) {
            return;
        }

        AnnotationRecord annRec;
        AnnotationRecord nextRec;
        TimeSlotRecord tsBRec;
        TimeSlotRecord tsERec;
        long curAdv = 0L;
        long avgDur = decoderInfo.getDefaultDuration();

        for (int i = 0; i < records.size(); i++) {
            annRec = (AnnotationRecord) records.get(i);
            tsBRec = annRec.getBeginTimeSlotRecord();
            tsERec = annRec.getEndTimeSlotRecord();

            if (tsBRec.getValue() < 0) {
                if (i > 0) {
                    if (tsERec.getValue() < 0) {
                        tsBRec.setValue(curAdv); // same as last end time
                    } else {
                        tsBRec.setValue(Math.max(curAdv,
                                tsERec.getValue() -
                                decoderInfo.getDefaultDuration()));
                    }
                } else {
                    tsBRec.setValue(0);
                }
            } else {
                curAdv = Math.max(curAdv, tsBRec.getValue());
            }

            if (tsERec.getValue() < 0) {
                // find the next aligned slot
                int numUnalignedSlots = 1;
                long nextTime = 0L;

                for (int j = i + 1; j < records.size(); j++) {
                    nextRec = (AnnotationRecord) records.get(j);

                    if (nextRec.getBeginTimeSlotRecord().getValue() >= 0) {
                        nextTime = nextRec.getBeginTimeSlotRecord().getValue();

                        break;
                    }

                    if (nextRec.getEndTimeSlotRecord().getValue() < 0) {
                        numUnalignedSlots++;
                    } else {
                        nextTime = nextRec.getEndTimeSlotRecord().getValue();

                        break;
                    }
                }

                if (numUnalignedSlots > 0) {
                    if (nextTime == 0) { // if there are no aligned slots at all
                        nextTime = curAdv +
                            (numUnalignedSlots * decoderInfo.getDefaultDuration());
                    }

                    avgDur = (nextTime - curAdv) / numUnalignedSlots;

                    avgDur = Math.min(avgDur, decoderInfo.getDefaultDuration());

                    int j = i;

                    for (int k = 0;
                            ((j < records.size()) && (k < numUnalignedSlots));
                            j++, k++) {
                        nextRec = (AnnotationRecord) records.get(j);

                        if (k == 0) {
                            nextRec.getEndTimeSlotRecord()
                                   .setValue(curAdv + avgDur);
                        } else {
                            if (nextRec.getBeginTimeSlotRecord().getValue() < 0) {
                                nextRec.getBeginTimeSlotRecord()
                                       .setValue(curAdv + (avgDur * k));
                            }

                            if (nextRec.getEndTimeSlotRecord().getValue() < 0) {
                                nextRec.getEndTimeSlotRecord()
                                       .setValue(curAdv + (avgDur * (k + 1)));
                            }
                        }
                    }

                    curAdv = nextTime;
                    i = j - 1;
                } else {
                    tsERec.setValue(Math.min(nextTime,
                            curAdv + decoderInfo.getDefaultDuration()));
                    curAdv = tsERec.getValue();
                }
            } else {
                // there is an endtime
                curAdv = tsERec.getValue();
            }
        }
    }

    /**
     * Tries to detect and correct possible overlaps within a single tier.
     *
     * @param annRecords the annotation records of one tier
     */
    private void correctOverlaps(List annRecords) {
        AnnotationRecord annRec;
        AnnotationRecord nextRec;
        TimeSlotRecord tsBRec;
        TimeSlotRecord tsERec;

        for (int i = 0; i < (annRecords.size() - 1); i++) {
            annRec = (AnnotationRecord) annRecords.get(i);
            nextRec = (AnnotationRecord) annRecords.get(i + 1);
            tsERec = annRec.getEndTimeSlotRecord();
            tsBRec = nextRec.getBeginTimeSlotRecord();

            if (tsERec.getValue() > tsBRec.getValue()) {
                tsERec.setValue(tsBRec.getValue());
            }
        }
    }

    //########### Interface getters, not all applicable ######################
    /**
     * Returns an empty List
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return an empty list
     */
    public ArrayList getMediaDescriptors(String fileName) {
        parse(fileName);

        return new ArrayList();
    }

    /**
     * Returns a list of Linguistic Type Records
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of ling. type records, by default only one lin. type
     */
    public ArrayList getLinguisticTypes(String fileName) {
        parse(fileName);

        if (lingTypeRecords.size() == 0) {
            LingTypeRecord lt = new LingTypeRecord();
            lt.setLingTypeId("default");
            lt.setTimeAlignable("true");
            lt.setGraphicReferences("false");

            lingTypeRecords.add(lt);
        }

        return lingTypeRecords;
    }

    /**
     * Returns an ordered list of time slot id's (Strings)
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of time slot id's
     */
    public ArrayList getTimeOrder(String fileName) {
        parse(fileName);
        timeOrder.clear();

        TimeSlotRecord tsr;

        for (int i = 0; i < timeSlots.size(); i++) {
            tsr = (TimeSlotRecord) timeSlots.get(i);
            timeOrder.add(TS_ID_PREFIX + tsr.getId());
        }

        return timeOrder;
    }

    /**
     * Returns a map containing id - time key value pairs
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a map containing id - time key value pairs
     */
    public HashMap getTimeSlots(String fileName) {
        parse(fileName);

        HashMap tsMap = new HashMap(timeSlots.size());
        TimeSlotRecord tsr;

        for (int i = 0; i < timeSlots.size(); i++) {
            tsr = (TimeSlotRecord) timeSlots.get(i);
            // note: could use the timeOrder objects (if we are sure of the order in
            // which the methods are called)?
            tsMap.put(TS_ID_PREFIX + tsr.getId(), Long.toString(tsr.getValue()));
        }

        return tsMap;
    }

    /**
     * Returns a list of tier names.
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of tier names
     */
    public ArrayList getTierNames(String fileName) {
        parse(fileName);

        return tierNames;
    }

    /**
     * Returns null
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return null
     */
    public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        return null;
    }

    /**
     * Returns the linguistic type name for a tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return the linguistic type name, defaults to "default"
     */
    public String getLinguisticTypeIDOf(String tierName, String fileName) {
        parse(fileName);

        return "default";
    }

    /**
     * Returns the parent tier name for a tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return the parent tier name, null by default
     */
    public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        // parent - child relations not in a del text file.
        return null;
    }

    /**
     * Returns a list of annotation records for the specified tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of annotation records
     */
    public ArrayList getAnnotationsOf(String tierName, String fileName) {
        parse(fileName);

        ArrayList records = (ArrayList) recordsPerTier.get(tierName);

        if (records != null) {
            return records;
        }

        return new ArrayList();
    }

    /**
     * Sets the decoder info object. Should be called before calling parse or
     * any of the getters.
     *
     * @param decoderInfo The decoderInfo to set.
     */
    public void setDecoderInfo(DecoderInfo decoderInfo) {
        if (decoderInfo instanceof DelimitedTextDecoderInfo) {
            this.decoderInfo = (DelimitedTextDecoderInfo) decoderInfo;
        }
    }

    /**
     * Special kind of Annotation Record Comparator.
     *
     * @author Han Sloetjes
     */
    private class AnnRecordComparator implements Comparator {
        /**
         * Creates a new AnnRecordComparator instance
         */
        public AnnRecordComparator() {
            super();
        }

        /**
         * Compares 2 AnnotationRecords. First compares the begin timeslot
         * records, next the end timeslot records and then the id's.<br>
         * Assumes that the annotation records do have a timeslot record
         * reference!
         *
         * @param o1 the first annotation record
         * @param o2 the second annotation record
         *
         * @return -1 if the first is less, 1 if the first is greater, 0
         *         otherwise
         */
        public int compare(Object o1, Object o2) {
            AnnotationRecord ar1 = (AnnotationRecord) o1;
            AnnotationRecord ar2 = (AnnotationRecord) o2;

            if ((ar1.getBeginTimeSlotRecord().getValue() > -1) &&
                    (ar2.getBeginTimeSlotRecord().getValue() > -1) &&
                    (ar1.getBeginTimeSlotRecord().getValue() < ar2.getBeginTimeSlotRecord()
                                                                      .getValue())) {
                return -1;
            }

            if ((ar1.getBeginTimeSlotRecord().getValue() > -1) &&
                    (ar2.getBeginTimeSlotRecord().getValue() > -1) &&
                    (ar1.getBeginTimeSlotRecord().getValue() > ar2.getBeginTimeSlotRecord()
                                                                      .getValue())) {
                return 1;
            }

            // begin time equal or undefined
            if ((ar1.getEndTimeSlotRecord().getValue() > -1) &&
                    (ar2.getEndTimeSlotRecord().getValue() > -1) &&
                    (ar1.getEndTimeSlotRecord().getValue() < ar2.getEndTimeSlotRecord()
                                                                    .getValue())) {
                return -1;
            }

            if ((ar1.getEndTimeSlotRecord().getValue() > -1) &&
                    (ar2.getEndTimeSlotRecord().getValue() > -1) &&
                    (ar1.getEndTimeSlotRecord().getValue() > ar2.getEndTimeSlotRecord()
                                                                    .getValue())) {
                return 1;
            }

            // end time equal or undefined, compare id's. Rely on the fact that the id 
            // is not 0 and that the length is > 1.
            try {
                Long l1 = Long.valueOf(ar1.getAnnotationId().substring(1));
                Long l2 = Long.valueOf(ar2.getAnnotationId().substring(1));

                if (l1.longValue() < l2.longValue()) {
                    return -1;
                }

                if (l1.longValue() > l2.longValue()) {
                    return 1;
                }
            } catch (NumberFormatException nfe) {
                LOG.warning("Invalid id of annotation record");
            }

            return 0;
        }
    }
}
