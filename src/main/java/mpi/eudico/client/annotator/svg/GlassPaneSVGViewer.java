package mpi.eudico.client.annotator.svg;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.mediacontrol.ControllerEvent;

import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.Collections;
import java.util.List;


/**
 * A GlassPaneSVGViewer that renders on top of a lightweight component in a transparent
 * pane. Unfinished...
 *
 * @author Alexander Klassmann
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
public class GlassPaneSVGViewer extends AbstractSVGViewer implements ElanLocaleListener,
    ComponentListener {
    private Component underlyingComponent;
    private BufferedImage graphicsBuffer;
    private Graphics2D big2d;
    private AffineTransform currentTransform;

    /** transparent color for erasing */
    private final Color clearColor = new Color(255, 255, 255, 0);

    /**
     * Creates a new GlassPaneSVGViewer instance
     *
     * @param transcription transcription
     */
    public GlassPaneSVGViewer(Transcription transcription) {
    	super(transcription);
        setOpaque(false);
        setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        //g2d.setColor(clearColor);
        //g2d.fillRect(0, 0, getWidth(), getHeight());

        if ((underlyingComponent == null) || (graphicsBuffer == null)) {
            return;
        }

        g2d.drawImage(graphicsBuffer, 0, 0, null);
		
    }

    private void paintBuffer() {
        if (underlyingComponent == null) {
            return;
        }

        int w = getWidth();
        int h = getHeight();

        //
        if ((graphicsBuffer == null) || (graphicsBuffer.getWidth() != w) ||
                (graphicsBuffer.getHeight() != h)) {
            graphicsBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            big2d = graphicsBuffer.createGraphics();
        }

        //
        //graphicsBuffer = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB);
		//big2d = graphicsBuffer.createGraphics();
        big2d.setBackground(clearColor);
        big2d.clearRect(0, 0, w, h);

        if (currentTransform != null) {
            big2d.setTransform(currentTransform);
        }

        //
        long time = getMediaTime();

        for (int i = 0; i < allGraphicTiers.size(); i++) {
            GraphicTier2D tier2d = (GraphicTier2D) allGraphicTiers.get(i);

            if (tier2d.isVisible()) {
                int index = Collections.binarySearch(tier2d.getNodeList(),
                        new Long(time));

                if (index >= 0) {
                    GraphicNode2D node2d = (GraphicNode2D) tier2d.getNodeList()
                                                                 .get(index);
                    big2d.setColor(Color.red);
                    node2d.paintShape(big2d, true);

                    //
                    if (node2d.getAnnotation() == getActiveAnnotation()) {
                        big2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);

                        //big2d.setStroke(new BasicStroke((float)(1 / big2d.getTransform().getScaleX())));
                        node2d.paintActiveMarker(big2d);
                    }
                }
            }
        }

        //
        repaint();
    }
    
	void requestRepaint() {
		paintBuffer();
	}

    /**
     * Repaint after a controller update.
     *
     * @param event controller event
     */
    public void controllerUpdate(ControllerEvent event) {
        paintBuffer();
    }

    /**
     * Sets (resp. replaces) the component on top of which SVG should be rendered.
     *
     * @param component the underlying component
     */
    public void setUnderlyingComponent(Component component) {
        if (underlyingComponent != null) {
            underlyingComponent.removeComponentListener(this);
        }

        underlyingComponent = component;
        underlyingComponent.addComponentListener(this);
        componentResized(null);
    }

    /**
     * Handles resize event.
     *
     * @param e component event
     */
    public void componentResized(ComponentEvent e) {
        Point pp = underlyingComponent.getParent().getLocation();
        Point vp = underlyingComponent.getLocation();

        this.setSize(underlyingComponent.getSize());
        this.setLocation(pp.x + vp.x,
            pp.y + vp.y);

        // calculate transform
        try {
            Dimension pref = ELANCommandFactory.getViewerManager(transcription)
                                               .getMasterMediaPlayer()
                                               .getVisualComponent()
                                               .getPreferredSize();
            double xscale = underlyingComponent.getWidth() / pref.getWidth();

            //currentTransform.setToScale(xscale, xscale);
            currentTransform.setToIdentity();
            currentTransform.scale(xscale, xscale);
        } catch (Exception ex) {
            currentTransform = new AffineTransform();
        }

        //repaint();
        paintBuffer();
    }

    /**
     * Handles component moved event.
     *
     * @param e component event
     */
    public void componentMoved(ComponentEvent e) {
        componentResized(e);
    }

    /**
     * Handles component shown event.
     *
     * @param e component event
     */
    public void componentShown(ComponentEvent e) {
        componentResized(e);
    }

    /**
     * Stub.
     *
     * @param e component event
     */
    public void componentHidden(ComponentEvent e) {
    }

	/**
	 * Forwards to paintComponent
	 */
	public void paintAnnotations(Graphics2D big2d) {
		paintComponent(big2d);		
	}

	/**
	 * Stub.
	 */
	public void paintAnnotations() {
		
	}

	public void setVisibleTiers(List tiers) {
		// TODO Auto-generated method stub
		
	}
}
