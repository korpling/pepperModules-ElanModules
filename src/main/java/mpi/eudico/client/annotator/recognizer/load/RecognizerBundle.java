package mpi.eudico.client.annotator.recognizer.load;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.util.FileUtility;

/**
 * A class that collects information and resources concerning recognizers
 * that have been detected in the extensions folder.
 * 
 * @author Han Sloetjes
 */
public class RecognizerBundle {
	private String id;
	/** a friendly name */
	private String name;
	/** the loader for this recognizer */ //?? needed?
	private ClassLoader loader;
	private String recognizerClassName;// binary name or fully qualified name
	private String recExecutionType; // direct, local etc.
	
	private Recognizer recognizer;
	private List<Param> paramList;
	private String helpFile;
	private URL[] javaLibs;
	private URL[] nativeLibs;
	private File baseDir;
	
	/**
	 * No-arg constructor.
	 */
	public RecognizerBundle() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name the name of the recognizer
	 */
	public RecognizerBundle(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public Recognizer getRecognizer() {
		return recognizer;
	}

	public void setRecognizer(Recognizer recognizer) {
		this.recognizer = recognizer;
	}

	public List<Param> getParamList() {
		if (paramList == null) {
			return null;
		}
		//make a copy of the list
		ArrayList<Param> params = new ArrayList<Param>(paramList.size());
		for (Param p : paramList) {
			try {
			params.add((Param) p.clone());
			} catch (CloneNotSupportedException cnse) {
				
			}
		}
		
		return params;
	} 
	
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	
	public void setHelpFile(String file) {		
		this.helpFile = file;		
	}
	
	public String getHelpFile() {
		if(helpFile == null || helpFile.trim().length() <= 0){
			return null;
		}		
		try {
			java.net.URL url = new URL(helpFile);
		} catch (MalformedURLException mue) {
			if(helpFile.startsWith(".")){				
				helpFile = FileUtility.pathToURLString(baseDir.getAbsolutePath() + helpFile.substring(1));
			}else{
				helpFile = FileUtility.pathToURLString(baseDir.getAbsolutePath() + File.separator + helpFile);
			}			
		}		
		return helpFile;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRecognizerClass() {
		return recognizerClassName;
	}

	public void setRecognizerClass(String recognizerClassName) {
		this.recognizerClassName = recognizerClassName;
	}

	public URL[] getJavaLibs() {
		return javaLibs;
	}

	public void setJavaLibs(URL[] javaLibs) {
		this.javaLibs = javaLibs;
	}

	public URL[] getNativeLibs() {
		return nativeLibs;
	}

	public void setNativeLibs(URL[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}

	public String getRecExecutionType() {
		return recExecutionType;
	}

	public void setRecExecutionType(String recExecutionType) {
		this.recExecutionType = recExecutionType;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	
}
