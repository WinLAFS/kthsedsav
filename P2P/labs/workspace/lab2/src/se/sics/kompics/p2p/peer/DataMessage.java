package se.sics.kompics.p2p.peer;

public class DataMessage extends BTMessage {

	private static final long serialVersionUID = -6815596147580962155L;
	private final int piece;
	private final int size;

//-------------------------------------------------------------------	
	public DataMessage(PeerAddress source, PeerAddress destination, int piece, int size) {
		super(source, destination);
		this.piece = piece;
		this.size = size;
	}

//-------------------------------------------------------------------	
	public int getPiece() {
		return this.piece;
	}

//-------------------------------------------------------------------	
	public int getSize() {
		return this.size;
	}
}

