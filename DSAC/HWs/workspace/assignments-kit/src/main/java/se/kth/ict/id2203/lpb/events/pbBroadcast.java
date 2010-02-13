package se.kth.ict.id2203.lpb.events;

import se.sics.kompics.Event;

public class pbBroadcast extends Event {
	
	private final pbDeliver pbd;
//	
//	public pbBroadcast(Address self, String msg) {
//		pbd = new pbDeliver(self, msg);
//	}

	public pbBroadcast(pbDeliver pbd) {
		this.pbd = pbd;
	}

	public pbDeliver getPbd() {
		return pbd;
	}

}
