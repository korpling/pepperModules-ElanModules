package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that creates a Command for shoebox style export.
 *
 * @author Han Sloetjes
 */
public class ExportShoeboxCA extends CommandAction {
    /**
     * Creates a new ExportShoeboxCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public ExportShoeboxCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_SHOEBOX);
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_SHOEBOX);
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return DOCUMENT ME!
     */
    protected Object getReceiver() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
    }
}
