package mpi.eudico.client.annotator.tier;

import java.awt.event.ActionListener;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Table model to display a table with checkable items
 * @author Jeffrey Lemein
 * @version March, 2011
 */
public class SelectableContentTableModel extends AbstractTableModel {
	private String[] columnNames;
	private Object[][] rowData;
	private JTable tableWithThisModel;
	private ActionListener actionListener;
	
	public SelectableContentTableModel( Set content, JTable table ){
		columnNames = new String[2];
		rowData = new Object[content.size()][2];
		tableWithThisModel = table;
		actionListener = null;
		
		Object[] contentArray = content.toArray();
		
		for( int i =0; i<contentArray.length; i++ ){
			rowData[i][0] = new Boolean(false);
			rowData[i][1] = contentArray[i];
		}
	}
	
	/**
	 * Creates a selectable content table model in which the data for two columns can be specified.
	 * Both arrays of data should be of the same length
	 * @param column1Data data for first column
	 * @param column2Data data for second column
	 * @param table the table to which this table model corresponds
	 */
	public SelectableContentTableModel( JTable table, Object[]... columnData ){
		int nrArguments = columnData.length;
		columnNames = new String[nrArguments+1];
		
		rowData = new Object[columnData[0].length][nrArguments+1];
		tableWithThisModel = table;
		actionListener = null;
		
		for( int i =0; i<columnData[0].length; i++ ){
			rowData[i][0] = new Boolean(false);
			for( int j=0; j<nrArguments; j++ )
				rowData[i][j+1] = columnData[j][i];
		}
	}
	
	public int getRowCount(){
		return rowData.length;			
	}
	
	public int getColumnCount(){
		return columnNames.length;
	}
	
	public Object getValueAt(int row, int column){
		return rowData[row][column];
	}
	
	public void addActionListener(ActionListener a){
		actionListener = a;
	}
	
	public void setValueAt( Object object, int row, int column ){
		if( getColumnCount() > column && getRowCount() > row ){
			rowData[row][column] = object;
		}
		if( actionListener != null )
			actionListener.actionPerformed(null);
	}
	
	public void selectAll(){
		if( getColumnCount() > 0 )
			for( int i = 0; i < getRowCount(); i++ )
				setValueAt( true, i, 0 );
		tableWithThisModel.repaint();
	}
	
	public Vector getSelectedValues(){
		Vector selectedValues = new Vector();
		
		for( int i=0; i<rowData.length; i++ )
			if( (Boolean) rowData[i][0] )
				selectedValues.add(rowData[i][1]);
		
		return selectedValues;
	}
	
	public void selectNone(){
		if( getColumnCount() > 0 )
			for( int i = 0; i < getRowCount(); i++ )
				setValueAt( false, i, 0 );
		tableWithThisModel.repaint();
	}
	
	public boolean nothingSelected(){
		for( int r=0; r<getRowCount(); r++ )
			if( (Boolean)getValueAt(r,0) == true )
				return false;
		
		return true;
	}
	
	public boolean isCellEditable(int row, int col) {
        //only the first column is editable (because it contains the checkboxes)
		return col <= 0;
    }
	
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
    }
}
