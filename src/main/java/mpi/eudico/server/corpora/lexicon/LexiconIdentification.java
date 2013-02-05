package mpi.eudico.server.corpora.lexicon;

/**
 * Holds the ID, name and description of a Lexicon
 * @author Micha Hulsbosch
 *
 */
public class LexiconIdentification implements Comparable {
	private String id;
	private String name;
	private String description;
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
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	
	public String toString() {
		return name;
	}
	public int compareTo(Object o) {
		if(o instanceof LexiconIdentification) {
			return name.compareToIgnoreCase(((LexiconIdentification) o).getName());
		}
		return 0;
	}
}
