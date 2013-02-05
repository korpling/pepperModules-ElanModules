package mpi.eudico.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;

import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.util.ACMEditableObject;


/**
 * this class adds undo/redo functionality to the super class
 *
 * @author klasal $Id: ControlledVocabulary.java 20115 2010-09-29 12:34:59Z wilelb $
 *
 */
public class ControlledVocabulary extends BasicControlledVocabulary {
    private final List undoableEditListeners = new ArrayList();
    private boolean changed = false;
    private ACMEditableObject acmEditableObj;

    /**
     *
     * @param name name of this cv
     */
    public ControlledVocabulary(String name) {
        super(name);
    }

    /**
     *
     * @param name name of this cv
     * @param description description of this cv
     */
    public ControlledVocabulary(String name, String description) {
        super(name, description);
    }

    /**
     * @param entry entry to be added
     * @return true if action was completed successfully
     */
    public boolean addEntry(CVEntry entry) {
        boolean b = super.addEntry(entry);

        if (b && !initMode) {
            fireUndoableEditUpdate(new UndoableEditEvent(this,
                    new UndoableCVEntryAdd(entry)));
        }

        return b;
    }

    
    /**
     * Fires an undoable edit event if the entry is replaced
     * 
	 * @see mpi.eudico.util.BasicControlledVocabulary#replaceEntry(mpi.eudico.util.CVEntry, mpi.eudico.util.CVEntry)
	 */
	public boolean replaceEntry(CVEntry oldEntry, CVEntry newEntry) {
		boolean b = super.replaceEntry(oldEntry, newEntry);
		
		if (b) {
			fireUndoableEditUpdate(new UndoableEditEvent(this, 
					new UndoableCVEntryReplace(oldEntry, newEntry)));
		}
		
		return b;
	}

	/**
     * Add listener
     * @param l listener
     */
    public void addUndoableEditListener(UndoableEditListener l) {
        if (!undoableEditListeners.contains(l)) {
            undoableEditListeners.add(l);
        }
    }

    /**
     * moves array of entries in a direction specified by moveType
     * @param entriesToBeMoved array of entries to be moved
     * @param moveType direction of the move
     */
    public void moveEntries(CVEntry[] entriesToBeMoved, int moveType) {
        CVEntry[] oldEntries = getEntries();
        super.moveEntries(entriesToBeMoved, moveType);
        fireUndoableEditUpdate(new UndoableEditEvent(this,
                new UndoableCVGlobalChange(oldEntries, "Move entries")));
    }
    
    /**
     * Returns a sorted array of entries. The values are sorted  using the
     * String.compareTo(String) method.
     *
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByAlphabet() {
        CVEntry[] oldEntries = getEntries();   
        CVEntry[] sortedEntries = super.getEntriesSortedByAlphabet();
        
        fireUndoableEditUpdate(new UndoableEditEvent(this,
                new UndoableCVGlobalChange(oldEntries, "Sort entries")));
		return sortedEntries;
    }

    /**
     * Returns a reverse sorted array of entries. The values are sorted  using the
     * String.compareTo(String) method.
     *
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByReverseAlphabetOrder() {
    	CVEntry[] oldEntries = getEntries();   
        CVEntry[] sortedEntries = super.getEntriesSortedByReverseAlphabetOrder();
        
        fireUndoableEditUpdate(new UndoableEditEvent(this,
                new UndoableCVGlobalChange(oldEntries, "Sort entries")));
		return sortedEntries;  
    }
    
    /**
     * @param entriesToBeRemoved list of entries
     * @return true if action was completed successfully
     */
    public boolean removeEntries(CVEntry[] entriesToBeRemoved) {
        CVEntry[] oldEntries = getEntries();
        boolean b = super.removeEntries(entriesToBeRemoved);

        if (b) {
            fireUndoableEditUpdate(new UndoableEditEvent(this,
                    new UndoableCVGlobalChange(oldEntries, "Delete entries")));
        }

        return b;
    }

    /**
     * remove listener
     * @param l listener
     */
    public void removeUndoableEditListener(UndoableEditListener l) {
        if (undoableEditListeners.contains(l)) {
            undoableEditListeners.remove(l);
        }
    }

    /**
     * notifies listeners
     * @param e event
     */
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        for (int i = undoableEditListeners.size() - 1; i >= 0; i--) {
            ((UndoableEditListener) undoableEditListeners.get(i)).undoableEditHappened(e);
        }
    }

    /**
	 * @see mpi.eudico.util.BasicControlledVocabulary#handleModified()
	 */
	protected void handleModified() {
		changed = true;
		if(acmEditableObj != null){
			acmEditableObj.modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, this);
		}
	}

	/**
	 * Returns whether the CV has changed.
	 * 
	 * @return the changed flag
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Sets the changed state. Can be called to reset to the unchanged state e.g. 
	 * after a save action or, at load time, after all entries have been added.
	 * 
	 * @param changed the changed state flag
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	/**
     * class representing an addition of an event
     * @author klasal
     *
     */
    class UndoableCVEntryAdd extends AbstractUndoableEdit {
        private final CVEntry entry;

        /**
         * 
         * @param entry added CVentry
         */
        UndoableCVEntryAdd(CVEntry entry) {
            this.entry = entry;
        }

        /**
         * 
         * @return name of the event
         */
        public String getRepresentationName() {
            return "add Entry";
        }

        /**
         * The actual redo action.
         *
         */
        public void redo() {
            super.redo();
            entries.add(entry);
        }

        /**
        * The actual undo action.
        *
        */
        public void undo() {
            super.undo();
            entries.remove(entry);
        }
    }

    /**
     * 
     * @author klasal
     *
     */
    class UndoableCVEntryReplace extends AbstractUndoableEdit {
        private final CVEntry oldEntry;
        private CVEntry newEntry;

        /**
         * 
         * @param oldEntry entry to be replaced
         * @param newEntry new entry
         */
        UndoableCVEntryReplace(CVEntry oldEntry, CVEntry newEntry) {
            this.oldEntry = oldEntry;
            this.newEntry = newEntry;
        }

        /**
         * 
         * @return name of the event
         */
        public String getRepresentationName() {
            return "change Entry";
        }

        /**
         * The actual redo action.
         *
         */
        public void redo() {
            super.redo();

            int index = entries.indexOf(oldEntry);
            entries.remove(index);
            entries.add(index, newEntry);
        }

        /**
        * The actual undo action.
        *
        */
        public void undo() {
            super.undo();

            int index = entries.indexOf(newEntry);
            entries.remove(index);
            entries.add(index, oldEntry);
        }
    }

    /**
     * unspecific change of more than one CVentry
     * @author klasal
     *
     */
    class UndoableCVGlobalChange extends AbstractUndoableEdit {
        private final CVEntry[] oldEntries;
        private String representationName;
        private CVEntry[] newEntries;

        /**
         * 
         * @param entries old entries
         * @param representationName name of the event
         */
        UndoableCVGlobalChange(CVEntry[] entries, String representationName) {
            this.oldEntries = entries;
            this.representationName = representationName;
        }

        /**
         * 
         * @return name of the event
         */
        public String getRepresentationName() {
            return representationName;
        }

        /**
         * The actual redo action.
         *
         */
        public void redo() {
            super.redo();
            entries.clear();

            for (int i = 0; i < newEntries.length; i++) {
                entries.add(newEntries[i]);
            }
        }

        /**
        * The actual undo action.
        *
        */
        public void undo() {
            super.undo();
            newEntries = getEntries();
            entries.clear();

            for (int i = 0; i < oldEntries.length; i++) {
                entries.add(oldEntries[i]);
            }
        }
    }

	public void setACMEditableObject(ACMEditableObject obj) {
		acmEditableObj = obj;
	}
	
	public void removeACMEditableObject() {
		acmEditableObj = null;
	}

}
