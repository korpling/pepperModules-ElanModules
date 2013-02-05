package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.NativeMediaPlayerWindowsDS;


/**
 * DOCUMENT ME!
 * $Id: PlayPauseCommand.java 8514 2007-04-05 13:17:36Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class PlayPauseCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;
    private ElanMediaPlayerController mediaPlayerController;

    /**
     * Creates a new PlayPauseCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PlayPauseCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is ElanMediaPlayerController
        player = (ElanMediaPlayer) receiver;
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];

        if (player == null) {
            return;
        }

        boolean playSel = mediaPlayerController.isPlaySelectionMode();
        mediaPlayerController.setPlaySelectionMode(false);
        mediaPlayerController.stopLoop();
        
        if (player.isPlaying() == true) {
            player.stop();
            
            if (playSel) {           
	            if (player instanceof NativeMediaPlayerWindowsDS) {
	            	// workaround
	                long t = player.getMediaTime();
	                player.setMediaTime(mediaPlayerController.getSelectionEndTime());
	                player.setStopTime(player.getMediaDuration());//??
	                player.setMediaTime(t);
	            } else {
	            	player.setStopTime(player.getMediaDuration());
	            }
            }
        } else {
            player.start();
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
