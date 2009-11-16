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
 * The <code>RingCheckPred</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingCheckPred.java 115 2006-11-19 12:55:29Z Roberto $
 */
public class RingCheckPred extends Event {

	/**
	 * Event issued when checking for the liveness of the predecessor (Tipically
	 * after a timer expires)
	 */
	public RingCheckPred() {

	}

}
