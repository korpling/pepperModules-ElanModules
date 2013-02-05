package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * DOCUMENT ME! $Id: RedoCA.java 14860 2009-04-23 09:55:05Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 */
public class RedoCA extends CommandAction {
    private CommandHistory commandHistory;

    /**
     * Creates a new RedoCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param theHistory DOCUMENT ME!
     */
    public RedoCA(ViewerManager2 theVM, CommandHistory theHistory) {
        super(theVM, ELANCommandFactory.REDO);
        commandHistory = theHistory;

        setEnabled(false); // initially disable
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent event) {
        commandHistory.redo();
    }
}
