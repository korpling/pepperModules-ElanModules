package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.Vector;

import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Import tiers (if necessary with Linguistic Types and ControlledVocabularies)
 * from an eaf or etf into a transcription. 
 */
public class ImportTiersCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    
    private ArrayList tiersAdded;
    private ArrayList typesAdded;
    private ArrayList cvsAdded;
    
    public ImportTiersCommand(String name) {
        commandName = name;
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    public void undo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        if (tiersAdded == null) {
            LOG.warning("No tiers have been added.");
            return;
        }
        for (int i = 0; i < tiersAdded.size(); i++) {
            transcription.removeTier((TierImpl) tiersAdded.get(i));
        }
        for (int i = 0; i < typesAdded.size(); i++) {
            transcription.removeLinguisticType((LinguisticType) typesAdded.get(i));
        }
        for (int i = 0; i < cvsAdded.size(); i++) {
            transcription.removeControlledVocabulary((ControlledVocabulary) cvsAdded.get(i));
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    public void redo() {
        if(transcription == null) {
            LOG.warning("The transcription is null.");
            return;
        }
        if (tiersAdded == null) {
            LOG.warning("No tiers can be added.");
            return;
        }
        for (int i = 0; i < cvsAdded.size(); i++) {
            transcription.addControlledVocabulary((ControlledVocabulary) cvsAdded.get(i));
        }       
        for (int i = 0; i < typesAdded.size(); i++) {
            transcription.addLinguisticType((LinguisticType) typesAdded.get(i));
        }
        for (int i = 0; i < tiersAdded.size(); i++) {
            transcription.addTier((TierImpl) tiersAdded.get(i));
        }

    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the fileName of an eaf
     *        or etf file (String)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        String fileName = (String) arguments[0];
        if (fileName == null) {
            LOG.warning("The filename is null");
            return; // report??
        }
        
        fileName = FileUtility.pathToURLString(fileName).substring(5);
        
        TranscriptionImpl srcTrans = null;
        try {
            EAFSkeletonParser parser = new EAFSkeletonParser(fileName);
            parser.parse();
            ArrayList tiers = parser.getTiers();
            ArrayList tierOrder = parser.getTierOrder();
            ArrayList types = parser.getLinguisticTypes();
            ArrayList cvs = parser.getControlledVocabularies();
            
            srcTrans = new TranscriptionImpl();
            //srcTrans.setNotifying(false);
            srcTrans.setLinguisticTypes(new Vector(types));

            String tierName;
            TierImpl tier;
            for (int i = 0; i < tierOrder.size(); i++) {
                tierName = (String) tierOrder.get(i);
                for (int j = 0; j < tiers.size(); j++) {
                	tier = (TierImpl)tiers.get(j);
                	if (tierName.equals(tier.getName())) {
                		srcTrans.addTier(tier);	
                	}
                }
            }
            
            for (int i = 0; i < cvs.size(); i++) {
                srcTrans.addControlledVocabulary((ControlledVocabulary)cvs.get(i));
            }
            
        } catch (ParseException pe) {
            LOG.warning(pe.getMessage());
            pe.printStackTrace();
            return;
        }
        // store current tiers types, cvs
        //transcription.setNotifying(false);
        
        ArrayList currentTiers = new ArrayList(transcription.getTiers());
        ArrayList currentTypes = new ArrayList(transcription.getLinguisticTypes());
        ArrayList currentCvs = new ArrayList(transcription.getControlledVocabularies());
        MergeUtil mergeUtil = new MergeUtil();
        ArrayList tiersAddable = mergeUtil.getAddableTiers(srcTrans, transcription, null);
        if (tiersAddable == null || tiersAddable.size() == 0) {
            LOG.warning("There are no tiers that can be imported");
            transcription.setNotifying(true);
            return;
        }
        tiersAddable = mergeUtil.sortTiers(tiersAddable);
        mergeUtil.addTiersTypesAndCVs(srcTrans, transcription, tiersAddable);
        //store the tiers, types and cvs that have been added, for undo/redo
        //transcription.setNotifying(true);
        
        tiersAdded = new ArrayList();
        typesAdded = new ArrayList();
        cvsAdded = new ArrayList();
        
        for (int i = 0; i < transcription.getTiers().size(); i++) {
            if (!currentTiers.contains(transcription.getTiers().get(i))) {
                tiersAdded.add(transcription.getTiers().get(i));
            }
        }
        for (int i = 0; i < transcription.getLinguisticTypes().size(); i++) {
            if (!currentTypes.contains(transcription.getLinguisticTypes().get(i))) {
                typesAdded.add(transcription.getLinguisticTypes().get(i));
            }
        }
        for (int i = 0; i < transcription.getControlledVocabularies().size(); i++) {
            if (!currentCvs.contains(transcription.getControlledVocabularies().get(i))) {
                cvsAdded.add(transcription.getControlledVocabularies().get(i));
            }
        }
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }


}
