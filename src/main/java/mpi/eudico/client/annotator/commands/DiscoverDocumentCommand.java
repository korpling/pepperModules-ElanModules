package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.p2p.PublishAndDiscoverPanel;
import mpi.eudico.p2p.ElanP2P;

/**
 *
 */
public class DiscoverDocumentCommand implements Command {
	private String commandName;

	public DiscoverDocumentCommand(String name) {
		commandName = name;
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver the ElanP2P object
	 * @param arguments the arguments:  <ul>
	 * 	<li>arg[0] = the ElanFrame2 object</li>
	 * </ul>
	 */
	public void execute(Object receiver, Object[] arguments) {
		PublishAndDiscoverPanel panel = new PublishAndDiscoverPanel();
		ElanFrame2 parent = null;
		if (arguments != null && arguments.length > 0) {
			if (arguments[0] instanceof ElanFrame2) {
				parent = (ElanFrame2)arguments[0];
			}
		}
		String docName = null;
		String name = null;
		String email = null;

		int option = -1;
		do {
			option = JOptionPane.showOptionDialog(
				parent, panel, ElanLocale.getString("Menu.P2P.DiscoverDocument"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, null, null);
			if (option != JOptionPane.OK_OPTION) {
				return;
			} else {
				docName = panel.getDocumentName();
				name = panel.getName();
				email = panel.getEmail();
				if (
					docName == null || docName.length() == 0 ||
					name == null || name.length() == 0 ||
					email == null || email.length() == 0 ||
					email.indexOf('@') < 1) {
					JOptionPane.showMessageDialog(
						parent, ElanLocale.getString("P2P.PublishAndDiscoverPanel.Message.Required"),
						ElanLocale.getString("Message.Warning"),
						JOptionPane.WARNING_MESSAGE);
				} else  {
					break;
				}
			}
		} while (true);
		// do something with the docname, name and email address
		
		// ((ElanP2P)receiver).startClient(docName, name, email);
		
		// temp progress monitor solution
		final String doc = docName;
		final String nm = name;
		final String em = email;
		final ElanP2P rec = (ElanP2P)receiver;
		final ElanFrame2 fr = parent;
		new Thread(new Runnable(){
			public void run() {
				JProgressBar progBar = new JProgressBar(0, 100);
				progBar.setIndeterminate(true);
				progBar.setPreferredSize(new Dimension(220, 26));
				JDialog dialog = new JDialog(fr, ElanLocale.getString("Menu.P2P.DiscoverDocument"));
				dialog.getContentPane().add(progBar);
				dialog.pack();
				dialog.setLocationRelativeTo(fr);
				dialog.show();
				
				((ElanP2P)rec).startClient(doc, nm, em);
				
				dialog.dispose();
			}
		}).start();

	}

	public String getName() {
		return commandName;
	}

}
