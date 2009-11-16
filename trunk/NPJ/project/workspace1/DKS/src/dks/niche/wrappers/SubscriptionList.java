/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import dks.DKSParameters;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.utils.IntervalsList;
import dks.utils.RingIntervals;

/**
 * The <code>SortedList</code> class
 * 
 * @author Joel
 * @version $Id: SortedList.java 294 2006-05-05 17:14:14Z joel $
 */
public class SubscriptionList {

	private List<BigInteger> ids;

	private List<HashMap<String, DelegationRequestMessage>> subscriptions;

	private BigInteger ringId;

	private BigInteger predecessorId;

	NicheAsynchronousInterface logger;

	private DKSParameters dksParameters;

	int replicationNumber;

	public SubscriptionList() {

	}

	public SubscriptionList(NicheAsynchronousInterface logger, BigInteger ringId,
			DKSParameters dksParameters) {
		this.logger = logger;

		this.ringId = this.predecessorId = ringId;

		this.dksParameters = dksParameters;

		ids = new LinkedList<BigInteger>();
		subscriptions = new LinkedList<HashMap<String, DelegationRequestMessage>>();
		ids.add(ringId);
		subscriptions.add(new HashMap<String, DelegationRequestMessage>());

	}

	public void put(BigInteger newRingId, NicheId nicheId,
			DelegationRequestMessage mei) {
		// logger.log("Before put op: " + Arrays.deepToString(ids.toArray()));

		int index = ids.indexOf(newRingId); // new
											// BigInteger(id.getLocation()));

		// logger.log("The existing index of "+newRingId+" is " + index);

		if (index < 0) { // find proper place for insertion
			index = getInsertionIndex(newRingId);

			// logger.log("The determined index of "+newRingId+" is " + index);

			ids.add(index, newRingId);
			HashMap<String, DelegationRequestMessage> element = new HashMap<String, DelegationRequestMessage>();
			element.put(nicheId.toString(), mei);
			subscriptions.add(index, element);

		} else {
			subscriptions.get(index).put(nicheId.toString(), mei);
		}
		/*#%*/ logger.log("The Subscription-list says: storing Id " + nicheId.toString() + " with index " + newRingId);

	}

	public boolean containsKey(String idString) {
		return containsKey(new BigInteger(idString));
	}

	public boolean containsKey(BigInteger id) {
		if (id.equals(predecessorId)) {
			return false;
		}
		return ids.contains(id);
	}

	public DelegationRequestMessage remove(NicheId elementToRemove) {
		int index = ids.indexOf(new BigInteger(elementToRemove.getLocation()));
		HashMap<String, DelegationRequestMessage> tempMap = subscriptions
				.get(index);
		DelegationRequestMessage result = tempMap.remove(elementToRemove
				.toString());
		if (tempMap.isEmpty()) {
			ids.remove(index);
			subscriptions.remove(index);
		}
		return result;
	}

	public DelegationRequestMessage get(BigInteger ringId, NicheId nicheId) {
		int index = ids.indexOf(ringId);
		if (index < 0) {
			return null;
		}
		return subscriptions.get(index).get(nicheId.toString());
	}

	// Used only when nodes have failed / left, and the id-range of the
	// successor has increased
	// The successor then requests 'a' symmetric neighbour to send over items
	// corresponding to
	// the increased interval
	public ArrayList<DelegationRequestMessage> getSequence(
			IntervalsList requestedRanges, BigInteger targetIntervalStart,
			BigInteger targetIntervalEnd, SimpleResourceManager rm) {

		/*#%*/ String logMessage = "SubscriptionList-getSequence says: I have " + ids.size()
		/*#%*/ 		+ " Ids, they are: " + Arrays.deepToString(ids.toArray())
		/*#%*/ 		+ "\nThe requested target range(s) lies between "
		/*#%*/ 		+ targetIntervalStart + " and " + targetIntervalEnd + "\n";

		// the intervalStart can be anywhere...
		// if(RingIntervals.belongsTo(intervalStart, predecessorId, ringId,
		// dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED)) {
		// startIndex = ids.contains(intervalStart) ? ids.indexOf(intervalStart)
		// : getInsertionIndex(intervalStart) + 1; //TODO check...!
		// } else {
		// startIndex = ids.indexOf(predecessorId);
		// }
		// int endIndex;
		// //the intervalEnd can be anywhere...
		// if(RingIntervals.belongsTo(intervalEnd, predecessorId, ringId,
		// dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED)) {
		// endIndex = ids.contains(intervalEnd) ? ids.indexOf(intervalEnd) :
		// getInsertionIndex(intervalEnd);
		// } else {
		// endIndex = ids.indexOf(ringId);
		// }

		ArrayList<DelegationRequestMessage> returnValue = new ArrayList<DelegationRequestMessage>();
		HashMap tempMap;
		Object[] copyTheseItems;
		Object[] getReplicaInfo;
		BigInteger currentId, replicaZeroLocation, replicaLocation;
		int replicaNumber;
		DelegationRequestMessage currentME;

		for (int i = 0; i < ids.size(); i++) {
			// <= endindex, since we want to also include the
			// (items stored under the) end-index

			currentId = ids.get(i);

			/*#%*/ logMessage += "The id " + currentId + " at the pos " + i
			/*#%*/ 		+ " in my list ";

			if (requestedRanges.contains(currentId)) {
				tempMap = subscriptions.get(i);
				copyTheseItems = tempMap.values().toArray();

				if (0 < copyTheseItems.length) {
					replicaZeroLocation = new BigInteger(
							((DelegationRequestMessage) copyTheseItems[0])
									.getId().getLocation());

					getReplicaInfo = rm.getReplicaTransferInfo(
							replicaZeroLocation, targetIntervalStart,
							targetIntervalEnd);

					replicaLocation = (BigInteger) getReplicaInfo[0];
					replicaNumber = (Integer) getReplicaInfo[1];
					// replicaZeroIndex + x*delta = in target range. find x,
					// x=replica number

					/*#%*/ logMessage += " has " + copyTheseItems.length
					/*#%*/ 		+ " items that will be copied.\n"
					/*#%*/ 		+ "Orginal location " + replicaZeroLocation
					/*#%*/ 		+ " will be mapped to " + replicaLocation
					/*#%*/ 		+ " with replica number " + replicaNumber + "\n";

					for (Object object : copyTheseItems) {

						currentME = ((DelegationRequestMessage) object);
						/*#%*/ logMessage += "Adding subscription "
						/*#%*/ 		+ currentME.getId().toString() + "\n";

						returnValue.add(
								((DelegationRequestMessage)
										currentME.getLiveCopy(
												replicaNumber,
												ManagementElementInterface.RECREATED_ON_FAIL,
												replicaLocation)
								)
							);
					}

				} /*#%*/ else {
				/*#%*/ 	logMessage += " was of interest, but had no items\n";
				/*#%*/ }

				// ok, we want to re-map the index from _this_ replica
				// to the replica number on the receiving side
				// so, we need the real "target" interval of the
				// broadcasting node for this to work!

				// should we be brave/stupid enough to assume that all
				// items stored under X has the same original id?

			} /*#%*/ else {
			/*#%*/ logMessage += " was of no interest\n";
			/*#%*/ }

		}

		/*#%*/ logger.log(logMessage);
		return returnValue;

	}

	public ArrayList<DelegationRequestMessage> updatePredecessor(
			BigInteger newPredecessorId) {

		/*#%*/ String logMessage = "SubscriptionList-updatePredecessor says: I had "
		/*#%*/ 		+ ids.size() + " ids, they were: "
		/*#%*/ 		+ Arrays.deepToString(ids.toArray())
		/*#%*/ 		+ " before updating my predecessor to " + newPredecessorId
		/*#%*/ 		+ "\n";

		BigInteger oldPredecessorId = predecessorId;
		predecessorId = newPredecessorId;

		int startIndex = ids.indexOf(oldPredecessorId);
		int endIndex;
		if (ids.contains(newPredecessorId)) {
			endIndex = ids.indexOf(newPredecessorId) + 1;
			// +1 since we want to move the data stored under this id
		} else {
			endIndex = getInsertionIndex(newPredecessorId);
		}

		ArrayList<DelegationRequestMessage> returnValue = new ArrayList<DelegationRequestMessage>();
		ArrayList removeTheseItems = new ArrayList();
		HashMap tempMap;
		int replicaNumber;
		BigInteger replicaLocation;
		DelegationRequestMessage currentME;

		for (int i = startIndex; i < endIndex; i++) {
			// I was starting at +1, since without replication, nothing "real"
			// is stored under the id of the prev predecessor...

			replicaLocation = ids.get(i);
			tempMap = subscriptions.get(i);

			/*#%*/ logMessage += "The id " + replicaLocation + " at the pos " + i
			/*#%*/ 		+ " in my list ";

			removeTheseItems.add(replicaLocation);
			removeTheseItems.add(tempMap);

			Object[] moveTheseItems = tempMap.values().toArray();

			// System.out.println(i + " moveTheseItems: " +
			// Arrays.deepToString(moveTheseItems));
			if (0 < moveTheseItems.length) {

				/*#%*/ logMessage += " has " + moveTheseItems.length
				/*#%*/ 		+ " items that will be moved.\n";

				for (Object object : moveTheseItems) {

					currentME = (DelegationRequestMessage) object;
					replicaNumber = currentME.getReplicaNumber();

					/*#%*/ logMessage += "Adding subscription " + currentME.getId().toString()
					/*#%*/ 		+ ":" + replicaNumber + "\n";

					if (0 <= replicaNumber) {
						returnValue.add(
							 (DelegationRequestMessage)
							 	currentME.setLiveInformation(
							 			replicaNumber,
							 			ManagementElementInterface.RECREATED_ON_MOVE,
							 			replicaLocation
							 	)
							
					);
								
					} else {
						returnValue.add(
								((DelegationRequestMessage) currentME)
						);
					}

				}
			} /*#%*/ else {
			/*#%*/ logMessage += " was of interest, but had no items\n";
			/*#%*/ }

		}

		for (int i = 0; i < removeTheseItems.size() - 1; i++) {

			ids.remove(removeTheseItems.get(i));
			subscriptions.remove(removeTheseItems.get(i + 1));
		}

		startIndex = ids.indexOf(oldPredecessorId);
		if (oldPredecessorId != ringId && -1 < startIndex) {
			ids.remove(startIndex);
			subscriptions.remove(startIndex); // safe - only the dummy is stored
											// under the predId
		}

		endIndex = getInsertionIndex(newPredecessorId);
		if (0 <= endIndex) {
			ids.add(endIndex, predecessorId);
			subscriptions.add(endIndex,
					new HashMap<String, DelegationRequestMessage>()); // dummy,
																		// to
																		// keep
																		// them
																		// aligned
		}

		/*#%*/ logMessage += "Now I have " + ids.size() + " subscription-ids, they are "
		/*#%*/ + Arrays.deepToString(ids.toArray());

		/*#%*/ logger.log(logMessage);

		return returnValue;
	}

	public String printPositions() {
		return Arrays.deepToString(ids.toArray());
	}

	public boolean belongsToMe(BigInteger id) {
		boolean value = RingIntervals.belongsTo(id, predecessorId, ringId,
				dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED);
		return value;
	}

	private int getInsertionIndex(BigInteger thisRingId) {
		if (ids.contains(thisRingId)) {
			return -1;
		}
		for (int i = 0; i < ids.size() - 1; i++) { // remember the bounds:
													// size-1
			if (RingIntervals.belongsTo(thisRingId, ids.get(i), ids.get(i + 1),
					dksParameters.N, RingIntervals.Bounds.OPEN_OPEN)) {
				return i + 1;
			}
		}
		// return ids.size()-1; //hmm, checkme
		return 0;
	}
	// public void put(String location, HashMap<String,
	// DelegationRequestMessage>>) {
	//	
	// }

}
