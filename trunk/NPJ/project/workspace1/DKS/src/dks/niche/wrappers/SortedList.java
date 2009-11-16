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
import dks.addr.DKSRef;
import dks.niche.hiddenEvents.SendRequestEvent;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.ReplicableMessageInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.utils.IntervalsList;
import dks.utils.RingIntervals;

/**
 * The <code>SortedList</code> class
 * 
 * @author Joel
 * @version $Id: SortedList.java 294 2006-05-05 17:14:14Z joel $
 */
public class SortedList {

	private List<BigInteger> ids;

	private List<HashMap<String, ManagementElementInterface>> elements;

	private BigInteger ringId;

	private BigInteger predecessorId;

	NicheAsynchronousInterface logger;

	private DKSParameters dksParameters;

	//int replicationNumber;

	int CHUNK_SIZE = 10;
	
	public SortedList() {

	}

	public SortedList(NicheAsynchronousInterface logger, BigInteger ringId,
			DKSParameters dksParameters) {
		this.logger = logger;

		this.ringId = this.predecessorId = ringId;

		this.dksParameters = dksParameters;

		ids = new LinkedList<BigInteger>();
		elements = new LinkedList<HashMap<String, ManagementElementInterface>>();
		ids.add(ringId);
		elements.add(new HashMap<String, ManagementElementInterface>());

	}

	public void put(BigInteger newRingId, NicheId nicheId,
			ManagementElementInterface mei) {
		// logger.log("Before put op: " + Arrays.deepToString(ids.toArray()));

		int index = ids.indexOf(newRingId); // new
											// BigInteger(id.getLocation()));

		// logger.log("The existing index of "+newRingId+" is " + index);

		if (index < 0) { // find proper place for insertion
			index = getInsertionIndex(newRingId);

			// logger.log("The determined index of "+newRingId+" is " + index);

			ids.add(index, newRingId);
			HashMap<String, ManagementElementInterface> element = new HashMap<String, ManagementElementInterface>();
			element.put(nicheId.toString(), mei);
			elements.add(index, element);

		} else {
			elements.get(index).put(nicheId.toString(), mei);
		}
		/*#%*/ logger.log("The List says: storing Id " + nicheId.toString() + " with key " + newRingId +  " and current index " + index);

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

	private Object[] getManagementElements(String location) {
		int index = ids.indexOf(new BigInteger(location));
		return elements.get(index).values().toArray();
	}

	public ManagementElementInterface remove(NicheId elementToRemove) {
		int index = ids.indexOf(new BigInteger(elementToRemove.getLocation()));
		HashMap<String, ManagementElementInterface> tempMap = elements
				.get(index);
		ManagementElementInterface result = tempMap.remove(
				elementToRemove.toString()
		);
		if (tempMap.isEmpty()) {
			ids.remove(index);
			elements.remove(index);
		}
		return result;
	}

	public ManagementElementInterface get(BigInteger ringId, NicheId nicheId) {
		int index = ids.indexOf(ringId);
		if (index < 0) {
			return null;
		}
		return elements.get(index).get(nicheId.toString());
	}

	// Used only when nodes have failed / left, and the id-range of the
	// successor has increased
	// The successor then requests 'a' symmetric neighbour to send over items
	// corresponding to
	// the increased interval
	public void triggerSequenceTransfer(
			IntervalsList requestedRanges,
			DKSRef believedReceiver,
			BigInteger targetIntervalStart,
			BigInteger targetIntervalEnd,
			SimpleResourceManager rm
		) {

		/*#%*/ String logMessage = "SortedList-getSequence says: I have " + ids.size()
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

//		ArrayList<DelegationRequestMessage> returnValue = new ArrayList<DelegationRequestMessage>();
		HashMap tempMap;
		
		Object[] copyTheseItems;
		Object[] getReplicaInfo;
		BigInteger currentId, replicaZeroLocation, destinationRingId;
		int replicaNumber;
//		ManagementElementInterface currentME;
//		DelegationRequestMessage currentMessage;
		
		for (int i = 0; i < ids.size(); i++) {
			// <= endindex, since we want to also include the
			// (items stored under the) end-index

			currentId = ids.get(i);

			/*#%*/ logMessage += "The id " + currentId + " at the pos " + i
			/*#%*/ 		+ " in my list ";

			if (requestedRanges.contains(currentId)) {
				
				tempMap = elements.get(i);
				copyTheseItems = tempMap.values().toArray();

				if (0 < copyTheseItems.length) {
					
					//we assume that all me:s stored with the same id should 
					//go to the same dest.
					
					replicaZeroLocation = new BigInteger(
							((ManagementElementInterface) copyTheseItems[0])
									.getId().getLocation());

					getReplicaInfo =
						rm.getReplicaTransferInfo(
							replicaZeroLocation,
							targetIntervalStart,
							targetIntervalEnd
						);

					destinationRingId = (BigInteger) getReplicaInfo[0];
					replicaNumber = (Integer) getReplicaInfo[1];
					// replicaZeroIndex + x*delta = in target range. find x,
					// x=replica number

					/*#%*/ logMessage += " has " + copyTheseItems.length
					/*#%*/ 		+ " items that will be copied.\n"
					/*#%*/ 		+ "Orginal location " + replicaZeroLocation
					/*#%*/ 		+ " will be mapped to " + destinationRingId
					/*#%*/ 		+ " with replica number " + replicaNumber + "\n";

					logger.publicExecute(
							new SendChunkClass(
									logger,
									believedReceiver,
									copyTheseItems,
									destinationRingId,
									replicaNumber,
									ManagementElementInterface.COPY_FLAG
							)
					);
				}//endif 0< copytheseitems
				
				/*#%*/ else {
				/*#%*/ 	logMessage += " was of interest, but had no items\n";
				/*#%*/ }

				// ok, we want to re-map the index from _this_ replica
				// to the replica number on the receiving side
				// so, we need the real "target" interval of the
				// broadcasting node for this to work!

				// should we be brave/stupid enough to assume that all
				// items stored under X has the same original id?

			}/*#%*/  else {
			/*#%*/ 	logMessage += " was of no interest\n";
			/*#%*/ }

		}

		/*#%*/ logger.log(logMessage);
		//return returnValue;

	}

	public void updatePredecessorAndTriggerTransfer(
			DKSRef newPredecessor,
			BigInteger newPredecessorId
		) {

		/*#%*/ String logMessage = "SortedList-updatePredecessor says: I had "
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

		//ArrayList<DelegationRequestMessage> returnValue = new ArrayList<DelegationRequestMessage>();
		ArrayList<Object> removeTheseItems = new ArrayList<Object>();
		HashMap<String, ManagementElementInterface> tempMap;
		//int replicaNumber;
		BigInteger replicaLocation;
		//ManagementElementInterface currentME;

		for (int i = startIndex; i < endIndex; i++) {
			// I was starting at +1, since without replication, nothing "real"
			// is stored under the id of the prev predecessor...

			replicaLocation = ids.get(i);
			tempMap = elements.get(i);

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

				logger.publicExecute(
						new SendChunkClass(
								logger,
								newPredecessor,
								moveTheseItems,
								newPredecessorId,
								0,
								ManagementElementInterface.MOVE_FLAG
						)
				);

			} /*#%*/ else {
			/*#%*/ logMessage += " was of interest, but had no items\n";
			/*#%*/ }

		}

		for (int i = 0; i < removeTheseItems.size() - 1; i++) {

			ids.remove(removeTheseItems.get(i));
			elements.remove(removeTheseItems.get(i + 1));
		}

		startIndex = ids.indexOf(oldPredecessorId);
		if (oldPredecessorId != ringId && -1 < startIndex) {
			ids.remove(startIndex);
			elements.remove(startIndex); // safe - only the dummy is stored
											// under the predId
		}

		endIndex = getInsertionIndex(newPredecessorId);
		if (0 <= endIndex) {
			ids.add(endIndex, predecessorId);
			elements.add(endIndex,
					new HashMap<String, ManagementElementInterface>()); // dummy,
																		// to
																		// keep
																		// them
																		// aligned
		}

		/*#%*/ logMessage += "Now I have " + ids.size() + " ids, they are "
		/*#%*/ 		+ Arrays.deepToString(ids.toArray());

		/*#%*/ logger.log(logMessage);

		//return returnValue;
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
	// ManagementElementInterface>>) {
	//	
	// }
	private class SendChunkClass implements Runnable {

		NicheAsynchronousInterface niche;
		Object[] handleTheseItems;
		int flag;
		ArrayList<DelegationRequestMessage> returnValue;
		BigInteger destinationRingId;
		NicheId destinationId;
		int replicaNumber;
		DKSRef believedReceiver;
		
		SendChunkClass(NicheAsynchronousInterface niche, DKSRef believedReceiver, Object[] handleTheseItems, BigInteger destinationRingId, int replicaNumber, int flag) {
			this.niche = niche;
			this.believedReceiver = believedReceiver;
			this.handleTheseItems = handleTheseItems;
			this.destinationRingId = destinationRingId;
			this.destinationId = niche.getResourceManager().getContainterId(destinationRingId.toString());
			this.replicaNumber = replicaNumber;
			this.flag = flag;
			int size = handleTheseItems.length /10;
			this.returnValue = new ArrayList<DelegationRequestMessage>(size == 0 ? 1 : size);
		}
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			
			ManagementElementInterface currentME;
			
			DelegationRequestMessage currentMessage;
			/*#%*/ String logMessage = "Preparing chunks: ";
			int counter = 0;			
			int messageFlag;
			boolean useOrginalReplicaNumber = flag == ManagementElementInterface.MOVE_FLAG; 
			
			if(flag == ManagementElementInterface.MOVE_FLAG) {
				messageFlag = ManagementElementInterface.RECREATED_ON_MOVE;
			} else {
				messageFlag = ManagementElementInterface.RECREATED_ON_FAIL;
			}
			for (Object object : handleTheseItems) {

				currentME = ((ManagementElementInterface) object);
				
				currentMessage = currentME.transfer(flag);
				
				if(currentMessage != null) {
					/*#%*/ logMessage += "Adding ME "
					/*#%*/ 		+ currentME.getId().toString() + "\n";

					counter++;
					
					returnValue.add(
							((DelegationRequestMessage)
									currentMessage
											.setLiveInformation(
													useOrginalReplicaNumber ? currentME.getReplicaNumber() : replicaNumber,
													messageFlag,
													destinationRingId
												)
											)
							);
				} /*#%*/ else {
				/*#%*/ logMessage += "NOT adding ME "
				/*#%*/ 		+ currentME.getId().toString() + " as it could not be copied\n";
				/*#%*/ }
				
				if(counter == CHUNK_SIZE) {
					
					//resent counter
					counter = 0;
					
					DelegationRequestMessage message =
						new DelegationRequestMessage(
							destinationId, //TODO: here we care only about the loc. part, so should it be this, or replicaLocation? test!
							DelegationRequestMessage.TYPE_BULK,
							returnValue
					);
	
					niche.sendToManagement(believedReceiver, destinationId, message);
//					niche.trigger(
//							new SendRequestEvent(
//								believedReceiver,
//								destinationRingId,
//								message,
//								null,
//								null,
//								(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_NODE)
//							)
//					);

					
				} //endif
			} //end for

			if(counter != 0) {
				
				//send what is left
				DelegationRequestMessage message =
					new DelegationRequestMessage(
						destinationId, //TODO: here we care only about the loc. part, so should it be this, or replicaLocation? test!
						DelegationRequestMessage.TYPE_BULK,
						returnValue
				);

				niche.sendToManagement(believedReceiver, destinationId, message);
				
//				niche.trigger(
//						new SendRequestEvent(
//							believedReceiver,
//							destinationRingId,
//							message,
//							null,
//							null,
//							(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_NODE)
//						)
//				);

				
			} //endif
			
			niche.log(logMessage);
		}//end run
		
	} //end inner class

}//end outer class
