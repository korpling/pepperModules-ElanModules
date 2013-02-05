/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;

import java.io.File;
import java.io.IOException;

import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.Transcription2TeX;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * $Id: ExportTeXDialog.java 27112 2011-10-31 13:51:54Z aarsom $
 *
 * @author $Author$
 * @version Aug 2005 Identity removed
 */
public class ExportTeXDialog extends AbstractTierExportDialog
    implements ListSelectionListener {
 
    /**
     * Creates a new ExportTabDialog instance
     *
     * @param parent DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param transcription DOCUMENT ME!
     * @param selection DOCUMENT ME!
     */
    public ExportTeXDialog(Frame parent, boolean modal,
        Transcription transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        updateLocale();
        postInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        String filename = "resources/testdata/elan/elan-example2.eaf";
        Transcription transcription = new TranscriptionImpl(filename);
        JFrame frame = new JFrame();
        javax.swing.JDialog dialog = new ExportTeXDialog(frame, false,
                transcription, null);
        dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.AbstractExportDialog#export(File)
     */
    public boolean startExport() throws IOException {
        String fileExtension = "tex";
        File exportFile = promptForFile("ExportTeXDialog.Title",
               null, new String[] { fileExtension }, false);

        if (restrictCheckBox.isSelected()) {
            Transcription2TeX.exportTiers(transcription,
                (String[]) getSelectedTiers().toArray(new String[0]),
                exportFile, selection.getBeginTime(),
                selection.getEndTime());
        } else {
            Transcription2TeX.exportTiers(transcription,
                (String[]) getSelectedTiers().toArray(new String[0]),
                exportFile);
        }

        return true;
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    public void valueChanged(ListSelectionEvent lse) {
        if ((model != null) && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model.findColumn(EXPORT_COLUMN);

            for (int i = b; i <= e; i++) {
                if (tierTable.isRowSelected(i)) {
                    model.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {
                Vector v = transcription.getTiers();
                TierImpl t;

                for (int i = 0; i < v.size(); i++) {
                    t = (TierImpl) v.get(i);

                    // add all
                    if (i == 0) {
                        model.addRow(new Object[] { Boolean.TRUE, t.getName() });
                    } else {
                        model.addRow(new Object[] { Boolean.FALSE, t.getName() });
                    }
                }
            }
        }
    }

    protected void makeLayout() {
        super.makeLayout();
        model.setColumnIdentifiers(new String[] { EXPORT_COLUMN, TIER_NAME_COLUMN });
        tierTable.getColumn(EXPORT_COLUMN).setCellEditor(new DefaultCellEditor(
                new JCheckBox()));
        tierTable.getColumn(EXPORT_COLUMN).setCellRenderer(new CheckBoxTableCellRenderer());
        tierTable.getColumn(EXPORT_COLUMN).setMaxWidth(30);
        tierTable.setShowVerticalLines(false);
        tierTable.setTableHeader(null);
        tierTable.getSelectionModel().addListSelectionListener(this);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);
    }

    protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportTeXDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTeXDialog.TitleLabel"));
    }
}
