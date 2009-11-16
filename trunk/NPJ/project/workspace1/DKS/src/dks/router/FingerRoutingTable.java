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
import java.util.HashMap;

import org.apache.log4j.Logger;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.ring.RingMaintenanceComponentInt;
import dks.utils.RingIntervals;

/**
 * The <code>RoutingTable</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FingerRoutingTable.java 608 2008-05-30 12:20:35Z joel $
 */
public class FingerRoutingTable implements GenericRoutingTableInterface {

	/*#%*/ private static Logger log = Logger.getLogger(FingerRoutingTable.class);

	private HashMap<Long, RoutingTableEntry> routingTable;

	private long intervals;

	private BigInteger N;

	private BigInteger myId;

	private RingMaintenanceComponentInt ringMaintainer;

	private Router router;

	/**
	 * Constructs an empty Routing Table for the d
	 * 
	 * @param ringMaintainer
	 */
	public FingerRoutingTable(BigInteger p,
			RingMaintenanceComponentInt ringMaintainer) {
		this.myId = p;

		this.ringMaintainer = ringMaintainer;

		routingTable = new HashMap<Long, RoutingTableEntry>();

		router = ringMaintainer.getRegistry().getRouterComponent();
		
		// Getting parameters
		DKSParameters dksParameters = ComponentRegistry.getDksParameters();
		int k = dksParameters.K;
		int l = dksParameters.L;
		N = dksParameters.N;

		// Number of Levels
		intervals = (k - 1) * l;

		BigInteger kLessOne = BigInteger.valueOf(k - 1);
		BigInteger one = BigInteger.valueOf(1);

		RoutingTableEntry previousEntry = null;

		// Generating entries
		for (long i = 1; i <= intervals; i++) {

			// Calculating f(i)
			BigInteger iLessOne = BigInteger.valueOf(i - 1);
			BigInteger divided = iLessOne.divide(kLessOne);
			BigInteger kPowDivided = BigInteger.valueOf(k).pow(
					divided.intValue());

			BigInteger f = p.add(
					((iLessOne).mod(kLessOne)).add(one).multiply(kPowDivided))
					.mod(N);
			// System.out.println("f(" + i + ")=" + f.toString());
			// Generating entry for f(i)
			RoutingTableEntry entry = new RoutingTableEntry(f, i, null);
			// Including entry in the RoutingTable
			routingTable.put(i, entry);

			if (i != 1) {
				// System.out.println("end of interval for f(" + (i - 1) + ")="
				// + f.subtract(BigInteger.ONE));
				BigInteger sub = f.subtract(BigInteger.ONE);
				if (sub.compareTo(BigInteger.ZERO) < 0) {
					sub = N.subtract(BigInteger.ONE);
				}
				previousEntry.setEndOfinterval(sub);
			}
			previousEntry = entry;

		}
		BigInteger sub = myId.subtract(BigInteger.ONE);
		if (sub.compareTo(BigInteger.ZERO) < 0) {
			sub = N.subtract(BigInteger.ONE);
		}
		previousEntry.setEndOfinterval(sub);

	}

	/**
	 * @param Id
	 *            The looked-up Id
	 * @return The DKSRef of the closest finger d preceding Id
	 */
	public DKSRef nextHop(BigInteger Id) {
		RoutingTableEntry currentEntry = null;
		DKSRef fingerPointer = null;
		DKSRef tempPointer = null;
		BigInteger fingerNodeId = null;

		synchronized (routingTable) {
			for (long i = intervals; i >= 1; i--) {

				currentEntry = routingTable.get(i);
				fingerPointer = currentEntry.getIntervalPointer();

				if (fingerPointer == null) {
					;
				} else {

					tempPointer = fingerPointer;

					fingerNodeId = fingerPointer.getId();

					if (RingIntervals.belongsTo(fingerNodeId, myId, Id, N,
							RingIntervals.Bounds.OPEN_OPEN)) {
						return fingerPointer;
					}
				}
			}
		}

		return null; //TODO check again
	}

	/**
	 * Returns the {@link RoutingTableEntry} responsible for the ID passed for
	 * updating purpose (the Entry which contains that ID)
	 * 
	 * @param Id
	 *            The ID
	 * @return The {@link RoutingTableEntry} responsible for that ID
	 */

	public synchronized RoutingTableEntry getContainingEntry(BigInteger Id) {
		RoutingTableEntry current = null;
		BigInteger currentId = null;
		RoutingTableEntry next = null;
		BigInteger nextId = null;

		synchronized (router) {

			for (long i = 1; i <= intervals; i++) {
				current = routingTable.get(i);
				if (i == (intervals))
					return current;
				currentId = current.getIntervalStartId();
				next = routingTable.get(i + 1);
				nextId = next.getIntervalStartId();
				if (RingIntervals.belongsTo(Id, currentId, nextId, N,
						RingIntervals.Bounds.CLOSED_OPEN)) {
					return current;
				}
			}
		}
		return null;
		//TEST/FIXME I'm not so sure about this one
	}

	// /**
	// * Updates the responsible d of a {@link RoutingTableEntry} only if needed
	// *
	// * @param ref
	// * The {@link DKSRef} of the d that could be the new responsible
	// * for the corresponding {@link RoutingTableEntry}
	// * @return true if the corresponding {@link RoutingTableEntry} has been
	// * updated, false otherwise
	// */
	// public boolean updateRoutingEntry(DKSRef ref) {
	// BigInteger refId = ref.getId();
	// RoutingTableEntry responsibleEntry = getContainingEntry(refId);
	//
	// if (responsibleEntry.getIntervalPointer() == null) {
	// responsibleEntry.setIntervalPointer(ref);
	// return true;
	// }
	// if (responsibleEntry.getIntervalPointer().equals(ref))
	// return false;
	//
	// BigInteger responsibleNodeId = responsibleEntry.getIntervalPointer()
	// .getId();
	//
	// BigInteger intervalStartId = responsibleEntry.getIntervalStartId();
	// if (RingIntervals.belongsTo(refId, intervalStartId, responsibleNodeId,
	// N, RingIntervals.Bounds.CLOSED_OPEN)) {
	// responsibleEntry.setIntervalPointer(ref);
	// return true;
	// } else
	// return false;
	// }

	/**
	 * Finds and, if equal to the d passed, removes the responsible of the entry
	 * associated
	 * 
	 * @param d
	 *            The d that left the ring
	 * 
	 */

	public boolean removePeerIfResponsible(DKSRef node) {
		BigInteger nodeId = node.getId();
		RoutingTableEntry responsibleEntry = getContainingEntry(nodeId);

		synchronized (router) {

			if (responsibleEntry.getIntervalPointer() != null
					&& responsibleEntry.getIntervalPointer().equals(node)) {
				/*#%*/ log.debug("Removing pointer of entry="
				/*#%*/ 		+ responsibleEntry.getIntervalStartId());
				responsibleEntry.setIntervalPointer(null);
				return true;
			} else
				return false;
		}
	}

	/**
	 * Checks if the desired id is between the d's predessor id and the d's id
	 * 
	 * @param i
	 *            The id to be checked
	 * @return true if the id is between, false otherwise
	 */

	public boolean terminate(BigInteger i) {
		DKSRef predecessor = ringMaintainer.getRingState().predecessor;
		if (RingIntervals.belongsTo(i, predecessor.getId(), myId, N,
				RingIntervals.Bounds.OPEN_CLOSED))
			return true;
		return false;
	}

	public String printVideoRoutingTable() {
		for (RoutingTableEntry entry : routingTable.values()) {
			/*#%*/ log.debug("f(" + entry.getIntervalNumber() + ")="
			/*#%*/ 		+ entry.getIntervalStartId() + " finger:"
			/*#%*/ 		+ entry.getIntervalPointer());
		}
		return null;
	}

	public String printRoutingTable() {
		StringBuffer representation = new StringBuffer();

		for (long i = 1; i <= intervals; i++) {
			RoutingTableEntry entry = routingTable.get(i);
			representation.append("f("
					+ i
					+ ")="
					+ entry.getIntervalStartId()
					+ ":"
					+ (entry.getIntervalPointer() != null ? entry
							.getIntervalPointer().getId() : null));
			if (i == intervals)
				break;
			representation.append(",");
		}
		return representation.toString();

	}

	public RoutingTableEntry getRoutingTableEntry(long i) {
		return routingTable.get(i);
	}

	public long getIntervalsNumber() {
		return intervals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.router.GenericRoutingTableInterface#getFofI(long)
	 */
	public BigInteger getStartingId(long i) {
		if(i > routingTable.size()) { return null; }
		return routingTable.get(i).getIntervalStartId();
	}

}
