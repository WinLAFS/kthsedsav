package se.kth.ict.id2203.uc.ports;

import se.kth.ict.id2203.uc.events.UCDecide;
import se.kth.ict.id2203.uc.events.UCPropose;
import se.sics.kompics.PortType;

public class UniformConsensus extends PortType {
	{
		positive(UCDecide.class);
		negative(UCPropose.class);
	}
}
