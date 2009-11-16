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
 * The <code>RingJoinReqOpExpired</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingOperationRequestExpiredEvent.java 213 2007-02-26 18:09:40Z Roberto $
 */
public class RingOperationRequestExpiredEvent extends Event {

	/**
	 * Event issued when the timer for the JoinReq OperationCarrier is expired
	 */
	public RingOperationRequestExpiredEvent() {
	}

}
