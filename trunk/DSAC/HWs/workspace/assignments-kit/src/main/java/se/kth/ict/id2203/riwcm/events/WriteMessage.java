package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.address.Address;

public class WriteMessage extends BebDeliver {

	public WriteMessage(String message, Address sender) {
		super(message, sender);
		// TODO Auto-generated constructor stub
	}

}
