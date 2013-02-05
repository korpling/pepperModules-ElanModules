package mpi.eudico.client.annotator.svg;

import com.sun.media.renderer.video.BasicVideoRenderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;


/**
 * A video renderer that can include graphic annotations in the frame
 * rendering.
 *
 * @author Han Sloetjes
 */
public class JMFGraphicVideoRenderer extends BasicVideoRenderer {
    /** Holds value of property DOCUMENT ME! */
    public static final String name = "Graphic Video Renderer";

    /** Holds value of property DOCUMENT ME! */
    protected RGBFormat inputFormat;

    /** Holds value of property DOCUMENT ME! */
    protected RGBFormat supportedRGB;

    /** Holds value of property DOCUMENT ME! */
    protected Format[] supportedFormats;

    /** Holds value of property DOCUMENT ME! */
    protected MemoryImageSource sourceImage;

    /** Holds value of property DOCUMENT ME! */
    protected Image inImage;

    /** Holds value of property DOCUMENT ME! */
    protected Buffer lastBuffer = null;

    /** Holds value of property DOCUMENT ME! */
    protected BufferedImage bufferedImage = null;

    /** Holds value of property DOCUMENT ME! */
    protected Graphics2D g2d;

    /** Holds value of property DOCUMENT ME! */
    protected BufferToImage bufToImg;

    /** Holds value of property DOCUMENT ME! */
    protected long currentFrameTime = 0L;

    /** Holds value of property DOCUMENT ME! */
    protected JMFSVGViewer viewer;

    /**
     * DOCUMENT ME!
     */
    public JMFGraphicVideoRenderer() {
        super(name);

        // set supported inputformats, taken from SampleAWTRenderer
        // should be extended to support more formats
        int rMask = 0x000000FF;
        int gMask = 0x0000FF00;
        int bMask = 0x00FF0000;

        supportedRGB = new RGBFormat(null, // size
                Format.NOT_SPECIFIED, // maxDataLength
                int[].class, // buffer type
                Format.NOT_SPECIFIED, // frame rate
                32, // bitsPerPixel
                rMask, gMask, bMask, // component masks
                1, // pixel stride
                Format.NOT_SPECIFIED, // line stride
                Format.FALSE, // flipped
                Format.NOT_SPECIFIED // endian
            );
        supportedFormats = new VideoFormat[1];
        supportedFormats[0] = supportedRGB;
    }

    /**
     * Renderer
     */
    public void start() {
        started = true;
    }

    /**
     * DOCUMENT ME!
     */
    public void stop() {
        started = false;
    }

    /**
     * Set the data input format.
     *
     * @param format DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Format setInputFormat(Format format) {
        if ((format != null) && format instanceof RGBFormat &&
                format.matches(supportedRGB)) {
            inputFormat = (RGBFormat) format;
            bufToImg = new BufferToImage(inputFormat);

            Dimension size = inputFormat.getSize();
            inWidth = size.width;
            inHeight = size.height;
            bufferedImage = new BufferedImage(inWidth, inHeight,
                    BufferedImage.TYPE_INT_RGB);

            return format;
        } else {
            return null;
        }
    }

    /**
     * Lists the input formats that are supported supported by this renderer.
     *
     * @return DOCUMENT ME!
     */
    public Format[] getSupportedInputFormats() {
        return supportedFormats;
    }

    /*
       public Component getComponent() {
           super.getComponent();
           // returns a HeavyComponent
    
               if (component != null) {
                   //help the layoutManager, Canvas has not a setPreferredSize method
                   //component.setSize(inWidth, inHeight);
               }
    
               return component;
           }
     */

    /**
     * PlugIn
     *
     * @throws ResourceUnavailableException DOCUMENT ME!
     */
    /**
     * Opens the plugin. Does not need to check the availability of any
     * resources.
     *
     * @throws ResourceUnavailableException DOCUMENT ME!
     */
    public void open() throws ResourceUnavailableException {
        sourceImage = null;
        inImage = null;
        lastBuffer = null;
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
    }

    /**
     * DOCUMENT ME!
     */
    public void close() {
    }

    /**
     * Here the actual processing of the image is handled.
     *
     * @param buffer DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected int doProcess(Buffer buffer) {
        //System.out.println("time: " + buffer.getTimeStamp());
        if (component == null) {
            return BUFFER_PROCESSED_FAILED;
        }

        Format inf = buffer.getFormat();

        if (inf == null) {
            return BUFFER_PROCESSED_FAILED;
        }

        // rely on the format of the buffer, don't check
        inImage = bufToImg.createImage(buffer);

        // the timestamp is not reliable ??
        //currentFrameTime = buffer.getTimeStamp() / 1000000;
        if (inImage != null) {
            g2d = bufferedImage.createGraphics();
            g2d.drawImage(inImage, 0, 0, null);

            if (viewer != null) {
                viewer.paintAnnotations(g2d);
            }
        }

        Graphics g = component.getGraphics();

        if (g != null) {
            bounds = component.getBounds();
            bounds.x = 0;
            bounds.y = 0;
            g.drawImage(bufferedImage, bounds.x, bounds.y, bounds.width,
                bounds.height, 0, 0, inWidth, inHeight, component);
        }

        return BUFFER_PROCESSED_OK;
    }

    /**
     * local methods
     *
     * @param viewer DOCUMENT ME!
     */
    /**
     * Connect a SVGViewer to this Renderer. The viewer will get the
     * opportunity to paint graphics to each frame.
     *
     * @param viewer the SVGViewer
     */
    public void connectViewer(JMFSVGViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Sets the <code>viewer</code> field to null, causing the media data to be
     * rendered unchanged.
     */
    public void disconnectViewer() {
        viewer = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Dimension myPreferredSize() {
        return new Dimension(inWidth, inHeight);
    }

    /**
     * DOCUMENT ME!
     */
    protected void repaint() {
        if (component != null) {
            if (inImage != null) {
                g2d = bufferedImage.createGraphics();
                g2d.drawImage(inImage, 0, 0, null);

                if (viewer != null) {
                    viewer.paintAnnotations(g2d);
                }
            }

            Graphics g = component.getGraphics();

            if (g != null) {
                bounds = component.getBounds();
                bounds.x = 0;
                bounds.y = 0;
                g.drawImage(bufferedImage, bounds.x, bounds.y, bounds.width,
                    bounds.height, 0, 0, inWidth, inHeight, component);
            }
        }
    }
}
