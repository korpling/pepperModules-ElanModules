package mpi.eudico.client.annotator.svg;

/**
 * A class to store some graphical annotations related preferences.
 *
 * @author Han Sloetjes
 * @version 1.0 may-2004
 */
public class SVGPrefs {
    private static boolean useSVG = false;

    /**
     * Enables or disables the use of graphical annotations (SVG).
     *
     * @param enable if true the use of SVG is enabled
     */
    public static void setUseSVG(boolean enable) {
        useSVG = enable;
    }

    /**
     * True if the use of graphical annotations is enabled.
     *
     * @return true if the use of graphical annotations is enabled, false
     *         otherwise
     */
    public static boolean getUseSVG() {
        return useSVG;
    }
}
