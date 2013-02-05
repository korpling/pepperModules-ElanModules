package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;


import java.awt.FontMetrics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;


/**
 * builds a layout (in memory) of a tier the layout consists of
 * AnnotationSizeContainers (which contain refs to     Annotations)     this
 * class is used by  SBTextViewer and ExportShoebox in the ACM tree
 */
public class AnnotationSize implements ACMEditListener {
    /** Holds value of property DOCUMENT ME! */
    Transcription _trans = null;

    /** Holds value of property DOCUMENT ME! */
    Annotation _refAnn = null;

    /** Holds value of property DOCUMENT ME! */
    Vector _vBlockTiers = new Vector();

    /** Holds value of property DOCUMENT ME! */
    TreeMap _htTimeList = new TreeMap();

    /** Holds value of property DOCUMENT ME! */
    FontMetrics _fontmetric = null;

    /** Holds value of property DOCUMENT ME! */
    Hashtable htAnnBetween = new Hashtable();
    
    HashMap metricsPerTier = null;

    /**
     * Creates a new AnnotationSize instance
     *
     * @param trans DOCUMENT ME!
     * @param _ref DOCUMENT ME!
     */
    public AnnotationSize(Transcription trans, Annotation _ref) {
        _trans = trans;
        _refAnn = _ref;
        _fontmetric = null;

        // getTiers for Current given annotion ref
        getBlockTiers();

        // get all time segments in block
        buildTimeBlock();
    }

    /**
     * Creates a new AnnotationSize instance
     *
     * @param trans DOCUMENT ME!
     * @param _ref DOCUMENT ME!
     * @param fontmetrics DOCUMENT ME!
     */
    public AnnotationSize(Transcription trans, Annotation _ref,
        FontMetrics fontmetrics) {
        _trans = trans;
        _refAnn = _ref;
        _fontmetric = fontmetrics;

        // getTiers for Current given annotion ref
        getBlockTiers();

        // get all time segments in block
        buildTimeBlock();
    }
    
    /**
     * Creates a new AnnotationSize instance
     *
     * @param trans the transcription!
     * @param _ref the reference annotation
     * @param fontmetrics the default font metrics object
     * @param metricsPerTier optional, special, user defined metrics per tier
     */
    public AnnotationSize(Transcription trans, Annotation _ref,
        FontMetrics defaultFontMetrics, HashMap metricsPerTier) {
        _trans = trans;
        _refAnn = _ref;
        _fontmetric = defaultFontMetrics;
        this.metricsPerTier = metricsPerTier;

        // getTiers for Current given annotion ref
        getBlockTiers();

        // get all time segments in block
        buildTimeBlock();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTierLayoutInChar(Tier tier) {
        return new Vector();

        //return getTierLayoutInPixels(tier,null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     * @param fontmetrics DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArrayList getTierLayoutInPixels(Tier tier, FontMetrics fontmetrics) {
        boolean bNaturalSize = false;
        _fontmetric = fontmetrics;

        ArrayList vecRet = new ArrayList(50);

        // NOTE:: this can be speeded up this is called twice.. the results are not cached but could be
        Vector vans = getAnnBetweenTime(tier, _refAnn.getBeginTimeBoundary(),
                _refAnn.getEndTimeBoundary());

        // if tier is not apart of interline layout return full annotation sizes
        if (!includeTierInCalculation((TierImpl) tier)) {
            bNaturalSize = true;
        }

        // loop thru all the annotation matched there start times w/ the timelist
        // if a time does not exist in the vector of annotations add a null time place holder
        Iterator enumTime = _htTimeList.keySet().iterator(); // a list of all the timeblocks for this segment
        long currentTime = _refAnn.getBeginTimeBoundary(); // HB, 16 aug 02, add if hasNext test
        long origCurrentTime = 0;
        int endMarker = 0;

        if (enumTime.hasNext()) {
            origCurrentTime = currentTime = ((Long) enumTime.next()).longValue();
        }

        int type = (_fontmetric != null) ? AnnotationSizeContainer.PIXELS
                                         : AnnotationSizeContainer.SPACES;

        // GNG: 24 Oct 02 , added so segments with 1 annotation will make them editable
        if (vans.size() == 0) {
            Integer tmptime;

            if ((tmptime = (Integer) _htTimeList.get(new Long(currentTime))) == null) {
                tmptime = new Integer(999);
            } else {
                //System.out.println(tmptime);
            }

            vecRet.add(new AnnotationSizeContainer(null, tmptime, currentTime,
                    currentTime, type));

            if (enumTime.hasNext()) {
                currentTime = ((Long) enumTime.next()).longValue();
            }
        }

        for (int ii = 0; ii < vans.size(); ii++) {
            Annotation aa = (Annotation) vans.elementAt(ii);

            //System.out.println(aa.getValue() + " "+ aa.getBeginTimeBoundary() + " " + currentTime );
            while ((aa.getBeginTimeBoundary() != currentTime) &&
                    enumTime.hasNext()) {
                //System.out.println("Null-" + aa.getValue() + " "+ aa.getBeginTimeBoundary() + " " + currentTime );
                //System.out.println(tname + "Mid add Null at "+currentTime);
                vecRet.add(new AnnotationSizeContainer(null,
                        (Integer) _htTimeList.get(new Long(currentTime)),
                        currentTime, currentTime, type));
                currentTime = ((Long) enumTime.next()).longValue();
            }

            // problem if we could not find 
            // a timeslot for our annotation
            // report error and bail out
            // Feb 2006: not applicable for the new "Included In" constraint
            /*
            if (aa.getBeginTimeBoundary() != currentTime) {
                System.err.println("ERROR:could not find matching time for " +
                    aa.getValue() + " " + aa.getBeginTimeBoundary());

                //	return null;
            }
            */
            if (!bNaturalSize) {
                vecRet.add(new AnnotationSizeContainer(aa,
                        (Integer) _htTimeList.get(new Long(currentTime)), type));
            } else {
                vecRet.add(new AnnotationSizeContainer(aa,
                        getStringSize(aa.getValue()), type));
            }

            if (enumTime.hasNext()) {
                currentTime = ((Long) enumTime.next()).longValue();
            } else {
                endMarker = -1;
            }

            //				currentTime = -1; // flag for end place holder loop below
        }

        // GNG: 24 Oct 02- added so trailing ann-placeholders will be added
        boolean truth = true;

        while (truth && (currentTime != origCurrentTime) && (endMarker != -1) &&
                (bNaturalSize == false)) {
            //System.out.println("End Null-" + currentTime );
            vecRet.add(new AnnotationSizeContainer(null,
                    (Integer) _htTimeList.get(new Long(currentTime)),
                    currentTime, currentTime, type));

            if (enumTime.hasNext()) {
                currentTime = ((Long) enumTime.next()).longValue();
            } else {
                truth = false;
            }
        }

        return vecRet;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Vector getTiers() {
        return _vBlockTiers;
    }

    /**
     * DOCUMENT ME!
     */
    public void getBlockTiers() {
        Vector vAllTiers = null;
        Enumeration etier = null;

        try {
            vAllTiers = _trans.getTiers();
            etier = vAllTiers.elements();
        } catch (Exception rmiexp) {
            rmiexp.printStackTrace();

            return;
        }

        while (etier.hasMoreElements()) {
            TierImpl t = (TierImpl) etier.nextElement();

            if (t.getRootTier() == _refAnn.getTier()) {
                _vBlockTiers.add(t);
            }
        }
    }

    private void buildTimeBlock() {
        Enumeration e = _vBlockTiers.elements();

        while (e.hasMoreElements()) {
            TierImpl ti = (TierImpl) e.nextElement();
            fillTimeBlock(ti);
        }
    }

    /*
     * creates a vector of all the times in the block
     */
    private void fillTimeBlock(TierImpl ti) {
        if (includeTierInCalculation(ti) == false) {
            return;
        }
        FontMetrics tiMetrics = null;
        FontMetrics refAnnMetrics = null;
        if (metricsPerTier != null) {
        	tiMetrics = (FontMetrics) metricsPerTier.get(ti.getName());
        	refAnnMetrics = (FontMetrics) metricsPerTier.get(
        			_refAnn.getTier().getName());
        }
        
        Vector v = getAnnBetweenTime(ti, _refAnn.getBeginTimeBoundary(),
                _refAnn.getEndTimeBoundary());

        Enumeration e = v.elements();
        int strLength;

        while (e.hasMoreElements()) {
            Annotation a = (Annotation) e.nextElement();
            Integer blocklen = null;

            if ((blocklen = (Integer) _htTimeList.get(
                            new Long(a.getBeginTimeBoundary()))) != null) {
            	strLength = getStringSize(a.getValue(), tiMetrics);
                if (blocklen.intValue() < strLength) {
                    _htTimeList.put(new Long(a.getBeginTimeBoundary()),
                        new Integer(strLength));
                }
            } else {
                _htTimeList.put(new Long(a.getBeginTimeBoundary()),
                    new Integer(getStringSize(a.getValue(), tiMetrics)));
            }
        }

        // If _htTimeList is zero add primary tier time stamp
        if (_htTimeList.size() == 0) {
            _htTimeList.put(new Long(_refAnn.getBeginTimeBoundary()),
                new Integer(getStringSize(_refAnn.getValue(), refAnnMetrics)));
        }
    }

    /**
     * gets all annotations the fall between start,end on a tier this is
     * duplicated from tierImpl.getOverLappingAnnotation  because that only
     * seems to work the TimeAlignAble anns
     *
     * @param tier DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param end DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Vector getAnnBetweenTime(Tier tier, long start, long end) {
        Vector v = null;
        TierImpl ti = (TierImpl) tier;

        // cache hack
        if (htAnnBetween.containsKey(tier)) {
            return (Vector) htAnnBetween.get(tier);
        }

        Vector vecRet = new Vector(50);

        try {
            v = ti.getAnnotations();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        for (int i = 0; i < v.size(); i++) {
            Annotation a = (Annotation) v.elementAt(i);

            if ((a.getBeginTimeBoundary() >= start) &&
                    (a.getEndTimeBoundary() <= end)) {
                vecRet.add(a);
            } else if (a.getEndTimeBoundary() > start) {
                break;
            }
        }

        htAnnBetween.put(tier, vecRet);

        return vecRet;
    }

    /**
     * decides wether or not a tier should be includede in the interline layout
     * ie: SYMBOLIC_ASSO or ROOT tiers should not be included
     *
     * @param ti DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean includeTierInCalculation(TierImpl ti) {
        if (!ti.hasParentTier()) {
            return false;
        }

        if (ti.getLinguisticType() == null) {
            // illegal state
            return true;
        }

        if (ti.getLinguisticType().getConstraints() == null) {
            // illegal state if ti.hasParentTier() returned false
            return true;
        }

        if ((ti.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) &&
                (ti.getParentTier() == ti.getRootTier())) {
            return false;
        }
        
        return true;
    }

    /**
     * returns the string in the current metric set the two options are
     * strlen() or fontmetric the option is set by w/ method is called
     *
     * @param str the string to measure
     *
     * @return the width in pixels if there is a font metrics object, 
     * the length of the string otherwise
     */
    public int getStringSize(String str) {
        if (_fontmetric != null) {
            return _fontmetric.stringWidth(str.trim());
        }

        return str.trim().length();
    }

    /**
     * returns the string in the current metric set the two options are
     * strlen() or fontmetric the option is set by w/ method is called
     *
     * @param str the string to measure
     * @param metrics the font metrics object that should perform the measuring
     *
     * @return the width in pixels
     */
    public int getStringSize(String str, FontMetrics metrics) {
        if (metrics == null) {
            return getStringSize(str);
        }

        return metrics.stringWidth(str.trim());
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void ACMEdited(ACMEditEvent e) {
    }
}
