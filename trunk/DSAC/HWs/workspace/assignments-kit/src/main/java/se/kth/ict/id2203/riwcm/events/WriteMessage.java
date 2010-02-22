package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.address.Address;

public class WriteMessage extends BebDeliver {
	private final int register;
	private final int requestID;
	private final int timestamp;
	private final int processRank;
	
	public WriteMessage(String value, Address sender, int register,
			int requestID, int timestamp, int processRank) {
		super(value, sender);
		this.register = register;
		this.requestID = requestID;
		this.timestamp = timestamp;
		this.processRank = processRank;
	}

	public int getRegister() {
		return register;
	}

	public int getRequestID() {
		return requestID;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public int getProcessRank() {
		return processRank;
	}

}
