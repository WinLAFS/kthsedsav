package se.kth.ict.id2203.beb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class BebDeliver extends Message{
	private final String message;
	private final Address sender;
	
	public BebDeliver(String message, Address sender) {
		super(sender, null);
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
