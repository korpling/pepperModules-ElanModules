package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.util.TierTree;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A Command to merge two transcriptions and write the result to file.
 * Merging here means that a selected set of tiers with their annotations from one 
 * transcription are added to another transcription. If one or more of the tiers already exist
 * in the destination transcription annotations will either be overwritten or preserved, 
 * whichever the user choses.
 * It is assumed that the destination transcription may be altered, that the destination 
 * file path has been checked and that, if the file already exists, it may be overwritten.
 */
public class MergeTranscriptionsCommand implements Command, ClientLogger {
    private String commandName;
    private ArrayList listeners;
    
    private TranscriptionImpl destTrans;
    private TranscriptionImpl srcTrans;
    private String fileName;
    private ArrayList selTiers;
    private boolean overwrite;
    private boolean addLinkedFiles;
    
    /**
     * A command to merge two transcriptions.
     * 
     * @param theName the name of the command
     */
    public MergeTranscriptionsCommand(String theName) {
        commandName = theName;
    }

    /**
     * Merges two transscriptions.
     * <b>Note: </b>it is assumed the types and order of the arguments
     * are correct.
     * 
     * @param receiver the receiving transcription
     * @param arguments the arguments: <ul><li>arg[0] = the second transcription
     *        (TranscriptionImpl)</li> <li>arg[1] = the path to the destination file (String)</li>
     *        <li>arg[2] = the names of the tiers to add to the destination (ArrayList)</li>
     *        <li>arg[3] = a flag to indicate whether existing annotations</li></ul>
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    public void execute(Object receiver, Object[] arguments) {
        destTrans = (TranscriptionImpl) receiver;
        srcTrans = (TranscriptionImpl) arguments[0];
        fileName = (String) arguments[1];
        selTiers = (ArrayList) arguments[2];
        overwrite = ((Boolean) arguments[3]).booleanValue();
        addLinkedFiles = ((Boolean) arguments[4]).booleanValue();
        
        if (destTrans != null) {
            destTrans.setNotifying(false);
        } else {
            progressInterrupt("No first transcription (destination) specified.");
            return;
        }
        
        if (srcTrans == null) {
            progressInterrupt("No second transcription (source) specified");
            return;
        }
        
        if (fileName == null) {
            progressInterrupt("No filename specified");
            return;
        }
        
        if (selTiers == null) {
            progressInterrupt("No tiers specifed");
            return;
        }
        // do the work in a separate thread
        new MergeThread().start();
    }

    /**
     * Returns the name of the command
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
     * A thread that performs the actual merging.
     */
    class MergeThread extends Thread {
        
        MergeThread() {
            super();
        }
        
        MergeThread(String name) {
            super(name);
        }
        
        public void run() {
            try {
	            progressUpdate(5, "Checking tiers to add...");
	            MergeUtil mergeUtil = new MergeUtil();
	            // list of tiers ( and/or annotations) that can be added
	            ArrayList tiersToAdd = mergeUtil.getAddableTiers(srcTrans, destTrans, selTiers);
	            progressUpdate(10, "Sorting the tiers to add...");
	            // order tiers hierarchically and add the new tiers
	            tiersToAdd = mergeUtil.sortTiers(tiersToAdd);
	            progressUpdate(20, "Creating the tiers. linguistic types, cv's...");
	            mergeUtil.addTiersTypesAndCVs(srcTrans, destTrans, tiersToAdd);
	            //addTiersTypesAndCVs(tiersToAdd);
	            progressUpdate(30, "Adding annotations...");
	            int numIndivTiers = 0;
	            TierImpl t;
	            for (int i =  0; i < tiersToAdd.size(); i++) {
	                t = (TierImpl) tiersToAdd.get(i);
	                if (!t.hasParentTier() || !tiersToAdd.contains(t.getParentTier())) {
	                    numIndivTiers++;
	                }
	            }
	            if (numIndivTiers > 0) {	            
		            int progPerTier = 60 / numIndivTiers;
		            int count = 1;
		            for (int i =  0; i < tiersToAdd.size(); i++) {
		                t = (TierImpl) tiersToAdd.get(i);
		                if (!t.hasParentTier() || !tiersToAdd.contains(t.getParentTier())) {
		                    addAnnotations(t);
		                    progressUpdate(30 + count * progPerTier, 
		                            "Merging of tier " + t.getName() + " done.");
		                }
		            }
	            }
	            
	            if(addLinkedFiles){
	            	Vector descriptors = srcTrans.getMediaDescriptors();
	            	Vector destDescriptors = destTrans.getMediaDescriptors();
	            	
	            	for(int i=0; i<descriptors.size(); i++){
	            		if(!destDescriptors.contains(descriptors.get(i))){
	            			destDescriptors.add(descriptors.get(i));	            			
	            		}
	            	}
	            	
	            	descriptors = srcTrans.getLinkedFileDescriptors();
	            	destDescriptors = destTrans.getLinkedFileDescriptors();
	            	
	            	for(int i=0; i<descriptors.size(); i++){
	            		if(!destDescriptors.contains(descriptors.get(i))){
	            			destDescriptors.add(descriptors.get(i));	            			
	            		}
	            	}
	            }
	            
	            progressUpdate(92, "Saving transcription...");
	            // save the transcription
	            TranscriptionStore transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
	            transcriptionStore.storeTranscription(destTrans, null,
	                new Vector(), fileName,
	                TranscriptionStore.EAF);
	            LOG.info("Transcription saved to " + fileName);
	            progressComplete("Merging complete");
            } catch (Exception ex) {
                LOG.severe("Error while merging: " + ex.getMessage());
                ex.printStackTrace();
                progressInterrupt("Error while merging: " + ex.getMessage());
            }
        }
                
        private void addAnnotations(TierImpl tier) {
            TierImpl parent = (TierImpl) tier.getParentTier();
            TierImpl destTier = (TierImpl) destTrans.getTierWithId(tier.getName());
            
            if (destTier == null) {
                LOG.warning("Destination tier " + tier.getName() + " not found in destination description");
                return;
            }
            AbstractAnnotation ann = null;
            DefaultMutableTreeNode recordNode = null;
            
            if (parent != null) {
                LinguisticType lt = tier.getLinguisticType();
                if (lt.getConstraints() == null) {
                    LOG.warning("Error: illegal type for tier: " + tier.getName());
                    return; 
                }
                if (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                    Vector annotations = tier.getAnnotations();
                    if (annotations.size() > 0) {
                        for (int i = 0; i < annotations.size(); i++) {
                            ann = (AbstractAnnotation) annotations.get(i);
                            if (overwrite || destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(), 
                                    ann.getEndTimeBoundary()).size() == 0) {
                                recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                                AnnotationRecreator.createAnnotationFromTree(destTrans,
                                        recordNode);
                            }
                        }
                    }                    
                } else {
                    // subdivision, copy groupwise
                    ArrayList group = new ArrayList();
                    TierImpl rootTier = tier.getRootTier();
                    Annotation curParent = null;
                    Vector annotations = tier.getAnnotations();
                    Iterator anIter = annotations.iterator();
                    
                    while (anIter.hasNext()) {
                        ann = (AbstractAnnotation) anIter.next();
                        int numOverlap = destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(),
                                ann.getEndTimeBoundary()).size();
                        if (curParent == null) {
                            // add to first group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }

                            curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());
                        } else if (rootTier.getAnnotationAtTime(
                                    ann.getBeginTimeBoundary()) == curParent) {
                            // add to current group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }
                        } else {
                            // finish group
                            if (group.size() > 0) {
                                AnnotationRecreator.createAnnotationsSequentially(destTrans, group);
                            }
                            group = new ArrayList();
                            curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());

                            // add to new group
                            if (overwrite || numOverlap == 0) {
                                group.add(AnnotationRecreator.createTreeForAnnotation(
                                        ann));
                            }
                        }
                    }
                    // add the last group...
                    if (group.size() > 0) {
                        AnnotationRecreator.createAnnotationsSequentially(destTrans, group);
                    }
                }
                
            } else {
                // no parent tier
                Vector annotations = tier.getAnnotations();
                if (annotations.size() > 0) {
                    for (int i = 0; i < annotations.size(); i++) {
                        ann = (AbstractAnnotation) annotations.get(i);
                        if (overwrite || destTier.getOverlappingAnnotations(ann.getBeginTimeBoundary(), 
                                ann.getEndTimeBoundary()).size() == 0) {
                            recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                            AnnotationRecreator.createAnnotationFromTree(destTrans,
                                    recordNode);
                        }
                    }
                }
            }
            LOG.info("Merging of tier " + tier.getName() + " done.");
        }
        
    } // end of MergeThread
    
}
