package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CommandAction to generate printout.
 *
 * @author Han Sloetjes
 */
public class PrintCA extends CommandAction {
    /**
     * Creates a new PrintCA instance
     *
     * @param viewerManager the viewer manager
     */
    public PrintCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PRINT);

        updateLocale(); //because of mnemonic
    }

    /**
     * Creates a new Print Command
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PRINT);
    }

    /**
     * The receiver is a Transcription object
     *
     * @return the transcription
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns null.
     *
     * @return null
     */
    protected Object[] getArguments() {
        return null;
    }
}
