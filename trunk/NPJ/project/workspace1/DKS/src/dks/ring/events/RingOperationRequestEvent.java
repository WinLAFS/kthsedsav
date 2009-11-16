/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.events;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.operations.events.OperationRequestEvent;

/**
 * The <code>RingOperationRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingOperationRequestEvent.java 250 2007-03-14 14:26:51Z Roberto $
 */
public class RingOperationRequestEvent extends OperationRequestEvent {

	/**
	 * Event issued when an RPC operation is needed. The RingMaintainer
	 * registers to the RPC operations in the OperationManager, so this event is
	 * issued by the latter when an RPC is requested.
	 * 
	 */
	public RingOperationRequestEvent(DKSRef initiator, long operationId,
			Message operationMessage) {
		super(initiator, operationId, operationMessage);
	}
}
