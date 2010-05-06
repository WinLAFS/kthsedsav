package se.sics.kompics.p2p.peer.btpeer;

import se.sics.kompics.Init;
import se.sics.kompics.p2p.peer.PeerAddress;

public final class BTPeerInit extends Init {

	private final PeerAddress peerSelf;
	private final int indegree;
	private final int outdegree;
	private final int numOfPieces;
	private final int pieceSize;
	private final PeerAddress tracker;

//-------------------------------------------------------------------	
	public BTPeerInit(PeerAddress peerSelf, int indegree, int outdegree, int numOfPieces, int pieceSize, PeerAddress tracker) {
		super();
		this.peerSelf = peerSelf;
		this.indegree = indegree;
		this.outdegree = outdegree;
		this.numOfPieces = numOfPieces;
		this.pieceSize = pieceSize;
		this.tracker = tracker;
	}

//-------------------------------------------------------------------	
	public PeerAddress getPeerSelf() {
		return this.peerSelf;
	}

//-------------------------------------------------------------------	
	public int getIndegree() {
		return this.indegree;
	}

//-------------------------------------------------------------------	
	public int getOutdegree() {
		return this.outdegree;
	}

//-------------------------------------------------------------------	
	public int getNumOfPieces() {
		return this.numOfPieces;
	}

//-------------------------------------------------------------------	
	public int getPieceSize() {
		return this.pieceSize;
	}

//-------------------------------------------------------------------	
	public PeerAddress getTracker() {
		return this.tracker;
	}
}
