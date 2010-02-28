package se.kth.ict.id2203.ac.events;

import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.address.Address;

public class BEBACWriteDeliver extends BebDeliver {
	
	private final int id;
	private final int tstamp;
	private final String tempValue;
	
	public BEBACWriteDeliver(Address sender, int id, int tstamp, String tempValue) {
		super("", sender);
		this.id=id;
		this.tstamp=tstamp;
		this.tempValue=tempValue;
	}

	public int getId() {
		return id;
	}

	public int getTstamp() {
		return tstamp;
	}

	public String getTempValue() {
		return tempValue;
	}
	

}
