package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.gui.EditLexSrvcDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

public class EditLexSrvcDlgCommand implements Command {
	private String commandName;

    /**
     * Creates a new edit lexicon service command.
     *
     * @param name the name of the command
     */
	public EditLexSrvcDlgCommand (String name) {
		commandName = name;
	}
	
	/**
     * Creates the edit lexicon service dialog.
     *
     * @param receiver the transcription holding the lexicon services
     * @param arguments null
     */
	public void execute(Object receiver, Object[] arguments) {
		Transcription transcription = (Transcription) receiver;
        new EditLexSrvcDialog(transcription).setVisible(true);
	}

	public String getName() {
		return commandName;
	}

}
