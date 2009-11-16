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
 * The <code>LookupOperationReplyMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TransitiveLookupOperationResponseMessage.java 171 2007-01-30
 *          17:35:32Z Roberto $
 */
public class TransitiveLookupOperationResponseMessage extends
		LookupOperationResponseMessage {

	private static final long serialVersionUID = -5653877187073195462L;
	

	public TransitiveLookupOperationResponseMessage() {
		super();
	}

	/**
	 * Constructor used to generate a lookup response
	 */

	public TransitiveLookupOperationResponseMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, DKSRef responsibleNode,
			Message operationReplyMessage) {
		super(lookupId, destinationId, strategy, reliable, initiator,
				responsibleNode, operationReplyMessage);

	}
	
	public TransitiveLookupOperationResponseMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, DKSRef responsibleNode) {
		super(lookupId, destinationId, strategy, reliable, initiator,
				responsibleNode);

	}

}
