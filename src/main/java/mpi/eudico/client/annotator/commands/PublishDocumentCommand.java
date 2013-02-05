package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.p2p.ElanP2P;
import mpi.eudico.p2p.PublishAndDiscoverPanel;

import mpi.eudico.server.corpora.clom.Transcription;

import javax.swing.JOptionPane;


/**
 *
 */
public class PublishDocumentCommand implements Command {
    private String commandName;

    /**
     * Creates a new PublishDocumentCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public PublishDocumentCommand(String name) {
        commandName = name;
    }

    /**
     * Publishes the document.<br>
     *
     * @param receiver the ElanP2P object
     * @param arguments the arguments:  <ul><li>arg[0] = the ElanFrame2
     *        object</li> <li>arg[1] = the Transcription object
     *        (DobesTranscription)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        PublishAndDiscoverPanel panel = new PublishAndDiscoverPanel(PublishAndDiscoverPanel.PUBLISH_MODE);
        ElanFrame2 parent = null;

        if ((arguments != null) && (arguments.length > 0)) {
            if (arguments[0] instanceof ElanFrame2) {
                parent = (ElanFrame2) arguments[0];
            }
        }

        if (arguments[1] instanceof Transcription) {
            Transcription transcription = (Transcription) arguments[1];

            panel.setDocumentName(transcription.getName());
        }

        String name = null;
        String email = null;
        int option = -1;

        do {
            option = JOptionPane.showOptionDialog(parent, panel,
                    ElanLocale.getString("Menu.P2P.PublishDocument"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, null, null);

            if (option != JOptionPane.OK_OPTION) {
                return;
            } else {
                name = panel.getName();
                email = panel.getEmail();

                if ((name == null) || (name.length() == 0) || (email == null) ||
                        (email.length() == 0) || (email.indexOf('@') < 1)) {
                    JOptionPane.showMessageDialog(parent,
                        ElanLocale.getString(
                            "P2P.PublishAndDiscoverPanel.Message.Required"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    break;
                }
            }
        } while (true);

        // do something with the name and email address
        ((ElanP2P) receiver).startServer(name, email);
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
