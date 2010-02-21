package se.kth.ict.id2203.riwc.events;

import se.sics.kompics.Event;

public class ReadRequest extends Event {
	final private int register;

	public ReadRequest(int register) {
		super();
		this.register = register;
	}

	public int getRegister() {
		return register;
	}
	
}
