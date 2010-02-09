package se.kth.ict.id2203.epfd.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class RestoreEvent extends Event {
	private Address address;
	
	public RestoreEvent(Address address) {
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}
}
