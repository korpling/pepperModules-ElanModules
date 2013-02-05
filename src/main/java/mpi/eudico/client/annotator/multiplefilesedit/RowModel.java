package mpi.eudico.client.annotator.multiplefilesedit;

import javax.swing.table.*;
import java.util.*;

public class RowModel {
	private Hashtable editor_data;
	private Hashtable renderer_data;

	public RowModel() {
		editor_data = new Hashtable();
		renderer_data = new Hashtable();
	}

	public void addRendererForRow(int row, TableCellRenderer e) {
		renderer_data.put(new Integer(row), e);
	}

	public void removeRendererForRow(int row) {
		renderer_data.remove(new Integer(row));
	}

	public TableCellRenderer getRenderer(int row) {
		return (TableCellRenderer) renderer_data.get(new Integer(row));
	}
	
	public void addEditorForRow(int row, TableCellEditor e) {
		editor_data.put(new Integer(row), e);
	}

	public void removeEditorForRow(int row) {
		editor_data.remove(new Integer(row));
	}

	public TableCellEditor getEditor(int row) {
		return (TableCellEditor) editor_data.get(new Integer(row));
	}
}
