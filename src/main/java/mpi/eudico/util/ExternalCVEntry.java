package mpi.eudico.util;

public class ExternalCVEntry extends CVEntry {
	private String id;

	public ExternalCVEntry(String content, String desc, String id) {
		super(content, desc);
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
