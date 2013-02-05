package mpi.eudico.client.annotator.export.multiplefiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTGEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTextGridEncoder;

/**
 * Final Step 3 
 * Actual export id done here.
 * The ui is progess monitor
 * 
 * @author aarsom
 * @version Feb,2012
 *
 */
public class MultipleFileExportPraatStep3 extends AbstractMultiFileExportProgessStepPane{

	private boolean correctTimes;
	private String encoding;

	/**
	 * Constructor
	 * 
	 * @param multiPane
	 */
	public MultipleFileExportPraatStep3(MultiStepPane multiPane) {
		super(multiPane);	
	}

	/**
	 * Actual export
	 * 
	 */
	protected boolean doExport(TranscriptionImpl transImpl, String fileName) {      
        long begin = 0l;
        long end = transImpl.getLatestTime();
        
        long mediaOffset = 0L;

        if (correctTimes) {
            Vector mds = transImpl.getMediaDescriptors();

            if ((mds != null) && (mds.size() > 0)) {
                mediaOffset = ((MediaDescriptor) mds.get(0)).timeOrigin;
            }
        }
        
        PraatTGEncoderInfo encInfo = new PraatTGEncoderInfo(begin, end);
        encInfo.setEncoding(encoding);
        encInfo.setOffset(mediaOffset);
        encInfo.setExportSelection(false);
        
        List<String> selectedTiersInThisTrans = new ArrayList<String>();
        for(String tierName :selectedTiers){
        	if(transImpl.getTierWithId(tierName) != null){
        		selectedTiersInThisTrans.add(tierName);
        	}
        }
        
        PraatTextGridEncoder encoder = new PraatTextGridEncoder();
        try {
			encoder.encodeAndSave(transImpl, encInfo, selectedTiersInThisTrans, 
					fileName);
		} catch (IOException e) {			
			e.printStackTrace();
			return false;
		}
        
        return true;
	}
	
	 /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    public void enterStepForward() {
    	 correctTimes = (Boolean) multiPane.getStepProperty("CorrectTimes");
    	 encoding = (String)multiPane.getStepProperty("Encoding");    	
       super.enterStepForward();
    }
}
