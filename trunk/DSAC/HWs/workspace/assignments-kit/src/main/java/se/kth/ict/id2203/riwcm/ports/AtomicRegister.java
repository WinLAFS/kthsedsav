package se.kth.ict.id2203.riwcm.ports;

import se.kth.ict.id2203.riwcm.events.ReadRequest;
import se.kth.ict.id2203.riwcm.events.ReadResponse;
import se.kth.ict.id2203.riwcm.events.WriteRequest;
import se.kth.ict.id2203.riwcm.events.WriteResponse;
import se.sics.kompics.PortType;

public class AtomicRegister extends PortType {
	{
		positive(ReadResponse.class);
		positive(WriteResponse.class);
		negative(ReadRequest.class);
		negative(WriteRequest.class);
	}
}
