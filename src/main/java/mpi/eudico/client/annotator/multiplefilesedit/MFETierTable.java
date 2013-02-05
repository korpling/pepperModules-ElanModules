package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.ElanLocale;

public class MFETierTable extends MFETable {
	private static final long serialVersionUID = -3438987131220519500L;
	private RowModel rm;
	
	public MFETierTable(MFEModel model) {
		super(model);
		setModel(new TableByTierModel(model));
		this.setRowEditorModel(new RowModel());
	}
	

	
	public void initCombobox() {
		int row_count = getModel().getRowCount();
		for(int i=0;i<row_count;i++) {
			String[] linguistic_types = model.getLinguisticTypeNamesByTier(i);
			this.rm.addEditorForRow(i, new MyComboBoxEditor(linguistic_types));
			this.rm.addRendererForRow(i, new MyComboBoxRenderer(linguistic_types));
		}
		repaint();
	}
	
	public void newRow(int new_row) {
		String[] linguistic_types = model.getLinguisticTypeNamesByTier(new_row);
		this.rm.addEditorForRow(new_row, new MyComboBoxEditor(linguistic_types));
		this.rm.addRendererForRow(new_row, new MyComboBoxRenderer(linguistic_types));
	}
	
	public void setRowEditorModel(RowModel rm)
	{
		this.rm = rm;
	}

	public RowModel getRowEditorModel()
	{
		return rm;
	}

	public TableCellEditor getCellEditor(int row, int col) {
		if (col == MFEModel.TIER_TYPECOLUMN) {
			TableCellEditor tmpEditor = null;
			if (rm != null)
				tmpEditor = rm.getEditor(row);
			if (tmpEditor != null)
				return tmpEditor;
		}
		return super.getCellEditor(row, col);
	}
	
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == MFEModel.TIER_TYPECOLUMN) {
			TableCellRenderer tmpRenderer = null;
			if (rm != null)
				tmpRenderer = rm.getRenderer(row);
			if (tmpRenderer != null)
				return tmpRenderer;
		}
		return super.getCellRenderer(row, col);
	}
	
	/**
	 * Custom cell renderers to render a JComboBox inside a table cell
	 */
	private class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
		private static final long serialVersionUID = 7619116878007010125L;

		public MyComboBoxRenderer(String[] items) {
            super(items);
        }
    
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            if(!model.isTypeConsistentTier(row)) {
            	setSelectedItem(ElanLocale.getString("MFE.Multiple"));
            	setEnabled(false);
            } else {
            	// Select the current value
            	setSelectedItem(value);
            	setEnabled(true);
            }

            return this;
        }
    }
    
    private class MyComboBoxEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 7602794950936468795L;

		public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }

}
