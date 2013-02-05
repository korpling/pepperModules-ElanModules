package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Component;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * DOCUMENT ME!
 * $Id: LangCellRenderer.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public class LangCellRenderer extends JLabel implements ListCellRenderer {
    /**
     * Creates a new LangCellRenderer instance
     */
    public LangCellRenderer() {
        setOpaque(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param list DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @param isSelected DOCUMENT ME!
     * @param cellHasFocus DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        setText(((Locale) value).getDisplayName());
        setBackground(isSelected ? Color.lightGray : Color.white);
        setForeground(isSelected ? Color.white : Color.black);

        return this;
    }
}
