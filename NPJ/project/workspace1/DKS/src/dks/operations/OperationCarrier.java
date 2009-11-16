/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.operations;

import dks.addr.DKSRef;
import dks.messages.Message;

/**
 * The <code>OperationCarrier</code> class
 * 
 * Object used to carry the operationMessage information to the components involved in
 * the operationMessage execution and management. An {@link OperationCarrier} instance
 * consists of an identifier of the operationMessage and a Message with the Operation
 * request/response
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationCarrier.java 248 2007-03-13 12:59:57Z Roberto $
 */
public class OperationCarrier {

	private long operationId;

	private Message operationMessage;

	private DKSRef initiator;
	
	/**
	 * @param operationId
	 *            The OperationCarrier Id
	 * @param operationMessage
	 *            The {@link Message} carrying the operationMessage to perform
	 */
	public OperationCarrier(long operationId, Message operation) {
		super();
		this.operationId = operationId;
		this.operationMessage = operation;
	}

	/**
	 * @param operationId
	 *            The OperationCarrier Id
	 * @param operationMessage
	 *            The {@link Message} carrying the operationMessage to perform
	 * 
	 * @param initiator
	 *            The DKSRef of the initiator of the operationMessage
	 */
	public OperationCarrier(long operationId, Message operation, DKSRef initiator) {
		super();
		this.operationId = operationId;
		this.operationMessage = operation;
		this.initiator = initiator;
	}

	/**
	 * @return Returns the operationMessage.
	 */
	public Message getOperationMessage() {
		return operationMessage;
	}

	/**
	 * @return Returns the operationId.
	 */
	public long getOperationId() {
		return operationId;
	}

	/**
	 * @return Returns the initiator.
	 */
	public DKSRef getInitiator() {
		return initiator;
	}

	/**
	 * @param initiator
	 *            The initiator to set.
	 */
	public void setSource(DKSRef source) {
		this.initiator = source;
	}

	/**
	 * @param operationMessage The operationMessage to set.
	 */
	public void setOperationReplyMessage(Message operation) {
		this.operationMessage = operation;
	}

}
