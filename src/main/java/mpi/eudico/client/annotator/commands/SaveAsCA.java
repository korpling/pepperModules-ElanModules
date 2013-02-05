package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class SaveAsCA extends CommandAction {
    private TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SaveAsCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE_AS);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.STORE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object[] getArguments() {
    	if (vm.getMultiTierControlPanel() != null) {
	        return new Object[] {
	            transcriptionStore, new Boolean(false), new Boolean(true),
	            vm.getMultiTierControlPanel().getVisibleTiers()
	        };
    	} else {
	        return new Object[] {
		            transcriptionStore, new Boolean(false), new Boolean(true),
		            new ArrayList(0)
		        };
    	}
    }
}
