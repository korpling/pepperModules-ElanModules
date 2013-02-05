package mpi.eudico.server.corpora.clomimpl.flex;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TierRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;


/**
 * A parser for FLEx files.
 *
 * @author Han Sloetjes
 */
public class FlexParser extends Parser {
    //private boolean verbose = false;
    private boolean parsed = false;
    private XMLReader reader;

    private ContainerElem topElement;
    private List<String> languages;
    
    private FlexDecoderInfo decoder;
	/** stores tiername - tierrecord pairs */
    private final HashMap<String, TierRecord> tierMap = new HashMap<String, TierRecord>();

    /** a map with tiername - ArrayList with Annotation Records pairs */
    //private final HashMap<String, List> tiers = new HashMap<String, List>();
    
    private HashMap<String, LingTypeRecord> lingTypeRecords = new HashMap<String, LingTypeRecord>(); // LingTypeRecord objects
    private TreeSet<String> tierNameSet = new TreeSet<String>();
    //private Map<String, String> tierToTypeMap = new HashMap<String, String>();
    //private HashMap<String, String> parentHash = new HashMap<String, String>();
    private ArrayList<long[]> timeOrder = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<long[]> timeSlots = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<AnnotationRecord> annotationRecords = new ArrayList<AnnotationRecord>();
    private HashMap<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();
    private HashMap<String, ArrayList<AnnotationRecord>> tierNameToAnnRecordMap = new HashMap<String, ArrayList<AnnotationRecord>>();
    // add a way to ensure that there is always a parenttier-per-level so that an empty
    // annotation can be created if a potential child annotation is there but no parent
    private HashMap<String, String> parentPerLevel = new HashMap<String, String>(8);
    // while parsing, store all combinations of item type and language
    private HashMap<String, LinkedHashSet<String>> typeLangPerLevel = new HashMap<String, LinkedHashSet<String>>(8);
    private List<String> unitLevels;
    // maintain a mapping of guid-id, in order to be able to reconstruct the annotation order
    private HashMap<String, String> guidIdMap = new HashMap<String, String>();
    // a few element counters
    private int parCount = 0;
    private int phraseCount = 0;
    private int wordCount = 0;
    private int morphCount = 0;
    
    private int itDur = 0;
    private int parDur = 0;
    private int phraseDur = 0;
    private int wordDur = 0;
    private int morphDur = 0;
    private int unitDur = 1;
    
    private int annotId = 1;
    private int tsId = 1;
    private final static String ANN_ID_PREFIX = "ann";
    private final static String TS_ID_PREFIX = "ts";
    private final String DEL = "-"; 
    
    /**
     * Creates a new FlexParser instance
     */
    public FlexParser() {
    	unitLevels = new ArrayList<String> (5);
    	unitLevels.add(FlexConstants.MORPH);
    	unitLevels.add(FlexConstants.WORD);
    	unitLevels.add(FlexConstants.PHRASE);
    	unitLevels.add(FlexConstants.PARAGR);
    	unitLevels.add(FlexConstants.IT);
        languages = new ArrayList<String>(5);

        try {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/validation", true);

            reader.setContentHandler(new FlexContentHandler());
        } catch (SAXException se) {
            se.printStackTrace();
        } /* catch (IOException ioe) {
           ioe.printStackTrace();
           }*/
    }

    /**
     * Sets the decoder info object, containing user provided information
     * for the parser.
     */
    @Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
		if (decoderInfo instanceof FlexDecoderInfo) {
			decoder = (FlexDecoderInfo) decoderInfo;
			if (!decoder.totalDurationSpecified) {
				calculateDurations();
			}
			// check validity of smallest time-aligned element 
			if (decoder.smallestWithTimeAlignment == FlexConstants.IT && !decoder.inclITElement) {
				if (decoder.inclParagraphElement) {
					decoder.smallestWithTimeAlignment = FlexConstants.PARAGR;
				} else {
					decoder.smallestWithTimeAlignment = FlexConstants.PHRASE;	
				}
			}
			if (decoder.smallestWithTimeAlignment == FlexConstants.PARAGR && !decoder.inclParagraphElement) {
				decoder.smallestWithTimeAlignment = FlexConstants.PHRASE;
			}
		}
	}


	/**
     * Returns a list of AnnotationRecords for the given tier.
     *
     * @param tierName the name of the tier
     * @param fileName the file name (for historic reasons)
     *
     * @return a list of AnnotationRecords
     */
    @Override
    public ArrayList getAnnotationsOf(String tierName, String fileName) {
    	parse(fileName);
    	
        ArrayList<AnnotationRecord> records = tierNameToAnnRecordMap.get(tierName);

        if (records == null) {
            records = new ArrayList<AnnotationRecord>(0);
        }

        return records;
    }

    /**
     * Returns the name of the linguistic type for the specified tier.
     *
     * @param tierName name of the tier
     * @param fileName the file name
     *
     * @return the name of the linguistic type
     */
    @Override
    public String getLinguisticTypeIDOf(String tierName, String fileName) {
    	parse(fileName);
    	
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		return tr.getLinguisticType();
    	}	

        return null;
    }

    /**
     * Returns a list of linguistic type records.
     *
     * @param fileName the file name
     *
     * @return a list of linguistic type records
     */
    @Override
    public ArrayList getLinguisticTypes(String fileName) {
    	parse(fileName);
    	
    	return new ArrayList(lingTypeRecords.values());
    }

    /**
     * Returns a list of media descriptors, if available
     *
     * @param fileName the file name
     *
     * @return a list of media descriptors
     */
    @Override
    public ArrayList getMediaDescriptors(String fileName) {
    	parse(fileName);
    	
    	if (decoder.getMediaDescriptors() instanceof ArrayList) {
    		return (ArrayList)decoder.getMediaDescriptors();
    	} else if (decoder.getMediaDescriptors() != null) {
    		return new ArrayList(decoder.getMediaDescriptors());
    	}
    	
    	return new ArrayList(0);
    }

    /**
     * Returns the parent tier name for the specified tier
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the parent tier name
     */
    @Override
    public String getParentNameOf(String tierName, String fileName) {
    	parse(fileName);
    	
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		return tr.getParentTier();
    	}
    	
        return null;
    }

    /**
     * Returns the participant part of the tier name, if any
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the participant
     */
    @Override
    public String getParticipantOf(String tierName, String fileName) {
    	parse(fileName);
        return null;
    }

    /**
     * Creates a list of tiernames.  Assumes
     * that this method is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of tiernames
     */
    @Override
    public ArrayList getTierNames(String fileName) {
    	parse(fileName);
        return new ArrayList(tierNameSet);
    }

    /**
     * Returns list of Strings in format "ts" + nnn.  Assumes that this method
     * is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of time slot id's
     */
    @Override
    public ArrayList getTimeOrder(String fileName) {
    	parse(fileName);
    	
        ArrayList<String> resultTimeOrder = new ArrayList<String>();

        for (int i = 0; i < timeOrder.size(); i++) {
            resultTimeOrder.add(TS_ID_PREFIX +
                ((long[]) (timeOrder.get(i)))[0]);
        }

        return resultTimeOrder;
    }

    /**
     * Returns a map of time slot id's to time values, all as strings. This is
     * not the most effective solution, but adheres to the Parser calls.
     * Assumes that this method is only called once.
     *
     * @param fileName the parsed file
     *
     * @return mappings of time slot id's to time values
     */
    @Override
    public HashMap getTimeSlots(String fileName) {
    	parse(fileName);

        HashMap<String, String> resultSlots = new HashMap<String, String>();

        Iterator timeSlotIter = timeSlots.iterator();
        String tsId;
        String timeValue;

        while (timeSlotIter.hasNext()) {
            long[] timeSlot = (long[]) timeSlotIter.next();
            tsId = TS_ID_PREFIX + ((long) timeSlot[0]);
            timeValue = Long.toString(((long) timeSlot[1]));

            resultSlots.put(tsId, timeValue);
        }

        return resultSlots;       
    }

    /**
     * 
     */
    @Override
	public Locale getDefaultLanguageOf(String tierName, String fileName) {
    	parse(fileName);
		
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null && tr.getDefaultLocale() != null) {
    		// could/should check the length of the language string?
    		return new Locale(tr.getDefaultLocale());
    	}
    	// use "en" if no other specified. otherwise possible ID conflict with the "en-US" default of a tier.
        return new Locale("en");
	}

	private void parse(String fileName) {
        if (parsed) {
            return;
        }
        if (decoder == null) {
        	// create one with default values
        	setDecoderInfo(new FlexDecoderInfo());
        	//decoder = new FlexDecoderInfo();
        }
        
        languages.clear();
        topElement = null;
        parentPerLevel.clear();
        
        try {
            reader.parse(fileName);
            parsed = true;
        	createLingTypes();
			if (decoder.totalDurationSpecified) {
				calculateDurations();
				updateElementTimes(topElement);
			}
			if (decoder.getStoredAlignment() != null) {
				restoreAlignment(topElement);
				resolveAlignmentConflicts(topElement);
				/*
				long[] boundaries = new long[]{0, Long.MAX_VALUE};
				long[] topB = decoder.getStoredAlignment().get(topElement.id);
				long oldDur = -1L;
				long shift = 0L;
				if (topB != null) {
					boundaries[0] = topB[0];
					// forget about the end time for now
					oldDur = boundaries[1];
					shift = topB[0];
				}

				// check topelement...
				if (topElement.getChildElems() != null) {
					long et = 0L;
					ContainerElem child;
					for (int i = 0; i < topElement.getChildElems().size(); i++) {
						child = topElement.getChildElems().get(i);
						et = child.et > et ? child.et : et;
					}
					if (topElement.et < et || topElement.et == Long.MAX_VALUE) {
						topElement.et = et;
					}
				}
				*/
			}
			checkParentPerLevel();
			preprocessRecords(topElement);
            createRecords();
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());
        }
    }

	/**
	 * Use the first txt per FLEx level or element as the parent for that level,
	 * if not there, just use the first value. 
	 * Could be extended by using a user-preferred language.
	 */
	private void checkParentPerLevel() {
		String key, first, txtstart;
		Iterator<String> levelIt = typeLangPerLevel.keySet().iterator();
		LinkedHashSet<String> values;
		while (levelIt.hasNext()) {
			key = levelIt.next();
			first = null;
			txtstart = key + DEL + FlexConstants.TXT;
			values = typeLangPerLevel.get(key);
			// first try to find a txt type
			boolean found = false;
			String combi;
			int count = 0;
			Iterator<String> valIt = values.iterator();
			while (valIt.hasNext()) {
				combi = valIt.next();
				if (count == 0) {
					first = combi;
					count++;
				}
				if (combi.startsWith(txtstart)) {
					parentPerLevel.put(key, combi);
					found = true;
					break;
				}
			}
			if (!found) {
				parentPerLevel.put(key, first);
			}
		}
		
		if (!parentPerLevel.containsKey(FlexConstants.IT)) {
			parentPerLevel.put(FlexConstants.IT, FlexConstants.IT);
		}
		if (!parentPerLevel.containsKey(FlexConstants.PARAGR)) {
			parentPerLevel.put(FlexConstants.PARAGR, FlexConstants.PARAGR);
		}
		if (!parentPerLevel.containsKey(FlexConstants.PHRASE)) {
			parentPerLevel.put(FlexConstants.PHRASE, FlexConstants.PHRASE);
		}
		if (!parentPerLevel.containsKey(FlexConstants.WORD)) {
			parentPerLevel.put(FlexConstants.WORD, FlexConstants.WORD);
		}
		if (!parentPerLevel.containsKey(FlexConstants.MORPH)) {
			parentPerLevel.put(FlexConstants.MORPH, FlexConstants.MORPH);
		}
	}
	
	/**
	 * Ensures that in every item list the per-level-parent is the first in the list.
	 * Add an empty item in case the per-level-parent is absent. 
	 */
	private void preprocessRecords(ContainerElem elem) {
    	if (elem == null) {
    		return;
    	}
    	Item item;
		boolean parentFound = false;
		
    	if (elem.getItems() != null && elem.getItems().size() != 0) {
			for (int i = 0; i < elem.getItems().size(); i++) {
				item = elem.getItems().get(i);
				if (item.tierName != null) {
					if (item.tierName.equals(parentPerLevel.get(elem.flexType))) {
						parentFound = true;
						if (i != 0) {
							elem.getItems().add(0, elem.getItems().remove(i));
						}
						break;
					}
				}				
			}
			
			if (!parentFound) {
				Item empty = new Item();
				empty.tierName = parentPerLevel.get(elem.flexType);
				elem.getItems().add(0, empty);
			}
    	} else {
    		String ppl = parentPerLevel.get(elem.flexType);
    		if (ppl == null) {
    			// add an element-only tier
    			ppl = elem.flexType;
    			parentPerLevel.put(elem.flexType, ppl);
    		}
    		Item empty = new Item();
			empty.tierName = ppl;
			elem.addItem(empty);
    	}
    	if (elem.getChildElems() != null && elem.getChildElems().size() > 0) {
    		for (ContainerElem celem : elem.getChildElems()) {
    			preprocessRecords(celem);
    		}
    	}
	}
	
    /**
     * Converts the stored container elements and items to tier records 
     * and annotation records.
     *
     */
    private void createRecords() {
    	if (topElement == null) {
    		return;
    	}
    	// the top element is always "interlinear-text"
    	AnnotationRecord par = null;
    	String topLevelTierName = null;
    	if (decoder.inclITElement) {
    		par = null;
    		
    		// calculate total time based on settings and/or element counts
    		if (topElement.getItems() != null && topElement.getItems().size() > 0) {
    			Item item;
    			String tName;
    			for (int i = 0; i < topElement.getItems().size(); i++) {
    				item = topElement.getItems().get(i);
    				tName = item.tierName;
//    				tName = FlexConstants.IT + "-" + item.type;
//    				if (item.lang != null) {
//    					tName = tName + "-" + item.lang;
//    				}
    				tierNameSet.add(tName);
    				TierRecord tr = new TierRecord();
    				tr.setName(tName);
    				tr.setDefaultLocale(item.lang);
    				tierMap.put(tName, tr);
    				
    				if (i == 0) {// the first item is always preprocessed 
        				tr.setLinguisticType(FlexConstants.TXT);
        				topLevelTierName = tName;
    					par = createAnnotationRecord(tName, null, null, topElement.bt, topElement.et);
    					par.setValue(item.value);
    					if (topElement.id != null) {
    						// store guid-id mapping
    						guidIdMap.put(topElement.id, par.getAnnotationId());
    						par.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + topElement.id);
    					}
    					//parentPerLevel.put(FlexConstants.IT, tName);
    				} else {
        				tr.setLinguisticType(FlexConstants.GLS);
        				tr.setParentTier(topLevelTierName);
	    				AnnotationRecord child = createRefAnnotationRecord(tName, par, null);
	    				child.setValue(item.value);
    				}
    			}
    		} else {// shouldn't happen anymore 
        		tierNameSet.add(FlexConstants.IT);
        		TierRecord tr = new TierRecord();
        		tr.setLinguisticType(FlexConstants.TXT);
        		tr.setName(FlexConstants.IT);
        		tierMap.put(FlexConstants.IT, tr);
        		//parentPerLevel.put(FlexConstants.IT, FlexConstants.IT);
        		// creating an empty annotation     		
    			par = createAnnotationRecord(FlexConstants.IT, null, null, topElement.bt, topElement.et);
    			//par.setValue("");
    		}
    		
    		//create child tiers
    		createChildRecords(topElement, par);
    	} else {//otherwise the direct children are the top elements
    		List<ContainerElem> childElems = topElement.getChildElems();
    		
    		if (childElems != null && childElems.size() > 0) {
    			ContainerElem elem;
    			par = null;
    			Item item;
    			String tName;
    			//StringBuilder tierNameBuf = new StringBuilder(25);
    			
    			for (int i = 0; i < childElems.size(); i++) {
    				elem = childElems.get(i);
    				
    				if (elem.getItems() != null && elem.getItems().size() > 0) {
        				
    					for (int j = 0; j < elem.getItems().size(); j++) {
            				//tierNameBuf.delete(0, tierNameBuf.length());
            				//tierNameBuf.append(elem.flexType);
    						item = elem.getItems().get(j);
    						//tierNameBuf.append("-");
    						//tierNameBuf.append(item.type);
    						//if (item.lang != null) {
    						//	tierNameBuf.append("-");
    						//	tierNameBuf.append(item.lang);
    						//}
    						//tName = tierNameBuf.toString();
    						tName = item.tierName;
    						tierNameSet.add(tName);
    						if (tierMap.get(tName) == null) {
	    			    		TierRecord tr = new TierRecord();   			    		
	    			    		tr.setName(tName);
	    			    		tr.setDefaultLocale(item.lang);
	    			    		tierMap.put(tName, tr);
	    			    		if (j == 0) {
	    			    			tr.setLinguisticType(FlexConstants.TXT);
	    			    		} else {
	    	    					tr.setLinguisticType(FlexConstants.GLS);
	    	    					tr.setParentTier(topLevelTierName);
	    			    		}
    						}
    	    				if (j == 0) {// hier...
    	    					//tr.setLinguisticType(FlexConstants.TXT);
    	    					topLevelTierName = tName;
    	    					par = createAnnotationRecord(tName, null, null, elem.bt, elem.et);
    	    					par.setValue(item.value);
    	    					if (elem.id != null) {
    	    						guidIdMap.put(elem.id, par.getAnnotationId());
    	    						par.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
    	    					}
    	    				} else {
    	    					//tr.setLinguisticType(FlexConstants.GLS);
    	    					//tr.setParentTier(topLevelTierName);
    		    				AnnotationRecord child = createRefAnnotationRecord(tName, par, null);
    		    				child.setValue(item.value);
    	    				}
    					}
    				} else {// should not happen
    					tName = elem.flexType;
						tierNameSet.add(tName);
						if (tierMap.get(tName) == null) {
				    		TierRecord tr = new TierRecord();
				    		tr.setLinguisticType(FlexConstants.TXT);
				    		tr.setName(tName);
				    		tierMap.put(tName, tr);
						}
    					par = createAnnotationRecord(tName, null/*par*/, null, elem.bt, elem.et);
    					//par.setValue("");
    					if (elem.id != null) {
    						guidIdMap.put(elem.id, par.getAnnotationId());
    						par.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
    					}
    				}
    				createChildRecords(elem, par);
    			}
    		}
    	}
    }
    
    /**
     * Creates tier and annotation records for the children of the specified element.
     * The passed element has already been processed.
     * 
     * @param parElem the parent Element
     * @param parentAnn the parent annotation to add direct children to
     */
    private void createChildRecords(ContainerElem parElem, AnnotationRecord parentAnn) {
    	if (parElem.getChildElems() == null || parElem.getChildElems().size() == 0) {
    		return;
    	}
		ContainerElem elem;
		AnnotationRecord nextPar = null;
		AnnotationRecord prevAnn = null;
		Item item;
		String tName;
		String firstItemTierName = null;
		boolean recordCreated = false;
		//StringBuilder tierNameBuf = new StringBuilder(25);
    	
		for (int i = 0; i < parElem.getChildElems().size(); i++) {
			elem = parElem.getChildElems().get(i);
			
			if (elem.getItems() != null && elem.getItems().size() > 0) {
				for (int j = 0; j < elem.getItems().size(); j++) {
					recordCreated = false;
					//tierNameBuf.delete(0, tierNameBuf.length());
					//tierNameBuf.append(elem.flexType);
					
					item = elem.getItems().get(j);
					//tierNameBuf.append("-");
					//tierNameBuf.append(item.type);
					//if (item.lang != null) {
					//	tierNameBuf.append("-");
					//	tierNameBuf.append(item.lang);
					//}
					//tName = tierNameBuf.toString();
					tName = item.tierName;
					// check the top-tier-per-level
					
					if (j == 0) {
						firstItemTierName = tName;
						if (!tierNameSet.contains(tName)) {
							tierNameSet.add(tName);
				    		TierRecord tr = new TierRecord();
				    		tr.setLinguisticType(elem.flexType);
				    		tr.setName(tName);
				    		tr.setParentTier(annotRecordToTierMap.get(parentAnn));
				    		tierMap.put(tName, tr);
						}
			    		if (isAlignable(elem.flexType)) {
			    			nextPar = createAnnotationRecord(tName, parentAnn, prevAnn, elem.bt, elem.et);
			    			if (elem.id != null) {
			    				guidIdMap.put(elem.id, nextPar.getAnnotationId());
			    				nextPar.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
			    			}
			    		} else {
			    			nextPar = createRefAnnotationRecord(tName, parentAnn, prevAnn);
			    		}
			    		nextPar.setValue(item.value);
			    		recordCreated = true;
			    		//nextPar.setValue("");
			    		prevAnn = nextPar;
					} else {
						if (!tierNameSet.contains(tName)) {
							tierNameSet.add(tName);
				    		TierRecord tr = new TierRecord();			    		
				    		tr.setName(tName);
				    		tr.setDefaultLocale(item.lang);
				    		tr.setLinguisticType(FlexConstants.GLS);
				    		tr.setParentTier(firstItemTierName);
				    		tierMap.put(tName, tr);
						}
    					AnnotationRecord child = createRefAnnotationRecord(tName, nextPar, null);
	    				child.setValue(item.value);
					}
					/*
    				if (j == 0) {// hier...
    					if (!parentPerLevel.containsKey(elem.flexType)) {
    						if (elem.flexType.equals(FlexConstants.PARAGR) || elem.flexType.equals(FlexConstants.PHRASE)) {
    							parentPerLevel.put(elem.flexType, tName);
    						} else if (elem.flexType.equals(FlexConstants.WORD) || elem.flexType.equals(FlexConstants.MORPH)) {
    							if (tName.indexOf("-" + FlexConstants.TXT) > -1) {
    								parentPerLevel.put(elem.flexType, tName);
    							} else {
    								// create a generic top-tier-per-level
    								parentPerLevel.put(elem.flexType, (elem.flexType + "-" + FlexConstants.TXT));
    								
    								// add to tiermap and create an empty parent annotation
    								String nextTierName = parentPerLevel.get(elem.flexType);
    								tierNameSet.add(nextTierName);
    					    		TierRecord tr = new TierRecord();
    					    		tr.setLinguisticType(elem.flexType);
    					    		tr.setName(nextTierName);
    					    		tr.setParentTier(annotRecordToTierMap.get(parentAnn));
    					    		tierMap.put(nextTierName, tr);
    					    		
    					    		if (isAlignable(elem.flexType)) {
    					    			nextPar = createAnnotationRecord(nextTierName, parentAnn, null, elem.bt, elem.et);
    					    			if (elem.id != null) {
    					    				guidIdMap.put(elem.id, nextPar.getAnnotationId());
    					    				nextPar.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
    					    			}
    					    		} else {
    					    			nextPar = createRefAnnotationRecord(nextTierName, parentAnn, null);
    					    		}
    					    		recordCreated = true;
    					    		//nextPar.setValue("");
    					    		prevAnn = nextPar;
    							}
    						}
    						firstItemTierName = parentPerLevel.get(elem.flexType);
    					} else {
    						if (!tName.equals(parentPerLevel.get(elem.flexType))) {
    							String nextTierName = parentPerLevel.get(elem.flexType);
    							firstItemTierName = nextTierName;
    							if (isAlignable(elem.flexType)) {
    								//nextPar = createAnnotationRecord(nextTierName, parentAnn, null, elem.bt, elem.et);//
    								nextPar = createAnnotationRecord(nextTierName, parentAnn, null, elem.bt, elem.et);
    								if (elem.id != null) {
    									guidIdMap.put(elem.id, nextPar.getAnnotationId());
					    				nextPar.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
					    			}
    							} else {
    								//nextPar = createRefAnnotationRecord(nextTierName, parentAnn, null);//
    								nextPar = createRefAnnotationRecord(nextTierName, parentAnn, null);
    							}
    							prevAnn = nextPar;//??
    							recordCreated = true;
    						}
    						firstItemTierName = parentPerLevel.get(elem.flexType);
    					}
    				}
    				
					if (!tierNameSet.contains(tName)) {
						tierNameSet.add(tName);
			    		TierRecord tr = new TierRecord();			    		
			    		tr.setName(tName);
			    		tr.setDefaultLocale(item.lang);
			    		if (j == 0) {
			    			tr.setLinguisticType(elem.flexType);
			    			tr.setParentTier(annotRecordToTierMap.get(parentAnn));
			    		} else {
			    			tr.setLinguisticType(FlexConstants.GLS);
			    			tr.setParentTier(firstItemTierName);
			    		}
			    		tierMap.put(tName, tr);
					}
					
    				if (j == 0 && !recordCreated) {
    					if (isAlignable(elem.flexType)) {
    						nextPar = createAnnotationRecord(tName, parentAnn, prevAnn, elem.bt, elem.et);
    						if (elem.id != null) {
    							guidIdMap.put(elem.id, nextPar.getAnnotationId());
			    				nextPar.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
			    			}
    					} else if (!recordCreated){
    						nextPar = createRefAnnotationRecord(tName, parentAnn, prevAnn);
    					}
	    				nextPar.setValue(item.value);
    					
    					prevAnn = nextPar;
    				} else {
    					AnnotationRecord child = createRefAnnotationRecord(tName, nextPar, null);
	    				child.setValue(item.value);
	    				//prevAnn = child;
    				}
    				*/
				}
			} else {// shouldn't happen
				if (!parentPerLevel.containsKey(elem.flexType)) {
//					tierNameBuf.append(elem.flexType);
//					if (elem.flexType.equals(FlexConstants.WORD) || elem.flexType.equals(FlexConstants.MORPH)) {
//						tierNameBuf.append("-");
//						tierNameBuf.append(FlexConstants.TXT);
//					}
//					parentPerLevel.put(elem.flexType, tierNameBuf.toString());
				} 
				String nextTierName = parentPerLevel.get(elem.flexType);

				if (!tierNameSet.contains(nextTierName)) {
					tierNameSet.add(nextTierName);
		    		TierRecord tr = new TierRecord();
		    		tr.setLinguisticType(elem.flexType);
		    		tr.setName(nextTierName);
		    		tr.setParentTier(annotRecordToTierMap.get(parentAnn));
		    		tierMap.put(nextTierName, tr);
				}
				if (isAlignable(elem.flexType)) {
					nextPar = createAnnotationRecord(nextTierName, parentAnn, prevAnn, elem.bt, elem.et);
					if (elem.id != null) {
						guidIdMap.put(elem.id, nextPar.getAnnotationId());
	    				nextPar.setAnnotationId(FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
	    			}
				} else {
					nextPar = createRefAnnotationRecord(nextTierName, parentAnn, prevAnn);
				}
				//par.setValue("");
				prevAnn = nextPar;
			}
			
			if (nextPar != null) {
				createChildRecords(elem, nextPar);
			}
		}
    }
    
    /**
     * Per element (paragraph, phrase etc) with multiple items, find the one of 
     * type "txt" and the first or a preferred "lang" as the top level.
     * Instead of just using the first in the list. 
     * 
     * @return the index of the first (or preferred) "txt" item, or zero if not found
     */
    private int getIndexOfPreferredTopLevelItem() {
    	return 0;
    }
    
    private void createLingTypes() {
    	// create "txt", "paragraph", "phrase", "word", "morph" and "gloss" 
    	// check the time alignment
    	LingTypeRecord lt = new LingTypeRecord();
    	lt.setLingTypeId(FlexConstants.TXT); // for toplevel tiers
    	lt.setTimeAlignable("true");
    	lt.setGraphicReferences("false");
    	lt.setStereoType(null);
    	
    	lingTypeRecords.put(FlexConstants.TXT, lt);

    	if (decoder.inclParagraphElement) {
    		lt = new LingTypeRecord();
    		lt.setLingTypeId(FlexConstants.PARAGR);
    		if (isAlignable(FlexConstants.PARAGR)) {
    			lt.setTimeAlignable("true");
    			if (decoder.inclITElement) {
    				lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
    			} else {
    				lt.setStereoType(null); // toplevel, but in that case "txt" will be used
    			}
    		} else {
    			lt.setTimeAlignable("false");
    			lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
    		}
    		lt.setGraphicReferences("false");
    		lingTypeRecords.put(FlexConstants.PARAGR, lt);
    	}
    	
    	lt = new LingTypeRecord();
    	lt.setLingTypeId(FlexConstants.PHRASE);
    	lt.setGraphicReferences("false");
    	if (isAlignable(FlexConstants.PHRASE)) {
    		lt.setTimeAlignable("true");
    		if (!decoder.inclITElement && !decoder.inclParagraphElement) {
    			lt.setStereoType(null); // toplevel, but in that case "txt" will be used
    		} else {
    			lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
    		}
    	} else {
			lt.setTimeAlignable("false");
			lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
    	}
    	lingTypeRecords.put(FlexConstants.PHRASE, lt);
    	
    	lt = new LingTypeRecord();
    	lt.setLingTypeId(FlexConstants.WORD);
    	lt.setGraphicReferences("false");
    	if (isAlignable(FlexConstants.WORD)) {
    		lt.setTimeAlignable("true");
    		lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
    	} else {
			lt.setTimeAlignable("false");
			lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
    	}
    	lingTypeRecords.put(FlexConstants.WORD, lt);
    	
    	lt = new LingTypeRecord();
    	lt.setLingTypeId(FlexConstants.MORPH);
    	lt.setGraphicReferences("false");
    	if (isAlignable(FlexConstants.MORPH)) {
    		lt.setTimeAlignable("true");
    		lt.setStereoType(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION]);
    	} else {
			lt.setTimeAlignable("false");
			lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
    	}
    	lingTypeRecords.put(FlexConstants.MORPH, lt);
    	
    	lt = new LingTypeRecord();
    	lt.setLingTypeId(FlexConstants.GLS);
    	lt.setGraphicReferences("false");
		lt.setTimeAlignable("false");
		lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);

    	lingTypeRecords.put(FlexConstants.GLS, lt);
    }
    
    private AnnotationRecord createAnnotationRecord(String tierName, 
    		AnnotationRecord par, AnnotationRecord prev, long bt, long et) {
    	AnnotationRecord ar = new AnnotationRecord();    	
    	ar.setAnnotationId(ANN_ID_PREFIX + annotId++);
    	ar.setAnnotationType(AnnotationRecord.ALIGNABLE);

    	if (par != null) {

    			if (prev != null) {
    				String oldEndTSId = prev.getEndTimeSlotId(); 
    				ar.setEndTimeSlotId(prev.getEndTimeSlotId());
    				int beginTSId = tsId++;
    				String nextId = TS_ID_PREFIX + beginTSId;
    				ar.setBeginTimeSlotId(nextId);
    				prev.setEndTimeSlotId(nextId);
    				// update all existing depending annotationrecords with the same end slot
    				updateAnnRecordEndTS(oldEndTSId, nextId, prev.getAnnotationId());
    				long[] ts = { beginTSId, bt};
    		        timeSlots.add(ts);
    		        timeOrder.add(ts);
    			} else {
    				ar.setBeginTimeSlotId(par.getBeginTimeSlotId());
    				ar.setEndTimeSlotId(par.getEndTimeSlotId());
    			}
    		
    	} else {
    		int beginTSId = tsId++;
            int endTSId = tsId++;
            ar.setBeginTimeSlotId(TS_ID_PREFIX + beginTSId);
            ar.setEndTimeSlotId(TS_ID_PREFIX + endTSId);

            // time info
            long[] begin = { beginTSId, bt };
            long[] end = { endTSId, et };

            timeSlots.add(begin);
            timeSlots.add(end);

            timeOrder.add(begin);
            timeOrder.add(end);
    	}
    	
        annotationRecords.add(ar);

        addRecordToTierMap(ar, tierName);
        
    	return ar;
    }
    
    private AnnotationRecord createRefAnnotationRecord(String tierName, 
    		AnnotationRecord par, AnnotationRecord prev) {
    	AnnotationRecord ar = new AnnotationRecord();
    	ar.setAnnotationId(ANN_ID_PREFIX + annotId++);
    	ar.setAnnotationType(AnnotationRecord.REFERENCE);
    	
    	if (par != null) {
    		ar.setReferredAnnotId(par.getAnnotationId());
    	} else {
    		// LOG error
    		System.out.println("Error: null as parent! " + tierName);
    	}
    	if (prev != null && prev.getAnnotationType() == AnnotationRecord.REFERENCE) {
			ar.setPreviousAnnotId(prev.getAnnotationId());
		}
    	
        annotationRecords.add(ar);
        addRecordToTierMap(ar, tierName);
        
    	return ar;
    }
    
   /**
    * Adds a record to the list of records of a specified tier. If the list
    * does not exist yet, it is created.
    *
    * @param annRec the record
    * @param tierName the tier name
    */
   private void addRecordToTierMap(AnnotationRecord annRec, String tierName) {
       annotRecordToTierMap.put(annRec, tierName);

       if (tierNameToAnnRecordMap.containsKey(tierName)) {
           tierNameToAnnRecordMap.get(tierName).add(annRec);
       } else {
           ArrayList<AnnotationRecord> ar = new ArrayList<AnnotationRecord>();
           ar.add(annRec);
           tierNameToAnnRecordMap.put(tierName, ar);
       }
   }
   
   /**
    * Adjust records with annotation id > than the specified id.
    * 
    * @param oldEndTSId the end timeslot id to find
    * @param nextId the new end timeslot id
    * @param annotId only update annotations with an id higher than this id
    */
   private void updateAnnRecordEndTS(String oldEndTSId, String nextId, String annotId) {
	   try {
		   int refId = 0;
		   if (annotId.startsWith(FlexConstants.FLEX_GUID_ANN_PREFIX)) {
			   refId = Integer.parseInt(guidIdMap.get(annotId.substring(
					   FlexConstants.FLEX_GUID_ANN_PREFIX.length())).substring(ANN_ID_PREFIX.length()));
		   } else {
			   refId = Integer.parseInt(annotId.substring(ANN_ID_PREFIX.length()));
		   }
		   int depId = 0;
		   
		   for (AnnotationRecord record : annotationRecords) {
			   if (record.getAnnotationType() == AnnotationRecord.ALIGNABLE) {
				   if (record.getEndTimeSlotId() == oldEndTSId) {
					   try {
						   if (record.getAnnotationId().startsWith(FlexConstants.FLEX_GUID_ANN_PREFIX)) {
							   depId = Integer.parseInt(guidIdMap.get(
									   record.getAnnotationId().substring(
											   FlexConstants.FLEX_GUID_ANN_PREFIX.length())).substring(
											   ANN_ID_PREFIX.length()));
						   } else {
							   depId = Integer.parseInt(
								   record.getAnnotationId().substring(ANN_ID_PREFIX.length()));
						   }
						   if (depId > refId) {
							   record.setEndTimeSlotId(nextId);
						   }
					   } catch (Exception ex) {
						   System.out.println("Cannot update depending annotation record: " 
								   + record.getAnnotationId()); 
					   }				   
				   }
			   }
		   }
	   } catch (NumberFormatException nfe) {
		   System.out.println("Cannot update depending annotation records of: " + annotId);
	   }
   }
   
   /** 
    * Returns whether a tier of a certain level (phrase, word, morph etc) is 
    * alignable, according to the settings.
    * 
    * @param tierLevel the level identifier
    * @return true if it is alignable, false otherwise
    */
   private boolean isAlignable(String tierLevel) {
	   return unitLevels.indexOf(tierLevel) >= 
		   unitLevels.indexOf(decoder.smallestWithTimeAlignment);
   }
 
   
   /**
    * Calculate the duration per unit, based on the information in the 
    * decoder object.
    */
   private void calculateDurations() {
	  if (decoder.totalDurationSpecified) {
		  itDur = (int) decoder.totalDuration;
		  if (FlexConstants.MORPH.equals(decoder.smallestWithTimeAlignment)) {
			  if (morphCount != 0) {
				  unitDur = itDur / morphCount;
			  } else if (wordCount != 0){
				  unitDur = itDur / wordCount;
			  } else if (phraseCount != 0) {
				  unitDur = itDur / phraseCount;
			  } else if (parCount != 0) {
				  unitDur = itDur / parCount;
			  }
		  } else if (FlexConstants.WORD.equals(decoder.smallestWithTimeAlignment)) {
			  if (wordCount != 0){
				  unitDur = itDur / wordCount;
			  } else if (phraseCount != 0) {
				  unitDur = itDur / phraseCount;
				  } else if (parCount != 0) {
					  unitDur = itDur / parCount;
				  } 
		  } else if (FlexConstants.PHRASE.equals(decoder.smallestWithTimeAlignment)) {
			  if (phraseCount != 0) {
				  unitDur = itDur / phraseCount;
			  } else if (parCount != 0) {
				  unitDur = itDur / parCount;
			  }
		  } else if (FlexConstants.PARAGR.equals(decoder.smallestWithTimeAlignment)) {
			  if (parCount != 0) {
				  unitDur = itDur / parCount;
			  }
		  }
	  } else {
		  if (FlexConstants.MORPH.equals(decoder.smallestWithTimeAlignment)) {
			  unitDur = (int) decoder.perElementDuration;
			  itDur = morphDur * morphCount;
		  } else if (FlexConstants.WORD.equals(decoder.smallestWithTimeAlignment)) {
			  unitDur = (int) decoder.perElementDuration;
			  itDur = wordDur * wordCount;
		  } else if (FlexConstants.PHRASE.equals(decoder.smallestWithTimeAlignment)) {
			  unitDur = (int) decoder.perElementDuration;
			  itDur = phraseDur * phraseCount;
		  } else if (FlexConstants.PARAGR.equals(decoder.smallestWithTimeAlignment)) {
			  unitDur = (int) decoder.perElementDuration;
			  itDur = parDur * parCount;
		  }
	  }
   }
   
   private void updateElementTimes(ContainerElem elem) {
	   	if (elem == null) {
			return;
		}
	   	elem.bt *= unitDur;
	   	elem.et *= unitDur;
	   	if (elem.getChildElems() != null) {
	   		for(ContainerElem child : elem.getChildElems()) {
	   			updateElementTimes(child);
	   		}
	   	}
   }
   
   /**
    * Restores previous alignment, in a second step conflicts, as a result of newly
    * added elements, are resolved.
    * @param elem the element to restore
    */
   private void restoreAlignment(ContainerElem elem) {
	   	if (elem == null || decoder.getStoredAlignment() == null) {
			return;
		}
	   	long[] stored = decoder.getStoredAlignment().get(elem.id);
	   	if (stored != null) {
	   		elem.bt = stored[0];
	   		elem.et = stored[1];
	   	} else {
	   		return;// the children are not aligned/stored
	   	}
	   	
	   	if (elem.getChildElems() != null) {
	   		ContainerElem child;
	   		for (int i = 0; i < elem.getChildElems().size(); i++) {
		   		child = elem.getChildElems().get(i);
		   		restoreAlignment(child);
		   	}
	   	}
   }
   
   /**
    * Update time alignment of the children, especially of annotations that have not been aligned before. 
    * Assumes that the parent has been correctly aligned; the parent may only be altered if it is the
    * top element. That one may be stretched to encompass the direct children. 
    * (The last fails if the topelement is interlinear text and the first child is one paragraph.)
    * This is only capable of handling new elements added to the end of the FLEx file.
    * 
    * @param parElem the parent element
    */
   private void resolveAlignmentConflicts(ContainerElem parElem) {
	   	if (parElem == null || decoder.getStoredAlignment() == null) {
			return;
		}
	   	
	   	if (parElem.getChildElems() != null) {
	   		ContainerElem child = null;
	   		ContainerElem prevChild = null;
	   		long[] stored;
	   		// check if there is a mix of new and restored elements
	   		if (parElem == topElement) {
	   			// update parent, assume new annotations only at the end
		   		for (int i = 0; i < parElem.getChildElems().size(); i++) {
			   		child = parElem.getChildElems().get(i);
			   		stored = decoder.getStoredAlignment().get(child.id);
			   		if (stored == null) {
			   			if (prevChild != null) {
			   				if (child.bt < prevChild.et) {
			   					long diff = prevChild.et - child.bt;
			   					child.bt = prevChild.et;
			   					child.et += diff;
			   				}
			   			} else {
			   				if (child.bt < parElem.bt) {
			   					child.bt = parElem.bt;
			   				}
			   			}
			   		} // else... don't change stored alignment
			   		prevChild = child;
			   	}
		   		if (child != null) {// child is the last element
		   			if (child.et > parElem.et) {
		   				parElem.et = child.et;
		   			}
		   		}
	   		} else {
	   			// parent is not the top element, adjust to the parent
	   			long diff = 0;
		   		for (int i = 0; i < parElem.getChildElems().size(); i++) {
			   		child = parElem.getChildElems().get(i);
			   		if (i == 0) {
			   			if (child.bt < parElem.bt) {
			   				diff = parElem.bt - child.bt;
					   		child.bt += diff;
					   		child.et += diff;
			   			}
			   			if (child.et > parElem.et) {
			   				child.et = parElem.et;
			   			}
			   		}
			   		if (prevChild != null) {
			   			if (prevChild.et >= parElem.et) {
			   				// make some space
			   				int num = parElem.getChildElems().size() - i;
			   				prevChild.et = parElem.et - (num * 80);//random small interval
			   				child.bt = prevChild.et;
			   				child.et = child.bt + 80;
			   			} else {
			   				if (child.bt != prevChild.et) {
			   					child.bt = prevChild.et;
			   					if (child.et > parElem.et) {
			   						child.et = parElem.et;
			   					}
			   					
			   				} 
			   			}
			   		}
			   		if (i == parElem.getChildElems().size() - 1) {
			   			if (child.et != parElem.et) {
			   				child.et = parElem.et;
			   			}
			   		}			   		
			   		prevChild = child;
		   		}
	   		}
	   		
	   		for (int i = 0; i < parElem.getChildElems().size(); i++) {
	   			resolveAlignmentConflicts(parElem.getChildElems().get(i));
	   		}
	   	} else {
	   		return;
	   	}
   }

    // ######################################################################
    /**
     * The SAX ContentHandler for Flex files. 
     * The following elements can have "item" child elements:
     * "interlinear-text", "phrase", "word" and "morph" 
     * @author Han Sloetjes
     * @version 1.0
      */
    private class FlexContentHandler implements ContentHandler {
        private ContainerElem curElem;
        private ContainerElem nextElem;
        private Item nextItem;
        private String lang;
        private String type;
        private StringBuilder content = new StringBuilder();
        // if a total duration is specified the time is treated as a scale factor 
        // or index for calculating the real time values after parsing when the 
        // number of elements is known (then time = curTime * unitDur)
        private long curTime = 0L;

        /**
         * Creates container objects for selected elements and item objects
         * for any "item" element.
         * 
         * @param uri 
         * @param localName 
         * @param qName 
         * @param atts 
         *
         * @throws SAXException 
         */
        public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            //System.out.println("E: " + qName);

            if (FlexConstants.DOC.equals(qName)) {
            	return;
            }
            // always create a top element for the root regardless of setting for
            // include interlinear-text element
            if (FlexConstants.IT.equals(qName)) {
                topElement = new ContainerElem(FlexConstants.IT);
                curElem = topElement;
                curElem.bt = curTime;
                // interlinear text has no guid?
                curElem.id = "1";//special case for top element -> "a_1"
                //curElem.id = atts.getValue(FlexConstants.GUID);// can be null
                // check attributes
                return;
            }

            if (decoder.inclParagraphElement && FlexConstants.PARAGR.equals(qName)) {
            	parCount++;
                nextElem = new ContainerElem(FlexConstants.PARAGR);
                nextElem.bt = curTime;
                nextElem.id = atts.getValue(FlexConstants.GUID);// can be null
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.PHRASE.equals(qName)) {
            	phraseCount++;
                nextElem = new ContainerElem(FlexConstants.PHRASE);
                nextElem.bt = curTime;
                nextElem.id = atts.getValue(FlexConstants.GUID);// can be null
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.WORD.equals(qName)) {
            	wordCount++;
                nextElem = new ContainerElem(FlexConstants.WORD);
                nextElem.bt = curTime;
                nextElem.id = atts.getValue(FlexConstants.GUID);// can be null
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.MORPH.equals(qName)) {
            	morphCount++;
                nextElem = new ContainerElem(FlexConstants.MORPH);
                nextElem.bt = curTime;
                // guid's for MORPH not unique
                // special case for type attribute of morph element
                // treat as an item child element
                String typeVal = atts.getValue(FlexConstants.TYPE);

                if ((typeVal != null) && (typeVal.length() > 0)) {
                    nextItem = new Item();
                    nextItem.type = FlexConstants.TYPE;
                    nextItem.value = typeVal;
                    nextElem.addItem(nextItem);
                    nextItem.tierName = nextElem.flexType + DEL + nextItem.type;
                    // add to map
                    if (typeLangPerLevel.get(nextElem.flexType) == null) {
                    	typeLangPerLevel.put(nextElem.flexType, new LinkedHashSet<String>(10));
                    }
                    typeLangPerLevel.get(nextElem.flexType).add(nextItem.tierName);
                }
                
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.ITEM.equals(qName)) {
                // check attributes
                type = atts.getValue(FlexConstants.TYPE);

                if ((type != null) && (type.length() > 0)) {
                    nextItem = new Item();
                    
                    // add "punct" to the text, txt, layer
                    // use the TXT constant for the type in order to check with "=="
                    if (FlexConstants.TXT.equals(type) || FlexConstants.PUNCT.equals(type)) {
                        nextItem.type = FlexConstants.TXT; 
                    } else {
                        nextItem.type = type;
                    }

                    nextItem.lang = atts.getValue(FlexConstants.LANG);
                    if (nextItem.lang != null && !languages.contains(nextItem.lang)) {
                    	languages.add(nextItem.lang);
                    }
                    // add combination of type and lang to a per level set
                    String tierName = null;
                    if (nextItem.lang == null) {
                    	tierName = curElem.flexType + DEL + nextItem.type;
                    } else {
                    	tierName = curElem.flexType + DEL + nextItem.type + DEL + nextItem.lang;
                    }
                    
                    nextItem.tierName = tierName;
                    
                    if (typeLangPerLevel.get(curElem.flexType) == null) {
                    	typeLangPerLevel.put(curElem.flexType, new LinkedHashSet<String>(10));
                    }
                    typeLangPerLevel.get(curElem.flexType).add(tierName);
                    //curElem.addItem(nextItem);// add at end of element
                }

                return;
            } else if (FlexConstants.LANGUAGE.equals(qName)) {
                lang = atts.getValue(FlexConstants.LANG);

                if (!languages.contains(lang)) {
                    languages.add(lang);
                }

                return;
            }
        }

        /**
         * Only "item" elements can have content.
         *
         * @param ch 
         * @param start 
         * @param length 
         *
         * @throws SAXException 
         */
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            //System.out.println("Ch: " + new String(ch) + " s: " + start);
            content.append(ch, start, length);
        }

        /**
         * Traverse up the containment tree for most relevant elements,
         * set the value of an item in case of "item" element.
         *
         * @param uri 
         * @param localName 
         * @param qName 
         *
         * @throws SAXException 
         */
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            // traverse up the container hierarchy
            if (FlexConstants.IT.equals(qName)) {
            	curElem.et = curTime;
                return;
            }

            if ((decoder.inclParagraphElement && FlexConstants.PARAGR.equals(qName)) ||
                    FlexConstants.PHRASE.equals(qName) ||
                    FlexConstants.WORD.equals(qName) ||
                    FlexConstants.MORPH.equals(qName)) {
            	if (decoder.smallestWithTimeAlignment.equals(qName)) {
            		curTime += unitDur;
            	}
                // increase time value if the element is an ancestor of the smallest with 
                // time alignment but has no subelements
                if (unitLevels.indexOf(qName) > unitLevels.indexOf(decoder.smallestWithTimeAlignment)) {
                	if ((curElem.getChildElems() == null || curElem.getChildElems().size() == 0) && 
                			(curElem.getItems() != null && curElem.getItems().size() > 0)) {
                		curTime += unitDur;
                	}
                }
            	curElem.et = curTime;
                curElem = curElem.parent;
            } else if (FlexConstants.ITEM.equals(qName)) {
                nextItem.value = content.toString().trim();
                content.delete(0, content.length());
                curElem.addItem(nextItem);
            }
        }

        /**
         * Stub.
         *
         * @throws SAXException 
         */
        public void endDocument() throws SAXException {

        }

        /**
         * Stub.
         *
         * @param prefix 
         *
         * @throws SAXException 
         */
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        /**
         * Stub.
         *
         * @param ch 
         * @param start 
         * @param length 
         *
         * @throws SAXException 
         */
        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        }

        /**
         * Stub.
         *
         * @param target 
         * @param data 
         *
         * @throws SAXException 
         */
        public void processingInstruction(String target, String data)
            throws SAXException {
        }

        /**
         * Stub.
         *
         * @param locator 
         */
        public void setDocumentLocator(Locator locator) {
        }

        /**
         * Stub.
         *
         * @param name
         *
         * @throws SAXException 
         */
        public void skippedEntity(String name) throws SAXException {
        }

        /**
         * Stub.
         *
         * @throws SAXException
         */
        public void startDocument() throws SAXException {
        }

        /**
         * Stub.
         *
         * @param prefix 
         * @param uri 
         *
         * @throws SAXException 
         */
        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        }
    }
}
