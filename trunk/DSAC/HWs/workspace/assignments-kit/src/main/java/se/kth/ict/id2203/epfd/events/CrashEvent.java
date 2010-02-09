package se.kth.ict.id2203.pfd.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class CrashEvent extends Event {
	private Address address;
	
	public CrashEvent(Address address) {
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}
}
