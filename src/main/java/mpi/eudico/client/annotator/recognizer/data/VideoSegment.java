package mpi.eudico.client.annotator.recognizer.data;

import java.awt.Shape;

//import java.util.List;
/**
 * Segments in the video domain (probably) need more attributes
 * to define an area or trajectory of interest.
 * 
 * Currently one shape is supported per segment
 * 
 * @author Han Sloetjes 
 */
public class VideoSegment extends Segment {
	//public List shapes;// need a List??
	public Shape shape;
	
	/**
	 * No-arg constructor.
	 */
	public VideoSegment() {
		super();
	}

	/**
	 * Constructor with begin time, end time and label arguments.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 */
	public VideoSegment(long beginTime, long endTime, String label) {
		super(beginTime, endTime, label);
	}

	/**
	 * Constructor with begin time, end time, label and shape arguments.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 * @param shape the region of interest
	 */
	public VideoSegment(long beginTime, long endTime, String label, Shape shape) {
		super(beginTime, endTime, label);
		this.shape = shape;
	}	
}
