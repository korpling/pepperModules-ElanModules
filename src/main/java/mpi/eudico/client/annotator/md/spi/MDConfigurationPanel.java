package mpi.eudico.client.annotator.md.spi;

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;


/**
 * Abstract class for configuration (selection) of metadata keys.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public abstract class MDConfigurationPanel extends JPanel {
    /**
     * Creates a new MDConfigurationPanel instance
     */
    public MDConfigurationPanel() {
        super();
    }

    /**
     * Applies the changes made to the selection.
     */
    public abstract void applyChanges();
}
