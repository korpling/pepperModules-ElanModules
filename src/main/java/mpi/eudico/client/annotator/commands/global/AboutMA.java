package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.AboutPanel;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


/**
 * Menu action that shows the About box.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class AboutMA extends FrameMenuAction {
    /**
     * Creates a new AboutMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public AboutMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an ELAN About message pane.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(frame, new AboutPanel(),
            ElanLocale.getString("Menu.Help.AboutDialog"),
            JOptionPane.PLAIN_MESSAGE, null);
    }
}
