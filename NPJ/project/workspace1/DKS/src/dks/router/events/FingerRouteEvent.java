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

import java.math.BigInteger;

import dks.messages.Message;
import dks.router.Router.LookupStrategy;

/**
 * The <code>RouterFingerRouteEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FingerRouteEvent.java 496 2007-12-20 15:39:02Z roberto $
 */
public class FingerRouteEvent extends RouteEvent {

	/**
	 * Event issued when a {@link Message} needs to be routed with the
	 * {@link FingerRouterComponent}
	 * 
	 * @param strategy
	 *            The {@link Strategy} that must be used to route the message
	 * @param message
	 *            The {@link Message} to send
	 * @param destinationId
	 *            The Id of the destination
	 */
	public FingerRouteEvent(LookupStrategy strategy, Message message,
			BigInteger destinationId) {
		super(destinationId, strategy, message);
	}

}
