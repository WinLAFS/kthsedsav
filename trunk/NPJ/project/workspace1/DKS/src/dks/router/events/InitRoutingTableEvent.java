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
 * The <code>InitRoutingTableEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: InitRoutingTableEvent.java 219 2007-03-01 16:23:04Z Roberto $
 */
public class InitRoutingTableEvent extends Event {

	/**
	 * Event issued to start the Routing table initialization process
	 */
	public InitRoutingTableEvent() {
	}

}
