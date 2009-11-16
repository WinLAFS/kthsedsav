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
 * The <code>RingStabilizeEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingStabilizeEvent.java 115 2006-11-19 12:55:29Z Roberto $
 */
public class RingStabilizeEvent extends Event {

	/**
	 * Event issued when a stabilization of the ring is needed (Tipically after
	 * a timer expires)
	 */
	public RingStabilizeEvent() {

	}

}
