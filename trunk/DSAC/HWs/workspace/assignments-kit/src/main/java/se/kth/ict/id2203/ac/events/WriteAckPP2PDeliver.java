package se.kth.ict.id2203.ac.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class WriteAckPP2PDeliver extends Pp2pDeliver {
	
	private final int id;
	private final int ts;
	
	public WriteAckPP2PDeliver(Address source, int id, int ts) {
		super(source);
		this.id=id;
		this.ts=ts;
	}

	public int getId() {
		return id;
	}

	public int getTs() {
		return ts;
	}
	
	
}
