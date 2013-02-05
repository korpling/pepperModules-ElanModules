package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.HashMap;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;

/**
 * A Command to import Linguistic Types (and referenced Controlled Vocabularies) from an .eaf or .etf 
 * file into an existing transcription. 
 */
public class ImportLinguisticTypesCommand implements UndoableCommand,
        ClientLogger {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    
    private ArrayList typesAdded = new ArrayList();
    private ArrayList cvsAdded = new ArrayList();
    
    /**
     * Creates a new instance of the command
     * 
     * @param name the name of the command
     */
    public ImportLinguisticTypesCommand(String name) {
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
        for (int i = 0; i < cvsAdded.size(); i++) {
            transcription.addControlledVocabulary((ControlledVocabulary) cvsAdded.get(i));
        }       
        for (int i = 0; i < typesAdded.size(); i++) {
            transcription.addLinguisticType((LinguisticType) typesAdded.get(i));
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
        ArrayList impTypes;
        ArrayList impCVs;
        try {
            EAFSkeletonParser parser = new EAFSkeletonParser(fileName);
            parser.parse();

            impTypes = parser.getLinguisticTypes();
            ArrayList cvs = parser.getControlledVocabularies();
            impCVs = new ArrayList(cvs.size());
            LinguisticType lt;
            ControlledVocabulary cv;
            String cvName;
            
            typeloop:
            for (int i = 0; i < impTypes.size(); i++) {
                lt = (LinguisticType) impTypes.get(i);
                if (lt.getControlledVocabylaryName() != null && lt.getControlledVocabylaryName().length() > 0) {
                    cvName = lt.getControlledVocabylaryName();
                    for (int j = 0; j < cvs.size(); j++) {
                        cv = (ControlledVocabulary) cvs.get(j);
                        if (cv.getName().equals(cvName)) {
                            impCVs.add(cv);
                            continue typeloop;
                        }
                    }
                }
            }
           
        } catch (ParseException pe) {
            LOG.warning(pe.getMessage());
            pe.printStackTrace();
            return;
        }
        
        ArrayList currentTypes = new ArrayList(transcription.getLinguisticTypes());
        ArrayList currentCvs = new ArrayList(transcription.getControlledVocabularies());
        
        addCVsAndTypes(impCVs, impTypes);
        
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
     * Returns the name of the command.
     * 
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    public String getName() {
        return commandName;
    }
    
    /**
     * Adds and if necessary renames Controlled Vocabularies and Linguistic Types.
     * 
     * @param cvs the list of CV's to add
     * @param typesToAdd the list of Linguistic Types to add
     */
    private void addCVsAndTypes(ArrayList cvs, ArrayList typesToAdd) {
        if (cvs == null) {
            LOG.info("No Controlled Vocabularies to add");
            cvs = new ArrayList(0);
            //return;
        }
        if (typesToAdd == null) {
            LOG.info("No Linguistic Types to add.");
            return;
        }
        HashMap renamedCVS = new HashMap(5);
        ControlledVocabulary cv;
        ControlledVocabulary cv2 = null;
        LinguisticType lt;
        String typeName;
        
        // add CV's, renaming when necessary
        for (int i = 0; i < cvs.size(); i++) {
            cv = (ControlledVocabulary) cvs.get(i);
            cv2 = transcription.getControlledVocabulary(cv.getName());

            if (cv2 == null) {
                transcription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-cp";
                int c = 1;
                while (transcription.getControlledVocabulary(newCVName + c) != null) {
                    c++;
                }
                newCVName = newCVName + c;
                LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                transcription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            }
        }
        // add linguistic types
        for (int i = 0; i < typesToAdd.size(); i++) {
            lt = (LinguisticType) typesToAdd.get(i);

            typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary() &&
                    renamedCVS.containsKey(lt.getControlledVocabylaryName())) {
                cv2 = (ControlledVocabulary) renamedCVS.get(lt.getControlledVocabylaryName());
                lt.setControlledVocabularyName(cv2.getName());
            }

            if (transcription.getLinguisticTypeByName(typeName) != null) {
                LOG.warning("Transcription already contains a Linguistic Type named: " + typeName);
                continue;
            }
            transcription.addLinguisticType(lt);
            LOG.info("Added Linguistic Type: " +
                        typeName);
        } // end linguistic types
    }

}
