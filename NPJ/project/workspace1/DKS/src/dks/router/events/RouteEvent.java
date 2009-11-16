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

import dks.arch.Event;
import dks.messages.Message;
import dks.router.Router.LookupStrategy;

/**
 * The <code>RouterRouteEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RouteEvent.java 211 2007-02-20 14:41:15Z Roberto $
 */
public class RouteEvent extends Event {

	private LookupStrategy strategy;

	private Message message;

	private BigInteger destinationId;

	/**
	 * 
	 * Event Issued when a message needs to be routed to the d responsible
	 * for the Id specified
	 * 
	 * @param strategy
	 *            The {@link Strategy} that must be used to route the message
	 * @param message
	 *            The {@link Message} to send
	 * @param destinationId
	 *            The Id of the destination
	 */
	public RouteEvent(BigInteger destinationId, LookupStrategy strategy,
			Message message) {
		super();
		this.strategy = strategy;
		this.message = message;
		this.destinationId = destinationId;
	}

	/**
	 * @return Returns the destinationId.
	 */
	public BigInteger getDestinationId() {
		return destinationId;
	}

	/**
	 * @return Returns the message.
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @return Returns the strategy.
	 */
	public Enum getStrategy() {
		return strategy;
	}

}
