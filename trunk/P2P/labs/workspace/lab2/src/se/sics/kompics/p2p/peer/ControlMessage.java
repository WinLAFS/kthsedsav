package se.sics.kompics.p2p.peer;

public class ControlMessage extends BTMessage {

	private static final long serialVersionUID = -6815596147580962155L;

//-------------------------------------------------------------------	
	public ControlMessage(PeerAddress source, PeerAddress destination) {
		super(source, destination);
	}

//-------------------------------------------------------------------	
	public int getSize() {
		return 0;
	}
}

