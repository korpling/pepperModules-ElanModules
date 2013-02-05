package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.util.TimeFormatter;

/**
 * DOCUMENT ME!
 * 
 * A command action for copying the current time to the pasteboard.
 * 
 * @author $Aarthy Somsundaram$
 * @version $Dec 2010$
 */

public class CopyCurrentTimeToPasteBoardCommand implements Command {
    private String commandName;
    
    private static String HH_MM_SS_MS = ElanLocale.getString("TimeCodeFormat.Hours");
    private static String SS_MS = ElanLocale.getString("TimeCodeFormat.Seconds");
    private static String MS = ElanLocale.getString("TimeCodeFormat.MilliSec");
    private static String NTSC = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC");   
    private static String PAL = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL");   

    /**
     * Creates a new CopyCurrentTimeToPasteBoardCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public CopyCurrentTimeToPasteBoardCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
    	// receiver is master ElanMediaPlayerController
        // arguments[0] is ElanMediaPlayer       
        
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            return;
        }
        String timeFormat = null;
        String currentTime = null;
        Object val = Preferences.get("CurrentTime.Copy.TimeFormat",null);
        if(val instanceof String){
        	timeFormat = val.toString();        	
        	if(timeFormat.equals(HH_MM_SS_MS)){
            	currentTime = TimeFormatter.toString(player.getMediaTime());
            } else if(timeFormat.equals(SS_MS)){
            	currentTime = TimeFormatter.toSSMSString(player.getMediaTime());
            }else if(timeFormat.equals(NTSC)){
            	currentTime = TimeFormatter.toTimecodeNTSC(player.getMediaTime());
            }else if(timeFormat.equals(PAL)){
            	currentTime = TimeFormatter.toTimecodePAL(player.getMediaTime());
            }   else {
            	currentTime = Long.toString(player.getMediaTime());
            }
        } else {
        	currentTime = Long.toString(player.getMediaTime());
        }
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection strSel = new StringSelection(currentTime);
		clipboard.setContents(strSel, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return commandName;
    }
}
