package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.InterlinearPrintable;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;


/**
 * Command for interlinear printing.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 */
public class PrintCommand implements Command {
    /** the PrinterJob object */
    public static PrinterJob printJob = PrinterJob.getPrinterJob();

    /** the PageFormat object */
    public static PageFormat pageFormat = printJob.defaultPage();

    // check for preferred paper format etc.
    static {
        double w;
        double h;
        double imgX;
        double imgY;
        double imgH;
        double imgW;
        int orient = pageFormat.getOrientation();

        Object curPref;
        curPref = Preferences.get("PageFormat.Height", null);

        if (curPref instanceof Double) {
            h = ((Double) curPref).doubleValue();
        } else {
            h = pageFormat.getHeight();
        }

        curPref = Preferences.get("PageFormat.Width", null);

        if (curPref instanceof Double) {
            w = ((Double) curPref).doubleValue();
        } else {
            w = pageFormat.getWidth();
        }

        curPref = Preferences.get("PageFormat.ImgX", null);

        if (curPref instanceof Double) {
            imgX = ((Double) curPref).doubleValue();
        } else {
            imgX = pageFormat.getImageableX();
        }

        curPref = Preferences.get("PageFormat.ImgY", null);

        if (curPref instanceof Double) {
            imgY = ((Double) curPref).doubleValue();
        } else {
            imgY = pageFormat.getImageableY();
        }

        curPref = Preferences.get("PageFormat.ImgHeight", null);

        if (curPref instanceof Double) {
            imgH = ((Double) curPref).doubleValue();
        } else {
            imgH = pageFormat.getImageableHeight();
        }

        curPref = Preferences.get("PageFormat.ImgWidth", null);

        if (curPref instanceof Double) {
            imgW = ((Double) curPref).doubleValue();
        } else {
            imgW = pageFormat.getImageableWidth();
        }

        curPref = Preferences.get("PageFormat.Orientation", null);

        if (curPref instanceof Integer) {
            orient = ((Integer) curPref).intValue();
        }

        Paper p = new Paper();
        p.setSize(w, h);
        p.setImageableArea(imgX, imgY, imgW, imgH);

        PageFormat pf = new PageFormat();
        pf.setOrientation(orient);
        pf.setPaper(p);

        pageFormat = pf;
    }

    private String commandName;

    /**
     * Creates a new PrintCommand instance
     *
     * @param name the name of the command
     */
    public PrintCommand(String name) {
        commandName = name;
    }

    /**
     * Shows a Print dialog and starts the printing process.
     *
     * @param receiver a Transcription object
     * @param arguments either null (printing withou preview, or argument[0] is
     *        an Interlinear object
     */
    public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;
        Interlinear interlinear;

        if (arguments == null) {
            interlinear = new Interlinear((TranscriptionImpl) tr);

            // init the interlinearizer
            interlinear.renderView(new BufferedImage(10, 10,
                    BufferedImage.TYPE_INT_RGB));
        } else {
            interlinear = (Interlinear) arguments[0];
        }

        InterlinearPrintable printable = new InterlinearPrintable(interlinear);
        printJob.setPrintable(printable, pageFormat);
        printJob.setPageable(printable);

        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        tr),
                    ElanLocale.getString(
                        "InterlinearizerOptionsDlg.Error.Print") + " \n" + "(" +
                    ex.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }
}
