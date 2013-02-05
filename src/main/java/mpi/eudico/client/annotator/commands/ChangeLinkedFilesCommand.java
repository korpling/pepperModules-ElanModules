package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.linkedmedia.LinkedFileDescriptorUtil;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.util.FrameConstants;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.util.Vector;


/**
 * A Command to change the set of linked media or secondary files.
 *
 * @author Han Sloetjes
 */
public class ChangeLinkedFilesCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription 
    private TranscriptionImpl transcription;
    private Vector oldDescriptors;
    private Vector newDescriptors;

    // a flag for the kind of descriptors that have been passed to the command
    private boolean areMediaDesc = true;

    /**
     * Creates a new ChangeLinkedFilesCommand instance
     *
     * @param name the name of the command
     */
    public ChangeLinkedFilesCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Restores the old values of the linked files.
     */
    public void undo() {
        if ((transcription != null) && (oldDescriptors != null)) {
            if (areMediaDesc) {
                updateMediaPlayers(transcription, oldDescriptors);
            } else {
                updateLinkedFiles(transcription, oldDescriptors);
            }
        }
    }

    /**
     * The redo action.
     */
    public void redo() {
        if ((transcription != null) && (newDescriptors != null)) {
            if (areMediaDesc) {
                updateMediaPlayers(transcription, newDescriptors);
            } else {
                updateLinkedFiles(transcription, newDescriptors);
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the vector with the new
     *        media descriptors or secondary linked files descriptors
     *        (Vector)</li> <li>arg[1] = a flag for the type of descriptors:
     *        if <code>true</code> the descriptors are primary, media
     *        descriptors, if <code>false</code> the descriptors are secondary
     *        linked file descriptors</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if ((arguments != null) && (arguments.length >= 1)) {
            newDescriptors = (Vector) arguments[0];

            if (arguments.length > 1) {
                areMediaDesc = ((Boolean) arguments[1]).booleanValue();
            }
        }

        if (transcription != null) {
            if (areMediaDesc) {
                oldDescriptors = transcription.getMediaDescriptors();

                // check if there is any difference??
                updateMediaPlayers(transcription, newDescriptors);
                // enable / disable the setFrameLength menu
                ElanFrame2 ef2 = (ElanFrame2) ELANCommandFactory.getRootFrame(transcription);
                if (ef2 != null) {
                    ElanMediaPlayer master = ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer();
                    ef2.setMenuEnabled(FrameConstants.FRAME_LENGTH, !master.isFrameRateAutoDetected());
                    ef2.updateMenu(FrameConstants.MEDIA_PLAYER);
                }
            } else {
                oldDescriptors = transcription.getLinkedFileDescriptors();
                updateLinkedFiles(transcription, newDescriptors);
            }
        }
    }

    /**
     * Tries to update the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    private void updateMediaPlayers(TranscriptionImpl transcription,
        Vector descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        MediaDescriptorUtil.updateMediaPlayers(transcription, descriptors);
    }

    /**
     * Delegates all updating that needs to be done to a utility class.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new linked file descriptors
     */
    private void updateLinkedFiles(TranscriptionImpl transcription,
        Vector descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        LinkedFileDescriptorUtil.updateLinkedFiles(transcription, descriptors);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }
}
