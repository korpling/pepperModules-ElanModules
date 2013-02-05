package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Comparator;

/**
 * A comparator class that compares 2 timeslot records by first comparing the time value 
 * and next the id.
 * 
 * @author Han Sloetjes
 */
public class TimeSlotRecordComparator implements Comparator {

	/**
	 * Constructor
	 */
	public TimeSlotRecordComparator() {
		super();
	}
	
	/**
	 * Compares 2 timeslot records, by first comparing their time value and then their id or index.
	 * 
	 * @param o1 the first record
	 * @param o2 the second record
	 * 
	 * @throws ClassCastException if one if the arguments is not a TimeSlotRecord
	 */
	public int compare(Object o1, Object o2) throws ClassCastException {
		TimeSlotRecord tsr1 = (TimeSlotRecord) o1;
		TimeSlotRecord tsr2 = (TimeSlotRecord) o2;
		
		if (tsr1.getValue() < tsr2.getValue()) {
			return -1;
		}
		if (tsr1.getValue() > tsr2.getValue()) {
			return 1;
		}
		if (tsr1.getValue() == tsr2.getValue()) {
			if (tsr1.getId() < tsr2.getId()) {
				return -1;
			}
			if (tsr1.getId() > tsr2.getId()) {
				return 1;
			}
		}
		
		return 0;
	}

}
