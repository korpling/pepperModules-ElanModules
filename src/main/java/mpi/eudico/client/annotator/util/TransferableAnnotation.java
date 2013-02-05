package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Constants;

/**
 * A transerable annotation, using an AnnotationdataRecord as the transferable object.
 * 
 * Note June 2006 transferal of application/x-java-serialized-object objects between jvm's 
 * doesn't seem to work on Mac OS X. The Transferable sun.awt.datatransfer.ClipboardTransferable@8b4cb8 
 * doesn't support any flavor with human presentable name "application/x-java-serialized-object" 
 */
public class TransferableAnnotation implements Transferable, ClipboardOwner {
    private AnnotationDataRecord record;
    private final static DataFlavor[] flavors;
    private static final int STRING = 0;
    private static final int ANNOTATION = 1; 

    private String copyOption = Constants.TEXTANDTIME_STRING;
    
    static {
        DataFlavor flav = AnnotationDataFlavor.getInstance();
        
        if (flav == null) {
            flavors = new DataFlavor[]{DataFlavor.stringFlavor};
        } else {
            flavors = new DataFlavor[]{DataFlavor.stringFlavor, flav};
        }
               
    }
        
    /**
     * Creates a new TransferableAnnotation
     * @param record the transferable object
     */
    public TransferableAnnotation(AnnotationDataRecord record) {
        if (record == null) {
            throw new NullPointerException("AnnotationDataRecord is null.");
        }
        this.record = record;
        
        Object obj = Preferences.get("EditingPanel.CopyOption", null);        
        // string should be a non-locale string, a string from Constants        
		if (obj instanceof String) {
		    copyOption = (String) obj;
		}
    }
    
    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor == null) {
            return false; // could throw NullPointer Exc.
        }
        
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        if (flavors.length > 1 && flavor.equals(flavors[ANNOTATION])) {
            return record; // clone?? once on the clipboard the record don't seem to change anymore
        } else if (flavor.equals(flavors[STRING])) {
            return recordParamString();
        }
        
        return null;
    }
    
    /**
     * Returns a string representation of the annotation data record.
     * 
     * @return a string representation of the annotation data record
     */
    private String recordParamString() {
        if (record != null) {   
            if (copyOption.equals(Constants.TEXT_STRING)) {
        	// copy annotation only
            	return record.getValue();
            } else {
            	return record.getValue() + ",T=" + record.getTierName() + ",B=" + record.getBeginTime() +
            		",E=" + record.getEndTime();
            }
        } else {
            return "null";
        }
    }

    /**
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        record = null;
    }

}
