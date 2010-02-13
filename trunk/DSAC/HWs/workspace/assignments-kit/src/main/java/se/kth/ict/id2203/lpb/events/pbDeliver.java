package se.kth.ict.id2203.lpb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class pbDeliver extends Event {

	private final Address sender;
	private final String msg;
	
	public pbDeliver(Address self, String msg) {
		this.sender = self;
		this.msg = msg;
	}

	public Address getSender() {
		return sender;
	}

	public String getMsg() {
		return msg;
	}
	
}
