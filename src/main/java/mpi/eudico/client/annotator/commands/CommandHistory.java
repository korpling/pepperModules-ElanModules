package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;


/**
 * DOCUMENT ME!
 * $Id: CommandHistory.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class CommandHistory {
    /** Holds value of property DOCUMENT ME! */
    public final static int historySize = 25;
    private Vector history;
    private int currentCommand;
    private UndoCA undoCA;
    private RedoCA redoCA;

    /**
     * Creates a new CommandHistory instance
     *
     * @param size DOCUMENT ME!
     */
    public CommandHistory(int size) {
        if (size > 0) {
            history = new Vector(size);

            for (int i = 0; i < size; i++) {
                history.add(null);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theCommand DOCUMENT ME!
     */
    public void addCommand(Command theCommand) {
        // algorithm:
        // - discard all commands that are more recent than current command
        // - shift the rest leaving 1 place at the beginning
        // - commands that go over the history size are discarded as well
        // - insert new command at the beginning
        // - adjust undo and redo command actions
        if (currentCommand > 0) { // not at beginning

            Iterator i = history.iterator();

            while (i.hasNext()) {
                Command c = (Command) i.next();
                int index = history.indexOf(c);

                if (index > (currentCommand - 1)) {
                    //	(index - (currentCommand -1) < history.size())) {
                    history.setElementAt(c, index - (currentCommand - 1)); // leave first untouched
                }

                if (currentCommand != 1) {
                    history.setElementAt(null, index);
                }
            }
        } else if (currentCommand == 0) { // at beginning, shift everything 1 pos right

            for (int i = history.size() - 2; i >= 0; i--) {
                history.setElementAt(history.elementAt(i), i + 1);
            }
        }

        // insert at beginning
        history.setElementAt(theCommand, 0);

        // adjust current command
        adjustCurrentCommand(0);
    }

    private void adjustCurrentCommand(int newIndex) {
        currentCommand = newIndex;

        if ((currentCommand < history.size()) &&
                (history.elementAt(currentCommand) != null)) {
            String undoString = ElanLocale.getString("Menu.Edit.Undo");
            undoString += " ";
            undoString += ElanLocale.getString(((Command) (history.elementAt(
                    currentCommand))).getName());

            undoCA.putValue(Action.NAME, undoString);
            undoCA.setEnabled(true);
        } else {
            undoCA.putValue(Action.NAME, ElanLocale.getString("Menu.Edit.Undo"));
            undoCA.setEnabled(false);
        }

        if (currentCommand > 0) {
            String redoString = ElanLocale.getString("Menu.Edit.Redo");
            redoString += " ";
            redoString += ElanLocale.getString(((Command) (history.elementAt(currentCommand -
                    1))).getName());

            redoCA.putValue(Action.NAME, redoString);
            redoCA.setEnabled(true);
        } else {
            redoCA.putValue(Action.NAME, ElanLocale.getString("Menu.Edit.Redo"));
            redoCA.setEnabled(false);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void undo() {
        // undo current command
        if (history.elementAt(currentCommand) != null) {
            ((UndoableCommand) (history.elementAt(currentCommand))).undo();

            // point to next
            currentCommand++;

            // adjust undo and redo command actions
            adjustCurrentCommand(currentCommand);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void redo() {
        if (currentCommand > 0) {
            // point to previous
            currentCommand--;

            // redo current command
            ((UndoableCommand) (history.elementAt(currentCommand))).redo();

            // adjust undo and redo command actions
            adjustCurrentCommand(currentCommand);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theUndoCA DOCUMENT ME!
     */
    public void setUndoCA(UndoCA theUndoCA) {
        undoCA = theUndoCA;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theRedoCA DOCUMENT ME!
     */
    public void setRedoCA(RedoCA theRedoCA) {
        redoCA = theRedoCA;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String s = "\n";

        for (int i = 0; i < history.size(); i++) {
            if (history.elementAt(i) != null) {
                if (i == currentCommand) {
                    s += "-> ";
                } else {
                    s += "   ";
                }

                s += (((Command) history.elementAt(i)).getName() + "\n");
            }
        }

        return s;
    }
}
