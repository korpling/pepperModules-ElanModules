package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.cleanUp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class Excel {
	
	public static void main(String[] args) throws Exception {
		Collection<String> fnames = getFileNamesInDirectory(args[0]);
		for (String fname : fnames){
			excelify(args[0] + fname);
		}
	}
	
	public static Collection<String> getFileNamesInDirectory(String path){
		String files;
		Collection<String> out = new Vector<String>();	
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		System.out.println("there are a number of files in this place: " + path + ", " + listOfFiles.length);
		for (int i = 0; i < listOfFiles.length; i++){ 
			if (listOfFiles[i].isFile()){
				files = listOfFiles[i].getName();
				if (files.endsWith(".eaf") || files.endsWith("(1).EAF")){
					out.add(files);
				}
		    }
	    }
		return out;
	}
	
	public static void excelify(String fname){
		// this is the map in which everything is stored
		Map<Long, Map> m = new HashMap<Long, Map>();
		
		// get the file to work on
		String fnameout = fname.replace(".eaf", ".csv");
		
		// parse the Elan file
		TranscriptionImpl eaf = new TranscriptionImpl(fname);
		
		System.out.println("working on " + fname);
		
		// Tiernames that are interesting
		ArrayList<String> tiernames = new ArrayList<String>();
		tiernames.add("M1a DDDTS Lemma");
		tiernames.add("Lemma");
		tiernames.add("Referenztext W");
		tiernames.add("Übersetzung");
		tiernames.add("M1b DDDTS Beleg");
		tiernames.add("M2a Flexion Lemma");
		tiernames.add("M2b Flexion Beleg 1");
		tiernames.add("M2b Flexion Beleg 2");
		tiernames.add("M2c Flexion Beleg");
		
		// go through tiers
		Vector<Tier> tiers = eaf.getTiers();
		for (Tier tier : tiers){
			TierImpl tierimpl = (TierImpl) tier;
			if (tiernames.contains(tierimpl.getName())){
				// go through annotations on tier
				Vector<AbstractAnnotation> annos = tierimpl.getAnnotations();
				for (AbstractAnnotation anno : annos){
					long begin = anno.getBeginTimeBoundary();
					String value = anno.getValue().trim();
					String attr = tierimpl.getName().trim();
					if (m.containsKey(begin)){
						Map<String, String> mm = m.get(begin);
						mm.put(attr, value);
					}
					if (!m.containsKey(begin)){
						Map<String, String> mm = new HashMap<String, String>();
						mm.put(attr, value);
						m.put(begin, mm);
					}
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();		
		Collections.sort(tiernames);
		sb.append("zeit;" + join( tiernames, ";") + "\n" );
		SortedMap<Long, Map> sortedMap = new TreeMap<Long, Map>(m);
		for (long timeslot : sortedMap.keySet()){
			sb.append(Milliseconds2HumanReadable(timeslot));
			for (String ebene : tiernames){
				if (m.get(timeslot).containsKey(ebene)){
					sb.append(";" + m.get(timeslot).get(ebene));
				}
				if (!m.get(timeslot).containsKey(ebene)){
					sb.append(";");
				}
			}
			sb.append("\n");
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fnameout));
			String outText = sb.toString();
			out.write(outText);
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String Milliseconds2HumanReadable(long millis){
		return String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);
	}

	public static String join(ArrayList<String> r,String d)
	{
	        if (r.size() == 0) return "";
	        StringBuilder sb = new StringBuilder();
	        int i;
	        for(i=0;i<r.size()-1;i++)
	            sb.append(r.get(i)+d);
	        return sb.toString()+r.get(i);
	}

}
