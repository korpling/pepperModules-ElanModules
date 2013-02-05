/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Positioner contains utility methods to calculate horizontal and vertical
 * positions to be used to render annotations on a page. It deals with line
 * and blockwise wrapping, and takes empty slots (absent annotations) into
 * account.
 *
 * @author hennie
 */
public class Positioner {
    /**
     * DOCUMENT ME!
     *
     * @param metrics DOCUMENT ME!
     */
    public static void calcHorizontalPositions(Metrics metrics) {
        // Find all visible root annotations.
        // Sort them according to time order.
        // Iterate over them.
        // Position all children, taking empty slots into account.
        TimeCodedTranscription tr = metrics.getTranscription();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();
        List vTierList = Arrays.asList(visibleTiers);

        Vector rootAnnotations = new Vector();
        Hashtable positionPerTier = new Hashtable();

        int hBlockOffset = 0; // horizontal offset per rootAnnotation

        Vector topTiers = ((TranscriptionImpl)tr.getTranscription()).getTopTiers();

        Iterator tierIter = topTiers.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            Vector annots = t.getAnnotations();
            rootAnnotations.addAll(annots);
        }

        Collections.sort(rootAnnotations);

        Iterator annIter = rootAnnotations.iterator();

        while (annIter.hasNext()) {
            Annotation a = (Annotation) annIter.next();
            positionAnnotation(a, hBlockOffset, vTierList, metrics,
                positionPerTier);

            hBlockOffset += (metrics.getUsedWidth(a) +
            metrics.getInterlinearizer().getEmptySpace());
            positionPerTier.clear(); // reset	
        }
    }

    private static void positionAnnotation(Annotation a, int blockOffset,
        List vTierList, Metrics metrics, Hashtable posPerTier) {
        int hPosition = 0;

        boolean annVisible = true;

        annVisible = vTierList.contains(a.getTier().getName());

        // set vertical position
        if (annVisible) {
            metrics.setVerticalPosition(a);
        }

        // set horizontal position.		
        Integer hPosInteger = (Integer) posPerTier.get(a.getTier());

        if (hPosInteger != null) {
            hPosition = hPosInteger.intValue();
        }

        // To take empty slots into account:
        //   if a has parent, make sure posPerTier is after parent hPosition
        Annotation parentAnn = a.getParentAnnotation();

        if (parentAnn != null) {
            int parentHPos = metrics.getHorizontalPosition(parentAnn);

            if (parentHPos > (hPosition + blockOffset)) {
                hPosition = parentHPos - blockOffset;
            }
        }

        // set also for invisible annots, to pass alignment on to visible children
        metrics.setHorizontalPosition(a, blockOffset + hPosition);

        // calculate and store new horizontal position
        hPosition += (metrics.getUsedWidth(a) +
        metrics.getInterlinearizer().getEmptySpace());
        posPerTier.put(a.getTier(), new Integer(hPosition));

        // position children and empty spaces
        TimeCodedTranscription tr = (TimeCodedTranscription) metrics.getTranscription();
        Vector childAnnots = null;

        childAnnots = tr.getChildAnnotationsOf(a);
        Collections.sort(childAnnots);

        Iterator childIter = childAnnots.iterator();

        while (childIter.hasNext()) {
            Annotation child = (Annotation) childIter.next();

            positionAnnotation(child, blockOffset, vTierList, metrics,
                posPerTier);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param metrics DOCUMENT ME!
     */
    public static void wrap(Metrics metrics) {
        int horWrap = 0; // horizontal component of wrap vector
        int vertWrap = 0; // vertical component of wrap vector

        int lastBlockStart = 0;
        int lastBlockIndex = 0;

        Vector topTiers = ((TranscriptionImpl)metrics.getTranscription().getTranscription()).getTopTiers();

        boolean wrap = false;

        Vector orderedAnnots = metrics.getBlockWiseOrdered();

        for (int i = 0; i < orderedAnnots.size(); i++) {
            Annotation a = (Annotation) orderedAnnots.elementAt(i);
            TierImpl t = (TierImpl) a.getTier();
            wrap = false;

            // only wrap on subdivision tiers, exclude top tiers...			
            if (topTiers.contains(t)) {
                lastBlockStart = metrics.getHorizontalPosition(a);
                lastBlockIndex = i;

                if ((metrics.getInterlinearizer().getBlockWrapStyle() == Interlinearizer.EACH_BLOCK) &&
                        (i > 0)) { // not first block
                    wrap = true;
                } else if ((metrics.getInterlinearizer().getBlockWrapStyle() == Interlinearizer.BLOCK_BOUNDARY) &&
                        ((metrics.getHorizontalPosition(a) +
                        metrics.getUsedWidth(a)) > (metrics.getInterlinearizer()
                                                               .getWidth() -
                        metrics.getLeftMargin()))) {
                    if (i > 0) {
                        wrap = true; // not first block		
                    }
                } else if ((metrics.getHorizontalPosition(a) // if page width not across a's value string
                         +metrics.getSize(a)) <= (metrics.getInterlinearizer()
                                                             .getWidth() -
                        metrics.getLeftMargin())) {
                    continue;
                }
            }

            // ...and symbolic associations of top tiers
            if (t.hasParentTier() && topTiers.contains(t.getParentTier()) &&
                    (t.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                continue;

                // if page width not across a's value string

                /*    if (    metrics.getHorizontalPosition(a)
                   + metrics.getSize(a) <=
                   metrics.getInterlinearizer().getWidth()
                   - metrics.getLeftMargin()) {
                
                       continue;
                   }    */
            }

            if (crossesPageWidth(a, metrics) || wrap) {
                // adjust wrap vector
                horWrap = -(metrics.getHorizontalPosition(a));
                vertWrap = metrics.getCumulativeTierHeights() +
                    metrics.getInterlinearizer().getBlockSpacing();

                // wrap annots starting right annotation
                // (keep root annots and their symb assocations together)
                int startAt = i;

                if (metrics.getHorizontalPosition(a) == lastBlockStart) {
                    startAt = lastBlockIndex;
                }

                wrap(metrics, horWrap, vertWrap, startAt);
                wrap = false;
            }
        }
    }

    private static boolean crossesPageWidth(Annotation a, Metrics metrics) {
        return (metrics.getHorizontalPosition(a) + metrics.getUsedWidth(a)) > (metrics.getInterlinearizer()
                                                                                      .getWidth() -
        metrics.getLeftMargin());
    }

    private static void wrap(Metrics metrics, int hWrap, int vWrap,
        int startingIndex) {
        Vector orderedAnnots = metrics.getBlockWiseOrdered();

        for (int i = startingIndex; i < orderedAnnots.size(); i++) {
            Annotation a = (Annotation) orderedAnnots.elementAt(i);
            metrics.setHorizontalPosition(a,
                metrics.getHorizontalPosition(a) + hWrap);
            metrics.setVerticalPosition(a,
                metrics.getVerticalPosition(a) + vWrap);
        }
    }

    /**
     * Hides lines were no annotations are drawn by vertical repositioning. So
     * also be applicable after wrapping.
     *
     * @param metrics
     */
    public static void hideEmptyLines(Metrics metrics) {
        int currentVPos = 0;
        int currentVBlockBegin = 0;

        Vector emptyLines = new Vector();
        Vector emptyLineHeights = new Vector(); // parallel with 'emptyLines'

        int[] vPositionsInTemplate = metrics.getVPositionsInTemplate();
        int maxVerticalPosition = metrics.getMaxVerticalPosition();

        // get 'Set' with all annotation positions.
        // repeatedly go over vPositionsInTemplate until past maxVerticalPosition.
        // if position not in set of annotation positions, line is empty.
        // delete lines by subtracting tier's space from verticalPositions > position.
        Vector annotPositions = metrics.getPositionsOfNonEmptyTiers();

        while (currentVPos < maxVerticalPosition) {
            for (int i = 0; i < vPositionsInTemplate.length; i++) {
                currentVPos = currentVBlockBegin + vPositionsInTemplate[i];

                Integer currentVPosInt = new Integer(currentVPos);

                if (!annotPositions.contains(currentVPosInt)) {
                    emptyLines.add(currentVPosInt);

                    int previousPos = 0;

                    if (i > 0) {
                        previousPos = vPositionsInTemplate[i - 1];
                    }

                    emptyLineHeights.add(new Integer(vPositionsInTemplate[i] -
                            previousPos));
                }
            }

            currentVBlockBegin = currentVPos +
                metrics.getInterlinearizer().getBlockSpacing() +
                metrics.getInterlinearizer().getLineSpacing();
        }

        // now delete empty lines
        // - get list of annots, sorted on vertical position
        // - iterate over them
        // - if 'next empty line' passed increase correction
        // - subtract correction from annots vertical position
        if (emptyLines.size() == 0) { // no empty lines, ready

            return;
        }

        Iterator emptyLineIter = emptyLines.iterator();
        Iterator lineHeightIter = emptyLineHeights.iterator();

        int nextEmptyLine = 0;

        if (emptyLineIter.hasNext()) {
            nextEmptyLine = ((Integer) emptyLineIter.next()).intValue();
        }

        int nextLineHeight = 0;

        if (lineHeightIter.hasNext()) {
            nextLineHeight = ((Integer) lineHeightIter.next()).intValue();
        }

        int correction = 0;

        Vector sortedAnnots = metrics.getVerticallyOrdered();
        Iterator annIter = sortedAnnots.iterator();

        while (annIter.hasNext()) {
            Annotation a = (Annotation) annIter.next();

            int vPos = metrics.getVerticalPosition(a);

            if (vPos > nextEmptyLine) {
                while (emptyLineIter.hasNext()) {
                    correction += nextLineHeight;

                    nextEmptyLine = ((Integer) emptyLineIter.next()).intValue();
                    nextLineHeight = ((Integer) lineHeightIter.next()).intValue();

                    if (vPos < nextEmptyLine) { // until all empty lines are 'eaten'

                        break;
                    }
                }
            }

            // apply correction
            if (correction > 0) {
                metrics.setVerticalPosition(a, vPos - correction);
            }
        }
    }
}
