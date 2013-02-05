package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates the "merge tiers wizard".
 */
public class MergeTiersDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param viewerManager the ViewerManager
     */
    public MergeTiersDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MERGE_TIERS);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MERGE_TIERS_DLG);
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
