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
 * @version $Id: RingJoinEvent.java 246 2007-03-12 14:41:03Z Roberto $
 */
public class RingRetryJoinEvent extends Event {

	private DKSRef nodeId = null;

	public RingRetryJoinEvent() {
	}

	/**
	 * Event issued when a d wants to join the ring
	 */
	public RingRetryJoinEvent(DKSRef nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return Returns the nodeId.
	 */
	public DKSRef getNodeId() {
		return nodeId;
	}

}
