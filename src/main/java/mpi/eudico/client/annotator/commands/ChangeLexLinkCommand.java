package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexiconLink;

public class ChangeLexLinkCommand implements UndoableCommand {

    private String commandName;

    // receiver
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    
    private LexiconLink oldLink;
    private LexiconLink newLink;

    /**
     * Creates a new AddLexLinkCommand instance
     *
     * @param name the name of the command
     */
    public ChangeLexLinkCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added Lexicon Link from the Transcription.
     */
    public void undo() {
    	if (transcription != null) {
    		//transcription.changeLexiconLink(newLink, oldLink);
    	}
    }

    /**
     * The redo action. Adds the created Lexicon Link to the Transcription.
     */
    public void redo() {
		if (transcription != null) {
			//transcription.changeLexiconLink(oldLink, newLink);
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: 
     * 		  arg[0] = the Lexicon Link object 
     * 		(Lexicon Link)
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        
        if (arguments[0] instanceof LexiconLink && arguments[1] instanceof LexiconLink) {
			oldLink = (LexiconLink) arguments[0];
			newLink = (LexiconLink) arguments[1];
			//transcription.changeLexiconLink(oldLink, newLink);
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
