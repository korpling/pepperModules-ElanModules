package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.gui.FixedSizePanel;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;


/**
 * The Empty implementation of an elan media player i.e. a MediaPlayer without
 * media
 */
public class EmptyMediaPlayer extends ControllerManager
    implements ElanMediaPlayer, ControllerListener {
    private long mediaTime;
    private long offset;
    private float rate;
    private float volume;
    private boolean playing;
    //private FixedSizePanel visualComponent;
    private long milliSecondsPerSample;
    private long duration;
    private long startTimeMillis;
    private boolean playingInterval;
    private PeriodicUpdateController periodicController;
    private long intervalStopTime;
    
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	/**
	 * Specifies a minimal duration for empty players.
	 */
	private final long MIN_DURATION = 5 * 60 * 1000L;

    /**
     * 
     *
     * @param duration DOCUMENT ME!
     */
    public EmptyMediaPlayer(long duration) {
        this.duration = Math.max(MIN_DURATION, duration);
        offset = 0;
        volume = 1;
        rate = 1;
        milliSecondsPerSample = 40;

        //visualComponent = new FixedSizePanel(200, 200);
    }

    public mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor getMediaDescriptor() {
    	return null;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFrameworkDescription() {
        return "Empty Media Player";
    }

    /**
     * Elan controllerUpdate Used to stop at the stop time in cooperation with
     * the playInterval method
     *
     * @param event DOCUMENT ME!
     */
    public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            if (periodicController != null) {
                if (getMediaTime() >= intervalStopTime) {
                    // stop the player
                    stop();
                }
            }
        }
    }

    /**
     * play between two times. This method uses the contollerUpdate method to
     * detect if the stop time is passed. The setStopTime method of JMF can
     * not be used because it gives unstable behaviour
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    public synchronized void playInterval(long startTime, long stopTime) {
        if (playingInterval || (stopTime <= startTime)) {
            return;
        }

        periodicController = new PeriodicUpdateController(25);
        periodicController.addControllerListener(this);
        addController(periodicController);
        intervalStopTime = stopTime;
        setMediaTime(startTime);
        playingInterval = true;
        start();
    }
    
    /**
     * Empty implementation for ElanMediaPlayer Interface
     * Only usefull for player that corectly supports setting stop time
     */
    public void setStopTime(long stopTime) {
    	
    }

    /**
     * Disable all code for interval playing
     */
    private void stopPlayingInterval() {
        if (periodicController != null) {
            periodicController.removeControllerListener(this);
            removeController(periodicController);
            periodicController = null;
        }

        playingInterval = false;
    }

    /**
     * Gets the display Component for this Player.
     *
     * @return DOCUMENT ME!
     */
    public java.awt.Component getVisualComponent() {
        return null; //visualComponent;
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceHeight()
     */
    public int getSourceHeight() {
        return 0;
    }
    
    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceWidth()
     */
    public int getSourceWidth() {
        return 0;
    }
    
    /**
     * Gets the ratio between width and height of the video image
     *
     * @return DOCUMENT ME!
     */
    public float getAspectRatio() {
        return 1.0f;
    }

    /**
     * Enforces an aspect ratio for the media component.
     * 
     * @param aspectRatio the new aspect ratio
     */
    public void setAspectRatio(float aspectRatio){
    	// stub
    }
    
    /**
     * Starts the Player as soon as possible. is not synchronized in JMF
     */
    public synchronized void start() {
        playing = true;

        startTimeMillis = System.currentTimeMillis();

        // make sure all managed controllers are started
        startControllers();
    }

    /**
     * Stop the media player
     */
    public synchronized void stop() {
        if (playing) {
        	if (rate == 1) {
        		mediaTime += (System.currentTimeMillis() - startTimeMillis);
        	} else {
        		float advance = (System.currentTimeMillis() - startTimeMillis) * rate;
        		mediaTime += (long) advance;
        	}
            
        }

        playing = false;

        // make sure all managed controllers are stopped
        stopControllers();

        // make sure that all interval playing is finished
        if (playingInterval) {
            stopPlayingInterval();
        }
    }

    /**
     * Tell if this player is playing
     *
     * @return DOCUMENT ME!
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the step size for one frame, defaults to 40 ms
     */
    public long getMilliSecondsPerSample() {
        return milliSecondsPerSample;
    }

    /**
     * DOCUMENT ME!
     *
     * @param milliSeconds the step size for one frame
     */
    public void setMilliSecondsPerSample(long milliSeconds) {
        milliSecondsPerSample = milliSeconds;
    }

    /**
     * Set the offset to be used in get and set media time for this player
     *
     * @param offset the offset in milli seconds
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the offset used by this player
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Gets this Clock's current media time in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getMediaTime() {       
        if (playing) {
        	if (rate == 1) {
        		//System.out.println("Eget mt play: " + ((mediaTime + System.currentTimeMillis()) - startTimeMillis));
        		return (mediaTime + System.currentTimeMillis()) - startTimeMillis;
        	} else {
        		float advance = (System.currentTimeMillis() - startTimeMillis) * rate;
        		return mediaTime + (long) advance;
        	}
        }
        //System.out.println("Eget mt : " + (mediaTime - offset) + "  " + playing);
        return mediaTime - offset;
    }

    /**
     * Sets the Clock's media time in milli seconds. is not synchronized in JMF
     *
     * @param time DOCUMENT ME!
     */
    public void setMediaTime(long time) {
        //System.out.println("Eset mt : " + (time + offset));	
        mediaTime = time + offset;
        setControllersMediaTime(time);
    }

    public void nextFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = getMediaTime() / milliSecondsPerSample;
    		setMediaTime((curFrame + 1) * milliSecondsPerSample);
    	} else {
    		setMediaTime(getMediaTime() + getMilliSecondsPerSample());
    	}
    }
    
    public void previousFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = getMediaTime() / milliSecondsPerSample;
    		if (curFrame > 0) {
    			setMediaTime((curFrame - 1) * milliSecondsPerSample);
    		} else {
    			setMediaTime(0);
    		}
    	} else {
    		setMediaTime(getMediaTime() - getMilliSecondsPerSample());
    	}
    }

    public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
    	frameStepsToFrameBegin = stepsToFrameBegin;
    }
    /**
     * Gets the current temporal scale factor.
     *
     * @return DOCUMENT ME!
     */
    public float getRate() {
        return rate;
    }

    /**
     * Sets the temporal scale factor.
     *
     * @param rate DOCUMENT ME!
     */
    public synchronized void setRate(float rate) {
        this.rate = rate;
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isFrameRateAutoDetected()
     */
    public boolean isFrameRateAutoDetected() {
        return false;
    }
    
    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return DOCUMENT ME!
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @param level DOCUMENT ME!
     */
    public void setVolume(float level) {
        volume = level;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layoutManager DOCUMENT ME!
     */
    public void setLayoutManager(ElanLayoutManager layoutManager) {
    }

    /**
     * DOCUMENT ME!
     */
    public void updateLocale() {
    }

    /**
     * Get the duration of the media represented by this object in milliseconds.
     *
     * @return the current duration of the player
     */
    public long getMediaDuration() {
        return duration;
    }
    
    /**
     * Set the duration of the media represented by this object in milliseconds.
     * The minimum duration is enforced.
     * 
     * @param dur the new duration
     */
    public void setMediaDuration(long dur) {
        duration = Math.max(MIN_DURATION, dur);;
    }

	public void cleanUpOnClose() {
		
		
	}
}
