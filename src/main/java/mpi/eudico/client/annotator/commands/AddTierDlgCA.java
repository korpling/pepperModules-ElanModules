package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTierDialog2;

/**
 * DOCUMENT ME!
 * $Id: AddTierDlgCA.java 14860 2009-04-23 09:55:05Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class AddTierDlgCA extends CommandAction {
    /**
     * Creates a new AddTierDlgCA instance
     *
     * @param theVM the viewer manager
     */
    public AddTierDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.ADD_TIER);
    }

    /**
     * Creates a new edit tiers dialog command
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_TIER);
    }

    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns the arguments, here the Add mode constant
     *
     * @return the arguments
     */
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = new Integer(EditTierDialog2.ADD);
		args[1] = null;
        return args;
    }
}
