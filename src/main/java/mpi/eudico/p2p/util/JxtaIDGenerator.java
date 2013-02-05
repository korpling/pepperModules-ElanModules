/*
 * Created on Feb 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.p2p.util;

import net.jxta.id.IDFactory;

import net.jxta.pipe.PipeID;

import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;


/**
 * DOCUMENT ME!
 *
 * @author hennie To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class JxtaIDGenerator {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        ModuleClassID mcID = IDFactory.newModuleClassID();

        ModuleSpecID msID = IDFactory.newModuleSpecID(mcID);

        PipeID pID = IDFactory.newPipeID(IDFactory.newPeerGroupID());

        System.out.println("module class id: " + mcID);
        System.out.println("module spec id: " + msID);
        System.out.println("pipe id: " + pID);
    }
}
