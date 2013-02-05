package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to replace an existing Controlled Vocabulary in a Transcription
 * by a new Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class ReplaceCVCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;
    private ControlledVocabulary conVoc;
    private ControlledVocabulary newConVoc;

    /**
     * Creates a new ReplaceCVCommand instance
     *
     * @param name the name of the command
     */
    public ReplaceCVCommand(String name) {
        commandName = name;
    }

    /**
     * Removes the new CV and adds the old CV again.
     */
    public void undo() {
    	if (transcription != null) {
			transcription.removeControlledVocabulary(newConVoc);

			transcription.addControlledVocabulary(conVoc);
    	}
    }

    /**
     * Removes the old CV and adds the new CV again.
     */
    public void redo() {
		if (transcription != null) {
			transcription.removeControlledVocabulary(conVoc);

			transcription.addControlledVocabulary(newConVoc);
		}
    }

    /**
     * Replacing a ControlledVocabulary comes down to deleting the old one  and
     * adding the new one in one (action)command.<br>
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the old Controlled
     *        Vocabulary (Controlled Vocabulary)</li> <li>arg[1] = the the new
     *        Controlled Vocabulary (Controlled Vocabulary)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        conVoc = (ControlledVocabulary) arguments[0];
        newConVoc = (ControlledVocabulary) arguments[1];

        if ((transcription.getControlledVocabulary(conVoc.getName()) != null) &&
                (newConVoc != null)) {
            transcription.removeControlledVocabulary(conVoc);

            transcription.addControlledVocabulary(newConVoc);
        }
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
