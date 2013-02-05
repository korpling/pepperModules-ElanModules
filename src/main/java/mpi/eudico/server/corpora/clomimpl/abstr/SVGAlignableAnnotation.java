package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.SVGAnnotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.event.ACMEditEvent;


import java.awt.Shape;


/**
 * Extends AlignableAnnotation by adding a field for an id of a referenced svg
 * element and a field for a corresponding Shape object.
 *
 * @author Alexander Klassmann
 * @version 14/08/2003
 */
public class SVGAlignableAnnotation extends AlignableAnnotation
    implements SVGAnnotation {
    private String SVGElementId = "";
    private Shape shape;

    /**
     * Creates a new SVGAlignableAnnotation instance
     *
     * @param bts the begin time time slot
     * @param ets the end time time slot
     * @param theTier the parent tier
     */
    public SVGAlignableAnnotation(TimeSlot bts, TimeSlot ets, Tier theTier) {
        super(bts, ets, theTier);
    }

    /**
     * Creates a new SVGAlignableAnnotation instance
     *
     * @param bts the begin time time slot
     * @param ets the end time time slot
     * @param theTier the parent tier
     * @param id the id of the referenced svg element
     */
    public SVGAlignableAnnotation(TimeSlot bts, TimeSlot ets, Tier theTier,
        String id) {
        super(bts, ets, theTier);
        setSVGElementID(id);
    }

    /**
     * Perform some checks on the passed String and send an ACMEditEvent when
     * necessary.
     *
     * @param id the id of the referenced svg element
     */
    public void setSVGElementID(String id) {
        if (id != null) {
            if (id.equals(SVGElementId)) { /* ignore case?? */

                return;
            }

            SVGElementId = id;
        } else {
            // treat passing null as removing the svg reference...
            if (SVGElementId.equals("")) {
                return; // avoid unnecessary ACMEditEvents 
            }

            SVGElementId = "";
        }

        //svgElementChanged();
    }

    /**
     * Returns the id of the referenced svg element.
     *
     * @return the id of the referenced svg element
     */
    public String getSVGElementID() {
        return SVGElementId;
    }

    /**
     * A parse-time method to insert a shape.<br>
     * This method does not create a ACMEditEvent and should therefore only be
     * used on the initialization of the svg objects.<br>
     *
     * @param shape the shape
     */
    public void insertShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Sets the (new) shape for this annotation. Creates a ACMEditEvent to
     * notify listeners of a change in the graphic annotation.
     *
     * @param shape the shape
     */
    public void setShape(Shape shape) {
        if (this.shape == shape) {
            return;
        }

        this.shape = shape;
        svgElementChanged();
    }

    /**
     * Returns the current shape/graphic annotation of this annotation.<br>
     *
     * @return the current shape
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * If the referenced graphics object has been changed / edited send an
     * ACMEditEvent.
     */
    public void svgElementChanged() {
        modified(ACMEditEvent.CHANGE_ANNOTATION_GRAPHICS, this);
    }
}
