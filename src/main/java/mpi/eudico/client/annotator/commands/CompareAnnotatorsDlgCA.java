package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates a dialog that allows comparison of the annotations on two tiers.
 */
public class CompareAnnotatorsDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param viewerManager the ViewerManager
     */
    public CompareAnnotatorsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.COMPARE_ANNOTATORS_DLG);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.COMPARE_ANNOTATORS_DLG);
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
