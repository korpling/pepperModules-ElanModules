package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.CHATExportDlg;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * DOCUMENT ME!
 *
 * @author Hennie Brugman
 */
public class ExportCHATCommand implements Command {
    private String commandName;

    /**
     * Creates a new StoreCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ExportCHATCommand(String name) {
        commandName = name;
    }

	public void execute(Object receiver, Object[] arguments) {
		//arguments:
		//[0]: TranscriptionStore eafTranscriptionStore
		//[1]: Vector visibleTiers
		Transcription tr = (Transcription) receiver;
		TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];

		List visibleTiers;
		if (arguments[1] != null) {
			visibleTiers = (List) arguments[1];
		} else {
			visibleTiers = new ArrayList(0);
		}
		
		JFrame fr = ELANCommandFactory.getRootFrame(tr);

		JOptionPane.showMessageDialog(fr, ElanLocale.getString("ExportCHATDialog.Message.CLANutility"), 
				"ELAN", JOptionPane.INFORMATION_MESSAGE);
		
		JDialog dlg = new CHATExportDlg(fr, true, tr, eafTranscriptionStore, visibleTiers);
		dlg.setVisible(true);
		
	}

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }
}
