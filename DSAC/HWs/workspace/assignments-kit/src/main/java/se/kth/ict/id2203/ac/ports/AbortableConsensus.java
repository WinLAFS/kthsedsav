package se.kth.ict.id2203.ac.ports;

import se.kth.ict.id2203.ac.events.ACDecide;
import se.kth.ict.id2203.ac.events.ACPropose;
import se.sics.kompics.PortType;

public class AbortableConsensus extends PortType {
	{
		positive(ACDecide.class);
		negative(ACPropose.class);
	}
}
