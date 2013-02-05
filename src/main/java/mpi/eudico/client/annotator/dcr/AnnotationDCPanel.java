package mpi.eudico.client.annotator.dcr;

import mpi.dcr.DCSmall;
import mpi.dcr.ILATDCRConnector;

import mpi.eudico.client.annotator.ElanLocale;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * A panel containing ui elements to set or change the reference from an
 * annotation to  a data category.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class AnnotationDCPanel extends LocalDCSPanel {
    // additional components
    /** the dcr panel */
    protected JPanel dcrPanel;

    /** the dcr label */
    protected JLabel dcrLabel;

    /** the dc identifier textfield */
    protected JTextField dcrField;

    /** the dc id field */
    protected JTextField dcIdField;

    /** the dcr button */
    protected JButton dcrButton;
    private String annotationLabel;

    /**
     * Creates a new AnnotationDCPanel instance
     *
     * @param connector the connector
     */
    public AnnotationDCPanel(ILATDCRConnector connector) {
        super(connector);
    }

    /**
     * Creates a new AnnotationDCPanel instance
     *
     * @param connector the connector
     * @param resBundle the resource bundle
     */
    public AnnotationDCPanel(ILATDCRConnector connector,
        ResourceBundle resBundle) {
        super(connector, resBundle);
    }

    /**
     * Creates a new AnnotationDCPanel instance
     *
     * @param connector the connector
     * @param resBundle the resource bundle
     * @param annotationLabel the annotation label
     */
    public AnnotationDCPanel(ILATDCRConnector connector,
        ResourceBundle resBundle, String annotationLabel) {
        super(connector, resBundle);
        this.annotationLabel = annotationLabel;
        updateLocale();
    }

    /**
     * Updates the dc text fields with information for the data category with
     * the specified id.
     *
     * @param dcId the id of the category
     */
    public void setAnnotationDCId(String dcId) {
        dcIdField.setText(dcId);

        // retrieve the identifier string
        DCSmall sm = ELANLocalDCRConnector.getInstance().getDCSmall(dcId);

        if (sm != null) {
            dcrField.setText(sm.getIdentifier());
        }
    }

    /**
     * Returns the current data category id.
     *
     * @return the current category id
     */
    public String getAnnotationDCId() {
        if ((dcIdField.getText() != null) &&
                (dcIdField.getText().length() > 0)) {
            return dcIdField.getText();
        }

        return null;
    }

    /**
     * Initializes ui components.
     */
    protected void initComponents() {
        dcrPanel = new JPanel(new GridBagLayout());
        dcrLabel = new JLabel();
        dcrField = new JTextField();
        dcIdField = new JTextField();
        dcIdField.setEditable(false);
        dcrField.setEditable(false);
        //dcrField.setEnabled(false);
        dcrButton = new JButton();

        super.initComponents();

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 6, 2, 6);
        dcrPanel.add(dcrLabel, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 6, 0, 6);
        dcrPanel.add(dcrField, gridBagConstraints);
        // add the id field?
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 0.5;
        dcrPanel.add(dcIdField, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0.0;
        //gridBagConstraints.insets = new Insets(2, 6, 0, 6);
        dcrPanel.add(dcrButton, gridBagConstraints);

        remove(catPanel);
        remove(profPanel);
        remove(descPanel);

        Insets insets = new Insets(2, 6, 2, 6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = insets;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 3;
        add(dcrPanel, gridBagConstraints);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        add(profPanel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        add(catPanel, gbc);

        //descPanel.setMinimumSize(dim);
        //descPanel.setPreferredSize(dim);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        add(descPanel, gbc);

        dcrButton.addActionListener(this);
    }

    /**
     * @see mpi.dcr.LocalDCSelectPanel#updateLocale()
     */
    protected void updateLocale() {
        super.updateLocale();
        dcrPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "DCR.Label.ISOCategory")));

        if (annotationLabel != null) {
            dcrLabel.setText(annotationLabel);
        }

        dcrButton.setText(ElanLocale.getString("Button.Delete"));
    }

    /**
     * After updating description panel, update the annotations dc fields
     *
     * @see mpi.dcr.AbstractDCSelectPanel#updateDescription(mpi.dcr.DCSmall)
     */
    protected void updateDescription(DCSmall dc) {
        super.updateDescription(dc);

        if (dc != null) {
            dcrField.setText(dc.getIdentifier());
            dcIdField.setText(dc.getId());
        } else {
            dcIdField.setText("");
            dcrField.setText("");
        }
    }

    /**
     * @see mpi.dcr.LocalDCSelectPanel#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dcrButton) {
            dcIdField.setText("");
            dcrField.setText("");

            return;
        }

        super.actionPerformed(e);
    }
}
