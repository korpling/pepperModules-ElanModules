package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.util.Tag2D;
import mpi.eudico.client.annotator.util.Tier2D;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import mpi.eudico.util.TimeInterval;
import mpi.eudico.util.TimeFormatter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingConstants;
import javax.swing.UIManager;


/**
 * A viewer that shows only one tier, its annotations and new segmentations
 * that  have not yet been committed to the transcription. It is a kind of
 * TimeLineViewer-Light.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
public class SegmentationViewer extends AbstractViewer
    implements SingleTierViewer, MouseListener, MouseMotionListener,
        ComponentListener {
    private Transcription transcription;
    private Font font;
    private FontMetrics metrics;
    private int rulerHeight;
    private TimeRuler ruler;
    private BufferedImage bi;
    private Graphics2D big2d;
    //private BasicStroke stroke;

    //private BasicStroke stroke2;
    private int msPerPixel;
    private int imageWidth;
    private int imageHeight;
    private long crossHairTime;
    private int crossHairPos;
    private long intervalBeginTime;
    private long intervalEndTime;
    //private long dragStartTime;
    private Point dragStartPoint;
    private Point dragEndPoint;
    private int pixelsForTierHeight;
    private int pixelsForTierHeightMargin;

    /** margin offset when the player is not playing */
    public final int SCROLL_OFFSET = 16;
    private boolean panMode;

    // data
    private TierImpl tier;
    private Tier2D tier2d;
    private Tier2D segments2d;
    //private ArrayList segments;
    private long currentBeginTime = -1L;
    /**
     * Creates an instance of SegmentationViewer.
     *
     * @param transcription the transcription
     */
    public SegmentationViewer(Transcription transcription) {
        this.transcription = transcription;
        initViewer();

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setOpaque(true);

        paintBuffer();
    }

    /**
     * Performs the initialization of fields and sets up the viewer.<br>
     */
    private void initViewer() {
        font = Constants.DEFAULTFONT;
        setFont(font);
        metrics = getFontMetrics(font);
        ruler = new TimeRuler(font, TimeFormatter.toString(0));
        rulerHeight = ruler.getHeight();
        //stroke = new BasicStroke();

        //stroke2 = new BasicStroke(2.0f);
        msPerPixel = 10;
        crossHairTime = 0L;
        crossHairPos = 0;
        //dragStartTime = 0;
        imageWidth = 0;
        imageHeight = 0;
        pixelsForTierHeight = font.getSize() * 3; //hardcoded for now
        pixelsForTierHeightMargin = 2;
    }

    /**
     * Override <code>JComponent</code>'s paintComponent to paint:<br>
     * - a BufferedImage with a ruler and the tags<br>
     * - the cursor / crosshair
     *
     * @param g the graphics object
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        int h = getHeight();

        if (bi != null) {
            g2d.drawImage(bi, 0, 0, this);
        }

        if ((crossHairPos >= 0) && (crossHairPos <= imageWidth)) {
            // prevents drawing outside the component on Mac
            g2d.setColor(Constants.CROSSHAIRCOLOR);
            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
        }
    }

    /**
     * Paint to a buffer.<br>
     * First paint the top ruler, next the annotations of the current tier.
     */
    private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if (imageWidth != getWidth()) {
            imageWidth = getWidth();
        }

        if (imageHeight != getHeight()) {
            imageHeight = getHeight();
        }

        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);

        if ((bi == null) || (bi.getWidth() < imageWidth) ||
                (bi.getHeight() < imageHeight)) {
            bi = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            big2d = bi.createGraphics();
        }

        if (SystemReporting.antiAliasedText) {
	        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        big2d.fillRect(0, 0, imageWidth, bi.getHeight());

        // mark the area beyond the media time
        if (intervalEndTime > getMediaDuration()) {
            int xx = xAt(getMediaDuration());
            if (!SystemReporting.isMacOS()) {
            	big2d.setColor(UIManager.getColor("Panel.background"));// problems on the mac
            } else {
            	big2d.setColor(Color.LIGHT_GRAY);
            }
            big2d.fillRect(xx, 0, imageWidth - xx, bi.getHeight());
        }

        /*paint time ruler */
        big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
        big2d.translate(-(intervalBeginTime / msPerPixel), 0.0);
        ruler.paint(big2d, intervalBeginTime, imageWidth, msPerPixel,
            SwingConstants.TOP);
        big2d.setFont(font);

        int height = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
        int y = rulerHeight + pixelsForTierHeightMargin;
        int x;
        int width;
        Tag2D tag2d;

        if (tier2d != null) {
            Iterator tagIt = tier2d.getTags();

            while (tagIt.hasNext()) {
                tag2d = (Tag2D) tagIt.next();

                if (tag2d.getEndTime() < intervalBeginTime) {
                    continue; //don't paint
                } else if (tag2d.getBeginTime() > intervalEndTime) {
                    break; // stop looping this tier
                }

                x = tag2d.getX();
                width = tag2d.getWidth();
                big2d.drawLine(x, y, x, y + height);
                big2d.drawLine(x, y + (height / 2), x + width, y +
                    (height / 2));
                big2d.drawLine(x + width, y, x + width, y + height);

                int descent = big2d.getFontMetrics().getDescent();
                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
                    (float) (y + ((height / 2) - descent + 1)));
            }
        }

        if (segments2d != null) {
            big2d.setColor(Constants.SHAREDCOLOR3);

            Iterator tagIt = segments2d.getTags();
            int intBeginPos = timeToPixels(intervalBeginTime);
            int intEndPos = timeToPixels(intervalEndTime);

            while (tagIt.hasNext()) {
                tag2d = (Tag2D) tagIt.next();
                x = tag2d.getX();
                width = tag2d.getWidth();

                if ((x + width) < intBeginPos) {
                    continue; //don't paint
                } else if (x > intEndPos) {
                    break; // stop looping this tier
                }

                big2d.drawLine(x, y, x, y + height);
                big2d.drawLine(x, y + (height / 2), x + width, y +
                    (height / 2));
                big2d.drawLine(x + width, y, x + width, y + height);
                
                int descent = big2d.getFontMetrics().getDescent();
                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
                    (float) (y + ((height / 2) - descent + 1)));
            }
        }
        
        if (currentBeginTime > -1) {
        	big2d.setColor(Constants.SHAREDCOLOR3);
        	x = timeToPixels(currentBeginTime);
        	big2d.drawLine(x, y, x, y + height);
        }       
        
        // end paint tags
        big2d.setTransform(new AffineTransform());

        repaint();
    }

    /**
     * Create a truncated String of a tag's value to display in the viewer.
     *
     * @param string the tag's value
     * @param width the available width for the String
     * @param fMetrics the font metrics
     *
     * @return the truncated String
     */
    private String truncateString(String string, int width, FontMetrics fMetrics) {
        String line = string.replace('\n', ' ');

        if (fMetrics != null) {
            int stringWidth = fMetrics.stringWidth(line);

            if (stringWidth > (width - 4)) { // truncate

                int i = 0;
                String s = "";
                int size = line.length();

                while (i < size) {
                    if (fMetrics.stringWidth(s) > (width - 4)) {
                        break;
                    } else {
                        s = s + line.charAt(i++);
                    }
                }

                if (!s.equals("")) {
                    line = s.substring(0, s.length() - 1);
                } else {
                    line = s;
                }
            }
        }

        return line;
    }

    /**
     * Changes the interval begin time locally.
     *
     * @param begin the new local interval begin time
     */
    private void setIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }

        if (playerIsPlaying()) {
            intervalBeginTime = begin;
            intervalEndTime = intervalBeginTime + (imageWidth * msPerPixel);

            crossHairPos = xAt(crossHairTime);
        } else {
            if (!panMode) {
                intervalBeginTime = begin - (SCROLL_OFFSET * msPerPixel);
            } else {
                intervalBeginTime = begin;
            }

            if (intervalBeginTime < 0) {
                intervalBeginTime = 0;
            }

            intervalEndTime = intervalBeginTime + (imageWidth * msPerPixel);

            crossHairPos = xAt(crossHairTime);
        }

        paintBuffer();
    }

    /**
     * @see mpi.eudico.client.annotator.AbstractViewer#controllerUpdate(mpi.eudico.client.annotator.ControllerEvent)
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

            //System.out.println("SV time: " + crossHairTime);
            if ((crossHairTime < intervalBeginTime) ||
                    (crossHairTime >= intervalEndTime)) {
                setIntervalBeginTime(crossHairTime);
            } else if (playerIsPlaying()) {
                long intervalMidTime = (intervalBeginTime + intervalEndTime) / 2;

                if (crossHairTime > (intervalMidTime + (1 * msPerPixel))) {
                    setIntervalBeginTime(intervalBeginTime +
                        (crossHairTime - intervalMidTime));
                } else if (crossHairTime < intervalMidTime) {
                    int oldPos = crossHairPos;
                    crossHairPos = xAt(crossHairTime);

                    if (crossHairPos >= oldPos) {
                        repaint(oldPos - 2, 0, crossHairPos - oldPos + 4,
                            getHeight());
                    } else {
                        repaint(crossHairPos - 2, 0, oldPos - crossHairPos + 4,
                            getHeight());
                    }
                } else {
                    repaint();
                }
            } else {
                int oldPos = crossHairPos;
                crossHairPos = xAt(crossHairTime);

                if (crossHairPos >= oldPos) {
                    repaint(oldPos - 2, 0, crossHairPos - oldPos + 4,
                        getHeight());
                } else {
                    repaint(crossHairPos - 2, 0, oldPos - crossHairPos + 4,
                        getHeight());
                }
            }
        }
    }

    /**
     * Stub.
     *
     * @see mpi.eudico.client.annotator.AbstractViewer#updateSelection()
     */
    public void updateSelection() {
    }

    /**
     * Stub.
     *
     * @see mpi.eudico.client.annotator.AbstractViewer#updateActiveAnnotation()
     */
    public void updateActiveAnnotation() {
    }

    /**
     * Stub.
     *
     * @see mpi.eudico.client.annotator.AbstractViewer#updateLocale()
     */
    public void updateLocale() {
    }

    /**
     * Sets the tier for this viewer.
     *
     * @param tier the tier
     *
     * @see mpi.eudico.client.annotator.SingleTierViewer#setTier(mpi.eudico.server.corpora.clom.Tier)
     */
    public void setTier(Tier tier) {
        if (tier instanceof TierImpl) {
            this.tier = (TierImpl) tier;
            //segments = new ArrayList();
        } else {
            this.tier = null;
        }

        createTier2D(this.tier);

        paintBuffer();
    }

    /**
     * @see mpi.eudico.client.annotator.SingleTierViewer#getTier()
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Restore position etc.
     */
	public void preferencesChanged() {
		// TODO Auto-generated method stub
		
	}
	
    /**
     * Adds a segment to the list of segmentations.
     *
     * @param ti the new time interval / segment
     */
    public void addSegment(TimeInterval ti) {
        if ((ti == null) || (ti.getDuration() <= 0)) {
            return;
        }

        Tag2D tag2d = new Tag2D(null);
        int x = timeToPixels(ti.getBeginTime());
        int w = timeToPixels(ti.getEndTime()) - x;
        tag2d.setX(x);
        tag2d.setWidth(w);
        
        if (ti instanceof AnnotationDataRecord) {
        	if (((AnnotationDataRecord) ti).getValue() != null) {
        		tag2d.setTruncatedValue(truncateString(((AnnotationDataRecord) ti).getValue(), w, metrics));
        	}
        }
        // reset the current "first stroke" begin time
        currentBeginTime = -1L;
        
        if (segments2d != null) {
            segments2d.insertTag(tag2d);
            paintBuffer();
        }
    }

    /**
     * Sets the current begin time for painting an indicator of the position 
     * of the first stroke of a segment.
     * 
     * @param time the current begin time
     */
    public void setCurrentBeginTime(long time) {
    	currentBeginTime = time;
    	paintBuffer();
    }
    
    /**
     * Creates a Tier2D with 2D tags for all annotations.
     *
     * @param tier the tier
     */
    private void createTier2D(TierImpl tier) {
        if (tier == null) {
            tier2d = null;
            segments2d = null;
        }

        tier2d = new Tier2D(tier);
        segments2d = new Tier2D(tier);

        Tag2D tag2d;
        int xPos;
        int tagWidth;

        Iterator annotIter = tier.getAnnotations().iterator();

        while (annotIter.hasNext()) {
            Annotation a = (Annotation) annotIter.next();

            //System.out.println("Annotation: " + a);
            tag2d = new Tag2D(a);
            xPos = timeToPixels(a.getBeginTimeBoundary());
            tag2d.setX(xPos);
            tagWidth = timeToPixels(a.getEndTimeBoundary()) - xPos;
            tag2d.setWidth(tagWidth);
            tag2d.setTruncatedValue(truncateString(a.getValue(), tagWidth,
                    metrics));
            tier2d.addTag(tag2d);
        }
    }

    /**
     * Returns the x-ccordinate for a specific time. The coordinate is in the
     * component's coordinate system.
     *
     * @param t time
     *
     * @return int the x-coordinate for the specified time
     */
    public int xAt(long t) {
        return (int) ((t - intervalBeginTime) / msPerPixel);
    }

    /**
     * Calculates the x coordinate in virtual image space.<br>
     * This virtual image would be an image of width <br>
     * media duration in ms / ms per pixel. Therefore the return value does
     * not correct for interval begin time and is not necessarily within the
     * bounds of this component.
     *
     * @param theTime the media time
     *
     * @return the x coordinate in the virtual image space
     */
    private int timeToPixels(long theTime) {
        return (int) theTime / msPerPixel;
    }

    /**
     * Returns the time in ms at a given position in the current image. The
     * given x coordinate is in the component's ("this") coordinate system.
     * The interval begin time is included in the calculation of the time at
     * the given coordinate.
     *
     * @param x x-coordinate
     *
     * @return the mediatime corresponding to the specified position
     */
    public long timeAt(int x) {
        return intervalBeginTime + (x * msPerPixel);
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        setMediaTime(timeAt(e.getPoint().x));
    }

    /**
     * Stub.
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Stub.
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * If the alt key is down the view can be "panned".
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (e.isAltDown()) {
            if (playerIsPlaying()) {
                stopPlayer();
            }

            dragStartPoint = e.getPoint();
            //dragStartTime = timeAt(dragStartPoint.x);

            panMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        if (panMode) {
            panMode = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Handles panning when the alt key is down.
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        //panning
        if (panMode) {
            dragEndPoint = e.getPoint();

            int scrolldiff = dragEndPoint.x - dragStartPoint.x;

            // some other viewer may have a media offset...
            long newTime = intervalBeginTime - (scrolldiff * msPerPixel);

            if ((intervalBeginTime < 0) && (newTime < intervalBeginTime)) {
                newTime = intervalBeginTime;
            }

            setIntervalBeginTime(newTime);
            dragStartPoint = dragEndPoint;
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Stub.
     *
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Stub.
     *
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
        paintBuffer();
    }

    /**
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void componentShown(ComponentEvent e) {
        paintBuffer();
    }

}
