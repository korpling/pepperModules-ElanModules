package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: GoToEndCommand.java 4129 2005-08-03 15:01:06Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class GoToEndCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;

    /**
     * Creates a new GoToEndCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public GoToEndCommand(String theName) {
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
        // arguments[0] is
        player = (ElanMediaPlayer) receiver;

        if (player == null) {
            return;
        }

        player.setMediaTime(player.getMediaDuration());
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
