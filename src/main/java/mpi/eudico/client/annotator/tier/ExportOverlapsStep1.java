package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ExportOverlapsStep1 extends StepPane implements ListSelectionListener {
	private ArrayList<String> tierNames;
    protected TierExportTableModel model1;
    protected TierExportTableModel model2;
    protected JTable table1;
    protected JTable table2;
    protected JLabel firstLabel;
    protected JLabel secLabel;
    
    /** column id for the include in export checkbox column, invisible */
    protected final String SELECT_COLUMN = "select";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";
    
    /**
     * Constructor
     * @param multiPane
     */
	public ExportOverlapsStep1(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
		SwingUtilities.invokeLater(new LoadThread());
	}

	protected void initComponents() {
		model1 = new TierExportTableModel();
        model2 = new TierExportTableModel();
        model1.setColumnIdentifiers(new String[] { TIER_NAME_COLUMN });
        model2.setColumnIdentifiers(new String[] { SELECT_COLUMN, TIER_NAME_COLUMN });

        table1 = new JTable(model1);
        table1.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table1.getSelectionModel().addListSelectionListener(this);
        table1.setTableHeader(null);
        table1.getSelectionModel().addListSelectionListener(this);
        
        table2 = new JTable(model2);
        table2.getSelectionModel()
              .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table2.getSelectionModel().addListSelectionListener(this);    
        table2.getColumn(SELECT_COLUMN)
                 .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table2.getColumn(SELECT_COLUMN)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        table2.getColumn(SELECT_COLUMN).setMaxWidth(30);
        table2.setShowVerticalLines(false);
        table2.setTableHeader(null);
        
        model1.addRow(new Object[]{"Loading tiers..."});
        
        Dimension prdim = new Dimension(120, 80);
        JScrollPane p1 = new JScrollPane(table1);
        p1.setPreferredSize(prdim);

        JScrollPane p2 = new JScrollPane(table2);
        p2.setPreferredSize(prdim);

        firstLabel = new JLabel(ElanLocale.getString(
                    "ExportOverlapsDialog.Label.RefTier"));
        secLabel = new JLabel(ElanLocale.getString(
                    "ExportOverlapsDialog.Label.OtherTiers"));

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;

        add(firstLabel, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p1, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        add(secLabel, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p2, gbc);
	}
	
	/**
	 * Extracts all unique tier names from the selected files.
	 */
	private void extractTierNames() {
		ArrayList<File> files = (ArrayList<File>) multiPane.getStepProperty("files");
		tierNames = new ArrayList<String>();
		if (files == null || files.size() == 0) {
			return;
		}
		TranscriptionImpl trans;
		Tier tier;
		String name;
		
		for (File f : files) {
			if (f == null) {
				continue;
			}
			try {
				trans = new TranscriptionImpl(f.getAbsolutePath());
				List tiers = trans.getTiers();
				
				for (int i = 0; i < tiers.size(); i++) {
					tier = (Tier) tiers.get(i);
					name = tier.getName();
					if (!tierNames.contains(name)) {
						tierNames.add(name);
					}
				}
			} catch (Exception ex) {
				// catch any exception, io, parse etc
			}
		}
		Collections.sort(tierNames);
		
		model1.removeRow(0);
		
        for (String tname : tierNames) {
        	model1.addRow(new Object[] { tname });
        	model2.addRow(new Object[] { Boolean.FALSE, tname });
        }
	}
	
	
	/**
	 * Returns the selected tiers.
	 * 
	 * @return a list of selected tiers
	 */
    private List<String> getSelectedTiers2() {
        int includeCol = model2.findColumn(SELECT_COLUMN);
        int nameCol = model2.findColumn(TIER_NAME_COLUMN);

        ArrayList<String> selectedTiers = new ArrayList<String>();

        // add selected tiers in the right order
        for (int i = 0; i < model2.getRowCount(); i++) {
            Boolean include = (Boolean) model2.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                selectedTiers.add((String) model2.getValueAt(i, nameCol));
            }
        }

        return selectedTiers;
    }
	
    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
        return ElanLocale.getString("ExportOverlapsDialog.SelectTiers");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    public boolean leaveStepForward() {
    	int row1 = table1.getSelectedRow();
    	if (row1 < 0) {
    		return false;
    	}
    	String refTier = (String) table1.getValueAt(row1, 0);
    	
    	List<String> selTiers2 = getSelectedTiers2();
    	selTiers2.remove(refTier);
    	// double check?
        if (selTiers2.size() > 0) {
            multiPane.putStepProperty("Tier-1", refTier);

            multiPane.putStepProperty("Tiers-2", selTiers2);

            return true;
        }

        return false;
    }
	
    /**
     * Checks if in both table one tier is selected and that they are not the
     * same.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getSource() == table2.getSelectionModel() && lse.getValueIsAdjusting()) {
            int b = lse.getFirstIndex();
            int e = lse.getLastIndex();
            int col = model2.findColumn(SELECT_COLUMN);

            for (int i = b; i <= e; i++) {
                if (table2.isRowSelected(i)) {
                    model2.setValueAt(Boolean.TRUE, i, col);
                }
            }
        }
    	
    	int row1 = table1.getSelectedRow();
    	
        int includeCol = model2.findColumn(SELECT_COLUMN);

        ArrayList<Integer> selectedTiers = new ArrayList<Integer>();

        // add selected tiers in the right order
        for (int i = 0; i < model2.getRowCount(); i++) {
            Boolean include = (Boolean) model2.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                selectedTiers.add(i);
            }
        }
        int[] rows2 = new int[selectedTiers.size()];
        for(int i = 0; i < selectedTiers.size(); i++) {
        	rows2[i] = selectedTiers.get(i);
        }
    	//int[] rows2 = table2.getSelectedRows();
    	
        if ((row1 > -1) && (rows2.length > 0)) {
        	if (rows2.length == 1 && rows2[0] == row1) {
        		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        	} else {
        		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        	}
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
    
    class LoadThread extends Thread {
    	
    	public void run() {
    		extractTierNames();
    	}
    }
}
