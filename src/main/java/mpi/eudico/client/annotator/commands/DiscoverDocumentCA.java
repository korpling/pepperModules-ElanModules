package mpi.eudico.client.annotator.commands;

import javax.swing.*;

import mpi.eudico.client.annotator.ViewerManager2;

public class DiscoverDocumentCA extends CommandAction {

	public DiscoverDocumentCA(ViewerManager2 theVM) {
		//super();
		super(theVM, ELANCommandFactory.DISCOVER_DOC);

		putValue(Action.NAME, "");
		updateLocale();
	}

	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.DISCOVER_DOC);
	}

	protected Object getReceiver() {
		return null;
	}
}