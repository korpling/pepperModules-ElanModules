package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.TierOrder;

import mpi.eudico.client.annotator.export.ExportPraatDialog;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that brings up a dialog for Praat TextGrid export.
 *
 * @author Han Sloetjes
 */
public class ExportPraatGridCommand implements Command {
    private String commandName;

    /**
     * Creates a new command
     *
     * @param theName the name of the command
     */
    public ExportPraatGridCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object (TranscriptionImpl)</li> <li>arg[1] = the Selection
     *        object (Selection)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {    	
    	new ExportPraatDialog(ELANCommandFactory.getRootFrame((TranscriptionImpl) arguments[0]), true,
				(TranscriptionImpl) arguments[0], (Selection) arguments[1]).setVisible(true);
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return commandName;
    }
}
