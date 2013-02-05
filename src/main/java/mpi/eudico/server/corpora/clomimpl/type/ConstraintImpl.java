package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;

import java.util.Iterator;
import java.util.Vector;


/**
 * DOCUMENT ME!
 * $Id: ConstraintImpl.java 4062 2005-07-26 13:45:50Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public abstract class ConstraintImpl implements Constraint {
    /** Holds value of property DOCUMENT ME! */
    protected Vector nestedConstraints;

    /**
     * Creates a new ConstraintImpl instance
     */
    public ConstraintImpl() {
        nestedConstraints = new Vector();
    }

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    public void forceTimes(long[] segment, Tier forTier) {
        Iterator cIter = nestedConstraints.iterator();

        while (cIter.hasNext()) {
            ((Constraint) cIter.next()).forceTimes(segment, forTier);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long t = 0;

        Iterator cIter = nestedConstraints.iterator();

        while (cIter.hasNext()) {
            t = ((Constraint) cIter.next()).getBeginTimeForRefAnnotation(theAnnot);
        }

        return t;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long t = 0;
        Iterator cIter = nestedConstraints.iterator();

        while (cIter.hasNext()) {
            t = ((Constraint) cIter.next()).getEndTimeForRefAnnotation(theAnnot);
        }

        return t; // default
    }

    /**
     * DOCUMENT ME!
     *
     * @param begin DOCUMENT ME!
     * @param end DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTimeSlotsForNewAnnotation(long begin, long end,
        Tier forTier) {
        Iterator cIter = nestedConstraints.iterator();
        Vector slots = new Vector();

        while (cIter.hasNext()) {
            Constraint c = (Constraint) cIter.next();

            slots = c.getTimeSlotsForNewAnnotation(begin, end, forTier);
        }

        return slots;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    public void enforceOnWholeTier(Tier theTier) {
        //	Iterator cIter = nestedConstraints.iterator();
        //	while (cIter.hasNext()) {
        //		((Constraint) cIter.next()).enforceOnWholeTier(theTier);
        //	}	
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsInsertion() {
        return false;
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
        return null;
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
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     */
    public void detachAnnotation(Annotation theAnn, Tier theTier) {
        // default: do nothing
    }

    /**
     * DOCUMENT ME!
     *
     * @param theConstraint DOCUMENT ME!
     */
    public void addConstraint(Constraint theConstraint) {
        nestedConstraints.add(theConstraint);
    }
	
	/**
	 * Overrides <code>Object</code>'s equals method by checking number and type
	 * of the nested Constraints of the other object to be equal to the number 
	 * and type of the nested Constraints in this object.
	 * 
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			// null is never equal
			return false;
		}
		if (obj == this) {
			// same object reference 
			return true;
		}
		if (!(obj instanceof ConstraintImpl)) {
			// it should be a ConstraintImpl object
			return false;
		}
		
		ConstraintImpl other = (ConstraintImpl) obj;
		
		if (other.getStereoType() != this.getStereoType()) {
			return false;
		}
		
		if (nestedConstraints.size() != other.nestedConstraints.size()) {
			return false;
		}
		
		boolean allConstraintsEqual = true;
		
		loop:
		for (int i = 0; i < nestedConstraints.size(); i++) {
			ConstraintImpl ci = (ConstraintImpl) nestedConstraints.get(i);
			for (int j = 0; j < other.nestedConstraints.size(); j++) {
				if (ci.equals(other.nestedConstraints.get(j))) {
					continue loop;	
				}
			}
			// if we get here constraints are unequal
			allConstraintsEqual = false;
			break;
		}
		
		return allConstraintsEqual;
	}
}
