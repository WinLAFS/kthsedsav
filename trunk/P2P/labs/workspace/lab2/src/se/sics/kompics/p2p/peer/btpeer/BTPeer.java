package se.sics.kompics.p2p.peer.btpeer;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.p2p.peer.CloseConnection;
import se.sics.kompics.p2p.peer.DataMessage;
import se.sics.kompics.p2p.peer.DownloadRequest;
import se.sics.kompics.p2p.peer.GetUploaderResponse;
import se.sics.kompics.p2p.peer.HandshakeRequest;
import se.sics.kompics.p2p.peer.HandshakeResponse;
import se.sics.kompics.p2p.peer.MessagePort;
import se.sics.kompics.p2p.peer.PeerAddress;
import se.sics.kompics.p2p.peer.PeerPort;
import se.sics.kompics.p2p.peer.CompleteUploaders;
import se.sics.kompics.p2p.peer.RegisterPeer;
import se.sics.kompics.p2p.simulator.launch.PeerType;
import se.sics.kompics.p2p.simulator.snapshot.Snapshot;

public final class BTPeer extends ComponentDefinition {
	Negative<PeerPort> peerPort = negative(PeerPort.class);

	Positive<MessagePort> network = positive(MessagePort.class);
	Positive<Timer> timer = positive(Timer.class);
	
	private PeerAddress peerSelf;
	private PeerAddress tracker;
	private int indegree;
	private int outdegree;
	private int numOfPieces;
	private int pieceSize;
	
    private Boolean[] buffer;
	
//-------------------------------------------------------------------
	public BTPeer() {
		subscribe(handleInit, control);
		subscribe(handleJoin, peerPort);
		subscribe(handleCompleteUploaders, timer);
		subscribe(handleGetUploaderResponse, network);
		subscribe(handleHandshakeRequest, network);
		subscribe(handleHandshakeResponse, network);
		subscribe(handleDownloadRequest, network);
		subscribe(handleRecvDataMessage, network);
		subscribe(handleCloseConnection, network);
	}

//-------------------------------------------------------------------
	Handler<BTPeerInit> handleInit = new Handler<BTPeerInit>() {
		public void handle(BTPeerInit init) {
			peerSelf = init.getPeerSelf();
			indegree = init.getIndegree();
			outdegree = init.getOutdegree();
			numOfPieces = init.getNumOfPieces();
			pieceSize = init.getPieceSize();
			tracker = init.getTracker();
			
			buffer = new Boolean[numOfPieces];
		}
	};

//-------------------------------------------------------------------
	Handler<JoinBTPeer> handleJoin = new Handler<JoinBTPeer>() {
		public void handle(JoinBTPeer event) {
			PeerType peerType = event.getPeerType();
			Snapshot.addPeer(peerSelf, numOfPieces, peerType);
			
			trigger(new RegisterPeer(peerSelf, tracker, peerType), network);
			
			if (peerType != PeerType.SEED) {
				SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(5000, 5000);
				spt.setTimeoutEvent(new CompleteUploaders(spt));
				trigger(spt, timer);
			}
		}
	};

//-------------------------------------------------------------------
	Handler<CompleteUploaders> handleCompleteUploaders = new Handler<CompleteUploaders>() {
		public void handle(CompleteUploaders event) {
		}
	};

//-------------------------------------------------------------------
	Handler<GetUploaderResponse> handleGetUploaderResponse = new Handler<GetUploaderResponse>() {
		public void handle(GetUploaderResponse event) {
		}
	};
	
//-------------------------------------------------------------------
	Handler<HandshakeRequest> handleHandshakeRequest = new Handler<HandshakeRequest>() {
		public void handle(HandshakeRequest event) {
		}
	};

//-------------------------------------------------------------------
	Handler<HandshakeResponse> handleHandshakeResponse = new Handler<HandshakeResponse>() {
		public void handle(HandshakeResponse event) {
		}
	};

//-------------------------------------------------------------------
	Handler<DownloadRequest> handleDownloadRequest = new Handler<DownloadRequest>() {
		public void handle(DownloadRequest event) {
		}
	};

//-------------------------------------------------------------------
	Handler<DataMessage> handleRecvDataMessage = new Handler<DataMessage>() {
		public void handle(DataMessage event) {
			int piece = event.getPiece();
			Snapshot.addPiece(peerSelf, piece);
		}
	};

//-------------------------------------------------------------------
	Handler<CloseConnection> handleCloseConnection = new Handler<CloseConnection>() {
		public void handle(CloseConnection event) {
		}
	};
}
