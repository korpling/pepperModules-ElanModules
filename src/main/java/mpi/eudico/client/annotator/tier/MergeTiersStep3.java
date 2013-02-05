package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.MergeTiersCommand;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import mpi.eudico.client.annotator.util.ProgressListener;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;



/**
 * The final step, the actual calculation.  A command is created and this pane
 * is connected as progress listener. The ui is a progress monitor.
 *
 * @author Han Sloetjes
 * @version 1.0 July 2008
 */
public class MergeTiersStep3 extends CalcOverlapsStep3 implements ProgressListener {
	MergeTiersCommand com;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public MergeTiersStep3(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane, transcription);

        //initComponents();
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.Calculating");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    public boolean doFinish() {
        // disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);

        String tierName1 = (String) multiPane.getStepProperty("Source-1");
        String tierName2 = (String) multiPane.getStepProperty("Source-2");
        String destTier = (String) multiPane.getStepProperty("DestTier");
        String typeName = (String) multiPane.getStepProperty("Type");
        Boolean concat = null;
        String contentType = (String) multiPane.getStepProperty("ContentType");
        Integer format = null;// only relevant if concat = false
        if ("Duration".equals(contentType)) {
            format = (Integer) multiPane.getStepProperty("Format");
            concat = new Boolean(false);
        } else {
        	concat = new Boolean(true);
        }
        Boolean matchingValuesOnly = (Boolean) multiPane.getStepProperty("MatchingValuesOnly");
        Boolean specValuesOnly = (Boolean) multiPane.getStepProperty("SpecificValueOnly");
        String specValue = (String) multiPane.getStepProperty("SpecificValue");
        
        if ((tierName1 == null) || (tierName2 == null) || (destTier == null) ||
                (typeName == null)) {
            progressInterrupted(null,
                "Illegal argument: a tier or type could not be found");
        }

        // create a command and connect as listener
        
        com = (MergeTiersCommand) ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.MERGE_TIERS);
        com.addProgressListener(this);
        com.execute(transcription,
            new Object[] {
                tierName1, tierName2, destTier, typeName, concat, format, matchingValuesOnly,
                specValuesOnly, specValue
            });
	
        return false;
    }

}
