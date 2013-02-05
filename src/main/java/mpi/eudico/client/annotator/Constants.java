package mpi.eudico.client.annotator;

import java.awt.Color;
import java.awt.Font;


/**
 * DOCUMENT ME!
 * $Id: Constants.java 31956 2012-07-13 06:36:32Z keeloo $
 * @author $Author$
 * @version $Revision$
 */
public class Constants {
    /** Holds value of property DOCUMENT ME! */
    public static String USERHOME = System.getProperty("user.home");

    /** Holds value of property DOCUMENT ME! */
    public static String FILESEPARATOR = System.getProperty("file.separator");

    /** Holds value of property DOCUMENT ME! */
    public static String STRPROPERTIESFILE = USERHOME + FILESEPARATOR +
        ".elan.config";

    /** Holds value of property DOCUMENT ME! */
    public static String ELAN_DATA_DIR = USERHOME + FILESEPARATOR +
        ".elan_data";

    /** Holds value of property DOCUMENT ME! */
    public static Color DEFAULTBACKGROUNDCOLOR = new Color(230, 230, 230);

    /** Holds value of property DOCUMENT ME! */
    public static Color DEFAULTFOREGROUNDCOLOR = Color.black;

    /** Holds value of property DOCUMENT ME! */
    public static Color SELECTIONCOLOR = new Color(204, 204, 255);

    /** Holds value of property DOCUMENT ME! */
    public static Color CROSSHAIRCOLOR = Color.red;
    
    /** ALBERT */
    public static Color SEGMENTATIONCOLOR = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color ACTIVEANNOTATIONCOLOR = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR = Color.gray;

    /** Holds value of property DOCUMENT ME! */
    public static Color MEDIAPLAYERCONTROLSLIDERCROSSHAIRCOLOR = Color.red.darker();

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALSTEREOBLENDEDCOLOR1 = Color.green;

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALSTEREOBLENDEDCOLOR2 = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALCHANNELCOLOR = new Color(224, 224, 224);

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR1 = Color.orange;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR2 = Color.yellow;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR3 = Color.gray;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR4 = Color.white;
    
    public static Color SHAREDCOLOR5 = new Color(128, 0, 128);
    public static Color SHAREDCOLOR6 = Color.DARK_GRAY;

    /** Holds value of property DOCUMENT ME! */
    public static Color ACTIVETIERCOLOR = new Color(230, 210, 210);
    
    public static Color LIGHTBACKGROUNDCOLOR = new Color(240, 240, 240);

    /** Holds value of property DOCUMENT ME! */
    public static Font DEFAULTFONT = new Font("Arial Unicode MS", Font.PLAIN, 12);

    /** Holds value of property DOCUMENT ME! */
    public static Font SMALLFONT = new Font("Arial Unicode MS", Font.PLAIN, 10);

    /** Holds value of property DOCUMENT ME! */
    public static final int SCROLLMIN = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int SCROLLINCREMENT = 1;
    
    public static final int COMBOBOX_VISIBLE_ROWS = 20;
    
    public static final int VISIBLE_MENUITEMS = 20;
    
    public static final int MAX_VISIBLE_PLAYERS = 4;

    // backup interval constants

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_NEVER = new Integer(0);
    
    public static final Integer BACKUP_1 = new Integer(60000);
    
	/** Holds value of property DOCUMENT ME! */
	public static final Integer BACKUP_5 = new Integer(300000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_10 = new Integer(600000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_20 = new Integer(1200000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_30 = new Integer(1800000);
    
    /** font sizes available in several viewers */
    public static final int[] FONT_SIZES = new int[]{8, 9, 10, 12, 14, 16, 18, 24, 36, 42, 48, 60, 72};
    
    /** decimal digits*/
    public static final int ONE_DIGIT = 1;
    
    public static final int TWO_DIGIT = 2;
    
    public static final int THREE_DIGIT = 3;
    
    /** Holds value of property DOCUMENT ME! */
    public static String PrerefNoOfDecimalDigits = System.getProperty("file.separator");
    
    // time format constants
	/** the hour/minutes/seconds/milliseconds format */
	public static final int HHMMSSMS = 100;

	/** the seconds/milliseconds format */
	public static final int SSMS = 101;

	/** the pure milliseconds format */
	public static final int MS = 102;
	
	/** the frame number format */
	public static final int HHMMSSFF = 103;

	public static final String HHMMSSMS_STRING = "hh:mm:ss.ms";
	public static final String SSMS_STRING = "ss.ms";
	public static final String MS_STRING = "ms";
	public static final String HHMMSSFF_STRING = "hh:mm:ss:ff";
	public static final String PAL_STRING = "PAL";
	public static final String NTSC_STRING = "NTSC";
	
	public static final String ELAN_BEGIN_LABEL = "ELANBegin";
	public static final String ELAN_END_LABEL = "ELANEnd";
	public static final String ELAN_PARTICIPANT_LABEL = "ELANParticipant";
	
	/** HS May 2008: new location of ELAN data folder on the Mac. */
	static {
		if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
			ELAN_DATA_DIR = USERHOME + FILESEPARATOR + "Library" + FILESEPARATOR + "Preferences" + FILESEPARATOR + "ELAN";
		}
	}
	
	public static final String TEXTANDTIME_STRING = "annotation + begintime + endtime";
	public static final String TEXT_STRING = "annotation only";
	public static final String URL_STRING = "filepath + tier name + begintime + endtime";

}
