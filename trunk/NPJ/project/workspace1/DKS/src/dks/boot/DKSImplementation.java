/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.boot;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.ring.events.RingJoinEvent;
import dks.ring.events.RingLeaveDoneEvent;
import dks.ring.events.RingLeaveEvent;
import dks.ring.events.RingNodeJoinedEvent;

/**
 * The <code>DKSImplementation</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSImplementation.java 444 2007-11-22 17:13:46Z roberto $
 */
public class DKSImplementation extends Component implements DKS {

	public DKSImplementation(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);

		registerForEvents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		register(RingLeaveDoneEvent.class, "handleRingLeaveDone");
		register(RingNodeJoinedEvent.class, "handleRingNodeJoined");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.boot.DKS#create()
	 */
	public synchronized void create() {

		// The argument it's null because it's the first d
		RingJoinEvent ringJoinEvent = new RingJoinEvent(null);
		trigger(ringJoinEvent);
		
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.boot.DKS#join(dks.addr.DKSRef)
	 */
	public synchronized void join(DKSRef ref) {
		RingJoinEvent ringJoinEvent = new RingJoinEvent(ref);
		trigger(ringJoinEvent);
		
		/*
		 * Blocks until the join procedure has been finished
		 */
		try {
			wait();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.boot.DKS#leave()
	 */
	public synchronized void leave() {
		RingLeaveEvent ringLeaveEvent = new RingLeaveEvent();
		trigger(ringLeaveEvent);

		/*
		 * Blocks until the leave procedure has been finished
		 */
		try {
			wait();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void handleRingNodeJoined(RingNodeJoinedEvent event) {
		notify();
	}

	public void handleRingLeaveDone(RingLeaveDoneEvent event) {
		notify();
	}

}
