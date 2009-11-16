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
 * The <code>RoutedMesage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LookupOperationRequestMessage.java 171 2007-01-30 17:35:32Z
 *          Roberto $
 */
public class LookupOperationRequestMessage extends Message {

	private static final long serialVersionUID = -2975635985251912557L;

	protected LookupStrategy strategy;

	protected BigInteger destinationId;

	protected long lookupId;

	protected Message operationMsg = null;

	protected DKSRef initiator;

	private long lookupTimerId;

	private boolean reliable;

	/**
	 * Constructor to be used to generate lookup requests
	 * 
	 * @param lookupId
	 *            The lookupId
	 * 
	 * @param destinationId
	 *            The destination id of the lookup
	 * @param strategy
	 *            The strategy to use
	 * @param initiator
	 *            The initiator of the Lookup request
	 * @param operationMsg
	 *            The Message carrying the operation
	 */

	public LookupOperationRequestMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, Message operationMsg) {
		this.lookupId = lookupId;
		this.destinationId = destinationId;
		this.strategy = strategy;
		this.reliable = reliable;
		this.initiator = initiator;
		this.operationMsg = operationMsg;
	}

	/**
	 * Default constructor for the marshaler instantiantion
	 */
	public LookupOperationRequestMessage() {
	}

	public LookupOperationRequestMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator) {
		this.lookupId = lookupId;
		this.destinationId = destinationId;
		this.strategy = strategy;
		this.reliable = reliable;
		this.initiator = initiator;
	}

	/**
	 * @return Returns the strategy.
	 */
	public LookupStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @return Returns the destinationId.
	 */
	public BigInteger getDestinationId() {
		return destinationId;
	}

	/**
	 * @return Returns the lookupId.
	 */
	public long getLookupId() {
		return lookupId;
	}

	/**
	 * @return Returns the operationMsg.
	 */
	public Message getOperationMsg() {
		return operationMsg;
	}

	/**
	 * @return Returns the initiator.
	 */
	public DKSRef getInitiator() {
		return initiator;
	}

	/**
	 * Sets a new lookuId to the request
	 * 
	 * @param newLookupId
	 */
	public void setNewLookupId(long newLookupId) {
		this.lookupId = newLookupId;
	}

	/**
	 * @return Returns the lookupTimerId.
	 */
	public long getLookupTimerId() {
		return lookupTimerId;
	}

	/**
	 * @param lookupTimerId
	 *            The lookupTimerId to set.
	 */
	public void setLookupTimerId(long lookupTimerId) {
		this.lookupTimerId = lookupTimerId;
	}

	/**
	 * @return Returns the reliable.
	 */
	public boolean isReliable() {
		return reliable;
	}

	public boolean hasOperation() {
		return (operationMsg == null ? false : true);
	}

}
