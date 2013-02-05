package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.util.WAVCuePoint;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.util.TimeFormatter;

// ALBERT
import mpi.eudico.client.annotator.recognizer.data.Boundary;
import mpi.eudico.client.annotator.recognizer.data.BoundarySegmentation;
// END ALBERT

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 * Draws a waveform of each audiochannel, a crosshair cursor denoting the
 * current media time and the selection if the user has selected a part of the
 * media file.
 */
public class SignalViewer extends TimeScaleBasedViewer
    implements ComponentListener, MouseListener, MouseMotionListener,
    MouseWheelListener, ActionListener, GesturesListener {
    /** Holds value of property DOCUMENT ME! */
    public static final int MONO = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int STEREO_SEPARATE = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int STEREO_MERGED = 2;

    /** Holds value of property DOCUMENT ME! */
    public static final int STEREO_BLENDED = 3;

    /** The default number of pixels for one second of media time. */
    static final int PIXELS_FOR_SECOND = 100;
    private int channelMode;

    /** An array of zoomlevels. */
    public final int[] VERT_ZOOM = new int[] {
            100, 150, 200, 300, 500, 1000, 2000, 3000
        };
    
    /** Holds value of property DOCUMENT ME! */
    private final int GAP = 4;
    private int rulerHeight;
    private BufferedImage bi;
    private Graphics2D big2d;
    private AlphaComposite alpha04;
    private AlphaComposite alpha07;
    private WavePart currentPart;

    /** Holds value of property DOCUMENT ME! */
    private final int SCREEN_BUFFER = 1;

    private WAVSampler samp;

    /** The initial number of milliseconds per pixel */
    public final int DEFAULT_MS_PER_PIXEL = 10;

    /** The current number of milliseconds per pixel */
    private float msPerPixel;

    /**
     * The number of sound samples per pixel. Is msPerPixel * samples per ms, is
     * msPerPixel * samplefrequency / 1000.
     */
    private float samplesPerPixel;

    /**
     * The resolution in number of pixels for a second. This is not a
     * percentage value. Historically resolution = PIXELS_FOR_SECOND  factor,
     * where factor = 100 / menu_resolution_percentage_value.
     */
    //private int resolution;
    private TimeRuler ruler;
    private int maxAmplitude;
    private int imageWidth;
    private int imageHeight;
    private long crossHairTime;
    private int crossHairPos;
    private long intervalBeginTime;
    private long intervalEndTime;
    private long dragStartTime;
    private long selectionBeginTime;
    private long selectionEndTime;
    private int selectionBeginPos;
    private int selectionEndPos;
    private Point dragStartPoint;
    private Point dragEndPoint;

    /** Holds value of property DOCUMENT ME! */
    public final int SCROLL_OFFSET = 16;
    private DragScroller scroller;
    private JPopupMenu popup;
    private JMenuItem praatSelMI;
    private JMenuItem clipSelPraatMI;
	private JMenuItem infoItem;
    private ButtonGroup zoomBG;
    private JRadioButtonMenuItem customZoomMI;
    private JMenuItem zoomSelectionMI;
    private ButtonGroup vertZoomGroup;
    private JRadioButtonMenuItem separateMI;
    private JRadioButtonMenuItem mergedMI;
    private JRadioButtonMenuItem blendMI;
    private JCheckBoxMenuItem timeRulerVisMI;
    private JCheckBoxMenuItem timeScaleConMI;
    private JMenuItem copyOrigTimeItem;
    private boolean timeScaleConnected;
    private boolean panMode;
    private boolean timeRulerVisible;
    private boolean clearSelOnSingleClick = true;
    private int vertZoom = 100;

    /** Holds value of property DOCUMENT ME! */
    private final Object paintlock = new Object();

    /**
     * an offset in milliseconds into the media file where the new media begin
     * point (0 point) is situated
     */
    private long mediaOffset;
    /** store the path to the media file */
    private String mediaFilePath;

    //a flag for the scroll thread

    /** Holds value of property DOCUMENT ME! */
    boolean stopScrolling = true;

    /** Holds value of property DOCUMENT ME! */
    boolean allowConnecting = true;
    private Color selectionColor = Constants.SELECTIONCOLOR;

    //  ALBERT
    JMenu segmentationMenu;
    BoundarySegmentation segmentationChannel1;
    boolean showSegmentationChannel1;
    JCheckBoxMenuItem segmentationChannel1Item;
    BoundarySegmentation segmentationChannel2;
    boolean showSegmentationChannel2;
    JCheckBoxMenuItem segmentationChannel2Item;
    // END ALBERT
    // some error feedback
    private String errorKey = null;
    
    
    private boolean recalculateInterval = true;
    private int horScrollSpeed = 10;
    
    /**
     * Create a SignalViewer with some default values but without any data to
     * display.
     */
    public SignalViewer() {
        initViewer();
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setDoubleBuffered(false);
        setOpaque(true);

        //setVisible(true);
    }
    
    
    /**
     * Constructor used from Corex
     *
     * @param mediaURL
     * @param allowConnecting
     */
    public SignalViewer(URL mediaURL, boolean allowConnecting) {
        this(mediaURL);
        this.allowConnecting = allowConnecting;
        removeMouseMotionListener(this);
    }

    /**
     * Creates a new SignalViewer instance
     *
     * @param mediaUrl DOCUMENT ME!
     */
    public SignalViewer(URL mediaUrl) {
        this(mediaUrl.toExternalForm());
    }
    
    /**
     * Creates a new SignalViewer using the specified path as the media source.
     * 
     * @param mediaPath the path to the media source (WAV file)
     */
    public SignalViewer(String mediaPath) {
    	this();
		setMedia(mediaPath);
		paintBuffer();
		System.out.println("MediaUrl SignalViewer: " + mediaPath);
		createPopupMenu();
    }
    
    /**
     * Overrides <code>JComponent</code>'s processKeyBinding by always returning false.
     * Necessary for the proper working of (menu) shortcuts in Elan. 
     */
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		return false;
	}

    /**
     * Do some initialisation.
     */
    private void initViewer() {
        ruler = new TimeRuler(Constants.DEFAULTFONT, TimeFormatter.toString(0));
        rulerHeight = ruler.getHeight();
        timeRulerVisible = true;
        channelMode = STEREO_MERGED;
        msPerPixel = DEFAULT_MS_PER_PIXEL;
        samplesPerPixel = ((msPerPixel * 44100) / 1000); //default freq
        //resolution = PIXELS_FOR_SECOND;
        maxAmplitude = Short.MAX_VALUE;
        imageWidth = 0;
        imageHeight = 0;
        crossHairTime = 0;
        crossHairPos = 0;
        intervalBeginTime = 0;
        intervalEndTime = 0;
        dragStartTime = 0;
        selectionBeginTime = 0;
        selectionEndTime = 0;
        selectionBeginPos = 0;
        selectionEndPos = 0;
        alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
        alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
        currentPart = new WavePart(WavePart.INT_ARRAY_MODE);

        //currentPart = new WavePart(WavePart.GENERAL_PATH_MODE);
        timeScaleConnected = true;
        mediaOffset = 0L;
    }


    /**
     * Sets the source for this viewer.
     *
     * @param mediaPath the URL of the source as a String
     */
    public void setMedia(String mediaPath) {
    	// old test; if the file is video file try to find 
    	// a .wav file with the same name
		if (!mediaPath.endsWith("wav")) {
			int i = mediaPath.lastIndexOf('.');
			if (i > 0) {
				mediaPath = mediaPath.substring(0, i) + ".wav";
			} else {
				mediaPath = mediaPath + ".wav";
			}
		}

		if (mediaPath.startsWith("file:")) {
			mediaPath = mediaPath.substring(5);
		} else {
			// check protocol??
		}
		mediaFilePath = mediaPath;
        initLoad(mediaPath);
        paintBuffer();

        //repaint();
    }
    
    /**
     * Returns the path to the .wav file.
     * 
     * @return the path to the .wav file
     */
    public String getMediaPath() {
    	return mediaFilePath;
    }
    
    /**
     * Tries to start the Praat executable and select the specified interval.
     * 
     * @see http://www.fon.hum.uva.nl/praat/
     * @param begin the selection begin time
     * @param end the selection end time
     */
    private void openInPraat(long begin, long end) {
    	PraatConnection.openInPraat(mediaFilePath, begin, end);
    }
    
	/**
	 * Tries to start the Praat executable and select the current selection.
	 * 
	 * @see http://www.fon.hum.uva.nl/praat/
	 */
    private void openSelectionInPraat() {
    	if (getSelectionBeginTime() == getSelectionEndTime()) {
    		openInPraat(0, 0);
    	} else {
			openInPraat(getSelectionBeginTime() + mediaOffset, getSelectionEndTime() + mediaOffset);
    	}
    }
    
    /**
     * Tries to create a clip from the wav file using Praat.
     */
    private void clipSelectionWithPraat() {
    	if (getSelectionBeginTime() == getSelectionEndTime()) {
    		return;
    	} 
		PraatConnection.clipWithPraat(mediaFilePath, getSelectionBeginTime() + mediaOffset, getSelectionEndTime() + mediaOffset);
    }
    
    /**
     * Shows a formatted media info message.
     */
    private void showMediaInfo() {
    	String[][] info = new String[3][2];
    	
    	info[0][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaURL");
    	info[0][1] = mediaFilePath;
    	info[1][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaOffset");
    	info[1][1] = String.valueOf(mediaOffset);
    	info[2][0] = ElanLocale.getString("Player.duration");
    	info[2][1] = TimeFormatter.toString((long)samp.getDuration());
    	
    	new FormattedMessageDlg(info);
    }

    /**
     * Returns duration calculated from WAV-file (used within Corex)
     *
     * @return long
     */
    public long getSignalDuration() {
        return (long) samp.getDuration();
    }

    /**
     * DOCUMENT ME!
     *
     * @param selectionColor DOCUMENT ME!
     */
    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    /**
     * Initializes a <code>WaveSampler</code> for the given URL.
     * <code>WaveSampler</code> currently only takes a String for the source
     * location.
     *
     * @param sourcePath the URL of the source file as a String
     */
    private void initLoad(String sourcePath) {

        try {
            samp = new WAVSampler(sourcePath);
        } catch (IOException ioe) {
            System.out.println("Failed to create a WAVSampler");
            errorKey = ElanLocale.getString("SignalViewer.Message.NoReader") + ": " + ioe.getMessage();
        }

        if (samp != null) {
            samplesPerPixel = ((msPerPixel * samp.getSampleFrequency()) / 1000);
            maxAmplitude = Math.max(samp.getPossibleMaxSample(),
                    Math.abs(samp.getPossibleMinSample()));

            if (samp.getWavHeader().getNumberOfChannels() == 1) {
                channelMode = MONO;
            } else if (samp.getWavHeader().getNumberOfChannels() == 2) {
                channelMode = STEREO_SEPARATE;
            }
            short compr = samp.getWavHeader().getCompressionCode();
            if (compr != samp.getWavHeader().WAVE_FORMAT_UNCOMPRESSED && 
            		compr != samp.getWavHeader().WAVE_FORMAT_PCM && compr != samp.getWavHeader().WAVE_FORMAT_ALAW) {
            	errorKey = ElanLocale.getString("SignalViewer.Message.Compression");
            }
        }

        int w = Toolkit.getDefaultToolkit().getScreenSize().width;

        //start with loading twice the screenwidth into the WavePart
        //loadData(0L, SCREEN_BUFFER * w * msPerPixel, w);
        loadData(0L, (long) msPerPixel, w);
    }

    /**
     * Loads the wave data for the specified interval into the WavePart object.<br>
     * The WaveSampler is requested to read all bytes for the interval in one
     * turn. This method then takes care of the extraction of the values per
     * pixel.
     *
     * @param fromTime the interval starttime in ms, in the viewer's time space (ignores offset)
     * @param toTime the interval stoptime in ms, in the viewer's time space (ignores offset)
     * @param width the width in number of pixels
     *
     * @return true if loading was successful, false otherwise
     *
     * @see WAVSampler#readInterval(int, int)
     */
    private boolean loadData(long fromTime, long toTime, final int width) {
        //long start = System.currentTimeMillis();
        if (samp == null) {
            return false;
        }

        fromTime += mediaOffset;
        toTime += mediaOffset;

        if ((fromTime > samp.getDuration()) || (fromTime > toTime) ||
                (toTime < 0)) {
            return false;
        }

        long to = (toTime > (long) samp.getDuration())
            ? (long) samp.getDuration() : toTime;
        long from = (fromTime < mediaOffset) ? mediaOffset : fromTime;
        long startSample = (long) (from / msPerPixel);
        long stopSample = (long) (to / msPerPixel);
        int extent = (int) (stopSample - startSample) + 1;
        int size = (extent > width) ? extent : width;
        
        // HS apr 04: in order to achieve more precision in the calculation of 
        // the sample to seek, the samplesPerPixel value isn't used here anymore
        double firstSeeksample = from * samp.getSampleFrequency() / 1000f;
        //samp.seekSample(startSample * samplesPerPixel); // old
		//samp.seekSample((long)firstSeeksample);

        int samplesRead = 0;
        int startSampleInt = (int) startSample;
        int samplesPerPixelInt = (int) samplesPerPixel;
        
        int min;
        int max;
        boolean roundProb = ((samplesPerPixel * 10) % 10) != 0;
        
        switch (channelMode) {
        case MONO:

        //fallthrough, same as STEREO_MERGED
        //break;
        case STEREO_MERGED:
            currentPart.reset();
            currentPart.setInterval(from, to, startSampleInt, size, extent);
            if (roundProb) {
                int startPixel = (int) (from / msPerPixel);
                long startSamp = (long) (startPixel * samplesPerPixel);
                samp.seekSample(startSamp);
                samplesRead = samp.readInterval((int)(extent * samplesPerPixel), 1);

                   for (int i = 0; i < size; i++) {
                       int sample;
                       min = 0;
                       max = 0;
                       // calculate start sample index for next pixel
                       int index = 0;
                       if (i > 0) {
                           long sm = (long) ((startPixel + i) * samplesPerPixel);
                           index = (int) (sm - startSamp);
                       }
                       for (int j = 0; j <= samplesPerPixel && (j + index) < samp.getFirstChannelArray().length; j++) {
                           sample = samp.getFirstChannelArray()[j + index];

                           if (sample < min) {
                               min = sample;
                           } else if (sample > max) {
                               max = sample;
                           }
                       }
                       currentPart.addLineToFirstChannel(startSampleInt + i, -max, -min);
                   }
            } else {
            	samp.seekSample((long)firstSeeksample);
            	samplesRead = samp.readInterval((int) (extent * samplesPerPixel), 1);
	            // don't read more than the number of samples returned by readInterval
	            for (int i = 0, p = startSampleInt;
	                    (i < (samplesRead - samplesPerPixelInt)) &&
	                    (p < (startSampleInt + size)); i += samplesPerPixelInt, p++) {
	            	int sample;
	                min = 0;
	                max = 0;
	
	                for (int j = 0; j < samplesPerPixelInt; j++) {
	                    sample = samp.getFirstChannelArray()[i + j];
	
	                    if (sample < min) {
	                        min = sample;
	                    } else if (sample > max) {
	                        max = sample;
	                    }
	                }	
	                currentPart.addLineToFirstChannel(p, -max, -min);
	            }
            }

            break;

        case STEREO_BLENDED:

        //fallthrough
        case STEREO_SEPARATE:
            currentPart.reset();
            currentPart.setInterval(from, to, startSampleInt, size, extent);
			if (roundProb) {
				int startPixel = (int) (from / msPerPixel);
				long startSamp = (long) (startPixel * samplesPerPixel);
				samp.seekSample(startSamp);
				samplesRead = samp.readInterval((int) (extent * samplesPerPixel), 2);

				int min2;
				int max2;
				for (int i = 0; i < size; i++) {
					int sample;
					int sample2;
					min = 0;
					max = 0;
	                min2 = 0;
	                max2 = 0;
					// calculate start sample index for next pixel
					int index = 0;
					if (i > 0) {
						long sm = (long) ((startPixel + i) * samplesPerPixel);
						index = (int) (sm - startSamp);
					}
					for (int j = 0; j <= samplesPerPixel
							&& (j + index) < samp.getFirstChannelArray().length; j++) {
						sample = samp.getFirstChannelArray()[j + index];

						if (sample < min) {
							min = sample;
						} else if (sample > max) {
							max = sample;
						}
						
	                    sample2 = samp.getSecondChannelArray()[j + index];
	                	
	                    if (sample2 < min2) {
	                        min2 = sample2;
	                    } else if (sample2 > max2) {
	                        max2 = sample2;
	                    }
					}
					currentPart.addLineToFirstChannel(startSampleInt + i, -max,
							-min);
	                currentPart.addLineToRightChannel(startSampleInt + i, -max2, -min2);
				}
			} else {
            	samp.seekSample((long)firstSeeksample);
	            samplesRead = samp.readInterval((int) (extent * samplesPerPixel), 2);
	
	            int min2;
	            int max2;
	
	            // don't read more than the number of samples returned by readInterval
	            // the existence of two int arrays is guaranteed, even if there is no
	            // second channel in the sound file (0 values will be read in that case).
	            for (int i = 0, p = startSampleInt;
	                    (i < (samplesRead - samplesPerPixelInt)) &&
	                    (p < (startSampleInt + size)); i += samplesPerPixelInt, p++) {
	                int sample;
	                int sample2;
	                min = 0;
	                max = 0;
	                min2 = 0;
	                max2 = 0;
				  
	                for (int j = 0; j < samplesPerPixelInt; j++) {
	                    sample = samp.getFirstChannelArray()[i + j];
	
	                    if (sample < min) {
	                        min = sample;
	                    } else if (sample > max) {
	                        max = sample;
	                    }
	
	                    sample2 = samp.getSecondChannelArray()[i + j];
	
	                    if (sample2 < min2) {
	                        min2 = sample2;
	                    } else if (sample2 > max2) {
	                        max2 = sample2;
	                    }
	                }
	                
	                currentPart.addLineToFirstChannel(p, -max, -min);
	                currentPart.addLineToRightChannel(p, -max2, -min2);
	            }
            }

            break;

        default:
            break;
        }

        //System.out.println("SignalViewer: samples read: " + samplesRead);
        //System.out.println("Load time: " + (System.currentTimeMillis() - start) + " ms");
        return true;
    }
    
    /**
     * Compares the currently loaded interval with the new interval, shifts the remaining part
     * of the interval and loads a bit of additional data without resetting the current WavePart.
     * It is assumed it is already checked whether it makes sense to try to keep most of the loaded
     * data.
     * 
     * @param from time from which to load data, time value in media time space
     * @param to time to which load data, time value in media time space
     * 
     * @return true if loading was successful
     */
    private boolean shiftAndLoadData(long fromTime, long toTime) {
        if (samp == null) {
            return false;
        }
        
        if ((fromTime > samp.getDuration()) || (fromTime > toTime) ||
                (toTime < 0)) {
            return false;
        }
        if (toTime > samp.getDuration()) {
        	toTime = (long) samp.getDuration();
        }
        long oldStop = currentPart.getStopTime();
        long oldStart = currentPart.getStartTime();
        
        long startSample = (long) (fromTime / msPerPixel);
        long stopSample = (long) (toTime / msPerPixel);
        int extent = (int) (stopSample - startSample) + 1;
        //int size = (extent > imageWidth) ? extent : imageWidth;
        int size = extent;
        
        boolean roundProb = ((samplesPerPixel * 10) % 10) != 0;
        double firstSeeksample;
        int oldStartPixel = (int) (oldStart / msPerPixel);
        int newStartPixel = (int) startSample;
        long loadTime;
        
    	int distance = oldStartPixel - newStartPixel;
    	if (distance == 0) {
    		//System.out.println("Too little distance to reload...");
    		return true;
    	}
    	if (currentPart.rightOverlap(fromTime, toTime)) {
    		currentPart.shiftInterval(distance);

    		currentPart.setInterval(fromTime, toTime, (int) (fromTime / msPerPixel), 
    				size, extent);
    		startSample = (long) (oldStop / msPerPixel);
    		extent = (int) (stopSample - startSample) + 1;
    		size = extent;
    		loadTime = oldStop;
  		
            //firstSeeksample = oldStop * samp.getSampleFrequency() / 1000;
            //samp.seekSample(startSample * samplesPerPixel); // old
    		//samp.seekSample((long) firstSeeksample);

    	} else if (currentPart.leftOverlap(fromTime, toTime)) {
    		currentPart.shiftInterval(distance);

    		currentPart.setInterval(fromTime, toTime, (int) (fromTime / msPerPixel), 
    				size, extent);
    		//startSample = (long) (fromTime / msPerPixel);
    		stopSample = (long) (oldStart / msPerPixel);
    		extent = (int) (stopSample - startSample) + 1;// or without + 1
    		size = extent;
    		loadTime = fromTime;
            //firstSeeksample = fromTime * samp.getSampleFrequency() / 1000;
            //samp.seekSample(startSample * samplesPerPixel); // old
    		//samp.seekSample((long)firstSeeksample);
    	} else {
    		return false;
    	}
        // do the reading. NB now at large a copy of loadData, this bit might go into a separate method
    	int samplesRead = 0;
        int startSampleInt = (int) startSample;
        int samplesPerPixelInt = (int) samplesPerPixel;
        firstSeeksample = loadTime * samp.getSampleFrequency() / 1000;
        int min;
        int max;

        switch (channelMode) {
        case MONO:

        //fallthrough, same as STEREO_MERGED
        //break;
        case STEREO_MERGED:
        	if (roundProb) {
                int startPixel = (int) (loadTime / msPerPixel);
                long startSamp = (long) (startPixel * samplesPerPixel);
                samp.seekSample(startSamp);
                samplesRead = samp.readInterval((int)(extent * samplesPerPixel), 1);
                
                for (int i = 0; i < size; i++) {
                    int sample;
                    min = 0;
                    max = 0;
                    // calculate start sample index for next pixel
                    int index = 0;
                    if (i > 0) {
                        long sm = (long) ((startPixel + i) * samplesPerPixel);
                        index = (int) (sm - startSamp);
                    }
                    for (int j = 0; j <= samplesPerPixel && (j + index) < samp.getFirstChannelArray().length; j++) {
                        sample = samp.getFirstChannelArray()[j + index];

                        if (sample < min) {
                            min = sample;
                        } else if (sample > max) {
                            max = sample;
                        }
                    }
                    currentPart.addLineToFirstChannel(startSampleInt + i, -max, -min);
                }
        	} else {
        		samp.seekSample((long) firstSeeksample);      	
	            samplesRead = samp.readInterval((int)(extent * samplesPerPixel), 1);
	
	            // don't read more than the number of samples returned by readInterval
	            for (int i = 0, p = startSampleInt;
	                    (i < (samplesRead - samplesPerPixelInt)) &&
	                    (p < (startSampleInt + size)); i += samplesPerPixelInt, p++) {
	                int sample;
	                min = 0;
	                max = 0;
	
	                for (int j = 0; j < samplesPerPixelInt; j++) {
	                    sample = samp.getFirstChannelArray()[i + j];
	
	                    if (sample < min) {
	                        min = sample;
	                    } else if (sample > max) {
	                        max = sample;
	                    }
	                }
	
	                currentPart.addLineToFirstChannel(p, -max, -min);
	            }
        	}
            break;

        case STEREO_BLENDED:

        //fallthrough
        case STEREO_SEPARATE:
        	if (roundProb) {
				int startPixel = (int) (loadTime / msPerPixel);
				long startSamp = (long) (startPixel * samplesPerPixel);
				samp.seekSample(startSamp);
				samplesRead = samp.readInterval((int) (extent * samplesPerPixel), 2);

				int min2;
				int max2;
				for (int i = 0; i < size; i++) {
					int sample;
					int sample2;
					min = 0;
					max = 0;
	                min2 = 0;
	                max2 = 0;
					// calculate start sample index for next pixel
					int index = 0;
					if (i > 0) {
						long sm = (long) ((startPixel + i) * samplesPerPixel);
						index = (int) (sm - startSamp);
					}
					for (int j = 0; j <= samplesPerPixel
							&& (j + index) < samp.getFirstChannelArray().length; j++) {
						sample = samp.getFirstChannelArray()[j + index];

						if (sample < min) {
							min = sample;
						} else if (sample > max) {
							max = sample;
						}
						
	                    sample2 = samp.getSecondChannelArray()[j + index];
	                	
	                    if (sample2 < min2) {
	                        min2 = sample2;
	                    } else if (sample2 > max2) {
	                        max2 = sample2;
	                    }
					}
					currentPart.addLineToFirstChannel(startSampleInt + i, -max,
							-min);
	                currentPart.addLineToRightChannel(startSampleInt + i, -max2, -min2);
				}
			} else {
	        	samp.seekSample((long)firstSeeksample);
	            samplesRead = samp.readInterval((int) (extent * samplesPerPixel), 2);
	
	            int min2;
	            int max2;
	
	            // don't read more than the number of samples returned by readInterval
	            // the existence of two int arrays is guaranteed, even if there is no
	            // second channel in the sound file (0 values will be read in that case).
	            for (int i = 0, p = startSampleInt;
	                    (i < (samplesRead - samplesPerPixelInt)) &&
	                    (p < (startSampleInt + size)); i += samplesPerPixelInt, p++) {
	                int sample;
	                int sample2;
	                min = 0;
	                max = 0;
	                min2 = 0;
	                max2 = 0;
				  
	                for (int j = 0; j < samplesPerPixelInt; j++) {
	                    sample = samp.getFirstChannelArray()[i + j];
	
	                    if (sample < min) {
	                        min = sample;
	                    } else if (sample > max) {
	                        max = sample;
	                    }
	
	                    sample2 = samp.getSecondChannelArray()[i + j];
	
	                    if (sample2 < min2) {
	                        min2 = sample2;
	                    } else if (sample2 > max2) {
	                        max2 = sample2;
	                    }
	                }
	                
	                currentPart.addLineToFirstChannel(p, -max, -min);
	                currentPart.addLineToRightChannel(p, -max2, -min2);
	            }
			}

            break;

        default:
            break;
        }
        
		return true;
    }

    /**
     * Paint to the BufferedImage. This is necessary in the following situations:<br>
     * - the viewer has been resized<br>
     * - the timeline cursor has (been) moved out of the current interval<br>
     * - the msPerPixel has been changed<br>
     * - the media source has been changed??<br>
     * - the timeline cursor has (been) moved out of the loaded samples<br>
     * - the MONO - STEREO mode has been changed
     */
    private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if ((getWidth() != imageWidth) || (getHeight() != imageHeight)) {
            imageWidth = getWidth();
            imageHeight = getHeight();

            if (intervalEndTime == 0) {
                intervalEndTime = intervalBeginTime +
                    (int) (imageWidth * msPerPixel);

                if (timeScaleConnected) {
                    setGlobalTimeScaleIntervalEndTime(intervalEndTime);
                }
            }
        }

        synchronized (paintlock) {
            if ((bi == null) || (bi.getWidth() < imageWidth) ||
                    (bi.getHeight() < imageHeight)) {
                bi = new BufferedImage(imageWidth, imageHeight,
                        BufferedImage.TYPE_INT_RGB);
                big2d = bi.createGraphics();
            }
            if (SystemReporting.antiAliasedText) {
	            big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            //check whether we have to load a new interval
            // HS March 2011 include media offset in comparison!
            long mediaBT = intervalBeginTime + mediaOffset;
            long mediaET = intervalEndTime + mediaOffset;
            if (mediaET > samp.getDuration()) {
            	mediaET = (long) samp.getDuration();
            }
            
            if (!currentPart.contains(mediaBT, mediaET)) {
            	//System.out.println("Overlap: " + currentPart.amountOfOverlap(mediaBT, mediaET));
            	//System.out.println("Left: " + currentPart.leftOverlap(mediaBT, mediaET));
            	//System.out.println("Right: " + currentPart.rightOverlap(mediaBT, mediaET));
            	//System.out.println("Same length: " + currentPart.sameIntervalLength(mediaBT, mediaET));
                boolean loaded = false;
            	long begin = System.currentTimeMillis();
            	
            	if (currentPart.amountOfOverlap(mediaBT, mediaET) > 0.8f && 
                		currentPart.sameIntervalLength(mediaBT, mediaET)) {
                	// shift and load additional
            		loaded = shiftAndLoadData(mediaBT, mediaET);
                }
                
            	if (!loaded) {
	            	loaded = loadData(intervalBeginTime,
	                        intervalBeginTime +
	                        (int) (SCREEN_BUFFER * imageWidth * msPerPixel), imageWidth);
            	}
            	//System.out.println("Loadtime: " + (System.currentTimeMillis() - begin));
                // loadData only returns false in case of a severe error

                /*
                   if (!loaded) {
                       //return;
                       currentPart.reset();
                       currentPart.setStartTime(0L);
                       currentPart.setStopTime(0L);
                   }
                 */
            }

            int channelHeight;
            int intervalX1 = (int) (intervalBeginTime / msPerPixel);

            //int intervalX2 = (int)(intervalEndTime / msPerPixel);
            // since there can be a media offset (i.e. the global begin time and the media begin
            // time are not the same) we need different translations for the ruler etc
            // and the wave part(s)
            int waveIntervalX1 = (int) ((intervalBeginTime + mediaOffset) / msPerPixel);
            //int waveIntervalX2 = (int) ((intervalEndTime + mediaOffset) / msPerPixel);
            int waveIntervalX2 = (int) (mediaET / msPerPixel);
            AffineTransform at = new AffineTransform();

            //paint the background color
            big2d.setComposite(AlphaComposite.Src);
            big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
            big2d.fillRect(0, 0, imageWidth, imageHeight);

            // mark the area beyond the media time
            if (intervalEndTime > getMediaDuration()) {
                int xx = xAt(getMediaDuration());
                if (!SystemReporting.isMacOS()) {
                	big2d.setColor(UIManager.getColor("Panel.background"));// problems on the mac
                } else {
                	big2d.setColor(Color.LIGHT_GRAY);
                }
                big2d.fillRect(xx, 0, imageWidth - xx, bi.getHeight());
            }

            
            /*paint time ruler */
            if (timeRulerVisible) {
	            big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
	            big2d.translate(-intervalX1, 0.0);
	            ruler.paint(big2d, intervalBeginTime, imageWidth, msPerPixel,
	                SwingConstants.TOP);
	            big2d.translate(intervalX1 - waveIntervalX1, 0.0);
            } else {
            	big2d.translate(-waveIntervalX1, 0.0);
            }
            
            ///end ruler
            // paint the wave(s)
            switch (channelMode) {
            case MONO:

            //fallthrough
            case STEREO_MERGED:

                //one merged channel for the wave stored in WavePart.firstPath
                channelHeight = imageHeight - rulerHeight;

                int channelMid = rulerHeight + Math.round(channelHeight / 2f);
                big2d.translate(0.0, channelMid);
                big2d.setColor(Color.DARK_GRAY);
                big2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
                big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
                at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));

                /* Graphics2D.draw(Shape.createTransformedShape(AffineTransform)) seems to be faster */

                //big2d.transform(at);
                //big2d.draw(currentPart.getFirstPath().createTransformedShape(at));
                currentPart.paintLeftChannelLimit(big2d, at, channelHeight / 2);
                big2d.translate(0.0, -channelMid);

                // ALBERT
                int leftTop = rulerHeight;
                int rightTop = rulerHeight;
                //int channelHeight = (rulerHeight + imageHeight) / 2;
                drawSegmentation(big2d, leftTop, leftTop + channelHeight, rightTop, rightTop + channelHeight);
                // END ALBERT
                
                break;

            case STEREO_SEPARATE:
                channelHeight = (imageHeight - rulerHeight - GAP) / 2;

                int leftChannelMid = rulerHeight +
                    (int) (Math.ceil(channelHeight / 2f));
                int rightChannelMid = imageHeight -
                    (int) (Math.ceil(channelHeight / 2f));

                // decoration
                big2d.setColor(Constants.SIGNALCHANNELCOLOR);
                big2d.fillRect(waveIntervalX1, rulerHeight, imageWidth + 2,
                    channelHeight);
                big2d.fillRect(waveIntervalX1, imageHeight - channelHeight,
                    imageWidth + 2, channelHeight);
                big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);

                //
                big2d.translate(0.0, leftChannelMid);
                big2d.setColor(Color.DARK_GRAY);
                big2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
                big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
                at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));

                //big2d.draw(currentPart.getFirstPath().createTransformedShape(at));//expensive??
                // big2d.setClip(0, -leftChannelMid + rulerHeight, imageWidth + 2, channelHeight);
                currentPart.paintLeftChannelLimit(big2d, at, channelHeight / 2);
                // big2d.setClip(null);
                big2d.translate(0.0, rightChannelMid - leftChannelMid);
                big2d.setColor(Color.DARK_GRAY);
                big2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
                big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);

                //big2d.draw(currentPart.getSecondPath().createTransformedShape(at));
                // big2d.setClip(0, -leftChannelMid + rulerHeight, imageWidth + 2, channelHeight);
                currentPart.paintRightChannelLimit(big2d, at, channelHeight / 2);

                big2d.translate(0.0, -rightChannelMid);
                // big2d.setClip(null);
             
                // ALBERT
                leftTop = rulerHeight;
                rightTop = leftTop + channelHeight + GAP;
                //int channelHeight = (rulerHeight + imageHeight) / 2;
                drawSegmentation(big2d, leftTop, leftTop + channelHeight, rightTop, rightTop + channelHeight);
                // END ALBERT
                
                break;

            case STEREO_BLENDED:
                channelHeight = imageHeight - rulerHeight;

                int chMid = rulerHeight + Math.round(channelHeight / 2f);
                big2d.translate(0.0, chMid);
                big2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
                at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));
                big2d.setColor(Constants.SIGNALSTEREOBLENDEDCOLOR1);

                //big2d.draw(currentPart.getFirstPath().createTransformedShape(at));
                currentPart.paintLeftChannelLimit(big2d, at, channelHeight / 2);

                big2d.setColor(Constants.SIGNALSTEREOBLENDEDCOLOR2);
                big2d.setComposite(alpha04);

                //big2d.draw(currentPart.getSecondPath().createTransformedShape(at));
                currentPart.paintRightChannelLimit(big2d, at, channelHeight / 2);
                big2d.setComposite(AlphaComposite.Src);
                big2d.translate(0.0, -chMid);

//              ALBERT
                leftTop = rulerHeight;
                rightTop = rulerHeight;
                //int channelHeight = (rulerHeight + imageHeight) / 2;
                drawSegmentation(big2d, leftTop, leftTop + channelHeight, rightTop, rightTop + channelHeight);
                // END ALBERT
                
                break;

            default:
                ;
            }

            drawCuePoints(big2d);
            big2d.setTransform(new AffineTransform()); //reset transform
        }
        if (timeRulerVisible) {
        	big2d.setColor(Constants.SELECTIONCOLOR);
        	big2d.drawLine(0, rulerHeight, imageWidth, rulerHeight);
        }
         // end synchronized block

        repaint();

    }



    //  ALBERT
    /**
     * Draws segmentation
     */
    private void drawSegmentation(Graphics2D big2d, int imageTopLeft, int imageBottomLeft, int imageTopRight, int imageBottomRight) {
        // imageWidth, msPerPixel. intervalBeginTime, intervalEndTime are supposed to be correct as a side effect from drawImage
    	AffineTransform aft = big2d.getTransform();
        if (segmentationChannel1 != null && showSegmentationChannel1) {
        	java.awt.font.FontRenderContext frc = big2d.getFontRenderContext();
        	// set explicit label font?
	        for (int i = 0; i < imageWidth; i++) {
	        	long from = intervalBeginTime + (int) (i * msPerPixel);
	        	long to = (long) (from + msPerPixel);
	        	Boundary boundary = segmentationChannel1.boundaryBetween(from, to);
	        	String label = null;
	        	if (boundary != null) {
	        		label = boundary.label;
	        	}
	        	int x = (int)(i - aft.getTranslateX());
	        	if (label != null) {
	        		big2d.setColor(Constants.SEGMENTATIONCOLOR);
	        		big2d.drawLine(x, imageTopLeft, x, imageBottomLeft); 
	        		if (!label.equals("")) {
		                java.awt.geom.Rectangle2D rect = big2d.getFont().getStringBounds(label, frc);
		                big2d.setColor(Color.white);
		                big2d.fill3DRect(x + 3, imageBottomLeft - (int)rect.getHeight() - 2, (int)rect.getWidth() + 6, (int)rect.getHeight() + 2, true);
		                big2d.setColor(Color.black); 
		                big2d.drawString(label, x + 5, imageBottomLeft - 3);
	        		}
	        	}
	        }
        }
        if (segmentationChannel2 != null && showSegmentationChannel2) {
        	java.awt.font.FontRenderContext frc = big2d.getFontRenderContext();
        	// set explicit label font?
	        for (int i = 0; i < imageWidth; i++) {
	        	long from = intervalBeginTime + (int) (i * msPerPixel);
	        	long to = (long) (from + msPerPixel);
	        	Boundary boundary = segmentationChannel2.boundaryBetween(from, to);
	        	String label = null;
	        	if (boundary != null) {
	        		label = boundary.label;
	        	}
	        	int x = (int)(i - aft.getTranslateX());
	        	if (label != null) {
	        		big2d.setColor(Constants.SEGMENTATIONCOLOR);
	        		big2d.drawLine(x, imageTopRight, x, imageBottomRight); 
	        		if (!label.equals("")) {
		                java.awt.geom.Rectangle2D rect = big2d.getFont().getStringBounds(label, frc);
		                big2d.setColor(Color.white);
		                big2d.fill3DRect(x + 3, imageBottomRight - (int)rect.getHeight() - 2, (int)rect.getWidth() + 6, (int)rect.getHeight() + 2, true);
		                big2d.setColor(Color.black); 
		                big2d.drawString(label, x + 5, imageBottomRight - 3);
	        		}
	        	}
	        }
        }
    }

    /**
     * The left segmentation is also used for mono signals
     */
    public void setSegmentation(BoundarySegmentation segmentation) {
    	segmentationChannel1 = segmentation;
    	if (segmentation != null) {
        	segmentationChannel2 = null;
			segmentationChannel1Item.setSelected(true);
	    	showSegmentationChannel1 = true;
    	} else {
			segmentationChannel1Item.setSelected(false);
	    	showSegmentationChannel1 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }

    public void setSegmentationChannel1(BoundarySegmentation segmentation) {
    	segmentationChannel1 = segmentation;
    	if (segmentationChannel1 != null) {
			segmentationChannel1Item.setSelected(true);
			showSegmentationChannel1 = true;
    	} else {
			segmentationChannel1Item.setSelected(false);
			showSegmentationChannel1 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }
    
    public void setSegmentationChannel2(BoundarySegmentation segmentation) {
    	segmentationChannel2 = segmentation;
    	if (segmentationChannel2 != null && segmentationChannel2Item != null) {
			segmentationChannel2Item.setSelected(true);
			showSegmentationChannel2 = true;
    	} else if (segmentationChannel2Item != null) {
			segmentationChannel2Item.setSelected(false);
			showSegmentationChannel2 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }
    
    public void enableSegmentationMenu() {
    	if (segmentationChannel1 != null) {
    		segmentationMenu.setEnabled(true);
    		segmentationChannel1Item.setEnabled(true);
    	} else {
    		segmentationChannel1Item.setEnabled(false);
    	}
    	if (segmentationChannel2 != null) {
    		segmentationMenu.setEnabled(true);
    		segmentationChannel2Item.setEnabled(true);
    	} else if (getChannelMode() != MONO) {
    		segmentationChannel2Item.setEnabled(false);
    	}
    	if (segmentationChannel1 == null && segmentationChannel2 == null) {
    		segmentationMenu.setEnabled(false);
    	}
    }
    // END ALBERT
    

    /**
     * Draws cue points if there are present in wav file
     *
     * @param big2g
     */
    private void drawCuePoints(Graphics2D big2g) {
    	if (samp == null) {
    		return;
		}
		
        WAVCuePoint[] cuePoints = samp.getWavHeader().getCuePoints();

        if (cuePoints.length > 0) {
            big2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] { 4.0f }, 0.0f));

            big2d.setColor(Color.darkGray);

            for (int i = 0; i < cuePoints.length; i++) {
                int time = (int) samp.getTimeAtSample(cuePoints[i].getSampleOffset());
                int x = (int) (time / msPerPixel);

                if ((intervalBeginTime <= time) && (time < intervalEndTime)) {
                    big2d.drawLine(x, rulerHeight, x, imageHeight);

                    String label = cuePoints[i].getLabel();
                    String note = cuePoints[i].getNote();

                    if ((label != null) && (note != null)) {
                        label += " : ";
                    }

                    big2d.drawString(((label != null) ? label
                                                      : new Integer(i).toString()) +
                        ((note != null) ? note : ""), x + 1, imageHeight - 1);
                }
            }

            big2d.setStroke(new BasicStroke());
        }
    }

    /**
     * Override <code>JComponent</code>'s paintComponent to paint:<br>
     * - a BufferedImage with a ruler and the waveform<br>
     * - the current selection<br>
     * - the cursor / crosshair
     *
     * @param g DOCUMENT ME!
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        int h = getHeight();

        if (bi != null) {
            //synchronized (paintlock) {
            g2d.drawImage(bi, 0, 0, this);

            //}		
        }

        //paint selection
        if (selectionBeginPos != selectionEndPos) {
            g2d.setColor(selectionColor);
            g2d.setComposite(alpha04);
            g2d.fillRect(selectionBeginPos, 0,
                (selectionEndPos - selectionBeginPos), rulerHeight);
            g2d.setComposite(alpha07);
            g2d.fillRect(selectionBeginPos, rulerHeight,
                (selectionEndPos - selectionBeginPos), h - rulerHeight);
            g2d.setComposite(AlphaComposite.Src);
        }

        //draw the cursor
        g2d.setColor(Constants.CROSSHAIRCOLOR);
        g2d.drawLine(crossHairPos, 0, crossHairPos, h);
        if (errorKey != null) {
        	g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
        	g2d.drawString(errorKey, 10, 20 + rulerHeight);
        }
    }

    /**
     * Implements updateTimeScale from TimeScaleBasedViewer to adjust the
     * TimeScale if needed and when in TimeScale connected mode.<br>
     * Checks the GlobalTimeScaleIntervalBeginTime and
     * GlobalTimeScaleMsPerPixel and adjusts the interval and resolution of
     * this viewer when they differ from the global values.<br>
     * For the time being assume that the viewer is notified only once when
     * the resolution or the interval begintime has changed.
     */
    public void updateTimeScale() {
        if (timeScaleConnected) {
            // if the resolution is changed recalculate the begin time
            if (getGlobalTimeScaleMsPerPixel() != msPerPixel) {
                setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
            } else if (getGlobalTimeScaleIntervalBeginTime() != intervalBeginTime) {
                //assume the resolution has not been changed
                setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());

                //System.out.println("update begin time in SignalViewer called");
            }
        }
    }

    /**
     * Sets whether or not this viewer listens to global time scale updates.
     *
     * @param connected the new timescale connected value
     */
    public void setTimeScaleConnected(boolean connected) {
        timeScaleConnected = connected;

        if (timeScaleConnected) {
            if (msPerPixel != getGlobalTimeScaleMsPerPixel()) {
                setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
            }

            if (intervalBeginTime != getGlobalTimeScaleIntervalBeginTime()) {
                setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());
            }
        }
    }

    /**
     * Gets whether this viewer listens to time scale updates from other
     * viewers.
     *
     * @return true when connected to global time scale values, false otherwise
     */
    public boolean getTimeScaleConnected() {
        return timeScaleConnected;
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * milliseconds per pixel value globally or locally.<br>
     * On setting the global MsPerPixel {@link #updateTimeScale()} will be
     * called and the local msPerPixel value will be updated also.
     *
     * @param mspp the new milliseconds per pixel value
     */
    public void setMsPerPixel(float mspp) {
        if (timeScaleConnected) {
            setGlobalTimeScaleMsPerPixel(mspp);
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleMsPerPixel(mspp);
        }
    }

    /**
     * Change the horizontal resolution or zoomlevel locally. The msPerPixel
     * denotes the number of milliseconds of which the samples should be
     * merged to one value. It corresponds to one pixel in image space (a
     * pixel is the smallest unit in image space).<br>
     * The position on the screen of crosshair cursor should change as little
     * as possible.<br>
     * This is calculated as follows:<br>
     * The absolute x coordinate in image space is the current media time
     * divided by the new msPerPixel.<br>
     * <pre>
     * |----------|----------|-------x--|-- <br>
     * |imagesize |                  | absolute x coordinate of media time<br>
     * |    1     |    2     |    3     |
     * </pre>
     * Calculate the number of screen images that fit within the absolute x
     * coordinate. The new position on the screen would then be the absolute x
     * coordinate minus the number of screen images multiplied by the image
     * width. The difference between the old x value and the new x value is
     * then used to calculate the new interval start time.<br>
     * The new start time = (number of screen images  image width -
     * difference)  msPerPixel.
     *
     * @param step the new horizontal zoomlevel
     */
    private void setLocalTimeScaleMsPerPixel(float step) {
        if (msPerPixel == step) {
            return;
        }
        if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }

        if (currentPart != null) {
        	currentPart.setDrawExtremesContour(msPerPixel <= 0.5f);
        }
        
        //resolution = (int) (1000f / msPerPixel);

        /*stop the player if necessary*/
        boolean playing = playerIsPlaying();

        if (playing) {
            stopPlayer();
        }

        long mediaTime = getMediaTime();
        int oldScreenPos = crossHairPos;
        int newMediaX = (int) (mediaTime / msPerPixel);
        int numScreens = (int) (mediaTime / (imageWidth * msPerPixel));
        int newScreenPos = newMediaX - (numScreens * imageWidth);
        int diff = oldScreenPos - newScreenPos;

        //new values
        intervalBeginTime = (long) (((numScreens * imageWidth) - diff) * msPerPixel);

        if (intervalBeginTime < 0) {
            intervalBeginTime = 0;
        }

        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
        crossHairPos = xAt(mediaTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());

        if (samp != null) {
            samplesPerPixel = ((msPerPixel * samp.getSampleFrequency()) / 1000);
            //System.out.println("Samples per pixel int: " + samplesPerPixel);
            //System.out.println("Samples per pixel float: " + ((msPerPixel * samp.getSampleFrequency()) / 1000));
        }

        //force to reload data
        currentPart.setStartTime(0L);
        currentPart.setStopTime(0L);
        paintBuffer();

        if (playing) {
            startPlayer();
        }

        int zoom = (int) (100f * (10f / msPerPixel));

        if (zoom <= 0) {
            zoom = 100;
        }
        
        updateZoomPopup(zoom);
        
        setPreference("SignalViewer.ZoomLevel", new Float(zoom), 
        		getViewerManager().getTranscription());

        //repaint();
    }

    /**
     * Returns the current msPerPixel.
     *
     * @return msPerPixel
     */
    public float getMsPerPixel() {
        return msPerPixel;
    }

    /**
     * Calls #setMsPerPixel with the appropriate value. In setMsPerPixel the
     * value of this.resolution is actually set. msPerPixel = 1000 / resolution<br>
     * resolution = 1000 / msPerPixel
     *
     * @param resolution the new resolution
     */
    /*
    public void setResolution(int resolution) {
        this.resolution = resolution;

        int mspp = (int) (1000f / resolution);
        setMsPerPixel(mspp);
    }
	*/
    /**
     * Sets the resolution by providing a factor the default PIXELS_FOR_SECOND
     * should be multiplied with.<br>
     * resolution = factor  PIXELS_FOR_SECOND.<br>
     * <b>Note:</b><br>
     * The factor = 100 / resolution_menu_percentage !
     *
     * @param factor the multiplication factor
     */
    /*
    public void setResolutionFactor(float factor) {
        int res = (int) (PIXELS_FOR_SECOND * factor);
        setResolution(res);
    }
	*/
    /**
     * Gets the current resolution
     *
     * @return the current resolution
     */
    /*
    public int getResolution() {
        return resolution;
    }
    */

    /**
     * Gets the current channel display mode.
     *
     * @return the current channel mode, one of MONO, STEREO_SEPARATE,
     *         STEREO_MERGED or STEREO_BLENDED
     */
    public int getChannelMode() {
        return channelMode;
    }

    /**
     * Sets the channel display mode.
     *
     * @param mode the new channel mode
     */
    public void setChannelMode(int mode, boolean storePref) {
        if (mode == channelMode) {
            return;
        }

        if ((mode <= MONO) || (mode > STEREO_BLENDED)) {
            channelMode = MONO;
        } else {
            if ((samp != null) &&
                    (samp.getWavHeader().getNumberOfChannels() != 2)) {
            	channelMode = MONO;
                return;
            }

            channelMode = mode;
        }
        if (storePref) {
	        setPreference("SignalViewer.StereoMode", new Integer(channelMode), 
	        		getViewerManager().getTranscription());
        }
        paintBuffer();
    }

    
    /**
	 * @return the timeRulerVisible
	 */
	public boolean isTimeRulerVisible() {
		return timeRulerVisible;
	}


	/**
	 * Sets the visibility of the time ruler. Preferences are not updated.
	 * 
	 * @param timeRulerVisible the timeRulerVisible to set
	 */
	public void setTimeRulerVisible(boolean timeRulerVisible) {
		this.timeRulerVisible = timeRulerVisible;
		if (timeRulerVisMI != null && timeRulerVisMI.isSelected() != timeRulerVisible) {
			timeRulerVisMI.setSelected(timeRulerVisible);
		}

		if (timeRulerVisible) {
			rulerHeight = ruler.getHeight();
		} else {
			rulerHeight = 0;
		}
		paintBuffer();
	}


	/**
     * Returns the x-coordinate for a specific time. The coordinate is in the
     * component's coordinate system.
     *
     * @param t time
     *
     * @return int the x-coordinate for the specified time
     */
    public int xAt(long t) {
    	return (int) ((t / msPerPixel) - (intervalBeginTime / msPerPixel));
        //return (int) ((t - intervalBeginTime) / msPerPixel);
    }

    /**
     * Returns the time in ms at a given position in the current image. The
     * given x coordinate is in the component's ("this") coordinate system.
     * The interval begin time is included in the calculation of the time at
     * the given coordinate.
     *
     * @param x x-coordinate
     *
     * @return the mediatime corresponding to the specified position
     */
    public long timeAt(int x) {
        return intervalBeginTime + (long) (x * msPerPixel);
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current interval begin time
     */
    public long getIntervalBeginTime() {
        return intervalBeginTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current interval end time
     */
    public long getIntervalEndTime() {
        return intervalEndTime;
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * interval begin time globally or locally.<br>
     * On setting the global interval begin time {@link #updateTimeScale()}
     * will be called and the local intervalBeginTime will be updated also.
     *
     * @param begin the new interval begin time
     */
    public void setIntervalBeginTime(long begin) {
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(begin);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(begin);
        }
    }
    
    public void setRecalculateInterval(boolean recalculate){
    	recalculateInterval = recalculate;
    }

    /**
     * Calculates the new interval begin and/or end time.<br>
     * There are two special cases taken into account:<br>
     * 
     * <ul>
     * <li>
     * when the player is playing attempts are made to shift the interval
     * <i>n</i> times the interval size to the left or to the right, until the
     * new interval contains the new mediatime.
     * </li>
     * <li>
     * when the player is not playing and the new interval begin time coincides
     * with the selection begin time, the interval is shifted a certain offset
     * away from the image edge. Same thing when the interval end time
     * coincides with the selection end time.
     * </li>
     * </ul>
     * 
     *
     * @param mediaTime
     */
    private void recalculateInterval(final long mediaTime) {    	
    	if(!recalculateInterval){
    		return;
    	}
        long newBeginTime = intervalBeginTime;
        long newEndTime = intervalEndTime;

        if (playerIsPlaying()) {
            // we might be in a selection outside the new interval
            // shift the interval n * intervalsize to the left or right
            if (mediaTime > intervalEndTime) {
                newBeginTime = intervalEndTime;
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);

                while ((newEndTime += (imageWidth + msPerPixel)) < mediaTime) {
                    newBeginTime += (imageWidth * msPerPixel);
                }
            } else if (mediaTime < intervalBeginTime) {
                newEndTime = intervalBeginTime;
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                while ((newEndTime -= (imageWidth * msPerPixel)) > mediaTime) {
                    newBeginTime -= (imageWidth * msPerPixel);
                }

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                    newEndTime = (long) (imageWidth * msPerPixel);
                }
            } else {
                // the new time appears to be in the current interval after all
                return;
            }
        } else { //player is not playing

            // is the new media time to the left or to the right of the current interval
            if (mediaTime < intervalBeginTime) {
                newBeginTime = mediaTime - (int) (SCROLL_OFFSET * msPerPixel);

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                }

                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            } else if (mediaTime > intervalEndTime){
                newEndTime = mediaTime + (int) (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // somehing would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }

            if ((newBeginTime == getSelectionBeginTime()) &&
                    (newBeginTime > (SCROLL_OFFSET * msPerPixel))) {
                newBeginTime -= (SCROLL_OFFSET * msPerPixel);
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            }

            if (newEndTime == getSelectionEndTime()) {
                newEndTime += (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }
            
        }

        if (timeScaleConnected) {
            //System.out.println("SV new begin time: " + newBeginTime);
            //System.out.println("SV new end time: " + newEndTime);
            setGlobalTimeScaleIntervalBeginTime(newBeginTime);
            setGlobalTimeScaleIntervalEndTime(newEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(newBeginTime);
        }
    }

    /**
     * Changes the interval begin time locally.
     *
     * @param begin the new local interval begin time
     */
    private void setLocalTimeScaleIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }

        intervalBeginTime = begin;
        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);

        crossHairPos = xAt(crossHairTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());

        //System.out.println("SV begin:" + intervalBeginTime);
        paintBuffer();
    }

    /**
     * Create a popup menu to enable the manipulation of some settings for this
     * viewer.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu("Signal Viewer");

        JMenu zoomMI = new JMenu(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
        zoomBG = new ButtonGroup();
        zoomSelectionMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Selection"));
        zoomSelectionMI.addActionListener(this);
        zoomSelectionMI.setActionCommand("zoomSel");
        zoomMI.add(zoomSelectionMI);
        customZoomMI = new JRadioButtonMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
        customZoomMI.setEnabled(false);
        zoomBG.add(customZoomMI);
        zoomMI.add(customZoomMI);
        zoomMI.addSeparator();
        //
        JRadioButtonMenuItem zoomRB;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            zoomRB = new JRadioButtonMenuItem(ZOOMLEVELS[i] + "%");
            zoomRB.setActionCommand(String.valueOf(ZOOMLEVELS[i]));
            zoomRB.addActionListener(this);
            zoomBG.add(zoomRB);
            zoomMI.add(zoomRB);

            if (ZOOMLEVELS[i] == 100) {
                zoomRB.setSelected(true);
            }
        }

        popup.add(zoomMI);
        
        JMenu vertZoomMenu = new JMenu(ElanLocale.getString("SignalViewer.VertZoom"));
        vertZoomGroup = new ButtonGroup();
        
        for (int i = 0; i < VERT_ZOOM.length; i++) {
            zoomRB = new JRadioButtonMenuItem(VERT_ZOOM[i] + "%");
            zoomRB.setActionCommand("vz-" + VERT_ZOOM[i]);
            zoomRB.addActionListener(this);
            vertZoomGroup.add(zoomRB);
            vertZoomMenu.add(zoomRB);
            if (vertZoom == VERT_ZOOM[i]) {
                zoomRB.setSelected(true);
            }
        }
        popup.add(vertZoomMenu);

        if ((samp != null) && (samp.getWavHeader().getNumberOfChannels() == 2)) {
            ButtonGroup chanGroup = new ButtonGroup();
            JMenu channelMI = new JMenu(ElanLocale.getString(
                        "SignalViewer.Stereo"));
            separateMI = new JRadioButtonMenuItem(ElanLocale.getString(
                        "SignalViewer.Stereo.Separate"));
            separateMI.setActionCommand("sep");
            separateMI.addActionListener(this);
            chanGroup.add(separateMI);
            channelMI.add(separateMI);
            if (channelMode == STEREO_SEPARATE) {
                separateMI.setSelected(true);
            }

            mergedMI = new JRadioButtonMenuItem(ElanLocale.getString(
                        "SignalViewer.Stereo.Merged"));
            mergedMI.setActionCommand("merge");
            mergedMI.addActionListener(this);
            chanGroup.add(mergedMI);
            channelMI.add(mergedMI);
            if (channelMode == STEREO_MERGED) {
                mergedMI.setSelected(true);
            }

            blendMI = new JRadioButtonMenuItem(ElanLocale.getString(
                        "SignalViewer.Stereo.Blended"));
            blendMI.setActionCommand("blend");
            blendMI.addActionListener(this);
            chanGroup.add(blendMI);
            channelMI.add(blendMI);
            if (channelMode == STEREO_BLENDED) {
                blendMI.setSelected(true);
            }
            popup.add(channelMI);
        }
        timeRulerVisMI = new JCheckBoxMenuItem(ElanLocale.getString(
			"TimeScaleBasedViewer.TimeRuler.Visible"));
        timeRulerVisMI.setSelected(timeRulerVisible);
        timeRulerVisMI.addActionListener(this);
        popup.add(timeRulerVisMI);
        
        if (allowConnecting) {
            timeScaleConMI = new JCheckBoxMenuItem(ElanLocale.getString(
                        "TimeScaleBasedViewer.Connected"), timeScaleConnected);
            timeScaleConMI.setActionCommand("connect");
            timeScaleConMI.addActionListener(this);
            popup.add(timeScaleConMI);
        }

        popup.addSeparator();
        
        JMenuItem praatMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.File"));
		praatMI.setActionCommand("praat");
		praatMI.addActionListener(this);
		popup.add(praatMI);
		
		praatSelMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.Selection"));
		praatSelMI.setActionCommand("praatSel");
		praatSelMI.addActionListener(this);
		popup.add(praatSelMI);
		
		clipSelPraatMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.Clip"));
		clipSelPraatMI.setActionCommand("clipSel");
		clipSelPraatMI.addActionListener(this);
		popup.add(clipSelPraatMI);

		if (getSelectionBeginTime() == getSelectionEndTime()) {
			praatSelMI.setEnabled(false);
			clipSelPraatMI.setEnabled(false);
		} else {
			praatSelMI.setEnabled(true);
			clipSelPraatMI.setEnabled(true);
		}
		
		//ALBERT
		if (samp != null) {
	        popup.addSeparator();
	        segmentationMenu = new JMenu(ElanLocale.getString("SignalViewer.Segmentation"));
	        
			if (samp.getWavHeader().getNumberOfChannels() == 2) {
		        segmentationChannel1Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.LeftVisible"), showSegmentationChannel1); 
		        segmentationChannel1Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel1Item);
		        segmentationChannel2Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.RightVisible"), showSegmentationChannel2); 
		        segmentationChannel2Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel2Item);
		        popup.add(segmentationMenu);
				
				segmentationMenu.setEnabled(false);
				segmentationChannel1Item.setEnabled(false);
				segmentationChannel2Item.setEnabled(false);
			} else {
		        segmentationChannel1Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.Visible"), showSegmentationChannel1); 
		        segmentationChannel1Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel1Item);
		        popup.add(segmentationMenu);
				
				segmentationMenu.setEnabled(false);
				segmentationChannel1Item.setEnabled(false);
			}
		}
        
		if (allowConnecting) {
			popup.addSeparator();
			infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
			infoItem.setActionCommand("info");
			infoItem.addActionListener(this);
			popup.add(infoItem);
		}
		copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
		copyOrigTimeItem.addActionListener(this);
		popup.add(copyOrigTimeItem);
		
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        int zoom = (int) (100f * (10f / msPerPixel));

        if (zoom <= 0) {
            zoom = 100;
        }

        updateZoomPopup(zoom);
    }

    /**
     * Updates the "zoom" menu item. Needed, when timeScaleConnected, after a
     * change of the zoomlevel in some other connected viewer.
     *
     * @param zoom the zoom level
     */
    private void updateZoomPopup(int zoom) {
        // first find the closest match (there can be rounding issues)
    	// 76 == 75%, 166 == 150%
        int zoomMenuIndex = -1;
//        if (zoom == 76) {
//        	zoom = 75;
//        } else if (zoom == 166) {
//        	zoom = 150;
//        }
        //int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
        	/*
            int d = Math.abs(ZOOMLEVELS[i] - zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
            */
        	if (zoom - ZOOMLEVELS[i] >= -1 && zoom - ZOOMLEVELS[i] <= 1) {
        		zoomMenuIndex = i;
        		break;
        	}
        }

        if (popup != null) {
            java.util.Enumeration en = zoomBG.getElements();
            int counter = 0;

            while (en.hasMoreElements()) {
                JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) en.nextElement();

                if (counter == zoomMenuIndex + 1) {
                    rbmi.setSelected(true);

                    break;
                } else {
                    rbmi.setSelected(false);
                }

                counter++;
            }
            if (zoomMenuIndex == -1) {
            	customZoomMI.setSelected(true);
            	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + zoom + "%");
            } else {
            	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
            }
        }
    }

    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    private void zoomIn() {
    	float zoom = 100 / ((float) msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation of the TimeLineViewer
    	float nz = zoom + 10;
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    	/*
        // first find the closest match (there can be rounding issues)
        int zoomMenuIndex = -1;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            int d = Math.abs(ZOOMLEVELS[i] - (int) zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
        }
    	
    	if (zoomMenuIndex > -1 && zoomMenuIndex < ZOOMLEVELS.length - 1) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex + 1];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/
    }
   
    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    private void zoomOut() {
    	float zoom = 100 / ((float) msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation in the TimeLineViewer
    	float nz = zoom - 10;
    	if (nz < 10) {
    		nz = 10;
    	}
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    	/*
        // first find the closest match (there can be rounding issues)
        int zoomMenuIndex = -1;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            int d = Math.abs(ZOOMLEVELS[i] - (int) zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
        }
    	
    	if (zoomMenuIndex > 0) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex - 1];
    		float nextMsPerPixel = (int) ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/
    }
    
    private void zoomToSelection() {
    	long selInterval = getSelectionEndTime() - getSelectionBeginTime();
    	//if (selInterval < 150) {
    	//	selInterval = 150;
    	//}
    	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getWidth() - (2 * SCROLL_OFFSET);
    	float nextMsPP = selInterval / (float) sw;
    	// set a limit of zoom = 5% or mspp = 200
    	if (nextMsPP > 200) {
    		nextMsPP = 200;
    	}
    	if (nextMsPP < TimeScaleBasedViewer.MIN_MSPP) {
    		nextMsPP = TimeScaleBasedViewer.MIN_MSPP;
    	}
    	setMsPerPixel(nextMsPP);
    	//customZoomMI.setSelected(true);
    	//customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + (int)(100 / ((float) msPerPixel / 10)) + "%");
    	if (!playerIsPlaying()) {
    		long ibt = getSelectionBeginTime() - (long)(SCROLL_OFFSET * msPerPixel);
    		if (ibt < 0) {
    			ibt = 0;
    		}
    		setIntervalBeginTime(ibt);
    	}
        setPreference("SignalViewer.ZoomLevel", new Float(100f * (10f / msPerPixel)), 
        		getViewerManager().getTranscription());
    }

    /**
     * Set the specified menuitem selected.
     * 
     * @param vertZoom the current vertical zoom level
     */
    private void updateVertZoomPopup( int vertZoom) {
        if (popup != null) {
            java.util.Enumeration en = vertZoomGroup.getElements();
            JRadioButtonMenuItem rbmi;
            while (en.hasMoreElements()) {
                rbmi = (JRadioButtonMenuItem) en.nextElement();

                if (rbmi.getText().indexOf("" + vertZoom) > -1) {
                    rbmi.setSelected(true);
                    
                    break;
                } else {
                    rbmi.setSelected(false);
                }
            }
        }	
    }
    
    /**
     * Updates the value of the media offset field, i.e. the new media start
     * point in the .wav file.
     *
     * @param offset the new offset in ms
     */
    public void setOffset(long offset) {
        //System.out.println("SignalViewer: new offset: " + offset);
        if (offset != mediaOffset) {
            mediaOffset = offset;

            //force a reload of data
            currentPart.setStartTime(0);
            currentPart.setStopTime(0);

            //currentPart.reset();
            paintBuffer();
        }
    }

    /**
     * When a TimeEvent or StopEvent is received the crosshair position is
     * updated. If necessary a new interval is loaded and the new position of
     * the selection in image space is calculated.<br>
     *
     * @see ControllerListener
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

            /*
               //System.out.println("SignalViewer time: " + crossHairTime);
               if (crossHairTime < intervalBeginTime || crossHairTime > intervalEndTime) {
                   if (playerIsPlaying()) {
                       // we might be in a selection outside the new interval
                       long newBeginTime = intervalEndTime;
                       long newEndTime = newBeginTime + (imageWidth * msPerPixel);
                       if (crossHairTime > newEndTime) {
                           while ((newEndTime += imageWidth + msPerPixel) < crossHairTime) {
                               newBeginTime += imageWidth * msPerPixel;
                           }
                       } else if (crossHairTime < newBeginTime) {
                           while ((newEndTime -= imageWidth * msPerPixel) > crossHairTime) {
                               newBeginTime -= imageWidth * msPerPixel;
                           }
                       }
                       setIntervalBeginTime(newBeginTime);
                   } else {
                       setIntervalBeginTime(crossHairTime);
                   }
             */
            if ((crossHairTime == intervalEndTime) && !playerIsPlaying()) {
                recalculateInterval(crossHairTime);
            } else if ((crossHairTime < intervalBeginTime) ||
                    (crossHairTime > intervalEndTime)) {
                recalculateInterval(crossHairTime);
            } else {
                //repaint a part of the viewer
                int oldCrossHairPos = crossHairPos;
                crossHairPos = xAt(crossHairTime);

                if (crossHairPos >= oldCrossHairPos) {
                    repaint(oldCrossHairPos - 1, 0,
                        crossHairPos - oldCrossHairPos + 2, getHeight());

                    //repaint();
                } else {
                    repaint(crossHairPos - 1, 0,
                        oldCrossHairPos - crossHairPos + 2, getHeight());

                    //repaint();
                }
            }
        }
    }

    /*
     * Implement SelectionUser
     * @see mpi.eudico.client.annotator.SelectionUser#updateSelection()
     */
    public void updateSelection() {
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        if (praatSelMI != null) {        
	        if (selectionBeginPos == selectionEndPos) {
				praatSelMI.setEnabled(false);
	        } else {
				praatSelMI.setEnabled(true);
	        }
        }
        if (clipSelPraatMI != null) {        
	        if (selectionBeginPos == selectionEndPos) {
	            clipSelPraatMI.setEnabled(false);
	        } else {
	            clipSelPraatMI.setEnabled(true);
	        }
        }
        repaint();
    }
    
    /**
     * zooms (wave form)the signal viewer for the given time interval
     * (similar to zoomTo selection, mainly used of signal viewer
     *  in transcription mode)
     * 
     * @param begin, the begin time of the interval
     * @param end, the end time of the interval
     */
    public void updateInterval(long begin, long end) {
    	
    	long selInterval = end - begin;     	
     	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getWidth() - (2 * SCROLL_OFFSET);
     	float nextMsPP = selInterval / (float) sw;
     	// set a limit of zoom = 5% or mspp = 200
     	if (nextMsPP > 200) {
     		nextMsPP = 200;
     	}
     	if (nextMsPP < TimeScaleBasedViewer.MIN_MSPP) {
     		nextMsPP = TimeScaleBasedViewer.MIN_MSPP;
     	}
     	setMsPerPixel(nextMsPP);     	
     	if (!playerIsPlaying()) {
     		long ibt = begin - (long)(SCROLL_OFFSET * msPerPixel);
     		if (ibt < 0) {
     			ibt = 0;
     		}
     		setIntervalBeginTime(ibt);
     	}      
        repaint();
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    public void updateActiveAnnotation() {
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    public void updateLocale() {
        if (popup != null) {
            createPopupMenu();
        }
    }

    /**
     * Layout information, gives the nr of pixels at the left of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the left that contain no time line related
     *         data
     */
    public int getLeftMargin() {
        return 0;
    }

    /**
     * Layout information, gives the nr of pixels at the right of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the right that contain no time line related
     *         data
     */
    public int getRightMargin() {
        return 0;
    }

    /*
     * For testing purposes
     */
    private void printMem(String message) {
        System.out.println(message);

        Runtime r = Runtime.getRuntime();
        System.out.println("Total memory: " + (r.totalMemory() / 1024) + " Kb");
        System.out.println("Free memory: " + (r.freeMemory() / 1024) + " Kb");
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

    //*************************************************************************************//

    /* implement ComponentListener */
    /*
     * Calculate a new BufferedImage taken the new size of the Component
     */
    public void componentResized(ComponentEvent e) {
        intervalEndTime = intervalBeginTime + (int) (getWidth() * msPerPixel);

        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        paintBuffer();

        //repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void componentShown(ComponentEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void componentHidden(ComponentEvent e) {
    }

    //***********************************************************************************

    /* implement MouseListener and MouseMotionListener
       /*
     * A mouse click in the SignalViewer updates the media time
     * to the time corresponding to the x-position.
     */
    public void mouseClicked(MouseEvent e) { 
    	if(!this.isEnabled()){
    		return;
    	}
        Point pp = e.getPoint();
        
        // ALBERT
        if ((e.getClickCount() == 2)) {
    		long clickTime = timeAt(pp.x);
        	switch (channelMode) {
            	case MONO: 
            		if (segmentationChannel1 != null && showSegmentationChannel1) {
		            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime);
		            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime);
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
            		}
            		break;
            	case STEREO_SEPARATE: 
            		if (pp.y < ((SignalViewer) e.getSource()).getHeight() / 2) {
                		if (segmentationChannel1 != null && showSegmentationChannel1) {
			            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime);
			            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime);
			            	setSelection(beforeTime, afterTime);
			            	setMediaTime(beforeTime);
                		}
            		} else {
                		if (segmentationChannel2 != null && showSegmentationChannel2) {
			            	long beforeTime = segmentationChannel2.boundaryTimeBefore(clickTime);
			            	long afterTime = segmentationChannel2.boundaryTimeAfter(clickTime);
			            	setSelection(beforeTime, afterTime);
			            	setMediaTime(beforeTime);
                		}
            		}
            		break;
            	case STEREO_MERGED:
                case STEREO_BLENDED:
                	if (segmentationChannel1 != null && showSegmentationChannel1 && !showSegmentationChannel2) {
		            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime);
		            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime);
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
                	} else if (segmentationChannel2 != null && showSegmentationChannel2 && !showSegmentationChannel1) {
		            	long beforeTime = segmentationChannel2.boundaryTimeBefore(clickTime);
		            	long afterTime = segmentationChannel2.boundaryTimeAfter(clickTime);
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
                	}	
        	}
        } // END ALBERT
        else if ((e.getClickCount() == 1) && e.isShiftDown()) {
            // change the selection interval
            if (getSelectionBeginTime() != getSelectionEndTime()) {
                long clickTime = timeAt(pp.x);

                if (clickTime > getSelectionEndTime()) {
                    // expand to the right
                    setSelection(getSelectionBeginTime(), clickTime);
                } else if (clickTime < getSelectionBeginTime()) {
                    // expand to the left
                    setSelection(clickTime, getSelectionEndTime());
                } else {
                    // reduce from left or right, whichever boundary is closest
                    // to the click time
                    if ((clickTime - getSelectionBeginTime()) < (getSelectionEndTime() -
                            clickTime)) {
                        setSelection(clickTime, getSelectionEndTime());
                    } else {
                        setSelection(getSelectionBeginTime(), clickTime);
                    }
                }
            } else {
            	// create a selection from media time to click time
            	long clickTime = timeAt(pp.x);
            	long medTime = getMediaTime();
            	if (clickTime > medTime) {
            		setSelection(medTime, clickTime);
            	} else if (clickTime < medTime) {
            		setSelection(clickTime, medTime);
		}
	    }
	} else if (SwingUtilities.isLeftMouseButton(e)) {
	    setMediaTime(timeAt(pp.x));
	}

	
	    // Selection clearing
	    // Skip clearing the selection if shift is down, i.e. if the selection 
	    // is being changed, or if a popup has been created by clicking right.
	    // Because popup.isShowing() on these clicks, the selection will
	    // not be cleared.

	    if (clearSelOnSingleClick && e.getClickCount() == 1
		    && !e.isShiftDown() && !popup.isShowing()
		    && !getViewerManager().getMediaPlayerController().getSelectionMode()) {
	    	setSelection(0, 0);
	    	// For clicks that close the popup not popup.isShowing()
	    	// therefore, we cannot avoid these clicks clearing the 
	    	// selection as well.
	    }
	
    }

    /*
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
	
    	if(!this.isEnabled()){
    		return;
    	}
    	    	
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
			Point pp = e.getPoint();
			
			if (popup == null) {
				createPopupMenu();
			}

			if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
				popup.show(this, pp.x, pp.y);
			} else {
				popup.show(this, pp.x, pp.y);
				SwingUtilities.convertPointToScreen(pp, this);

				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				Window w = SwingUtilities.windowForComponent(this);

				if ((pp.x + popup.getWidth()) > d.width) {
					pp.x -= popup.getWidth();
				}

				//this does not account for a desktop taskbar
				if ((pp.y + popup.getHeight()) > d.height) {
					pp.y -= popup.getHeight();
				}

				//keep it in the window then
				if ((pp.y + popup.getHeight()) > (w.getLocationOnScreen().y +
						w.getHeight())) {
					pp.y -= popup.getHeight();
				}

				popup.setLocation(pp);
			}
            return;
        }

        if (playerIsPlaying()) {
            stopPlayer();
        }

        dragStartPoint = e.getPoint();
        dragStartTime = timeAt(dragStartPoint.x);

        if (e.isAltDown() && (dragStartPoint.y < rulerHeight)) {
            panMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //System.out.println("alt down");
        } else {
            panMode = false;

            /* just to be sure a running scroll thread can be stopped */
            stopScroll();
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        //stop scrolling thread
        stopScroll();

        // changing the selection might have changed the intervalBeginTime
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        if (panMode) {
            panMode = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /*
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        stopScroll();
        repaint();
    }

    /*
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }

        dragEndPoint = e.getPoint();

        //panning
        if (panMode) {
            int scrolldiff = dragEndPoint.x - dragStartPoint.x;
            long newTime = intervalBeginTime - (int) (scrolldiff * msPerPixel);
            setIntervalBeginTime((newTime < -mediaOffset) ? (-mediaOffset)
                                                          : newTime);
            dragStartPoint = dragEndPoint;

            return;
        }

        /*e.getPoint can be outside the image size*/
        if ((dragEndPoint.x <= 0) || (dragEndPoint.x >= getWidth())) {
            stopScroll();

            /*
               if (timeScaleConnected) {
                   setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
                   setGlobalTimeScaleIntervalEndTime(intervalEndTime);
               }
             */
            return;
        }

        //auto scroll first
        if ((dragEndPoint.x < SCROLL_OFFSET) && (dragEndPoint.x > 0)) {
            /*
               long begin = intervalBeginTime - SCROLL_OFFSET * msPerPixel;
               if (begin < 0) {
                   begin = 0;
               }
               setIntervalBeginTime(begin);
               paintBuffer();
             */
            if (scroller == null) {
                // if the dragging starts close to the edge call setSelection
                if ((dragStartPoint.x < SCROLL_OFFSET) &&
                        (dragStartPoint.x > 0)) {
                    setSelection(dragStartTime, dragStartTime);
                }

                stopScrolling = false;
                scroller = new DragScroller(-SCROLL_OFFSET / 4, dragScrollSleepTime);
                scroller.start();
            }

            return;
        } else if ((dragEndPoint.x > (getWidth() - SCROLL_OFFSET)) &&
                (dragEndPoint.x < getWidth())) {
            /*
               long begin = intervalBeginTime + SCROLL_OFFSET * msPerPixel;
               setIntervalBeginTime(begin);
               paintBuffer();
             */
            if (scroller == null) {
                //			if the dragging starts close to the edge call setSelection
                if ((dragStartPoint.x > (getWidth() - SCROLL_OFFSET)) &&
                        (dragStartPoint.x < getWidth())) {
                    setSelection(dragStartTime, dragStartTime);
                }

                stopScrolling = false;
                scroller = new DragScroller(SCROLL_OFFSET / 4, dragScrollSleepTime);
                scroller.start();
            }

            return;
        } else {
            stopScroll();

            /* selection and media time, prevent from selecting beyond the media length*/
            if (timeAt(dragEndPoint.x) > dragStartTime) { //left to right
                selectionEndTime = timeAt(dragEndPoint.x);

                if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                    selectionEndTime = (long) samp.getDuration();
                }

                selectionBeginTime = dragStartTime;

                if (selectionBeginTime < 0) {
                    selectionBeginTime = 0L;
                }

                if (selectionEndTime < 0) {
                    selectionEndTime = 0L;
                }

                setMediaTime(selectionEndTime);
            } else { //right to left
                selectionBeginTime = timeAt(dragEndPoint.x);

                if ((samp != null) &&
                        (selectionBeginTime > samp.getDuration())) {
                    selectionBeginTime = (long) samp.getDuration();
                }

                selectionEndTime = dragStartTime;

                if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                    selectionEndTime = (long) samp.getDuration();
                }

                if (selectionBeginTime < 0) {
                    selectionBeginTime = 0L;
                }

                if (selectionEndTime < 0) {
                    selectionEndTime = 0L;
                }

                setMediaTime(selectionBeginTime);
            }

            setSelection(selectionBeginTime, selectionEndTime);
            repaint();
        }
    }

    /*
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        if (e.getPoint().y <= rulerHeight) {
            //setToolTipText(Long.toString(timeAt(e.getPoint().x)));
            setToolTipText(TimeFormatter.toString(timeAt(e.getPoint().x)));
        } else if ((e.getPoint().x >= selectionBeginPos) &&
                (e.getPoint().x <= selectionEndPos)) {
            setToolTipText("Selection: " +
                TimeFormatter.toString(getSelectionBeginTime()) + " - " +
                TimeFormatter.toString(getSelectionEndTime()));
        } else {
            setToolTipText(null);
        }
    }

    /**
     * The use of a mousewheel needs Java 1.4!<br>
     * Zoom in or out.
     *
     * @param e the mousewheel event
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
    	if (e.isControlDown()) {
    		if (e.getUnitsToScroll() > 0) {
    			zoomOut();
    		} else {
    			zoomIn();
    		}
    		return;
    	} else if (e.isShiftDown()) {// on Mac this is the same as hor. scroll with two fingers on the trackpad
	        if (e.getWheelRotation() != 0) {
	        	int timeDiff = (int) (horScrollSpeed * e.getWheelRotation() * msPerPixel);// 2: arbitrary acceleration of the gesture
	        	long newTime = intervalBeginTime + timeDiff;
	        	if (newTime != intervalBeginTime && !(intervalBeginTime < 0 && newTime < intervalBeginTime)) {
	        		setIntervalBeginTime(newTime);
	        	}
	        }
	        return;
        }
    }
    
    /**
     * Zooming in or out as the "pinch" behaviour.
     */
	public void magnify(double zoom) {
		if (zoom > 0) {
			zoomIn();
		} else if (zoom < 0) {
			zoomOut();
		}
	}

	/**
	 * Horizontal swiping behaviour.
	 */
	public void swipe(int x, int y) {
		if (x != 0) {
			long newTime = intervalBeginTime + (long) (x * msPerPixel);
			if (newTime != intervalBeginTime && !(newTime < 0 && newTime < intervalBeginTime)) {
				setIntervalBeginTime(newTime);
			}
		}		
	}
    
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == timeRulerVisMI) {
    		timeRulerVisible = timeRulerVisMI.isSelected();
    		if (timeRulerVisible) {
    			rulerHeight = ruler.getHeight();
    		} else {
    			rulerHeight = 0;
    		}
    		paintBuffer();
    		setPreference("SignalViewer.TimeRulerVisible", new Boolean(timeRulerVisible),
    				getViewerManager().getTranscription());
    	}
    	else if (e.getActionCommand().equals("connect")) {
            boolean connected = ((JCheckBoxMenuItem) e.getSource()).getState();
            setTimeScaleConnected(connected);
            setPreference("SignalViewer.TimeScaleConnected", new Boolean(connected), 
            		getViewerManager().getTranscription());
        } else if (e.getActionCommand().equals("sep")) {
            setChannelMode(STEREO_SEPARATE, true);
        } else if (e.getActionCommand().equals("merge")) {
            setChannelMode(STEREO_MERGED, true);
        } else if (e.getActionCommand().equals("blend")) {
            setChannelMode(STEREO_BLENDED, true);
        } else if (e.getActionCommand().equals("praat")) {
            openInPraat(0, 0);
        } else if (e.getActionCommand().equals("praatSel")) {
            openSelectionInPraat();
        } else if (e.getActionCommand().equals("clipSel")) {
            clipSelectionWithPraat();
        } else if (e.getActionCommand().equals("info")) {
            showMediaInfo();
        }/*else if (e.getActionCommand().indexOf("res") > -1) {
           String com = e.getActionCommand();
           String resString = com.substring(com.indexOf(" ") + 1);
           float factor = 100 / Float.parseFloat(resString);
           int newMsPerPixel = (int)(1000f / (PIXELS_FOR_SECOND * factor));
           setMsPerPixel(newMsPerPixel);
           }*/
        else if (e.getActionCommand().startsWith("vz-")) {
            // vertical zoom or amplification
            int nvz = 100;
            String vzVal = e.getActionCommand().substring(3);
            try {
                nvz = Integer.parseInt(vzVal);
            } catch (NumberFormatException nfe) {
                System.out.println("Error parsing the vertical zoom level");
            }
            vertZoom = nvz;
            paintBuffer();
            setPreference("SignalViewer.VerticalZoomLevel", new Integer(vertZoom), 
            		getViewerManager().getTranscription());
        }
        else if (e.getSource() == zoomSelectionMI) {
    		zoomToSelection();
    	}
        // ALBERT
        else if (e.getSource() == segmentationChannel1Item) {
        	showSegmentationChannel1 = segmentationChannel1Item.getState();
            paintBuffer();
        } 
        else if (e.getSource() == segmentationChannel2Item) {
        	showSegmentationChannel2 = segmentationChannel2Item.getState();
            paintBuffer();
        } else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + mediaOffset;
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
        // END ALBERT
        else {
            /* the rest are zoom menu items*/
            String zoomString = e.getActionCommand();
            int zoom = 100;

            try {
                zoom = Integer.parseInt(zoomString);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing the zoom level");
            }

            float newMsPerPixel = ((100f / zoom) * 10);
            setMsPerPixel(newMsPerPixel);            
        }
    }

    /**
     * Updates the viewer after a change in or loading of preferences.
     */
	public void preferencesChanged() {
	    // super.preferencesChanged (); 
		Object zoomLevel = getPreference("SignalViewer.ZoomLevel", 
				getViewerManager().getTranscription());
		if (zoomLevel instanceof Float) {
			float zl = ((Float) zoomLevel).floatValue();
			if(zl > 5000.0){
				zl = 5000;
			}
			float newMsPerPixel = (100f / zl) * 10;
            setMsPerPixel(newMsPerPixel);
			//updateZoomPopup((int)zl);
		}
		Boolean tsConnect = (Boolean) getPreference("SignalViewer.TimeScaleConnected", 
				getViewerManager().getTranscription());
		if (tsConnect != null) {
			if (timeScaleConMI != null) {
				timeScaleConMI.setSelected(tsConnect.booleanValue());
			}
			setTimeScaleConnected(tsConnect.booleanValue());
		}
		Integer stereoMode = (Integer) getPreference("SignalViewer.StereoMode", 
				getViewerManager().getTranscription());
		if (stereoMode != null) {
			setChannelMode(stereoMode.intValue(), false);
			if (popup != null && separateMI != null) { // the items for stereo mode are all created together
				switch(channelMode) {
				case STEREO_MERGED:
					mergedMI.setSelected(true);
					break;
				case STEREO_BLENDED:
					blendMI.setSelected(true);
					break;
				default:
					separateMI.setSelected(true);
				}
			}
		}
		Object vzLevel = getPreference("SignalViewer.VerticalZoomLevel",
				getViewerManager().getTranscription());
		if (vzLevel instanceof Integer) {
			vertZoom =((Integer) vzLevel).intValue();
			updateVertZoomPopup(vertZoom);
		}
		Boolean rulerVis = (Boolean) getPreference("SignalViewer.TimeRulerVisible", 
				getViewerManager().getTranscription());
		if (rulerVis != null) {
			timeRulerVisible = rulerVis.booleanValue();
			if (timeRulerVisMI != null) {
				timeRulerVisMI.setSelected(timeRulerVisible);
			}
			if (timeRulerVisible) {
				rulerHeight = ruler.getHeight();
			} else {
				rulerHeight = 0;
			}
		}
		Object obj = getPreference("ClearSelectionOnSingleClick",null);
		if (obj instanceof Boolean){
		    clearSelOnSingleClick = ((Boolean) obj).booleanValue();
		}
		
		Object val = getPreference("Preferences.TimeLine.HorScrollSpeed", null);
		if (val instanceof Integer) {
			horScrollSpeed = (Integer) val;
		}
		
		paintBuffer();
	}
	
    /**
     * Try to close the RandomAccessFile of the WAVSampler.
     */
    public void finalize() {
        if (samp != null) {
            samp.close();
        }
    }

    /**
     * Scrolls the image while dragging to the left or right with the specified
     * number  of pixels.<br>
     * This method is called from a separate Thread.
     *
     * @param numPixels the number of pixels to scroll the interval
     *
     * @see DragScroller
     */
    synchronized void scroll(int numPixels) {
        long begin = intervalBeginTime + (int) (numPixels * msPerPixel);

        if (numPixels > 0) {
            // left to right
            setIntervalBeginTime(begin);
            selectionEndTime = getSelectionEndTime() +
            (int) (numPixels * msPerPixel);

            if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                selectionEndTime = (long) samp.getDuration();
            }

            setMediaTime(selectionEndTime);
            setSelection(getSelectionBeginTime(), selectionEndTime);
        } else {
            // right to left
            if (begin < 0) {
                begin = 0;
            }

            setIntervalBeginTime(begin);
            selectionBeginTime = getSelectionBeginTime() +
            (int) (numPixels * msPerPixel);

            if (selectionBeginTime < 0) {
                selectionBeginTime = 0;
            }

            setMediaTime(selectionBeginTime);
            setSelection(selectionBeginTime, getSelectionEndTime());
        }

        //repaint();
    }

    /**
     * DOCUMENT ME!
     */
    void stopScroll() {
        /*
           if (scroller != null) {
               try {
                   scroller.interrupt();
               } catch (SecurityException se) {
                   System.out.println("SignalViewer: could not stop scroll thread");
               } finally {
               }
               scroller = null;
           }
         */
        stopScrolling = true;
        scroller = null;
    }

    /**
     * DOCUMENT ME!
     * $Id: SignalViewer.java 34749 2012-12-11 23:40:54Z hasloe $
     * @author $Author$
     * @version $Revision$
     */
    class DragScroller extends Thread {
        /** Holds value of property DOCUMENT ME! */
        int numPixels;

        /** Holds value of property DOCUMENT ME! */
        long sleepTime;

        /**
         * Creates a new DragScroller instance
         *
         * @param numPixels DOCUMENT ME!
         * @param sleepTime DOCUMENT ME!
         */
        DragScroller(int numPixels, long sleepTime) {
            this.numPixels = numPixels;
            this.sleepTime = sleepTime;
        }

        /**
         * DOCUMENT ME!
         */
        public void run() {
            while (!stopScrolling) {
                SignalViewer.this.scroll(numPixels);

                try {
                    sleep(sleepTime);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

}
