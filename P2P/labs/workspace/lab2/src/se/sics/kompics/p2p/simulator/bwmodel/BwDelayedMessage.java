package se.sics.kompics.p2p.simulator.bwmodel;

import se.sics.kompics.p2p.peer.BTMessage;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class BwDelayedMessage extends Timeout {

	private final BTMessage message;
	private final boolean beingSent;

//-------------------------------------------------------------------	
	public BwDelayedMessage(ScheduleTimeout st, BTMessage message, boolean beingSent) {
		super(st);
		this.message = message;
		this.beingSent = beingSent;
	}

//-------------------------------------------------------------------	
	public BTMessage getMessage() {
		return message;
	}

//-------------------------------------------------------------------	
	public boolean isBeingSent() {
		return beingSent;
	}
}
