package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clom.AnnotationCore;

import java.util.Comparator;


/**
 * Compares both begin and end time of 2 annotations or annotation data
 * records. The times may be interpolated times.
 */
public class AnnotationDataComparator implements Comparator {
    /**
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof AnnotationCore ||
                o1 instanceof AnnotationDataRecord)) {
            throw new ClassCastException("Invalid type: " + o1.getClass());
        }

        if (!(o2 instanceof AnnotationCore ||
                o2 instanceof AnnotationDataRecord)) {
            throw new ClassCastException("Invalid type: " + o1.getClass());
        }

        if (o1 instanceof AnnotationCore) {
            if (o2 instanceof AnnotationCore) {
                // begin time
                if (((AnnotationCore) o1).getBeginTimeBoundary() < ((AnnotationCore) o2).getBeginTimeBoundary()) {
                    return -1;
                } else if (((AnnotationCore) o1).getBeginTimeBoundary() > ((AnnotationCore) o2).getBeginTimeBoundary()) {
                    return 1;
                }

                // begin time equal, compare end time
                if (((AnnotationCore) o1).getEndTimeBoundary() < ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return -1;
                } else if (((AnnotationCore) o1).getEndTimeBoundary() > ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return 1;
                }
            } else {
                if (((AnnotationCore) o1).getBeginTimeBoundary() < ((AnnotationDataRecord) o2).getBeginTime()) {
                    return -1;
                } else if (((AnnotationCore) o1).getBeginTimeBoundary() > ((AnnotationDataRecord) o2).getBeginTime()) {
                    return 1;
                }

                if (((AnnotationCore) o1).getEndTimeBoundary() < ((AnnotationDataRecord) o2).getEndTime()) {
                    return -1;
                } else if (((AnnotationCore) o1).getEndTimeBoundary() > ((AnnotationDataRecord) o2).getEndTime()) {
                    return 1;
                }
            }
        } else {
            if (o2 instanceof AnnotationCore) {
                if (((AnnotationDataRecord) o1).getBeginTime() < ((AnnotationCore) o2).getBeginTimeBoundary()) {
                    return -1;
                } else if (((AnnotationDataRecord) o1).getBeginTime() > ((AnnotationCore) o2).getBeginTimeBoundary()) {
                    return 1;
                }

                if (((AnnotationDataRecord) o1).getEndTime() < ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return -1;
                } else if (((AnnotationDataRecord) o1).getEndTime() > ((AnnotationCore) o2).getEndTimeBoundary()) {
                    return 1;
                } //else return 0
            } else {
                if (((AnnotationDataRecord) o1).getBeginTime() < ((AnnotationDataRecord) o2).getBeginTime()) {
                    return -1;
                } else if (((AnnotationDataRecord) o1).getBeginTime() > ((AnnotationDataRecord) o2).getBeginTime()) {
                    return 1;
                }

                if (((AnnotationDataRecord) o1).getEndTime() < ((AnnotationDataRecord) o2).getEndTime()) {
                    return -1;
                } else if (((AnnotationDataRecord) o1).getEndTime() > ((AnnotationDataRecord) o2).getEndTime()) {
                    return 1;
                }
            }
        }

        return 0;
    }
}
