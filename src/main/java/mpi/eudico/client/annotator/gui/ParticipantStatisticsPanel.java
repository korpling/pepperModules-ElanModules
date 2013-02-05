package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.util.TableHeaderToolTipAdapter;

import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntStringComparator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * Calculates and shows participant statistics per transcription.
 *
 * @author Mark Blokpoel
 * @version 1.0
 */
public class ParticipantStatisticsPanel extends AbstractStatisticsPanel {
    /** formatter for average durartions */
    private DecimalFormat format = new DecimalFormat("#0.######",
            new DecimalFormatSymbols(Locale.US));

    /** formatter for ss.ms values */
    private DecimalFormat format2 = new DecimalFormat("#0.###",
            new DecimalFormatSymbols(Locale.US));

    /** stores a set of all participants used in the file */
    private Set<String> participants;

    /**
     * Default constructor, invoking superclass
     *
     * @param transcription the transription
     */
    public ParticipantStatisticsPanel(Transcription transcription) {
        super(transcription);
        initComponents();
    }

    /**
     * Creates a new ParticipantStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param duration the media duration
     */
    public ParticipantStatisticsPanel(TranscriptionImpl transcription,
        long duration) {
        super(transcription, duration);
        initComponents();
    }

    /**
     * Returns the table
     *
     * @return the statistics table
     */
    @Override
    public JTable getStatisticsTable() {
        return statTable;
    }

    /**
     * Initializes the table.
     */
    @Override
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
     * This method initializes the table and fills is with statistics.
     */
    private void initTable() {
        if (transcription != null) {
            Vector<TierImpl> tiers = transcription.getTiers();
            participants = new HashSet<String>();

            for (TierImpl tier : tiers)
                participants.add(tier.getParticipant());

            int numRows = participants.size();
            int numCols = 10;

            String[][] data = new String[numRows][numCols];
            String[] headers = {
                    ElanLocale.getString("Statistics.Participant"), //0
                    ElanLocale.getString("Statistics.NumTiers"), //1
                    ElanLocale.getString("Statistics.NumAnnotations"), //2
                    ElanLocale.getString("Statistics.MinimalDuration"), //3
                    ElanLocale.getString("Statistics.MaximalDuration"), //4
                    ElanLocale.getString("Statistics.AverageDuration"), //5
                    ElanLocale.getString("Statistics.MedianDuration"), //6
                    ElanLocale.getString("Statistics.TotalDuration"), //7
                    ElanLocale.getString("Statistics.TotalDurationPercentage"), //8
                    ElanLocale.getString("Statistics.Latency"), //9
                };

            fillTable(numRows, data);

            DefaultTableModel model = new DefaultTableModel(data, headers);
            statTable.setModel(model);
            statTable.getTableHeader()
                     .addMouseMotionListener(new TableHeaderToolTipAdapter(
                    statTable.getTableHeader()));
            
            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
            IntStringComparator<String> intComp = new IntStringComparator<String>();
            rowSorter.setComparator(1, intComp);
            rowSorter.setComparator(2, intComp);
            
            FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
            for (int i = 3; i < numCols; i++) {
            	rowSorter.setComparator(i, fsComp);
            }
            
            statTable.setRowSorter(rowSorter);
        }
    }

    /**
     * This method fills the table with statistics and labels
     *
     * @param numRows The number of rows in the table, usually equal to the
     *        number of linguistic types in the file
     * @param data The data matrix that will be filled.
     */
    private void fillTable(int numRows, String[][] data) {
        int y = 0;

        for (String participant : participants) {
            Vector<TierImpl> tiersWithParticipant = new Vector<TierImpl>();

            for (TierImpl tier : ((Vector<TierImpl>) transcription.getTiers())) {
                if (participant.equals(tier.getParticipant())) {
                    tiersWithParticipant.add(tier);
                }
            }

            data[y][0] = participant.length() == 0 ? "-" : participant;
            data[y][1] = new Integer(tiersWithParticipant.size()).toString();

            Integer nrAnnotations = new Integer(0);
            ArrayList<Long> durationList = new ArrayList<Long>(nrAnnotations);
            long timeAnnotated = 0;
            long maxDuration = 0;
            long minDuration = Long.MAX_VALUE;
            ArrayList<Long> beginTimes = new ArrayList<Long>(nrAnnotations);

            for (TierImpl tier : tiersWithParticipant) {
                nrAnnotations += tier.getNumberOfAnnotations();

                // Calculate the duration of each annotation and add to timeAnnotated
                /*
                   Vector<AlignableAnnotation> annotations = tier.getAnnotations();
                   for(AlignableAnnotation annotation:annotations){
                       TimeSlot beginTimeSlot = annotation.getBegin();
                       TimeSlot endTimeSlot = annotation.getEnd();
                       long duration = endTimeSlot.getTime()-beginTimeSlot.getTime();
                 */
                Vector<AbstractAnnotation> annotations = tier.getAnnotations();

                for (AbstractAnnotation annotation : annotations) {
                    long begin = annotation.getBeginTimeBoundary();
                    long end = annotation.getEndTimeBoundary();
                    long duration = end - begin;

                    durationList.add(new Long(duration));
                    timeAnnotated += duration;
                    maxDuration = (maxDuration < duration) ? duration
                                                           : maxDuration;
                    minDuration = (minDuration > duration) ? duration
                                                           : minDuration;
                    beginTimes.add(new Long(begin));
                }
            }

            if (nrAnnotations == 0) {
                data[y][2] = "-"; /* NumAnnotations */
                data[y][3] = "-"; /* MinDuration */
                data[y][4] = "-"; /* MaxDuration */
                data[y][5] = "-"; /* AvarageDuration */
                data[y][6] = "-"; /* MedianDuration */
                data[y][7] = "-"; /* TotalDuration */
                data[y][8] = "-"; /* TotalDurationPercentage */
                data[y][9] = "-"; /* Latency */
            } else {
                data[y][2] = nrAnnotations.toString(); /* NumAnnotations */
                data[y][3] = format2.format(minDuration / (float) 1000); /* MinDuration */
                data[y][4] = format2.format(maxDuration / (float) 1000); /* MaxDuration */
                data[y][5] = format2.format(timeAnnotated / nrAnnotations / (float) 1000); /* AvarageDuration */

                Collections.sort(durationList);

                int numDurs = durationList.size(); // should be same as numAnns
                long medianDur = 0L;

                if (numDurs == 1) {
                    medianDur = ((Long) durationList.get(0)).longValue();
                } else {
                    if ((numDurs % 2) != 0) {
                        // in case of an odd number, take the middle value
                        medianDur = ((Long) durationList.get(numDurs / 2)).longValue();
                    } else {
                        // in case of an even number, calculate the average of the 
                        // two middle values
                        long h = ((Long) durationList.get(numDurs / 2)).longValue();
                        long l = ((Long) durationList.get((numDurs / 2) - 1)).longValue();
                        medianDur = (h + l) / 2;
                    }
                }

                data[y][6] = format2.format(medianDur / (float) 1000); /* MedianDuration */
                data[y][7] = format2.format(timeAnnotated / (float) 1000); /* TotalDuration */
                data[y][8] = format2.format(timeAnnotated / (float) (totalDuration * tiersWithParticipant.size()) * 100); /* TotalDurationPercentage */

                if (beginTimes.size() > 0) {
                    Collections.sort(beginTimes);
                    data[y][9] = format2.format(beginTimes.get(0) / (float) 1000); /* Latency */
                } else {
                    data[y][9] = "-"; /* Latency */
                }
            }

            y++;
        }
    }
}
