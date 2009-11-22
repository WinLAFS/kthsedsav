/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.events;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.operations.events.OperationRequestEvent;

/**
 * The <code>ExternalGetRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: ExternalGetRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ExternalGetRequestEvent extends OperationRequestEvent {

	/**
	 * @param initiator
	 * @param operationId
	 * @param operationMessage
	 */
	public ExternalGetRequestEvent(DKSRef initiator, long operationId, Message operationMessage) {
		super(initiator, operationId, operationMessage);
		// TODO Auto-generated constructor stub
	}

}