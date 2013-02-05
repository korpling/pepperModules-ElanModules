package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates the annotations from gaps dialog.
 */
public class AnnotationsFromGapsDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param viewerManager the ViewerManager
     */
    public AnnotationsFromGapsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANN_FROM_GAPS);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ANN_FROM_GAPS_COM);
    }

    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }
}
