package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.PlayStepAndRepeatCA;
import mpi.eudico.client.annotator.gui.*;

import mpi.eudico.client.annotator.viewer.*;

import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * MediaPlayerControlPanel A collection of buttons, sliders, etc to controls
 * the media, e.g. playing, setting current time.
 */
public class ElanMediaPlayerController extends AbstractViewer {
    /** Holds value of property DOCUMENT ME! */
    final private static Dimension BUTTON_SIZE = new Dimension(30, 20);
    private long userTimeBetweenLoops = 500; //used when playing selection in loop mode, default 0.5 seconds
    private ViewerManager2 vm;
    private ElanSlider rateslider;
    private ElanSlider volumeslider;
    private SelectionPanel selectionpanel;
    private VolumeIconPanel volumeIconPanel;
    private StepAndRepeatPanel stepAndRepeatPanel;
    
    //private MediaPlayerControlSlider mpcs;
    private DurationPanel durationPanel;
    
    //private TimePanel timePanel;
    private PlayButtonsPanel playButtonsPanel;
    private AnnotationNavigationPanel annotationPanel;
    private SelectionButtonsPanel selectionButtonsPanel;
    private ModePanel modePanel;
    private long stopTime = 0;
    private boolean playingSelection = false;
    private boolean bLoopMode = false;
    private boolean bSelectionMode = false;
    private boolean bBeginBoundaryActive = false;
    private boolean stepAndRepeatMode = false;
    
    // loopthread moved from PlaySelectionCommand to here to be able to stop it 
    // actively (instead of passively with a boolean, which has all kind of side effects)
    private LoopThread loopThread;
    
    private StepAndRepeatThread stepThread;

    /**
     * Constructor
     *
     * @param theVM DOCUMENT ME!
     */
    public ElanMediaPlayerController(ViewerManager2 theVM) {
        vm = theVM;

        rateslider = new ElanSlider("ELANSLIDERRATE", 0, 200, 100, vm);
        volumeslider = new ElanSlider("ELANSLIDERVOLUME", 0, 100, 100, vm);
        selectionpanel = new SelectionPanel(vm);

        //	mpcs = new MediaPlayerControlSlider();
        //	timePanel = new TimePanel();
        durationPanel = new DurationPanel(vm.getMasterMediaPlayer()
                                            .getMediaDuration());
        playButtonsPanel = new PlayButtonsPanel(getButtonSize(), vm);
        annotationPanel = new AnnotationNavigationPanel(getButtonSize(), vm);
        selectionButtonsPanel = new SelectionButtonsPanel(getButtonSize(), vm);
        modePanel = new ModePanel(vm, this);        
        volumeIconPanel = new VolumeIconPanel(vm, SwingConstants.VERTICAL, getButtonSize());
        stepAndRepeatPanel = new StepAndRepeatPanel(vm);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getButtonSize() {
        return BUTTON_SIZE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getUserTimeBetweenLoops() {
        return userTimeBetweenLoops;
    }

    /**
     * DOCUMENT ME!
     *
     * @param loopTime DOCUMENT ME!
     */
    public void setUserTimeBetweenLoops(long loopTime) {
        userTimeBetweenLoops = loopTime;
    }

    // getters for subpanels
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MediaPlayerControlSlider getSliderPanel() {
        return vm.getMediaPlayerControlSlider();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AnnotationDensityViewer getAnnotationDensityViewer() {
        return vm.getAnnotationDensityViewer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getRatePanel() {
        return rateslider; //.getSlider();
    }

    /**
     * Sets the play rate and updates the ui.
     *
     * @param rate the play rate
     */
    public void setRate(float rate) {
        super.setRate(rate);
        // multiply by 100; the slider uses ints
        rateslider.setValue((int) (100 * rate));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getVolumePanel() {
        return volumeslider;
    }

    /**
     * Sets the volume and updates the ui.
     *
     * @param volume the volume
     */
    public void setVolume(float volume) {
        super.setVolume(volume);
        // multiply by 100; the slider uses ints
        volumeslider.setValue((int) (100 * volume));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getModePanel() {
        return modePanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SelectionButtonsPanel getSelectionButtonsPanel() {
        return selectionButtonsPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public PlayButtonsPanel getPlayButtonsPanel() {
        return playButtonsPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AnnotationNavigationPanel getAnnotationNavigationPanel() {
        return annotationPanel;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public VolumeIconPanel getVolumeIconPanel() {
        return volumeIconPanel;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getDurationPanel() {
        return durationPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getTimePanel() {
        return vm.getTimePanel();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getSelectionPanel() {
        return selectionpanel;
    }
    
    /**
     * Returns the step-and-repeat mode panel.
     * 
     * @return
     */
    public StepAndRepeatPanel getStepAndRepeatPanel() {
    	return stepAndRepeatPanel;
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    public void updateActiveAnnotation() {
    }

    /**
     * DOCUMENT ME!
     */
    public void updateLocale() {
    }

    /**
     * AR notification that the selection has changed method from SelectionUser
     * not implemented in AbstractViewer
     */
    public void updateSelection() {
    }

    private void adjustSelection() {
        // set active boundary to current media time
        long currTime = getMediaTime();
        long beginTime = getSelectionBeginTime();
        long endTime = getSelectionEndTime();

        if (bBeginBoundaryActive) {
            beginTime = currTime;
        } else {
            endTime = currTime;
        }

        if (beginTime > endTime) { // begin and end change place
            setSelection(endTime, beginTime);
            toggleActiveSelectionBoundary();
        } else {
            setSelection(beginTime, endTime);
        }
    }

    /**
     * AR notification that some media related event happened method from
     * ControllerListener not implemented in AbstractViewer
     *
     * @param event DOCUMENT ME!
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StartEvent) {
            //playingSelection = true;
            return;
        }

        // ignore time events within a certain time span after a stop event
        // that happened while playing a selection. This is needed to keep the
        // current selection after play selection is done in selection mode
        if (event instanceof TimeEvent &&
                ((System.currentTimeMillis() - stopTime) < 700)) {
            return;
        }

        // remember the stop time if the stop happened while playing a selection
        // time events will be ignored for a certain period after this stop time
        if (event instanceof StopEvent) {
            if (!bLoopMode) {
                playingSelection = false;
            }

            stopTime = System.currentTimeMillis();

            // change active annotation boundary if at end of selection and active edge was on the left
            // added for practical reasons, users got confused and inadvertently destroyed the selection
            //    		long halfTime = getSelectionBeginTime() + (getSelectionEndTime() - getSelectionBeginTime()) / 2;
            if (isBeginBoundaryActive() &&
                    (getMediaTime() == getSelectionEndTime())) {
                toggleActiveSelectionBoundary();
            }
            
            // HS Aug 2008: make sure that in selection mode the selection is updated
            // the selection is always a bit behind the media playhead 
            //return;
        }

        //in some cases set a new selection 
        if (!playingSelection && (bSelectionMode == true)) {
            adjustSelection();
        }
    }

    /**
     * Switches the controller to the playing-selection mode.
     *
     * @param b the mode, on or off
     */
    public void setPlaySelectionMode(boolean b) {
        playingSelection = b;
    }

    /**
     * Returns whether the controller is in play selection mode.
     *
     * @return whether the controller is in play selection mode
     */
    public boolean isPlaySelectionMode() {
        return playingSelection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setLoopMode(boolean b) {
        bLoopMode = b;
        modePanel.updateLoopMode(bLoopMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getLoopMode() {
        return bLoopMode;
    }

    /**
     * Toggles the loop mode
     */
    public void doToggleLoopMode() {
        if (bLoopMode == true) {
            bLoopMode = false;
        } else {
            bLoopMode = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getSelectionMode() {
        return bSelectionMode;
    }

    /**
     * Toggles the selection mode
     */
    public void doToggleSelectionMode() {
        // bSelectionMode = !bSelectionMode
        if (bSelectionMode == true) {
            bSelectionMode = false;
        } else {
            bSelectionMode = true;
        }

        // generate a time event to make sure the image on the button toggles
        // this sometimes sets the selection begin time to 0
        //setMediaTime(getMediaTime());
        modePanel.updateSelectionMode(bSelectionMode);//??
        getModePanel().revalidate();
    }

    /**
     * When main time is begintime, main time is set to endtime (of selection)
     * When main time is endtime, main time is set to begintime (of selection)
     */
    public void toggleActiveSelectionBoundary() {
        // bBeginBoundaryActive = !bBeginBoundaryActive
        if (bBeginBoundaryActive == true) {
            bBeginBoundaryActive = false;
        } else {
            bBeginBoundaryActive = true;
        }

        // otherwise the button image is not always updated immediately
        if (!playerIsPlaying()) {
            setMediaTime(getMediaTime());
        }
    }

    /**
     * Returns whether the selection begin boundary is active.
     *
     * @return whether the selection begin boundary is active
     */
    public boolean isBeginBoundaryActive() {
        return bBeginBoundaryActive;
    }

    /**
     * Starts a new play selection in a loop thread, after stopping the current
     * one  (if necessary)
     *
     * @param begin selection begintime
     * @param end selection endtime
     */
    public void startLoop(long begin, long end) {
        // stop current loop if necessary
        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
            //??
            /*
            try {
            	loopThread.join(500);
            } catch (InterruptedException ie) {
            	
            }*/
        }

        loopThread = new LoopThread(begin, end);
        loopThread.start();
    }

    /**
     * Stops the current loop thread, if active.
     */
    public void stopLoop() {
        setPlaySelectionMode(false);

        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
        }
    }
    
    // step and repeat mode methods
    public void setStepAndRepeatMode(boolean mode) {
    	if (stepAndRepeatMode == mode) {
    		return;
    	} else if (stepAndRepeatMode) {
    		stepAndRepeatMode = false;
    		if (stepThread != null) {
    			try {
    				stepThread.interrupt();
    			} catch (Exception ie) {
    				ie.printStackTrace();
    			}
    		}
    		playButtonsPanel.setEnabled(true);
    		selectionButtonsPanel.setEnabled(true);
    		stepAndRepeatPanel.setPlayIcon(true);
    	} else {
    		playButtonsPanel.setEnabled(false);
    		selectionButtonsPanel.setEnabled(false);
    		stepAndRepeatPanel.setPlayIcon(false);
	    	this.stepAndRepeatMode = mode;

	    	// stop player, stop play selection, play selectionmode = false
	    	playingSelection = false;
	    	bLoopMode = false;
	    	stepThread = new StepAndRepeatThread();
	    	stepThread.start();
    	}
    }
    
    public boolean isStepAndRepeatMode() {
    	return stepAndRepeatMode;
    }

    /**
     * Adjust volume and rate.
     */
	public void preferencesChanged() {
		Float volume = (Float) getPreference("MediaControlVolume", 
				vm.getTranscription());
		if (volume != null) {
			setVolume(volume.floatValue());
		}
		Float rate = (Float) getPreference("MediaControlRate", 
				vm.getTranscription());
		if (rate != null) {
			setRate(rate.floatValue());
		}
	}
	
    /**
     * Starts a new playing thread when loopmode is true
     */
    private class LoopThread extends Thread {
        private long beginTime;
        private long endTime;
        private boolean stopLoop = false;

        /**
         * Creates a new LoopThread instance
         *
         * @param begin the interval begin time
         * @param end the interval endtime
         */
        LoopThread(long begin, long end) {
            this.beginTime = begin;
            this.endTime = end;
        }

        /**
         * Sets the flag that indicates that the loop thread should stop to
         * true.
         */
        public void stopLoop() {
            stopLoop = true;
        }

        /**
         * Restarts the player to play the interval as long as the controller
         * is in  loop mode and the loop is not explicitly stopped.
         */
        public void run() {
            while (!stopLoop && getLoopMode()) {
                if (!playerIsPlaying()) {
                    playInterval(beginTime, endTime);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
                }

                while (playerIsPlaying() == true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                    if (stopLoop) {
                    	return;
                    }
                }

                try {
                    Thread.sleep(getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * A thread that plays an interval of duration t n times and then shifts the interval forward with
     * a step size s.
     * 
     * @author Han Sloetjes
     */
    class StepAndRepeatThread extends Thread {
    	private long interval = 2000;
    	private long repeats = 3;// number of repeat, or total number of times each interval is played
    	private long step = 1000;
    	private long pauseBetweenLoops = 500;
    	private long begin, end;
    	private long ultimateEnd;
    	private long count = 0;// count from 0 or 1?
    	
    	/**
		 * Constructor, initializes fields based on settings stored in the step-and-repeat panel.
		 */
		public StepAndRepeatThread() {
			super();
			
			if (stepAndRepeatPanel.getBeginTime() < 0) {
				begin = getMediaTime();
			} else {
				begin = stepAndRepeatPanel.getBeginTime();
			}
			if (begin == getMediaDuration()) {
				begin = 0;//?? restart from begin?
			}
			interval = stepAndRepeatPanel.getIntervalDuration();
			end = begin + interval;
			repeats = stepAndRepeatPanel.getNumRepeats();
			step = stepAndRepeatPanel.getStepSize();
			pauseBetweenLoops = stepAndRepeatPanel.getPauseDuration();
			
			if (stepAndRepeatPanel.getEndTime() <= 0) {
				ultimateEnd = getMediaDuration();
			} else {
				ultimateEnd = stepAndRepeatPanel.getEndTime();
			    if (ultimateEnd < begin + interval) {
			    	ultimateEnd = begin + interval;// or change the interval?
			    	if (ultimateEnd > getMediaDuration()) {
			    		ultimateEnd = getMediaDuration();
			    		interval = ultimateEnd - begin;
			    	}
			    }
			}
		}


		public void run() {
			
			if (!playerIsPlaying()) {
				playInterval(begin, end);
				// wait until playing is started
				while (!playerIsPlaying()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						return;
					}
				}
			}
			//System.out.println("Start playing at " + begin);
			
    		while (!isInterrupted()) {
    			if (!playerIsPlaying()) {
    				if (isInterrupted()) {
    					return;
    				}
    				//System.out.println("Playing interval at " + begin + " count: " + count);
    				playInterval(begin, end);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
    			}
    			
                while (playerIsPlaying()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                    if (isInterrupted()) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                }
                
                //System.out.println("Playing at end of interval " + end + " count: " + count);
                try {
                    Thread.sleep(pauseBetweenLoops);
                } catch (Exception ex) {
                	break;
                }
                
                count++;
                if (count == repeats) {
                	begin += step;// check media duration
                	if (begin >= ultimateEnd) {
                		break;
                	}
                	end += step;// check media duration
                	if (end > ultimateEnd) {
                		end = ultimateEnd;
                	} else if (ultimateEnd - end < step) {
                		end = ultimateEnd;
                	}
                	// if the remaining interval is too short, break
                	if (end - begin < 100) {
                		break;
                	}
                	count = 0;                	
                }
    		}
    		
    		ElanMediaPlayerController.this.setStepAndRepeatMode(false);
    	}// end run
    		
    }// end StepAndRepeatThread class
    
}
//end of ElanMediaPlayerController
