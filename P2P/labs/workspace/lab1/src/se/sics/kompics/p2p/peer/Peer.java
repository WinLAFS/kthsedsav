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
	private static final int TRYING_TO_REJOIN_ROUNDS = 20;
	public static BigInteger RING_SIZE = new BigInteger(2 + "").pow(Configuration.Log2Ring);
	public static int FINGER_SIZE = Configuration.Log2Ring;
	public static int SUCC_SIZE = Configuration.Log2Ring;
	private static int STABILIZING_PERIOD = 1000;
	
	Negative<PeerPort> msPeerPort = negative(PeerPort.class);

	Positive<Network> network = positive(Network.class);
	Positive<Timer> timer = positive(Timer.class);

	private Component fd, bootstrap;
	
	private Address self;
	private PeerAddress peerSelf;
	
	private PeerAddress pred;
	private PeerAddress succ;
	private PeerAddress[] fingers = new PeerAddress[FINGER_SIZE];
	private PeerAddress[] succList = new PeerAddress[SUCC_SIZE];
	
	private HashMap<Address, UUID> fdRequests;
	private HashMap<Address, PeerAddress> fdPeers;
	
	private boolean bootstrapped = false;
	private static Logger logger = LoggerFactory.getLogger(Peer.class);
	protected int nextFinger = -1;
	private boolean callingBoostrap;
	protected boolean tryingToFindNewSucc;
	protected int roundsWaitingForNewSucc;
	private boolean testingNewSucc;
	protected PeerAddress myPreviusSucc;
	protected int greaterSuccTest = 0;
	
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
			Snapshot.addPeer(peerSelf);
			BootstrapRequest request = new BootstrapRequest("chord", 1);
			trigger(request, bootstrap.getPositive(P2pBootstrap.class));
			
			SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(5*STABILIZING_PERIOD, STABILIZING_PERIOD);
			spt.setTimeoutEvent(new PeriodicStabilization(spt));
			trigger(spt, timer);
			
			roundsWaitingForNewSucc = 2 * TRYING_TO_REJOIN_ROUNDS;
		}
	};


//-------------------------------------------------------------------
	Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
		public void handle(BootstrapResponse event) {
			PeerAddress peer;
			Set<PeerEntry> somePeers = event.getPeers();
			if (!bootstrapped) {
				bootstrapped = true;
				if (!somePeers.isEmpty()) {
					PeerEntry peerEntry = (PeerEntry) somePeers.toArray()[0];
					peer = (PeerAddress) peerEntry.getOverlayAddress();
					//find the correct node to join - our succ
					FindSucc fs = new FindSucc(peerSelf, peer, peerSelf, peerSelf.getPeerId(), -1);
					trigger(fs, network);
				}
				else {
					pred = null;
					succ = peerSelf;
					succList = new PeerAddress[SUCC_SIZE];
					succList[0] = succ;
					fingers = new PeerAddress[FINGER_SIZE];
					fingers[0] = succ;
					Snapshot.setSuccList(peerSelf, succList);
					Snapshot.setSucc(peerSelf, succ);
					Snapshot.setPred(peerSelf, pred);
					Snapshot.setFingers(peerSelf, fingers);
					trigger(new BootstrapCompleted("chord", peerSelf), bootstrap.getPositive(P2pBootstrap.class));
				}
				
			}
			if (callingBoostrap) {
				callingBoostrap = false;
				if (!somePeers.isEmpty()) {
					PeerEntry peerEntry = (PeerEntry) somePeers.toArray()[0];
					peer = (PeerAddress) peerEntry.getOverlayAddress();
//					FindSucc fs = new FindSucc(peerSelf, peer, peerSelf, peerSelf.getPeerId(), -2);
//					trigger(fs, network);
					succ = peer;
					Snapshot.setSucc(peerSelf, peer);
					fingers = new PeerAddress[FINGER_SIZE];
					Snapshot.setFingers(peerSelf, fingers);
					
					logger.info(peerSelf.getPeerId() + ": bootstrap gave me: "+ peer);
				}
			}
		}
	};
	
//-------------------------------------------------------------------
	Handler<FindSucc> handleFindSucc = new Handler<FindSucc>() {
		public void handle(FindSucc event) {
			BigInteger targetNodeID = event.getID();
			PeerAddress nextPeer;
			
			//if current node responsible
			if (pred != null && RingKey.belongsTo(targetNodeID, pred.getPeerId(), peerSelf.getPeerId(), 
					IntervalBounds.OPEN_CLOSED, RING_SIZE)) {
				FindSuccReply fsr = new FindSuccReply(peerSelf, event.getInitiator(), peerSelf, event.getFingerIndex());
				trigger(fsr, network);
			}
			//if successor responsible
			else if ((succ != null) && RingKey.belongsTo(targetNodeID, peerSelf.getPeerId(), succ.getPeerId(), 
					IntervalBounds.OPEN_CLOSED, RING_SIZE)) {
				//found its correct successor
				FindSuccReply fsr = new FindSuccReply(peerSelf, event.getInitiator(), succ, event.getFingerIndex());
				trigger(fsr, network);
			}
			else {
				nextPeer = findClosestPrecedingNode(targetNodeID);
				if (nextPeer != null) {
					FindSucc fs = new FindSucc(peerSelf, nextPeer, event.getInitiator(), targetNodeID, event.getFingerIndex());
					trigger(fs, network);
				}
//				logger.info(peerSelf.getPeerId() + ": routing: " + targetNodeID + " to: " + nextPeer.getPeerId());
//				logger.info("\t my finger 0 is: " + ((fingers[0] != null) ? fingers[0].getPeerId() : "null"));
			}
		}
	};
	
	PeerAddress findClosestPrecedingNode(BigInteger targetID) {
		for (int i = (FINGER_SIZE - 1); i >= 0; i--) {
			if ((fingers[i] != null) && 
					RingKey.belongsTo(fingers[i].getPeerId(), peerSelf.getPeerId(), targetID, 
							IntervalBounds.OPEN_OPEN, RING_SIZE)) {
				return fingers[i];
			}
		}
//		return peerSelf;
		return succ;
	}
	
//-------------------------------------------------------------------
	Handler<FindSuccReply> handleFindSuccReply = new Handler<FindSuccReply>() {
		public void handle(FindSuccReply event) {
			if (event.getFingerIndex() == -1) {
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
				pred = null;
				Snapshot.setSucc(peerSelf, succ);
				Snapshot.setPred(peerSelf, pred);
				Snapshot.setFingers(peerSelf, fingers);
				Snapshot.setSuccList(peerSelf, succList);
				
				Notify notify = new Notify(peerSelf, succ, peerSelf);
				trigger(notify, network);
				
				trigger(new BootstrapCompleted("chord", peerSelf), bootstrap.getPositive(P2pBootstrap.class));
			}
			else if (event.getFingerIndex() == -2) {
				succ = event.getResponsible();
				logger.info(peerSelf.getPeerId() + ": rejoined @: " + succ);
				fdRegister(succ);
				succList = new PeerAddress[SUCC_SIZE];
				succList[0] = succ;
				Snapshot.setSuccList(peerSelf, succList);
				fingers[0] = succ;
				Snapshot.setSucc(peerSelf, succ);
				Snapshot.setPred(peerSelf, pred);
				Snapshot.setFingers(peerSelf, fingers);
				Snapshot.setSuccList(peerSelf, succList);
				
				Notify notify = new Notify(peerSelf, succ, peerSelf);
				trigger(notify, network);
			}
			else
			{
				fingers[event.getFingerIndex()] = event.getResponsible();
				Snapshot.setFingers(peerSelf, fingers);
			}
		}
	};
	
//-------------------------------------------------------------------
	Handler<PeriodicStabilization> handlePeriodicStabilization = new Handler<PeriodicStabilization>() {
		public void handle(PeriodicStabilization event) {
			if (succ != null) {
				nextFinger = (nextFinger + 1) % FINGER_SIZE;
				BigInteger targetID = BigInteger.valueOf((long) (Math.pow(2.0, nextFinger) + peerSelf.getPeerId().doubleValue()));
				
				FindSucc findSucc = new FindSucc(peerSelf, succ, peerSelf, targetID, nextFinger);
				trigger(findSucc, network);
				
				WhoIsPred wp = new WhoIsPred(peerSelf, succ);
				trigger(wp, network);
				
				if ((testingNewSucc || callingBoostrap) && ((roundsWaitingForNewSucc == 0))) {
					logger.info(peerSelf.getPeerId() + ": my new succ is DEAD: " + succ.getPeerId());
					findNextSucc();
				}
				else {
					roundsWaitingForNewSucc--;
				}
			}
			else if (!bootstrapped) {
				if ((roundsWaitingForNewSucc == 0)) {
					logger.info(peerSelf.getPeerId() + ": calling BOOTSTRAP AGAIN!");
					roundsWaitingForNewSucc = TRYING_TO_REJOIN_ROUNDS;
					BootstrapRequest request = new BootstrapRequest("chord", 1);
					trigger(request, bootstrap.getPositive(P2pBootstrap.class));
				}
				else {
					roundsWaitingForNewSucc--;
				}
			}
			else {
				logger.info(peerSelf.getPeerId() + ": NULL succ :S");
				findNextSucc();
			}
			if (greaterSuccTest == 5*TRYING_TO_REJOIN_ROUNDS) {
				greaterSuccTest = 0;
				logger.info(peerSelf.getPeerId() + ": my succ looks to failed SOSOSOSOSOSOS");
				findNextSucc();
			}
			else {
				greaterSuccTest++;
			}
		}
	};

//-------------------------------------------------------------------
	Handler<WhoIsPred> handleWhoIsPred = new Handler<WhoIsPred>() {
		public void handle(WhoIsPred event) {
			WhoIsPredReply wpr = new WhoIsPredReply(peerSelf, event.getMSPeerSource(), pred, succList);
			trigger(wpr, network);
		}
	};


//-------------------------------------------------------------------
	Handler<WhoIsPredReply> handleWhoIsPredReply = new Handler<WhoIsPredReply>() {
		public void handle(WhoIsPredReply event) {
			//TODO ??
			if (testingNewSucc && !myPreviusSucc.equals(event.getMSPeerSource())) {
				testingNewSucc = false;
				logger.info(peerSelf.getPeerId() + ": verified new succ: " + succ.getPeerId());
			}
			greaterSuccTest = 0;
			
			
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
				Snapshot.setFingers(peerSelf, fingers);
			}
			
			Notify notify = new Notify(peerSelf, succ, peerSelf);
			trigger(notify, network);
			
			succList = new PeerAddress[SUCC_SIZE];
			succList[0] = succ;
			PeerAddress[] successosorsSuccList = event.getSuccList();
			for (int i = 0; i < (SUCC_SIZE - 1); i++) {
				if (successosorsSuccList[i] != null) {
					succList[i + 1] = successosorsSuccList[i];
				}
			}
			
			Snapshot.setSuccList(peerSelf, succList);
			
		}
	};

//-------------------------------------------------------------------
	Handler<Notify> handleNotify = new Handler<Notify>() {
		public void handle(Notify event) {
			if (pred == null || event.getMSPeerSource().belongsTo(pred, peerSelf, 
					se.sics.kompics.p2p.peer.PeerAddress.IntervalBounds.OPEN_OPEN, RING_SIZE)) {
				
				PeerAddress ppred = pred;
				pred = event.getMSPeerSource();
				if (ppred == null || !ppred.equals(pred)) {
					fdUnregister(ppred);
					fdRegister(pred);	
				}
				Snapshot.setPred(peerSelf, pred);
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
					logger.info("\t" + pred.getPeerId() + " was my pred");
					fdUnregister(pred);
					pred = null;
					Snapshot.setPred(peerSelf, pred);
				}
				else if (suspectedPeerAddress.equals(succ.getPeerAddress())) {
					logger.info("\t" + succ.getPeerId() + " was my succ");
					fdUnregister(succ);
					myPreviusSucc = succ;
					succ = null;
					Snapshot.setSucc(peerSelf, succ);
					findNextSucc();
				}
			}
		}

	};

	private void findNextSucc() {
		String log = peerSelf.getPeerId() + ": findNextSucc, ";
		if (succ != null) {
			fdUnregister(succ);
		}
		shiftSuccList();
		roundsWaitingForNewSucc = TRYING_TO_REJOIN_ROUNDS;
		if (succList[0] != null) {
			log += succList[0];
			succ = succList[0];
			Snapshot.setSucc(peerSelf, succ);
			fdRegister(succ);
			testingNewSucc = true;
		}
		else {
			log += "BOOTSTRAP";
			callingBoostrap = true;
			BootstrapRequest request = new BootstrapRequest("chord", 1);
			trigger(request, bootstrap.getPositive(P2pBootstrap.class));
		}
		logger.info(log);
	}
	
//-------------------------------------------------------------------
	private void fdRegister(PeerAddress peer) {
		Address peerAddress = peer.getPeerAddress();
		StartProbingPeer spp = new StartProbingPeer(peerAddress, peer);
		fdRequests.put(peerAddress, spp.getRequestId());
		trigger(spp, fd.getPositive(FailureDetector.class));
		
		fdPeers.put(peerAddress, peer);
	}


protected void shiftSuccList() {
	for (int i = 0; i < (SUCC_SIZE - 1); i++) {
		succList[i] = succList[i + 1];
	}
	succList[SUCC_SIZE - 1] = null;
	Snapshot.setSuccList(peerSelf, succList);
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
