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

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;

/**
 * The <code>LookupResultEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LookupResultEvent.java 250 2007-03-14 14:26:51Z Roberto $
 */
public class LookupResultEvent extends Event {

	private BigInteger id;

	private DKSRef responsible;

	private Message operationMessage;

	/**
	 * Event issued when a Lookup operation is completed
	 * 
	 * @param id
	 *            The looked up id
	 * @param responsible
	 *            The responsible for the looked up id
	 * @param operation
	 *            The operation result message
	 */
	public LookupResultEvent(BigInteger id, DKSRef responsible,
			Message operationMessage) {
		super();
		this.id = id;
		this.responsible = responsible;
		this.operationMessage = operationMessage;

	}

	/**
	 * @return Returns the id.
	 */
	public BigInteger getLookedUpId() {
		return id;
	}

	/**
	 * @return Returns the responsible.
	 */
	public DKSRef getResponsible() {
		return responsible;
	}

	/**
	 * @return Returns the operation.
	 */
	// public LookupOperationType getOperation() {
	// return operation;
	// }
	/**
	 * @return Returns the operationMessage.
	 */
	public Message getOperationMessage() {
		return operationMessage;
	}

}
