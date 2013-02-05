package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * Stores information needed to construct a CVEntry object.
 * 
 * @see mpi.eudico.util.CVEntry
 * 
 * @author Han Sloetjes
 * @version jun 2004
 */
public class CVEntryRecord {
	private String description;
	private String value;
	private String extRefId;
	private String id;

	/**
	 * Returns the description.
	 * @return the description, or null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the description.
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the value.
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	

	/**
	 * Returns the id of an external reference object
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept defined in ISO DCR
	 */
	public String getExtRefId() {
		return extRefId;
	}

	/**
	 * Sets the external reference id.
	 * 
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

}
