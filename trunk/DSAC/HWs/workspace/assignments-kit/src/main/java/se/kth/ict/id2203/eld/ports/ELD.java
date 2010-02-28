package se.kth.ict.id2203.eld.ports;

import se.sics.kompics.PortType;

public class ELD extends PortType {
	{
//		negative(eventType);
		positive(se.kth.ict.id2203.eld.events.Trust.class);
	}
}
