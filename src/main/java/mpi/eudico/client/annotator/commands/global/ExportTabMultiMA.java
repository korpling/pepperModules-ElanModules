package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.export.ExportTabDialog;

import java.awt.event.ActionEvent;
import java.io.File;

import java.util.ArrayList;


/**
 * Creates a dialog to select tiers from multiple files for tab delimited text
 * export.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportTabMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportTabMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportTabMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates an ExportTabDialog.
     *
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        ArrayList<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportTiersDialog.Title"));

        if ((files == null) || (files.size() == 0)) {
            return;
        }

        new ExportTabDialog(frame, true, files).setVisible(true);
    }
}
