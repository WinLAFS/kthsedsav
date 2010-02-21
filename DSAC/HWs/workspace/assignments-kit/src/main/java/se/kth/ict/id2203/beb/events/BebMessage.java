package se.kth.ict.id2203.beb.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class BebMessage extends Pp2pDeliver {

	private final BebDeliver bebDeliver;
	
	public BebMessage(Address source, BebDeliver bebDeliver) {
		super(source);
		this.bebDeliver = bebDeliver;
	}

	public BebDeliver getBebDeliver() {
		return bebDeliver;
	}
	
}
