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
 * The <code>MustLeaveEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MustLeaveEvent.java 226 2007-03-07 19:07:22Z Roberto $
 */
public class MustLeaveEvent extends Event {

	/**
	 * Event issued when the Leave process starts to notify all the component to
	 * finish all the pending operations
	 */
	public MustLeaveEvent() {
	}

}
