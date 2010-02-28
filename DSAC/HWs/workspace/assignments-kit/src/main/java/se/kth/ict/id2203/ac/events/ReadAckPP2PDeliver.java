package se.kth.ict.id2203.ac.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class ReadAckPP2PDeliver extends Pp2pDeliver {
	
	private final int id;
	private final int wts;
	private final String val;
	private final int ts;
	
	public ReadAckPP2PDeliver(Address source, int id, int wts, String val, int ts) {
		super(source);
		this.id=id;
		this.wts=wts;
		this.val=val;
		this.ts=ts;
	}

	public int getId() {
		return id;
	}

	public int getWts() {
		return wts;
	}

	public String getVal() {
		return val;
	}

	public int getTs() {
		return ts;
	}
	
	
}
