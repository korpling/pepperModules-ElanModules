package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 * A Command to merge two Controlled Vocabularies from two different Transcriptions,
 * i.e. entries present in the second CV but not in the first are copied to the first
 * CV.
 *
 * @author Han Sloetjes
 */
public class MergeCVSCommand implements UndoableCommand {
    private String commandName;
    
    // receiver
    private TranscriptionImpl transcription;
    private ControlledVocabulary conVoc;
    private ControlledVocabulary secConVoc;
    private ArrayList copiedEntries;
    
    /**
     * Creates a new MergeCVSCommand
     */
    public MergeCVSCommand(String name) {
        commandName = name;
    }

    /**
     * Removes the entries that have been added by the merging process
     * 
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    public void undo() {
        if (conVoc != null && copiedEntries != null && copiedEntries.size() > 0) {
            CVEntry entry;
            for (int i = 0; i < copiedEntries.size(); i++) {
                entry = (CVEntry) copiedEntries.get(i);
                conVoc.removeEntry(entry);
            }
        }
    }

    /**
     * Again adds the previously added entries.
     * 
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    public void redo() {
        if (conVoc != null && copiedEntries != null && copiedEntries.size() > 0) {
            CVEntry entry;
            for (int i = 0; i < copiedEntries.size(); i++) {
                entry = (CVEntry) copiedEntries.get(i);
                conVoc.addEntry(entry);
            }
        }
    }

    /**
     * Merges two ControlledVocabularies.<br>
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the first Controlled
     *        Vocabulary (Controlled Vocabulary)</li> <li>arg[1] = the second
     *        Controlled Vocabulary (Controlled Vocabulary)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        conVoc = (ControlledVocabulary) arguments[0];
        secConVoc = (ControlledVocabulary) arguments[1];
        
        if ((transcription.getControlledVocabulary(conVoc.getName()) != null) &&
                (secConVoc != null)) {
            copiedEntries = new ArrayList();
            CVEntry[] first = conVoc.getEntriesSortedByAlphabet();
            CVEntry[] second = secConVoc.getEntriesSortedByAlphabet();
            CVEntry nextEntry;
            outerloop:
            for (int i = 0; i < second.length; i++) {
                for (int j = 0 ; j < first.length; j++) {
                    // compare second[i] en first[j]
                    if (first[j].getValue().equals(second[i].getValue())) {
                        continue outerloop;
                    }
                    if (j == first.length - 1 || first[j].getValue().compareTo(second[i].getValue()) > 0) {
                        // the second entry is not in the list...
                        nextEntry = new CVEntry(second[i].getValue(), second[i].getDescription());
                        copiedEntries.add(nextEntry);
                        conVoc.addEntry(nextEntry );
                        continue outerloop;
                    }
                }
            }
            
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }

}
