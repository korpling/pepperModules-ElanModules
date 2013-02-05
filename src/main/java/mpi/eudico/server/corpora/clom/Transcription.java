package mpi.eudico.server.corpora.clom;

import java.util.HashMap;
import java.util.Vector;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.location.LocatorManager;
import mpi.eudico.server.corpora.util.ACMEditableDocument;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.server.corpora.util.SharedDataObject;

/**
 * Transcription encapsulates the notion of a transcription. Transcriptions are
 * contained in Sessions. Each implementation of the EUDICO abstraction layer
 * has it's own implementation of Transcription. A Transcription is browsable
 * (TreeViewable), is accessible to multiple Identities (DataLocatorList), can
 * be manipulated or displayed by Tools (ToolAdministrator) and is part of the
 * Corpus data hierarchy (DataTreeNode).
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 5-Nov-1998
 * @version Aug 2005 Identity removed
 */
// 10 0ct 2000; Daan Broeder; made it a LanguageResource

public interface Transcription extends LanguageResource, SharedDataObject,
		DataTreeNode, ACMEditableObject, ACMEditableDocument {

	public static int NORMAL = 0;
	public static int BULLDOZER = 1;
	public static int SHIFT = 2;

	/**
	 * Gives the Transcription name.
	 *
	 * @return	the name
	 */
	public String getName();

	public void setName(String theName);

	/**
	 * MK:02/06/19 adding missing getter, which leaded to bloated parameter lists.
	 *
	 * @return	locatorManager
	 */
	// HS Nov 2009: removed from interface
	//public LocatorManager getLocatorManager();

	/**
	 * Returns the owner of the Transcription object.
	 *
	 * @return	the owner
	 */
	public String getOwner();

	/**
	 * Returns the list of Tiers that are accessible.
	 *
	 * @return	the list of Tiers
	 */
	public Vector getTiers();

	/**
	 * Returns an ordered list of all Tags on any of theTiers. This list for example
	 * is used in MultiTierViewers.
	 */
	// HS Nov 2009: removed from interface
	//public Vector getTagsForTiers(Vector theTiers);

	/**
	 * Loads all Tags for all Tiers. This method is particularly useful if
	 * Tags from multiple Tiers can be read in order from a Transcription that is
	 * implemented as a file.
	 * <p>MK:02/06/12M<br>
	 * Implemented in CGN, bogus implemtation in Gesture,
	 * using annotations in DOBES, NOP in shoebox, implemented in CHAT.<br>
	 * Tags are probably 'the former annotations'.
	 * Naming must be corrected.
	 * </p>
	 */
	// HS Nov 2009: removed from interface
	//public void loadTags();

	//public MetaTime getMetaTime();

	/**
	 * Prints some statistics of the Transcription. This method is used for testing
	 * and debugging purposes. Since Transcription is implemented as remote object
	 * the statistics are printed on the server's console.
	 */
	public void printStatistics();
	// HS Nov 2009: removed from interface
	//public Object getConcreteData();


	/**
	 * Adds a Tier to the Transcription.
	 *
	 * @param theTier	the Tier to be added
	 */
	public void addTier(Tier theTier);


	/**
	 * Removes a Tier from the Transcription.
	 *
	 * @param theTier	the Tier to be removed
	 */
	public void removeTier(Tier theTier);


	/**
	 * Returns all TimeSlots, ordered in a TimeOrder object.
	 *
	 */
	public TimeOrder getTimeOrder();

	public void setAuthor(String theAuthor);

	public String getAuthor() ;

	public void setLinguisticTypes(Vector theTypes);

	public Vector getLinguisticTypes();

	public void addLinguisticType(LinguisticType theType);
	public void removeLinguisticType(LinguisticType theType);
	public void changeLinguisticType(LinguisticType linType,
			String newTypeName,
			Vector constraints,
			String newControlledVocabularyName,
			boolean newTimeAlignable,
			boolean newGraphicsAllowed,
			String dataCategoryId,
			LexiconQueryBundle2 queryBundle);

	public Vector getTiersWithLinguisticType(String typeID);
	// lexicon related methods
	/** 
	 * 
	 * @param link
	 */
	public void addLexiconLink(LexiconLink link);
	public HashMap<String, LexiconLink> getLexiconLinks();
	public LexiconLink getLexiconLink(String linkName);
	public void removeLexiconLink(LexiconLink link);

	/**
	 * <p>MK:02/06/19<br>Too general API: Should be protected, not public. implemented as NOP, except for Dobes,
	 * which is used only from Corpus-Database. Should be called only from constructor.
	 * </p>
	 */
	public void setMainMediaFile(String pathName);

	public Vector getMediaDescriptors();
	public void setMediaDescriptors(Vector theMediaDescriptors);
	
	/**
	 * Returns the collection of linked file descriptors
	 * @return the linked file descriptors
	 */
	public Vector getLinkedFileDescriptors();
	
	/**
	 * Sets the collection of linked files descriptors.
	 * 
	 * @param descriptors the new descriptors
	 */
	public void setLinkedFileDescriptors(Vector descriptors);

	/**
	 * <p>MK:02/06/12<br>
	 * The ID of a tier has yet to be defined.
	 * Tiers so far have only names given by the user,
	 * which have been used in the EAF XML file format as XML IDs.
	 * Because tier name are used as IDs, not two
	 * tiers can have the same name. This is unacceptable, because each tier has
	 * its own speaker/participant. When two persons speak on the same tier, the
	 * tier has to be renamed for each person. For shoebox, this is done by
	 * appending '@'spekaer to the tiername. This will confuse the user.
	 * For lifting this restriction, tiers should get a proper ID.
	 * <p>
	 * @param theTierId currently the name of the tier. Has to be changed!
	 * @return Tier with gien name, or null
	 * */
	public Tier getTierWithId(String theTierId);

	public Vector getAnnotationsUsingTimeSlot(TimeSlot theSlot);
	
	/**
	 * Returns a Vector containing all annotations (as ids!) covering the corresponding time
	 * @param time
	 * @return
	 */
	public Vector getAnnotationIdsAtTime(long time);
	
	/**
	 * Returns the annotation with the corresponding id or null. 
	 * @param id
	 * @return
	 */
	public Annotation getAnnotation(String id);

	public long getLatestTime();
	
	/**
	 * Returns whether any modifications are made since the last reset (when saving)
	 */
	public boolean isChanged();

	/**
	 * Resets 'changed' status to unchanged
	 */
	public void setUnchanged();

	/**
	 * Sets 'changed' status to changed
	 */
	public void setChanged();
	
	/**
	 * Returns time Change Propagation Mode (normal, bulldozer or shift)
	 * 
	 * @author hennie
	 */
	public int getTimeChangePropagationMode();
	
	/**
	 * Set Time Change Propagation Mode (normal, bulldozer or shift)
	 * @author hennie
	 */
	public void setTimeChangePropagationMode(int theMode);
}
