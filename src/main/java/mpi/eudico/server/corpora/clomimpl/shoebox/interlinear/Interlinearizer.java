/*
 * Created on Sep 24, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Interlinearizer renders a range of different interlinear views. These views
 * differ with respect to their 'unit of alignment' and can be controlled by a
 * number of configuration settings. Possible 'units of alignment' are pixels
 * (drawing is on a BufferedImage that can be used for ELAN's interlinear
 * viewer, for printout or for a  web application) and bytes (can be used for
 * Shoebox/Toolbox file output).  The role of Interlinearizer is to store and
 * manage configuration parameters, and to control the rendering process by
 * delegating subtasks to the appropriate helper classes. Interlinearizer uses
 * a Metrics object to store and pass around size  and position information.
 *
 * @author hennie
 */
public class Interlinearizer {
    // constants
    // units for page width and height

    /** Holds value of property DOCUMENT ME! */
    public static final int CM = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int INCH = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int PIXEL = 2;

    // wrap styles for blocks and lines

    /** Holds value of property DOCUMENT ME! */
    public static final int EACH_BLOCK = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int BLOCK_BOUNDARY = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int WITHIN_BLOCKS = 2;

    /** Holds value of property DOCUMENT ME! */
    public static final int NO_WRAP = 3;

    // time code types

    /** Holds value of property DOCUMENT ME! */
    public static final int HHMMSSMS = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int SSMS = 1;

    // strings for unaligned time codes

    /** Holds value of property DOCUMENT ME! */
    public static final String UNALIGNED_HHMMSSMS = "??:??:??:???";

    /** Holds value of property DOCUMENT ME! */
    public static final String UNALIGNED_SSMS = "?.???";

    // unit for text alignment

    /** Holds value of property DOCUMENT ME! */
    public static final int PIXELS = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int BYTES = 1;

    // font
    //	public static Font DEFAULTFONT = new Font("SansSerif", Font.PLAIN, 12);

    /** Holds value of property DOCUMENT ME! */
    public static Font DEFAULTFONT = new Font("MS Arial Unicode", Font.PLAIN, 12);

    /** Holds value of property DOCUMENT ME! */
    public static final int DEFAULT_FONT_SIZE = 12;

    /** Holds value of property DOCUMENT ME! */
    public static final int TIMECODE_FONT_SIZE = 10;

    // empty line style

    /** Holds value of property DOCUMENT ME! */
    public static final int TEMPLATE = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int HIDE_EMPTY_LINES = 1;

    // sorting style

    /** Holds value of property DOCUMENT ME! */
    public static final int EXTERNALLY_SPECIFIED = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int TIER_HIERARCHY = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int BY_LINGUISTIC_TYPE = 2;

    /** Holds value of property DOCUMENT ME! */
    public static final int BY_PARTICIPANT = 3;

    // character encoding

    /** Holds value of property DOCUMENT ME! */
    public static final int UTF8 = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int ISOLATIN = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int SIL = 2;

    // other

    /** Holds value of property DOCUMENT ME! */
    public static double SCALE = 300.0 / 72.0; // from 72 dpi to 300 dpi

    // members	
    private TimeCodedTranscription transcription = null;
    private int width;
    private int height;
    private String[] visibleTiers;
    private boolean tierLabelsShown;
    private long[] visibleTimeInterval;
    private int blockWrapStyle;
    private int lineWrapStyle;
    private boolean timeCodeShown;
    private int timeCodeType;
    private Hashtable fonts;
    private Hashtable fontSizes;
    private boolean emptySlotsShown;
    private int lineSpacing;
    private int blockSpacing = -1;
    private Annotation activeAnnotation;
    private long[] selection;
    private long mediaTime;
    private int alignmentUnit;
    private int emptyLineStyle; // show full 'template' for block, or hide empty lines
    private int sortingStyle;
    private Hashtable charEncodings;
    private Metrics metrics;

    //	private BufferedImage bi;
    private boolean forPrinting = false;
    private boolean renderingFirstPage = true;
    private int pageHeight = 0;
    private boolean sorted = false;
    private boolean correctAnnotationTimes = false;

    /**
     * Creates a new Interlinearizer instance
     *
     * @param tr DOCUMENT ME!
     */
    public Interlinearizer(TimeCodedTranscription tr) {
        transcription = tr;

        metrics = new Metrics(tr, this);
        setDefaultValues();
    }

    private void resetMetrics() {
        metrics.reset();
    }

    private void setDefaultValues() {
        tierLabelsShown = true;
        blockWrapStyle = NO_WRAP;
        lineWrapStyle = NO_WRAP;
        timeCodeShown = false;
        timeCodeType = HHMMSSMS;
        emptySlotsShown = false;
        alignmentUnit = PIXELS;
        emptyLineStyle = HIDE_EMPTY_LINES;
        sortingStyle = EXTERNALLY_SPECIFIED;

        // set default visible tiers to all tier names
        if (transcription != null) {
            Vector tiers = transcription.getTiers();
            visibleTiers = new String[tiers.size()];

            for (int i = 0; i < tiers.size(); i++) {
                String tName = ((Tier) tiers.elementAt(i)).getName();
                visibleTiers[i] = tName;
            }
        }

        // defaults for font and fontsizes
        fonts = new Hashtable();
        fontSizes = new Hashtable();
        charEncodings = new Hashtable();
    }

    /**
     * DOCUMENT ME!
     *
     * @param bi DOCUMENT ME!
     */
    public void renderView(BufferedImage bi) {
        if (this.isTimeCodeShown()) {
            transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
            addTimeCodeTiers(false);
        }

        calculateMetrics(bi.getGraphics());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] renderAsText() {
        if (this.isTimeCodeShown()) {
            transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
            addTimeCodeTiers(true);
        }

        calculateMetrics();

        return ByteRenderer.render(metrics);
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     * @param pageWidth DOCUMENT ME!
     * @param pageHeight DOCUMENT ME!
     * @param pageIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean renderPage(Graphics g, int pageWidth, int pageHeight,
        int pageIndex) {
        boolean pageExists = false;

        this.setWidth(pageWidth);
        this.setHeight(pageHeight);

        // first page
        if (renderingFirstPage) {
            if (this.isTimeCodeShown()) {
                transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
                addTimeCodeTiers(false);
            }

            calculateMetrics(g);
            renderingFirstPage = false;
        }

        // all pages
        pageExists = drawPage(g, pageIndex);

        // no more pages
        if (!pageExists) {
            if (isTimeCodeShown()) {
                transcription.cleanupTimeCodeTiers();
                removeTimeCodeTiers();
            }
        }

        return pageExists;
    }

    /**
     * Adjusts visibleTiers to show time code tiers for 'tier bundles' with one
     * or more visible tiers. The tc tiers are positioned right after the last
     * visible tier of their bundle.
     *
     * @param atTopOfBlock DOCUMENT ME!
     */
    private void addTimeCodeTiers(boolean atTopOfBlock) {
        // clean up first
        removeTimeCodeTiers();

        Vector tcTiers = transcription.getTimeCodeTiers();
        Vector vTierVector = new Vector(Arrays.asList(visibleTiers));

        // if tcTier has a root tier in common with a visible tier, then
        // it is visible. Add at proper position to vTierList.
        for (int j = 0; j < tcTiers.size(); j++) {
            Tier tcTier = (Tier) tcTiers.elementAt(j);

            // set font size for time code tiers
            // find minimum font size for visible tiers
            int minSize = TIMECODE_FONT_SIZE;

            for (int i = 0; i < vTierVector.size(); i++) {
                int sz = this.getFontSize((String) vTierVector.elementAt(i));

                if (sz < minSize) {
                    minSize = sz;
                }
            }

            setFontSize(tcTier.getName(), minSize);

            Tier rootTier = transcription.getRootTier(tcTier);

            // iterate over visibleTiers, remember position of last tier
            // that has same root tier as tcTier
            int lastIndex = -1;
            int firstIndex = -1;
            int index = 0;
            Iterator vTierIter = vTierVector.iterator();

            while (vTierIter.hasNext()) {
                String visTierName = (String) vTierIter.next();
                Tier visTier = transcription.getTranscription().getTierWithId(visTierName);
                TierImpl rootOfVisTier = transcription.getRootTier(visTier);

                if ((rootOfVisTier != null) && (rootTier == rootOfVisTier)) {
                    lastIndex = index;

                    if (firstIndex == -1) { // not yet set
                        firstIndex = index;
                    }
                }

                index++;
            }

            if (atTopOfBlock && (firstIndex >= 0)) {
                vTierVector.add(firstIndex + 1, tcTier.getName());

                continue;
            }

            if (lastIndex >= 0) {
                vTierVector.add(lastIndex + 1, tcTier.getName());
            }
        }

        int counter = 0;
        String[] newVisTiers = new String[vTierVector.size()];
        Iterator vIter = vTierVector.iterator();

        while (vIter.hasNext()) {
            String newTierName = newTierName = (String) vIter.next();

            newVisTiers[counter] = newTierName;
            counter++;
        }

        setVisibleTiers(newVisTiers);
    }

    private void removeTimeCodeTiers() {
        Vector newVTierVector = new Vector();

        for (int i = 0; i < visibleTiers.length; i++) {
            String vTierName = visibleTiers[i];

            if (!vTierName.startsWith(TimeCodedTranscription.TC_TIER_PREFIX)) {
                newVTierVector.add(vTierName);
            } else if (fontSizes.contains(vTierName)) {
                fontSizes.remove(vTierName);
            }
        }

        String[] newVisTiers = new String[newVTierVector.size()];

        for (int i = 0; i < newVTierVector.size(); i++) {
            newVisTiers[i] = (String) newVTierVector.elementAt(i);
        }

        setVisibleTiers(newVisTiers);
    }

    private void calculateMetrics(Graphics graphics) {
        resetMetrics();

        SizeCalculator.calculateSizes(metrics, graphics);
        SizeCalculator.calculateUsedWidths(metrics);

        Positioner.calcHorizontalPositions(metrics);

        if ((lineWrapStyle != NO_WRAP) || (blockWrapStyle != NO_WRAP)) {
            Positioner.wrap(metrics);
        }

        if (emptyLineStyle == HIDE_EMPTY_LINES) {
            Positioner.hideEmptyLines(metrics);
        }
    }

    /**
     * Calculates metrics based on byte-wise alignment.
     */
    private void calculateMetrics() {
        resetMetrics();

        SizeCalculator.calculateSizes(metrics);
        SizeCalculator.calculateUsedWidths(metrics);

        Positioner.calcHorizontalPositions(metrics);

        if ((lineWrapStyle != NO_WRAP) || (blockWrapStyle != NO_WRAP)) {
            Positioner.wrap(metrics);
        }

        if (emptyLineStyle == HIDE_EMPTY_LINES) {
            Positioner.hideEmptyLines(metrics);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param bi DOCUMENT ME!
     * @param offset DOCUMENT ME!
     */
    public void drawViewOnImage(BufferedImage bi, int[] offset) {
        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            ImageRenderer.render(metrics, bi, offset);
        }
    }

    private boolean drawPage(Graphics g, int pageIndex) {
        boolean pageExists = true;

        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            pageExists = ImageRenderer.render(metrics, g, pageIndex);
        }

        return pageExists;
    }

    // getters and setters

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public Annotation getActiveAnnotation() {
        return activeAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getAlignmentUnit() {
        return alignmentUnit;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getBlockWrapStyle() {
        return blockWrapStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isEmptySlotsShown() {
        return emptySlotsShown;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return
     */
    public Font getFont(String tierName) {
        Font f = (Font) fonts.get(tierName);

        if (f == null) { // use default font
            f = DEFAULTFONT;
        }

        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return
     */
    public int getFontSize(String tierName) {
        int size = 0;
        Integer sizeInt = (Integer) fontSizes.get(tierName);

        if (sizeInt != null) {
            size = sizeInt.intValue();
        } else {
            size = DEFAULT_FONT_SIZE;
        }

        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getHeight() {
        if (height > 0) {
            return height;
        } else {
            // find width from max horizontally used space
            return metrics.getMaxVerticalPosition();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getLineSpacing() {
        return lineSpacing;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getBlockSpacing() {
        if (blockSpacing < 0) { // default: derived from line spacing

            return 20 + (3 * getLineSpacing());
        } else {
            return blockSpacing;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param blockSpacing DOCUMENT ME!
     */
    public void setBlockSpacing(int blockSpacing) {
        this.blockSpacing = blockSpacing;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getLineWrapStyle() {
        return lineWrapStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public long getMediaTime() {
        return mediaTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public long[] getSelection() {
        return selection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isTierLabelsShown() {
        return tierLabelsShown;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isTimeCodeShown() {
        return timeCodeShown;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getTimeCodeType() {
        return timeCodeType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public String[] getVisibleTiers() {
        if (sorted) {
            return visibleTiers;
        }

        if (sortingStyle == TIER_HIERARCHY) {
            visibleTiers = sortByHierarchy(visibleTiers);
        } else if (sortingStyle == BY_LINGUISTIC_TYPE) {
            visibleTiers = sortByLinguisticType(visibleTiers);
        } else if (sortingStyle == BY_PARTICIPANT) {
            visibleTiers = sortByParticipant(visibleTiers);
        }

        sorted = true;

        return visibleTiers;
    }

    /**
     * Sorts according to tier hierarchy.
     *
     * @param visibleTiers
     *
     * @return
     */
    private String[] sortByHierarchy(String[] visibleTiers) {
        Vector sortedTiers = new Vector();
        String[] sortedTierNames = new String[visibleTiers.length];

        List vTierList = Arrays.asList(visibleTiers);

        Vector topTiers = ((TranscriptionImpl)transcription.getTranscription()).getTopTiers();

        for (int i = 0; i < topTiers.size(); i++) {
            TierImpl topTier = (TierImpl) topTiers.elementAt(i);
            sortedTiers.addAll(transcription.getTierTree(topTier));
        }

        int arrayIndex = 0;

        for (int j = 0; j < sortedTiers.size(); j++) {
            TierImpl t = (TierImpl) sortedTiers.elementAt(j);

            if (vTierList.contains(t.getName())) {
                sortedTierNames[arrayIndex++] = t.getName();
            }
        }

        return sortedTierNames;
    }

    private String[] sortByLinguisticType(String[] visibleTiers) {
        Hashtable typesHash = new Hashtable();

        LinguisticType notSpecifiedLT = new LinguisticType("NOT_SPECIFIED");
        LinguisticType tcLT = new LinguisticType(TimeCodedTranscription.TC_LING_TYPE);

        for (int i = 0; i < visibleTiers.length; i++) {
            String tierName = visibleTiers[i];

            LinguisticType lt = notSpecifiedLT;
            TierImpl tier = ((TierImpl) transcription.getTranscription()
                                                     .getTierWithId(tierName));

            if (tier != null) {
                lt = tier.getLinguisticType();

                if (lt == null) {
                    lt = notSpecifiedLT;
                }
            } else if (tierName.startsWith(
                        TimeCodedTranscription.TC_TIER_PREFIX)) {
                lt = tcLT;
            }

            Vector tiersOfType = (Vector) typesHash.get(lt);

            if (tiersOfType == null) {
                tiersOfType = new Vector();
                typesHash.put(lt, tiersOfType);
            }

            tiersOfType.add(tierName);
        }

        Vector sortedTierNames = new Vector();
        Enumeration typeEnum = typesHash.keys();

        while (typeEnum.hasMoreElements()) {
            LinguisticType type = (LinguisticType) typeEnum.nextElement();
            sortedTierNames.addAll((Vector) typesHash.get(type));
        }

        String[] sortedNameStrings = new String[visibleTiers.length];

        for (int j = 0; j < sortedTierNames.size(); j++) {
            sortedNameStrings[j] = (String) sortedTierNames.elementAt(j);
        }

        return sortedNameStrings;
    }

    private String[] sortByParticipant(String[] visibleTiers) {
        Hashtable participantHash = new Hashtable();

        String notSpecifiedParticipant = "NOT_SPECIFIED";

        for (int i = 0; i < visibleTiers.length; i++) {
            String tierName = visibleTiers[i];

            String participant = notSpecifiedParticipant;
            TierImpl tier = ((TierImpl) transcription.getTranscription()
                                                     .getTierWithId(tierName));

            if (tier != null) {
                participant = tier.getParticipant();

                if (participant == null) {
                    participant = notSpecifiedParticipant;
                }
            }

            Vector tiersOfParticipant = (Vector) participantHash.get(participant);

            if (tiersOfParticipant == null) {
                tiersOfParticipant = new Vector();
                participantHash.put(participant, tiersOfParticipant);
            }

            tiersOfParticipant.add(tierName);
        }

        Vector sortedTierNames = new Vector();
        Enumeration participantEnum = participantHash.keys();

        while (participantEnum.hasMoreElements()) {
            String part = (String) participantEnum.nextElement();
            sortedTierNames.addAll((Vector) participantHash.get(part));
        }

        String[] sortedNameStrings = new String[visibleTiers.length];

        for (int j = 0; j < sortedTierNames.size(); j++) {
            sortedNameStrings[j] = (String) sortedTierNames.elementAt(j);
        }

        return sortedNameStrings;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public long[] getVisibleTimeInterval() {
        return visibleTimeInterval;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getWidth() {
        if (width > 0) {
            return width;
        } else {
            // find width from max horizontally used space
            return metrics.getMaxHorizontallyUsedWidth();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotation
     */
    public void setActiveAnnotation(Annotation annotation) {
        activeAnnotation = annotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setAlignmentUnit(int i) {
        alignmentUnit = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setBlockWrapStyle(int i) {
        blockWrapStyle = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b
     */
    public void setEmptySlotsShown(boolean b) {
        emptySlotsShown = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setHeight(int i) {
        height = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setLineSpacing(int i) {
        lineSpacing = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setLineWrapStyle(int i) {
        lineWrapStyle = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param l
     */
    public void setMediaTime(long l) {
        mediaTime = l;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ls
     */
    public void setSelection(long[] ls) {
        selection = ls;
    }

    /**
     * DOCUMENT ME!
     *
     * @param show
     */
    public void setTierLabelsShown(boolean show) {
        tierLabelsShown = show;
        metrics.showLeftMargin(show);
    }

    /**
     * DOCUMENT ME!
     *
     * @param b
     */
    public void setTimeCodeShown(boolean b) {
        timeCodeShown = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setTimeCodeType(int i) {
        timeCodeType = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @param strings
     */
    public void setVisibleTiers(String[] strings) {
        visibleTiers = strings;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ls
     */
    public void setVisibleTimeInterval(long[] ls) {
        visibleTimeInterval = ls;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setWidth(int i) {
        width = i;
    }

    /**
     *
     */
    public void setFont(String tierName, Font f) {
        int fontSize = getFontSize(tierName);
        f = f.deriveFont((float) fontSize);

        fonts.put(tierName, f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param size DOCUMENT ME!
     */
    public void setFontSize(String tierName, int size) {
        fontSizes.put(tierName, new Integer(size));
        fonts.put(tierName, getFont(tierName).deriveFont((float) size));
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getEmptyLineStyle() {
        return emptyLineStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setEmptyLineStyle(int i) {
        emptyLineStyle = i;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getSortingStyle() {
        return sortingStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setSortingStyle(int i) {
        sortingStyle = i;
        sorted = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TimeCodedTranscription getTranscription() {
        return transcription;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean forPrinting() {
        return forPrinting;
    }

    /**
     * DOCUMENT ME!
     *
     * @param forPrinting DOCUMENT ME!
     */
    public void setForPrinting(boolean forPrinting) {
        this.forPrinting = forPrinting;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPageHeight() {
        return pageHeight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param height DOCUMENT ME!
     */
    public void setPageHeight(int height) {
        pageHeight = height;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getCharEncoding(String tierName) {
        int encoding = UTF8;

        if (tierName == null) {
            return encoding; // UTF8 is always the default
        }

        if (!charEncodings.containsKey(tierName)) {
            return encoding;
        }

        Integer encodingInt = (Integer) charEncodings.get(tierName);

        if (encodingInt != null) {
            encoding = encodingInt.intValue();
        }

        return encoding;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param charEncoding DOCUMENT ME!
     */
    public void setCharEncoding(String tierName, int charEncoding) {
        charEncodings.put(tierName, new Integer(charEncoding));
    }

    /**
     * Empty horizontal space between neightbouring annotations.
     *
     * @return DOCUMENT ME!
     */
    public int getEmptySpace() {
        int emptySpace = 10; // 10 pixels in case of image

        if (getAlignmentUnit() == BYTES) {
            emptySpace = 1;
        }

        return emptySpace;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Metrics getMetrics() {
        return metrics;
    }
    
    public boolean getCorrectAnnotationTimes() {
        return correctAnnotationTimes;
    }
    
    public void setCorrectAnnotationTimes(boolean correctAnnotationTimes) {
        this.correctAnnotationTimes = correctAnnotationTimes;
    }
}
