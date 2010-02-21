package se.kth.ict.id2203.riwc.ports;

import se.kth.ict.id2203.riwc.events.ReadRequest;
import se.kth.ict.id2203.riwc.events.ReadResponse;
import se.kth.ict.id2203.riwc.events.WriteRequest;
import se.kth.ict.id2203.riwc.events.WriteResponse;
import se.sics.kompics.PortType;

public class AtomicRegister extends PortType {
	{
		positive(ReadResponse.class);
		positive(WriteResponse.class);
		negative(ReadRequest.class);
		negative(WriteRequest.class);
	}
}
