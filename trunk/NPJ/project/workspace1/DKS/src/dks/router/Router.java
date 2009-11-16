/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.router;

import java.util.List;

import dks.addr.DKSRef;

/**
 * The <code>Router</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id:Router.java 107 2006-11-16 12:03:07Z cosmin $
 */
public interface Router {
	public static enum RouterType {
		FINGER_ROUTER,RING_ROUTER,PROXIMITY_ROUTER
	};
	
	public static enum LookupStrategy {
//		ITERATIVE,
		RECURSIVE,
		TRANSITIVE,
//		TWOWAY
	};
	
	public static Enum routerUsed=RouterType.FINGER_ROUTER;

	public GenericRoutingTableInterface getRoutingTable();

	public List<DKSRef> getRoutingNeighbors();

	public int getTraversingReliableRecursiveLookupsNumber();

	public int getPendingOperationsNumber();

	public boolean isTopoloMaintenanceRunning();

	public List<DKSRef> getBackList();

	public String getRoutingTableStringRepresentation();
	
	public void remNeighbor(DKSRef ref); //Added by Joel
	
}
