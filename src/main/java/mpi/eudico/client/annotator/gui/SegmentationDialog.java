package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.*;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.viewer.SegmentationViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.util.TimeInterval;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


/**
 * A dialog for convenient batch-like creation of annotations/segmentation.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 * @version Jan 2008 fixed sized, one stroke annotation mode added
 * @version Dec 2009 Added support for shortcut keys of controlled vocabulary entries.
 */
public class SegmentationDialog extends ClosableDialog implements ActionListener,
    ItemListener, ControllerListener {
    private JComboBox tierComboBox;
    private JLabel keyLabel;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JLabel tierLabel;
    private JRadioButton oneClickRB;
    private JRadioButton twoClicksRB;
    private ButtonGroup optionButtonGroup;
    private JPanel tierSelectionPanel;
    private JPanel tierPreviewPanel;
    private JPanel controlPanel;
    private JButton applyButton;
    private JButton closeButton;
    private JPanel buttonPanel;
    private TranscriptionImpl transcription;
    private ElanMediaPlayer player;

    // jan 2008 new ui elements
    private JPanel fixedOptPanel;
    private JRadioButton oneClickFixedRB;
    private JLabel durLabel;
    private JTextField durTF;
    private JRadioButton beginStrokeRB;
    private JRadioButton endStrokeRB;
    private ButtonGroup boundGroup;

    /** no selection */
    private final String EMPTY = "-";

    /**
     * every time the enter key is typed either a begin or an end time is added
     */
    private final int TWO_TIMES_SEGMENTATION = 0;

    /** every time the enter key is typed an end and a begin are added */
    private final int ONE_TIME_SEGMENTTATION = 1;

    /** every time the enter key is typed an annotation of fixed duration is created,
     * the stroke time is either the begin or the end time */
    private final int ONE_TIME_FIXED_SEGMENTTATION = 2;

    // by default the single stroke of a fixed annotation marks the begin
    private boolean singleStrokeIsBegin = true;
    private long fixedDuration = 1000;

    // administration
    private ArrayList timeSegments;
    private String curTier;
    private CVEntry[] entries;

    // default mode is the two-times-segmentation mode
    private int mode = TWO_TIMES_SEGMENTATION;
    private long lastSegmentTime = -1;
    private int timeCount = 0;
    private SegmentationViewer previewer;
	private InputMap mainInputMap;
	private InputMap cvInputMap;
	private ActionMap mainActionMap;
	private ActionMap cvActionMap;

    /**
     * Creates a new SegmentationDialog instance
     *
     * @param transcription the transcription
     */
    public SegmentationDialog(Transcription transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = (TranscriptionImpl) transcription;
        player = ELANCommandFactory.getViewerManager(transcription)
                                   .getMasterMediaPlayer();
//        previewer = ELANCommandFactory.getViewerManager(transcription)
//                                      .createSegmentationViewer();
        previewer = new SegmentationViewer(transcription);
        timeSegments = new ArrayList();

        initComponents();
        postInit();
        extractRootTiers();
        //postInit();
    }

    /**
     * Extract the root tiers as candidates for auto segmentation.
     */
    private void extractRootTiers() {
        if (transcription != null) {
            Vector tiers = transcription.getTiers();
            Iterator tierIt = tiers.iterator();
            TierImpl tier = null;

            while (tierIt.hasNext()) {
                tier = (TierImpl) tierIt.next();

                if (tier.getLinguisticType().getConstraints() == null) {
                    tierComboBox.addItem(tier.getName());
                }
            }

            // if there are no tiers yet
            if (tierComboBox.getModel().getSize() == 0) {
                tierComboBox.addItem(EMPTY);
            }
        } else {
            tierComboBox.addItem(EMPTY);
        }

        curTier = (String) tierComboBox.getSelectedItem();

        if (!curTier.equals(EMPTY)) {
            previewer.setTier(transcription.getTierWithId(curTier));
        }
    }

    /**
     * Initializes UI elements.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        tierComboBox = new JComboBox();
        keyLabel = new JLabel();
        titleLabel = new JLabel();
        titlePanel = new JPanel();
        tierLabel = new JLabel();
        oneClickRB = new JRadioButton();
        twoClicksRB = new JRadioButton();
        optionButtonGroup = new ButtonGroup();
        tierSelectionPanel = new JPanel();
        tierPreviewPanel = new JPanel();
        controlPanel = new JPanel();
        applyButton = new JButton();
        closeButton = new JButton();
        buttonPanel = new JPanel();
        fixedOptPanel = new JPanel(new GridBagLayout());
        oneClickFixedRB = new JRadioButton();
        durLabel = new JLabel();
        durTF = new JTextField(6);
        beginStrokeRB = new JRadioButton();
        endStrokeRB = new JRadioButton();
        boundGroup = new ButtonGroup();
        updateLocale();

        GridBagConstraints gridBagConstraints;
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        Insets optInsets = new Insets(1, 6, 0, 6);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(titlePanel, gridBagConstraints);

        tierSelectionPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(tierLabel, gridBagConstraints);

        tierComboBox.addItemListener(this);
        tierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(tierComboBox, gridBagConstraints);

        twoClicksRB.setSelected(true);
        optionButtonGroup.add(twoClicksRB);
        twoClicksRB.addActionListener(this);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        gridBagConstraints.weightx = 1.0;
        tierSelectionPanel.add(twoClicksRB, gridBagConstraints);

        optionButtonGroup.add(oneClickRB);
        oneClickRB.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(oneClickRB, gridBagConstraints);

        optionButtonGroup.add(oneClickFixedRB);
        oneClickFixedRB.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(oneClickFixedRB, gridBagConstraints);

        // the fixed size annotation options panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = optInsets;
        fixedOptPanel.add(durLabel, gbc);

        durTF.setText(String.valueOf(fixedDuration));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fixedOptPanel.add(durTF, gbc);

        beginStrokeRB.setSelected(true);
        boundGroup.add(beginStrokeRB);
        beginStrokeRB.addActionListener(this);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        fixedOptPanel.add(beginStrokeRB, gbc);

        endStrokeRB.addActionListener(this);
        boundGroup.add(endStrokeRB);
        gbc.gridy = 2;
        fixedOptPanel.add(endStrokeRB, gbc);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(fixedOptPanel, gridBagConstraints);

        keyLabel.setFont(Constants.DEFAULTFONT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = optInsets;
        tierSelectionPanel.add(keyLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(tierSelectionPanel, gridBagConstraints);

        Dimension tpd = new Dimension(400, 60);
        tierPreviewPanel.setMinimumSize(tpd);
        tierPreviewPanel.setPreferredSize(tpd);
        tierPreviewPanel.setLayout(new GridBagLayout());

        //JPanel timeLinePanel = new JPanel(); // timeline light
        //timeLinePanel.setBackground(java.awt.Color.white);
        //previewer.setMinimumSize(tpd);
        //previewer.setPreferredSize(tpd);
        previewer.setBorder(new LineBorder(Constants.SHAREDCOLOR3, 1));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tierPreviewPanel.add(previewer, gridBagConstraints);

        controlPanel.setLayout(new GridLayout(1, 3, 6, 0));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.GO_TO_BEGIN)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.PREVIOUS_SCROLLVIEW)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.SECOND_LEFT)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.PLAY_PAUSE)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.SECOND_RIGHT)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.NEXT_SCROLLVIEW)));
        controlPanel.add(new JButton(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.GO_TO_END)));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        tierPreviewPanel.add(controlPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tierPreviewPanel, gridBagConstraints);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        applyButton.addActionListener(this);
        buttonPanel.add(applyButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);

        enableFixedDurUI(false);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        Rectangle dialogBounds = (Rectangle) Preferences.get("SegmentationDialogBounds",
                null);

        if (dialogBounds != null) {
            setBounds(dialogBounds);
        } else {
            int w = 550;
            int h = 380;
            setSize((getSize().width < w) ? w : getSize().width,
                (getSize().height < h) ? h : getSize().height);
            setLocationRelativeTo(getParent());
        }

        setResizable(true);

        mainInputMap = getRootPane()
                                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		mainActionMap = getRootPane().getActionMap();
		if (mainInputMap instanceof ComponentInputMap && (mainActionMap != null)) {
            Action[] invActions = new Action[] {
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.PLAY_PAUSE),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.GO_TO_BEGIN),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.GO_TO_END),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.PREVIOUS_SCROLLVIEW),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.NEXT_SCROLLVIEW),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.SECOND_LEFT),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.SECOND_RIGHT),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.PREVIOUS_FRAME),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.NEXT_FRAME),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.PIXEL_LEFT),
                    ELANCommandFactory.getCommandAction(transcription,
                        ELANCommandFactory.PIXEL_RIGHT), new SegmentAction()
                };

            /*
                        for (int i = 0; i < invActions.length; i++) {
                            inputMap.put((KeyStroke) invActions[i].getValue(
                                    Action.ACCELERATOR_KEY),
                                invActions[i].getValue(Action.DEFAULT));
                            actionMap.put(invActions[i].getValue(Action.DEFAULT),
                                invActions[i]);
                        }
                        */
            String id = "Act-";
            String nextId;

            for (int i = 0; i < invActions.length; i++) {
                nextId = id + i;
                mainInputMap.put((KeyStroke) invActions[i].getValue(
                        Action.ACCELERATOR_KEY), nextId);
                mainActionMap.put(nextId, invActions[i]);
            }
        }

        if (transcription != null) {
            ELANCommandFactory.getViewerManager(transcription)
                              .connectListener(this);
            ELANCommandFactory.getViewerManager(transcription)
                              .connectListener(previewer);
        }
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        setTitle(ElanLocale.getString("SegmentationDialog.Title"));
        titleLabel.setText(ElanLocale.getString("SegmentationDialog.Title"));
        keyLabel.setText(ElanLocale.getString("SegmentationDialog.Label.Key") +
            "  " +
            ELANCommandFactory.convertAccKey(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, 0)));
        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "SegmentationDialog.Title")));
        tierPreviewPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "SegmentationDialog.Preview")));
        tierLabel.setText(ElanLocale.getString("SegmentationDialog.Label.Tier"));
        oneClickRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.SingleStroke"));
        twoClicksRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.DoubleStroke"));
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        closeButton.setText(ElanLocale.getString("Button.Cancel"));
        oneClickFixedRB.setText(ElanLocale.getString(
                "SegmentationDialog.Mode.SingleStrokeFixed"));
        durLabel.setText(ElanLocale.getString(
                "SegmentationDialog.Label.Duration"));
        beginStrokeRB.setText(ElanLocale.getString(
                "SegmentationDialog.Label.BeginStroke"));
        endStrokeRB.setText(ElanLocale.getString(
                "SegmentationDialog.Label.EndStroke"));
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog(WindowEvent evt) {
        if (transcription != null) {
            ELANCommandFactory.getViewerManager(transcription)
                              .disconnectListener(this);
            ELANCommandFactory.getViewerManager(transcription)
                              .destroyViewer(previewer);
        }

        savePreferences();
        setVisible(false);
        dispose();
    }

    /**
     * Save preferences.
     */
    private void savePreferences() {
        Preferences.set("SegmentationDialogBounds", getBounds(), null);
    }

    /**
     * Enables or disables ui elements when the player is started or stopped.
     *
     * @param enable if true most option elements are enabled, some depend on
     * the selected state of others
     */
    private void enableUI(boolean enable) {
        tierComboBox.setEnabled(enable);
        twoClicksRB.setEnabled(enable);
        oneClickRB.setEnabled(enable);
        oneClickFixedRB.setEnabled(enable);

        if (oneClickFixedRB.isSelected() && enable) {
            enableFixedDurUI(enable);
        } else {
            enableFixedDurUI(false);
        }
    }

    /**
     * Enables/disables the ui elements for the fixed duration annotation mode.
     *
     * @param enable if true the fixed duration ui elements are enabled
     */
    private void enableFixedDurUI(boolean enable) {
        fixedOptPanel.setEnabled(enable);
        durLabel.setEnabled(enable);
        durTF.setEditable(enable);
        beginStrokeRB.setEnabled(enable);
        endStrokeRB.setEnabled(enable);
    }

    /**
     * The button actions.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == applyButton) {
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.ADD_SEGMENTATION);

            c.execute(transcription, new Object[] { curTier, timeSegments });

            closeDialog(null);
        } else if (source == closeButton) {
            closeDialog(null);
        } else if (source == oneClickRB) {
            if (mode != ONE_TIME_SEGMENTTATION) {
                timeCount = 0;
                lastSegmentTime = -1;
            }

            enableFixedDurUI(false);
        } else if (source == twoClicksRB) {
            if (mode != TWO_TIMES_SEGMENTATION) {
                timeCount = 0;
                lastSegmentTime = -1;
            }

            enableFixedDurUI(false);
        } else if (source == oneClickFixedRB) {
            if (mode != ONE_TIME_FIXED_SEGMENTTATION) {
                timeCount = 0;
                lastSegmentTime = -1;
            }

            enableFixedDurUI(true);
        } else if (source == beginStrokeRB) {
            singleStrokeIsBegin = true;
        } else if (source == endStrokeRB) {
            singleStrokeIsBegin = false;
        }
    }

    /**
     * Selection of a different tier.
     *
     * @param ie the item event
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            String newSel = (String) tierComboBox.getSelectedItem();

            if (newSel.equals(curTier)) {
                return;
            }

            if ((player != null) && player.isPlaying()) {
                player.stop();
            }

            if (timeSegments.size() > 0) {
                if (showConfirmDialog(ElanLocale.getString(
                                "SegmentationDialog.Message.Apply"))) {
                    Command c = ELANCommandFactory.createCommand(transcription,
                            ELANCommandFactory.ADD_SEGMENTATION);

                    c.execute(transcription,
                        new Object[] { curTier, timeSegments });
                }

                timeSegments = new ArrayList();
                timeCount = 0;
                lastSegmentTime = -1;
            }

            curTier = newSel;
            entries = null;

            if (!curTier.equals(EMPTY)) {
            	TierImpl t = (TierImpl) transcription.getTierWithId(curTier);
                previewer.setTier(t);
                String cvname = t.getLinguisticType().getControlledVocabylaryName();
                if (cvname != null) {
                	ControlledVocabulary cv = transcription.getControlledVocabulary(cvname);
                	if (cv != null) {
                		entries = cv.getEntries();
                		// extract keys? add actions
                		if (cvInputMap == null) { 
                			cvInputMap = new ComponentInputMap(getRootPane());
                			mainInputMap.setParent(cvInputMap);
                		} else {
                			cvInputMap.clear();
                		}
                		if (cvActionMap == null) {
                			cvActionMap = new ActionMap();
                			mainActionMap.setParent(cvActionMap);
                		} else {
                			cvActionMap.clear();
                		}
                		String cveId = "cve-";
                		String nextId;
                		CVEntry cve;
                		SegmentAction sa;
                		
                		for (int i = 0; i < entries.length; i++) {
                			if (entries[i].getShortcutKeyCode() <= 0) {
                				continue;
                			}
                			nextId = cveId + i;
                			sa = new SegmentAction(entries[i]);
                			cvInputMap.put((KeyStroke) sa.getValue(Action.ACCELERATOR_KEY), nextId);
                			cvActionMap.put(nextId, sa);
                		}
                		
                	} else {//no cv
                		if (cvInputMap != null) {
                    		cvInputMap.clear();
                    	}
                    	if (cvActionMap != null) {
                    		cvActionMap.clear();
                    	}
                	}
                } else {// no cv name
                	if (cvInputMap != null) {
                		cvInputMap.clear();
                	}
                	if (cvActionMap != null) {
                		cvActionMap.clear();
                	}
                }
            } else {// no tier
            	if (cvInputMap != null) {
            		cvInputMap.clear();
            	}
            	if (cvActionMap != null) {
            		cvActionMap.clear();
            	}
            }
        }
    }

    /**
     * Handles controller (i.e. player) updates.
     *
     * @param event the controller event
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StartEvent) {
            if (twoClicksRB.isSelected()) {
                mode = TWO_TIMES_SEGMENTATION;
            } else if (oneClickRB.isSelected()) {
                mode = ONE_TIME_SEGMENTTATION;
            } else {
                mode = ONE_TIME_FIXED_SEGMENTTATION;

                String durVal = durTF.getText();

                try {
                    fixedDuration = Long.parseLong(durVal);
                } catch (NumberFormatException nfe) {
                    // warning message??
                    fixedDuration = 1000;
                    durTF.setText(String.valueOf(fixedDuration));
                }
            }

            enableUI(false);
        } else if (event instanceof StopEvent) {
            enableUI(true);
        }
    }

    /**
     * Add the begin or end boundary of an annotation.
     */
    /*
    public void addSegmentTime() {
        if (player == null) {
            return;
        }

        long cur = player.getMediaTime();

        if (mode == TWO_TIMES_SEGMENTATION) {
            if (cur != lastSegmentTime) {
                timeCount++;

                if ((timeCount % 2) != 0) {
                    lastSegmentTime = cur;
                    previewer.setCurrentBeginTime(lastSegmentTime);
                } else {
                    TimeInterval ti = new TimeInterval(lastSegmentTime, cur);

                    if (cur < lastSegmentTime) {
                        ti = new TimeInterval(cur, lastSegmentTime);
                    }

                    if (ti.getDuration() > 0) {
                        timeSegments.add(ti);
                        previewer.addSegment(ti);
                    }

                    lastSegmentTime = cur;
                }
            }
        } else if (mode == ONE_TIME_SEGMENTTATION) {
            if (lastSegmentTime == -1) {
                timeCount++;
                lastSegmentTime = cur;
                previewer.setCurrentBeginTime(lastSegmentTime);
            } else {
                if (cur != lastSegmentTime) {
                    timeCount++;

                    TimeInterval ti = new TimeInterval(lastSegmentTime, cur);

                    if (cur < lastSegmentTime) {
                        ti = new TimeInterval(cur, lastSegmentTime);
                    }

                    if (ti.getDuration() > 0) {
                        timeSegments.add(ti);
                        previewer.addSegment(ti);
                    }

                    lastSegmentTime = cur;
                }
            }
        } else {
            // ONE_TIME_FIXED_SEGMENTTATION mode
            TimeInterval ti = null;

            if (singleStrokeIsBegin) {
                ti = new TimeInterval(cur, cur + fixedDuration);
            } else {
                long bb = cur - fixedDuration;

                if (bb < 0) {
                    bb = 0;
                }

                ti = new TimeInterval(bb, cur);
            }

            if (ti.getDuration() > 0) {
                timeSegments.add(ti);
                previewer.addSegment(ti);
            }

            lastSegmentTime = cur;
        }
    }
    */
    
    /**
     * Add the begin or end boundary of an annotation and the value of the CV entry.
     */
    public void addSegmentTime(CVEntry cve) {
    	if (player == null) {
            return;
        }

        long cur = player.getMediaTime();

        if (mode == TWO_TIMES_SEGMENTATION) {
            if (cur != lastSegmentTime) {
                timeCount++;

                if ((timeCount % 2) != 0) {
                    lastSegmentTime = cur;
                    previewer.setCurrentBeginTime(lastSegmentTime);
                } else {
                	if (cve != null) {
                		AnnotationDataRecord adr;
                		if (cur > lastSegmentTime) {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), lastSegmentTime, cur);
                		} else {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, lastSegmentTime);
                		}
                		adr.setExtRef(cve.getExternalRef());
                		
                		if (adr.getEndTime() - adr.getBeginTime() > 0) {
                			timeSegments.add(adr);
                			previewer.addSegment(adr);
                		}
                	} else {
	                    TimeInterval ti;
	                    if (cur > lastSegmentTime) {
	                    	ti = new TimeInterval(lastSegmentTime, cur);
	                    } else {
	                        ti = new TimeInterval(cur, lastSegmentTime);
	                    }
	
	                    if (ti.getDuration() > 0) {
	                        timeSegments.add(ti);
	                        previewer.addSegment(ti);
	                    }
                	}

                    lastSegmentTime = cur;
                }
            }
        } else if (mode == ONE_TIME_SEGMENTTATION) {
            if (lastSegmentTime == -1) {
                timeCount++;
                lastSegmentTime = cur;
                previewer.setCurrentBeginTime(lastSegmentTime);
            } else {
                if (cur != lastSegmentTime) {
                    timeCount++;
                    if (cve != null) {
                		AnnotationDataRecord adr;
                		if (cur > lastSegmentTime) {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), lastSegmentTime, cur);
                		} else {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, lastSegmentTime);
                		}
                		adr.setExtRef(cve.getExternalRef());
                		
                		if (adr.getEndTime() - adr.getBeginTime() > 0) {
                			timeSegments.add(adr);
                			previewer.addSegment(adr);
                		}
                    } else {
	                    TimeInterval ti;
	                    if (cur > lastSegmentTime) {
	                    	ti = new TimeInterval(lastSegmentTime, cur);
	                    } else {
	                        ti = new TimeInterval(cur, lastSegmentTime);
	                    }
	
	                    if (ti.getDuration() > 0) {
	                        timeSegments.add(ti);
	                        previewer.addSegment(ti);
	                    }
                    }

                    lastSegmentTime = cur;
                }
            }
        } else {
            // ONE_TIME_FIXED_SEGMENTTATION mode
        	if (cve != null) {
        		AnnotationDataRecord adr;
        		
                if (singleStrokeIsBegin) {
                    adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, cur + fixedDuration);
                } else {
                    long bb = cur - fixedDuration;

                    if (bb < 0) {
                        bb = 0;
                    }

                    adr = new AnnotationDataRecord(curTier, cve.getValue(), bb, cur);
                }
                adr.setExtRef(cve.getExternalRef());
                
                if (adr.getEndTime() - adr.getBeginTime() > 0) {
                    timeSegments.add(adr);
                    previewer.addSegment(adr);
                }
        	} else {
	            TimeInterval ti = null;
	
	            if (singleStrokeIsBegin) {
	                ti = new TimeInterval(cur, cur + fixedDuration);
	            } else {
	                long bb = cur - fixedDuration;
	
	                if (bb < 0) {
	                    bb = 0;
	                }
	
	                ti = new TimeInterval(bb, cur);
	            }
	
	            if (ti.getDuration() > 0) {
	                timeSegments.add(ti);
	                previewer.addSegment(ti);
	            }
        	}

            lastSegmentTime = cur;
        }	
    }

    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the messsage to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    private boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message,
                ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }
    

    /**
     * An action class to handle the enter-key-typed event.
     *
     * @author Han Sloetjes
     */
    class SegmentAction extends AbstractAction {
        /**
         * Constructor, sets the accelerator key to the VK_ENTER key.
         */
        public SegmentAction() {
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            putValue(Action.DEFAULT, null);
        }
        
        /**
         * Constructor with key code and (annotation) value as parameters.
         * 
         * @param keyCode the key code
         * @param value the value for the segment
         */
        public SegmentAction(CVEntry cve) {
        	putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(cve.getShortcutKeyCode(), 0));
        	putValue(Action.DEFAULT, cve);
        }

        /**
         * Forwards the action to the enclosing class.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
        	Object val = getValue(Action.DEFAULT);
        	if (val instanceof CVEntry) {
        		SegmentationDialog.this.addSegmentTime((CVEntry) val);
        	} else {
        		SegmentationDialog.this.addSegmentTime(null);
        	}
        }
    }

}
