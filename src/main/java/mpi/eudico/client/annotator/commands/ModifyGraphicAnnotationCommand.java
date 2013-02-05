package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;

import java.awt.Shape;


/**
 * A command for modifying a graphical annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyGraphicAnnotationCommand implements UndoableCommand {
    private String commandName;
    private SVGAlignableAnnotation annotation;
    private Shape oldShape;
    private Shape newShape;

    /**
     * Creates a new ModifyGraphicAnnotationCommand instance
     *
     * @param name the name of the command
     */
    public ModifyGraphicAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * Undo the changes made by this command.
     */
    public void undo() {
        if (annotation != null) {
            annotation.setShape(oldShape);
        }
    }

    /**
     * Redo the changes made by this command.
     */
    public void redo() {
        if (annotation != null) {
            annotation.setShape(newShape);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the active Annotation
     * @param arguments the arguments:  <ul><li>arg[0] = the new value of the
     *        graphical annotation (Shape)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        annotation = (SVGAlignableAnnotation) receiver;
        oldShape = annotation.getShape();
        newShape = (Shape) arguments[0];
        annotation.setShape(newShape);
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
