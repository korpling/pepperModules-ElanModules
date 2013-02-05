package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder for participants information for multiple  files.
 * 
 * @author Han Sloetjes
 */
public 	class ParticipantStats {
	private String partName;
	public List<Long> durations;// should every individual duration be in the list or only the unique durations?
	//public int numFiles;
	public int numTiers;
	public int numAnnotations;
	public long minDur;
	public long maxDur;
	public long totalDur;
	public long latency;
	
	private List<String> fileNames;
	private List<String> tierNames;
	
	/**
	 * @param partName
	 */
	public ParticipantStats(String partName) {
		super();
		this.partName = partName;
		fileNames = new ArrayList<String>();
		tierNames = new ArrayList<String>();
		durations = new ArrayList<Long>();
	}
	
	public String getPartName() {
		return partName;
	}
	
	public void addFileName(String fileName) {
		if (!fileNames.contains(fileName)) {
			fileNames.add(fileName);
		}
	}
	
	public void addTierName(String name) {
		if (!tierNames.contains(name)) {
			tierNames.add(name);
		}
	}
	
	public int getNumFiles() {
		return fileNames.size();
	}
	
	public int getNumUniqueTiers() {
		return tierNames.size();
	}
}
