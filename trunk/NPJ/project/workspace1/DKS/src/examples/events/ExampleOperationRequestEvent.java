/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples.events;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.operations.events.OperationRequestEvent;

/**
 * The <code>ExampleOperationRequestEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ExampleOperationRequestEvent.java 231 2007-03-08 14:34:49Z
 *          Roberto $
 */
public class ExampleOperationRequestEvent extends OperationRequestEvent {

	public ExampleOperationRequestEvent(DKSRef initiator,long operationId,
			Message operationMessage) {
		super(initiator, operationId, operationMessage);
	}

}
