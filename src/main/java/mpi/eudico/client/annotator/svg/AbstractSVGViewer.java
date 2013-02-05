package mpi.eudico.client.annotator.svg;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;
import mpi.eudico.client.annotator.viewer.MultiTierViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;


import java.awt.Color;
import java.awt.Graphics2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;


/**
 * An abstract class that handles (part of) the managing of svg annotations.
 *
 * @author Han Sloetjes
 * @version july 2004
 * @version Aug 2005 Identity removed
 */
public abstract class AbstractSVGViewer extends AbstractViewer
    implements MultiTierViewer, ACMEditListener {
    /** the transcription */
    Transcription transcription;

    /** list that contains the graphic tiers */
    ArrayList allGraphicTiers;

    /** the color to use for the stroke of 2d annotations */
    public final Color STROKE_COLOR = Color.red;

    /**
     * Creates a new AbstractSVGViewer instance
     *
     * @param transcription the transcription
     */
    public AbstractSVGViewer(Transcription transcription) {
        this.transcription = transcription;

        // parse the svg file
        SVGParserAndStore.parse(transcription);

        allGraphicTiers = new ArrayList();
        initViewer();
    }

    /**
     * Initializes the viewer by extracting the graphical annotations.
     */
    void initViewer() {
        TierImpl tier;
        GraphicTier2D tier2d;

        Iterator tierIt = transcription.getTiers().iterator();

        while (tierIt.hasNext()) {
            tier = (TierImpl) tierIt.next();
            tier2d = createTier2D(tier);

            if (tier2d != null) {
                allGraphicTiers.add(tier2d);
            }
        }
    }

    /**
     * Extract tiers with graphic annotations and create Graphic nodes from
     * these annotations.
     *
     * @param tier the tier to examine
     *
     * @return a GraphicsTier2D
     */
    GraphicTier2D createTier2D(TierImpl tier) {
        if ((tier == null) || (tier.getLinguisticType() == null) ||
                !tier.getLinguisticType().hasGraphicReferences()) {
            return null;
        }

        GraphicTier2D tier2d = new GraphicTier2D(tier);
        Vector annotations = tier.getAnnotations();
        Iterator annIt = annotations.iterator();
        Annotation a;
        SVGAlignableAnnotation ann;

        while (annIt.hasNext()) {
            a = (Annotation) annIt.next();

            if (!(a instanceof SVGAlignableAnnotation)) {
                break;
            }

            ann = (SVGAlignableAnnotation) a;

            if (ann.getShape() != null) {
                GraphicNode2D node2d = new GraphicNode2D(ann, ann.getShape());
                tier2d.insertNode(node2d);
            }
        }

        return tier2d;
    }

    /**
     * Update the active annotation.
     */
    public void updateActiveAnnotation() {
    }

    /**
     * When a tier is set invisible in the multitierviewers don't render the
     * graphics.
     *
     * @param tiers the visible tiers
     */
    public void setVisibleTiers(Vector tiers) {
        GraphicTier2D tier2d;

        for (int i = 0; i < allGraphicTiers.size(); i++) {
            tier2d = (GraphicTier2D) allGraphicTiers.get(i);

            if (tiers.contains(tier2d.getTier())) {
                tier2d.setVisible(true);
            } else {
                tier2d.setVisible(false);
            }
        }

        requestRepaint();
    }

    //////////
    // acm edit event handling methods
    /////////

    /**
     * A tier has been added.
     *
     * @param tier the new tier
     */
    void tierAdded(TierImpl tier) {
        GraphicTier2D tier2d = createTier2D(tier);

        if (tier2d != null) {
            allGraphicTiers.add(tier2d);
            requestRepaint();
        }
    }

    /**
     * A tier has been removed.
     *
     * @param tier the removed tier
     */
    void tierRemoved(TierImpl tier) {
        if ((tier != null) && (tier.getLinguisticType() != null) &&
                tier.getLinguisticType().hasGraphicReferences()) {
            GraphicTier2D tier2d = null;

            for (int i = 0; i < allGraphicTiers.size(); i++) {
                tier2d = (GraphicTier2D) allGraphicTiers.get(i);

                if (tier2d.getTier() == tier) {
                    allGraphicTiers.remove(i);
                    requestRepaint();

                    return;
                }
            }
        }
    }

    /**
     * An annotation has been added.<br>
     * Creation of an annotation can effect existing annotations on the same
     * tier and/or dependent tiers. Just reextract the graphic objects  from
     * these tiers.  In Shift mode all tiers could be changed, so all tracks
     * will be  recreated in that case.
     *
     * @param annotation the new annotation
     */
    void annotationAdded(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            int mode = transcription.getTimeChangePropagationMode();

            if (mode != Transcription.SHIFT) {
                TierImpl tier = (TierImpl) annotation.getTier();
                Vector depTiers = tier.getDependentTiers();
                GraphicTier2D tier2d = null;

                for (int i = 0; i < allGraphicTiers.size(); i++) {
                    tier2d = (GraphicTier2D) allGraphicTiers.get(i);

                    if ((tier2d.getTier() == tier) ||
                            depTiers.contains(tier2d.getTier())) {
                        reextractNodesForTier2D(tier2d);
                    }
                }
            } else {
                transcriptionChanged();

                return;
            }

            /*
               if (tier2d != null) {
                   //String svgId = annotation.getSVGElementID();
                   if (annotation.getShape() != null) {
                       GraphicNode2D node2d = new GraphicNode2D(annotation,
                               annotation.getShape());
                       tier2d.insertNode(node2d);
                   }
                   requestRepaint();
               }
             */
            requestRepaint();
        }
    }

    /**
     * Check every currently present tier with graphical annotations.  We could
     * assume no tiers have been added and no tiers have been removed. There
     * are other events for that kind of actions. Doublecheck to be sure.
     */
    void transcriptionChanged() {
        Vector tiers = transcription.getTiers();
        GraphicTier2D tier2d;

        for (int i = 0; i < allGraphicTiers.size(); i++) {
            tier2d = (GraphicTier2D) allGraphicTiers.get(i);

            if (tiers.contains(tier2d.getTier())) {
                reextractNodesForTier2D(tier2d);
            }
        }

        requestRepaint();
    }

    /**
     * Checks if the linguistic type of any of the current tiers with graphic
     * references has been changed to not allow graphic references.
     */
    void linguisticTypeChanged() {
        GraphicTier2D tier2d;
        LinguisticType type;

        Vector tiers = transcription.getTiers();
        TierImpl tier;

        for (int i = 0; i < tiers.size(); i++) {
            tier = (TierImpl) tiers.get(i);
            type = tier.getLinguisticType();

            if ((type != null) && type.hasGraphicReferences()) {
                boolean alreadyThere = false;

                for (int j = 0; j < allGraphicTiers.size(); j++) {
                    tier2d = (GraphicTier2D) allGraphicTiers.get(j);

                    if (tier2d.getTier() == tier) {
                        alreadyThere = true;

                        break;
                    }
                }

                if (!alreadyThere) {
                    tierAdded(tier);
                }
            } else {
                // if the tier was there before, remove it
                for (int j = 0; j < allGraphicTiers.size(); j++) {
                    tier2d = (GraphicTier2D) allGraphicTiers.get(j);

                    if (tier2d.getTier() == tier) {
                        allGraphicTiers.remove(j);
                        requestRepaint();

                        break;
                    }
                }
            }
        }

        /*
           for (int i = 0; i < allGraphicTiers.size(); i++) {
               tier2d = (GraphicTier2D) allGraphicTiers.get(i);
               type = tier2d.getTier().getLinguisticType();
               if ((type == null) || !type.hasGraphicReferences()) {
                   allGraphicTiers.remove(i);
                   i--;
               }
           }
         */
        requestRepaint();
    }

    /**
     * An annotation's begin and/or end time has changed.  Changing the begin
     * and/or end time of an annotation can effect  existing annotations on
     * the same tier and/or dependent tiers.  Just reextract the graphic
     * objects from these tiers.  In Shift mode all tiers could be changed, so
     * all tracks will be  recreated in that case.
     *
     * @param annotation the annotation
     */
    void annotationTimeChanged(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            int mode = transcription.getTimeChangePropagationMode();

            if (mode != Transcription.SHIFT) {
                TierImpl tier = (TierImpl) annotation.getTier();
                Vector depTiers = tier.getDependentTiers();
                GraphicTier2D tier2d = null;

                for (int i = 0; i < allGraphicTiers.size(); i++) {
                    tier2d = (GraphicTier2D) allGraphicTiers.get(i);

                    if ((tier2d.getTier() == tier) ||
                            depTiers.contains(tier2d.getTier())) {
                        reextractNodesForTier2D(tier2d);
                    }
                }
            } else {
                transcriptionChanged();

                return;
            }

            requestRepaint();
        }
    }

    /**
     * A graphic object has been edited.
     *
     * @param annotation the edited annotation
     */
    void annotationGraphicChanged(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            GraphicTier2D tier2d = null;

            for (int i = 0; i < allGraphicTiers.size(); i++) {
                tier2d = (GraphicTier2D) allGraphicTiers.get(i);

                if (tier2d.getTier() == annotation.getTier()) {
                    break;
                }
            }

            if (tier2d != null) { // the tier was already present

                int index = Collections.binarySearch(tier2d.getNodeList(),
                        new Long(annotation.getBeginTimeBoundary()));

                if (index >= 0) {
                    GraphicNode2D node2d = (GraphicNode2D) tier2d.getNodeList()
                                                                 .get(index);

                    // since the annotation's begin and endtime have not changed, this should return the right node
                    if (node2d.getAnnotation() == annotation) {
                        if (annotation.getShape() == null) {
                            //remove the graphicnode
                            tier2d.removeNode(node2d);
                        } else {
                            node2d.setShape(annotation.getShape());
                        }

                        requestRepaint();
                    }
                } else {
                    if (annotation.getShape() != null) {
                        GraphicNode2D node2d = new GraphicNode2D(annotation,
                                annotation.getShape());
                        tier2d.insertNode(node2d);
                        requestRepaint();
                    }
                }
            } else { //the tier's linguistic type has changed
                tier2d = createTier2D((TierImpl) annotation.getTier());

                if (tier2d != null) {
                    allGraphicTiers.add(tier2d);

                    if (annotation.getShape() != null) {
                        GraphicNode2D node2d = new GraphicNode2D(annotation,
                                annotation.getShape());
                        tier2d.insertNode(node2d);
                        requestRepaint();
                    }
                }
            }
        }
    }

    /**
     * Reextracts the nodes for the specified tier.
     *
     * @param tier2d the effected tier2d
     */
    void reextractNodesForTier2D(GraphicTier2D tier2d) {
        if ((tier2d == null) || (tier2d.getTier() == null)) {
            return;
        }

        Vector annotations = tier2d.getTier().getAnnotations();

        // throw away everything
        tier2d.getNodeList().clear();

        Iterator annIt = annotations.iterator();
        SVGAlignableAnnotation ann;

        while (annIt.hasNext()) {
            ann = (SVGAlignableAnnotation) annIt.next();

            if (ann.getShape() != null) {
                GraphicNode2D node2d = new GraphicNode2D(ann, ann.getShape());
                tier2d.insertNode(node2d);
            }
        }
    }

    /**
     * Update of the locale.
     */
    public void updateLocale() {
    }

    /**
     * Sets the active tier. Handled in repainting.
     *
     * @param tier the active tier
     */
    public void setActiveTier(Tier tier) {
    }

    /**
     * Stub; no multi-tiercontrolpanel connected.
     *
     * @param controller the control panel
     */
    public void setMultiTierControlPanel(MultiTierControlPanel controller) { /* ignore */
    }

    /**
     * Check if anything has changed on a tier that allows graphic references.
     *
     * @param e the edit event
     */
    public void ACMEdited(ACMEditEvent e) {
        //System.out.println("ACMEdited:: operation: " + e.getOperation() + ", invalidated: " + e.getInvalidatedObject());
        //System.out.println("\tmodification: " + e.getModification() + ", source: " + e.getSource());
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierAdded((TierImpl) e.getModification());
            }

            break;

        case ACMEditEvent.REMOVE_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierRemoved((TierImpl) e.getModification());
            }

            break;

        case ACMEditEvent.ADD_ANNOTATION_BEFORE:

        // fallthrough: on time-subdivision tiers this can effect the boundaries
        // of other annotations
        // or should unaligned annotations on time-sub tiers not allow graphic refs??
        case ACMEditEvent.ADD_ANNOTATION_AFTER:

        case ACMEditEvent.ADD_ANNOTATION_HERE:

            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof SVGAlignableAnnotation) {
                annotationAdded((SVGAlignableAnnotation) e.getModification());
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATIONS:

            if (e.getInvalidatedObject() instanceof Transcription) {
                transcriptionChanged();
            }

            break;

        case ACMEditEvent.REMOVE_ANNOTATION: /* complex modification, transcription is invalidated */

            if (e.getModification() instanceof Annotation) {
            } else if (e.getInvalidatedObject() instanceof Transcription) {
                transcriptionChanged();
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATION_TIME:

            if (e.getModification() instanceof SVGAlignableAnnotation) {
                annotationTimeChanged((SVGAlignableAnnotation) e.getModification());
            }

            break;

        case ACMEditEvent.REMOVE_LINGUISTIC_TYPE:

        // fallthrough...
        //break;
        case ACMEditEvent.CHANGE_LINGUISTIC_TYPE:
            linguisticTypeChanged();

            break;

        case ACMEditEvent.CHANGE_ANNOTATION_GRAPHICS:

            if (e.getModification() instanceof SVGAlignableAnnotation) {
                annotationGraphicChanged((SVGAlignableAnnotation) e.getModification());
            }

            break;

        default:
            break;
        }
    }

    /**
     * Do not react on Controller events.  The implementing class can choose
     * whether to react on the event or not. Painting of the annotations is
     * probably  triggered by a player effect or renderer.
     *
     * @param event the controller event
     */
    public void controllerUpdate(ControllerEvent event) {
    }

    /**
     * Stub.
     */
    public void updateSelection() {
    }

    /**
     * 
     */
	public void preferencesChanged() {
		// method stub		
	}
	
    /**
     * Requests the media player to repaint the current frame.<br>
     * Called e.g. after edit operations etc.
     */
    abstract void requestRepaint();

    /**
     * Paint the annotations to the specified Graphics object.
     *
     * @param big2d the Graphics object
     */
    abstract void paintAnnotations(Graphics2D big2d);

    /**
     * Paint the annotations. Implementation depends on the player / viewer
     * structure.
     */
    abstract void paintAnnotations();
}
