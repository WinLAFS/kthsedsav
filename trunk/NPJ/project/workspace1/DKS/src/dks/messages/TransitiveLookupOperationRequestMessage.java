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
 * The <code>LookupOperationMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TransitiveLookupOperationRequestMessage.java 171 2007-01-30
 *          17:35:32Z Roberto $
 */
public class TransitiveLookupOperationRequestMessage extends
		LookupOperationRequestMessage {

	private static final long serialVersionUID = 2878124993486878324L;

	public TransitiveLookupOperationRequestMessage() {
		super();
	}

	public TransitiveLookupOperationRequestMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator, Message operationMsg) {
		super(lookupId, destinationId, strategy, reliable, initiator,
				operationMsg);
	}
	
	public TransitiveLookupOperationRequestMessage(long lookupId,
			BigInteger destinationId, LookupStrategy strategy,
			boolean reliable, DKSRef initiator) {
		super(lookupId, destinationId, strategy, reliable, initiator);
	}

}
