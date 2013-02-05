package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ProgressListener;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Converts segmentations produced by a recognizer to tiers and annotations. In
 * fact this command receives a map with tiername to annotation records, so it
 * could be used by other operations as well. As long as all annotations are
 * time-aligned and there is no assumption of tier dependencies.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SegmentsToTiersCommand implements UndoableCommand {
    private ArrayList listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private Map resolvedMap;
    private String lingTypeName = "";
    private boolean newLTCreated = false;

    /**
     * Creates a new SegmentsToTiersCommand instance
     *
     * @param commandName the name of the command
     */
    public SegmentsToTiersCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Re-creates the tiers and annotations using the same code.
     */
    public void redo() {
        createTiers();
    }

    /**
     * Delete all created tiers and if needed the created linguistic type.
     */
    public void undo() {
        Iterator nameIt = resolvedMap.keySet().iterator();
        String name = null;
        TierImpl tier = null;

        while (nameIt.hasNext()) {
            name = (String) nameIt.next();

            tier = (TierImpl) transcription.getTierWithId(name);

            if (tier != null) {
                transcription.removeTier(tier);
            }
        }

        if (newLTCreated) {
            LinguisticType lt = transcription.getLinguisticTypeByName(lingTypeName);

            if (lt != null) {
                transcription.removeLinguisticType(lt);
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = a map containing
     *        segmentation or tier name to annotation records (in a List)
     *        mappings (HashMap)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        Map segMap = (Map) arguments[0];

        // check tier names
        boolean changeNames = false;

        String name;
        TierImpl tier;
        Iterator nameIt = segMap.keySet().iterator();

        while (nameIt.hasNext()) {
            name = (String) nameIt.next();
            tier = (TierImpl) transcription.getTierWithId(name);

            if (tier != null) {
                changeNames = true;

                break;
            }
        }

        if (!changeNames) {
            resolvedMap = new HashMap(segMap);

            //resolvedMap = segMap;//??
        } else {
            resolvedMap = new HashMap(segMap.size());

            Object segments;
            nameIt = segMap.keySet().iterator();

            while (nameIt.hasNext()) {
                name = (String) nameIt.next();
                segments = segMap.get(name);
                tier = (TierImpl) transcription.getTierWithId(name);

                if (tier != null) {
                    int count = 1;

                    while (count < 30) {
                        tier = (TierImpl) transcription.getTierWithId(name +
                                "-" + count);

                        if (tier == null) {
                            resolvedMap.put(name + "-" + count, segments);

                            break;
                        }

                        count++;
                    }
                } else {
                    resolvedMap.put(name, segments);
                }
            }
        }

        LinguisticType lt = (LinguisticType) transcription.getLinguisticTypeByName(
                "default-lt");

        if ((lt != null) && (lt.getConstraints() == null)) {
            lingTypeName = "default-lt";
        } else {
            String ltName = "segmentation";
            lt = (LinguisticType) transcription.getLinguisticTypeByName(ltName);

            if ((lt != null) && (lt.getConstraints() == null)) {
                lingTypeName = ltName;
            } else if (lt == null) {
                lingTypeName = ltName;
                newLTCreated = true;
            } else {
                // create new
                int count = 1;

                while (count < 30) {
                    lt = (LinguisticType) transcription.getLinguisticTypeByName(ltName +
                            "-" + count);

                    if (lt == null) {
                        lingTypeName = ltName + "-" + count;
                        newLTCreated = true;

                        break;
                    }

                    count++;
                }
            }
        }

        ConvertThread ct = new ConvertThread();

        try {
            ct.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            transcription.setNotifying(true);
            progressInterrupt("An exception occurred: " + ex.getMessage());
        }

        //createTiers();
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
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressInterrupted(this,
                    message);
            }
        }
    }

    /**
     * Creates a new LinguisticType if needed and then creates new tiers and
     * annotations.
     */
    private void createTiers() {
        LinguisticType lt = transcription.getLinguisticTypeByName(lingTypeName);

        if (newLTCreated || (lt == null)) { // double check?
            lt = new LinguisticType(lingTypeName);
            lt.setTimeAlignable(true);
            transcription.addLinguisticType(lt);
        }

        int curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        int numTiers = resolvedMap.size();
        float progPerTier = (numTiers == 0) ? 100 : (100 / numTiers);

        Iterator nameIt = resolvedMap.keySet().iterator();
        String name = null;
        TierImpl tier = null;
        List segments = null;
        AlignableAnnotation aa = null;
        AnnotationDataRecord record = null;

        int curProg = 0;
        int curTierIndex = 0;

        while (nameIt.hasNext()) {
            name = (String) nameIt.next();
            curTierIndex++;

            if (transcription.getTierWithId(name) != null) {
                curProg = (int) (curTierIndex * progPerTier);
                progressUpdate(curProg, "");

                continue; // don't try to add to the tier
            }

            tier = new TierImpl(name, null, transcription, lt);
            transcription.addTier(tier);

            transcription.setNotifying(false);

            segments = (List) resolvedMap.get(name);

            if ((segments != null) && (segments.size() > 0)) {
                float perSeg = progPerTier / segments.size();

                for (int i = 0; i < segments.size(); i++) {
                    record = (AnnotationDataRecord) segments.get(i);

                    if (record != null) {
                        aa = (AlignableAnnotation) tier.createAnnotation(record.getBeginTime(),
                                record.getEndTime());

                        if ((aa != null) && (record.getValue() != null)) {
                            aa.setValue(record.getValue());
                        }
                    }

                    //curProg += (int) perSeg;
                    progressUpdate((int) (curProg + (i * perSeg)), "");
                }
            }

            transcription.setNotifying(true);

            curProg = (int) (curTierIndex * progPerTier);
            progressUpdate(curProg, "");
        }

        transcription.setNotifying(true);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

        progressComplete("");
    }

    /**
     * Run on separate thread to enable progress monitoring.
     *
     * @author Han Sloetjes
     */
    private class ConvertThread extends Thread {
        /**
         * The actual action of this thread.
         */
        public void run() {
            createTiers();
        }

        /**
         * Interrupts the current merging process.
         */
        public void interrupt() {
            super.interrupt();
            progressInterrupt("Operation interrupted...");
        }
    }
}
