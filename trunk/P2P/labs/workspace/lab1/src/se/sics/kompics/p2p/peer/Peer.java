package se.sics.kompics.p2p.peer;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.bootstrap.BootstrapRequest;
import se.sics.kompics.p2p.bootstrap.BootstrapResponse;
import se.sics.kompics.p2p.bootstrap.P2pBootstrap;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClient;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClientInit;
import se.sics.kompics.p2p.fd.FailureDetector;
import se.sics.kompics.p2p.fd.PeerFailureSuspicion;
import se.sics.kompics.p2p.fd.StartProbingPeer;
import se.sics.kompics.p2p.fd.StopProbingPeer;
import se.sics.kompics.p2p.fd.ping.PingFailureDetector;
import se.sics.kompics.p2p.fd.ping.PingFailureDetectorInit;
import se.sics.kompics.p2p.simulator.launch.Configuration;
import se.sics.kompics.p2p.simulator.snapshot.Snapshot;
import se.sics.kompics.timer.Timer;

public final class Peer extends ComponentDefinition {
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
		}
	};

//-------------------------------------------------------------------
	Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
		public void handle(BootstrapResponse event) {
		}
	};
	
//-------------------------------------------------------------------
	Handler<FindSucc> handleFindSucc = new Handler<FindSucc>() {
		public void handle(FindSucc event) {
		}
	};
	
//-------------------------------------------------------------------
	Handler<FindSuccReply> handleFindSuccReply = new Handler<FindSuccReply>() {
		public void handle(FindSuccReply event) {
		}
	};
	
//-------------------------------------------------------------------
	Handler<PeriodicStabilization> handlePeriodicStabilization = new Handler<PeriodicStabilization>() {
		public void handle(PeriodicStabilization event) {
		}
	};

//-------------------------------------------------------------------
	Handler<WhoIsPred> handleWhoIsPred = new Handler<WhoIsPred>() {
		public void handle(WhoIsPred event) {
		}
	};

//-------------------------------------------------------------------
	Handler<WhoIsPredReply> handleWhoIsPredReply = new Handler<WhoIsPredReply>() {
		public void handle(WhoIsPredReply event) {
		}
	};

//-------------------------------------------------------------------
	Handler<Notify> handleNotify = new Handler<Notify>() {
		public void handle(Notify event) {
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
