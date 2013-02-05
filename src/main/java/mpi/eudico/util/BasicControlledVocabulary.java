package mpi.eudico.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * A ControlledVocabulary holds a restricted list of entries.<br>
 * The entries should be unique in the sence that the value of the entries
 * must be unique.  Pending: we are using a List now and take care ourselves
 * that all elements are unique. Could use some kind of Set when we would
 * decide to let CVEntry override the equals() method. Pending: should the
 * entries always be sorted (alphabetically)?  <b>Note:</b> this class is not
 * thread-safe.
 *
 * This class has no undo/redo - functionality!
 *
 * @author Han Sloejes, Alex Klassmann
 * $Id: BasicControlledVocabulary.java 20115 2010-09-29 12:34:59Z wilelb $
 */
public class BasicControlledVocabulary {
    /** constant for the move-to-top edit type */
    public static final int MOVE_TO_TOP = 0;

    /** constant for the move-up edit type */
    public static final int MOVE_UP = 1;

    /** constant for the move-down edit type */
    public static final int MOVE_DOWN = 2;

    /** constant for the move-to-bottom edit type */
    public static final int MOVE_TO_BOTTOM = 3;
    protected List entries;
    private String description;
    private String name;
    protected boolean initMode;

    /**
     * Creates a CV with the specified name and description and the specified description.
     *
     * @param name the name of the CV
     * @param description the description of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *            or of length 0
     */
    public BasicControlledVocabulary(String name, String description) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                "The name can not be null or empty.");
        }

        this.name = name;
        this.description = description;

        entries = new ArrayList();
    }

    /**
     * Creates a CV with the specified name and empty description.
     *
     * @param name the name of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *         or of length 0
     */
    public BasicControlledVocabulary(String name) {
        this(name, "");
    }

    /**
     * Sets the description of this CV.
     *
     * @param description the new description of the CV
     */
    public void setDescription(String description) {
        this.description = description;

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Returns the description of the CV.
     *
     * @return the description of the CV, can be <code>null</code>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns an array containing all entries in this Vocabulary.
     *
     * @return an array of entries
     */
    public CVEntry[] getEntries() {
        return (CVEntry[]) entries.toArray(new CVEntry[] {  });
    }

    /**
     * Returns a sorted array of entries. The values are sorted  using the
     * String.compareTo(String) method.
     *
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByAlphabet() {
        CVEntry[] allEntries = getEntries();
        Arrays.sort(allEntries);
        
        entries.clear();
        for(int i=0; i< allEntries.length; i++){
        	entries.add(allEntries[i]);        	
        }
        return allEntries;
    }
    
    /**
     * Returns a array of reverse sorted entries. The values are sorted using the
     * String.compareTo(String) method.
     *
     * @return a sorted array of CVEntry objects
     */
    public CVEntry[] getEntriesSortedByReverseAlphabetOrder() {
        CVEntry[] allEntries = getEntries();
        Arrays.sort(allEntries,  Collections.reverseOrder());
        
        entries.clear();
        for(int i=0; i< allEntries.length; i++){
        	entries.add(allEntries[i]);        	
        }
        return allEntries;
    }

    /**
     * Returns an array containing the values (Strings) of the entries. This is
     * convenience method to get a view on the entry values in the CV.
     *
     * @return an array of Strings containing the vallues in this CV
     */
    public String[] getEntryValues() {
        String[] values = new String[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            values[i] = ((CVEntry) entries.get(i)).getValue();
        }

        return values;
    }

    /**
     * Returns the CVEntry with the specified value, if present.
     *
     * @param value the value of the entry
     *
     * @return the CVEntry with the specified value, or null
     */
    public CVEntry getEntryWithValue(String value) {
        CVEntry entry = null;

        if (value == null) {
            return entry;
        }

        for (int i = 0; i < entries.size(); i++) {
            //ignore case ?
            if (((CVEntry) entries.get(i)).getValue().equals(value)) {
                entry = (CVEntry) entries.get(i);

                break;
            }
        }

        return entry;
    }

    /**
     * @param initMode if true, don't call handleModified
     */
    public void setInitMode(boolean initMode) {
        this.initMode = initMode;
    }

    /**
     * Sets the name of this CV.
     *
     * @param name the new name of the CV
     *
     * throws IllegalArgumentException when the name is <code>null</code>
     *            or of length 0
     */
    public void setName(String name) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException(
                "The name can not be null or empty.");
        }

        this.name = name;

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Returns the name of the CV.
     *
     * @return the name of this CV
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an array containing the values (Strings) of the entries, ordered alphabetically.<br>
     * This is convenience method to get an ordered view on the entry values
     * in the CV.
     *
     * @return an sorted array of Strings containing the vallues in this CV
     */
    public String[] getValuesSortedByAlphabet() {
        String[] values = getEntryValues();
        Arrays.sort(values);

        return values;
    }

    /**
     * A shorthand for adding more than one CVEntry at a time.
     *
     * @param entries an array of entries
     */
    public void addAll(CVEntry[] entries) {
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                addEntry(entries[i]);
            }
        }

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Adds a new CVEntry to the List.
     *
     * @param entry the new entry
     *
     * @return true if the entry was successfully added, false otherwise
     */
    public boolean addEntry(CVEntry entry) {
        if (entry == null) {
            return false;
        }

        Iterator it = entries.iterator();

        while (it.hasNext()) {
            if (((CVEntry) it.next()).getValue().equals(entry.getValue())) {
                return false;
            }
        }

        entries.add(entry);

        if (!initMode) {
            handleModified();
        }

        return true;
    }

    /**
     * Removes all entries from this ControlledVocabulary.
     */
    public void clear() {
        entries.clear();

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * Checks whether the specified CVEntry is in this CV.<br>
     * <b>Note:</b> This only checks for object equality.
     *
     * @param entry the CVEntry
     *
     * @return true if entry is in the CV, false otherwise
     *
     * @see #containsValue(String)
     */
    public boolean contains(CVEntry entry) {
        if (entry == null) {
            return false;
        }

        return entries.contains(entry);
    }

    /**
     * Checks whether there is a CVEntry with the specified value in this
     * CV.<br>
     *
     * @param value the value
     *
     * @return true if there is an entry with this value in the CV, false
     *         otherwise
     *
     * @see #contains(CVEntry)
     */
    public boolean containsValue(String value) {
        if (value == null) {
            return false;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (((CVEntry) entries.get(i)).getValue().equals(value)) { //ignore case??

                return true;
            }
        }

        return false;
    }

    /**
     * Overrides <code>Object</code>'s equals method by checking all  fields of
     * the other object to be equal to all fields in this  object.
     *
     * @param obj the reference object with which to compare
     *
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            // null is never equal
            return false;
        }

        if (obj == this) {
            // same object reference 
            return true;
        }

        if (!(obj instanceof BasicControlledVocabulary)) {
            // it should be a MediaDescriptor object
            return false;
        }

        // check the fields
        BasicControlledVocabulary other = (BasicControlledVocabulary) obj;

        if (!name.equals(other.getName())) {
            return false;
        }

        if ((description != null) &&
                !description.equals(other.getDescription())) {
            return false;
        }

        if ((other.getDescription() != null) &&
                !other.getDescription().equals(description)) {
            return false;
        }

        // compare cventries, ignoring the order in the list
        boolean entriesEqual = true;

        CVEntry entry;
        CVEntry[] otherEntries = other.getEntries();
loop: 
        for (int i = 0; i < entries.size(); i++) {
            entry = (CVEntry) entries.get(i);

            for (int j = 0; j < otherEntries.length; j++) {
                if (entry.equals(otherEntries[j])) {
                    continue loop;
                }
            }

            // if we get here the cv entries are unequal
            entriesEqual = false;

            break;
        }

        return entriesEqual;
    }

    /**
     * return arbitrary fix number since class is mutable
     * @return hashCode
     */
    public int hashCode() {
        return 1;
    }

    /**
     * This is a checked way to change the value of an existing CVEntry.<br>
     * This method (silently) does nothing when the specified entry is not  in
     * this ControlledVocabulary, or when the value already exists in this
     * CV.
     *
     * @param entry the CVEntry
     * @param value the new value for the entry
     */
    public void modifyEntryValue(CVEntry entry, String value) {
        if ((entry == null) || (value == null)) {
            return;
        }

        if (!entries.contains(entry)) {
            return;
        }

        if (containsValue(value)) {
            return;
        }

        // the entry is in the list and the new value is not, 
        // replace the oldEntry with a new one
        CVEntry newEntry = new CVEntry(value, entry.getDescription());
        int index = entries.indexOf(entry);
        entries.set(index, newEntry);

        if (!initMode) {
            handleModified();
        }
    }

    /**
         * Moves a set of entries up or down in the list of entries of the Vocabulary.
         *
         * @param entryArray the entries to move
     * @param moveType the type of move action, one of MOVE_TO_TOP, MOVE_UP,
     *        MOVE_DOWN or MOVE_TO_BOTTOM
     */
    public void moveEntries(CVEntry[] entryArray, int moveType) {
        switch (moveType) {
        case MOVE_TO_TOP:
            moveToTop(entryArray);

            break;

        case MOVE_UP:
            moveUp(entryArray);

            break;

        case MOVE_DOWN:
            moveDown(entryArray);

            break;

        case MOVE_TO_BOTTOM:
            moveToBottom(entryArray);

            break;

        default:
            break;
        }
    }

    /**
     * Removes a set of entries from the Vocabulary.
     *
     * @param entryArray the entries to remove
     * @return true if action was completed successfully
     */
    public boolean removeEntries(CVEntry[] entryArray) {
        if (entryArray == null) {
            return false;
        }

        boolean removed = false;

        for (int i = 0; i < entryArray.length; i++) {
            boolean b = entries.remove(entryArray[i]);

            if (b) {
                removed = true;
            }
        }

        if (removed) {
            if (!initMode) {
                handleModified();
            }
        }

        return removed;
    }

    /**
     * Removes an entry from the Vocabulary.
     *
     * @param entry the entry to remove
     * @return true if action was completed successfully
     */
    public boolean removeEntry(CVEntry entry) {
        boolean b = entries.remove(entry);

        if (b && !initMode) {
            handleModified();
        }

        return b;
    }

    /**
     * Removes the CVEntry with the specified value from the CV, if present.
     *
     * @param value the value to remove
     *
     */
    public void removeValue(String value) {
        if (value == null) {
            return;
        }

        CVEntry en;
        boolean removed = false;

        for (int i = 0; i < entries.size(); i++) {
            en = (CVEntry) entries.get(i);

            if (en.getValue().equals(value)) { //ignore case ??
                removed = entries.remove(en);

                break;
            }
        }

        if (removed) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Removes all existing CVEntries and adds the specified new entries.
     *
     * @param newEntries the new entries for the CV
     */
    public void replaceAll(CVEntry[] newEntries) {
        if (newEntries == null) {
            return;
        }

        entries.clear();

        addAll(newEntries);

        if (!initMode) {
            handleModified();
        }
    }

    /**
     * replace an entry with another
     * @param oldEntry the entry to be replaced
     * @param newEntry replacement
     * @return true if action was completed successfully
     */
    public boolean replaceEntry(CVEntry oldEntry, CVEntry newEntry) {
        if ((oldEntry == null) || (newEntry == null)) {
            return false;
        }

        int index = entries.indexOf(oldEntry);

        if (index == -1) {
            return false;
        }

        entries.remove(oldEntry);
        entries.add(index, newEntry);

        if (!initMode) {
            handleModified();
        }

        return true;
    }

    /**
     * Override Object's toString method to return the name of the CV.
     *
     * @return the name of the CV
     */
    public String toString() {
        return name;
    }

    /**
     * Sends a general notification to an interested Object, that this CV has been changed.<br>
     * This method does not specify the kind of modification.
     */
    protected void handleModified() {
    }

    /**
     * Moves the CVEntries in the array to the top of the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    protected void moveToTop(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];

            boolean removed = entries.remove(entry);

            if (removed) {
                moved = true;
                entries.add(i, entry);
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array down one position in the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveDown(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;
        int curIndex;

        for (int i = entryArray.length - 1; i >= 0; i--) {
            entry = entryArray[i];
            curIndex = entries.indexOf(entry);

            if ((curIndex >= 0) && (curIndex < (entries.size() - 1))) {
                boolean removed = entries.remove(entry);

                if (removed) {
                    moved = true;
                    entries.add(curIndex + 1, entry);
                }
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array to the bottom of the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveToBottom(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];

            boolean removed = entries.remove(entry);

            if (removed) {
                moved = true;
                entries.add(entry);
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }

    /**
     * Moves the CVEntries in the array up one position in the list.<br>
     * It is assumed that the entries come in ascending order!
     *
     * @param entryArray the array of CVEntry objects
     */
    private void moveUp(CVEntry[] entryArray) {
        if ((entryArray == null) || (entryArray.length == 0)) {
            return;
        }

        CVEntry entry = null;
        boolean moved = false;
        int curIndex;

        for (int i = 0; i < entryArray.length; i++) {
            entry = entryArray[i];
            curIndex = entries.indexOf(entry);

            if (curIndex > 0) {
                boolean removed = entries.remove(entry);

                if (removed) {
                    moved = true;
                    entries.add(curIndex - 1, entry);
                }
            }
        }

        if (moved) {
            if (!initMode) {
                handleModified();
            }
        }
    }
}
