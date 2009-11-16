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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Stack;

import dks.addr.DKSRef;
import dks.router.Router.LookupStrategy;

/**
 * The <code>RecursiveLookupOperationRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RecursiveLookupOperationRequestMessage.java 294 2006-05-05
 *          17:14:14Z Roberto $
 */
public class RecursiveLookupOperationRequestMessage extends
		LookupOperationRequestMessage implements Serializable{
	
	private static final long serialVersionUID = -7507746883906759663L;

	/**
	 * Stack of DKSReferences to store all the nodes on the path
	 */
	private Stack<DKSRef> path;

	/**
	 * Default Constructor for the Marshaler
	 */
	public RecursiveLookupOperationRequestMessage() {
		path = new Stack<DKSRef>();
	}

	/**
	 * Recursive Lookup constructor
	 * 
	 * @param lookupId
	 *            The lookup identifier
	 * @param destinationId
	 *            The destination id of the lookup
	 * @param strategy
	 *            The strategy to use
	 * 
	 * @param reliable
	 *            If the lookup is a reliable one or not *
	 * @param initiator
	 *            The initiator of the lookup process
	 * @param operationMsg
	 *            The Message carrying the operation
	 */
	public RecursiveLookupOperationRequestMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, Message operationMsg) {
		super(lookupId, destinationId, strategy, reliable, initiator,
				operationMsg);

		path = new Stack<DKSRef>();

	}

	/**
	 * Pushes the passed reference into the top of the path carried by the
	 * Request
	 * 
	 * @param dksref
	 *            The {@link DKSRef}
	 */
	public void push(DKSRef dksref) {
		path.push(dksref);
	}

	/**
	 * @return Returns the path.
	 */
	public Stack<DKSRef> getPath() {
		return path;
	}

	// /**
	// * Gets the initiator of the Lookup request (the last reference in the
	// * stack)
	// *
	// * @return The {@link DKSRef} of the initiator
	// */
	// public DKSRef getInitiator() {
	// return path.lastElement();
	// }

	// /**
	// *
	// * Th object given can be either a {@link LookupOperationRequestMessage}
	// or
	// * a {@link LookupOperationResponseMessage}, in both cases the methods
	// * returns true if the lookupId is equal to the one of the current
	// instance
	// *
	// *
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object obj) {
	// Class otherClass = null;
	// if (this == obj) {
	// return true;
	// }
	// if (obj == null) {
	// return false;
	// }
	// if (getClass() != (otherClass = obj.getClass())) {
	// if (otherClass.equals(LookupOperationResponseMessage.class)) {
	// final LookupOperationResponseMessage other =
	// (LookupOperationResponseMessage) obj;
	// if (this.lookupId == 0) {
	// if (other.lookupId != 0) {
	// return false;
	// }
	// } else if (this.lookupId != other.lookupId) {
	// return false;
	// }
	// if (this.initiator == null) {
	// if (other.initiator != null) {
	// return false;
	// }
	// } else if (!this.initiator.equals(other.initiator)) {
	// return false;
	// }
	// return true;
	// } else
	// return false;
	// }
	// final LookupOperationRequestMessage other =
	// (LookupOperationRequestMessage) obj;
	// if (this.lookupId == 0) {
	// if (other.lookupId != 0) {
	// return false;
	// }
	// } else if (this.lookupId != other.lookupId) {
	// return false;
	// }
	// if (this.initiator == null) {
	// if (other.initiator != null) {
	// return false;
	// }
	// } else if (!this.initiator.equals(other.initiator)) {
	// return false;
	// }
	// return true;
	// }
}
