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

import dks.arch.Event;
import dks.messages.Message;

/**
 * The <code>DeliverToNodeEvent</code> class
 *
 * @author Joel
 * @version $Id: DeliverToNodeEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeliverToNodeEvent extends ManagementEvent {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -8240451243023224795L;
	Object message;
	public DeliverToNodeEvent(Serializable message) {
		//this.message = message;
		super(message);
	}
//	public Message getMessage() {
//		return message;
//	}
}

