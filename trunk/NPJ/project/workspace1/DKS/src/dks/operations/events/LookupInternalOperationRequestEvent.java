/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.operations.events;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;

/**
 * The <code>LookupInternalOperationRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LookupInternalOperationRequestEvent.java 213 2007-02-26
 *          18:09:40Z Roberto $
 */
public class LookupInternalOperationRequestEvent extends Event {

	private final long operationId;

	private final DKSRef initiator;

	private final Message operationMessage;

	/**
	 * Event issued when an internal operation is requested from a lookup
	 * 
	 * @param operationId
	 * @param initiator
	 * @param operationMessage
	 */
	public LookupInternalOperationRequestEvent(long operationId,
			DKSRef initiator, Message operationMessage) {
		this.operationId = operationId;
		this.initiator = initiator;
		this.operationMessage = operationMessage;

	}
	
	/**
	 * @return Returns the initiator.
	 */
	public DKSRef getInitiator() {
		return initiator;
	}

	/**
	 * @return Returns the operationId.
	 */
	public long getOperationId() {
		return operationId;
	}

	/**
	 * @return Returns the operationMessage.
	 */
	public Message getOperationMessage() {
		return operationMessage;
	}

}
