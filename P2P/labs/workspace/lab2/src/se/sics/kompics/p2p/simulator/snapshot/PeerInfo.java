package se.sics.kompics.p2p.simulator.snapshot;

import se.sics.kompics.p2p.simulator.launch.PeerType;

public class PeerInfo {
	private Boolean[] buffer;

//-------------------------------------------------------------------
	public PeerInfo(PeerType peerType, int numOfPieces) {
		this.buffer = new Boolean[numOfPieces];
		boolean state = false;
		
		if (peerType == PeerType.SEED)
			state = true;
		
		for (int i = 0; i < numOfPieces; i++)
			this.buffer[i] = state;
	}
	
//-------------------------------------------------------------------
	public void addPiece(int piece) {
		this.buffer[piece] = true;
	}

//-------------------------------------------------------------------
	public boolean isSeed() {
		for (int i = 0; i < this.buffer.length; i++) {
			if (this.buffer[i] == false)
				return false;
		}
		
		return true;
	}

//-------------------------------------------------------------------
	@Override
	public String toString() {
		String str = new String("[");
		
		for (int i = 0; i < this.buffer.length; i++) {
			if (this.buffer[i] == true)
				str += "1";
			else
				str += "0";
			
			if (i < this.buffer.length - 1)
				str += ", ";
		}
		
		str += "]";
		
		return str;
	}
}
