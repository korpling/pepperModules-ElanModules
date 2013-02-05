package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

/**
 * DOCUMENT ME! $Id: UndoCA.java 14860 2009-04-23 09:55:05Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 */
public class UndoCA extends CommandAction {
    private CommandHistory commandHistory;

    /**
     * Creates a new UndoCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param theHistory DOCUMENT ME!
     */
    public UndoCA(ViewerManager2 theVM, CommandHistory theHistory) {
        super(theVM, ELANCommandFactory.UNDO);
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
        commandHistory.undo();
    }
}
