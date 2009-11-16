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
 * The <code>CommPeerSuspected</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SuspectEvent.java 148 2007-01-22 15:49:48Z Roberto $
 */
public class SuspectEvent extends Event {

	public DKSRef peer;

	/**
	 * Event issued when the failure detector suspects about a peer beeing
	 * unreachable
	 * 
	 * @param peer
	 *            The peer beeing suspected
	 */
	public SuspectEvent(DKSRef peer) {
		super();
		this.peer = peer;
	}

	/**
	 * @return Returns the peer.
	 */
	public DKSRef getSuspectedPeer() {
		return peer;
	}

}
