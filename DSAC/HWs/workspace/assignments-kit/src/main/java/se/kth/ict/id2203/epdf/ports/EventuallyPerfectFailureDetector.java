package se.kth.ict.id2203.epfd.ports;

import se.kth.ict.id2203.epfd.events.RestoreEvent;
import se.kth.ict.id2203.epfd.events.CrashEvent;
import se.sics.kompics.Event;
import se.sics.kompics.PortType;

public class EventuallyPerfectFailureDetector extends PortType {
	{
		positive(CrashEvent.class);
		positive(RestoreEvent.class);
		negative(Event.class);
	}
}
