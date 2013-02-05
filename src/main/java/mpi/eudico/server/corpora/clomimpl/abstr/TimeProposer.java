package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.event.ACMEditEvent;

import mpi.eudico.server.corpora.util.ACMEditableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * A class that calculates virtual times for unaligned annotations  on a
 * time-alignable tier. <br>
 * <b>Note: </b>the virtual times should be calculated and recalculated
 * <i>after</i> (not during) every relevant edit action.
 *
 * @author Han Sloetjes
 */
public class TimeProposer {
    /**
     * No-arg constructor. There might be a constructor with an Transcription
     * as an argument.
     */
    public TimeProposer() {
    }

    /**
     * Invokes the recalculation of the virtual times of unaligned TimeSlots.
     *
     * @param transcription the Transcription
     * @param source the invalidated editable object
     * @param operation the type of edit
     * @param modification the modified or modifying object
     */
    public void correctProposedTimes(TranscriptionImpl transcription,
        ACMEditableObject source, int operation, Object modification) {
        //System.out.println("TLV::ACMEdited:: operation: " + operation + ", invalidated: " + source);
        //System.out.println("\tmodification: " + modification);
        if ((transcription != null) &&
                (transcription.getTimeChangePropagationMode() != Transcription.NORMAL)) {
            correctAllProposedTimes(transcription);

            return;
        }

        Annotation modAnnotation;
        TierImpl invalidTier;

        switch (operation) {
        case ACMEditEvent.ADD_ANNOTATION_AFTER:
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
        case ACMEditEvent.ADD_ANNOTATION_HERE:
            modAnnotation = (Annotation) modification;
            invalidTier = (TierImpl) modAnnotation.getTier();

            if (!(modAnnotation instanceof AlignableAnnotation)) {
                return;
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATIONS:
        case ACMEditEvent.REMOVE_ANNOTATION:
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
            correctAllProposedTimes(transcription);

            return;

        default:
            return;
        }

        // when we reach this point correct proposed times for all effected slots
        AlignableAnnotation rootAnn = (AlignableAnnotation) modAnnotation;
        TierImpl rootTier = null;

        if (invalidTier.hasParentTier()) {
            boolean rootAnnFound = false;
            rootTier = (TierImpl) invalidTier.getParentTier();

            while (!rootAnnFound) {
                Vector annos = rootTier.getAnnotations();
                Iterator anIt = annos.iterator();
                AlignableAnnotation ann = null;

                while (anIt.hasNext()) {
                    ann = (AlignableAnnotation) anIt.next();

                    if (ann.getParentListeners().contains(rootAnn)) {
                        rootAnn = ann;

                        if (rootAnn.getBegin().isTimeAligned() &&
                                rootAnn.getEnd().isTimeAligned()) {
                            rootAnnFound = true;

                            break;
                        } else {
                            rootTier = (TierImpl) rootTier.getParentTier();

                            break;
                        }
                    }
                }
            }
        } else {
            //rootTier = invalidTier;
            // if this is a root tier there can be no unaligned annotations
            return;
        }

        // get an ordered list of relevant depending tiers
        //ArrayList orderedTiers = getRelevantOrderedTiers(rootTier);

        // calculate the proposed times, skip the root
        //calculateProposedTimes(orderedTiers, rootAnn);
        calculateProposedTimesForPL(rootAnn);
    }

    /**
     * Recalculates all unaligned slots of the Transcription.
     *
     * @param transcription the Transcription
     */
    private void correctAllProposedTimes(TranscriptionImpl transcription) {
        if (transcription == null) {
            return;
        }

        Vector allTiers = null;
        ArrayList allTierCopy = null;

        allTiers = transcription.getTiers();

        allTierCopy = new ArrayList(allTiers);

        TierImpl t;

        for (int i = allTierCopy.size() - 1; i >= 0; i--) {
            t = (TierImpl) allTierCopy.get(i);

            if (t.hasParentTier()) {
                allTierCopy.remove(i);
            }
        }

        if ((allTierCopy != null) && (allTierCopy.size() > 0)) {
            for (int i = 0; i < allTierCopy.size(); i++) {
                TierImpl tier = (TierImpl) allTierCopy.get(i);
                //ArrayList orderedTiers = getRelevantOrderedTiers(tier);

                Vector annos = tier.getAnnotations();
                Iterator anIt = annos.iterator();
                AbstractAnnotation aa;

                while (anIt.hasNext()) {
                    aa = (AbstractAnnotation) anIt.next();

                    if (aa instanceof AlignableAnnotation) {
                        //calculateProposedTimes(orderedTiers,
                        //    (AlignableAnnotation) aa);
                    	calculateProposedTimesForPL((AlignableAnnotation) aa);
                    }
                }
            }
        }
    }

    /**
     * Creates an arraylist of dependent time-alignable tiers, loosely ordered
     * by their hierarchical relationships (top-down).
     *
     * @param rootTier the root or parent tier
     *
     * @return an ArrayList of time-alignable Tiers
     */
    private ArrayList getRelevantOrderedTiers(TierImpl rootTier) {
        // remove tiers that are not time alignable
        Vector depTiers = rootTier.getDependentTiers();

        for (int i = depTiers.size() - 1; i >= 0; i--) {
            TierImpl t = (TierImpl) depTiers.get(i);

            if (!t.isTimeAlignable()) {
                depTiers.remove(i);
            }
        }

        // reorder
        ArrayList orderedTiers = new ArrayList(depTiers.size() + 1);
        orderedTiers.add(rootTier);

tierloop: 
        while (depTiers.size() > 0) {
            TierImpl t1;
            TierImpl t2;

            for (int i = 0; i < depTiers.size(); i++) {
                t1 = (TierImpl) depTiers.get(i);

                for (int j = 0; j < orderedTiers.size(); j++) {
                    t2 = (TierImpl) orderedTiers.get(j);

                    if (t1.getParentTier() == t2) {
                        depTiers.remove(i);
                        orderedTiers.add(j + 1, t1);

                        continue tierloop;
                    }
                }
            }
        }

        return orderedTiers;
    }

    /**
     * Calculates virtual times for unaligned annotations in the hierarchy
     * under the  (aligned) root annotation.<br>
     * The method of calculating the virtual times between aligned slots
     * (currently)  relies on the integrity of the indexes of the TimeSlots.
     *
     * @param orderedTiers the tiers (including the root or parent tier)
     *        involved
     * @param rootAnn the root annotation
     */
    private void calculateProposedTimes(ArrayList orderedTiers,
        AlignableAnnotation rootAnn) {
        // calculate the proposed times, skip the root
        if (orderedTiers.size() > 1) {
            ArrayList slotsDone = new ArrayList();
            TimeSlot beginSlot = rootAnn.getBegin();
            TimeSlot endSlot = rootAnn.getEnd();
            TierImpl curTier;
            Vector allAnnos;
            TreeSet betweenSlots;
            
            // for each tier find the right slots
            for (int i = 1; i < orderedTiers.size(); i++) {
                curTier = (TierImpl) orderedTiers.get(i);
                betweenSlots = new TreeSet();

                allAnnos = curTier.getAnnotations();

                Iterator it = allAnnos.iterator();
                AlignableAnnotation ann;
                AbstractAnnotation aa;

                while (it.hasNext()) {
                    aa = (AbstractAnnotation) it.next();

                    if (aa instanceof AlignableAnnotation) {
                        // should always be true on an alignable tier
                        ann = (AlignableAnnotation) aa;

                        if ((ann.getBegin().getIndex() >= beginSlot.getIndex()) &&
                                (ann.getEnd().getIndex() <= endSlot.getIndex()) &&
                                (
                            // consistency test on the annotation's slots
                            ann.getBegin().getIndex() < ann.getEnd().getIndex())) {
                            betweenSlots.add(ann.getBegin());
                            betweenSlots.add(ann.getEnd());
                        }

                        if (ann.getBegin().getIndex() > endSlot.getIndex()) {
                            break;
                        }
                    }
                }

                if (betweenSlots.size() > 0) {
                    TimeSlotImpl[] slots = (TimeSlotImpl[]) betweenSlots.toArray(new TimeSlotImpl[] {
                                
                            });
                    int beginIndex = -1;
                    int endIndex = -1;
                    boolean beginFound = false;
                    long beginTime = 0;
                    long endTime = 0;

                    for (int j = 0; j < slots.length; j++) {
                        if (!slots[j].isTimeAligned() &&
                                !slotsDone.contains(slots[j])) {
                            if (!beginFound) {
                                beginFound = true;
                                beginIndex = j - 1; // check > 0? not necessary if rootAnn really is aligned

                                if (beginIndex < 0) {
                                    //System.out.println("Can't propose time..." + rootAnn.getValue());
                                    continue;
                                }

                                if (slots[beginIndex].isTimeAligned()) {
                                    beginTime = slots[beginIndex].getTime();
                                } else {
                                    beginTime = slots[beginIndex].getProposedTime();
                                }

                                continue;
                            } else {
                                continue;
                            }
                        }

                        if (slots[j].isTimeAligned() ||
                                slotsDone.contains(slots[j])) {
                            if (beginFound) {
                                endIndex = j;

                                if (slots[j].isTimeAligned()) {
                                    endTime = slots[j].getTime();
                                } else {
                                    endTime = slots[j].getProposedTime();
                                }

                                int count = endIndex - beginIndex;
                                long segmentDiff = (endTime - beginTime) / count;

                                for (int k = 1; k < count; k++) {
                                    slots[beginIndex + k].setProposedTime(beginTime +
                                        (k * segmentDiff));
                                    slotsDone.add(slots[beginIndex + k]);
                                }

                                //reset
                                beginFound = false;
                                beginIndex = -1;
                                endIndex = -1;
                                beginTime = 0;
                                endTime = 0;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Alternative method of calculating proposed times for unaligned time slots of child annotations.
     * (Instead of depending on the index of time slots, which is not reliable).
     * Called recursively.
     * 
     * @param rootAnn the parent annotation (which has already been processed)
     */
    private void calculateProposedTimesForPL(AlignableAnnotation rootAnn) {
    	ArrayList pList = rootAnn.getParentListeners();
        if (pList.size() == 0) {
        	return;
        }
    	AbstractAnnotation ann;
    	AlignableAnnotation aa;
    	HashMap<Tier, TreeSet<TimeSlot>> map = new HashMap<Tier, TreeSet<TimeSlot>>();
    	for (int i = 0; i < pList.size(); i++) {
    		ann = (AbstractAnnotation) pList.get(i);
    		if (ann instanceof AlignableAnnotation) {
    			aa = (AlignableAnnotation) ann;
    				if (map.containsKey(aa.getTier())) {
    					map.get(aa.getTier()).add(aa.getBegin());
    					map.get(aa.getTier()).add(aa.getEnd());
    				} else {
    					TreeSet<TimeSlot> ts = new TreeSet<TimeSlot>();
    					ts.add(aa.getBegin());
    					ts.add(aa.getEnd());
    					map.put(aa.getTier(), ts);
    				}
    		}
    	}
    	
    	TreeSet<TimeSlot> betweenSlots = null;
    	ArrayList<TimeSlot> slotsDone = null;
    	Iterator<Tier> tierIt = map.keySet().iterator();
    	while (tierIt.hasNext()) {
    		betweenSlots = map.get(tierIt.next());
    		slotsDone = new ArrayList<TimeSlot>();
    		slotsDone.add(rootAnn.getBegin());
    		slotsDone.add(rootAnn.getEnd());
    		
    		if (betweenSlots.size() > 0) {
                TimeSlotImpl[] slots = (TimeSlotImpl[]) betweenSlots.toArray(new TimeSlotImpl[] {
                            
                        });
                int beginIndex = -1;
                int endIndex = -1;
                boolean beginFound = false;
                long beginTime = 0;
                long endTime = 0;

                for (int j = 0; j < slots.length; j++) {
                    if (!slots[j].isTimeAligned() &&
                            !slotsDone.contains(slots[j])) {
                        if (!beginFound) {
                            beginFound = true;
                            beginIndex = j - 1; // check > 0? not necessary if rootAnn really is aligned

                            if (beginIndex < 0) {
                                //System.out.println("Can't propose time..." + rootAnn.getValue());
                                continue;
                            }

                            if (slots[beginIndex].isTimeAligned()) {
                                beginTime = slots[beginIndex].getTime();
                            } else {
                                beginTime = slots[beginIndex].getProposedTime();
                            }

                            continue;
                        } else {
                            continue;
                        }
                    }

                    if (slots[j].isTimeAligned() ||
                            slotsDone.contains(slots[j])) {
                        if (beginFound) {
                            endIndex = j;

                            if (slots[j].isTimeAligned()) {
                                endTime = slots[j].getTime();
                            } else {
                                endTime = slots[j].getProposedTime();
                            }

                            int count = endIndex - beginIndex;
                            long segmentDiff = (endTime - beginTime) / count;

                            for (int k = 1; k < count; k++) {
                                slots[beginIndex + k].setProposedTime(beginTime +
                                    (k * segmentDiff));
                                slotsDone.add(slots[beginIndex + k]);
                            }

                            //reset
                            beginFound = false;
                            beginIndex = -1;
                            endIndex = -1;
                            beginTime = 0;
                            endTime = 0;
                        }
                    }
                }
            }
    	}
    	
    	for (int i = 0; i < pList.size(); i++) {
    		ann = (AbstractAnnotation) pList.get(i);
    		if (ann instanceof AlignableAnnotation) {
    			calculateProposedTimesForPL((AlignableAnnotation) ann);
    		}
    	}
    }
}
