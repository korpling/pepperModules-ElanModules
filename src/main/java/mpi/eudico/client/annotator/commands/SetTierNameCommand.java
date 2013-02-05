package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * DOCUMENT ME!
 * $Id: SetTierNameCommand.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class SetTierNameCommand implements UndoableCommand {
    private String commandName;

    // store state for undo and redo
    private TierImpl t;
    private String newTierName;
    private String oldTierName;

    /**
     * Creates a new SetTierNameCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public SetTierNameCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
    }

    /**
     * DOCUMENT ME!
     */
    public void undo() {
    }

    /**
     * DOCUMENT ME!
     */
    public void redo() {
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
