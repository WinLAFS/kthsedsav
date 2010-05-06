package se.sics.kompics.p2p.peer;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;

public class CompleteUploaders extends Timeout {

//-------------------------------------------------------------------	
	public CompleteUploaders(SchedulePeriodicTimeout request) {
		super(request);
	}
}
