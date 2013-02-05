package mpi.eudico.client.annotator.gui;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


/**
 * An abstract panel class for a statistics table and related gui elements.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public abstract class AbstractStatisticsPanel extends JPanel {
    /** the transcription */
    protected TranscriptionImpl transcription;

    /** the total (media) duration */
    protected long totalDuration;

    //Statistics table GUI 
    /** the scrollpane for the table */
    protected JScrollPane statPane;

    /** the panel for the table */
    protected JPanel statPanel;

    /** the statistics table */
    protected JTable statTable;

    /** no selection */
    protected final String EMPTY = "-";

    /**
     * Creates a new AbstractStatisticsPanel instance
     *
     * @param transcription the transcription
     */
    public AbstractStatisticsPanel(Transcription transcription) {
        super();
        this.transcription = (TranscriptionImpl) transcription;
        initComponents();
    }

    /**
     * Creates a new AbstractStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param totalDuration the duration
     */
    public AbstractStatisticsPanel(Transcription transcription,
        long totalDuration) {
        super();
        this.transcription = (TranscriptionImpl) transcription;
        this.totalDuration = totalDuration;
    }

    /**
     * initialise ui elements and create the initial table
     */
    abstract void initComponents();

    /**
     * Returns the current table.
     *
     * @return the current statistics table
     */
    public abstract JTable getStatisticsTable();
}
