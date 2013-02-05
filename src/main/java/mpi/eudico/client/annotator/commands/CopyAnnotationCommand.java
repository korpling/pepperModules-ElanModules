package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.TransferableAnnotation;
import mpi.eudico.server.corpora.clom.Annotation;

/**
 * A Command to copy an annotation (i.e. a transferable AnnotationDataRecord) to the System's 
 * Clipboard.
 */
public class CopyAnnotationCommand implements Command, ClientLogger {
    private String commandName;
    
    /**
     * Creates a new CopyAnnotationCommand instance
     * 
     * @param name the name of the command
     */
    public CopyAnnotationCommand(String name) {
        commandName = name;
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the (active) annotation
     *        (Annotation)</li></ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Annotation) {
            AnnotationDataRecord record = new AnnotationDataRecord((Annotation) arguments[0]);
            TransferableAnnotation ta = new TransferableAnnotation(record);
            
            if (canAccessSystemClipboard()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ta, ta);
            }
        }

    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }
    
    /**
     * Performs a check on the accessibility of the system clipboard.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    protected boolean canAccessSystemClipboard() {

        if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
                LOG.warning("Cannot copy, cannot access the clipboard.");

                return false;
            }
        }

        return true;
    }

}
