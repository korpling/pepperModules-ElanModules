package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * DOCUMENT ME!
 * $Id: SelectionButtonsPanel.java 22614 2011-03-15 13:02:17Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class SelectionButtonsPanel extends JComponent {
    private JButton butPlaySelection;
    private JButton butClearSelection;
    private JButton butToggleCrosshairInSelection;

    /**
     * Creates a new SelectionButtonsPanel instance
     *
     * @param buttonSize DOCUMENT ME!
     * @param theVM DOCUMENT ME!
     */
    public SelectionButtonsPanel(Dimension buttonSize, ViewerManager2 theVM) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        butPlaySelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.PLAY_SELECTION));
        butPlaySelection.setPreferredSize(buttonSize);
        add(butPlaySelection);

        butClearSelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.CLEAR_SELECTION));
        butClearSelection.setPreferredSize(buttonSize);
        add(butClearSelection);

        butToggleCrosshairInSelection = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(),
                    ELANCommandFactory.SELECTION_BOUNDARY));
        butToggleCrosshairInSelection.setPreferredSize(buttonSize);
        add(butToggleCrosshairInSelection);
    }

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		butPlaySelection.getAction().setEnabled(enabled);
		butClearSelection.getAction().setEnabled(enabled);
		butToggleCrosshairInSelection.getAction().setEnabled(enabled);
	}
    
    
}
