package se.kth.ict.id2203.pfd.ports;

import se.kth.ict.id2203.pfd.events.SuspectEvent;
import se.sics.kompics.Event;
import se.sics.kompics.PortType;

public class PerfectFailureDetector extends PortType {
	{
		positive(SuspectEvent.class);
		negative(Event.class);
	}
}
