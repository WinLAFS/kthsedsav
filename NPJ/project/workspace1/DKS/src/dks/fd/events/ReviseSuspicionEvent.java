/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd.events;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>ReviseSuspicionEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ReviseSuspicionEvent.java 179 2007-02-02 10:51:06Z Roberto $
 */
public class ReviseSuspicionEvent extends Event {
	
	private DKSRef peer;
	
	/**
	 * Event issued when a peer that was previously suspected to be dead is
	 * found alive
	 */
	public ReviseSuspicionEvent(DKSRef ref) {
		this.peer=ref;
	}

	/**
	 * @return Returns the peer.
	 */
	public DKSRef getRectifiedPeer() {
		return peer;
	}

}
