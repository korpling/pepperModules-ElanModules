package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.List;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Command that splits the active annotation into two annotations
 * if invoked from the context menu, then split is done at the point where
 * its invoked, otherwise it is split equally
 *
 * @author Aarthy Somasundaram
 * @version 1.0
 */
public class SplitAnnotationCommand implements UndoableCommand {
    private String commandName;
    private AnnotationDataRecord annotationRecord;
    private Transcription transcription;
    long splitTime;
    
    private boolean annotationsSplit = false;

    
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public SplitAnnotationCommand(String name) {
        commandName = name;
    }    
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  (can be null)
     * 			<ul><li>arg[0] = the annotation (Annotation)
     * 				<li>arg[1] = splitTime, 
     * 					time to split the annotation (long)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {    	
    	transcription = (Transcription) receiver;
    	
    	AlignableAnnotation annotation = null;
        if(arguments[0] instanceof AlignableAnnotation){
        	annotation = (AlignableAnnotation) arguments[0];        }
        if(annotation != null){
        	long begin = annotation.getBeginTimeBoundary();
        	long end = annotation.getEndTimeBoundary();
        	if(arguments.length > 1 &&  arguments[1] != null){
        		splitTime = (Long) arguments[1];
        	}else {
        		splitTime = (begin+end)/2;
        	}
        }
    	
    	splitAnnotation(annotation);    
    	if(annotationsSplit && MonitoringLogger.isInitiated()){
			   MonitoringLogger.getLogger(transcription).log(MonitoringLogger.SPLIT_ANNOTATION);
		}
    }    
    
    public void splitAnnotation(AlignableAnnotation annotation){
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        setWaitCursor(true);
        ((TranscriptionImpl) transcription).setNotifying(false);
        
    	if(annotation != null){
    	   long begin = annotation.getBeginTimeBoundary();
    	   long end = annotation.getEndTimeBoundary();  
    	   
    	   TierImpl tier = (TierImpl) annotation.getTier();
    	   
   		   String value = annotation.getValue();
   		   annotationRecord = new AnnotationDataRecord(annotation);        	
   		   if(tier.isTimeAlignable()){
   			   if(!tier.hasParentTier()){
   				   List childAnnotations = annotation.getParentListeners();
   				   annotation.updateTimeInterval(begin, splitTime);  	
   				   Annotation ann = tier.createAnnotation(splitTime, end);   				   
   				   annotationsSplit = true;
   				   
   				   if(ann != null){
   					   ann.setValue(value);
   				   }
   				   
   				   if(childAnnotations != null){
    				   for(int j=0; j< childAnnotations.size();j++){
    					   value = ((AbstractAnnotation)childAnnotations.get(j)).getValue();    	
    					   long time = (splitTime+end)/2;
    					   ann = ((TierImpl)((AbstractAnnotation)childAnnotations.get(j)).getTier()).createAnnotation(time, time);
    					   if(ann != null){
    						   ann.setValue(value);
    					   	}
    				   }
   				   }
   			   }   			   
   		   }
    	}

		setWaitCursor(false);
        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
        ((TranscriptionImpl) transcription).setNotifying(true);
    }

	public String getName() {
		return commandName;
	}
	
	public void undo() {
		 int curPropMode = 0;
         curPropMode = transcription.getTimeChangePropagationMode();
         if (curPropMode != Transcription.NORMAL) {
             transcription.setTimeChangePropagationMode(Transcription.NORMAL);
         }
         setWaitCursor(true);
         
         AlignableAnnotation ann = null;
         if ((annotationRecord != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());          
            if(tier != null){            
            	tier.removeAnnotation(tier.getAnnotationAtTime(splitTime));
            	ann = (AlignableAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());
            	if(ann != null){
            		ann.updateTimeInterval(annotationRecord.getBeginTime(), annotationRecord.getEndTime());
            	}
            } 
            
            annotationsSplit = false;
            if(MonitoringLogger.isInitiated()){
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.SPLIT_ANNOTATION);
            }
		}  
         
         setWaitCursor(false);

         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);
	}
	
	public void redo() {			
		if ((annotationRecord != null)) {
			TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
	        if(tier != null){
	        	AlignableAnnotation ann = (AlignableAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());
	        	splitAnnotation(ann);
	        }
	        
	        if(annotationsSplit && MonitoringLogger.isInitiated()){
	        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.SPLIT_ANNOTATION);
	        }
		}		
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
