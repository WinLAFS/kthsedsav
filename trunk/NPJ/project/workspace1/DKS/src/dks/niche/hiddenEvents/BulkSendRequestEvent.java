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

import java.util.ArrayList;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>BulkSendRequestEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: BulkSendRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class BulkSendRequestEvent extends Event {

	
	public BulkSendRequestEvent(DKSRef initiator, ArrayList<DKSRef> dksRefs, ArrayList<Object> content, int[]positions) {
		
		//attachment = new BulkSendContent(dksRefs, content, positions);
	}

}
