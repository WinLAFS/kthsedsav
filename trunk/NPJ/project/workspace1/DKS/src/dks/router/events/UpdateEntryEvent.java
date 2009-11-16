/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.router.events;

import dks.arch.Event;
import dks.router.RoutingTableEntry;

/**
 * The <code>UpdateEntryEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: UpdateEntryEvent.java 222 2007-03-05 18:28:55Z Roberto $
 */
public class UpdateEntryEvent extends Event {

	private long i;
	
	public UpdateEntryEvent() {
	}
	/**
	 * Event issued when the pointer of a {@link RoutingTableEntry} must be
	 * updated due to leave/failure of the responsible node
	 */
	public UpdateEntryEvent(long i) {
		this.i = i;
	}

	/**
	 * @return Returns the i.
	 */
	public long getI() {
		return i;
	}

}
