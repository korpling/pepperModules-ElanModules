package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


/**
 * A table model for tab delimited text export, where there is a separate
 * column  per tier and where annotations with the same begin and end time
 * share the same  row, regardless of whether or not there is any relation
 * between the tiers.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TabExportTableModel implements TableModel {
    /** Holds value of property DOCUMENT ME! */
    public final String BT = "begin";

    /** Holds value of property DOCUMENT ME! */
    public final String ET = "end";
    
    public final String DES = ElanLocale.getString("EditCVDialog.Label.CVDescription");
    private List columnIds;
    private List rows;
    private String fileName = null;
    private String absoluteFilePath = null;

    /**
     * Creates and fills the table model, annotations with the same begin and
     * end time  are added to the same row.  NB no null checking is performed
     *
     * @param annotations the annotations
     * @param tierNames the tier names, for the columns
     */
    public TabExportTableModel(List annotations, HashMap<String, HashMap<String, String>> cvMap, String[] tierNames) {
        columnIds = new ArrayList(tierNames.length + 3);
        rows = new ArrayList(annotations.size());

        columnIds.add(BT);
        columnIds.add(ET);

        for (int i = 0; i < tierNames.length; i++) {
            columnIds.add(tierNames[i]);
        }
        if(cvMap != null){
        	columnIds.add(DES);
        }
        fillRows(annotations, cvMap);
        
    }

    /**
     * Returns the file name.
     * 
     * @return the file name
     */
    public String getFileName() {
		return fileName;
	}

    /**
     * Sets the file name.
     * 
     * @param fileName the name of the file the annotations stems from
     */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
     * Returns the  file path.
     * 
     * @return the file path
     */
    public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

    /**
     * Sets the absolute file path.
     * 
     * @param filePath the absolute path of the file the annotations stems from
     */
	public void setAbsoluteFilePath(String filePath) {
		this.absoluteFilePath = filePath;
	}


	/**
     * Adds the annotations to the proper table cell, based on tier name and
     * begin and end time. It is assumed that the annotations are sorted in
     * ascending order, so that an annotations  is either added to a new row,
     * or to the row of the previous annotation.
     *
     * @param annotations the sorted annotations
     */
    private void fillRows(List annotations, HashMap<String, HashMap<String, String>> cvMap) {
        long bt;
        long lastBT = -1;
        long et;
        long lastET = -1;
        Annotation ann;
        String cvName;
        String description = null;
        HashMap <String, String> map;
       
        for (int i = 0; i < annotations.size(); i++) {
            ann = (Annotation) annotations.get(i);
            bt = ann.getBeginTimeBoundary();
            et = ann.getEndTimeBoundary();
            if(cvMap != null){
            	cvName = ((TierImpl)ann.getTier()).getLinguisticType().getControlledVocabylaryName();
            	if(cvName != null){
            		map = cvMap.get(cvName);
            		description = map.get(ann.getValue());
            	}
            }
            
            if ((bt != lastBT) || (et != lastET)) {
                ArrayList row = new ArrayList(columnIds.size());

                for (int j = 0; j < columnIds.size(); j++) {
                    row.add(j, null);
                }

                row.set(0, new Long(bt));
                row.set(1, new Long(et));

                int col = findColumn(ann.getTier().getName());

                if (col > -1) {
                    row.set(col, ann.getValue());
                }
                
                col = findColumn(DES);
                if (col > -1 && description != null) {
                    row.set(col, description);
                }

                rows.add(row);
            } else {
                // add to the previous row, if the sorting is correct
                int col = findColumn(ann.getTier().getName());

                if (col > -1) {
                    setValueAt(ann.getValue(), rows.size() - 1, col);
                }
                
                col = findColumn(DES);
                if (col > -1 && description != null) {
                    List row = (List) rows.get(rows.size() - 1);
                    row.set(col, description);
                }
                
                // just checking
                //System.out.println("Correct row: " + (((Long)getValueAt(rows.size() - 1, 0)).longValue() == 
                //    ann.getBeginTimeBoundary()) + " - " + (((Long)getValueAt(rows.size() - 1, 1)).longValue() == 
                //        ann.getEndTimeBoundary()));
            }

            lastBT = bt;
            lastET = et;
        }
    }

    /**
     * Returns the column index of the column with the specified id.
     *
     * @param columnId the id
     *
     * @return the column index
     */
    public int findColumn(String columnId) {
        return columnIds.indexOf(columnId);
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return columnIds.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + columnIndex +
                " < 0 or > " + (columnIds.size() - 1));
        }

        return (String) columnIds.get(columnIndex);
    }

    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + columnIndex +
                " < 0 or > " + (columnIds.size() - 1));
        }

        if ((columnIndex == 0) || (columnIndex == 1)) {
            return Long.class;
        }

        return String.class;
    }

    /**
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + columnIndex +
                " < 0 or > " + (columnIds.size() - 1));
        }

        if ((rowIndex < 0) || (rowIndex >= rows.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + rowIndex +
                " < 0 or > " + (rows.size() - 1));
        }

        List row = (List) rows.get(rowIndex);

        return row.get(columnIndex);
    }

    /**
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + columnIndex +
                " < 0 or > " + (columnIds.size() - 1));
        }

        if ((rowIndex < 0) || (rowIndex >= rows.size())) {
            throw new ArrayIndexOutOfBoundsException("Index " + rowIndex +
                " < 0 or > " + (rows.size() - 1));
        }

        List row = (List) rows.get(rowIndex);
        row.set(columnIndex, aValue);
    }

    /**
     * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
     */
    public void addTableModelListener(TableModelListener l) {
    }

    /**
     * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
     */
    public void removeTableModelListener(TableModelListener l) {
    }
}
