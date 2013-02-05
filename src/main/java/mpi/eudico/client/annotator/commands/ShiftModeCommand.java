package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;


/**
 *
 */
public class ShiftModeCommand implements Command {
    private String commandName;

    /**
     * Creates a new ShiftModeCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ShiftModeCommand(String name) {
        commandName = name;
    }

    /**
     *
     */
    public void execute(Object receiver, Object[] arguments) {
        ((Transcription) receiver).setTimeChangePropagationMode(Transcription.SHIFT);
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
