package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;

import java.util.Iterator;


/**
 * DOCUMENT ME!
 * $Id: MultipleRefs.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public class MultipleRefs extends ConstraintImpl {
    /**
     * Creates a new MultipleRefs instance
     */
    public MultipleRefs() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStereoType() {
        return Constraint.MULTIPLE_REFS;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long beginTimeBoundary = Long.MAX_VALUE;
        long beginB = 0;

        Iterator refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            beginB = ((Annotation) refIter.next()).getBeginTimeBoundary();

            beginTimeBoundary = Math.min(beginTimeBoundary, beginB);
        }

        return beginTimeBoundary;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long endTimeBoundary = 0;
        long endB = 0;

        Iterator refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            endB = ((Annotation) refIter.next()).getEndTimeBoundary();

            endTimeBoundary = Math.max(endTimeBoundary, endB);
        }

        return endTimeBoundary;
    }
}
