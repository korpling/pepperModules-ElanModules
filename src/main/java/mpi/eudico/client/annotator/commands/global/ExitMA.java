package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.FrameManager;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * Action that starts the Exit Application sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ExitMA extends MenuAction {
    /**
     * Creates a new ExitMA instance.
     *
     * @param name the name of the action (command)
     */
    public ExitMA(String name) {
        super(name);
    }

    /**
     * Invokes exit on the FrameManager.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        FrameManager.getInstance().exit();
    }
}
