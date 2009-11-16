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
import java.util.LinkedList;
import java.util.List;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.utils.RingIntervals;

/**
 * The <code>RoutingTableEntry</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RoutingTableEntry.java 580 2008-03-20 14:22:48Z ahmad $
 */
public class RoutingTableEntry {

	private BigInteger intervalStartId;

	private BigInteger intervalEndId;

	private DKSRef intervalPointer;

	private long intervalNumber;

	private List<DKSRef> backupPointers;

	/**
	 * Constructs a RoutingTableEntry with the value of the first Id of the
	 * interval and the d responsible for it
	 * 
	 * @param intervalNumber
	 */
	public RoutingTableEntry(BigInteger intervalStartId, long intervalNumber,
			DKSRef intervalPointer) {
		this.intervalPointer = intervalPointer;
		this.intervalStartId = intervalStartId;
		this.intervalNumber = intervalNumber;
		this.backupPointers = new LinkedList<DKSRef>();
	}

	/**
	 * @return Returns the intervalPointer.
	 */
	public DKSRef getIntervalPointer() {
		return intervalPointer;
	}

	/**
	 * @return Returns the intervalStartId.
	 */
	public BigInteger getIntervalStartId() {
		return intervalStartId;
	}

	public boolean hasPointer() {
		return (intervalPointer == null ? false : true);
	}

	/**
	 * @return Returns the intervalNumber.
	 */
	public long getIntervalNumber() {
		return intervalNumber;
	}

	/**
	 * @param intervalPointer
	 *            The intervalPointer to set.
	 */
	public void setIntervalPointer(DKSRef intervalPointer) {
		this.intervalPointer = intervalPointer;
	}

	public boolean hasFinger() {
		return (intervalPointer == null ? false : true);
	}

	/**
	 * @return Returns the backupPointers.
	 */
	public List<DKSRef> getBackupPointers() {
		return backupPointers;
	}

	/**
	 * @param backupPointers
	 *            The backupPointers to set.
	 */
	public void setBackupPointers(List<DKSRef> backupPointers) {
		this.backupPointers = backupPointers;
	}

	/**
	 * Checks if the id passed is less than the id of the current pointer
	 * 
	 * @param id
	 *            The id to check
	 * @return true if it's less,false therwise
	 */
	public boolean isAbetterResponsibleId(BigInteger id) {
		if (this.intervalPointer == null) {
			return true;
		} else {
			return (RingIntervals.belongsTo(id, intervalStartId,
					intervalPointer.getId(), ComponentRegistry
							.getDksParameters().N,
					RingIntervals.Bounds.CLOSED_OPEN));
		}
	}

	/**
	 * Checks if the id passed is in the range covered by the routing entry
	 * 
	 * @param id
	 * @return
	 */
	public boolean isInRange(BigInteger id) {
		
		return RingIntervals.belongsTo(id, intervalStartId, intervalEndId, ComponentRegistry.getDksParameters().N, RingIntervals.Bounds.CLOSED_CLOSED);

	}

	/**
	 * Sets the end of the last identifier of the range covered by this entry
	 * 
	 * @param integer
	 */
	public void setEndOfinterval(BigInteger integer) {
		this.intervalEndId = integer;
	}

}
