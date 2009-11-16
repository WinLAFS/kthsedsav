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

import dks.arch.Event;

/**
 * The <code>RingLeaveDoneEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingLeaveDoneEvent.java 246 2007-03-12 14:41:03Z Roberto $
 */
public class RingLeaveDoneEvent extends Event {

	/**
	 * Event issued when the Leave Operation is completed
	 */
	public RingLeaveDoneEvent() {
	}

}
