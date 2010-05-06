package se.sics.kompics.p2p.simulator;

import java.math.BigInteger;

import se.sics.kompics.Event;
import se.sics.kompics.p2p.simulator.launch.PeerType;

public final class BTPeerJoin extends Event {

	private final BigInteger peerId;
	private final PeerType peerType;

//-------------------------------------------------------------------	
	public BTPeerJoin(BigInteger peerId, PeerType peerType) {
		this.peerId = peerId;
		this.peerType = peerType;
	}

//-------------------------------------------------------------------	
	public BigInteger getPeerId() {
		return this.peerId;
	}

//-------------------------------------------------------------------	
	public PeerType getPeerType() {
		return this.peerType;
	}
}
