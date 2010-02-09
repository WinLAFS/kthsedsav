package se.kth.ict.id2203.epfd.events;

import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatMessage extends Pp2pDeliver {

	private static final long serialVersionUID = 6763881557581375553L;

	public HeartbeatMessage(Address source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	public Object getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
