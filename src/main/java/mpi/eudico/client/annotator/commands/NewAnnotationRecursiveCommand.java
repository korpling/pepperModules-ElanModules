package mpi.eudico.client.annotator.commands;


import java.util.Vector;

import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;

/**
 * An undoable command that builds on the new annotation command.
 * It takes the new annotation as input, and creates new annotation recursively
 * on all the dependent tiers
 * This is then one undoable command in the undo/redo list.
 */
public class NewAnnotationRecursiveCommand extends NewAnnotationCommand {    
    private TierImpl tier;
    private int numberofRecAnnotations;
    
    
    /**
     * Creates a new NewDependentAnnotationCommand instance
     * 
     * @param name the name of the command
     */
    public NewAnnotationRecursiveCommand(String name) {
    	super(name);    	
    }

    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br>
     * July 2006: removed the ViewerManager as one of the objects in the arguments array
     *
     * @param receiver the TierImpl
     * @param arguments the arguments:  <ul> <li>arg[0] = the begin time of the
     *        annotation (Long)</li> <li>arg[1] = the end time of the
     *        annotation (Long)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) { 
    	super.execute(receiver, arguments);
    	tier = (TierImpl) receiver;         
        
        createNewAnnotaionsRecursively();
        
        if(numberofRecAnnotations > 0 && MonitoringLogger.isInitiated()){
         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.RECURSIVE_ANNOTATIONS, "number of annotations created recursively : " + numberofRecAnnotations);
        }
    }
    
    /**
     * The creation of the new annotation recursively
     * on all dependent tiers
     */
    
    private void createNewAnnotaionsRecursively(){
    	    	
    	Vector dependentTiers = tier.getDependentTiers();
	    if(dependentTiers != null && dependentTiers.size() > 0){
	    	for(int i=0; i< dependentTiers.size(); i++){	    		
	    		TierImpl currentChildTier = (TierImpl) dependentTiers.elementAt(i);
	        		
	        	if(currentChildTier.isTimeAlignable()){
	            	currentChildTier.createAnnotation(newAnnBegin, newAnnEnd);
	            }else{
	            	long time = (newAnnBegin +  newAnnEnd) / 2;
	            	currentChildTier.createAnnotation(time, time);	            		
	            } 
	        	
	        	numberofRecAnnotations++;
	        }	    	
	    	((TranscriptionImpl) tier.getParent()).notifyListeners(tier, ACMEditEvent.ADD_ANNOTATION_HERE , tier.getAnnotationAtTime(newAnnBegin));	    	
	    }
    }
    

    /**
     * The redo action.
     */
    public void redo() {  
    	super.redo();   
    	createNewAnnotaionsRecursively();    	
    	if(numberofRecAnnotations > 0 && MonitoringLogger.isInitiated()){
         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.RECURSIVE_ANNOTATIONS);
        }
    }
    
    /**
     * The undo action.
     */
    public void undo() {
        super.undo();  
        numberofRecAnnotations = 0;
        if(numberofRecAnnotations > 0 && MonitoringLogger.isInitiated()){
         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.RECURSIVE_ANNOTATIONS);
        }
    }
    
    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return super.getName();
    }
}


