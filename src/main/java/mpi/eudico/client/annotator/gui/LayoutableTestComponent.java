package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * DOCUMENT ME!
 * $Id: LayoutableTestComponent.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class LayoutableTestComponent extends JComponent implements Layoutable {
    private boolean bWantsAllAvailableSpace = false;
    private boolean bIsOptional = false;
    private boolean bIsDetachable = false;
    private boolean bIsHorizontallyResizable = false;
    private boolean bIsVerticallyResizable = false;
    private int imageOffset = 0;
    private int minimalWidth = 0;
    private int minimalHeight = 0;
    private int nr;
    private Color color;

    /**
     * Creates a new LayoutableTestComponent instance
     *
     * @param nr DOCUMENT ME!
     * @param color DOCUMENT ME!
     */
    LayoutableTestComponent(int nr, Color color) {
        this.nr = nr;
        this.color = color;

        setBackground(color);

        JButton but = new JButton("" + nr);
        but.setBackground(color);
        but.setSize(getMinimalWidth(), getMinimalHeight());

        setLayout(new BorderLayout());
        add(but, BorderLayout.CENTER);
    }

    // uses all free space in horizontal direction
    public boolean wantsAllAvailableSpace() {
        if ((nr == 7) || (nr == 8)) {
            return true;
        } else {
            return false;
        }
    }

    // can be shown/hidden. If hidden, dimensions are (0,0), position in layout is kept
    public boolean isOptional() {
        return false;
    }

    // can be detached, re-attached from main document window
    public boolean isDetachable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isWidthChangeable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isHeightChangeable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinimalWidth() {
        return 50;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinimalHeight() {
        return 50;
    }

    // position of image wrt Layoutable's origin, to be used for spatial alignment
    public int getImageOffset() {
        return 10;
    }
}
