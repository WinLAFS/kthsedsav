/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import dks.addr.DKSRef;
import dks.utils.RingIntervals;
import dks.utils.RingSuccessorListOrder;

/**
 * The <code>SuccessorList</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SuccessorList.java 627 2008-07-11 23:17:56Z joel $
 */
public class SuccessorList {

	/*#%*/ private static Logger log = Logger.getLogger(SuccessorList.class);

	private List<DKSRef> succlist;

	// private ConcurrentHashMap<DKSRef, String> sharedSucclist;

	private RingState ringState;

	private RingMaintenanceComponentInt ringMaintainer;

	private DKSRef myDKSRef;

	/**
	 * Limited list of succcessors
	 * 
	 * @param ringState
	 * @param ringMaintainer
	 * @param nid
	 */
	public SuccessorList(RingState ringState, DKSRef n,
			RingMaintenanceComponentInt ringMaintainer) {
		succlist = new LinkedList<DKSRef>();
		// sharedSucclist = new ConcurrentHashMap<DKSRef, String>();
		this.ringState = ringState;
		this.ringMaintainer = ringMaintainer;
		this.myDKSRef = n;
	}

	
	public void addNode(DKSRef node) {
		if (node != null && !succlist.contains(node) && !node.equals(myDKSRef)) {
			succlist.add(0, node); //Joel changed, July
			//succlist.add(node);
			// ringMaintainer.issueConnPermanent(d);
			// log.debug("Connection with " + d + " has to be permanent");
		}

		// truncate();
	}

	public void addIfSmallNode(DKSRef node) {
		if(succlist.size() < ringMaintainer.getDksParameters().L) {
			addNode(node);
			truncate();
		}
	}
	/**
	 * Truncate the list of successors to a SIZE_OF_SUCCESSORS_LIST number
	 */

	public void truncate() {
		//Collections.sort(succlist); //is inside next command as well
		
		//RingSuccessorListOrder.order(succlist, myDKSRef);
		
		int size = succlist.size();
		if (size == ringMaintainer.getDksParameters().L + 1) {
			/*
			 * removing the last peer from the list
			 */
			DKSRef removedPeer = succlist.remove(size - 1);
			// Putting the connection with the removed peer temporary if the
			// d is not my predecessor
			if (ringState.predecessor != null
					&& !ringState.predecessor.equals(removedPeer)
					&& !succlist.contains(removedPeer)) {
				ringMaintainer.remNeighbor(removedPeer);
				/*#%*/ log.debug("Connection with " + removedPeer
				/*#%*/ 		+ " has to be temporary");
			}
			
			// for (Iterator iter = succlist.iterator(); iter.hasNext()
			// && ((size - i) > ringMaintainer.getDksParameters().L);) {
			// DKSRef removedPeer = succlist.remove(size - i - 1);
			// // Putting the connection with the removed peer temporary if the
			// // d is not my predecessor
			// if (ringState.predecessor != null
			// && !ringState.predecessor.equals(removedPeer)
			// ) {
			// ringMaintainer.remNeighbor(removedPeer);
			// log.debug("Connection with " + removedPeer
			// + " has to be temporary");
			// }
			// i++;
			// }
		} else if (size > ringMaintainer.getDksParameters().L + 1) {
			/*#%*/ log.debug("truncate is not happy");
			System.err.println("truncate is not happy");
		}
		// RingSuccessorListOrder.order(succlist, myDKSRef);
		// Set<DKSRef> permanentConnectionsSet = sharedSucclist.keySet();
		// sharedSucclist.clear();
		// for (DKSRef node : succlist) {
		// log.debug(node);
		// sharedSucclist.put(node, "");
		// // if (!permanentConnectionsSet.contains(d))
		// ringMaintainer.addNeighbor(node);
		// }
	}

	public List<DKSRef> getSuccessorsList() {
		return succlist;
	}

	// public List<DKSRef> getSuccessorsList(int index) {
	// return succlist.subList(index, succlist.size());
	// }

	// public Set<DKSRef> getSharedList() {
	// return sharedSucclist.keySet();
	// }

	/**
	 * Adds the List of peers given as parameter to the SuccessorsList
	 * 
	 * @param slist
	 *            The List of successors
	 */
	public void addAll(List<DKSRef> slist) {
		if (slist == null)
			return;
//		 for (DKSRef ref : slist) {
//			 if (ref != null && !ref.equals(myDKSRef) && !succlist.contains(ref))
//				 succlist.add(ref);
//		 }

		slist.add(0, ringState.successor);
		succlist = slist;
	}
	/**
	 * Adds the List of peers given as parameter to the SuccessorsList
	 * 
	 * @param slist
	 *            The List of successors
	 */
	public void addAllBigger(List<DKSRef> slist, BigInteger nID, BigInteger N) {
		if (slist == null)
			return;
		 for (DKSRef ref : slist) {
			 if (ref != null && !ref.equals(myDKSRef) && !succlist.contains(ref) && !RingIntervals.belongsTo(ref.getId(), nID, ringState.successor.getId(), N, RingIntervals.Bounds.OPEN_CLOSED))
				 succlist.add(ref);
		 }

//		slist.add(0, ringState.successor);
//		succlist = slist;
	}

	/**
	 * Removes the passed peer from the Successor List
	 * 
	 * @param peerToRemove
	 *            The peer to remove
	 */
	public boolean remove(DKSRef peerToRemove) {
		boolean contained = false;
		while (succlist.contains(peerToRemove)) {
			contained = true;
			succlist.remove(peerToRemove);
		}
		return contained;
		// sharedSucclist.remove(peerToRemove);
	}

	/**
	 * Checks if the SuccessorList contains the passed peer
	 * 
	 * @param ref
	 *            The peer to check for
	 * @return true if it's on the SuccessorList, false otherwise
	 */

	public boolean contains(DKSRef ref) {
		return succlist.contains(ref);
	}

	/**
	 * Checks if the successor list is empty
	 * 
	 * @return true when's empty, false otherwise
	 */
	public boolean isEmpty() {
		return succlist.isEmpty();
	}

}
