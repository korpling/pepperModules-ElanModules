package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.export.ImageExporter;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import player.JavaQTMoviePlayer;
import player.JavaQTMoviePlayerCreatedListener;
import player.NoCocoaPlayerException;

/**
 * A media player for MacOS, based on Apple's CocoaComponent and independent of 
 * QuickTime for Java. The actual player (that is wrapped by this class), is a Cocoa
 * (QTKit) wrapper for QuickTime.
 * Synchronization of multiple players and incorporation of a media offset are
 * dealt with on the Cocoa/C level.
 * 
 * @author Han Sloetjes
 *
 */
public class CocoaQTMediaPlayer extends ControllerManager implements
		ElanMediaPlayer, ActionListener, JavaQTMoviePlayerCreatedListener, VideoFrameGrabber {
	private MediaDescriptor mediaDescriptor;
	private String filePath = null;
	private JavaQTMoviePlayer movie;
	private ElanLayoutManager layoutManager;
	private long offset = 0L;
	private boolean playerInited = false;
	// fields for detaching/attaching
	private long curMediaTime = -1L;
	private float curRate = -1.0f;
	private float curVolume = -1.0f;
	
	private boolean isWavPlayer = false;
	private long stopTime = Long.MAX_VALUE;
	private float aspectRatio = -1f;
	
	//popup
	private JPopupMenu popup;
    private boolean detached;
    private JMenuItem durationItem;
    private JMenuItem detachItem;
	private JMenuItem infoItem;
	private JMenuItem saveItem;
	private JMenuItem ratio_4_3_Item;
	private JMenuItem ratio_3_2_Item;
	private JMenuItem ratio_16_9_Item;
	private JMenuItem ratio_185_1_Item;
	private JMenuItem ratio_235_1_Item;
	private JMenuItem copyOrigTimeItem;
	private MouseHandler mouseHandler;

	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
    private long milliSecondsPerSample;
	
	private List<CocoaQTMediaPlayer> listeners;
	private JavaQTMoviePlayer master;
	private IntervalStopController intervalController;
	private EndController endController;
	private boolean firstInit = false;
	
	// mpeg
	private int mpegImageWidth = 0;
	private int mpegImageHeight = 0;

	/**
	 * @param mediaDescriptor
	 */
	public CocoaQTMediaPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		super();
		this.mediaDescriptor = mediaDescriptor;
		String cocoaComp = System.getProperty("com.apple.eawt.CocoaComponent.CompatibilityMode");
		if (cocoaComp == null) {
			System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
		}
		/*
		 // library loading is performed in JavaQTMoviePlayer
    	try {
    		//System.loadLibrary("libJavaQTMovieView");
    	} catch (UnsatisfiedLinkError ule) {
    		System.out.println(ule.getMessage());
    	}
    	*/
        milliSecondsPerSample = 40;
		offset = mediaDescriptor.timeOrigin;

        String URLString = mediaDescriptor.mediaURL;

        System.out.println("mediaURL = " + URLString);
        String fileString = URLString;

        if (URLString.startsWith("file:")) {
        	fileString = URLString.substring(7);
        	// trivial mpg testing
        	URLString = URLString.toLowerCase();
        	if ((URLString.endsWith("mpg") || URLString.endsWith("mpeg")) ) {
            		MPEGVideoHeader mpegHeader = new MPEGVideoHeader(fileString);
            		mpegImageWidth = mpegHeader.getWidth();
            		mpegImageHeight = mpegHeader.getHeight();
            		// System.out.println("MPEG w: " + mpegImageWidth + " - h: " + mpegImageHeight);
            	}
        }
        //System.out.println("File path = " + fileString);
        filePath = fileString;
        //System.out.println("library path: " + System.getProperty("java.library.path"));
        try {
        	movie = new JavaQTMoviePlayer(filePath, 0, 1, 1f, this);
        } catch(UnsatisfiedLinkError ule) {
        	System.out.println("library path: " + System.getProperty("java.library.path"));
        	throw new NoPlayerException("Cocoa player could not be created: " + ule.getMessage());
        } catch (NoCocoaPlayerException ncpe) {
        	throw new  NoPlayerException(ncpe.getMessage());
        } catch (Throwable tr) {
        	throw new NoPlayerException(tr.getMessage());
        }
        //System.out.println("Duration: " + movie.getMediaDuration());
        //System.out.println("Init width: " + (int) movie.getNaturalWidth());
        //System.out.println("Has video: " + movie.hasVideo());
        //movie.createMovie(filePath);
        //System.out.println("Auto detected..FrameDuration " + movie.getFrameDuration());

        //playerInited = true;

        popup = new JPopupMenu();
        durationItem = new JMenuItem(ElanLocale.getString(
                    "Player.duration") + ":  ");
        durationItem.setEnabled(false);
		infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
		infoItem.addActionListener(this);
		saveItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame"));
		saveItem.addActionListener(this);
		ratio_4_3_Item = new JMenuItem("4:3");//1.33
		ratio_4_3_Item.addActionListener(this);
		ratio_3_2_Item = new JMenuItem("3:2");//1.66
		ratio_3_2_Item.addActionListener(this);
		ratio_16_9_Item = new JMenuItem("16:9");//1.78
		ratio_16_9_Item.addActionListener(this);
		ratio_185_1_Item = new JMenuItem("1.85:1");
		ratio_185_1_Item.addActionListener(this);
		ratio_235_1_Item = new JMenuItem("2.35:1");
		ratio_235_1_Item.addActionListener(this);
		copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
		copyOrigTimeItem.addActionListener(this);
		JMenu arMenu = new JMenu(ElanLocale.getString("Player.ForceAspectRatio"));
		arMenu.add(ratio_4_3_Item);
		arMenu.add(ratio_3_2_Item);
		arMenu.add(ratio_16_9_Item);
		arMenu.add(ratio_185_1_Item);
		arMenu.add(ratio_235_1_Item);
        popup.addSeparator();
        popup.add(saveItem);
        popup.add(infoItem);
        popup.add(arMenu);
        popup.add(durationItem);
        popup.add(copyOrigTimeItem);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        mouseHandler = new MouseHandler();
        movie.addMouseListener(mouseHandler);
        intervalController = new IntervalStopController();
        endController = new EndController(40);
	}
	
	/**
	 * Returns whether creation of a native movie player has been successful.
	 *  
	 * @return true if the native movie player has been created and the file is of a supported format
	 */
	public boolean isPlayerCreated() {
		if (movie != null) {
			return movie.isMovieValid();
		}
		return false;
	}

	/**
	 * Notification from the native player that the player is created and initialized.
	 */
	public void playerCreated(JavaQTMoviePlayer jqtPlayer) {
		//System.out.println("Created: " + mediaDescriptor.mediaURL);
		if (jqtPlayer != movie) {
			ClientLogger.LOG.warning("Wrong player notified...!");
			return;
		}
		if (!movie.isMovieValid()) {
			ClientLogger.LOG.warning("Invalid movie: format not supported");
			// no use to throw Exception
			return;
		}
		playerInited = true;		
		
		if (offset != 0) {
			movie.setOffset(offset);
		}
		
		if (curMediaTime > -1) {
			setMediaTime(curMediaTime);	  	
		} else {
			setMediaTime(0L);
		}
		if (curRate > -1) {
			setRate(curRate);
		}
		
		if (master != null) {
			movie.setVolume(0f);
		} else if (curVolume > -1) {
			movie.setVolume(curVolume);
		}
		
		if (!firstInit) {
			if (!movie.hasVideo()) {
				isWavPlayer = true;
			}
			durationItem.setText(ElanLocale.getString("Player.duration") + ":  " +
	                        TimeFormatter.toString(getMediaDuration())); // incorporates the media offset
			
			if (movie.getNaturalHeight() != 0) {
				aspectRatio = movie.getNaturalWidth() / movie.getNaturalHeight();					
			}
			if (!isWavPlayer) {
				milliSecondsPerSample = movie.getFrameDuration();
			}
			stopTime = getMediaDuration();
			endController.setEndTime(stopTime);
			addController(endController);
			firstInit = true;
			if (isWavPlayer) {
				aspectRatio = -1;
			}
		} else {
			if (listeners != null) {
		  		   for (CocoaQTMediaPlayer cpl : listeners) {
		  			   movie.addListener(cpl.getNativeMoviePlayer());
		  		       cpl.setMasterJavaQTMoviePlayer(movie);
		  		   }
		  	   }
		  	   if (master != null) {
		  		   master.addListener(movie);
		  	   }
		}

	}

	/**
	 * Called when a movie is removed from a window. Native resources can be freed.
	 */
	public void cleanUpOnClose() {
		if (movie != null) {
			if (isPlaying()) {
				stop();
			}
			movie.cleanUpOnClose();
		}
	}

	/**
	 * Returns the aspect ratio (of the video).
	 * 
	 * @return the aspect ratio
	 */
	public float getAspectRatio() {		
		return aspectRatio;

	}

	/**
     * Enforces an aspect ratio for the media component.
     * 
     * @param aspectRatio the new aspect ratio
     */
    public void setAspectRatio(float aspectRatio) {
    	this.aspectRatio = aspectRatio;
    }
    
	/**
	 * Returns a description of the player.
	 * 
	 * @return description of the player
	 */
	public String getFrameworkDescription() {
		return "Cocoa QT Player";
	}
	
	/**
	 * Returns the (mpeg) media width and height as read from the mpegfile. 
	 * 
	 * @return the dimension according to fields in the stream headers
	 */
	public Dimension getMediaFileDimension() {
		if (mpegImageWidth > 0 && mpegImageHeight > 0) {
			return new Dimension(mpegImageWidth, mpegImageHeight);
		}
		return null;
	}
	
	/**
	 * Returns the media descriptor
	 * 
	 * @return the media descriptor
	 */
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	/**
	 * @return the duration of the media file
	 */
	public long getMediaDuration() {
		if (movie != null) {
			return movie.getMediaDuration();
		}
		return 0;
	}

	/**
	 * @return the current media time
	 */
	public long getMediaTime() {
		if (!playerInited) {
			return 0;
		}

		if (movie != null) {
			//System.out.println("T: " + movie.getMediaTime());
			return movie.getMediaTime();
		}
		return 0L;
	}

	/**
	 * @return the number of ms per sample (video frame)
	 */
	public long getMilliSecondsPerSample() {
		if (playerInited && !isWavPlayer) {
			return movie.getFrameDuration();
		}
		return milliSecondsPerSample;
	}

	/**
	 * @return the media offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return the playback rate
	 */
	public float getRate() {
		if (!playerInited) {
			return 1f;
		}

		if (movie != null) {
			return movie.getRate();
		}
		return 1f;
	}

	/**
	 * Returns the height of a video frame, as interpreted by QT.
	 * 
	 * @return the original source height
	 */
	public int getSourceHeight() {
		if (isWavPlayer) {
			return 1;
		}
		if (!playerInited) {
			return 1;
		}

		if (movie != null) {
			return (int) movie.getNaturalHeight();
		}
		return 1;
	}

	/**
	 * Returns the width of a video frame, as interpreted by QT.
	 * 
	 * @return the original source width
	 */
	public int getSourceWidth() {
		if (isWavPlayer) {
			return 1;
		}
		if (!playerInited) {
			return 1;
		}

		if (movie != null) {
			return (int) movie.getNaturalWidth();
		}
		return 1;
	}

	/**
	 * Even for an audio only file a component is created, otherwise it won't work
	 * Only after the component has been added to a frame the QT player is created
	 */
	public Component getVisualComponent() {
		if (isWavPlayer) {
			return null;
		}
		if (movie != null) {
			return movie;
		}
		
		return null;
	}

	/**
	 * Workaround method to implement attach/detach functionality. A new movie (== visual component)
	 * must be created, after the old movie has been removed from a frame. 
	 * This must only be called after the old movie has been removed!
	 * 
	 * @return a new instance of the movie player
	 */
	public synchronized java.awt.Component createNewVisualComponent() {
		/*
		if (isWavPlayer) {
			return null;
		} else {*/
			
		// launching in detached mode, "detach" programmatically
			if (!firstInit && aspectRatio < 0) {
				curMediaTime = movie.getMediaTime();
				curRate = movie.getRate();
				curVolume = movie.getVolume();
				movie.removeMouseListener(mouseHandler);
	
				// if listeners != null, remove listeners
				// if master != null unregister as listener
				if (listeners != null) {
					for (CocoaQTMediaPlayer cpl : listeners) {
						movie.removeListener(cpl.getNativeMoviePlayer());
						cpl.setMasterJavaQTMoviePlayer(null);
					}
				}
				if (master != null) {
					master.removeListener(movie);
				}
				movie.cleanUpOnClose();
				detachItem.setText(ElanLocale.getString("Detachable.attach"));
				detached = true;
			}
			
			try {
				movie = new JavaQTMoviePlayer(filePath, this);
			} catch (NoCocoaPlayerException npe) {
				ClientLogger.LOG.warning(npe.getMessage());
				return null;
			}

			movie.addMouseListener(mouseHandler);
			
			// connect listeners in the case of launching in detached mode
			if (!firstInit && aspectRatio < 0) {
				if (listeners != null) {
					for (CocoaQTMediaPlayer cpl : listeners) {
						movie.addListener(cpl.getNativeMoviePlayer());
						cpl.setMasterJavaQTMoviePlayer(movie);
					}
				}
				if (master != null) {
					master.addListener(movie);
				}
			}

			return movie;
		//}
		//return null;
	}
	
	/**
	 * @return the playback volume
	 */
	public float getVolume() {
		if (!playerInited) {
			return 1f;
		}

		if (movie != null) {
			return movie.getVolume();
		}
		return 1f;
	}

	/**
	 * @return whether the frame rate has successfully been detected.
	 */
	public boolean isFrameRateAutoDetected() {
		if (playerInited) {
			return movie.getFrameDuration() != 0;
		}
		return false;
	}

	/**
	 * @return whether the player currently is playing
	 */
	public boolean isPlaying() {
		if (!playerInited) {
			return false;
		}

		if (movie != null) {
			return movie.isPlaying();
		}
		return false;
	}

	/**
	 * Steps to the next frame; either to the begin of the next frame, or jumps n ms forwards, where n
	 * is the ms per sample value
	 */
	public void nextFrame() {
		if (!playerInited || master != null) {
			return;
		}

    	if (frameStepsToFrameBegin) {
    		long curFrame = getMediaTime() / milliSecondsPerSample;
    		setMediaTime((curFrame + 1) * milliSecondsPerSample);
    	} else {
    		setMediaTime(getMediaTime() + getMilliSecondsPerSample());
    	}
	}

	/**
	 * Plays an interval. Uses QT's built in facility for playing a selection
	 */
	public void playInterval(long startTime, long stopTime) {
		if (!playerInited || master != null) {
			return;
		}

		if (movie != null  && stopTime > startTime) {
		    //long startOff = startTime;
		    //long stopOff = stopTime;
			//System.out.println("Starting controllers...." + " (" + System.currentTimeMillis() + ")");
			//System.out.println("start, stop " + startTime + " " + stopTime);
			setMediaTime(startTime);

		    //System.out.println("current media time: " + getMediaTime());
		    setStopTime(stopTime);
			startControllers();
			
			intervalController.setStopTime(stopTime);
			intervalController.start();
//			try {
//				Thread.sleep(40);
//			} catch (InterruptedException ie) {
//			}

			movie.playInterval(startTime, stopTime);	
		}
	}

	/**
	 * Steps to the previous frame; either to the begin of the previous frame, or jumps n ms backwards, where n
	 * is the ms per sample value
	 */
	public void previousFrame() {
		if (!playerInited) {
			return;
		}

		if (movie != null) {
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
	}

	/**
	 * Sets the flag whether frame stepping always jumps to the begin of a frame
	 */
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		frameStepsToFrameBegin = stepsToFrameBegin;
	}

	/**
	 * Sets the layout manager.
	 * 
	 * @param layoutManager the layout manager for a document window
	 */
	public void setLayoutManager(ElanLayoutManager layoutManager) {
        if (this.layoutManager == null && !(playerInited && isWavPlayer)) {
			detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
			detachItem.addActionListener(this);
			popup.insert(detachItem, 0);
        }
        
		this.layoutManager = layoutManager;
		if (playerInited && isWavPlayer) {
			layoutManager.remove(movie);
		}
		//layoutManager.getElanFrame().getContentPane().addComponentListener(movie);
	}

	/**
	 * Sets the media time.
	 * 
	 * @param time the media time
	 */
	public void setMediaTime(long time) {
		//System.out.println("Set T: " + time + " M: " + filePath);
		if (!playerInited) {
			return;
		}
        // do not set rate on a started player
        if (isPlaying()) {
            stop();
        }
        
        if (movie != null) {
			movie.setMediaTime(time);
	        // set the media time for the connected controllers
			//
			try {
				long et = System.currentTimeMillis() + 150;
				while (movie.getMediaTime() != (time) && System.currentTimeMillis() < et) {
					Thread.sleep(20);
				}				
			} catch (Exception ex){
				ex.printStackTrace();
			}
			//
	        setControllersMediaTime(time);
        }
	}

	/**
	 * Sets the ms per sample value, in case this has not been detected.
	 * 
	 * @param milliSeconds the new ms per sample value
	 */
	public void setMilliSecondsPerSample(long milliSeconds) {
		if (!isFrameRateAutoDetected()) {
			milliSecondsPerSample = milliSeconds;
		}
	}

	/**
	 * Sets the offset.
	 * 
	 * @param offset the offset
	 */
	public void setOffset(long offset) {
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;
        
        if (playerInited && movie != null) {
    		//System.out.println("Offset: " + offset + " (" + mediaDescriptor.mediaURL + ")");
        	movie.setOffset(offset);

			durationItem.setText(ElanLocale.getString("Player.duration") + ":  " +
	                        TimeFormatter.toString(getMediaDuration())); // incorporates the media offset
			if (endController != null) {
				endController.setEndTime(getMediaDuration());
			}
        }
	}

	/**
	 * Sets the playback rate.
	 * 
	 * @param rate the rate
	 */
	public void setRate(float rate) {
		if (!playerInited) {
			return;
		}

        // do not set rate on a started player
        if (isPlaying()) {
            stop();
        }
        
        if (movie != null) {
        	movie.setRate(rate);
        }	
	}

	/**
	 * Sets the stop time for the player.
	 * 
	 * @param stopTime the new stoptime
	 */
	public void setStopTime(long stopTime) {
		if (!playerInited) {
			return;//??
		}
		
		this.stopTime = stopTime;
		//endController.setStopTime(stopTime);
	}

	/**
	 * Sets the volume.
	 * 
	 * @param level the playback volume
	 */
	public void setVolume(float level) {
		if (!playerInited) {
			return;
		}

		if (movie != null) {
			if (master == null) {
				movie.setVolume(level);
			} else {
				movie.setVolume(0f);
			}
		}
	}

	/**
	 * Starts the player.
	 */
	public void start() {
		if (!playerInited) {
			return;
		}

        // do not try to start at the end of the media, the JMF player blocks
        // start playing at the beginning of the media data
        if ((getMediaDuration() - getMediaTime()) < 40) {
            setMediaTime(0);
        }
        if (movie != null) {
	        // make sure all managed controllers are started
        	//System.out.println("Movie: " + movie.hashCode());
	        startControllers();
			movie.start();
        }
	}

	/**
	 * Stops the player.
	 */
	public void stop() {
		if (!playerInited) {
			return;
		}

		if (movie != null) {
			movie.stop();
	
	        // make sure all managed controllers are stopped
	        stopControllers();
	        setControllersMediaTime(getMediaTime());
		}
	}

	/**
	 * Updates the popup menu items.
	 */
	public void updateLocale() {
    	if (infoItem != null) {
			infoItem.setText(ElanLocale.getString("Player.Info"));
    	}
        if (durationItem != null) {
            durationItem.setText(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(getMediaDuration()));
        }
        if (saveItem != null) {
			saveItem.setText(ElanLocale.getString("Player.SaveFrame"));
        }		

        if (detachItem != null) {
            if (detached) {
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
            } else {
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
            }
        }
        if (copyOrigTimeItem != null) {
        	copyOrigTimeItem.setText(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
        }
	}

	   /**
     * Puts the specified text on the clipboard.
     * 
     * @param text the text to copy
     */
    private void copyToClipboard(String text) {
    	    if (text == null) {
    		    return;
    	    }
    	    //System.out.println(text);
    	    if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (SecurityException se) {
                //LOG.warning("Cannot copy, cannot access the clipboard.");
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        } else {
            try {
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        }

    }
    
	/**
	 * Grabs the current video frame and converts it to an Image object.
	 * 
	 * @return the current video frame
	 */
	public Image getCurrentFrameImage() {
		return getFrameImageForTime(getMediaTime());
	}
	
	/**
	 * Grabs the frame for the given time and converts it to a BufferedImage.<br>
	 * The size of the image is determined by the size read from the header of 
	 * the mediafile; QT often uses another size for MPEG-1 files.
	 * QT on Windows uses another default pixel format than QT on the Mac; the current 
	 * implementation seems to work on both platforms.
	 * 
	 * @param time the media time for the frame 
	 * @return the frame image or null
	 */	
	public Image getFrameImageForTime(long time) {
		
		// by default mpeg movies seem to be sized to 320 x 240 by qt
		// this is not right...
		int w = 352;
		int h = 288;
		
		if (mpegImageWidth > 0 && mpegImageHeight > 0) {
			w = mpegImageWidth;
			h = mpegImageHeight;
		} else {
			//w = (int)(movie.getNaturalBoundsRect().getWidth() * movie.getMatrix().getSx());
			//h = (int)(movie.getNaturalBoundsRect().getHeight() * movie.getMatrix().getSy());
			w = (int) movie.getNaturalWidth();
			h = (int) movie.getNaturalHeight();
		}
		//byte[] bytes = movie.getFrame(time, filePath)
		byte[] bytes = movie.getFrame(time, (int) movie.getNaturalWidth(), (int) movie.getNaturalHeight());
		//System.out.println("Num bytes: " + bytes.length + " W: " + (int) movie.getNaturalWidth() + " H: " + (int) movie.getNaturalHeight());
		try {
			//IIOByteBuffer iiobb = new IIOByteBuffer(bytes, 0, bytes.length);
			MemoryCacheImageInputStream mci = new MemoryCacheImageInputStream(
				new ByteArrayInputStream(bytes, 0, bytes.length));
			BufferedImage bi = ImageIO.read(mci);
			return bi;
		} catch (IOException ioe) {
			
		}
		return null;
	}
	
	
    /*
    *
    */
   public void actionPerformed(ActionEvent e) {
       if (e.getSource().equals(detachItem) && (layoutManager != null)) {
           if (detached) {
        	   // store rate and volume and mediatime
        	   if (movie.isPlaying()) {
        		   movie.stop();
        	   }
        	   curMediaTime = movie.getMediaTime();
        	   curRate = movie.getRate();
        	   curVolume = movie.getVolume();
        	   movie.removeMouseListener(mouseHandler);
        	   playerInited = false;
        	   // if listeners != null, remove listeners
        	   // if master != null unregister as listener
        	   if (listeners != null) {
        		   for (CocoaQTMediaPlayer cpl : listeners) {
        			   movie.removeListener(cpl.getNativeMoviePlayer());
        			   cpl.setMasterJavaQTMoviePlayer(null);
        		   }
        	   }
        	   if (master != null) {
        		   master.removeListener(movie);
        	   }
        	   movie.cleanUpOnClose();
               layoutManager.attach(movie);
               detachItem.setText(ElanLocale.getString("Detachable.detach"));
               detached = false;
           } else {
        	   // ??
        	   // store rate and volume and mediatime
        	   if (movie.isPlaying()) {
        		   movie.stop();
        	   }
        	   curMediaTime = movie.getMediaTime();
        	   curRate = movie.getRate();
        	   curVolume = movie.getVolume();
        	   movie.removeMouseListener(mouseHandler);
        	   
        	   playerInited = false;

        	   // if listeners != null, remove listeners
        	   // if master != null unregister as listener
        	   if (listeners != null) {
        		   for (CocoaQTMediaPlayer cpl : listeners) {
        			   movie.removeListener(cpl.getNativeMoviePlayer());
        			   cpl.setMasterJavaQTMoviePlayer(null);
        		   }
        	   }
        	   if (master != null) {
        		   master.removeListener(movie);
        	   }
        	   movie.cleanUpOnClose();
               layoutManager.detach(movie);
               detachItem.setText(ElanLocale.getString("Detachable.attach"));
               detached = true;
           }
       } else if (e.getSource() == infoItem) {
			new FormattedMessageDlg(this);
		} else if (e.getSource() == saveItem) {
			ImageExporter export = new ImageExporter(layoutManager.getElanFrame());
			export.exportImage(getCurrentFrameImage());
		} else if (e.getSource() == ratio_4_3_Item) {
			aspectRatio = 1.33f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_3_2_Item) {
			aspectRatio = 1.66f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_16_9_Item) {
			aspectRatio = 1.78f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_185_1_Item) {
			aspectRatio = 1.85f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());			
		} else if (e.getSource() == ratio_235_1_Item) {
			aspectRatio = 2.35f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + offset;
			Object val = Preferences.get("CurrentTime.Copy.TimeFormat", null);
			String timeFormat = null;
			String currentTime = null;
			
	        if(val instanceof String){
	        	timeFormat = val.toString();        	
	        	if(timeFormat.equals(Constants.HHMMSSMS_STRING)){
	            	currentTime = TimeFormatter.toString(t);
	            } else if(timeFormat.equals(Constants.SSMS_STRING)){
	            	currentTime = TimeFormatter.toSSMSString(t);
	            } else if(timeFormat.equals(Constants.NTSC_STRING)){
	            	currentTime = TimeFormatter.toTimecodeNTSC(t);
	            }else if(timeFormat.equals(Constants.PAL_STRING)){
	            	currentTime = TimeFormatter.toTimecodePAL(t);
	            }   else {
	            	currentTime = Long.toString(t);
	            }
	        } else {
	        	currentTime = Long.toString(t);
	        }
	        copyToClipboard(currentTime);
		}
   }
	
   /**
    * In case of multiple videos, there is one leading player. Synchronous playback is handled on 
    * a lower level.
    * @param controller a controller to connect
    */
	public synchronized void addController(Controller controller) {
	   //System.out.println("C: " + controller.getClass().getName());
	   if (controller instanceof CocoaQTMediaPlayer) {
		   movie.addListener(((CocoaQTMediaPlayer) controller).getNativeMoviePlayer());
		   if (listeners == null) {
			   listeners = new ArrayList<CocoaQTMediaPlayer>(5);
		   }
		   listeners.add((CocoaQTMediaPlayer) controller);
		   ((CocoaQTMediaPlayer) controller).setMasterJavaQTMoviePlayer(movie);
		   this.master = null;
		   return;
	   }
		// call super
		super.addController(controller);
	}

	/**
	 * Sets a reference to the leading JavaQTMoviePlayer. This is needed for registration
	 * of the new JavaQTMoviePlayer in case of detaching/attaching. 
	 * 
	 * @param master the leading JavaQTMoviePlayer player
	 */
	void setMasterJavaQTMoviePlayer(JavaQTMoviePlayer master) {
		// only the master should take care of stopping other controllers.
		removeController(endController);
		movie.setVolume(0f);
		this.master = master;
	}
   
	/**
	 * @param removes a controller
	 */
	public synchronized void removeController(Controller controller) {
		//System.out.println("C rem: " + controller.getClass().getName());
		if (controller instanceof CocoaQTMediaPlayer) {
	        movie.removeListener(((CocoaQTMediaPlayer) controller).getNativeMoviePlayer());
			   if (listeners != null) {
				   listeners.remove((CocoaQTMediaPlayer) controller);
			   }
			   
			return;
		}
		// call
		super.removeController(controller);
	}


	public JavaQTMoviePlayer getNativeMoviePlayer() {
	    return movie;
    }

/**
    * A handler for mouse events.
    * 
    * @author HS
    * @version 1.0
    */
   protected class MouseHandler extends MouseAdapter {
   	private final DecimalFormat format = new DecimalFormat("#.###");
   	
       /**
	 * @param forVideo
	 */
	public MouseHandler() {
		super();
	}

	/**
        * Creates and shows a popup menu.
        *
        * @param e the event
        */
       public void mousePressed(MouseEvent e) {
           if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				if (layoutManager != null && layoutManager.isAttached(CocoaQTMediaPlayer.this)) {
					if (detached) {
						detached = false;
						detachItem.setText(ElanLocale.getString("Detachable.detach"));
					}
				}
               popup.show(getVisualComponent(), e.getPoint().x, e.getPoint().y);
           }
       }
		
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (layoutManager != null) {
					//System.out.println("Mouse double clicked..." + CocoaQTMediaPlayer.this.mediaDescriptor.mediaURL);
					layoutManager.setFirstPlayer(CocoaQTMediaPlayer.this);
				}
				return;
			}
           if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				if (layoutManager != null && layoutManager.isAttached(CocoaQTMediaPlayer.this)) {
					if (detached) {
						detached = false;
						detachItem.setText(ElanLocale.getString("Detachable.detach"));
					}
				}
               popup.show(getVisualComponent(), e.getPoint().x, e.getPoint().y);
               return;
           }
           //System.out.println("X: " + e.getX() + " Y: " + e.getY());
           //System.out.println("CW: " + visualComponent.getWidth() + " CH: " + visualComponent.getHeight());
           try {
               //System.out.println("OW: " + movie.getNaturalBoundsRect().getWidth() + " OH: " + 
           	   //    movie.getNaturalBoundsRect().getHeight());
               if (e.isAltDown()) {
               	   copyToClipboard(format.format(e.getX() / (float)movie.getWidth()) + "," 
               			   + format.format(e.getY() / (float)movie.getHeight()));
               }  else if (e.isShiftDown()){
            	   //TODO make sure to get the encoded width and height
                   copyToClipboard("" + (int)((movie.getWidth() / (float)movie.getWidth()) * e.getX()) 
               		    + "," + (int)((movie.getHeight() / (float)movie.getHeight()) * e.getY()));
               } else {
            	 //TODO make sure to get the encoded width and height
                   copyToClipboard("" + (int)((movie.getWidth() / (float)movie.getWidth()) * e.getX()) 
               		    + "," + (int)((movie.getHeight() / (float)movie.getHeight()) * e.getY())
               		    + " [" + movie.getWidth() + "," + movie.getHeight() + "]");
               }
               // notify the layout manager to paste to the active annotation if there is one and depending
               // on a user preference
           } catch (Exception exep) {
           	   exep.printStackTrace();
           }
       }
   }

   /**
    * A controller that takes care of stopping the connected controllers.
    * 
    * @author Han Sloetjes
    *
    */
   private class IntervalStopController implements Controller, Runnable {
	    /** the started state */
	    final int STARTED = 0;

	    /** the stopped state */
	    final int STOPPED = 1;
	    long period = 40;
	    Thread thread;
	    volatile int state;
	    long stopTime = 0L;
	   /**
		 * Constructor.
		 */
		public IntervalStopController() {
			super();
			state = STOPPED;
		}

		/**
		 * 
		 * @param period the interval for checking the state of the player
		 */
		public IntervalStopController(long period) {
			super();
			this.period = period;
			state = STOPPED;
		}
		
		/**
		 * Starts the controller.
		 */
		public void start() {
	        if (state == STARTED) {
	            return;
	        }
	        
	        state = STARTED;
	        // start the run method
	        thread = new Thread(this, "StopController");
	        thread.start();
		   }
	
		public void addControllerListener(ControllerListener listener) {
			// ignored
		}
	
		public int getNrOfConnectedListeners() {
			return 0;
		}
	
		public void postEvent(ControllerEvent event) {
			// ignored
		}
	
		public void removeControllerListener(ControllerListener listener) {
			// ignored
		}
	
		public void setMediaTime(long time) {
			// ignored?
		}
	
		public void setRate(float rate) {
			// ignored			
		}
	
		public void setStopTime(long time) {
			this.stopTime = time;
			//System.out.println("set stop time: " + time);
		}
	
		/**
		 * Stops the controller.
		 */
		public void stop() {
	        if (state == STOPPED) {
	            return;
	        }

	        state = STOPPED;

	        if (thread != null) {
	        	try {
	        		thread.interrupt();
	        	} catch (Exception ex) {
	        		
	        	}
	        }				
		}
	
		/**
		 * Checks the state of the player at a regular interval and notifies listeners once the player has stopped playing.
		 */
		public void run() {
	        state = STARTED;
	        long lastMediaTime = 0;
	        long curTime = 0L;
	        int numRepeats = 0;// the number of times the same media time is measured

	        while (state == STARTED) {
	            // sleep period milli seconds
	            if (!Thread.currentThread().isInterrupted()) {
	                // sleep until next event
	                try {
	                    Thread.sleep(period);
	                } catch (InterruptedException e) {
	                	//e.printStackTrace();
	                }
	            }
	            
	            curTime = CocoaQTMediaPlayer.this.getMediaTime();
	            if (curTime >= this.stopTime) {
//	            	stopControllers();
	            	if (CocoaQTMediaPlayer.this.isPlaying()) {
	            		CocoaQTMediaPlayer.this.stop();
	            	} else {
	            		//System.out.println("Stopping controllers...." + curTime + " (" + System.currentTimeMillis() + ")");	            		
	            		stopControllers();
	            	}
	            	state = STOPPED;
	            	break;
	            } else {
	            	// this is a workaround for those cases the internal QT play selection function never reaches 
	            	// the designated stop time. Force a stop on the controllers.
	            	//System.out.println("Cur time: " + curTime + " last: " + lastMediaTime);
	            	if (curTime == lastMediaTime) {
	            		numRepeats++;
	            		if (numRepeats > 5) {// how often?
	            			//System.out.println("Forced stop of Play Selection");
	    	            	//stopControllers();
	    	            	if (CocoaQTMediaPlayer.this.isPlaying()) {
	    	            		CocoaQTMediaPlayer.this.stop();
	    	            		
		    	            	if (state != STOPPED && this.stopTime - CocoaQTMediaPlayer.this.getMediaTime() <= 10) {
		    	            		//System.out.println("Setting media time...");
		    	            		CocoaQTMediaPlayer.this.setMediaTime(this.stopTime);
		    	            	}
	    	            	} else {
	    	            		//System.out.println("Stopping controllers 2...." + curTime + " (" + System.currentTimeMillis() + ")");
	    	            		stopControllers();
	    	            	}	    	            	

	    	            	state = STOPPED;
	    	            	break;
	            		}
	            	} else {
	            		lastMediaTime = curTime;
	            		numRepeats = 0;
	            	}
	            }
	        }
	        //System.out.println("Leaving interval thread...." + curTime + " (" + System.currentTimeMillis() + ")");
		}
   }

   /**
    * A controller that only detects the end of the media stop.
    * 
    * @author Han Sloetjes
    */
   private class EndController extends IntervalStopController {
	   long endTime = 0L;

		/**
		 * 
		 */
		public EndController() {
			super();
		}
	
		/**
		 * @param period
		 */
		public EndController(long period) {
			super(period);
		}

		@Override
		public void setStopTime(long time) {
			// ignore the interval stop time
		}
		
		public void setEndTime(long time) {
			endTime = time;
		}

		@Override
		public void run() {
			//System.out.println("Start End Thread...");
	        state = STARTED;
	        
	        while (state == STARTED) {
	            // sleep period milliseconds
	            if (!Thread.currentThread().isInterrupted()) {
	                // sleep until next event
	                try {
	                    Thread.sleep(period);
	                } catch (InterruptedException e) {
	                }
	            }

	            if (CocoaQTMediaPlayer.this.getMediaTime() >= this.endTime) {
	            	//System.out.println("Media time >= end time");
	            	stopControllers();
	            	if (CocoaQTMediaPlayer.this.isPlaying()) {
	            		stop();
	            	}
	            	state = STOPPED;
	            	break;
	            } 
	        }
		}
		
	   
   }

}
