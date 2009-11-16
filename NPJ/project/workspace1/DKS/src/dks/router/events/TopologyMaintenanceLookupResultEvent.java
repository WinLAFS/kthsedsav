/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.router.events;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;

/**
 * The <code>TopologyMaintenanceLookupResultEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TopologyMaintenanceLookupResultEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class TopologyMaintenanceLookupResultEvent extends LookupResultEvent {

	/**
	 * Event issued when a topology maintenance lookup result must be passed to
	 * the router
	 * @param id
	 * @param responsible
	 * @param operationMessage
	 */
	public TopologyMaintenanceLookupResultEvent(BigInteger id, DKSRef responsible, Message operationMessage) {
		super(id, responsible, operationMessage);
	}


}
