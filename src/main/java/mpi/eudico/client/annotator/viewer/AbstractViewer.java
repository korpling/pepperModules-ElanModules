package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.ActiveAnnotation;
import mpi.eudico.client.annotator.ActiveAnnotationUser;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.MediaPlayerUser;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesUser;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.SelectionUser;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import javax.swing.JComponent;


/**
 *
 */
public abstract class AbstractViewer extends JComponent
    implements ControllerListener, MediaPlayerUser, SelectionUser,
        ActiveAnnotationUser, ElanLocaleListener, Viewer,
        PreferencesUser {
    private ViewerManager2 viewerManager;
    private ElanMediaPlayer player;
    private Selection selection;
    private ActiveAnnotation activeAnnotation;

    // ControllerListener methods

    /*
     * Notification for a ControllerListener that a media related event happened.
     * This method is called by a separate thread for each event. Therefore
     * the actual implementation of this method might need to take care of
     * problems caused by more than one thread being active in the
     * controllerUpdate method. There are 3 options:
     *
     *        1. do nothing
     *
     *        2. make the method synchronized:
     *                public synchronized void controllerUpdate(ControllerEvent e) {
     *
     *        3. discard events that come while another is being handled:
     *                public void controllerUpdate(ControllerEvent e) {
     *                     if (handlingEvent) {
     *                        return;
     *                    }
     *                    handlingEvent = true;
     *
     *                    .... DO YOUR THING HERE
     *
     *
     *                    handlingEvent = false;
     *                }
     *
     *
     */
    public abstract void controllerUpdate(ControllerEvent event);

    // viewer manager
    public void setViewerManager(ViewerManager2 viewerManager) {
        this.viewerManager = viewerManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ViewerManager2 getViewerManager() {
        return viewerManager;
    }

    /**
     * This is a wrapper around the controllerUpdate method to make sure that
     * all viewers implement it synchronized. Maybe for some viewers it might
     * be a better solution to discard events instead of synchronizing on them
     * but this is the prefered soultion, a viewer must in principle be fast
     * enough to handle all the events it gets.
     *
     * @param player DOCUMENT ME!
     */

    //    public synchronized void synchronizedControllerUpdate(ControllerEvent event) {
    //		controllerUpdate(event);
    //	}
    // MediaPlayerUser methods

    /**
     * Set the player that receives all the player commands from this viewer
     *
     * @param player DOCUMENT ME!
     */
    public void setPlayer(ElanMediaPlayer player) {
        this.player = player;
    }

    /**
     * start the master player
     */
    public void startPlayer() {
        if (player == null) {
            return;
        }

        player.start();
    }

    /**
     * Play between a start and a stop time
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    public void playInterval(long startTime, long stopTime) {
        if (player == null) {
            return;
        }

        player.playInterval(startTime, stopTime);
    }

    /**
     * stop the master player
     */
    public void stopPlayer() {
        if (player == null) {
            return;
        }

        player.stop();
        viewerManager.getMediaPlayerController().stopLoop();
    }

    /**
     * returns a boolean that tells if the player is playing
     *
     * @return DOCUMENT ME!
     */
    public boolean playerIsPlaying() {
        if (player == null) {
            return false;
        }

        return player.isPlaying();
    }

    /**
     * Set the player media time in milli seconds
     *
     * @param milliSeconds DOCUMENT ME!
     */
    public void setMediaTime(long milliSeconds) {
        if (player == null) {
            return;
        }

        player.setMediaTime(milliSeconds);
        viewerManager.getMediaPlayerController().stopLoop();
    }

    /**
     * Get the player media time in milli seconds
     *
     * @return DOCUMENT ME!
     */
    public long getMediaTime() {
        if (player == null) {
            return 0;
        }

        return player.getMediaTime();
    }

    /**
     * Set the media player playing rate
     *
     * @param rate DOCUMENT ME!
     */
    public void setRate(float rate) {
        if (player == null) {
            return;
        }

        player.setRate(rate);
    }

    /**
     * Get the media player playing rate
     *
     * @return DOCUMENT ME!
     */
    public float getRate() {
        if (player == null) {
            return 0;
        }

        return player.getRate();
    }

    /**
     * Get the media duration in milli seconds
     *
     * @return DOCUMENT ME!
     */
    public long getMediaDuration() {
        if (player == null) {
            return 0;
        }

        return player.getMediaDuration();
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return DOCUMENT ME!
     */
    public float getVolume() {
        if (player == null) {
            return 0;
        }

        return player.getVolume();
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @param level DOCUMENT ME!
     */
    public void setVolume(float level) {
        if (player == null) {
            return;
        }

        player.setVolume(level);
    }

    // SelectionUser methods

    /*
     * Set the Selection object that contains the selection for this Viewer
     */
    public void setSelectionObject(Selection selection) {
        this.selection = selection;
    }

    /*
     * Set the selection begin and end time in milli seconds
     */
    public void setSelection(long begin, long end) {
        if (selection == null) {
            return;
        }

        if (begin == end) {
        	selection.setSelection(begin, end);
        	return;
        }
        
        Tier constrainingTier = null;

        if (getActiveAnnotation() != null) {
            constrainingTier = getActiveAnnotation().getTier();
        } else {
        	if(getViewerManager().getMultiTierControlPanel() != null){
            constrainingTier = getViewerManager().getMultiTierControlPanel()
                                   .getActiveTier();
        	}
        }

        if (constrainingTier != null) {
            if (((TierImpl) constrainingTier).getLinguisticType()
                     .hasConstraints()) {
                Constraint c = ((TierImpl) constrainingTier).getLinguisticType()
                                .getConstraints();
                long[] segment = { begin, end };
				
				Tier parent = ((TierImpl) constrainingTier).getParentTier();
				if (getActiveAnnotation() == null || 
					! (getActiveAnnotation() instanceof AlignableAnnotation)) {					                
	                c.forceTimes(segment, parent);	
				} else {
					Annotation pa = getActiveAnnotation().getParentAnnotation();
					if (pa != null && pa instanceof AlignableAnnotation) {
						segment[0] = begin < pa.getBeginTimeBoundary() ? pa.getBeginTimeBoundary() : begin;
						segment[1] = end > pa.getEndTimeBoundary() ? pa.getEndTimeBoundary() : end;
					} else {
						c.forceTimes(segment, parent);
					}
				}
				begin = segment[0];
				end = segment[1];
            }
        }

        selection.setSelection(begin, end);
    }
    
    public void isClosing(){
    	
    }

    /*
     * Set the selection to the boundaries of the annotation
     */
    public void setSelection(Annotation annotation) {
        if (selection == null) {
            return;
        }

        selection.setSelection(annotation);
    }
    
    /*
     * get the selection begin time in milli seconds
     */
    public long getSelectionBeginTime() {
        if (selection == null) {
            return 0;
        }

        return selection.getBeginTime();
    }

    /*
     * get the selection end time in milli seconds
     */
    public long getSelectionEndTime() {
        if (selection == null) {
            return 0;
        }

        return selection.getEndTime();
    }

    /*
     * Called when the selection is changed
     * The viewer must implement this method and take action to
     * update the selection in its view
     */
    public abstract void updateSelection();

    // ActiveAnnotationUser related methods
    public void setActiveAnnotationObject(ActiveAnnotation activeAnnotation) {
        this.activeAnnotation = activeAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation getActiveAnnotation() {
        if (activeAnnotation == null) {
            return null;
        }

        return activeAnnotation.getAnnotation();
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotation DOCUMENT ME!
     */
    public void setActiveAnnotation(final Annotation annotation) {
        if (activeAnnotation == null) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(getViewerManager()
                                                         .getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
        c.execute(getViewerManager(), new Object[] { annotation });
    }

    /**
     * Abstract method to be implemented by all abstract viewers. This is not
     * nice because not all viewers are meant to render Annotation related
     * data, for example SignalViewer, and therefore should not be bothered
     * with this update call. Maybe we can make for this (and other abstract
     * methods) an empy implementation. Thereby we no longer force the viewer
     * that extends AbstractViewer to implement the method.
     */
    public abstract void updateActiveAnnotation();

    /*
     * Called when the locale is changed
     * The viewer must implement this method and take action to
     * update the locale
     */
    public abstract void updateLocale();

	/**
	 * @see mpi.eudico.client.annotator.PreferencesUser#setPreference(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}		
	}
	
	/**
	 * Returns the stored preference for the specified key, or null.
	 * 
	 * @param key the key
	 * @param document the transcription
	 * @return the preference object or null
	 */
	protected Object getPreference(String key, Transcription document) {
		return Preferences.get(key, document);
	}

	/**
	 * @see mpi.eudico.client.annotator.PreferencesListener#preferencesChanged()
	 */
	public abstract void preferencesChanged();
}
