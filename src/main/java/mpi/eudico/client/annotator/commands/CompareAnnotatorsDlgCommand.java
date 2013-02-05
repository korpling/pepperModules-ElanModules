package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.CompareAnnotatorsDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates a dialog that allows to compare the segmentation and labeling of two annotators.
 */
public class CompareAnnotatorsDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public CompareAnnotatorsDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the dialog
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;

        new CompareAnnotatorsDlg(trans, ELANCommandFactory.getRootFrame(trans)).setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    public String getName() {
        return commandName;
    }
}
