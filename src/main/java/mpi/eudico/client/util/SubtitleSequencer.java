package mpi.eudico.client.util;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import mpi.eudico.util.TimeRelation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


/**
 * Class that creates a sequence of data units for subtitles. The units consist
 * of a text value and a begin and end time, where the  end time can be either
 * the real end time or a calculated end time based on a minimum duration.<br>
 * Overlaps are handled by splitting into multiple units.
 *
 * @author Han Sloetjes
 */
public class SubtitleSequencer implements ClientLogger {
    /**
     * Creates a new SubtitleSequencer instance
     */
    public SubtitleSequencer() {
        super();
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     * @param offset the number of ms. to add to all time values
     * @param resolveOverlaps detects overlapping units and creates new,
     *        merging units for the overlaps
     *
     * @return a list of subtitle objects
     *
     * @throws NullPointerException if the {@link Transcription} or the list of
     *         tiernames is null
     * @throws IllegalArgumentException if the size of the list of tier names
     *         is 0
     */
    public List createSequence(Transcription transcription, List tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration, long offset,
        boolean resolveOverlaps) {
        if (transcription == null) {
            throw new NullPointerException("The transcription is null");
        }

        if (tierNames == null) {
            throw new NullPointerException("The list of tier names is null");
        }

        if (tierNames.size() == 0) {
            throw new IllegalArgumentException("No tiers have been specified");
        }

        Stack units = new Stack();
        String name;
        TierImpl tier;
        AbstractAnnotation ann;
        SubtitleUnit sub1, sub2;
        List annotations;

        for (int i = 0; i < tierNames.size(); i++) {
            name = (String) tierNames.get(i);
            tier = (TierImpl) transcription.getTierWithId(name);

            if (tier == null) {
                LOG.warning("The tier does not exist: " + name);
                continue;
            }

            annotations = tier.getAnnotations();
            sub1 = null;
            sub2 = null;

            for (int j = 0; j < annotations.size(); j++) {
                ann = (AbstractAnnotation) annotations.get(j);

                if ((ann != null) &&
                        TimeRelation.overlaps(ann, intervalBegin, intervalEnd)) {
                    sub1 = new SubtitleUnit(ann.getBeginTimeBoundary() +
                            offset, ann.getEndTimeBoundary() + offset, i, 
                            ann.getValue());

                    // If the preceding unit is below minimum length:
                    if ((sub2 != null) && ((sub2.getCalcEnd() - 
                         sub2.getBegin()) < minimalDuration)) {

                        if (resolveOverlaps && sub2.getBegin() 
                        		+ minimalDuration > sub1.getBegin()) {
                        	// HS: check if there is overlap
                            // Merge the preceding annotation and this
                            // annotation into a single unit, extend the
                            // end time, then move on.
                            sub2.setValue(sub2.getValue() + " " + 
                                sub1.getValue());
                            sub2.setCalcEnd(sub1.getCalcEnd());
                            
                            continue;
                        } else {
                            // Increase the end time of the preceding annota-
                            // tion to the minimum duration (or as close as
                            //  we can get without merging with the next
                            // annotation on this tier).
                            sub2.setCalcEnd(Math.min(sub1.getBegin(), 
                                sub2.getBegin() + minimalDuration));
                        }
                    }

                    units.add(sub1);
                    sub2 = sub1;
                }
            }
        }

        // all units have been added, sort first
        Collections.sort(units, Collections.reverseOrder());

        if (!resolveOverlaps) {
            return units;
        }

        ArrayList output = new ArrayList();
        Stack agroup = new Stack();

        do {
            // Read in a group of annotations that have the same start time.
            SubtitleUnit sub = null;
            long tmpStart = -1L;
            while (units.size() > 0) {
                sub = (SubtitleUnit) units.peek();
                if (tmpStart < 0) {
                    tmpStart = sub.getBegin();
                }

                if ((sub.getBegin() != tmpStart) && (agroup.size() != 1)) {
                    break;
                }

                agroup.add(units.pop());
            }
//            for (int i = 0; i < agroup.size(); i++) {
//            }

            // Calculate the starting and ending time of the earliest units
            // in the current group, as well as the starting time of the
            // next earliest unit, and group all of the units with the
            // earliest starting times together.
            ArrayList<SubtitleUnit> firsts = new ArrayList<SubtitleUnit>();
            long first_start = -1L;
            long second_start = -1L;
            long first_end = -1L;
            for (int i = 0; i < agroup.size(); i++) {
                sub = (SubtitleUnit) agroup.get(i);
                if ((first_start < 0) || (first_start > sub.getBegin())) {
                    if ((second_start < 0) || (second_start > first_start)) {
                        second_start = first_start;
                    }

                    first_start = sub.getBegin();
                    firsts.clear();
                }

                if ((sub.getBegin() != first_start) && 
                    ((second_start < 0) || (second_start > sub.getBegin()))) {
                    second_start = sub.getBegin();
                }

                if (first_start == sub.getBegin()) {
                    firsts.add(sub);
                }

                if ((first_end < 0) ||
                    ((first_start == sub.getBegin()) &&
                     (first_end > sub.getCalcEnd()))) {
                    first_end = sub.getCalcEnd();
                }
            }

            // Add a new subtitle unit.
            ArrayList<String> values = new ArrayList<String>();
            // sort the units according to the tier order
            if (firsts.size() > 1) {
            	for (int i = 1; i < firsts.size(); i++) {
            		for (int j = 0; j < i; j++) {
            			if (firsts.get(i).getLineIndex() < firsts.get(j).getLineIndex()) {
            				firsts.add(j, firsts.remove(i));
            			}
            		}
            	}
            }
            
            for (int i = 0; i < firsts.size(); i++) {
                sub = (SubtitleUnit) firsts.get(i);
                String[] vals = sub.getValues();
                if (vals == null) {
                    String val = sub.getValue();
                    if (val != null) {
                        values.add(val);
                    }
                } else {
                    for (int j = 0; j < vals.length; j++) {
                        values.add(vals[j]);
                    }
                }
            }

            long min_end = Math.min(first_end,
                (second_start < 0) ? first_end : second_start);
            SubtitleUnit out = new SubtitleUnit(first_start, min_end, null);
            out.setValues((String[]) values.toArray(new String[0]));
            output.add(out);

            // Now adjust the start times for each of the items just output,
            // removing any annotations that no longer have any run time.
            for (int i = 0; i < firsts.size(); i++) {
                sub = (SubtitleUnit) firsts.get(i);
                sub.setBegin(min_end);

                if (min_end >= sub.getCalcEnd()) {
                    agroup.remove(sub);
                }
            }
        } while (! (agroup.isEmpty() && units.isEmpty()));

        return output;
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, List, long, long, int, boolean)
     */
    public List createSequence(Transcription transcription, List tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration) {
        return createSequence(transcription, tierNames, intervalBegin,
            intervalEnd, minimalDuration, 0L, false);
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     * @param offset the number of ms. to add to all time values
     * @param resolveOverlaps detects overlapping units and creates new,
     *        merging units for the overlaps
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, List, long, long, int, boolean)
     */
    public List createSequence(Transcription transcription, String[] tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration, long offset,
        boolean resolveOverlaps) {
        ArrayList names = null;

        if (tierNames != null) {
            names = new ArrayList(tierNames.length);

            for (int i = 0; i < tierNames.length; i++) {
                names.add(tierNames[i]);
            }
        }

        return createSequence(transcription, names, intervalBegin, intervalEnd,
            minimalDuration, offset, resolveOverlaps);
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, String[], long, long, int, boolean)
     */
    public List createSequence(Transcription transcription, String[] tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration) {
        return createSequence(transcription, tierNames, intervalBegin,
            intervalEnd, minimalDuration, 0L, false);
    }
}
