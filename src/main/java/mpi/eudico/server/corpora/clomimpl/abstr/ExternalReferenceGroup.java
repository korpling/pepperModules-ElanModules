package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.ExternalReference;

import java.util.ArrayList;
import java.util.List;


/**
 * A class for grouping a number of external reference objects.  Note:  this
 * class extend ExternalReferenceImpl for reasons of convenience (and it
 * allows that a group can be added to a group).
 *
 * @author Han Sloetjes
 */
public class ExternalReferenceGroup extends ExternalReferenceImpl {
    private List group;

    /**
     * Creates a new instance.
     */
    public ExternalReferenceGroup() {
        super(null, ExternalReference.REFERENCE_GROUP);
    }

    /**
     * Creates a new instance.
     *
     * @param value the value, can be null in case of a group
     */
    public ExternalReferenceGroup(String value) {
        super(value, ExternalReference.REFERENCE_GROUP);
    }

    /**
     * Adds an external reference to the group.
     *
     * @param extRef the ExternalReference to add
     */
    public void addReference(ExternalReference extRef) {
        if (extRef == null) {
            // ignore
            return;
        }

        if (group == null) {
            group = new ArrayList(4);
        }

        group.add(extRef);
    }

    /**
     * Removes the specified external reference from the list.
     *
     * @param extRef the external reference
     *
     * @return true if the reference has been removed and thus the group has
     *         been changed,  false if the group has not been changed
     */
    public boolean removeReference(ExternalReference extRef) {
        if ((extRef == null) || (group == null)) {
            // ignore
            return false;
        }

        return group.remove(extRef);
    }

    /**
     * Returns the list of references.  Note: grants direct access to the list.
     *
     * @return the list of references, can be null
     */
    public List getAllReferences() {
        return group;
    }

    /**
     * Type is always the group type.
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#setReferenceType(int)
     */
    public void setReferenceType(int refType) {
        // ignore
    }

    /**
     * Returns a param. string of all references in the group.
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#paramString()
     */
    public String paramString() {
        if (group != null) {
            StringBuffer buf = new StringBuffer();
            ExternalReference er = null;

            for (int i = 0; i < group.size(); i++) {
                er = (ExternalReference) group.get(i);
                buf.append(i + " - " + er.paramString() + "; ");
            }

            return buf.toString();
        }

        return super.paramString();
    }

    /**
     * Creates a copy (deep clone) of this group.
     *
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        ExternalReferenceGroup groupCopy = new ExternalReferenceGroup();

        if (group != null) {
            for (int i = 0; i < group.size(); i++) {
                Object er = group.get(i);

                if (er instanceof ExternalReference) {
                    groupCopy.addReference((ExternalReference) ((ExternalReference) er).clone());
                }
            }
        }

        return groupCopy;
    }

    /**
     * First check the list, then call super.equals(). The elements in the list
     * must be equal and in the same order. (The order could be ignored?
     *
     * @see mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ExternalReferenceGroup)) {
            return false;
        }

        ExternalReferenceGroup other = (ExternalReferenceGroup) obj;

        if (((other.getAllReferences() == null) && (group != null)) ||
                ((other.getAllReferences() != null) && (group == null))) {
            return false;
        }

        if (group != null) {
            if (group.size() != other.getAllReferences().size()) {
                return false;
            }

            // check the objects, ignore the order?
            Object o1;

            // check the objects, ignore the order?
            Object o2;

            for (int i = 0; i < group.size(); i++) {
                o1 = group.get(i);
                o2 = other.getAllReferences().get(i);

                if ((o1 == null) && (o2 != null)) {
                    return false;
                }

                if ((o1 != null) && (o2 == null)) {
                    return false;
                }

                if ((o1 != null) && !o1.equals(o2)) {
                    return false;
                }
            }
        }

        return super.equals(obj);
    }
}
