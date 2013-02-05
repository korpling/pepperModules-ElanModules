package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * A CommandAction for creating a clip from media files by means of calling a script.
 */
public class ClipMediaCA extends CommandAction implements SelectionListener {
	private File scriptFile;
	// the executable part of the script
	private String executable;
	// the parameter part of the script
	private String paramLine;
	private final String scriptFileName = "clip-media.txt";
	private long lastModified = 0L;
	
	private String outFilePath = null;
	
	/**
	  * Creates a new ClipMediaCA instance
	  *
	  * @param viewerManager vm
	  */
	public ClipMediaCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.CLIP_MEDIA);

		try {
			scriptFile = getScriptFile();
			if (scriptFile != null) {
				//lastModified = scriptFile.lastModified();
				ClientLogger.LOG.info("Found clipping script: " + scriptFile.getName());
			} else {
				ClientLogger.LOG.info("No clipping script found!");
			}
			
		} catch (Exception e) {
			ClientLogger.LOG.info("No clipping script found");
		}	 
		 	 
	     viewerManager.connectListener(this);
	 }
	 
	 /**
	  * Creates a new ClipMedia command
	  */
	 protected void newCommand() {
		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
				 ELANCommandFactory.CLIP_MEDIA);
	 }
	 
	 /**
	  * There's no logical receiver for this CommandAction.
	  *
	  * @return null
	  */
	 public void setPath(String outFilePath) {
		 this.outFilePath = outFilePath;
	 }

	 /**
	  * There's no logical receiver for this CommandAction.
	  *
	  * @return null
	  */
	 protected Object getReceiver() {
		 return null;
	 }

	 /**
	  * Returns an array of arguments.
	  *
	  * @return the arguments
	  */
	 protected Object[] getArguments() {
		 if (scriptFile == null) {
			 return new Object[] { vm, new Exception(ElanLocale.getString("ExportClipDialog.Message.NoScript") + 
					 "\n" + ElanLocale.getString("ExportClipDialog.Message.LookingFor") +
					 "\n" + (System.getProperty("user.dir") + File.separator + scriptFileName) +
					 "\n" + (Constants.ELAN_DATA_DIR + File.separator + scriptFileName)) };
		 } else if (executable == null) {
			 return new Object[] { vm, new Exception(ElanLocale.getString("ExportClipDialog.Message.InvalidScript")) };
		 }
		 
		 if(outFilePath != null){
			 return new Object[] { vm, executable, paramLine, outFilePath };
		 }else{
			 return new Object[] { vm, executable, paramLine };
		 }
			 
		 
	 }

	 // only activate menu item when selection is made
	 // if the user may define a time interval in a dialog this is obsolete
	 public void updateSelection() {
 		if (vm.getSelection().getEndTime() > vm.getSelection().getBeginTime()) {
 			// could check the media players and/or the media descriptors
 			setEnabled(true);
 		} else {
 			setEnabled(false);
 		}
	 }
	 
	 /**
	  * Checks whether the clipping script file can be found in any known place.
	  * @version 04-2012 changed the order of file discovery; first in the 
	  * ELAN data dir in the user's home, then in user.dir (ELAN's install dir)
	  * @return the File object of the script
	  */
	 private File getScriptFile() {
			// check if there is script file in the user dir or in the ELAN data dir
			// do this in the constructor or every time an attempt is made to call the script 
//			File f = new File(System.getProperty("user.dir") + File.separator + scriptFileName);
		 	File f = new File(Constants.ELAN_DATA_DIR + File.separator + scriptFileName);
			if (f.exists() && f.isFile() && f.canRead()) {
				return f;
			} else {
				// alternative location
				f = new File(System.getProperty("user.dir") + File.separator + scriptFileName);
//				f = new File(Constants.ELAN_DATA_DIR + File.separator + scriptFileName);
				
				if (f.exists() && f.isFile() && f.canRead()) {
					return f;
				}
			}
			
		 return null;
	 }
	 
	 /**
	  * Tries to discover and parse the actual script line/command line.
	  * Ignores all lines starting with a # sign
	  *
	  */
	 private void parseScriptLine() {
		 if (scriptFile != null) {
			 // read the file, extract the command
			try {
				FileReader fileRead = new FileReader(scriptFile);
				BufferedReader bufRead = new BufferedReader(fileRead);
				String line = null;
				
				while ((line = bufRead.readLine()) != null) {
					if (line.length() == 0) {
						continue;
					}
					if (line.startsWith("#")) {
						continue;
					}
					// first real line is parsed
					 line = line.trim();
					 if (line.startsWith("\"")) {// check if the executable is within double quotes
						 int index = line.indexOf("\"", 1);
						 if (index > -1) {
							 executable = line.substring(1, index);
							 if (index < line.length() - 2) {
								 paramLine = line.substring(index + 2);
							 }
						 } else {// no closing double quote
							 executable = null;
						 }
					 } else {
						 int index = line.indexOf(' ');
						 if (index > 0) {
							 executable = line.substring(0, index);
							 if (index < line.length() - 1) {
								 paramLine = line.substring(index + 1);
							 }
						 }
					 }
				}
			} catch (FileNotFoundException fnfe) {
				ClientLogger.LOG.warning("The script file can not be found");
				executable = null;
				paramLine = null;
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Error while reading the script file " + ioe.getMessage());
				executable = null;
				paramLine = null;
			}
		 }
		 
		 return;
	 }

	/**
	 * Checks if a script has already been found and if so checks the last modified value.
	 * If needed the script is read again.
	 * 
	 * @see mpi.eudico.client.annotator.commands.CommandAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// (re) check if there is a script file and check the last modified field
		if (scriptFile == null) {
			scriptFile = getScriptFile();
			if (scriptFile != null) {
				lastModified = scriptFile.lastModified();
				parseScriptLine();
			}
		} else {
			long lm = scriptFile.lastModified();
			if (lm > lastModified) {
				parseScriptLine();
				lastModified = lm;
			}
		}
		super.actionPerformed(event);
	}
	 
	 
}
