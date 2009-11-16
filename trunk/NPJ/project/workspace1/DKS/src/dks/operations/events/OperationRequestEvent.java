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
 * The <code>OperationRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationRequestEvent.java 250 2007-03-14 14:26:51Z Roberto $
 */
public class OperationRequestEvent extends Event {

	protected DKSRef initiator;

	protected long operationId;

	protected Message operationMessage;

	/**
	 * Event issued when an operation is needed, the event can be extended by
	 * any other event implementing the same methods
	 * 
	 * @param lookupId
	 *            The lookupId
	 * @param initiator
	 *            The initiator of the operation
	 * @param operationId
	 *            The system-unique operation identifier
	 * @param operationMessage
	 *            The operation message
	 */
	public OperationRequestEvent(DKSRef initiator,
			long operationId, Message operationMessage) {
		super();
		this.initiator = initiator;
		this.operationId = operationId;
		this.operationMessage = operationMessage;
	}

	/**
	 * @return Returns the operationMessage.
	 */
	public Message getOperationMessage() {
		return operationMessage;
	}

	/**
	 * @param operationMessage The operationMessage to set.
	 */
	public void setOperationReplyMessage(Message operationMessage) {
		this.operationMessage = operationMessage;
	}

	/**
	 * @return Returns the initiator.
	 */
	public DKSRef getSource() {
		return initiator;
	}

	/**
	 * @return Returns the operationId.
	 */
	public long getOperationId() {
		return operationId;
	}

	

}
