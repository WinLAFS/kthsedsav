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
 * The <code>UnreliableLookupRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: UnreliableLookupRequestEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class UnreliableLookupRequestEvent extends LookupRequestEvent {

	/**
	 * Constructs the event that starts the lookup process, it will carry along
	 * the Dks message containing the operation that needs to be executed on the
	 * responsible peer.
	 * 
	 * @param id
	 *            The destination id of the lookup
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * @param operationRequestMessage
	 *            The {@link Message} containing the operation
	 */
	public UnreliableLookupRequestEvent(BigInteger id, LookupStrategy strategy,
			Message operationRequestMessage) {
		super(id, strategy, operationRequestMessage);
	}

	/**
	 * Constructs the event that starts the lookup process, it will carry along
	 * the Dks message containing the operation that needs to be executed on the
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
	public UnreliableLookupRequestEvent(BigInteger id, LookupStrategy strategy,
			Message operationRequestMessage, Class eventToIssue) {
		super(id, strategy, operationRequestMessage, eventToIssue);
	}

}
