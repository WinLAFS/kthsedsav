package se.kth.ict.id2203.beb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class BebBroadcast extends Event {
	private final BebMessage bebMessage;
	private final Address sender;

	public BebBroadcast(BebMessage bebMessage, Address sender) {
		super();
		this.bebMessage = bebMessage;
		this.sender = sender;
	}
	public BebMessage getBebMessage() {
		return bebMessage;
	}
	public Address getSender() {
		return sender;
	}
	
}
