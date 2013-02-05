/*
 * Created on Feb 17, 2004
 *
 * First onset to make Elan a service provider for collaborative annotation services
 *
 */
package mpi.eudico.p2p;

import mpi.eudico.client.annotator.commands.CommandAction;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import mpi.eudico.server.corpora.clom.Transcription;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;

import net.jxta.discovery.DiscoveryService;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;

import net.jxta.endpoint.Message;

import net.jxta.exception.PeerGroupException;

import net.jxta.id.IDFactory;

import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;

import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;

import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;

import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;


/**
 * DOCUMENT ME!
 *
 * @author hennie
 */
public class ElanP2PServer implements PipeMsgListener {
    private static PeerGroup netGroup = null;
    private static DiscoveryService netDiscoSvc;

    //	private final static String SERVICE = "JXTASPEC:ELAN"; // service name

    /** Holds value of property DOCUMENT ME! */
    private final static String TAG = "DataTag"; // tag in message

    /** Holds value of property DOCUMENT ME! */
    private final static String COMMAND = "Command";

    /** Holds value of property DOCUMENT ME! */
    private final static String TRANSCRIPTION_ID = "TranscriptionID";

    /** Holds value of property DOCUMENT ME! */
    private final static String FILENAME = "pipeserver.adv"; // file containing pipe advert.
    private PeerGroup sessionGroup = null;
    private DiscoveryService sessionDiscoSvc;
    private PipeService pipeSvc;
    private InputPipe myPipe; // input pipe for the service
    private Message msg; // message received on input pipe
    private OutputPipe broadcastPipe;
    private Transcription transcription;

    /**
     * Creates a new ElanP2PServer instance
     *
     * @param theTranscription DOCUMENT ME!
     */
    public ElanP2PServer(Transcription theTranscription) {
        transcription = theTranscription;
    }

    /**
     * DOCUMENT ME!
     */
    public static void initP2P() {
        System.out.println("Setup Elan for p2p services");
        startJxta();
    }

    private static void startJxta() {
        try {
            // create, and Start the default jxta NetPeerGroup
            netGroup = PeerGroupFactory.newNetPeerGroup();
        } catch (PeerGroupException e) {
            // could not instanciate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();

            return;
        }

        // get the discovery, and pipe service
        System.out.println("Getting NetDiscoveryService");
        netDiscoSvc = netGroup.getDiscoveryService();

        //		System.out.println("Getting PipeService");
        //		pipeSvc = group.getPipeService();      
    }

    /**
     * DOCUMENT ME!
     */
    public void startServer() {
        sessionGroup = createGroup();

        if (sessionGroup != null) {
            joinGroup(sessionGroup);
        }

        try {
            System.out.println("Share " + transcription.getName() +
                " in p2p annotation session");

            sessionDiscoSvc = sessionGroup.getDiscoveryService();

            System.out.println("Getting Session PipeService");
            pipeSvc = sessionGroup.getPipeService();

            // Create the Module class advertisement associated with the service
            // We build the module class advertisement using the Advertisement
            // Factory class by passing it the type of the advertisement we
            // want to construct. The Module class advertisement is a
            // a very small advertisement that only advertises the existence
            // of service. In order to access the service, a peer will
            // have to discover the associated module spec advertisement.
            ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());

            mcadv.setName("JXTAMOD:ELAN");
            mcadv.setDescription(
                "First attempt to provide p2p annotation services");

            ModuleClassID mcID = IDFactory.newModuleClassID();
            mcadv.setModuleClassID(mcID);

            // Ok the Module Class advertisement was created, just publish
            // it in my local cache and to my peergroup. This
            // is the NetPeerGroup
            sessionDiscoSvc.publish(mcadv, DiscoveryService.ADV);
            sessionDiscoSvc.remotePublish(mcadv, DiscoveryService.ADV);

            // Create the Module Spec advertisement associated with the service
            // We build the module Spec Advertisement using the advertisement
            // Factory class by passing in the type of the advertisement we
            // want to construct. The Module Spec advertisement will contain
            // all the information necessary for a client to contact the service
            // for instance it will contain a pipe advertisement to
            // be used to contact the service
            ModuleSpecAdvertisement mdadv = (ModuleSpecAdvertisement) AdvertisementFactory.newAdvertisement(ModuleSpecAdvertisement.getAdvertisementType());

            // Setup some of the information field about the servive. In this
            // example, we just set the name, provider and version and a pipe
            // advertisement. The module creates an input pipes to listen
            // on this pipe endpoint.
            mdadv.setName(transcription.getName());
            mdadv.setVersion("Version 1.0");
            mdadv.setCreator("mpi.nl");

            ModuleSpecID id = (ModuleSpecID) IDFactory.newModuleSpecID(mcID);

            mdadv.setModuleSpecID(id);
            mdadv.setSpecURI("http://www.mpi.nl/tools");

            // Create a pipe advertisement for the Service. The client MUST use
            // the same pipe advertisement to talk to the server. When the client
            // discovers the module advertisement it will extract the pipe
            // advertisement to create its pipe.  
            PipeAdvertisement pipeadv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

            // Setup some of the information field about the servive. In this
            // example, we just set the name, provider and version and a pipe
            // advertisement. The module creates an input pipes to listen
            // on this pipe endpoint.
            pipeadv.setName("JXTA-ELAN");

            PipeID pid = (PipeID) IDFactory.newPipeID(sessionGroup.getPeerGroupID());

            pipeadv.setPipeID(pid);

            /*            System.out.println("Reading in file " + FILENAME);
               PipeAdvertisement pipeadv = null;
            
                try {
                    FileInputStream is = new FileInputStream(FILENAME);
                    pipeadv = (PipeAdvertisement)
                        AdvertisementFactory.newAdvertisement(
                        new MimeMediaType("text/xml"), is);
                    is.close();
                } catch (java.io.IOException e) {
                    System.out.println("failed to read/parse pipe advertisement");
                    e.printStackTrace();
                    System.exit(-1);
                }  */
            // add the pipe advertisement to the ModuleSpecAdvertisement
            mdadv.setPipeAdvertisement(pipeadv);

            // display the advertisement as a plain text document.
            System.out.println("Created service advertisement:");

            StructuredTextDocument doc = (StructuredTextDocument) mdadv.getDocument(new MimeMediaType(
                        "text/plain"));

            StringWriter out = new StringWriter();
            doc.sendToWriter(out);
            System.out.println(out.toString());
            out.close();

            // Ok the Module advertisement was created, just publish
            // it in my local cache and into the NetPeerGroup.
            sessionDiscoSvc.publish(mdadv, DiscoveryService.ADV);
            sessionDiscoSvc.remotePublish(mdadv, DiscoveryService.ADV);

            // We are now ready to start the service --
            // create the input pipe endpoint clients will
            // use to connect to the service
            myPipe = pipeSvc.createInputPipe(pipeadv, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Server: Error publishing the module");
        }
    }

    // By implementing PipeMsgListener, we define this method to deal with
    // messages as they occur
    public void pipeMsgEvent(PipeMsgEvent event) {
        Message msg = null;

        try {
            msg = event.getMessage();

            if (msg == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }

        // Get message
        String commandID = msg.getString(COMMAND);
        String broadcastPipeString = msg.getString("BroadcastPipe");

        if (broadcastPipeString != null) {
            try {
                InputStream is = new ByteArrayInputStream(broadcastPipeString.getBytes());
                PipeAdvertisement broadcastPipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(new MimeMediaType(
                            "text/xml"), is);
                broadcastPipe = pipeSvc.createOutputPipe(broadcastPipeAdv, 10000);

                System.out.println("broadcast pipe advertisement received: " +
                    broadcastPipeAdv.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return;
        }

        if (commandID == null) {
            System.out.println("msg received is null");
        } else {
            try {
                if (broadcastPipe != null) {
                    msg = pipeSvc.createMessage();
                    msg.setString("ACK", commandID);

                    // send the message to the service pipe
                    broadcastPipe.send(msg);
                }

                System.out.println("Received command: " + commandID +
                    " for transcription: " + transcription.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CommandAction ca = ELANCommandFactory.getCommandAction(transcription,
                commandID);

        if (ca != null) {
            ca.externalCommand(null, null);
        } else {
            System.out.println("no command could be retrieved for received msg");
        }
    }

    private PeerGroup createGroup() {
        PeerGroup pg; // new peer group
        PeerGroupAdvertisement adv; // advertisement for the new peer group

        System.out.println("Creating a new group advertisement");

        try {
            // create a new all purpose peergroup.
            ModuleImplAdvertisement implAdv = netGroup.getAllPurposePeerGroupImplAdvertisement();

            pg = netGroup.newGroup(null, // Assign new group ID
                    implAdv, // The implem. adv
                    transcription.getName(), // The name
                    "testing group adv"); // Helpful descr.

            // print the name of the group and the peer group ID
            adv = pg.getPeerGroupAdvertisement();

            PeerGroupID GID = adv.getPeerGroupID();
            System.out.println("  Group = " + adv.getName() +
                "\n  Group ID = " + GID.toString());
        } catch (Exception eee) {
            System.out.println("Group creation failed with " + eee.toString());

            return (null);
        }

        try {
            // publish this advertisement
            // (send out to other peers/rendezvous peers)
            netDiscoSvc.remotePublish(adv, DiscoveryService.GROUP);
            System.out.println("Group published successfully.\n");
        } catch (Exception e) {
            System.out.println("Error publishing group advertisement");
            e.printStackTrace();

            return (null);
        }

        return (pg);
    }

    private void joinGroup(PeerGroup grp) {
        System.out.println("Joining peer group...");

        StructuredDocument creds = null;

        try {
            // Generate the credentials for the Peer Group
            AuthenticationCredential authCred = new AuthenticationCredential(grp,
                    null, creds);

            // Get the MembershipService from the peer group
            MembershipService membership = grp.getMembershipService();

            // Get the Authenticator from the Authentication creds
            Authenticator auth = membership.apply(authCred);

            // Check if everything is okay to join the group
            if (auth.isReadyForJoin()) {
                Credential myCred = membership.join(auth);

                // what do I do with the credential it returns?
                System.out.println("Successfully joined group " +
                    grp.getPeerGroupName());

                // display the credential as a plain text document.
                System.out.println("\nCredential: ");

                StructuredTextDocument doc = (StructuredTextDocument) myCred.getDocument(new MimeMediaType(
                            "text/plain"));

                StringWriter out = new StringWriter();
                doc.sendToWriter(out);
                System.out.println(out.toString());
                out.close();
            } else {
                System.out.println("Failure: unable to join group");
            }
        } catch (Exception e) {
            System.out.println("Failure in authentication.");
            e.printStackTrace();
        }
    }
}
