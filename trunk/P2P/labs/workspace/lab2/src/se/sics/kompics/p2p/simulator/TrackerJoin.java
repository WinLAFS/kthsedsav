package se.sics.kompics.p2p.simulator;

import java.math.BigInteger;

import se.sics.kompics.Event;

public final class TrackerJoin extends Event {

	private final BigInteger peerId;

//-------------------------------------------------------------------	
	public TrackerJoin(BigInteger id) {
		this.peerId = id;
	}

//-------------------------------------------------------------------	
	public BigInteger getPeerId() {
		return this.peerId;
	}

}
