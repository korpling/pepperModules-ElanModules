package mpi.eudico.client.annotator.tier;

import java.awt.event.ActionListener;

import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.util.SelectEnableObject;


/**
 * A simple table model that denotes cells with a Boolean value as editable.
 *
 * @author Han Sloetjes
 */
public class TierExportTableModel extends DefaultTableModel {
    private ActionListener actionListener = null;

	/**
     * Returns true for the Boolean columns, false for tall other columns.
     *
     * @param row the row
     * @param column the column
     *
     * @return true if the value is of type Boolean, false otherwise
     *
     * @see #getValueAt
     */
    public boolean isCellEditable(int row, int column) {
    	
    	if(getValueAt(row, column) instanceof SelectEnableObject){
    		return true;
    	}
    	
    	return (getValueAt(row, column) instanceof Boolean);
    }
    
    public void addActionListener(ActionListener a){
		actionListener  = a;
	}
	
	public void setValueAt( Object object, int row, int column ){
		super.setValueAt(object, row, column);
		if( actionListener != null )
			actionListener.actionPerformed(null);
	}
}
