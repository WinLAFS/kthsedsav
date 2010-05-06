package se.sics.kompics.p2p.simulator.bwmodel;

import se.sics.kompics.p2p.peer.BTMessage;

public final class Link {

	private final long capacity; // bytes per second
	private long lastExitTime;

//-------------------------------------------------------------------	
	public Link(long capacity) {
		this.capacity = capacity;
		this.lastExitTime = System.currentTimeMillis();
	}

//-------------------------------------------------------------------	
	public long addMessage(BTMessage message) {
		double size = message.getSize();
		double capacityPerMs = ((double) capacity) / 1000;
		long bwDelayMs = (long) (size / capacityPerMs);
		long now = System.currentTimeMillis();

		if (now >= lastExitTime) {
			// the pipe is empty
			lastExitTime = now + bwDelayMs;
		} else {
			// the pipe has some messages and the last message's 
			// exit time is stored in lastExitTime
			lastExitTime = lastExitTime + bwDelayMs;

			bwDelayMs = lastExitTime - now;
		}

		return bwDelayMs;
	}
}
