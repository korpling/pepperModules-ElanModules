/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * SizeCalculator contains utility methods to calculate how much horizontal
 * space  each visible annotation in a Transcription needs, both for
 * annotations by themselves, and in the context of interlinearized lines
 * (including additional empty space).
 *
 * @author hennie
 */
public class SizeCalculator {
    /**
     * Calculates how much horizontal space each individual visible annotation
     * occupies. Measured in units specified by Interlinearizer's
     * alignmentUnit parameter, in this case PIXELS
     *
     * @param metrics Stores and transfers (intermediate and final) results of
     *        interlinearizing
     * @param g DOCUMENT ME!
     */
    public static void calculateSizes(Metrics metrics, Graphics g) {
        TimeCodedTranscription tr = metrics.getTranscription();

        //	int alignmentUnit = metrics.getInterlinearizer().getAlignmentUnit();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();

        int size = 0;
        int maxTierLabelWidth = 0;

        List vTierList = Arrays.asList(visibleTiers);

        // iterate over visible tiers, over annotations
        Iterator tierIter = tr.getTiers().iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            if (vTierList.contains(t.getName())) { // only visible tiers

                Font font = metrics.getInterlinearizer().getFont(t.getName());
                FontMetrics fontMetrics = g.getFontMetrics(font);

                int tierHeight = fontMetrics.getHeight();
                metrics.setTierHeight(t.getName(), tierHeight);

                Iterator annIter = t.getAnnotations().iterator();

                while (annIter.hasNext()) {
                    Annotation a = (Annotation) annIter.next();

                    size = fontMetrics.stringWidth(a.getValue().trim());

                    // store size in Metrics
                    metrics.setSize(a, size);
                }

                if (fontMetrics.stringWidth(t.getName()) > maxTierLabelWidth) {
                    maxTierLabelWidth = fontMetrics.stringWidth(t.getName());
                }
            }
        }

        metrics.setLeftMargin(maxTierLabelWidth + 10);
    }

    /**
     * Calculates how much horizontal space each individual visible annotation
     * occupies. Measured in units specified by Interlinearizer's
     * alignmentUnit parameter, in this case BYTES
     *
     * @param metrics Stores and transfers (intermediate and final) results of
     *        interlinearizing
     */
    public static void calculateSizes(Metrics metrics) {
        TimeCodedTranscription tr = metrics.getTranscription();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();

        int size = 0;
        int maxTierLabelWidth = 0;

        List vTierList = Arrays.asList(visibleTiers);

        // iterate over visible tiers, over annotations
        Iterator tierIter = tr.getTiers().iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            if (vTierList.contains(t.getName())) { // only visible tiers

                int charEncoding = metrics.getInterlinearizer().getCharEncoding(t.getName());
                metrics.setTierHeight(t.getName(), 1); // for bytes case, just to index lines

                Iterator annIter = t.getAnnotations().iterator();

                while (annIter.hasNext()) {
                    Annotation a = (Annotation) annIter.next();

                    if (charEncoding == Interlinearizer.UTF8) {
                        size = getNumOfBytes(a.getValue());
                    } else {
                        size = a.getValue().length(); // default 1 bytes per char
                    }

                    // store size in Metrics
                    metrics.setSize(a, size);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param utf8String DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int getNumOfBytes(String utf8String) {
        int numOfBytes = 0;

        char[] chars = new char[utf8String.length()];
        utf8String.getChars(0, utf8String.length(), chars, 0);

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            if ((ch == '\u0000') || ((ch >= '\u0080') && (ch <= '\u07ff'))) { // 2 bytes
                numOfBytes += 2;
            } else if ((ch >= '\u0800') && (ch <= '\uffff')) { // 3 bytes
                numOfBytes += 3;
            } else {
                numOfBytes += 1;
            }
        }

        return numOfBytes;
    }

    /**
     * Calculates occupied horizontal space for each visible annotation,
     * including empty space needed for interlinearization.
     *
     * @param metrics Stores and transfers results of interlinearizing
     */
    public static void calculateUsedWidths(Metrics metrics) {
        // algoritm: recursively calls getUsedWidth(annot).
        //
        // - get list of root annotations (==with no parent annot).
        // - iterate over these annots
        // - for each annot, 
        //     - get 'size' from Metrics
        //     - per immediate child tier, get child annots
        //     - recursively determine total usedWidth for those, including some empty space
        //     - find max of 'size' and total widths of child tiers, only taking visible
        //       tiers into account.
        //     - store each determined usedWidth in Metrics.
        Vector rootAnnotations = new Vector();

        TimeCodedTranscription tr = metrics.getTranscription();
        int alignmentUnit = metrics.getInterlinearizer().getAlignmentUnit();
        int usedWidht = 0;

        // iterate over top tiers, over annotations
        Vector topTiers = ((TranscriptionImpl)tr.getTranscription()).getTopTiers();

        Iterator tierIter = topTiers.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();
            Vector annots = t.getAnnotations();
            rootAnnotations.addAll(annots);
        }

        Iterator annotIter = rootAnnotations.iterator();

        while (annotIter.hasNext()) {
            // result is stored in Metrics as a side effect
            Annotation ann = (Annotation) annotIter.next();
            determineUsedWidth(ann, metrics);
        }
    }

    /**
     * Recursive method that calculates and stores used width for an annotation
     * in an interlinear layout. Result includes necessary empty space for
     * alignment.
     *
     * @param ann
     * @param metrics used to pass maximum width down the recursion tree in
     *        case parents are wider
     *
     * @return used width for annotation in number of alignmentUnits (pixels,
     *         bytes,...)
     */
    private static int determineUsedWidth(Annotation ann, Metrics metrics) {
        // - get 'size' from Metrics
        // - per immediate child tier, get child annots
        // - recursively determine total usedWidth for those, including some empty space
        // - find max of 'size' and total widths of child tiers, only taking visible
        //   tiers into account.
        // - store each determined usedWidth in Metrics.
        TimeCodedTranscription tr = (TimeCodedTranscription) metrics.getTranscription();

        int maxUsedWidth = metrics.getSize(ann); // if invisible, size == 0
        int annWidth = maxUsedWidth;
        int usedWidth = 0;
        Hashtable usedPerTier = new Hashtable();
        Hashtable lastChildPerTier = new Hashtable();

        Vector childAnnots = tr.getChildAnnotationsOf(ann);

        Iterator childIter = childAnnots.iterator();

        while (childIter.hasNext()) {
            Annotation child = (Annotation) childIter.next();
            usedWidth = determineUsedWidth(child, metrics);

            Integer currWidthForTier = (Integer) usedPerTier.get(child.getTier());

            if (currWidthForTier != null) {
                // TODO: substitute 10 with proper amount of empty space!!!
                usedPerTier.put(child.getTier(),
                    new Integer(currWidthForTier.intValue() + usedWidth +
                        metrics.getInterlinearizer().getEmptySpace()));
            } else {
                usedPerTier.put(child.getTier(), new Integer(usedWidth));
            }

            // store last child on each tier
            Annotation lastOnTier = (Annotation) lastChildPerTier.get(child.getTier());

            if (lastOnTier != null) {
                if (child.compareTo(lastOnTier) > 0) {
                    lastChildPerTier.put(child.getTier(), child);
                }
            } else { // lastOnTier not yet set
                lastChildPerTier.put(child.getTier(), child);
            }
        }

        Collection sizes = usedPerTier.values();
        Iterator sizePerTierIter = sizes.iterator();

        while (sizePerTierIter.hasNext()) {
            int sizePerTier = ((Integer) sizePerTierIter.next()).intValue();

            if (sizePerTier > maxUsedWidth) {
                maxUsedWidth = sizePerTier;
            }
        }

        // if maxUsedWidth determined by ann itself, propagate this down the tree again.
        // algoritm: add extra width to last of ann's children on each tier.
        if (maxUsedWidth == annWidth) {
            Enumeration tierEnum = usedPerTier.keys();

            while (tierEnum.hasMoreElements()) {
                Tier t = (Tier) tierEnum.nextElement();
                int widthToBeAdded = maxUsedWidth -
                    ((Integer) usedPerTier.get(t)).intValue();

                if (widthToBeAdded > 0) {
                    Annotation lastAnn = (Annotation) lastChildPerTier.get(t);
                    metrics.setUsedWidth(lastAnn,
                        metrics.getUsedWidth(lastAnn) + widthToBeAdded);
                }
            }
        }

        // store maxUsedWidth in Metrics
        metrics.setUsedWidth(ann, maxUsedWidth);

        return maxUsedWidth;
    }
}
