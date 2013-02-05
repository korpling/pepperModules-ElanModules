package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF26Parser.EAFContentHandler;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ExternalCV;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


/**
 * Parses an eaf file, creating objects of ControlledVocabularies,
 * LinguisticTypes and Tiers only. The rest is skipped.
 *
 * @author Han Sloetjes
 * @version 1.0 jan 2006: reflects EAFv2.2.xsd
 * @version 2.0 jan 2007: reflects EAFv2.4.xsd, attribute "ANNOTATOR" added to 
 * element "TIER"
 * @version 3.0 may 2008: reflects EAFv2.6.xsd, external references (DCR) added 
 * to Linguistic Type and CV entry
 */
public class EAFSkeletonParser {
    /** the sax parser */
    //private final SAXParser saxParser;
    private XMLReader reader;

    /** the currently supported eaf version */
    private final String version = "2.7";
    private String fileName;

    /** stores tiername - tierrecord pairs */
    private final HashMap tierMap = new HashMap();
    private ArrayList tiers;
    private ArrayList tierOrder = new ArrayList();

    /** stores linguistic types records! */
    private final ArrayList lingTypeRecords = new ArrayList();
    private ArrayList linguisticTypes;

    /** stores the Locales */
    private final ArrayList locales = new ArrayList();

    /** stores the ControlledVocabulary objects */
    private final ArrayList cvList = new ArrayList();
    /** stores external references */
	private final HashMap extReferences = new HashMap();
	private final HashMap cvEntryExtRef = new HashMap();
    private String currentTierId;
    private String currentCVId;
    private ControlledVocabulary currentCV;
    private String currentEntryDesc;
    private String currentEntryExtRef;
    private String content = "";

    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public EAFSkeletonParser(String fileName) throws ParseException {
    	this(fileName, false);
    	
    	/*
    	if (fileName == null) {
            throw new NullPointerException();
        }

        this.fileName = fileName;
        saxParser = new SAXParser();

        try {
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            saxParser.setFeature("http://apache.org/xml/features/validation/dynamic",
                true);
            saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                "http://www.mpi.nl/tools/elan/EAFv2.6.xsd");
            saxParser.setContentHandler(new EAFSkeletonHandler());
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        }
    	*/
    }

    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public EAFSkeletonParser(String fileName, boolean strict) throws ParseException {
        if (fileName == null) {
            throw new NullPointerException();
        }

        this.fileName = fileName;

    	try {
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", true);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	        reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
	        		this.getClass().getResource(ACMTranscriptionStore.getCurrentEAFSchemaLocal()).openStream());
	        //reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
	        //		"http://www.mpi.nl/tools/elan/EAFv2.6.xsd");
	        reader.setContentHandler(new EAFSkeletonHandler());
	        if (strict) {
	        	reader.setErrorHandler(new EAFErrorHandler());
	        }
    	} catch (SAXException se) {
    		se.printStackTrace();
    		throw new ParseException(se.getMessage());
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    		throw new ParseException(ioe.getMessage());
    	}
    }
    /*
       public ArrayList getMediaDescriptors() {
           return null;
       }
     */

    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypes(java.lang.String)
     */
    public ArrayList getLinguisticTypes() {
        return linguisticTypes;
    }

    /**
     * Returns a list of tier objects.
     *
     * @return a list of tiers
     */
    public ArrayList getTiers() {
        return tiers;
    }

    /**
     * Returns a list of the tiernames in the same order as in the file.
     *  
     * @return a list of the tiernames in the same order as in the .eaf file
     */
    public ArrayList getTierOrder() {
    	return tierOrder;
    }
    
    /**
     * Returns a list of CVs.
     *
     * @return a list of Controlled Vocabularies
     */
    public ArrayList getControlledVocabularies() {
        return cvList;
    }

    /**
     * Returns the current version of the skeleton parser.
     *
     * @return the current version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Starts the actual parsing.
     *
     * @throws ParseException any parse exception
     */
    public void parse() throws ParseException {
        // init maps and lists
        try {
            //saxParser.parse(fileName);
        	reader.parse(fileName);
            createObjects();
        } catch (SAXException sax) {
            System.out.println("Parsing error: " + sax.getMessage());

            // the SAX parser can have difficulties with certain characters in 
            // the filepath: try to create an InputSource for the parser
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            File f = new File(fileName);

            if (f.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    InputSource source = new InputSource(fis);
                    //saxParser.parse(source);
                    reader.parse(source);
                    createObjects();

                    // just catch any exception
                } catch (Exception ee) {
                    System.out.println("Parsing retry error: " +
                        ee.getMessage());
                    throw new ParseException(ee.getMessage(), ee.getCause());
                }
            }
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());

            // the SAX parser can have difficulties with certain characters in 
            // the filepath: try to create an InputSource for the parser
            // HS Mar 2007: depending on Xerces version a SAXException or an IOException 
            // is thrown in such case
            File f = new File(fileName);

            if (f.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    InputSource source = new InputSource(fis);
                    //saxParser.parse(source);
                    reader.parse(source);
                    createObjects();

                    // just catch any exception
                } catch (Exception ee) {
                    System.out.println("Parsing retry error: " +
                        ee.getMessage());
                    throw new ParseException(ee.getMessage(), ee.getCause());
                }
            }
        } catch (Exception e) {
        	throw new ParseException(e.getMessage(), e.getCause());
        }
    }

    /**
     * After parsing create objects from the records; tiers and linguistic
     * types. CV's + CVEntries and Locales have already been made.
     */
    private void createObjects() {
        linguisticTypes = new ArrayList(lingTypeRecords.size());

        for (int i = 0; i < lingTypeRecords.size(); i++) {
            LingTypeRecord ltr = (LingTypeRecord) lingTypeRecords.get(i);

            LinguisticType lt = new LinguisticType(ltr.getLingTypeId());

            boolean timeAlignable = true;

            if (ltr.getTimeAlignable().equals("false")) {
                timeAlignable = false;
            }

            lt.setTimeAlignable(timeAlignable);

            boolean graphicReferences = false;

            if (ltr.getGraphicReferences().equals("true")) {
                graphicReferences = true;
            }

            lt.setGraphicReferences(graphicReferences);

            String stereotype = ltr.getStereoType();
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
                } else if (stereotype.equals(
                            Constraint.stereoTypes[Constraint.INCLUDED_IN])) {
                    c = new IncludedIn();
                }
            }

            if (c != null) {
                lt.addConstraint(c);
            }

            lt.setControlledVocabularyName(ltr.getControlledVocabulary());

			// check ext ref (dcr), in Linguistic Type this is a string
			if (ltr.getExtRefId() != null) {
				ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(ltr.getExtRefId());
				if (eri != null) {
					lt.setDataCategory(eri.getValue());
				}
			}
			
            linguisticTypes.add(lt);
        }

        tiers = new ArrayList(tierMap.size());

        HashMap parentHash = new HashMap();

        Iterator tierIt = tierMap.values().iterator();
        TierRecord rec;
        TierImpl tier;
        LinguisticType type;
        Locale loc;

        while (tierIt.hasNext()) {
            tier = null;
            type = null;

            rec = (TierRecord) tierIt.next();
            tier = new TierImpl(null, rec.getName(), rec.getParticipant(),
                    null, null);

            Iterator typeIter = linguisticTypes.iterator();

            while (typeIter.hasNext()) {
                LinguisticType lt = (LinguisticType) typeIter.next();

                if (lt.getLinguisticTypeName().equals(rec.getLinguisticType())) {
                    type = lt;

                    break;
                }
            }

            if (type == null) {
                // don't add the tier, something's wrong
                continue;
            }

            tier.setLinguisticType(type);

            if (rec.getDefaultLocale() == null) {
                // default, en
                tier.setDefaultLocale(new Locale("en", "", ""));
            } else {
                Iterator locIt = locales.iterator();

                while (locIt.hasNext()) {
                    loc = (Locale) locIt.next();

                    if (loc.getLanguage().equals(rec.getDefaultLocale())) {
                        tier.setDefaultLocale(loc);

                        break;
                    }
                }
            }

            if (rec.getParentTier() != null) {
                parentHash.put(tier, rec.getParentTier());
            }
            
            if (rec.getAnnotator() != null) {
            	tier.setAnnotator(rec.getAnnotator());
            }

            tiers.add(tier);
        }

        // all Tiers are created. Now set all parent tiers
        Iterator parentIter = parentHash.keySet().iterator();

        while (parentIter.hasNext()) {
            TierImpl t = (TierImpl) parentIter.next();
            String parent = (String) parentHash.get(t);

            Iterator secIt = tiers.iterator();

            while (secIt.hasNext()) {
                TierImpl pt = (TierImpl) secIt.next();

                if (pt.getName().equals(parent)) {
                    t.setParentTier(pt);

                    break;
                }
            }
        }
        
        // post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	CVEntry entry;
        	String erId;
        	Iterator entIter = cvEntryExtRef.keySet().iterator();
        	while (entIter.hasNext()) {
        		entry = (CVEntry) entIter.next();
        		erId = (String) cvEntryExtRef.get(entry);
        		
        		ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(erId);
        		if (eri != null) {
        			try {
        				entry.setExternalRef(eri.clone());
        			} catch (CloneNotSupportedException cnse) {
        				System.out.println("Could not set the external reference: " + cnse.getMessage());
        			}
        		}
        	}
        	
        }
    }


    /**
     * An error handler for the eaf parser.<br>
     * The exception thrown (by Xerces 2.6.2) contains apart from file name,
     * line and column number, only a description of the problem in it's message.
     * To really deal with a problem a handler would need to parse the message
     * for certain strings (defined in a Xerces resource .properties file) and/or
     * read the file to the specified problem line.
     *
     * @author Han Sloetjes, MPI
     */
    class EAFErrorHandler implements ErrorHandler {

		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage());
			// system id is the file path
			System.out.println("System id: " + exception.getSystemId());
			System.out.println("Public id: " + exception.getPublicId());
			System.out.println("Line: " + exception.getLineNumber());
			System.out.println("Column: " + exception.getColumnNumber());
			throw exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());
			throw exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());
		}

    }

    //#######################
    // Content handler
    //#######################
    class EAFSkeletonHandler implements ContentHandler {
        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endDocument()
         */
        public void endDocument() throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startDocument()
         */
        public void startDocument() throws SAXException {
        }

        /**
         * ContentHandler method
         *
         * @param ch the characters
         * @param start start index
         * @param end end index
         *
         * @throws SAXException sax exception
         */
        public void characters(char[] ch, int start, int end)
            throws SAXException {
            content += new String(ch, start, end);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
         */
        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
         */
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
         */
        public void skippedEntity(String name) throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        public void setDocumentLocator(Locator locator) {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
         */
        public void processingInstruction(String target, String data)
            throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
         */
        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
            content = "";
            if (name.equals("TIER")) {
                currentTierId = attributes.getValue("TIER_ID");

                // First check whether this tier already exists, prevent duplicates
                if (!tierMap.containsKey(currentTierId)) {
                    // create a record
                    TierRecord tr = new TierRecord();
                    tr.setName(currentTierId);
                    tierMap.put(currentTierId, tr);
                    tierOrder.add(currentTierId);

                    tr.setParticipant(attributes.getValue("PARTICIPANT"));
                    tr.setAnnotator(attributes.getValue("ANNOTATOR"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));
                }
            } else if (name.equals("LINGUISTIC_TYPE")) {
                LingTypeRecord ltr = new LingTypeRecord();

                ltr.setLingTypeId(attributes.getValue("LINGUISTIC_TYPE_ID"));

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
                
        		if(stereotype != null && stereotype.startsWith("Symbolic")) {
        			ltr.setTimeAlignable("false");
        		}
        		
                ltr.setControlledVocabulary(attributes.getValue(
                        "CONTROLLED_VOCABULARY_REF"));

                ltr.setExtRefId(attributes.getValue("EXT_REF"));
                ltr.setLexiconReference(attributes.getValue("LEXICON_REF"));
                
                lingTypeRecords.add(ltr);
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
    			currentCV = new ControlledVocabulary(currentCVId);

    			String desc = attributes.getValue("DESCRIPTION");

    			if (desc != null) {
    				currentCV.setDescription(desc);
    			}

    			// by Micha: if a CV has an external reference
                // it is an external CV
                String extRefId = attributes.getValue("EXT_REF");
    			if (extRefId != null) {
    				currentCV = new ExternalCV(currentCV);
    				ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(
    						(extRefId));
    				if (eri != null) {
    					try {
    						((ExternalCV) currentCV).setExternalRef(eri.clone());
    					} catch (CloneNotSupportedException cnse) {
    						//LOG.severe("Could not set the external reference: " + cnse.getMessage());
    					}
    				}
    			}
    			
    			cvList.add(currentCV);
    		} else if (name.equals("CV_ENTRY")) {
                currentEntryDesc = attributes.getValue("DESCRIPTION");
                currentEntryExtRef = attributes.getValue("EXT_REF");
            } else if (name.equals("EXTERNAL_REF")) {
            	String value = attributes.getValue("VALUE");
            	String type = attributes.getValue("TYPE");
            	String dcId = attributes.getValue("EXT_REF_ID");
        		if (value != null && value.length() > 0) {
        			ExternalReferenceImpl eri = new ExternalReferenceImpl(value, ExternalReference.UNDEFINED);
        			if (type != null) {
        				if (type.equals("iso12620")) {
        					eri.setReferenceType(ExternalReference.ISO12620_DC_ID);
        				} else if (type.equals("resource_url")) {
        					eri.setReferenceType(ExternalReference.RESOURCE_URL);
        				} else if (type.equals("ecv")) {
        					eri.setReferenceType(ExternalReference.EXTERNAL_CV);
        				} else if (type.equals("cve_id")) {
        					eri.setReferenceType(ExternalReference.CVE_ID);
        				} else if (type.equals("lexen_id")) {
        					eri.setReferenceType(ExternalReference.LEXEN_ID);
        				}
        			}
        			extReferences.put(dcId, eri);
        		}
            	
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String nameSpaceURI, String name, String rawName)
            throws SAXException {
            if (name.equals("CV_ENTRY")) {
            	CVEntry entry = new CVEntry(content, currentEntryDesc);
                currentCV.addEntry(entry);
                if (currentEntryExtRef != null) {
                	cvEntryExtRef.put(entry, currentEntryExtRef);
                }
            }
        }
    }
}
