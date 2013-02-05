package mpi.eudico.client.annotator.recognizer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

public class DemoRecognizer implements Recognizer { 
	private static String EVEN_LABEL = "E";
	private static String UNEVEN_LABEL = "U";
	private RecognizerHost host;
	private DemoRecognizerPanel controlPanel;
	private boolean keepRunning;
	private String currentMediaFilePath;
	float duration = 12000f;
	private String name = "Demo Recognizer";
	private StringBuilder reportBuilder;
	
	/**
	 * Lightweight constructor, try to do as little as possible here
	 *
	 */
	public DemoRecognizer() {
		
	}

	/**
	 * Called by RecognizerHost to get a name for this recognizer in the ComboBox with available recognizers
	 * 
	 * @return the name of this recognizer
	 */
	public String getName() {
		// make sure this name is unique among the other recognizers!
		return name;
	}

	/**
	 * Called by RecognizerHost to get a control panel for this recognizers parameters
	 * 
	 * @return a JPanel with the recognizers GUI controls or null if there are no controls
	 */
	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new DemoRecognizerPanel(host.getSelectionPanel(null));
		}
		return controlPanel;
	}
	
	/**
	 * Called by RecognizerHost to set the media files for this recognizer
	 * 
	 * @param mediaFilePaths list of full path of media files 
	 * @return true 
	 */
	public boolean setMedia(List<String> mediaFilePaths) {
		((DemoRecognizerPanel)getControlPanel()).updateMediaFiles(mediaFilePaths);
		return true;
	}
	
	/**
	 * Called by RecognizerHost to give this recognizer an object for callbacks
	 * 
	 * @param host the RecognizerHost that talks with this recognizer
	 */
	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;
	}

		
	/**
	 * Called by RecognizerHost to start the recognizer
	 *
	 */
	public void start() {
		keepRunning = true;
		getReportBuilder().delete(0, getReportBuilder().length());
		getReportBuilder().append("Recognizer: " + name + " starting...\n");
		recog();
	}

	/**
	 * Called by RecognizerHost to stop the recognizer, MUST BE OBEYED AT ALL TIMES
	 *
	 */
	public void stop() {
		keepRunning = false;
		getReportBuilder().append("Recognizer: " + name + " stopped...\n");
	}
	
	/**
	 * Code that implements the actual recognition task
	 * This is only a demo that sets a segment at the interval
	 * as defined by the slider on the DemoRecognizerPanel
	 *
	 */
	protected void recog() {
		long start = -1;
		long end = -1;
		// a real recognizer could get the example selections and process them
		// look for a real example at the ELAN source file
		// mpi.eudico.client.annotator.recognizer.silence.SilenceRecognizer.java	

		currentMediaFilePath = controlPanel.getSelectedMediaFile();
		
		ArrayList<RSelection> selections = controlPanel.getSelections();
		if (selections != null) {
			for (int i = 0; i < selections.size(); i++) {
				RSelection selection = (RSelection) selections.get(i);
				// do something interesting with the selection to inform the 
				// recognition algorithm about the patterns it is supposed to find
				
				// here get the lowest time value and create segments from there?
				if (start == -1) {
					start = selection.beginTime;
				} else if (selection.beginTime < start){
					start = selection.beginTime;
				}
				if (selection.endTime > end) {
					end = selection.endTime;
				}
			}
		}
		
		start = start < 0 ? 0 : start;
		end = end < start + 12000 ? start + 12000 : end;
		getReportBuilder().append("Creating segments in interval: " + start + " - " + end + "\n");
		duration = end - start;
		ArrayList<RSelection> segments = new ArrayList<RSelection>();
		int stepDuration = 1000 * controlPanel.getStepDuration(); // time in milliseconds
		getReportBuilder().append("Segment size in ms.: " + stepDuration + "\n");
		int nSteps = (int) (duration / stepDuration);
		if (nSteps < 0) {
			nSteps = 1;
		}
		float perStep = 1f / nSteps;
		for (int step = 0; step < nSteps; step++) {
			long time = start + step * stepDuration;
			// inform the host about the progress we are making
			host.setProgress(time / duration);
			
			// add a dummy segment
			Segment segment = new Segment();
			segment.beginTime = time;
			segment.endTime = time + stepDuration;
			segment.label = step % 2 == 0 ? EVEN_LABEL : UNEVEN_LABEL;
			segments.add(segment);
			
			// sleep a while to make it look more interesting
			try {
				Thread.currentThread().sleep(50);
			} catch (Exception e) {
			
			}
			host.setProgress(step * perStep);
			if (!keepRunning) {
				break;
			}
		}
		getReportBuilder().append("Number of segments created: " + segments.size() + "\n");
		// if keepRunning is still true make sure the progress is set to 1
		if (keepRunning) {
			host.setProgress(1);
		}
		
		// give the resulting segmentation to the host
		MediaDescriptor descriptor = new MediaDescriptor(currentMediaFilePath, 1);
		Segmentation seg = new Segmentation("DEMO", segments, descriptor);
		host.addSegmentation(seg);
	}
	
	/**
	 * This method is called if the locale changes within ELAN
	 */
	public void updateLocale(Locale locale) {
		// optional to implement, usualy english GUI elements are ok.
	}

	public boolean canCombineMultipleFiles() {
		return false;
	}

	/**
	 * Always returns true, because the media file is not actually used in this demo
	 */
	public boolean canHandleMedia(String mediaFilePath) {
		return true;
	}

	public void dispose() {
		if (reportBuilder != null) {
			reportBuilder.delete(0, reportBuilder.length());
			reportBuilder = null;
		}
		controlPanel = null;
	}

	public Object getParameterValue(String param) {
		// will only be called if the control panel is not an instance of ParamPreferences
		if ("StepDuration".equals(param)) {
			if (controlPanel != null) {
				return new Integer(controlPanel.getStepDuration());
			}
		}
		
		return null;
	}

	/**
	 * Since this demo recognizer doesn't do anything real with the media file
	 * return MIXED, to make it show up in the audio and the video tab.
	 */
	public int getRecognizerType() {
		return Recognizer.MIXED_TYPE;
	}

	public String getReport() {
		if (reportBuilder != null) {
			return reportBuilder.toString();
		}
		
		return null;
	}

	public void setName(String name) {
		this.name = name;
		
	}

	public void setParameterValue(String param, String value) {
		
	}

	public void setParameterValue(String param, float value) {
		if ("StepDuration".equals(param)) {
			if (controlPanel != null) {
				controlPanel.setStepDuration((int) value);
			}
		}
	}
	
	/**
	 * Returns the report builder, first creating it if needed.
	 * 
	 * @return the report builder
	 */
	private StringBuilder getReportBuilder() {
		if (reportBuilder == null) {
			reportBuilder = new StringBuilder();
		}
		
		return reportBuilder;
	}

	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		// TODO Auto-generated method stub		
	}
}
