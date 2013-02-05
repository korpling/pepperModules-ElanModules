package mpi.eudico.client.annotator.imports.praat;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clomimpl.praat.PraatSpecialChars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class to extract annotations from a Praat .TextGrid file.
 * Only "IntervalTier"s are supported.
 * The expected format is roughly like below, but the format is only losely checked.
 * 
 * 1 File type = "ooTextFile"
 * 2 Object class = "TextGrid"
 * 3 
 * 4 xmin = 0 
 * 5 xmax = 36.59755102040816 
 * 6 tiers? &lt;exists&; 
 * 7 size = 2 
 * 8 item []: 
 * 9     item [1]:
 * 10         class = "IntervalTier" 
 * 11         name = "One" 
 * 12         xmin = 0 
 * 13         xmax = 36.59755102040816 
 * 14         intervals: size = 5 
 * 15         intervals [1]:
 * 16             xmin = 0 
 * 17             xmax = 1 
 * 18             text = "" 
 */
public class PraatTextGrid implements ClientLogger {
    private final char brack = '[';
    private final String eq = "=";
    private final String item = "item";
    private final String cl = "class";
    private final String tierSpec = "IntervalTier";
    private final String textTierSpec = "TextTier";
    private final String nm = "name";
    private final String interval = "intervals";
    private final String min = "xmin";
    private final String max = "xmax";
    private final String tx = "text";
    private final String points = "points";
    private final String time = "time";
    private final String mark = "mark";
    private final String number = "number";
    
    private boolean includeTextTiers = false;
    private int pointDuration = 1;
    private String encoding;
    
    private File gridFile;
    private List tierNames;
    private Map annotationMap;
    private PraatSpecialChars lookUp;

    /**
     * Creates a new Praat TextGrid parser for the file at the specified path
     *
     * @param fileName the path to the file
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(String fileName) throws IOException {
        this(fileName, false, 1);
    }
    
    /**
     * Creates a new Praat TextGrid parser for the file at the specified path. 
     *
     * @param fileName the path to the file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(String fileName, boolean includeTextTiers, int pointDuration) 
        throws IOException {
        if (fileName != null) {
            gridFile = new File(fileName);
        }
        this.includeTextTiers = includeTextTiers;
        this.pointDuration = pointDuration;
        
        parse();
    }

    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     *
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile) throws IOException {
        this(gridFile, false, 1);
    }

    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     * 
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile, boolean includeTextTiers, int pointDuration) throws IOException {
        this(gridFile, includeTextTiers, pointDuration, null);
    }
    
    /**
     * Creates a new Praat TextGrid parser for the specified file.
     *
     * @param gridFile the TextGrid file
     * @param includeTextTiers if true "TextTiers" will also be parsed
     * @param pointDuration the duration of annotations if texttiers are also parsed  
     * @param encoding the character encoding of the file
     * 
     * @throws IOException if the file can not be read, for whatever reason
     */
    public PraatTextGrid(File gridFile, boolean includeTextTiers, int pointDuration, 
    		String encoding) throws IOException {
        this.gridFile = gridFile;
        
        this.includeTextTiers = includeTextTiers;
        this.pointDuration = pointDuration;
        this.encoding = encoding;
        
        parse();
    }
    
    /**
     * Returns a list of detected interval tiers.
     *
     * @return a list of detected interval tiernames
     */
    public List getTierNames() {
        return tierNames;
    }

    /**
     * Returns a list of annotation records for the specified tier.
     *
     * @param tierName the name of the tier
     *
     * @return the annotation records of the specified tier
     */
    public List getAnnotationRecords(String tierName) {
        if ((tierName == null) || (annotationMap == null)) {
            return null;
        }

        Object value = annotationMap.get(tierName);

        if (value instanceof List) {
            return (List) value;
        }

        return null;
    }

    /**
     * Parses the file and extracts interval tiers with their annotations.
     *
     * @throws IOException if the file can not be read for any reason
     */
    private void parse() throws IOException {
        if ((gridFile == null) || !gridFile.exists()) {
            LOG.warning("No existing file specified.");
            throw new IOException("No existing file specified.");
        }

        BufferedReader reader = null;

        try {
        	if (encoding == null) {      	
        		reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(gridFile)));
        	} else {
        		try {
        			reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(gridFile), encoding));
        		} catch (UnsupportedEncodingException uee) {
        			LOG.warning("Unsupported encoding: " + uee.getMessage());
        			reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(gridFile)));
        		}
        	}
            // Praat files on Windows and Linux are created with encoding "Cp1252"
            // on Mac with encoding "MacRoman". The ui could/should be extended
            // with an option to specify the encoding
            // InputStreamReader isr = new InputStreamReader(
            //        new FileInputStream(gridFile));
            // System.out.println("Encoding: " + isr.getEncoding());
        	System.out.println("Read encoding: " + encoding);
        	
            tierNames = new ArrayList(4);
            annotationMap = new HashMap(4);

            ArrayList records = new ArrayList();
            AnnotationDataRecord record = null;

            String line;
            //int lineNum = 0;
            String tierName = null;
            String annValue = "";
            long begin = -1;
            long end = -1;
            boolean inTier = false;
            boolean inInterval = false;
            boolean inTextTier = false;
            boolean inPoints = false;
            int eqPos = -1;

            while ((line = reader.readLine()) != null) {
                //lineNum++;
                //System.out.println(lineNum + " " + line);

                if ((line.indexOf(cl) >= 0) && 
                        ((line.indexOf(tierSpec) > 5) || (line.indexOf(textTierSpec) > 5))) {
                    // check if we have to include text (point) tiers
                    if (line.indexOf(textTierSpec) > 5) {
                        if (includeTextTiers) {
                            inTextTier = true;
                        } else {
                            inTextTier = false;
                            inTier = false;
                            continue;
                        }
                    }
                    // begin of a new tier
                    records = new ArrayList();
                    inTier = true;
            
                    continue;
                }

                if (!inTier) {
                    continue;
                }

                eqPos = line.indexOf(eq);
                
                if (inTextTier) {
                    // text or point tier
                    if (eqPos > 0) {
	                    // split and parse
	                    if (!inPoints && (line.indexOf(nm) >= 0) &&
	                            (line.indexOf(nm) < eqPos)) {
	                        tierName = extractTierName(line, eqPos);
	
	                        if (!annotationMap.containsKey(tierName)) {
	                            annotationMap.put(tierName, records);
	                            tierNames.add(tierName);
	                            LOG.info("Point Tier detected: " + tierName);
	                        } else {
	                        	// the same (sometimes empty) tiername can occur more than once, rename
	                        	int count = 2;
	                        	String nextName = "";
	                        	for (; count < 50; count++) {
	                        		nextName = tierName + "-" + count;
	                        		if (!annotationMap.containsKey(nextName)) {
	    	                            annotationMap.put(nextName, records);
	    	                            tierNames.add(nextName);
	    	                            LOG.info("Tier detected: " + tierName + " and renamed to: " + nextName);
	    	                            break;
	                        		}
	                        	}
	                        }
	
	                        continue;
	                    } else if (!inPoints) {
	                        continue;
	                    } else if (line.indexOf(time) > -1 || line.indexOf(number) > -1) {
	                        begin = extractLong(line, eqPos);
	                        //System.out.println("B: " + begin);
	                    } else if (line.indexOf(mark) > -1) {
	                        // extract value
	                        annValue = extractTextValue(line, eqPos);
	                        // finish and add the annotation record
	                        inPoints = false;
	                        //System.out.println("T: " + annValue);
	                        record = new AnnotationDataRecord(tierName, annValue,
	                                begin, begin + pointDuration);
	                        records.add(record);
	                        // reset
	                        annValue = "";
	                        begin = -1;
	                    }
	                } else {
	                    // points??
	                    if ((line.indexOf(points) >= 0) &&
	                            (line.indexOf(brack) > points.length())) {
	                        inPoints = true;
	
	                        continue;
	                    } else {
	                        if ((line.indexOf(item) >= 0) &&
	                                (line.indexOf(brack) > item.length())) {
	                            // reset
	                            inTextTier = false;
	                            inPoints = false;
	                        }
	                    }
	                } // end point tier
                } else {
                    // interval tier
	                if (eqPos > 0) {
	                    // split and parse
	                    if (!inInterval && (line.indexOf(nm) >= 0) &&
	                            (line.indexOf(nm) < eqPos)) {
	                        tierName = extractTierName(line, eqPos);
	
	                        if (!annotationMap.containsKey(tierName)) {
	                            annotationMap.put(tierName, records);
	                            tierNames.add(tierName);
	                            LOG.info("Tier detected: " + tierName);
	                        } else {
	                        	// the same (sometimes empty) tiername can occur more than once, rename
	                        	int count = 2;
	                        	String nextName = "";
	                        	for (; count < 50; count++) {
	                        		nextName = tierName + "-" + count;
	                        		if (!annotationMap.containsKey(nextName)) {
	    	                            annotationMap.put(nextName, records);
	    	                            tierNames.add(nextName);
	    	                            LOG.info("Tier detected: " + tierName + " and renamed to: " + nextName);
	    	                            break;
	                        		}
	                        	}
	                        }
	
	                        continue;
	                    } else if (!inInterval) {
	                        continue;
	                    } else if (line.indexOf(min) > -1) {
	                        begin = extractLong(line, eqPos);
	                        //System.out.println("B: " + begin);
	                    } else if (line.indexOf(max) > -1) {
	                        end = extractLong(line, eqPos);
	                        //System.out.println("E: " + end);
	                    } else if (line.indexOf(tx) > -1) {
	                        // extract value
	                        annValue = extractTextValue(line, eqPos);
	                        // finish and add the annotation record
	                        inInterval = false;
	                        //System.out.println("T: " + annValue);
	                        record = new AnnotationDataRecord(tierName, annValue,
	                                begin, end);
	                        records.add(record);
	                        // reset
	                        annValue = "";
	                        begin = -1;
	                        end = -1;
	                    }
	                } else {
	                    // interval?
	                    if ((line.indexOf(interval) >= 0) &&
	                            (line.indexOf(brack) > interval.length())) {
	                        inInterval = true;
	
	                        continue;
	                    } else {
	                        if ((line.indexOf(item) >= 0) &&
	                                (line.indexOf(brack) > item.length())) {
	                            // reset
	                            inTier = false;
	                            inInterval = false;
	                        }
	                    }
	                }
                }
            }

            reader.close();
        } catch (IOException ioe) {
            if (reader != null) {
                reader.close();
            }

            throw ioe;
        } catch (Exception fe) {
            if (reader != null) {
                reader.close();
            }

            throw new IOException("Error occurred while reading the file: " +
                fe.getMessage());
        }
    }

    /**
     * Extracts the tiername from a line.
     *
     * @param line the line
     * @param eqPos the indexof the '=' sign
     *
     * @return the tier name
     */
    private String extractTierName(String line, int eqPos) {
        if (line.length() > (eqPos + 1)) {
            String name = line.substring(eqPos + 1).trim();

            if (name.length() < 3) {
            	if ("\"\"".equals(name)) {
            		return "Noname";
            	}
            	
                return name;
            }

            return removeQuotes(name);
        }

        return line; // or null??
    }

    /**
     * Extracts the text value and, if needed, converts Praat's special
     * character sequences into unicode chars.
     *
     * @param value the text value
     * @param eqPos the index of the equals sign
     *
     * @return the annotation value. If necessary Praat's special symbols have
     *         been converted  to Unicode.
     */
    private String extractTextValue(String value, int eqPos) {
        if (value.length() > (eqPos + 1)) {
            String rawV = removeQuotes(value.substring(eqPos + 1).trim()); // should be save

            if (lookUp == null) {
                lookUp = new PraatSpecialChars();
            }
            rawV = lookUp.replaceIllegalXMLChars(rawV);
            
            if (rawV.indexOf('\\') > -1) {
                // convert
//                if (lookUp == null) {
//                    lookUp = new PraatSpecialChars();
//                }

                return lookUp.convertSpecialChars(rawV);
            }

            return rawV;
        }

        return "";
    }

    /**
     * Extracts a double time value, multiplies by 1000 (sec to ms) and
     * converts to long.
     *
     * @param value the raw value
     * @param eqPos the index of the equals sign
     *
     * @return the time value rounded to milliseconds
     */
    private long extractLong(String value, int eqPos) {
        if (value.length() > (eqPos + 1)) {
            String v = value.substring(eqPos + 1).trim();
            long l = -1;

            try {
                Double d = new Double(v);
                l = Math.round(d.doubleValue() * 1000);
            } catch (NumberFormatException nfe) {
                LOG.warning("Not a valid numeric value: " + value);
            }

            return l;
        }

        return -1;
    }

    /**
     * Removes a beginning and end quote mark from the specified string. Does
     * no null check nor are spaces trimmed.
     *
     * @param value the value of which leading and trailing quote chars should
     *        be removed
     *
     * @return the value without the quotes
     */
    private String removeQuotes(String value) {
        if (value.charAt(0) == '"') {
            if (value.charAt(value.length() - 1) == '"' && value.length() > 1) {
                return value.substring(1, value.length() - 1);
            } else {
                return value.substring(1);
            }
        } else {
            if (value.charAt(value.length() - 1) == '"') {
                return value.substring(0, value.length() - 1);
            } else {
                return value;
            }
        }
    }

}
