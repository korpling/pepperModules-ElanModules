package mpi.eudico.client.annotator.recognizer.api;

import mpi.eudico.client.annotator.recognizer.data.*;
import mpi.eudico.client.annotator.recognizer.gui.TierSelectionPanel;

import java.util.ArrayList;

public interface RecognizerHost {
	/**
	 * Called by a Recognizer to send a Segmentation object to the RecognizerHost
	 * More than one Segmentation can be sent.
	 * 
	 * @param segmentation the Segmentation produced by the Recognizer.
	 */
	public void addSegmentation(Segmentation segmentation);
	
	/**
	 * Periodically called by a Recognizer to inform the RecognizerHost 
	 * about the progress of the recognition task.
	 * 
	 * @param progress a float between 0.0 and 1.0 where 1.0 means that the recognizer has finished processing.
	 */
	public void setProgress(float progress);
	
	/**
	 * Periodically called by a Recognizer to inform the RecognizerHost 
	 * about the progress of the recognition task.
	 * 
	 * @param progress a float between 0.0 and 1.0 where 1.0 means that the recognizer has finished processing.
	 * @param message a progress message
	 */
	public void setProgress(float progress, String message);
	
	/**
	 * Can be called by a recognizer to signal that a fatal error occurred and the recognition task stopped
	 * 
	 * @param message a description of the error
	 */
	public void errorOccurred(String message);
	
	/**
	 * Returns a tier selection panel mainly used by the
	 * java plug-in recognizers
	 * 
	 * @param param name for which the tierSelectionpanel is used
	 * 	@return
	 */
	public TierSelectionPanel getSelectionPanel(String paramName);
	
	/**
	 * Called by ELAN to get the recognition result.
	 * 
	 * @return an ArrayList with Segmentation objects.
	 */
	public ArrayList<Segmentation> getSegmentations();
	
	/**
	 * Tells ELAN if there is one or more recognizer busy
	 * 
	 * @return true if there is some recognizing going on
	 */
	public boolean isBusy();
}