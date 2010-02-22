package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class ReadValueDeliver extends Pp2pDeliver {
	private final int register;
	private final int requestID;
	private final int timestamp;
	private final int processRank;
	

	public ReadValueDeliver(Address source, int register, int requestID,
			int timestamp, int processRank) {
		super(source);
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
