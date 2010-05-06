package se.sics.kompics.p2p.simulator;

import java.math.BigInteger;

import se.sics.kompics.Event;

public final class BTPeerFail extends Event {

	private final BigInteger peerId;

//-------------------------------------------------------------------	
	public BTPeerFail(BigInteger peerId) {
		this.peerId = peerId;
	}

//-------------------------------------------------------------------	
	public BigInteger getPeerId() {
		return peerId;
	}
}
