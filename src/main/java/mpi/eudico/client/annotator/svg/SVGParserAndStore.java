package mpi.eudico.client.annotator.svg;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.IoUtil;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgentAdapter;

//import org.apache.batik.css.engine.CSSStylableElement; //batik 1.5
//import org.apache.batik.css.engine.SVGCSSEngine; //batik 1.5
//import org.apache.batik.css.engine.value.Value; //batik 1.5
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.GenericAttrNS;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMCircleElement;
import org.apache.batik.dom.svg.SVGOMDefsElement;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMLineElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.dom.svg.SVGOMSymbolElement;
import org.apache.batik.dom.svg.SVGOMUseElement;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;

import java.io.File;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * Parses a .svg file referenced by an eaf Transcription and adds graphical
 * objects  to annotation.<br>
 * It currently also stores a svg/graphics library for each parsed svg file. <br>
 * NB: this class should be in some other package, e.g. eudico.server.xxx or
 * mpi.alt.org.w3c.dom.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
public class SVGParserAndStore {
    private static Hashtable libraries = new Hashtable(4);
    private static Hashtable documents = new Hashtable(4);
    private static SVGParserAndStore parser = null;

    /** the current SVGDocument object */
    private static SVGOMDocument svgDoc = null;

    /** Holds value of property DOCUMENT ME! */
    private static final Logger LOG = Logger.getLogger(SVGParserAndStore.class.getName());

    //private static SVGCSSEngine cssEngine = null; //batik 1.5
    private SVGParserAndStore() {
    }

    /**
     * This class follows the Singleton pattern. The public methods are
     * synchronized  and thus grab the lock for the class.
     *
     * @return the one instance of this class
     */
    public static SVGParserAndStore getInstance() {
        if (parser == null) {
            parser = new SVGParserAndStore();
        }

        return parser;
    }

    /**
     * Returns the symbols library for the specified Transcription.
     *
     * @param transcription the transcription
     *
     * @return a Hashtable with id strings as keys and shapes as entries
     */
    public static Object getLibrary(Transcription transcription) {
        return libraries.get(transcription);
    }

    /**
     * Parses the svg file belonging to the transcription, if present. It
     * builds a library of symbols and adds shapes to annotation using  the
     * svg id's in the annotations.
     *
     * @param transcription the transcription to parse
     */
    public static synchronized void parse(Transcription transcription) {
        //start the parsing of the svg file referenced by the annotation
        String svgFileString = null;

        if (transcription instanceof TranscriptionImpl) {
            if (((TranscriptionImpl) transcription).getSVGFile() != null) {
                // there has been a check on this file in DobesTranscription
                svgFileString = ((TranscriptionImpl) transcription).getSVGFile();
                LOG.info("Parse SVG file: " + svgFileString);
            }
        }

        if (svgFileString == null) {
            return;
        }

        UserAgentAdapter userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);

        //BridgeContext ctx = new BridgeContext(userAgent, loader);
        try {
            svgDoc = (SVGOMDocument) loader.loadDocument(svgFileString);
            documents.put(transcription, svgDoc);
        } catch (IOException ioe) {
            LOG.severe("Could not load the .svg file");

            return;
        }

        //cssEngine = (SVGCSSEngine)svgDoc.getCSSEngine(); //batik 1.5
        buildLibrary(transcription);
        createGraphicAnnotations(transcription);
    }

    /**
     * Loops over the tiers and annotation of this Transcription and tries to
     * create a Shape for each annotation with a reference to a svg element.
     *
     * @param transcription the Transcription to process
     */
    private static void createGraphicAnnotations(
        final Transcription transcription) {
        Shape shape = null;

        Iterator it = transcription.getTiers().iterator();

        while (it.hasNext()) {
            TierImpl tier = (TierImpl) it.next();

            if ((tier.getLinguisticType() != null) &&
                    tier.getLinguisticType().hasGraphicReferences()) {
                //iter over the annotations
                Iterator annIt = tier.getAnnotations().iterator();
                SVGAlignableAnnotation ann;

                while (annIt.hasNext()) {
                    ann = (SVGAlignableAnnotation) annIt.next();

                    if (ann.getSVGElementID().length() > 0) {
                        String id = ann.getSVGElementID();
                        SVGOMElement el = (SVGOMElement) svgDoc.getElementById(id);

                        if (el == null) {
                            continue;
                        }

                        int elX = parseToInt(el.getAttribute("x"));
                        int elY = parseToInt(el.getAttribute("y"));

                        if (el instanceof SVGOMUseElement) {
                            shape = createShapeFromUseElement((SVGOMUseElement) el,
                                    elX, elY);
                        } else {
                            shape = createShapeFromElement(el, elX, elY);
                        }

                        if (shape != null) {
                            ann.insertShape(shape);
                        }

                        //System.out.println("SVG id: " + id + " - shape: " + shape);
                    }
                }
            }
        }
    }

    /**
     * Builds a library of Shape for use in an editor. Only objects within  a
     * 'defs' tag are included in the library.
     *
     * @param transcription the Transcription to build this library for
     */
    private static void buildLibrary(Transcription transcription) {
        SVGOMDocument svgDoc = (SVGOMDocument) documents.get(transcription);

        if (svgDoc == null) {
            return;
        }

        NodeList children = svgDoc.getRootElement().getChildNodes();
        SVGOMDefsElement defsEl = null;

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);

            if (n.getNodeName().equals("defs")) {
                defsEl = (SVGOMDefsElement) n;

                break;
            }
        }

        if (defsEl == null) {
            return;
        }

        Hashtable symbols = new Hashtable();
        children = defsEl.getChildNodes();

        Node nextNode;

        for (int i = 0; i < children.getLength(); i++) {
            nextNode = children.item(i);

            Shape shape = null;

            if (nextNode.getNodeName().equals("symbol")) {
                SVGOMSymbolElement sym = (SVGOMSymbolElement) nextNode;
                int symX = parseToInt(sym.getAttribute("x"));
                int symY = parseToInt(sym.getAttribute("y"));
                NodeList symChilds = sym.getChildNodes();

                for (int j = 0; j < symChilds.getLength(); j++) {
                    if (symChilds.item(j) instanceof SVGOMUseElement) {
                        SVGOMUseElement use = (SVGOMUseElement) symChilds.item(j);
                        int useX = parseToInt(use.getAttribute("x")) + symX;
                        int useY = parseToInt(use.getAttribute("y")) + symY;
                        shape = createShapeFromUseElement(use, useX, useY);

                        break;
                    } else if (symChilds.item(j) instanceof SVGOMElement) {
                        shape = createShapeFromElement((SVGOMElement) symChilds.item(
                                    j), symX, symY);

                        // allow only one child for the time being
                        break;
                    }
                }
            } else if (nextNode.getNodeName().equals("g")) {
                SVGOMGElement gel = (SVGOMGElement) nextNode;
                int gX = parseToInt(gel.getAttribute("x"));
                int gY = parseToInt(gel.getAttribute("y"));
                shape = createShapeFromGElement(gel, gX, gY);

                //allow only one child for the time being
                break;
            }

            if (shape != null) {
                symbols.put(((SVGOMElement) nextNode).getId(), shape);
            }
        }

        libraries.put(transcription, symbols);
    }

    //end buildLibrary
    private static Shape createShapeFromElement(final SVGOMElement el,
        int transX, int transY) {
        if (el instanceof SVGOMGElement) {
            return createShapeFromGElement((SVGOMGElement) el, transX, transY);
        }

        if (el instanceof SVGOMRectElement) {
            return createShapeFromRectElement((SVGOMRectElement) el, transX,
                transY);
        }

        if (el instanceof SVGOMEllipseElement) {
            return createShapeFromEllipseElement((SVGOMEllipseElement) el,
                transX, transY);
        }

        if (el instanceof SVGOMCircleElement) {
            return createShapeFromCircleElement((SVGOMCircleElement) el,
                transX, transY);
        }

        if (el instanceof SVGOMPathElement) {
            return createShapeFromPathElement((SVGOMPathElement) el, transX,
                transY);
        }

        if (el instanceof SVGOMLineElement) {
            return createShapeFromLineElement((SVGOMLineElement) el, transX,
                transY);
        }

        return null;
    }

    private static Shape createShapeFromSymbolElement(
        final SVGOMSymbolElement el, int transX, int transY) {
        NodeList symChilds = el.getChildNodes();
        int x = parseToInt(el.getAttribute("x")) + transX;
        int y = parseToInt(el.getAttribute("y")) + transY;

        for (int j = 0; j < symChilds.getLength(); j++) {
            if (symChilds.item(j) instanceof SVGOMUseElement) {
                SVGOMUseElement use = (SVGOMUseElement) symChilds.item(j);

                return createShapeFromUseElement(use, x, y);
            } else if (symChilds.item(j) instanceof SVGOMElement) {
                return createShapeFromElement((SVGOMElement) symChilds.item(j),
                    x, y);
            }
        }

        return null;
    }

    private static Shape createShapeFromUseElement(final SVGOMUseElement el,
        int transX, int transY) {
        String href = "";

        // batik 1.5
        // href = el.getHref().getBaseVal(); //batik 1.5
        // batik 1.1.1
        //String href = el.getAttributeNS(null, "xlink:href"); //empty in batik 1.1.1
        NamedNodeMap m = el.getAttributes();

        //m.getNamedItem("xlink:href"); returns null
        for (int j = 0; j < m.getLength(); j++) {
            Node n = m.item(j);

            if (n instanceof GenericAttrNS) {
                GenericAttrNS ans = (GenericAttrNS) n;

                if (ans.getName().equals("xlink:href")) {
                    //the first child of the xlink:href is the referenced node
                    Node cn = ans.getFirstChild();

                    if (cn != null) {
                        href = cn.getNodeValue();
                    }

                    break;
                }
            }
        }

        if ((href != null) && (href.length() > 1)) {
            href = href.substring(1); // remove the #
        } else {
            return null;
        }

        int x = parseToInt(el.getAttribute("x")) + transX;
        int y = parseToInt(el.getAttribute("y")) + transY;
        SVGOMElement ref = (SVGOMElement) svgDoc.getElementById(href);

        if (ref instanceof SVGOMSymbolElement) {
            return createShapeFromSymbolElement((SVGOMSymbolElement) ref, x, y);
        }

        if (ref instanceof SVGOMElement) {
            return createShapeFromElement((SVGOMElement) ref, x, y);
        }

        return null;
    }

    private static Shape createShapeFromGElement(final SVGOMGElement el,
        int transX, int transY) {
        NodeList ch = el.getChildNodes();
        int x = parseToInt(el.getAttribute("x")) + transX;
        int y = parseToInt(el.getAttribute("y")) + transY;

        for (int i = 0; i < ch.getLength(); i++) {
            Node n = ch.item(i);

            //allow only one shape for now
            if (n instanceof SVGOMUseElement) {
                return createShapeFromUseElement((SVGOMUseElement) n, x, y);
            }

            if (n instanceof SVGGraphicsElement) {
                return createShapeFromElement((SVGGraphicsElement) n, x, y);
            }
        }

        return null;
    }

    private static Shape createShapeFromRectElement(final SVGOMRectElement el,
        int transX, int transY) {
        //Shape shape = null;
        int x;

        //Shape shape = null;
        int y;

        //Shape shape = null;
        int width;

        //Shape shape = null;
        int height;
        x = parseToInt(el.getAttribute("x")) + transX;
        y = parseToInt(el.getAttribute("y")) + transY;
        width = parseToInt(el.getAttribute("width"));
        height = parseToInt(el.getAttribute("height"));

        // to do: include transform
        Rectangle r = new Rectangle(x, y, width, height);

        //shape = new StyledShape(r);
        //style

        /*
           float strokeWidth = getComputedStrokeWidth(el);
           if (strokeWidth > 0) {
               shape.setStrokeWidth(strokeWidth);
           }
           Color strokeColor = getComputedColor(el, "stroke");
           if (strokeColor != null) {
               shape.setStrokeColor(strokeColor);
           }
           Color fillColor = getComputedColor(el, "fill");
           if (fillColor != null) {
               shape.setFillColor(fillColor);
           }
         */
        return r;
    }

    private static Shape createShapeFromEllipseElement(
        final SVGOMEllipseElement el, int transX, int transY) {
        //StyledShape shape = null;
        int cx;

        //StyledShape shape = null;
        int cy;

        //StyledShape shape = null;
        int rx;

        //StyledShape shape = null;
        int ry;
        cx = parseToInt(el.getAttribute("cx"));
        cy = parseToInt(el.getAttribute("cy"));
        rx = parseToInt(el.getAttribute("rx"));
        ry = parseToInt(el.getAttribute("ry"));

        Ellipse2D.Float ellipse = new Ellipse2D.Float((transX + cx) - (rx / 2),
                (transY + cy) - (ry / 2), 2 * rx, 2 * ry);

        //shape = new StyledShape(ellipse);
        //style

        /*
           float strokeWidth = getComputedStrokeWidth(el);
           if (strokeWidth > 0) {
               shape.setStrokeWidth(strokeWidth);
           }
           Color strokeColor = getComputedColor(el, "stroke");
           if (strokeColor != null) {
               shape.setStrokeColor(strokeColor);
           }
           Color fillColor = getComputedColor(el, "fill");
           if (fillColor != null) {
               shape.setFillColor(fillColor);
           }
         */
        return ellipse;
    }

    private static Shape createShapeFromCircleElement(
        final SVGOMCircleElement el, int transX, int transY) {
        //StyledShape shape = null;
        int cx;

        //StyledShape shape = null;
        int cy;

        //StyledShape shape = null;
        int r;
        cx = parseToInt(el.getAttribute("cx"));
        cy = parseToInt(el.getAttribute("cy"));
        r = parseToInt(el.getAttribute("r"));

        Ellipse2D.Float ellipse = new Ellipse2D.Float((transX + cx) - (r / 2),
                (transY + cy) - (r / 2), 2 * r, 2 * r);

        //shape = new StyledShape(ellipse);
        //style

        /*
           float strokeWidth = getComputedStrokeWidth(el);
           if (strokeWidth > 0) {
               shape.setStrokeWidth(strokeWidth);
           }
           Color strokeColor = getComputedColor(el, "stroke");
           if (strokeColor != null) {
               shape.setStrokeColor(strokeColor);
           }
           Color fillColor = getComputedColor(el, "fill");
           if (fillColor != null) {
               shape.setFillColor(fillColor);
           }
         */
        return ellipse;
    }

    private static Shape createShapeFromPathElement(final SVGOMPathElement el,
        int transX, int transY) {
        Shape shape = null;

        return shape;
    }

    private static Shape createShapeFromLineElement(final SVGOMLineElement el,
        int transX, int transY) {
        int p1x;
        int p1y;
        int p2x;
        int p2y;

        p1x = parseToInt(el.getAttribute("x1"));
        p1y = parseToInt(el.getAttribute("y1"));
        p2x = parseToInt(el.getAttribute("x2"));
        p2y = parseToInt(el.getAttribute("y2"));

        Line2D.Float line = new Line2D.Float(p1x + transX, p1y + transY,
                p2x + transX, p2y + transY);

        return line;
    }

    /////////////////////////////////////////////////////
    // utility methods
    /////////////////////////////////////////////////////
    private static int parseToInt(String s) {
        if ((s == null) || (s.length() == 0)) {
            return 0;
        }

        try {
            int val = 0;

            if (s.indexOf('.') > -1) {
                float floatVal = Float.parseFloat(s);
                val = (int) floatVal;
            } else {
                val = Integer.parseInt(s);
            }

            return val;
        } catch (NumberFormatException nfe) {
            LOG.warning("NFE: " + nfe.getMessage());
        }

        return 0;
    }

    /*
       private static float getComputedStrokeWidth(CSSStylableElement el) {
           float strokeWidth = -1;
           int index = -1;
           index = cssEngine.getPropertyIndex("stroke-width");
           if (index >= 0) {
               strokeWidth = cssEngine.getComputedStyle(el, null, index).getFloatValue();
           }
           return strokeWidth;
       }
     */
    /*
       private static Color getComputedColor(CSSStylableElement el, String attrib) {
           Color c = null;
           int index = -1;
           index = cssEngine.getPropertyIndex(attrib);
           if (index >= 0) {
               Value v = cssEngine.getComputedStyle(el, null, index);
               if (v != null) {
                   c = new Color((int)v.getRed().getFloatValue(), (int)v.getGreen().getFloatValue(), (int)v.getBlue().getFloatValue());
               }
           }
           return c;
       }
     */

    /////////////////////////////////////////////////////////////////////
    // Storage of the svg file
    /////////////////////////////////////////////////////////////////////

    /**
     * Creates a dom tree, fetching the <code>defs</code> from the stored
     * SVGDocument, and writes it to the svg file referenced by the
     * Transcription.
     *
     * @param transcription the Transcription containing svg id's and graphical
     *        shapes
     */
    public static synchronized void storeSVG(Transcription transcription) {
        if (transcription != null) {
            // check the svg path
            String svgFileString = null;

            if (transcription instanceof TranscriptionImpl) {
                svgFileString = ((TranscriptionImpl) transcription).getSVGFile();
            }

            if ((svgFileString == null) || (svgFileString.length() == 0)) {
                // create a svg file path
                String path = "";

                if (transcription instanceof TranscriptionImpl) {
                    path = transcription.getFullPath();
                } else {
                    path = transcription.getName();
                }

                if (path.endsWith(".eaf")) {
                    int index = path.lastIndexOf(".eaf");
                    svgFileString = path.substring(0, index) + ".svg";
                } else {
                    return;
                }

                if (!svgFileString.toLowerCase().endsWith(".svg")) {
                    svgFileString += ".svg";
                }
            }

            if (svgFileString.startsWith("file:")) {
                svgFileString = svgFileString.substring(5);
            }
            /*
            if ((((TranscriptionImpl) transcription).getSVGFile() == null) ||
                    !((TranscriptionImpl) transcription).getSVGFile().equals(svgFileString)) {
                ((TranscriptionImpl) transcription).setSVGFile(svgFileString);
            }
            */
            if (((TranscriptionImpl) transcription).getSVGFile() != null) {            
	            if (documents.get(transcription) instanceof SVGOMDocument) {
	                svgDoc = (SVGOMDocument) documents.get(transcription);
	            }
	
	            Element docElement = null;
	
	            try {
	                docElement = createDOM(transcription);
	            } catch (DOMException dome) {
	                LOG.severe(
	                    "Could not save the svg file: could not create DOM tree: " + dome.getMessage());
	
	                return;
	            }
	
	            if (docElement != null) {
	                try {
	                    IoUtil.writeEncodedFile("UTF-8", svgFileString, docElement);
	                } catch (Exception ex) {
	                    LOG.severe("Error while saving file: " + ex.getMessage());
	                }
	            } else {
	                LOG.warning("Empty svg file.");
	            }
            } else {
                // try to rename an existing svg file
                File svgFile = new File(svgFileString);

                if (svgFile.exists()) {
                    File renamed = new File(svgFileString + "_old");
                    boolean success = svgFile.renameTo(renamed);

                    if (!success) {
                        LOG.warning("Could not rename empty svg file");
                    }
                }
            }
            /* for the time being rename the file when there are no tiers with a linguistic type
             * that allows graphical annotations. This must change when svg files are decently
             * referenced in the eaf as external files. Renaming prevents that the next time
             * the eaf is loaded an SVGViewer is being created.
             */
            /* done elsewhere now
            boolean atLeastOne = false;

            Vector tiers = transcription.getTiers();
            Iterator tierIt = tiers.iterator();
            TierImpl tier;

            while (tierIt.hasNext()) {
                tier = (TierImpl) tierIt.next();

                if (tier.getLinguisticType().hasGraphicReferences()) {
                    atLeastOne = true;

                    break;
                }
            }

            if (!atLeastOne) {
                File svgFile = new File(svgFileString);

                if (svgFile.exists()) {
                    File renamed = new File(svgFileString + "_old");
                    boolean success = svgFile.renameTo(renamed);

                    if (!success) {
                        LOG.warning("Could not rename empty svg file");
                    }
                }
            }
            */
        } else {
            return;
        }
    }

    private static Element createDOM(Transcription transcription) throws DOMException {
        boolean defsEmpty = true;

        SVG10Factory svgFactory = new SVG10Factory();

        // clone the doctype element - does not get parsed yet
        if ((svgDoc != null) && (svgDoc.getDoctype() != null)) {
            svgFactory.appendChild(svgDoc.getDoctype());
        }

        Element svgEl = svgFactory.getDocumentElement();

        //svgFactory.appendChild(svgEl);
        // clone the defs element from the original file, if any
        if (svgDoc != null) {
            NodeList children = svgDoc.getRootElement().getChildNodes();
            SVGOMDefsElement defsEl = null;

            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);

                if (n.getNodeName().equals("defs")) {
                    defsEl = (SVGOMDefsElement) n;

                    break;
                }
            }

            if ((defsEl != null) && defsEl.hasChildNodes()) {
                defsEmpty = false;

                AbstractNode copyNode = (AbstractNode) defsEl.cloneNode(true);
                Document d = (Document) svgFactory.getDocument();
                copyNode.setOwnerDocument(d);
                svgEl.appendChild(copyNode);
            }
        }

        // now iterate over the tiers with graphic annotations
        Shape shape = null;

        Iterator it = transcription.getTiers().iterator();

        while (it.hasNext()) {
            TierImpl tier = (TierImpl) it.next();

            if ((tier.getLinguisticType() != null) &&
                    tier.getLinguisticType().hasGraphicReferences()) {
                //iter over the annotations
                Iterator annIt = tier.getAnnotations().iterator();
                SVGAlignableAnnotation ann;

                while (annIt.hasNext()) {
                    ann = (SVGAlignableAnnotation) annIt.next();

                    shape = ann.getShape();

                    String id = ann.getSVGElementID();

                    if ((shape == null) || (id.length() == 0)) {
                        continue;
                    }

                    Element group = svgFactory.newGroup(id);

                    if (shape instanceof Rectangle) {
                        Element rectEl = svgFactory.newRect((RectangularShape) shape);
                        group.appendChild(rectEl);
                        svgEl.appendChild(group);
                    } else if (shape instanceof Ellipse2D) {
                        Element ellEl = svgFactory.newEllipse((RectangularShape) shape);
                        group.appendChild(ellEl);
                        svgEl.appendChild(group);
                    } else if (shape instanceof Line2D) {
                        Element ellEl = svgFactory.newLine((Line2D) shape);
                        group.appendChild(ellEl);
                        svgEl.appendChild(group);
                    }
                }
            }
        }

        // there should at least be content in either the defs element or the svg element 
        //if (!defsEmpty || svgFactory.getDocumentElement().hasChildNodes()) {
        return svgFactory.getDocumentElement();

        //} else {
        //	return null;
        //}
    }
}
