package mpi.eudico.client.annotator.md.imdi;

import mpi.eudico.client.annotator.util.ClientLogger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Performs a quick trivial test on a metadata file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiSaxCheck {
    /**
     * Creates a new ImdiSaxCheck instance
     */
    public ImdiSaxCheck() {
        super();
    }

    /**
     * Starts parsing the file, only 2 elements
     *
     * @param file the metadata file
     *
     * @return true if it is a "METATRANSCRIPT" file of type "SESSION" and the
     *         first element is "Session"
     */
    public boolean isSessionFile(File file) {
        if ((file == null) || !file.exists()) {
            return false;
        }

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/validation", false);

            ImdiCheckHandler handler = new ImdiCheckHandler();
            reader.setContentHandler(handler);

            FileInputStream fis = new FileInputStream(file);
            InputSource source = new InputSource(fis);

            try {
                reader.parse(source);
            } catch (SAXException sax) {
            	ClientLogger.LOG.info("Is IMDI Session file: " + handler.isSessionFile());
                return handler.isSessionFile();
            } catch (IOException ioe) {
                ClientLogger.LOG.warning("Cannot read file: " +
                    ioe.getMessage());
            }
        } catch (SAXException sex) {
            ClientLogger.LOG.warning("Cannot parse file: " + sex.getMessage());
        } catch (FileNotFoundException fnfe) {
            ClientLogger.LOG.warning("Cannot find file: " + fnfe.getMessage());
        }

        return false;
    }
}
