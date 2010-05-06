package se.sics.kompics.p2p.peer.tracker;

import se.sics.kompics.Init;
import se.sics.kompics.p2p.peer.PeerAddress;

public final class TrackerInit extends Init {

	private final PeerAddress peerSelf;
	private final int numOfPieces;

//-------------------------------------------------------------------	
	public TrackerInit(PeerAddress peerSelf, int numOfPieces) {
		super();
		this.peerSelf = peerSelf;
		this.numOfPieces = numOfPieces;
	}

//-------------------------------------------------------------------	
	public PeerAddress getPeerSelf() {
		return this.peerSelf;
	}

//-------------------------------------------------------------------	
	public int getNumOfPieces() {
		return this.numOfPieces;
	}
}
