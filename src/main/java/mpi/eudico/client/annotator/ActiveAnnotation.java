package mpi.eudico.client.annotator;

import mpi.eudico.server.corpora.clom.Annotation;

import java.util.Vector;


/**
 * Administrates the current ActiveAnnotation.
 */
public class ActiveAnnotation {
    private Vector listeners;
    private Annotation annotation;

    /**
     * Creates an empty Cursor.
     */
    public ActiveAnnotation() {
        listeners = new Vector();
        annotation = null;
    }

    /**
     * Sets the Annotation
     *
     * @param annotation DOCUMENT ME!
     */
    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;

        // Tell all the interested ActiveAnnotation about the change
        notifyListeners();
    }

    /**
     * Gets the Annotation
     *
     * @return DOCUMENT ME!
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Tell all ActiveAnnotationListeners about a change in the
     * ActiveAnnotation
     */
    public void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            ((ActiveAnnotationListener) listeners.elementAt(i)).updateActiveAnnotation();
        }
    }

    /**
     * Add a listener for ActiveAnnotation events.
     *
     * @param listener the listener that wants to be notified for
     *        ActiveAnnotation events.
     */
    public void addActiveAnnotationListener(ActiveAnnotationListener listener) {
        listeners.add(listener);
        listener.updateActiveAnnotation();
    }

    /**
     * Remove a listener for ActiveAnnotation events.
     *
     * @param listener the listener that no longer wants to be notified for
     *        ActiveAnnotation events.
     */
    public void removeActiveAnnotationListener(
        ActiveAnnotationListener listener) {
        listeners.remove(listener);
    }
}
