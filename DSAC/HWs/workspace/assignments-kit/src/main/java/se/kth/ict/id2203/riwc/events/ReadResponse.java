package se.kth.ict.id2203.riwc.events;

import se.sics.kompics.Event;

public class ReadResponse extends Event {
	private final int register;
	private final String value;
	
	public ReadResponse(int register, String value) {
		super();
		this.register = register;
		this.value = value;
	}

	public int getRegister() {
		return register;
	}

	public String getValue() {
		return value;
	}
}
