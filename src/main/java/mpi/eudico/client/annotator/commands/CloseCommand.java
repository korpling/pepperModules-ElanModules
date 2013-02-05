package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanFrame2;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that closes a single Elan frame.
 * 
 * @author Han Sloetjes, MPI
 * @version 1.0
  */
public class CloseCommand implements Command {
    private String commandName;

    /**
     * Creates a new CloseCommand instance
     *
     * @param name the name of the command
     */
    public CloseCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>null</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        final TranscriptionImpl transcription = (TranscriptionImpl) receiver;
        final ElanFrame2 frame = (ElanFrame2) ELANCommandFactory.getRootFrame(transcription);
        
        frame.checkSaveAndClose();

//        if (transcription.isChanged()) {
//            // the frame should unregister with the FrameManager
//            frame.checkSaveAndClose();
//        } else {
//            frame.doClose(true);
//        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }
}
