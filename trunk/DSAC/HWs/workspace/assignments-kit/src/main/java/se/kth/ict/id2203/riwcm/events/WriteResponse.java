package se.kth.ict.id2203.riwcm.events;

import se.sics.kompics.Event;

public class WriteResponse extends Event {
	final private int register;

	public WriteResponse(int register) {
		super();
		this.register = register;
	}

	public int getRegister() {
		return register;
	}
}
