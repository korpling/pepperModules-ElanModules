package mpi.eudico.client.annotator.recognizer.api;

import javax.swing.JPanel;

import java.util.List;
import java.util.Locale;

/**
 * Interface to be implemented by (audio) Recognizers that want to be hosted by ELAN
 * 
 * @author albertr
 * @version 2 Jan 2010: some changes in the interface, new methods added: 
 *     getExamplesSupport (optional, required or not supported), 
 *     getParameters (in case no control panel is provided),
 *     setParameterValues (called by host before starting the detector),
 *     getReport (returns an overview of results, or a collection of messages, errors)
 */
public interface Recognizer {
//	public static final int EXAMPLE_SEGMENTS_OPTIONAL = 0;
//	public static final int EXAMPLE_SEGMENTS_REQUIRED = 1;
//	public static final int EXAMPLE_SEGMENTS_NOT_SUPPORTED = 2;
	
	public static final int AUDIO_TYPE = 0;
	public static final int VIDEO_TYPE = 1;
	public static final int MIXED_TYPE = 2;// combined audio and video recognizer
	
/**
	 * Updates the list of supported media files to the recognizer(mainly used
	 * to update the media files list in the java plug-in recognizers)	 * 
	 
	 * @param mediaFilePaths an List with full paths to the media files the recognizer must handle.
	 * @return boolean - value is ignored
	 */
	public boolean setMedia(List<String> mediaFilePaths);
	
	/**
	 * Called by a host to determine whether a certain media file can be handled by the recognizer.
	 * The test should be as light-weight as possible.
	 * 
	 * @param mediaFilePath the media path
	 * @return true if the recognizer can process the file
	 */
	public boolean canHandleMedia(String mediaFilePath);
	
	/**
	 * Returns a flag whether a recognizer is capable of combined processing of multiple streams.
	 * 
	 * @return true if the recognizer is able to combine multiple sources for the recognition task
	 */
	public boolean canCombineMultipleFiles();
	
	/**
	 * Gives a name by which the recognizer can be chosen by a user among other recognizers.
	 * 
	 * @return the name for the recognizer
	 */
	public String getName();
	
	/**
	 * Sets the name by which the recognizer can be chosen by a user among other recognizers.
	 * Is used to pass the name from the CMDI metadata file to the recognizer.
	 * More information on the CMDI based interface specification can be found at <br>
	 * <a href="http://www.mpi.nl/research/research-projects/language-archiving-technology/avatech/">
	 * <code>http://www.mpi.nl/research/research-projects/language-archiving-technology/avatech/</code></a>
	 * 
	 * @param name the name for the recognizer
	 */
	public void setName(String name);
	
	/**
	 * Indicates the type of data the recognizer can work with.
	 * Returns one of the constants {@link #AUDIO_TYPE}, {@link #VIDEO_TYPE} or {@link #MIXED_TYPE}.
	 * 
	 * @return one of the constants {@link #AUDIO_TYPE}, {@link #VIDEO_TYPE} or {@link #MIXED_TYPE}
	 */
	public int getRecognizerType();
	
	/**
	 * Will be called by software that controls the recognizer to give it a call back handle
	 * The RecognizerHost Object has methods that allow the recognizer to communicate with
	 * ELAN about selections an segmentations.
	 * 
	 * @param host the recognizerHost Object the Recognizer can communicate with
	 */
	public void setRecognizerHost(RecognizerHost host);
	
	/**
	 * This method can return a control panel for the recognizers parameters.
	 * 
	 * @return JPanel with the recognizers control elements on it or null if such a Panel does not exist
	 */
	public JPanel getControlPanel();
	
//	/**
//	 * Called to check the level of support for example segments. Should return one of the constants:
//	 * <code>EXAMPLE_SEGMENTS_OPTIONAL</code>, the recognizer will use example segments if available
//	 * but will have defaults if not, <code>EXAMPLE_SEGMENTS_REQUIRED</code>, if the recognizer will 
//	 * not work without example segments, <code>EXAMPLE_SEGMENTS_NOT_SUPPORTED</code>, if the recognizer 
//	 * will never use example segments.
//	 *  
//	 * @return one of the constants {@link #EXAMPLE_SEGMENTS_OPTIONAL}, {@link #EXAMPLE_SEGMENTS_REQUIRED} or 
//	 * {@link #EXAMPLE_SEGMENTS_NOT_SUPPORTED}
//	 */
//	public int getExamplesSupport();
	
	/**
	 * Can return an overview of processing messages, results etc.
	 * 
	 * @return an overview of results, processing messages etc. 
	 */
	public String getReport();
	
	/**
	 * Sets the value of a textual parameter, as defined in configuration file 
	 * 
	 * @param param the name of the parameter
	 * @param value the value for the parameter
	 */
	public void setParameterValue(String param, String value);
	
	/**
	 * Sets the value of a numerical parameter, as defined in configuration file
	 * 
	 * @param param the name of the parameter
	 * @param value the value for the parameter
	 */
	public void setParameterValue(String param, float value);
	
	/**
	 * Returns the current value of the specified parameter.
	 * 
	 * @param param the name of the parameter
	 * @return the value of the parameter
	 */
	public Object getParameterValue(String param);
	
	/**
	 * Can be implemented optionally to support locale changes in the user interface
	 * 
	 * @param locale
	 */
	public void updateLocale(Locale locale);
	
	/**
	 * When this method is called the recognizer should start executing.
	 * This method runs in a separate Thread that is controlled by ELAN.
	 * 
	 * Typically the recognizer then:
	 * 1. Asks the RecognizerHost for the example selections
	 * 2. Starts processing the data to produce segments
	 *    and gives progress information to the RecognizerHost
	 * 3. When processing is ready it sends the Segmentation(s) to the RecognizerHost
	 *
	 */
	public void start();
	
	/**
	 * When this method is called the recognizer should stop executing.
	 * It is advisable to give the segmentations made up to that moment
	 * to the RecognizerHost. 
	 * Cached intermediate results can be kept in memory for successive runs.
	 */
	public void stop();
	
	/**
	 * Notifies this recognizer that it has been unloaded and that any in-memory 
	 * resources can be released.
	 */
	public void dispose();
	
	/**
	 * Method used to check the whether all  parameters that are required
	 * by the recognizer to start its process are set.
	 * 
	 * If the required parameters are not filled, then throw a 
	 * RecognizerConfigurationException with the message indicating the 
	 * reason for not starting the recognizer
	 * 
	 * @throws RecognizerConfigurationException
	 */
	public void validateParameters() throws RecognizerConfigurationException;
}