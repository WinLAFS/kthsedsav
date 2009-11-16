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

import java.math.BigInteger;

import dks.addr.DKSRef;

/**
 * The <code>GeneralRoutingTable</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: GenericRoutingTableInterface.java 177 2007-02-01 14:33:48Z
 *          Roberto $
 */
public interface GenericRoutingTableInterface {

	// /**
	// * Updates the responsible d of the corresponding {@link
	// RoutingTableEntry}
	// * i, only if dID is closer to f(i) than the previous pointer ID
	// *
	// * @param ref
	// * The {@link DKSRef} of the d that could be the new responsible
	// * for the corresponding {@link RoutingTableEntry}
	// * @return true if the corresponding {@link RoutingTableEntry} has been
	// * updated, false otherwise
	// */
	// public boolean updateRoutingEntry(DKSRef ref);

	/**
	 * Finds and, if equal to the ref passed, removes the responsible of the
	 * entry associated
	 * 
	 * @param ref
	 *            The {@link DKSRef} of the node that left the ring
	 * @return true if the pointer has been removed, false otherwise
	 */
	public boolean removePeerIfResponsible(DKSRef ref);

	/**
	 * Gets the {@link RoutingTableEntry} instance which range covers the id
	 * passed
	 * 
	 * @param id
	 *            The id of the to check
	 * @return The right {@link RoutingTableEntry}
	 */
	public RoutingTableEntry getContainingEntry(BigInteger id);

	/**
	 * Finds and returns the best finger to send the Lookup
	 * 
	 * @param Id
	 *            The looked-up Id
	 * @return The DKSRef of the closest finger d preceding Id
	 */
	public DKSRef nextHop(BigInteger id);

	/**
	 * Returns the String representation of the routing table
	 * 
	 */
	public String printRoutingTable();

	/**
	 * Gets the {@link RoutingTableEntry} associated with the interval number
	 * passed
	 * 
	 * @param i
	 *            The interval number
	 * @return The corresponding {@link RoutingTableEntry}
	 */
	public RoutingTableEntry getRoutingTableEntry(long i);

	/**
	 * Gets the starting id "f(i)" of the interval corresponding to the interval
	 * number passed
	 * 
	 * @param i
	 *            The interval number
	 * @return The startting identifier
	 */
	public BigInteger getStartingId(long i);

	/**
	 * Gets the number of intervals in the routing table (k-1)*logk (N)
	 * 
	 */
	public long getIntervalsNumber();
}
