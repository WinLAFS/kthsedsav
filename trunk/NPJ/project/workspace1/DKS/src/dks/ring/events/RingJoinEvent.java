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
 * The <code>RingJoinEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingJoinEvent.java 444 2007-11-22 17:13:46Z roberto $
 */
public class RingJoinEvent extends Event {

	private DKSRef nodeRef = null;

	/**
	 * Event issued when a d wants to join the ring
	 */
	public RingJoinEvent(DKSRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * @return Returns the nodeRef.
	 */
	public DKSRef getNodeRef() {
		return nodeRef;
	}

}
