package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.util.TableHeaderToolTipAdapter;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Calculates and shows (tier) statistics per transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class TierStatisticsPanel extends AbstractStatisticsPanel
    implements ClientLogger {
    /** formatter for average durartions */
    private DecimalFormat format = new DecimalFormat("#0.######",
            new DecimalFormatSymbols(Locale.US));

    /** formatter for ss.ms values */
    private DecimalFormat format2 = new DecimalFormat("#0.###",
            new DecimalFormatSymbols(Locale.US));

    // columns: tiername, number of annotations, minimum, maximum, 
    // average and median duration, total annotation duration, (total annotation duration as percentage 
    // of media duration,) latency
    private int numCols = 8;

    /**
     * Creates a new TierStatisticsPanel instance
     *
     * @param transcription the transcription
     */
    public TierStatisticsPanel(Transcription transcription) {
        super(transcription);
        initComponents();
    }

    /**
     * Creates a new TierStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param totalDuration total duration
     */
    public TierStatisticsPanel(Transcription transcription,
        long totalDuration) {
        super(transcription, totalDuration);
        numCols++;
        initComponents();
    }

    /**
     * Returns the statistics table.
     *
     * @return the table
     */
    public JTable getStatisticsTable() {
        return statTable;
    }

    /**
     * Initializes ui components and table.
     */
    void initComponents() {
        //Statistics table components
        statPanel = new JPanel();
        statTable = new JTable();
        statTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        //statTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
        statTable.setEnabled(false);
        //Initializing table
        initTable();
        statPane = new JScrollPane(statTable);

        Dimension size = new Dimension(500, 100);
        //	statPane.setMinimumSize(size);
        statPane.setPreferredSize(size);

        updateLocale();

        GridBagConstraints gridBagConstraints;
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        statPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        statPanel.add(statPane, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(statPanel, gridBagConstraints);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    public void updateLocale() {
        statPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "Statistics.Pane.Table")));
    }

    /**
     * Creates contents and headers for the statistics table.
     */
    private void initTable() {
        if (transcription != null) {
            TierImpl tier = null;

            List tierList = transcription.getTiers();
            int numRows = tierList.size();

            String[][] data = new String[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                tier = (TierImpl) tierList.get(i);
                data[i] = getRowForTier(tier);
            }

            String[] headers = new String[numCols];
            headers[0] = ElanLocale.getString("Frame.GridFrame.ColumnTierName");
            headers[1] = ElanLocale.getString("Statistics.NumAnnotations");
            headers[2] = ElanLocale.getString("Statistics.MinimalDuration");
            headers[3] = ElanLocale.getString("Statistics.MaximalDuration");
            headers[4] = ElanLocale.getString("Statistics.AverageDuration");
            headers[5] = ElanLocale.getString("Statistics.MedianDuration");
            headers[6] = ElanLocale.getString("Statistics.TotalDuration");
            headers[7] = ElanLocale.getString("Statistics.Latency");
            
            if (numCols == 9) {
            	headers[7] = ElanLocale.getString("Statistics.TotalDurationPercentage");
            	headers[8] = ElanLocale.getString("Statistics.Latency");
            }

            DefaultTableModel model = new DefaultTableModel(data, headers);
            statTable.setModel(model);
            statTable.getTableHeader().addMouseMotionListener(new TableHeaderToolTipAdapter(statTable.getTableHeader()));
            
            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
            IntStringComparator<String> intComp = new IntStringComparator<String>();
            rowSorter.setComparator(1, intComp);
            
            FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
            for (int i = 2; i < numCols; i++) {
            	rowSorter.setComparator(i, fsComp);
            }
            
            statTable.setRowSorter(rowSorter);
        }
    }

    /**
     * Iterates over all annotations and calculates minimum, maximum, average
     * a,d total duration as well as number of annotations and the latency
     * (which currently is the begin time of the first annotation).
     *
     * @param tier the tier
     *
     * @return a string array, one row of the statistics table
     */
    private String[] getRowForTier(TierImpl tier) {
        String[] row = new String[numCols];
        row[0] = tier.getName();

        List annotations = tier.getAnnotations();
        int numAnn = annotations.size();

        if (numAnn == 0) {
            for (int i = 1; i < numCols; i++) {
                row[i] = EMPTY;
            }

            return row;
        }

        AbstractAnnotation ann = null;
        long minDur = Long.MAX_VALUE;
        long maxDur = 0L;
        long totalDur = 0L;
        long medianDur = 0L;
        List durList = new ArrayList(numAnn);
        long firstOcc = Long.MAX_VALUE;

        long b;
        long e;
        long d;

        for (int i = 0; i < numAnn; i++) {
            ann = (AbstractAnnotation) annotations.get(i);
            b = ann.getBeginTimeBoundary();
            e = ann.getEndTimeBoundary();
            d = e - b;
            durList.add(new Long(d));
            
            if (b < firstOcc) {
                firstOcc = b;
            }

            if (d < minDur) {
                minDur = d;
            }

            if (d > maxDur) {
                maxDur = d;
            }

            totalDur += d;
        }
        
        // calculate median
        Collections.sort(durList);
        int numDurs = durList.size();// should be same as numAnns
        if (numDurs == 1) {
        	medianDur = ((Long) durList.get(0)).longValue();
        } else {
        	if (numDurs % 2 != 0) {
        		// in case of an odd number, take the middle value
        		medianDur = ((Long) durList.get(numDurs / 2)).longValue();
        	} else {
        		// in case of an even number, calculate the average of the 
        		// two middle values
        		long h = ((Long) durList.get(numDurs / 2)).longValue();
        		long l = ((Long) durList.get((numDurs / 2) - 1)).longValue();
        		medianDur = (h + l) / 2;
        	}
        }
        
        row[1] = String.valueOf(numAnn);
        row[2] = format2.format(minDur / (float) 1000);
        row[3] = format2.format(maxDur / (float) 1000);
        row[4] = format.format((totalDur / (float) numAnn) / 1000);
        row[5] = format2.format(medianDur / (float) 1000);
        row[6] = format2.format(totalDur / (float) 1000);
        row[7] = format2.format(firstOcc / (float) 1000);

        if (numCols == 9) {
        	if (totalDuration != 0) {
        		row[7] = format2.format((totalDur / (float) totalDuration) * 100);
        	} else {
        		row[7] = "-";
        	}
        	row[8] = format2.format(firstOcc / (float) 1000);
        }
        return row;
    }

}