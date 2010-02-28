package se.kth.ict.id2203.beb.events;

import se.sics.kompics.address.Address;

public class BebDecidedDeliver extends BebDeliver {
	private final int id;
	private final String value;

	public BebDecidedDeliver(Address sender, int id,
			String value) {
		super(null, sender);
		this.id = id;
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
}
