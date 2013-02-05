package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.util.TimeInterval;

import java.awt.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * A Command that adds a group of segments/annotations to a tier in one,
 * undoable, action.
 *
 * @author Han Sloetjes
 */
public class AddSegmentationCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;
    private String tierName;
    private TierImpl tier;
    private ArrayList segments;

    // undo/redo

    /** Holds value of property DOCUMENT ME! */
    ArrayList changedAnnotations;

    /** Holds value of property DOCUMENT ME! */
    ArrayList removedAnnotations;

    /**
     * Creates a new AddSegmentationCommand instance
     *
     * @param name the name of the command
     */
    public AddSegmentationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added annotations from the Tier.
     */
    public void undo() {
        if (transcription != null) {
            restoreAnnotations();
        }
    }

    /**
     * The redo action. Adds the segments/annotations to the Tier.
     */
    public void redo() {
        if (transcription != null) {
            createNewAnnotations();
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the name of the Tier,
     *        which should be a root tier  (TierImpl)</li> <li>arg[1] = a list
     *        of time intervals (ArrayList containing  TimeInterval
     *        objects)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        tierName = (String) arguments[0];
        segments = (ArrayList) arguments[1];

        if (transcription != null) {
            tier = (TierImpl) transcription.getTierWithId(tierName);
        }

        if (tier != null) {
            setWaitCursor(true);

            preProcessIntervals();
            changedAnnotations = new ArrayList();
            removedAnnotations = new ArrayList();

            storeAnnotations();

            createNewAnnotations();

            setWaitCursor(false);
        }
    }

    /**
     * Sort the intervals and correct overlapping intervals.<br>
     * This is done by changing the end time of one annotation to  the begin
     * time of the next annotation, in case the old end time  is greater than
     * the next begin time.
     */
    private void preProcessIntervals() {
        if (segments != null) {
            Collections.sort(segments, new IntervalComparator());

            TimeInterval t1 = null;
            TimeInterval t2 = null;

            for (int i = segments.size() - 1; i > 0; i--) {
                t1 = (TimeInterval) segments.get(i - 1);
                t2 = (TimeInterval) segments.get(i);

                if (t2.getBeginTime() < t1.getEndTime()) {
                	if (t1 instanceof AnnotationDataRecord) {
                		((AnnotationDataRecord) t1).setEndTime(t2.getBeginTime());
                		
                		if (((AnnotationDataRecord) t1).getDuration() <= 0) {
                			segments.remove(t1);
                		}
                	} else {
	                    TimeInterval ti = new TimeInterval(t1.getBeginTime(),
	                            t2.getBeginTime());
	                    segments.remove(t1);
	
	                    if (ti.getDuration() > 0) {
	                        segments.add(i - 1, ti);
	                    }
                	}
                }
            }
        }
    }

    /**
     * Makes a backup of the effected annotations of this tier, including child
     * annotations (for undo).
     */
    private void storeAnnotations() {
        if ((segments != null) && (segments.size() > 0)) {
            ArrayList changedTemp = new ArrayList();
            TimeInterval curInterval = null;
            AbstractAnnotation aa = null;

            for (int i = 0; i < segments.size(); i++) {
                curInterval = (TimeInterval) segments.get(i);

                Vector effectedAnn = tier.getOverlappingAnnotations(curInterval.getBeginTime(),
                        curInterval.getEndTime());

                for (int j = 0; j < effectedAnn.size(); j++) {
                    aa = (AbstractAnnotation) effectedAnn.get(j);

                    if (aa.getBeginTimeBoundary() < curInterval.getBeginTime()) {
                        if (!changedTemp.contains(aa)) {
                            changedTemp.add(aa);
                            changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    aa));
                        }
                    } else if (aa.getEndTimeBoundary() > curInterval.getEndTime()) {
                        if (!changedTemp.contains(aa)) {
                            changedTemp.add(aa);
                            changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    aa));
                        }
                    } else {
                        removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                aa));
                    }
                }
            }
        }
    }

    /**
     * Creates annotations of the provided time interval objects.
     */
    private void createNewAnnotations() {
        if ((segments == null) || (tier == null)) {
            return;
        }

        if (!tier.isTimeAlignable()) {
            return;
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);
        setWaitCursor(true);

        TimeInterval ti = null;
        AnnotationDataRecord adr = null;
        AbstractAnnotation aa;
        Object segment;

        for (int i = 0; i < segments.size(); i++) {
        	segment = segments.get(i);
        	if (segment instanceof AnnotationDataRecord) {
        		adr = (AnnotationDataRecord) segment;
        		
        		aa = (AbstractAnnotation) tier.createAnnotation(adr.getBeginTime(), adr.getEndTime());
        		if (aa != null) {
        			aa.setValue(adr.getValue());
        			if (adr.getExtRef() != null) {
        				aa.setExtRef(adr.getExtRef());
        			}
        		}
        	} else if (segment instanceof TimeInterval) {
        		ti = (TimeInterval) segment;

        		tier.createAnnotation(ti.getBeginTime(), ti.getEndTime());
        	}
        }

        transcription.setNotifying(true);
        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    private void restoreAnnotations() {
        if ((tier == null) || (segments == null) || (segments.size() == 0)) {
            return;
        }

        setWaitCursor(true);

        transcription.setNotifying(false);

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        TimeInterval ti = null;
        AnnotationDataRecord adr = null;
        Object segment;
        AbstractAnnotation aa = null;
        DefaultMutableTreeNode node = null;
        AnnotationDataRecord annRecord = null;

        // first remove the created annotations
        for (int i = 0; i < segments.size(); i++) {
            ti = (TimeInterval) segments.get(i);
            aa = (AbstractAnnotation) tier.getAnnotationAtTime(ti.getBeginTime());

            if (aa != null) {
                tier.removeAnnotation(aa);
            } else {
                // log..
                System.out.println(
                    "Could not delete a previously created annotation");
            }
        }

        // then remove the changed annotations
        for (int i = 0; i < changedAnnotations.size(); i++) {
            node = (DefaultMutableTreeNode) changedAnnotations.get(i);
            annRecord = (AnnotationDataRecord) node.getUserObject();

            // because we don't know whether the annotation's begin time, 
            // end time or both have been changed, just find any annotation 
            // within the time interval
            // the annotation might have been removed by two successive new annotations
            Vector v = tier.getOverlappingAnnotations(annRecord.getBeginTime(),
                    annRecord.getEndTime());

            if (v.size() > 1) {
                // log...
                System.out.println("Found more than one annotation in interval");
            }

            for (int j = 0; j < v.size(); j++) {
                aa = (AbstractAnnotation) v.get(j);
                tier.removeAnnotation(aa);
            }
        }

        // the tier should be cleared from all new and all changed annotations
        // recreate the changed and the removed annotations
        if (changedAnnotations.size() > 0) {
            for (int i = 0; i < changedAnnotations.size(); i++) {
                node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
            }
        }

        if (removedAnnotations.size() > 0) {
            for (int i = 0; i < removedAnnotations.size(); i++) {
                node = (DefaultMutableTreeNode) removedAnnotations.get(i);
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
            }
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

        transcription.setNotifying(true);
        setWaitCursor(false);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }

    private void printSegments() {
        if (segments == null) {
            return;
        }

        if (segments.size() > 0) {
            for (int i = 0; i < segments.size(); i++) {
                TimeInterval ti = (TimeInterval) segments.get(i);
                System.out.println("Segment: " + ti.getBeginTime() + " - " +
                    ti.getEndTime());
            }
        }
    }

    /**
     * Compares two TimeInterval objects.<br>
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @author Han Sloetjes
     */
    class IntervalComparator implements Comparator {
        /**
         * Compares two TimeInterval objects. First the begin times are
         * compared. If they are the same the end times  are compared.  Note:
         * this comparator imposes orderings that are inconsistent with
         * equals.
         *
         * @param o1 the first interval
         * @param o2 the second interval
         *
         * @return DOCUMENT ME!
         *
         * @throws ClassCastException when either object is not a TimeInterval
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        public int compare(Object o1, Object o2) throws ClassCastException {
            if (!(o1 instanceof TimeInterval) || !(o2 instanceof TimeInterval)) {
                throw new ClassCastException(
                    "Objects should be of type TimeInterval");
            }

            if (((TimeInterval) o1).getBeginTime() < ((TimeInterval) o2).getBeginTime()) {
                return -1;
            }

            if ((((TimeInterval) o1).getBeginTime() == ((TimeInterval) o2).getBeginTime()) &&
                    (((TimeInterval) o1).getEndTime() < ((TimeInterval) o2).getEndTime())) {
                return -1;
            }

            if ((((TimeInterval) o1).getBeginTime() == ((TimeInterval) o2).getBeginTime()) &&
                    (((TimeInterval) o1).getEndTime() == ((TimeInterval) o2).getEndTime())) {
                return 0;
            }

            return 1;
        }
    }
}
