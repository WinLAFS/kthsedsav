/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.messages;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.router.Router.LookupStrategy;

/**
 * The <code>RoutedMessageReply</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LookupOperationResponseMessage.java 183 2007-02-07 15:29:31Z
 *          Roberto $
 */
public class LookupOperationResponseMessage extends Message {

	private static final long serialVersionUID = 9078837566052787071L;

	protected long lookupId;

	protected BigInteger id;

	private LookupStrategy strategy;

	protected DKSRef responsibleNode;

	protected Message operationReplyMessage;

	protected DKSRef initiator;

	private boolean reliable;

	public LookupOperationResponseMessage() {
	}

	/**
	 * Lookup response constructor
	 * 
	 * @param lookupId
	 *            The lookupId of the request previously received
	 * @param id
	 *            The looked up id
	 * @param reliable
	 *            true if it's a response to a reliable lookup,false otherwise
	 * @param responsibleNode
	 *            The responsible node for the looked up id
	 * @param operationReplyMessage
	 *            The operation reply message
	 */
	public LookupOperationResponseMessage(long lookupId, BigInteger id,
			LookupStrategy strategy, boolean reliable, DKSRef initiator,
			DKSRef responsibleNode, Message operationReplyMessage) {
		this.lookupId = lookupId;
		this.id = id;
		this.strategy = strategy;
		this.reliable = reliable;
		this.initiator = initiator;
		this.responsibleNode = responsibleNode;
		this.operationReplyMessage = operationReplyMessage;
	}

	public LookupOperationResponseMessage(long lookupId, BigInteger id,
			LookupStrategy strategy, boolean reliable, DKSRef initiator,
			DKSRef responsibleNode) {
		this.lookupId = lookupId;
		this.id = id;
		this.strategy = strategy;
		this.reliable = reliable;
		this.initiator = initiator;
		this.responsibleNode = responsibleNode;
	}

	/**
	 * @return Returns the lookupId.
	 */
	public long getLookupId() {
		return lookupId;
	}

	/**
	 * @return Returns the responsibleNode.
	 */
	public DKSRef getResponsibleNode() {
		return responsibleNode;
	}

	/**
	 * @return Returns the operationReplyMessage.
	 */
	public Message getOperationResultMessage() {
		return operationReplyMessage;
	}

	/**
	 * @return Returns the id.
	 */
	public BigInteger getId() {
		return id;
	}

	/**
	 * @return Returns the initiator.
	 */
	public DKSRef getInitiator() {
		return initiator;
	}

	/**
	 * @return Returns the strategy.
	 */
	public LookupStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @return Returns the reliable.
	 */
	public boolean isReliable() {
		return reliable;
	}

	public boolean hasOperation() {

		return (operationReplyMessage == null ? false : true);

	}

}
