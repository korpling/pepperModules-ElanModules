package mpi.eudico.server.corpora.clomimpl.abstr;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.DataTreeNode;
import mpi.eudico.server.corpora.clom.DecoderInfo;
//import mpi.eudico.server.corpora.clom.MediaObject;
//import mpi.eudico.server.corpora.clom.MetaTime;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
//import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;
//import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;
import mpi.eudico.server.corpora.location.LocatorManager;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;

/**
 * TranscriptionImpl implements Transcription.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 22-Jun-1999
 *
 *
 */
// modified Daan Broeder 23-10-2000
// added url attribute + getFullPath() method
// added url parameter to constructor
//
public class TranscriptionImpl implements Transcription {

		private ArrayList listeners;

		public void addACMEditListener(ACMEditListener l){
		if(!listeners.contains(l)) {
			listeners.add(l);
		}
	}

		public void removeACMEditListener(ACMEditListener l){
		listeners.remove(l);
	}

	public void notifyListeners(ACMEditableObject source, int operation, Object modification){
		Iterator i=listeners.iterator();
		ACMEditEvent event = new ACMEditEvent(source, operation, modification);
		while(i.hasNext()){
			((ACMEditListener)i.next()).ACMEdited(event);
		}
	}


   	public void modified(int operation, Object modification) {
   		handleModification(this, operation, modification);
   	}


    public void handleModification(ACMEditableObject source, int operation, Object modification) {
		if (changed == false) changed = true;
		
		timeProposer.correctProposedTimes(this, source, operation, modification); 
		
		if (isNotifying) {
			notifyListeners(source, operation, modification);
		}		
	}
	
	/**
	 * Sets the notification flag.
	 * When set to false ACMEditListeners are no longer notified of modification.
	 * When set to true listeners are notified of an CHANGE_ANNOTATIONS 
	 * ACMEditEvent. Every modification will then be followed by a notification.
	 * 
	 * @param notify the new notification flag  
	 */
	public void setNotifying(boolean notify) {
		isNotifying = notify;

		if (isNotifying) {
			modified(ACMEditEvent.CHANGE_ANNOTATIONS, null);		
		}
	}
	
	/**
	 * Returns the notifying flag.
	 * @return true when ACMEditListeners are notified of every modification, 
	 * 	false otherwise
	 */
	public boolean isNotifying() {
		return isNotifying;
	}


	/**
	 * The list of Tiers in this Transcription.
	 */
	protected Vector tiers;

	/**
	 * The media file or stream associated with this Transcription
	 * - deprecated since ELAN 2.0
	 */
	//protected MediaObject mediaObject;

	/**
	 * Descriptors for the media files or streams associated with this Transcription
	 */
	protected Vector mediaDescriptors;
	
	/**
	 * Descriptors for secondary associated files. I.e. non-audio/video files, or files 
	 * representing sources that are not a primary subject of transcription.
	 */
	protected Vector linkedFileDescriptors;

	/**
	 * The url of the transcription (if applicable)
	 */
	protected String url;
	/**
	 * The content type of the transcription (if applicable)
	 * default impl. is "text/plain"
	 */
	protected String content_type="text/plain";

	/**
	 * Transcription name
	 */
	protected String name;
	protected String owner;
	private DataTreeNode parent; // back reference, used for deletion of Transcriptions

	protected LocatorManager locatorManager;

	//protected MetaTime metaTime;

	protected TimeOrder timeOrder;
	protected Vector linguisticTypes;		// contains id strings for all types
	protected String author;
	protected boolean isLoaded;

	private boolean changed = false;
	
	private int timeChangePropagationMode = Transcription.NORMAL;
	private TimeProposer timeProposer;
	
	/** 
	 * Holds associated ControlledVocabulary objects.
	 * @since jun 04
	 */
	protected Vector controlledVocabularies;
	
	private ArrayList docProperties;
	private HashMap<String, LexiconServiceClientFactory> lexiconServiceClientFactories;
	private boolean lexcionServicesLoaded = false;
	
	/**
	 * Holds associated Lexicon Link objects
	 * @author Micha Hulsbosch
	 * @since October 2010
	 */
	private HashMap<String, LexiconLink> lexiconLinks;
	
	
	/**
	 * A flag to temporarily turn of unnecessary notification of ACMEditListeners, 
	 * e.g. when a number of modifications is performed in a batch.
	 * @since oct 04
	 */
	protected boolean isNotifying;

	/**
	 * New constructor for unknown file name
	 */
	public TranscriptionImpl() {
		this(UNDEFINED_FILE_NAME);
	}

	/**
	 * New constructor with only the full file path
	 *
	 * @param eafFilePath DOCUMENT ME!
	 */
	public TranscriptionImpl(String eafFilePath) {
		this(eafFilePath.substring(eafFilePath.lastIndexOf(System.getProperty(
				"file.separator")) + 1),
				null,
				null,  
				"file:" + eafFilePath);
				
		initialize(eafFilePath.substring(eafFilePath.lastIndexOf(System.getProperty(
				"file.separator")) + 1),
				eafFilePath, null);	
	}

	/**
	 * Constructor with the full source file path and an additional info object for the decoder/parser.
	 *
	 * @param sourceFilePath the full path to the source file
	 * @param decoderInfo the info object for the parser
	 */
	public TranscriptionImpl(String sourceFilePath, DecoderInfo decoderInfo) {
		this(sourceFilePath.substring(sourceFilePath.lastIndexOf(System.getProperty(
				"file.separator")) + 1),
				null,
				null,  
				"file:" + sourceFilePath);
				
		initialize(sourceFilePath.substring(sourceFilePath.lastIndexOf(System.getProperty(
				"file.separator")) + 1),
				sourceFilePath, decoderInfo);	
	}
	
	/**
	 * MK:02/06/19
	 * @param myURL unclear, hack from Dobes or Chat, often set null.
	 */
	public TranscriptionImpl(String theName, 
							DataTreeNode theParent, 
							LocatorManager theLocatorManager,
							String myURL) {
		name = theName;
		parent = theParent;
		url = myURL;
		locatorManager = theLocatorManager;

		tiers = new Vector();

		//metaTime = new FastMetaTime();
		listeners=new ArrayList();

		timeOrder = new TimeOrderImpl(this);
		linguisticTypes = new Vector();
		mediaDescriptors = new Vector();
		linkedFileDescriptors = new Vector();
		controlledVocabularies = new Vector();
		docProperties = new ArrayList(5);
		isLoaded = false;
		isNotifying = true;
		timeProposer = new TimeProposer();
	}

	/**
	 * @param name HAS TO BE THE SAME AS THE XML NAME PREFIX
	 * @param fileName the absolute path of the XML transcription file.
	 * @param decoderInfo decoder info object for certain file types(Toolbox, Transcriber etc)
	 */
	private void initialize(String name, String fileName, DecoderInfo decoderInfo) {
     
		if (fileName.startsWith("file:")) {
			fileName = fileName.substring(5);
		}

		author = ""; // make sure that it is initialized to empty string

		File fff = new File(fileName);
		if (!fff.exists()) {
			isLoaded = true; // prevent loading
		} else {
			isLoaded = false;
		}

		this.fileName = fileName;

		// we don't know if it's wav or mpg or mov. We have to try.
		String mimeType = "";
		this.mediafileName = null;
		// TODO the following 3 blocks should be removed, check
		// try mpg first!
		// replace *.??? by *.mpg
		if (this.mediafileName == null && fileName != null && fileName.length() > 3) {
			String test = this.fileName.substring(0, this.fileName.length() -
					3) + "mpg";

			if (test.startsWith("file:")) {
				test = test.substring(5);
			}

			if ((new File(test)).exists()) {
				this.mediafileName = test;
				mimeType = MediaDescriptor.MPG_MIME_TYPE;
			}
		}

		// replace *.??? by *.wav
		if (this.mediafileName == null && fileName != null && fileName.length() > 3) {
			String test = this.fileName.substring(0, this.fileName.length() -
					3) + "wav";

			if (test.startsWith("file:")) {
				test = test.substring(5);
			}

			if ((new File(test)).exists()) {
				this.mediafileName = test;
				mimeType = MediaDescriptor.WAV_MIME_TYPE;
			}
		}

		// HS 21-11-2001 mov added
		// replace *.??? by *.mov
		if (this.mediafileName == null && fileName != null && fileName.length() > 3) {
			String test = this.fileName.substring(0, this.fileName.length() -
					3) + "mov";

			if (test.startsWith("file:")) {
				test = test.substring(5);
			}

			if ((new File(test)).exists()) {
				this.mediafileName = test;
			}
		}

        lexiconLinks = new HashMap<String, LexiconLink>();
		// media object is not used anymore
		//this.mediaObject = createMediaObject(this.mediafileName);

		// HB, 3 dec 03. After this, media descriptors are instantiated, if in the EAF file
		if (!isLoaded()) {
			ACMTranscriptionStore.getCurrentTranscriptionStore().loadTranscription(this, decoderInfo);
			// jul 2005: make sure the proposed times are precalculated
			timeProposer.correctProposedTimes(this, null, 
							ACMEditEvent.CHANGE_ANNOTATIONS, null);
		}
		
		// if no media descriptors, and mediafileName is not null, create media descriptors
		if ((mediaDescriptors.size() == 0) && (mediafileName != null) && 
			mediafileName.length() > 0) {
			String mediaURL = pathToURLString(mediafileName);

			//		String mediaURL = "file:///" + mediafileName;
			//		mediaURL = mediaURL.replace('\\', '/');
			MediaDescriptor masterMD = new MediaDescriptor(mediaURL, mimeType);
			mediaDescriptors.add(masterMD);

			String checkFile = this.fileName.substring(0,
					this.fileName.length() - 3) + "wav";

			if (checkFile.startsWith("file:")) {
				checkFile = checkFile.substring(5);
			}

			if ((new File(checkFile)).exists()) {
				String signalURL = pathToURLString(checkFile);

				//			String signalURL = "file:///" + checkFile;
				//			signalURL = signalURL.replace('\\', '/');
				mimeType = MediaDescriptor.WAV_MIME_TYPE;

				MediaDescriptor signalMD = new MediaDescriptor(signalURL,
						mimeType);
				signalMD.extractedFrom = mediaURL;
				mediaDescriptors.add(signalMD);
			}
		}
		// in ELAN versions < 2.7 the .svg file was not referred to in the eaf
		// deal with these older files now by creating a LinkedFileDescriptor
		if (svgFile == null && fileName != null && fileName.length() > 3) {
			String test = this.fileName.substring(0, this.fileName.length() -
					3) + "svg";

			if (test.startsWith("file:")) {
				test = test.substring(5);
			}

			if ((new File(test)).exists()) {
			    setSVGFile(test);
			}
		}
	}

	/*
	 * This method should be in a Utility class or a URL class
	 * Convert a path to a file URL string. Takes care of Samba related problems
	 * file:///path works for all files except for samba file systems, there we need file://machine/path,
	 * i.e. 2 slashes insteda of 3
	 *
	 * What's with relative paths?
	 */
	private String pathToURLString(String path) {
		// replace all back slashes by forward slashes
		path = path.replace('\\', '/');

		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		// remove leading slashes and count them
		int n = 0;

		while (path.charAt(0) == '/') {
			path = path.substring(1);
			n++;
		}

		// add the file:// or file:/// prefix
		if (n == 2) {
			return "file://" + path;
		} else {
			return "file:///" + path;
		}
	}
	

	/**
	 * Returns the name of the Transcription
	 *
	 * @return	name of Transcription
	 */
	public String getName() {
		return name;
	}


	public void setName(String theName) {
		name = theName;
	}

	/**
	 * MK:02/06/19 implementing method from interface Transcription
	 *
	 * @return	locatorManager
	 */
	/* HS Nov 2009 not used, removed
	public LocatorManager getLocatorManager() {
		return locatorManager;
	}
	 */

	/**
	 * Returns the url of the Transcription
	 *
	 * @return	url string of Transcription
	 */
	public String getFullPath() {
		return url;
	}

	/**
	 * getContentType()
	 * @returns The (semi) mime-type for the content of the resource
	 *
	 * default impl returns "text/plain"
	 */
	public String getContentType() {
		return content_type;
	}
	/**
	 * getContentStream()
	 * @returns The InputStream for the content of the resource
	 */
	public InputStream getContentStream() {
	  InputStream is = null;
	  if( url != null )
		try{
		  is = (new URL(url)).openStream();
		} catch (Exception e){
		  is = null;
		}
	  return is;
	}

	public String getOwner() {
		return owner;
	}

	/**
	 * Returns a list of all Tags in theTiers, sorted according to MetaTime ordering.
	 *
	 * @param theTiers	list of Tiers whose Tags should be returned.
	 * @return		an ordered list of Tags.
	 */
	/* HS Nov 2009 not used, removed
	public Vector getTagsForTiers(Vector theTiers) {
		Vector tagList = null;
		Vector tierTags = null;
		TreeSet allTags = new TreeSet();
		Vector tagsToCompare = null;

		loadTags();

		Iterator tierIter = theTiers.iterator();
		while (tierIter.hasNext()) {
			Tier t = (Tier) tierIter.next();
			tierTags = t.getTags();

			allTags.addAll(tierTags);
		}

		tagList = new Vector(allTags);

		return tagList;
	}
	*/

	/* HS Nov 2009 superseded, removed
	public MediaObject getMediaObject() {
		return mediaObject;
	}
	*/

	public Vector getMediaDescriptors() {
		return mediaDescriptors;
	}

	public void setMediaDescriptors(Vector theMediaDescriptors) {
		mediaDescriptors = theMediaDescriptors;
	}

	/**
	 * Returns the collection of linked file descriptors
	 * @return the linked file descriptors
	 */
	public Vector getLinkedFileDescriptors() {
		return linkedFileDescriptors;
	}
	
	/**
	 * Sets the collection of linked files descriptors.
	 * NB: could check for null here
	 * @param descriptors the new descriptors
	 */
	public void setLinkedFileDescriptors(Vector descriptors) {
		linkedFileDescriptors = descriptors;
	}

	/* HS Nov 2009, not used, removed
	public MetaTime getMetaTime() {
		return metaTime;
	}
	*/
	
	public void printStatistics() {
		System.out.println("");
		System.out.println(">>> Name: " + name);
		System.out.println(">>> Number of tiers: " + tiers.size());
	}

	// TreeViewable interface methods

	public String getNodeName() {
		return getName();
	}

	public boolean isTreeViewableLeaf() {
		return true;
	}

	public Vector getChildren() {
		return new Vector();
	}

	// SharedDataObject interface method(s), via Transcription interface

	// Unreferenced interface method

	public void unreferenced() {
		// corpus should store the only reference to transcription, so
		// removing this reference results in deletion by GC

		getParent().removeChild(this);
	}

	// ToolAdministrator interface method

	 /**
	 * Returns the parent object in the hierarchy of Corpus data objects.
	 *
	 * @return	the parent DataTreeNode or null, if no parent exists
	 */
	public DataTreeNode getParent() {
		return parent;
	}

	/**
	 * Removes a child in the Corpus data hierarchy by deleting the reference
	 * to the child. The garbage collector will then do the actual deletion.
	 * Children for GestureTranscription are Tiers. Removing Tiers is not yet
	 * implemented, therefore removeChild does nothing yet.
	 *
	 * @param theChild	the child to be deleted
	 */
	public void removeChild(DataTreeNode theChild) {
		removeTier((Tier) theChild);
	}
	/*
	public Object getConcreteData() {
		return null;
	}
	*/
	// HB, 17-oct-01, migrated methods from DobesTranscription to here.

	public boolean isLoaded() {
		return isLoaded;
	}


	public void setLoaded(boolean loaded) {
		isLoaded = loaded;
		
		// jul 2005: useless here: tiers and annotations have not yet been added 
		// at this point
		/*
		if (loaded) {
			timeProposer.correctProposedTimes(this, null, 
				ACMEditEvent.CHANGE_ANNOTATIONS, null);
		}
		*/
	}

	public void addTier(Tier theTier) {
		// HS Dec 2012 TODO throw an exception if a tier is added with a name that is 
		// already in use by a tier in the list of tiers
		if (theTier == null || getTierWithId(theTier.getName()) != null) {
			// tier (name) already there, return (should throw exception
			return;
		}
		tiers.add(theTier);

		if (isLoaded()) {
			modified(ACMEditEvent.ADD_TIER, theTier);
		}
	}


	public void removeTier(Tier theTier) {
		((TierImpl) theTier).removeAllAnnotations();

		Vector deletedTiers = new Vector();
		deletedTiers.add(theTier);

		// loop over tiers, remove all tiers where:
		// - number of annotations is 0
		// - and tier.hasAncestor(theTier)

		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			if (	(t.getNumberOfAnnotations() == 0) &&
				(t.hasAncestor((TierImpl) theTier)) ){

				deletedTiers.add(t);
			}
		}
		tiers.removeAll(deletedTiers);

		modified(ACMEditEvent.REMOVE_TIER, theTier);
	}


	public TimeOrder getTimeOrder() {
		return timeOrder;
	}


	public void pruneAnnotations() {
		// remove all annotations that are marked deleted
		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			((TierImpl) tierIter.next()).pruneAnnotations();
		}

		timeOrder.pruneTimeSlots();
		// HB, 9 aug 02: moved from tier to transcription to because delete
		// usually concerns more than one tier.
		modified(ACMEditEvent.REMOVE_ANNOTATION, null);

	}

	/**
	 * Refined prune method; only the 'source' tier and its dependent tiers will 
	 * be asked to prune their annotations.
	 * 
	 * @param fromTier the tier that might have ben changed
	 */
	public void pruneAnnotations(Tier fromTier) {
		if (fromTier instanceof TierImpl) {
			Vector depTiers = ((TierImpl) fromTier).getDependentTiers();
			depTiers.add(0, fromTier);
			Iterator tierIter = depTiers.iterator();
			while (tierIter.hasNext()) {
				((TierImpl) tierIter.next()).pruneAnnotations();
			}
			timeOrder.pruneTimeSlots();
			modified(ACMEditEvent.REMOVE_ANNOTATION, null);
		}
	}
	

	public void setAuthor(String theAuthor) {
		author = theAuthor;
	}

	public String getAuthor() {
		return author;
	}


	public void setLinguisticTypes(Vector theTypes) {
		linguisticTypes = theTypes;
	}


	public Vector getLinguisticTypes() {
		return linguisticTypes;
	}

	public LinguisticType getLinguisticTypeByName(String name) {
		LinguisticType lt = null;
		
		if (linguisticTypes != null) {
			Iterator typeIt = linguisticTypes.iterator();
			LinguisticType ct = null;
			while (typeIt.hasNext()) {
				ct = (LinguisticType) typeIt.next();
				
				if (ct.getLinguisticTypeName().equals(name)) {
				    lt = ct;
					break;
				}
			}
		}
		
		return lt;
	}

	public void addLinguisticType(LinguisticType theType) {
		linguisticTypes.add(theType);

		modified(ACMEditEvent.ADD_LINGUISTIC_TYPE, theType);
	}


	public void removeLinguisticType(LinguisticType theType) {
		linguisticTypes.remove(theType);

		modified(ACMEditEvent.REMOVE_LINGUISTIC_TYPE, theType);
	}


	public void changeLinguisticType(LinguisticType linType,
									String newTypeName,
									Vector constraints,
									String cvName,
									boolean newTimeAlignable,
									boolean newGraphicsAllowed,
									String dcId,
									LexiconQueryBundle2 queryBundle) {

		linType.setLinguisticTypeName(newTypeName);
		linType.removeConstraints();
		if (constraints != null) {
			Iterator cIter = constraints.iterator();
			while (cIter.hasNext()) {
				Constraint constraint = (Constraint) cIter.next();
				linType.addConstraint(constraint);
			}
		}
		linType.setControlledVocabularyName(cvName);
		linType.setTimeAlignable(newTimeAlignable);
		linType.setGraphicReferences(newGraphicsAllowed);
		linType.setDataCategory(dcId);
		linType.setLexiconQueryBundle(queryBundle);

		modified(ACMEditEvent.CHANGE_LINGUISTIC_TYPE, linType);
	}

	public Vector getTiersWithLinguisticType(String typeID) {
		Vector matchingTiers = new Vector();

		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();
			if (t.getLinguisticType().getLinguisticTypeName().equals(typeID)) {
				matchingTiers.add(t);
			}
		}

		return matchingTiers;
	}


	public Vector getCandidateParentTiers(Tier forTier) {
		// in the future, this method could take constraints on linguistic types into account.
		// for now, it returns all tiers except forTier itself and tiers that have forTier as
		// an ancestor
		Vector candidates = new Vector();
		Enumeration e = null;


		e=getTiers().elements();

		while(e.hasMoreElements()){
			try{
				TierImpl dTier = (TierImpl) e.nextElement();
				if (dTier.hasAncestor(forTier)) {
					break;
				}

				if (dTier != forTier) {
					candidates.add(dTier);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		return candidates;

	}


//    public abstract void setMainMediaFile(String pathName);

	/**
	 * <p>MK:02/06/12<br>
	 * Implementing method from interface Transription.
	 * <p>
	 * @param theTierId see there!
	 * @return see there!
	 */
	public Tier getTierWithId(String theTierId) {
	Tier t = null;
	Tier result = null;

	Iterator tierIter = tiers.iterator();
	while (tierIter.hasNext()) {
		t = (Tier) tierIter.next();

		if (t.getName().equals(theTierId)) {
			result = t;
			break;
		}

	}

	return result;
	}

	/**
	 * <p>MK:02/06/12<br>
	 * Where the name of all tiers are unique for a transcription, this method
	 * returns the tier with the given name.
	 * If no tier matches the given name, null is returned.<br>
	 * Unless tier IDs are introduced, this method and getTierWithId() are identical.
	 * <br>
	 * Non-unique tiernames must be introduced.
	 * </p>
	 * @param name name of tier, as in tier.getName()
	 * @return first tier in transription with given name, or null.
	 */
	protected final Tier getTierByUniqueName(String name) {
		if (tiers == null) return null;
		Enumeration all = this.tiers.elements();
		while (all.hasMoreElements()) {
			Tier t = (Tier) all.nextElement();
			String n = (String) t.getName();
			if (n.equals(name)) return t;
		}
		return null;
	}

	public Vector getAnnotationsUsingTimeSlot(TimeSlot theSlot) {
		Vector resultAnnots = new Vector();

		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			resultAnnots.addAll(t.getAnnotationsUsingTimeSlot(theSlot));
		}

		return resultAnnots;
	}


	
	public Vector getAnnotsBeginningAtTimeSlot(TimeSlot theSlot) {
		Vector resultAnnots = new Vector();

		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			resultAnnots.addAll(t.getAnnotsBeginningAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	/**
	 * Refined version of getAnnotsBeginningAtTimeSlot(TimeSlot); here only the specified tier 
	 * (optional) and its depending tiers are polled.
	 * @param theSlot the TimeSlot
	 * @param forTier the tier
	 * @param includeThisTier if true annotations on this tier will also be included
	 * @return a Vector containing annotations
	 */
	public Vector getAnnotsBeginningAtTimeSlot(TimeSlot theSlot, Tier forTier, boolean includeThisTier) {
		Vector resultAnnots = new Vector();

		Vector depTiers = ((TierImpl) forTier).getDependentTiers();
		if (includeThisTier) {
			depTiers.add(0, forTier);
		}		
		Iterator tierIter = depTiers.iterator();

		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			resultAnnots.addAll(t.getAnnotsBeginningAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	public Vector getAnnotsEndingAtTimeSlot(TimeSlot theSlot) {
		Vector resultAnnots = new Vector();
		
		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			resultAnnots.addAll(t.getAnnotsEndingAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}
		
	/**
	 * Refined version of getAnnotsEndingAtTimeSlot(TimeSlot); here only the specified tier 
	 * (optional) and its depending tiers are polled.
	 * @param theSlot the TimeSlot
	 * @param forTier the tier
	 * @param includeThisTier if true annotations on this tier will also be included
	 * @return a Vector containing annotations
	 */
	public Vector getAnnotsEndingAtTimeSlot(TimeSlot theSlot, Tier forTier, boolean includeThisTier) {
		Vector resultAnnots = new Vector();
		
		Vector depTiers = ((TierImpl) forTier).getDependentTiers();
		if (includeThisTier) {
			depTiers.add(0, forTier);
		}
		
		Iterator tierIter = depTiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();

			resultAnnots.addAll(t.getAnnotsEndingAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	/**
	 * Iterates over all annotations of time alignable tiers and adds each 
	 * referenced TimeSlot to a HashSet.
	 * 
	 * @return a set of referenced TimeSlots
	 */
	public HashSet getTimeSlotsInUse() {
		HashSet usedSlots = new HashSet(getTimeOrder().size());
		
		Iterator tierIter = tiers.iterator();
		while (tierIter.hasNext()) {
			TierImpl t = (TierImpl) tierIter.next();
			Object annObj;
			AlignableAnnotation aa;
			if (t.isTimeAlignable()) {
				Iterator annIter = t.getAnnotations().iterator();
				while (annIter.hasNext()) {
					annObj = annIter.next();
					if (annObj instanceof AlignableAnnotation) {
						aa = (AlignableAnnotation) annObj;
						usedSlots.add(aa.getBegin());
						usedSlots.add(aa.getEnd());
					}
				}
			}
		}
		
		return usedSlots;
	}
	
	public Vector getAnnotationIdsAtTime(long time){
		Vector resultAnnots = new Vector();
		
		Iterator tierIter = tiers.iterator();
		while(tierIter.hasNext()){
			TierImpl t = (TierImpl) tierIter.next();
			Annotation ann = t.getAnnotationAtTime(time);
			if(ann != null){
				resultAnnots.add(ann.getId());
			}
		}
		
		return resultAnnots;
	}

	public Annotation getAnnotation(String id){
		if(id == null) return null;
		Iterator tierIter = tiers.iterator();
		Annotation a;
		while(tierIter.hasNext()){
			TierImpl t = (TierImpl) tierIter.next();
			a = t.getAnnotation(id);
			if(a != null) return a;
		}
		return null;
	}
	
	public long getLatestTime() {
		long latestTime = 0;
		
		Enumeration elmts = getTimeOrder().elements();
		while (elmts.hasMoreElements()) {
			long t = ((TimeSlot) elmts.nextElement()).getTime();
			if (t > latestTime) {
				latestTime = t;
			}
		}		
		return latestTime;
	}
	
	
	
	/**
	 * This method returns all child annotations for a given annotation,
	 * irrespective of which tier it is on. There exists an alternative method
	 * Annotation.getChildrenOnTier(). The main difference is, that getChildAnnotationsOf
	 * does not base itself on ParentAnnotationListeners for the case of AlignableAnnotations.
	 * This is essential during deletion of annotations.
	 * THEREFORE: DO NOT REPLACE THIS METHOD WITH getChildrenOnTier. DELETION OF ANNOTATIONS
	 * WILL THEN FAIL !!!
	 * */
	public Vector getChildAnnotationsOf(Annotation theAnnot) {
		Vector children = new Vector();
		//Tier annotsTier = theAnnot.getTier();
		if (theAnnot instanceof RefAnnotation) {
			children.addAll(((RefAnnotation) theAnnot).getParentListeners());
		}
		else {	// theAnnot is AlignableAnnotation

			// HB, 6-5-03, to take closed annotation graphs into account

			TreeSet connectedAnnots = new TreeSet();
			//TreeSet connectedTimeSlots = new TreeSet();
		//	getConnectedAnnots(connectedAnnots, connectedTimeSlots, ((AlignableAnnotation) theAnnot).getBegin());
			// HS mar 06: pass the top tier as well, to prevent iterations over unrelated tiers
			getConnectedSubtree(connectedAnnots, 
								((AlignableAnnotation) theAnnot).getBegin(),
								((AlignableAnnotation) theAnnot).getEnd(),
								theAnnot.getTier());

			Vector connAnnotVector = new Vector(connectedAnnots);	// 'contains' on TreeSet seems to go wrong in rare cases
																	// I don't understand this, but it works - HB
			
			// <<< end of HB, 6-5-03

			// first collect direct descendant tiers of theAnnot's tier
			Vector descTiers = new Vector();

			Iterator tierIter = tiers.iterator();
			while (tierIter.hasNext()) {
				TierImpl t = (TierImpl) tierIter.next();
				//if (t.getParentTier() == theAnnot.getTier()) {
				if (t.hasAncestor(theAnnot.getTier())) {
					descTiers.add(t);
				}
			}

			// on these descendant tiers, check all annots if they have theAnnot as parent
			Iterator descTierIter = descTiers.iterator();
			while (descTierIter.hasNext()) {
				TierImpl descT = (TierImpl) descTierIter.next();

				Iterator annIter = descT.getAnnotations().iterator();
				while (annIter.hasNext()) {
					Annotation a = (Annotation) annIter.next();
					if (a instanceof RefAnnotation) {
						// HS 29 jul 04: added test on the size of the Vector to prevent 
						// NoSuchElementException
						if (((RefAnnotation) a).getReferences().size() > 0 && 
							((RefAnnotation) a).getReferences().firstElement() == theAnnot) {
							children.add(a);
						}
					}
					else if (a instanceof AlignableAnnotation) {												
						if (connAnnotVector.contains(a)) {
							children.add(a);
						} else if (descT.getLinguisticType().getConstraints().getStereoType() == 
						    Constraint.INCLUDED_IN) {
						    // feb 2006: special case for Included_In tier, child annotations are not 
						    // found in the graph tree, test overlap, or more precise inclusion
						    /*
						    if (a.getBeginTimeBoundary() >= theAnnot.getBeginTimeBoundary() &&  
						            a.getEndTimeBoundary() <= theAnnot.getEndTimeBoundary()) {
						        children.add(a);
						    }
						    */
						    // return overlapping instead of included annotations
						    if (a.getBeginTimeBoundary() < theAnnot.getEndTimeBoundary() &&  
						            a.getEndTimeBoundary() > theAnnot.getBeginTimeBoundary()) {
						        children.add(a);
						    }
						    if (a.getBeginTimeBoundary() > theAnnot.getEndTimeBoundary()) {
						        break;
						    }
						}
					}
				}
			}
		}

		return children;
	}

	/**
	 * Annotation based variant of the graph based getConnectedAnnots(TreeSet, TreeSet, TimeSlot).
	 * Annotations on "Included_In" tiers are not discovered in the graph based way.
	 * 
	 * @param connectedAnnots storage for annotations found
	 * @param connectedTimeSlots storage for slots found
	 * @param fromAnn the parent annotation
	 */	
	public void getConnectedAnnots(TreeSet connectedAnnots, TreeSet connectedTimeSlots, 
	        AlignableAnnotation fromAnn) {
	    // first get the annotations and slots, graph based
	    getConnectedAnnots(connectedAnnots, connectedTimeSlots, fromAnn.getBegin());
	    // then find Included_In dependent tiers
	    TierImpl t = (TierImpl) fromAnn.getTier();
	    Vector depTiers = t.getDependentTiers();
	    Iterator dtIt = depTiers.iterator();
	    TierImpl tt;
	    Vector anns;
	    while (dtIt.hasNext()) {
	        tt = (TierImpl) dtIt.next();
	        if (tt.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN) {
	            //anns = tt.getOverlappingAnnotations(fromAnn.getBeginTimeBoundary(), fromAnn.getEndTimeBoundary());
	            anns = tt.getAnnotations();
	            AlignableAnnotation aa;;
	            Iterator anIt = anns.iterator(); // iterations over all annotations on the tier...
	            while (anIt.hasNext()) {
	                aa = (AlignableAnnotation) anIt.next();
	                if (fromAnn.isAncestorOf(aa)) {
	                    getConnectedAnnots(connectedAnnots, connectedTimeSlots, 
	        	                       aa);
	                }
	                //getConnectedAnnots(connectedAnnots, connectedTimeSlots, 
	                //        ((AlignableAnnotation) anIt.next()).getBegin());
	            }
	        }
	        
	    }
	}
	
	public void getConnectedAnnots(TreeSet connectedAnnots, TreeSet connectedTimeSlots, TimeSlot startingFromTimeSlot) {

		Vector annots = null;
		connectedTimeSlots.add(startingFromTimeSlot);

		// annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot);
		annots = getAnnotationsUsingTimeSlot(startingFromTimeSlot);

		if (annots != null) {
			//connectedAnnots.addAll(annots);
			
			Iterator aIter = annots.iterator();
			while (aIter.hasNext()) {
				AlignableAnnotation aa = (AlignableAnnotation) aIter.next();
				boolean added = connectedAnnots.add(aa);
				if (!added) {
				    continue;
				}

				if (!connectedTimeSlots.contains(aa.getBegin())) {
					getConnectedAnnots(connectedAnnots, connectedTimeSlots, aa.getBegin());
				}
				if (!connectedTimeSlots.contains(aa.getEnd())) {
					getConnectedAnnots(connectedAnnots, connectedTimeSlots, aa.getEnd());
				}
			}
		}
	}

	/**
	 * Find all annotations part of the (sub) graph, time slot based.
	 * HS mar 06: the top level tier for the search can be specified to prevent 
	 * iterations over unrelated tiers.
	 * 
	 * @param connectedAnnots TreeSet to add found annotations to (in/out)
	 * @param startingFromTimeSlot begin time slot of the subtree
	 * @param stopTimeSlot end time slot of the sub tree
	 * @param topTier the top level tier for the subtree search (can be a dependent tier though)
	 * @return true when the end time slot has been reached, false otherwise
	 */
	public boolean getConnectedSubtree(TreeSet connectedAnnots, 
									TimeSlot startingFromTimeSlot,
									TimeSlot stopTimeSlot,
									Tier topTier) {
		Vector annots = null;
		boolean endFound = false;
		
		if (topTier == null) {
		    annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot); 
		} else {
		    annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot, topTier, true);
		}
		

		if (annots != null) {
			Iterator aIter = annots.iterator();
			while (aIter.hasNext()) {
				AlignableAnnotation aa = (AlignableAnnotation) aIter.next();

				if (aa.getEnd() != stopTimeSlot) {
					//endFound = getConnectedSubtree(connectedAnnots, aa.getEnd(), stopTimeSlot, topTier);
				    endFound = getConnectedSubtree(connectedAnnots, aa.getEnd(), stopTimeSlot, aa.getTier());
					if (endFound) {
						connectedAnnots.add(aa);
					}
				}
				else {
					endFound = true;
					connectedAnnots.add(aa);
				}
			}
		}
		
		return endFound;
	}


	public boolean eachRootHasSubdiv() {
		boolean eachRootHasSubdiv = true;

			Vector topTiers = getTopTiers();
			Iterator tierIter = topTiers.iterator();

			while (tierIter.hasNext()) {
				TierImpl t = (TierImpl) tierIter.next();

				Vector annots = t.getAnnotations();
				Iterator aIter = annots.iterator();
				while (aIter.hasNext()) {
					AbstractAnnotation a = (AbstractAnnotation) aIter.next();

					ArrayList children = a.getParentListeners();
					if ((children == null) || (children.size() == 0)) {
						return false;
					}
					else {
						boolean subdivFound = false;
						Iterator childIter = children.iterator();
						while (childIter.hasNext()) {
							AbstractAnnotation ch = (AbstractAnnotation) childIter.next();
							Constraint constraint = ((TierImpl) ch.getTier()).getLinguisticType().getConstraints();

							if (	(constraint.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) ||
								(constraint.getStereoType() == Constraint.TIME_SUBDIVISION)) {

								subdivFound = true;
								break;
							}
						}

						if (!subdivFound) {
							return false;
						}
					}

				}
			}


		return eachRootHasSubdiv;
	}


	/**
	 * Returns whether any modifications are made since the last reset (when saving)
	 */
	public boolean isChanged() {
		return changed;
	}


	/**
	 * Resets 'changed' status to unchanged.
	 * Dec 2009 HS also set the Controlled Vocabularies to unchanged
	 */
	public void setUnchanged() {
		changed = false;
		
		if (controlledVocabularies != null) {
			ControlledVocabulary cv;
			for (int i = 0; i < controlledVocabularies.size(); i++) {
				cv = (ControlledVocabulary) controlledVocabularies.get(i);
				cv.setChanged(false);
			}
		}
	}


	/**
	 * Sets 'changed' status to changed
	 */
	public void setChanged() {
		changed = true;
	}

	/**
	 * Returns time Change Propagation Mode (normal, bulldozer or shift)
	 * 
	 * @author hennie
	 */
	public int getTimeChangePropagationMode() {
		return timeChangePropagationMode;
	}
	
	/**
	 * Set Time Change Propagation Mode (normal, bulldozer or shift)
	 * @author hennie
	 */
	public void setTimeChangePropagationMode(int theMode) {
		timeChangePropagationMode = theMode;
	}
	
	/**
	 * Propagate time changes by shifting all next annotations to later times,
	 * maintaining gaps. Algorithm: shift all time slots after end of fixedAnnotation
	 * over distance newEnd minus oldEnd.
	 * 
	 * @param fixedAnnotation the source annotation 
	 * @param fixedSlots slots connected to the source annotation that should not be shifted
	 * @param oldBegin the old begin time of the annotation
	 * @param oldEnd the old end time of the annotation
	 */
	public void correctOverlapsByShifting(AlignableAnnotation fixedAnnotation, Vector fixedSlots, 
		long oldBegin, long oldEnd) {
		long newEnd = fixedAnnotation.getEnd().getTime();
		
		// right shift
		if (newEnd > oldEnd) {	// implies that end is time aligned
			long shift = newEnd - oldEnd;
			
			Vector otherFixedSLots = getOtherFixedSlots(oldBegin , fixedAnnotation.getTier().getName());
			if(otherFixedSLots != null){
				for(int i=0; i <otherFixedSLots.size(); i++){
					if(!fixedSlots.contains(otherFixedSLots.get(i))){
						fixedSlots.add(otherFixedSLots.get(i));
					}
				}
				
			}
			getTimeOrder().shift(oldBegin, shift, fixedAnnotation.getEnd(), fixedSlots);
		}
	}
	
	private Vector getOtherFixedSlots(long from, String excludeTimeSLotsFromTier){
		Vector tiers = this.getTiers();
		Vector childTiers;
		Vector childAnnotations;
		Iterator iter;
		TierImpl tier;
		TierImpl dependTier;
		AlignableAnnotation ann;
		AlignableAnnotation childAnn;
		Vector fixedSlots = null;
		for(int t=0; t<tiers.size(); t++){
			tier = (TierImpl) tiers.get(t);
			childTiers = tier.getChildTiers();
			if(tier.getLinguisticType().getConstraints() == null){
				if(excludeTimeSLotsFromTier != null && tier.getName().equals(excludeTimeSLotsFromTier)){
					continue;
				}
				iter = tier.getAnnotations().iterator();				
				while (iter.hasNext()) {
					ann = (AlignableAnnotation) iter.next();
					if(ann.getBeginTimeBoundary() < from && ann.getEndTimeBoundary() > from){
						if(fixedSlots == null){
							fixedSlots = new Vector();
						}
						fixedSlots.add(ann.endTime);
						if(childTiers != null){
							for(int i= 0; i< childTiers.size(); i++){
								dependTier = (TierImpl) childTiers.get(i);
								if(dependTier.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION || 
										dependTier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN){
									childAnnotations = ann.getChildrenOnTier((Tier) childTiers.get(i));
									if(childAnnotations != null){
										for(int a = 0; a < childAnnotations.size(); a++){
											childAnn = (AlignableAnnotation) childAnnotations.get(a);
											if(childAnn.getBeginTimeBoundary() > from){
												fixedSlots.add(childAnn.beginTime);
											}
											
											if(childAnn.getEndTimeBoundary() > from){
												fixedSlots.add(childAnn.endTime);
											}
										}
									}
								}
							}
						}
						
					}
				}
			}
		}
		
		return fixedSlots;
		
	}
	
	/**
	 * Propagate time changes by shifting time slots.<br> 
	 * <b>Note: </b> this method is intended only to be used to 
	 * reverse the effects of an earlier call to <code>correctOverlapsByShifting
	 * </code>, as in an undo operation!
	 * 
	 * @see #correctOverlapsByShifting(AlignableAnnotation, long, long)
	 * @param from starting point for shifting
	 * @param amount the distance to shift the timeslots
	 */
	public void shiftBackward(long from, long amount) {
		if (amount < 0) {	
			
			
			getTimeOrder().shift(from, amount, null, getOtherFixedSlots(from , null));
			// let listeners know the whole transcription 
			// could be changed
			modified(ACMEditEvent.CHANGE_ANNOTATIONS, null);
		}		
	}
	
	/**
	 * Shifts all aligned timeslots with the specified amount of ms.<br>
	 * When an attempt is made to shift slots such that one or more slots 
	 * would have a negative time value an exception is thrown. 
	 * Slots (and annotations) will never implicitely be deleted.
	 * The shift operation is delegated to th TimeOrder object.
	 * 
	 * @param shiftValue the number of ms to add to the time value of aligned timeslots, 
	 *    can be less than zero
	 * @throws IllegalArgumentException when the shift value is such that any aligned 
	 *    slot would get a negative time value 
	 */
	public void shiftAllAnnotations(long shiftValue) throws IllegalArgumentException {
		timeOrder.shiftAll(shiftValue);
		// notify
		modified(ACMEditEvent.CHANGE_ANNOTATIONS, null); 
	}

	/** Holds value of property DOCUMENT ME! */
	public static final String UNDEFINED_FILE_NAME = "aishug294879ryshfda9763afo8947a5gf";

	protected String fileName;

	protected String mediafileName;

	protected String svgFile;
	
	/**
	     * Returns all Tiers from this Transcription.
	     *
	     * @return DOCUMENT ME!
	     */
	public Vector getTiers() {
		// HS 09-2011 implicit or delayed loading not supported anymore
		/*
	    if (!isLoaded()) {
	        //			(new MinimalTranscriptionStore()).loadTranscription(this, identity);
	        ACMTranscriptionStore.getCurrentTranscriptionStore().loadTranscription(this, null);
	    }
		*/
	    return tiers;
	}


	/**
	     * Check if an Object is equal to this Transcription. For DOBES equality is
	     * true if the names of two Transcriptions are the same.
	     *
	     * @param obj DOCUMENT ME!
	     *
	     * @return DOCUMENT ME!
	     */
	public boolean equals(Object obj) {

	        if (obj instanceof TranscriptionImpl &&
	                (((TranscriptionImpl) obj).getName() == name)) {
	            return true;
	        }
	
	    return false;
	}

	/**
	     * DOCUMENT ME!
	     *
	     * @param identity DOCUMENT ME!
	     */
	/*
	public void loadTags() {
	    loadAnnotations();
	}
	*/

	/**
	     * DOCUMENT ME!
	     *
	     * @return DOCUMENT ME!
	     */
	public String getPathName() {
	    return fileName;
	}

	/**
	     * DOCUMENT ME!
	     *
	     * @param theFileName DOCUMENT ME!
	     */
	public void setPathName(String theFileName) {
	    if (theFileName.startsWith("file:")) {
	        theFileName = theFileName.substring(5);
	    }
	
	    fileName = theFileName;
	    url = pathToURLString(fileName);
	}

	/**
	     * Load the Annotations into the Tiers.
	     *
	     * @param identity DOCUMENT ME!
	     */
	/*
	public void loadAnnotations() {
	    if (!isLoaded()) {
	        ACMTranscriptionStore.getCurrentTranscriptionStore().loadTranscription(this, null);
	    }
	}
	*/

	/**
	     * DOCUMENT ME!
	     *
	     * @param pathName DOCUMENT ME!
	     */
	public void setMainMediaFile(String pathName) {
		// HS feb 05:
		// let the auto detected media file name precede
		if (mediafileName == null) {
			this.mediafileName = pathName;

		    //mediaObject = createMediaObject(mediafileName);
		}
	}

	/**
	  * Sets the path to an svg file containing the information of graphical annotations (2D).
	  * This assumes that there is only one such file. This might need to be changed for cases of 
	  * graphical annotations on multiple videos, but it might also stay the same. 
	  * 2006-11 HS: create a LinkedFileDescriptor for the svg file if it isn't already there. This replaces 
	  * the implicit association based on file name and location.
	  * 
	  * @param fileName the path to an .svg file
	  */
	public void setSVGFile(String fileName) {
	    if (fileName == null) {
	        removeSVGFile();
	        return;
	    }
	    String svgURL = fileName;
		// check if there is already a linked file descriptor for this file
	    if (!svgURL.startsWith("file:")) {
	        svgURL = pathToURLString(svgURL);
	    }
	    svgFile = svgURL;
		LinkedFileDescriptor lfd;
		for (int i = 0; i < linkedFileDescriptors.size(); i++) {
		    lfd = (LinkedFileDescriptor) linkedFileDescriptors.get(i);
		    if (lfd.linkURL.equals(svgURL)) {
		        return;
		    }
		}
		// if it is not there, remove any existing one 
		for (int i = 0; i < linkedFileDescriptors.size(); i++) {
		    lfd = (LinkedFileDescriptor) linkedFileDescriptors.get(i);
		    if (lfd.mimeType.equals(LinkedFileDescriptor.SVG_TYPE)) {
		    		linkedFileDescriptors.remove(i);
		        break;
		    }
		}
		// create a new one 
		lfd = new LinkedFileDescriptor(svgURL, LinkedFileDescriptor.SVG_TYPE);
		linkedFileDescriptors.add(lfd);
		
		MediaDescriptor mmd;
		// create association based on file name
		for (int i = 0; i < mediaDescriptors.size(); i++) {
		    mmd = (MediaDescriptor) mediaDescriptors.get(i);
		    if (mmd.mediaURL.substring(0, mmd.mediaURL.lastIndexOf('.')).equals(
		            svgURL.substring(0, svgURL.lastIndexOf('.')))) {
		        if (mmd.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE) ||
		                mmd.mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE) || 
		                mmd.mimeType.equals(MediaDescriptor.GENERIC_VIDEO_TYPE)) {
		            lfd.associatedWith = mmd.mediaURL;
		            break;
		        }
		    }
		}
		if (lfd.associatedWith == null) {
		    // associate with the first video
		    for (int i = 0; i < mediaDescriptors.size(); i++) {
			    mmd = (MediaDescriptor) mediaDescriptors.get(i);
			    if (mmd.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE) ||
			            mmd.mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE) || 
			            mmd.mimeType.equals(MediaDescriptor.GENERIC_VIDEO_TYPE)) {
			        lfd.associatedWith = mmd.mediaURL;
			        break;
			    }
			}
		}
	}

	/**
	     * DOCUMENT ME!
	     *
	     * @return DOCUMENT ME!
	     */
	public String getSVGFile() {
	    return svgFile;
	}
	
	/**
	 * Removes the LinkedFileDescriptor that corresponds to the current svgFile.
	 */
	private void removeSVGFile() {
	    if (svgFile != null) { 
		    LinkedFileDescriptor lfd;
		    for (int i = 0; i < linkedFileDescriptors.size(); i++) {
		        lfd = (LinkedFileDescriptor) linkedFileDescriptors.get(i);
			    if (lfd.linkURL.equals(svgFile)) {
			        linkedFileDescriptors.remove(i);
			        
			        // rename the .svg file
			        String svgFileString = svgFile.substring(5);	        
	                File svgFile = new File(svgFileString);

	                if (svgFile.exists()) {
	                    File renamed = new File(svgFileString + "_old");
	                    try {
	                    	svgFile.renameTo(renamed);
	                    } catch (Exception exc) {
	                    	
	                    }
	                }
			        break;
			    }
		    }
		    svgFile = null;
	    }
	}
	
	/******** support for controlled vocabularies  ******/
	
	/**
	 * Sets the collection of ControlledVocabularies known to this Transcription.
	 * 
	 * @param controlledVocabs the CV's for this transcription
	 */
	public void setControlledVocabularies(Vector controlledVocabs) {
		if (controlledVocabs != null) {
			controlledVocabularies = controlledVocabs;
		}
		//called at parse/construction time, don't call modified 		
	}
	
	/**
	 * Returns the collection of controlled vocabularies known to this Transcription.
	 * If there are no CV's an empty Vector is returned.
	 * 
	 * @return the list of associated cv's
	 */
	public Vector getControlledVocabularies() {
		return controlledVocabularies;
	}
	
	/**
	 * Returns the ControlledVocabulary with the specified name if it exists
	 * in the Transcription or null otherwise.
	 * 
	 * @param name the name of the cv
	 * 
	 * @return the CV with the specified name or <code>null</code>
	 */
	public ControlledVocabulary getControlledVocabulary(String name) {
		if (name == null) {
			return null;	
		}
		ControlledVocabulary conVoc = null;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = (ControlledVocabulary)controlledVocabularies.get(i);
			if (conVoc.getName().equalsIgnoreCase(name)) {
				break;
			} else {
				conVoc = null;
			}
		}
		return conVoc;
	}
	
	/**
	 * Adds the specified CV to the list of cv's if it is not already in the list 
	 * and if there is not already a cv with the same name in the list.
	 * 
	 * @param cv the ControlledVocabulary to add
	 */
	public void addControlledVocabulary(ControlledVocabulary cv) {
		if (cv == null) {
			return;
		}
		ControlledVocabulary conVoc;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = (ControlledVocabulary)controlledVocabularies.get(i);
			if ( conVoc == cv || conVoc.getName().equalsIgnoreCase(cv.getName())) {
				return;
			}
		}
		controlledVocabularies.add(cv);
		// register as a listener for modifications
		// A.K. was never used -> removed 
		cv.setACMEditableObject(this);
		
		if (isLoaded) {		
			modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
		}		
	}
	
	/**
	 * Removes the specified CV from the list.<br>
	 * Any LinguisticTypes that reference the specified ControlledVocabulary will 
	 * have their refernce set to <code>null</code>.
	 * 
	 * @param cv the CV to remove from the list
	 */
	public void removeControlledVocabulary(ControlledVocabulary cv) {
		if (cv == null) {
			return;
		}
		Vector types = getLinguisticTypesWithCV(cv.getName());
		for (int i = 0; i < types.size(); i++) {
			((LinguisticType)types.get(i)).setControlledVocabularyName(null);
		}
		
		cv.removeACMEditableObject();
		controlledVocabularies.remove(cv);

		modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
	}
	
	/**
	 * Updates the name and description of the specified CV and updates the Linguistic 
	 * Types referencing this CV, if any.<br> The contents of a ControlledVocabulary is changed 
	 * directly in an editor; the CV class insures it's integrity. THe ditor class 
	 * should call setChanged on the Transcription object.<br><br>
	 * Pending: The moment updating existing annotation values is offered as an option, 
	 * this implementation has to been changed.
	 * 
	 * @param cv the cv that has been changed or is to be changed.
	 * @param name the new name of the cv
	 * @param description the description for the cv
	 */
	public void changeControlledVocabulary(ControlledVocabulary cv , String name, 
		String description/*, Vector entries*/) {
		boolean newChange = false;
		String oldName = cv.getName();
		String oldDescription = cv.getDescription();
		// doublecheck on the name
		ControlledVocabulary conVoc;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = (ControlledVocabulary)controlledVocabularies.get(i);
			if ( conVoc != cv && conVoc.getName().equalsIgnoreCase(name)) {
				return;
			}
		}
		if (!oldName.equals(name)) {			
			newChange = true;
			Vector types = getLinguisticTypesWithCV(oldName);
			for (int i = 0; i < types.size(); i++) {
				((LinguisticType)types.get(i)).setControlledVocabularyName(name);
			}
			cv.setName(name);
		}
		if (!oldDescription.equals(description)) {
			cv.setDescription(description);
			newChange = true;
		}
		
		if (newChange) {
			modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
		}
	}
	
	/**
	 * To be called after externally defined Controlled Vocabularies have been 
	 * loaded. Checks whether annotations that link to an entry in such a CV 
	 * have to be updated as a result of a change in the CV.
	 */
	public void checkAnnotECVConsistency() {
		Iterator tierIt = tiers.iterator();
		
		while (tierIt.hasNext()) {
			TierImpl currentTier = (TierImpl) tierIt.next();
			String cvName = currentTier.getLinguisticType()
					.getControlledVocabylaryName();
			ControlledVocabulary cv = getControlledVocabulary(cvName);
			
			if (cv instanceof ExternalCV) {
				ExternalCV ecv = (ExternalCV) cv;
				
				if (!ecv.isLoadedFromURL() && !ecv.isLoadedFromCache()) {
					continue;
				}
				Iterator annnotationIt = currentTier.getAnnotations().iterator();
				
				while (annnotationIt.hasNext()) {
					AbstractAnnotation currentAnn = (AbstractAnnotation) annnotationIt
							.next();
					String cvEntryRefId = currentAnn
							.getExtRefValue(ExternalReference.CVE_ID);
					if (cvEntryRefId == null) {
						continue;
					}
					
					CVEntry entry = ecv.getEntrybyId(cvEntryRefId);
					if (entry != null && entry.getValue() != null
							&& !entry.getValue().equals(currentAnn.getValue())) {
						currentAnn.setValue(entry.getValue());
						setChanged();
					} else if (entry == null) {
						// The entry the External Ref. refers to is no longer
						// there, so remove the External Ref. from the annotation
						String value = ((AbstractAnnotation) currentAnn).getExtRefValue(ExternalReference.CVE_ID);
						ExternalReferenceImpl extRef = new ExternalReferenceImpl(
								value, ExternalReference.CVE_ID);
						((AbstractAnnotation) currentAnn).removeExtRef(extRef);
						setChanged();
					}
				}
			}
		}
	} 
	
	/**
	 * Finds the LinguisticTypes that hold a reference to the CV with the
	 * specified name and returns them in a Vector.
	 * 
	 * @param name
	 *            the identifier of the ControlledVocabulary
	 * @return a list of linguistic types
	 */
	public Vector getLinguisticTypesWithCV(String name) {
		Vector matchingTypes = new Vector();
		LinguisticType lt;
		String cvName;
		for (int i = 0; i < linguisticTypes.size(); i++) {
			lt = (LinguisticType)linguisticTypes.get(i);
			cvName = lt.getControlledVocabylaryName();
			if (cvName != null && cvName.equalsIgnoreCase(name)) {
				matchingTypes.add(lt);
			}
		}
		
		return matchingTypes;
	}
	
	/**
	 * Finds the tiers with a linguistic type that references the Controlled Vocabulary
	 * with the specified name.
	 * 
	 * @see #getLinguisticTypes(String)
	 * 
	 * @param name the identifier of the ControlledVocabulary
	 * 
	 * @return a list of all tiers using the specified ControlledVocabulary
	 */
	public Vector getTiersWithCV(String name) {
		Vector matchingTiers = new Vector();
		if (name == null || name.length() == 0) {
			return matchingTiers;
		}
		
		Vector types = getLinguisticTypesWithCV(name);
		if (types.size() > 0) {
			Vector tv;
			LinguisticType type;
			for (int i = 0; i < types.size(); i++) {
				type = (LinguisticType)types.get(i);			
				tv = getTiersWithLinguisticType(type.getLinguisticTypeName());
				if (tv.size() > 0) {
					matchingTiers.addAll(tv);
				}					
			}
		}
		return matchingTiers;
	}
	
	public boolean allRootAnnotsUnaligned() {
		// used when importing unaligned files from for example Shoebox or CHAT		
		
			Vector topTiers = getTopTiers();
			Iterator tierIter = topTiers.iterator();
	
			while (tierIter.hasNext()) {
				TierImpl t = (TierImpl) tierIter.next();
	
				Vector annots = t.getAnnotations();
				Iterator aIter = annots.iterator();
				while (aIter.hasNext()) {
					AlignableAnnotation a = (AlignableAnnotation) aIter.next();	
					if (a.getBegin().isTimeAligned() || a.getEnd().isTimeAligned()) {
						return false;
					}
				}
			}	
		
		return true;
	}
	
	public void alignRootAnnots() {
		Vector rootTimeSlots = new Vector();
		
		// collect timeslots of root annotations
	
			Vector topTiers = getTopTiers();
			Iterator tierIter = topTiers.iterator();
	
			while (tierIter.hasNext()) {
				TierImpl t = (TierImpl) tierIter.next();
	
				Vector annots = t.getAnnotations();
				Iterator aIter = annots.iterator();
				while (aIter.hasNext()) {
					AlignableAnnotation a = (AlignableAnnotation) aIter.next();
					
					rootTimeSlots.add(a.getBegin());
					rootTimeSlots.add(a.getEnd());					
				}
			}		
		
		// align at regular intervals. Assume that slots belong to one
		// annotation pairwise		
		int cnt = 0;
		
		Object[] tsArray = rootTimeSlots.toArray();
		Arrays.sort(tsArray);
		
		for (int i = 0; i < tsArray.length; i++) {
			TimeSlot ts = (TimeSlot) tsArray[i];
			if (i%2 == 0) {
				ts.setTime(1000*cnt++);
			}
			else {
				ts.setTime(1000*cnt);
			}
		}
	}

	/**
	 * MK:02/06/27<br>
	 * A transcription contain tiers withour parent-tiers. 
	 * @return Vector of TierImpl without parent-tier
	 */
	public final Vector getTopTiers() {
	      Vector result = new Vector();
	      for (Enumeration e = getTiers().elements() ; e.hasMoreElements() ;) {
	        Object o = e.nextElement();
	        if (! (o instanceof TierImpl)) continue; 
	        TierImpl t = (TierImpl) o;
	        // the parent tier
	        TierImpl dad = (TierImpl) t.getParentTier();
	        if (dad == null) {
	          result.add(t); 
	        } else {
	          //System.out.println(" -- tierpa " + dad.getName());
	          }
	      }
	    return result;	
	}
	/*
	protected void finalize() throws Throwable {
	    System.out.println("Finalize Transcription...");
	    super.finalize();
	}
	*/
	
	/**
	 * Adds all Property objects in the given list to the list of document properties
	 * if they are not already in that list.
	 * 
	 * @param props a list of property objects to add
	 */
	public void addDocProperties(ArrayList props) {
		if (props != null) {
			Object prop;
			for (int i = 0; i < props.size(); i++) {
				prop = props.get(i);
				if (prop instanceof Property && !docProperties.contains(prop)) {
					docProperties.add(prop);
				}
			}
		}
	}
	
	/**
	 * Adds a single document property object to the list.
	 * 
	 * @param prop the property
	 */
	public void addDocProperty(Property prop) {
		if (prop != null && !docProperties.contains(prop)) {
			docProperties.add(prop);
		}
	}
	
	/**
	 * Removes the specified object from the list.
	 * 
	 * @param prop the Property to remove
	 * @return true if it was in the list, false otherwise
	 */
	public boolean removeDocProperty(Property prop) {
		if (prop != null) {
			return docProperties.remove(prop);
		}
		
		return false;
	}
	
	/**
	 * Returns the ArrayList containing the document properties.
	 * Note: it is not a copy of the list.
	 * 
	 * @return the list of document properties
	 */
	public ArrayList getDocProperties() {
		return docProperties;
	}
	
	/**
	 * Empties the document properties list.
	 */
	public void clearDocProperties() {
		docProperties.clear();
	}

	/******** support for lexicon links ******/
	
	/**
	 * Return the LexiconServiceClientFactories of this transcription
	 * 
	 * @return the lexiconServiceClientFactories of this transcription, can be null!
	 * @author Micha Hulsbosch
	 */
	public HashMap<String, LexiconServiceClientFactory> getLexiconServiceClientFactories() {
		return lexiconServiceClientFactories;
	}
	
	/**
	 * Adds a client factory to the map.
	 * 
	 * @param name name of the service factory
	 * @param fact the service client factory
	 */
	public void addLexiconServiceClientFactory(String name, LexiconServiceClientFactory fact) {
		if (lexiconServiceClientFactories == null) {
			lexiconServiceClientFactories = new HashMap<String, LexiconServiceClientFactory>(6);
		}
		lexiconServiceClientFactories.put(name, fact);
		lexcionServicesLoaded = true; // adding a factory means loading has been performed
	}

	/**
	 * 
	 * @param name the name of the service factory to remove
	 */
	public void removeLexiconServiceClientFactory(String name) {
		if (lexiconServiceClientFactories != null) {
			lexiconServiceClientFactories.remove(name);
		}
	}
	
	/** 
	 * Returns whether client factories for lexicon services have been loaded,
	 * or at least have been attempted to be loaded.
	 * Allows for "lazy" loading of the factories.
	 * 
	 * @return true if loading has been tried
	 */
	public boolean isLexcionServicesLoaded() {
		return lexcionServicesLoaded;
	}
	
	/**
	 * After loading has been performed  (or has been attempted), 
	 * this flag can be set to true.
	 * 
	 * @param lexcionServicesLoaded
	 */
	public void setLexcionServicesLoaded(boolean lexcionServicesLoaded) {
		this.lexcionServicesLoaded = lexcionServicesLoaded;
	}

	/**
	 * Return the Lexicon Links of this transcription
	 * @return the Lexicon Links of this transcription
	 * @author Micha Hulsbosch
	 */
	public HashMap<String, LexiconLink> getLexiconLinks() {
		return lexiconLinks;
	}

	/**
	 * Adds a Lexicon Link to this transcription
	 * @param the Lexicon Link to add
	 * @author Micha Hulsbosch
	 */
	public void addLexiconLink(LexiconLink link) {
		lexiconLinks.put(link.getName(), link);
		if (isLoaded) {		
			modified(ACMEditEvent.ADD_LEXICON_LINK, link);
		}
	}


		/**
		 * Remove a Lexicon Link from this transcription
		 * @param the Lexicon Link
		 * @author Micha Hulsbosch
		 */
		public void removeLexiconLink(LexiconLink link) {
			for(LinguisticType type : (Vector<LinguisticType>) linguisticTypes) {
				if(type.isUsingLexiconQueryBundle() && 
						type.getLexiconQueryBundle().getLink().getName().equals(link.getName())) {
					type.setLexiconQueryBundle(null);
				}
			}
			
			lexiconLinks.remove(link.getName());
			if (isLoaded) {		
				modified(ACMEditEvent.DELETE_LEXICON_LINK, link);
			}
		}

	/**
	 * Returns the Lexicon Link that has a certain name or null if does not exist
	 * @param the name of the Lexicon Link
	 * @return the Lexicon Link that has a certain name or null if does not exist
	 * @author Micha Hulsbosch
	 */
	public LexiconLink getLexiconLink(String linkName) {
		return lexiconLinks.get(linkName);
	}

	/**
	 * Returns the Linguistic Types that use a certain Lexicon Link
	 * @param the name of the Lexicon Link
	 * @return the Linguistic Types that use a certain Lexicon Link
	 * @author Micha Hulsbosch
	 */
	public Vector<LinguisticType> getLinguisticTypesWithLexLink(String name) {
		Vector<LinguisticType> types = new Vector<LinguisticType>();
		for(LinguisticType type : (Vector<LinguisticType>) linguisticTypes) {
			if(type.isUsingLexiconQueryBundle() && 
					type.getLexiconQueryBundle().getLink().getName().equals(name)) {
				types.add(type);
			}
		}
		return types;
	}

}
