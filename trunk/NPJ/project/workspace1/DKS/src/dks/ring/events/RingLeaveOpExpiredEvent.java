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
 * The <code>RingLeaveOpExpiredEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingLeaveOpExpiredEvent.java 141 2007-01-17 14:31:18Z Roberto $
 */
public class RingLeaveOpExpiredEvent extends Event {

	/**
	 * Event issued when a Leave operation has expired
	 */
	public RingLeaveOpExpiredEvent() {
	}

}
