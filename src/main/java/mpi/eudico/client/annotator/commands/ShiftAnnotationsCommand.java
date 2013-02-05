package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.server.corpora.event.IllegalEditException;

import javax.swing.JOptionPane;


/**
 * Command that shifts all annotations on a specified root tier that are
 * completely inside a specified time interval. The user has been prompted to
 * specify the amount of ms to shift the annotations, to the left or to the
 * right. All depending annotations are shifted as well.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class ShiftAnnotationsCommand implements UndoableCommand {
    String commandName;

    // receiver; the transcription 
    TranscriptionImpl transcription;
    private TierImpl tier;
    Long bt;
    Long et;
    Long shiftValue;

    /**
     * Creates a new ShiftAnnotationsCommand instance
     *
     * @param name the name of the command
     */
    public ShiftAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Shift the annotations back.
     */
    public void redo() {
    	if (transcription != null) {
    		transcription.setNotifying(false);
    		
    		shift(tier, shiftValue, bt, et);
    		
    		transcription.setNotifying(true);
    	}
    }

    /**
     * Shift the annotations again.
     */
    public void undo() {
    	if (transcription != null) {
    		transcription.setNotifying(false);
    		
	        shift(tier, -shiftValue, (bt == 0 ? bt : (bt + shiftValue)),
	            ((et == Long.MAX_VALUE) ? et : (et + shiftValue)));
	        
	        transcription.setNotifying(true);
    	}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the (active) tier
     *        (TierImpl)</li> <li>arg[1] = the begin of the time interval the
     *        annotations in which are  to be shifted (Long)</li> <li>arg[2] =
     *        the end time of that interval (Long)</li> <li>arg[3] = the shift
     *        value (Long)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if (arguments != null) {
            tier = (TierImpl) arguments[0];
            bt = (Long) arguments[1];
            et = (Long) arguments[2];
            shiftValue = (Long) arguments[3];

            transcription.setNotifying(false);
            
            shift(tier, shiftValue, bt, et);
            
            transcription.setNotifying(true);
        }
    }

    void shift(TierImpl tier, long shiftValue, long bt, long et) {
        if (tier != null) {
        	
            try {
                tier.shiftAnnotations(shiftValue, bt, et);
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn5") + " " +
                    iae.getMessage(), ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                iae.printStackTrace();
            } catch (IllegalEditException iee) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(
                        transcription),
                    ElanLocale.getString("ShiftAllDialog.Warn5") + " " +
                    iee.getMessage(), ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                iee.printStackTrace();
            }
                       
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name
     */
    public String getName() {
        return commandName;
    }
}
