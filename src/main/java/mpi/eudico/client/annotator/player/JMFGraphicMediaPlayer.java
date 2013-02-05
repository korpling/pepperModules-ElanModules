package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.svg.JMFGraphicVideoRenderer;
import mpi.eudico.client.annotator.svg.JMFSVGViewer;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;

import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.net.URL;

import javax.media.Buffer;
import javax.media.Control;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;
import javax.media.util.BufferToImage;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * This media player extends <code>JMFMediaPlayer</code> by allowing a viewer
 * for graphical annotations to connect to the <code>VideoRenderer</code> of
 * the player.<br>
 * In order to make this possible this <code>ElanMediaPlayer</code> uses a
 * Processor instead of a Player and the processor connects a custom
 * <code>VideoRenderer</code> to  the VideoTrack. This renderer includes the
 * graphics from the graphical annotation viewer in the visualization of the
 * videotrack.
 *
 * @author Han Sloetjes
 */
public class JMFGraphicMediaPlayer extends JMFMediaPlayer {
    /** Holds value of property DOCUMENT ME! */
    final int transitionTime = 10000;

    /** Holds value of property DOCUMENT ME! */
    final int sleepTime = 50;

    /** Holds value of property DOCUMENT ME! */
    protected Processor processor;

    /** Holds value of property DOCUMENT ME! */
    protected JMFGraphicVideoRenderer renderer;

	/** Holds value of property DOCUMENT ME! */
	protected FrameGrabbingControl frameGrabber;

	/** Holds value of property DOCUMENT ME! */
	protected FramePositioningControl framePositioner;

    /**
     * DOCUMENT ME!
     *
     * @param mediaDescriptor
     *
     * @throws NoPlayerException
     */
    public JMFGraphicMediaPlayer(MediaDescriptor mediaDescriptor)
        throws NoPlayerException {
        super(mediaDescriptor);

        String URLString = mediaDescriptor.mediaURL;

        try {
            //System.out.println("mediaURL = "+ URLString);
            if (URLString.startsWith("rtsp")) {
                // do something for streaming
                System.out.println("stream");

                MediaLocator ml = new MediaLocator(URLString);
                processor = Manager.createProcessor(ml);
            } else {
                URL mediaURL = new URL(URLString);
                processor = Manager.createProcessor(mediaURL);
            }
        } catch (javax.media.NoPlayerException e) {
            System.out.println(
                "javax.media.NoPlayerException while creating JMF player");

            // use the default player
            e.printStackTrace();

            //throw new NoPlayerException(e.getMessage());
        } catch (IOException e) {
            System.out.println("IO exception while creating JMF player");

            //use the default player
            e.printStackTrace();

            //throw new NoPlayerException("IO exception, problem to connect to data source");
        }

        processor.configure();

        if (!checkState(processor, Processor.Configured)) {
            System.out.println("Could not configure the Processor");

            // use the default player
            //throw new NoPlayerException("The Processor could not be configured");
        }

        // use the processor as a player
        processor.setContentDescriptor(null);

        TrackControl[] tc = processor.getTrackControls();

        if ((tc == null) || (tc.length == 0)) {
            System.err.println(
                "Failed to obtain track controls from the processor.");
        } else {
            // get videotrack
            TrackControl videoTrack = null;

            for (int i = 0; i < tc.length; i++) {
                if (tc[i].getFormat() instanceof VideoFormat) {
                    videoTrack = tc[i];

                    break;
                }
            }

            if (videoTrack != null) {
                try {
                    renderer = new JMFGraphicVideoRenderer();
                    videoTrack.setRenderer(renderer);
                } catch (Exception e) {
                    System.out.println("Could not set renderer for videotrack");
                }
            }
        }

        processor.prefetch();

        if (!checkState(processor, Processor.Prefetched)) {
            System.out.println("Could not prefetch the processor");

            // use the default player
            //throw new NoPlayerException("The processor could not be prefetched");
        }

        if ((processor != null) &&
                (processor.getState() >= Processor.Prefetched)) {
            player = processor;
        }

		//printControls(player);
		Control[] controls = player.getControls();

		for (int j = 0; j < controls.length; j++) {
			if (controls[j] instanceof FrameGrabbingControl) {
				frameGrabber = (FrameGrabbingControl) controls[j];

				//System.out.println("Grabber: " + frameGrabber);
			}

			if (controls[j] instanceof FramePositioningControl) {
				framePositioner = (FramePositioningControl) controls[j];

				//System.out.println("Positioner: " + framePositioner);
			}
		}

        // copied from JMFMediaPlayer
        GainControl gain = player.getGainControl();
        float level = 1.0f;

        if (gain != null) {
            level = gain.getLevel();
            gain.setLevel(0);
        }

        systemVolumeLevel = level;
        start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stop();
        setMediaTime(0);

        if (gain != null) {
            gain.setLevel(level);
        }

        // this takes care of detecting the EndOfMediaEvent
        player.addControllerListener(this);

        if (player.getVisualComponent() != null) {
            player.getVisualComponent().addMouseListener(new MouseHandler());
        }

        popup = new JPopupMenu();
        durationItem = new JMenuItem(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(getMediaDuration()));
        durationItem.setEnabled(false);
        popup.addSeparator();
        popup.add(durationItem);

        // end copied
    }

    /**
     * Give access to the custom VideoRenderer. Can be null if there is no
     * video track in the media.
     *
     * @return the videorenderer or <code>null</code>
     */
    public VideoRenderer getRenderer() {
        return renderer;
    }

    /**
     * Connects a graphical viewer to the videorenderer.<br>
     * Note: currently the type of the parameter is a JMFSVGPlayer;  this has
     * to be changed into some superclass type when  other kinds of graphical
     * viewers are needed.
     *
     * @param viewer the viewer providing graphical annotations
     *
     * @return true if the viewers can be connected, false otherwise
     */
    public boolean connectViewer(JMFSVGViewer viewer) {
        if (renderer != null) {
            renderer.connectViewer(viewer);

            return true;
        }

        return false;
    }

	/**
	 * Tries to grab the frame for the specified time and convert it to a
	 * BufferedImage.
	 *
	 * @param time the mediatime to grab the frame for
	 *
	 * @return the frame image or null
	 */
	public Image getFrameImageForTime(long time) {
		// forget about the frame positioner..., does not work

		/*
		   if (framePositioner != null) {
			   int frameNumber = framePositioner.mapTimeToFrame(new Time(time * 1000000));
			   System.out.println("Time ms: " + time +" - frame: " + frameNumber);
			   if (frameNumber != FramePositioningControl.FRAME_UNKNOWN) {
				   int actual = framePositioner.seek(frameNumber);
				   if (actual != frameNumber) {
					   System.out.println("Frame deviation: " + (frameNumber - actual));
				   }
			   }
		   }
		 */
		if (getMediaTime() != time) {
			setMediaTime(time);
		}

		if (frameGrabber != null) {
			Buffer buffer = frameGrabber.grabFrame();
			BufferToImage b2i = new BufferToImage((VideoFormat) buffer.getFormat());
			BufferedImage bi = (BufferedImage) b2i.createImage(buffer);

			return bi;
		}

		return null;
	}
	
    /**
     * Give the player a certain amount of time to reach the desired state.
     *
     * @param p the player to monitor
     * @param state the desired state
     *
     * @return true if the state has been reached, false otherwise
     */
    private boolean checkState(Player p, int state) {
        int time = 0;

        if (p.getState() == state) {
            return true;
        }

        while (time < transitionTime) {
            try {
                Thread.sleep(sleepTime);

                if (p.getState() == state) {
                    return true;
                }

                time += sleepTime;
            } catch (InterruptedException ie) {
            }
        }

        return false;
    }

    /**
     * Print some information about the current player
     *
     * @param player
     */
    void printControls(Player player) {
        System.out.println("Player info: " + player);

        Control[] controls = player.getControls();

        for (int i = 0; i < controls.length; i++) {
            System.out.print("\t" + i + ": ");
            System.out.println(controls[i].getClass());

            if (controls[i] instanceof FormatControl) {
                FormatControl fc = (FormatControl) controls[i];
                System.out.println("\tFormatcontrol format: " + fc.getFormat());
                System.out.println("\tControl Component: " +
                    fc.getControlComponent());
            }

            if (controls[i] instanceof FrameGrabbingControl) {
                System.out.println("\t - is framgrabbing control");
            }

            if (controls[i] instanceof FramePositioningControl) {
                System.out.println("\t - is framepositioning control");
            }
        }
    }
}
