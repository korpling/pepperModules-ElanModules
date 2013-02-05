package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.TimeSlot;

import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;


/**
 * A Parser for Eudico Annotation Format (EAF) compliant XML files. 
 * In version 2.3 support for LinkedFleDescrptors and for stereotype 
 * Included In is added.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @version 1-Dec-2003
 * @version jun 2004 addition of ControlledVocabularies
 * @version sep 2005 the constructor is now public giving up the singleton pattern
 * the path parameter of all getter methods can be removed in the next parser version
 * (replace by a public parse(String path) method)
 * @version Feb 2006 support for LinkedFleDescrptors and for stereotype 
 * Included In is added. For compatibility reasons the filename parameter to the getters is maintained.
 */
public class EAF23Parser extends Parser {
    /** The EAF v2.3 XML file is parsed. */
    private static boolean verbose = false;

    /** Holds value of property DOCUMENT ME! */
    private final SAXParser saxParser;

    /** stores tiername - tierrecord pairs */
    private final HashMap tierMap = new HashMap();
    
    /** a map with tiername - ArrayList with Annotation Records pairs */
    private final HashMap tiers = new HashMap();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList tierNames = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    //private final HashMap tierAttributes = new HashMap();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList linguisticTypes = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList locales = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    private final HashMap timeSlots = new HashMap();
    
    /** stores the ControlledVocabulary objects by their ID */
    private final HashMap controlledVocabularies = new HashMap();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList timeOrder = new ArrayList(); // since a HashMap is not ordered, all time_slot_ids have to be stored in order separately.
    private String mediaFile;
    private ArrayList mediaDescriptors = new ArrayList();
    private ArrayList linkedFileDescriptors = new ArrayList();
    private String svgFile;
    private String author;
    private String currentTierId;
    private String currentAnnotationId;
    private AnnotationRecord currentAnnRecord;
    private String currentCVId;
    private CVEntryRecord currentEntryRecord;
    private String content = "";
    private String lastParsed = "";
    private String currentFileName;
    private boolean parseError;

    /**
     * Singleton pattern is removed in version 2.2.
     */
    public EAF23Parser() {
        saxParser = new SAXParser();

        try {
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            saxParser.setFeature("http://apache.org/xml/features/validation/dynamic",
                true);
            saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                "http://www.mpi.nl/tools/elan/EAFv2.3.xsd");
            saxParser.setContentHandler(new EAFContentHandler());
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * For backward compatibility; not used anymore
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return media file name
     */
    public String getMediaFile(String fileName) {
        parse(fileName);

        return mediaFile;
    }

    /**
     * Returns the media descriptors
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return the media descriptors
     */
    public ArrayList getMediaDescriptors(String fileName) {
        parse(fileName);

        return mediaDescriptors;
    }
    
    /**
     * Returns the linked file descriptors
     * 
     * @param fileName the eaf file name, for historic reasons
     * 
     * @return a list of linked file descriptors
     */
    public ArrayList getLinkedFileDescriptors(String fileName) {
        parse(fileName);
        
        return linkedFileDescriptors;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getSVGFile(String fileName) {
        parse(fileName);

        return svgFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAuthor(String fileName) {
        parse(fileName);

        return author;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getLinguisticTypes(String fileName) {
        parse(fileName);

        return linguisticTypes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getTimeOrder(String fileName) {
        parse(fileName);

        return timeOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HashMap getTimeSlots(String fileName) {
        parse(fileName);

        return timeSlots;
    }
    
	/**
	 * Returns a Hastable of ArrayLists with the cv id's as keys.<br>
	 * Each ArrayList can contain one String, the description and an 
	 * unknown number of CVEntryRecords.
	 *
	 * @param fileName the eaf filename
	 *
	 * @return a Hastable of ArrayLists with the cv id's as keys
	 */
    public HashMap getControlledVocabularies(String fileName) {
    	parse(fileName);
    	
    	return controlledVocabularies;
    }

    /**
     * Returns the names of the Tiers that are present in the Transcription
     * file
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getTierNames(String fileName) {
        parse(fileName);

        return tierNames;
    }

    /**
     * Returns participant attribute of a tier. 
     * The tier record is not used in TranscriptionStore yet.
     * 
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the participant
     */
    public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        //String part = "";
        
        if (tierMap.get(tierName) != null) {
        	if (((TierRecord) tierMap.get(tierName)).getParticipant() != null) {
        		return ((TierRecord) tierMap.get(tierName)).getParticipant();
        	}           
        }
        /*
        if (((HashMap) tierAttributes.get(tierName)).get("PARTICIPANT") != null) {
            part = (String) ((HashMap) tierAttributes.get(tierName)).get(
                    "PARTICIPANT");
        }
*/
        return "";
    }

    /**
     * Returns the name of the linguistic type of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return name of the type
     */
    public String getLinguisticTypeIDOf(String tierName, String fileName) {

        parse(fileName);

        //String lType = ""; // name of type
        if (tierMap.get(tierName) != null) {
        	if (((TierRecord) tierMap.get(tierName)).getLinguisticType() != null) {
        		return ((TierRecord) tierMap.get(tierName)).getLinguisticType();
        	}       
        }
        /*
        if (((HashMap) tierAttributes.get(tierName)).get(
                    "LINGUISTIC_TYPE_REF") != null) {
            lType = (String) ((HashMap) tierAttributes.get(tierName)).get(
                    "LINGUISTIC_TYPE_REF");
        }
        */
        return "";
    }

    /**
     * Returns the Locale object for a tier.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the default Locale object
     */
    public Locale getDefaultLanguageOf(String tierName, String fileName) {
        parse(fileName);

        Locale resultLoc = null;

        String localeId = null;
        //String localeId = (String) ((HashMap) tierAttributes.get(tierName)).get(
        //        "DEFAULT_LOCALE");
        if (tierMap.get(tierName) != null) {
            localeId = ((TierRecord) tierMap.get(tierName)).getDefaultLocale();
        }
         
        Iterator locIter = locales.iterator();

        while (locIter.hasNext()) {
            Locale l = (Locale) locIter.next();

            if (l.getLanguage().equals(localeId)) {
                resultLoc = l;
            }
        }

        return resultLoc;
    }

    /**
     * Returns the name of the parent tier, if any.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the name of the parent tier, or null
     */
    public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
            return ((TierRecord) tierMap.get(tierName)).getParentTier();
        }
        //return (String) ((HashMap) tierAttributes.get(tierName)).get(
        //    "PARENT_REF");
        return null;
    }

    /**
     * Returns a ArrayList with the Annotations for this Tier. Each
     * AnnotationRecord contains begin time, end time and text values
     * 
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return ArrayList of AnnotationRecord objects for the tier
     */
    public ArrayList getAnnotationsOf(String tierName, String fileName) {
        // make sure that the correct file has been parsed
        parse(fileName);

        //long start = System.currentTimeMillis();
        return (ArrayList) tiers.get(tierName);
        /*
        ArrayList annotationList = new ArrayList();

        // get the tags from the tiers HashMap
        HashMap annotations = (HashMap) tiers.get(tierName);

        // get an iterator that iterates over the tags in the right order.
        Iterator iter = annotations.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            
			annotationList.add(annotations.get(key));
        }

        //long duration = System.currentTimeMillis() - start;

        //	System.out.println("Extracting Annotations took " + duration + " milli seconds");
        return annotationList;
        */
    }

    /**
     * Parses a EAF v2.3 xml file.
     *
     * @param fileName the EAF v2.3 xml file that must be parsed.
     */
    private void parse(String fileName) {
        //long start = System.currentTimeMillis();

        //		System.out.println("Parse : " + fileName);
        //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
        // only parse the same file once
        if (lastParsed.equals(fileName)) {
            return;
        }

        // (re)set everything to null for each parse
        tiers.clear();
        tierNames.clear(); // HB, 2-1-02, to store name IN ORDER
        //tierAttributes.clear();
        mediaFile = "";
        linguisticTypes.clear();
        locales.clear();
        timeSlots.clear();
        timeOrder.clear();
        mediaDescriptors.clear();
        linkedFileDescriptors.clear();
        controlledVocabularies.clear();

        // parse the file
        lastParsed = fileName;
        currentFileName = fileName;

        try {
            saxParser.parse(fileName);
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
			// the SAX parser can have difficulties with certain characters in 
			// the filepath: try to create an InputSource for the parser 
            File f = new File(fileName);
            if (f.exists()) {
				try {
					FileInputStream fis = new FileInputStream(f);
					InputSource source = new InputSource(fis);
					saxParser.parse(source);
					// just catch any exception
				} catch (Exception ee) {
					System.out.println("Parsing retry error: " + ee.getMessage());
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            printErrorLocationInfo("Fatal(?) Error! " + e.getMessage());
        }

        //long duration = System.currentTimeMillis() - start;

        //	System.out.println("Parsing took " + duration + " milli seconds");
    }

    private void println(String s) {
        if (verbose) {
            System.out.println(s);
        }
    }

    private void printErrorLocationInfo(String message) {
        System.out.println(message);
        System.out.println("Exception for " + currentFileName);
        System.out.println("Tier id " + currentTierId);
        System.out.println("Annotation id " + currentAnnotationId);
    }

    /**
     * DOCUMENT ME!
     * $Id: EAF23Parser.java 7255 2006-11-06 15:27:08Z hasloe $
     * @author $Author$
     * @version $Revision$
     */
    class EAFContentHandler implements ContentHandler {
        private Locator locator;

        /**
         * DOCUMENT ME!
         *
         * @param locator DOCUMENT ME!
         */
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        /**
         * DOCUMENT ME!
         *
         * @param prefix DOCUMENT ME!
         * @param uri DOCUMENT ME!
         */
        public void startPrefixMapping(String prefix, String uri) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param prefix DOCUMENT ME!
         */
        public void endPrefixMapping(String prefix) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param ch DOCUMENT ME!
         * @param start DOCUMENT ME!
         * @param end DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void ignorableWhitespace(char[] ch, int start, int end)
            throws SAXException {
        }

        /**
         * DOCUMENT ME!
         *
         * @param name DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void skippedEntity(String name) throws SAXException {
        }

        /**
         * DOCUMENT ME!
         *
         * @param target DOCUMENT ME!
         * @param data DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void processingInstruction(String target, String data)
            throws SAXException {
        }

        /**
         * ContentHandler method
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void startDocument() throws SAXException {
            parseError = false;
        }

        /**
         * ContentHandler method
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void endDocument() throws SAXException {
        }

        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         * @param attributes DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
            //	System.out.println("startElement called for name:" + name);
            content = "";

            if (name.equals("ANNOTATION_DOCUMENT")) {
                author = attributes.getValue("AUTHOR");
            } else if (name.equals("HEADER")) {
                // implement when dealing with MediaObject
                mediaFile = attributes.getValue("MEDIA_FILE");
                // remove, there is not such attribute in the eaf schema
                svgFile = attributes.getValue("SVG_FILE");
            } else if (name.equals("MEDIA_DESCRIPTOR")) {
                String mediaURL = attributes.getValue("MEDIA_URL");
                String mimeType = attributes.getValue("MIME_TYPE");

                MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);

                long timeOrigin = 0;

                if (attributes.getValue("TIME_ORIGIN") != null) {
                    timeOrigin = Long.parseLong(attributes.getValue(
                                "TIME_ORIGIN"));
                    md.timeOrigin = timeOrigin;
                }

                String extractedFrom = "";

                if (attributes.getValue("EXTRACTED_FROM") != null) {
                    extractedFrom = attributes.getValue("EXTRACTED_FROM");
                    md.extractedFrom = extractedFrom;
                }

                mediaDescriptors.add(md);
            } else if (name.equals("LINKED_FILE_DESCRIPTOR")) {
                String linkURL = attributes.getValue("LINK_URL");
                String mime = attributes.getValue("MIME_TYPE");
                LinkedFileDescriptor lfd = new LinkedFileDescriptor(linkURL, mime);
                
                if (attributes.getValue("TIME_ORIGIN") != null) {
                    try {
                        long origin = Long.parseLong(attributes.getValue("TIME_ORIGIN"));
                        lfd.timeOrigin = origin;
                    } catch (NumberFormatException nfe) {
                        System.out.println("Could not parse the time origin: " + nfe.getMessage());
                    }
                }
                
                String assoc = attributes.getValue("ASSOCIATED_WITH");
                if (assoc != null) {
                    lfd.associatedWith = assoc;
                }
                linkedFileDescriptors.add(lfd);
            } else if (name.equals("TIME_ORDER")) {
                // nothing to be done, tierOrder ArrayList already created
            } else if (name.equals("TIME_SLOT")) {
                String timeValue = String.valueOf(TimeSlot.TIME_UNALIGNED);

                if (attributes.getValue("TIME_VALUE") != null) {
                    timeValue = attributes.getValue("TIME_VALUE");
                }

                timeSlots.put(attributes.getValue("TIME_SLOT_ID"), timeValue);
                timeOrder.add(attributes.getValue("TIME_SLOT_ID"));
            } else if (name.equals("TIER")) {
                currentTierId = attributes.getValue("TIER_ID");

                // First check whether this tier already exists
                if (!tiers.containsKey(currentTierId)) {
                    // create a record
                    TierRecord tr = new TierRecord();
                    tr.setName(currentTierId);
                    tierMap.put(currentTierId, tr);

                    tr.setParticipant(attributes.getValue("PARTICIPANT"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));
                    
                    // create entries in the tiers and tierAttributes HashMaps for annotations and attributes resp.
                    tiers.put(currentTierId, new ArrayList());
                    /*
                     * tierAttributes.put(currentTierId, new HashMap());
                    */
                    // HB, 2-1-02
                    tierNames.add(currentTierId);
                }

                // store tier attributes
                /*
                HashMap attrHash = (HashMap) tierAttributes.get(currentTierId);

                if (attributes.getValue("PARTICIPANT") != null) {
                    attrHash.put("PARTICIPANT",
                        attributes.getValue("PARTICIPANT"));
                }

                attrHash.put("LINGUISTIC_TYPE_REF",
                    attributes.getValue("LINGUISTIC_TYPE_REF"));

                if (attributes.getValue("DEFAULT_LOCALE") != null) { // HB, 29 oct 02: added condition
                    attrHash.put("DEFAULT_LOCALE",
                        attributes.getValue("DEFAULT_LOCALE"));
                } else { // HB, 30 oct 02, added default case
                    attrHash.put("DEFAULT_LOCALE", "en");
                }

                if (attributes.getValue("PARENT_REF") != null) {
                    attrHash.put("PARENT_REF", attributes.getValue("PARENT_REF"));
                }
                */
            } else if (name.equals("ALIGNABLE_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                currentAnnRecord = new AnnotationRecord();
                currentAnnRecord.setAnnotationId(currentAnnotationId);
				String svg_ref = attributes.getValue("SVG_REF");
				if (svg_ref != null) {
				    currentAnnRecord.setAnnotationType(AnnotationRecord.ALIGNABLE_SVG);
				    currentAnnRecord.setSvgReference(svg_ref);
				} else {
				    currentAnnRecord.setAnnotationType(AnnotationRecord.ALIGNABLE);
				}
				currentAnnRecord.setBeginTimeSlotId(attributes.getValue("TIME_SLOT_REF1"));
				currentAnnRecord.setEndTimeSlotId(attributes.getValue("TIME_SLOT_REF2"));
				((ArrayList) tiers.get(currentTierId)).add(currentAnnRecord);

            } else if (name.equals("REF_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                 currentAnnRecord = new AnnotationRecord();
                 currentAnnRecord.setAnnotationId(currentAnnotationId);
                 currentAnnRecord.setAnnotationType(AnnotationRecord.REFERENCE);
                 currentAnnRecord.setReferredAnnotId(attributes.getValue("ANNOTATION_REF"));
				if (attributes.getValue("PREVIOUS_ANNOTATION") != null) {
				    currentAnnRecord.setPreviousAnnotId(attributes.getValue("PREVIOUS_ANNOTATION"));
				} else {
				    currentAnnRecord.setPreviousAnnotId("");
				}
				((ArrayList) tiers.get(currentTierId)).add(currentAnnRecord);

            } else if (name.equals("LINGUISTIC_TYPE")) {
            	LingTypeRecord ltr = new LingTypeRecord();

				ltr.setLingTypeId(attributes.getValue(
						"LINGUISTIC_TYPE_ID"));
						
                String timeAlignable = "true";

                if ((attributes.getValue("TIME_ALIGNABLE") != null) &&
                        (attributes.getValue("TIME_ALIGNABLE").equals("false"))) {
                    timeAlignable = "false";
                }

                ltr.setTimeAlignable(timeAlignable);

                String graphicReferences = "false";

                if ((attributes.getValue("GRAPHIC_REFERENCES") != null) &&
                        (attributes.getValue("GRAPHIC_REFERENCES").equals("true"))) {
                    graphicReferences = "true";
                }

                ltr.setGraphicReferences(graphicReferences);

                String stereotype = attributes.getValue("CONSTRAINTS");
                ltr.setStereoType(stereotype);
                
				ltr.setControlledVocabulary(
					attributes.getValue("CONTROLLED_VOCABULARY_REF"));

                linguisticTypes.add(ltr);
            } else if (name.equals("LOCALE")) {
                String langCode = attributes.getValue("LANGUAGE_CODE");
                String countryCode = attributes.getValue("COUNTRY_CODE");

                if (countryCode == null) {
                    countryCode = "";
                }

                String variant = attributes.getValue("VARIANT");

                if (variant == null) {
                    variant = "";
                }

                Locale l = new Locale(langCode, countryCode, variant);
                locales.add(l);
            } else if (name.equals("CONTROLLED_VOCABULARY")) {
            	currentCVId = attributes.getValue("CV_ID");
            	ArrayList cv = new ArrayList();
            	
            	String desc = attributes.getValue("DESCRIPTION");
            	if (desc != null) {
            		cv.add(desc);
            	}
            	controlledVocabularies.put(currentCVId, cv);
            } else if (name.equals("CV_ENTRY")) {
            	currentEntryRecord = new CVEntryRecord();
            	
				currentEntryRecord.setDescription(
					attributes.getValue("DESCRIPTION"));
					
				((ArrayList)controlledVocabularies.get(currentCVId)).add(currentEntryRecord);
            }
        }
         //startElement

        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void endElement(String nameSpaceURI, String name, String rawName)
            throws SAXException {
            if (name.equals("ANNOTATION_VALUE")) {
                currentAnnRecord.setValue(content);
                //((AnnotationRecord) ((HashMap) tiers.get(currentTierId)).get(currentAnnotationId)).setValue(content);
            } else if (name.equals("CV_ENTRY")) {
            	currentEntryRecord.setValue(content);
            }
        }

        /**
         * ContentHandler method
         *
         * @param ch DOCUMENT ME!
         * @param start DOCUMENT ME!
         * @param end DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
        public void characters(char[] ch, int start, int end)
            throws SAXException {
            content += new String(ch, start, end);
        }
    }
}
