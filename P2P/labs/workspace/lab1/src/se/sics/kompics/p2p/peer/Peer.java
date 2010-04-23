package se.sics.kompics.p2p.peer;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.bootstrap.BootstrapCompleted;
import se.sics.kompics.p2p.bootstrap.BootstrapRequest;
import se.sics.kompics.p2p.bootstrap.BootstrapResponse;
import se.sics.kompics.p2p.bootstrap.P2pBootstrap;
import se.sics.kompics.p2p.bootstrap.PeerEntry;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClient;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClientInit;
import se.sics.kompics.p2p.fd.FailureDetector;
import se.sics.kompics.p2p.fd.PeerFailureSuspicion;
import se.sics.kompics.p2p.fd.StartProbingPeer;
import se.sics.kompics.p2p.fd.StopProbingPeer;
import se.sics.kompics.p2p.fd.ping.PingFailureDetector;
import se.sics.kompics.p2p.fd.ping.PingFailureDetectorInit;
import se.sics.kompics.p2p.peer.RingKey.IntervalBounds;
import se.sics.kompics.p2p.simulator.launch.Configuration;
import se.sics.kompics.p2p.simulator.snapshot.Snapshot;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

public final class Peer extends ComponentDefinition {
	public static BigInteger RING_SIZE = new BigInteger(2 + "").pow(Configuration.Log2Ring);
	public static int FINGER_SIZE = Configuration.Log2Ring;
	public static int SUCC_SIZE = Configuration.Log2Ring;
	private static int STABILIZING_PERIOD = 1000;
	
	Negative<PeerPort> msPeerPort = negative(PeerPort.class);

	Positive<Network> network = positive(Network.class);
	Positive<Timer> timer = positive(Timer.class);
	
	private static Logger logger = LoggerFactory.getLogger(Peer.class);

	private Component fd, bootstrap;
	
	private Address self;
	private PeerAddress peerSelf;
	
	private PeerAddress pred;
	private PeerAddress succ;
	private PeerAddress[] fingers = new PeerAddress[FINGER_SIZE];
	private PeerAddress[] succList = new PeerAddress[SUCC_SIZE];
	
	private HashMap<Address, UUID> fdRequests;
	private HashMap<Address, PeerAddress> fdPeers;
	
	private boolean bootstrapped;
	private int nextFinger = -1;

//-------------------------------------------------------------------
	public Peer() {
		fdRequests = new HashMap<Address, UUID>();
		fdPeers = new HashMap<Address, PeerAddress>();
		
		for (int i = 0; i < SUCC_SIZE; i++)
			this.succList[i] = null;

		for (int i = 0; i < FINGER_SIZE; i++)
			this.fingers[i] = null;

		fd = create(PingFailureDetector.class);
		bootstrap = create(BootstrapClient.class);
		
		connect(network, fd.getNegative(Network.class));
		connect(network, bootstrap.getNegative(Network.class));
		connect(timer, fd.getNegative(Timer.class));
		connect(timer, bootstrap.getNegative(Timer.class));
		
		subscribe(handleInit, control);
		subscribe(handlePeriodicStabilization, timer);
		subscribe(handleJoin, msPeerPort);
		subscribe(handleFindSucc, network);
		subscribe(handleFindSuccReply, network);
		subscribe(handleWhoIsPred, network);
		subscribe(handleWhoIsPredReply, network);
		subscribe(handleNotify, network);
		subscribe(handleBootstrapResponse, bootstrap.getPositive(P2pBootstrap.class));
		subscribe(handlePeerFailureSuspicion, fd.getPositive(FailureDetector.class));
	}

//-------------------------------------------------------------------
	Handler<PeerInit> handleInit = new Handler<PeerInit>() {
		public void handle(PeerInit init) {
			peerSelf = init.getMSPeerSelf();
			self = peerSelf.getPeerAddress();

			trigger(new BootstrapClientInit(self, init.getBootstrapConfiguration()), bootstrap.getControl());
			trigger(new PingFailureDetectorInit(self, init.getFdConfiguration()), fd.getControl());
		}
	};

//-------------------------------------------------------------------
	Handler<JoinPeer> handleJoin = new Handler<JoinPeer>() {
		public void handle(JoinPeer event) {
			//we add the bootstrap server for the node that we will use to join
			Snapshot.addPeer(peerSelf);
			BootstrapRequest request = new BootstrapRequest("chord", 1);
			trigger(request, bootstrap.getPositive(P2pBootstrap.class));			
		}
	};

//-------------------------------------------------------------------
	Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
		public void handle(BootstrapResponse event) {
			if (!bootstrapped) {
				bootstrapped = true;
				PeerAddress peer;
				Set<PeerEntry> somePeers = event.getPeers();
				
				logger.info(peerSelf.getPeerId() + "\t: " + " try to join.");
				
				if (!somePeers.isEmpty()) {
					PeerEntry peerEntry = (PeerEntry) somePeers.toArray()[0];
					peer = (PeerAddress) peerEntry.getOverlayAddress();
					//find the correct node to join - our succ
					logger.info(peerSelf.getPeerId() + "\t: " + " starting point: " + peer.getPeerId());
					FindSucc fs = new FindSucc(peerSelf, peer, peerSelf, peerSelf.getPeerId(), -1);
					trigger(fs, network);
				}
				else {
					//we are the first node
					logger.info(self + ": NOT found another peer, create new ring");
					pred = null;
					succ = peerSelf;
					succList = new PeerAddress[SUCC_SIZE];
					succList[0] = succ;
					Snapshot.setSuccList(peerSelf, succList);
					Snapshot.setSucc(peerSelf, succ);
					trigger(new BootstrapCompleted("chord", peerSelf), bootstrap.getPositive(P2pBootstrap.class));
				}

				SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(STABILIZING_PERIOD, STABILIZING_PERIOD);
				spt.setTimeoutEvent(new PeriodicStabilization(spt));
				trigger(spt, timer);
			}
		}
	};
	
//-------------------------------------------------------------------
	Handler<FindSucc> handleFindSucc = new Handler<FindSucc>() {
		public void handle(FindSucc event) {
			BigInteger targetNodeID = event.getID();
			PeerAddress nextPeer;
			if (RingKey.belongsTo(targetNodeID, peerSelf.getPeerId(), succ.getPeerId(), 
					IntervalBounds.OPEN_CLOSED, RING_SIZE)) {
				//TODO wtf is fingerIndex, is andrei's idea??
				//found its correct successor
				FindSuccReply fsr = new FindSuccReply(peerSelf, event.getInitiator(), succ, event.getFingerIndex());
				trigger(fsr, network);
			}
			else {
//				nextPeer = findClosestPrecedingNode(event.getInitiator());
//				FindSucc fs = new FindSucc(peerSelf, nextPeer, event.getInitiator(), event.getID(), -1);
				FindSucc fs = new FindSucc(peerSelf, succ, event.getInitiator(), event.getID(), event.getFingerIndex());
				trigger(fs, network);
			}
		}
	};
	
	PeerAddress findClosestPrecedingNode(PeerAddress targetAddress) {
		for (int i = (FINGER_SIZE - 1); i >= 0; i--) {
			if ((fingers[i] != null) && (fingers[i].belongsTo(peerSelf, targetAddress, 
					se.sics.kompics.p2p.peer.PeerAddress.IntervalBounds.OPEN_OPEN, RING_SIZE))) {
				return fingers[i];
			}
		}
		
		return peerSelf;
	}
	
//-------------------------------------------------------------------
	//found his correct successor
	Handler<FindSuccReply> handleFindSuccReply = new Handler<FindSuccReply>() {
		public void handle(FindSuccReply event) {
			if (event.getFingerIndex() == -1) {
				succ = event.getResponsible();
				succList = new PeerAddress[SUCC_SIZE];
				succList[0] = succ;
				Snapshot.setSuccList(peerSelf, succList);
				pred = null;
				logger.info(peerSelf.getPeerId() + "\t: " + "found succ: " + succ.getPeerId());
				Snapshot.setSucc(peerSelf, succ);
				
				Notify notify = new Notify(peerSelf, succ, peerSelf);
				trigger(notify, network);
				
				trigger(new BootstrapCompleted("chord", peerSelf), bootstrap.getPositive(P2pBootstrap.class));
			}
			else {
				fingers[event.getFingerIndex()] = event.getResponsible();
				Snapshot.setFingers(peerSelf, fingers);
			}
			
		}
	};
	
//-------------------------------------------------------------------
	Handler<PeriodicStabilization> handlePeriodicStabilization = new Handler<PeriodicStabilization>() {
		public void handle(PeriodicStabilization event) {
			if (succ != null) {
				logger.info(peerSelf.getPeerId() + ": doing stabilization");
				
				nextFinger = (nextFinger + 1) % FINGER_SIZE;
				BigInteger targetID = BigInteger.valueOf((long) (Math.pow(2.0, nextFinger) + peerSelf.getPeerId().doubleValue()));
				
				FindSucc findSucc = new FindSucc(peerSelf, succ, peerSelf, targetID, nextFinger);
				trigger(findSucc, network);
				
				
				WhoIsPred wp = new WhoIsPred(peerSelf, succ);
				trigger(wp, network);
			}
			
		}
	};

//-------------------------------------------------------------------
	Handler<WhoIsPred> handleWhoIsPred = new Handler<WhoIsPred>() {
		public void handle(WhoIsPred event) {
			//TODO WHAT IS THIS AGAIN???
			WhoIsPredReply wpr = new WhoIsPredReply(peerSelf, event.getMSPeerSource(), pred, succList);
			trigger(wpr, network);
		}
	};

//-------------------------------------------------------------------
	Handler<WhoIsPredReply> handleWhoIsPredReply = new Handler<WhoIsPredReply>() {
		public void handle(WhoIsPredReply event) {
			if ((event.getPred() != null) && (event.getPred().belongsTo(peerSelf, succ, 
					se.sics.kompics.p2p.peer.PeerAddress.IntervalBounds.OPEN_OPEN, RING_SIZE))) {
				succ = event.getPred();
				succList = new PeerAddress[SUCC_SIZE];
				succList[0] = succ;
				Snapshot.setSuccList(peerSelf, succList);
				Snapshot.setSucc(peerSelf, succ);
			}
			
			succList = new PeerAddress[SUCC_SIZE];
			succList[0] = succ;
			PeerAddress[] successosorsSuccList = event.getSuccList();
			for (int i = 0; i < (SUCC_SIZE - 1); i++) {
				if (successosorsSuccList[i] != null) {
					succList[i + 1] = successosorsSuccList[i];
				}
			}
			
			Snapshot.setSuccList(peerSelf, succList);
			
			Notify notify = new Notify(peerSelf, succ, peerSelf);
			trigger(notify, network);
		}
	};

//-------------------------------------------------------------------
	Handler<Notify> handleNotify = new Handler<Notify>() {
		public void handle(Notify event) {
			logger.info(peerSelf.getPeerId() + ": trying to become my pred: " + event.getMSPeerSource());
			if (pred == null || event.getMSPeerSource().belongsTo(pred, peerSelf, 
					se.sics.kompics.p2p.peer.PeerAddress.IntervalBounds.OPEN_OPEN, RING_SIZE)) {
				pred = event.getMSPeerSource();
				Snapshot.setPred(peerSelf, pred);
				logger.info(peerSelf.getPeerId() + ": \tAccepted pred: " + event.getMSPeerDestination().getPeerId());
			}
		}
	};

//-------------------------------------------------------------------	
	Handler<PeerFailureSuspicion> handlePeerFailureSuspicion = new Handler<PeerFailureSuspicion>() {
		public void handle(PeerFailureSuspicion event) {
		}
	};
	
//-------------------------------------------------------------------
	private void fdRegister(PeerAddress peer) {
		Address peerAddress = peer.getPeerAddress();
		StartProbingPeer spp = new StartProbingPeer(peerAddress, peer);
		fdRequests.put(peerAddress, spp.getRequestId());
		trigger(spp, fd.getPositive(FailureDetector.class));
		
		fdPeers.put(peerAddress, peer);
	}

//-------------------------------------------------------------------	
	private void fdUnregister(PeerAddress peer) {
		if (peer == null)
			return;
			
		Address peerAddress = peer.getPeerAddress();
		trigger(new StopProbingPeer(peerAddress, fdRequests.get(peerAddress)), fd.getPositive(FailureDetector.class));
		fdRequests.remove(peerAddress);
		
		fdPeers.remove(peerAddress);
	}
}
