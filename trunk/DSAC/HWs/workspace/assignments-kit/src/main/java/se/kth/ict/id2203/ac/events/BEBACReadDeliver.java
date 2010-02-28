package se.kth.ict.id2203.ac.events;

import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.address.Address;

public class BEBACReadDeliver extends BebDeliver {
	
	private final int id;
	private final int ts;
	
	public BEBACReadDeliver(int id, int ts, Address sender) {
		super("", sender);
		this.id = id;
		this.ts = ts;
	}
	
	public int getId() {
		return id;
	}
	
	public int getTs() {
		return ts;
	}
}
