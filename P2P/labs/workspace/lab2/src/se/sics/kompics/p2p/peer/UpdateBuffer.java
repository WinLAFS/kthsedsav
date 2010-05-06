package se.sics.kompics.p2p.peer;

public class UpdateBuffer extends ControlMessage {
	private static final long serialVersionUID = -8995112275205487250L;
	
	private final int pieceIndex;

//-------------------------------------------------------------------	
	public UpdateBuffer(PeerAddress source, PeerAddress destination, int pieceIndex) {
		super(source, destination);
		this.pieceIndex = pieceIndex;
	}

//-------------------------------------------------------------------	
	public int getPieceIndex() {
		return this.pieceIndex;
	}

}

