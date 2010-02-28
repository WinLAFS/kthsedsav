package se.kth.ict.id2203.ac.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class NackPP2PDeliver extends Pp2pDeliver {
	private final int id;
	
	public NackPP2PDeliver(Address source, int id) {
		super(source);
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
}
