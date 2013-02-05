package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.Property;


/**
 * Simple implementation of a property class. 
 * This class stores a property that <strong>can</strong> have a name
 * and <strong>can</strong> have a value,.
 * 
 * @author Han Sloetjes, MPI
 */
public class PropertyImpl implements Property {
    private String name;
    private Object value;

    /**
     * @see mpi.eudico.server.corpora.clom.Property#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.Property#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.Property#getValue()
     */
    public Object getValue() {
        return value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.Property#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the name and value separated by a colon if both are not null. If
     * name is null value.toString is returned, if value is null the name is
     * returned. If both are null an empty String is returned.
     *
     * @return a string representation of the property
     */
    public String toString() {
        if (name == null) {
            if (value == null) {
                return "";
            } else {
                return value.toString();
            }
        } else if (value == null) {
            return name;
        }

        return name + ": " + value.toString();
    }
}
