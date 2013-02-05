package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

public class CreateAudioRecognizerMA extends FrameMenuAction {	
    /**
     * Creates a new CreateAudioRecognizerMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public CreateAudioRecognizerMA(String name, ElanFrame2 frame) {
        super(name, frame);        
    }

    /**
     * Sets the preference setting when changed 
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
    	boolean value;
    	if( e.getSource() instanceof JCheckBoxMenuItem){    
    		value = ((JCheckBoxMenuItem)e.getSource()).isSelected();
    		Preferences.set(commandId, value , null, false);
    		if(frame.isIntialized()){
    			frame.getLayoutManager().updateViewer(ELANCommandFactory.AUDIO_RECOGNIZER , value);
    			if(!value){
    				frame.getViewerManager().destroyPanel(ELANCommandFactory.AUDIO_RECOGNIZER);
    			}
    		}
    	}       
    }
}


