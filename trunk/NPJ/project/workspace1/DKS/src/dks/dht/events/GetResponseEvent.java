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

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.router.events.LookupResultEvent;

/**
 * The <code>GetResponseEvent</code> class
 *
 * @author Joel
 * @version $Id: GetResponseEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class GetResponseEvent extends LookupResultEvent {

	/**
	 * @param id
	 * @param dksRef
	 * @param operationReplyMessage
	 */
	public GetResponseEvent(BigInteger id, DKSRef dksRef, Message operationReplyMessage) {
		super(id, dksRef, operationReplyMessage);
		// TODO Auto-generated constructor stub
	}

}
