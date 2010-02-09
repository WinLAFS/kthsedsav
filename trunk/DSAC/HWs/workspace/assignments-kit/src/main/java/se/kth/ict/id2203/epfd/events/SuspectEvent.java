package se.kth.ict.id2203.epfd.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class SuspectEvent extends Event {
	private Address address;
	
	public SuspectEvent(Address address) {
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}
}
