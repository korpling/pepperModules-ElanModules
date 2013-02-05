package mpi.eudico.client.annotator.svg;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;


/**
 * A factory class for building a svg dom tree.  It creates a
 * <code>org.w3c.dom.Document</code> object and provides methods  for the
 * creation of <code>org.w3c.dom.Element</code> objects. A getter method gives
 * access to the document element.
 *
 * @author Han Sloetjes
 */
public class SVG10Factory {
    /** the document */
    private final Document doc;

    /**
     * Constructor creates the <code>Document</code>.
     */
    public SVG10Factory() {
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

        // createDocumentType is not implemented...
        //DocumentType docType = domImpl.createDocumentType("svg", "-//W3C//DTD SVG 1.0//EN", "");
        doc = domImpl.createDocument(svgNS, "svg", null);
    }

    /**
     * Returns the document element.
     *
     * @return the document element
     *
     * @see org.w3c.dom.Element.getDocumentElement()
     */
    public final Element getDocumentElement() {
        return doc.getDocumentElement();
    }

    /**
     * Returns the Document.
     *
     * @return the document
     */
    public final Document getDocument() {
        return doc;
    }

    /**
     * Appends a Node to the document element.
     *
     * @param e DOCUMENT ME!
     *
     * @return the added Node
     *
     * @see org.w3c.dom.Element.appendChild()
     */
    public final Node appendChild(Node e) {
        return doc.appendChild(e);
    }

    /**
     * Creates a <code>svg</code> element.
     *
     * @return a <code>svg</code> element
     */
    public final Element newSVGElement() {
        Element el = doc.createElement("svg");

        return el;
    }

    /**
     * Creates a <code>g</code> element with an id.
     *
     * @param id the id of the group
     *
     * @return a <code>g</code> element
     *
     * @throws NullPointerException if id is null
     */
    public final Element newGroup(String id) {
        if (id == null) {
            throw new NullPointerException("No id");
        }

        Element el = doc.createElement("g");
        el.setAttribute("id", id);

        return el;
    }

    /**
     * Creates a simple <code>rect</code> element.
     *
     * @param shape the graphic shape
     *
     * @return a <code>rect</code> element
     */
    public final Element newRect(RectangularShape shape) {
        if (shape == null) {
            new NullPointerException("Rectangle");
        }

        Element el = doc.createElement("rect");
        el.setAttribute("x", String.valueOf(shape.getX()));
        el.setAttribute("y", String.valueOf(shape.getY()));
        el.setAttribute("width", String.valueOf(shape.getWidth()));
        el.setAttribute("height", String.valueOf(shape.getHeight()));

        return el;
    }

    /**
     * Creates a simple <code>rect</code> element with an id.
     *
     * @param shape the graphic shape
     * @param id the id of the element
     *
     * @return a <code>rect</code> element
     *
     * @throws NullPointerException if id is null
     */
    public final Element newRect(RectangularShape shape, String id) {
        if (id == null) {
            throw new NullPointerException("No id");
        }

        Element el = newRect(shape);
        el.setAttribute("id", id);

        return el;
    }

    /**
     * Creates a simple <code>ellipse</code> element.
     *
     * @param shape the graphic shape
     *
     * @return a <code>ellipse</code> element
     */
    public final Element newEllipse(RectangularShape shape) {
        if (shape == null) {
            new NullPointerException("Ellipse");
        }

        Element el = doc.createElement("ellipse");
        el.setAttribute("cx", String.valueOf(shape.getCenterX()));
        el.setAttribute("cy", String.valueOf(shape.getCenterY()));
        el.setAttribute("rx", String.valueOf(shape.getWidth() / 2));
        el.setAttribute("ry", String.valueOf(shape.getHeight() / 2));

        return el;
    }

    /**
     * Creates a simple <code>ellipse</code> element.
     *
     * @param shape the graphic shape
     * @param id the id of the element
     *
     * @return a <code>ellipse</code> element
     *
     * @throws NullPointerException if id is null
     */
    public final Element newEllipse(RectangularShape shape, String id) {
        if (id == null) {
            throw new NullPointerException("No id");
        }

        Element el = newEllipse(shape);
        el.setAttribute("id", id);

        return el;
    }

	/**
	 * Creates a simple <code>line</code> element.
	 *
	 * @param shape the graphic shape
	 * @param id the id of the element
	 *
	 * @return a <code>line</code> element
	 *
	 * @throws NullPointerException if shape is null
	 */
	public final Element newLine(Line2D shape) {
		if (shape == null) {
			new NullPointerException("Line");
		}

		Element el = doc.createElement("line");
		el.setAttribute("x1", String.valueOf(shape.getX1()));
		el.setAttribute("y1", String.valueOf(shape.getY1()));
		el.setAttribute("x2", String.valueOf(shape.getX2()));
		el.setAttribute("y2", String.valueOf(shape.getY2()));

		return el;
	}

    /**
     * Creates a simple <code>line</code> element.
     *
     * @param shape the graphic shape
     * @param id the id of the element
     *
     * @return a <code>line</code> element
     *
     * @throws NullPointerException if id is null
     */
    public final Element newLine(Line2D shape, String id) {
        if (id == null) {
            throw new NullPointerException("No id");
        }

		Element el = newLine(shape);
		el.setAttribute("id", id);

        return el;
    }
}
