package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.cleanUp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.awt.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

public class DDDPreparer {

	private static TranscriptionImpl eaf;
	private static String log = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//get properties file
		FileInputStream in = new FileInputStream("/home/tom/DDDcorpora/heliand-settings.txt");
		Properties prop = new Properties();
		prop.load(new InputStreamReader(in, "UTF-8"));
		
		Collection<String> fnames = getFileNamesInDirectory(prop.getProperty("input"));
		for (String fname : fnames){
			prepare(fname, prop);
		}
	}
		
	public static void prepare(String fname, Properties prop){
		// parse the Elan file
		eaf = new TranscriptionImpl(prop.getProperty("input") + "/" + fname);
		System.out.println("working on " + fname);
		
		// go through the tiers and report issues (e.g. timeslots, weird symbols)
		reportIssues(fname, "/home/tom/Dropbox/ElanModule/DDDPreparer.issues");
		
		// search and replace
		searchAndReplaces(fetchArrayFromPropFile("searchAndReplace", prop));
		
		// create the reference tier "tok" on the basis of a given tier
		TierImpl tierTok = (TierImpl) eaf.getTierWithId(prop.getProperty("tok"));
		tierTok.setName("tok");
		
		// create the txt tier which holds the actual words
		createTxtTier(prop.getProperty("txt"));
		
		// rename the remaining tiers
		String[][] translation = fetchArrayFromPropFile("rename", prop);
		renameTiers(translation);
		
		// get rid of the tiers that are not necessary
		String tiersToBeRemoved[] = prop.getProperty("remove").split(",");
		removeTiers(tiersToBeRemoved);
		
		// save the new file
		AbstractAnnotation lastToken = (AbstractAnnotation) tierTok.getAnnotations().lastElement();
		long endtime = lastToken.getEndTimeBoundary();
		String foutName = prop.getProperty("output") + "/" + fname;
		SaveEAF e = new SaveEAF(eaf, (long) 0, (long) endtime, foutName);
		
		// write a logfile
		try {
			FileUtils.writeStringToFile(new File( prop.getProperty("log")), log);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void reportIssues(String fin, String fout){
		Vector<TierImpl> tiers = eaf.getTiers();
		for (TierImpl tier : tiers){
			String tierName = tier.getName();
			Vector<AbstractAnnotation> annos = tier.getAnnotations();
			for (AbstractAnnotation anno : annos){
				String annoValue = anno.getValue().trim();
				int beginTime = (int) anno.getBeginTimeBoundary();
				int endTime = (int) anno.getEndTimeBoundary();
				
				// check consistency of annotation boundaries for main-tiers
				int beginDiff = beginTime%200;
				int endDiff = endTime%200;
				if (tier.getLinguisticType().getLinguisticTypeName().equals("main-tier")){
					if (beginDiff > 0 | endDiff > 0){
						log = log + ("WARNING: " + fin + ":" + 
								tierName + ":" + 
								Milliseconds2HumanReadable(beginTime) +
								Milliseconds2HumanReadable(endTime) + ":" +
								annoValue.trim() + 
								" | wrong begin or end time, please correct in original file\n");
					}
				}
				
				// notify of - at the beginning or ending of annotations in referenztext w
				if (tierName.equals("Referenztext W")){
					if ( annoValue.trim().length() > 1 & (annoValue.startsWith("-") | annoValue.endsWith("-"))){
						String newValue = annoValue.replaceAll("\\b-", "").replaceAll("-\\b", "");
						anno.setValue(newValue);
						log = log + ("WARNING: " + fin + ":" + 
								tierName + ":" + 
								Milliseconds2HumanReadable(beginTime) + ":" + 
								Milliseconds2HumanReadable(endTime) + ":" + 
								annoValue.trim() + 
								" | removed a minus character at beginning or ending, this might be an error\n");
					}
				}
				
				// notify of [] at the beginning or ending of annotations
				if (annoValue.contains("[") | annoValue.contains("]")){
					String newValue = annoValue.replace("[", "").replace("]", "");
					anno.setValue(newValue);
					log = log + ("CHANGE: " + fin + ":" + 
							tierName + ":" + 
							Milliseconds2HumanReadable(beginTime) + ":" + 
							Milliseconds2HumanReadable(endTime) + ":" + 
							annoValue.trim() + 
							" | removed a [ or ] character at beginning or ending, this might be an error\n");
				}
			}
		}
	}
	
	private static Collection<String> getFileNamesInDirectory(String path){
		String files;
		Collection<String> out = new Vector();	
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(); 
		for (int i = 0; i < listOfFiles.length; i++){ 
			if (listOfFiles[i].isFile()){
				files = listOfFiles[i].getName();
				if (files.endsWith(".eaf") || files.endsWith(".EAF")){
					out.add(files);
				}
		    }
	    }
		return out;
	}
	
	private static String Milliseconds2HumanReadable(int millis){
		return String.format("%d min, %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);
	}
	
	private static void searchAndReplaces(String[][] m){
		for (int i = 0; i < m.length; i++) {
	        String targetTier = m[i][0].trim();
	        String annoValue = m[i][1].trim();
	        String findValue = m[i][2].trim();
	        String replaceValue = m[i][3].trim();
	        String[] conditions = Arrays.copyOfRange(m[i], 4, m[i].length);
	        searchAndReplace(targetTier, annoValue, findValue, replaceValue, conditions);
		}
	}
	
	private static String[][] fetchArrayFromPropFile(String propertyName, Properties propFile) {
		String[] a = propFile.getProperty(propertyName).split(";");
		String[][] array = new String[a.length][a.length];
		for(int i = 0;i < a.length;i++) {
			a[i] = a[i].replace("\\,", "COMMA");
			array[i] = a[i].split(",");
			for (int j = 0; j < array[i].length; j++){
				array[i][j] = array[i][j].replaceAll("COMMA", ",");
			}
		}
		return array;
	}
	
	private static void searchAndReplace(String targetTier, String annoValue, String findValue, String replaceValue, String[] conditions) {
		TierImpl ctier = (TierImpl) eaf.getTierWithId(targetTier);
		String fin = eaf.getFullPath().substring(eaf.getFullPath().lastIndexOf("/")+1);
		Vector<AbstractAnnotation> annos = ctier.getAnnotations();
				
		for (int i = 0; i < annos.size(); i++){
			AbstractAnnotation targetAnno = annos.get(i);
			AbstractAnnotation compareAnno = null;
			boolean test = true;
			String condition = "";

			for (int c = 0; c < conditions.length-1; c=c+3){
				if (test){
					String direction = conditions[c].trim();
					String condTier = conditions[c+1].trim();
					String condValue = conditions[c+2].trim();
					String actualCondition = new String("on tier " + condTier + ", position " + direction + ", has the value " + condValue);
					test = false;
	
					TierImpl tierWithCondition = null;
					Vector<AbstractAnnotation> condAnnos = null;
					if ( condTier.length() > 0) {
						tierWithCondition = (TierImpl) eaf.getTierWithId(condTier);
						condAnnos = tierWithCondition.getAnnotations();
					}
				
					// check direction
					if (direction.equals("left") & i > 0){
						compareAnno = (AbstractAnnotation) tierWithCondition.getAnnotationBefore(targetAnno.getBeginTimeBoundary());
					}	
					if (direction.equals("right") & i < annos.size()){
						compareAnno = (AbstractAnnotation) tierWithCondition.getAnnotationAfter(targetAnno.getEndTimeBoundary());
					}
					if (direction.equals("align")){
						compareAnno = (AbstractAnnotation) tierWithCondition.getAnnotationAtTime(targetAnno.getBeginTimeBoundary());
					}
				
					// check the comparison
					if (compareAnno != null){
						if (compareAnno.getValue().matches("\\b(" + condValue + ")\\b")){
							test = true;
							condition = condition + "; " + actualCondition;
						}
					}
				}
			}
			if (test){
				if (targetAnno.getValue().matches("\\b(" + annoValue + ")\\b")){
					String targetValue = targetAnno.getValue();
					String newTargetValue = targetValue.replace(findValue, replaceValue);
					if (!targetValue.equals(newTargetValue)){
						targetAnno.setValue(newTargetValue);
						log = log + ("CHANGE: " + fin + ":" + 
							targetTier + ":" + 
							Milliseconds2HumanReadable((int) targetAnno.getBeginTimeBoundary()) + ":" + 
							Milliseconds2HumanReadable((int) targetAnno.getEndTimeBoundary()) + ":" + 
							targetValue.trim() + 
							" | changed this value to " + newTargetValue + " because I found " + findValue + " with the condition " + condition + "\n");
					}
				}
			}
		}
	}
	
	public static void createTxtTier(String levelname){
		LinguisticType type = new LinguisticType("main-tier");
		TierImpl t = new TierImpl("txt", null, (Transcription) eaf, type);
		eaf.addTier(t);
		TierImpl source = (TierImpl) eaf.getTierWithId(levelname);
		Vector<AbstractAnnotation> sourceAnnos = source.getAnnotations();
		long beginTime = -1;
		long endTime = -1;
		String value = "";
		for (AbstractAnnotation sourceAnno : sourceAnnos){
			if (beginTime < 0){
				beginTime = sourceAnno.getBeginTimeBoundary();
			}
			boolean test = isPunctuation(sourceAnno.getValue().trim());
			if (!test){
				value = value + sourceAnno.getValue();
				endTime = sourceAnno.getEndTimeBoundary();
			}
			if (test){
				if (!value.isEmpty()){
					AlignableAnnotation aa = (AlignableAnnotation) t.createAnnotation(beginTime, endTime);
					aa.setValue(value);
				}
				if (!sourceAnno.getValue().trim().isEmpty()){
					AlignableAnnotation aa = (AlignableAnnotation) t.createAnnotation(sourceAnno.getBeginTimeBoundary(), sourceAnno.getEndTimeBoundary());
					aa.setValue(sourceAnno.getValue());
				}
				beginTime = -1;
				value = "";
			}	
		}
	}
	
	public static boolean isPunctuation(String s){
		boolean out = false;
		String punct = "!()?.:,;\"";
		if (s.isEmpty()){
			out = true;
		}
		if (!s.isEmpty()){
			if (punct.contains(s)){
				out = true;
			}
		}
		return out;
	}
	
	public static void renameTiers(String[][] m){
		for (int i = 0; i < m.length; i++) {
	        String origName = m[i][0].trim();
	        String newName = m[i][1].trim();
	        TierImpl tier = (TierImpl) eaf.getTierWithId(origName);
	        if (tier != null){
	        	tier.setName(newName);
	        	tier.setParentTier(null);
	        }
	    }
	}
	
	public static void removeTiers(String[] toBeRemoved){
		for (String remove : toBeRemoved){
			remove = remove.trim();
			try{
				TierImpl tier = (TierImpl) eaf.getTierWithId(remove);
				boolean removeok = true;
				for (TierImpl subtier : (Collection<TierImpl>) tier.getChildTiers()){
					for (String check : toBeRemoved){
						if (subtier.equals(check)){
							removeok = false;
							break;
						}
					}
				}
				if (removeok){
					eaf.removeTier(tier);
				}
			} catch (java.lang.NullPointerException nothing){
				continue;
			}
		}
	}
}