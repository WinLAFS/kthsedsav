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

import dks.arch.Event;

/**
 * The <code>HeartBeatExpiredEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: PingTimedOutEvent.java 496 2007-12-20 15:39:02Z roberto $
 */
public class PingTimedOutEvent extends Event {

	/**
	 * Issued when an HEARTBEAT message expires
	 * 
	 * In the attachment will be set the {@link ConnectionState} representing
	 * the connection in which the Timer B has expired
	 */
	public PingTimedOutEvent() {
	}

}
