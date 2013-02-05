package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;


/**
 * A simple dialog showing a process report. Currently this is done in a
 * textarea in a scrollpane.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class ReportDialog extends ClosableDialog {
    private ProcessReport report;

    /**
     * Creates a new ReportDialog instance for the specified report.
     *
     * @param report The report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(ProcessReport report) throws HeadlessException {
        super();
        this.report = report;
        initComponents();
    }

    /**
     * Creates a new ReportDialog instance for the specified report with the
     * specified owner for the dialog.
     *
     * @param owner the parent dialog
     * @param report the report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(Dialog owner, ProcessReport report)
        throws HeadlessException {
        super(owner);
        this.report = report;
        initComponents();
    }

    /**
     * Creates a new ReportDialog instance for the specified report with the
     * specified owner for the dialog.
     *
     * @param owner the parent frame
     * @param report the report to show
     *
     * @throws HeadlessException
     */
    public ReportDialog(Frame owner, ProcessReport report)
        throws HeadlessException {
        super(owner);
        this.report = report;
        initComponents();
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("ProcessReport"));
        getContentPane().setLayout(new BorderLayout(2, 2));

        JPanel content = new JPanel(new BorderLayout(4, 4));

        if (report != null) {
            if ((report.getName() != null) && (report.getName().length() > 0)) {
                content.setBorder(new TitledBorder(report.getName()));
            } else {
                content.setBorder(new TitledBorder(ElanLocale.getString(
                            "ProcessReport")));
            }

            if (report instanceof SimpleReport) {
                JTextArea area = new JTextArea(report.getReportAsString());
                area.setLineWrap(false);

                JScrollPane pane = new JScrollPane(area);
                Dimension dim = new Dimension(400, 300);
                pane.setPreferredSize(dim);
                pane.setMinimumSize(dim);
                content.add(pane);
            } else {
                // the same at this moment, change if e.g. a logrecord based report is available
                JTextArea area = new JTextArea(report.getReportAsString());
                area.setLineWrap(false);

                JScrollPane pane = new JScrollPane(area);
                Dimension dim = new Dimension(400, 300);
                pane.setPreferredSize(dim);
                pane.setMinimumSize(dim);
                content.add(pane);
            }
        } else {
            JLabel mes = new JLabel(ElanLocale.getString(
                        "ProcessReport.NoReport"));
            mes.setPreferredSize(new Dimension(200, 80));
            content.add(mes);
        }

        getContentPane().add(content);
        pack();

        if (this.getParent() != null) {
            setLocationRelativeTo(this.getParent());
        }
    }
}
