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
import se.sics.kompics.p2p.fd.SuspicionStatus;
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
	private int roundsWaiting = 0;
	private boolean lookingForFirstSucc = true;
	private static int WAITING_ROUNDS = 10;

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
				
//				logger.info(peerSelf.getPeerId() + "\t: " + " try to join.");
				
				if (!somePeers.isEmpty()) {
					PeerEntry peerEntry = (PeerEntry) somePeers.toArray()[0];
					peer = (PeerAddress) peerEntry.getOverlayAddress();
					//find the correct node to join - our succ
					FindSucc fs = new FindSucc(peerSelf, peer, peerSelf, peerSelf.getPeerId(), -1);
					trigger(fs, network);
				}
				else {
					//we are the first node
//					logger.info(self + ": NOT found another peer, create new ring");
					lookingForFirstSucc = false;
					pred = null;
					succ = peerSelf;
					succList = new PeerAddress[SUCC_SIZE];
					succList[0] = succ;
					Snapshot.setSuccList(peerSelf, succList);
					Snapshot.setSucc(peerSelf, succ);
					fingers[0] = succ;
//					Snapshot.setFingers(peerSelf, fingers);
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
//			logger.info(">> NODE: " + peerSelf.getPeerId() + " || try to route: " + event.getID());
//			logger.info("\tmy succ: " + succ.getPeerId() + " | my fl0 " + (fingers[0] ==null ? "null" : fingers[0].getPeerId()) );
			if (succ != null && RingKey.belongsTo(targetNodeID, peerSelf.getPeerId(), succ.getPeerId(), 
					IntervalBounds.OPEN_CLOSED, RING_SIZE)) {
				//found its correct successor
				FindSuccReply fsr = new FindSuccReply(peerSelf, event.getInitiator(), succ, event.getFingerIndex());
				trigger(fsr, network);
//				logger.info("! ");
			}
			else {
				boolean found = false;
				
				for (int i = 0; i < (SUCC_SIZE - 1); i++) {
					if ((succList[i + 1] != null) && RingKey.belongsTo(targetNodeID, 
							succList[i].getPeerId(), succList[i + 1].getPeerId(), 
							IntervalBounds.OPEN_CLOSED, RING_SIZE)) {
						
						FindSuccReply fsr = new FindSuccReply(peerSelf, event.getInitiator(), succList[i + 1], event.getFingerIndex());
						trigger(fsr, network);
						found = true;
//						logger.info("> ");
						break;
					}
				}
				
				if (!found) {
//					FindSucc fs = new FindSucc(peerSelf, succ, event.getInitiator(), event.getID(), event.getFingerIndex());

					nextPeer = findClosestPrecedingNode(event.getID());
					FindSucc fs = new FindSucc(peerSelf, nextPeer, event.getInitiator(), event.getID(), event.getFingerIndex());
					trigger(fs, network);
					
				}
				
				
			}
		}
	};
	
	PeerAddress findClosestPrecedingNode(BigInteger targetID) {
		for (int i = (FINGER_SIZE - 1); i >= 0; i--) {
			if ((fingers[i] != null) && 
					RingKey.belongsTo(fingers[i].getPeerId(), peerSelf.getPeerId(), targetID, 
							IntervalBounds.OPEN_OPEN, RING_SIZE)) {
//				logger.info(">> ");
				return fingers[i];
			}
		}
		
		
		return peerSelf;
//		return succ;
	}
	
//-------------------------------------------------------------------
	//found his correct successor
	Handler<FindSuccReply> handleFindSuccReply = new Handler<FindSuccReply>() {
		public void handle(FindSuccReply event) {
			if (event.getFingerIndex() == -1) {
				lookingForFirstSucc = false;
				PeerAddress psucc = succ;
				succ = event.getResponsible();
				if (psucc == null || !psucc.equals(succ)) {
					fdUnregister(psucc);
					fdRegister(succ);	
				}
				
				succList = new PeerAddress[SUCC_SIZE];
				succList[0] = succ;
				Snapshot.setSuccList(peerSelf, succList);
				fingers[0] = succ;
//				Snapshot.setFingers(peerSelf, fingers);
				pred = null;
//				logger.info(peerSelf.getPeerId() + "\t: " + "found succ: " + succ.getPeerId());
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
//				roundsWaiting = 0;
//				logger.info(peerSelf.getPeerId() + ": doing stabilization");
				
				nextFinger = (nextFinger + 1) % FINGER_SIZE;
				BigInteger targetID = BigInteger.valueOf((long) (Math.pow(2.0, nextFinger) + peerSelf.getPeerId().doubleValue()));
				
				FindSucc findSucc = new FindSucc(peerSelf, succ, peerSelf, targetID, nextFinger);
				trigger(findSucc, network);
				
				
				WhoIsPred wp = new WhoIsPred(peerSelf, succ);
				trigger(wp, network);
			}
			else {
				if (roundsWaiting == WAITING_ROUNDS) {
					if (lookingForFirstSucc) {
						logger.info(":-o first succ : " + peerSelf.getPeerId());
						BootstrapRequest request = new BootstrapRequest("chord", 1);
						trigger(request, bootstrap.getPositive(P2pBootstrap.class));			
					}
					else {
						logger.info(":-o succ");
						lookForNewSucc();
					}
					roundsWaiting = 0;
				}
				else {
					roundsWaiting++;
				}
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
				PeerAddress psucc = succ;
				succ = event.getPred();
				if (psucc == null || !psucc.equals(succ)) {
					fdUnregister(psucc);
					fdRegister(succ);	
				}
				succList = new PeerAddress[SUCC_SIZE];
				succList[0] = succ;
				Snapshot.setSuccList(peerSelf, succList);
				Snapshot.setSucc(peerSelf, succ);
				fingers[0] = succ;
//				Snapshot.setFingers(peerSelf, fingers);
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
//			logger.info(peerSelf.getPeerId() + ": trying to become my pred: " + event.getMSPeerSource());
			if (pred == null || event.getMSPeerSource().belongsTo(pred, peerSelf, 
					se.sics.kompics.p2p.peer.PeerAddress.IntervalBounds.OPEN_OPEN, RING_SIZE)) {
				
				PeerAddress ppred = pred;
				pred = event.getMSPeerSource();
				if (ppred == null || !ppred.equals(pred)) {
					fdUnregister(ppred);
					fdRegister(pred);	
				}
				Snapshot.setPred(peerSelf, pred);
//				logger.info(peerSelf.getPeerId() + ": \tAccepted pred: " + event.getMSPeerDestination().getPeerId());
			}
		}
	};

//-------------------------------------------------------------------	
	Handler<PeerFailureSuspicion> handlePeerFailureSuspicion = new Handler<PeerFailureSuspicion>() {
		public void handle(PeerFailureSuspicion event) {
			Address suspectedPeerAddress = event.getPeerAddress();
			
			if (event.getSuspicionStatus().equals(SuspicionStatus.SUSPECTED)) {
				logger.info(peerSelf.getPeerId() + " : suspected " + suspectedPeerAddress);
				
				if (pred != null && suspectedPeerAddress.equals(pred.getPeerAddress())) {
					logger.info("\t it is my pred");
					pred = null;
				}
				else if (suspectedPeerAddress.equals(succ.getPeerAddress())) {
					logger.info("\t it is my succ");
					succ = null;
					roundsWaiting = 0;
					lookForNewSucc();
				}
			}
		}
	};
	
	private void lookForNewSucc() {
		PeerAddress peer;
		int index = (int) ((Math.ceil((Math.random() * SUCC_SIZE))) % (SUCC_SIZE-1)) + 1;
		peer = succList[index];
		if (peer == null) {
			index = (int) ((Math.ceil((Math.random() * FINGER_SIZE))) % (FINGER_SIZE-1)) + 1;
			peer = fingers[index];
		}
		if (peer != null) {
			logger.info(":-o found peer to use: " + peer.getPeerId());
			FindSucc fs = new FindSucc(peerSelf, peer, peerSelf, peerSelf.getPeerId(), -1);
			trigger(fs, network);
		}
		
	}
	
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
