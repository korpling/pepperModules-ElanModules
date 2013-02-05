package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 *
 */
public class BulldozerModeCA extends CommandAction {
    /**
     * Creates a new BulldozerModeCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public BulldozerModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.BULLDOZER_MODE);
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BULLDOZER_MODE);
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
