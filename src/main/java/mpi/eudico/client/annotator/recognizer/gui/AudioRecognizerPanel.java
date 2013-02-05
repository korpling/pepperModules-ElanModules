package mpi.eudico.client.annotator.recognizer.gui;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import mpi.eudico.client.annotator.recognizer.api.RecogAvailabilityDetector;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.data.BoundarySegmentation;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

import mpi.eudico.client.annotator.ViewerManager2;


/** AudioRecognizerPanel
 * 
 * @updated by aarsom
 * @version Sep 2012
 *
 */
public class AudioRecognizerPanel extends AbstractRecognizerPanel  {
	
	/**
	 * Calls the super constructor and check the "visible audio" file, updates the selection panel if needed
	 */
	public AudioRecognizerPanel(ViewerManager2 viewerManager, ArrayList<String> audioFilePaths) {
		super(viewerManager, audioFilePaths);	
	}
	
	@Override
	protected void initComponents() {
		mode = Recognizer.AUDIO_TYPE;
		super.initComponents();
	}	
	
	@Override
	protected HashMap<String, Recognizer> getAvailableRecognizers() {
		return RecogAvailabilityDetector.getAudioRecognizers();
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source.equals(startStopButton)) {
			if (currentRecognizer != null && !isRunning) {				
				if (viewerManager.getSignalViewer() != null) {
					viewerManager.getSignalViewer().setSegmentationChannel1(null);
					viewerManager.getSignalViewer().setSegmentationChannel2(null);
				}
			}
		}		
		super.actionPerformed(e);
	}
	

	/**
	 * @return the audioFilePath
	 */
	public ArrayList<String> getAudioFilePaths() {
		return mediaFilePaths;
	}
	
	/**
	 * Sets the new audio file paths
	 * 
	 * @param audioFilePath the audioFilePath to set
	 */
	public void setAudioFilePaths(ArrayList<String> audioFilePaths) {				
		mediaFilePaths.clear();
		for(int i= 0; i < audioFilePaths.size(); i++){
			String path = audioFilePaths.get(i);
			if (path.startsWith("file:")) {
    			path = path.substring(5);
    		}	
			if(!mediaFilePaths.contains(path)){
				mediaFilePaths.add(path);
			}
		}		
		segmentations = new HashMap();
		
		// update the files list
		updateMediaFiles();
	}
	
	// RecognizerHost interface implementation	//	
	/**
	 * Add a segmentation to the 
	 */
	public void addSegmentation(Segmentation segmentation) {
		super.addSegmentation(segmentation);
		
		// make sure the SignalViewer knows about the new segmentation
		if (viewerManager.getSignalViewer() != null) {
			int channel = 0;
			ArrayList<MediaDescriptor> mediaDescriptors = segmentation.getMediaDescriptors();
			for (int i = 0; i < mediaDescriptors.size(); i++) {
				MediaDescriptor descriptor = (MediaDescriptor) mediaDescriptors.get(i);
				// CHECK IF THE MEDIA FILE IS THE VISIBLE ONE!!! ???
				channel += descriptor.channel;
			}
			if (channel == 0 && mediaDescriptors.size() <= 1) {
				viewerManager.getSignalViewer().setSegmentation(new BoundarySegmentation(segmentation));
			} else
			if (channel == 1) {
				if (notMono) {
					viewerManager.getSignalViewer().setSegmentationChannel1(new BoundarySegmentation(segmentation));
				} else {
					viewerManager.getSignalViewer().setSegmentation(new BoundarySegmentation(segmentation));
				}
			} else if (channel == 2) {
				viewerManager.getSignalViewer().setSegmentationChannel2(new BoundarySegmentation(segmentation));
			} else if (channel == 3) { // something for combined channel result?
				
			}
		}
	}
}
