package mpi.eudico.client.util;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Utility class for sorting the tiers of a transcription. Note: could be
 * extended by returning a DefaultMutableTreeNode.
 *
 * @author Han Sloetjes
 * @version 1.0 apr 2005
 */
public class TierSorter {
    /** Holds value of the unsorted sorting property */
    public final int UNSORTED = 0;

    /** Holds value of the sort by hierarchy sorting property */
    public final int BY_HIERARCHY = 1;

    /** Holds value of the sort by participant sorting property */
    public final int BY_PARTICIPANT = 2;

    /** Holds value of the sort by linguistic type sorting property */
    public final int BY_LINGUISTIC_TYPE = 3;

    /** A constant for unspecified participant or linguistic type */
    private final String NOT_SPECIFIED = "not specified";
    private TranscriptionImpl transcription;

    /**
     * Creates a new TierSorter instance.
     *
     * @param transcription the transcription containing the tiers to sort
     */
    public TierSorter(TranscriptionImpl transcription) {
        this.transcription = transcription;
    }

    /**
     * Returns a sorted list of the tiers. The sorting algorithm is determined
     * by the specified sorting mode.
     *
     * @param mode the sorting mode, one of BY_HIERARCHY, BY_PARTICIPANT,
     *        BY_LINGUISTIC_TYPE or UNSORTED
     *
     * @return an ArrayList containing TierImpl objects
     *
     * @see #sortTiers(int, ArrayList)
     */
    public ArrayList sortTiers(int mode) {
        return sortTiers(mode, null);
    }

    /**
     * Returns a sorted list of the tiers. The sorting algorithm is determined
     * by the specified sorting mode and is further based on the ordering in
     * the specified tier list.
     *
     * @param mode the sorting mode, one of BY_HIERARCHY, BY_PARTICIPANT,
     *        BY_LINGUISTIC_TYPE or UNSORTED
     * @param currentTierOrder a list of the 'current' or default ordering
     *
     * @return an ArrayList containing TierImpl objects
     *
     * @see #sortTiers(int)
     */
    public ArrayList sortTiers(final int mode, final ArrayList currentTierOrder) {
        ArrayList sortedTiers = new ArrayList();

        // create a list based on the current preferred order
        ArrayList tierList = null;

        if (currentTierOrder == null) {
            tierList = new ArrayList();
        } else {
            tierList = new ArrayList(currentTierOrder);
        }

        Vector allTiers = transcription.getTiers();

        for (int i = 0; i < allTiers.size(); i++) {
            TierImpl tier = (TierImpl) allTiers.elementAt(i);

            if (!tierList.contains(tier)) {
                tierList.add(tier);
            }
        }

        switch (mode) {
        case BY_HIERARCHY:

            HashMap nodes = new HashMap();
            DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
                    "Root");

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(tier);

                nodes.put(tier, n);
            }

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                if (tier.getParentTier() == null) {
                    sortedRootNode.add((DefaultMutableTreeNode) nodes.get(tier));
                } else {
                    ((DefaultMutableTreeNode) nodes.get(tier.getParentTier())).add((DefaultMutableTreeNode) nodes.get(
                            tier));
                }
            }

            Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

            // skip root
            nodeEnum.nextElement();

            while (nodeEnum.hasMoreElements()) {
                DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode) nodeEnum.nextElement();
                sortedTiers.add(nextnode.getUserObject());
            }

            break;

        case BY_PARTICIPANT:

            HashMap participantTable = new HashMap();
            ArrayList names = new ArrayList();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                String part = tier.getParticipant();

                if (part.length() == 0) {
                    part = NOT_SPECIFIED;
                }

                if (participantTable.get(part) == null) {
                    ArrayList list = new ArrayList();
                    list.add(tier);
                    participantTable.put(part, list);
                    names.add(part);
                } else {
                    ((ArrayList) participantTable.get(part)).add(tier);
                }
            }

            if (participantTable.size() > 0) {
                //Collections.sort(names);
                for (int j = 0; j < names.size(); j++) {
                    ArrayList pList = (ArrayList) participantTable.get(names.get(
                                j));

                    for (int k = 0; k < pList.size(); k++) {
                        sortedTiers.add(pList.get(k));
                    }
                }
            }

            break;

        case BY_LINGUISTIC_TYPE:

            HashMap typeTable = new HashMap();
            ArrayList types = new ArrayList();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = (TierImpl) tierList.get(i);

                LinguisticType type = tier.getLinguisticType();

                if (type == null) {
                    type = new LinguisticType(NOT_SPECIFIED);
                }

                if (typeTable.get(type) == null) {
                    ArrayList list = new ArrayList();
                    list.add(tier);
                    typeTable.put(type, list);
                    types.add(type);
                } else {
                    ((ArrayList) typeTable.get(type)).add(tier);
                }
            }

            if (typeTable.size() > 0) {
                for (int j = 0; j < types.size(); j++) {
                    ArrayList typeList = (ArrayList) typeTable.get(types.get(j));

                    for (int k = 0; k < typeList.size(); k++) {
                        sortedTiers.add(typeList.get(k));

                        //System.out.println("type sort node added: " + k + " " + ((TierTreeNode)typeList.get(k)).getTierName());
                    }
                }
            }

            break;

        case UNSORTED:
        // fallthrough default order
        default:
            sortedTiers = tierList;
        }

        /*
           try {
               for (int i = 0; i < sortedTiers.size(); i++) {
                   TierImpl t = (TierImpl) sortedTiers.get(i);
                   System.out.println("Index: " + i + " -- Tier: " + t.getName());
               }
           } catch (Exception e) {
           }
         */
        return sortedTiers;
    }
}
