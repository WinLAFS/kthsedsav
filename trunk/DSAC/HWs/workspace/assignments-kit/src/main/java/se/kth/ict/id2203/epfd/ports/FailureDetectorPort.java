package se.kth.ict.id2203.pfd.ports;

import se.kth.ict.id2203.pfd.events.CrashEvent;
import se.sics.kompics.PortType;

public class FailureDetectorPort extends PortType {
	{
		positive(CrashEvent.class);
	}
}
