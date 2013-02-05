package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.client.annotator.gui.EditTierDialog2;


/**
 * Brings up a JDialog for importing tiers.
 *
 * @author Han Sloetjes
 */
public class ImportTiersDlgCA extends CommandAction {
    /**
     * Creates a new ImportTiersDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ImportTiersDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.IMPORT_TIERS);
    }

    /**
     * Creates a new edit tier dialog command
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_TIER);
    }

    /**
     * Returns the receiver of the command
     *
     * @return the receiver
     */
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * The arguments, i.e the initial mode of the dialog.
     *
     * @return the arguments
     */
    protected Object[] getArguments() {
        return new Object[] { new Integer(EditTierDialog2.IMPORT), null };
    }
}
