package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tag;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TagImpl;


/**
 * The Dobes minimal Tag
 *
 * @author Hennie Brugman
 * @version 6-Apr-2001
 */
public class AnnotationWrapperTag extends TagImpl {
    private Annotation annotation;

    /**
     * Constructs a Dobes Tag
     *
     * @param beginTime the Tag's begin time, beginTime = endTime = 0 if
     *        unaligned.
     * @param endTime the Tag's end time, beginTime = endTime = 0 if unaligned.
     * @param tier the Tier to which this Tag belongs
     * @param index ordering information of this Tag, (gives the position in
     *        the Transcription)
     * @param theAnnotation DOCUMENT ME!
     */
    public AnnotationWrapperTag(long beginTime, long endTime, Tier tier, int index,
        Annotation theAnnotation) {
        super(beginTime, endTime, tier);

        annotation = theAnnotation;

        this.index = index;
    }

    /**
     * Returns true if this tag comes after the parameter tag
     *
     * @param tag the tag against which is to be checked if this tag comes
     *        after it.
     *
     * @return DOCUMENT ME!
     */
    public boolean isAfter(Tag tag) {
        if (isTimeAligned() && tag.isTimeAligned() &&
                (beginTime > tag.getBeginTime())) {
            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String s = "";

        s += ("BT: " + new String(Long.toString(beginTime)) + ",ET: " +
        new String(Long.toString(endTime)));
        s += ("\n" + getValues());

        return s;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    // Compares based on comparison of wrapped Annotations
    public int compareTo(Object obj) {
        return ((Annotation) annotation).compareTo(((AnnotationWrapperTag) obj).getAnnotation());
    }

    /**
     * Checks equalitiy on basis of equality of wrapped Annotations
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o) {
        if (o instanceof AnnotationWrapperTag) {
            return ((Annotation) annotation).equals(((AnnotationWrapperTag) o).getAnnotation());
        } else {
            return false;
        }
    }
}
