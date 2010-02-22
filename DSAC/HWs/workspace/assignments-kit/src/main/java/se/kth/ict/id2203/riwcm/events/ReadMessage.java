package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.address.Address;

public class ReadMessage extends BebDeliver {
	private final int register;
	private final int requestID;

	public ReadMessage(Address sender, int register,
			int requestID) {
		super(null, sender);
		this.register = register;
		this.requestID = requestID;
	}
	
	public int getRegister() {
		return register;
	}
	public int getRequestID() {
		return requestID;
	}
}
