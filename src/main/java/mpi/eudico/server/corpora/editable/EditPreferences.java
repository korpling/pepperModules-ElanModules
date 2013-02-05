package mpi.eudico.server.corpora.editable;

/**
 * DOCUMENT ME!
 * $Id: EditPreferences.java 2 2004-03-25 16:22:33Z wouthuij $
 * @author $Author$
 * @version $Revision$
 */
public class EditPreferences {
    /** Holds value of property DOCUMENT ME! */
    public final static String SERVER_NAME = "mpisun21.mpi.nl";

    /** Holds value of property DOCUMENT ME! */
    public final static String SERVER_PORT = "1098";

    /** Holds value of property DOCUMENT ME! */
    public static int LIVELINESS_CHECK_PERIOD = 5 * 1000; // 5 minutes?

    /** Holds value of property DOCUMENT ME! */
    public static int MAX_TIME_BETWEEN_SAVE_ACTIONS = 20 * 1000; // 2 hours?

    /** Holds value of property DOCUMENT ME! */
    public static int MAX_EDIT_MODE_CONFIRMATION_RESPONSE_TIME = 10 * 1000; // 30 minutes?
}
