package mpi.eudico.client.util;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Created on Apr 2, 2004
 *
 * @author Alexander Klassmann
 * @version Apr 2, 2004
 * @version Aug 2005 Identity removed
 */
public class TierTree {
    private DefaultMutableTreeNode[] nodes;

    /**
     * Creates a new TierTree instance
     *
     * @param transcription DOCUMENT ME!
     * @param identity DOCUMENT ME!
     */
    public TierTree(Transcription transcription) {
        Hashtable tierNodes = new Hashtable();

        Vector tierVector = transcription.getTiers();
        nodes = new DefaultMutableTreeNode[tierVector.size() + 1];
        nodes[0] = new DefaultMutableTreeNode();

        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = (TierImpl) tierVector.elementAt(i);
            nodes[i + 1] = new DefaultMutableTreeNode(tier.getName());
            tierNodes.put(tier, nodes[i + 1]);
        }

        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = (TierImpl) tierVector.elementAt(i);

            if (tier.hasParentTier()) {
                if ((DefaultMutableTreeNode) tierNodes.get(tier.getParentTier()) != null) {
                    ((DefaultMutableTreeNode) tierNodes.get(tier.getParentTier())).add(nodes[i +
                        1]);
                }
            } else {
                nodes[0].add(nodes[i + 1]);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DefaultMutableTreeNode getTree() {
        return nodes[0];
    }
}
