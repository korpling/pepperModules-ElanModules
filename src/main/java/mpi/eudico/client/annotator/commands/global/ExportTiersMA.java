package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.export.ExportTiersDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JDialog;


/**
 * Creates an exporting tiers dialog
 *
 * @author Jeffrey Lemein
 * @version March 2010
 */
public class ExportTiersMA extends AbstractProcessMultiMA {
    /**
     * Creates a dialog for exporting tiers
     *
     * @param name action name
     * @param frame the parent frame
     */
    public ExportTiersMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a export tiers dialog
     */
    @Override
    public void actionPerformed(ActionEvent e) { 	
    	ArrayList<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportTabDialog.Title"));

        if ((files == null) || (files.size() == 0)) {
            return;
        }
        
        new ExportTiersDialog(frame, true, files).setVisible(true);
    }
}
