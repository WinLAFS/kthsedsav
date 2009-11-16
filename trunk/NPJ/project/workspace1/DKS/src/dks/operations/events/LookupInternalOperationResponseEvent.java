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
 * The <code>LookupInternalOperationResponseEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LookupInternalOperationResponseEvent.java 294 2006-05-05
 *          17:14:14Z Roberto $
 */
public class LookupInternalOperationResponseEvent extends Event {

	protected DKSRef initiator;

	protected long operationId;

	protected Message operationReplyMessage;

	/**
	 * Event issued when a Lookup Operation is completed, the event is addressed
	 * to the router Component
	 * 
	 * @param The {@link OperationResponseEvent}
	 */
	public LookupInternalOperationResponseEvent(
			OperationResponseEvent operationResponseEvent) {
		this.initiator = operationResponseEvent.getRequestEvent().getSource();
		this.operationId = operationResponseEvent.getRequestEvent()
				.getOperationId();
		this.operationReplyMessage = operationResponseEvent
				.getOperationReplyMessage();
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
	 * @return Returns the operationReplyMessage.
	 */
	public Message getOperationReplyMessage() {
		return operationReplyMessage;
	}
	
	

}
