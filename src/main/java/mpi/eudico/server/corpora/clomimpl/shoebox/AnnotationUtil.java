package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;

import java.util.Enumeration;
import java.util.Vector;

/**
 * DOCUMENT ME! $Id: AnnotationUtil.java 4222 2005-08-11 15:28:22Z hasloe $
 *
 * @author $Author$
 * @version $Revision$
 */
public class AnnotationUtil {
//    private static Logger logger = Logger.getLogger(AnnotationUtil.class.toString());

    /**
     * MK:02/06/28<br> An Annotation has children, possibly on many tiers.
     *
     * @param tthis DOCUMENT ME!
     * @param tier restrict direct childs to this tier
     *
     * @return all direct child RefAnnotations on given tier, or empty Vector.
     */
    public static final Vector getKids(Annotation tthis, Tier tier) {
 //       logger.log(Level.INFO,
 //           "-- getKids (" + tthis.getValue() + ", " + tier.getName());

        Vector result = new Vector();

        if (!(tthis instanceof Annotation)) {
            return result;
        }

        Vector v = new Vector(((AbstractAnnotation) tthis).getParentListeners());

        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();

            if (!(o instanceof RefAnnotation)) {
                continue;
            }

            RefAnnotation r = (RefAnnotation) o;

            if (r.getTier() != tier) {
                continue;
            }

 //           logger.log(Level.INFO, " >> " + r.getValue());
            result.add(r);
        }

//        logger.log(Level.INFO, "");

        return result;
    }

    /**
     * MK:02/06/28<br> An Annotation has children, possibly on many tiers.  If
     * there are kids are on the same tier, they are chained according the
     * next member of a RefAnno. 'this' may not have any childs, signaled by
     * returning null.
     *
     * @param tthis DOCUMENT ME!
     * @param tier restrict direct childs to this tier
     *
     * @return last direct child Annotations or null!
     */
    public static final RefAnnotation getLastKid(Annotation tthis, Tier tier) {
 //       logger.log(Level.INFO,
 //           "-- getLastKid (" + tthis.getValue() + ", " + tier.getName());

        Vector kids = AnnotationUtil.getKids(tthis, tier);

        if (kids.size() == 0) {
            return null;
        } else {
            return AnnotationUtil.getLast((RefAnnotation) kids.elementAt(0));
        }
    }

    /**
     * MK:02/06/28<br>The next-chain of RefAnnos lead to the last brother. If
     * there is no next-chain, return yourself
     *
     * @param tthis DOCUMENT ME!
     *
     * @return last Annotations from next-chain
     */
    public static final RefAnnotation getLast(RefAnnotation tthis) {
        //System.out.println("------- looking for last of " + tthis.getValue());
        if (tthis.getNext() == null) {
            return tthis;
        }

        // looking for a stack overflow
        if (tthis.getNext() == tthis) {
            return tthis; // who knows...
        }

        return AnnotationUtil.getLast(tthis.getNext());
    }
}
