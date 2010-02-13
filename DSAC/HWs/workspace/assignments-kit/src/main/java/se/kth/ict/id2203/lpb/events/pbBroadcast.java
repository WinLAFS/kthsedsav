package se.kth.ict.id2203.lpb.events;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

public class pbBroadcast extends Event {
	
	private final pbDeliver pbd;
	
	public pbBroadcast(Address self, String msg) {
		pbd = new pbDeliver(self, msg);
	}

	public pbDeliver getPbd() {
		return pbd;
	}

}
