package mpi.eudico.client.annotator.tier;

import java.util.List;
import java.util.Vector;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.AnnotationsFromOverlapsUndoableCommand;
import mpi.eudico.client.annotator.commands.AnnotationsFromSubtractionUndoableCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for Step 5: The final step and actual calculation.  A command is created and this pane
 * is connected as progress listener. The ui is a progress monitor.
 *
 * @author Han Sloetjes , Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
public class OverlapsOrSubtractionStep5 extends ProgressStepPane {
    private TranscriptionImpl transcription;
    private boolean subtractionDialog;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public OverlapsOrSubtractionStep5(MultiStepPane multiPane, TranscriptionImpl transcription) {
       this(multiPane, transcription, false);
    }
    
    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public OverlapsOrSubtractionStep5(MultiStepPane multiPane, TranscriptionImpl transcription, boolean subtractionDialog) {
        super(multiPane);
        this.subtractionDialog = subtractionDialog;
        this.transcription = transcription;
        initComponents();
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.Calculating");
    }

    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
        doFinish();
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    public boolean doFinish() {
    	completed = false;
        
    	// disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
		
		//retrieve selected tier names
		Vector selectedValues = (Vector) multiPane.getStepProperty("SelectedTiers");
		String sourceTiers[] = new String[selectedValues.size()];
		for( int i=0; i<selectedValues.size(); i++ )
			sourceTiers[i] = (String) selectedValues.get(i);		
		
		//retrieve name for destination tier
		String destTierName = (String) multiPane.getStepProperty("DestinationTierName");
		
		//retrieve linguistic type		
		String destLingType = (String) multiPane.getStepProperty("linguisticType");		
		
		String parentTierName = (String) multiPane.getStepProperty("ParentTierName");	
	
		//retrieve annotation value type
		int annotationValueType = (Integer) multiPane.getStepProperty("AnnotationValueType");	
		
		//store time format
		String timeFormat = (String) multiPane.getStepProperty("TimeFormat");
		boolean usePalFormat = (Boolean) multiPane.getStepProperty("UsePalFormat");
		
		//retrieve annotation value
		String annWithValue = (String) multiPane.getStepProperty("AnnotationValue");
		
		//annotation from specific tier name
		String annFromTier = (String) multiPane.getStepProperty("AnnFromTier");
		
		int transcriptionMode = (Integer) multiPane.getStepProperty("TranscriptionMode");
		
		List  openedFileList = (List) multiPane.getStepProperty("OpenedFiles");
		Object[] filenames;
		if( openedFileList != null )
			filenames = openedFileList.toArray();
		else
			filenames = null;	
		
		Object args[] = null;
		AnnotationFromOverlaps com = null;
		if(subtractionDialog){
			//referenceTierName
			String referenceTierName = (String) multiPane.getStepProperty("ReferenceTierName");
			
			args = new Object[]{ sourceTiers, destTierName, destLingType,
					annotationValueType, timeFormat, annWithValue, transcriptionMode, filenames,
					usePalFormat, parentTierName, referenceTierName };
			
			// create a command and connect as listener
			if( transcription != null )
				com = (AnnotationsFromSubtractionUndoableCommand)ELANCommandFactory.createCommand(transcription, ELANCommandFactory.ANN_FROM_SUBTRACTION);
			else{
				com = new AnnotationFromSubtraction(ELANCommandFactory.ANN_FROM_OVERLAP);
				
			}			  
			
		}else{
			//retrieve overlaps criteria
			int overlapsCriteria = (Integer) multiPane.getStepProperty("overlapsCriteria");
			
			//retrieve specific settings
			List<String[]> tierValuePairs = (List<String[]>) multiPane.getStepProperty("tierValueConstraints");
			
			List tierOrder = (List) multiPane.getStepProperty("TierOrder");
			
			args = new Object[]{ sourceTiers, destTierName, destLingType,
					annotationValueType, timeFormat, annWithValue, annFromTier, tierOrder, 
					overlapsCriteria, tierValuePairs, transcriptionMode, filenames,
					usePalFormat, parentTierName };
			
			// create a command and connect as listener
			if( transcription != null )
				com = (AnnotationsFromOverlapsUndoableCommand)ELANCommandFactory.createCommand(transcription, ELANCommandFactory.ANN_FROM_OVERLAP);
			else{
				com = new AnnotationFromOverlaps(ELANCommandFactory.ANN_FROM_OVERLAP);				
			}  
		}
		com.addProgressListener(this);		
		com.execute(transcription, args);
		
		// the action is performed on a separate thread, don't close
        return false;
    }
}
