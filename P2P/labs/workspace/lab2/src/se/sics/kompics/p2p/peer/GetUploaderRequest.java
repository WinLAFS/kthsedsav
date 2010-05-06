package se.sics.kompics.p2p.peer;

public class GetUploaderRequest extends ControlMessage {
	private static final long serialVersionUID = 314947423942404050L;

//-------------------------------------------------------------------	
	public GetUploaderRequest(PeerAddress source, PeerAddress destination) {
		super(source, destination);
	}
}

