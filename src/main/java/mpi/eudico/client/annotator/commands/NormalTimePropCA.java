package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 *
 */
public class NormalTimePropCA extends CommandAction {
    /**
     * Creates a new NormalTimePropCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public NormalTimePropCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.TIMEPROP_NORMAL);
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.TIMEPROP_NORMAL);
    }

    /**
     *
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns null, no arguments need to be passed.
     *
     * @return DOCUMENT ME!
     */
    protected Object[] getArguments() {
        return null;
    }
}
