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

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.router.FingerRoutingTable;

/**
 * The <code>RouterDiscoveredNodesEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RouterDiscoveredNodesEvent.java 294 2006-05-05 17:14:14Z
 *          Roberto $
 */
public class DiscoveredNodesEvent extends Event {

	private DKSRef[] discoveredNodes;

	/**
	 * Event Issued when the {@link DKSRingMaintenanceComponent} Discovers new
	 * nodes that could be useful for updating the {@link FingerRoutingTable}
	 */
	public DiscoveredNodesEvent(DKSRef[] discoveredNodes) {
		this.discoveredNodes = discoveredNodes;
	}

	/**
	 * @return Returns the discoveredNodes.
	 */
	public DKSRef[] getDiscoveredNodes() {
		return discoveredNodes;
	}

}
