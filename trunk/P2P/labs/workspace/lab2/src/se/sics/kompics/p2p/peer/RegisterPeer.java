package se.sics.kompics.p2p.peer;

import se.sics.kompics.p2p.simulator.launch.PeerType;

public class RegisterPeer extends ControlMessage {
	private static final long serialVersionUID = 314947423942404050L;
	private final PeerType peerType;
	
//-------------------------------------------------------------------	
	public RegisterPeer(PeerAddress source, PeerAddress destination, PeerType peerType) {
		super(source, destination);
		this.peerType = peerType;
	}

//-------------------------------------------------------------------
	public PeerType getPeerType() {
		return this.peerType;
	}
}

