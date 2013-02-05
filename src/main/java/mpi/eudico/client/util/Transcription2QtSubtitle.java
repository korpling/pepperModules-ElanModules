package mpi.eudico.client.util;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import mpi.eudico.util.TimeRelation;

import mpi.eudico.util.TimeFormatter;

import java.awt.Color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.HashMap;
import java.util.List;


/**
 * Exports the annotations of a selection of tiers as a QuickTime (subtitle)
 * text file.   Created on Jul 2, 2004
 *
 * @author Alexander Klassmann
 * @version Dec 2007 support for an offset, a minimal duration per entry and merging of  tiers added
 */
public class Transcription2QtSubtitle {
    /** new line character */
    final static private String NEWLINE = "\n";
    final static private char[] bracks = new char[]{'[', ']', '(', ')'}; 
    final static private char NL_CHAR = '\n'; 

    /**
     * Exports all annotations on specified tiers
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile) throws IOException {
        exportTiers(transcription, tierNames, exportFile, 0L, Long.MAX_VALUE);
    }

    /**
     * Exports all annotations on specified tiers, applying the specified
     * minimal duration.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param minimalDuration the minimal duration for each annotation/subtitle
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, int minimalDuration)
        throws IOException {
        exportTiers(transcription, tierNames, exportFile, 0L, Long.MAX_VALUE);
    }

    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime)
        throws IOException {
        exportTiers(transcription, tierNames, exportFile, beginTime, endTime,
            0L, 0, 0L, false, null);
    }
    
    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified,  applying the specified offset and minimal duration. For
     * each tier a separate text file is created.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration)
        throws IOException {
    	exportTiers(transcription, tierNames, exportFile, beginTime, endTime,
                offset, minimalDuration, mediaDuration , false, null);
    }

    /**
     * Exports all annotations on specified tiers that overlap the interval
     * specified,  applying the specified offset and minimal duration. For
     * each tier a separate text file is created.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     * @param the font and color setting for the subtitles
     *
     * @throws IOException any io exception
     */
    static public void exportTiers(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration, boolean reCalculateTime, HashMap newSubtitleSetting)
        throws IOException {
        if (exportFile == null) {
            return;
        }

        Annotation[] annotations = null;
        FileOutputStream out = null;
        BufferedWriter writer = null;

        if (tierNames.length == 1) {
            out = new FileOutputStream(exportFile);
            writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        }        
        
        long recalculateTimeInterval = 0L;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime; 
        	offset = 0L;
        	
        	for (int j = 0; j < tierNames.length; j++) {        	
                
                TierImpl tier = (TierImpl) transcription.getTierWithId(tierNames[j]);

                annotations = (Annotation[]) tier.getAnnotations()
                                                 .toArray(new Annotation[0]);
                for (int i = 0; i < annotations.length; i++) {
                	if (annotations[i] != null) {
                		if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) { 
                    	                    	                    	                    	
                        long b = annotations[i].getBeginTimeBoundary(); 
                        
                        if (b < recalculateTimeInterval){
                        		recalculateTimeInterval = b;
                        		break;
                        	}                        	
                        }
                	}
                }
            }
        	annotations = null;
        }
        
        for (int j = 0; j < tierNames.length; j++) {
            if (tierNames.length > 1) {
                String nextName = exportFile.getAbsolutePath();
                int index = nextName.lastIndexOf('.');

                if (index > 0) {
                    nextName = nextName.substring(0, index) + "_" +
                        tierNames[j] + ".txt";
                } else {
                    nextName = nextName + "_" + tierNames[j];
                }

                out = new FileOutputStream(new File(nextName));
                writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            }

            TierImpl tier = (TierImpl) transcription.getTierWithId(tierNames[j]);

            annotations = (Annotation[]) tier.getAnnotations()
                                             .toArray(new Annotation[0]);
            
            writer.write(getSettings(newSubtitleSetting));            

            long b;
            long e;
            long d;
            long nextB = 0L;
            long lastE = 0L;            
            
            for (int i = 0; i < annotations.length; i++) {
                if (annotations[i] != null) {
                    if (TimeRelation.overlaps(annotations[i], beginTime, endTime)) { 
                    	                    	                    	                    	
                        b = annotations[i].getBeginTimeBoundary();
                        d = b + minimalDuration;
                        e = Math.max(annotations[i].getEndTimeBoundary(), d);  
                        
                        if (i < (annotations.length - 1)) {
                            nextB = annotations[i + 1].getBeginTimeBoundary();
                            e = Math.min(e, nextB);
                        }

                        if (lastE < e) {
                            lastE = e;
                        }

                        writer.write("[" + TimeFormatter.toString(b -recalculateTimeInterval + offset) +
                            "]" + NEWLINE);
                        writer.write("{textEncoding:256}");
                        writer.write(replaceBrackets(annotations[i].getValue()));
                        writer.append(NL_CHAR);
                        if (nextB - e < 10 && nextB - b >= 20) {// min 10 ms in between
                            writer.write("[" + TimeFormatter.toString(nextB-recalculateTimeInterval - 10 + offset) +
                                    "]" + NEWLINE);
                        } else {
                        	writer.write("[" + TimeFormatter.toString(e -recalculateTimeInterval+ offset) +
                            "]" + NEWLINE);
                        }
                    }
                }
            }

            if (mediaDuration > lastE + 20) {
            	// what about the offset?
                writer.write("[" +
                    TimeFormatter.toString(mediaDuration -
                        Math.min(40, (mediaDuration - lastE + 10))) + "]" + NEWLINE);
                writer.write("[" + TimeFormatter.toString(mediaDuration) + "]");
            }

            if (tierNames.length > 1) {
                writer.close();
            }
        }

        writer.close();
    }

    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     *
     * @throws IOException any io exception
     */    
    static public void exportTiersMerged(Transcription transcription,
            String[] tierNames, File exportFile, long beginTime, long endTime,
            long offset, int minimalDuration, long mediaDuration) throws IOException{
    	exportTiersMerged(transcription, tierNames, exportFile, beginTime,
                endTime, offset, minimalDuration, mediaDuration ,false,  null);    	
    }
    
    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param beginTime the interval begin time
     * @param endTime the interval end time
     * @param offset the offset to add to all time values
     * @param minimalDuration the minimal duration for each annotation/subtitle
     * @param mediaDuration the total duration of the media, used to insert a
     *        dummy  subtitle at the end of the media file
     * @param newSubtitleSetting the font and color setting for the subtitles
     *
     * @throws IOException any io exception
     */
    static public void exportTiersMerged(Transcription transcription,
        String[] tierNames, File exportFile, long beginTime, long endTime,
        long offset, int minimalDuration, long mediaDuration, boolean reCalculateTime, HashMap newSubtitleSetting)
        throws IOException {
        if (exportFile == null) {
            return;
        }
        
        long recalculateTimeInterval = 0L;
        int selection =0;
        if(reCalculateTime){
        	recalculateTimeInterval = beginTime;  
        	offset = 0L;
        	
        }  
        
       String fileName = exportFile.getAbsolutePath();
        int index = fileName.lastIndexOf('.');
        
        if (index > 0) {
        	fileName = fileName.substring(0, index) +".txt";
        }
        
        FileOutputStream out = new FileOutputStream(new File(fileName));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    "UTF-8"));
        writer.write(getSettings(newSubtitleSetting)); 
        
        SubtitleSequencer sequencer = new SubtitleSequencer();

        List allUnits = sequencer.createSequence(transcription, tierNames,
                beginTime, endTime, minimalDuration, offset, true);

        SubtitleUnit unit = null;
        SubtitleUnit nextUnit = null;
        Long d = 0L;

        for (int i = 0; i < allUnits.size(); i++) {
            unit = (SubtitleUnit) allUnits.get(i);
            if(selection ==0){
            	if (unit.getBegin() < recalculateTimeInterval){
            		recalculateTimeInterval = unit.getBegin();
            		selection =1;
            	}            	
            }            
            writer.write("[" + TimeFormatter.toString(unit.getBegin() - recalculateTimeInterval) + "]" +
            		NEWLINE);            	
            writer.write("{textEncoding:256}");

            if (unit.getValue() != null) {
                //writer.write(unit.getValue().replace('\n', ' '));
                writer.write(replaceBrackets(unit.getValue()));
                writer.append(NL_CHAR);
            } else {
                for (int j = 0; j < unit.getValues().length; j++) {
                    //writer.write(unit.getValues()[j].replace('\n', ' '));
                    writer.write(replaceBrackets(unit.getValues()[j]));
                    writer.append(NL_CHAR);
                }
            }
            
            if (i < allUnits.size() - 1) {
            	nextUnit = (SubtitleUnit) allUnits.get(i + 1);
            	if (nextUnit.getBegin() - unit.getCalcEnd() < 10 && 
            			nextUnit.getBegin() - unit.getBegin() >= 20) {// adjust end time: min 10 ms in between
            		writer.write("[" + TimeFormatter.toString(nextUnit.getBegin() - 10 - recalculateTimeInterval ) + "]" +
                            NEWLINE);
            	} else {
            		writer.write("[" + TimeFormatter.toString(unit.getCalcEnd() - recalculateTimeInterval ) + "]" +
                            NEWLINE);
            	}
            } else {
            	writer.write("[" + TimeFormatter.toString(unit.getCalcEnd() -recalculateTimeInterval ) + "]" +
                        NEWLINE);
            }
            
        }

        if ((unit != null) && (mediaDuration > unit.getCalcEnd() + 20)) { // unit is the last unit
            writer.write("[" +
                TimeFormatter.toString(mediaDuration -
                    Math.min(40, (mediaDuration - unit.getCalcEnd() - 10 ))) + "]" +
                NEWLINE);
            writer.write("[" + TimeFormatter.toString(mediaDuration) + "]");
        }

        writer.close();
    }

    /**
     * Exports the annotations on specified tiers to one text file. The tiers
     * are "merged" overlaps are corrected.
     *
     * @param transcription the source transcription
     * @param tierNames the names of the tiers to export
     * @param exportFile the file to export to
     * @param minimalDuration the minimal duration for each annotation/subtitle
     *
     * @throws IOException any io exception
     */
    static public void exportTiersMerged(Transcription transcription,
        String[] tierNames, File exportFile, int minimalDuration)
        throws IOException {
        exportTiersMerged(transcription, tierNames, exportFile, 0L,
            Long.MAX_VALUE, 0L, minimalDuration, 0L, false, null );
    }
    
    /**
     * Replaces square brackets by parentheses because square brackets have 
     * a special meaning in the QT text format.
     *  
     * @param value the string value
     * @return a char array without square brackets
     */
    private static char[] replaceBrackets(String value) {
    	if (value == null || value.length() == 0) {
    		return new char[]{};
    	}
    	char[] ch = value.toCharArray();
    	
    	for (int i = 0; i < ch.length; i++) {
    		if (ch[i] == bracks[0]) {
    			ch[i] = bracks[2];
    		} else if (ch[i] == bracks[1]) {
    			ch[i] = bracks[3];
    		}
    	}
    	
    	return ch;
    }
    
    /**
     * Creates a string with  the necessary settings required for the subtitles
     *  
     * @param newSubtitleSetting the Map which has the subtitle settings
     * @return a char array without square brackets
     */
   
    
    private static String getSettings(HashMap newSubtitleSetting)
    {
    	if(newSubtitleSetting != null){
        	String setting = "{QTtext}{timescale:100}";
        	
        	if (newSubtitleSetting.get("font") != null){
        		setting += "{font:" + newSubtitleSetting.get("font").toString() + "}";
        	}else {
        		setting += "{font:Arial Unicode MS}";
        	}
        	
        	if(newSubtitleSetting.get("size") != null){
        		setting += "{size:" + newSubtitleSetting.get("size").toString() +"}";
        	}else{
        		setting +="{size:12}";
        	}
        	
        	if (newSubtitleSetting.get("backColor") != null )
        	{
        		
        		Color newColor = (Color)newSubtitleSetting.get("backColor");
        		setting += "{backColor:" + newColor.getRed() * (65535/255) + ","+ newColor.getGreen() *(65535/255) + ","+ newColor.getBlue() *(65535/255) + "}";
        	}
        	else{
        		setting += "{backColor:0,0,0}";
        	}
        	
        	if ( newSubtitleSetting.get("textColor") != null ){
        		Color newColor = (Color)newSubtitleSetting.get("textColor");
        		setting += "{textColor:" + newColor.getRed() * (65535/255) + ","+ newColor.getGreen()  * (65535/255)+ ","+ newColor.getBlue() * (65535/255) + "}";
        	}else{
        		setting += "{textColor:65535,65535,65535}";
        	}
        	
        	if(newSubtitleSetting.get("transparent") != null){
        		if((Boolean)newSubtitleSetting.get("transparent")){
        			setting += "{keyedText:on}";
        		} else{
        		setting += "{keyedText:off}";
        		}
        	}      		
        	
        	setting += "{width:320}";
        	
        	if(newSubtitleSetting.get("justify") != null){
        		setting +="{justify:" + newSubtitleSetting.get("justify").toString() +"}";
        	}else{
        		setting += "{justify:left}";
        	}
        	setting += NEWLINE;
        	
        	return setting;
        }
        else{
        	return "{QTtext}{timescale:100}{size:12}{backColor:0,0,0}{textColor:65535,65535,65535}{width:320}{justify:left}" + NEWLINE;
        }
    	
    	
    }
}
