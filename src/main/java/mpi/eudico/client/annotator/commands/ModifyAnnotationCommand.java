package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.MonitoringLogger;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceGroup;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.ArrayList;

/**
 * A command for modifying an annotation value.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;
    private AnnotationDataRecord annotationRecord;
    private String oldValue;
    private String newValue;
    private Object oldExtRef;
    private Object newExtRef;

    /**
     * Creates a new ModifyAnnotationCommand instance
     *
     * @param name the name of the command
     */
    public ModifyAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. We can not just use an object reference to the
     * annotation because the annotation might have been deleted and recreated
     * between the  calls to the execute and undo methods. The reference would
     * then be invalid.
     */
    public void undo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            Annotation annotation = tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
            if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                annotation.setValue(oldValue);
                if(MonitoringLogger.isInitiated()){        	
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
                }

                
                ((AbstractAnnotation) annotation).setExtRef(oldExtRef);
            }
        }
    }

    /**
     * The redo action.
     *
     * @see #undo
     */
    public void redo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            Annotation annotation = tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
            if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                annotation.setValue(newValue);
                
                if(MonitoringLogger.isInitiated()){        	
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
                }
                
                //((AbstractAnnotation) annotation).setExtRef(newExtRef);
        		ArrayList eriList = new ArrayList();
        		int listIndex = 0;
        		eriList.add(newExtRef);
        		while(listIndex < eriList.size()) {
        			if (eriList.get(listIndex) instanceof ExternalReferenceGroup) {
        				eriList.addAll(((ExternalReferenceGroup) eriList.get(listIndex)).getAllReferences());
        			}
        			listIndex++;
        		}

        		for(Object er : eriList) {
        			if(er instanceof ExternalReferenceImpl && !(er instanceof ExternalReferenceGroup)) {
        				String value = ((AbstractAnnotation) annotation).getExtRefValue(((ExternalReferenceImpl) er).getReferenceType());
        				((AbstractAnnotation) annotation).removeExtRef(new ExternalReferenceImpl(value, ((ExternalReferenceImpl) er).getReferenceType()));
        				if(((ExternalReferenceImpl) er).getValue() != null) {
        					((AbstractAnnotation) annotation).addExtRef((ExternalReferenceImpl) er);
                    	}
        			}
        		}
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation (AbstractAnnotation)
     * @param arguments the arguments:  <ul><li>arg[0] = the old value of the
     *        annotation (String)</li> <li>arg[1] = the new value of the
     *        annotation (String)</li> <li>arg[2] = the (new) external reference object 
     *        that can be set by a CV entry</li></ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        AbstractAnnotation annotation = (AbstractAnnotation) receiver;

        if (annotation != null) {
            transcription = (Transcription) annotation.getTier().getParent();
        }

        annotationRecord = new AnnotationDataRecord(annotation);

        oldValue = (String) arguments[0];
        newValue = (String) arguments[1];
        annotation.setValue(newValue);
        
        if(MonitoringLogger.isInitiated()){        	
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
        }

        
        // if an ext ref argument is passed, remove the current ext ref of the annotation 
        // even if the new ext ref is null
        if (arguments.length >= 3) {
        	ExternalReferenceImpl eri = (ExternalReferenceImpl) arguments[2];// can be null
        	try {
        		if (annotation.getExtRef() instanceof ExternalReference) {
        			oldExtRef = ((ExternalReference) annotation.getExtRef()).clone();
        		}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
        	newExtRef = eri;
        	
        	// List all Ext. Refs that are in the third argument 
        	ArrayList eriList = new ArrayList();
        	int listIndex = 0;
        	eriList.add(eri);
        	while(listIndex < eriList.size()) {
        		if (eriList.get(listIndex) instanceof ExternalReferenceGroup) {
        			eriList.addAll(((ExternalReferenceGroup) eriList.get(listIndex)).getAllReferences());
        		}
        		listIndex++;
        	}

        	// Remove all Ext. Refs of the types that are in the list
        	for(Object er : eriList) {
        		if(er instanceof ExternalReferenceImpl && !(er instanceof ExternalReferenceGroup)) {
        			String value = annotation.getExtRefValue(((ExternalReferenceImpl) er).getReferenceType());
        			annotation.removeExtRef(new ExternalReferenceImpl(value, ((ExternalReferenceImpl) er).getReferenceType()));
        			if(((ExternalReferenceImpl) er).getValue() != null) {
                		annotation.addExtRef((ExternalReferenceImpl) er);
                	}
        		}
        	}
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
}
