package mpi.eudico.client.annotator.svg;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Stores the GraphicNode2D objects belonging to one tier. Since the tier
 * allows graphic references it should be time-aligned.  The tier can be
 * visible or not and can be the active tier or not.<br>
 * Note: this class does not extend Tier2D because it stores a different
 * kind of objects (tags - nodes).
 *
 * @author Han Sloetjes
 * @version 0.1 17 feb 2004
 * @version 0.2 may 2004
 */
public class GraphicTier2D {
    private TierImpl tier;
    private ArrayList nodeList;
    private boolean isActiveTier;
    private boolean isVisible;
    
    /** 
     * On every controller update there is a search for the graphical annotation 
     * at the current media time, if any. To speed up this process the index of the 
     * last used annotation is stored; on the next controller update a check can be 
     * performed whether the media time is still within the current annotation's 
     * boundaries. 
     */
    private int currentIndex = -1;

    /**
     * Creates a new GraphicTier2D instance
     *
     * @param tier the TierImpl enclosed by this Tier2D
     */
    public GraphicTier2D(TierImpl tier) {
        this.tier = tier;
        isActiveTier = false;
        isVisible = true;
        nodeList = new ArrayList(20);
    }

    /**
     * Returns the TierImpl enclosed by this Tier2D.
     *
     * @return the TierImpl enclosed by this Tier2D
     */
    public TierImpl getTier() {
        return tier;
    }

    /**
     * Returns an Iterator of the list of nodes.
     *
     * @return an iterator of the list of nodes
     */
    public Iterator getNodes() {
        return nodeList.iterator();
    }

    /**
     * Returns the ArrayList containing the Node2D objects on this Tier2D.
     *
     * @return the ArrayList containing the Node2D objects on this Tier2D.
     */
    public ArrayList getNodeList() {
        return nodeList;
    }

    /**
     * Inserts a Node2D object according to its natural ordering.<br>
     * It is assumed that annotations don't overlap, the ordering is based on
     * increasing begin time values.
     *
     * @param node2d inserts a Node2D object at the right index
     */
    public void insertNode(GraphicNode2D node2d) {
        if (node2d != null) {
            node2d.setTier2D(this);

            if (nodeList.size() == 0) {
                nodeList.add(node2d);
            } else {
                int pos = Collections.binarySearch(nodeList, node2d);

                if (pos < 0) { // should be

                    if (-pos > nodeList.size()) {
                        nodeList.add(node2d);
                    } else {
                        nodeList.add(-pos - 1, node2d);
                    }
                } else {
                    //System.out.println("List already contains this node");
                }
            }
        }
    }

    /**
     * Removes a Node2D from this Tier2D.
     *
     * @param node2d the node to remove
     */
    public void removeNode(GraphicNode2D node2d) {
        if (node2d != null) {
            nodeList.remove(node2d);
            // reset the index
            currentIndex = -1;
        }
    }

    /**
     * Marks this Tier/Tier2D as the active tier.
     *
     * @param active true if this is the active tier, false otherwise
     */
    public void setActive(boolean active) {
        isActiveTier = active;
    }

    /**
     * Returns whether or not this is the active tier.
     *
     * @return true if this as the active tier, false otherwise
     */
    public boolean isActive() {
        return isActiveTier;
    }

    /**
     * Returns whether this tier/tier2d is currently visible.
     *
     * @return trtue if this tier is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Sets the visible state of this tier/tier2d.
     *
     * @param visible true if this tier is visible, false otherwise
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }
	/**
	 * Returns the index of last used/painted annotation.
	 * 
	 * @return the index of the last used annotation
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Sets the index of the last used annotation.
	 * @param index the index of the last used annotation
	 */
	public void setCurrentIndex(int index) {
		if ( index < nodeList.size()) {
			currentIndex = index;
		} else {
			currentIndex = -1;
		}		
	}

}
