package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;

/**
 * A Command to change an entry in a Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class ChangeCVEntryCommand implements Command {
	private String commandName;
	
	/**
	 * Creates a new ChangeCVEntryCommand instance
	 *
	 * @param name the name of the command
	 */
	public ChangeCVEntryCommand(String name) {
		commandName = name;
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are 
	 * correct.<br>
	 * When the CV is connected to a Transcription it will handle the notification 
     * of the change.
     * 
	 * @param receiver the Controlled Vocabulary
	 * @param arguments the arguments: <ul><li>arg[0] = the old value
	 *        of the entry (String)</li> <li></li>arg[1] = the new value 
	 *        of the entry (String)<li>arg[2] = the description of
	 *        the entry</li> </ul>
	 */
	public void execute(Object receiver, Object[] arguments) {

		ControlledVocabulary conVoc = (ControlledVocabulary) receiver;
		String oldValue = (String) arguments[0];
		String newValue = (String) arguments[1];
		String desc = (String) arguments[2];
		
		CVEntry curEntry = conVoc.getEntryWithValue(oldValue);
		if (curEntry != null) {
			conVoc.modifyEntryValue(curEntry, newValue);
			curEntry.setDescription(desc);
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
