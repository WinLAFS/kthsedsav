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
 * The <code>UnreliableLookupRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: UnreliableLookupRequestEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public abstract class LookupRequestEvent extends Event {

	private BigInteger id;

	private LookupStrategy strategy;

	private Message operationRequestMessage;

	private Class<Event> eventToIssue;

	// private LookupOperationType operation;

	/**
	 * Constructs the event that starts the lookup process, it will carry along
	 * the message containing the operation that needs to be executed on the
	 * responsible peer.
	 * 
	 * 
	 * @param id
	 *            The destination id of the lookup
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * @param operationRequestMessage
	 *            The {@link Message} containing the operation
	 */
	public LookupRequestEvent(BigInteger id, LookupStrategy strategy,
			Message operationRequestMessage) {
		super();
		this.id = id;
		this.strategy = strategy;
		this.operationRequestMessage = operationRequestMessage;
	}

	/**
	 * Constructs the event that starts the lookup process, it will carry along
	 * the message containing the operation that needs to be executed on the
	 * responsible peer. This constructor specifies also wich event has to be
	 * issued when the lookup operation is completed (Not applicable to Deliver
	 * operations). An {@link LookupResultEvent} instance will be attached to
	 * the specified event.
	 * 
	 * 
	 * @param id
	 *            The destination id of the lookup
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * @param operationRequestMessage
	 *            The {@link Message} containing the operation
	 * 
	 * @param eventToIssue
	 *            The classe of the event that must be issued
	 */

	public LookupRequestEvent(BigInteger id, LookupStrategy strategy,
			Message operationRequestMessage, Class eventToIssue) {
		super();
		this.id = id;
		this.strategy = strategy;
		this.operationRequestMessage = operationRequestMessage;
		this.eventToIssue = eventToIssue;
	}

	/**
	 * Constructs the event that starts the lookup process. Id this constructor
	 * is used, the lookup will only return the responsible of the looked up
	 * identifier. This constructor specifies also which event has to be issued
	 * when the lookup operation is completed .
	 * 
	 * 
	 * @param id
	 *            The destination id of the lookup
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * 
	 * @param eventToIssue
	 *            The classe of the event that must be issued
	 */

	public LookupRequestEvent(BigInteger id, LookupStrategy strategy,
			Class<Event> eventToIssue) {
		super();
		this.id = id;
		this.strategy = strategy;
		this.eventToIssue = eventToIssue;
	}

	/**
	 * @return Returns the eventToIssue.
	 */
	public Class<Event> getEventToIssue() {
		return eventToIssue;
	}

	public boolean hasEventToIssue() {
		return (eventToIssue == null ? false : true);
	}

	/**
	 * @return Returns the id.
	 */
	public BigInteger getId() {
		return id;
	}

	/**
	 * @return Returns the strategy.
	 */
	public LookupStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @return Returns the operationRequestMessage.
	 */
	public Message getOperationRequestMessage() {
		return operationRequestMessage;
	}

	public boolean hasOperation() {
		return (operationRequestMessage == null ? false : true);
	}

}
