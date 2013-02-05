/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * $Id: Transcription2Tiger.java 8110 2007-02-06 14:46:34Z klasal $ exports
 * annotation on selected tiers into the Tiger Syntax Format (as leaf nodes)
 * In "Tiger-terminology": annotations will become feature values of terminal
 * nodes
 *
 * @author $Author$
 * @version $Revision$
 */
public class Transcription2Tiger {
    private static final String wordFeature = "word";
    private static final String posFeature = "pos";
    private static final String morphFeature = "morph";
    private static final String lemmaFeature = "lemma";

    public static final String[] defaultFeatureNames = new String[] {
            wordFeature, posFeature, morphFeature, lemmaFeature
        };

    /**
     * DOCUMENT ME!
     *
     * @param transcription DOCUMENT ME!
     * @param tierRelationHash DOCUMENT ME!
     * @param fileName DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     * @param beginTime DOCUMENT ME!
     * @param endTime DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void exportTiers(Transcription transcription,
        HashMap tierRelationHash, File exportFile, String encoding,
        long beginTime, long endTime) throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("corpus");
            root.setAttribute("xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xsi:noNamespaceSchemaLocation",
                "http://www.ims.uni-stuttgart.de/projekte/TIGER/TIGERSearch/public/TigerXML.xsd");
            root.setAttribute("id", transcription.getName());

            Element meta = doc.createElement("meta");
            root.appendChild(meta);

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(transcription.getName()
                                                             .replaceAll(".eaf",
                        ".tig")));
            meta.appendChild(name);

            Element author = doc.createElement("author");
            author.appendChild(doc.createTextNode(transcription.getAuthor()));
            meta.appendChild(author);

            Element date = doc.createElement("date");
            SimpleDateFormat dateFmt = new SimpleDateFormat(
                    "yyyy.MM.dd HH:mm:ss");
            String dateString = dateFmt.format(Calendar.getInstance().getTime());
            date.appendChild(doc.createTextNode(dateString));
            meta.appendChild(date);

            Element format = doc.createElement("format");
            format.appendChild(doc.createTextNode("Negra format 3"));
            meta.appendChild(format);

            Element body = doc.createElement("body");

            root.appendChild(body);

            Set sentenceAnnotations = getOrderedAnnotations(transcription,
                    tierRelationHash.keySet());

            for (Iterator it = sentenceAnnotations.iterator(); it.hasNext();) {
                Annotation sentenceAnnotation = (Annotation) it.next();

                if ((beginTime <= sentenceAnnotation.getBeginTimeBoundary()) &&
                        (sentenceAnnotation.getEndTimeBoundary() <= endTime)) {
                    Element s = doc.createElement("s");
                    body.appendChild(s);
                    s.setAttribute("id", sentenceAnnotation.getId());

                    Element graph = doc.createElement("graph");
                    s.appendChild(graph);

                    Element terminals = doc.createElement("terminals");
                    graph.appendChild(terminals);

                    HashMap featureHash = (HashMap) tierRelationHash.get(sentenceAnnotation.getTier());

                    Vector childTiers = ((TierImpl) sentenceAnnotation.getTier()).getChildTiers();

                    for (Iterator descendantTiers = featureHash.keySet()
                                                               .iterator();
                            descendantTiers.hasNext();) {
                        TierImpl featureTier = (TierImpl) descendantTiers.next();
                        
                        if (childTiers.contains(featureTier)) {
                            Vector childAnnotations = sentenceAnnotation.getChildrenOnTier(featureTier);

                            for (int j = 0; j < childAnnotations.size(); j++) {
                                Annotation childAnnotation = (Annotation) childAnnotations.get(j);

                                Element t = doc.createElement("t");
                                terminals.appendChild(t);
                                t.setAttribute("id", childAnnotation.getId());

                                String feature = (String) featureHash.get(featureTier);
                                
                                if ((feature != null) &&
                                        (feature.length() > 0)) {
                                    t.setAttribute(feature,
                                        childAnnotation.getValue());
                                }

                                addFeatures(featureHash, t,
                                    childAnnotation);
                            }
                        }
                    }
                }
            }

            writeTigerFile(encoding, exportFile, root);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IOException("Parser configuration error: " +
                e.getMessage());
        }
    }

    private static void addFeatures(HashMap featureHash, Element t, Annotation annotation) {
        Vector childTiers = ((TierImpl) annotation.getTier()).getChildTiers();

        if (childTiers.size() > 0) {
            for (Iterator it = featureHash.keySet().iterator(); it.hasNext();) {
                TierImpl featureTier = (TierImpl) it.next();
                
                if (childTiers.contains(featureTier)) {
                	
                    String feature = (String) featureHash.get(featureTier);

                    Vector featureChildrenAnnotations = annotation.getChildrenOnTier(featureTier);

                    if (featureChildrenAnnotations.size() > 0) {
                        Annotation childAnnotation = (Annotation) featureChildrenAnnotations.get(0);

                        if ((feature != null) && (feature.length() > 0)) {
                            t.setAttribute(feature,
                                childAnnotation.getValue());
                        }

                        addFeatures(featureHash, t,
                            childAnnotation);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param encoding DOCUMENT ME!
     * @param filename DOCUMENT ME!
     * @param content DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private static final void writeTigerFile(String encoding, File exportFile,
        Element content) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    exportFile), encoding);
        OutputFormat format = new OutputFormat(content.getOwnerDocument(),
                encoding, true);

        XMLSerializer ser = new XMLSerializer(out, format);
        ser.asDOMSerializer();
        ser.serialize(content);
        out.close();
    }

    private static TreeSet getOrderedAnnotations(Transcription transcription,
        Set sentenceTierIds) {
        TreeSet annotations = new TreeSet(new AnnotationComparator());

        for (Iterator it = sentenceTierIds.iterator(); it.hasNext();) {
            annotations.addAll(((TierImpl) it.next()).getAnnotations());
        }

        return annotations;
    }

    /**
     * Compares two TimeInterval objects.<br>
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @author Han Sloetjes
     */
    static class AnnotationComparator implements Comparator {
        /**
         * Compares two TimeInterval objects. First the begin times are
         * compared. If they are the same the end times  are compared.  Note:
         * this comparator imposes orderings that are inconsistent with
         * equals.
         *
         * @param o1 the first interval
         * @param o2 the second interval
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassCastException when either object is not a TimeInterval
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        public int compare(Object o1, Object o2) throws ClassCastException {
            if (!(o1 instanceof AnnotationCore) ||
                    !(o2 instanceof AnnotationCore)) {
                throw new ClassCastException(
                    "Objects should be of type AnnotationCore");
            }

            if (((AnnotationCore) o1).getBeginTimeBoundary() < ((AnnotationCore) o2).getBeginTimeBoundary()) {
                return -1;
            }

            if (((AnnotationCore) o1).getBeginTimeBoundary() == ((AnnotationCore) o2).getBeginTimeBoundary()) {
                if (((AnnotationCore) o1).getEndTimeBoundary() < ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return -1;
                } else if (((AnnotationCore) o1).getEndTimeBoundary() == ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return 0;
                }
            }

            return 1;
        }
    }
}
