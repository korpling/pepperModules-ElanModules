package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JDialog;
import javax.swing.WindowConstants;


/**
 * Test class.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class Test {
    private TranscriptionImpl trans;

    /**
     * Creates a new Test instance
     *
     * @param t DOCUMENT ME!
     */
    public Test(Transcription t) {
        trans = (TranscriptionImpl) t;
    }

    /**
     * DOCUMENT ME!
     */
    public void test() {
        MultiStepPane pane = new MultiStepPane();
        StepPane step1 = new CopyTierStep1(pane, trans);
        StepPane step2 = new CopyTierStep2(pane, trans);
        StepPane step3 = new CopyTierStep3(pane, trans);
        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(trans), 
        		"Reparent test", true);
        //dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        //dialog.pack();
        dialog.setVisible(true);
    }
}
