package se.kth.ict.id2203.riwcm.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class ACKMessage extends Pp2pDeliver {

	protected ACKMessage(Address source) {
		super(source);
	}

}
