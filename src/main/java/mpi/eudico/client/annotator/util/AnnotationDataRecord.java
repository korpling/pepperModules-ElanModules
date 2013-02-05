package mpi.eudico.client.annotator.util;

import java.io.Serializable;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.TimeSlot;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.util.TimeInterval;


/**
 * A class to store annotation data that are essential for the programmatic
 * re-creation of an annotation.
 *
 * @author Han Sloetjes
 */
public class AnnotationDataRecord extends TimeInterval implements Serializable {
    /** the name of the tier the annotation belongs to */
    private String tierName;

    /** the value of the annotation */
    private String value;
    
    /** the annotation id */
    private String id;

    /** the isTimeAligned value of the begin time TimeSlot */
    private boolean beginTimeAligned;

    /** the isTimeAligned value of the end time TimeSlot */
    private boolean endTimeAligned;
    
    /** any references to external concepts or documents */
    private Object extRef;

    /**
     * Creates an AnnotationData object from the specified Annotation.
     *
     * @param annotation the Annotation
     */
    public AnnotationDataRecord(Annotation annotation) {
    	super(0, 0);
        TimeSlot ts = null;

        if (annotation != null) {
            value = annotation.getValue();
            beginTime = annotation.getBeginTimeBoundary();
            endTime = annotation.getEndTimeBoundary();
            id = annotation.getId();

            tierName = annotation.getTier().getName();

            if (annotation instanceof AlignableAnnotation) {
                ts = ((AlignableAnnotation) annotation).getBegin();
                beginTimeAligned = ts.isTimeAligned();
                ts = ((AlignableAnnotation) annotation).getEnd();
                endTimeAligned = ts.isTimeAligned();
            }
            if (annotation instanceof AbstractAnnotation) {
            	if (((AbstractAnnotation) annotation).getExtRef() instanceof ExternalReference) {
            		try {
            			// create a copy
            		    extRef = ((ExternalReference) ((AbstractAnnotation) annotation).getExtRef()).clone();
            		} catch (CloneNotSupportedException cnse) {
            			System.out.println("Could not clone:" + cnse.getMessage());
            		}
            	}
            }
        }
    }

    /**
     * Creates a new AnnotationData object from a tier name, annotation value, begintime and
     * an endtime.
     * 
     * @param tierName the tiername
     * @param value the annotation value
     * @param beginTime the begintime
     * @param endTime the endtime
     */
    public AnnotationDataRecord(String tierName, String value, long beginTime, long endTime) {
    	super(beginTime, endTime);
        this.tierName = tierName;
        this.value = value;

        if (this.endTime < this.beginTime && this.endTime >= 0) {
            this.endTime = this.beginTime + 1;
        }
        if (this.beginTime > -1) {
            beginTimeAligned = true;
        }
        if (this.endTime > -1) {
            endTimeAligned = true;
        }
    }

    /**
     * Returns true when the TimeSlot belonging to the begin boundary is  time
     * aligned. Only an AlignableAnnotation has a TimeSlot reference.
     *
     * @return true if the begin time TimeSlot is timealignable, false
     *         otherwise
     */
    public boolean isBeginTimeAligned() {
        return beginTimeAligned;
    }

    /**
     * Returns true when the TimeSlot belonging to the end boundary is  time
     * aligned. Only an AlignableAnnotation has a TimeSlot reference.
     *
     * @return true if the end time TimeSlot is timealignable, false otherwise
     */
    public boolean isEndTimeAligned() {
        return endTimeAligned;
    }

    /**
     * Sets the aligned flag of the begin time. The flag can only be set to true
     * when the begin time value is > -1. 
     * 
     * @param beginTimeAligned 
     */
    public void setBeginTimeAligned(boolean beginTimeAligned) {
    	if (beginTimeAligned) {
    		if (beginTime > -1) {
    			this.beginTimeAligned = beginTimeAligned;
    		}
    	} else {
    		this.beginTimeAligned = beginTimeAligned;
    	}
	}

    /**
     * Sets the aligned flag of the end time. The flag can only be set to true
     * when the end time value is > -1. 
     * 
     * @param endTimeAligned 
     */
	public void setEndTimeAligned(boolean endTimeAligned) {
		if (endTimeAligned) {
			if (endTime > -1) {
				this.endTimeAligned = endTimeAligned;
			}
		} else {
			this.endTimeAligned = endTimeAligned;
		}		
	}

	/**
     * Returns the name of the tier this annotation belongs to.
     *
     * @return the tier name
     */
    public String getTierName() {
        return tierName;
    }

    /**
	 * @param tierName the tierName to set
	 */
	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	/**
     * The text value of annotation.
     *
     * @return the text value of the annotation
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the value of the annotation.
     *
     * @return a String representation of this object; is the same as the value
     */
    public String toString() {
        return value;
    }
    
    /**
     * Sets the begin time. This method does not set the aligned flag for the begin time,
     * so that the begin time value can still be a virtual or interpolated time value.
     * 
     * @param beginTime the begin time
     */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }
    
    /**
     * Sets the end time. This method does not set the aligned flag for the end time,
     * so that the end time value can still be a virtual or interpolated time value.
     * 
     * @param endTime the end time
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the id.
     * 
     * @return the id, or null
     */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the annotation.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the external reference object.
	 * 
	 * @return the extRef
	 */
	public Object getExtRef() {
		return extRef;
	}

	/**
	 * Sets the external reference for this record. Only to be used in case of construction with a String
	 * instead of Annotation. The external reference of the annotation (even if not null) will not be 
	 * updated.
	 * 
	 * @param extRef sets the external reference value
	 */
	public void setExtRef(Object extRef) {
		this.extRef = extRef;
	}
    
}
