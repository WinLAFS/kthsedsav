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
 * The <code>SuspitionTimeoutEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SendPingEvent.java 496 2007-12-20 15:39:02Z roberto $
 */
public class SendPingEvent extends Event {

	/**
	 * Event Issued when the failure detector starts to become supicious about a
	 * peer
	 * 
	 * In the attachment will be set the {@link ConnectionState} representing
	 * the connection in which the Timer A has expired
	 */
	public SendPingEvent() {
	}

}
