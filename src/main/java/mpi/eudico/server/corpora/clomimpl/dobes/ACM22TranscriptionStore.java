package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;

import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.ParserFactory;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeOrderImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotComparator;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.chat.CHATEncoder;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxEncoder;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * A TranscriptionStore that corresponds to EAF v2.2.<br>
 * Version 2.2 extends v2.1 by adding support for Controlled Vocabularies.
 *
 * @see EAF21TranscriptionStore
 * 
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @version jun 2004
 * @version Aug 2005 Identity removed
 */
public class ACM22TranscriptionStore implements TranscriptionStore {
    // we want the XML to be saved to a file
    // HS 19-11-2002: "private final" changed to "public" to enable automatic
    // backup (if fileToWirteXMLinto is not null, then the transcription will
    // be written to that file

    /** Holds value of property DOCUMENT ME! */
    public java.io.File fileToWriteXMLinto = null; 
		
	private static final Logger LOG = Logger.getLogger(
		ACM22TranscriptionStore.class.getName());
		
	private boolean debug = false;
	
    /**
     * Creates a new ACM22TranscriptionStore instance
     */
    public ACM22TranscriptionStore() {
        super();
		//debug = true;
    }
    
    /*

    // File lacks println: here we add it.
    // TO DO: put this somewhere in mpi.alt
    public PrintWriter addPrintln(File f) {
        try {
            java.io.FileWriter fileWriter1 = new java.io.FileWriter(f);

            return new PrintWriter(fileWriter1, true);
        } catch (IOException ioe) {
            // return null is all we say.
        }

        return null;
    }
	*/
	
    /**
     * Requests to save the specified Transcription to a file.<br>
     * The path to the file is taken from the Transcription.
     *
     * @param theTranscription the Transcription to save
     * @param encoderInfo additional encoder information 
     * @param tierOrder the preferred ordering of the tiers
     * @param format the document / file format
     */
    public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, int format) throws IOException{
        if (theTranscription instanceof TranscriptionImpl) {
            String pathName = ((TranscriptionImpl) theTranscription).getPathName();

            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, encoderInfo, tierOrder, 
                pathName, format);
        }
    }

	/**
	 * Requests to save the specified Transcription to a file.<br>
	 * The path to the file is specified by the given pathName.
	 *
	 * @param theTranscription the Transcription to save
	 * @param tierOrder the preferred ordering of the tiers
	 * @param pathName the path to the file to use for storage
	 */
    public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, String pathName, int format) throws IOException{
        if (theTranscription instanceof TranscriptionImpl) {
            //String pathName = ((DobesTranscription) theTranscription).getPathName();
            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, encoderInfo, tierOrder, 
                pathName, format);
        }
    }

    /**
     * Writes to the file specified by given path, unless the field 
     * <code>fileToWriteXMLinto</code> is not null.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     * @param path the path to the file to use for storage
     */
    public void storeTranscriptionIn(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, String path, int format) throws IOException{

		switch (format) {
			case TranscriptionStore.EAF:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}
								
				new EAF22Encoder().encodeAndSave(theTranscription, null, tierOrder, path);			
				break;
				
			case TranscriptionStore.CHAT:
			
				new CHATEncoder().encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
				break;
				
			case TranscriptionStore.SHOEBOX:
			
				new ShoeboxEncoder(path).encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
			
				break;
				
			default:
				break;
		}
    }



	/**
	 * Creates a template file using the given path, unless the field 
	 * <code>fileToWriteXMLinto</code> is not null.
	 *
	 * @param theTranscription the Transcription to use for the template (not null)
	 * @param tierOrder the preferred ordering of the tiers
	 * @param path the path to the file to use for storage
	 */
    public void storeTranscriptionAsTemplateIn(Transcription theTranscription,
        List tierOrder, String path) {
        	
        if (this.fileToWriteXMLinto != null) {
        	path = fileToWriteXMLinto.getAbsolutePath();	
        }
        
        new EAF22Encoder().encodeAsTemplateAndSave(theTranscription, tierOrder, path);
    }


    /**
     * Loads the Transcription from an eaf file.
     *
     * @param theTranscription DOCUMENT ME!
     */
    public void loadTranscription(Transcription theTranscription) {
        //	System.out.println("EAFTranscriptionStore.loadTranscription called");
        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;
        
        String trPathName = attisTr.getPathName();
        String lowerPathName = trPathName.toLowerCase();
		Parser parser = null;
		if (lowerPathName.endsWith("cha")) {
			parser = ParserFactory.getParser(ParserFactory.CHAT);
		}
		else if (lowerPathName.endsWith("txt")) {	// Shoebox
			parser = ParserFactory.getParser(ParserFactory.SHOEBOX);
		}
		else if (lowerPathName.endsWith("trs")) {	// Transcriber
			parser = ParserFactory.getParser(ParserFactory.TRANSCRIBER);
		}
		else if (lowerPathName.endsWith("imdi")) {	// CGN
			parser = ParserFactory.getParser(ParserFactory.CGN);
		}
		else {
			parser = ParserFactory.getParser(ParserFactory.EAF22);
		}
		long beginTime = System.currentTimeMillis();
        // NOTE: media file is not used by either Elan 1.4.1 or Elan 2.0
        // Instead MediaDescriptors are introduced. Mediafile is temporarily maintained
        // for compatibility of EAF 2.1 with Elan 1.4.1
        // set media transcription's media file
        String mediaFileName = parser.getMediaFile(trPathName);
		if (debug) {
			System.out.println("Parsing eaf took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();
		}
		
        if ((mediaFileName != null) && (mediaFileName.startsWith("file:"))) {
            mediaFileName = mediaFileName.substring(5);
        }

        attisTr.setMainMediaFile(mediaFileName);

        // make media descriptors available in transcription
        ArrayList mediaDescriptors = parser.getMediaDescriptors(trPathName);
        attisTr.setMediaDescriptors(new Vector(mediaDescriptors));

        String svgFile = parser.getSVGFile(trPathName);

        if (svgFile != null) {
            if (!svgFile.startsWith("file:")) {
                svgFile = "file:" + mediaFileName;
            }

            attisTr.setSVGFile(svgFile);
        }

        // set author
        String author = parser.getAuthor(trPathName);

        if (attisTr.getAuthor().equals("")) {
            attisTr.setAuthor(author);
        }
		
		if (debug) {
			System.out.println("Extracting header took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        // make linguistic types available in transcription
        ArrayList linguisticTypes = parser.getLinguisticTypes(trPathName);

        ArrayList typesCopy = new ArrayList(linguisticTypes.size());

        for (int i = 0; i < linguisticTypes.size(); i++) {
 //           typesCopy.add(i, linguisticTypes.get(i));
 
 			LingTypeRecord ltr = (LingTypeRecord) linguisticTypes.get(i);
 
			LinguisticType lt = new LinguisticType(ltr.getLingTypeId());

			boolean timeAlignable = true;

			if (ltr.getTimeAlignable().equals("false")) {
				timeAlignable = false;
			}

			lt.setTimeAlignable(timeAlignable);

			boolean graphicReferences = false;

			if (ltr.getGraphicReferences().equals("true")) {
				graphicReferences = true;
			}

			lt.setGraphicReferences(graphicReferences);

			String stereotype = ltr.getStereoType();
			Constraint c = null;

			if (stereotype != null) {
				stereotype = stereotype.replace('_', ' '); // for backwards compatibility

				if (stereotype.equals(
							Constraint.stereoTypes[Constraint.TIME_SUBDIVISION])) {
					c = new TimeSubdivision();
				} else if (stereotype.equals(
							Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION])) {
					c = new SymbolicSubdivision();
				} else if (stereotype.equals(
							Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION])) {
					c = new SymbolicAssociation();
				}
			}

			if (c != null) {
				lt.addConstraint(c);
			}
			
			lt.setControlledVocabularyName(ltr.getControlledVocabulary());

			typesCopy.add(lt); 
        }

        attisTr.setLinguisticTypes(new Vector(typesCopy));

		if (debug) {
			System.out.println("Creating linguistic types took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        //attisTr.setLinguisticTypes(linguisticTypes);
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // populate TimeOrder with TimeSlots
        ArrayList order = parser.getTimeOrder(trPathName);
        HashMap slots = parser.getTimeSlots(trPathName);

        HashMap timeSlothash = new HashMap(); // temporarily stores map from id to TimeSlot object

        Iterator orderedIter = order.iterator();
		TimeSlot ts = null;
		String tsKey = null;
		long time;
		ArrayList tempSlots = new ArrayList(order.size());
		int index = 0;
		// jan 2006: sort the timeslots before adding them all to the TimeOrder object
		// (for performance reasons)
        while (orderedIter.hasNext()) {

            tsKey = (String) orderedIter.next();
            time = Long.parseLong((String) slots.get(tsKey));

            if (time != TimeSlot.TIME_UNALIGNED) {
                ts = new TimeSlotImpl(time, timeOrder);
            } else {
                ts = new TimeSlotImpl(timeOrder);
            }
            
			ts.setIndex(index++);
            //timeOrder.insertTimeSlot(ts);
			tempSlots.add(ts);
            timeSlothash.put(tsKey, ts);
        }
		
		Collections.sort(tempSlots, new TimeSlotComparator());
		((TimeOrderImpl)timeOrder).insertOrderedSlots(tempSlots);
		
		if (debug) {
			System.out.println("Creating time slots and time order took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        HashMap parentHash = new HashMap();

        if (!attisTr.isLoaded()) {
            Iterator iter = parser
                         		.getTierNames(trPathName)
                                .iterator();

            // HB, 27 aug 03, moved earlier
            attisTr.setLoaded(true); // else endless recursion !!!!!

            while (iter.hasNext()) {
                String tierName = (String) iter.next();
				
				TierImpl tier = new TierImpl(null, tierName, null, attisTr, null);

                // set tier's metadata
                String participant = parser.getParticipantOf(tierName,
						trPathName);
                String linguisticTypeID = parser
                                              .getLinguisticTypeIDOf(tierName,
						trPathName);
				
				LinguisticType linguisticType = null;
				Iterator typeIter = typesCopy.iterator();
				while (typeIter.hasNext()) {
					LinguisticType lt = (LinguisticType) typeIter.next();
					if (lt.getLinguisticTypeName().equals(linguisticTypeID)) {
						linguisticType = lt;
						break;
					}
				}
				
                Locale defaultLanguage = parser
                                              .getDefaultLanguageOf(tierName,
						trPathName);

                tier.setMetadata("PARTICIPANT", participant);
                tier.setLinguisticType(linguisticType);

                if (defaultLanguage != null) { // HB, 29 oct 02: added condition, since DEFAULT_LOCALE is IMPLIED
                    tier.setMetadata("DEFAULT_LOCALE", defaultLanguage);
                }

                // potentially, set tier's parent
                String parentId = parser.getParentNameOf(tierName,
						trPathName);

                if (parentId != null) {
                    // store tier-parent_id pair until all Tiers instantiated
                    parentHash.put(tier, parentId);
                }

                attisTr.addTier(tier);
            }
        }

        // all Tiers are created. Now set all parent tiers
        Iterator parentIter = parentHash.keySet().iterator();

        while (parentIter.hasNext()) {
            TierImpl t = (TierImpl) parentIter.next();
            t.setParentTier(attisTr.getTierWithId((String) parentHash.get(t)));
        }

		if (debug) {
			System.out.println("Creating tiers took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        //	attisTr.setLoaded(true);	// else endless recursion !!!!!
        Vector tiers = attisTr.getTiers();

        // create Annotations. Algorithm:
        // 1. loop over annotationRecords Vector. Instantiate right Annotations. Store
        //    references to annotations in intermediate data structures
        // 2. loop over intermediate structure. Realize references to Annotations by object
        //    references, iso using annotation_id's
        HashMap idToAnnotation = new HashMap();
        HashMap references = new HashMap();
        HashMap referenceChains = new HashMap();

        // HB, 2-1-02: temporarily store annotations, before adding them to tiers.
        // Reason: object reference have to be in place to add annotations in correct order.
        HashMap tempAnnotationsForTiers = new HashMap();

        // create Annotations, either AlignableAnnotations or RefAnnotations
        Iterator tierIter = tiers.iterator();

        while (tierIter.hasNext()) {
            Tier tier = (Tier) tierIter.next();
            ArrayList annotationRecords = parser.getAnnotationsOf(tier.getName(),
					trPathName);

            // HB, 2-1-02
            ArrayList tempAnnotations = new ArrayList();

            Iterator it1 = annotationRecords.iterator();

            while (it1.hasNext()) {
                Annotation annotation = null;

                AnnotationRecord annotationRecord = (AnnotationRecord) it1.next();

                if (annotationRecord.getAnnotationType().equals(AnnotationRecord.ALIGNABLE)) {
                	// when the parser does not find an SVG_REF attribute the annotation is 
                	// marked alignable even if it is on a tier that allows graphic references,
                	// therefore check here whether to create an AlignableAnnotation or a
                	// SVGAlignableAnnotation
                	if ( ((TierImpl)tier).getLinguisticType().hasGraphicReferences() ) {
						annotation = new SVGAlignableAnnotation((TimeSlot) timeSlothash.get(
									annotationRecord.getBeginTimeSlotId()),
									(TimeSlot) timeSlothash.get(annotationRecord.getEndTimeSlotId()), tier,
									null);
                	} else {               	
	                    annotation = new AlignableAnnotation((TimeSlot) timeSlothash.get(
	                                annotationRecord.getBeginTimeSlotId()),
	                            (TimeSlot) timeSlothash.get(annotationRecord.getEndTimeSlotId()), tier);
                	}

                    /*
					annotation = new SVGAlignableAnnotation((TimeSlot) timeSlothash.get(
								timeSlotId1),
								(TimeSlot) timeSlothash.get(timeSlotId2), tier,
								null);
					*/
                } else if (annotationRecord.getAnnotationType().equals(AnnotationRecord.ALIGNABLE_SVG)) {
                    annotation = new SVGAlignableAnnotation((TimeSlot) timeSlothash.get(
                                annotationRecord.getBeginTimeSlotId()),
                            (TimeSlot) timeSlothash.get(annotationRecord.getEndTimeSlotId()), tier,
                            annotationRecord.getSvgReference());
                } else if (annotationRecord.getAnnotationType().equals(AnnotationRecord.REFERENCE)) {
                    annotation = new RefAnnotation(null, tier);

                    references.put(annotationRecord.getAnnotationId(), annotationRecord.getReferredAnnotId());

                    if (annotationRecord.getPreviousAnnotId() != null) {
                        referenceChains.put(annotationRecord.getAnnotationId(), annotationRecord.getPreviousAnnotId());
                    }
                }

                if (annotationRecord.getValue() != null) {
                    annotation.setValue(annotationRecord.getValue());
                }

                annotation.setId(annotationRecord.getAnnotationId());
                                
                idToAnnotation.put(annotationRecord.getAnnotationId(), annotation);

                //	tier.addAnnotation(annotation);
                // HB, 2-1-02
                tempAnnotations.add(annotation);
            }

            // end of loop over annotation records
            // HB, 2-1-02
            tempAnnotationsForTiers.put(tier, tempAnnotations);
        }

        // end of loop over tierIter
        // realize object references
        Iterator refIter = references.keySet().iterator();

        while (refIter.hasNext()) {
            String key = (String) refIter.next();
            Annotation referedAnnotation = (Annotation) idToAnnotation.get(references.get(
                        key));
            RefAnnotation refAnnotation = null;

            try {
                refAnnotation = (RefAnnotation) idToAnnotation.get(key);
                refAnnotation.addReference(referedAnnotation);
            } catch (Exception ex) {
                //MK:02/09/17 adding exception handler
                Object o = idToAnnotation.get(key);
                LOG.warning("failed to add a refanno to  (" +
                    referedAnnotation.getTier().getName() + ", " +
                    referedAnnotation.getBeginTimeBoundary() + ", " +
                    referedAnnotation.getEndTimeBoundary() + ") " +
                    referedAnnotation.getValue());

                if (o instanceof AlignableAnnotation) {
                    AlignableAnnotation a = (AlignableAnnotation) o;
					LOG.warning("  found AlignableAnnotation (" +
                        a.getTier().getName() + ", " +
                        a.getBeginTimeBoundary() + ", " +
                        a.getEndTimeBoundary() + ") " + a.getValue());
                } else {
					LOG.warning("  found " + o);
                }
            }
        }

        // realize reference chains (== within tiers)
        Iterator rIter = referenceChains.keySet().iterator();

        while (rIter.hasNext()) {
            String key = (String) rIter.next();
            RefAnnotation previous = (RefAnnotation) idToAnnotation.get(referenceChains.get(
                        key));
            RefAnnotation a = (RefAnnotation) idToAnnotation.get(key);

            if (previous != null) {
                previous.setNext(a);
            }
        }

        // HB, 2-1-01: with object references in place, add annotations to the correct tiers.
        // This is now done in the correct order (RefAnnotation.compareTo delegates comparison
        // to it's parent annotation.
        Iterator tIter = tempAnnotationsForTiers.keySet().iterator();

        while (tIter.hasNext()) {
            TierImpl t = (TierImpl) tIter.next();

            ArrayList annots = (ArrayList) tempAnnotationsForTiers.get(t);
            Iterator aIter = annots.iterator();

            while (aIter.hasNext()) {
                // HB, 14 aug 02, changed from addAnnotation
                t.insertAnnotation((Annotation) aIter.next());
            }
        }

        // HB, 4-7-02: with all annotations on the proper tiers, register implicit
        // parent-child relations between alignable annotations explicitly
        Iterator tierIter2 = tiers.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            if (t.isTimeAlignable() && t.hasParentTier()) {
                Iterator alannIter = t.getAnnotations().iterator();

                while (alannIter.hasNext()) {
                    Annotation a = (Annotation) alannIter.next();

                    if (a instanceof AlignableAnnotation) {
                        ((AlignableAnnotation) a).registerWithParent();
                    }
                }
            }
        }
        
		if (debug) {
			System.out.println("Creating and connecting annotations took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
        // HS jun 2004 create the ControlledVocabularies, if any
        
        HashMap cvTable = parser.getControlledVocabularies(trPathName);
        if ((cvTable != null) && (cvTable.size() > 0)) {
        	ControlledVocabulary cv = null;
        	Iterator cvIt = cvTable.keySet().iterator();
        	while (cvIt.hasNext()) {
        		String cvName = (String)cvIt.next();
        		if (cvName == null) {
        			continue;
        		}
        		cv = new ControlledVocabulary(cvName);
        		// the contents vector can contain one description String
        		// and many CVEntryRecords
        		ArrayList contents = (ArrayList)cvTable.get(cvName);

        		if (contents.size() > 0) {
					Object next;
					CVEntry entry;
        			for (int i = 0; i < contents.size(); i++) {
        				next = contents.get(i);
        				if (next instanceof String) {
        					cv.setDescription((String)next);
        				} else if (next instanceof CVEntryRecord) {        					
        					entry = new CVEntry(((CVEntryRecord)next).getValue(),	
								((CVEntryRecord)next).getDescription());
							if (entry != null) {
								cv.addEntry(entry);
							}
        				}
        			}
        		}
        		
        		//cv.setACMEditableObject(attisTr);
				attisTr.addControlledVocabulary(cv);
        	}
        }

		if (debug) {
			System.out.println("Creating CV's took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
		// if all root annotations unaligned (in case of shoebox or chat import)
		// align them at 1 second intervals
		if (attisTr.allRootAnnotsUnaligned()) {
			attisTr.alignRootAnnots();
		}
		
		// hb, 23-9-04
		// There are cases where more than one symbolically associated annotation refers
		// to the same parent annotation (e.g. shoebox files with interlinearized tiers
		// with 'tokens' separated by spaces, as for Advanced Glossing stuff.
		// Fix this by concatenating the values of those annotations in one RefAnnotation
		concatenateSymbolicAssociations(attisTr);

		if (debug) {
			System.out.println("Post-processing took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}      
        //System.out.println("getName: " + attisTr.getName());
        //System.out.println("fullpath: " + attisTr.getFullPath());
        //System.out.println("pathname: " + attisTr.getPathName());
    }
    
    private void concatenateSymbolicAssociations(TranscriptionImpl transcription) {
		Annotation lastParent = null;
		RefAnnotation lastAnnot = null;
		
		Vector annotsToRemove = new Vector();
		
		Vector tiers = transcription.getTiers();
			
			Iterator tierIter = tiers.iterator();
			while (tierIter.hasNext()) {
				lastParent = null;
				lastAnnot = null;
				annotsToRemove.clear();
				
				TierImpl t = (TierImpl) tierIter.next();
				LinguisticType lt = t.getLinguisticType();
				Constraint c = null;
				if (lt != null) {
					c = lt.getConstraints();
				}
				if (c != null && c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
					// iterate over annots, take annots with same parent together
					Iterator annIter = t.getAnnotations().iterator();
					while (annIter.hasNext()) {
						RefAnnotation a = (RefAnnotation) annIter.next();
						if (a.getParentAnnotation() == lastParent) {
							lastAnnot.setValue(lastAnnot.getValue() + " " + a.getValue());
							annotsToRemove.add(a);
						} 
						else {
							lastParent = a.getParentAnnotation();
							lastAnnot = a;
						}
					}
					
					// remove concatenated annots
					Iterator rIter = annotsToRemove.iterator();
					while (rIter.hasNext()) {
						t.removeAnnotation((Annotation) rIter.next());
					}
				}
			}

    }

    /**
     * Ignores the decoder info.
     */
	public void loadTranscription(Transcription theTranscription, DecoderInfo decoderInfo) {
		loadTranscription(theTranscription);		
	}
}
