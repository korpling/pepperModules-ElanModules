package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.help.HelpException;
import mpi.eudico.client.annotator.help.HelpWindow;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/**
 * A menu action that creates a new help contents window, or brings the
 * exisitng window to front.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class HelpMA extends FrameMenuAction {
    private static Window window;

    /**
     * Creates a new HelpMA instance
     *
     * @param name the name of the action
     */
    public HelpMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates and/or shows the help window.
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        if (window != null) {
            if (!window.isVisible()) {
                window.setVisible(true);
            }

            window.toFront();
        } else {
            try {
                window = HelpWindow.getHelpWindow();
                window.setVisible(true);
            } catch (HelpException he) {
                String message = he.getMessage();

                if (he.getCause() != null) {
                    message += ("\n" + he.getCause().getMessage());
                }

                JOptionPane.showMessageDialog(frame,
                    ElanLocale.getString("Message.NoHelp") + "\n" + message,
                    ElanLocale.getString("Menu.Help"),
                    JOptionPane.ERROR_MESSAGE, null);
            }
        }
    }
}
