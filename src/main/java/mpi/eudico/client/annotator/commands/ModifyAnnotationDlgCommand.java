package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.InlineEditBox;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * Brings up an edit box for the selected annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ModifyAnnotationDlgCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ModifyAnnotationDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the active Annotation
     * @param arguments the arguments: null
     */
    public void execute(Object receiver, Object[] arguments) {
        Annotation activeAnn = (Annotation) receiver;

        if (activeAnn != null) {
            InlineEditBox box = new InlineEditBox(false);
            box.setAnnotation(activeAnn);
            box.detachEditor();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }
}
