package mpi.eudico.client.annotator.svg;

import java.awt.Graphics2D;


/**
 * Interface for different types of svg viewers.
 *
 * @author Han Sloetjes
 * @version july 2004
 */
interface SVGViewer {
    /**
     * Paint the annotations to the specified Graphics object.
     *
     * @param big2d the Graphics object
     */
    void paintAnnotations(Graphics2D big2d);

    /**
     * Paint the annotations
     */
    void paintAnnotations();
}
