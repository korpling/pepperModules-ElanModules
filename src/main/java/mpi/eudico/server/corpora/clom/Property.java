package mpi.eudico.server.corpora.clom;

/**
 * Defines that a property that has a name and a value.
 *
 * @author Han Sloetjes, MPI
 */
public interface Property {
    /**
     * Returns the name of the property.
     *
     * @return the name of the property, can be <code>null</code>
     */
    public String getName();

    /**
     * Sets the name of the property.
     *
     * @param name the name of the property
     */
    public void setName(String name);

    /**
     * Returns the value of the property.
     *
     * @return the value of the property, can be <code>null</code>
     */
    public Object getValue();

    /**
     * Sets the value of the property, typically a String.
     *
     * @param value the value of the property
     */
    public void setValue(Object value);
}
