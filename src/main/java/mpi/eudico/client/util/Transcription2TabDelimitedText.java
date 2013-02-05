package mpi.eudico.client.util;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.export.TabExportTableModel;

import mpi.eudico.client.annotator.util.AnnotationDataComparator;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Created on Apr 20, 2004 Jun 2005: added optional export of time values in
 * milliseconds
 *
 * @author Alexander Klassmann
 * @version June 30, 2005
 * @version Aug 2005 Identity removed
 */
public class Transcription2TabDelimitedText {
    /** Holds value of property DOCUMENT ME! */
    final static public String TAB = "\t";

    /** Holds value of property DOCUMENT ME! */
    final static private String NEWLINE = "\n";

    /**
     * Exports all annotations on specified tiers
     *
     * @param transcription
     * @param tierNames
     * @param exportFile
     *
     * @throws IOException
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile) throws IOException {
        exportTiers(transcription, tierNames, exportFile, 0L, Long.MAX_VALUE);
    }

    /**
     * Exports annotations that overlap with specified time interval
     *
     * @param transcription
     * @param tierNames
     * @param exportFile
     * @param beginTime
     * @param endTime
     *
     * @throws IOException
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime)
        throws IOException {
        exportTiers(transcription, tierNames, exportFile, beginTime, endTime,
            false, true, true, true, true, true, false); 
    }

    /**
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 6 params.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param beginTime begin time of the selected interval
     * @param endTime end time of the selected interval
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     *
     * @throws IOException i/o exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        boolean includeCVDescrip, boolean includeBeginTime, boolean includeEndTime,
        boolean includeDuration, boolean includeHHMM, boolean includeSSMS,
        boolean includeMS) throws IOException {
        exportTiers(transcription, tierNames, exportFile, "UTF-8", beginTime,
            endTime, includeCVDescrip, includeBeginTime, includeEndTime, includeDuration,
            includeHHMM, includeSSMS, includeMS);
    }

    /**
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 6 params.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param charEncoding the character encoding
     * @param beginTime begin time of the selected interval
     * @param endTime end time of the selected interval
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     *
     * @throws IOException i/o exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, String charEncoding,
        long beginTime, long endTime, boolean includeCVDescrip, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS) throws IOException {
        exportTiers(transcription, tierNames, exportFile, charEncoding,
            beginTime, endTime, includeCVDescrip, includeBeginTime, includeEndTime,
            includeDuration, includeHHMM, includeSSMS, includeMS, false, false,
            0L, true, true);
    }

    /**
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 7 params.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param charEncoding the character encoding
     * @param beginTime begin time of the selected interval
     * @param endTime end time of the selected interval
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     * @param includeSMPTE if true include times in hh:mm:ss:ff format
     * @param palFormat if includeSMPTE is true: use PAL timecode if palFormat is true,
     * otherwise use NTSC drop frame
     * @param mediaOffset the (master) media offset to be added to the annotations' time values
     * @param includeNames if true include tier/participant names
     * @param includeParticipants if true include participant attributes
     *
     * @throws IOException i/o exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, String charEncoding,
        long beginTime, long endTime, boolean includeCVDescrip,  boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        boolean palFormat, long mediaOffset, boolean includeNames, boolean includeParticipants) throws
        IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        Annotation[] annotations = null;
        String participant = "";

        for (int j = 0; j < tierNames.length; j++) {
        	HashMap<String, String> cvEntryMap = null;
        	String cvName = null;
            CVEntry[] entries = null;
            
            TierImpl tier = (TierImpl) transcription.getTierWithId(tierNames[j]);
            participant = tier.getParticipant();
            
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

            if (participant == null) {
                participant = "";
            }

            annotations = (Annotation[]) tier.getAnnotations().toArray(new Annotation[0]);
                        
            for (int i = 0; i < annotations.length; i++) {  
                if (annotations[i] != null) {
                    if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) {
                        if (includeNames) {
                            writer.write(tierNames[j]);
                            if (includeParticipants) {
                            	writer.write(TAB + participant);
                            }
                        } else {
                        	if (includeParticipants) {
                        		writer.write(participant);
                        	}
                        }

                        String ts = getTabString(annotations[i],
                            includeBeginTime, includeEndTime,
                            includeDuration, includeHHMM,
                            includeSSMS, includeMS, includeSMPTE,
                            palFormat, mediaOffset);
                        
                        if(includeCVDescrip){      
                        	String description = cvEntryMap.get(annotations[i].getValue());
                        	if(description != null){
                        		StringBuffer tsBuffer = new StringBuffer(ts.replace(NEWLINE, TAB));
                        		tsBuffer.append(description + NEWLINE);
                        		ts = tsBuffer.toString();
                        	}
                        }

                        // July 2010 fix: the third argument should also depend on the includeNames flag
//                        writer.write(ts, 0 + ((includeNames) ? 0 : 1),
//                        		includeNames ? ts.length() : ts.length() - 1);
                        if (includeNames || includeParticipants) {
                        	writer.write(ts);
                        } else {
                        	writer.write(ts, 1, ts.length() - 1);
                        }
                    }
                }
            }
        }
        writer.close();
    }

    /**
     * Exports the annotations of each tier in a separate column. If annotations
     * of multiple tiers share the same begin AND end time they will be on the same
     * row in the output. All annotations are collected in one list and sorted on
     * the time values.
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 6 params.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param charEncoding the character encoding
     * @param beginTime begin time of the selected interval
     * @param endTime end time of the selected interval
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     * @param includeSMPTE if true include times in hh:mm:ss:ff format
     * @param palFormat if includeSMPTE is true: use PAL timecode if palFormat is true,
     * otherwise use NTSC drop frame
     * @param mediaOffset the (master) media offset to be added to the annotations' time values
     *
     * @throws IOException i/o exception
     */
    static public void exportTiersColumnPerTier(Transcription transcription,
        String[] tierNames, File exportFile, String charEncoding, boolean includeCVDescrip, 
        long beginTime, long endTime, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        boolean palFormat, long mediaOffset) throws IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        List allAnnotations = new ArrayList(100);
        List<String> allCVs = new ArrayList<String>();
        AnnotationCore[] annotations = null;
        
        TierImpl tier;
        String cvName;

        for (int j = 0; j < tierNames.length; j++) {
            tier = (TierImpl) transcription.getTierWithId(tierNames[j]);

            if (tier != null) {
                annotations = (AnnotationCore[]) tier.getAnnotations().toArray(new AnnotationCore[0]);
                if(includeCVDescrip){
                	cvName = tier.getLinguisticType().getControlledVocabylaryName();
                	if(cvName != null){
                		allCVs.add(cvName);
                	}
                }

                for (int i = 0; i < annotations.length; i++) {
                    if (annotations[i] != null) {
                        if (TimeRelation.overlaps(annotations[i], beginTime,
                                    endTime)) {
                            allAnnotations.add(annotations[i]);
                        }

                        if (annotations[i].getBeginTimeBoundary() > endTime) {
                            break;
                        }
                    }
                }
            }
        }
         // end tier loop

        Collections.sort(allAnnotations, new AnnotationDataComparator());        
        
        HashMap<String, HashMap<String,String>> cvMap = null;
        if (includeCVDescrip){
        	CVEntry[] entries;
        	cvMap = new HashMap<String, HashMap<String,String>>();
        	for(int i=0; i< allCVs.size(); i++){
        		HashMap<String,String> map = new HashMap<String,String>();
        		entries = ((TranscriptionImpl)transcription).getControlledVocabulary(allCVs.get(i)).getEntries();
        		if(entries != null){
        			cvMap.put(allCVs.get(i), map);
        			for(CVEntry entry : entries){
        				map.put(entry.getValue(), entry.getDescription());
        			}
        		}
        	}
        }

        // group the annotations that share the same begin and end time
        TabExportTableModel model = new TabExportTableModel(allAnnotations, 
        		cvMap, tierNames);

        // write header, write each row, taking into account the formatting flags
        writer.write(getHeaders(model, includeBeginTime, includeEndTime,
                includeDuration, includeHHMM, includeSSMS, includeMS,
                includeSMPTE, palFormat));
        
        writeRows(writer, model, includeBeginTime, includeEndTime,
            includeDuration, includeHHMM, includeSSMS, includeMS, includeSMPTE,
            palFormat, mediaOffset, false, false );

        writer.close();
    }

    /**
     * Exports the annotations of each tier in a separate column. If annotations
     * of multiple tiers share the same begin AND end time they will be on the same
     * row in the output. All annotations are collected in one list and sorted on
     * the time values.
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 6 params.
     *
     * @param files the transcription files
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param charEncoding the character encoding
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     * @param includeSMPTE if true include times in hh:mm:ss:ff format
     * @param palFormat if includeSMPTE is true: use PAL timecode if palFormat is true,
     * otherwise use NTSC drop frame
     * @param includeFileName if true include only the file name
     * @param includeFilePath if true include the full file path
     * 
     * @throws IOException i/o exception
     */
    static public void exportTiersColumnPerTier(List files, String[] tierNames,
        File exportFile, String charEncoding, boolean includeCVDescrip, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        boolean palFormat, boolean includeFileName, boolean includeFilePath) throws IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            throw new IOException("No files specified for export");
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        List allAnnotations = new ArrayList(100);
        AnnotationCore[] annotations = null;
        TabExportTableModel model = null;
        AnnotationDataComparator comparator = new AnnotationDataComparator();

        File file;
        TranscriptionImpl trans;
        TierImpl tier;
        String name;
        String tabString;
              
        List<String> allCVs = new ArrayList<String>();
        String cvName;

        for (int i = 0; i < files.size(); i++) {
            file = (File) files.get(i);
            allAnnotations.clear();

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());

                for (int j = 0; j < tierNames.length; j++) {
                    tier = (TierImpl) trans.getTierWithId(tierNames[j]);

                    if (tier != null) {
                    	if(includeCVDescrip){
                        	cvName = tier.getLinguisticType().getControlledVocabylaryName();
                        	if(cvName != null){
                        		allCVs.add(cvName);
                        	}
                        }
                    	
                        annotations = (AnnotationCore[]) tier.getAnnotations()
                                                             .toArray(new AnnotationCore[0]);

                        for (int k = 0; k < annotations.length; k++) {
                            if (annotations[k] != null) {
                                //if (TimeRelation.overlaps(annotations[k], beginTime, endTime)) {
                                allAnnotations.add(annotations[k]);

                                //}
                                //if (annotations[k].getBeginTimeBoundary() > endTime) {
                                //    break;
                                //}
                            }
                        }
                    }
                }
                 // end tier loop

                Collections.sort(allAnnotations, comparator);
                
                HashMap<String, HashMap<String,String>> cvMap = null;
                if (includeCVDescrip){
                	CVEntry[] entries;
                	cvMap = new HashMap<String, HashMap<String,String>>();
                	for(int x=0; x< allCVs.size(); x++){
                		HashMap<String,String> map = new HashMap<String,String>();
                		entries = ((TranscriptionImpl)trans).getControlledVocabulary(allCVs.get(x)).getEntries();
                		if(entries != null){
                			cvMap.put(allCVs.get(x), map);
                			for(CVEntry entry : entries){
                				map.put(entry.getValue(), entry.getDescription());
                			}
                		}
                	}
                }

                // group the annotations that share the same begin and end time
                model = new TabExportTableModel(allAnnotations, cvMap, tierNames);
                model.setFileName(file.getName());
                model.setAbsoluteFilePath(file.getAbsolutePath());
                
                // write header, write each row, taking into account the formatting flags
                if (i == 0) {
                    String header = getHeaders(model, includeBeginTime,
                            includeEndTime, includeDuration, includeHHMM,
                            includeSSMS, includeMS, includeSMPTE, palFormat);
                    writer.write(header, 0, header.length() - 1);
                    writer.write(TAB +
                        ElanLocale.getString("Frame.GridFrame.ColumnFileName") + TAB + 
                        ElanLocale.getString("Frame.GridFrame.ColumnFilePath") + NEWLINE); 
                }

                // temporary extra output, implement in UI as well: option to have the filename
                // in a separate row preceding the annotations of that file. 
//                writer.write(trans.getName());
//                writer.write(NEWLINE);
                
                writeRows(writer, model, includeBeginTime, includeEndTime,
                    includeDuration, includeHHMM, includeSSMS, includeMS,
                    includeSMPTE, palFormat, 0L, includeFileName, includeFilePath);
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                System.out.println("Could not handle file: " +
                    file.getAbsolutePath());
            }
        }

        writer.close();
    }

    /**
     * Exports annotations of the specified tiers that overlap the specified
     * interval. Which time information and in which time formats should be
     * included in the output is specified by the last 6 params.
     *
     * @param files the source transcription files
     * @param tierNames the names of the tiers to export
     * @param exportFile the destination file
     * @param charEncoding the character encoding
     * @param includeBeginTime if true include the begin time of annotations
     * @param includeEndTime if true include the end time of annotations
     * @param includeDuration if true include the duration of annotations
     * @param includeHHMM if true include the times in hh:mm:ss.ms format
     * @param includeSSMS if true include the times in ss.ms format
     * @param includeMS if true include the times in ms format
     * @param includeSMPTE if true include times in hh:mm:ss:ff format
     * @param palFormat if includeSMPTE is true: use PAL timecode if palFormat is true,
     * otherwise use NTSC drop frame
     * @param includeNames if true include tier names
     * @param includeParticipants if true include the participant attribute in the export
     * @param includeFileName if true include only the file name
     * @param includeFilePath if true include the full file path
     *
     * @throws IOException i/o exception
     */
    static public void exportTiers(List files, String[] tierNames,
        File exportFile, String charEncoding, boolean includeCVDescrip, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        boolean palFormat, boolean includeNames, boolean includeParticipants, boolean includeFileName, boolean includeFilePath) throws IOException {
        if (exportFile == null) {
            throw new IOException("No destination file specified for export");
        }

        if ((files == null) || (files.size() == 0)) {
            throw new IOException("No files specified for export");
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(out, charEncoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);
        AnnotationCore[] annotations = null;

        File file;
        TranscriptionImpl trans;
        TierImpl tier;
        final String EMPTY = "";
        String participant = EMPTY;
        String name;
        String tabString;

        for (int i = 0; i < files.size(); i++) {
            file = (File) files.get(i);

            if (file == null) {
                continue;
            }

            try {
                trans = new TranscriptionImpl(file.getAbsolutePath());

                List tiers = trans.getTiers();

tierloop: 
                for (int j = 0; j < tiers.size(); j++) {
                    tier = (TierImpl) tiers.get(j);
                    name = tier.getName();

                    if (tierNames != null) {
                        for (int k = 0; k < tierNames.length; k++) {
                            if (tierNames[k].equals(name)) {
                                break;
                            }

                            if (k == (tierNames.length - 1)) {
                                // not in the list, next tier
                                continue tierloop;
                            }
                        }
                    }
                    
                    HashMap<String, String> cvEntryMap = null;
                	String cvName = null;
                    CVEntry[] entries = null;
                    
                    if(includeCVDescrip){        
                    	cvEntryMap = new HashMap<String, String>();
                        cvName = tier.getLinguisticType().getControlledVocabylaryName();
                    	if(cvName != null){
                    		entries = trans.getControlledVocabulary(cvName).getEntries();
                    		for(CVEntry cv : entries){
                            	cvEntryMap.put(cv.getValue(), cv.getDescription());
                    		}
                    	}
                	}

                    annotations = (AnnotationCore[]) tier.getAnnotations()
                                                         .toArray(new AnnotationCore[0]);
                    participant = tier.getParticipant();
                    
                    if (participant == null) {
                    	participant = EMPTY;
                    }

                    for (int k = 0; k < annotations.length; k++) {
                        if (annotations[k] != null) {
                            if (includeNames) {
                                writer.write(name);
                                if (includeParticipants) {
                                	writer.write(TAB + participant);
                                }
                            } else {
                            	if (includeParticipants) {
                                	writer.write(participant);
                                }
                            }

                            tabString = getTabString(annotations[k],
                                    includeBeginTime, includeEndTime,
                                    includeDuration, includeHHMM, includeSSMS,
                                    includeMS, includeSMPTE, palFormat, 0);
                            
                            if(includeCVDescrip){      
                            	String description = cvEntryMap.get(annotations[i].getValue());
                            	if(description != null){
                            		StringBuffer tsBuffer = new StringBuffer(tabString.replace(NEWLINE, TAB));
                            		tsBuffer.append(description + NEWLINE);
                            		tabString = tsBuffer.toString();
                            	}
                            }  
                            
//                            writer.write(tabString,
//                                    0 + ((includeNames) ? 0 : 1),
//                                    tabString.length() - 1);
                            if (includeNames || includeParticipants) {
                                writer.write(tabString, 0,
                                        tabString.length() - 1);
                            } else {
                            	writer.write(tabString, 1,
                                        tabString.length() - 2);
                            }
                            
                            if (includeFileName){
                            	writer.write(TAB + file.getName() );
                            }
                            
                            if (includeFilePath){
                            	writer.write( TAB + file.getAbsolutePath() );
                            }
                            
                            writer.write( NEWLINE);                              
                        }
                    }
                }
            } catch (Exception ex) {
                // catch any exception that could occur and continue
                System.out.println("Could not handle file: " +
                    file.getAbsolutePath());
            }
        }

        writer.close();
    }

    /**
     * Returns the column headers / labels.
     * @param model the table model used to group annotations with the same begin and end time
     * in one row
     * @param includeBeginTime
     * @param includeEndTime
     * @param includeDuration
     * @param includeHHMM
     * @param includeSSMS
     * @param includeMS
     * @param includeSMPTE
     * @param palFormat
     * @return the header labels delimited by tabs
     */
    private static String getHeaders(TabExportTableModel model,
        boolean includeBeginTime, boolean includeEndTime,
        boolean includeDuration, boolean includeHHMM, boolean includeSSMS,
        boolean includeMS, boolean includeSMPTE, boolean palFormat) {
        StringBuffer buf = new StringBuffer();

        if (includeBeginTime) {
            if (includeHHMM) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode") + TAB);
            }

            if (includeSSMS) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds") + TAB);
            }

            if (includeMS) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnBeginTime") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec") + TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnBeginTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        if (includeEndTime) {
            if (includeHHMM) {
                buf.append(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.TimeCode") +
                    TAB);
            }

            if (includeSSMS) {
                buf.append(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.Seconds") +
                    TAB);
            }

            if (includeMS) {
                buf.append(ElanLocale.getString("Frame.GridFrame.ColumnEndTime") +
                    " - " + ElanLocale.getString("TimeCodeFormat.MilliSec") +
                    TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnEndTime") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        if (includeDuration) {
            if (includeHHMM) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.TimeCode") + TAB);
            }

            if (includeSSMS) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.Seconds") + TAB);
            }

            if (includeMS) {
                buf.append(ElanLocale.getString(
                        "Frame.GridFrame.ColumnDuration") + " - " +
                    ElanLocale.getString("TimeCodeFormat.MilliSec") + TAB);
            }

            if (includeSMPTE) {
                if (palFormat) {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.PAL") + TAB);
                } else {
                    buf.append(ElanLocale.getString(
                            "Frame.GridFrame.ColumnDuration") + " - " +
                        ElanLocale.getString("TimeCodeFormat.SMPTE.NTSC") +
                        TAB);
                }
            }
        }

        for (int i = 2; i < model.getColumnCount(); i++) {
            buf.append(model.getColumnName(i));

            if (i != (model.getColumnCount() - 1)) {
                buf.append(TAB);
            }
        }        buf.append(NEWLINE);

        return buf.toString();
    }

    private static void writeRows(BufferedWriter writer,
        TabExportTableModel model, boolean includeBeginTime,
        boolean includeEndTime, boolean includeDuration, boolean includeHHMM,
        boolean includeSSMS, boolean includeMS, boolean includeSMPTE,
        boolean palFormat, long mediaOffset, boolean includeFileName, boolean includeFilePath ) throws IOException {
        long bt;
        long et;
        Object value;

        for (int i = 0; i < model.getRowCount(); i++) {
            bt = ((Long) model.getValueAt(i, 0)).longValue() + mediaOffset;
            et = ((Long) model.getValueAt(i, 1)).longValue() + mediaOffset;

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
            for (int j = 2; j < model.getColumnCount(); j++) {
                value = model.getValueAt(i, j);

                if (value instanceof String) {
                    writer.write(((String) value).replace(NEWLINE, " "));
                }

                if (j != (model.getColumnCount() - 1)) {
                    writer.write(TAB);
                }
            }
            
            if (model.getFileName() != null) {
            	if(includeFileName)
            		writer.write(TAB + model.getFileName());
            	if(includeFilePath)
            		writer.write(TAB + model.getAbsoluteFilePath());            		
            }

            writer.write(NEWLINE);
        }

        writer.write(NEWLINE);
    }

    /**
     * Exports a List of AnnotationCores to Tab limited text
     *
     * @param tierName DOCUMENT ME!
     * @param annotations
     * @param exportFile
     *
     * @throws IOException
     */
    static public void exportAnnotations(String tierName, List annotations,
        File exportFile) throws IOException {
        if (exportFile == null) {
            return;
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    "UTF-8"));

        for (int i = 0; i < annotations.size(); i++) {
            if (annotations.get(i) instanceof AnnotationCore) {
                writer.write(tierName +
                    getTabString((AnnotationCore) annotations.get(i)));
            }
        }

        writer.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotationCore
     *
     * @return String
     */
    static public String getTabString(AnnotationCore annotationCore) {
        return getTabString(annotationCore, true, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotationCore
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in second.milliseconds format
     *
     * @return String
     */
    static public String getTabString(AnnotationCore annotationCore,
        boolean HHMMformat, boolean SSMSFormat) {
        return getTabString(annotationCore, true, true, true, HHMMformat,
            SSMSFormat, false); 
    }
    

    /**
     * Creates a tab delimited string with time information and the annotation
     * value.
     *
     * @param annotationCore the annotation
     * @param beginTime include begintime in output
     * @param endTime include endtime in output
     * @param duration include duration in output
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in sec.milliseconds format
     * @param MSFormat if true, output of times in milliseconds format
     *
     * @return String the tab seperated result string
     */
    static public String getTabString(AnnotationCore annotationCore,
        boolean beginTime, boolean endTime, boolean duration,
        boolean HHMMformat, boolean SSMSFormat, boolean MSFormat) {
        return getTabString(annotationCore, beginTime, endTime, duration,
            HHMMformat, SSMSFormat, MSFormat, false, false, 0L);
    }

    /**
     * Creates a tab delimited string with time information and the annotation
     * value.
     *
     * @param annotationCore the annotation
     * @param beginTime include begintime in output
     * @param endTime include endtime in output
     * @param duration include duration in output
     * @param HHMMformat if true, output of times in HHMMss.mmm format
     * @param SSMSFormat if true, output of times in sec.milliseconds format
     * @param MSFormat if true, output of times in milliseconds format
     * @param SMPTEFormat if true, output times in SMPTE timecode format
     * @param PAL if SMPTEFormat is true and PAL is true use PAL timecode,
     * if SMPTEFormat is true and PAL is false use NTSC drop frame timecode
     * @param mediaOffset the (master) media offset to be added to the annotations' time values
     *
     * @return String the tab separated result string
     */
    static public String getTabString(AnnotationCore annotationCore,
        boolean beginTime, boolean endTime, boolean duration,
        boolean HHMMformat, boolean SSMSFormat, boolean MSFormat,
        boolean SMPTEFormat, boolean PAL, long mediaOffset) {
        StringBuffer sb = new StringBuffer(TAB);

        long bt = annotationCore.getBeginTimeBoundary() + mediaOffset;
        long et = annotationCore.getEndTimeBoundary() + mediaOffset;
        
        // begin time
        if (beginTime) {
            if (HHMMformat) {
                sb.append(TimeFormatter.toString(bt) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(bt / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(bt + TAB);
            }

            if (SMPTEFormat) {
                if (PAL) {
                    sb.append(TimeFormatter.toTimecodePAL(bt) + TAB);
                } else {
                    sb.append(TimeFormatter.toTimecodeNTSC(bt) + TAB);
                }
            }
        }

        // end time
        if (endTime) {
            if (HHMMformat) {
                sb.append(TimeFormatter.toString(et) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(et / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(et + TAB);
            }

            if (SMPTEFormat) {
                if (PAL) {
                    sb.append(TimeFormatter.toTimecodePAL(et) + TAB);
                } else {
                    sb.append(TimeFormatter.toTimecodeNTSC(et) + TAB);
                }
            }
        }

        // duration
        if (duration) {
            long d = et - bt;

            if (HHMMformat) {
                sb.append(TimeFormatter.toString(d) + TAB);
            }

            if (SSMSFormat) {
                sb.append(Double.toString(d / 1000.0) + TAB);
            }

            if (MSFormat) {
                sb.append(d + TAB);
            }
        }        	
        
        sb.append(annotationCore.getValue().replace('\n', ' ') + NEWLINE);

        return sb.toString();
    }
}
