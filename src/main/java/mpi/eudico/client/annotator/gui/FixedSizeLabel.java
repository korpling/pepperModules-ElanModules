package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;

import javax.swing.JLabel;


/**
 * DOCUMENT ME!
 * $Id: FixedSizeLabel.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class FixedSizeLabel extends JLabel {
    private Dimension dimension;

    /**
     * Creates a new FixedSizeLabel instance
     *
     * @param label DOCUMENT ME!
     * @param width DOCUMENT ME!
     * @param height DOCUMENT ME!
     */
    public FixedSizeLabel(String label, int width, int height) {
        super(label, JLabel.CENTER);
        dimension = new Dimension(width, height);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getMinimumSize() {
        return dimension;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPreferredSize() {
        return dimension;
    }
}
