package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.TimeSlot;

import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;


/**
 * A Parser for Eudico Annotation Format (EAF) compliant XML files. MAYBE THIS
 * CLASS MUST BE MADE THREAD SAFE BY ADDING SOME SYNCHRONIZED BLOCKS OR BY
 * GIVING UP THE SINGLETON PATTERN.
 *
 * @author Hennie Brugman
 * @version 18-Jul-2002
 */
public class EAF20Parser {
    /** The EAF v2.0 XML file is parsed. */
    private static boolean verbose = false;
    private static EAF20Parser parser;

    /** Holds value of property DOCUMENT ME! */
    private final SAXParser saxParser;

    /** Holds value of property DOCUMENT ME! */
    private final Hashtable tiers = new Hashtable();

    /** Holds value of property DOCUMENT ME! */
    private final Vector tierNames = new Vector();

    /** Holds value of property DOCUMENT ME! */
    private final Hashtable tierAttributes = new Hashtable();

    /** Holds value of property DOCUMENT ME! */
    private final Vector linguisticTypes = new Vector();

    /** Holds value of property DOCUMENT ME! */
    private final Vector locales = new Vector();

    /** Holds value of property DOCUMENT ME! */
    private final Hashtable timeSlots = new Hashtable();

    /** Holds value of property DOCUMENT ME! */
    private final Vector timeOrder = new Vector(); // since a Hashtable is not ordered, all time_slot_ids have to be stored in order separately.
    private String mediaFile;
    private String svgFile;
    private String author;
    private String currentTierId;
    private String currentAnnotationId;
    private String content = "";
    private String lastParsed = "";
    private String currentFileName;
    private boolean parseError;

    /**
     * Private constructor for EAFParser because the Singleton pattern is
     * applied here.
     */
    private EAF20Parser() {
        saxParser = new SAXParser();

        try {
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            saxParser.setFeature("http://apache.org/xml/features/validation/dynamic",
                true);
            saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                "http://www.mpi.nl/tools/elan/EAFv2.0.xsd");
            saxParser.setContentHandler(new EAFContentHandler());
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The instance method returns the single incarnation of EAFParser to the
     * caller.
     *
     * @return DOCUMENT ME!
     */
    public static EAF20Parser Instance() {
        if (parser == null) {
            parser = new EAF20Parser();
        }

        return parser;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMediaFile(String fileName) {
        parse(fileName);

        return mediaFile;
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
    public Vector getLinguisticTypes(String fileName) {
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
    public Vector getTimeOrder(String fileName) {
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
    public Hashtable getTimeSlots(String fileName) {
        parse(fileName);

        return timeSlots;
    }

    /**
     * Returns the names of the Tiers that are present in the Transcription
     * file
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierNames(String fileName) {
        parse(fileName);

        return tierNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        String part = "";

        if (((Hashtable) tierAttributes.get(tierName)).get("PARTICIPANT") != null) {
            part = (String) ((Hashtable) tierAttributes.get(tierName)).get(
                    "PARTICIPANT");
        }

        return part;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public LinguisticType getLinguisticTypeOf(String tierName, String fileName) {
        LinguisticType lt = null;

        parse(fileName);

        String lType = ""; // name of type

        if (((Hashtable) tierAttributes.get(tierName)).get(
                    "LINGUISTIC_TYPE_REF") != null) {
            lType = (String) ((Hashtable) tierAttributes.get(tierName)).get(
                    "LINGUISTIC_TYPE_REF");
        }

        Iterator ltIter = linguisticTypes.iterator();

        while (ltIter.hasNext()) {
            LinguisticType l = (LinguisticType) ltIter.next();

            if (l.getLinguisticTypeName().equals(lType)) {
                lt = l;

                break;
            }
        }

        return lt;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Locale getDefaultLanguageOf(String tierName, String fileName) {
        parse(fileName);

        Locale resultLoc = null;

        String localeId = (String) ((Hashtable) tierAttributes.get(tierName)).get(
                "DEFAULT_LOCALE");
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
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        return (String) ((Hashtable) tierAttributes.get(tierName)).get(
            "PARENT_REF");
    }

    /**
     * Returns a Vector with the Annotations for this Tier. Each
     * AnnotationRecord contains begin time, end time and text values
     * 
     * <p>
     * MK:02/06/10<br> Elements of Vector are no CLOM/ACM Annotations but yet
     * another Vector of String . The inner Vector is interpreted as variant
     * record in DAFTranscriptionStore.loadTranscription
     * </p>
     *
     * @param tierName DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     *
     * @return Vector of Vector of String
     */
    public Vector getAnnotationsOf(String tierName, String fileName) {
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

        long start = System.currentTimeMillis();

        Vector annotationVector = new Vector();

        // get the tags from the tiers Hashtable
        Hashtable annotations = (Hashtable) tiers.get(tierName);

        // get an iterator that iterates over the tags in the right order.
        Iterator iter = annotations.keySet().iterator();

        while (iter.hasNext()) {
            Vector annotationRecord = new Vector();

            Object key = iter.next();

            annotationRecord.add(key);
            annotationRecord.addAll(((Vector) annotations.get(key)));

            annotationVector.add(annotationRecord);
        }

        long duration = System.currentTimeMillis() - start;

        //	System.out.println("Extracting Annotations took " + duration + " milli seconds");
        return annotationVector;
    }

    /**
     * Parses a EAF v2.0 xml file.
     *
     * @param fileName the EAF v2.0 xml file that must be parsed.
     */
    private void parse(String fileName) {
        long start = System.currentTimeMillis();

        //		System.out.println("Parse : " + fileName);
        //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
        // only parse the same file once
        if (lastParsed.equals(fileName)) {
            return;
        }

        // (re)set everything to null for each parse
        tiers.clear();
        tierNames.clear(); // HB, 2-1-02, to store name IN ORDER
        tierAttributes.clear();
        mediaFile = "";
        linguisticTypes.clear();
        locales.clear();
        timeSlots.clear();
        timeOrder.clear();

        // parse the file
        lastParsed = fileName;
        currentFileName = fileName;

        try {
            saxParser.parse(fileName);
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            printErrorLocationInfo("Fatal(?) Error! " + e.getMessage());
        }

        long duration = System.currentTimeMillis() - start;

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
     * $Id: EAF20Parser.java 2 2004-03-25 16:22:33Z wouthuij $
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
                svgFile = attributes.getValue("SVG_FILE");
            } else if (name.equals("TIME_ORDER")) {
                // nothing to be done, tierOrder Vector already created
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
                    // create entries in the tiers and tierAttributes Hashtables for annotations and attributes resp.
                    tiers.put(currentTierId, new Hashtable());
                    tierAttributes.put(currentTierId, new Hashtable());

                    // HB, 2-1-02
                    tierNames.add(currentTierId);
                }

                // store tier attributes
                Hashtable attrHash = (Hashtable) tierAttributes.get(currentTierId);

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
            } else if (name.equals("ALIGNABLE_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations Hashtable for current tier
                ((Hashtable) tiers.get(currentTierId)).put(currentAnnotationId,
                    new Vector());

                // mark type of annotation, add start and end times to this AnnotationRecord
                String svg_ref = attributes.getValue("SVG_REF");
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add((svg_ref == null)
                    ? "alignable" : "alignable_svg");
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                        "TIME_SLOT_REF1"));
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                        "TIME_SLOT_REF2"));

                if (svg_ref != null) {
                    ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(svg_ref);
                }
            } else if (name.equals("REF_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations Hashtable for current tier
                ((Hashtable) tiers.get(currentTierId)).put(currentAnnotationId,
                    new Vector());

                // mark type of annotation, add annotation reference to this AnnotationRecord
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(
                    "reference");
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                        "ANNOTATION_REF"));

                if (attributes.getValue("PREVIOUS_ANNOTATION") != null) {
                    ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                            "PREVIOUS_ANNOTATION"));
                } else {
                    ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(
                        "");
                }
            } else if (name.equals("LINGUISTIC_TYPE")) {
                LinguisticType lt = new LinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_ID"));

                boolean timeAlignable = true;

                if ((attributes.getValue("TIME_ALIGNABLE") != null) &&
                        (attributes.getValue("TIME_ALIGNABLE").equals("false"))) {
                    timeAlignable = false;
                }

                lt.setTimeAlignable(timeAlignable);

                boolean graphicReferences = false;

                if ((attributes.getValue("GRAPHIC_REFERENCES") != null) &&
                        (attributes.getValue("GRAPHIC_REFERENCES").equals("true"))) {
                    graphicReferences = true;
                }

                lt.setGraphicReferences(graphicReferences);

                String stereotype = attributes.getValue("CONSTRAINTS");
                Constraint c = null;

                if (stereotype != null) {
                    stereotype = stereotype.replace('_', ' '); // for backwards compatibility

                    if (stereotype.equals(
                                Constraint.stereoTypes[Constraint.TIME_SUBDIVISION])) {
                        c = new TimeSubdivision();
                    } else if (stereotype.equals(
                                Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION])) {
                        c = new SymbolicSubdivision();
                    } else if (stereotype.equals(
                                Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION])) {
                        c = new SymbolicAssociation();
                    }
                }

                if (c != null) {
                    lt.addConstraint(c);
                }

                linguisticTypes.add(lt);
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
                ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(content);
            }
        }

        /**
         * ContentHandler method
         *
         * @param $paramType$ DOCUMENT ME!
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
