package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * The first panel of a wizard that merges the annotations on two
 * tiers and creates annotations for each new segment on a third tier. The
 * duration of each new annotation is the sum of the duration of the overlapping 
 * annotations.<br>
 * This panel shows two tables with the time-alignable tiers, in both one can
 * be selected.
 * 
 * @vesrion 1.0 July 2008
 * @author Han Sloetjes
 */
public class MergeTiersStep1 extends CalcOverlapsStep1 implements ListSelectionListener {

    /**
     * Constructor.
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public MergeTiersStep1(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane, transcription);
    }

    /**
     * Initialize ui components etc.
     */
    public void initComponents() {
    	super.initComponents();
//        firstLabel = new JLabel(ElanLocale.getString(
//            "OverlapsDialog.Label.First"));
//        secLabel = new JLabel(ElanLocale.getString(
//            "OverlapsDialog.Label.Second"));
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.SelectTiers");
    }
}
