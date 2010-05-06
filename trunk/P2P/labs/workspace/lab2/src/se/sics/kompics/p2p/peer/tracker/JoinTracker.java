package se.sics.kompics.p2p.peer.tracker;

import java.math.BigInteger;

import se.sics.kompics.Event;

public class JoinTracker extends Event {

	private final BigInteger peerId;

//-------------------------------------------------------------------
	public JoinTracker(BigInteger peerId) {
		this.peerId = peerId;
	}

//-------------------------------------------------------------------
	public BigInteger getPeerId() {
		return this.peerId;
	}
}
