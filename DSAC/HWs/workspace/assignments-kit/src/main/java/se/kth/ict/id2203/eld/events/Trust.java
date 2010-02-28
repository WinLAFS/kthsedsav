package se.kth.ict.id2203.eld.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class Trust extends Event {
	private final Address leader;

	public Trust(Address leader) {
		super();
		this.leader = leader;
	}

	public Address getLeader() {
		return leader;
	}
	
}
