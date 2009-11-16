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

/**
 * The <code>TopologyMaintenanceTimerExpiredEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TopologyMaintenanceTimerExpiredEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class TopologyMaintenanceTimerExpiredEvent extends Event {

	/**
	 * Event issued when the timer for updating the routing table entries, which
	 * pointer is null, is expired
	 */
	public TopologyMaintenanceTimerExpiredEvent() {
	}

}
