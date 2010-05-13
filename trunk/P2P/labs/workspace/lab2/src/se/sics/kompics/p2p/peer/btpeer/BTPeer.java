package se.sics.kompics.p2p.peer.btpeer;

import java.util.ArrayList;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.p2p.peer.CloseConnection;
import se.sics.kompics.p2p.peer.DataMessage;
import se.sics.kompics.p2p.peer.DownloadRequest;
import se.sics.kompics.p2p.peer.GetUploaderRequest;
import se.sics.kompics.p2p.peer.GetUploaderResponse;
import se.sics.kompics.p2p.peer.HandshakeRequest;
import se.sics.kompics.p2p.peer.HandshakeResponse;
import se.sics.kompics.p2p.peer.HandshakeStatus;
import se.sics.kompics.p2p.peer.MessagePort;
import se.sics.kompics.p2p.peer.PeerAddress;
import se.sics.kompics.p2p.peer.PeerPort;
import se.sics.kompics.p2p.peer.CompleteUploaders;
import se.sics.kompics.p2p.peer.RegisterPeer;
import se.sics.kompics.p2p.peer.UpdateBuffer;
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
    private boolean isSeed;
    private int numOfUploaders = 0;
    private int numOfDownloaders = 0;
    private ArrayList<PeerAddress> myUploaders = new ArrayList<PeerAddress>();
	
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

			isSeed = true;
			if (peerType != PeerType.SEED) {
				isSeed = false;
				SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(5000, 5000); //check every 5s if you are a seed
				spt.setTimeoutEvent(new CompleteUploaders(spt));
				trigger(spt, timer);
			}
			
			for (int i = 0; i < numOfPieces; i++) {
				buffer[i] = isSeed;
			}
		}
	};

//-------------------------------------------------------------------
	Handler<CompleteUploaders> handleCompleteUploaders = new Handler<CompleteUploaders>() {
		public void handle(CompleteUploaders event) {
			//here you check if you became a seed..
			//step 5
			if (!isSeed) {
				isSeed = true;
				for (boolean piece : buffer) {
					isSeed = isSeed && piece;
					if (!isSeed)
						break;
				}
				
				if (!isSeed) {
					int freeDLSlots = indegree - numOfUploaders ;
					if (freeDLSlots > 0) {
						for (int i = 1; i <= freeDLSlots; i++) {
							trigger(new GetUploaderRequest(peerSelf, tracker), network);
						}
					}
				}
			}
			
			
			
		}
	};

//-------------------------------------------------------------------
	Handler<GetUploaderResponse> handleGetUploaderResponse = new Handler<GetUploaderResponse>() {
		public void handle(GetUploaderResponse event) {
			PeerAddress uploader = event.getUploader();
			int pieceIndex = event.getPiece();
			
			//TODO update the list somewhere
			if (!myUploaders.contains(uploader)) {
				trigger(new HandshakeRequest(peerSelf, uploader, pieceIndex), network);
			}
			
		}
	};
	
//-------------------------------------------------------------------
	Handler<HandshakeRequest> handleHandshakeRequest = new Handler<HandshakeRequest>() {
		public void handle(HandshakeRequest event) {
			PeerAddress downloader = event.getPeerSource();
			int pieceIndex = event.getPiece();

			if (outdegree - numOfDownloaders > 0) {
				trigger(new HandshakeResponse(peerSelf, downloader, pieceIndex, HandshakeStatus.ACCEPT), network);
			}
			else {
				trigger(new HandshakeResponse(peerSelf, downloader, pieceIndex, HandshakeStatus.REJECT), network);
			}
		}
	};

//-------------------------------------------------------------------
	Handler<HandshakeResponse> handleHandshakeResponse = new Handler<HandshakeResponse>() {
		public void handle(HandshakeResponse event) {
			if (event.getHandshakeStatus() == HandshakeStatus.ACCEPT) {
				PeerAddress uploader = event.getPeerSource();
				
				numOfUploaders++;
				myUploaders.add(uploader);
				
				trigger(new DownloadRequest(peerSelf, uploader, event.getPiece()), network);
			}
		}
	};

//-------------------------------------------------------------------
	Handler<DownloadRequest> handleDownloadRequest = new Handler<DownloadRequest>() {
		public void handle(DownloadRequest event) {
			trigger(new DataMessage(peerSelf, event.getPeerSource(), event.getPiece(), pieceSize), network);
			numOfDownloaders++;
		}
	};

//-------------------------------------------------------------------
	Handler<DataMessage> handleRecvDataMessage = new Handler<DataMessage>() {
		public void handle(DataMessage event) {
			int piece = event.getPiece();
			buffer[piece] = true;
			Snapshot.addPiece(peerSelf, piece);
			trigger(new UpdateBuffer(peerSelf, tracker, piece), network);
			
			numOfUploaders--;
			myUploaders.remove(event.getPeerSource());
			
			trigger(new CloseConnection(peerSelf, event.getPeerSource()), network);
		}
	};

//-------------------------------------------------------------------
	Handler<CloseConnection> handleCloseConnection = new Handler<CloseConnection>() {
		public void handle(CloseConnection event) {
			numOfDownloaders--;
		}
	};
}
