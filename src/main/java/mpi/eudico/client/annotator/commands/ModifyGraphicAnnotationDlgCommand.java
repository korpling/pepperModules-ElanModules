package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.svg.SVGEditor;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Brings up an edit box for the selected graphical annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyGraphicAnnotationDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ModifyGraphicAnnotationDlgCommand instance
     *
     * @param name the name of this command
     */
    public ModifyGraphicAnnotationDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the active Annotation
     * @param arguments the arguments:  <ul><li>arguments[0] = the
     *        transcription (Transcription)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        Annotation activeAnn = (Annotation) receiver;

        if (activeAnn != null && activeAnn instanceof SVGAlignableAnnotation) {
        	// double check...
        	TierImpl tier = (TierImpl)activeAnn.getTier();
        	if(tier.getLinguisticType() != null && 
        			tier.getLinguisticType().hasGraphicReferences()) {
        	
	            new SVGEditor((Transcription) arguments[0],
	                (SVGAlignableAnnotation) activeAnn);
        	}
        }
    }

    /**
     * Returns the name of this command.
     *
     * @return the name of this command
     */
    public String getName() {
        return commandName;
    }
}
