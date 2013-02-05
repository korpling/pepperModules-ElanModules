package mpi.eudico.client.annotator.recognizer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.data.VideoSegment;

public class VideoTestRecognizer implements Recognizer {
	private RecognizerHost host;
	private StringBuilder report;
	private boolean isRunning = false;
	private Segmentation results;
	private String mediaPath;
	
	/**
	 * 
	 */
	public VideoTestRecognizer() {
		super();
	}

	public boolean canCombineMultipleFiles() {
		return false;
	}

	public boolean canHandleMedia(String mediaFilePath) {
		return true;
	}

	public void dispose() {
		System.out.println("Video TEst dispose.");

	}

	public JPanel getControlPanel() {
		return null;
	}

	public String getName() {
		return "Test Video Recognizer";
	}

	public Object getParameterValue(String param) {
		return null;
	}

	public int getRecognizerType() {
		return Recognizer.VIDEO_TYPE;
	}

	public String getReport() {
		if (report != null) {
			return report.toString();
		}
		return null;
	}

	public boolean setMedia(List<String> mediaFilePaths) {
		if (mediaFilePaths != null && mediaFilePaths.size() > 0) {
			mediaPath = mediaFilePaths.get(0);
		} else {
			mediaPath = "";
		}
		return true;
	}

	public void setName(String name) {
		

	}

	public void setParameterValue(String param, String value) {

	}

	public void setParameterValue(String param, float value) {

	}

	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;
	}

	public void start() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		
		if (host != null) {
			report = new StringBuilder();
			
			// unfinished - not in sync 
			//host.getSelections() method is removed. TierSelectionPanel
			// should be added and the selections can be obtained
			// from the tier selectionPanel
			//ArrayList<RSelection> selections = host.getSelections();
			ArrayList<RSelection> selections = null;
			
			if (selections != null && selections.size() > 0) {
				results = new Segmentation("VidSeg", new ArrayList<RSelection>(), mediaPath);
				RSelection sel;
				VideoSegment vs;
				Segment segment;
				
				float prog = 1f / selections.size();
				
				for (int i = 0; i < selections.size(); i++) {
					if (!isRunning) {
						host.addSegmentation(results);// add current segments
						break;
					}
					sel = selections.get(i);
					segment = new Segment(sel.beginTime, sel.endTime, "");
					report.append("Selection " + (i + 1) + ": " + sel.beginTime + " - " + sel.endTime);
					if (sel instanceof VideoSegment) {
						vs = (VideoSegment) sel;
						if (vs.label != null) {
							report. append(", " + vs.label);
							segment.label = vs.label;
						}
						if (vs.shape != null) {
							report.append(", " + vs.shape.toString());
						}
					}
					report.append("\n");
					results.getSegments().add(segment);
					host.setProgress((i + 1) * prog, "Segment " + (i + 1));
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
												
					}
				}
				
				host.addSegmentation(results);
			}
		}
	}

	public void stop() {
		if (isRunning) {
			if (report != null) {
				report.append("Cancelled...");
				
				isRunning = false;
			}
		}
	}

	public void updateLocale(Locale locale) {

	}

	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		// TODO Auto-generated method stub
		
	}

}
