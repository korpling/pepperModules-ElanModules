package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.cleanUp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import mpi.eudico.client.annotator.commands.MergeTranscriptionsCommand;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class MergeEAFs {
	public static void main(String[] args){
		String path = "/home/tom/otfrit/";
		Collection<String> dirs = getDirNamesInDirectory(path);
		for (String dir : dirs){
			System.out.println("fetching files in "+dir);
			ArrayList<String> fnames = getFileNamesInDirectory(path + dir);
			doMerge(path, dir, fnames);
		}
	}
	
	public static Collection<String> getDirNamesInDirectory(String path){
		File file = new File(path);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		return (Collection<String>) Arrays.asList(directories);
	}
	
	public static ArrayList<String> getFileNamesInDirectory(String path){
		String files;
		ArrayList<String> out = new ArrayList<String>();	
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++){ 
			if (listOfFiles[i].isFile()){
				files = listOfFiles[i].getName();
				if (files.endsWith(".eaf") || files.endsWith("(1).EAF")){
					out.add(files);
				}
		    }
	    }
		Collections.sort(out);
		return out;
	}
	
	public static void doMerge(String path, String dir, ArrayList<String> filenames){
		if (filenames.size() == 2){
			String ultimateFileName = filenames.get(filenames.size() - 1);
			String penultimateFileName = filenames.get(filenames.size()- 2);
			String outFileName = penultimateFileName.substring(0, penultimateFileName.lastIndexOf(".")) + "+" + ultimateFileName.substring(ultimateFileName.lastIndexOf("/")+1);
			merge(path, dir, penultimateFileName, ultimateFileName, outFileName.replace(".eaf", "_komplett.eaf"));
		}
		if (filenames.size() > 2){
			String ultimateFileName = filenames.get(filenames.size() - 1);
			String penultimateFileName = filenames.get(filenames.size()- 2);
			String outFileName = penultimateFileName.substring(0, penultimateFileName.lastIndexOf(".")) + "+" + ultimateFileName.substring(ultimateFileName.lastIndexOf("/")+1);
			System.out.println("storing intermediate file as" + path + dir + "/" +outFileName);
			merge(path, dir, penultimateFileName, ultimateFileName, outFileName);
			filenames.remove(penultimateFileName);
			filenames.remove(ultimateFileName);
			filenames.add(outFileName);
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doMerge(path, dir, filenames); // recursive call
		}
	}

	public static void merge(String path, String dir, String firstFileName, String secondFileName, String outFileName){	
		System.out.println("about to merge " + path + dir + "/" +firstFileName + " and " + path + dir + "/" +secondFileName + " to " + path + outFileName);
		MergeTranscriptionsCommand mtc = new MergeTranscriptionsCommand("mergeTranscriptions");
		TranscriptionImpl destTrans = new TranscriptionImpl(path + dir + "/"+firstFileName);
		TranscriptionImpl srcTrans = new TranscriptionImpl(path + dir + "/" +secondFileName);
		long d = destTrans.getLatestTime();
    	srcTrans.shiftAllAnnotations(d);
		String fileName = new String(path + dir +"/" + outFileName);
		Collection<Tier> tiers = destTrans.getTiers();
		ArrayList<String> selTiers = new ArrayList<String>();
		for (Tier tier : tiers){
			selTiers.add(tier.getName());
		}
		boolean overwrite = false;
		boolean addLinkedFiles = false;
		
		Object[] arguments = new Object[5];
		arguments[0] = srcTrans;
		arguments[1] = fileName;
		arguments[2] = selTiers;
		arguments[3] = overwrite;
		arguments[4] = addLinkedFiles;
		
		mtc.execute(destTrans, arguments);
	}
}