package se.kth.ict.id2203.beb.ports;

import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebDeliver;
import se.sics.kompics.PortType;

public class BEBPort extends PortType {
	{
		positive(BebDeliver.class);
		negative(BebBroadcast.class);
	}
}
