package mpi.eudico.client.annotator.svg;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import quicktime.QTException;

import quicktime.qd.PixMap;
import quicktime.qd.QDColor;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;

import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;

import quicktime.std.image.GraphicsMode;
import quicktime.std.image.ImageDescription;
import quicktime.std.image.Matrix;

import quicktime.std.movies.Atom;
import quicktime.std.movies.AtomContainer;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;

import quicktime.std.movies.media.SpriteDescription;
import quicktime.std.movies.media.SpriteMedia;
import quicktime.std.movies.media.SpriteMediaHandler;

import quicktime.util.EncodedImage;
import quicktime.util.EndianDescriptor;
import quicktime.util.EndianOrder;
import quicktime.util.QTHandle;
import quicktime.util.QTUtils;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * A class that creates QuickTime Sprite tracks for each tier that has  graphic
 * references.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
public class QTSVGViewer extends AbstractSVGViewer {
    /** a logger */
    private static final Logger LOG = Logger.getLogger(QTSVGViewer.class.getName());

    /** the QT movie object */
    private Movie movie;

    /** stores created tracks with the tiername as key */
    private Map trackTable;

    /** default sprite description */
    private SpriteDescription spriteDesc;

    /** the duration of the media according to QT */
    private int qtDuration;

    /**
     * the width of the media according to QT;  in case of MPEG-1 media QT
     * (often) creates a movie  of size 320240 (instead of 352288)
     */
    private int qtMediaWidth;

    /** the height of the media according to QT */
    private int qtMediaHeight;

    /** the media rect according to QT: 0, 0, qtMediaWidth, qtMediaHeight */
    private QDRect qtMediaRect;

    /** the dimension of the media as read from the header of the mediafile */
    private QDRect fileMediaRect;

    /** the color to use for the stroke of 2d annotations */
    private final QDColor QD_STROKE_COLOR;

    /**
     * Creates a new QTSVGViewer instance
     *
     * @param transcription the transcription
     */
    public QTSVGViewer(Transcription transcription) {
        super(transcription);

        trackTable = new Hashtable();

        try {
            spriteDesc = new SpriteDescription();
        } catch (QTException qte) {
        }

        float[] colors = new float[3];
        colors = STROKE_COLOR.getRGBColorComponents(colors);
        QD_STROKE_COLOR = new QDColor(colors[0], colors[1], colors[2]);
    }

    /**
     * Sets the movie object that will contain the new Tracks.
     *
     * @param movie the QT movie
     */
    public void setMovie(Movie movie) {
        this.movie = movie;

        try {
            qtDuration = movie.getDuration();
            qtMediaWidth = movie.getNaturalBoundsRect().getWidth();
            qtMediaHeight = movie.getNaturalBoundsRect().getHeight();
        } catch (QTException qte) {
            qtMediaWidth = 320;
            qtMediaHeight = 240;
            qtDuration = Integer.MAX_VALUE;
        }

        qtMediaRect = new QDRect(0, 0, qtMediaWidth, qtMediaHeight);
        fileMediaRect = new QDRect(0, 0, 352, 288);

        initViewer();
    }

    /**
     * Sets the dimension of the (mpeg) movie, as read from the mpeg header.
     *
     * @param d the image width and height of the video
     */
    public void setMediaFileDimension(Dimension d) {
        if (d == null) {
            // hardwired for now
            fileMediaRect = new QDRect(0, 0, 352, 288);
        } else {
            fileMediaRect = new QDRect(0, 0, (int) d.getWidth(),
                    (int) d.getHeight());
        }
    }

    /**
     * Initializes the viewer by extracting the graphical annotations.<br>
     * It then creates SpriteTracks and adds them to the Movie.
     */
    void initViewer() {
        if (movie == null) {
            return;
        }

        TierImpl tier;

        Iterator tierIt = transcription.getTiers().iterator();

        while (tierIt.hasNext()) {
            tier = (TierImpl) tierIt.next();
            createTrackFromTier(tier);
        }

        QTUtils.reclaimMemory();
    }

    /**
     * This methods loops over all annotations on the specified tier and
     * creates a sample  for each one containing a graphical annotation.  It
     * seems that only a duration can be specified for a sample, not a start
     * time.  Therefore empty samples are inserted in the gaps between
     * annotations.
     *
     * @param tier the tier containing the annotations for the track
     */
    private void createTrackFromTier(TierImpl tier) {
        if ((tier == null) || (tier.getLinguisticType() == null) ||
                !tier.getLinguisticType().hasGraphicReferences()) {
            return;
        }

        try {
            String tierName = tier.getName();
            Track track = null;
            SpriteMedia spriteMedia = null;

            if (trackTable.containsKey(tierName)) {
                track = (Track) trackTable.get(tierName);

                if (track != null) {
                    movie.removeTrack(track);
                    trackTable.remove(tierName);
                    track.disposeQTObject();
                }
            }

            track = movie.newTrack(qtMediaWidth, qtMediaHeight, 0f);
            spriteMedia = new SpriteMedia(track, movie.getTimeScale(), null);

            AtomContainer sampleContainer;
            AtomContainer emptyAC;

            Vector annos = tier.getAnnotations();
            long curEndTime = 0L;
            Iterator annIt = annos.iterator();
            Annotation ann;
            SVGAlignableAnnotation svgAnn;

            spriteMedia.beginEdits();

            while (annIt.hasNext()) {
                ann = (Annotation) annIt.next();

                if (!(ann instanceof SVGAlignableAnnotation)) {
                    // this can happen when an existing lin. type is changed
                    // but annotations have not yet been converted
                    break;
                }

                svgAnn = (SVGAlignableAnnotation) ann;

                if (svgAnn.getShape() != null) {
                    if (curEndTime < svgAnn.getBeginTimeBoundary()) {
                        // add empty sample
                        emptyAC = new AtomContainer();

                        int duration = (int) (svgAnn.getBeginTimeBoundary() -
                            curEndTime);
                        spriteMedia.addSample(emptyAC, 0, // dataOffset
                            emptyAC.getSize(), duration, // duration in ms (ticks : one frame * 25?)
                            spriteDesc, 1, // 1 samples
                            0); // flags - this is a sync sample)							
                    }

                    // now add a sample for svgAnn
                    sampleContainer = createSample(svgAnn);

                    if (sampleContainer != null) {
                        int duration = (int) (svgAnn.getEndTimeBoundary() -
                            svgAnn.getBeginTimeBoundary());
                        spriteMedia.addSample(sampleContainer, 0, // dataOffset
                            sampleContainer.getSize(), duration, // duration in ms
                            spriteDesc, 1, // 1 sample
                            0); // sync sample
                    }

                    curEndTime = svgAnn.getEndTimeBoundary();
                }
            }

            if (curEndTime < qtDuration) {
                int duration = (int) (qtDuration - curEndTime);
                emptyAC = new AtomContainer();
                spriteMedia.addSample(emptyAC, 0, // dataOffset
                    emptyAC.getSize(), duration, // duration in ms (ticks : one frame * 25?)
                    spriteDesc, 1, // 1 samples
                    0);
            }

            spriteMedia.endEdits();

            /* add sprite media to track */
            track.insertMedia(0, 0, spriteMedia.getDuration(), 1);

            // track properties
            AtomContainer spriteTrackProperties = new AtomContainer();

            /*
               QDColor bgColor = QDColor.blue;
               EndianOrder.flipNativeToBigEndian(bgColor, 0,
                       QDColor.getEndianDescriptorRGBColor());
               spriteTrackProperties.insertChild(new Atom(StdQTConstants.kParentAtomIsContainer),
                   StdQTConstants.kSpriteTrackPropertyBackgroundColor, 1, 1, bgColor);
             */
            /*
               int idleAsFastAsPossible = 0;
               spriteTrackProperties.insertChild(
                   new Atom(StdQTConstants.kParentAtomIsContainer),
                   StdQTConstants.kSpriteTrackPropertyQTIdleEventsFrequency,
                   1,
                   1,
                   EndianOrder.flipNativeToBigEndian32(idleAsFastAsPossible));
             */
            spriteTrackProperties.insertChild(new Atom(
                    StdQTConstants.kParentAtomIsContainer),
                StdQTConstants.kSpriteTrackPropertyScaleSpritesToScaleWorld, 1,
                1, EndianOrder.flipNativeToBigEndian16((short) 1));

            /* does not work (yet)
               spriteTrackProperties.insertChild(
                   new Atom(StdQTConstants.kParentAtomIsContainer),
                   StdQTConstants.kSpriteTrackPropertyVisible,
                   1,
                   1,
                   EndianOrder.flipNativeToBigEndian16((short)1));//visible
             */
            spriteMedia.setPropertyAtom(spriteTrackProperties);

            SpriteMediaHandler handler = spriteMedia.getSpriteHandler();
            handler.setGraphicsMode(new GraphicsMode(QDConstants.transparent,
                    QDColor.black));

            // store the track reference for later use
            Object t = trackTable.put(tierName, track);

            /*
               if (t instanceof Track) {
                   ((Track)t).disposeQTObject();
               }
             */

            // reclaim memory
            QTUtils.reclaimMemory();
        } catch (QTException qte) {
            LOG.warning("Unable to create track for tier: " + qte.getMessage());
        }
    }

    /**
     * First creates an image from the annotation's shape and then creates an
     * AtomContainer with the right property and data atoms.
     *
     * @param svgAnn the annotation that holds a graphical shape/annotation
     *
     * @return a dressed up AtomContainer or Sample
     */
    private AtomContainer createSample(SVGAlignableAnnotation svgAnn) {
        if ((svgAnn == null) || (svgAnn.getShape() == null)) {
            return null;
        }

        AtomContainer sampleContainer = null;

        try {
            sampleContainer = new AtomContainer();

            QTHandle imageHandle = createImageHandleForShape(svgAnn.getShape());

            if (imageHandle != null) {
                Atom spriteSharedDataAtom;
                Atom spriteImageContainerAtom;
                Atom spriteImageAtom;
                Atom spritePropAtom;
                int spriteID = 1;

                spriteSharedDataAtom = sampleContainer.insertChild(new Atom(
                            StdQTConstants.kParentAtomIsContainer),
                        StdQTConstants.kSpriteSharedDataAtomType, spriteID, // id
                        0); //index

                if (spriteSharedDataAtom == null) {
                    return null;
                }

                spriteImageContainerAtom = sampleContainer.insertChild(spriteSharedDataAtom,
                        StdQTConstants.kSpriteImagesContainerAtomType,
                        spriteID, //id
                        0); //index

                if (spriteImageContainerAtom == null) {
                    return null;
                }

                spriteImageAtom = sampleContainer.insertChild(spriteImageContainerAtom,
                        StdQTConstants.kSpriteImageAtomType, spriteID, //id
                        0); //index

                if (spriteImageAtom == null) {
                    return null;
                }

                // add data
                sampleContainer.insertChild(spriteImageAtom,
                    StdQTConstants.kSpriteImageDataAtomType, spriteID, //id
                    0, //index
                    imageHandle);

                // add group id??
                sampleContainer.insertChild(spriteImageAtom,
                    StdQTConstants.kSpriteImageGroupIDAtomType, spriteID, 0,
                    EndianOrder.flipNativeToBigEndian32(1));

                // create properties atom
                spritePropAtom = sampleContainer.insertChild(new Atom(
                            StdQTConstants.kParentAtomIsContainer),
                        StdQTConstants.kSpriteAtomType, spriteID, //id
                        0);

                // setVisible
                sampleContainer.insertChild(spritePropAtom,
                    StdQTConstants.kSpritePropertyVisible, spriteID, //id
                    0, EndianOrder.flipNativeToBigEndian16((short) 1)); //1 = visible

                // imageIndex
                sampleContainer.insertChild(spritePropAtom,
                    StdQTConstants.kSpritePropertyImageIndex, 1, 0,
                    EndianOrder.flipNativeToBigEndian16((short) (spriteID)));

                // layer, needed??
                sampleContainer.insertChild(spritePropAtom,
                    StdQTConstants.kSpritePropertyLayer, 1, 0,
                    EndianOrder.flipNativeToBigEndian16((short) 0)); // 0 = topmost

                /*
                   Matrix theMatrix = new Matrix();
                   theMatrix.setSx(((float)320) / 352);
                   theMatrix.setSy(((float)240) / 288);
                   //theMatrix.setIdentity();
                   EndianOrder.flipNativeToBigEndian(theMatrix, 0, new EndianDescriptor(EndianDescriptor.kFlipAllFields32));
                   sampleContainer.insertChild (
                       spritePropAtom,
                       StdQTConstants.kSpritePropertyMatrix,
                       1,
                       0,
                       theMatrix);
                 */
            }
        } catch (QTException qte) {
            LOG.warning("Could not create sample from annotation: " + qte.getMessage());
        }

        return sampleContainer;
    }

    /**
     * Paints the graphical annotations to an offscreen QDGraphics object,
     * creates an image from this graphics object and returns it as a QTHandle
     * object.
     *
     * @param shape the graphical annotation
     *
     * @return a QTHandle
     */
    private QTHandle createImageHandleForShape(Shape shape) {
        if (shape == null) {
            return null;
        }

        try {
            QDGraphics qdg = new QDGraphics(QDGraphics.kDefaultPixelFormat,
                    qtMediaRect);
            qdg.setForeColor(QD_STROKE_COLOR);

            QDRect shapeRect;
            float scaleX = qtMediaRect.getWidthF() / fileMediaRect.getWidth();
            float scaleY = qtMediaRect.getHeightF() / fileMediaRect.getHeight();

            if (shape instanceof RectangularShape) {
                RectangularShape rs = (RectangularShape) shape;
                shapeRect = new QDRect((float) (rs.getX() * scaleX),
                        (float) (rs.getY() * scaleY),
                        (float) (rs.getWidth() * scaleX),
                        (float) (rs.getHeight() * scaleY));

                if (shape instanceof Ellipse2D) {
                    qdg.frameOval(shapeRect);
                } else if (shape instanceof Rectangle2D) {
                    qdg.frameRect(shapeRect);
                }
            } else if (shape instanceof Line2D) {
                Point2D p1 = ((Line2D) shape).getP1();
                Point2D p2 = ((Line2D) shape).getP2();
                int p1x = (int) (scaleX * p1.getX());
                int p1y = (int) (scaleY * p1.getY());
                int p2x = (int) (scaleX * p2.getX());
                int p2y = (int) (scaleY * p2.getY());
                qdg.moveTo(p1x, p1y);
                qdg.lineTo(p2x, p2y);
            }

            PixMap pixmap = PixMap.fromQDGraphics(qdg);
            EncodedImage encodedImage = pixmap.getPixelData();
            ImageDescription imageDescription = new ImageDescription(pixmap);

            //Endian flip the ImageDescription
            EndianOrder.flipNativeToBigEndian(imageDescription, 0,
                ImageDescription.getEndianDescriptor());

            return new QTHandle(imageDescription,
                QTHandle.fromEncodedImage(encodedImage));
        } catch (QTException qte) {
        }

        return null;
    }

    /**
     * Removes the track corresponding to the tier from the movie.
     *
     * @param tierName the tier identifier
     */
    private void removeTrack(String tierName) {
        if (trackTable.containsKey(tierName)) {
            Track track = (Track) trackTable.get(tierName);

            if (track != null) {
                try {
                    movie.removeTrack(track);
                    track.disposeQTObject();
                } catch (StdQTException sqte) {
                    LOG.warning("Cannot remove track: " + sqte.getMessage());
                } catch (QTException qte) {
                	LOG.warning("Cannot remove track: " + qte.getMessage());
                }
            }

            trackTable.remove(tierName);
        }
    }

    /**
     * When a tier is set invisible in the multitierviewers don't render the
     * graphics.
     *
     * @param tiers the visible tiers
     */
    public void setVisibleTiers(Vector tiers) {
        // when it is possible to successfully set the visibility property 
        // of a track implement this method
    }

    ////////
    // acm edit event handling methods
    /////////

    /**
     * A tier has been added.
     *
     * @param tier the new tier
     */
    void tierAdded(TierImpl tier) {
        createTrackFromTier(tier);
        requestRepaint();
    }

    /**
     * A tier has been removed.
     *
     * @param tier the removed tier
     */
    void tierRemoved(TierImpl tier) {
        if ((tier != null) && (tier.getLinguisticType() != null) &&
                tier.getLinguisticType().hasGraphicReferences()) {
            removeTrack(tier.getName());
        }
    }

    /**
     * An annotation has been added.<br>
     * Creation of an annotation can effect existing annotations on the same
     * tier and/or dependent tiers. Just reextract the graphic objects  from
     * these tiers.  In Shift mode all tiers could be changed, so all tracks
     * will be  recreated in that case.
     *
     * @param annotation the new annotation
     */
    void annotationAdded(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            int mode = transcription.getTimeChangePropagationMode();

            if (mode != Transcription.SHIFT) {
                TierImpl tier = (TierImpl) annotation.getTier();
                Vector depTiers = tier.getDependentTiers();
                depTiers.add(0, tier);

                for (int i = 0; i < depTiers.size(); i++) {
                    tier = (TierImpl) depTiers.get(i);
                    createTrackFromTier(tier);
                }
            } else {
                transcriptionChanged();
            }

            requestRepaint();
        }
    }

    /**
     * Check every currently present tier with graphical annotations.  We could
     * assume no tiers have been added and no tiers have been removed. There
     * are other events for that kind of actions. Doublecheck to be sure.
     */
    void transcriptionChanged() {
        Vector tiers = transcription.getTiers();
        TierImpl tier;

        for (int i = 0; i < tiers.size(); i++) {
            tier = (TierImpl) tiers.get(i);
            createTrackFromTier(tier);
        }

        requestRepaint();
    }

    /**
     * Checks if the linguistic type of any of the current tiers with graphic
     * references has been changed to not allow graphic references.
     */
    void linguisticTypeChanged() {
        LinguisticType type;

        Vector tiers = transcription.getTiers();
        TierImpl tier;
        String tierName;

        for (int i = 0; i < tiers.size(); i++) {
            tier = (TierImpl) tiers.get(i);
            tierName = tier.getName();
            type = tier.getLinguisticType();

            if ((type != null) && type.hasGraphicReferences()) {
                if (!trackTable.containsKey(tierName)) {
                    createTrackFromTier(tier);
                }
            } else {
                if (trackTable.containsKey(tierName)) {
                    removeTrack(tierName);
                }
            }
        }

        requestRepaint();
    }

    /**
     * An annotation's begin and/or end time has changed.  Changing the begin
     * and/or end time of an annotation can effect  existing annotations on
     * the same tier and/or dependent tiers.  Just reextract the graphic
     * objects from these tiers.  In Shift mode all tiers could be changed, so
     * all tracks will be  recreated in that case.
     *
     * @param annotation the annotation
     */
    void annotationTimeChanged(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            int mode = transcription.getTimeChangePropagationMode();

            if (mode != Transcription.SHIFT) {
                TierImpl tier = (TierImpl) annotation.getTier();
                Vector depTiers = tier.getDependentTiers();
                depTiers.add(0, tier);

                for (int i = 0; i < depTiers.size(); i++) {
                    tier = (TierImpl) depTiers.get(i);
                    createTrackFromTier(tier);
                }
            } else {
                transcriptionChanged();

                return;
            }

            requestRepaint();
        }
    }

    /**
     * A graphic object has been edited.
     *
     * @param annotation the edited annotation
     */
    void annotationGraphicChanged(SVGAlignableAnnotation annotation) {
        if (annotation != null) {
            // we could update a single atom...
            TierImpl tier = (TierImpl) annotation.getTier();
            createTrackFromTier(tier);
            requestRepaint();
        }
    }

    /**
     * Creates the Sprite tracks for each tier referencing graphic annotations.
     */
    void requestRepaint() {
        if (movie != null) {
            try {
                movie.update();
            } catch (QTException qte) {
                //ignore
            }
        }
    }

    /**
     * Stub. Ignored by this viewer. Rendering of the annotations is handled by
     * the QT player.
     *
     * @param big2d the graphics object provided by the player
     */
    void paintAnnotations(Graphics2D big2d) {
    }

    /**
     * Stub. Ignored by this viewer. Rendering of the annotations is handled by
     * the QT player.
     */
    void paintAnnotations() {
    }

	public void setVisibleTiers(List tiers) {
		// TODO Auto-generated method stub
		
	}
}
