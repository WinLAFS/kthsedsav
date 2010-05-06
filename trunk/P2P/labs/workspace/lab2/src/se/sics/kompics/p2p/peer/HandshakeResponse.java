package se.sics.kompics.p2p.peer;

public class HandshakeResponse extends ControlMessage {
	private static final long serialVersionUID = 314947423942404050L;
	private final int piece;
	private final HandshakeStatus handshakeStatus;
	
//-------------------------------------------------------------------	
	public HandshakeResponse(PeerAddress source, PeerAddress destination, int piece, HandshakeStatus handshakeStatus) {
		super(source, destination);
		this.piece = piece;
		this.handshakeStatus = handshakeStatus;
	}

//-------------------------------------------------------------------	
	public int getPiece() {
		return this.piece;
	}

//-------------------------------------------------------------------	
	public HandshakeStatus getHandshakeStatus() {
		return this.handshakeStatus;
	}
}

