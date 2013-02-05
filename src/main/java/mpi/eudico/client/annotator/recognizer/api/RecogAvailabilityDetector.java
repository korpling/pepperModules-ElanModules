package mpi.eudico.client.annotator.recognizer.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xml.sax.SAXException;
import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.load.RecognizerLoader;
import mpi.eudico.client.annotator.recognizer.load.RecognizerBundle;
import mpi.eudico.client.annotator.recognizer.load.RecognizerParser;
import mpi.eudico.client.annotator.recognizer.silence.SilenceRecognizer;
import mpi.eudico.client.annotator.util.AvailabilityDetector;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * Class which creates a template map for all 
 * the bundles created for the available audio
 * and video recognizer
 * 
 * @version Sep 2012
 * @author updated by aarsom
 *
 */
public class RecogAvailabilityDetector {
	/** a template map for audio recognizers */
	private static Map<String, RecognizerBundle> audioRecognizerBundles = new HashMap<String, RecognizerBundle>(6);
	/** a template map for video recognizers */
	private static Map<String, RecognizerBundle> videoRecognizerBundles = new HashMap<String, RecognizerBundle>(6);
	private static RecognizerLoader recognizerLoader;	
	
	/**
	 * Private constructor
	 */
	private RecogAvailabilityDetector() {
		super();
	}

	/** Return the list of available audio recognizers	 
	 * 
	 * @return an ArrayList with the available audio recognizers
	 */
	public static HashMap<String, Recognizer> getAudioRecognizers() {
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		
		HashMap<String, Recognizer> audioRecs = new HashMap<String, Recognizer>(6);
		SilenceRecognizer sr = new SilenceRecognizer();
		audioRecs.put(sr.getName(), sr);
//		DemoRecognizer demoRec = new DemoRecognizer();
//		audioRecs.put(demoRec.getName(), demoRec);
		
		Iterator<String> keyIt = audioRecognizerBundles.keySet().iterator();
		String key;
		RecognizerBundle bundle;
		Recognizer rec;
		while (keyIt.hasNext()) {
			key = keyIt.next();
			bundle = audioRecognizerBundles.get(key);
			
			if (bundle.getRecExecutionType().equals("local")) {
				LocalRecognizer localRecognizer = new LocalRecognizer(bundle.getRecognizerClass());
				localRecognizer.setParamList(bundle.getParamList());
				localRecognizer.setName(bundle.getName());
				localRecognizer.setRecognizerType(Recognizer.AUDIO_TYPE);
				localRecognizer.setBaseDir(bundle.getBaseDir());
				if (videoRecognizerBundles.containsKey(key)) {
					localRecognizer.setRecognizerType(Recognizer.MIXED_TYPE);
				}				
				audioRecs.put(key, localRecognizer);
			} else if (bundle.getRecExecutionType().equals("shared")) {
				SharedRecognizer sharedRecognizer = new SharedRecognizer(bundle.getRecognizerClass());
				sharedRecognizer.setParamList(bundle.getParamList());// returns a copy of the list
				sharedRecognizer.setName(bundle.getName());
				sharedRecognizer.setRecognizerType(Recognizer.VIDEO_TYPE);
				sharedRecognizer.setBaseDir(bundle.getBaseDir());
				if (videoRecognizerBundles.containsKey(key)) {
					sharedRecognizer.setRecognizerType(Recognizer.MIXED_TYPE);
				}
				audioRecs.put(key, sharedRecognizer);
			} else if (bundle.getJavaLibs() != null) {// assume "direct" ?
				
				if (recognizerLoader == null) {
					recognizerLoader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				} else {
					recognizerLoader.addLibs(bundle.getJavaLibs());
					recognizerLoader.addNativeLibs(bundle.getNativeLibs());
				}
				//RecognizerLoader loader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				
				try {
					//loader.loadNativeLibs();
					//rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, loader).newInstance();
					rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, recognizerLoader).newInstance();
					rec.setName(bundle.getName());
					audioRecs.put(key, rec);
				} catch (ClassNotFoundException cnfe) {
					ClientLogger.LOG.severe("Cannot load the recognizer class: " + bundle.getRecognizerClass() + " - Class not found");
				} catch (InstantiationException ie) {
					ClientLogger.LOG.severe("Cannot instantiate the recognizer class: " + bundle.getRecognizerClass());
				} catch (IllegalAccessException iae) {
					ClientLogger.LOG.severe("Cannot access the recognizer class: " + bundle.getRecognizerClass());
				} catch (Exception ex) {// any other exception
					ClientLogger.LOG.severe("Cannot load the recognizer: " + bundle.getRecognizerClass() + " - " + ex.getMessage());
				}
			} else {
				ClientLogger.LOG.severe("Cannot load the recognizer: no Java library has been found: " + bundle.getName());
			}
		}
		
		return audioRecs;
		

	}
	
	/**
	 * Maybe add parameters for needed capabilities like file formats
	 * 
	 * @return an ArrayList with the available video recognizers
	 */
	public static HashMap<String, Recognizer> getVideoRecognizers() {
		
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		
		HashMap<String, Recognizer> videoRecs = new HashMap<String, Recognizer>(6);
		//VideoTestRecognizer vtr = new VideoTestRecognizer();
		//videoRecs.put(vtr.getName(), vtr);
//		DemoRecognizer demoRec = new DemoRecognizer();
//		videoRecs.put(demoRec.getName(), demoRec);
		
		Iterator<String> keyIt = videoRecognizerBundles.keySet().iterator();
		String key;
		RecognizerBundle bundle;
		Recognizer rec;
		while (keyIt.hasNext()) {
			key = keyIt.next();
			bundle = videoRecognizerBundles.get(key);
			
			if (bundle.getRecExecutionType().equals("local")) {
				LocalRecognizer localRecognizer = new LocalRecognizer(bundle.getRecognizerClass());
				localRecognizer.setParamList(bundle.getParamList());// returns a copy of the list
				localRecognizer.setName(bundle.getName());
				localRecognizer.setRecognizerType(Recognizer.VIDEO_TYPE);
				localRecognizer.setBaseDir(bundle.getBaseDir());
				if (audioRecognizerBundles.containsKey(key)) {
					localRecognizer.setRecognizerType(Recognizer.MIXED_TYPE);
				}
				videoRecs.put(key, localRecognizer);
			} else if (bundle.getRecExecutionType().equals("shared")) {
				SharedRecognizer sharedRecognizer = new SharedRecognizer(bundle.getRecognizerClass());
				sharedRecognizer.setParamList(bundle.getParamList());// returns a copy of the list
				sharedRecognizer.setName(bundle.getName());
				sharedRecognizer.setRecognizerType(Recognizer.VIDEO_TYPE);
				sharedRecognizer.setBaseDir(bundle.getBaseDir());
				if (audioRecognizerBundles.containsKey(key)) {
					sharedRecognizer.setRecognizerType(Recognizer.MIXED_TYPE);
				}
				videoRecs.put(key, sharedRecognizer);
			} else if (bundle.getJavaLibs() != null) {// "direct"
			
				if (recognizerLoader == null) {
					recognizerLoader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				} else {
					recognizerLoader.addLibs(bundle.getJavaLibs());
					recognizerLoader.addNativeLibs(bundle.getNativeLibs());
				}
				//RecognizerLoader loader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				
				try {
					//loader.loadNativeLibs();
					//rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, loader).newInstance();
					rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, recognizerLoader).newInstance();
					rec.setName(bundle.getName());
					videoRecs.put(key, rec);
				} catch (ClassNotFoundException cnfe) {
					ClientLogger.LOG.severe("Cannot load the recognizer class: " + bundle.getRecognizerClass() + " - Class not found");
				} catch (InstantiationException ie) {
					ClientLogger.LOG.severe("Cannot instantiate the recognizer class: " + bundle.getRecognizerClass());
				} catch (IllegalAccessException iae) {
					ClientLogger.LOG.severe("Cannot access the recognizer class: " + bundle.getRecognizerClass());
				} catch (Exception ex) {// any other exception
					ClientLogger.LOG.severe("Cannot load the recognizer: " + bundle.getRecognizerClass() + " - " + ex.getMessage());
				}
			} else {
				ClientLogger.LOG.severe("Cannot load the recognizer: no Java library has been found ");
			}
		}
		
		return videoRecs;

	}
	
	/**
	 * Returns the parameter list of a recognizer.
	 * 
	 * @param recognizerName the name of the recognizer
	 * 
	 * @return a List containing the parameters
	 */
	public static List<Param> getParamList(String recognizerName) {
		if (recognizerName != null) {
			RecognizerBundle bundle = audioRecognizerBundles.get(recognizerName);
			List<Param> params = null;
			if (bundle != null) {
				params = bundle.getParamList();
			} else {
				bundle = videoRecognizerBundles.get(recognizerName);
				if (bundle != null) {
					params = bundle.getParamList();
				}
			}
			
			if (params != null) {
				List<Param> copyList = new ArrayList<Param>(params.size());
				for (Param p : params) {
					if (p == null) {
						continue;
					}
					try {
						copyList.add((Param) p.clone());
					} catch (CloneNotSupportedException cnse) {
						ClientLogger.LOG.warning("Cannot clone a parameter: " + p.id);
					}
				}
				
				return copyList;
			}
		}
		
		return null;
	}                                                                                                                                                                                          
	
	/**
	 * Returns the help file of a recognizer.
	 * 
	 * @param recognizerName the name of the recognizer
	 * 
	 * @return a string specifying the filename, can be null
	 */
	public static String getHelpFile(String recognizerName){
		if (recognizerName != null) {
			RecognizerBundle bundle = audioRecognizerBundles.get(recognizerName);
			String helpFile = null;
			if (bundle != null) {
				helpFile = bundle.getHelpFile();
			} else {
				bundle = videoRecognizerBundles.get(recognizerName);
				if (bundle != null) {
					helpFile = bundle.getHelpFile();
				}
			}			
			return helpFile;
		}		
		return null;
	}
	
	/**
	 * Creates a parser, a classloader, instantiates the recognizer, creates a bundle 
	 * and adds the bundle to the proper recognizer map(s).
	 * 
	 * @param mdStream the stream representing the "recognizer.cmdi" from a directory or from a jar
	 * @param libs the recognizer's Java libraries
	 * @param natLibs the recognizer's native libraries
	 * @param baseDir the directory the recognizer runs from
	 */
	public static void createBundle(InputStream mdStream, URL[] libs, URL[] natLibs, File baseDir) {
		boolean isDetector = false;
		String binaryName = null;
		RecognizerBundle bundle = null;
		RecognizerParser parser = null;
		
		try {
			parser = new RecognizerParser(mdStream);
			parser.parse();
			if (parser.recognizerType == null || 
					(!parser.recognizerType.equals("direct") && !parser.recognizerType.equals("local") && !parser.recognizerType.equals("shared"))) {
				ClientLogger.LOG.warning("Unsupported recognizer type, should be 'direct', 'local' or 'shared': " + parser.recognizerType);
				return;
			}
			if (!parser.curOsSupported) {
				ClientLogger.LOG.warning("Recognizer does not support this Operating System: " + parser.recognizerName);
				return;
			}
			if (parser.implementor != null) {
				isDetector = true;
				binaryName = parser.implementor;
			} else {
				ClientLogger.LOG.warning("The implementing class name has not been specified.");
				return;
			}
			
		} catch (SAXException sax) {
			ClientLogger.LOG.severe("Cannot parse metadata file: " + sax.getMessage());
		}
		
		if (isDetector) {
			boolean audio = false;
			boolean video = false;
			
			if (parser.paramList != null) {
				for (Param par : parser.paramList) {
					if (par instanceof FileParam) {
						if (((FileParam) par).ioType == FileParam.IN) {
							if (((FileParam) par).contentType == FileParam.AUDIO) {
								audio = true;
							} else if (((FileParam) par).contentType == FileParam.VIDEO) {
								video = true;
							}
						}
					}
				}
			} // else exception?
			
			if (parser.recognizerType.equals("direct")) {
				if (libs == null) {
					return;
				}
				// create a classloader, bundle			
				RecognizerLoader loader = new RecognizerLoader(libs, natLibs);
	
				if (binaryName != null) {
					try {
						Class<?> c = loader.loadClass(binaryName);
						
						// if the above works, assume everything is all right
						bundle = new RecognizerBundle();
						bundle.setId(parser.recognizerName);
						bundle.setName(parser.description);// friendly name
						bundle.setParamList(parser.paramList);						
						bundle.setRecognizerClass(binaryName);
						bundle.setRecExecutionType(parser.recognizerType);
						bundle.setJavaLibs(libs);
						bundle.setNativeLibs(natLibs);
						bundle.setBaseDir(baseDir);
						bundle.setHelpFile(parser.helpFile);
							
						if (audio) {
							audioRecognizerBundles.put(bundle.getName(), bundle);
						}
						if (video) {
							videoRecognizerBundles.put(bundle.getName(), bundle);
						}
						
	//					Recognizer rec = (Recognizer) c.newInstance();
	//					bundle.setRecognizer(rec);
	//					if (bundle.getName() == null) {
	//						bundle.setName(rec.getName());
	//					} else {
	//						if (rec.getName() != null || !rec.getName().equals(bundle.getName())){
	//							bundle.setName(rec.getName());
	//						}
	//					}
	//					if (rec.getRecognizerType() == Recognizer.AUDIO_TYPE) {
	//						audioRecognizerBundles.put(rec.getName(), bundle);
	//					} else if (rec.getRecognizerType() == Recognizer.VIDEO_TYPE) {
	//						videoRecognizerBundles.put(rec.getName(), bundle);
	//					} else if (rec.getRecognizerType() == Recognizer.MIXED_TYPE) {
	//						audioRecognizerBundles.put(rec.getName(), bundle);
	//						videoRecognizerBundles.put(rec.getName(), bundle);
	//					}
					} catch (ClassNotFoundException cne) {
						ClientLogger.LOG.severe("Cannot load the recognizer class: " + binaryName + " - Class not found");
					} 
	//				catch (InstantiationException ie) {
	//					ClientLogger.LOG.severe("Cannot instantiate the recognizer class: " + binaryName);
	//				} catch (IllegalAccessException iae) {
	//					ClientLogger.LOG.severe("Cannot access the recognizer class: " + binaryName);
	//				}
				}// else throw exception?
				else {
					ClientLogger.LOG.warning("Cannot load the recognizer class: Class not found");
				}
			} else if (parser.recognizerType.equals("local") || parser.recognizerType.equals("shared")) {
				
				bundle = new RecognizerBundle();
				bundle.setId(parser.recognizerName);
				bundle.setName(parser.description);// friendly name
				bundle.setParamList(parser.paramList);				
				bundle.setRecognizerClass(binaryName);// reuse the class name for the run command
				bundle.setRecExecutionType(parser.recognizerType);
				bundle.setBaseDir(baseDir);
				bundle.setHelpFile(parser.helpFile);
				
				if (audio) {
					audioRecognizerBundles.put(bundle.getName(), bundle);
				}
				if (video) {
					videoRecognizerBundles.put(bundle.getName(), bundle);
				}
			}
		}
	}
}

