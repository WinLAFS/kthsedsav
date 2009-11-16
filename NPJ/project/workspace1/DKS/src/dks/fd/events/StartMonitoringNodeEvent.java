/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd.events;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>StartMonitoringNodeEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: StartMonitoringNodeEvent.java 294 2006-05-05 17:14:14Z roberto $
 */
public class StartMonitoringNodeEvent extends Event {

	private DKSRef node;

	public StartMonitoringNodeEvent(DKSRef node) {
		this.node = node;
	}

	public DKSRef getNode() {
		return node;
	}

}
