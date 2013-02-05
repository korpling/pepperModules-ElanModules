package mpi.eudico.client.annotator.viewer;

import mpi.eudico.server.corpora.clom.Tier;

import java.util.List;


/**
 * Interface that gives the methods to be implemented by a viewer that can show
 * more than one Tier.
 */
public interface MultiTierViewer {
    /**
     * DOCUMENT ME!
     *
     * @param tiers DOCUMENT ME!
     */
    public void setVisibleTiers(List tiers);

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     */
    public void setActiveTier(Tier tier);

    /**
     * DOCUMENT ME!
     *
     * @param controller DOCUMENT ME!
     */
    public void setMultiTierControlPanel(MultiTierControlPanel controller);
}
