package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.util.RemoteListIterator;

/**
 * MetaTime encapsulates the ordering of Tags (of multiple Tiers) in a
 * Transcription. It is considered to be part of the Transcription. Ordering
 * is only stored for time segments that are not aligned with media time.
 * So, the more tags are aligned with media time, the smaller the resulting
 * MetaTime is.
 * The MetaTime is used when comparing Tags in the Tag's compareTo method.
 * Given a constructed MetaTime, it is then sufficient to add Tags to a TreeSet,
 * they will be ordered according to the MetaTime automatically.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 20-Apr-1999
 */
public interface MetaTime extends RemoteListIterator {

	/**
	 * Adds a Tag to the MetaTime at currentTag. The Tag
	 * can be either time-aligned or not time-aligned. insertTag may only
	 * be called from Tier.addTag
	 *
	 * @param theTag	the Tag to be inserted.
	 */		
	public void insertTag(Tag theTag); 

	/**
	 * A utility method to print the current state of MetaTime to standard output.
	 */	
	public void printMetaTime();
	
	/**
	 * Returns true if tag1 starts before tag2, according to the order
	 * specified by the MetaTime. Each Tag can be either time-aligned or 
	 * not time-aligned.
	 *
	 * @param tag1	first tag to be compared.
	 * @param tag2	second tag to be compared.
	 * @return	true if tag1 starts before tag2.
	 */	
	public boolean startsBefore(Tag tag1, Tag tag2);	
	
	/**
	 * Returns the number of elements in MetaTime.
	 */
	public int size();	
}