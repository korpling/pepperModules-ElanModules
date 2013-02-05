package mpi.eudico.client.annotator.md.imdi;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * Performs a quick, trivial test on a file. It checks the "type" attribute of
 * the "METATRANSCRIPT" element to be "SESSION" and checks that the next
 * element is a "Session" element. Then returns.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiCheckHandler implements ContentHandler {
    private int numElements = 0;
    private boolean isSessionType = false;
    private boolean isSessionElem = false;

    /**
     * Creates a new ImdiCheckHandler instance
     */
    public ImdiCheckHandler() {
        super();
    }

    /**
     * Returns whether the file is an imdi session file.
     *
     * @return true if it is an IMDI Session file
     */
    public boolean isSessionFile() {
        return (isSessionType && isSessionElem);
    }

    /**
     * Checks 2 elements, then throws an exception
     *
     * @param uri
     * @param localName
     * @param qName
     * @param atts
     *
     * @throws SAXException thrown to stop parsing beyond the first 2 elements
     */
    public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        numElements++;

        if (localName.equals("METATRANSCRIPT")) {
            String type = atts.getValue("Type");

            if ((type != null) && type.equals("SESSION")) {
                isSessionType = true;
            }
        } else if (localName.equals("Session")) {
            //if (numElements < 3) {
                isSessionElem = true;
            //}

            throw new SAXException("Parsed " + numElements + " elements...");
        }
    }

    /**
     * stub
     *
     * @param ch
     * @param start
     * @param length
     *
     * @throws SAXException
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * stub
     *
     * @throws SAXException
     */
    public void endDocument() throws SAXException {
    }

    /**
     * stub
     *
     * @param uri
     * @param localName
     * @param qName
     *
     * @throws SAXException
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
    }

    /**
     * stub
     *
     * @param prefix
     *
     * @throws SAXException
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * stub
     *
     * @param ch
     * @param start
     * @param length
     *
     * @throws SAXException
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * stub
     *
     * @param target
     * @param data
     *
     * @throws SAXException
     */
    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    /**
     * stub
     *
     * @param locator
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * stub
     *
     * @param name
     *
     * @throws SAXException
     */
    public void skippedEntity(String name) throws SAXException {
    }

    /**
     * stub
     *
     * @throws SAXException
     */
    public void startDocument() throws SAXException {
    }

    /**
     * stub
     *
     * @param prefix
     * @param uri
     *
     * @throws SAXException
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }
}
