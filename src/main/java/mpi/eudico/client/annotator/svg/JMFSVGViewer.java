package mpi.eudico.client.annotator.svg;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.player.JMFGraphicMediaPlayer;

import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import java.util.Collections;
import java.util.List;

import javax.media.renderer.VideoRenderer;


/**
 * Attempt to implement a graphics viewer as a codec/effect/renderer to the
 * media.
 *
 * @author Han Sloetjes
 */
public class JMFSVGViewer extends AbstractSVGViewer {

    /**
     * Creates a new JMFSVGViewer instance
     *
     * @param transcription the transcription
     */
    public JMFSVGViewer(Transcription transcription) {
        super(transcription);
    }

    /**
	 * This viewer calls getMediaTime every time paintAnnotations is called by 
	 * the JMG video renderer. This should garantee consistent synchronization 
	 * with the Elan world.
     *
     * @param big2d the video renderer's graphics
     */
    public void paintAnnotations(Graphics2D big2d) {
        long time = getMediaTime();
        big2d.setStroke(new BasicStroke(
                (float) (1 / big2d.getTransform().getScaleX())));

        for (int i = 0; i < allGraphicTiers.size(); i++) {
            GraphicTier2D tier2d = (GraphicTier2D) allGraphicTiers.get(i);

            if (tier2d.isVisible()) {
				GraphicNode2D node2d = null;
				int index = tier2d.getCurrentIndex();
            	if (index > -1) {
            		node2d = (GraphicNode2D) tier2d.getNodeList().get(index);
            		if ( !(node2d.getAnnotation().getBeginTimeBoundary() <= time && 
            				node2d.getAnnotation().getEndTimeBoundary() > time) ) {
            			index = -1;
            		}
            	} 
            	if (index == -1) {
					index = Collections.binarySearch(tier2d.getNodeList(),
													new Long(time));
					if (index >= 0) {
						node2d = (GraphicNode2D) tier2d.getNodeList().get(index);
						tier2d.setCurrentIndex(index);		
					} else {
						node2d = null;
						tier2d.setCurrentIndex(-1);
					}
            	}
                

                if (node2d != null) {
                    big2d.setColor(STROKE_COLOR);
                    node2d.paintShape(big2d, true);

                    //
                    if (node2d.getAnnotation() == getActiveAnnotation()) {
                        big2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);

                        //big2d.setStroke(new BasicStroke((float)(1 / big2d.getTransform().getScaleX())));
                        node2d.paintActiveMarker(big2d);
                    }
                }
            }
        }
    }
    
    /**
     * Stub; ignored by this viewer.
     */
	public void paintAnnotations() {
	}

    /**
     * Provokes the renderer of the video to redraw itself.
     */
    protected void requestRepaint() {
        if (getViewerManager().getMasterMediaPlayer() instanceof JMFGraphicMediaPlayer) {
            VideoRenderer renderer = ((JMFGraphicMediaPlayer) (getViewerManager()
                                                                   .getMasterMediaPlayer())).getRenderer();

            if ((renderer != null) &&
                    (renderer instanceof JMFGraphicVideoRenderer)) {
                ((JMFGraphicVideoRenderer) renderer).repaint();
            }
        }
    }

	public void setVisibleTiers(List tiers) {
		// TODO Auto-generated method stub
		
	}
}
