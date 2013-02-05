package mpi.eudico.server.corpora.clomimpl.abstr;

import java.io.Serializable;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Tag;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.util.ACMEditableObject;

/**
 * Tag is an implementation of the Tag interface. A Tag has begin and 
 * end times, and a list of
 * associated text values as specified by the CodeGroup of the Tier
 * that contains the Tag. Since many Tags are used and regularly 
 * accessed on the client, Tags are not implemented as server based
 * objects but they are Serializable instead. The accessibility of
 * Tags is determined by the accessibility of the (server based) Tier
 * that contains the Tags.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 4-May-1999
 */
public abstract class TagImpl implements Tag, Serializable{

    	public void modified(int operation, Object modification) {
    		handleModification(this, operation, modification);
    	}
    	
    	
   	public void handleModification(ACMEditableObject source, int operation, Object modification) {
   		if (tier != null) {
   			tier.handleModification(source, operation, modification);
   		}
   	}


	/**
	 * The Tag's begin time in milliseconds.
	 */
	protected long beginTime;
	
	/**
	 * The Tag's end time in milliseconds.
	 */
	protected long endTime;
	
	/**
	 * The list of text values as specified by a CodeGroup.
	 */
	protected Vector valueList;
	
	/**
	 * The Tag's Tier.
	 */
	private Tier tier;
	
	/**
	 * Index of Tag in Transcription. Administered by MetaTime.
	 */
	protected int index;
	
	public TagImpl(long theBeginTime, long theEndTime, Tier theTier) {
		beginTime = theBeginTime;
		endTime = theEndTime;
				
		tier = theTier;
		
		valueList = new Vector();
	}
	
	public long getBeginTime() {
		return beginTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Adds a value at the end of a Tag's value list.
	 *
	 * @param theValue	text value to be added
	 */
	public void addValue(String theValue) {
		// sometimes tab and/or newline characters are part of the value's string.
		// in painted views (e.g. time line viewer) these characters appear as little
		// squares. For the moment we chose to replace tabs by the substring "\t"
		// and newlines by the substring "\n".	
		int lastIndex = theValue.lastIndexOf('\n');		
		while (lastIndex > 0) {
			theValue = theValue.substring(0, lastIndex - 1) + "\n" + theValue.substring(lastIndex + 1);
			lastIndex =theValue.lastIndexOf('\n');
		}
	
		lastIndex = theValue.lastIndexOf('\t');		
		while (lastIndex > 0) {
			theValue = theValue.substring(0, lastIndex) + "\\t" + theValue.substring(lastIndex + 1);
			lastIndex =theValue.lastIndexOf('\t');
		}
	
		// alternative implentation, replacing these characters by spaces.
	//	theValue = theValue.replace('\n', ' ');
	//	theValue = theValue.replace('\t', ' ');
		
		valueList.add(theValue.trim());
	}
	
	/**
	 * Adds a value at the end of a Tag's value list.
	 *
	 * @param theValue	text value to be added
	 */
	public void addValue(Object value) {
		valueList.add(value);
	}
	
	/**
	 * Returns the Tag's text values in the order specified by
	 * the CodeGroup of the Tag's containing Tier.
	 *
	 * @return 	the list of text values
	 */
	public Vector getValues() {
		return valueList;
	}
	
	/**
	 * Returns the Tier of which this Tag is a component.
	 *
	 * @return	the Tag's Tier
	 */
	 public Tier getTier() {
	 	return tier;
	 }

	 public int getIndex() {
	 	return index;
	 }
	 
	 public void setIndex(int theIndex) {
	 	index = theIndex;
	 }

	 /**
	  * Returns true if the Tag is aligned with the time axis of Transcription's
	  * media. An unaligned Tag has zero begin and end times.
	  */
	 public boolean isTimeAligned() {
	 	if ((beginTime == 0) && (endTime == 0)) {
	 		return false;
	 	}
	 	else {
	 		return true;
	 	}
	 }
	
	 /**
	  * Returns true if this Tag comes after theTag. Implementation specific Tag ordering
	  * is handled here (used when adding to MetaTime). Comparable.compareTo orders Tags on index.
	  */
	 public abstract boolean isAfter(Tag theTag);


	// Comparable interface method
	/**
	 * Specifies sort relation between Tags: Tags are sorted
	 * in order specified by the Transcription's MetaTime.
	 */
/*	public int compareTo(Object obj) {
		int ret = 1;
		
			MetaTime metaTime = ((Transcription)tier.getParent()).getMetaTime();
		
			if (metaTime.startsBefore(((Tag) obj), this)) {
				ret = 1;
			}
			else {
				ret = -1;
			}

		return ret;
	} 
*/
	// here compareTo uses Tag.getIndex, which is not a remote
	// method invocation, in contrast with metaTime.startsBefore.
	// This works substantially faster.
	public int compareTo(Object obj) {
		int ret = 1;
		
		if (this.getIndex() > ((Tag)obj).getIndex()) {
			ret = 1;
		}
		else {
			ret = -1;
		} 
		
		return ret;		
	}
	
	/**
	 * Default comparison for equality of Tags is done on basis of index values
	 * as assigned by the Transcription's MetaTime. For specific implementations
	 * of TagImpl this may be overridden. For example, in case of Tags initialized
	 * from a relational database equality checking may be done on basis of 
	 * unique database key values.
	 */		
	public boolean equals(Object o) {
	//	if ((((Tag) o).getBeginTime() == beginTime) &&
	//	   (((Tag) o).getEndTime() == endTime) &&
	//	   (((Tag) o).getValues().equals(valueList))) {	
		   
		   if ( (!(o instanceof Tag)) || (((Tag) o).getIndex() != index)) {
		   	return false;
		   }
		   else {
		   	return true;
		   }
	}	

	/**
	 * Default hashCode is overriden the same way as the equals method.
	 * This is necessary for getting consistent results in a Hashtable. 
	 */
	//AK 21.06.2002

	public int hashCode(){
		return index;
	}
}
