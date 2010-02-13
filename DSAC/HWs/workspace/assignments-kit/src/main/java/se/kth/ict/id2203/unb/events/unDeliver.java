package se.kth.ict.id2203.unb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class unDeliver extends Event {

	private static final long serialVersionUID = 5790783562931987100L;
	private final Address source;
	private final String message;

	public unDeliver(Address source, String message) {
		this.source = source;
		this.message = message;
	}

	public Address getSender() {
		return source;
	}

	public String getMessage() {
		return message;
	}

}
