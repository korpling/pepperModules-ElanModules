package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.svg.SVGAnnotationDataRecord;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;

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
import java.util.HashMap;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * A class to create copies of annotations on copies of tiers. Two main issues: <br>
 * - the tier names that have been stored in AnnotationDataRecords have to be
 * mapped to the names of the copies of the tiers <br>
 * - if the toplevel tier of the copies is not an independent tier a suitable
 * parent annotation has to be found for each annotation copy.
 */
public class TierCopier implements ClientLogger {
    /** a constant for copying to a tier of the same stereotype */
    public static final int SAME = 0;

    /** transition from tier of any stereotype to root */
    public static final int ANY_TO_ROOT = 1;

    /** transition from root tier to time subdivision tier */
    public static final int ROOT_TO_TIMESUB = 2;

    /** transition from root tier to symbolic subdivision tier */
    public static final int ROOT_TO_SYMSUB = 3;

    /** transition from root tier to symbolic association tier */
    public static final int ROOT_TO_ASSOC = 4;

    /** transition from time subdivision tier to symbolic subdivision tier */
    public static final int TIMESUB_TO_SYMSUB = 5;

    /** transition from time subdivision tier to symbolic association tier! */
    public static final int TIMESUB_TO_ASSOC = 6;

    /** transition from symbolic subdivision tier to time subdivision tier */
    public static final int SYMSUB_TO_TIMESUB = 7;

    /** transition from symbolic subdivision tier to symbolic association tier  */
    public static final int SYMSUB_TO_ASSOC = 8;

    /** transition from symbolic association tier to time subdivision tier  */
    public static final int ASSOC_TO_TIMESUB = 9;

    /** transition from symbolic association tier to symbolic subdivision tier  */
    public static final int ASSOC_TO_SYMSUB = 10;
    
    public static final int ROOT_TO_INCLUDED_IN = 11;
    
    public static final int TIMESUB_TO_INCLUDED_IN = 12;
    
    public static final int SYMSUB_TO_INCLUDED_IN = 13;
    
    public static final int ASSOC_TO_INCLUDED_IN = 14;
    
    public static final int INCLUDED_IN_TO_TIMESUB = 15;
    
    public static final int INCLUDED_IN_TO_SYMSUB = 16;
    
    public static final int INCLUDED_IN_TO_ASSOC = 17;
    
    private boolean keepSiblingsTogether = false;
    private boolean appendSubdivisionSiblings = true;
    /**
     * Creates a new TierCopier instance
     */
    public TierCopier() {
    }

    /**
     * Suitable for recreation of annotations on root tiers or timesubdivision
     * tiers.
     *
     * @param trans the transcription
     * @param root the 'root' node
     * @param tierMapping old names to new names mapping
     *
     * @return the annotation for 'rootNode'
     */
    public AbstractAnnotation createAnnotationFromTree(Transcription trans,
        DefaultMutableTreeNode root, HashMap tierMapping) {
        if ((trans == null) || (root == null) || (tierMapping == null)) {
            return null;
        }

        AbstractAnnotation annotation = null;
        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        long begin;
        long end;
        int linStereoType = -1;
        long[] timeBounds = new long[] { 0, 0 };

        //
        // find a parent annotation if the top level copy-tier is not a root
        annData = (AnnotationDataRecord) root.getUserObject();
        tierName = (String) tierMapping.get(annData.getTierName());
        tier = (TierImpl) trans.getTierWithId(tierName);

        if (tier == null) {
            LOG.warning(
                "Cannot recreate annotations: tier copy does not exist: " +
                tierName);

            return null;
        }

        if (tier.hasParentTier()) {
            Vector overlap = ((TierImpl) tier.getParentTier()).getOverlappingAnnotations(annData.getBeginTime(),
                    annData.getEndTime());

            if (overlap.size() > 0) {
                long overl = 0;
                int index = 0;
                for (int i = 0; i < overlap.size(); i++) {
                    an = (Annotation) overlap.get(i);
                   long ol = 0;
                   if (an.getBeginTimeBoundary() > annData.getBeginTime()) {
                       if (an.getEndTimeBoundary() > annData.getEndTime()) {
                           ol = annData.getEndTime() - an.getBeginTimeBoundary();
                       } else {
                           ol = an.getEndTimeBoundary() - an.getBeginTimeBoundary();
                       }
                   } else {
                       if (an.getEndTimeBoundary() > annData.getEndTime()) {
                           ol = annData.getEndTime() - annData.getBeginTime(); 
                       } else {
                           ol = an.getEndTimeBoundary() - annData.getBeginTime();
                       }
                   }
                   if (ol > overl) {
                       overl = ol;
                       index = i;
                   }
                    /*
                    if (tier.getOverlappingAnnotations(
                                an.getBeginTimeBoundary(),
                                an.getEndTimeBoundary()).size() == 0) {
                        timeBounds[0] = an.getBeginTimeBoundary();
                        timeBounds[1] = an.getEndTimeBoundary();

                        break;
                    }*/
                }
                an = (Annotation) overlap.get(index);
                timeBounds[0] = an.getBeginTimeBoundary();
                timeBounds[1] = an.getEndTimeBoundary();
                
                if ((timeBounds[0] == 0) && (timeBounds[1] == 0)) {
                    return null;
                }
            } else {
                return null; // no overlap, no annotation
            }
        }

        //
        Enumeration en = root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            aa = null; //reset
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (annData.isBeginTimeAligned()) {
                    begin = annData.getBeginTime();
                    end = annData.getEndTime();

                    // correct to fit in the parentbounds
                    if (begin < timeBounds[0]) {
                        begin = timeBounds[0];
                    }

                    if (end > timeBounds[1]) {
                        end = timeBounds[1];
                    }

                    // should nor happen anymore; sometimes an annotation can have the same begin and 'virtual'
                    // end time on a time-subdivision tier
                    if (!annData.isEndTimeAligned() && (end == begin)) {
                        end++;
                    }

                    aa = (AlignableAnnotation) tier.createAnnotation(begin, end);

                    if (node == root) {
                        annotation = aa;
                    }

                    if (aa != null) {
                        aa.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	aa.setExtRef(annData.getExtRef());
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
                        LOG.warning(
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

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals((String) tierMapping.get(
                                    annData.getTierName())) ||
                            (prevAnn.getEndTimeBoundary() <= annData.getBeginTime()))) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        end = annData.getEndTime();

                        // correct to fit in the parentbounds
                        if (begin < timeBounds[0]) {
                            begin = timeBounds[0];
                        }

                        if (end > timeBounds[1]) {
                            end = timeBounds[1];
                        }

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
                        LOG.warning(
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

                    // correct to fit in the parentbounds
                    if ((begin < timeBounds[0]) &&
                            (annData.getEndTime() > timeBounds[0])) {
                        begin = timeBounds[0];
                    }

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals((String) tierMapping.get(
                                    annData.getTierName()))) {
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
                    } else {
                        LOG.warning(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                        System.out.println("Begin: " + begin);
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    if ((begin < timeBounds[0]) &&
                            (annData.getEndTime() > timeBounds[0])) {
                        begin = timeBounds[0];
                    }

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
                    } else {
                        LOG.warning(
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
     * Suitable for recreation of annotations on root tiers. Almost identical
     * to AnnotationRecreator.createRootAnnotationFromTree(), but  assumes
     * that the root annotation is created on a toplevel tier (no parent).
     *
     * @param trans the transcription
     * @param root the rootnode
     * @param tierMapping mappings of old tier name to new tier names
     *
     * @return the 'root' annotation
     */
    public AbstractAnnotation createRootAnnotationFromTree(
        Transcription trans, DefaultMutableTreeNode root, HashMap tierMapping) {
        if ((trans == null) || (root == null) || (tierMapping == null)) {
            return null;
        }

        AbstractAnnotation annotation = null;
        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        long begin;
        long end;
        int linStereoType = -1;

        annData = (AnnotationDataRecord) root.getUserObject();
        tierName = (String) tierMapping.get(annData.getTierName());
        tier = (TierImpl) trans.getTierWithId(tierName);

        if (tier == null) {
            LOG.warning(
                "Cannot recreate annotations: tier copy does not exist: " +
                tierName);
        }

        Enumeration en = root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            aa = null; //reset
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

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

                    aa = (AlignableAnnotation) tier.createAnnotation(begin, end);

                    if (node == root) {
                        annotation = aa;
                    }

                    if (aa != null) {
                        aa.setValue(annData.getValue());

                        if (annData.getExtRef() != null) {
                        	aa.setExtRef(annData.getExtRef());
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
                        LOG.warning(
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

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals((String) tierMapping.get(
                                    annData.getTierName())) ||
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
                        LOG.warning(
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
                            !prevAnn.getTier().getName().equals((String) tierMapping.get(
                                    annData.getTierName()))) {
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
                    } else {
                        LOG.warning(
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
                    } else {
                        LOG.warning(
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
     * Creates a number of related annotations (siblings) in one run. Not to be
     * used on root tiers.
     *
     * @param trans the transcription
     * @param group a group of annotations that belong together and should end
     *        up under the same (higher level) parent
     * @param tierMapping mappings of old tier name to new tier names
     */
    public void createAnnotationsSequentially(Transcription trans,
        ArrayList group, HashMap tierMapping) {
        if ((trans == null) || (group == null) || (group.size() == 0) ||
                (tierMapping == null)) {
            return;
        }

        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        LinguisticType linType = null;
        int linStereoType = -1;
        Annotation an = null;

        long[] timeBounds = new long[] { 0, 0 };

        node = (DefaultMutableTreeNode) group.get(0);
        annData = (AnnotationDataRecord) node.getUserObject();
        tierName = (String) tierMapping.get(annData.getTierName());
        tier = (TierImpl) trans.getTierWithId(tierName);

        if ((tier == null) || (tier.getParentTier() == null)) {
            LOG.warning(
                "Cannot recreate annotations: tier copy or it's parent does not exist: " +
                tierName);

            return;
        }

        linType = tier.getLinguisticType();

        if (linType.getConstraints() != null) {
            linStereoType = linType.getConstraints().getStereoType();
        }

        AnnotationDataRecord annData2 = (AnnotationDataRecord) ((DefaultMutableTreeNode) group.get(group.size() -
                1)).getUserObject();
//        Vector overlap = ((TierImpl) tier.getParentTier()).getOverlappingAnnotations(annData.getBeginTime(),
//                annData2.getEndTime());
        Vector overlap = ((TierImpl) tier.getRootTier()).getOverlappingAnnotations(annData.getBeginTime(),
                annData2.getEndTime());

        if (overlap.size() > 0) {
            for (int i = 0; i < overlap.size(); i++) {
                an = (Annotation) overlap.get(i);

                if (tier.getOverlappingAnnotations(an.getBeginTimeBoundary(),
                            an.getEndTimeBoundary()).size() == 0) {
                    timeBounds[0] = an.getBeginTimeBoundary();
                    timeBounds[1] = an.getEndTimeBoundary();

                    break;
                }
            }

            if ((timeBounds[0] == 0) && (timeBounds[1] == 0)) {
                return;
            }
        } else {
            return; // no overlap, no annotation
        }

        // time subdivision, symbolic subdivision and symbolic association could/should be treated in 
        // their own way, or better each 'transition type' should be handled differently
        if (linStereoType == Constraint.TIME_SUBDIVISION || linStereoType == Constraint.INCLUDED_IN) {
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode("p");

            for (int i = 0; i < group.size(); i++) {
                node = (DefaultMutableTreeNode) group.get(i);
                annData = (AnnotationDataRecord) node.getUserObject();
                
                // transition time-sub -> time-sub check aligned annotations only
                if (!((annData.getBeginTime() > timeBounds[1]) ||
                        (annData.getEndTime() < timeBounds[0]))) {
                    //group.remove(i);
                	if (!annData.isBeginTimeAligned() || !annData.isEndTimeAligned()) {
                		// the above times are virtual or inherited then (sym_sub to time_sub or included_in)
                		// change the record to be alignable for correct treatment in the next step 
                		annData.setBeginTimeAligned(true);
                		annData.setEndTimeAligned(true);
                	}
                    pNode.add(node);
                }
            }

            createTimeSubAnnotationsSkipRoot(trans, pNode, tierMapping);
        } else if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode("p");

            for (int i = 0; i < group.size(); i++) {
                node = (DefaultMutableTreeNode) group.get(i);
                pNode.add(node);
            }

            if (keepSiblingsTogether) {
            	adjustTimes(pNode, timeBounds[0], timeBounds[1]);
            }
            createSymSubAnnotationsSkipRoot(trans, pNode, tierMapping);
        } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode("p");

            for (int i = 0; i < group.size(); i++) {
                node = (DefaultMutableTreeNode) group.get(i);
                pNode.add(node);
            }
            if (keepSiblingsTogether) {
            	adjustTimes(pNode, timeBounds[0], timeBounds[1]);
            }
            createSymAssAnnotationsSkipRoot(trans, pNode, tierMapping);
        }
    }

    /**
     * Creates a number of sibling annotations; the root node is just a
     * container node. It is assumed that the annotations are created on a
     * time subdivision tier.
     *
     * @param trans the transcription
     * @param pNode the container node
     * @param tierMapping mappings of old tier name to new tier names
     */
    private void createTimeSubAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode pNode, HashMap tierMapping) {
        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;
        Annotation prevAnn = null;
        long begin;
        long mid;
        long end;
        int linStereoType = -1;
        long[] timeBounds = new long[] { 0, 0 };

        // first recreate aligned annotations
        Enumeration en = pNode.breadthFirstEnumeration();
        en.nextElement(); // skip root

        while (en.hasMoreElements()) {
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                //createAnnotationFromTree(trans, node, tierMapping);
                tierName = (String) tierMapping.get(annData.getTierName());
                tier = (TierImpl) trans.getTierWithId(tierName);

                if (tier == null) {
                    LOG.warning(
                        "Cannot recreate annotations: tier copy does not exist: " +
                        tierName);

                    return;
                }

                begin = annData.getBeginTime();
                end = annData.getEndTime();

                Vector overlap = ((TierImpl) tier.getParentTier()).getOverlappingAnnotations(begin,
                        end);

                if (overlap.size() > 0) {
                    an = (Annotation) overlap.get(0);
                    timeBounds[0] = an.getBeginTimeBoundary();
                    timeBounds[1] = an.getEndTimeBoundary();
                }

                if ((timeBounds[0] == 0) && (timeBounds[1] == 0)) {
                    continue;
                }

                // correct to fit in the parentbounds
                if (begin < timeBounds[0]) {
                    begin = timeBounds[0];
                }

                if (end > timeBounds[1]) {
                    end = timeBounds[1];
                }

                aa = (AlignableAnnotation) tier.createAnnotation(begin, end);

                if (aa != null) {
                    aa.setValue(annData.getValue());
                    
                    if (annData.getExtRef() != null) {
                    	aa.setExtRef(annData.getExtRef());
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
                    LOG.warning("Alignable annotation could not be recreated: " +
                        annData.getValue() + " bt: " + annData.getBeginTime() +
                        " et: " + annData.getEndTime());
                }
            }
        }

        // next recreate the rest
        en = pNode.breadthFirstEnumeration();
        en.nextElement(); // skip root

        while (en.hasMoreElements()) {
            aa = null; //reset
            an = null;
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                // we already had this one
                continue;
            }

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals(tierName) ||
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
                        LOG.info(
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
                    if (begin < timeBounds[0]) {
                        begin = timeBounds[0];
                    }

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(tierName)) {
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
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    end = annData.getEndTime();
                    if (begin < timeBounds[0]) {
                        begin = timeBounds[0];
                    }

                    if (end > timeBounds[1]) {
                        end = timeBounds[1];
                    }
                    mid = (begin + end) / 2;
                    an = tier.getAnnotationAtTime(mid);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(mid, mid);
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                }
            }
        }
    }

    /**
     * Creates a number of sibling annotations; the root node is just a
     * container node. It is assumed that the annotations are created on a
     * symbolic subdivision tier.
     *
     * @param trans the transcription
     * @param pNode the container node
     * @param tierMapping mappings of old tier name to new tier names
     */
    private void createSymSubAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode pNode, HashMap tierMapping) {
        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        RefAnnotation ra = null;
        Annotation an = null;
        Annotation prevAnn = null;
        long begin;
        long mid;
        int linStereoType = -1;

        // first recreate aligned annotations
        Enumeration en = pNode.breadthFirstEnumeration();
        en.nextElement(); // skip root

        while (en.hasMoreElements()) {
            an = null; //reset
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                LOG.warning("Tier should not be time alignable");
            } else {
                // ref annotations
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(tierName)) {
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
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    mid = (begin + annData.getEndTime()) / 2;
                    an = tier.getAnnotationAtTime(mid);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(mid, mid);
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                }
            }
        }
    }

    /**
     * Creates a number of sibling annotations; the root node is just a
     * container node. It is assumed that the annotations are created on a
     * symbolic association tier.  Incomplete...
     *
     * @param trans the transcription
     * @param pNode the container node
     * @param tierMapping mappings of old tier name to new tier names
     */
    private void createSymAssAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode pNode, HashMap tierMapping) {
        DefaultMutableTreeNode node;
        AnnotationDataRecord annData = null;
        String tierName = null;
        TierImpl tier = null;
        RefAnnotation ra = null;
        Annotation an = null;
        Annotation prevAnn = null;
        long begin;
        long mid;
        int linStereoType = -1;

        Enumeration en = pNode.breadthFirstEnumeration();
        en.nextElement(); // skip root

        while (en.hasMoreElements()) {
            an = null; //reset
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            tierName = (String) tierMapping.get(annData.getTierName());
            tier = (TierImpl) trans.getTierWithId(tierName);

            if (tier == null) {
                LOG.warning(
                    "Cannot recreate annotations: tier does not exist: " +
                    tierName);

                continue;
            }

            if (tier.isTimeAlignable()) {
                LOG.warning("Tier should not be time alignable");
            } else {
                // ref annotations
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();
                
                if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    mid = (begin + annData.getEndTime()) / 2;
                    an = tier.getAnnotationAtTime(mid);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(mid, mid);
                    } else if (appendSubdivisionSiblings){
                    	an.setValue(an.getValue() + " " + annData.getValue());
                    	continue;
                    }

                    if (ra != null) {
                        ra.setValue(annData.getValue());
                        
                        if (annData.getExtRef() != null) {
                        	ra.setExtRef(annData.getExtRef());
                        }
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } else if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(tierName)) {
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
                    } else {
                        LOG.info(
                            "Reference annotation could not be recreated: " +
                            annData.getValue() + " bt: " +
                            annData.getBeginTime());
                    }
                } 
            }
        }
    }

    /**
     * Calculates (new) begin and endtimes for a series of (unaligned) child
     * annotations  using the parent's begin and end times.
     *
     * @param node the parent node
     * @param bt the parents begin time
     * @param et the parents end time
     */
    private void adjustTimes(DefaultMutableTreeNode node, long bt, long et) {
        if (node == null) {
            return;
        }

        int numChildren = node.getChildCount();

        if (numChildren == 0) {
            return;
        }

        int perAnn = (int) (et - bt) / numChildren;
        DefaultMutableTreeNode n = null;
        AnnotationDataRecord annData = null;

        for (int i = 0; i < numChildren; i++) {
            n = (DefaultMutableTreeNode) node.getChildAt(i);
            annData = (AnnotationDataRecord) n.getUserObject();
            annData.setBeginTime(bt + (i * perAnn));
            annData.setEndTime(bt + ((i + 1) * perAnn));
            adjustTimes(n, annData.getBeginTime(), annData.getEndTime());
        }
    }
}
