package mpi.eudico.client.annotator;

import javax.swing.JLabel;

import mpi.eudico.util.TimeFormatter;


/**
 * DOCUMENT ME!
 * $Id: DurationPanel.java 20468 2010-10-21 15:00:51Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class DurationPanel extends JLabel {
    /**
     * Creates a new DurationPanel instance
     *
     * @param duration DOCUMENT ME!
     */
    public DurationPanel(long duration) {
        super(TimeFormatter.toString(duration)); //0 doesn't matter because player isn't set yet (see ViewerManager)
        setFont(Constants.SMALLFONT);
    }
}
