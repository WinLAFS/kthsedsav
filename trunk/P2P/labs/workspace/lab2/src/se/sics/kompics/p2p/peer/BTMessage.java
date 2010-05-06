package se.sics.kompics.p2p.peer;

import se.sics.kompics.network.Message;

public abstract class BTMessage extends Message {

	private static final long serialVersionUID = -6815596147580962155L;

	private final PeerAddress source;
	private final PeerAddress destination;

//-------------------------------------------------------------------	
	public BTMessage(PeerAddress source, PeerAddress destination) {
		super(source.getPeerAddress(), destination.getPeerAddress());
		this.source = source;
		this.destination = destination;
	}

//-------------------------------------------------------------------	
	public PeerAddress getPeerDestination() {
		return destination;
	}

//-------------------------------------------------------------------	
	public PeerAddress getPeerSource() {
		return source;
	}

//-------------------------------------------------------------------	
	public abstract int getSize();
}

