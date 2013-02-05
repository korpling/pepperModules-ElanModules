package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.MetaTime;
import mpi.eudico.server.corpora.clom.Tag;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;


/**
 * MetaTime encapsulates the ordering of Tags (of multiple Tiers) in a
 * Transcription. It is considered to be part of the Transcription.  The
 * MetaTime is used when comparing Tags in the Tag's compareTo method. Given a
 * constructed MetaTime, it is then sufficient to add Tags to a TreeSet, they
 * will be ordered according to the MetaTime automatically.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 16-Apr-1999
 */
public class FastMetaTime implements MetaTime {
    private Vector orderedTagList;
    private ListIterator listIter;

    /**
     * Creates a new FastMetaTime instance
     */
    public FastMetaTime() {
        orderedTagList = new Vector();
        listIter = orderedTagList.listIterator();
    }

    /**
     * Adds a Tag to the MetaTime at currentTag. This method may only be called
     * from Tier.addTag
     *
     * @param theTag the Tag to be inserted.
     */
    public void insertTag(Tag theTag) {
        // update tag indices from current tag up
        int nextTagIndex = listIter.nextIndex();

        Iterator iter = orderedTagList.iterator();
        int i = 0;

        while (iter.hasNext()) {
            Tag t = (Tag) iter.next();
            i = t.getIndex();

            if (i >= nextTagIndex) {
                t.setIndex(i + 1);
            }
        }

        theTag.setIndex(nextTagIndex);
        listIter.add(theTag);
    }

    /**
     * A utility method to print the current state of MetaTime to standard
     * output.
     */
    public void printMetaTime() {
        System.out.println("");

        Iterator iter = orderedTagList.iterator();

        while (iter.hasNext()) {
            Tag t = (Tag) iter.next();
            System.out.println(t.getIndex() + " " + t.getBeginTime() + " " +
                +t.getEndTime() + " " + t.getValues());
        }
    }

    /**
     * Returns true if tag1 starts before tag2, according to the order
     * specified by the MetaTime. Each Tag can be either time-aligned or  not
     * time-aligned.
     *
     * @param tag1 first tag to be compared.
     * @param tag2 second tag to be compared.
     *
     * @return true if tag1 starts before tag2.
     */
    public boolean startsBefore(Tag tag1, Tag tag2) {
        boolean before = true;

        if (tag1.getIndex() > tag2.getIndex()) {
            before = false;
        } else {
            before = true;
        }

        return before;
    }

    /**
     * Returns number of elements of MetaTime.
     *
     * @return DOCUMENT ME!
     */
    public int size() {
        return orderedTagList.size();
    }

    public void add(Object o) {
        listIter.add(o);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasNext() {
        return listIter.hasNext();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasPrevious() {
        return listIter.hasPrevious();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object next() {
        return listIter.next();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int nextIndex() {
        return listIter.nextIndex();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object previous() {
        return listIter.previous();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int previousIndex() {
        return listIter.previousIndex();
    }

    /**
     * DOCUMENT ME!
     */
    public void remove() {
        listIter.remove();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     */
    public void set(Object o) {
        listIter.set(o);
    }
}
