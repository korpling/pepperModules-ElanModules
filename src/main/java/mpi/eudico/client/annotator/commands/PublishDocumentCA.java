package mpi.eudico.client.annotator.commands;

import javax.swing.*;

import mpi.eudico.client.annotator.ViewerManager2;

public class PublishDocumentCA extends CommandAction {

	public PublishDocumentCA(ViewerManager2 theVM) {
		//super();
		super(theVM, ELANCommandFactory.PUBLISH_DOC);

        putValue(Action.NAME, "");
	}

	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PUBLISH_DOC);
	}

	protected Object getReceiver() {
		return vm.getTranscription();
	}
}
