package mpi.eudico.client.annotator.commands;

import javax.swing.Action;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates the "annotations from overlaps wizard".
 */
public class AnnotationsFromOverlapsClasDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param viewerManager the ViewerManager
     */
    public AnnotationsFromOverlapsClasDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANN_FROM_OVERLAP);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.ANN_FROM_OVERLAP_COM_CLAS);
    }
    
    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

	@Override
	public void updateLocale() {
		super.updateLocale();
		putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.ANN_FROM_OVERLAP) + " (Classic)");
	}
    
    
}
