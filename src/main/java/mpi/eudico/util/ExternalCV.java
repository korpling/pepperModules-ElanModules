package mpi.eudico.util;

/**
 * A class for an externally defined and stored controlled vocabulary, 
 * i.e. not part of the annotation document.
 *
 */
public class ExternalCV extends ControlledVocabulary {

    private Object externalRef;
    private boolean isLoadedFromURL;
    private boolean isLoadedFromCache;

	
	public ExternalCV(String name) {
		super(name);
	}

	public ExternalCV(ControlledVocabulary cv) {
		super(cv.getName());
		this.setDescription(cv.getDescription());
		addAll(cv.getEntries());
	}
	
//	public ExternalCV(String cvName, String extRefId) {
//		this(cvName);
//		setExternalRef(extRefId);
//	}

	public Object getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(Object externalRef) {
		this.externalRef = externalRef;
	}
	
	
	protected void handleModified() {
		// do nothing, external CV's are not editable
	}

	/**
	 * Returns the entry with the given id.
	 * 
	 * @param entryId
	 * @return the CVEntry or null
	 */
	public CVEntry getEntrybyId(String entryId) {
		if (entryId == null) {
			return null;
		}
		
		ExternalCVEntry iterCVE;
		for (int i = 0; i < entries.size(); i++) {
			iterCVE = (ExternalCVEntry) entries.get(i);
			if (entryId.equals(iterCVE.getId())) {
				return iterCVE;
			}
		}
		
		return null;
	}

	public boolean isLoadedFromURL() {
		return isLoadedFromURL;
	}

	public void setLoadedFromURL(boolean isLoadedFromURL) {
		this.isLoadedFromURL = isLoadedFromURL;
	}

	public boolean isLoadedFromCache() {
		return isLoadedFromCache;
	}

	public void setLoadedFromCache(boolean isLoadedFromCache) {
		this.isLoadedFromCache = isLoadedFromCache;
	}
	
//	public ArrayList getSuggestions(String input) {
//		ArrayList suggestions = new ArrayList();
//		for(String entry: getEntryValues()) {
//			if(entry.startsWith(input)) {
//				suggestions.add(entry);
//			}
//		}
//		return suggestions;
//	}
}
