package mpi.eudico.util;

import java.awt.Color;
import java.io.Serializable;


/**
 * $Id: CVEntry.java 20115 2010-09-29 12:34:59Z wilelb $
 * An entry in a ContolledVocabulary.<br>
 * An entry has a value and an optional description.  Pending: the entries'
 * value in a controlled vocabulary should be unique. We could override the
 * equals(o) method of <code>Object</code> to return
 * this.value.equals(((CVEntry)o).getValue()). This however would not be
 * consistent with hashCode().
 *
 */
public class CVEntry implements Comparable, Serializable {
    private String value;
    private String description;
    /** field for reference to an external concept or entity, like a Data Category */
    private Object externalRef;
    private int shortcutKeyCode = -1;
    private Color prefColor;

    public void setValue(String s){
    		value = s;
    }
    
    public CVEntry(){
    }
    
    /**
     * Creates a new entry with the specified value.
     *
     * @param value the value
     *
     * @see #CVEntry(String,String)
     */
    public CVEntry(String value) {
        this(value, null);
    }

    /**
     * Creates a new entry with the specified value and the specified
     * description.
     *
     * @param value the value
     * @param description the description
     *
     */
    public CVEntry(String value, String description) {
        if (value == null) {
            throw new IllegalArgumentException("The value can not be null.");
        }

        this.value = value;
        this.description = description;
    }
    
    /**
     * Creates a copy of the specified entry.
     * 
     * @param origEntry the entry to copy
     */
    public CVEntry(CVEntry origEntry){
        if (origEntry == null) {
            throw new IllegalArgumentException("The CVEntry can not be null.");
        }
        value = origEntry.getValue();
        description = origEntry.getDescription();
        externalRef = origEntry.getExternalRef();
        prefColor = origEntry.getPrefColor();
        shortcutKeyCode = origEntry.getShortcutKeyCode();
	}

    /**
     * Sets the description of this entry.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description.
     *
     * @return the description or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the reference to an externally defined concept or entity.
     * 
	 * @return the externalRef the reference to an external concept or entity
	 */
	public Object getExternalRef() {
		return externalRef;
	}

	/**
	 * Sets the reference to an externally defined concept or entity.
	 * 
	 * @param externalRef the reference to an external concept or entity
	 */
	public void setExternalRef(Object externalRef) {
		this.externalRef = externalRef;
	}

	/**
	 * Returns the shortcut key to use to select this entry value.
	 * 
	 * @return the shortcut key code
	 */
	public int getShortcutKeyCode() {
		return shortcutKeyCode;
	}

	/**
	 * Sets the shortcut key to use to select this entry value.
	 * 
	 * @param shortcutKeyCode the new key code
	 */
	public void setShortcutKeyCode(int shortcutKeyCode) {
		this.shortcutKeyCode = shortcutKeyCode;
	}

	/**
	 * Returns the preferred color for display in viewer components.
	 * 
	 * @return the preferred color, can be null
	 */
	public Color getPrefColor() {
		return prefColor;
	}

	/**
	 * Sets the preferred color for this entry.
	 * 
	 * @param prefColor the preferred color
	 */
	public void setPrefColor(Color prefColor) {
		this.prefColor = prefColor;
	}

	/**
     * implementation of the comparable interface.
     *
     * @param o the object this class is compared to
     * @return compareTo of 'value's, or, if they are equal, compareTo of 'description's
     */
    public int compareTo(Object o) {
        CVEntry other = (CVEntry) o;

        int compare = this.getValue().compareTo(other.getValue());

        if (compare == 0) {
            compare = this.getDescription().compareTo(other.getDescription());
        }

        return compare;
    }

    /**
     * Overrides <code>Object</code>'s equals method by checking if value and
     * description of the two objects are equal.
     *
     * Note, that also subclasses of this class might be equal to this class!!!
     *
     * @param obj the reference object with which to compare
     *
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof CVEntry)) {
            return false;
        }

        // check the fields
        CVEntry other = (CVEntry) obj;

        if (value.equals(other.getValue())) {
            if (description == null) {
            	if (other.getDescription() != null) {
            		return false;
            	}               
            } else if (!description.equals(other.getDescription())) {
            		return false;
            }

            if (externalRef == null) {
            	if (other.getExternalRef() != null) {
            		return false;
            	}
            } else if (!externalRef.equals(other.getExternalRef())) {
            	return false;
            }

            return true;
        }

        return false;
    }

    /**
     * returns hashCode of 'value'.
     *
     * (note that it is not necessary to return different hashcodes if objects are not equal;
     * including the field 'description' would cause problems since it is mutable)
     *
     * @return hashCode
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Overrides <code>Object</code>'s toString() method to just return  the
     * value of this entry.<br>
     * This way this object can easily be used directly in Lists, ComboBoxes
     * etc.
     *
     * @return the value
     */
    public String toString() {
        return value;
    }
}
