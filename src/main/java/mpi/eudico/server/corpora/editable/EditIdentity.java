package mpi.eudico.server.corpora.editable;

import java.io.Serializable;


/**
 * SHOULD BE IMPLEMENTED IN ../authorization/Identity.java
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 01-12-1999
 */
public class EditIdentity implements Serializable {
    private String name;
    private long number;

    /**
     * Creates a new EditIdentity instance
     *
     * @param name DOCUMENT ME!
     * @param number DOCUMENT ME!
     */
    public EditIdentity(String name, long number) {
        this.name = name;
        this.number = number;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getNumber() {
        return number;
    }

    /**
     * DOCUMENT ME!
     *
     * @param otherIdentity DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(EditIdentity otherIdentity) {
        return ((number == otherIdentity.getNumber()) &&
        name.equals(otherIdentity.getName()));
    }
}
