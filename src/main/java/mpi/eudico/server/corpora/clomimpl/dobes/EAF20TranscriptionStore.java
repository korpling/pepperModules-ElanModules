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
import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
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
 * DOCUMENT ME! $Id: EAF20TranscriptionStore.java,v 1.1.1.1 2004/03/25 16:23:20
 * wouthuij Exp $
 *
 * @author $Author$
 * @version $Revision$
 * @version Aug 2005 Identity removed
 */
public class EAF20TranscriptionStore implements TranscriptionStore {
    // we want the XML to be saved to a file
    // HS 19-11-2002: "private final" changed to "public"to enable automatic
    // backup (if fileToWirteXMLinto is not null, then the transcription will
    // be written to that file

    /** Holds value of property DOCUMENT ME! */
    public java.io.File fileToWriteXMLinto = null; //currently final (not used)

    /**
     * Creates a new EAF20TranscriptionStore instance
     */
    public EAF20TranscriptionStore() {
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

    /**
     * DOCUMENT ME!
     *
     * @param theTranscription DOCUMENT ME!
     * @param tierOrder DOCUMENT ME!
     */
    public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, int format) {
        if (theTranscription instanceof TranscriptionImpl) {
            String pathName = ((TranscriptionImpl) theTranscription).getPathName();

            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, tierOrder,
                pathName);
        }
    }

    //M_P
    public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List tierOrder, String pathName, int format) {
        if (theTranscription instanceof TranscriptionImpl) {
            //String pathName = ((DobesTranscription) theTranscription).getPathName();
            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, tierOrder,
                pathName);
        }
    }

    /**
     * Writes to original file if this.fileToWriteXMLinto is null
     *
     * @param theTranscription MUST NOT BE NULL
     * @param tierOrder DOCUMENT ME!
     * @param path DOCUMENT ME!
     */
    public void storeTranscriptionIn(Transcription theTranscription,
        List tierOrder, String path) {
        //	System.out.println("EAF20TranscriptionStore.storeTranscription called");
        //	System.out.println("theTranscription == " + theTranscription.getName());
        Element documentElement = createDOM(theTranscription, tierOrder);

        if (this.fileToWriteXMLinto != null) {
            save(documentElement, fileToWriteXMLinto.getAbsolutePath(), true);
        } else {
            save(documentElement, path, false);
        }
    }

    private static void save(Element documentElement, String path,
        boolean backup) {
        System.out.println(path + " <----XML output\n");

        try {
            // test for errors
            if (("" + documentElement).length() == 0) {
                String txt = "Sorry: unable to save this file (zero length).";
                JOptionPane.showMessageDialog(null, txt, txt,
                    JOptionPane.ERROR_MESSAGE);

                return;
            }

            IoUtil.writeEncodedFile("UTF-8", path, documentElement);

            //Don't show message for backup
            if (!backup) {
                JOptionPane.showMessageDialog(null,
                    "Your file has been written to " + path);
            }
        } catch (Exception eee) {
            String txt = "Sorry: unable to save this file. (" +
                eee.getMessage() + ")";
            JOptionPane.showMessageDialog(null, txt, txt,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTranscription DOCUMENT ME!
     * @param tierOrder DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Element createDOM(Transcription theTranscription,
        List tierOrder) {
        Hashtable tierElements = new Hashtable(); // for temporary storage of created tier Elements
        Hashtable timeSlotIds = new Hashtable(); // for temporary storage of generated tsIds
        Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds
        Vector usedLocales = new Vector(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            System.out.println(
                "[[ASSERTION FAILED]] EAF20TranscriptionStore/storeTranscription: theTranscription is null");
        }

        ;

//        if (attisTr.getMediaObject() == null) {
//            System.out.println(
//                "[[ASSERTION FAILED]] EAF20TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
//        }

        ;

        EAF20 eafFactory = null;

        try {
            eafFactory = new EAF20();
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

        String version = "2.0"; // version of DTD/format, not of file. That is handled by 'date'

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, version);
        eafFactory.appendChild(annotDocument);

        // HEADER
//        Element header = eafFactory.newHeader(attisTr.getMediaObject()
//                                                     .getMediaURL().toString());
//        annotDocument.appendChild(header);

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

            Element tierElement = eafFactory.newTier(id, participant, lingType,
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

                Element annElement = eafFactory.newAnnotation();
                ((Element) tierElements.get(t.getName())).appendChild(annElement);

                Element annSubElement = null;

                String annId = (String) annotationIds.get(ann);

                if (ann instanceof AlignableAnnotation) {
                    String beginTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getBegin());
                    String endTsId = (String) timeSlotIds.get(((AlignableAnnotation) ann).getEnd());

                    annSubElement = eafFactory.newAlignableAnnotation(annId,
                            beginTsId, endTsId);
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
                        stereotype);

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

        annotDocument.appendChild(timeSubdivision);
        annotDocument.appendChild(symbSubdivision);
        annotDocument.appendChild(symbAssociation);

        return eafFactory.getDocumentElement();
    }

    //M_P
    public void storeTranscriptionAsTemplateIn(Transcription theTranscription,
        List tierOrder, String path) {
        Element documentElement = createTemplateDOM(theTranscription,
                tierOrder);

        if (this.fileToWriteXMLinto != null) {
            save(documentElement, fileToWriteXMLinto.getAbsolutePath(), true);
        } else {
            save(documentElement, path, false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTranscription DOCUMENT ME!
     * @param tierOrder DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Element createTemplateDOM(Transcription theTranscription,
        List tierOrder) {
        Hashtable tierElements = new Hashtable(); // for temporary storage of created tier Elements
        Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds
        Vector usedLocales = new Vector(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            System.out.println(
                "[[ASSERTION FAILED]] EAF20TranscriptionStore/storeTranscription: theTranscription is null");
        }

        ;

//        if (attisTr.getMediaObject() == null) {
//            System.out.println(
//                "[[ASSERTION FAILED]] EAF20TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
//        }

        ;

        EAF20 eafFactory = null;

        try {
            eafFactory = new EAF20();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm z");

        String dateString = dateFmt.format(Calendar.getInstance().getTime());

        String author = attisTr.getAuthor();

        //always set author to empty in template
        author = "";

        String version = "2.0"; // version of DTD/format, not of file. That is handled by 'date'

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, version);
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

            Element tierElement = eafFactory.newTier(id, participant, lingType,
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
                        stereotype);

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

        annotDocument.appendChild(timeSubdivision);
        annotDocument.appendChild(symbSubdivision);
        annotDocument.appendChild(symbAssociation);

        return eafFactory.getDocumentElement();
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTranscription DOCUMENT ME!
     */
    public void loadTranscription(Transcription theTranscription) {
        //	System.out.println("EAFTranscriptionStore.loadTranscription called");
        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        // set media transcription's media file
        String mediaFileName = EAF20Parser.Instance().getMediaFile(attisTr.getPathName());

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

        String svgFile = EAF20Parser.Instance().getSVGFile(attisTr.getPathName());

        if (svgFile != null) {
            if (!svgFile.startsWith("file:")) {
                svgFile = "file:" + mediaFileName;
            }

            attisTr.setSVGFile(svgFile);
        }

        // set author
        String author = EAF20Parser.Instance().getAuthor(attisTr.getPathName());

        if (attisTr.getAuthor().equals("")) {
            attisTr.setAuthor(author);
        }

        // make linguistic types available in transcription
        Vector linguisticTypes = EAF20Parser.Instance().getLinguisticTypes(attisTr.getPathName());

        Vector typesCopy = new Vector(linguisticTypes.size());

        for (int i = 0; i < linguisticTypes.size(); i++) {
            typesCopy.add(i, linguisticTypes.get(i));
        }

        attisTr.setLinguisticTypes(typesCopy);

        //attisTr.setLinguisticTypes(linguisticTypes);
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // populate TimeOrder with TimeSlots
        Vector order = EAF20Parser.Instance().getTimeOrder(attisTr.getPathName());
        Hashtable slots = EAF20Parser.Instance().getTimeSlots(attisTr.getPathName());

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
            Iterator iter = EAF20Parser.Instance()
                                       .getTierNames(attisTr.getPathName())
                                       .iterator();

            // HB, 27 aug 03, moved earlier
            attisTr.setLoaded(true); // else endless recursion !!!!!

            while (iter.hasNext()) {
                String tierName = (String) iter.next();

				TierImpl tier = new TierImpl(null, tierName, null, attisTr, null);

                // set tier's metadata
                String participant = EAF20Parser.Instance().getParticipantOf(tierName,
                        attisTr.getPathName());
                LinguisticType linguisticType = EAF20Parser.Instance()
                                                           .getLinguisticTypeOf(tierName,
                        attisTr.getPathName());
                Locale defaultLanguage = EAF20Parser.Instance()
                                                    .getDefaultLanguageOf(tierName,
                        attisTr.getPathName());

                tier.setMetadata("PARTICIPANT", participant);
                tier.setLinguisticType(linguisticType);

                if (defaultLanguage != null) { // HB, 29 oct 02: added condition, since DEFAULT_LOCALE is IMPLIED
                    tier.setMetadata("DEFAULT_LOCALE", defaultLanguage);
                }

                // potentially, set tier's parent
                String parentId = EAF20Parser.Instance().getParentNameOf(tierName,
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

        //	attisTr.setLoaded(true);	// else endless recursion !!!!!
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
            Vector annotationRecords = EAF20Parser.Instance().getAnnotationsOf(tier.getName(),
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
                String svg_ref = null;

                while (it2.hasNext()) {
                    if (index == 0) { // first element annotation id
                        id = (String) it2.next();
                    } else if (index == 1) { // second flag indicating type of annotation
                        annotType = (String) it2.next();
                    } else if (index == 2) {
                        if (annotType.equals("alignable") ||
                                annotType.equals("alignable_svg")) {
                            timeSlotId1 = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            annotRefId = (String) it2.next();
                        }
                    } else if (index == 3) {
                        if (annotType.equals("alignable") ||
                                annotType.equals("alignable_svg")) {
                            timeSlotId2 = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            prevRefId = (String) it2.next();
                        }
                    } else if (index == 4) {
                        if (annotType.equals("alignable")) {
                            annotValue = (String) it2.next();
                        } else if (annotType.equals("alignable_svg")) {
                            svg_ref = (String) it2.next();
                        } else if (annotType.equals("reference")) {
                            annotValue = (String) it2.next();
                        }
                    } else if (index == 5) {
                        if (annotType.equals("alignable_svg")) {
                            annotValue = (String) it2.next();
                        }
                    }

                    index++;
                }

                if (annotType.equals("alignable")) {
                    annotation = new AlignableAnnotation((TimeSlot) timeSlothash.get(
                                timeSlotId1),
                            (TimeSlot) timeSlothash.get(timeSlotId2), tier);
                } else if (annotType.equals("alignable_svg")) {
                    annotation = new SVGAlignableAnnotation((TimeSlot) timeSlothash.get(
                                timeSlotId1),
                            (TimeSlot) timeSlothash.get(timeSlotId2), tier,
                            svg_ref);
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
            RefAnnotation refAnnotation = null;

            try {
                refAnnotation = (RefAnnotation) idToAnnotation.get(key);
                refAnnotation.addReference(referedAnnotation);
            } catch (Exception ex) {
                //MK:02/09/17 adding exception handler
                Object o = idToAnnotation.get(key);
                System.out.println("failed to add a refanno to  (" +
                    referedAnnotation.getTier().getName() + ", " +
                    referedAnnotation.getBeginTimeBoundary() + ", " +
                    referedAnnotation.getEndTimeBoundary() + ") " +
                    referedAnnotation.getValue());

                if (o instanceof AlignableAnnotation) {
                    AlignableAnnotation a = (AlignableAnnotation) o;
                    System.out.println("  found AlignableAnnotation (" +
                        a.getTier().getName() + ", " +
                        a.getBeginTimeBoundary() + ", " +
                        a.getEndTimeBoundary() + ") " + a.getValue());
                } else {
                    System.out.println("  found " + o);
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

	/**
	 * Ignores encoder information and format.
	 */
	public void storeTranscriptionIn(Transcription theTranscription, EncoderInfo encoderInfo, List tierOrder, String path, int format) throws IOException {
		storeTranscriptionIn(theTranscription, tierOrder, path);		
	} 
}
