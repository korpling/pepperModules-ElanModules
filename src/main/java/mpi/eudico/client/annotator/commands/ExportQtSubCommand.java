package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportQtSubtitleDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Created on Jul 2, 2004
 * @author Alexander Klassmann
 * @version Jul 2, 2004
 */
public class ExportQtSubCommand implements Command {
	private String commandName;

	/**
	 * Creates a new ExportSmilCommand instance
	 *
	 * @param theName DOCUMENT ME!
	 */
	public ExportQtSubCommand(String theName) {
		commandName = theName;
	}

	/**
	  * <b>Note: </b>it is assumed the types and order of the arguments are
	  * correct.
	  *
	  * @param receiver null
	  * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
	  *        object(Transcription)</li> <li>arg[1] = the Selection object
	  *        (Selection)</li> </ul>
	  */
	 public void execute(Object receiver, Object[] arguments) {
		 new ExportQtSubtitleDialog(ELANCommandFactory.getRootFrame(
				 (Transcription) arguments[0]), true,
			 (Transcription) arguments[0], (Selection) arguments[1]).setVisible(true);
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
