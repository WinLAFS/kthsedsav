package se.sics.kompics.p2p.peer;

public class GetUploaderResponse extends ControlMessage {
	private static final long serialVersionUID = 314947423942404050L;
	private final PeerAddress uploader;
	private final int piece;
	
//-------------------------------------------------------------------	
	public GetUploaderResponse(PeerAddress source, PeerAddress destination, PeerAddress uploader, int piece) {
		super(source, destination);
		this.uploader = uploader;
		this.piece = piece;
	}

//-------------------------------------------------------------------	
	public PeerAddress getUploader() {
		return this.uploader;
	}

//-------------------------------------------------------------------	
	public int getPiece() {
		return this.piece;
	}
}

