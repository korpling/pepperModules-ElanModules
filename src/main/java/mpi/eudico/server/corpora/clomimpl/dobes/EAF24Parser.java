package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;

/**
 * A (SAX2) Parser for Elan Annotation Format (EAF) compliant XML files. 
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
 * @version Dec 2006 element PROPERTY has been added to the HEADER element, attribute
 * ANNOTATOR has been added to element TIER
 */
public class EAF24Parser extends Parser {
    private boolean verbose = false;
	private XMLReader reader;
	
	/** stores tiername - tierrecord pairs */
    private final HashMap tierMap = new HashMap();
    
    /** a map with tiername - ArrayList with Annotation Records pairs */
    private final HashMap tiers = new HashMap();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList tierNames = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList linguisticTypes = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    private final ArrayList locales = new ArrayList();

    /** Holds value of property DOCUMENT ME! */
    private final HashMap timeSlots = new HashMap();
    
    /** stores the ControlledVocabulary objects by their ID */
    private final HashMap controlledVocabularies = new HashMap();
    
    private final ArrayList docProperties = new ArrayList();

    /** stores the time slots orderd by id */
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
    private PropertyImpl currentProperty;
    private boolean parseError;

    /**
     * Constructor, creates a new XMLReader
     *
     */
    public EAF24Parser() {
    	try {  	
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", true);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	        reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
	        		this.getClass().getResource("/mpi/eudico/resources/EAFv2.4.xsd").openStream());
	        //reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
	        //		"http://www.mpi.nl/tools/elan/EAFv2.4.xsd");
	        reader.setContentHandler(new EAFContentHandler());
	        //reader.setErrorHandler(new EAFErrorHandler());

    	} catch (SAXException se) {
    		se.printStackTrace();
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
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
     * Returns a list of PropertyImpl objects that have been retrieved from the eaf.
     * 
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTranscriptionProperties(java.lang.String)
	 */
	public ArrayList getTranscriptionProperties(String fileName) {
		parse(fileName);
		
		return docProperties;
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
        
        if (tierMap.get(tierName) != null) {
        	if (((TierRecord) tierMap.get(tierName)).getParticipant() != null) {
        		return ((TierRecord) tierMap.get(tierName)).getParticipant();
        	}           
        }

        return "";
    }

    /**
     * Returns the annotator attribute of a tier. 
     * The tier record is not used in TranscriptionStore yet.
     * 
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the annotator of the tier
     */
    public String getAnnotatorOf(String tierName, String fileName) {
    	parse(fileName);
    	
        if (tierMap.get(tierName) != null) {
        	if (((TierRecord) tierMap.get(tierName)).getAnnotator() != null) {
        		return ((TierRecord) tierMap.get(tierName)).getAnnotator();
        	}           
        }

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

        if (tierMap.get(tierName) != null) {
        	if (((TierRecord) tierMap.get(tierName)).getLinguisticType() != null) {
        		return ((TierRecord) tierMap.get(tierName)).getLinguisticType();
        	}       
        }

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

        return (ArrayList) tiers.get(tierName);
    }

    /**
     * Parses a EAF v2.4 (or <) xml file.
     *
     * @param fileName the EAF v2.4 xml file that must be parsed.
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
            reader.parse(fileName);
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
			// the SAX parser can have difficulties with certain characters in 
			// the filepath: try to create an InputSource for the parser 
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            File f = new File(fileName);
            if (f.exists()) {
				try {
					FileInputStream fis = new FileInputStream(f);
					InputSource source = new InputSource(fis);
					reader.parse(source);
					// just catch any exception
				} catch (Exception ee) {
					System.out.println("Parsing retry error: " + ee.getMessage());
				}
            }
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
			// the SAX parser can have difficulties with certain characters in 
			// the filepath: try to create an InputSource for the parser 
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            File f = new File(fileName);
            if (f.exists()) {
				try {
					FileInputStream fis = new FileInputStream(f);
					InputSource source = new InputSource(fis);
					reader.parse(source);
					// just catch any exception
				} catch (Exception ee) {
					System.out.println("Parsing retry error: " + ee.getMessage());
				}
            }
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
     * An error handler for the eaf parser.<br>
     * The exception thrown (by Xerces 2.6.2) contains apart from file name,
     * line and column number, only a description of the problem in it's message.
     * To really deal with a problem a handler would need to parse the message 
     * for certain strings (defined in a Xerces resource .properties file) and/or
     * read the file to the specified problem line. 
     * Problematic... 
     * 
     * @author Han Sloetjes, MPI
     */
    class EAFErrorHandler implements ErrorHandler {

		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage());
			// system id is the file path
			System.out.println("System id" + exception.getSystemId());
			System.out.println("Public id" + exception.getPublicId());
			System.out.println("Line: " + exception.getLineNumber());
			System.out.println("Column: " + exception.getColumnNumber());
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());
			
		}

		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());
			
		}
    	
    }
    
    /**
     * EAF 2.4 content handler.
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
            } else if (name.equals("PROPERTY")) {
                // transcription properties
            	currentProperty = new PropertyImpl();
            	if (attributes.getValue("NAME") != null) {
            		currentProperty.setName(attributes.getValue("NAME"));
            	}
            	docProperties.add(currentProperty);
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
                    tr.setAnnotator(attributes.getValue("ANNOTATOR"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));
                    
                    // create entries in the tiers and tierAttributes HashMaps for annotations and attributes resp.
                    tiers.put(currentTierId, new ArrayList());

                    tierNames.add(currentTierId);
                }
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
            } else if (name.equals("CV_ENTRY")) {
            	currentEntryRecord.setValue(content);
            } else if (name.equals("PROPERTY")) {
            	if (content.length() > 0) {
            		currentProperty.setValue(content);
            	}       	
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
