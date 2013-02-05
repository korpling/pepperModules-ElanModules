package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * An undoable command that builds on the create dependent annotation command.
 * It takes the active annotation as input, and creates new annotation recursively
 * on all the dependent tiers
 * This is then one undoable command in the undo/redo list.
 */
public class CreateDependentAnnotationsCommand	implements UndoableCommand{  
	
    private String commandName;
    private TierImpl tier;    
    TranscriptionImpl transcription;   
    private long begin;
    private long end;
    List<String> annotationTiers;    
    int numberOfannotaionsCreated = 0;
    
    /**
     * Creates a new CreateDependentAnnotationsCommand instance
     * 
     * @param name the name of the command
     */
    public CreateDependentAnnotationsCommand(String name) {
    	commandName = name;
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br>     
     *
     * @param receiver the TierImpl
     * @param arguments the arguments: 
     * 		 <ul> 
     * 		<li>arg[0] = the begin time of the annotation (Long)</li> 
     * 		<li>arg[1] = the end time of the annotation (Long)</li> 
     * 		</ul>
     */
    public void execute(Object receiver, Object[] arguments) {     	
    	tier = (TierImpl) receiver; 
    	begin = ((Long) arguments[0]).longValue();
        end = ((Long) arguments[1]).longValue();
        transcription = ((TranscriptionImpl) tier.getParent());
        
        annotationTiers = new ArrayList<String>();        
        createDependingAnnotations();
        
        if(numberOfannotaionsCreated > 0){
        	if(MonitoringLogger.isInitiated()){
	         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS, tier.getName(), tier.getAnnotationAtTime(begin).getValue(), Long.toString(begin), Long.toString(end), 
	         			"number of annotations created : " + numberOfannotaionsCreated);
	        }
        }
    }
    
    public void createDependingAnnotations(){
    	annotationTiers.clear();
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        // setWaitCursor(true);
        ((TranscriptionImpl) transcription).setNotifying(false);
                
        Vector dependentTiers = tier.getDependentTiers();
        int firstAnnotation = -1;
        
 	    if(dependentTiers != null){
 	    	Annotation ann = null;
 	    	for(int i=0; i< dependentTiers.size(); i++){	    		
 	        	TierImpl currentChildTier = (TierImpl) dependentTiers.elementAt(i);
 	        	if(currentChildTier.getAnnotationAtTime(begin) == null ){
 	        		if(firstAnnotation < 0){
 	        			firstAnnotation = i;
 	        		}
 	        		annotationTiers.add(currentChildTier.getName());
 	        		if(currentChildTier.isTimeAlignable()){
 	        			currentChildTier.createAnnotation(begin, end);
 	        		}else{
 	        			long time = (begin + end) / 2;
 	        			currentChildTier.createAnnotation(time, time);	            		
 	        		} 
 	        		numberOfannotaionsCreated++;
 	        	}
 	        }
 	    	
 	        ((TranscriptionImpl) transcription).setNotifying(true);
 	        if(firstAnnotation >=0){
 	        	((TranscriptionImpl) tier.getParent()).notifyListeners(tier, 3 ,((TierImpl) dependentTiers.elementAt(firstAnnotation)).getAnnotationAtTime(begin));
 	        }
 	     }   
 	    
 	    //setWaitCursor(false);
         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);       
    }

    /**
     * The redo action.
     */
    public void redo() {  
    	createDependingAnnotations();
    	if(numberOfannotaionsCreated > 0){
        	if(MonitoringLogger.isInitiated()){
	         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS);
	        }
        }
    }
    
    /**
     * The undo action.
     */
    public void undo() {    	
		 int curPropMode = 0;
         curPropMode = transcription.getTimeChangePropagationMode();
         if (curPropMode != Transcription.NORMAL) {
             transcription.setTimeChangePropagationMode(Transcription.NORMAL);
         }
        // setWaitCursor(true);
         Annotation ann = null;
         
        for(int i=0; i< annotationTiers.size(); i++){	    		
        	TierImpl currentChildTier = (TierImpl)transcription.getTierWithId(annotationTiers.get(i));
 	        if(currentChildTier.isTimeAlignable()){
 	        	ann = currentChildTier.getAnnotationAtTime(begin);
 	        	if(ann != null){
 	        		currentChildTier.removeAnnotation(ann);
 	        	} 	        	
 	        }else{
 	        	long time = (begin + end) / 2;
 	        	ann = currentChildTier.getAnnotationAtTime(time);
 	        	if(ann != null){
 	        		currentChildTier.removeAnnotation(ann);
 	        	} 	
 	        }
 	     } 
        
        numberOfannotaionsCreated = 0 ;
        if(MonitoringLogger.isInitiated()){
         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS);
        }
         
//       Vector dependentTiers = tier.getDependentTiers();
// 	     if(dependentTiers != null){
// 	    	Annotation ann = null;
// 	    	for(int i=0; i< dependentTiers.size(); i++){	    		
// 	        		TierImpl currentChildTier = (TierImpl) dependentTiers.elementAt(i);
// 	        		
// 	        		if(currentChildTier.isTimeAlignable()){
// 	        			currentChildTier.removeAnnotation(currentChildTier.getAnnotationAtTime(begin));
// 	            	}else{
// 	            		long time = (begin + end) / 2;
// 	            		currentChildTier.removeAnnotation(currentChildTier.getAnnotationAtTime(time));            		
// 	            	} 
// 	        }	    	
// 	       // ((TranscriptionImpl) transcription).setNotifying(true);
// 	    	//((TranscriptionImpl) tier.getParent()).notifyListeners(tier, 3 ,((TierImpl) dependentTiers.elementAt(0)).getAnnotationAtTime(begin));
// 	     }   
         
        // setWaitCursor(false);

         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);
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
