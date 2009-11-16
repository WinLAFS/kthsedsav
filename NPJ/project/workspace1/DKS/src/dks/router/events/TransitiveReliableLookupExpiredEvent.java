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
 * The <code>TransitiveReliableLookupExpiredEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TransitiveReliableLookupExpiredEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class TransitiveReliableLookupExpiredEvent extends Event {

	/**
	 * Event issued when a reliable lookup request has expired
	 */
	public TransitiveReliableLookupExpiredEvent() {
	}

}
