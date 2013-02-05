package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.FrameManager;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * Action that activates the next window.
 *
 * @author Han Sloetjes, MPI
 */
public class PrevWindowMA extends MenuAction {
    /**
     * Constructor.
     *
     * @param name the name of the action
     */
    public PrevWindowMA(String name) {
        super(name);
    }

    /**
     * Tells the FrameManager to activate the previous window in the menu.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        FrameManager.getInstance().activateNextFrame(false);
    }
}
