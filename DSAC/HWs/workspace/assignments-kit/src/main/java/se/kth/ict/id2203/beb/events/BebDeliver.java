package se.kth.ict.id2203.beb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class BebDeliver extends Event {
	private final String message;
	private final Address sender;
	
	public BebDeliver(String message, Address sender) {
		super();
		this.message = message;
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public Address getSender() {
		return sender;
	}
	
}
