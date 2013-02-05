package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Deletes multiple annotations selected by the user.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteSelectedAnnotationsCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;

    private List<DefaultMutableTreeNode> delAnnRecords;

    /**
     * Creates a new DeleteSelectedAnnotationsCommand instance.
     *
     * @param name the name of the command
     */
    public DeleteSelectedAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Deletes the annotations again
     */
    public void redo() {
        deleteAnnotations();
    }

    /**
     * Restores the annotations that have been deleted
     */
    public void undo() {
        if ((transcription != null) && (delAnnRecords != null)) {
	        	
	            int curPropMode = 0;
	
	            curPropMode = transcription.getTimeChangePropagationMode();
	
	            if (curPropMode != Transcription.NORMAL) {
	                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
	            }
	            ((TranscriptionImpl) transcription).setNotifying(false);
	            setWaitCursor(true);
	            
                for (DefaultMutableTreeNode n : delAnnRecords) {
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        n, true);
                }
                
	            setWaitCursor(false);
	        	
	            // restore the time propagation mode
	            transcription.setTimeChangePropagationMode(curPropMode);
	            ((TranscriptionImpl) transcription).setNotifying(true);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the selected annotations
     *        (List (AbstractAnnotation))</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if (arguments != null && arguments[0] instanceof List) {
        	List selectedAnnos = (List) arguments[0];

            delAnnRecords = new ArrayList<DefaultMutableTreeNode>(selectedAnnos.size());

            setWaitCursor(true);
            storeAnnotationTrees(selectedAnnos);

            deleteAnnotations();
            setWaitCursor(false);
        }
    }

    private void storeAnnotationTrees(List selectedAnnos) {
    	Object next, next2;
        AbstractAnnotation annotation, annotation2;
        DefaultMutableTreeNode node;
        if (selectedAnnos != null) {
        	// first check if there are no parent-child (ancestor-descendant) combinations in the list
        	outerloop:
        	for (int i = 0; i < selectedAnnos.size(); i++) {
        		next = selectedAnnos.get(i);
        		if (! (next instanceof AbstractAnnotation)) {
        			continue;
        		}
        		annotation = (AbstractAnnotation) next;
        		
        		for (int j = 0; j < selectedAnnos.size(); j++) {
        			if (j == i) {
        				continue;
        			}
        			next2 = selectedAnnos.get(j);
        			
        			if ((next2 instanceof AbstractAnnotation)) {
        				annotation2 = (AbstractAnnotation) next2;
        				if (((TierImpl) annotation.getTier()).hasAncestor(annotation2.getTier())) {
        					if (annotation.getBeginTimeBoundary() >= annotation2.getBeginTimeBoundary() && 
        							annotation.getEndTimeBoundary() <= annotation2.getEndTimeBoundary()) {
        						continue outerloop;
        					}       					
        				}
        			}
        		}
    			// if we get here there is no ancestor annotation of this annotation
    			// in the list, so add the annotation
                node = AnnotationRecreator.createTreeForAnnotation(annotation);
            	
                if (node != null) {
                    delAnnRecords.add(node);
                }
        	}
        }
    }

    /**
     * Deletes the annotations based on the stored records.
     */
    private void deleteAnnotations() {
        if ((delAnnRecords != null) && (delAnnRecords.size() > 0)) {
        		List<Annotation> toDelete = new ArrayList<Annotation>(delAnnRecords.size());
        		TierImpl tier = null;
        		AbstractAnnotation annotation;
   	            AnnotationDataRecord record;
   	            
	            for (DefaultMutableTreeNode n : delAnnRecords) {
	                record = (AnnotationDataRecord) n.getUserObject();
	                tier = ((TierImpl) transcription.getTierWithId(record.getTierName()));
	                
	                if (tier == null) {
	                	 ClientLogger.LOG.warning("The tier could not be found: " +
	                			 record.getTierName());
	                	 continue;
	                }
	                
	                annotation = (AbstractAnnotation) tier.getAnnotationAtTime((record.getBeginTime() +
	                        record.getEndTime()) / 2);
	
	                if (annotation != null) {
	                	toDelete.add(annotation);
	                }
	            }
	            
	            ((TranscriptionImpl) transcription).setNotifying(false);
	            for (Annotation ann : toDelete) {
	            	((TierImpl) ann.getTier()).removeAnnotation(ann);
	            }
	            ((TranscriptionImpl) transcription).setNotifying(true);
        }
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
}
