package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.TimeSlot;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.File;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * A Parser for Dobes Annotation Format (DAF) compliant XML files. MAYBE THIS
 * CLASS MUST BE MADE THREAD SAFE BY ADDING SOME SYNCHRONIZED BLOCKS OR BY
 * GIVING UP THE SINGLETON PATTERN.
 *
 * @author Hennie Brugman
 * @version 6-Jul-2001
 */
public class DAFParser extends HandlerBase {
    private static DAFParser parser;

    /** The DOBES DAF XML file is parsed. */
    private final Float ORDERED_KEYS_KEY = new Float(0.12345);
    private boolean verbose;
    private SAXParser saxParser;
    private String lastParsed;
    private String currentFileName;
    private File xmlFile;
    private boolean parseError;
    private Hashtable tiers;
    private Vector tierNames;
    private Hashtable tierAttributes;
    private String mediaFile;
    private String author;
    private Vector linguisticTypes;
    private Vector locales;
    private Hashtable timeSlots;
    private Vector timeOrder; // since a Hashtable is not ordered, all time_slot_ids have to be stored in order separately.
    private String currentTierId;
    private String currentAnnotationId;
    private String currentSpeakerId;
    private String currentStart;
    private String currentEnd;
    private String content;

    /**
     * Private constructor for DAFParser because the Singleton pattern is
     * applied here.
     */
    private DAFParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            saxParser = factory.newSAXParser();
            lastParsed = "";
            verbose = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The instance method returns the single incarnation of DAFParser to the
     * caller.
     *
     * @return DOCUMENT ME!
     */
    public static DAFParser Instance() {
        if (parser == null) {
            parser = new DAFParser();
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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

        return mediaFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAuthor(String fileName) {
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

        //	Vector tierNames = new Vector(tiers.keySet());
        //	Collections.sort(tierNames);
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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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

        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
        // make sure that the correct file has been parsed
        if (!lastParsed.equals(fileName)) {
            parse(fileName);
        }

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
     * Parses a DOBES-minimal compliant xml file.
     *
     * @param fileName the DOBES-minimal compliant xml file that must be
     *        parsed.
     */
    private void parse(String fileName) {
        long start = System.currentTimeMillis();

        try {
            //		System.out.println("Parse : " + fileName);
            //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
            // only parse the same file once
            if (lastParsed.equals(fileName)) {
                return;
            }

            // (re)set everything to null for each parse
            tiers = new Hashtable();
            tierNames = new Vector(); // HB, 2-1-02, to store name IN ORDER
            tierAttributes = new Hashtable();
            mediaFile = "";
            linguisticTypes = new Vector();
            locales = new Vector();
            timeSlots = new Hashtable();
            timeOrder = new Vector();

            // parse the file
            xmlFile = new File(fileName);
            lastParsed = fileName;
            currentFileName = fileName;
            saxParser.parse(xmlFile, this);
        } catch (Exception e) {
            printErrorLocationInfo("Fatal(?) Error! " + e.getMessage());
        }

        long duration = System.currentTimeMillis() - start;

        //	System.out.println("Parsing took " + duration + " milli seconds");
    }

    /**
     * HandlerBase method
     */
    public void startDocument() {
        parseError = false;
    }

    /**
     * HandlerBase method
     */
    public void endDocument() {
    }

    /**
     * HandlerBase method
     *
     * @param name DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     */
    public void startElement(String name, AttributeList attributes) {
        //	System.out.println("startElement called for name:" + name);
        content = null;

        if (name.equals("ANNOTATION_DOCUMENT")) {
            author = attributes.getValue("AUTHOR");
        } else if (name.equals("HEADER")) {
            // implement when dealing with MediaObject
            mediaFile = attributes.getValue("MEDIA_FILE");
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
                attrHash.put("PARTICIPANT", attributes.getValue("PARTICIPANT"));
            }

            attrHash.put("LINGUISTIC_TYPE_REF",
                attributes.getValue("LINGUISTIC_TYPE_REF"));
            attrHash.put("DEFAULT_LOCALE", attributes.getValue("DEFAULT_LOCALE"));

            if (attributes.getValue("PARENT_REF") != null) {
                attrHash.put("PARENT_REF", attributes.getValue("PARENT_REF"));
            }
        } else if (name.equals("ALIGNABLE_ANNOTATION")) {
            currentAnnotationId = attributes.getValue("ANNOTATION_ID");

            // create new "AnnotationRecord" and add to annotations Hashtable for current tier
            ((Hashtable) tiers.get(currentTierId)).put(currentAnnotationId,
                new Vector());

            // mark type of annotation, add start and end times to this AnnotationRecord
            ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(
                "alignable");
            ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                    "TIME_SLOT_REF1"));
            ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(attributes.getValue(
                    "TIME_SLOT_REF2"));
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
            linguisticTypes.add(new LinguisticType(attributes.getValue(
                        "LINGUISTIC_TYPE_ID")));
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
     * HandlerBase method
     *
     * @param name DOCUMENT ME!
     */
    public void endElement(String name) {
        if (name.equals("ANNOTATION_VALUE")) {
            ((Vector) ((Hashtable) tiers.get(currentTierId)).get(currentAnnotationId)).add(content);
        }
    }

    /**
     * HandlerBase method
     *
     * @param buf DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param length DOCUMENT ME!
     */
    public void characters(char[] buf, int start, int length) {
        if (content == null) {
            content = removeWhiteSpace(buf, start, length);
        } else {
            content += removeWhiteSpace(buf, start, length);
        }
    }

    /**
     * HandlerBase method
     *
     * @param publicId DOCUMENT ME!
     * @param systemId DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public InputSource resolveEntity(String publicId, String systemId) {
        InputSource inputSource = null;

        /*        try {
           // Open an InputSource to a DOBES-DAF DTD
           // The location of the dtd defs is under the corpus directory in the path dobes/dtd.
           if (systemId.endsWith(".dtd")) {
               int to = systemId.indexOf(".dtd") + 4;
               int from = systemId.lastIndexOf('/', to) + 1;
               String fileName = ServerConfiguration.CORPUS_DIRECTORY + File.separator + "dobes" +
                           File.separator + "dtd" + File.separator + systemId.substring(from, to);
           //    inputSource = new InputSource(new FileInputStream(fileName));
           //    inputSource = new InputSource(StringUtil.openEncodedFile("UTF-8", fileName));
               inputSource = new InputSource(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
           }
           }
           catch (Exception e) {
               e.printStackTrace();
           }
         */
        return inputSource;
    }

    /**
     * HandlerBase method
     *
     * @param e DOCUMENT ME!
     */
    public void error(SAXParseException e) {
        printErrorLocationInfo("Parse error " + e.getMessage());
        parseError = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void fatalError(SAXParseException e) {
        printErrorLocationInfo("Fatal Parse Error " + e.getMessage());
        parseError = true;
    }

    private String removeWhiteSpace(char[] buf, int start, int length) {
        int from = start;
        int to = start + length;

        /*
           for (int i = start; i  < start + length; i++) {
               if (buf[i] == ' ' || buf[i] == '\t') {
                   from++;
               }
               else {
                   to = from;
                   for (int j = from; j < start + length; j++) {
                       if  (buf[j] != ' ' && buf[i] != '\t') {
                           to++;
                       }
                       else {
                           break;
                       }
                   }
                   break;
               }
           }
         */
        return new String(buf, from, to - from);
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
}
