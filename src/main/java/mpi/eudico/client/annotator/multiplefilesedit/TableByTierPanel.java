package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mpi.eudico.client.annotator.ElanLocale;

public class TableByTierPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = -9116156249200869862L;

	private MFEModel model;
	
	private MFETierTable table;
	private JButton addRowButton;
	private JButton removeRowButton;
	
	public TableByTierPanel(MFEModel model) {
		super();
		this.model = model;
		initComponents();
		initCombobox();
	}

	public void initCombobox() {
		table.initCombobox();
	}
	
	private void initComponents() {
		GridBagLayout lm = new GridBagLayout();
		setLayout(lm);
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight=1;
        c.weightx=1;
        c.weighty=0.9;
		c.fill = GridBagConstraints.BOTH;
		table = new MFETierTable(model);
		JScrollPane scroll_pane = new JScrollPane(table);
		table.getModel().addTableModelListener(new TableListener());
		table.setRowHeight(24);
		//HS reordering false
		table.getTableHeader().setReorderingAllowed(false);
		add(scroll_pane, c);
		
		c.gridwidth = 1;
		c.gridy = 0;
		c.weighty=0;
		addRowButton = new JButton(ElanLocale.getString("MFE.TierTab.AddTier"));
		addRowButton.setActionCommand("addRow");
		addRowButton.addActionListener(this);
		add(addRowButton, c);
		
		c.gridx = 1;
		removeRowButton = new JButton(ElanLocale.getString("MFE.TierTab.RemoveTier"));
		removeRowButton.setActionCommand("removeRow");
		removeRowButton.addActionListener(this);
		add(removeRowButton, c);
		
		enableUI(false);
	}
	
	
	
	public void updateLocale() {
		
	}

	public void enableUI(boolean b) {
		addRowButton.setEnabled(b);
		removeRowButton.setEnabled(b && model.areTiersRemovable());
	}

//	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton)
		{
			if(e.getActionCommand().equals("addRow")) {
				int new_row = ((TableByTierModel)table.getModel()).newRow();
				table.newRow(new_row);
				table.showCell(table.getRowCount()-1, 0);
			} else if (e.getActionCommand().equals("removeRow")) {
				int[] selectedRows=table.getSelectedRows();
//				int[] convertedSelectedRows=new int[selectedRows.length];
//				for(int i=0;i<selectedRows.length;i++) {
//					convertedSelectedRows[i]=table.convertRowIndexToModel(selectedRows[i]);
//				}
//				((TableByTierModel)table.getModel()).removeRows(convertedSelectedRows);
				((TableByTierModel)table.getModel()).removeRows(selectedRows);
			}
		}
	}

	public void rowAdded(int row_nr) {
		((TableByTierModel)table.getModel()).fireTableRowsInserted(row_nr, row_nr);
	}
}
