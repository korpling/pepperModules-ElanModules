/*
 * Created on Sep 22, 2003
 *
 *
 */
package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.grid.*;
import mpi.eudico.client.annotator.player.*;
import mpi.eudico.client.annotator.recognizer.gui.AudioRecognizerPanel;
import mpi.eudico.client.annotator.recognizer.gui.VideoRecognizerPanel;
import mpi.eudico.client.annotator.search.result.viewer.ElanResultViewer;
import mpi.eudico.client.annotator.svg.JMFSVGViewer;
import mpi.eudico.client.annotator.svg.GlassPaneSVGViewer;
import mpi.eudico.client.annotator.svg.QTSVGViewer;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionViewer;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.viewer.*;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.mediacontrol.TimeLineController;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditListener;

import mpi.eudico.server.corpora.util.ACMEditableDocument;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;

/**
 * A ViewerManager must manage the viewer world that is created around a
 * Transcription. It takes care of creating, destroying, enabling and
 * disabling viewers and media players ensuring that all connections between
 * controllers and listeners are as they should be.
 * @version Aug 2005 Identity removed
 */
public class ViewerManager2 {
	/** Holds value of property DOCUMENT ME! */
	private final static long SIGNAL_VIEWER_PERIOD = 50; // make these values setable?

	/** Holds value of property DOCUMENT ME! */
	private final static long TIME_LINE_VIEWER_PERIOD = 50;

	/** Holds value of property DOCUMENT ME! */
	private final static long INTERLINEAR_VIEWER_PERIOD = 100;

	/** Holds value of property DOCUMENT ME! */
	private final static long MEDIA_CONTROL_PANEL_PERIOD = 100;
	private ElanMediaPlayer masterMediaPlayer;
	private ElanMediaPlayer signalSourcePlayer;
	private SignalViewer signalViewer;
	private AudioRecognizerPanel audioRecognizerPanel;
	private VideoRecognizerPanel videoRecognizerPanel;
	private Transcription transcription;
	private Selection selection;
	private TimeScale timeScale;
	private ActiveAnnotation activeAnnotation;
	private TierOrder tierOrder;
	private ElanMediaPlayerController mediaPlayerController;
	private AnnotationDensityViewer annotationDensityViewer;
	private MediaPlayerControlSlider mediaPlayerControlSlider;
	private TimePanel timePanel;
	private MultiTierControlPanel multiTierControlPanel;
	private Vector slaveMediaPlayers;
	private Vector disabledMediaPlayers;
	private Hashtable controllers;
	private Vector viewers;
	private Vector enabledViewers;
	private Vector disabledViewers;
	private Tier emptyTier;
	private MetadataViewer metadataViewer;
	private Map<AbstractViewer, GestureDispatcher> gestureMap;//use interface for dispatcher
	
	private GridViewer gridViewer;
	private TimeLineViewer timeLineViewer;
	private TextViewer textViewer;
	private LexiconEntryViewer lexiconViewer;
	private Vector<SubtitleViewer> subtitleViewers;	
	private InterlinearViewer interlinearViewer;	
	private TranscriptionViewer transcriptionViewer;	
	private String signalMediaURL;
	private ArrayList<String> audioPaths;
	private ArrayList<String> videoPaths;
	
	/** The maximal number of video players in Elan */
	public static final int MAX_NUM_VIDEO_PLAYERS = 2; // will be 4
	/** The maximal number of audio players in Elan */
	public static final int MAX_NUM_AUDIO_PLAYERS = 1;

	/**
	 * Create a ViewerManager for a specific Transcription
	 *
	 * @param transcription the Transcription used in this ViewerManagers
	 *        universe
	 */
	public ViewerManager2(Transcription transcription) {
		this.transcription = transcription;

		// as long as no real media player is set as master player use
		// an empty media player.
		masterMediaPlayer = new EmptyMediaPlayer(Integer.MAX_VALUE);

		// observables for this viewer universe
		selection = new Selection();
		timeScale = new TimeScale();
		activeAnnotation = new ActiveAnnotation();
		
		createTierOrderObject();
		//tierOrder = new TierOrder();

		// administration objects
		slaveMediaPlayers = new Vector();
		disabledMediaPlayers = new Vector();
		controllers = new Hashtable();
		viewers = new Vector();
		subtitleViewers = new Vector<SubtitleViewer>();
		enabledViewers = new Vector();
		disabledViewers = new Vector();
		gestureMap = new HashMap<AbstractViewer, GestureDispatcher>();
		
		audioPaths = new ArrayList<String>();
		videoPaths  = new ArrayList<String>();
		
		try {
			//        emptyTier = new DobesTier(null, null, null, null);
			emptyTier = new TierImpl(null, null, null, null, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createTierOrderObject(){	
		tierOrder = new TierOrder(transcription);
		connectListener(tierOrder);
		List tiers = transcription.getTiers();
		List tierOrderList = (List) Preferences.get("MultiTierViewer.TierOrder", 
			transcription);
		
		if (tierOrderList != null) {				
			// add (new) tiers, tiers that are not in the preferences
			Tier t;
			for (int i = 0; i < tierOrderList.size(); i++) {
				t = transcription.getTierWithId((String) tierOrderList.get(i));					
				if ( t == null ) {
					tierOrderList.remove(i);
					i--;
				}					
			}	
			
			
			for (int i = 0; i < tiers.size(); i++) {
				t = (Tier) tiers.get(i);					
				if ( !tierOrderList.contains(t.getName()) ) {
					tierOrderList.add(t.getName()); 
				}					
			}				
		} else{
			tierOrderList = new ArrayList();
			for(int i=0;  i < tiers.size(); i++){
				tierOrderList.add(((TierImpl)tiers.get(i)).getName());
			}				
		}
		
		if(tierOrderList instanceof ArrayList){
			tierOrder.setTierOrder(tierOrderList);
		} else {
			tierOrder.setTierOrder(new ArrayList(tierOrderList));
		}			
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the Transcription object for this viewer universe
	 */
	public Transcription getTranscription() {
		return transcription;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the Selection object for this viewer universe
	 */
	public Selection getSelection() {
		return selection;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the TimeScale object for this viewer universe
	 */
	public TimeScale getTimeScale() {
		return timeScale;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the ActiveAnnotation object for this viewer universe
	 */
	public ActiveAnnotation getActiveAnnotation() {
		return activeAnnotation;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return the ActiveAnnotation object for this viewer universe
	 */
	public TierOrder getTierOrder() {
		return tierOrder;
	}


	/**
	 * Makes an ElanMediaPlayer master media player. The current master media
	 * player becomes a slave from the new master. The old master media player
	 * should be destroyed separately if it is no longer needed.
	 *
	 * @param player the ElanMediaPlayer that must become master player
	 */
	public void setMasterMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		// remember the volume level of the current master
		float volume = masterMediaPlayer.getVolume();
		float rate = masterMediaPlayer.getRate();

		// make sure all current master media player connections are removed
		// disconnect slave players
		for (int i = 0; i < slaveMediaPlayers.size(); i++) {
			masterMediaPlayer.removeController((Controller) slaveMediaPlayers.elementAt(i));
		}

		// disconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Enumeration en = controllers.elements(); en.hasMoreElements();) {
			masterMediaPlayer.removeController((Controller) en.nextElement());
		}

		// remove the new master player from the slave or disabled list
		// and add the current master player to the slave list
		slaveMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		slaveMediaPlayers.add(masterMediaPlayer);

		// set the master
		masterMediaPlayer = player;

		// connect the new master media player to the viewer universe
		// reconnect slave players
		for (int i = 0; i < slaveMediaPlayers.size(); i++) {
			masterMediaPlayer.addController((Controller) slaveMediaPlayers.elementAt(i));
		}

		// reconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Enumeration en = controllers.elements(); en.hasMoreElements();) {
			masterMediaPlayer.addController((Controller) en.nextElement());
		}

		// set the player in all existing viewers
		for (Enumeration en = viewers.elements(); en.hasMoreElements();) {
			((AbstractViewer) en.nextElement()).setPlayer(masterMediaPlayer);
		}

		// set the volume level
		masterMediaPlayer.setVolume(volume);
		masterMediaPlayer.setRate(rate);

		for (int i = 0; i < slaveMediaPlayers.size(); i++) {
			((ElanMediaPlayer) slaveMediaPlayers.elementAt(i)).setVolume(0);
		}
	}

	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor)
		throws NoPlayerException {
		// ask the player factory to create a player
		ElanMediaPlayer player = PlayerFactory.createElanMediaPlayer(mediaDescriptor);

		if (player == null) {
			return null;
		}

		ElanLocale.addElanLocaleListener(transcription, player);
		player.setRate(masterMediaPlayer.getRate());
		player.setVolume(0);

		// connect it to the master media player
		masterMediaPlayer.addController(player);

		// update the adminstration
		slaveMediaPlayers.add(player);

		//		if (isSignalImageSource) {
		//			signalSourcePlayer = player;
		//		}
		return player;
	}
	
	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player.
	 * It first tries to create a player of the preferred type; if this fails 
	 * it will try to create a player the default way. 
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 * @param preferredMediaFramework the preferred media framework
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor, 
		String preferredMediaFramework) throws NoPlayerException {
		if (preferredMediaFramework == null) {
			return createMediaPlayer(mediaDescriptor);
		}
		// ask the player factory to create a player
		ElanMediaPlayer player = null;
		StringBuilder errors = new StringBuilder();
		try {
			if (preferredMediaFramework.equals(PlayerFactory.QT_MEDIA_FRAMEWORK)) {
				player = PlayerFactory.createQTMediaPlayer(mediaDescriptor);
			} else if (preferredMediaFramework.equals(PlayerFactory.JMF_MEDIA_FRAMEWORK)) {
				player = PlayerFactory.createJMFMediaPlayer(mediaDescriptor);
			} else if (preferredMediaFramework.equals(
				PlayerFactory.NATIVE_WINDOWS_MEDIA_FRAMEWORK)) {
				player = PlayerFactory.createNativeMediaPlayerDS(mediaDescriptor);
			} else if (preferredMediaFramework.equals(PlayerFactory.COCOA_QT)) {
				try {
					player = PlayerFactory.createCocoaQTMediaPlayer(mediaDescriptor);
				} catch (Exception ex) {
					errors.append(ex.getMessage() + "\n");
					player = PlayerFactory.createQTMediaPlayer(mediaDescriptor);
				}
			} else {
				try {
					return createMediaPlayer(mediaDescriptor); //default
				} catch (NoPlayerException np) {
					// then player is null
					errors.append(np.getMessage() + "\n");
				}
			}
		} catch (NoPlayerException npe) {
			errors.append(npe.getMessage() + "\n");
			try {
				return createMediaPlayer(mediaDescriptor);
			} catch (NoPlayerException np) {
				errors.append(np.getMessage() + "\n");
			}
		}
		

		if (player == null) {
			throw new NoPlayerException(errors.toString());
			//return null;
		}

		ElanLocale.addElanLocaleListener(transcription, player);
		player.setRate(masterMediaPlayer.getRate());
		player.setVolume(0);

		// connect it to the master media player
		masterMediaPlayer.addController(player);

		// update the adminstration
		slaveMediaPlayers.add(player);

		//		if (isSignalImageSource) {
		//			signalSourcePlayer = player;
		//		}
		return player;
	}

	/**
	 * Adds a custom made Elan media player to the list of slave media players 
	 * and connects it to the master media player.
	 * 
	 * @see #destroyMediaPlayer(ElanMediaPlayer)
	 * @param player the palyer to add
	 */
	public void addMediaPlayer(ElanMediaPlayer player) {
		if (player == null || slaveMediaPlayers.contains(player) ||
				player == masterMediaPlayer) {
			return;
		}

		ElanLocale.addElanLocaleListener(transcription, player);
		player.setRate(masterMediaPlayer.getRate());
		player.setVolume(0);

		// connect it to the master media player
		masterMediaPlayer.addController(player);

		// update the adminstration
		slaveMediaPlayers.add(player);	
	}
	
	/**
	 * Removes an ElanMediaPlayer from this viewer universe. Nothing will be
	 * done if an attempt is made to remove the master media player
	 *
	 * @param player the ElanMediaPlayer that must be destroyed
	 */
	public void destroyMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		// disconnect the player from the master player
		masterMediaPlayer.removeController(player);
		//player.cleanUpOnClose();

		// update the administration, the player is in one of two vectors
		slaveMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		
		player = null;
	}

	/**
	 * Enables an ElanMediaPlayer that was previously disabled
	 *
	 * @param player the ElanMediaPlayer that must be enabled.
	 */
	public void enableMediaPlayer(ElanMediaPlayer player) {
		// only enable a player that is a disabled player
		if (disabledMediaPlayers.contains(player)) {
			// reconnect the player to the master player
			masterMediaPlayer.addController(player);

			// update the administration
			slaveMediaPlayers.add(player);
			disabledMediaPlayers.remove(player);
		}
	}

	/**
	 * Temporarily disconnects the player from the master media player. It can
	 * be reconnected by calling enableElanMediaPlayer The master media player
	 * will not be disabled.
	 *
	 * @param player the ElanMediaPlayer that must be disabled.
	 */
	public void disableMediaPlayer(ElanMediaPlayer player) {
		// only disable a player that is a slave player
		if (slaveMediaPlayers.contains(player)) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController(player);

			// update the administration
			slaveMediaPlayers.remove(player);
			disabledMediaPlayers.add(player);
		}
	}

	/**
	 * Enable all players except the master player.
	 */
	public void enableDisabledMediaPlayers() {
		Enumeration en = disabledMediaPlayers.elements();

		while (en.hasMoreElements()) {
			// reconnect the player to the master player
			masterMediaPlayer.addController((ElanMediaPlayer) en.nextElement());
		}

		// update the administration
		slaveMediaPlayers.addAll(disabledMediaPlayers);
		disabledMediaPlayers.clear();
	}

	/**
	 * Disable all players except the master player.
	 */
	public void disableSlaveMediaPlayers() {
		Enumeration en = slaveMediaPlayers.elements();

		while (en.hasMoreElements()) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController((ElanMediaPlayer) en.nextElement());
		}

		// update the administration
		disabledMediaPlayers.addAll(slaveMediaPlayers);
		slaveMediaPlayers.clear();
	}

	// this must be called from the outside, maybe viewer manager can derive
	// the signalSourcePlayer implicitly. The sugnal source player is an mpeg
	// or wav player that renders the audio for the wav data that is used in the signal viewer
	public void setSignalSourcePlayer(ElanMediaPlayer player) {
		signalSourcePlayer = player;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public long getSignalViewerOffset() {
		long offset = 0;

		if (signalSourcePlayer != null) {
			offset = signalSourcePlayer.getOffset();
		}

		return offset;
	}

	/* ALBERT
	 */
	public SignalViewer getSignalViewer() {
		return signalViewer;
	}
	
	/* END ALBERT
	 */
	/**
	 * DOCUMENT ME!
	 *
	 * @param player DOCUMENT ME!
	 * @param offset DOCUMENT ME!
	 */
	public void setOffset(ElanMediaPlayer player, long offset) {
		player.setOffset(offset);

		if ((player == signalSourcePlayer) && (signalViewer != null)) {
			signalViewer.setOffset(offset);
		}
		transcription.setChanged();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the ElanMediaPlayer that is the current master media player.
	 */
	public ElanMediaPlayer getMasterMediaPlayer() {
		return masterMediaPlayer;
	}
	
	/**
	 * Returns the collection of slave mediaplayers.
	 * 
	 * @return the collection of slave mediaplayers
	 */
	public Vector getSlaveMediaPlayers() {
		return slaveMediaPlayers;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the controlpanel for the master media player
	 */
	public ElanMediaPlayerController getMediaPlayerController() {
		if (mediaPlayerController == null) {
			mediaPlayerController = new ElanMediaPlayerController(this);

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerController, controller);
			connect(mediaPlayerController);
			viewers.add(mediaPlayerController);
			enabledViewers.add(mediaPlayerController);
		}

		return mediaPlayerController;
	}
	
	/**
	 * Remove the transcription Viewer
	 */
	public void destroyElanMediaPlayerController() {
		if(mediaPlayerController != null){
			destroyViewer(mediaPlayerController);
			mediaPlayerController = null;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public MediaPlayerControlSlider getMediaPlayerControlSlider() {
		if (mediaPlayerControlSlider == null) {
			mediaPlayerControlSlider = new MediaPlayerControlSlider();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerControlSlider, controller);
			connect(mediaPlayerControlSlider);
			viewers.add(mediaPlayerControlSlider);
			enabledViewers.add(mediaPlayerControlSlider);
		}

		return mediaPlayerControlSlider;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public AnnotationDensityViewer getAnnotationDensityViewer() {
		if (annotationDensityViewer == null) {
			annotationDensityViewer = new AnnotationDensityViewer(transcription);
			
			annotationDensityViewer.setTierOrderObject(tierOrder);
			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(annotationDensityViewer, controller);
			connect(annotationDensityViewer);
			viewers.add(annotationDensityViewer);
			enabledViewers.add(annotationDensityViewer);
		}

		return annotationDensityViewer;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public TimePanel getTimePanel() {
		if (timePanel == null) {
			timePanel = new TimePanel();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(timePanel, controller);
			connect(timePanel);
			viewers.add(timePanel);
			enabledViewers.add(timePanel);
		}

		return timePanel;
	}
	
	/**
	 * Creates a Viewer for the specified fully qualified class name.
	 * 
	 * @param className the class name
	 * @param controllerPeriod the requested period for controller updates
	 * 
	 * @return the viewer
	 */
	public Viewer createViewer(String className, long controllerPeriod) {
	    Viewer viewer = null; 
	    
	    try {
	        viewer = (Viewer) Class.forName(className).newInstance();
	        viewer.setViewerManager(this);
	        if (viewer instanceof AbstractViewer) {
		    		PeriodicUpdateController controller = getControllerForPeriod(controllerPeriod);
		    		controllers.put(viewer, controller);
		    		connect((AbstractViewer) viewer);
	
		    		viewers.add(viewer);
		    		enabledViewers.add(viewer);
	        } else if (viewer instanceof ControllerListener) {
	            getControllerForPeriod(controllerPeriod).addControllerListener(
	                    (ControllerListener) viewer);
	        } else {
	            // special case for syntax viewer?
	            /*
	            try {
	                Method method = viewer.getClass().getDeclaredMethod("getControllerListener", null);
	                ControllerListener listener = (ControllerListener) method.invoke(viewer, null);
	                getControllerForPeriod(controllerPeriod).addControllerListener(
	                        listener);	                
	            } catch (Exception e){
	                System.out.println("Could not connect controller: " + e.getMessage());
	            }
	            */
	        }
	    } catch (Exception e) {
	        System.out.println("Could not create viewer: " + className + ": " + e.getMessage());
	    }
	    
	    return viewer;
	}
	
	/**
	 * Connection method to be used by external objects that want to connect a
	 * listener
	 *
	 * @param listener
	 */
	public void connectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).addControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.addSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.addActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.addTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, (PreferencesListener) listener);
		}
	}

	/**
	 * Connection method to be used by external objects that want to connect a
	 * listener
	 *
	 * @param listener
	 */
	public void disconnectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).removeControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.removeSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.removeActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.removeTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).removeACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, (PreferencesListener) listener);
		}
	}

	/**
	 * DOCUMENT ME! 
	 *
	 * @return the controlpanel for the Transcriptions tiers
	 */
	public MultiTierControlPanel getMultiTierControlPanel() {
		if(multiTierControlPanel == null){
			return createMultiTierControlPanel();
		}
		return multiTierControlPanel;
	}
	
	public MultiTierControlPanel createMultiTierControlPanel(){
		if(multiTierControlPanel == null){
			multiTierControlPanel = new MultiTierControlPanel(transcription, tierOrder);
			//multiTierControlPanel.setTierOrderObject(tierOrder);
			ElanLocale.addElanLocaleListener(transcription, multiTierControlPanel);
			Preferences.addPreferencesListener(transcription, multiTierControlPanel);
		}
		
		return multiTierControlPanel;
	}
	
	public void destroyMultiTierControlPanel(){
		if(multiTierControlPanel != null){
			Preferences.removePreferencesListener(transcription,multiTierControlPanel);
			ElanLocale.removeElanLocaleListener(multiTierControlPanel);
			multiTierControlPanel = null;
		}
	}

	/**
	 * Creates a TimeLineViewer that is connected to the Viewer universe.
	 *
	 * @return the TimeLineViewer for the Transcrption in this ViewerManager
	 */
	public TimeLineViewer createTimeLineViewer() {
		timeLineViewer = new TimeLineViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(timeLineViewer, controller);
		connect(timeLineViewer);

		viewers.add(timeLineViewer);
		enabledViewers.add(timeLineViewer);

		return timeLineViewer;
	}

	/**
	 * Creates a TimeLineViewer that is connected to the Viewer universe.
	 *
	 * @return the TimeLineViewer for the Transcrption in this ViewerManager, can be null
	 */
	public InterlinearViewer createInterlinearViewer() {
		Object val = Preferences.get(ELANCommandFactory.INTERLINEAR_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(interlinearViewer == null){				
	    		interlinearViewer = new InterlinearViewer(transcription);
	    		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
	    		controllers.put(interlinearViewer, controller);
	    		connect(interlinearViewer);

	    		viewers.add(interlinearViewer);
	    		enabledViewers.add(interlinearViewer);
			}
	    	return interlinearViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 *Returns the interlinear viewer, can be null
	 *
	 * @return the InterlinearViewer, can be null
	 */
	public InterlinearViewer getInterlinearViewer() {		
		return interlinearViewer;
	}	
	
	
	public String getSignalMediaURL(){
		return signalMediaURL;
	}
	
	/**Set the paths the audio, if linked
	 * 
	 * @return audioPath the path of the audio
	 */
	public void setAudioPaths(ArrayList<String> audioPath){
		audioPaths.clear();
		if(audioPath != null){
			for(int i=0; i< audioPath.size(); i++){
				String path = audioPath.get(i);
				if (path.startsWith("file:")) {
	    			path = path.substring(5);
	    		}	
				if(!audioPaths.contains(path)){
					audioPaths.add(path);
				}				
			}
		}
	}
	
	/**Returns the paths the audio, if linked. Can be null
	 * 
	 * @return audioPaths the path of the audio, can be null
	 */
	public ArrayList<String> getAudioPaths(){
		return audioPaths;
	}
	
	/**
	 * Set the paths the video, if linked
	 * 
	 * @param videoPath
	 */	 
	public void setVideoPaths(ArrayList<String> videoPath){
		videoPaths.clear();
		if(videoPath != null){
			for(int i=0; i< videoPath.size(); i++){
				String path = videoPath.get(i);
				if (path.startsWith("file:")) {
	    			path = path.substring(5);
	    		}	
				if(!videoPaths.contains(path)){
					videoPaths.add(path);
				}				
			}
		}
	}
	
	/**Returns the paths the video, if linked. Can be null
	 * 
	 * @return videoPaths the path of the audio, can be null
	 */
	public ArrayList<String> getVideoPaths(){
		return videoPaths;
	}
	
	public SignalViewer createSignalViewer() {
		Object val = Preferences.get(ELANCommandFactory.SIGNAL_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(signalViewer == null){
	    		if(signalMediaURL != null){
	    			createSignalViewer(signalMediaURL);
	    			signalViewer.setOffset(getSignalViewerOffset());
	    			signalViewer.preferencesChanged();
	    		}
	    	}	    	
	    	return signalViewer;
	    }else {
	    	return null;
	    }
	}
	

	/**
	 * Creates a SignalViewer that is connected to the Viewer universe.
	 *
	 * @param mediaURL String that represents the signal media URL
	 *
	 * @return the SignalViewer for the media URL
	 */
	public SignalViewer createSignalViewer(String mediaURL) { // throw exception ?
		SignalViewer viewer = null;
		if(mediaURL != null){
			signalMediaURL = mediaURL;
		}
	
		// URL or String  problem to be solved, is a problem for rtsp://
		// the SignalViewer dows not work with streaming (rtsp://)
		if (mediaURL.startsWith("rtsp://")) {
			return viewer; // == null
		}
		
		 Object val = Preferences.get(ELANCommandFactory.SIGNAL_VIEWER, null);
		 if (val!=null && val instanceof Boolean && !((Boolean)val)) {
			 return viewer;
		 }

		viewer = new SignalViewer(mediaURL);	
		
		PeriodicUpdateController controller = getControllerForPeriod(SIGNAL_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		// something to set the offset
		viewers.add(viewer);
		enabledViewers.add(viewer);
		signalViewer = viewer; // a problem when there is more than one signal viewer		

		return viewer;
	}	
	
	public TranscriptionViewer createTranscriptionViewer() {
		transcriptionViewer = new TranscriptionViewer(this);
		PeriodicUpdateController controller = getControllerForPeriod(this.MEDIA_CONTROL_PANEL_PERIOD);
		controllers.put(transcriptionViewer, controller);
		connect(transcriptionViewer);

		viewers.add(transcriptionViewer);
		enabledViewers.add(transcriptionViewer);

		return transcriptionViewer;
	}
	
	/**
	 *Returns the transcription viewer, can be null
	 *
	 * @return the transcriptionViewer, can be null
	 */
	public TranscriptionViewer getTranscriptionViewer() {		
		return transcriptionViewer;
	}	

	/**
	 * Creates a GridViewer that is connected to the Viewer universe but not
	 * yet connected to a certain Tier.
	 *
	 * @return the GridViewer, can return null
	 */
	public GridViewer createGridViewer() {
		Object val = Preferences.get(ELANCommandFactory.GRID_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(gridViewer == null){				
	    		gridViewer = new GridViewer();
				connect(gridViewer);
				viewers.add(gridViewer);
				enabledViewers.add(gridViewer);
			}
	    	return gridViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 *Returns the grid viewer, can be null
	 *
	 * @return the GridViewer, can be null
	 */
	public GridViewer getGridViewer() {		
		return gridViewer;
	}	
	

	/**
	* Creates a GridViewer that is connected to the Viewer universe but not
	* yet connected to a certain Tier.
	*
	* @return the GridViewer
	*/
	public ElanResultViewer createSearchResultViewer() {
		ElanResultViewer viewer = new ElanResultViewer();
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	    * Creates a SubtitleViewer that is connected to the Viewer universe but
	    * not yet connected to a certain Tier.
	    *
	    * @return the SubtitleViewer
	    */
	public SubtitleViewer createSubtitleViewer() {
		Object val = Preferences.get(ELANCommandFactory.SUBTITLE_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	SubtitleViewer subtitleViewer = new SubtitleViewer();
			connect(subtitleViewer);

			viewers.add(subtitleViewer);
			enabledViewers.add(subtitleViewer);
			
			subtitleViewers.add(subtitleViewer);
			
	    	return subtitleViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the subtitle viewer, can be null
	 *
	 * @return the SubtitleViewer, can be null
	 */
	public Vector<SubtitleViewer> getSubtitleViewers() {		
		return subtitleViewers;
	}

	/**
	 * Creates a TextViewer that is connected to the Viewer universe but not
	 * yet connected to a certain Tier.
	 *
	 * @return the TextViewer, can be null
	 */
	public TextViewer createTextViewer() {
		Object val = Preferences.get(ELANCommandFactory.TEXT_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(textViewer == null){				
	    		textViewer =  new TextViewer();
				connect(textViewer);
				viewers.add(textViewer);
				enabledViewers.add(textViewer);
			}
	    	return textViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the text viewer, can be null.
	 * 
	 * @return the textViewer, can be null
	 */
	public TextViewer getTextViewer() {		
		return textViewer;
	}

	/**
	 * Creates a GlassPaneSVGViewer that can be used as an transparent overlay  on
	 * lightweight components. Unfinished.
	 *
	 * @return DOCUMENT ME!
	 */
	public GlassPaneSVGViewer createSVGViewer() {
		GlassPaneSVGViewer viewer = new GlassPaneSVGViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	 * A JMF specific GlassPaneSVGViewer solution that uses the JMF plugin mechanism.<br>
	 * The viewer does not need controller updates, it paints on the frames of
	 * the player. This viewer should be created after the mastermediaplayer!
	 *
	 * @return a connected JMFSVGViewer that is connected to a custom
	 *         VideoRenderer
	 */
	public JMFSVGViewer createJMFSVGViewer() {
		if ((getMasterMediaPlayer() != null)
			&& getMasterMediaPlayer() instanceof JMFGraphicMediaPlayer) {
			if (((JMFGraphicMediaPlayer) getMasterMediaPlayer()).getRenderer() != null) {
				JMFSVGViewer viewer = new JMFSVGViewer(transcription);

				if (((JMFGraphicMediaPlayer) getMasterMediaPlayer()).connectViewer(viewer)) {
					connect(viewer);
					viewers.add(viewer); //??
					enabledViewers.add(viewer); //??
				}
				else {
					viewer = null;
				}

				return viewer;
			}
		}

		return null;
	}
	
	/**
	 * Creates a SVGViewer that creates QT SpriteTracks for tiers that have references 
	 * to graphic annotations and adds them to the movie.
	 * 
	 * @return a connected QTSVGViewer
	 */
	public QTSVGViewer createQTSVGViewer() {
		if ((getMasterMediaPlayer() != null)
					&& getMasterMediaPlayer() instanceof QTMediaPlayer) {
			if (((QTMediaPlayer)getMasterMediaPlayer()).getMovie() != null) {
				QTSVGViewer viewer = new QTSVGViewer(transcription);
				viewer.setMediaFileDimension(((QTMediaPlayer)getMasterMediaPlayer()).getMediaFileDimension());		
				viewer.setMovie(((QTMediaPlayer)getMasterMediaPlayer()).getMovie());
				((QTMediaPlayer)getMasterMediaPlayer()).setStopMode(
					QTMediaPlayer.STOP_WITH_STOP_TIME);
				connect(viewer);
				viewers.add(viewer); //??
				enabledViewers.add(viewer); //??
				
				return viewer;
			}
		}
		return null;
	}
	
	/**
	 * Creates a SegmentationViewer that is connected to the Viewer universe.
	 *
	 * @return a SegmentationViewer for the Transcription in this ViewerManager
	 */
	public SegmentationViewer2 createSegmentationViewer() {
		SegmentationViewer2 viewer = new SegmentationViewer2(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	 * Creates a TimeSeriesViewer that is connected to the Viewer universe.
	 * 
	 * @return a TimeSeriesViewer for the Transcription in this ViewerManager
	 */
	public TimeSeriesViewer createTimeSeriesViewer() {
		TimeSeriesViewer viewer = new TimeSeriesViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);
		return viewer;
	}	
	
	public void connectViewer(AbstractViewer viewer, boolean connect){
		if(viewer == null){
			return;
		}
		if(connect){
			if(viewer instanceof TimeSeriesViewer){
				controllers.put(viewer, getControllerForPeriod(TIME_LINE_VIEWER_PERIOD));
			}else if(viewer instanceof SignalViewer){
				controllers.put(viewer, getControllerForPeriod(SIGNAL_VIEWER_PERIOD));
			}
			connect(viewer);
			viewers.add(viewer);
			enabledViewers.add(viewer);
			disabledViewers.remove(viewer);
		} else {
			disconnect(viewer , false);
			enabledViewers.remove(viewer);
			disabledViewers.add(viewer);
		}
	}
	
	/**
	 * Creates and connects a metadata viewer.
	 * 
	 * @return the metadata viewer
	 */
	public MetadataViewer createMetadataViewer() {
		Object val = Preferences.get(ELANCommandFactory.METADATA_VIEWER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(metadataViewer == null){				
	    		metadataViewer = new MetadataViewer(this);
	    		ElanLocale.addElanLocaleListener(transcription, metadataViewer);
	    		Preferences.addPreferencesListener(transcription, metadataViewer);
				}
	    	return metadataViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the metadata viewer, can be null
	 * 
	 * @return the metadata viewer, can be null
	 */
	public MetadataViewer getMetadataViewer() {
		return metadataViewer;
	}
	
	/**
	 * Removes an MetaData from this viewer universe. 
	 * @param metadataViewer 
	  
	 */
	public void destroyMetaDataViewer() {
		if(metadataViewer != null){
			ElanLocale.removeElanLocaleListener(metadataViewer);
			Preferences.removePreferencesListener(transcription, metadataViewer);
			metadataViewer = null;
		}
	}
	
	/**
	 * Note: use media descriptor instead of path?
	 * Note: if there is nothing to connect this could also be done in e.g. ElanFrame
	 * @param mediaPath
	 * @return a panel for selection and configuration of an audio based recognizer 
	 */
	public AudioRecognizerPanel createAudioRecognizerPanel() {		
		Object val = Preferences.get(ELANCommandFactory.AUDIO_RECOGNIZER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(audioRecognizerPanel == null){
				if(audioPaths != null && audioPaths.size() > 0){
					audioRecognizerPanel = new AudioRecognizerPanel(this, audioPaths);
					
					// connect to anything??
					ElanLocale.addElanLocaleListener(transcription, audioRecognizerPanel);
				}
	    	}
	    	return audioRecognizerPanel;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the audio recognizer panel, can be null.
	 * 
	 * @return the audio recognizer panel, can be null
	 */
	public AudioRecognizerPanel getAudioRecognizerPanel() {
		return audioRecognizerPanel;
	}	
	
	/**
	 * Note: use media descriptor instead of paths?
	 * Note: if there is nothing to connect this could also be done in e.g. ElanFrame
	 *
	 * @return a panel for selection and configuration of an video based recognizer 
	 */
	public VideoRecognizerPanel createVideoRecognizerPanel() {
		Object val = Preferences.get(ELANCommandFactory.VIDEO_RECOGNIZER, null);
	    if ( val==null || (val instanceof Boolean && (Boolean)val)) {
	    	if(videoRecognizerPanel == null){
				if(videoPaths != null && videoPaths.size() > 0){
					videoRecognizerPanel = new VideoRecognizerPanel(this, videoPaths);					
					// connect to anything??
					ElanLocale.addElanLocaleListener(transcription, videoRecognizerPanel);
				}
	    	}
	    	return videoRecognizerPanel;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Creates and connects a LexiconEntryViewer.
	 * 
	 * @return a lexicon entry viewer
	 */
	public LexiconEntryViewer createLexiconEntryViewer() {
		lexiconViewer = new LexiconEntryViewer();
		
		connect(lexiconViewer);
		
		viewers.add(lexiconViewer);
		enabledViewers.add(lexiconViewer);

		return lexiconViewer;
	}
	
	/**
	 * Returns the lexicon viewer, can be null.
	 * 
	 * @return the lexicon viewer, can be null
	 */
	public LexiconEntryViewer getLexiconViewer() {
		return lexiconViewer;
	}
	
	public void destroyPanel(String panelName){
		if(panelName == null){
			return;
		}
		if (panelName.equals(ELANCommandFactory.VIDEO_RECOGNIZER)) {
			if(videoRecognizerPanel != null){
				ElanLocale.removeElanLocaleListener(videoRecognizerPanel);
				videoRecognizerPanel = null;
			}
		} else if (panelName.equals(ELANCommandFactory.AUDIO_RECOGNIZER)) {
			if(audioRecognizerPanel != null){
				ElanLocale.removeElanLocaleListener(audioRecognizerPanel);
				audioRecognizerPanel = null;
			}
        }
	}
	
	/**
	 * Returns the video recognizer panel, can be null.
	 * 
	 * @return the video recognizer panel, can be null
	 */
	public VideoRecognizerPanel getVideoRecognizerPanel() {
		return videoRecognizerPanel;
	}
	
	/**
	 * Registers (Tier-) time line controllers to viewer 
	 * @param viewer the viewer for which the controllers must be set
	 * @param tierNames array of Tier Names 
	 */
	public void setControllersForViewer(AbstractViewer viewer, String[] tierNames){
	    try{
	        Tier[] tiers = new Tier[tierNames.length];
	        for(int i=0; i<tierNames.length; i++){
	            tiers[i] = transcription.getTierWithId(tierNames[i]);
	        }
	        setControllersForViewer(viewer, tiers);
	    }
	    catch(Exception e){}
	}
	
	public void setTierForViewer(SingleTierViewer viewer, Tier tier){
	    if(viewer instanceof AbstractViewer)
	        setControllersForViewer((AbstractViewer) viewer, tier == null ? new Tier[0] : new Tier[]{tier});
	    viewer.setTier(tier);
	}
	
	/**
	 * Registers (Tier-) time line controllers to viewer
	 * @param viewer the viewer for which the controllers must be set
	 * @param tiers array of tiers that must be set
	 */
	public void setControllersForViewer(AbstractViewer viewer, Tier[] tiers) {
		if (viewer == null) {
			return;
		}

	    // disconnect an old controller if it exists
        disconnectController(viewer, true);

	    //TODO: connect to all tiers, not just first (-> change storage of controllers)
	    // connect the viewer to the right controller
	    if (tiers != null && tiers.length > 0) {
	        TimeLineController controller = getControllerForTier(tiers[0]);
	        controller.addControllerListener(viewer);
	        controllers.put(viewer, controller);

            // set the controller in the started state if player is playing
            if (masterMediaPlayer.isPlaying()) {
                controller.start();
            }
        }
	}

	/**
	 * Creates a SingleTierViewerPanel and connects it to the Transcription as
	 * an ACMEditListener.
	 *
	 * @return the new SingleTierViewerPanel
	 */
	public SingleTierViewerPanel createSingleTierViewerPanel() {
		SingleTierViewerPanel panel = new SingleTierViewerPanel(this);
		tierOrder.addTierOrderListener(panel);

		try {
			((ACMEditableDocument) transcription).addACMEditListener((ACMEditListener) panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.addElanLocaleListener(transcription, panel);

		return panel;
	}

	/**
	 * Destroys (disconnects) the SingleTierViewerPanel as ACMEditListener from
	 * the Transcription.
	 *
	 * @param panel the SingleTierViewerPanel to be destroyed (disconnected).
	 */
	public void destroySingleTierViewerPanel(SingleTierViewerPanel panel) {
		if(panel == null){
			return;
		}
		tierOrder.removeTierorderListener(panel);
		try {
			((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) panel);
			
			Preferences.removePreferencesListener(transcription, panel);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.removeElanLocaleListener(panel);
	}

	/**
	 * Removes an AbstractViewer completely from the viewer universe
	 *
	 * @param viewer the AbstractViewer that must be destroyed
	 */
	public void destroyViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
		} if(disabledViewers.contains(viewer)){
			disabledViewers.remove(viewer);
		}
	
		disconnect(viewer, true);
		viewers.remove(viewer);	
	}
	
	/**
	 * Remove the transcription Viewer
	 */
	public void destroyTranscriptionViewer() {
		if(transcriptionViewer != null){
			destroyViewer(transcriptionViewer);
			transcriptionViewer = null;
		}
	}
	
	/**
	 * Removes an Grid Viewer
	 */
	public void destroyGridViewer() {
		if(gridViewer != null){
			destroyViewer(gridViewer);
			gridViewer = null;
		}
	}
	
	/**
	 * Removes an Text Viewer	   
	 */
	public void destroyTextViewer() {
		if(textViewer != null){
			destroyViewer(textViewer);
			textViewer = null;
		}
	}
	
	/**
	 * Removes an subtitle Viewers	 
	 */
	public void destroySubtitleViewers() {
		if(subtitleViewers != null){
			for (int i = 0; i < subtitleViewers.size(); i++) {
				destroyViewer(subtitleViewers.elementAt(i));
			}
			subtitleViewers.clear();
			//subtitleViewers = null;
		}
	}
	
	/**
	 * Removes an Lexicon Viewer	  
	 */
	public void destroyLexiconViewer() {
		if(lexiconViewer != null){
			destroyViewer(lexiconViewer);
			lexiconViewer = null;
		}
	}
	
	/**
	 * Removes an Signal Viewer	
	 */
	public void destroySignalViewer() {
		if(signalViewer != null){
			destroyViewer(signalViewer);
			signalViewer = null;
		}
	}
	
	/**
	 * Removes an Interlinear Viewer	
	 */
	public void destroyInterlinearViewer() {
		if(interlinearViewer != null){
			destroyViewer(interlinearViewer);
			interlinearViewer = null;
		}		
	}
	
	/**
	 * Removes an Timeline Viewer	
	 */
	public void destroyTimeLineViewer() {
		if(timeLineViewer != null){
			destroyViewer(timeLineViewer);
			timeLineViewer = null;
		}		
	}		
	
	/**
	 * Removes an Annotation Density Viewer	
	 */
	public void destroyAnnotationDensityViewer() {
		if(annotationDensityViewer != null){
			destroyViewer(annotationDensityViewer);
			annotationDensityViewer = null;
		}		
	}

	/**
	 * Removes an Media Player Control Slider
	 */
	public void destroyMediaPlayerControlSlider() {
		if(mediaPlayerControlSlider != null){
			destroyViewer(mediaPlayerControlSlider);
			mediaPlayerControlSlider = null;
		}		
	}
	
	/**
	 * Removes an Time Panel
	 */
	public void destroyTimePanel() {
		if(timePanel != null){
			destroyViewer(timePanel);
			timePanel = null;
		}		
	}
	/**
	 * Disconnects an AbstractViewer from the viewer universe in such a manner
	 * that it can be reconnected.
	 *
	 * @param viewer the AbstractViewer that must be disabled
	 */
	public void disableViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
			disconnect(viewer, false);
			disabledViewers.add(viewer);
		}
	}

	/**
	 * Reconnects an AbstractViewer to the viewer universe from which it was
	 * temporarily disconnected.
	 *
	 * @param viewer the AbstractViewer that must be enabled
	 */
	public void enableViewer(AbstractViewer viewer) {
		if (disabledViewers.contains(viewer)) {
			disabledViewers.remove(viewer);
			connect(viewer);
			enabledViewers.add(viewer);
		}
	}
	
	/**
	 * Sets a flag on existing players whether frame forward/backward always jumps
	 * to the beginning of the next/previous frame or jumps with the ms per frame value.
	 * 
	 * @param stepsToBegin if true frame forward/backward jumps to begin of next/previous
	 * frame
	 */
	public void setFrameStepsToBeginOfFrame(boolean stepsToBegin) {
		if (masterMediaPlayer != null) {
			masterMediaPlayer.setFrameStepsToFrameBegin(stepsToBegin);
		}
		ElanMediaPlayer player;
		for (int i = 0; i < slaveMediaPlayers.size(); i++) {
			player = (ElanMediaPlayer) slaveMediaPlayers.get(i);
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
		for (int i = 0; i < disabledMediaPlayers.size(); i++) {
			player = (ElanMediaPlayer) disabledMediaPlayers.get(i);
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
	}

	/**
	 * Tries to make sure resources are freed, especially the created 
	 * media players might have to release resources.
	 * Preliminary....
	 */
	public void cleanUpOnClose() {
		ElanMediaPlayer player;
		
		if(masterMediaPlayer != null && 
				masterMediaPlayer.isPlaying()){
			masterMediaPlayer.stop();
	    }
		
		for (int i = 0; i < slaveMediaPlayers.size(); i++) {
			player = (ElanMediaPlayer) slaveMediaPlayers.get(i);
			player.cleanUpOnClose();
		}
		for (int i = 0; i < disabledMediaPlayers.size(); i++) {
			player = (ElanMediaPlayer) disabledMediaPlayers.get(i);
			player.cleanUpOnClose();
		}
		if (masterMediaPlayer != null) {
			masterMediaPlayer.cleanUpOnClose();
		}
		for (int i = 0; i < viewers.size(); i++) {
			AbstractViewer viewer = (AbstractViewer) viewers.get(i);
			disconnect(viewer, true);
			if (viewer instanceof ACMEditListener && transcription != null) {
				((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			}
			if (viewer instanceof TimeLineViewer) {
				((TimeLineViewer)viewer).setTranscription(null);
			}
			if (viewer instanceof InterlinearViewer) {
				((InterlinearViewer)viewer).setTranscription(null);
			}
		}
		if (audioRecognizerPanel != null) {
			// it will be removed as locale listener when the transcription is removed
		}
		enabledViewers.clear();
		viewers.clear();
		disabledViewers.clear();
	}
	
	/**
	 * Connect an AbstractViewer to the Viewer Universe.
	 *
	 * @param viewer the viewer that must be connected;
	 */
	private void connect(AbstractViewer viewer) {
		// observables for all viewers
		viewer.setPlayer(masterMediaPlayer);
		viewer.setSelectionObject(selection);
		selection.addSelectionListener(viewer);
		viewer.setActiveAnnotationObject(activeAnnotation);
		activeAnnotation.addActiveAnnotationListener(viewer);
		ElanLocale.addElanLocaleListener(transcription, viewer);

		viewer.setViewerManager(this);

		// only for viewers that show trancription data
		if (viewer instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener((ACMEditListener) viewer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			((TimeScaleUser) viewer).setGlobalTimeScale(timeScale);
			timeScale.addTimeScaleListener((TimeScaleListener) viewer);
		}
		
		if (viewer instanceof GesturesListener) {
			if (SystemReporting.isMacOS()) {
				GestureMacDispatcher disp = new GestureMacDispatcher((JComponent) viewer, (GesturesListener) viewer);
				gestureMap.put(viewer, disp);
				disp.connect();
			}
		}

		// only multi tier viewers are connected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.addViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, viewer);
		}
		// if there is a controller associated with this viewer connect them
		Controller controller = (Controller) controllers.get(viewer);

		if (controller != null) {
			controller.addControllerListener((ControllerListener) viewer);

			// make sure the viewer is in sync
			viewer.controllerUpdate(new TimeEvent(controller));
		}
	}

	/**
	 * Disconnect an AbstractViewer from the Viewer Universe.
	 *
	 * @param viewer the viewer that must be disconnected;
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnect(AbstractViewer viewer, boolean finalDisconnection) {
		// observables for all viewers
		viewer.setPlayer(null);
		viewer.setSelectionObject(null);
		selection.removeSelectionListener(viewer);
		viewer.setActiveAnnotationObject(null);
		activeAnnotation.removeActiveAnnotationListener(viewer);
		ElanLocale.removeElanLocaleListener(viewer);
		
		viewer.setViewerManager(null);
		
		// only for viewers that show trancription data
		// TEMPRORARY? disabled because disconnected viewers are not aware of changes in teh edited
		// document after they wake up. Keeoing them connected looks like the easiest solution.
		// DO NOT disable the same block in the connect method because that takes care of the
		// first time connection.

		if (viewer instanceof ACMEditListener) {
			try {
		       ((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			} catch (Exception e) {
		       e.printStackTrace();
			}
		}		

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			timeScale.removeTimeScaleListener((TimeScaleUser) viewer);
		}

		if (viewer instanceof GesturesListener) {
			if (SystemReporting.isMacOS()) {
				GestureDispatcher disp = gestureMap.remove(viewer);
				if (disp != null) {
					disp.disconnect();
				}

			}
		}
		
		// only multi tier viewers are disconnected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.removeViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, viewer);
		}
		
		disconnectController(viewer, finalDisconnection);
	}

	/**
	 * Break the connection between a viewer and its controller. Removes the
	 * associated controller if it has no connected Viewers left after this
	 * operation.
	 *
	 * @param viewer the viewer that must be disconnected from its controller
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnectController(AbstractViewer viewer, boolean finalDisconnection) {
		//	get the controller for this viewer and remove the viewer as listener
		Controller controller = (Controller) controllers.get(viewer);

		//searchResultViewer might be created yet not connected -> controller == null
		if (controller != null) {
			controller.removeControllerListener(viewer);

			// remove the viewer key from the controllers hashtable if the disconnection is final
			if (finalDisconnection) {
				controllers.remove(viewer);

				// if there are no more listeners for the controller clean it up
				if (controller.getNrOfConnectedListeners() == 0) {
					removeFromHashTable(controller, controllers);
					masterMediaPlayer.removeController(controller);
					controller = null;
				}
			}
		}
	}

	/**
	 * Gets a TimeLineController for a Tier. If the Controller already exists
	 * it is reused otherwise it is created
	 *
	 * @param tier the Tier for which the TimeLineController must be created
	 *
	 * @return the TimeLineController for the Tier
	 */
	private TimeLineController getControllerForTier(Tier tier) {
		if (tier == null) {
			return null;
		}

		TimeLineController controller = null;

		// first see if the controller already exists
		if (controllers.containsKey(tier)) {
			controller = (TimeLineController) controllers.get(tier);
		}
		else {
			// The controller does not exist, create it
			controller = new TimeLineController(tier, masterMediaPlayer);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(tier, controller);
		}

		return controller;
	}

	/**
	 * Gets a PeriodicUpdateController for a period. If the Controller already
	 * exists it is reused otherwise it is created
	 *
	 * @param period the period in milli seconds for which the
	 *        PeriodicUpdateController must be created
	 *
	 * @return the PeriodicUpdateController for the period
	 */
	private PeriodicUpdateController getControllerForPeriod(long period) {
		PeriodicUpdateController controller = null;
		Long periodKey = new Long(period);

		// first see if the controller already exists
		if (controllers.containsKey(periodKey)) {
			controller = (PeriodicUpdateController) controllers.get(periodKey);
		}
		else {
			// The controller does not exist, create it
			controller = new PeriodicUpdateController(period);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(periodKey, controller);
		}

		return controller;
	}

	/**
	 * Utility to remove all occurences of an object from a Hashtable. There is
	 * no direct method for this in the Java API if you do not know the key
	 *
	 * @param object the Object to be removed
	 * @param hashtable the Hastable that contains the Object
	 *
	 * @return boolean to flag if the Object was actualy removed
	 */
	private boolean removeFromHashTable(Object object, Hashtable hashtable) {
		Object key;
		boolean objectRemoved = false;

		for (Enumeration e = hashtable.keys(); e.hasMoreElements();) {
			key = e.nextElement();

			if (controllers.get(key) == object) {
				hashtable.remove(key);
				objectRemoved = true;
			}
		}

		return objectRemoved;
	}
}
