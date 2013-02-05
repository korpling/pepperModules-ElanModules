package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * DOCUMENT ME!
 * $Id: SymbolicAssociation.java 4060 2005-07-26 13:42:10Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicAssociation extends ConstraintImpl {
    /**
     * Creates a new SymbolicAssociation instance
     */
    public SymbolicAssociation() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStereoType() {
        return Constraint.SYMBOLIC_ASSOCIATION;
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
        //	System.out.println("begin for ref annot: " + theAnnot.getValue() + " on tier: " + theAnnot.getTier().getName());

        long beginTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = (Annotation) (theAnnot.getReferences()
                                                   .firstElement());
            beginTB = ref.getBeginTimeBoundary();
        }

        return beginTB;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long endTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = (Annotation) (theAnnot.getReferences()
                                                   .firstElement());
            endTB = ref.getEndTimeBoundary();
        }

        return endTB;
    }
}
