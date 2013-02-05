package mpi.eudico.client.annotator.svg;

import mpi.eudico.server.corpora.clom.Annotation;

import java.awt.Graphics2D;
import java.awt.Shape;


/**
 * Stores a reference to an Annotation and the Shape/GraphicsNode that is
 * linked to it.<br>
 *
 * @author Han Sloetjes
 * @version 0.1 17 feb 2004
 */
public class GraphicNode2D implements Comparable {
    private Annotation annotation;
    private GraphicTier2D tier2d;
    private Shape shape;

    /**
     * Creates a new GraphicNode2D instance
     *
     * @param annotation the annotation this node belongs to
     * @param shape the shape
     */
    public GraphicNode2D(Annotation annotation, Shape shape) {
        this.annotation = annotation;
        this.shape = shape;
    }

    /**
     * Sets the Tier2D object this NOde2D belongs to.
     *
     * @param tier2d the Tier2D object this NOde2D belongs to
     */
    public void setTier2D(GraphicTier2D tier2d) {
        this.tier2d = tier2d;
    }

    /**
     * Returns the Tier2D object this NOde2D belongs to.
     *
     * @return the Tier2D object this NOde2D belongs to
     */
    public GraphicTier2D getTier2D() {
        return tier2d;
    }

    /**
     * Returns the Shape created from the referenced svg element. The shape
     * marks a 2d region in the media.
     *
     * @return the Shape created from the referenced svg element
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Sets the Shape created from the referenced svg element. The shape marks
     * a 2d region in the media.
     *
     * @param shape the Shape created from the referenced svg element
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Returns the annotation this node is linked to.
     *
     * @return the annotation this node is linked to
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Assumes that the Graphics object is properly configured (transformation
     * etc);
     *
     * @param g2d a Graphics2D object to render to
     */
    public void paintShape(final Graphics2D g2d) {
        if (shape != null) {
            // style information??
            g2d.draw(shape);
        }
    }

    /**
     * Avoids the scaling of stroke-width etc. when painting in a Graphics
     * object  with a transform other then IDENTITY.
     *
     * @param g2d a Graphics2D object to render to
     * @param preserveStyle true if style information (i.g. stroke-width) has
     *        to  be recalculated for the current transform, false otherwise
     */
    public void paintShape(final Graphics2D g2d, boolean preserveStyle) {
        if (shape != null) {
            //style information??
            //styledShape.paint(g2d, preserveStyle);
            g2d.draw(shape);
        }
    }

    /**
     * Paints the outline of the bounding box in order to visually mark this
     * object as belonging to the active annotation.
     *
     * @param g2d a Graphics2D object to render to
     */
    public void paintActiveMarker(final Graphics2D g2d) {
        if (shape != null) {
            g2d.draw(shape.getBounds2D());
        }
    }

    /**
     * Implements Comparable interface. Legal Objects are Long, Annotation and GraphicNode2D.<br>
     * Returns: <br>
     * -1 if the time or the other Annotation's begin time is less then  the
     * begin time of this annotation<br>
     * 0 if the time or the other Annotation's begin time is between the begin
     * and end time af this annotation<br>
     * 1 if the time or the other Annotation's begin time is greater then the
     * end time of this annotation<br>
     *
     * @param o the object to compare with the current object
     *
     * @return -1 if this object is considered smaller than the specified
     *         object
     */
    public int compareTo(Object o) {
        if (o instanceof Long) {
            long time = ((Long) o).longValue();

            return compareTo(time);
        } else if (o instanceof Annotation) {
            Annotation other = (Annotation) o;

            // just rely on consistency of (non-overlapping) annotations
            return compareTo(other.getBeginTimeBoundary());
        } else if (o instanceof GraphicNode2D) {
            return compareTo(((GraphicNode2D) o).getAnnotation());
        }

        // or throw a ClassCastException??
        return -1;
    }

    private int compareTo(long time) {
        if (time < annotation.getBeginTimeBoundary()) {
            return 1;
        }

        if (time >= annotation.getEndTimeBoundary()) {
            return -1;
        }

        return 0;
    }
}
