package se.sics.kompics.p2p.simulator;

import java.math.BigInteger;
import java.util.HashMap;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.peer.BTMessage;
import se.sics.kompics.p2p.peer.MessagePort;
import se.sics.kompics.p2p.peer.PeerAddress;
import se.sics.kompics.p2p.peer.PeerConfiguration;
import se.sics.kompics.p2p.peer.PeerPort;
import se.sics.kompics.p2p.peer.btpeer.JoinBTPeer;
import se.sics.kompics.p2p.peer.btpeer.BTPeer;
import se.sics.kompics.p2p.peer.btpeer.BTPeerInit;
import se.sics.kompics.p2p.peer.tracker.JoinTracker;
import se.sics.kompics.p2p.peer.tracker.Tracker;
import se.sics.kompics.p2p.peer.tracker.TrackerInit;
import se.sics.kompics.p2p.simulator.bwmodel.BwDelayedMessage;
import se.sics.kompics.p2p.simulator.bwmodel.Link;
import se.sics.kompics.p2p.simulator.launch.Configuration;
import se.sics.kompics.p2p.simulator.launch.PeerType;
import se.sics.kompics.p2p.simulator.snapshot.Snapshot;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public final class Simulator extends ComponentDefinition {

	Positive<SimulatorPort> simulator = positive(SimulatorPort.class);
	Positive<Network> network = positive(Network.class);
	Positive<Timer> timer = positive(Timer.class);

	private int peerIdSequence;

	private BigInteger idSpaceSize;
	private ConsistentHashtable<BigInteger> view;
	private final HashMap<BigInteger, Component> peers;
	private final HashMap<BigInteger, Link> uploadLink;
	private final HashMap<BigInteger, Link> downloadLink;
	private final HashMap<BigInteger, PeerAddress> peersAddress;
	
	private Address peer0Address;
	private PeerAddress trackerAddress;
	private PeerConfiguration peerConfiguration;

//-------------------------------------------------------------------	
	public Simulator() {
		peers = new HashMap<BigInteger, Component>();
		uploadLink = new HashMap<BigInteger, Link>();
		downloadLink = new HashMap<BigInteger, Link>();
		peersAddress = new HashMap<BigInteger, PeerAddress>();
		view = new ConsistentHashtable<BigInteger>();

		subscribe(handleInit, control);

		subscribe(handleGenerateReport, timer);
		subscribe(handleDelayedMessage, timer);
		
		subscribe(handleMessageReceived, network);
		
		subscribe(handleTrackerJoin, simulator);
		subscribe(handlePeerJoin, simulator);
		subscribe(handlePeerFail, simulator);
	}

//-------------------------------------------------------------------	
	Handler<SimulatorInit> handleInit = new Handler<SimulatorInit>() {
		public void handle(SimulatorInit init) {
			peers.clear();
			peerIdSequence = 0;

			peer0Address = init.getPeer0Address();
			peerConfiguration = init.getMSConfiguration();

			idSpaceSize = new BigInteger(2 + "").pow(Configuration.Log2Ring);
			
			// generate periodic report
			int snapshotPeriod = Configuration.SNAPSHOT_PERIOD;
			SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(snapshotPeriod, snapshotPeriod);
			spt.setTimeoutEvent(new GenerateReport(spt));
			trigger(spt, timer);
		}
	};

//-------------------------------------------------------------------	
	Handler<TrackerJoin> handleTrackerJoin = new Handler<TrackerJoin>() {
		public void handle(TrackerJoin event) {
			BigInteger id = event.getPeerId();

			Component tracker = createAndStartTracker(id);
			view.addNode(id);

			trigger(new JoinTracker(id), tracker.getPositive(PeerPort.class));
		}
	};
	
//-------------------------------------------------------------------	
	Handler<BTPeerJoin> handlePeerJoin = new Handler<BTPeerJoin>() {
		public void handle(BTPeerJoin event) {
			BigInteger id = event.getPeerId();
			PeerType peerType = event.getPeerType();
			
			// join with the next id if this id is taken
			BigInteger successor = view.getNode(id);
			while (successor != null && successor.equals(id)) {
				id = id.add(BigInteger.ONE).mod(idSpaceSize);
				successor = view.getNode(id);
			}

			Component newPeer = createAndStartNewPeer(id);
			view.addNode(id);

			trigger(new JoinBTPeer(id, peerType), newPeer.getPositive(PeerPort.class));
		}
	};

//-------------------------------------------------------------------	
	Handler<BTPeerFail> handlePeerFail = new Handler<BTPeerFail>() {
		public void handle(BTPeerFail event) {
			BigInteger id = view.getNode(event.getPeerId());

			if (view.size() == 0) {
				System.err.println("Empty network");
				return;
			}
			
			if (id.equals(Configuration.TRACKER_ID)) {
				System.err.println("Can not remove tracker ...");
				return;
			}

			view.removeNode(id);
			stopAndDestroyPeer(id);
		}
	};
	
//-------------------------------------------------------------------	
	Handler<BTMessage> handleMessageSent = new Handler<BTMessage>() {
		public void handle(BTMessage message) {
			// message just sent by some peer goes into peer's up pipe
			Link link = uploadLink.get(message.getPeerSource().getPeerId());
			if (link == null)
				return;
			
			long delay = link.addMessage(message);
			
			if (delay == 0) {
				// immediately send to cloud
				trigger(message, network);
				return;
			}
			
			ScheduleTimeout st = new ScheduleTimeout(delay);
			st.setTimeoutEvent(new BwDelayedMessage(st, message, true));
			trigger(st, timer);
		}
	};

//-------------------------------------------------------------------	
	Handler<BTMessage> handleMessageReceived = new Handler<BTMessage>() {
		public void handle(BTMessage message) {
			// message to be received by some peer goes into peer's down pipe
			Link link = downloadLink.get(message.getPeerDestination().getPeerId());
			
			if (link == null)
				return;
			
			long delay = link.addMessage(message);
			
			if (delay == 0) {
				// immediately deliver to peer
				Component peer = peers.get(message.getPeerDestination().getPeerId());
				trigger(message, peer.getNegative(MessagePort.class));
				return;
			}
			
			ScheduleTimeout st = new ScheduleTimeout(delay);
			st.setTimeoutEvent(new BwDelayedMessage(st, message, false));
			trigger(st, timer);
		}
	};

//-------------------------------------------------------------------	
	Handler<BwDelayedMessage> handleDelayedMessage = new Handler<BwDelayedMessage>() {
		public void handle(BwDelayedMessage delayedMessage) {
			if (delayedMessage.isBeingSent()) {
				// message comes out of upload pipe
				BTMessage message = delayedMessage.getMessage();
				// and goes to the network cloud
				trigger(message, network);
			} else {
				// message comes out of download pipe
				BTMessage message = delayedMessage.getMessage();
				Component peer = peers.get(message.getPeerDestination().getPeerId());
				if (peer != null) {
					// and goes to the peer
					trigger(message, peer.getNegative(MessagePort.class));
				}
			}
		}
	};
	
//-------------------------------------------------------------------	
	private final Component createAndStartTracker(BigInteger id) {
		Component tracker = create(Tracker.class);
		int peerId = ++peerIdSequence;
		Address peerAddress = new Address(peer0Address.getIp(), peer0Address.getPort(), peerId);
		trackerAddress = new PeerAddress(peerAddress, id);
		int numOfPieces = peerConfiguration.getNumOfPieces();
		
		connect(timer, tracker.getNegative(Timer.class));

		subscribe(handleMessageSent, tracker.getNegative(MessagePort.class));

		trigger(new TrackerInit(trackerAddress, numOfPieces), tracker.getControl());

		trigger(new Start(), tracker.getControl());
		peers.put(id, tracker);
		uploadLink.put(id, new Link(Integer.MAX_VALUE));
		downloadLink.put(id, new Link(Integer.MAX_VALUE));
		peersAddress.put(id, trackerAddress);
		
		return tracker;
	}

//-------------------------------------------------------------------	
	private final Component createAndStartNewPeer(BigInteger id) {
		Component peer = create(BTPeer.class);
		int peerId = ++peerIdSequence;
		Address peerAddress = new Address(peer0Address.getIp(), peer0Address.getPort(), peerId);
		PeerAddress btPeerAddress = new PeerAddress(peerAddress, id);
		int downloadBW = peerConfiguration.getDownloadBW();
		int uploadBW = peerConfiguration.getUploadBW();
		int indegree = peerConfiguration.getIndegree();
		int outdegree = peerConfiguration.getOutdegree();
		int numOfPieces = peerConfiguration.getNumOfPieces();
		int pieceSize = peerConfiguration.getPieceSize();
		
		connect(timer, peer.getNegative(Timer.class));

		subscribe(handleMessageSent, peer.getNegative(MessagePort.class));

		trigger(new BTPeerInit(btPeerAddress, indegree, outdegree, numOfPieces, pieceSize, trackerAddress), peer.getControl());

		trigger(new Start(), peer.getControl());
		peers.put(id, peer);
		uploadLink.put(id, new Link(uploadBW));
		downloadLink.put(id, new Link(downloadBW));
		peersAddress.put(id, btPeerAddress);
		
		return peer;
	}
	
//-------------------------------------------------------------------	
	private final void stopAndDestroyPeer(BigInteger id) {
		Component peer = peers.get(id);

		trigger(new Stop(), peer.getControl());

		subscribe(handleMessageSent, peer.getNegative(Network.class));

		disconnect(network, peer.getNegative(Network.class));
		disconnect(timer, peer.getNegative(Timer.class));

		Snapshot.removePeer(peersAddress.get(id));

		peers.remove(id);
		uploadLink.remove(id);
		downloadLink.remove(id);
		peersAddress.remove(id);

		destroy(peer);
	}

//-------------------------------------------------------------------	
	Handler<GenerateReport> handleGenerateReport = new Handler<GenerateReport>() {
		public void handle(GenerateReport event) {
			Snapshot.report();
		}
	};
}

