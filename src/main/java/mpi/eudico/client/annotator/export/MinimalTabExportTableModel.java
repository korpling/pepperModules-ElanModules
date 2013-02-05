package mpi.eudico.client.annotator.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
     * A (kind of) table model for a group of annotations, part of a hierarchy.
     * Contains methods to add annotations with the same begin and end time to
     * the same row, as well as to repeat values of annotations that overlap
     * multiple other annotation in order to reduce the number of empty cells.<br>
     * <br>
     * Note: in each row the first cell contains an annotation (or null), the
     * second cell the begin time, the third the end time.
     *
     * @author Han Sloetjes
     */
    class MinimalTabExportTableModel {
        /** constant as a flag for hidden rows */
        public final Object HIDDEN = new Object();
        private List<String> columnIds;
        private List<List> rows;
        private List template;
        private final Object filler = null;
        private int numCols;
        private final String TAB = "\t";
        private boolean repeatValues = true;
        private boolean hideSpanningRowsInsteadOfRemove = false;
        private long[] span;
        private String fileName = null;
        private String filePath = null;
        private List<Annotation> annotations;
        private HashMap<String, String> cvEntryMap;
        /**
         * Constructor accepting deault values for repeating spanning
         * annotations and whether or not to completely remove spanning rows.
         *
         * @param columnNames the tier names
         * @param annotations the annotations
         */
        MinimalTabExportTableModel(List<String> columnNames, List<Annotation> annotations, HashMap<String, String> cvEntryMap) {
            this(columnNames, annotations, cvEntryMap, true, false);
        }

        /**
         * Constructor that allows to specify whether to repeat values of
         * annotations spanning other annotations and whether to "hide"
         * spanning  lines rather than deleting them.
         *
         * @param columnNames the tier names
         * @param annotations the annotations
         * @param repeatValues if true the value of a spanning annotation is
         *        repeated
         * @param hideSpanningRowsInsteadOfRemove if hidden (true) the rows can
         *        be used  for repeating values across multiple blocks
         */
        MinimalTabExportTableModel(List<String> columnNames,
            List<Annotation> annotations, HashMap<String, String> cvEntryMap, 
            boolean repeatValues, boolean hideSpanningRowsInsteadOfRemove) {
            this.repeatValues = repeatValues;
            this.hideSpanningRowsInsteadOfRemove = hideSpanningRowsInsteadOfRemove;
            columnIds = new ArrayList<String>(columnNames);
            numCols = columnIds.size() + 3; // columns for the annotation, begin time and end time
                                            //columnIds.add(0, "BT&*()");
                                            //columnIds.add(1, "ET&*()");
            if(cvEntryMap != null){
            	numCols = numCols+1; // columns for the cv description
            }

            template = new ArrayList(numCols);

            for (int i = 0; i < numCols; i++) {
                template.add(filler);
            }

            rows = new ArrayList<List>(annotations.size());
            this.annotations = annotations;
            this.cvEntryMap = cvEntryMap;
            initTable();
        }

        /**
         * Creates the data for the model, fills the rows, replicates values,
         * sorts and hides or deletes spanning rows.
         */
        private void initTable() {
            String curTier;
            Annotation curAnn;
            Annotation otherAnn;

            // add the annotation itself in e.g the first column
            // for optional removal of rows with annotations with more than 1 sub div child
            List<Annotation> repRemovals = new ArrayList<Annotation>();

            for (int i = 0; i < annotations.size(); i++) {
                curAnn = (Annotation) annotations.get(i);
                curTier = curAnn.getTier().getName();

                int index = columnIds.indexOf(curTier);

                if (index > -1) {
                    index += 3;
                    
                    List row = new ArrayList(template);
                    row.set(0, curAnn);
                    row.set(1, new Long(curAnn.getBeginTimeBoundary()));
                    row.set(2, new Long(curAnn.getEndTimeBoundary()));
                    row.set(index, curAnn.getValue());
                    if(cvEntryMap != null){
                    	 row.set(row.size()-1, cvEntryMap.get(curAnn.getValue()));
                    }                   
                    rows.add(row);

                    if (repeatValues) {
                        List pl = ((AbstractAnnotation) curAnn).getParentListeners();

                        if (pl.size() > 0) {
                            List<String> overlapsPart = new ArrayList<String>(10);

                            for (int j = 0; j < pl.size(); j++) {
                                otherAnn = (Annotation) pl.get(j);
                                // only consider child annotations if they are also in the export
                                if (!columnIds.contains(otherAnn.getTier().getName())) {
                                	continue;
                                }
                                if ((otherAnn.getBeginTimeBoundary() != curAnn.getBeginTimeBoundary()) ||
                                        (otherAnn.getEndTimeBoundary() != curAnn.getEndTimeBoundary())) {
                                    if (!overlapsPart.contains(
                                                otherAnn.getTier().getName())) {
                                        overlapsPart.add(otherAnn.getTier()
                                                                 .getName());
                                    } else {
                                        // more than 1 partially overlapping annotations on the same tier
                                        // remove the spanning parent at the end
                                        repRemovals.add(curAnn);

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // merge rows with identical begin and end time
            List curRow;

            // merge rows with identical begin and end time
            List prevRow;
            long l1;
            long l2;
            long l3;
            long l4;
            List<Integer> removals = new ArrayList<Integer>();

outerloop: 
            for (int i = rows.size() - 1; i >= 0; i--) {
                curRow = (List) rows.get(i);
                l1 = ((Long) curRow.get(1)).longValue();
                l2 = ((Long) curRow.get(2)).longValue();

                for (int j = 0; j < i; j++) {
                    prevRow = (List) rows.get(j);
                    l3 = ((Long) prevRow.get(1)).longValue();
                    l4 = ((Long) prevRow.get(2)).longValue();

                    if ((l1 == l3) && (l2 == l4)) {
                        removals.add(i);

                        Object val;

                        for (int k = 3; k < curRow.size(); k++) {
                            val = curRow.get(k);

                            if (val != filler) {
                                prevRow.set(k, val);
                            }
                        }

                        continue outerloop;
                    }
                }
            }

            //remove the "normal" removables, after merging
            for (int i = 0; i < removals.size(); i++) {
                rows.remove(((Integer) removals.get(i)).intValue());
            }

            //tabOutput();
            // fill empty cells, column wise, top - down
            curRow = null;
            prevRow = null;

            if (repeatValues) {
                Object val = null;
                Object curVal;

                for (int j = 0; j < rows.size(); j++) {
                    curRow = (List) rows.get(j);
                    l1 = ((Long) curRow.get(1)).longValue();
                    l2 = ((Long) curRow.get(2)).longValue();

                    for (int i = 0; i < rows.size(); i++) {
                        if (i == j) {
                            continue;
                        }

                        prevRow = (List) rows.get(i);
                        l3 = ((Long) prevRow.get(1)).longValue();
                        l4 = ((Long) prevRow.get(2)).longValue();

                        if ((l1 <= l3) && (l2 >= l4)) {
                            for (int k = 3; k < numCols; k++) {
                                curVal = prevRow.get(k);
                                val = curRow.get(k);

                                if (curVal == null) {
                                    if (val != null) {
                                        prevRow.set(k, val);
                                    }
                                }
                            }
                        }
                    }
                }

                // finally remove the replicated/repeated, spanning rows
                if (repRemovals.size() > 0) {
                    for (int i = rows.size() - 1; i >= 0; i--) {
                        curRow = (List) rows.get(i);

                        if (repRemovals.contains(curRow.get(0))) {
                            if (hideSpanningRowsInsteadOfRemove) {
                                curRow.set(0, HIDDEN);
                            } else {
                                rows.remove(i);
                            }
                        }
                    }
                }
            }

            //tabOutput();
            // sort the rows on time?
            Collections.sort(rows, new RowComparator());
            annotations = null; //??
        }

        /**
         * Returns the overall time span of the block of annotations.
         *
         * @return the total time span
         */
        public long[] getSpan() {
            return span;
        }

        /**
         * Sets the total time span of the annotations
         *
         * @param span the total time span
         */
        public void setSpan(long[] span) {
            this.span = span;
        }

        /**
         * Returns the name of the file these annotations came from
         *
         * @return the name of the file (can be path)
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Sets from which file the annotations came
         *
         * @param fileName the name or path of the file
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        /**
         * Returns the path of the file these annotations came from
         *
         * @return the path of the file
         */
        public String getFilePath() {
            return filePath;
        }

        /**
         * Sets the path of the file from which the annotations came
         *
         * @param filePath the path of the file
         */
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        private void tabOutput() {
            System.out.print("BT" + TAB + "ET" + TAB);

            for (int i = 0; i < columnIds.size(); i++) {
                System.out.print(columnIds.get(i) + TAB);
            }

            System.out.println();
            System.out.println();

            List curRow = null;
            Object val;

            for (int i = 0; i < rows.size(); i++) {
                curRow = (List) rows.get(i);

                for (int j = 1; j < curRow.size(); j++) {
                    val = curRow.get(j);

                    if (val != null) {
                        if (val instanceof String) {
                            System.out.print(((String) val).replace("\n", " "));
                        } else {
                            System.out.print(val); // Long
                        }
                    } else {
                        System.out.print("*");
                    }

                    System.out.print(TAB);
                }

                System.out.println();
            }
        }

        /**
         * Returns the table as a tab delimited String, for testing
         *
         * @return the table as a string
         */
        public String getTableAsString() {
            StringBuffer buf = new StringBuffer();

            /*
               buf.append("BT" + TAB + "ET" + TAB);
               for (int i = 0; i < columnIds.size(); i++) {
                   buf.append(columnIds.get(i) + TAB);
               }
               buf.append("\n");
             */
            List curRow = null;
            Object val;

            for (int i = 0; i < rows.size(); i++) {
                curRow = (List) rows.get(i);

                for (int j = 1; j < curRow.size(); j++) {
                    val = curRow.get(j);

                    if (val != null) {
                        if (val instanceof String) {
                            buf.append(((String) val).replace("\n", " "));
                        } else {
                            buf.append(val); // Long
                        }
                    } else {
                        buf.append("*");
                    }

                    buf.append(TAB);
                }

                buf.append("\n");
            }

            return buf.toString();
        }

        /**
         * Returns the rows of the model.
         *
         * @return rows
         */
        public List<List> getRows() {
            return rows;
        }

        /**
         * A comparator for sorting rows, based on the time values in cell 1
         * and 2.
         *
         * @author Han Sloetjes
         * @version 1.0
         */
        private class RowComparator implements Comparator<List> {
            private long l1;
            private long l2;
            private long l3;
            private long l4;

            /**
             * Row lists are assumed, no checking on this.
             *
             * @param row1 the first row
             * @param row2 the second row
             *
             * @return -1 if the first row is smaller (earlier), 0 if they have
             *         the same begin and end time, 1 if the second row is
             *         earlier. <br>
             *         *  Note: if the begin times are equal, the row with
             *         the greatest end time will be "smaller"
             */
            public int compare(List row1, List row2) {
                l1 = ((Long) row1.get(1)).longValue();
                l2 = ((Long) row1.get(2)).longValue();
                l3 = ((Long) row2.get(1)).longValue();
                l4 = ((Long) row2.get(2)).longValue();

                if ((l1 < l3) || ((l1 == l3) && (l2 > l4))) { // the spanning ones first

                    return -1;
                } else if ((l1 == l3) && (l2 == l4)) {
                    return 0;
                }

                return 1;
            }
        }
    }