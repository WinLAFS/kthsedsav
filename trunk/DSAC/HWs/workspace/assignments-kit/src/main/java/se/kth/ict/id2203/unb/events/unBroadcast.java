package se.kth.ict.id2203.unb.events;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class unBroadcast extends Message {

	private static final long serialVersionUID = 1194336603571260767L;
	private final unDeliver undeliver;

	protected unBroadcast(Address source, Address destination, unDeliver undeliver) {
		super(source, destination);
		this.undeliver = undeliver;
		// TODO Auto-generated constructor stub
	}

	public unDeliver getUnDeliver() {
		return undeliver;
	}

}
