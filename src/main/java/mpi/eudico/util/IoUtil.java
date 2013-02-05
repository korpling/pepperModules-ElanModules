/*
 * Created on Mar 30, 2004
 * $Id: IoUtil.java 10098 2007-09-12 14:43:03Z klasal $
 */
package mpi.eudico.util;

import org.apache.xml.serialize.ElementState;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;


/**
 * Convenience methods for IO operations.
 *
 * @author Wouter Huijnink
 */
public final class IoUtil {
    /**
     * Open a file using the given encoding.
     *
     * @param encoding example: UTF-8
     * @param filename the name of the file
     *
     * @return handle to the file
     *
     * @throws Exception DOCUMENT ME!
     */
    public final static BufferedReader openEncodedFile(String encoding,
        String filename) throws Exception {
        /*
           A file is opened from the operating system.
           This file is a stream of bytes.
           This could be a UTF-8 encoded unicode stream.
           The decision, if to read UTF-8 is done by the reader:
         */
        File file = new File(filename);

        // Convert the File into an input stream
        InputStream fis = new FileInputStream(file);

        // interpret the input stream as an UTF-8 stream
        // convert it to a reader
        Reader filereader = new InputStreamReader(fis, encoding);

        // explicit performance care: buffering the filereader
        return new BufferedReader(filereader);
    }

    /**
     * Write the content to the new file using the given encoding.
     *
     * @param encoding example: UTF-8
     * @param filename the name of the file
     * @param content the content of the file to be written
     *
     * @throws Exception DOCUMENT ME!
     */
    public final static void writeEncodedFile(String encoding, String filename,
        Element content) throws Exception {
        
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
											filename), encoding);
        OutputFormat format = new OutputFormat(content.getOwnerDocument(), encoding, true);
		format.setLineWidth(0);

        XMLSerializer ser = new XMLSerializer(out, format);
        ser.asDOMSerializer();
        ser.serialize(content);
        out.close();
        /*
		FileOutputStream outputstream = new FileOutputStream(filename);
		OutputFormat format = new OutputFormat(Method.XML, encoding, true);
		format.setPreserveSpace(true);

		XMLSerializer ser = new XMLSerializer(outputstream, format);
		ser.startNonEscaping();
		ser.startDocument();
		ser.serialize(content);
		ser.endDocument();
		outputstream.close();
		*/
        
    } 
    
	/**
	 * Write the content to the new file using the given encoding.
	 * Specialised version for eaf. Tabs and newline characters should be 
	 * maintained and the result's indentation should still be pretty.
	 * There doesn't seem to be a method to setPreserveSpace to true only for
	 * certain Elements or Nodes.?
	 *
	 * @param encoding example: UTF-8
	 * @param filename the name of the file
	 * @param content the content of the file to be written
	 *
	 * @throws Exception any io or SAX exception that can occur
	 */
	public final static void writeEncodedEAFFile(String encoding, String filename,
		   Element content) throws Exception {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
												filename), encoding);
		OutputFormat format = new OutputFormat(content.getOwnerDocument(), encoding, true);
		format.setLineWidth(0);
		//format.setNonEscapingElements(new String[]{"ANNOTATION_VALUE"}); //??

		// override a single XMLSerializer method...
		XMLSerializer ser = new XMLSerializer(out, format) {
			/**
			 * Sets preserveSpace to true for "ANNOTATION_VALUE" elements, CVEntry too??
			 */
			protected ElementState enterElementState( String namespaceURI, String localName,
					String rawName, boolean preserveSpace ) {

				if (rawName.equals("ANNOTATION_VALUE")) {
					return super.enterElementState(namespaceURI, localName, rawName, true);								  	
				}
				return super.enterElementState(namespaceURI, localName, rawName, preserveSpace);
			}
		};
		ser.asDOMSerializer();
		ser.serialize(content);
		out.close();	
    		   	
	}
   
}
