/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.router.events.LookupResultEvent;

/**
 * The <code>SendToIdAckEvent</code> class
 *
 * @author Joel
 * @version $Id: SendToIdAckEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendToIdAckEvent extends LookupResultEvent {

	/**
	 * @param id
	 * @param responsible
	 * @param operationMessage
	 */
	public SendToIdAckEvent(BigInteger id, DKSRef responsible, Message operationMessage) {
		super(id, responsible, operationMessage);
		// TODO Auto-generated constructor stub
	}

}
