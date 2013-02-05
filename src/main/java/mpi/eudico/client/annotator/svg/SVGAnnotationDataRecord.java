package mpi.eudico.client.annotator.svg;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;

import java.awt.Shape;


/**
 * A class to store annotation data that are essential for the programmatic
 * re-creation of an annotation. Extends <code>AnnotationDataRecord</code> by
 * optionally storing a graphical Shape object.
 *
 * @author Han Sloetjes
 */
public class SVGAnnotationDataRecord extends AnnotationDataRecord {
    /** Stores the shape object */
    protected Shape shape;

    /** stores the svg element id */
    protected String svgElementId;

    /**
     * Creates an AnnotationData object from the specified Annotation.
     *
     * @param annotation the Annotation
     */
    public SVGAnnotationDataRecord(SVGAlignableAnnotation annotation) {
        super(annotation);

        shape = annotation.getShape();
        svgElementId = annotation.getSVGElementID();
    }

    /**
     * Returns the Shape object.
     *
     * @return the Shape object which can be null
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Returns the element id.
     *
     * @return the element id or null
     */
    public String getSvgElementId() {
        return svgElementId;
    }
}
