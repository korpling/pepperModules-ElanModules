package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * CommandAction to manually set the video standard (of the media file) to PAL.<br>
 * This only influences the number of milliseconds per frame for Elan.<br>
 * PAL has 25 frames per second, resulting in 1000 / 25 = 40 milliseconds per
 * frame. See <a
 * href="http://archive.ncsa.uiuc.edu/SCMS/training/general/details/pal.html">NCSA
 * web site</a>.
 *
 * @author Han Sloetjes
 */
public class SetPALCA extends CommandAction {
    // the number of ms per frame

    /** Holds value of property DOCUMENT ME! */
    private final Object[] args = new Object[] { new Long(40L) };

    /**
     * Creates a new SetPALCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SetPALCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_PAL);
    }

    /**
     * DOCUMENT ME!
     */
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_PAL);
    }

    /**
     * The receiver of this CommandAction is an ElanMediaPlayer.
     *
     * @return DOCUMENT ME!
     */
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Object[] getArguments() {
        return args;
    }
}
