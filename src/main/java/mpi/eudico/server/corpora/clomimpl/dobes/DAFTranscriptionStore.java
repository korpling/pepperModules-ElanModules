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
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.IoUtil;


import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;


/**
 * DOCUMENT ME! $Id: DAFTranscriptionStore.java,v 1.1.1.1 2004/03/25 16:23:20
 * wouthuij Exp $
 *
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public class DAFTranscriptionStore implements TranscriptionStore {
    // we want the XML to be saved to a file

    /** Holds value of property DOCUMENT ME! */
    private final java.io.File fileToWriteXMLinto = null; //currently final (not used)

    /**
     * Creates a new DAFTranscriptionStore instance
     */
    public DAFTranscriptionStore() {
        super();
    }

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

	public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
		List tierOrder, String pathName, int format) {
		
		// added to stay compatible with ACM22.	
	}

    /**
     * Writes to original file if this.fileToWriteXMLinto is null
     *
     * @param theTranscription MUST NOT BE NULL
     * @param tierOrder DOCUMENT ME!
     */
    public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, int format) {
        //	System.out.println("DAFTranscriptionStore.storeTranscription called");
        //	System.out.println("theTranscription == " + theTranscription.getName());
        Hashtable tierElements = new Hashtable(); // for temporary storage of created tier Elements
        Hashtable timeSlotIds = new Hashtable(); // for temporary storage of generated tsIds
        Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds
        Vector usedLocales = new Vector(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            System.out.println(
                "[[ASSERTION FAILED]] DAFTranscriptionStore/storeTranscription: theTranscription is null");
        }

        ;

//        if (attisTr.getMediaObject() == null) {
//            System.out.println(
//                "[[ASSERTION FAILED]] DAFTranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
//        }

        ;

        DAF dafFactory = null;

        try {
            dafFactory = new DAF();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm z");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());

        String author = attisTr.getAuthor();

        if (author == null) {
            author = "unspecified";
        }

        String version = "1.0"; // version of DTD/format, not of file. That is handled by 'date'

        Element annotDocument = dafFactory.newAnnotationDocument(dateString,
                author, version);
        dafFactory.appendChild(annotDocument);

        // HEADER
//        Element header = dafFactory.newHeader(attisTr.getMediaObject()
//                                                     .getMediaURL().toString());
//        annotDocument.appendChild(header);

        // TIME_ORDER
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // HB, July 19, 2001: cleanup unused TimeSlots first
        timeOrder.pruneTimeSlots();

        Element timeOrderElement = dafFactory.newTimeOrder();
        annotDocument.appendChild(timeOrderElement);

        int index = 1;

        Enumeration tsElements = timeOrder.elements();

        while (tsElements.hasMoreElements()) {
            TimeSlot ts = (TimeSlot) tsElements.nextElement();

            Element tsElement = null;
            String tsId = "ts" + index;

            // store ts with it's id temporarily
            timeSlotIds.put(ts, tsId);

            if (ts.getTime() != TimeSlot.TIME_UNALIGNED) {
                tsElement = dafFactory.newTimeSlot(tsId, ts.getTime());
            } else {
                tsElement = dafFactory.newTimeSlot(tsId);
            }

            timeOrderElement.appendChild(tsElement);

            index++;
        }

        // TIERS
        Vector tiers = attisTr.getTiers();

        Vector storeOrder = new Vector(tierOrder); // start with tiers in specified order
        Iterator tIter = tiers.iterator();

        while (tIter.hasNext()) { // add other tiers in document order

            Tier t = (Tier) tIter.next();

            if (!storeOrder.contains(t)) {
                storeOrder.add(t);
            }
        }

        int annIndex = 1; // used to create annotation id values

        Iterator tierIter = storeOrder.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            String id = t.getName();
            String participant = (String) t.getMetadataValue("PARTICIPANT");
            String lingType = t.getLinguisticType().getLinguisticTypeName();

            if (lingType == null) {
                lingType = "not specified";
            }

            Locale lang = (Locale) t.getMetadataValue("DEFAULT_LOCALE");

            if (lang == null) {
                lang = new Locale("not specified", "", "");
            }

            // check is quick solution, TreeSet would do this but compareTo causes ClassCastException
            if (!usedLocales.contains(lang)) {
                usedLocales.add(lang);
            }

            String parentName = null;

            if (t.getParentTier() != null) {
                parentName = t.getParentTier().getName();
            }

            Element tierElement = dafFactory.newTier(id, participant, lingType,
                    lang, parentName);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use

            Vector annotations = t.getAnnotations();

            Iterator annotIter = annotations.iterator();

            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();
                annotationIds.put(ann, "a" + annIndex);

                annIndex++;
            }
        }

        // ANNOTATIONS
        // second pass. Actually creates and adds Annotation Elements
        Iterator tierIter2 = storeOrder.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            Vector annotations = t.getAnnotations();

            Iterator annotIter2 = annotations.iterator();

            while (annotIter2.hasNext()) {
                Annotation ann = (Annotation) annotIter2.next();

                Element annElement = dafFactory.newAnnotation();
                ((Element) tierElements.get(t.getName())).appendChild(annElement);

                Element annSubElement = null;

                String annId = (String) annotationIds.get(ann);

                if (ann instanceof AlignableAnnotation) {
                    String beginTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getBegin());
                    String endTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getEnd());

                    annSubElement = dafFactory.newAlignableAnnotation(annId,
                            beginTsId, endTsId);
                } else if (ann instanceof RefAnnotation) {
                    String refId = null;
                    Vector refs = ((RefAnnotation) ann).getReferences();

                    // for the moment, take the first, if it exists
                    if (refs.size() > 0) {
                        refId = (String) annotationIds.get((Annotation) refs.firstElement());
                    }

                    annSubElement = dafFactory.newRefAnnotation(annId, refId);
                }

                annElement.appendChild(annSubElement);

                // ANNOTATION_VALUE
                Element valueElement = dafFactory.newAnnotationValue(ann.getValue());
                annSubElement.appendChild(valueElement);
            }
        }

        // LINGUISTIC_TYPES
        Vector lTypes = attisTr.getLinguisticTypes();

        if (lTypes != null) {
            Iterator typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                //	String lt = (String) typeIter.next();
                LinguisticType lt = (LinguisticType) typeIter.next();

                //	Element typeElement = dafFactory.newLinguisticType(lt);
                Element typeElement = dafFactory.newLinguisticType(lt.getLinguisticTypeName());

                annotDocument.appendChild(typeElement);
            }
        }

        // LOCALES
        Iterator locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = (Locale) locIter.next();
            Element locElement = dafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }

        if (this.fileToWriteXMLinto != null) {
            // This piece of code will be used for SaveAs....
            // But: where is the mediafile going?
            System.out.println(fileToWriteXMLinto.getAbsolutePath() +
                " <----XML output SAVE AS!\n");

            PrintWriter printWriter1 = addPrintln(this.fileToWriteXMLinto);

            if (printWriter1 == null) {
                String txt = "Sorry: unable to save this file.";
                JOptionPane.showMessageDialog(null, txt, txt,
                    JOptionPane.ERROR_MESSAGE);
            } else {
                printWriter1.println(dafFactory.getDocumentElement());
            }
        } else {
            // store XML in the file it came from
            System.out.println(attisTr.getPathName() + " <----XML output!\n");

            try {
                // test for errors
                if (("" + dafFactory.getDocumentElement()).length() == 0) {
                    String txt = "Sorry: unable to save this file (zero length).";
                    JOptionPane.showMessageDialog(null, txt, txt,
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }

                String filecontentToBeWritten = "" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<!DOCTYPE ANNOTATION_DOCUMENT>" +
                    dafFactory.getDocumentElement();
                IoUtil.writeEncodedFile("UTF-8", attisTr.getPathName(),
                    dafFactory.getDocumentElement());
                JOptionPane.showMessageDialog(null,
                    "Your file has been written to " + attisTr.getPathName());
            } catch (Exception eee) {
                String txt = "Sorry: unable to save this file. (" +
                    eee.getMessage() + ")";
                JOptionPane.showMessageDialog(null, txt, txt,
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTranscription DOCUMENT ME!
     */
    public void loadTranscription(Transcription theTranscription) {
        //	System.out.println("DAFTranscriptionStore.loadTranscription called");
        // ASSUME THAT theTranscription IS A DobesTranscription
        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        // set media transcription's media file
        String mediaFileName = DAFParser.Instance().getMediaFile(attisTr.getPathName());

        //debug System.out.println("mediaFileName == " + mediaFileName);

        /*
           author: Markus
           problem: if the String starts with "file:", then it cannot be used later.
           solution (not done): find the place where "file:" is created
           solution (not done): find the place where "file:" creates a problem.
           workaround: remove "file:"
         */
        if (mediaFileName.startsWith("file:")) {
            mediaFileName = mediaFileName.substring(5);
        }

        attisTr.setMainMediaFile(mediaFileName);

        // set author
        String author = DAFParser.Instance().getAuthor(attisTr.getPathName());

        if (attisTr.getAuthor().equals("")) {
            attisTr.setAuthor(author);
        }

        // make linguistic types available in transcription
        Vector linguisticTypes = DAFParser.Instance().getLinguisticTypes(attisTr.getPathName());
        attisTr.setLinguisticTypes(linguisticTypes);

        TimeOrder timeOrder = attisTr.getTimeOrder();

        // populate TimeOrder with TimeSlots
        Vector order = DAFParser.Instance().getTimeOrder(attisTr.getPathName());
        Hashtable slots = DAFParser.Instance().getTimeSlots(attisTr.getPathName());

        Hashtable timeSlothash = new Hashtable(); // temporarily stores map from id to TimeSlot object

        Iterator orderedIter = order.iterator();

        while (orderedIter.hasNext()) {
            TimeSlot ts = null;

            String key = (String) orderedIter.next();

            long time = Long.parseLong((String) slots.get(key));

            if (time != TimeSlot.TIME_UNALIGNED) {
                ts = new TimeSlotImpl(time, timeOrder);
            } else {
                ts = new TimeSlotImpl(timeOrder);
            }

            timeOrder.insertTimeSlot(ts);
            timeSlothash.put(key, ts);
        }

        Hashtable parentHash = new Hashtable();

        if (!attisTr.isLoaded()) {
            Iterator iter = DAFParser.Instance()
                                     .getTierNames(attisTr.getPathName())
                                     .iterator();

            while (iter.hasNext()) {
                String tierName = (String) iter.next();

				TierImpl tier = new TierImpl(null, tierName, null, attisTr, null);

                // set tier's metadata
                String participant = DAFParser.Instance().getParticipantOf(tierName,
                        attisTr.getPathName());
                LinguisticType linguisticType = DAFParser.Instance()
                                                         .getLinguisticTypeOf(tierName,
                        attisTr.getPathName());
                Locale defaultLanguage = DAFParser.Instance()
                                                  .getDefaultLanguageOf(tierName,
                        attisTr.getPathName());

                tier.setMetadata("PARTICIPANT", participant);
                tier.setLinguisticType(linguisticType);
                tier.setMetadata("DEFAULT_LOCALE", defaultLanguage);

                // potentially, set tier's parent
                String parentId = DAFParser.Instance().getParentNameOf(tierName,
                        attisTr.getPathName());

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

        attisTr.setLoaded(true); // else endless recursion !!!!!

        Vector tiers = attisTr.getTiers();

        // create Annotations. Algorithm:
        // 1. loop over annotationRecords Vector. Instantiate right Annotations. Store
        //    references to annotations in intermediate data structures
        // 2. loop over intermediate structure. Realize references to Annotations by object
        //    references, iso using annotation_id's
        Hashtable idToAnnotation = new Hashtable();
        Hashtable references = new Hashtable();
        Hashtable referenceChains = new Hashtable();

        // HB, 2-1-02: temporarily store annotations, before adding them to tiers. 
        // Reason: object reference have to be in place to add annotations in correct order.
        Hashtable tempAnnotationsForTiers = new Hashtable();

        // create Annotations, either AlignableAnnotations or RefAnnotations
        Iterator tierIter = tiers.iterator();

        while (tierIter.hasNext()) {
            Tier tier = (Tier) tierIter.next();
            Vector annotationRecords = DAFParser.Instance().getAnnotationsOf(tier.getName(),
                    attisTr.getPathName());

            // HB, 2-1-02
            Vector tempAnnotations = new Vector();

            Iterator it1 = annotationRecords.iterator();

            while (it1.hasNext()) {
                Annotation annotation = null;

                Vector annotationRecord = (Vector) it1.next();

                // annotationRecord has the following format. Either:
                // id, "alignable", time_slot_id1, time_slot_id2, value, or
                // id, "reference", annotation_ref_id, previous_annotation, value
                Iterator it2 = annotationRecord.iterator();
                int index = 0;

                String id = null;
                String annotType = null;
                String timeSlotId1 = null;
                String timeSlotId2 = null;
                String annotRefId = null;
                String annotValue = null;
                String prevRefId = null;

                while (it2.hasNext()) {
                    if (index == 0) { // first element annotation id
                        id = (String) it2.next();
                    } else if (index == 1) { // second flag indicating type of annotation
                        annotType = (String) it2.next();
                    } else if (index == 2) {
                        if (annotType.equals("alignable")) {
                            timeSlotId1 = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            annotRefId = (String) it2.next();
                        }
                    } else if (index == 3) {
                        if (annotType.equals("alignable")) {
                            timeSlotId2 = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            prevRefId = (String) it2.next();
                        }
                    } else if (index == 4) {
                        if (annotType.equals("alignable")) {
                            annotValue = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            annotValue = (String) it2.next();
                        }
                    }

                    index++;
                }

                if (annotType.equals("alignable")) {
                    annotation = new AlignableAnnotation((TimeSlot) timeSlothash.get(
                                timeSlotId1),
                            (TimeSlot) timeSlothash.get(timeSlotId2), tier);
                } else if (annotType.equals("reference")) {
                    annotation = new RefAnnotation(null, tier);

                    references.put(id, annotRefId);

                    if (!prevRefId.equals("")) {
                        referenceChains.put(id, prevRefId);
                    }
                }

                if (annotValue != null) {
                    annotation.setValue(annotValue);
                }

                idToAnnotation.put(id, annotation);

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
            RefAnnotation refAnnotation = (RefAnnotation) idToAnnotation.get(key);

            refAnnotation.addReference(referedAnnotation);
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

            Vector annots = (Vector) tempAnnotationsForTiers.get(t);
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
    }

    /**
     * Ignores the decoder info.
     */
	public void loadTranscription(Transcription theTranscription, DecoderInfo decoderInfo) {
		loadTranscription(theTranscription);
	}

	public void storeTranscriptionAsTemplateIn(Transcription theTranscription, List tierOrder, String path) throws IOException {
		// not implemented, added to stay compatible with ACM22.			
	}
	
	/**
	 * Ignores encoder information.
	 */
	public void storeTranscriptionIn(Transcription theTranscription, EncoderInfo encoderInfo, List tierOrder, String path, int format) throws IOException {
		storeTranscription(theTranscription, encoderInfo, tierOrder, format);		
	} 
}
