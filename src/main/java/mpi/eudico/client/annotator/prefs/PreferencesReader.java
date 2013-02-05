package mpi.eudico.client.annotator.prefs;

import mpi.eudico.client.annotator.util.ClientLogger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;


/**
 * An xml reader for ELAN preferences file.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 1.1 Dec 2009 the schema version is incremented to 1.1, prefGroups can now be nested 
 * and a prefList is allowed as child of prefGroup
 */
public class PreferencesReader implements ContentHandler,
    PrefConstants {
    private final int BL_TYPE = 0;
    private final int INT_TYPE = 1;
    private final int LONG_TYPE = 2;
    private final int FLOAT_TYPE = 3;
    private final int DOUBLE_TYPE = 4;
    private final int STR_TYPE = 5;
    private final int OBJ_TYPE = 6;
    private XMLReader reader;
    private ArrayList<Pref> preferences;

    // parse time temp objects
    private Stack<Map> mapStack;// replace by Dequeue when J1.6 is the oldest supported version
    private HashMap curMap;
    private Pref curMapPref;
    private ArrayList curList;
    private Pref curListPref;
    private Pref curPref;
    private ObjectPref curObj;
    private String curKey;
    private String content;
    private int curType = -1;
    private PrefObjectConverter converter;

    /**
     * Creates a new PreferencesReader instance
     */
    public PreferencesReader() {
    	converter = new PrefObjectConverter();
        try {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");

            try {
                reader.setFeature("http://xml.org/sax/features/namespaces", true);
                reader.setFeature("http://xml.org/sax/features/validation", true);
                reader.setFeature("http://apache.org/xml/features/validation/schema",
                    true);
                reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                    true);
            } catch (Exception e) {
                // catch any SAX exception
            	ClientLogger.LOG.warning("Could not set feature: " + e.getMessage());
            }

            try {
                reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
                    this.getClass()
                        .getResource("/mpi/eudico/resources/Prefs_v1.1.xsd")
                        .openStream());

                //reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                //		"http://www.mpi.nl/tools/elan/Prefs_v1.1.xsd");
            } catch (Exception ee) {
            	ClientLogger.LOG.warning("Could not set property: " + ee.getMessage());
            }

            reader.setContentHandler(this);
            //reader.setErrorHandler(new PrefErrorHandler());
        } catch (SAXException se) {
        	ClientLogger.LOG.severe("Could not create xml reader: " + se.getMessage());
            se.printStackTrace();
        }

        curMap = null;
        curList = null;
        content = "";
        mapStack = new Stack<Map>();
    }

    /**
     * Parses the preferences file and returns a HashMap (for historic reasons)
     * with preference objects.
     *
     * @param fileName the path to the preferences file
     *
     * @return a HashMap containing preferences objects
     */
    public synchronized Map parse(String fileName) {
        preferences = new ArrayList<Pref>();

        if ((reader == null) || (fileName == null)) {
        	ClientLogger.LOG.warning("Cannot parse preferences file: " + fileName);

            return new HashMap(0);
        }

        // reset objects
        if (curMap != null) {
        	curMap.clear();
        }
        if (curList != null) {
        	curList.clear();
        }     
        curPref = null;
        curObj = null;
        content = "";
        mapStack.clear();

        File pf = new File(fileName);

        if (pf.exists()) {
            try {
                FileInputStream fis = new FileInputStream(pf);
                InputSource source = new InputSource(fis);
                reader.parse(source);
                ClientLogger.LOG.info("Reading preferences: " + fileName);
            } catch (Exception e) {
            	ClientLogger.LOG.warning("Exception while parsing preferences file: " +
                    pf.getAbsolutePath());
            	ClientLogger.LOG.warning(e.getMessage());
            }
        } else {
        	ClientLogger.LOG.warning("Preferences file does not exist: " + fileName);

            return new HashMap(0);
        }
        // after reading the file create a map with the proper objects 
        HashMap prefMap = new HashMap(preferences.size());
        Pref iterPref;
        //Object val;
        for (int i = 0; i < preferences.size(); i++) {
        	iterPref = (Pref) preferences.get(i);
        	if (iterPref.getKey().equals("LastUsedShoeboxMarkers")) {
        		// skip, obsolete per ELAN 3.2
        		continue;
        	}

        	prefMap.put(iterPref.getKey(), iterPref.getValue());
        }
        
        return prefMap;
    }

    // ################## Content Handler methods ##################################################
    /**
     * @see ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
    	content = "";
        if (localName.equals(PREF)) {
            if (curMap == null) {
                curPref = new Pref(atts.getValue(KEY_ATTR), null);
                preferences.add(curPref);
            } else {
                curKey = atts.getValue(KEY_ATTR);

                // wait until end element to add to the map
            }
        } else if (localName.equals(PREF_GROUP)) {
        	curMap = new HashMap();           
            if (mapStack.empty()) {
            	// add at top level
            	curMapPref = new Pref(atts.getValue(KEY_ATTR), curMap);
            	preferences.add(curMapPref);
            } else {
            	// add to the first, enclosing map/group (== previous curMap)
            	mapStack.peek().put(atts.getValue(KEY_ATTR), curMap);
            }
            mapStack.push(curMap);
        } else if (localName.equals(PREF_LIST)) {
            curList = new ArrayList();
            if (mapStack.empty()) {
            	// add at top level
            	curListPref = new Pref(atts.getValue(KEY_ATTR), curList);
            	preferences.add(curListPref);
            } else {
            	// add to the first, enclosing map/group (== curMap)
            	mapStack.peek().put(atts.getValue(KEY_ATTR), curList);
            }
        } else if (localName.equals(BOOLEAN)) {
            curType = BL_TYPE;
        } else if (localName.equals(INT)) {
            curType = INT_TYPE;
        } else if (localName.equals(LONG)) {
            curType = LONG_TYPE;
        } else if (localName.equals(FLOAT)) {
            curType = FLOAT_TYPE;
        } else if (localName.equals(DOUBLE)) {
            curType = DOUBLE_TYPE;
        } else if (localName.equals(STRING)) {
            curType = STR_TYPE;
        } else if (localName.equals(OBJECT)) {
            curType = OBJ_TYPE;
            curObj = new ObjectPref(atts.getValue(CLASS_ATTR), null);
        }
    }

    /**
     * @see ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        if (localName.equals(PREF)) {
        	if (curObj != null) {
        		if (curObj.getObject() != null) {
        			if (curMap != null) {
        				curMap.put(curKey, curObj.getObject());
        			} else {
        				curPref.setValue(curObj.getObject());
        			}
        		} else {
        			if (curMap == null) {
        				preferences.remove(curPref);
        			}
        		}
        		curObj = null;
        	} else {
        		Object value = getCurrentValue();
        		
                if (curMap != null) {
                    // the current pref value is added as value to the map
                    if (value != null) {
                        curMap.put(curKey, value);
                    }
                } else {
                    // a toplevel pref element
                    if (value != null) {
                        curPref.setValue(value);
                    } else {
                        preferences.remove(curPref);
                    }
                }
        	}
        	
            curPref = null;

            if (content.length() > 0) {
                content = "";
            }
        } else if (localName.equals(PREF_GROUP)) {
        	if (!mapStack.empty()) {
        		mapStack.pop();        		
        		if (mapStack.empty()) {
        			// if the last one is popped set curMap to null
        			curMap = null;
        			curMapPref = null;
        		}  else{
        			curMap = (HashMap) mapStack.peek();
        		}
        	} else {// should not happen
        		curMap = null;
        		curMapPref = null;
        	}           
            
        } else if (localName.equals(PREF_LIST)) {
            curList = null;
            curListPref = null;
        } else if (localName.equals(OBJECT)) {
            Object value = getCurrentValue();

            if (value instanceof String) {
                curObj.setValue((String) value);

                if (curList != null) {
                    curList.add(curObj);
                    curObj = null;
                }
            } else {
            	if (curList != null) {
            		curList.add(value);
            		curObj = null;
            	} else {
            		curObj.setObject(value);
            	}        	
            }

            content = "";
        } else if (curList != null) {
            // add current element to the list
            Object value = getCurrentValue();

            if (value != null) {
                curList.add(value);
            }

            content = "";
        }

        // in all other cases the contents will be stored when the end of 
        // the enclosing pref element is reached		
    }

    /**
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        content += new String(ch, start, length);
    }

    /**
     * Converts the current value (== contents) to an object corresponding to
     * the current  type element type.
     *
     * @return a parsed object
     */
    private Object getCurrentValue() {
        if ((content == null) || (content.length() == 0)) {
            return null;
        }
        content = content.trim();
        Object vl = null;

        switch (curType) {
        case BL_TYPE:
            vl = new Boolean(content);

            break;

        case INT_TYPE:

            try {
                vl = new Integer(content);
            } catch (NumberFormatException nfe) {
                // let the value be null or provide a default??
            }

            break;

        case LONG_TYPE:

            try {
                vl = new Long(content);
            } catch (NumberFormatException nfe) {
            }

            break;

        case FLOAT_TYPE:

            try {
                vl = new Float(content);
            } catch (NumberFormatException nfe) {
            }

            break;

        case DOUBLE_TYPE:

            try {
                vl = new Double(content);
            } catch (NumberFormatException nfe) {
            }

            break;

        case OBJ_TYPE: 
        	
        	if (curObj != null) {
        		vl = converter.stringToObject(curObj.getClassName(), content);
        	} else {
        		vl = content;
        	}
        	
        	break;
        default:
            // String type and Object type, return the value as string
            vl = content;
        }

        return vl;
    }

    /**
     * @see ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /**
     * @see ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * @see ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * @see ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    /**
     * @see ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * @see ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
    }

    /**
     * @see ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /**
     * @see ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }
    
    private class PrefErrorHandler implements ErrorHandler {

		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage() + "\n" +
					exception.getLineNumber() + " " + exception.getColumnNumber());
			System.out.println("systemID: " + exception.getSystemId());
			System.out.println("publicID: " + exception.getPublicId()); 
			exception.printStackTrace();
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void warning(SAXParseException exception) throws SAXException {
			// TODO Auto-generated method stub
			
		}
    	
    }
}
