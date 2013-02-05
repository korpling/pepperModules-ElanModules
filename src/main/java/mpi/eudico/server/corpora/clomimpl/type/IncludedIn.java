package mpi.eudico.server.corpora.clomimpl.type;

import java.util.Iterator;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


public class IncludedIn extends ConstraintImpl {

    /**
     * A time subdivision constraint that allows gaps, i.e. empty space before, after and/or 
     * between annotations on a depending tier, within the interval of an annotation on the 
     * parent tier are allowed.
     */
    public IncludedIn() {
        super();
    }

    /**
     * Force begin and end of an annotation {segment}
     * Copied from TimeSubdivision.
     */
    public void forceTimes(long[] segment, Tier forTier) {
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
     * @see mpi.eudico.server.corpora.clomimpl.type.Constraint#getStereoType()
     */
    public int getStereoType() {
        return Constraint.INCLUDED_IN;
    }

    /**
     * Don't allow unaligned annotations on this kind of tiers.
     *
     * @return true if annotations can be added with insertBefore and insertAfter, 
     * false otherwise.
     */
    public boolean supportsInsertion() {
        return false;
    }
    
    public void detachAnnotation(Annotation theAnn, Tier theTier) {
        // do nothing??
    }
    
    public void enforceOnWholeTier(Tier theTier) {
        // empty ??
        System.out.println("IncludedIn: enforce...");
    }
    
    /**
     * Create 2 new time slots or find existing slots for the new annotation.
     * It is assumed that end and begin have been checked: end > begin.
     * It is also assumed that the tier has a parent tier and that the parent 
     * is time alignable
     */
    public Vector getTimeSlotsForNewAnnotation(long begin, long end,
            Tier forTier) {
        Vector slots = new Vector(2);
        
        TierImpl parentTier = (TierImpl) ((TierImpl) forTier).getParentTier();
        AlignableAnnotation parentAnn = null;
        Vector overlappingParentAnns = parentTier.getOverlappingAnnotations(begin, end);
        if (overlappingParentAnns.size() == 0) {
            return slots;
        } else if (overlappingParentAnns.size() >= 1) {
            // pick the first of the overlapping annotations on the parent
            parentAnn = (AlignableAnnotation) overlappingParentAnns.get(0);
        }
        if (!parentAnn.getBegin().isTimeAligned() || !parentAnn.getEnd().isTimeAligned()) {
            // don't create an annotation if the parent is a time subdivision tier and one 
            // of the slots is unaligned
            return slots;
        }
        if (parentAnn.getBegin().getTime() > begin) {
            begin = parentAnn.getBegin().getTime();
        }
        if (parentAnn.getEnd().getTime() < end) {
            end = parentAnn.getEnd().getTime();
        }
        // we have a candidate parent annotation and a begin and end time within the boundaries
        // get existing annotations on tier
        Vector overlappingAnnots = ((TierImpl) forTier).getOverlappingAnnotations(begin, end);
        
        TimeOrder timeOrder = ((TranscriptionImpl) (forTier.getParent())).getTimeOrder();
        TimeSlot bts = new TimeSlotImpl(begin, timeOrder);
        TimeSlot ets = new TimeSlotImpl(end, timeOrder);
        timeOrder.insertTimeSlot(bts);// insertTimeSlot(bts, parentAnn.getBegin(), parentAnn.getEnd())
        timeOrder.insertTimeSlot(ets);
        slots.add(bts);
        slots.add(ets);
        
        Iterator anIt = overlappingAnnots.iterator();
        AlignableAnnotation curAnn = null;
        
        while (anIt.hasNext()) {
            curAnn = (AlignableAnnotation) anIt.next();
            if (curAnn.getBegin().getTime() >= begin) {
                if (curAnn.getEnd().getTime() > end) {
                    curAnn.getBegin().setTime(end);
                } else {
                    curAnn.setBegin(curAnn.getEnd());// will be marked for deletion
                }
            } else {//curann.begin < begin
                if (curAnn.getEnd().getTime() > begin) {
                    curAnn.getEnd().setTime(begin);
                }
            }
        }
        
        return slots;
    }

}
