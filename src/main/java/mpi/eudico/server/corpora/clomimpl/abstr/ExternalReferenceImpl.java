package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.ExternalReference;


/**
 * A class for storing information about references to external resources or
 * externally  defined concepts.
 *
 * @author Han Sloetjes
 */
public class ExternalReferenceImpl implements ExternalReference, Cloneable {
    private String value;
    private int type;

    /**
     * Constructor.
     *
     * @param value the value of the reference
     * @param type the type of reference, one of the constants declared in
     *        <code>ExternalReference</code>
     */
    public ExternalReferenceImpl(String value, int type) {
        super();
        this.value = value;
        // check the type??
        this.type = type;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#getReferenceType()
     */
    public int getReferenceType() {
        return type;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#getValue()
     */
    public String getValue() {
        return value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#setReferenceType(int)
     */
    public void setReferenceType(int refType) {
        type = refType;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @see mpi.eudico.server.corpora.clom.ExternalReference#paramString()
     */
    public String paramString() {
        if (type == ExternalReference.ISO12620_DC_ID) {
            return "DCR: " + value;
        } else if (type == ExternalReference.RESOURCE_URL) {
            return "URL: " + value;
        } else if (type == ExternalReference.EXTERNAL_CV) {
            return "ECV: " + value;
        }

        return value;
    }

    /**
     * Creates a clone of this object.
     *
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        ExternalReferenceImpl cloneER = new ExternalReferenceImpl(value, type);

        return cloneER;
    }

    /**
     * Returns true if the type is equal and the values are equal or both null.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof ExternalReferenceImpl) {
            ExternalReferenceImpl other = (ExternalReferenceImpl) obj;

            if (other.getReferenceType() != type) {
                return false;
            }

            if ((other.getValue() != null) && !other.getValue().equals(value)) {
                return false;
            }

            if ((value != null) && !value.equals(other.getValue())) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
