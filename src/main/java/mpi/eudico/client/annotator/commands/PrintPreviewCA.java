package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to generate printout.
 *
 * @author Han Sloetjes
 */
public class PrintPreviewCA extends CommandAction {
    /**
     * Creates a new PrintPreviewCA instance
     *
     * @param viewerManager the viewer manager
     */
    public PrintPreviewCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PREVIEW);
    }

    /**
     * Creates a new Command
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PREVIEW);
    }

    /**
     * The receiver is a Transcription.
     *
     * @return a Transcription object
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * A Selection object.
     *
     * @return the selection
     */
    protected Object[] getArguments() {
        return new Object[] { vm.getSelection() };
    }
}
