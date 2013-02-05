package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ShiftAnnotationsLROfCommand extends ShiftAnnotationsCommand {
    
    
	public ShiftAnnotationsLROfCommand(String name) {
		super(name);
	}

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the begin of the time interval the
     *        annotations in which are  to be shifted (Long)</li> <li>arg[1] =
     *        the end time of that interval (Long)</li> <li>arg[2] = the shift
     *        value (Long)</li> </ul>
     */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;
		
        if (arguments != null) {
            bt = (Long) arguments[0];
            et = (Long) arguments[1];
            shiftValue = (Long) arguments[2];

            transcription.setNotifying(false);
            
            TierImpl tier = null;
            List rootTiers = transcription.getTopTiers();
            for (int i = 0; i < rootTiers.size(); i++) {
            	tier = (TierImpl) rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
            		shift(tier, shiftValue, bt, et);
            	}
            }
            
            transcription.setNotifying(true);
        }
	}

    /**
     * Shift the annotations again.
     */
	@Override
	public void redo() {
		if (transcription != null) {
			transcription.setNotifying(false);
			
            TierImpl tier = null;
            List rootTiers = transcription.getTopTiers();
            for (int i = 0; i < rootTiers.size(); i++) {
            	tier = (TierImpl) rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
            		shift(tier, shiftValue, bt, et);
            	}
            }
            
            transcription.setNotifying(true);
		}
	}

    /**
     * Shift the annotations back.
     */
	@Override
	public void undo() {
		if (transcription != null) {
			transcription.setNotifying(false);
			
            TierImpl tier = null;
            List rootTiers = transcription.getTopTiers();
            for (int i = 0; i < rootTiers.size(); i++) {
            	tier = (TierImpl) rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
                    shift(tier, -shiftValue, (bt == 0 ? bt : (bt + shiftValue)),
                            ((et == Long.MAX_VALUE) ? et : (et + shiftValue)));
            	}
            }
            
            transcription.setNotifying(true);
		}
	}
    
    
}
