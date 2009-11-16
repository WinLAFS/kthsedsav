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

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>RingNodeLeftEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingNodeLeftEvent.java 246 2007-03-12 14:41:03Z Roberto $
 */
public class RingNodeLeftEvent extends Event {

	private DKSRef ref;
	
	/**
	 * Event Issued when a d in the ring has left
	 */
	public RingNodeLeftEvent(DKSRef ref) {
		this.ref=ref;
	}
	/**
	 * @return Returns the ref.
	 */
	public DKSRef getRef() {
		return ref;
	}

	
}
