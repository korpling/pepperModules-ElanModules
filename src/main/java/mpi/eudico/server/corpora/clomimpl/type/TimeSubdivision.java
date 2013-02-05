package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.Iterator;
import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: TimeSubdivision.java 4255 2005-08-17 13:34:41Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class TimeSubdivision extends ConstraintImpl {
    /**
     * Creates a new TimeSubdivision instance
     */
    public TimeSubdivision() {
        super();

        addConstraint(new NoTimeGapWithinParent());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStereoType() {
        return Constraint.TIME_SUBDIVISION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    public void forceTimes(long[] segment, Tier forTier) {
        //	System.out.println("TimeSubdivision.forceTimes called");	
        if (forTier != null) {
            Annotation annAtBegin = ((TierImpl) forTier).getAnnotationAtTime(segment[0]);
            Annotation annAtEnd = ((TierImpl) forTier).getAnnotationAtTime(segment[1]);

            if ((annAtBegin != null) && (annAtEnd == null)) {
                segment[1] = annAtBegin.getEndTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd != null)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin != null) && (annAtEnd != null) &&
                    (annAtBegin != annAtEnd)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd == null)) {
                // if annotations in between, constrain to first of them
                Vector annotsInBetween = ((TierImpl) forTier).getOverlappingAnnotations(segment[0],
                        segment[1]);

                if (annotsInBetween.size() > 0) {
                    AlignableAnnotation a = (AlignableAnnotation) annotsInBetween.elementAt(0);
                    segment[0] = a.getBegin().getTime();
                    segment[1] = a.getEnd().getTime();
                } else {
                    segment[0] = segment[1];
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    public void enforceOnWholeTier(Tier theTier) {
        System.out.println("enforcing on tier: " + theTier.getName());

        // force times (later to "IncludedIn" constraint?)
        Iterator annIter = null;

        annIter = ((TierImpl) theTier).getAnnotations().iterator();

        while (annIter.hasNext()) {
            AlignableAnnotation ann = (AlignableAnnotation) annIter.next();

            //	long[] segment = {ann.getBegin().getTime(), ann.getEnd().getTime()};
            long[] segment = {
                ann.getBeginTimeBoundary(), ann.getEndTimeBoundary()
            };

            Tier parent = ((TierImpl) theTier).getParentTier();
            forceTimes(segment, parent);

            //	if (segment[0] == segment[1]) {
            //			((TierImpl) theTier).removeAnnotation(ann);
            //	}
            //	else {
            ann.getBegin().setTime(segment[0]);
            ann.getEnd().setTime(segment[1]);

            //	}						
        }

        // no gaps within parent segment, pass to NoTimeGapWithinParent constraint
        Iterator cIter = nestedConstraints.iterator();

        while (cIter.hasNext()) {
            ((Constraint) cIter.next()).enforceOnWholeTier(theTier);
        }
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
        // assumption: first added Constraint is a NoTimeGapWithinParent
        return ((NoTimeGapWithinParent) nestedConstraints.elementAt(0)).insertBefore(beforeAnn,
            theTier);
    }

    /**
     * DOCUMENT ME!
     *
     * @param afterAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation insertAfter(Annotation afterAnn, Tier theTier) {
        // assumption: first added Constraint is a NoTimeGapWithinParent
        return ((NoTimeGapWithinParent) nestedConstraints.elementAt(0)).insertAfter(afterAnn,
            theTier);
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     */
    public void detachAnnotation(Annotation theAnn, Tier theTier) {
        // assumption: first added Constraint is a NoTimeGapWithinParent
        ((NoTimeGapWithinParent) nestedConstraints.elementAt(0)).detachAnnotation(theAnn,
            theTier);
    }
}
