/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.search.model;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import mpi.search.SearchLocale;

import mpi.eudico.util.BasicControlledVocabulary;


/**
 * $Id: ElanType.java 27519 2011-11-18 12:54:35Z hasloe $ 
 * 
 * This class describes Transcription-specific Types and Relations of
 * tiers and possible units of distance between them. It is meant for the
 * (one) Transcription open in ELAN. 
 * 
 * $Author$ $Version$
 */
public class ElanType extends EAFType {
    private Hashtable langHash = new Hashtable();

    /** Holds value of property DOCUMENT ME! */
    private final Transcription transcription;

    /**
     * The Constructor builds a tree with the tiers of the transcription as
     * nodes The root node itself is an empty node.
     *
     * @param transcription DOCUMENT ME!
     */
    public ElanType(Transcription transcription) {
        this.transcription = transcription;

        Vector tierVector = transcription.getTiers();
        tierNames = new String[tierVector.size()];
        Locale loc;
        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = (TierImpl) tierVector.elementAt(i);
            tierNames[i] = tier.getName();

            loc = tier.getDefaultLocale();
            if (loc != null) {
                langHash.put(tierNames[i],
                    loc);
            }
        }
    }

    /**
     *
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getClosedVoc(String tierName) {
        TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
        String cvName = tier.getLinguisticType().getControlledVocabylaryName();
        BasicControlledVocabulary cv = ((TranscriptionImpl) transcription).getControlledVocabulary(cvName);

        return (cv != null) ? Arrays.asList(cv.getEntries()) : null;
    }

    /**
     *
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosedVoc(String tierName) {
        TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
        if(tier == null) return false;

        String cvName = tier.getLinguisticType().getControlledVocabylaryName();
        return ((TranscriptionImpl) transcription).getControlledVocabulary(cvName) != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Locale getDefaultLocale(String tierName) {
        return (Locale) langHash.get(tierName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName1 DOCUMENT ME!
     * @param tierName2 DOCUMENT ME!
     *
     * @return array of unit tier names
     */
    public String[] getPossibleUnitsFor(String tierName1, String tierName2) {
        Vector commonAncestors = new Vector();
        String[] possibleUnits = new String[0];

        TierImpl tier1 = ((TierImpl) transcription.getTierWithId(tierName1));
        TierImpl tier2 = ((TierImpl) transcription.getTierWithId(tierName2));

        TierImpl loopTier = tier1;

        do {
            if (loopTier.equals(tier2) || tier2.hasAncestor(loopTier)) {
                commonAncestors.add(loopTier.getName() + " " +
                    SearchLocale.getString("Search.Annotation_PL"));
            }
        } while ((loopTier = (TierImpl) loopTier.getParentTier()) != null);

        possibleUnits = (String[]) commonAncestors.toArray(new String[0]);
        standardUnit = (possibleUnits.length > 0)
            ? (String) commonAncestors.get(0) : null;

        return possibleUnits;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getRelatedTiers(String tierName) {
        String[] relatedTiers = new String[0];

        try {
            TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
            TierImpl rootTier = tier.getRootTier();
            Vector dependentTiers = rootTier.getDependentTiers();

            relatedTiers = new String[dependentTiers.size() + 1];
            relatedTiers[0] = rootTier.getName();

            for (int i = 0; i < dependentTiers.size(); i++) {
                relatedTiers[i + 1] = ((TierImpl) dependentTiers.get(i)).getName();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return relatedTiers;
    }

    /**
     * Returns the transcription of this type object.
     * 
     * @return the transcription
     */
	public Transcription getTranscription() {
		return transcription;
	}
}
