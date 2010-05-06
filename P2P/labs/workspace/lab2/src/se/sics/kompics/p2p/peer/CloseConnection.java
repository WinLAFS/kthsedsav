package se.sics.kompics.p2p.peer;

public class CloseConnection extends ControlMessage {
	private static final long serialVersionUID = 314947423942404050L;
	
//-------------------------------------------------------------------	
	public CloseConnection(PeerAddress source, PeerAddress destination) {
		super(source, destination);
	}
}

