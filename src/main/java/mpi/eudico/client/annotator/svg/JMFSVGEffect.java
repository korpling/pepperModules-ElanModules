package mpi.eudico.client.annotator.svg;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.media.util.ImageToBuffer;


/**
 * An effect that adds graphic annotation visualization to  the media.
 *
 * @author Han Sloetjes
 */
public class JMFSVGEffect implements Effect {
    // need a collection of more specified formats

    /** Holds value of property DOCUMENT ME! */
    protected Format rgbFormat = new RGBFormat();

    // support all video formats the framework supports.

    /** Holds value of property DOCUMENT ME! */
    protected Format[] supportedIns = new Format[] { rgbFormat };

    // support all video formats the framework supports.

    /** Holds value of property DOCUMENT ME! */
    protected Format[] supportedOuts = new Format[] { rgbFormat };

    /** Holds value of property DOCUMENT ME! */
    protected Format input = null;

    /** Holds value of property DOCUMENT ME! */
    protected Format output = null;

    /** Holds value of property DOCUMENT ME! */
    protected JMFSVGViewer viewer;

    //reuse resources

    /** Holds value of property DOCUMENT ME! */
    protected BufferedImage inImg;

    /** Holds value of property DOCUMENT ME! */
    protected BufferedImage outImg;

    /** Holds value of property DOCUMENT ME! */
    protected BufferToImage bti;

    /** Holds value of property DOCUMENT ME! */
    protected Graphics2D bufG2d;

    /** Holds value of property DOCUMENT ME! */
    protected float rate = 25.0f;

    /** Holds value of property DOCUMENT ME! */
    protected int imgW;

    /** Holds value of property DOCUMENT ME! */
    protected int imgH;

    /** Holds value of property DOCUMENT ME! */
    protected long currentFrameTime = 0L;

    /** Holds value of property DOCUMENT ME! */
    protected int msPerFrame = 40; //initial guess

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Format[] getSupportedInputFormats() {
        //System.out.println("Effect: getSupportedInputFormats() called");
        return supportedIns;
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Format[] getSupportedOutputFormats(Format in) {
        //System.out.println("Effect: getSupportedOutputFormats(Format in) called :: " + in.getClass());
        if (in == null) {
            return supportedOuts;
        } else {
            // return the same VideoFormat as the argument
            //System.out.println("Requested in Format: " + in + " - " + in.getClass());
            Format[] outs = new Format[1];
            outs[0] = in;

            return outs;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Format setInputFormat(Format in) {
        //System.out.println("Effect: setInputFormat called:: " + in.getClass());
        input = in;
        bti = new BufferToImage((VideoFormat) input);

        // rely on the size and rate of the video frames
        RGBFormat form = (RGBFormat) in;
        imgW = form.getSize().width;
        imgH = form.getSize().height;
        rate = form.getFrameRate();
        msPerFrame = (int) (1000 / rate);
        outImg = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);

        //bufG2d = outImg.createGraphics();
        return input;
    }

    /**
     * DOCUMENT ME!
     *
     * @param out DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Format setOutputFormat(Format out) {
        //System.out.println("Effect: setOutputFormat called:: " + out.getClass());
        output = out;

        return output;
    }

    /**
     * Connect a SVGViewer to this Effect. The viewer will get the  opportunity
     * to paint graphics to each frame.
     *
     * @param viewer the SVGViewer
     */
    public void connectViewer(JMFSVGViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Sets the <code>viewer</code> field to null, causing the media data to
     * pass unchanged.
     */
    public void disconnectViewer() {
        viewer = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int process(Buffer in, Buffer out) {
        // This is the "Callback" to access individual frames.		
        if (viewer == null) {
            Object data = in.getData();
            in.setData(out.getData());
            out.setData(data);

            //Copy the input attributes to the output
            out.setFormat(in.getFormat());
            out.setLength(in.getLength());
            out.setOffset(in.getOffset());

            return BUFFER_PROCESSED_OK;
        }

        inImg = (BufferedImage) (bti.createImage(in));

        Buffer outBuffer = null;

        //currentFrameTime = in.getTimeStamp() / 1000000;
        if (inImg != null) {
            //System.out.println("Image: " + inImg);
            //BufferedImage bi = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            bufG2d = outImg.createGraphics();
            bufG2d.drawImage(inImg, 0, 0, null);

            if (viewer != null) {
                viewer.paintAnnotations(bufG2d);
            }

            outBuffer = ImageToBuffer.createBuffer((Image) outImg, rate);
        } else {
            System.out.println("Effect: Image = null");

            //return BUFFER_PROCESSED_FAILED;//??
        }

        // Swap the data between the input & output.
        Object data = in.getData();
        in.setData(out.getData());

        if (outBuffer != null) {
            out.setData(outBuffer.getData());
            out.setFormat(outBuffer.getFormat());
            out.setLength(outBuffer.getLength());
            out.setOffset(outBuffer.getOffset());
        } else {
            out.setData(data);

            // Copy the input attributes to the output
            out.setFormat(in.getFormat());
            out.setLength(in.getLength());
            out.setOffset(in.getOffset());
        }

        return BUFFER_PROCESSED_OK;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return "Graphic Annotation Codec";
    }

    /**
     * DOCUMENT ME!
     *
     * @throws ResourceUnavailableException DOCUMENT ME!
     */
    public void open() throws ResourceUnavailableException {
        //System.out.println("Effect: open called");
    }

    /**
     * DOCUMENT ME!
     */
    public void close() {
        //System.out.println("Effect: closed called");
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
        //System.out.println("Effect: reset called");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object[] getControls() {
        return new Object[0];
    }

    /**
     * DOCUMENT ME!
     *
     * @param controlType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getControl(String controlType) {
        return null;
    }
}
