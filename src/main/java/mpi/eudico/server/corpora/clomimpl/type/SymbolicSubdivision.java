package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: SymbolicSubdivision.java 4060 2005-07-26 13:42:10Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicSubdivision extends ConstraintImpl {
    /**
     * Creates a new SymbolicSubdivision instance
     */
    public SymbolicSubdivision() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStereoType() {
        return Constraint.SYMBOLIC_SUBDIVISION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    public void forceTimes(long[] segment, Tier forTier) {
        //		if (forTier != null) {
        //			segment[1] = segment[0];
        //		}
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] + (long) (elmtsLeftAndRight[0] * durationPerAnnot));
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] +
        (long) ((elmtsLeftAndRight[0] + 1) * durationPerAnnot));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsInsertion() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param beforeAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation insertBefore(Annotation beforeAnn, Tier theTier) {
        Annotation parentAnn = (Annotation) ((RefAnnotation) beforeAnn).getReferences()
                                             .firstElement();
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        if (((RefAnnotation) beforeAnn).hasPrevious()) {
            RefAnnotation prevAnn = ((RefAnnotation) beforeAnn).getPrevious();

            prevAnn.setNext(newAnn);
        }

        newAnn.setNext((RefAnnotation) beforeAnn);

        ((TierImpl) theTier).addAnnotation(newAnn);

        return newAnn;
    }

    /**
     * <p>
     * MK:02/06/24<br> Method inserts a new RefAnnotation after a given RefAnnotation.<br>
     * Note that you can create the new Annotation on a different tier than
     * the given Annotation
     * </p>
     *
     * @param afterAnn WRONG TYPE: must be RefAnnotation. Tier member ignored.
     *        Not nullable.
     * @param theTier tier of the newly created RefAnnotation, not nullable.
     *
     * @return DOCUMENT ME!
     */
    public Annotation insertAfter(Annotation afterAnn, Tier theTier) {
        Annotation parentAnn = (Annotation) ((RefAnnotation) afterAnn).getReferences()
                                             .firstElement();
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        //MK:02/06/24 insert into "next" chain 
        if (((RefAnnotation) afterAnn).hasNext()) {
            RefAnnotation nextAnn = ((RefAnnotation) afterAnn).getNext();

            newAnn.setNext(nextAnn);
        }

        ((RefAnnotation) afterAnn).setNext(newAnn);

        ((TierImpl) theTier).addAnnotation(newAnn);

        return newAnn;
    }

    private void getSegmentForChainOf(RefAnnotation theAnnot, long[] segment,
        int[] elmtsLeftAndRight) {
        RefAnnotation firstOfChain = getFirstOfChain(theAnnot, elmtsLeftAndRight);
        RefAnnotation lastOfChain = getLastOfChain(theAnnot, elmtsLeftAndRight);

        Vector refsOfFirst = firstOfChain.getReferences();

        if (refsOfFirst.size() > 0) {
            Annotation beginRef = (Annotation) (refsOfFirst.firstElement());
            segment[0] = beginRef.getBeginTimeBoundary();
        }

        Vector refsOfLast = lastOfChain.getReferences();

        if (refsOfLast.size() > 0) {
            Annotation endRef = (Annotation) (lastOfChain.getReferences()
                                                         .firstElement());
            segment[1] = endRef.getEndTimeBoundary();
        }
    }

    private RefAnnotation getFirstOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation first = theAnnot;

        int leftElementCount = 0;

        while (first.hasPrevious()) {
            first = first.getPrevious();
            leftElementCount++;
        }

        elmtsLeftAndRight[0] = leftElementCount;

        return first;
    }

    private RefAnnotation getLastOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation last = theAnnot;

        int rightElementCount = 0;

        while (last.hasNext()) {
            last = last.getNext();
            rightElementCount++;
        }

        elmtsLeftAndRight[1] = rightElementCount;

        return last;
    }

    /**
     * Detach annotation theAnn from tier theTier by reconnecting remaining
     * Annotations on the tier. Assumes that all references and
     * ParentAnnotationListener registrations are already cleaned up.
     *
     * @param theAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     */
    public void detachAnnotation(Annotation theAnn, Tier theTier) {
        RefAnnotation a = (RefAnnotation) theAnn; // cast is safe for case of SymbolicSubdivision

        RefAnnotation prev = a.getPrevious();
        RefAnnotation next = a.getNext();

        // reconnect
        if (prev != null) {
            prev.setNext(next);
        } else if (next != null) {
            next.setPrevious(null);
        }
    }
}
