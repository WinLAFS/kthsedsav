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

import dks.arch.Event;
import dks.messages.Message;

/**
 * The <code>OperationResponseEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationResponseEvent.java 250 2007-03-14 14:26:51Z Roberto $
 */
public class OperationResponseEvent extends Event {

	private OperationRequestEvent requestEvent;

	private Message operationReplyMessage;

	/**
	 * Event issued when an operation is completed
	 * 
	 * @param requestEvent
	 *            The request event previously received
	 * @param operationReplyMessage
	 *            The reply message
	 */
	public OperationResponseEvent(OperationRequestEvent requestEvent,
			Message operationReplyMessage) {
		this.requestEvent = requestEvent;
		this.operationReplyMessage = operationReplyMessage;

	}

	/**
	 * @return Returns the operationReplyMessage.
	 */
	public Message getOperationReplyMessage() {
		return operationReplyMessage;
	}

	/**
	 * @return Returns the requestEvent.
	 */
	public OperationRequestEvent getRequestEvent() {
		return requestEvent;
	}

}
