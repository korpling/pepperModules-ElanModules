package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MFETable extends JTable {
	private static final long serialVersionUID = -5306413363969435914L;
	protected MFEModel model;

	public MFETable(MFEModel model) {
		super();
		this.model = model;
		// setAutoCreateRowSorter(true);
	}

	public void showCell(int row, int column) {
		Rectangle rect = getCellRect(row, column, true);
		scrollRectToVisible(rect);
		clearSelection();
		setRowSelectionInterval(row, row);
//		getModel().fireTableDataChanged(); // notify the model
	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(234,245,245));
		} else {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		}
		c.setForeground(new Color(0, 0, 0));
		
		int[] selectedRows = getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			if (rowIndex == selectedRows[i]) {
				c.setBackground(new Color(200,215,215));
			}
		}
		return c;
	}
}
