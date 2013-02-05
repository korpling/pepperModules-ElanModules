package mpi.eudico.client.annotator.search.model;

import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.search.query.viewer.EAFPopupMenu;

import mpi.search.content.model.CorpusType;


/**
 * Generic EAF-Implementation of the SearchableType-Interface. Aimed at
 * describing a minimal set of tiers present in every eaf-file; relevant when
 * searching through different eaf-files -> future purpose
 *
 * @author Alexander Klassmann
 * @version april 2004
 */
public class EAFType extends CorpusType {
    /**
     * Creates a new EAFType object.
     */
    public EAFType() {
        frameTitle = "Elan Search";
    }

    /**
     * so far eaf tiers don't contain attributes
     *
     * @param tierName name of tier with attributes
     *
     * @return dummy
     */
    public String[] getAttributeNames(String tierName) {
        return new String[0];
    }

    /**
     * return closed vocabulary for a tier
     *
     * @param tierName tier name
     *
     * @return list of ControlledVocabulary's
     */
    public List getClosedVoc(String tierName) {
        return null;
    }

    /**
     * info about tier spec
     *
     * @param tierName tier name
     *
     * @return true if only a closed vocabulary is allowed for annotations values of the tier
     */
    public boolean isClosedVoc(String tierName) {
        return false;
    }

    /**
     * don't allow to change closed vocabularies within a query
     *
     * @param closedVoc closed vocabulary
     *
     * @return always false
     */
    public boolean isClosedVocEditable(List closedVoc) {
        return true;
    }

    /**
     * returns default locale for a tier
     *
     * @param tierName tier name
     *
     * @return Locale for the tier (e.g. Chinese, IPA, etc)
     */
    public Locale getDefaultLocale(String tierName) {
        return null;
    }

    /**
     * determines the unit which should be preselected in GUI
     *
     * @return standard unit
     */
    public String getDefaultUnit() {
        return standardUnit;
    }

    /**
     * in COREX some tiers contain an index, e.g. an id for a lexical entry
     *
     * @return names of tiers that contain an index; always empty for eaf
     */
    public String[] getIndexTierNames() {
        return new String[0];
    }

    /**
     * returns a popup to specify input methods;
     * if case of eaf, it concerns the locale of the input GUI
     *
     * @return Popup
     */
    public Class getInputMethodClass() {
        return EAFPopupMenu.class;
    }

    /**
     * in COREX, some tiers have attributes with a closed set of values
     *
     * @param tierName name of tier
     * @param attributeName name of attribute
     *
     * @return always null in eaf
     */
    public Object getPossibleAttributeValues(String tierName,
        String attributeName) {
        return null;
    }

    /**
     * 
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getRelatedTiers(String tierName) {
        return tierNames;
    }

    /**
     *
     *
     * @param attributeName DOCUMENT ME!
     *
     * @return always empty String
     */
    public String getToolTipTextForAttribute(String attributeName) {
        return "";
    }

    /**
     * can search be done with quantifier 'NO'?
     *
     * @return always true for eaf
     */
    public boolean allowsQuantifierNO() {
        return true;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public boolean allowsSearchOverMultipleTiers() {
        return true;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public boolean allowsTemporalConstraints() {
        return true;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public boolean hasAttributes() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean strictCaseSensitive(String tierName) {
        return false;
    }
}
