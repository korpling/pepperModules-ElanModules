package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TimeSlot;

import java.util.Comparator;


/**
 * Compares two TimeSlot objects by first comparing their time and next
 * (if one of the slots is unaligned) their index.
 *
 * @author Han Sloetjes
 */
public class TimeSlotComparator implements Comparator {
    private TimeSlot t1;
    private TimeSlot t2;

    /**
     * Compares two TimeSlot objects by first comparing their time and next
     * their index.
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof TimeSlot)) {
            throw new IllegalArgumentException(
                "First object is not an TimeSlot object");
        }

        if (!(o2 instanceof TimeSlot)) {
            throw new IllegalArgumentException(
                "Second object is not an TimeSlot object");
        }

        t1 = (TimeSlot) o1;
        t2 = (TimeSlot) o2;

        if (t1.isTimeAligned() && t2.isTimeAligned()) {
            if (t1.getTime() < t2.getTime()) {
                return -1;
            } else if (t1.getTime() == t2.getTime()) {
                return 0;
            }

            return 1;
        } else {
            if (t1.getIndex() < t2.getIndex()) {
                return -1;
            } else if (t1.getIndex() == t2.getIndex()) {
                return 0;
            }

            return 1;
        }
    }
}
