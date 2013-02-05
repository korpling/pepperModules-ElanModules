package mpi.eudico.client.annotator.util;

import mpi.eudico.client.annotator.svg.SVGAnnotationDataRecord;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * This class provides methods for storing annotations' state and recreation of
 * (deleted) annotations.<br>
 * It can: <br>
 * - create a dependency tree for annotations and store relevant data from
 * each annotation involved<br>
 * - recreate annotations (including dependent annotations) given the stored
 * information - handle conversion of annotations after a modification of
 * attributes of  a Linguistic Type  Note: preliminary
 *
 * @author Han Sloetjes
 * @version july 2004
 */
public class AnnotationRecreator {
    /** a logger */
    private static final Logger LOG = Logger.getLogger(AnnotationRecreator.class.getName());

    /**
     * Recreates annotations on Tiers with the specified LinguisticType.<br>
     * This operation can be neccessary when an other kind of Annotation type
     * is needed after a modification in the type, e.g. conversion of
     * AlignableAnnotations  into SVGAlignableAnnotations when the
     * LinguisticType has been changed  to allow graphic references.<br>
     * Be sure to lock the whole transcription while this operation is going
     * on!
     *
     * @param trans the Transcription
     * @param type the modified LinguisticType
     */
    public static void convertAnnotations(Transcription trans,
        LinguisticType type) {
        if ((trans == null) || (type == null)) {
            return;
        }

        ArrayList convertedTiers = new ArrayList();
        Vector tiersToConvert = null;

        tiersToConvert = trans.getTiersWithLinguisticType(type.getLinguisticTypeName());

        Vector rootTiers = new Vector();

        // put all root tiers at the beginning of the vector
        for (int i = 0; i < tiersToConvert.size(); i++) {
            TierImpl t = (TierImpl) tiersToConvert.get(i);

            if (!t.hasParentTier()) {
                rootTiers.add(t);
            }
        }

        for (int i = 0; i < tiersToConvert.size(); i++) {
            TierImpl t = (TierImpl) tiersToConvert.get(i);

            if (!rootTiers.contains(t)) {
                rootTiers.add(t);
            }
        }

        tiersToConvert = rootTiers;

        // convert all annotations on these tiers
        Iterator convIt = tiersToConvert.iterator();
        TierImpl curTier = null;

        while (convIt.hasNext()) {
            curTier = (TierImpl) convIt.next();

            if (!convertedTiers.contains(curTier)) {
                AnnotationRecreator.convertAnnotations(trans, curTier);

                convertedTiers.add(curTier);

                Vector depTiers = curTier.getDependentTiers();

                for (int i = 0; i < depTiers.size(); i++) {
                    if (!convertedTiers.contains(depTiers.get(i))) {
                        convertedTiers.add(depTiers.get(i));
                    }
                }
            }
        }
    }

    /**
     * Recreates annotations on the specified Tier.<br>
     * This operation can be necessary when an other kind of Annotation type
     * is needed after a modification in the type, e.g. conversion of
     * AlignableAnnotations  into SVGAlignableAnnotations when the
     * LinguisticType has been changed  to allow graphic references.<br>
     * Be sure to lock the whole transcription while this operation is going
     * on!
     *
     * @param trans the Transcription
     * @param tier the modified LinguisticType
     */
    public static void convertAnnotations(Transcription trans, TierImpl tier) {
        if ((trans == null) || (tier == null)) {
            return;
        }

        Vector annotations = null;
        AbstractAnnotation absAnn = null;
        DefaultMutableTreeNode root = null;

        annotations = tier.getAnnotations();

        Iterator annIt = annotations.iterator();
        ArrayList annTreeList = new ArrayList(annotations.size());

        // step 1: create data structures
        while (annIt.hasNext()) {
            absAnn = (AbstractAnnotation) annIt.next();
            root = createTreeForAnnotation(absAnn);
            annTreeList.add(root);
        }

        annIt = annotations.iterator();

        // step 2: delete annotations
        while (annIt.hasNext()) {
            absAnn = (AbstractAnnotation) annIt.next();
            tier.removeAnnotation(absAnn);
        }

        annIt = annTreeList.iterator();

        // step 3: recreate annotations
        while (annIt.hasNext()) {
            root = (DefaultMutableTreeNode) annIt.next();
            AnnotationRecreator.createAnnotationFromTree(trans, root);
        }
    }

    /**
     * Creates a tree structure from one annotation.<br>
     * The specified annotation will be the root of the tree.  UNFINISHED!
     *
     * @param aa the annotation
     *
     * @return the root of the created tree
     */
    public static DefaultMutableTreeNode createTreeForAnnotation(
        AbstractAnnotation aa) {
        DefaultMutableTreeNode root = null;

        if (aa instanceof SVGAlignableAnnotation) {
            root = new DefaultMutableTreeNode(new SVGAnnotationDataRecord(
                        (SVGAlignableAnnotation) aa));
        } else {
            root = new DefaultMutableTreeNode(new AnnotationDataRecord(aa));
        }

        ArrayList children = null;
        AbstractAnnotation next = null;
        AbstractAnnotation parent = null;
        DefaultMutableTreeNode nextNode = null;
        DefaultMutableTreeNode parentNode = root;
        TierImpl tier = null;
        String tierName = null;
        DefaultMutableTreeNode tempNode = null;
        AnnotationDataRecord dataRecord = null;

        children = aa.getParentListeners();

        if (children.size() > 0) {
downloop: 
            for (int i = 0; i < children.size(); i++) {
                next = (AbstractAnnotation) children.get(i);

                if (next instanceof SVGAlignableAnnotation) {
                    nextNode = new DefaultMutableTreeNode(new SVGAnnotationDataRecord(
                                (SVGAlignableAnnotation) next));
                } else {
                    nextNode = new DefaultMutableTreeNode(new AnnotationDataRecord(
                                next));
                }

                // children can come in any order
                tier = (TierImpl) next.getTier();

                if (parentNode.getChildCount() == 0) {
                    parentNode.add(nextNode);
                } else {
                    long bt = next.getBeginTimeBoundary();

                    for (int k = 0; k < parentNode.getChildCount(); k++) {
                        tempNode = (DefaultMutableTreeNode) parentNode.getChildAt(k);
                        dataRecord = (AnnotationDataRecord) tempNode.getUserObject();

                        tierName = next.getTier().getName();

                        if ((dataRecord.getBeginTime() > bt) &&
                                (tierName != null) &&
                                dataRecord.getTierName().equals(tierName)) {
                            parentNode.insert(nextNode, k);

                            break;
                        } else if (k == (parentNode.getChildCount() - 1)) {
                            parentNode.add(nextNode);
                        }
                    }
                }

                if (next.getParentListeners().size() > 0) {
                    children = next.getParentListeners();
                    parentNode = nextNode;
                    i = -1;

                    continue downloop;
                }

                if (i == (children.size() - 1)) {
uploop: 
                    while (true) {
                        parent = (AbstractAnnotation) next.getParentAnnotation();

                        if (parent != null) {
                            parentNode = (DefaultMutableTreeNode) nextNode.getParent();
                            children = parent.getParentListeners();

                            int j = children.indexOf(next);

                            if (j == (children.size() - 1)) {
                                if (parent == aa) {
                                    break downloop;
                                }

                                next = parent;
                                nextNode = parentNode;

                                continue uploop;
                            } else {
                                i = j;

                                continue downloop;
                            }
                        } else {
                            break downloop;
                        }
                    }
                }
            }
        }

        /*
           Enumeration en = root.depthFirstEnumeration();
           System.out.println("Depth First:\n");
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Breadth First:\n");
           en = root.breadthFirstEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Post Order:\n");
           en = root.postorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Pre Order:\n");
           en = root.preorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
         */
        return root;
    }

    /**
     * Creates a treenode without children from one annotation.<br>
     *
     * @param aa the annotation
     *
     * @return the root of the created treenode
     */
    public static DefaultMutableTreeNode createNodeForAnnotation(
        AbstractAnnotation aa) {
        DefaultMutableTreeNode node = null;

        if (aa instanceof SVGAlignableAnnotation) {
            node = new DefaultMutableTreeNode(new SVGAnnotationDataRecord(
                        (SVGAlignableAnnotation) aa));
        } else {
            node = new DefaultMutableTreeNode(new AnnotationDataRecord(aa));
        }

        return node;
    }

    /**
     * (Re)creates an annotation without reproducing the annotation ID.
     * @param trans the transcription
     * @param root the root node
     * 
     * @return the created root annotation or null
     * @see #createAnnotationFromTree(Transcription, DefaultMutableTreeNode, boolean)
     */
    public static AbstractAnnotation createAnnotationFromTree(
            Transcription trans, DefaultMutableTreeNode root) {
    		return createAnnotationFromTree(trans, root, false);
    }
    
    /**
     * (Re)creates an annotation with all depending annotations from the
     * information  contained in the specified Node. <br>
     * Suitable for annotations on a root tier or any other tier where only
     * one  annotation has to be recreated.
     *
     * @param trans the Transcription to work on
     * @param root the rootnode containing the data objects for the annotations
     * @param includeID whether or not to restore the annotation id from the record,
     * the default is false
     *
     * @return the created (root) annotation or null
     */
    public static AbstractAnnotation createAnnotationFromTree(
        Transcription trans, DefaultMutableTreeNode root, boolean includeID) {
        if ((trans == null) || (root == null)) {
            return null;
        }

        AbstractAnnotation annotation = null;

        DefaultMutableTreeNode node;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        /* create new annotations
         * iterate twice over the depending annotations: on the first
         * pass only Annotations with a time alignable begin time are
         * created, on the second pass the rest is done.
         */
        Enumeration en = root.breadthFirstEnumeration();

        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        long begin;
        long end;
        int linStereoType = -1;

        while (en.hasMoreElements()) {
            aa = null; //reset
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (annData.isBeginTimeAligned()) {
                    begin = annData.getBeginTime();
                    end = annData.getEndTime();

                    // this sucks... sometimes an annotation can have the same begin and 'virtual'
                    // end time on a time-subdivision tier
                    if (!annData.isEndTimeAligned() && (end == begin)) {
                        end++;
                    }
                    // if the annotation to create was the first in a series of unaligned annotations 
                    // on a time alignable tier, use createAnnotationBefore
                    if (!annData.isEndTimeAligned() && tier.getLinguisticType().getConstraints() != null && 
                    		tier.getLinguisticType().getConstraints().supportsInsertion()) {
                    	AlignableAnnotation curAnn = (AlignableAnnotation) tier.getAnnotationAtTime(annData.getBeginTime());
                    	if (curAnn != null && curAnn.getBeginTimeBoundary() == annData.getBeginTime()) {
                    		aa = (AlignableAnnotation) tier.createAnnotationBefore(curAnn);
                    	} else {
                    		aa = (AlignableAnnotation) tier.createAnnotation(begin, end);
                    	}
                    } else {
                    	aa = (AlignableAnnotation) tier.createAnnotation(begin, end);
                    }

                    if (node == root) {
                        annotation = aa;
                    }

                    if (aa != null) {
                        aa.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	aa.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeID) {
                        	aa.setId(annData.getId());
                        }

                        if (aa instanceof SVGAlignableAnnotation &&
                                annData instanceof SVGAnnotationDataRecord) {
                            SVGAnnotationDataRecord svgRec = (SVGAnnotationDataRecord) annData;

                            if (svgRec.getShape() != null) {
                                ((SVGAlignableAnnotation) aa).setShape(svgRec.getShape());
                            }

                            if (svgRec.getSvgElementId() != null) {
                                ((SVGAlignableAnnotation) aa).setSVGElementID(svgRec.getSvgElementId());
                            }
                        }
                    } else {
                        LOG.severe(
                            "Alignable annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime() + " et: " +
                            annData.getEndTime());
                    }
                }
            } else {
                // non-alignable in second run
            }
        }

        // second run
        en = root.breadthFirstEnumeration();

        // for re-creation of unaligned annotation on Alignable (Time-Subdivision) tiers
        Annotation prevAnn = null;

        while (en.hasMoreElements()) {
            aa = null; //reset
            an = null;
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();

            annData = (AnnotationDataRecord) node.getUserObject();

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals(annData.getTierName()) ||
                            (prevAnn.getEndTimeBoundary() <= annData.getBeginTime()))) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        end = annData.getEndTime();
                        an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                begin /*< end ? begin + 1 : begin*/);

                        if (an != null) {
                            aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                            prevAnn = aa;
                        } else {
                            // time subdivision of a time subdivision...
                            aa = (AlignableAnnotation) tier.createAnnotation(begin,
                                    end);
                            prevAnn = aa;
                        }
                    } else {
                        aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                        prevAnn = aa;
                    }

                    if (node == root) {
                        annotation = aa;
                    }

                    if (aa != null) {
                        aa.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	aa.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeID) {
                        	aa.setId(annData.getId());
                        }

                        // ?? shapes with unaligned annotations ??
                        if (aa instanceof SVGAlignableAnnotation &&
                                annData instanceof SVGAnnotationDataRecord) {
                            SVGAnnotationDataRecord svgRec = (SVGAnnotationDataRecord) annData;

                            if (svgRec.getShape() != null) {
                                ((SVGAlignableAnnotation) aa).setShape(svgRec.getShape());
                            }

                            if (svgRec.getSvgElementId() != null) {
                                ((SVGAlignableAnnotation) aa).setSVGElementID(svgRec.getSvgElementId());
                            }
                        }
                    } else {
                        LOG.severe(
                            "Alignable annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else {
                    //reset the prevAnn object when an aligned annotation is encountered
                    prevAnn = null;
                }
            } else {
                // ref annotations
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(annData.getTierName())) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        an = tier.getAnnotationAtTime(begin);

                        if (an != null) {
                            if (an.getBeginTimeBoundary() == begin) {
                                // the first annotation
                                ra = (RefAnnotation) tier.createAnnotationBefore(an);
                            } else {
                                ra = (RefAnnotation) tier.createAnnotationAfter(an);
                            }
                        } else {
                            ra = (RefAnnotation) tier.createAnnotation(begin,
                                    begin);
                        }

                        prevAnn = ra;
                    } else {
                        ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                        prevAnn = ra;
                    }

                    if (node == root) {
                        annotation = ra;
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeID) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    if (node == root) {
                        annotation = ra;
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeID) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                }
            }
        }

        // end second run
        return annotation;
    }

	/**
	 * @see #createAnnotationsSequentially(Transcription, ArrayList, boolean)
	 * @param trans the Transcription
	 * @param annotationsNodes an ArrayList containing a DefaultMutableTreeNode
	 *        for each annotation to recreate
	 */
    public static void createAnnotationsSequentially(Transcription trans,
        ArrayList annotationsNodes) {
    		createAnnotationsSequentially(trans, annotationsNodes, false);
    }
    
    /**
     * Creates a number of annotations with child-annotations in a sequence. <br>
     * Should handle the recreation of sequences of unaligned annotations on
     * either Time-Subdivision or Symbolic Subdivision tiers correctly.<br>
     * When annotations are only to be recreated on one tier (one level, no
     * child annotations) createAnnotationsSequentiallyDepthless can be used
     * instead.
     *
     * @param trans the Transcription to work on
     * @param annotationsNodes an ArrayList containing a DefaultMutableTreeNode
     *        for each annotation to recreate
     *
     * @see #createAnnotationsSequentiallyDepthless(Transcription, ArrayList)
     */
    public static void createAnnotationsSequentially(Transcription trans,
        ArrayList annotationsNodes, boolean includeId) {
        if ((trans == null) || (annotationsNodes == null) ||
                (annotationsNodes.size() == 0)) {
            return;
        }

        DefaultMutableTreeNode node;
        DefaultMutableTreeNode parentNode;
        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        LinguisticType linType = null;

        // first recreate aligned annotations
        for (int i = 0; i < annotationsNodes.size(); i++) {
            node = (DefaultMutableTreeNode) annotationsNodes.get(i);
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                AnnotationRecreator.createAnnotationFromTree(trans, node, includeId);
            }
        }

        // next recreate the rest
        for (int i = 0; i < annotationsNodes.size(); i++) {
            node = (DefaultMutableTreeNode) annotationsNodes.get(i);
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                // we already had this one
                continue;
            }

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            linType = tier.getLinguisticType();

            if ((linType.getConstraints() != null) &&
                    (linType.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                AnnotationRecreator.createAnnotationFromTree(trans, node, includeId);

                continue;
            }

            // tier is of a subdivision type...
            // count the number of unaligned annotations we have to create under a 
            // single parent annotation, horizontally....
            TierImpl parentTier = (TierImpl) tier.getParentTier();
            AbstractAnnotation parentAnn = (AbstractAnnotation) parentTier.getAnnotationAtTime(annData.getBeginTime());

            if (parentAnn == null) {
                LOG.severe(
                    "Cannot recreate annotations: parent annotation does not exist.");

                continue;
            }

            parentNode = new DefaultMutableTreeNode("parent");
            parentNode.add(node);

            for (; i < annotationsNodes.size(); i++) {
                node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                annData = (AnnotationDataRecord) node.getUserObject();

                if ((parentTier.getAnnotationAtTime(annData.getBeginTime()) == parentAnn) &&
                        !annData.isBeginTimeAligned()) {
                    parentNode.add(node);

                    if (i == (annotationsNodes.size() - 1)) {
                        AnnotationRecreator.createChildAnnotationsSkipRoot(trans,
                            parentNode, includeId);
                    }
                } else {
                    AnnotationRecreator.createChildAnnotationsSkipRoot(trans,
                        parentNode, includeId);
                    i--;

                    break;
                }
            }
        }
    }

    /**
     * Creates a number of unaligned child annotations that share the same
     * parent  annotation. Does not set the id of the annotations.
     *
     * @param trans the transcription
     * @param parentNode the parent node containing the nodes with the
     *        information necessary to recreate the child annotations
     *        @see #createChildAnnotationsSkipRoot(Transcription, DefaultMutableTreeNode, boolean)
     */
    static void createChildAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode parentNode) {
    		createChildAnnotationsSkipRoot(trans, parentNode, false);
    }
    
    /**
     * Creates a number of unaligned child annotations that share the same
     * parent  annotation.
     *
     * @param trans the transcription
     * @param parentNode the parent node containing the nodes with the
     *        information necessary to recreate the child annotations
     * @param includeId whether or not to (re)store the annotation id
     */
    static void createChildAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode parentNode, boolean includeId) {
        if ((trans == null) || (parentNode == null) ||
                (parentNode.getChildCount() == 0)) {
            return;
        }

        Annotation prevAnn = null;
        DefaultMutableTreeNode node;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        AnnotationDataRecord annData = null;
        TierImpl tier = null;

        long begin;
        int linStereoType = -1;

        Enumeration en = parentNode.breadthFirstEnumeration();

        // skip the empty root
        en.nextElement();

        while (en.hasMoreElements()) {
            aa = null; //reset
            an = null;
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();

            annData = (AnnotationDataRecord) node.getUserObject();

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals(annData.getTierName()) ||
                            (prevAnn.getEndTimeBoundary() <= annData.getBeginTime()))) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                begin /*< end ? begin + 1 : begin*/);

                        if (an != null) {
                            aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                            prevAnn = aa;
                        } else {
                            // time subdivision of a time subdivision...
                            aa = (AlignableAnnotation) tier.createAnnotation(begin,
                                    annData.getEndTime());
                            prevAnn = aa;
                        }
                    } else {
                        aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                        prevAnn = aa;
                    }

                    if (aa != null) {
                        aa.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	aa.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeId) {
                        	aa.setId(annData.getId());
                        }

                        // ?? shapes with unaligned annotations ??
                        if (aa instanceof SVGAlignableAnnotation &&
                                annData instanceof SVGAnnotationDataRecord) {
                            SVGAnnotationDataRecord svgRec = (SVGAnnotationDataRecord) annData;

                            if (svgRec.getShape() != null) {
                                ((SVGAlignableAnnotation) aa).setShape(svgRec.getShape());
                            }

                            if (svgRec.getSvgElementId() != null) {
                                ((SVGAlignableAnnotation) aa).setSVGElementID(svgRec.getSvgElementId());
                            }
                        }
                    } else {
                        LOG.severe(
                            "Alignable annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else {
                    //should nor happen; all annotations should be unaligned in this case
                    prevAnn = null;
                }
            } else {
                // ref annotations
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(annData.getTierName())) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    // should not be necessary here
                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                        prevAnn = ra;
                    } else {
                        ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                        prevAnn = ra;
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeId) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeId) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                }
            }
        }
    }
    
    /**
     * @see #createAnnotationsSequentiallyDepthless(Transcription, ArrayList, boolean)
     * @param trans the transcription
     * @param annotationsRecords the annotation records
     */
    public static void createAnnotationsSequentiallyDepthless(
        Transcription trans, ArrayList annotationsRecords) {
    		createAnnotationsSequentiallyDepthless(trans, annotationsRecords, false);
    }

    /**
     * Creates a number of annotations in a sequence without creating child
     * annotations. <br>
     * Should handle the recreation of sequences of unaligned annotations on
     * either Time-Subdivision or Symbolic Subdivision tiers correctly.<br>
     * Depthless means that annotations are only created on one level, no
     * information on child annotations is to be expected (like a tree without
     * branches). The arraylist does not contain DefaultMutableTreeNode
     * objects but Lists of AnnotationDataRecord objects instead; this
     * prevents the unnecessary creation  of DefaultMutableTreeNode objects,
     * as well as unnecessary checks on the existence of child annotations.
     * Useful for tokenizations.
     *
     * @param trans the Transcription to work on
     * @param annotationsRecords an ArrayList containing ArrayLists of
     *        AnnotationDataRecord objects for each annotation to recreate,
     *        all 'siblings' grouped in one list
     * @param includeId whether or not to restore the annotation id. 
     * 
     * @see #createAnnotationsSequentially(Transcription, ArrayList)
     */
    public static void createAnnotationsSequentiallyDepthless(
        Transcription trans, ArrayList annotationsRecords, boolean includeId) {
        if ((trans == null) || (annotationsRecords == null) ||
                (annotationsRecords.size() == 0)) {
            return;
        }

        ArrayList siblingList = null;

        for (int i = 0; i < annotationsRecords.size(); i++) {
            siblingList = (ArrayList) annotationsRecords.get(i);
            createSiblingAnnotations(trans, siblingList, includeId);
        }
    }

    /**
     * Creates a number of unaligned child annotations that share the same
     * parent annotation and without further child annotations (one level).
     *
     * @param trans the transcription
     * @param siblings the list containing the information necessary to
     *        recreate the child annotations
     * @param includeId whether to restore the annotation id
     */
    static void createSiblingAnnotations(Transcription trans, ArrayList siblings, boolean includeId) {
        if ((trans == null) || (siblings == null) || (siblings.size() == 0)) {
            return;
        }

        Annotation prevAnn = null;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        long begin;
        int linStereoType = -1;

        for (int i = 0; i < siblings.size(); i++) {
            aa = null; //reset
            an = null;
            ra = null;
            annData = (AnnotationDataRecord) siblings.get(i);

            // only get the tier once...
            if (tier == null) {
                tier = (TierImpl) trans.getTierWithId(annData.getTierName());
            }

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                return;
            }

            if (tier.isTimeAlignable()) {
                if (annData.isBeginTimeAligned()) {
                    // only the first annotation
                    aa = (AlignableAnnotation) tier.createAnnotation(annData.getBeginTime(),
                            annData.getEndTime());
                    prevAnn = aa;
                } else {
                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                begin /*< end ? begin + 1 : begin*/);

                        if (an != null) {
                            aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                            prevAnn = aa;
                        }
                    } else {
                        aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                        prevAnn = aa;
                    }
                }

                if (aa != null) {
                    aa.setValue(annData.getValue());
                    
                    if (annData.getExtRef() != null) {
                    	aa.setExtRef(annData.getExtRef());
                    }
                    
                    if (includeId) {
                    	aa.setId(annData.getId());
                    }

                    // ?? check for alignment ??
                    if (aa instanceof SVGAlignableAnnotation &&
                            annData instanceof SVGAnnotationDataRecord) {
                        SVGAnnotationDataRecord svgRec = (SVGAnnotationDataRecord) annData;

                        if (svgRec.getShape() != null) {
                            ((SVGAlignableAnnotation) aa).setShape(svgRec.getShape());
                        }

                        if (svgRec.getSvgElementId() != null) {
                            ((SVGAlignableAnnotation) aa).setSVGElementID(svgRec.getSvgElementId());
                        }
                    }
                } else {
                    LOG.severe("Alignable annotation could not be recreated: " +
                        annData.getValue() + " bt: " + annData.getBeginTime());
                }
            } else {
                // ref annotations					
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(annData.getTierName())) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                        prevAnn = ra;
                    } else {
                        ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                        prevAnn = ra;
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeId) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                        
                        if (includeId) {
                        	ra.setId(annData.getId());
                        }
                    } else {
                        LOG.severe(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                }
            }
        }
    }

    /**
     * Creates SVGAnnotationDataRecord objects of every SVGAlignableAnnotation
     * referencing  a graphical object, on every tier of the specified LinguisticType.<br>
     * Annotations that do not reference a graphical object (yet) are being
     * skipped!
     *
     * @param transcription the Transcription
     * @param type the LinguisticType
     *
     * @return an ArrayList of SVGAnnotationDataRecord objects or null
     */
    public static ArrayList storeGraphicsData(Transcription transcription,
        LinguisticType type) {
        if ((transcription == null) || (type == null)) {
            return null;
        }

        ArrayList storedGraphics = new ArrayList();
        Vector tiers = null;

        tiers = transcription.getTiersWithLinguisticType(type.getLinguisticTypeName());

        ArrayList temp;
        TierImpl curTier;

        for (int i = 0; i < tiers.size(); i++) {
            curTier = (TierImpl) tiers.get(i);
            temp = AnnotationRecreator.storeGraphicsData(transcription, curTier);

            if (temp != null) {
                storedGraphics.addAll(temp);
            }
        }

        return storedGraphics;
    }

    /**
     * Creates SVGAnnotationDataRecord objects of every SVGAlignableAnnotation
     * referencing  a graphical object on the specified tier.<br>
     * Annotations that do not reference a graphical object (yet) are being
     * skipped!
     *
     * @param transcription the Transcription
     * @param tier the Tier
     *
     * @return an ArrayList of SVGAnnotationDataRecord objects or null
     */
    public static ArrayList storeGraphicsData(Transcription transcription,
        TierImpl tier) {
        if ((transcription == null) || (tier == null)) {
            return null;
        }

        ArrayList dataRecords = new ArrayList();

        if (!tier.isTimeAlignable()) {
            return dataRecords;
        }

        Vector annos = tier.getAnnotations();
        Annotation aa;
        SVGAlignableAnnotation svgAa;

        for (int i = 0; i < annos.size(); i++) {
            aa = (Annotation) annos.get(i);

            if (aa instanceof SVGAlignableAnnotation) {
                svgAa = (SVGAlignableAnnotation) aa;

                // only store it if there actually is a Shape
                if (svgAa.getShape() != null) {
                    dataRecords.add(new SVGAnnotationDataRecord(svgAa));
                }
            }
        }

        return dataRecords;
    }

    /**
     * Restores graphical objects to SVGAlignableAnnotations.<br>
     * The Annotations themselves are not (re)created; it is assumed (and
     * checked)  that they exist.
     *
     * @param transcription the Transcription
     * @param graphicsDataRecords an Arraylist containing
     *        SVGAnnotationDataRecords
     */
    public static void restoreGraphicsData(Transcription transcription,
        ArrayList graphicsDataRecords) {
        if ((transcription == null) || (graphicsDataRecords == null)) {
            return;
        }

        TierImpl curTier;
        String tierName;
        Annotation aa;
        SVGAlignableAnnotation svgAa;
        SVGAnnotationDataRecord record;

        for (int i = 0; i < graphicsDataRecords.size(); i++) {
            record = (SVGAnnotationDataRecord) graphicsDataRecords.get(i);
            tierName = record.getTierName();
            curTier = (TierImpl) transcription.getTierWithId(tierName);

            aa = curTier.getAnnotationAtTime(record.getBeginTime());

            if (aa instanceof SVGAlignableAnnotation) {
                svgAa = (SVGAlignableAnnotation) aa;
                svgAa.setShape(record.getShape());
                svgAa.setSVGElementID(record.getSvgElementId());
            }
        }
    }
}
