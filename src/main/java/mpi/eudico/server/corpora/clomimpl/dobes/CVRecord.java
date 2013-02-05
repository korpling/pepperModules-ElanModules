package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.ArrayList;

/**
 * Stores information needed to construct a ControlledVocabulary object
 * 
 * @see mpi.util.ControlledVocabulary
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class CVRecord {
	private String cv_id;
	private String description;
	private String extRefId;
	private ArrayList entries;
	private String cvName;
	
	
	/**
	 * Construct an empty CVRecord, sets cv_id
	 * 
	 * @param cv_id
	 */
	public CVRecord(String cv_id) {
		setCv_id(cv_id);
		description = null;
		extRefId = null;
		entries = new ArrayList();
	}
	
	/**
	 * Returns a boolean saying whether this record has contents
	 * 
	 * @return
	 */
	public boolean hasContents() {
		if(description != null && description != "") {
			return true;
		}
		if(entries.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return the cv_id
	 */
	public String getCv_id() {
		return cv_id;
	}
	/**
	 * @param cvId the cv_id to set
	 */
	public void setCv_id(String cvId) {
		cv_id = cvId;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the extRefId
	 */
	public String getExtRefId() {
		return extRefId;
	}
	/**
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	/**
	 * @return the entries
	 */
	public ArrayList getEntries() {
		return entries;
	}
	/**
	 * @param entries the entries to set
	 */
	public void setEntries(ArrayList entries) {
		this.entries = entries;
	}
	
	/**
	 * @param cvEntryRecord
	 */
	public void addEntry(CVEntryRecord cvEntryRecord) {
		entries.add(cvEntryRecord);
	}
	
	/**
	 * @param cvEntryRecord
	 */
	public void removeEntry(CVEntryRecord cvEntryRecord) {
		entries.remove(cvEntryRecord);
	}

	/**
	 * @param cvName
	 */
	public void setCvName(String cvName) {
		this.cvName = cvName;
	}
}
