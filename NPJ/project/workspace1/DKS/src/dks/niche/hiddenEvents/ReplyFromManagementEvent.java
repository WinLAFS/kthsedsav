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

import java.io.Serializable;

import dks.bcast.events.PseudoReliableIntervalBroadcastAckEvent;
import dks.bcast.interfaces.PseudoReliableIntervalBroadcastAckInterface;

/**
 * The <code>ReplyFromManagementEvent</code> class
 *
 * @author Joel
 * @version $Id: ReplyFromManagementEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ReplyFromManagementEvent extends ManagementEvent implements Serializable  {
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7815086826945256437L;
	Object originalMessage;
	public ReplyFromManagementEvent(Object originalMessage, Serializable message) {
		super(message);
		this.originalMessage = originalMessage;
	}
	public Object getOriginalMessage() {
		return originalMessage;
	}
}
