package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.client.annotator.util.TierNameCompare;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A class to move a (group of) annotation(s) to a different tier.
 * Roughly equivalent to a copy, paste and delete original sequence.
 *  
 * @author Han Sloetjes
 *
 */
public class MoveAnnotationToTierCommand extends PasteAnnotationTreeCommand {
	private DefaultMutableTreeNode movedNode = null;
	private String origTierName = null;
	
	/**
	 * Constructor.
	 * @param name
	 */
	public MoveAnnotationToTierCommand(String name) {
		super(name);
	}

	/**
	 * Creates the new annotation again and deletes the original annotation.
	 */
	public void redo() {
        if (transcription != null) {
            setWaitCursor(true);
            transcription.setNotifying(false);
            
            newAnnotation();
            
            AbstractAnnotation aa = null;
            if (record != null && origTierName != null) {
            	// record can not be null
            	TierImpl t = (TierImpl) transcription.getTierWithId(origTierName);
            	if (t != null) {
            		aa = (AbstractAnnotation) t.getAnnotationAtTime((record.getBeginTime() + record.getEndTime()) / 2);
            		if (aa != null) {
            			t.removeAnnotation(aa);
            		}
            	}
            }
            transcription.setNotifying(true);
            setWaitCursor(false);
        }
	}

	/**
	 * Calls the super implementation to remove the moved annotation from its new tier
	 * and recreates the annotation (with depending annotations) on the original tier.
	 * 
	 */
	public void undo() {
		boolean setNotifyingOff = false;

        // copied from New AnnotationCommand
        if ((tier != null) && (newAnnotation != null)) {
            setWaitCursor(true);

            Annotation aa = tier.getAnnotationAtTime((newAnnBegin + newAnnEnd) / 2);

            if (aa != null) {
                tier.removeAnnotation(aa);
                if(MonitoringLogger.isInitiated()){
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.NEW_ANNOTATION);
                }
            }

            if (tier.isTimeAlignable()) {
                transcription.setNotifying(false);
                setNotifyingOff = true;

                restoreInUndo();
                
//                transcription.setNotifying(true);
            }
            
            if (movedNode != null) {
            	//transcription.setNotifying(false);
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            	}
            	
            	AnnotationRecreator.createAnnotationFromTree(transcription, movedNode, true);
            	
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(timePropMode);
            	}
            	//transcription.setNotifying(true);
            } else if (record != null) {
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            	}
            	TierImpl t = (TierImpl) transcription.getTierWithId(origTierName);
            	AbstractAnnotation oa = (AbstractAnnotation) t.createAnnotation(record.getBeginTime(), record.getEndTime());
            	
            	if (oa != null) {
            		oa.setValue(record.getValue());
                    if (record.getExtRef() != null) {
                    	((AbstractAnnotation) oa).setExtRef(record.getExtRef());
                    }
            	}
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(timePropMode);
            	}
            }
            
            if (setNotifyingOff) {
            	transcription.setNotifying(true);
            }
            
            setWaitCursor(false);
        }
	}

	/**
	 * @param receiver the destination tier
	 * @param arguments the arguments
	 * <ul> <li>arg[0] the annotation to move</li>
	 * 		<li>arg[1] = the begin time of the
     *        annotation (Long) (if present)</li> <li>arg[2] = the end time of the
     *        annotation (Long) (if present)</li> </ul>
	 */
	public void execute(Object receiver, Object[] arguments) {
		// some initialization that normally occurs in NewAnnotationCommand
		tier = (TierImpl) receiver;

		destTierName = tier.getName();
		transcription = (TranscriptionImpl) tier.getParent();
        changedAnnotations = new ArrayList();
        removedAnnotations = new ArrayList();
		
		AbstractAnnotation aa = (AbstractAnnotation) arguments[0];
		TierImpl srcTier = (TierImpl) aa.getTier();
		origTierName = srcTier.getName();
		// hier check tier type compatibility 
		if (tier == null || srcTier == null || (tier.getParentTier() != null)) {
			return;
		}
		
		if (aa.getParentListeners().size() == 0) {
			// create data record
			record = new AnnotationDataRecord(aa);
		} else {
			// create tree 
			node = AnnotationRecreator.createTreeForAnnotation(aa);
			record = (AnnotationDataRecord) node.getUserObject();
		}
		timePropMode = transcription.getTimeChangePropagationMode();
		// store existing annotations on dest tier
		begin = aa.getBeginTimeBoundary();
		end = aa.getEndTimeBoundary();
		
		if (arguments.length > 2) {
			begin = (Long) arguments[1];
			end = (Long) arguments[2];
		}
		newAnnBegin = begin;
		newAnnEnd = end;
		
		if (node != null) {
			adjustTierNames(node, tier.getName());
		} else if (record != null) {
			record.setTierName(tier.getName());
		}
		
		//Object[] nextArgs = new Object[]{new Long(aa.getBeginTimeBoundary()), new Long(aa.getEndTimeBoundary())};
		//super.execute(receiver, nextArgs);
		storeForUndo();
		newAnnotation();
		
		// finally delete original annotation
		if (newAnnotation != null) {
			movedNode = AnnotationRecreator.createTreeForAnnotation(aa);
			((TierImpl) aa.getTier()).removeAnnotation(aa);
		}
		
        // HS July 2012: after the actions above the focus is completely lost, 
        // none of the keyboard shortcuts seem to function
        // try to correct this by giving the frame the focus
		// see also DuplicateAnnotation, PasteAnnotation
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                ELANCommandFactory.getRootFrame(transcription).requestFocus();
            }
        });
	}

	
	/**
	 * Do not adjust the begin and end times
	 */
	protected void adjustTimes() {

	}
	
	/**
	 * Updates names of tiers in the tree, based on shared suffixes.
	 * @param inNode the root node
	 * @param destTierName the new root tier name
	 */
	protected void adjustTierNames(DefaultMutableTreeNode inNode, String destTierName) {
		if (inNode != null && destTierName != null) {
			TierNameCompare tnc = new TierNameCompare();
			AnnotationDataRecord aRecord = (AnnotationDataRecord) inNode.getUserObject();
			
        	int[] indices = tnc.findCorrespondingAffix(aRecord.getTierName(), destTierName);
        	if (indices != null && indices[0] > -1) {
        		char del = aRecord.getTierName().charAt(indices[0]);
        		String affix = "";
        		if (indices[1] <= TierNameCompare.PREFIX_MODE) {
        			int di = destTierName.lastIndexOf(del);
        			if (di > -1) {
        				affix = destTierName.substring(di);
        			}
        		} else {
        			int di = destTierName.indexOf(del);
        			if (di > -1) {
        				affix = destTierName.substring(0, di);
        			}
        		}
        		tnc.adjustTierNames(inNode, affix, del, indices[1]);
        	}
		}
		
	}

	/**
	 * Returns the name.
	 */
	public String getName() {
		return super.getName();
	}

}
