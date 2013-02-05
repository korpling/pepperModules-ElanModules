package mpi.eudico.client.annotator.linkedmedia;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


/**
 * An abstract TableModel for a non editable table displaying information
 * about linked files.
 *
 * @author Han Sloetjes
 */
public abstract class LinkedFilesTableModel extends AbstractTableModel {
    /** table column and label identifiers */
    public static final String LABEL_PREF = "LinkedFilesDialog.Label.";

    /** name of the file */
    public static final String NAME = "MediaName";

    /** url of the file */
    public static final String URL = "MediaURL";

    /** mime type of the file */
    public static final String MIME_TYPE = "MimeType";

    /** extracted from field for audio files */
    public static final String EXTRACTED_FROM = "ExtractedFrom";

    /** the offset or time origin */
    public static final String OFFSET = "MediaOffset";

    /** the master media field */
    public static final String MASTER_MEDIA = "MasterMedia";

    /** the status, linked or missing */
    public static final String LINK_STATUS = "LinkStatus";

    /** the missing status */
    public static final String MISSING = "StatusMissing";

    /** the linked status */
    public static final String LINKED = "StatusLinked";
    
    /** the associated with field  */
	public static final String ASSOCIATED_WITH = "AssociatedWith";

    /** not applicable string */
    public static final String N_A = "-";

    /** a list of column id's */
    List columnIds;

    /** a list for the data of the model */
    List data;

    /** a list of column class types */
    List types;

    /**
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    public int getColumnCount() {
        return columnIds.size();
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        return data.size();
    }

    /**
     * Returns the value at the given row and column. Note: returns null
     * instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row
     * @param columnIndex the column
     *
     * @return the value at the given row and column
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= data.size()) || (columnIndex < 0) ||
                (columnIndex >= columnIds.size())) {
            return null;
        }

        ArrayList row = (ArrayList) data.get(rowIndex);

        return row.get(columnIndex);
    }

    /**
     * Returns false regardless of parameter values. The values are not  to be
     * edited directly in the table.
     *
     * @param row the row
     * @param column the column
     *
     * @return false
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * Returns the class of the data in the specified column. Note: returns
     * null instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param columnIndex the column
     *
     * @return the <code>class</code> of the objects in column
     *         <code>columnIndex</code>
     */
    public Class getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= types.size())) {
            return null;
        }

        return (Class) types.get(columnIndex);
    }

    /**
     * Returns the (internal) identifier of the column. Note: returns null
     * instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param columnIndex the column
     *
     * @return the id of the column or null
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return null;
        }

        return (String) columnIds.get(columnIndex);
    }
}
