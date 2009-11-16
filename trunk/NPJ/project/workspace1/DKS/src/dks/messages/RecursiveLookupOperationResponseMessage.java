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
import java.util.EmptyStackException;
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
public class RecursiveLookupOperationResponseMessage extends
		LookupOperationResponseMessage {

	private static final long serialVersionUID = 5504838733367472121L;

	/**
	 * Stack of DKSReferences to store all the nodes on the path
	 */
	private Stack<DKSRef> path;

	/**
	 * Default Constructor for the Marshaler
	 */
	public RecursiveLookupOperationResponseMessage() {
		this.path = new Stack<DKSRef>();
	}

	/**
	 * Recursive Lookup Response constructor
	 * 
	 */

	public RecursiveLookupOperationResponseMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, DKSRef responsibleNode,
			Message operationReplyMessage, Stack<DKSRef> path) {
		super(lookupId, destinationId, strategy, reliable, initiator,
				responsibleNode, operationReplyMessage);

		this.path = path;

	}

	/**
	 * Pops the first reference from the top of the path carried by the Response
	 */
	public DKSRef pop() {
		try {
			return path.pop();
		} catch (EmptyStackException ex) {
			return null;
		}
	}
}
