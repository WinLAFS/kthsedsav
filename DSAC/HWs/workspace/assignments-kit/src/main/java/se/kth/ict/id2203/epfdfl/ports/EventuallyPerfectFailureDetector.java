package se.kth.ict.id2203.epfdfl.ports;

import se.kth.ict.id2203.epfdfl.events.RestoreEvent;
import se.kth.ict.id2203.epfdfl.events.SuspectEvent;
import se.sics.kompics.Event;
import se.sics.kompics.PortType;

public class EventuallyPerfectFailureDetector extends PortType {
	{
		positive(SuspectEvent.class);
		positive(RestoreEvent.class);
		negative(Event.class);
	}
}
