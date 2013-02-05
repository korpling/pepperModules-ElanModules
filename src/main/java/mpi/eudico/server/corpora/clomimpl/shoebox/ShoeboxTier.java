/*
 * $Id: ShoeboxTier.java 4255 2005-08-17 13:34:41Z hasloe $
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationWrapperTag;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Helper Class. Should be 'package' , not public.
 * @version Aug 2005 Identity removed
 */
public class ShoeboxTier extends TierImpl {
//    private static Logger logger = Logger.getLogger(ShoeboxTier.class.getName());

    /**
     * <p>
     * MK:02/06/19<br> becomes a top tier
     * </p>
     *
     * @param name matches Ref_AT_Paul
     * @param participant DOCUMENT ME!
     * @param parent the parent transcription
     * @param theType DOCUMENT ME!
     */
    public ShoeboxTier(String name, String participant, Transcription parent,
        LinguisticType theType) {
        super(name, participant, parent, theType);
        
		//register 'this' tier with the parent transcription
		if (parent != null) {	// since viewermanager2 (ab)uses an empty tier
			parent.addTier(this);
		}
    }

    /**
     * <p>
     * MK:02/06/19<br>
     * </p>
     *
     * @param parenttier the parenttier
     * @param name matches Ref_AT_Paul
     * @param participant DOCUMENT ME!
     * @param parent the parent transcription
     * @param theType DOCUMENT ME!
     */
    public ShoeboxTier(Tier parenttier, String name, String participant,
        Transcription parent, LinguisticType theType) {
        // HB, 12 jul 02: added LinguisticType argument.
        super(parenttier, name, participant, parent, theType);
        
		//register 'this' tier with the parent transcription
		if (parent != null) {	// since viewermanager2 (ab)uses an empty tier
			parent.addTier(this);
		}
    }

    /**
     * <p>
     * MK:02/06/10<br> The creation of a time-aligned annotation requires three
     * parameters (see parameter list). After setting all paramters, the
     * Annoation is added to 'this' tier and returned.
     * </p>
     *
     * @param t0 begin-time
     * @param t1 end-time
     * @param val text
     *
     * @return AlignableAnnotation --> should go in TierImpl.
     */
    public final Annotation addAlignableAnnotation(long t0, long t1, String val) {
//        logger.log(Level.FINE,
//            "-- Afix (" + t0 + ", " + t1 + ", " + this.getName() + ", " + val +
 //           ")");

        TimeSlot tt0 = new TimeSlotImpl(t0,
                ((Transcription) this.parent).getTimeOrder());
        TimeSlot tt1 = new TimeSlotImpl(t1,
                ((Transcription) this.parent).getTimeOrder());

        //MK:02/06/27 downcast needed till refactoring of Tier.parent
        ((Transcription) this.parent).getTimeOrder().insertTimeSlot(tt0);
        ((Transcription) this.parent).getTimeOrder().insertTimeSlot(tt1);

        Annotation result = new AlignableAnnotation(tt0, tt1, this);
        result.setValue(val);
        this.insertAnnotation(result); // HB, 14 aug 02, from addAnnotation

        return result;
    }

    /**
     * <p>
     * MK:02/06/17<br> The creation of referenced annotation requires two
     * parameters (see parameter list). After setting all paramters, the
     * Annoation is added to 'this' tier, appended to the last kid of the
     * parent and returned.
     * </p>
     *
     * @param dad parent annotation
     * @param val text
     *
     * @return RefAnnotation --> should go in TierImpl.
     */
    public final Annotation addRefAnnotation(Annotation dad, String val) {
//        logger.log(Level.FINE,
//            "-- Aref (" + dad.getValue() + ", " + this.getName() + ", " + val +
//            ")");

        Annotation result = new RefAnnotation(dad, this);

        //dad.addACMEditListener(result);
        result.setValue(val);

        RefAnnotation brother = AnnotationUtil.getLastKid(dad, this);

        if ((brother != null) && (brother != result)) {
            brother.setNext((RefAnnotation) result);
 //           logger.log(Level.FINE,
 //               "-- brother " + brother.getValue() + "<-----" +
 //               result.getValue());
        }

        this.insertAnnotation(result); // HB, 14 aug 02, from addAnnotation

        return result;
    }

    /**
     * Quick fix because tiername is unique.<br> Example: ref_AT_Paul --> ref
     *
     * @return the name of 'this' tier without the speaker
     */
    public final String getNameNoSpeaker() {
        String result = this.getName();

        return result.substring(0, result.indexOf('@'));
    }

    /**
     * <p>
     * MK:02/06/10<br> Implementing (the only) abstract method of abstract
     * superclass TierImpl. All(?) Viewers handle only DobesTags, not
     * Annotations
     * </p>
     *
     * @return Vector of DobesTags
     */
    public Vector getTags() {
        //MK:02/06/14 copied from DobesTier.
        Vector result = new Vector();
        int index = 0;
        Iterator annIter = annotations.iterator();

        while (annIter.hasNext()) {
            Annotation annot = (Annotation) annIter.next();
            AnnotationWrapperTag tag = new AnnotationWrapperTag(annot.getBeginTimeBoundary(),
                    annot.getEndTimeBoundary(), this, index, annot);
            tag.addValue(annot.getValue());
            result.add(tag);
            index++;
        }

        return result;
    }
}
