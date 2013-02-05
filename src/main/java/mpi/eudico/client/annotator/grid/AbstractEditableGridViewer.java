package mpi.eudico.client.annotator.grid;

import java.util.List;

import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * This class makes the GridViewer editable and let him responds to changes in
 * ACM elsewhere
 * @version Aug 2005 Identity removed
 */
abstract public class AbstractEditableGridViewer extends AbstractGridViewer implements
        ACMEditListener {

    protected GridEditor gridEditor;

    /**
     * Constructor
     * 
     */
    public AbstractEditableGridViewer(AnnotationTable table) {
        super(table);
    }

    protected void initTable() {
        super.initTable();
        gridEditor = new GridEditor(this, dataModel);
        table.setDefaultEditor(Object.class, gridEditor);
    }

    /**
     * Update method from ActiveAnnotationUser.
     */
    public void updateActiveAnnotation() {
        if (dataModel.getRowCount() == 0) {
            return;
        }
        repaint();
        if (getActiveAnnotation() != null) {
            doUpdate();
        }
    }

    /**
     * Checks the kind of edit that has happened and updates the table when
     * necessary.
     * 
     * @param e
     *            the ACMEditEvent
     */
    public void ACMEdited(ACMEditEvent e) {
        if (dataModel.getRowCount() == 0) {
            return;
        }
        switch (e.getOperation()) {
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
            repaint();
            break;
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:
            repaint();
            break;
        default:
            repaint();
        }
    }

    protected void updateDataModel(List annotations) {
        gridEditor.cancelCellEditing();
        if (annotations != null)
            dataModel.updateAnnotations(annotations);
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    public void updateLocale() {
        gridEditor.updateLocale();
        super.updateLocale();
    }

    /**
     * 
     */
	public void preferencesChanged() {
		// method stub		
	}
}
