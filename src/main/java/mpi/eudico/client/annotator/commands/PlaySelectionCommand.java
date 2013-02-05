package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.NativeMediaPlayerWindowsDS;
import mpi.eudico.client.annotator.player.QTMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: PlaySelectionCommand.java 29147 2012-02-03 10:18:20Z aarsom $
 * @author $Author$
 * @version $Revision$
 */
public class PlaySelectionCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;
    private Selection s;
    private ElanMediaPlayerController mediaPlayerController;
    private long beginTime;
    private long endTime;

    /**
     * Creates a new PlaySelectionCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PlaySelectionCommand(String theName) {
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
        // arguments[0] is Selection
        // arguments[1] is ElanMediaPlayerController
        // arguments[2] is the play around selection value
        player = (ElanMediaPlayer) receiver;
        s = (Selection) arguments[0];
        mediaPlayerController = (ElanMediaPlayerController) arguments[1];

        int playAroundSelectionValue = ((Integer) arguments[2]).intValue();

        if (player == null) {
            return;
        }

        //stop if a selection is being played
        if (player.isPlaying()) {
        	//mediaPlayerController.setPlaySelectionMode(false);
            player.stop();
            //mediaPlayerController.setLoopMode(false);
            mediaPlayerController.stopLoop();
            mediaPlayerController.setPlaySelectionMode(false);
            // if playing a selection has been stopped before the end of the selection this 
            // makes the player jump to the end of the file when the old stoptime is reached
            // with Windows media framework
            if (player instanceof NativeMediaPlayerWindowsDS) {
            	// workaround
                long t = player.getMediaTime();
                player.setMediaTime(mediaPlayerController.getSelectionEndTime());
                player.setStopTime(player.getMediaDuration());//??
                player.setMediaTime(t);
            } else {
            	player.setStopTime(player.getMediaDuration());
            }
            
            return;
        }
        
        long mediaTime = player.getMediaTime();

        beginTime = s.getBeginTime();
        endTime = s.getEndTime();

        //if there is no selection
        if (beginTime == endTime) {
            return;
        }

        //apply the play around selection value
        if (playAroundSelectionValue > 0) {
            beginTime -= playAroundSelectionValue;

            if (beginTime < 0) {
                beginTime = 0;
            }

            endTime += playAroundSelectionValue;

            if (endTime > player.getMediaDuration()) {
                endTime = player.getMediaDuration();
            }
        }

        //if not playing, start playing
        if ((player.isPlaying() == false) && (mediaTime >= beginTime) &&
                (mediaTime < endTime - 5)) {
        	if(!mediaPlayerController.isBeginBoundaryActive() ){
        		mediaPlayerController.toggleActiveSelectionBoundary();
        	}
        	//mediaPlayerController.toggleActiveSelectionBoundary();
            mediaPlayerController.setPlaySelectionMode(true);
            playInterval(mediaTime, endTime);
            if (mediaPlayerController.getLoopMode()) {
	            // start the loop thread, delayed
	            delayedStartLoop();
            }

            return;
        }

        if (mediaPlayerController.getLoopMode() == true) {
            mediaPlayerController.setPlaySelectionMode(true);
            doStartLoop();
        } else {
        	mediaPlayerController.setPlaySelectionMode(true);
            playInterval(beginTime, endTime);
        }
    }

    private void playInterval(long begin, long end) {
        player.playInterval(begin, end);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }

    /**
     * DOCUMENT ME!
     */
    public void doStartLoop() {
        mediaPlayerController.startLoop(beginTime, endTime);
    }
    
    /**
     * Starts the loop after a first partial play selection has finished
     */
    private void delayedStartLoop() {
        LoopThread loopthread = new LoopThread();
        loopthread.start();
    }

    /**
     * Calls the media player controllers startloop method after a first, partial selection playback has finished.
     */
    private class LoopThread extends Thread {
        /**
         * DOCUMENT ME!
         */
        public void run() {
            if (mediaPlayerController.isPlaySelectionMode() && mediaPlayerController.getLoopMode() == true) {
	            try {// give player time to start
	            	Thread.sleep(200);
	            } catch (InterruptedException ie) {
	            	
	            }
	            while (player.isPlaying()) {// wait until stopped
	            	try {
	            		Thread.sleep(50);
	            	} catch (InterruptedException ie) {
	            		
	            	}
	            }
	            // then start the loop, if player not yet stopped
	            if (mediaPlayerController.isPlaySelectionMode()) {	 
	            	try {
	            		Thread.sleep(500);
	            	} catch (InterruptedException ie) {
	            		
	            	}
	            	mediaPlayerController.startLoop(beginTime, endTime);
	            }
	            
            	/*
                if (!player.isPlaying()) {
                    playInterval(beginTime, endTime);    
                }

                while (player.isPlaying() == true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                }

                try {
                    Thread.sleep(mediaPlayerController.getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
                */
            }
        }
    }
     //end of LoopThread
}
