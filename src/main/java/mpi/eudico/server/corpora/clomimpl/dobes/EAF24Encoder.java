package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.IoUtil;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;

import org.w3c.dom.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


/**
 * Encodes a Transcription to EAF 2.4 format and saves it.
 *
 * @version Aug 2005 Identity removed
 * @version Feb 2006 Constraint Included In added as well as LinkedFileDescriptors
 * @version Dec 2006 attribute Annotator of Tier added and writing of document 
 * level Properties.
 */
public class EAF24Encoder implements AnnotationDocEncoder, ServerLogger {
    /** the version string of the format / dtd */
    public static final String VERSION = "2.4";
    public static boolean debug = false;
    /**
     * Creates a DOM and saves.
     *
     * @param theTranscription the Transcription to store
     * @param encoderInfo additional information for encoding
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    public void encodeAndSave(Transcription theTranscription,
        EncoderInfo encoderInfo, List tierOrder, String path)
    	   throws IOException{
        Element documentElement = createDOM(theTranscription, tierOrder);
        save(documentElement, path);
    }

    /**
     * Saves a template eaf of the Transcription; everything is saved except for
     * the annotations
     *
     * @param theTranscription the Transcription to store
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    public void encodeAsTemplateAndSave(Transcription theTranscription,
        List tierOrder, String path) throws IOException{
        Element documentElement = createTemplateDOM(theTranscription, tierOrder);
        save(documentElement, path);
    }

    /**
     * Create the DOM tree and returns the document element.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     *
     * @return the document element
     */
    public static Element createDOM(Transcription theTranscription,
        List tierOrder) {
    	long beginTime = System.currentTimeMillis();
    	if (debug) {
    		System.out.println("Encoder creating DOM...");
    	}
        Hashtable tierElements = new Hashtable(); // for temporary storage of created tier Elements
        Hashtable timeSlotIds = new Hashtable(); // for temporary storage of generated tsIds
        Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds
                                                   //Hashtable svgIds = new Hashtable(); // for temporary storage of generated svg ids.

        Vector usedLocales = new Vector(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            LOG.warning(
                "[[ASSERTION FAILED]] ACM24TranscriptionStore/storeTranscription: theTranscription is null");
        }

        /* the media object is now deprecated; MediaDescriptors are used instead
           if (attisTr.getMediaObject() == null) {
               System.out.println(
                   "[[ASSERTION FAILED]] ACM22TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
           }
         */
        EAF24 eafFactory = null;

        try {
            eafFactory = new EAF24();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        String author = attisTr.getAuthor();

        if (author == null) {
            author = "unspecified";
        }

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, VERSION);
        eafFactory.appendChild(annotDocument);

        // HEADER
        Element header = eafFactory.newHeader(""); // mediaFile maintained for compat with 1.4.1
        annotDocument.appendChild(header);

        Iterator mdIter = attisTr.getMediaDescriptors().iterator();

        while (mdIter.hasNext()) {
            MediaDescriptor md = (MediaDescriptor) mdIter.next();

            String origin = null;

            if (md.timeOrigin != 0) {
                origin = String.valueOf(md.timeOrigin);
            }

            String extrFrom = null;

            if ((md.extractedFrom != null) && (md.extractedFrom != "")) {
                extrFrom = md.extractedFrom;
            }

            Element mdElement = eafFactory.newMediaDescriptor(md.mediaURL,
                    md.mimeType, origin, extrFrom);

            header.appendChild(mdElement);
        }

        Iterator lfdIt = attisTr.getLinkedFileDescriptors().iterator();
        LinkedFileDescriptor lfd;

        while (lfdIt.hasNext()) {
            lfd = (LinkedFileDescriptor) lfdIt.next();

            String origin = null;

            if (lfd.timeOrigin != 0) {
                origin = String.valueOf(lfd.timeOrigin);
            }

            Element lfdElement = eafFactory.newLinkedFileDescriptor(lfd.linkURL,
                    lfd.mimeType, origin, lfd.associatedWith);
            header.appendChild(lfdElement);
        }
        // jan 2007 add document properties
        int lastUsedAnnId = 0;
        ArrayList props = attisTr.getDocProperties();
        if (props.size() > 0) {
        	PropertyImpl prop;
        	for (int i = 0; i < props.size(); i++) {
        		prop = (PropertyImpl) props.get(i);
        		if (prop != null && 
        				(prop.getName() != null || prop.getValue() != null)) {
        			// special treatment of last used ann id
        			if (prop.getName().equals("lastUsedAnnotationId")) {
        				if (prop.getValue() != null) {
	        				try {
	        					lastUsedAnnId = Integer.parseInt((String) prop.getValue());
	        				} catch (NumberFormatException nfe) {
	        					System.out.println("Could not retrieve the last used annotation id.");
	        				}
        				}
        				continue;
        			}
        			
        			if (prop.getValue() != null) {
        				header.appendChild(eafFactory.newProperty(
            					prop.getName(), prop.getValue().toString())); 				
        			} else {
        				header.appendChild(eafFactory.newProperty(
            					prop.getName(), null));
        			}
        			
        		}
        	}
        }
        
        if (debug) {
        	System.out.println("Header creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // TIME_ORDER
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // HB, July 19, 2001: cleanup unused TimeSlots first
        timeOrder.pruneTimeSlots();

        Element timeOrderElement = eafFactory.newTimeOrder();
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
                tsElement = eafFactory.newTimeSlot(tsId, ts.getTime());
            } else {
                tsElement = eafFactory.newTimeSlot(tsId);
            }

            timeOrderElement.appendChild(tsElement);

            index++;
        }
        if (debug) {
        	System.out.println("TimeSlots creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
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

        int svgIndex = 1; // used to create svg id values
        //int annIndex = 0;
        int annIndex = lastUsedAnnId;

        Iterator tierIter = storeOrder.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            String id = t.getName();
            String participant = (String) t.getMetadataValue("PARTICIPANT");
            String annotator = (String) t.getMetadataValue("ANNOTATOR");
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

            Element tierElement = eafFactory.newTier(id, participant, annotator, 
            		lingType, lang, parentName);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use

            Vector annotations = t.getAnnotations();

            Iterator annotIter = annotations.iterator();
            /*
            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();
                annotationIds.put(ann, "a" + annIndex);

                annIndex++;
            }
            */
            /*
            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();

                //annotation has already an id
                if ((ann.getId() != null) && !ann.getId().equals("")) {
                    annotationIds.put(ann, ann.getId());
                } else {
                    //create an id that isn't yet in the transcription
                    do {
                        annIndex++;
                    } while (attisTr.getAnnotation("a" + annIndex) != null);

                    annotationIds.put(ann, "a" + annIndex);
                }
            }
            */
            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();

                //annotation has already an id
                if ((ann.getId() != null) && !ann.getId().equals("")) {
                    annotationIds.put(ann, ann.getId());
                } else {
                    //create an id that isn't yet in the transcription
                	   annIndex++;
                    annotationIds.put(ann, "a" + annIndex);
                }
            }

        }
        header.appendChild(eafFactory.newProperty("lastUsedAnnotationId", String.valueOf(annIndex)));

        // ANNOTATIONS
        // second pass. Actually creates and adds Annotation Elements
        Iterator tierIter2 = storeOrder.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            Vector annotations = t.getAnnotations();

            Iterator annotIter2 = annotations.iterator();

            while (annotIter2.hasNext()) {
                Annotation ann = (Annotation) annotIter2.next();

                Element annElement = eafFactory.newAnnotation();
                ((Element) tierElements.get(t.getName())).appendChild(annElement);

                Element annSubElement = null;

                String annId = (String) annotationIds.get(ann);

                if (ann instanceof AlignableAnnotation) {
                    String beginTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getBegin());
                    String endTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getEnd());

                    if (ann instanceof SVGAlignableAnnotation) {
                        if (((SVGAlignableAnnotation) ann).getShape() != null) {
                            String svgId = "ga" + svgIndex;
                            ((SVGAlignableAnnotation) ann).setSVGElementID(svgId);
                            svgIndex++;
                            //svgIds.put(ann, svgId);
                            annSubElement = eafFactory.newAlignableAnnotation(annId,
                                    beginTsId, endTsId, svgId);
                        } else {
                            ((SVGAlignableAnnotation) ann).setSVGElementID(null);
                            annSubElement = eafFactory.newAlignableAnnotation(annId,
                                    beginTsId, endTsId, null);
                        }
                    } else {
                        annSubElement = eafFactory.newAlignableAnnotation(annId,
                                beginTsId, endTsId, null);
                    }
                } else if (ann instanceof RefAnnotation) {
                    String refId = null;
                    String prevId = null;
                    Vector refs = ((RefAnnotation) ann).getReferences();
                    RefAnnotation prev = ((RefAnnotation) ann).getPrevious();

                    // for the moment, take the first, if it exists
                    if (refs.size() > 0) {
                        refId = (String) annotationIds.get((Annotation) refs.firstElement());
                    }

                    if (prev != null) {
                        prevId = (String) annotationIds.get(prev);
                    }

                    annSubElement = eafFactory.newRefAnnotation(annId, refId,
                            prevId);
                }

                annElement.appendChild(annSubElement);

                // ANNOTATION_VALUE
                Element valueElement = eafFactory.newAnnotationValue(ann.getValue());
                annSubElement.appendChild(valueElement);
            }
        }
        if (debug) {
        	System.out.println("Tiers and Annotations creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // LINGUISTIC_TYPES
        Vector lTypes = attisTr.getLinguisticTypes();

        if (lTypes != null) {
            Iterator typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                LinguisticType lt = (LinguisticType) typeIter.next();

                String stereotype = null;

                if (lt.hasConstraints()) {
                    stereotype = Constraint.stereoTypes[lt.getConstraints()
                                                          .getStereoType()];
                    stereotype = stereotype.replace(' ', '_');
                }

                Element typeElement = eafFactory.newLinguisticType(lt.getLinguisticTypeName(),
                        lt.isTimeAlignable(), lt.hasGraphicReferences(),
                        stereotype, lt.getControlledVocabylaryName());

                annotDocument.appendChild(typeElement);
            }
        }

        if (debug) {
        	System.out.println("Linguistic Types creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        
        // LOCALES
        Iterator locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = (Locale) locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }

        // HB, 18 jul 02: for the moment manually add relevant Constraints
        // CONSTRAINTS
        Element timeSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION].replace(
                    ' ', '_'),
                "Time subdivision of parent annotation's time interval, no time gaps allowed within this interval");
        Element symbSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION].replace(
                    ' ', '_'),
                "Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered");
        Element symbAssociation = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION].replace(
                    ' ', '_'), "1-1 association with a parent annotation");
        Element includedIn = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.INCLUDED_IN].replace(
                    ' ', '_'),
                "Time alignable annotations within the parent annotation's time interval, gaps are allowed");

        annotDocument.appendChild(timeSubdivision);
        annotDocument.appendChild(symbSubdivision);
        annotDocument.appendChild(symbAssociation);
        annotDocument.appendChild(includedIn);

        //CONTROLLED VOCABULARIES
        Vector conVocs = attisTr.getControlledVocabularies();

        if (conVocs.size() > 0) {
            ControlledVocabulary cv;
            CVEntry entry;
            Element cvElement;
            Element entryElement;

            for (int i = 0; i < conVocs.size(); i++) {
                cv = (ControlledVocabulary) conVocs.get(i);
                cvElement = eafFactory.newControlledVocabulary(cv.getName(),
                        cv.getDescription());

                CVEntry[] entries = cv.getEntries();

                for (int j = 0; j < entries.length; j++) {
                    entry = entries[j];
                    entryElement = eafFactory.newCVEntry(entry.getValue(),
                            entry.getDescription());
                    cvElement.appendChild(entryElement);
                }

                annotDocument.appendChild(cvElement);
            }
        }
        if (debug) {
        	System.out.println("Constraints and CV's creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        return eafFactory.getDocumentElement();
    }

    /**
     * Create the DOM tree containing only elements that need to be stored in
     * the  template; i.e. everything except timeorder, time slots,
     * annotations and media descriptors.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     *
     * @return the document element
     */
    public static Element createTemplateDOM(Transcription theTranscription,
        List tierOrder) {
        Hashtable tierElements = new Hashtable(); // for temporary storage of created tier Elements
                                                  //Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds

        Vector usedLocales = new Vector(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            LOG.warning(
                "[[ASSERTION FAILED]] TranscriptionStore/storeTranscription: theTranscription is null");
        }

        /* the media object is deprecated
           if (attisTr.getMediaObject() == null) {
               System.out.println(
                   "[[ASSERTION FAILED]] ACM22TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
           }
         */
        EAF24 eafFactory = null;

        try {
            eafFactory = new EAF24();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm z");

        String dateString = dateFmt.format(Calendar.getInstance().getTime());

        String author = attisTr.getAuthor();

        //always set author to empty in template
        author = "";

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, VERSION);
        eafFactory.appendChild(annotDocument);

        // HEADER
        //always set header to empty in template
        //Element header = eafFactory.newHeader(attisTr.getMediaObject().getMediaURL().toString());
        Element header = eafFactory.newHeader("");
        annotDocument.appendChild(header);

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
            String annotator = (String) t.getMetadataValue("ANNOTATOR");
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

            Element tierElement = eafFactory.newTier(id, participant, annotator,
            		lingType, lang, parentName);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use
                                                        /*
               Vector annotations = t.getAnnotations();
               Iterator annotIter = annotations.iterator();
               while (annotIter.hasNext()) {
                   Annotation ann = (Annotation) annotIter.next();
                   annotationIds.put(ann, "a" + annIndex);
                   annIndex++;
               }
             */
        }

        // LINGUISTIC_TYPES
        Vector lTypes = attisTr.getLinguisticTypes();

        if (lTypes != null) {
            Iterator typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                LinguisticType lt = (LinguisticType) typeIter.next();

                String stereotype = null;

                if (lt.hasConstraints()) {
                    stereotype = Constraint.stereoTypes[lt.getConstraints()
                                                          .getStereoType()];
                    stereotype = stereotype.replace(' ', '_');
                }

                Element typeElement = eafFactory.newLinguisticType(lt.getLinguisticTypeName(),
                        lt.isTimeAlignable(), lt.hasGraphicReferences(),
                        stereotype, lt.getControlledVocabylaryName());

                annotDocument.appendChild(typeElement);
            }
        }

        // LOCALES
        Iterator locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = (Locale) locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }

        // HB, 18 jul 02: for the moment manually add relevant Constraints
        // CONSTRAINTS
        Element timeSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION].replace(
                    ' ', '_'),
                "Time subdivision of parent annotation's time interval, no time gaps allowed within this interval");
        Element symbSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION].replace(
                    ' ', '_'),
                "Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered");
        Element symbAssociation = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION].replace(
                    ' ', '_'), "1-1 association with a parent annotation");
        Element includedIn = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.INCLUDED_IN].replace(
                    ' ', '_'),
                "Time alignable annotations within the parent annotation's time interval, gaps are allowed");

        annotDocument.appendChild(timeSubdivision);
        annotDocument.appendChild(symbSubdivision);
        annotDocument.appendChild(symbAssociation);
        annotDocument.appendChild(includedIn);

        // CONTROLLED VOCABULARIES
        Vector conVocs = attisTr.getControlledVocabularies();

        if (conVocs.size() > 0) {
            ControlledVocabulary cv;
            CVEntry entry;
            Element cvElement;
            Element entryElement;

            for (int i = 0; i < conVocs.size(); i++) {
                cv = (ControlledVocabulary) conVocs.get(i);
                cvElement = eafFactory.newControlledVocabulary(cv.getName(),
                        cv.getDescription());

                CVEntry[] entries = cv.getEntries();

                for (int j = 0; j < entries.length; j++) {
                    entry = entries[j];
                    entryElement = eafFactory.newCVEntry(entry.getValue(),
                            entry.getDescription());
                    cvElement.appendChild(entryElement);
                }

                annotDocument.appendChild(cvElement);
            }
        }

        return eafFactory.getDocumentElement();
    }

    /**
     * Creates a validating date string.
     *
     * @param strIn the date string to correct
     *
     * @return a validating date string
     */
    private static String correctDate(String strIn) {
        String strResult = new String(strIn);

        try {
            int offsetGMT = Calendar.getInstance().getTimeZone().getRawOffset() / (60 * 60 * 1000);

            String strOffset = "+";

            if (offsetGMT < 0) {
                strOffset = "-";
            }

            offsetGMT = Math.abs(offsetGMT);

            if (offsetGMT < 10) {
                strOffset += "0";
            }

            strOffset += (offsetGMT + ":00");

            strResult += strOffset;

            int indexSpace = strResult.indexOf(" ");

            if (indexSpace != -1) {
                String strEnd = strResult.substring(indexSpace + 1);
                strResult = strResult.substring(0, indexSpace);
                strResult += "T";
                strResult += strEnd;
            }

            strResult = strResult.replace('.', '-');
        } catch (Exception ex) {
            return strIn;
        }

        return strResult;
    }

    private static void save(Element documentElement, String path) throws IOException{
        LOG.info(path + " <----XML output\n");

        try {
            // test for errors
            if (("" + documentElement).length() == 0) {
                throw new IOException("Unable to save this file (zero length).");
                /*
                String txt = "Sorry: unable to save this file (zero length).";
                JOptionPane.showMessageDialog(null, txt, txt,
                    JOptionPane.ERROR_MESSAGE);

                //bla
                return;
                */
            }
            long beginTime = System.currentTimeMillis();
            //IoUtil.writeEncodedFile("UTF-8", path, documentElement);
            IoUtil.writeEncodedEAFFile("UTF-8", path, documentElement);
            
            if (debug) {
            	System.out.println("Saving file took: " + 
            			(System.currentTimeMillis() - beginTime) + " ms");	
            }
        } catch (Exception eee) {
            throw new IOException("Unable to save this file: " + eee.getMessage());
            /*
            String txt = "Sorry: unable to save this file. (" +
                eee.getMessage() + ")";
            JOptionPane.showMessageDialog(null, txt, txt,
                JOptionPane.ERROR_MESSAGE);
                */
        }
    }
}
