/*
 * Created on Feb 17, 2004
 *
 * First onset to make Elan a service provider for collaborative annotation services
 *
 */
package mpi.eudico.p2p;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.StructuredDocument;
import net.jxta.document.MimeMediaType;
import net.jxta.document.Element;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.endpoint.Message;
import net.jxta.pipe.PipeService;
import net.jxta.id.IDFactory;
import net.jxta.impl.util.BidirectionalPipeService;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.pipe.InputPipe;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;


import mpi.eudico.client.annotator.*;
import mpi.eudico.client.annotator.commands.*;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * @author hennie
 *
 */
public class ElanP2P implements PipeMsgListener {
//public class ElanP2P implements BidirectionalPipeService.MessageListener, Runnable {

	private static boolean jxtaInitialized = false;
	private static PeerGroup netGroup = null;
	private static DiscoveryService netDiscoSvc;
	private PeerGroup sessionGroup = null;
	private DiscoveryService sessionDiscoSvc;
	private PipeService pipeSvc;
	private InputPipe inputPipe; // input pipe for the service
	private OutputPipe outputPipe;
	private Message msg;      // message received on input pipe
//	private final static String SERVICE = "JXTASPEC:ELAN"; // service name
	private final static String TAG = "DataTag";               // tag in message
	private final static String COMMAND = "Command";
	private final static String ELAN_COMMAND = "elanCommand";
	private final static String PARAMETER1 = "parameter1";
	private final static String PARAMETER2 = "parameter2";
	private final static String SET_EAF = "setEAF";
	private final static String EAF_STRING = "EAFString";
	private final static String INPIPE_OFFERED = "InpipeOffered";
	private final static String PARTICIPANT_MAIL = "ParticipantMail";
	private final static String PARTICIPANT_NAME = "ParticipantName";
	private final static String GET_PARTICIPANTS = "GetParticipants";
	private final static String ADD_PARTICIPANT = "AddParticipant";
	private final static String TRANSCRIPTION_ID = "TranscriptionID";
	private final static String FILENAME = "pipeserver.adv";   // file containing pipe advert.
	private final static String REQUEST_CONTROL = "RequestControl";
	private final static String LEAVE_SESSION = "LeaveSession";
	private final static String GET_SESSION_INFO = "GetSessionInfo";
	private final static String SESSION_INFO = "SessionInfo";
	
	private OutputPipe broadcastPipe;

	private BidirectionalPipeService.Pipe pipe = null;
	private BidirectionalPipeService.AcceptPipe acceptPipe = null;

	private Transcription transcription;
	private ViewerManager2 viewerManager;
	private ElanFrame2 frame;
	private ElanLayoutManager layoutManager;
	private P2P2Here p2p2Here;
	private CollaborationPanel collaborationPanel;

	private Hashtable outputPipeHash;

	private boolean hasControl = false;

	private String localEmail;
	private String localName;

	/**
	 * Constructor for P2P client
	 * use setManagers for ViewerManager2 and LayoutManager after they are constructed in init elan
	 * p2p initiator already has an .eaf open so all managers are available
	 * ElanFrame is needed by p2p client for openEAF(String fullPath)
	 */
	public ElanP2P(ElanFrame2 frame) {
		this.frame = frame;

		// do init in here?
		initP2P();
	}

	public void setManagers(ViewerManager2 viewerManager, ElanLayoutManager layoutManager) {
			this.viewerManager = viewerManager;
			this.layoutManager = layoutManager;
			transcription = viewerManager.getTranscription();
			p2p2Here = new P2P2Here(this, viewerManager, frame);

			collaborationPanel = new CollaborationPanel(this);
			populateCollaborationPanel();

			layoutManager.add(collaborationPanel);
	}

	public ElanP2P(Transcription theTranscription) {
		transcription = theTranscription;
	}

	private void initP2P() {
		if (!jxtaInitialized) {
			System.out.println("Setup Elan for p2p services");
			startJxta();
			jxtaInitialized = true;
		}
	}

	public void sendCommand(String command) {
		sendCommand(command, "", "");
	}

	public void sendCommand(String command, String parameter1) {
		sendCommand(command, parameter1, "");
	}

	public void sendCommand(String command, String parameter1, String parameter2) {
		sendCommand(COMMAND, command, parameter1, parameter2);
	}

	public void sendElanCommand(String command) {
		sendElanCommand(command, "", "");
	}

	public void sendElanCommand(String command, String parameter1) {
		sendElanCommand(command, parameter1, "");
	}

	public void sendElanCommand(String command, String parameter1, String parameter2) {
		if (hasControl) {
			sendCommand(ELAN_COMMAND, command, parameter1, parameter2);
		}
	}


	private void sendCommand(String type, String command, String parameter1, String parameter2) {
		if (pipeSvc == null) {	// not yet connected
			return;
		}

		try {
			// just to be sure
			if (parameter1 == null) {
				parameter1 = "";
			}

			// just to be sure
			if (parameter2 == null) {
				parameter2 = "";
			}

			Message msg = pipeSvc.createMessage();
			msg.setString(type, command);
			msg.setString(PARAMETER1, parameter1);
			msg.setString(PARAMETER2, parameter2);
			if (outputPipe != null) {
				outputPipe.send(msg);
			}

			System.out.println("sent command: " + command + " par1 = " + parameter1 + " par2 = " + parameter2 + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public void startServer(String theName, String theEmail) {
		hasControl = true;

		localName = theName;
		localEmail = theEmail;

		populateCollaborationPanel();

		sessionGroup = createGroup();
				if (sessionGroup != null)
					joinGroup(sessionGroup);
		try {
			System.out.println("Share " + transcription.getName() + " in p2p annotation session");
			
			collaborationPanel.setSessionName(transcription.getName());
			collaborationPanel.setChairName(localEmail);
			collaborationPanel.setSharedDocumentName(transcription.getName());
			
			sessionDiscoSvc = sessionGroup.getDiscoveryService();

//			System.out.println("Getting Session PipeService");
//			pipeSvc = sessionGroup.getPipeService();
			if (pipeSvc == null) {
				System.out.println("Getting Session PipeService");
				pipeSvc = sessionGroup.getPipeService();
			}

			// Create the Module class advertisement associated with the service
			// We build the module class advertisement using the Advertisement
			// Factory class by passing it the type of the advertisement we
			// want to construct. The Module class advertisement is a
			// a very small advertisement that only advertises the existence
			// of service. In order to access the service, a peer will
			// have to discover the associated module spec advertisement.
			ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement)
				AdvertisementFactory.newAdvertisement(
				ModuleClassAdvertisement.getAdvertisementType());

			mcadv.setName("JXTAMOD:ELAN");
			mcadv.setDescription("First attempt to provide p2p annotation services");

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

			ModuleSpecAdvertisement mdadv = (ModuleSpecAdvertisement)
				AdvertisementFactory.newAdvertisement(
				ModuleSpecAdvertisement.getAdvertisementType());

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
			PipeAdvertisement pipeadv = (PipeAdvertisement)
				AdvertisementFactory.newAdvertisement(
				PipeAdvertisement.getAdvertisementType());

			// Setup some of the information field about the servive. In this
			// example, we just set the name, provider and version and a pipe
			// advertisement. The module creates an input pipes to listen
			// on this pipe endpoint.

			pipeadv.setName("JXTA-ELAN");
			PipeID pid = (PipeID) IDFactory.newPipeID(sessionGroup.getPeerGroupID());

			pipeadv.setPipeID(pid);

		/*	BidirectionalPipeService pipeService = new BidirectionalPipeService(sessionGroup);
			BidirectionalPipeService.AcceptPipe acceptPipe = pipeService.bind("JXTA-ELAN");
			PipeAdvertisement pipeadv = acceptPipe.getAdvertisement(); */

			// add the pipe advertisement to the ModuleSpecAdvertisement
			mdadv.setPipeAdvertisement(pipeadv);

			// display the advertisement as a plain text document.
			System.out.println("Created service advertisement:");
			StructuredTextDocument doc = (StructuredTextDocument)
			mdadv.getDocument(new MimeMediaType("text/plain"));

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
			inputPipe = pipeSvc.createInputPipe(pipeadv, this);

//			new Thread(this).start();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Server: Error publishing the module");
		}
	}


/*	public void run() {
		while (null == pipe) {
			try {
				pipe = acceptPipe.accept(30000, this);

			} catch (Exception e) {	// InterruptedException or IOException
				// e.printStackTrace();
			}
		}
	} */

	public void startClient(String key, String theName, String theEmail) {
		System.out.println("Starting Client peer ....");

		hasControl = false;

		localName = theName;
		localEmail = theEmail;

		PeerGroup sessionGroup = discoverGroup(key);
		if (sessionGroup == null) {
			// hardcoded message string...
			JOptionPane.showMessageDialog(frame, "The requested document was not found",
				"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (sessionDiscoSvc == null) {
			sessionDiscoSvc = sessionGroup.getDiscoveryService();
		}

		if (pipeSvc == null) {
			System.out.println("Getting Session PipeService");
			pipeSvc = sessionGroup.getPipeService();
		}

		connectToDocument(sessionGroup);

		offerInputPipe(sessionGroup);

//		buildPipeInfrastructure();

		//sendCommands();

	}

	private PeerGroup discoverGroup(String key) {
		String groupName = "";
		Enumeration en = null;
		PeerGroup sessionPeerGroup = null;

		// read group name from command line
	//	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	//	System.out.println("Type key: ");
		try {
		//	if ((groupName = br.readLine()) != null) {
			if (key != null) {
				groupName = key;
				System.out.println("Searching for the " + groupName + " Group");

				final int timeout = 20000;
				long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start <= timeout) {
					try {

						// let's look first in our local cache to see
						// if we have it! We try to discover an advertisement
						// which has the (Name, JXTASPEC:JXTA-EX1) tag value
						//
						en = netDiscoSvc.getLocalAdvertisements(DiscoveryService.GROUP
						, "Name"
						, groupName);

						// Found it! Stop searching and go send a message.
						if ((en != null) && en.hasMoreElements()) break;

						// We could not find anything in our local cache, so let's send a
						// remote discovery request searching for the service advertisement
						netDiscoSvc.getRemoteAdvertisements(null
						, DiscoveryService.GROUP
						, "Name"
						, groupName,1, null);

						// The discovery is asynchronous as we do not know
						// how long is going to take
						try { // sleep as much as we want. Yes we
							// could implement asynchronous listener pipe...
							Thread.sleep(2000);
						} catch (Exception e){
						}

					} catch (IOException e){
						// found nothing!  move on
					}

					System.out.print(".");
				}
				if (en == null || !en.hasMoreElements()) {
					System.out.println("\nNo group found");
					return null;
				}

				System.out.println("We found the group!");

				String str=null;
				PeerGroupAdvertisement pgAdv=null;

				while (en.hasMoreElements()) {

					try {
						pgAdv = (PeerGroupAdvertisement) en.nextElement();

						// let's print the advertisement as a plain text document
						StructuredTextDocument doc = (StructuredTextDocument)
								pgAdv.getDocument(new MimeMediaType("text/plain"));

						StringWriter out = new StringWriter();
						doc.sendToWriter(out);
						System.out.println(out.toString());
						out.close();

						System.out.println(" Peer Group = " + pgAdv.getName());

						if (pgAdv.getName().equals(groupName))  {
							try {
								sessionPeerGroup = netGroup.newGroup(pgAdv);
							} catch (PeerGroupException e){
								System.out.println("error creating PeerGroup from advertisement");
							}
							joinGroup(sessionPeerGroup);

						}
					}
					catch (java.io.IOException e) {
						// got a bad response. continue to the next response
						System.out.println("error parsing response element");
						e.printStackTrace();
						continue;
					}

				} // end while
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sessionPeerGroup;
	}

	private void connectToDocument(PeerGroup sessionPeerGroup) {
		if (sessionPeerGroup == null) {
			System.out.println("Peer group for session not avaible");
			return;
		}

		String groupName = sessionPeerGroup.getPeerGroupName();

		// let's try to locate the service advertisement groupName
		// we will loop until we find it!
		System.out.println("Searching for the " + groupName + " Service advertisement");
		Enumeration en = null;
		while (true) {
			try {

				// let's look first in our local cache to see
				// if we have it! We try to discover an advertisement
				// which has the (Name, JXTASPEC:JXTA-EX1) tag value
				//
				en = sessionDiscoSvc.getLocalAdvertisements(DiscoveryService.ADV
				, "Name"
				, groupName);

				// Found it! Stop searching and go send a message.
				if ((en != null) && en.hasMoreElements()) break;

				// We could not find anything in our local cache, so let's send a
				// remote discovery request searching for the service advertisement
				sessionDiscoSvc.getRemoteAdvertisements(null
				, DiscoveryService.ADV
				, "Name"
				, groupName,1, null);

				// The discovery is asynchronous as we do not know
				// how long is going to take
				try { // sleep as much as we want. Yes we
					// could implement asynchronous listener pipe...
					Thread.sleep(2000);
				} catch (Exception e){
				}

			} catch (IOException e){
				// found nothing!  move on
			}

			System.out.print(".");
		}

		System.out.println("We found the service advertisement:");

		// Ok get the service advertisement as a ModuleSpecAdvertisement
		ModuleSpecAdvertisement mdsadv = (ModuleSpecAdvertisement) en.nextElement();
		try {

			// let's print the advertisement as a plain text document
			StructuredTextDocument doc = (StructuredTextDocument)
			mdsadv.getDocument(new MimeMediaType("text/plain"));

			StringWriter out = new StringWriter();
			doc.sendToWriter(out);
			System.out.println(out.toString());
			out.close();

			// Get the pipe advertisement -- need it to talk to the service
			PipeAdvertisement pipeadv = mdsadv.getPipeAdvertisement();

			if (pipeadv == null){
				System.out.println("Error -- Null pipe advertisement!");
				System.exit(1);
			}

			// create the output pipe endpoint to connect
			// to the server, try 3 times to bind the pipe endpoint to
			// the listening endpoint pipe of the service
			outputPipe = null;
			for (int i=0; i<3; i++) {

				System.out.println("Trying to bind to pipe...");
				try {
					outputPipe = pipeSvc.createOutputPipe(pipeadv, 10000);
					break;
				} catch (java.io.IOException e) {
					// go try again;
				}
			}
			if (outputPipe == null) {
				System.out.println("Error resolving pipe endpoint");
				System.exit(1);
			}
			else {
				if (outputPipeHash == null) {
					outputPipeHash = new Hashtable();
				}
				outputPipeHash.put("KNOEP", outputPipe);
			}

/*			BidirectionalPipeService.Pipe pipe = null;
			BidirectionalPipeService pipeService = new BidirectionalPipeService(sessionPeerGroup);
			while (null == pipe) {
				pipe = pipeService.connect(pipeadv, 30000);
			}

			InputPipe input = pipe.getInputPipe();
			while (true) {
				Message message = input.waitForMessage();
				System.out.println(message.getString("ACK"));
			} */

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Client: Error sending message to the service");
		}
	}

	private void offerInputPipe(PeerGroup sessionPeerGroup) {
        try {
			// Create a pipe advertisement for the Service.
			PipeAdvertisement pipeadv = (PipeAdvertisement)
				AdvertisementFactory.newAdvertisement(
				PipeAdvertisement.getAdvertisementType());

			pipeadv.setName("JXTA-ELAN");
			PipeID pid = (PipeID) IDFactory.newPipeID(sessionPeerGroup.getPeerGroupID());
			pipeadv.setPipeID(pid);

			// We are now ready to start the service --
			// create the input pipe endpoint clients will
			// use to connect to the service
			inputPipe = pipeSvc.createInputPipe(pipeadv, this);

			// send pipe advertisement
			// create the pipe message
			StructuredTextDocument doc = (StructuredTextDocument)
					pipeadv.getDocument(new MimeMediaType("text/xml"));

			StringWriter out = new StringWriter();
			doc.sendToWriter(out);

			msg = pipeSvc.createMessage();
			msg.setString(INPIPE_OFFERED, out.toString());
			msg.setString(PARTICIPANT_MAIL, localEmail);
			msg.setString(PARTICIPANT_NAME, localName);

			// send the message to the service pipe
			outputPipe.send(msg);
			System.out.println("Input pipe advertisement returned");

			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Server: Error publishing the module");
		}
	}


	private void buildPipeInfrastructure() {
		msg = pipeSvc.createMessage();
		msg.setString("Init", "PipeAdvertisements");
		try {
			outputPipe.send(msg);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	private void sendCommands() {
		// read command from command line
		String command = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Type command: ");

		try {
			while ((command = br.readLine()) != null) {

				// create the pipe message
				msg = pipeSvc.createMessage();
				msg.setString(COMMAND, command);

				// send the message to the service pipe
				outputPipe.send(msg);
				System.out.println("Command: \"" + command + "\" sent to the Server");
				System.out.println("\nType command: ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// By implementing PipeMsgListener, we define this method to deal with
	// messages as they occur

	public void pipeMsgEvent ( PipeMsgEvent event ){
		Message msg=null;
		try {
			msg = event.getMessage();
			if (msg == null)
				return;
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// look for internal command
		String command = msg.getString(COMMAND);
		if (command != null) {
			String par1 = msg.getString(PARAMETER1);
			String par2 = msg.getString(PARAMETER2);

			System.out.println("received command: " + command + " with p1: " + par1 + " and p2: " + par2);

			if (command.equals(GET_PARTICIPANTS)) {
				sendCommand(ADD_PARTICIPANT, localName, localEmail);
			}
			else if (command.equals(ADD_PARTICIPANT)) {
				collaborationPanel.addParticipant(par1, par2);
				collaborationPanel.setControllingParticipant(par2);

			//	collaborationPanel.addParticipant(localName, localEmail);
			} else if (command.equals(REQUEST_CONTROL)) {
				collaborationPanel.setControllingParticipant(par1);
				hasControl = false;
			} else if (command.equals(LEAVE_SESSION)) {
				collaborationPanel.removeParticipant(par1);
				// close the connection to that participant...
				// voor nu zorg dat er geen berichten meer verstuurd worden
				pipeSvc = null;
			} else if (command.equals(GET_SESSION_INFO)) {
				sendCommand(SESSION_INFO, collaborationPanel.getSharedDocumentName(), 
					collaborationPanel.getChairName());
			} else if (command.equals(SESSION_INFO)) {
				collaborationPanel.setSessionName(par1);
				collaborationPanel.setChairName(par2);
				collaborationPanel.setSharedDocumentName(par1);
			}
		}

		// look for specific elan message
		command = msg.getString(ELAN_COMMAND);
		if (command != null) {
			String parameter1 = msg.getString(PARAMETER1);
			String parameter2 = msg.getString(PARAMETER2);

			// must be handled here because no p2p2Here yet
			if (command.equals(SET_EAF)) {
				String eafString = parameter1;
				// save in user dir en openEAF in elanFrame aanroepen
				try {
					String fullPath = Constants.USERHOME + Constants.FILESEPARATOR + "p2p.eaf";
					File p2pEafFile = new File(fullPath);
					FileWriter out = new FileWriter(p2pEafFile);
					out.write(eafString, 0, eafString.length());
					out.flush();
					out.close();
					frame.openEAF(fullPath);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				p2p2Here.handleCommand(command, parameter1, parameter2);
			}

			return;
		}


		String offeredInputPipeString = msg.getString(INPIPE_OFFERED);
		if (offeredInputPipeString != null) {
			try {
				InputStream is = new ByteArrayInputStream( offeredInputPipeString.getBytes() );
				PipeAdvertisement offeredPipeAdv = (PipeAdvertisement)
						AdvertisementFactory.newAdvertisement(
							new MimeMediaType( "text/xml" ), is);
				OutputPipe p = pipeSvc.createOutputPipe(offeredPipeAdv, 10000);

				String participantEmailString = msg.getString(PARTICIPANT_MAIL);
				String participantNameString = msg.getString(PARTICIPANT_NAME);
				if (participantEmailString != null) {
					if (outputPipeHash == null) {
						outputPipeHash = new Hashtable();
					}
					outputPipeHash.put(participantEmailString, p);
					collaborationPanel.addParticipant(participantNameString, participantEmailString);

					// revise later
					if (outputPipe == null) outputPipe = p;

					System.out.println("pipe advertisement received: " + offeredPipeAdv.getName()
								+ " from: " + participantEmailString);

					System.out.println("sending eaf document to newly connected in pipe");
					sendEAFDocument(p);
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}

			return;
		}

	}

	private void sendEAFDocument(OutputPipe theOutPipe) {
		if (transcription != null) {
			// save changes to document first?
			try {
				String eafPath = ((TranscriptionImpl) transcription).getFullPath();
				eafPath = "/" + eafPath.substring(5);
				File eafFile = new File(eafPath);
				FileReader in = new FileReader(eafFile);
				BufferedReader br = new BufferedReader(in);

				int c;
				StringBuffer sb = new StringBuffer();
				while ((c = br.read()) != -1) {
					sb.append((char) c);
				}
				br.close();

				String eafString = sb.toString();

				// send it to the peer, must use this outputPipe so do not use sendElanCommand
				Message msg = pipeSvc.createMessage();
				msg.setString(ELAN_COMMAND, SET_EAF);
				msg.setString(PARAMETER1, eafString);
				theOutPipe.send(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void messageReceived(Message message, OutputPipe pipe) {
		System.out.println("message received:" + message.getString(COMMAND));
	}

	private PeerGroup createGroup() {
		PeerGroup pg;               // new peer group
		PeerGroupAdvertisement adv; // advertisement for the new peer group

		System.out.println("Creating a new group advertisement");

		try {
			// create a new all purpose peergroup.
			ModuleImplAdvertisement implAdv =
			netGroup.getAllPurposePeerGroupImplAdvertisement();

			pg = netGroup.newGroup(null,                // Assign new group ID
			implAdv,              // The implem. adv
			transcription.getName(),           // The name
			"testing group adv"); // Helpful descr.

			// print the name of the group and the peer group ID
			adv = pg.getPeerGroupAdvertisement();
			PeerGroupID GID = adv.getPeerGroupID();
			System.out.println("  Group = " +adv.getName() +
			"\n  Group ID = " + GID.toString());

		}
		catch (Exception eee) {
			System.out.println("Group creation failed with " + eee.toString());
			return (null);
		}

		try {
			// publish this advertisement
			// (send out to other peers/rendezvous peers)
			netDiscoSvc.remotePublish(adv, DiscoveryService.GROUP);
			System.out.println("Group published successfully.\n");
		}
		catch (Exception e) {
			System.out.println("Error publishing group advertisement");
			e.printStackTrace();
			return (null);
		}

		return(pg);

	}

	private void joinGroup(PeerGroup grp) {
		System.out.println("Joining peer group...");

		StructuredDocument creds = null;

		try {
			// Generate the credentials for the Peer Group
			AuthenticationCredential authCred =
			new AuthenticationCredential( grp, null, creds );

			// Get the MembershipService from the peer group
			MembershipService membership = grp.getMembershipService();

			// Get the Authenticator from the Authentication creds
			Authenticator auth = membership.apply( authCred );

			// Check if everything is okay to join the group
			if (auth.isReadyForJoin()){
				Credential myCred = membership.join(auth);
				// what do I do with the credential it returns?

				System.out.println("Successfully joined group " +
				grp.getPeerGroupName());

				// display the credential as a plain text document.
				System.out.println("\nCredential: ");
				StructuredTextDocument doc = (StructuredTextDocument)
				myCred.getDocument(new MimeMediaType("text/plain"));

				StringWriter out = new StringWriter();
				doc.sendToWriter(out);
				System.out.println(out.toString());
				out.close();

			}
			else
				System.out.println("Failure: unable to join group");

		}
		catch (Exception e){
			System.out.println("Failure in authentication.");
			e.printStackTrace();
		}
	}

	public void requestControl() {
		sendCommand(REQUEST_CONTROL, localEmail);
		hasControl = true;
	}

	public void leaveSession() {
		sendCommand(LEAVE_SESSION, localEmail);
		pipeSvc = null;
	}

	public void populateCollaborationPanel() {
		if (localEmail != null) {
			collaborationPanel.setLocalParticipantName(localName);
			collaborationPanel.setLocalParticipantMail(localEmail);
			collaborationPanel.addParticipant(localName, localEmail);
		}
		if (hasControl) {
			collaborationPanel.setControllingParticipant(localEmail);
		}

		if (outputPipe != null) {
			sendCommand(GET_PARTICIPANTS);
			sendCommand(GET_SESSION_INFO);
		}
	}
}

