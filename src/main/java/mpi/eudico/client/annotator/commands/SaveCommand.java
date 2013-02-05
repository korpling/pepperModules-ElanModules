package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;


import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class SaveCommand implements Command {
    private String commandName;

    /**
     * Creates a new SaveCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public SaveCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;

        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];

        // for the moment, don't deal with visible tiers
        try {
            eafTranscriptionStore.storeTranscription(tr, null, new Vector(), null,
                    TranscriptionStore.EAF);
            tr.setUnchanged();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                    //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                    "Unable to save the transcription file: " +
                    "(" + ioe.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
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
