package mpi.eudico.client.annotator.search.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.*;
import java.util.regex.*;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.eudico.util.TimeRelation;

import mpi.search.SearchLocale;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.*;

import mpi.search.model.SearchEngine;
import mpi.search.model.SearchListener;
import mpi.search.query.model.Query;


/**
 * The SearchEngine performs the actual search in ELAN
 *
 * @author Alexander Klassmann
 * @version Aug 2005 Identity removed
 */
public class ElanSearchEngine implements SearchEngine {
    private static final Logger logger = Logger.getLogger(ElanSearchEngine.class.getName());
    private Hashtable annotationHash = new Hashtable();
    private Hashtable patternHash = new Hashtable();
    private Hashtable relationshipHash = new Hashtable();
    private Hashtable unitTierHash = new Hashtable();
    private Transcription transcription;

    /**
     * constructor
     *
     * @param transcription
     */
    public ElanSearchEngine(SearchListener searchTool,
        Transcription transcription) {
        this.transcription = transcription;
        logger.setLevel(Level.ALL);
    }

    /**
     * Performs search
     *
     * @param query
     *
     * @throws PatternSyntaxException DOCUMENT ME!
     * @throws QueryFormulationException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public void executeThread(ContentQuery query)
        throws PatternSyntaxException, QueryFormulationException, 
            NullPointerException {
        //set unlimited size since search is done only within one transcription
        query.getResult().setPageSize(Integer.MAX_VALUE);
        initHashtables(query);

        AnchorConstraint anchorConstraint = query.getAnchorConstraint();

        String[] tierNames = anchorConstraint.getTierNames();

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = (String[]) annotationHash.keySet().toArray(new String[0]);
        }

        for (int i = 0; i < tierNames.length; i++) {
            List anchorAnnotations = (List) annotationHash.get(tierNames[i]);
            List anchorMatches;

            if (!(anchorConstraint instanceof RestrictedAnchorConstraint)) {
                int[] range = getAnnotationIndicesInScope(anchorAnnotations,
                        anchorConstraint.getLowerBoundary(),
                        anchorConstraint.getUpperBoundary(),
                        anchorConstraint.getUnit());

                anchorMatches = getMatches(null,
                        (Pattern) patternHash.get(anchorConstraint),
                        anchorConstraint.getId(), anchorAnnotations, range);
            } else {
                anchorMatches = ((RestrictedAnchorConstraint) anchorConstraint).getResult()
                                 .getMatches(tierNames[i]);
            }

            filterDependentConstraints(anchorMatches, anchorConstraint);

            for (int j = 0; j < anchorMatches.size(); j++) {
            	ElanMatch em = (ElanMatch) anchorMatches.get(j);
            	em.setFileName(((TranscriptionImpl) transcription).getPathName());
            	query.getResult().addMatch(em);
                //query.getResult().addMatch((ElanMatch) anchorMatches.get(j));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void performSearch(Query query) throws Exception {
        executeThread((ContentQuery) query);
    }

    /**
     * same as getAnnotationIndicesInScope(...) with distance 0
     *
     * @param annotationList
     * @param intervalBegin
     * @param intervalEnd
     * @param timeComparisonMode
     *
     * @return int[]
     */
    private static int[] getAnnotationIndicesInScope(List annotationList,
        long intervalBegin, long intervalEnd, String timeComparisonMode) {
        return getAnnotationIndicesInScope(annotationList, intervalBegin,
            intervalEnd, 0L, timeComparisonMode);
    }

    /**
     * returns indices of annotations that fulfull the time constraint the
     * parameter "distance" is used only for particular timeComparisonModes
     *
     * @param annotationList
     * @param intervalBegin
     * @param intervalEnd
     * @param distance
     * @param timeComparisonMode
     *
     * @return int[]
     */
    private static int[] getAnnotationIndicesInScope(List annotationList,
        long intervalBegin, long intervalEnd, long distance,
        String timeComparisonMode) {
        int[] annotationsInInterval = new int[annotationList.size()];
        int index = 0;

        for (int i = 0; i < annotationList.size(); i++) {
            Annotation annotation = (Annotation) annotationList.get(i);
            boolean constraintFulfilled = false;

            if (Constraint.OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlaps(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.IS_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NO_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.doesNotOverlap(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.NOT_INSIDE.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isNotInside(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.LEFT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnLeftSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.RIGHT_OVERLAP.equals(timeComparisonMode)) {
                constraintFulfilled = TimeRelation.overlapsOnRightSide(annotation,
                        intervalBegin, intervalEnd);
            } else if (Constraint.WITHIN_OVERALL_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinDistance(annotation,
                        intervalBegin, intervalEnd, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_LEFT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.WITHIN_DISTANCE_TO_RIGHT_BOUNDARY.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isWithinRightDistance(annotation,
                        intervalEnd, distance);
            } else if (Constraint.BEFORE_LEFT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isBeforeLeftDistance(annotation,
                        intervalBegin, distance);
            } else if (Constraint.AFTER_RIGHT_DISTANCE.equals(
                        timeComparisonMode)) {
                constraintFulfilled = TimeRelation.isAfterRightDistance(annotation,
                        intervalEnd, distance);
            }

            if (constraintFulfilled) {
                annotationsInInterval[index++] = i;
            }
        }

        int[] range = new int[index];
        System.arraycopy(annotationsInInterval, 0, range, 0, index);

        return range;
    }

    /**
     * Returns list with the annotations (not their indices!) in constraint
     * tier within specified range
     *
     * @param lowerBoundary
     * @param upperBoundary
     * @param unitTier
     * @param unitAnnotations
     * @param relationship
     * @param centralAnnotation
     *
     * @return List
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    private static List getAnnotationsInScope(long lowerBoundary,
        long upperBoundary, TierImpl unitTier, List unitAnnotations,
        TierImpl[] relationship, Annotation centralAnnotation)
        throws NullPointerException {
        List annotationsInScope = new ArrayList();
        Annotation centralUnitAnnotation = centralAnnotation;

        while ((centralUnitAnnotation.getTier() != unitTier) &&
                (centralUnitAnnotation != null)) {
            centralUnitAnnotation = centralUnitAnnotation.getParentAnnotation();
        }

        if (centralUnitAnnotation == null) {
            throw new NullPointerException();
        }

        int unitAnnotationIndex = unitAnnotations.indexOf(centralUnitAnnotation);

        int[] unitAnnotationIndicesInScope = getRangeForTier(unitTier,
                lowerBoundary, upperBoundary, unitAnnotationIndex);

        Annotation rootOfCentralAnnotation = centralUnitAnnotation;

        while (rootOfCentralAnnotation.hasParentAnnotation()) {
            rootOfCentralAnnotation = rootOfCentralAnnotation.getParentAnnotation();
        }

        logger.log(Level.FINE,
            "Unit annotation " + centralUnitAnnotation.getValue());

        Annotation unitAnnotation;

        for (int k = 0; k < unitAnnotationIndicesInScope.length; k++) {
            unitAnnotation = (Annotation) unitAnnotations.get(unitAnnotationIndicesInScope[k]);

            boolean haveSameRoot = true;

            if (unitAnnotation.hasParentAnnotation()) {
                Annotation rootOfUnitAnnotation = unitAnnotation;

                while (rootOfUnitAnnotation.hasParentAnnotation()) {
                    rootOfUnitAnnotation = rootOfUnitAnnotation.getParentAnnotation();
                }

                haveSameRoot = rootOfUnitAnnotation == rootOfCentralAnnotation;
            }

            if (haveSameRoot) {
                annotationsInScope.addAll(getDescAnnotations(unitAnnotation,
                        relationship));
            }
        }

        return annotationsInScope;
    }

    /**
     * gets all descendant annotations (e.g. children of children etc.)
     *
     * @param ancestorAnnotation
     * @param relationship
     *
     * @return Vector
     */
    private static List getDescAnnotations(Annotation ancestorAnnotation,
        TierImpl[] relationship) {
        List childAnnotations = new ArrayList();
        List parentAnnotations = new ArrayList();
        parentAnnotations.add(ancestorAnnotation);

        for (int r = relationship.length - 1; r >= 0; r--) {
            childAnnotations = new ArrayList();

            try {
                for (int i = 0; i < parentAnnotations.size(); i++) {
                    childAnnotations.addAll(((Annotation) parentAnnotations.get(
                            i)).getChildrenOnTier(relationship[r]));
                }
            } catch (Exception re) {
                re.printStackTrace();

                return new ArrayList();
            }

            parentAnnotations = childAnnotations;
        }

        return parentAnnotations;
    }

    /**
     * get all (pattern) matches in a tier
     *
     * @param parentMatch DOCUMENT ME!
     * @param pattern
     * @param constraintId DOCUMENT ME!
     * @param annotationList
     * @param range subindices
     *
     * @return int[]
     */
    private static List getMatches(ElanMatch parentMatch, Pattern pattern,
        String constraintId, List annotationList, int[] range) {
        List matchList = new ArrayList();

        for (int i = 0; i < range.length; i++) {
            Annotation annotation = (Annotation) annotationList.get(range[i]);
            Matcher matcher = pattern.matcher(annotation.getValue());

            if (matcher.find()) {
                List substringIndices = new ArrayList();

                do {
                    substringIndices.add(new int[] {
                            matcher.start(0), matcher.end(0)
                        });
                } while (matcher.find());

                ElanMatch match = new ElanMatch(parentMatch, annotation,
                        constraintId, range[i],
                        (int[][]) substringIndices.toArray(new int[0][0]));

                if (range[i] > 0) {
                    match.setLeftContext(((Annotation) annotationList.get(range[i] -
                            1)).getValue());
                }

                if ((match.getIndex() + 1) < annotationList.size()) {
                    match.setRightContext(((Annotation) annotationList.get(range[i] +
                            1)).getValue());
                }

                //add parent
                if(((Annotation) annotationList.get(i)).hasParentAnnotation()){
                	match.setParentContext(((Annotation) annotationList.get(i)).getParentAnnotation().getValue());
                }
                
                //add children
                TierImpl tier=(TierImpl)((Annotation) annotationList.get(i)).getTier();
                match.setChildrenContext(constructChildrenString(tier.getChildTiers(), ((Annotation) annotationList.get(i))));
                
                matchList.add(match);
            }
        }

        return matchList;
    }

    /**
     * Method to construct a string with the children of a particular annotation mod. Coralie Villes
     * @param tiers the tier list of children
     * @param annotation the current annotation
     * @return String representation of annotation's children
     */
    public static String constructChildrenString(List tiers, Annotation annotation){
    	String childrenBuffer="";
    	if (!tiers.isEmpty()){
    		List<String> childrenContextList=new ArrayList<String>();
    		for(int j=0; j<tiers.size(); j++){
    			List<Annotation> children =annotation.getChildrenOnTier((Tier)tiers.get(j));
    			Collections.sort(children);
    			childrenBuffer+="[";
    			for(Annotation child : children){
    				childrenBuffer+=child.getValue()+" ";
    			}
    			childrenBuffer+="] ";
    		}
    	}
    	return childrenBuffer;
    }
    
    /**
     * computes intersection of range and [0..tier.size] returns array of the
     * integers in this intersection
     *
     * @param tier
     * @param lowerBoundary
     * @param upperBoundary
     * @param center
     *
     * @return int[]
     */
    private static int[] getRangeForTier(TierImpl tier, long lowerBoundary,
        long upperBoundary, int center) {
        int newLowerBoundary = (lowerBoundary == Long.MIN_VALUE) ? 0
                                                                 : (int) Math.max(0,
                center + lowerBoundary);
        int newUpperBoundary = (upperBoundary == Long.MAX_VALUE)
            ? (tier.getNumberOfAnnotations() - 1)
            : (int) Math.min(tier.getNumberOfAnnotations() - 1,
                center + upperBoundary);

        int[] range = new int[-newLowerBoundary + newUpperBoundary + 1];

        for (int i = 0; i < range.length; i++) {
            range[i] = i + newLowerBoundary;
        }

        return range;
    }

    /**
     * returns array of all Tiers between ancester and descendant tier,
     * including descendTier, excluding ancestorTier; empty, if ancestorTier
     * == descendTier
     *
     * @param ancesterTier
     * @param descendTier
     *
     * @return TierImpl[]
     */
    private static TierImpl[] getRelationship(TierImpl ancesterTier,
        TierImpl descendTier) {
        List relationship = new ArrayList();
        TierImpl parentTier = descendTier;

        try {
            if (descendTier.hasAncestor(ancesterTier)) {
                while (!ancesterTier.equals(parentTier)) {
                    relationship.add(parentTier);
                    parentTier = (TierImpl) parentTier.getParentTier();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (TierImpl[]) relationship.toArray(new TierImpl[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param match
     * @param constraint
     *
     * @return DOCUMENT ME!
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    private List getChildMatches(ElanMatch match, Constraint constraint)
        throws NullPointerException {
        TierImpl unitTier = null;
        List unitAnnotations = null;
        List constraintAnnotations = null;
        TierImpl[] relShip = null;

        long lowerBoundary = constraint.getLowerBoundary();
        long upperBoundary = constraint.getUpperBoundary();
        Pattern pattern = (Pattern) patternHash.get(constraint);
        // HS Nov 2011: added support for multiple "child" tiers
        String[] tierNames = constraint.getTierNames();
        
        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tierNames = (String[]) annotationHash.keySet().toArray(new String[0]);
        }
        
        List allMatches = new ArrayList();
        
        for (String name : tierNames) {      
	        constraintAnnotations = (List) annotationHash.get(name);
	
	        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
	            unitTier = (TierImpl) unitTierHash.get(constraint);
	
	            unitAnnotations = (List) annotationHash.get(unitTier.getName());
	
	            relShip = (TierImpl[]) relationshipHash.get(constraint);
	        }
	
	        List annotationsInScope;
	        int[] annotationIndicesInScope;
	        Annotation annotation = match.getAnnotation();
	
	        if (Constraint.TEMPORAL.equals(constraint.getMode())) {
	            annotationIndicesInScope = getAnnotationIndicesInScope(constraintAnnotations,
	                    annotation.getBeginTimeBoundary(),
	                    annotation.getEndTimeBoundary(), upperBoundary,
	                    constraint.getUnit());
	        } else {
	            annotationsInScope = getAnnotationsInScope(lowerBoundary,
	                    upperBoundary, unitTier, unitAnnotations, relShip,
	                    annotation);
	
	            annotationIndicesInScope = new int[annotationsInScope.size()];
	
	            for (int j = 0; j < annotationsInScope.size(); j++) {
	                annotationIndicesInScope[j] = constraintAnnotations.indexOf(annotationsInScope.get(
	                            j));
	                logger.log(Level.FINE,
	                    "Constraint annotation: " +
	                    ((Annotation) annotationsInScope.get(j)).getValue());
	            }
	        }
	
	        List matches = getMatches(match, pattern, constraint.getId(),
	                constraintAnnotations, annotationIndicesInScope);
	
	        filterDependentConstraints(matches, constraint);
	        allMatches.addAll(matches);
        }
        
        return allMatches;
    }

    private void fillAnnotationHash(Constraint constraint)
        throws QueryFormulationException {
        String[] tierNames = constraint.getTierNames();
        TierImpl[] tiers;

        if (tierNames[0].equals(Constraint.ALL_TIERS)) {
            tiers = (TierImpl[]) transcription.getTiers().toArray(new TierImpl[0]);
        } else {
            tiers = new TierImpl[tierNames.length];

            for (int i = 0; i < tierNames.length; i++) {
                tiers[i] = (TierImpl) transcription.getTierWithId(tierNames[i]);

                if (tiers[i] == null) {
                    throw new QueryFormulationException(SearchLocale.getString(
                            "Search.Exception.CannotFindTier") + " '" +
                        tierNames[i] + "'");
                }
            }
        }

        for (int i = 0; i < tiers.length; i++) {
            annotationHash.put(tiers[i].getName(), tiers[i].getAnnotations());
        }

        //find unit tiers for dependent constraints
        if (Constraint.STRUCTURAL.equals(constraint.getMode())) {
            String tierName = constraint.getUnit().substring(0,
                    constraint.getUnit().lastIndexOf(' '));

            TierImpl unitTier = (TierImpl) transcription.getTierWithId(tierName);

            if (unitTier == null) {
                throw new QueryFormulationException(SearchLocale.getString(
                        "Search.Exception.CannotFindTier") + " '" + tierName +
                    "'");
            }

            unitTierHash.put(constraint, unitTier);
            relationshipHash.put(constraint, getRelationship(unitTier, tiers[0]));

            if (!annotationHash.containsKey(tierName)) {
                List annotations = unitTier.getAnnotations();
                annotationHash.put(tierName, annotations);
            }
        }
    }

    /*
     * traverse whole tree
     */
    private void fillHashes(CorpusType type, Constraint constraint)
        throws QueryFormulationException {
        for (Enumeration e = constraint.children(); e.hasMoreElements();) {
            fillHashes(type, (Constraint) e.nextElement());
        }

        fillAnnotationHash(constraint);
        patternHash.put(constraint, Utilities.getPattern(constraint, type));
    }

    private void filterDependentConstraints(List startingMatches,
        Constraint constraint) throws NullPointerException {
        for (Enumeration e = constraint.children(); e.hasMoreElements();) {
            int j = 0;

            Constraint childConstraint = (Constraint) e.nextElement();

            while (j < startingMatches.size()) {
                ElanMatch match = (ElanMatch) startingMatches.get(j);

                List childMatches = getChildMatches(match, childConstraint);
                /*
                if (((childConstraint.getQuantifier() == Constraint.ANY) &&
                        (childMatches.size() > 0)) ||
                        ((childConstraint.getQuantifier() == Constraint.NONE) &&
                        (childMatches.size() == 0))) {
                        */
                // HS 03-2008 replaced the "==" equality test by equals because e.g. when a query
                // has been read from file the constants are not always used. All other equality 
                // tests in this class are also performed using equals.
                if (((Constraint.ANY.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() > 0)) ||
                        ((Constraint.NONE.equals(childConstraint.getQuantifier())) &&
                        (childMatches.size() == 0))) {
                    j++;
                    match.addChildren(childMatches);
                } else {
                    startingMatches.remove(j);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @throws QueryFormulationException
     * @throws PatternSyntaxException
     */
    private void initHashtables(ContentQuery query)
        throws QueryFormulationException, PatternSyntaxException {
        patternHash.clear();
        annotationHash.clear();
        unitTierHash.clear();
        relationshipHash.clear();

        fillHashes(query.getType(), query.getAnchorConstraint());
    }
}
