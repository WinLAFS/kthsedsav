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

/**
 * The <code>RemoveNodeEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RemoveNodeEvent.java 496 2007-12-20 15:39:02Z roberto $
 */
public class RemoveNodeEvent extends Event {

	private DKSRef nodeToRemove;

	/**
	 * Event Issued when the {@link DKSRingMaintenanceComponent} discovers that a
	 * d has left the Ring
	 * 
	 * @param removedNodes
	 *            The removed nodes
	 */
	public RemoveNodeEvent(DKSRef discoveredNode) {
		super();
		this.nodeToRemove = discoveredNode;
	}

	/**
	 * @return Returns the removedNodes.
	 */
	public DKSRef getNodeToRemove() {
		return nodeToRemove;
	}

}
