package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.webserviceclient.WebServicesDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A command that creates and shows the Web Services window.
 * @author Han Sloetjes
 */
public class WebServicesDlgCommand implements Command {
	private String name;
	
	/**
	 * Constructor.
	 * @param name name of the command
	 */
	public WebServicesDlgCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver the viewermanager (or transcription?)
	 * @param arguments null
	 */
	public void execute(Object receiver, Object[] arguments) {
		ViewerManager2 vm = (ViewerManager2) receiver;
		WebServicesDialog dialog = null;
		if (vm != null) {
			dialog = new WebServicesDialog(
				ELANCommandFactory.getRootFrame(vm.getTranscription()), false);
		} else {
			dialog = new WebServicesDialog();
		}
		dialog.setTranscription((TranscriptionImpl) vm.getTranscription());
		dialog.setVisible(true);
	}

	/**
	 * Returns the name
	 */
	public String getName() {
		return name;
	}

}
