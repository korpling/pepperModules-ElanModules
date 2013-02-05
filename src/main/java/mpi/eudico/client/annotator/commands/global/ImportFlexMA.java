package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.gui.ImportFLExDialog;
import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexDecoderInfo;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * Action that starts an Import FLEx sequence.
 *
 * @author Han Sloetjes, MPI
 */
public class ImportFlexMA extends FrameMenuAction {
    /**
     * Creates a new ImportFlexMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportFlexMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import FLEx dialog and creates a new transcription.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        ImportFLExDialog dialog = new ImportFLExDialog(frame);
        Object value = dialog.getDecoderInfo();
        dialog.dispose();//??
        
        if (value == null) {
            return;
        }

        DecoderInfo decInfo = (DecoderInfo) value;

        if (decInfo.getSourceFilePath() == null) {
            return;
        }

        try {
            //long before = System.currentTimeMillis();
            //Transcription transcription = new TranscriptionImpl(new File(path).getAbsolutePath());
            String path = decInfo.getSourceFilePath();
            // aug 2009: check for existing eaf and extract time values
            checkTimeInfoEAF((FlexDecoderInfo) decInfo);
            Transcription transcription = new TranscriptionImpl(path, decInfo);

            //long after = System.currentTimeMillis();
            //System.out.println("open eaf took " + (after - before) + "ms");
            transcription.setChanged();

            int lastSlash = path.lastIndexOf('/');
            String flexPath = path.substring(0, lastSlash);
            boolean validMedia = true;

            if (frame != null) {
                validMedia = frame.checkMedia(transcription, flexPath);
            }

            if (!validMedia) {
                // ask if no media session is ok, if not return
                int answer = JOptionPane.showConfirmDialog(frame,
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaQuestion"),
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaAvailable"),
                        JOptionPane.YES_NO_OPTION);

                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            FrameManager.getInstance().createFrame(transcription);
        } catch (Exception e) {
        	ClientLogger.LOG.warning("Could not convert the FLEx file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tries to extract time alignment from a previous import, assuming that the existing eaf has the same name
     * (without extension) as the FLEx file and resides in the same folder. Only annotations with id "a_<guid>"
     * are stored.
     *  
     * @param decInfo the decoder info object
     */
    private void checkTimeInfoEAF(FlexDecoderInfo decInfo) {
    	File eafFile = new File(decInfo.getSourceFilePath().substring(0, decInfo.getSourceFilePath().lastIndexOf('.')) + ".eaf");
    	
    	if (!eafFile.exists()) {
    		eafFile = new File(decInfo.getSourceFilePath().substring(0, decInfo.getSourceFilePath().lastIndexOf('.')) + ".EAF");
    	}
    	
    	if (!eafFile.exists()) {
    		return;
    	}
    	
    	try {
    		TranscriptionImpl trans = new TranscriptionImpl(eafFile.getAbsolutePath());
    		HashMap<String, long[]> alignment = new HashMap<String, long[]>();
    		
    		TierImpl tier;
    		AlignableAnnotation aa;
    		int numToplevel = 0;
    		ArrayList<TierImpl> tiers = new ArrayList<TierImpl>(trans.getTiers());
    		for (int i = 0; i < tiers.size(); i++) {
    			tier = tiers.get(i);
    			if (tier.getParentTier() == null) {
    				numToplevel++;
    			}
    		}
    		
    		if (numToplevel == 1) {
        		for (int i = 0; i < tiers.size(); i++) {
        			tier = tiers.get(i);
        			if (tier.getParentTier() == null) {
        				List anns = tier.getAnnotations();
        				if (anns.size() == 1) {
        					// assume / test this is the "interlinear" top tier, without guid 
        					aa = (AlignableAnnotation) anns.get(0);
        					if (aa.getId() != null && aa.getId().startsWith(FlexConstants.FLEX_GUID_ANN_PREFIX)) {
        						alignment.put(aa.getId().substring(FlexConstants.FLEX_GUID_ANN_PREFIX.length()), 
        								new long[]{aa.getBeginTimeBoundary(), aa.getEndTimeBoundary()});
        					} else {
        						alignment.put("1", new long[]{aa.getBeginTimeBoundary(), aa.getEndTimeBoundary()});
        					}
        				}
        				break;
        			}
        		}
    		}
    		// now add all a_guid annotations
    		for (int i = 0; i < tiers.size(); i++) {
    			tier = tiers.get(i);
    			if (tier.isTimeAlignable()) {
    				List anns = tier.getAnnotations();
    				for (int j = 0; j < anns.size(); j++) {
    					aa = (AlignableAnnotation) anns.get(j);
    					if (aa.getId() != null && aa.getId().startsWith(FlexConstants.FLEX_GUID_ANN_PREFIX)) {//processes "a_1" again if it existed
    						alignment.put(aa.getId().substring(FlexConstants.FLEX_GUID_ANN_PREFIX.length()), 
    								new long[]{aa.getBeginTimeBoundary(), aa.getEndTimeBoundary()});
    					}
    				}
    			}
    		}
    		
    		decInfo.setStoredAlignment(alignment);
    		
    	} catch (Exception ex) {// any exception!
    		ClientLogger.LOG.warning("Could not load the existing eaf file: " + ex.getMessage());
    	}
    }
}
