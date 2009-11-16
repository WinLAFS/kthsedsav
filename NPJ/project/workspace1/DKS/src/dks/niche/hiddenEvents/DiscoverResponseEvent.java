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

import dks.messages.Message;
import dks.operations.events.OperationRequestEvent;
import dks.operations.events.OperationResponseEvent;

/**
 * The <code>DiscoverResponseEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DiscoverResponseEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DiscoverResponseEvent extends OperationResponseEvent {

	/**
	 * @param requestEvent
	 * @param operationReplyMessage
	 */
	public DiscoverResponseEvent(OperationRequestEvent requestEvent, Message operationReplyMessage) {
		super(requestEvent, operationReplyMessage);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * 
	 */
	

}
