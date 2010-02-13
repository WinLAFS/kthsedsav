package se.kth.ict.id2203.unb.ports;

import se.kth.ict.id2203.unb.events.unBroadcast;
import se.kth.ict.id2203.unb.events.unDeliver;
import se.sics.kompics.PortType;

public class UnreliableBroadcast extends PortType {
	{
//		positive(unBroadcast.class);
//		negative(unDeliver.class);
		negative(unBroadcast.class);
		positive(unDeliver.class);
	}
}
