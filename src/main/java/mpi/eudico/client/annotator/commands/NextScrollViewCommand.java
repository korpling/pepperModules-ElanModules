package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: NextScrollViewCommand.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class NextScrollViewCommand implements Command {
    private String commandName;

    /**
     * Creates a new NextScrollViewCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public NextScrollViewCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            long duration = ts.getIntervalDuration();
            ts.setBeginTime(ts.getBeginTime() + duration);
            ts.setEndTime(ts.getBeginTime() + duration);

            if ((((ElanMediaPlayer) receiver).getMediaTime() + duration) > ((ElanMediaPlayer) receiver).getMediaDuration()) {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaDuration());
            } else {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() +
                    ts.getIntervalDuration());
            }
        }
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
