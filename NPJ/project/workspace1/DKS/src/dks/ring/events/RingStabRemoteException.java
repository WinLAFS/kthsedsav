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
 * The <code>RingStabRemoteException</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingStabRemoteException.java 125 2006-11-29 19:27:52Z Roberto $
 */
public class RingStabRemoteException extends Event {

	/**
	 * Event issued when one of the timeouts in the Stabilization algorithm
	 * expires
	 */
	public RingStabRemoteException() {
	}

}
