/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.events;

import java.util.concurrent.locks.ReentrantLock;

import dks.arch.Event;

/**
 * The <code>RingLeaveEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingLeaveEvent.java 352 2007-07-06 18:24:01Z ahmad $
 */
public class RingLeaveEvent extends Event {
	
	
	private ReentrantLock lock;

	// REMOVE ME ?? or not?? please someone check!!
	public RingLeaveEvent() {
		this.lock=null;
	}
	
	
	/**
	 * Event Issued when a d wants to leave the Ring
	 * @param lock 
	 */
	public RingLeaveEvent(ReentrantLock lock) {
		this.lock=lock;
	}

	/**
	 * @return Returns the lock.
	 */
	public ReentrantLock getLock() {
		return lock;
	}

}
