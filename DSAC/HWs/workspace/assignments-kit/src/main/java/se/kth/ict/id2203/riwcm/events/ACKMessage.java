package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class ACKMessage extends Pp2pDeliver {
	
	private int register;
	private int requestID;
	
	public int getRegister() {
		return register;
	}

	public void setRegister(int register) {
		this.register = register;
	}

	public int getRequestID() {
		return requestID;
	}

	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}

	public ACKMessage(Address source, int register, int requestID) {
		super(source);
		this.register = register;
		this.requestID = requestID;
	}

}
