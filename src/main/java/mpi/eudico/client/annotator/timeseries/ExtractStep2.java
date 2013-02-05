package mpi.eudico.client.annotator.timeseries;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Second step of a wizard to extract certain data from a time series track
 * based on time intervals (annotations) on a time-alignable tier. In this
 * step a track should be selected and a calculation and overwrite  method can
 * be specified.
 *
 * @author Han Sloetjes
 * @version 1.0 March 2006
 */
public class ExtractStep2 extends StepPane implements ListSelectionListener {
    private TranscriptionImpl transcription;
    private TSTrackManager manager;
    private JList trackList;
    private DefaultListModel trackModel;
    private JRadioButton aveRB;
    private JRadioButton minRB;
    private JRadioButton maxRB;
    private JRadioButton sumRB;
    private JRadioButton atBeginRB;
    private JRadioButton atEndRB;
    private JCheckBox overwriteCB;

    /**
     * A panel for the second step of the wizard.
     *
     * @param multiPane the container multistep pane
     * @param transcription the transcription containing source and destination
     *        tier
     * @param manager the track manager containing the time series tracks
     */
    public ExtractStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription, TSTrackManager manager) {
        super(multiPane);
        this.transcription = transcription;
        this.manager = manager;
        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    public void initComponents() {
        // setPreferredSize
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        JScrollPane listScroll;

        trackModel = new DefaultListModel();
        trackList = new JList(trackModel);
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScroll = new JScrollPane(trackList);

        if (manager != null) {
            ArrayList tracks = manager.getRegisteredTracks();
            AbstractTSTrack tr;

            for (int i = 0; i < tracks.size(); i++) {
                tr = (AbstractTSTrack) tracks.get(i);
                trackModel.addElement(tr.getName());
            }
        }

        ButtonGroup group = new ButtonGroup();
        aveRB = new JRadioButton(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Average"));
        minRB = new JRadioButton(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Minimum"));
        maxRB = new JRadioButton(ElanLocale.getString(
        	"TimeSeriesViewer.Extract.Maximum"));
        sumRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.Sum"));
        atBeginRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.AtBegin"));
        atEndRB = new JRadioButton(ElanLocale.getString(
    		"TimeSeriesViewer.Extract.AtEnd"));
        group.add(aveRB);
        group.add(minRB);
        group.add(maxRB);
        group.add(sumRB);
        group.add(atBeginRB);
        group.add(atEndRB);
        minRB.setSelected(true);
        overwriteCB = new JCheckBox(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.Overwrite"));
        overwriteCB.setSelected(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.SourceTrack") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(listScroll, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.Method") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        add(minRB, gbc);
        gbc.gridx = 1;
        add(maxRB, gbc);
        gbc.gridx = 2;
        add(aveRB, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        add(sumRB, gbc);
        gbc.gridx = 1;
        add(atBeginRB, gbc);
        gbc.gridx = 2;
        add(atEndRB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(overwriteCB, gbc);

        trackList.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Returns the title of this step.
     *
     * @return the title
     */
    public String getStepTitle() {
        return ElanLocale.getString("TimeSeriesViewer.Extract.SelectTrack");
    }

    /**
     * Check if the Finish button can be enabled.
     */
    public void enterStepForward() {
        if (trackList.getSelectedIndex() >= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }

    /**
     * Enable/disable buttons.
     *
     * @see #enterStepForward()
     */
    public void enterStepBackward() {
        this.enterStepForward();
    }

    /**
     * If a track has been selected store track name, calculation method and
     * overwrite mode.
     *
     * @return true if all conditions to proceed have been met, false otherwise
     */
    public boolean leaveStepForward() {
        if (trackList.getSelectedIndex() >= 0) {
            String trackName = (String) trackList.getSelectedValue();
            multiPane.putStepProperty("TrackName", trackName);

            String calcType = "Min";

            if (maxRB.isSelected()) {
                calcType = "Max";
            } else if (aveRB.isSelected()) {
                calcType = "Ave";
            } else if (sumRB.isSelected()) {
            	calcType = "Sum";
            } else if (atBeginRB.isSelected()) {
            	calcType = "AtBegin";
            } else if (atEndRB.isSelected()) {
            	calcType = "AtEnd";
            }

            multiPane.putStepProperty("Calc", calcType);
            multiPane.putStepProperty("Overwrite",
                String.valueOf(overwriteCB.isSelected()));

            return true;
        } else {
            return false;
        }
    }

    /**
     * Finishes by moving to the next step, the progress panel.
     *
     * @return false, the window should not be closed yet
     */
    public boolean doFinish() {
        if (leaveStepForward()) {
            multiPane.nextStep();
        }

        return false;
    }

    /**
     * Change in track selection; check if the Finish button can be enabled
     *
     * @param e the event
     */
    public void valueChanged(ListSelectionEvent e) {
        if (trackList.getSelectedIndex() >= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }
}
