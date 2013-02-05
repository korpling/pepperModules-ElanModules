package mpi.eudico.client.annotator.commands;

import java.util.Vector;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * Split the active annotation equally into two annotations
 *
 * @author Aarthy Somasundaram
 * @version 1.0,  Nov 2010
 */
public class SplitAnnotationCA extends CommandAction implements ActiveAnnotationListener{
					
	 private Annotation activeAnnotation;
	 
	 /**
	  * Creates a new SplitAnnotationCA instance
	  *
	  * @param viewerManager the viewerManager
	  */
	 public SplitAnnotationCA(ViewerManager2 viewerManager) {
		 super(viewerManager, ELANCommandFactory.SPLIT_ANNOTATION);	    
		 viewerManager.connectListener(this);
	     activeAnnotation = viewerManager.getActiveAnnotation().getAnnotation();
	     setEnabled(false);	
	 }
	   
	 /**
	  * Creates a new command.
	  */
	 protected void newCommand() {
	     command = ELANCommandFactory.createCommand(vm.getTranscription(),
	             ELANCommandFactory.SPLIT_ANNOTATION);	     
	 }

	 /**
	  *
	  * @return the receiver, the transcription
	  */
	 protected Object getReceiver() {
	     return vm.getTranscription();
	 }
	   
	 /**	 
	  * @return array, with the annotation to be split
	  */
	 protected Object[] getArguments() {
		Object[] arg = new Object[1];
		arg[0] = activeAnnotation;
	  	return arg;
	 }
	 	    
	 /**
	  * On a change of ActiveAnnotation perform a check to determine whether
	  * this action should be enabled or disabled.<br>
	  *
	  * @see ActiveAnnotationListener#updateActiveAnnotation()
	  */
	 public void updateActiveAnnotation() {
	     activeAnnotation = vm.getActiveAnnotation().getAnnotation();
	     checkState();
	 }
	 
	 private Vector getDependingAnnotation(TierImpl tier){
		 Vector dependentTiers = tier.getDependentTiers();
		 Vector dependingAnnotations = new Vector();
		 for(int i=0;i < dependentTiers.size();i++){
			 if( ((TierImpl) dependentTiers.get(i)).getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
				continue; 
			 }
			 dependingAnnotations.addAll(activeAnnotation.getChildrenOnTier((Tier) dependentTiers.get(i)));
		 }
		 return dependingAnnotations;
	 }

	 /**
	  * Checks whether there is an active annotation.
	  */
	 protected void checkState() {	
		 setEnabled(false);
	     if (activeAnnotation != null) {
	    	 TierImpl tier = (TierImpl) activeAnnotation.getTier();	  	   
	  	   	 if(tier.isTimeAlignable() && !tier.hasParentTier()){ 	 
	  	   		 Vector childTiers = tier.getChildTiers();
	  	   		 if(childTiers!= null){
	  	   			 boolean valid = false;
	  	   			 for(int i=0; i <childTiers.size(); i++){
	  	   				 TierImpl childTier = (TierImpl)childTiers.get(i);
	  	   				 if(childTier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION){
	  	   					 break;
	  	   				 }
	  	   				 valid = true;
	  	   			 }
	  	   			 setEnabled(valid);
	  			 
	  	   			 if(!valid){
	  	   				 if(getDependingAnnotation(tier).size() == 0){
	  	   					 setEnabled(true);
	  	   				 }
	  	   			 }
	  	   		 }
	  	   	 }
	     }	  		   
	 }
}
