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

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.router.events.LookupResultEvent;

/**
 * The <code>ExampleOperationResponseEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ExampleOperationResponseEvent.java 231 2007-03-08 14:34:49Z
 *          Roberto $
 */
public class ExampleOperationResponseEvent extends LookupResultEvent {

	/**
	 * @param id
	 * @param responsible
	 * @param operationMessage
	 */
	public ExampleOperationResponseEvent(BigInteger id, DKSRef responsible, Message operationMessage) {
		super(id, responsible, operationMessage);
	}



}
