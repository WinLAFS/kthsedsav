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
 * The <code>RingRouter</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id:RingRouter.java 107 2006-11-16 12:03:07Z cosmin $
 */
public class RingRouter implements Router {

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getRoutingNeighbors()
	 */
	public List<DKSRef> getRoutingNeighbors() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getRoutingTable()
	 */
	public GenericRoutingTableInterface getRoutingTable() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getBackList()
	 */
	public List<DKSRef> getBackList() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getPendingOperationsNumber()
	 */
	public int getPendingOperationsNumber() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getPendingReliableLookupsNumber()
	 */
	public int getPendingReliableLookupsNumber() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getPendingUnreliableLookupsNumber()
	 */
	public int getPendingUnreliableLookupsNumber() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getRoutingTableStringRepresentation()
	 */
	public String getRoutingTableStringRepresentation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#getTraversingReliableRecursiveLookupsNumber()
	 */
	public int getTraversingReliableRecursiveLookupsNumber() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.Router#isTopoloMaintenanceRunning()
	 */
	public boolean isTopoloMaintenanceRunning() {

		return false;
	}

	/* (non-Javadoc)
	 * @see dks.router.Router#remNeighbor(dks.addr.DKSRef)
	 */
	public void remNeighbor(DKSRef ref) {
		// TODO Auto-generated method stub
		
	}

}
