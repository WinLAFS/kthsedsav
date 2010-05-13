package se.sics.kompics.p2p.peer.tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.p2p.peer.GetUploaderRequest;
import se.sics.kompics.p2p.peer.GetUploaderResponse;
import se.sics.kompics.p2p.peer.MessagePort;
import se.sics.kompics.p2p.peer.PeerAddress;
import se.sics.kompics.p2p.peer.PeerPort;
import se.sics.kompics.p2p.peer.RegisterPeer;
import se.sics.kompics.p2p.peer.UpdateBuffer;
import se.sics.kompics.p2p.simulator.launch.PeerType;

public final class Tracker extends ComponentDefinition {
	Negative<PeerPort> peerPort = negative(PeerPort.class);

	Positive<MessagePort> network = positive(MessagePort.class);
	Positive<Timer> timer = positive(Timer.class);
	
	private PeerAddress peerSelf;
	private int numOfPieces;
    private HashMap<PeerAddress, Boolean[]> peers = new HashMap<PeerAddress, Boolean[]>();
	
//-------------------------------------------------------------------
	public Tracker() {
		subscribe(handleInit, control);
		subscribe(handleJoin, peerPort);
		subscribe(handleRegisterPeer, network);
		subscribe(handleUpdateBuffer, network);
		subscribe(handleGetUploader, network);
	}

//-------------------------------------------------------------------
	Handler<TrackerInit> handleInit = new Handler<TrackerInit>() {
		public void handle(TrackerInit init) {
			peerSelf = init.getPeerSelf();
			numOfPieces = init.getNumOfPieces();
		}
	};

//-------------------------------------------------------------------
	Handler<JoinTracker> handleJoin = new Handler<JoinTracker>() {
		public void handle(JoinTracker event) {
		}
	};

//-------------------------------------------------------------------
	Handler<RegisterPeer> handleRegisterPeer = new Handler<RegisterPeer>() {
		public void handle(RegisterPeer event) {
			PeerType peerType = event.getPeerType();
			PeerAddress peer = event.getPeerSource();
	        Boolean[] buffer = new Boolean[numOfPieces];
	        boolean piece;
	        
	        if (peerType == PeerType.SEED)
	        	piece = true;
	        else
	        	piece = false;

	        for (int i = 0; i < numOfPieces; i++)
	        	buffer[i] = piece;

	        peers.put(peer, buffer);
		}
	};

//-------------------------------------------------------------------
	Handler<UpdateBuffer> handleUpdateBuffer = new Handler<UpdateBuffer>() {
		public void handle(UpdateBuffer event) {
			//step 2
			int pieceReceivedIndex = event.getPieceIndex();
			PeerAddress peer = event.getPeerSource();
			
			Boolean[] peerBuffer = peers.get(peer);
			peerBuffer[pieceReceivedIndex] = true;
		}
	};

//-------------------------------------------------------------------
	Handler<GetUploaderRequest> handleGetUploader = new Handler<GetUploaderRequest>() {
		public void handle(GetUploaderRequest event) {
			boolean sent = false;
			
			PeerAddress requester = event.getPeerSource(); 
			Boolean[] requesterBuffer = peers.get(requester);
			
			List<PeerAddress> peerSet = new ArrayList<PeerAddress>(peers.keySet()); 
			Collections.shuffle((List<PeerAddress>) peerSet);

			for (PeerAddress responder : peerSet) {
				if (!requester.equals(responder)) {
					Boolean[] responderBuffer = peers.get(responder);
					
					ArrayList<Integer> shuffledList = createRandomListOfIntegers(numOfPieces);
					Iterator<Integer> shuffledIterator = shuffledList.iterator();
					while (shuffledIterator.hasNext()) {
						Integer index = (Integer) shuffledIterator.next();
						if ((!requesterBuffer[index]) && responderBuffer[index]) {
							//it is ok! use this
							trigger(new GetUploaderResponse(peerSelf, requester, responder, index), network);
							sent = true;
							break;
						}
					}
					if (sent) {
						break;
					}
				}
			}
		}
	};
	
	ArrayList<Integer> createRandomListOfIntegers(int size) {
		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			intList.add(new Integer(i));
		}
		
		Collections.shuffle(intList);
		
		return intList;
	}
}

