package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * A command action for modifying a graphical annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyGraphicAnnotationCA extends CommandAction
    implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new ModifyGraphicAnnotationCA instance
     *
     * @param viewerManager the ViewerManager
     */
    public ModifyGraphicAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_GRAPHIC_ANNOTATION);

        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new Command.
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MODIFY_GRAPHIC_ANNOTATION_DLG);
    }

    /**
     * The receiver of this CommandAction is the Annotation that should be
     * modified.
     *
     * @return the receiver of this command
     */
    protected Object getReceiver() {
        return activeAnnotation;
    }

    /**
     * Returns the arguments for this command
     *
     * @return the arguments for this command
     */
    protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * Check whether or not this Action should be enabled given the current 
     * active annotation.
     */
    protected void checkState() {
        setEnabled(false);

        if (activeAnnotation == null) {
            return;
        }

        TierImpl tier = (TierImpl) activeAnnotation.getTier();

        if (activeAnnotation instanceof SVGAlignableAnnotation && tier.getLinguisticType().hasGraphicReferences()) {
            setEnabled(true);
        }
    }
}
