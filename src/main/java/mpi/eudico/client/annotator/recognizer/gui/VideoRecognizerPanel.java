package mpi.eudico.client.annotator.recognizer.gui;

import java.util.ArrayList;
import java.util.HashMap;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.recognizer.api.RecogAvailabilityDetector;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;

public class VideoRecognizerPanel extends AbstractRecognizerPanel {

	public VideoRecognizerPanel(ViewerManager2 viewerManager,
			ArrayList<String> mediaFilePaths) {
		super(viewerManager, mediaFilePaths);
	}

	/**
	 * Sets the mode to video and calls the super implementation of initComponents.
	 */
	@Override
	protected void initComponents() {
		mode = Recognizer.VIDEO_TYPE;
		super.initComponents();
	}
	
	/** Sets the new video file paths
	 * 
	 * @param videoFilePath the videoFilePath to set
	 */
	public void setVideoFilePaths(ArrayList<String> videoFilePaths) {
		mediaFilePaths.clear();
		for(int i= 0; i < videoFilePaths.size(); i++){
			String path = videoFilePaths.get(i);
			if (path.startsWith("file:")) {
    			path = path.substring(5);
    		}	
			if(!mediaFilePaths.contains(path)){
				mediaFilePaths.add(path);
			}
		}		
		// update the files list
		updateMediaFiles();
	}

	@Override
	protected HashMap<String, Recognizer> getAvailableRecognizers() {
		return RecogAvailabilityDetector.getVideoRecognizers();
	}
	
}
