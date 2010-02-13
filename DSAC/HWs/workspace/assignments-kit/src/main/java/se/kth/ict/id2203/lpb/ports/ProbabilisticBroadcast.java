package se.kth.ict.id2203.lpb.ports;

import javax.swing.event.DocumentEvent.EventType;

import se.kth.ict.id2203.lpb.events.pbBroadcast;
import se.kth.ict.id2203.lpb.events.pbDeliver;
import se.sics.kompics.PortType;

public class ProbabilisticBroadcast extends PortType {
	{
		positive(pbBroadcast.class);
		negative(pbDeliver.class);
	}
}
