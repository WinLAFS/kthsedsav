/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.bcast.events.PseudoReliableIntervalBroadcastAckEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastDeliverEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.bcast.events.RecursiveIntervalAggregationMyValueEvent;
import dks.bcast.events.RecursiveIntervalAggregationProcessValuesEvent;
import dks.bcast.events.RecursiveIntervalAggregationValuesProcessedEvent;
import dks.bcast.interfaces.PseudoReliableIntervalBroadcastAckInterface;
import dks.bcast.interfaces.PseudoReliableIntervalBroadcastDeliverInterface;
import dks.bcast.messages.PseudoReliableIntervalBroadcastAckMessage;
import dks.bcast.messages.PseudoReliableIntervalBroadcastMessage;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.fd.events.SuspectEvent;
import dks.fd.events.ReviseSuspicionEvent;
import dks.ring.ChordRingMaintenanceComponent;
import dks.router.GenericRoutingTableInterface;
import dks.utils.CompactSet;
import dks.utils.IntervalsList;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;

/**
 * The <code>PseudoReliableIntervalBroadcastComponent</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: PseudoReliableIntervalBroadcastComponent.java 294 2006-05-05
 *          17:14:14Z alshishtawy $
 */

public class PseudoReliableIntervalBroadcastComponent extends
		CommunicatingComponent {

	class InstanceData {
		// If aggregating & this node is in the interval then should wait for a
		// value from the app
		boolean waitForApp;

		// the node ID and the Info containing the interval it is responsible
		// for
		Hashtable<BigInteger, IntervalBroadcastInfo> pendingAckSet;

		ArrayList<Object> aggregationValues;

		DKSRef parent;

		DKSRef initiator;

		BigInteger instanceId;

		Boolean aggregate;

		boolean idRangeCast;

		Boolean intermediateNode;

		Boolean processValues;

		IntervalsList interval;

		IntervalBroadcastInfo info;

		public InstanceData() {
			intermediateNode = new Boolean(false);
			processValues = new Boolean(false);
			waitForApp = false;
			pendingAckSet = new Hashtable<BigInteger, IntervalBroadcastInfo>();
			aggregationValues = new ArrayList<Object>();
		}

		public String getUniqueId() {
			return initiator.getId() + ":" + instanceId;
		}

		@Override
		public String toString() {
			String s;
			s = String.format("UID: %5s, Parent: %5s, aggr: %s\n",
					getUniqueId(), parent.getId(), aggregate);
			s += "Intervals: " + interval + "\n";
			s += "Values:\n";
			for (Object o : aggregationValues) {
				s += o.toString() + "\n";
			}
			s += "Pending set:\n";
			for (IntervalBroadcastInfo info : pendingAckSet.values()) {
				s += info + "\n";
			}

			return s;
		}

	}

	/*#%*/ private static Logger log = Logger.getLogger(PseudoReliableIntervalBroadcastComponent.class);

	DKSRef myRef;

	static BigInteger msgID = BigInteger.ZERO;

	// Node ID and the set of messages recieved from that node.
	// OBS this needs to be garbage collected periodically
	//TODO
	Hashtable<BigInteger, HashMap<BigInteger, IntervalsList>> deliveredSet;

	Hashtable<String, InstanceData> instancesData;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public PseudoReliableIntervalBroadcastComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);
		// System.out.println("Ahmad: init
		// PseudoReliableIntervalBroadcastComponent");
		myRef = registry.getRingMaintainerComponent().getMyDKSRef();
		instancesData = new Hashtable<String, InstanceData>();
		deliveredSet = new Hashtable<BigInteger, HashMap<BigInteger, IntervalsList>>();

		registerForEvents();

		registerConsumers();
	}

	/**
	 * 
	 */
	protected void registerConsumers() {
		registerConsumer("handlePseudoReliableIntervalBroadcastMessage",
				PseudoReliableIntervalBroadcastMessage.class);
		registerConsumer("handlePseudoReliableIntervalBroadcastAckMessage",
				PseudoReliableIntervalBroadcastAckMessage.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		// System.out.println("Ahmad: Registering for events");
		register(SuspectEvent.class, "handlePeerSuspectedEvent");
		register(ReviseSuspicionEvent.class, "handleRectifyPeerEvent");
		register(PseudoReliableIntervalBroadcastStartEvent.class,
				"handlePseudoReliableIntervalBroadcastStartEvent");
		register(RecursiveIntervalAggregationMyValueEvent.class,
				"handleRecursiveIntervalAggregationMyValueEvent");
		register(RecursiveIntervalAggregationValuesProcessedEvent.class,
				"handleRecursiveIntervalAggregationValuesProcessedEvent");
	}

	/**
	 * @param event
	 */
	public void handlePseudoReliableIntervalBroadcastMessage(
			DeliverMessageEvent event) {
		PseudoReliableIntervalBroadcastMessage pseudoReliableMessage = (PseudoReliableIntervalBroadcastMessage) event
				.getMessage();
		IntervalBroadcastInfo info = pseudoReliableMessage.getInfo();
		preprocessData(info);
		processPRIB(info);

		// System.out.println("BCAST GOT MESSAGE from="
		// + event.getMessageInfo().getSource());
	}

	public void handlePseudoReliableIntervalBroadcastAckMessage(
			DeliverMessageEvent event) {
		PseudoReliableIntervalBroadcastAckMessage message = (PseudoReliableIntervalBroadcastAckMessage) event
				.getMessage();
		IntervalsList interval = message.getInterval();
		InstanceData data = instancesData.get(message.getUniqueId());

		/*#%*/ log.debug("AckMessage with Id "
		/*#%*/ 	+ message.getUniqueId()
		/*#%*/ 	+ " for the interval "
		/*#%*/ 	+ interval.toString()
		/*#%*/ 	+ " received from "
		/*#%*/ 	+ message.getSenderRef()
		/*#%*/ );
		
		// System.out.println("Ahmad: Interval is " + interval);

		IntervalBroadcastInfo info = data.pendingAckSet.get(message
				.getSenderRef().getId());
		info.interval.subtractFromSelf(interval);
		
		if (info.interval.isEmpty()) {
			data.pendingAckSet.remove(message.getSenderRef().getId());
		}

		if (data.aggregate) {
			data.aggregationValues.addAll(message.getValues());
		}

		// if I got an ack from someone then I'm an intermediateNode :)
		data.intermediateNode = true;

		if (checkTermination(data.getUniqueId())) {
			sendAckToParent(data.getUniqueId());
		}

	}

	public void handlePeerSuspectedEvent(SuspectEvent event) {
		BigInteger susPeer = event.peer.getId();
		// System.out.println("BCast: PSE: Node " + susPeer + " is suspected at
		// node " + myRef.getId());
		IntervalBroadcastInfo info = null;
		// check if this peer is pending in all instances
		for (InstanceData data : instancesData.values()) {
			info = data.pendingAckSet.get(susPeer);
			// if info is not null then the susPeer is in the pending set for
			// the current instance
			if (info != null) {
				// remove susPeer from the pending set
				data.pendingAckSet.remove(susPeer);
				// reprocess the interval of susPeer so it will be assigned to
				// other peers using the same algorithm
				processPRIB(info);
			}
		}
	}

	public void handleRectifyPeerEvent(ReviseSuspicionEvent event) {
		BigInteger peer = event.getRectifiedPeer().getId();

	}

	public void handlePseudoReliableIntervalBroadcastStartEvent(
			PseudoReliableIntervalBroadcastStartEvent event) {

		// System.out.println("Ahmad:
		// handlePseudoReliableIntervalBroadcastStartEvent");

		IntervalBroadcastInfo info = event.getInfo();
		preprocess(info);
		processPRIB(info);

		// if(info.getAggregate()) {
		// // Add an entry for this instance in the hash table
		// aggregationValues.put(info.getInstanceId(), new ArrayList<Object>());
		// TimerComponent timer = registry.getTimerComponent();
		// timer.registerTimer(RecursiveIntervalAggregationTimeoutEvent.class,
		// info.getInstanceId(), info.getAggregationTimeout());
		// }

	}

	public void handleRecursiveIntervalAggregationMyValueEvent(
			RecursiveIntervalAggregationMyValueEvent event) {

		/*#%*/ String logMessage = "I am in handleRecursiveIntervalAggregationMyValueEvent ";

		InstanceData data = instancesData.get(event.getUniqueId());
		data.waitForApp = false;
		data.aggregationValues.addAll(event.getValues());
		if (checkTermination(data.getUniqueId())) {

			/*#%*/ logMessage += "and I reply";
			sendAckToParent(data.getUniqueId());
		} /*#%*/ else {
			/*#%*/ logMessage += "and I do NOT reply";
		/*#%*/ }
		/*#%*/ log.debug(logMessage);

	}

	public void handleRecursiveIntervalAggregationValuesProcessedEvent(
			RecursiveIntervalAggregationValuesProcessedEvent event) {
		String uid = event.getUniqueId();
		InstanceData data = instancesData.get(uid);
		data.aggregationValues = event.getValues();

		sendAckToParent2(uid);
	}

	/**
	 * This method is called only by the initiator to fill in default values if
	 * they where not set by the application.
	 * 
	 * @param info
	 */
	private void preprocess(IntervalBroadcastInfo info) {

		if (info.getInitiator() == null)
			info.setInitiator(myRef);
		if (info.getSource() == null)
			info.setSource(myRef);
		if (info.getDestination() == null)
			info.setDestination(myRef);
		if (info.getInstanceId() == null) {
			info.setInstanceId(msgID);
			msgID = msgID.add(BigInteger.ONE);
		}
		if (info.getInterval() == null) {
			BigInteger N = registry.getRingMaintainerComponent()
					.getDksParameters().N;
			try {
				info.setInterval(new IntervalsList(BigInteger.ZERO, N
						.subtract(BigInteger.ONE), Bounds.CLOSED_CLOSED, N));
			} catch (SimpleIntervalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processPRIB(IntervalBroadcastInfo info) {
		BigInteger myId = myRef.getId();
		BigInteger N = registry.getRingMaintainerComponent().getDksParameters().N;
		BigInteger limit;

		IntervalsList I = new IntervalsList(info.getInterval()); // info.getInterval();
		IntervalsList rest = new IntervalsList(info.getInterval()); // //

		// System.out.println(I.toString() + " " + myRef.getId());

		ArrayList<DKSRef> u = getCurrentUniquePointers();
		InstanceData data = getInstanceData(info);

		IntervalsList J = null, intervalToRemoveNormallyJ = null;
		/*#%*/ String deliverMessage;

		if (info.isIdRangeCast()) {

			int i = u.size() - 1;
			DKSRef tempRef = u.get(i--);
			
			while(tempRef == null) {
				tempRef = u.get(i--);
			}
			//if null, go further in the finger-pointer-list and make the interval bigger
			try {

				J = new IntervalsList(
						tempRef.getId(),
						myId,
						Bounds.CLOSED_CLOSED, //Check endpoints
						N
				);
					
			} catch (SimpleIntervalException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (i == u.size() - 2) {
				//the normal case
				intervalToRemoveNormallyJ = J;
			} else {
				
				try {
					intervalToRemoveNormallyJ = 
						new IntervalsList(
								myId,
								myId,
								Bounds.CLOSED_CLOSED, //Check endpoints
								N
						);
				} catch (SimpleIntervalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*#%*/ deliverMessage = "There is churn in the system, "
				/*#%*/ 	+ "it is NOT guaranteed that I'm delivering this message correctly "
				/*#%*/ 	+"- the correct finger position pointer at "
				/*#%*/ 	+ (u.size() - 1)
				/*#%*/ 	+ " was null, so "
				/*#%*/ 	+ tempRef.getId()
				/*#%*/ 	+ " at position "
				/*#%*/ 	+ i
				/*#%*/ 	+ " was used instead. ";				
			}

			/*#%*/ deliverMessage = "This is a broadcast to 'responsible of ids in the range-"
			/*#%*/ 		+ info.getInterval().toString() + "'-message. ";
		} /*#%*/ else {
		/*#%*/ deliverMessage = "This is a broadcast to 'nodes in the range'-"
		/*#%*/ 			+ info.getInterval() + "'-message. ";
		/*#%*/ }

		if (
				I.contains(myId) || 
					(
							info.isIdRangeCast()
						&&
							J.intersects(I)
					)
			) {

			// TODO: check joel-hack:
			if (info.isIdRangeCast()) {
				I.subtractFromSelf(intervalToRemoveNormallyJ);
				rest.subtractFromSelf(I);
				info.setInterval(rest);
			}
			// deliver the message to the application
			/*#%*/ deliverMessage += "I am delivering the message to myself, after which I "
			/*#%*/ 		+ I.toString() + " remains";
			boolean newInstance = deliver(info);
			// if aggregating also then I should wait for a value from the
			// application
			// but only if this is a new instance (not reprocessing due to
			// failure)
			// if not new then I already have the reply from the app
			if (newInstance && info.aggregate) {
				data.waitForApp = true;
			}

		}/*#%*/  else {
		/*#%*/ 	deliverMessage += "I am NOT delivering the message to myself";
		/*#%*/ }

		/*#%*/ log.debug(deliverMessage);
		
		limit = myId;
		DKSRef next = info.getSource();
		// boolean hasSentToSuccessor = false;
		// System.out.println("Ahmad: Unique pointers size = " + u.size());
		int fingerLimit = info.isIdRangeCast() ? 1 : 0;
		BigInteger intervalStart;
		Bounds bounds = info.isIdRangeCast() ?
				Bounds.CLOSED_CLOSED
			:
				Bounds.CLOSED_OPEN
		;
		
		for (int i = u.size() - 1; i > fingerLimit; i--) { //OBS OBS, don't send to succ here if it is a range-cast, do it below to avoid duplication

			if (u.get(i) != null) {
				try {
					intervalStart = info.isIdRangeCast() ?
							u.get(i).getId().add(BigInteger.ONE)
						:
							u.get(i).getId()
					;
					J = new IntervalsList(
							intervalStart,
							limit,
							bounds,
							N
					);
					//Check the boundaries
					if (I.intersects(J)) {
						
						//please disable when stable:
						/*#%*/ log.debug(
						/*#%*/ 		"Target range I "
						/*#%*/ 		+ I.toString()
						/*#%*/ 		+ " intersects range "
						/*#%*/ 		+ J.toString()
						/*#%*/ 		+ " of node "
						/*#%*/ 		+ u.get(i)
						/*#%*/ 		+ " with finger number "
						/*#%*/ 		+ i
						/*#%*/ );
						
						IntervalBroadcastInfo tmpInfo = new IntervalBroadcastInfo(
								info);
						tmpInfo.setSource(myRef);
						tmpInfo.setDestination(u.get(i));
						tmpInfo.setInterval(IntervalsList.intersection(I, J));
						sendPRIB(tmpInfo);
						updateInstanceData(tmpInfo);
						I.subtractFromSelf(J);
						limit = u.get(i).getId();
					}

				} catch (SimpleIntervalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}/*#%*/  else {
			/*#%*/ 	log.debug("NULL in routing table!");
			/*#%*/ }
		} // if node-cast, then end here!

		if (info.isIdRangeCast()) {
			DKSRef succ = u.get(1);
			try {
				J = IntervalsList.add(
						new IntervalsList(
								myId,
								succ.getId(),
								Bounds.CLOSED_OPEN,
								N
						), 
						new IntervalsList(
								succ.getId(),
								limit,
								Bounds.CLOSED_OPEN,
								N
						)
				);

				if (I.intersects(J) // && !hasSentToSuccessor
						&& !next.equals(u.get(1))) {

					/*#%*/ log.debug(
					/*#%*/ 		"Target range I "
					/*#%*/ 		+ I.toString()
					/*#%*/ 		+ " also intersects the range "
					/*#%*/ 		+ J.toString()
					/*#%*/ 		+ " of my successor & ev leftovers"
					/*#%*/ );
					
					IntervalBroadcastInfo tmpInfo = new IntervalBroadcastInfo(
							info);
					tmpInfo.setSource(myRef);
					tmpInfo.setDestination(u.get(1));
					tmpInfo.setInterval(IntervalsList.intersection(I, J));
					sendPRIB(tmpInfo);
					updateInstanceData(tmpInfo);

				}

			} catch (SimpleIntervalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (checkTermination(info.getUniqueId())) {
			sendAckToParent(info.getUniqueId());
		}
	}

	private void sendAckToParent(String uniqueId) {
		InstanceData data = instancesData.get(uniqueId);

		IntervalBroadcastInfo info = data.info;

		// Check if we need to preprocess (aggregation & process is true) AND
		// intermediateNode
		/*#%*/ log.debug("intermediateNode = " + data.intermediateNode
		/*#%*/ 		+ ", aggregate = " + data.aggregate + ", processValues = "
		/*#%*/ 		+ data.processValues);
		if (data.intermediateNode && data.aggregate && data.processValues) {
			RecursiveIntervalAggregationProcessValuesEvent event = null;
			;
			if (info.processValuesClassName != null) { // the app request
				// overriding default
				// event
				try {
					event = (RecursiveIntervalAggregationProcessValuesEvent) Class
							.forName(info.processValuesClassName).newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				event = new RecursiveIntervalAggregationProcessValuesEvent();
			}
			/*#%*/ log.debug("Sending " + data.aggregationValues.size()
			/*#%*/ 		+ " values to be preprocessed by the App.");
			event.setValues(data.aggregationValues);
			event.setUniqueId(uniqueId);
			trigger(event);

		} else {
			/*#%*/ if (!data.intermediateNode && data.aggregate && data.processValues) {
			/*#%*/ log.debug("I'm a leaf so no preprocessing of data!");
			/*#%*/ }
			// no processing of values
			sendAckToParent2(uniqueId);
		}

	}

	/**
	 * @param uniqueId
	 */
	private void sendAckToParent2(String uniqueId) {
		InstanceData data = instancesData.get(uniqueId);
		IntervalBroadcastInfo info = data.info;
		if (data.initiator.equals(myRef)) { // i'm done
			PseudoReliableIntervalBroadcastAckInterface event = null;
			if (info.getAckAggrEventClassName() != null) {
				try {
					event = (PseudoReliableIntervalBroadcastAckInterface) Class
							.forName(info.getAckAggrEventClassName())
							.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				event = new PseudoReliableIntervalBroadcastAckEvent();
			}
			// event.setAggregate(data.aggregate);
			// event.setInitiator(myRef);
			// event.setInstanceId(data.instanceId);
			event.setValues(data.aggregationValues);
			/*#%*/ log.debug("Done! AckToApp at " + myRef.getId() + " event id "
			/*#%*/ 		+ uniqueId + " had type "
			/*#%*/ 		+ event.getClass().getSimpleName());
			trigger((Event) event);

		} else {
			/*#%*/ log.debug("AckToParent " + data.parent.getId());
			PseudoReliableIntervalBroadcastAckMessage message = new PseudoReliableIntervalBroadcastAckMessage();
			message.setInitiatorRef(data.initiator);
			message.setInstanceId(data.instanceId);
			message.setSenderRef(myRef);
			message.setAggregate(data.aggregate);
			message.setInterval(data.interval);
			// if not aggregating this will be ignored
			message.setValues(data.aggregationValues);

			send(message, myRef, data.parent);
		}

	}

	/**
	 * @param info
	 * @return
	 */
	private boolean checkTermination(String uniqueId) {
		InstanceData data = instancesData.get(uniqueId);

		if (!data.waitForApp && data.pendingAckSet.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * @param tmpInfo
	 */
	private void updateInstanceData(IntervalBroadcastInfo tmpInfo) {
		InstanceData data = instancesData.get(tmpInfo.getUniqueId());

		if (data.pendingAckSet.contains(tmpInfo.destination.getId())) {
			IntervalBroadcastInfo destInfo = data.pendingAckSet
					.get(tmpInfo.destination.getId());
			destInfo.interval.addToSelf(tmpInfo.interval);
		} else {
			/*#%*/ log.debug("Broadcast: "
			/*#%*/ 		+ tmpInfo.getUniqueId()
			/*#%*/ 		+ " adds "
			/*#%*/ 		+ tmpInfo.destination.getId()
			/*#%*/ 		+ " as responsible for "
			/*#%*/ 		+ tmpInfo.interval.toString()
			/*#%*/ 		);
			
			data.pendingAckSet.put(tmpInfo.destination.getId(),
					new IntervalBroadcastInfo(tmpInfo));
		}

	}

	/**
	 * First time for an instance creats a new one if recovering after an error
	 * then gets the one created before
	 * 
	 * @param info
	 * @return
	 */
	private InstanceData getInstanceData(IntervalBroadcastInfo info) {
		InstanceData data = instancesData.get(info.getUniqueId());
		// System.out.println("BCast: Info is INITIALIZED");
		if (data == null) {
			data = new InstanceData();
			data.initiator = info.initiator;
			data.instanceId = info.instanceId;
			data.aggregate = info.aggregate;
			data.parent = info.source;
			data.processValues = info.processValues;
			data.interval = new IntervalsList(info.interval);
			data.info = new IntervalBroadcastInfo(info);
			data.idRangeCast = info.isIdRangeCast();
			instancesData.put(info.getUniqueId(), data);
		}
		return data;
	}

	/**
	 * @param info
	 */
	private void preprocessData(IntervalBroadcastInfo info) {
		InstanceData data = instancesData.get(info.getUniqueId());
		if (data != null) { // this else happens only if a node failed and its
			// parent is reprocessing the interval
			// My parent may change as a result of a node failure
			data.parent = info.source;
			// I don't need to reporcess interval that I have already covered
			info.interval.subtractFromSelf(data.interval);
			// add to the instance data the new Interval that now I'm
			// responsible for
			data.interval.addToSelf(info.interval);
		}
	}

	/**
	 * Forwards the responsibility of a sub interval to another node
	 * 
	 * @param destRef
	 *            The reference of the node.
	 * @param list
	 *            The interval that the node will be responsible for.
	 * @param msg
	 *            The boradcasted message.
	 */
	private void sendPRIB(IntervalBroadcastInfo info) {

		PseudoReliableIntervalBroadcastMessage message = new PseudoReliableIntervalBroadcastMessage();
		message.setInfo(info);

		send(message, myRef, info.getDestination());

		// MessageInfo messageInfo = new MessageInfo(myRef,
		// info.getDestination(), null,
		// null, 0);
		// MarshallMessageEvent marshallMessageEvent = new MarshallMessageEvent(
		// message, messageInfo);
		// trigger(marshallMessageEvent);

	}

	/**
	 * Gets the unique fingers of a node. The element at 0 contains the self
	 * DKSRef.
	 * 
	 * @return a <code>DKSRef</code> ArrayList that contains all unique
	 *         fingers of a node.
	 */
	private ArrayList<DKSRef> getCurrentUniquePointers() {

		ArrayList<DKSRef> list = new ArrayList<DKSRef>();
		list.add(myRef);
		// System.out.println("B-Cast says: I'm " + myRef + " and u[]
		// contains:");
		GenericRoutingTableInterface table = registry.getRouterComponent()
				.getRoutingTable();
		for (long i = 0; i < table.getIntervalsNumber(); i++) {
			if (table.getRoutingTableEntry(i) != null
					&& table.getRoutingTableEntry(i).getIntervalPointer() != null) {
				DKSRef ptr = table.getRoutingTableEntry(i).getIntervalPointer();
				list.add(ptr);
				// System.out.println(ptr);
			}

		}

		DKSRef succ = registry.getRingMaintainerComponent().getRingState().successor;
		DKSRef pred = registry.getRingMaintainerComponent().getRingState().predecessor;
		if (!list.contains(pred)) {
			// System.out.println("and Pred = " + pred);
			list.add(pred);
		}
		if (!list.contains(succ)) {
			// System.out.println("and Succ = " + succ);
			list.add(1, succ);
		}

		return list;
	}

	/**
	 * Delivers the message to the application.
	 * 
	 * @param msg
	 *            The message to deliver to the application.
	 */
	private boolean deliver(IntervalBroadcastInfo info) {
		
		/*#%*/ String logMessage;
		
		boolean send = false;
		HashMap<BigInteger, IntervalsList> nodeDeliveredSet = deliveredSet.get(info.initiator.getId());
		if (nodeDeliveredSet == null) {
			
			nodeDeliveredSet = new HashMap<BigInteger, IntervalsList>();
			nodeDeliveredSet.put(info.instanceId, info.interval);
			deliveredSet.put(info.initiator.getId(), nodeDeliveredSet);
			send = true;
			/*#%*/ logMessage = "Deliver, based on: " + info.toString();
		} else {
			
			IntervalsList alreadyProcessedInterval = nodeDeliveredSet.get(info.instanceId);

			if(alreadyProcessedInterval == null) {
				nodeDeliveredSet.put(info.instanceId, info.interval);
				send = true;
				/*#%*/ logMessage = "Deliver, based on: " + info.toString();
				
			} else {
				IntervalsList workToBeDone = IntervalsList.subtract(info.interval, alreadyProcessedInterval);
				if(workToBeDone.isEmpty()) {
					/*#%*/ logMessage = "Do NOT Deliver," + info.instanceId + " is duplicated and fully overlapping";
				} else {
					//Book keeping - update the range we have covered:
					alreadyProcessedInterval.addToSelf(info.interval);
					nodeDeliveredSet.put(info.instanceId, alreadyProcessedInterval);
					info.setInterval(workToBeDone);
					send = true;
					/*#%*/ logMessage = "Duplicated but not overlapping: for id " + info.instanceId + " " + info.interval + " remains to be covered";
				}
					
			}
		}		
		/*#%*/ log.debug(logMessage);
		
		if (send) {
			PseudoReliableIntervalBroadcastDeliverEvent event = null;
			if (info.deliverEventClassName != null) {
				try {
					event = (PseudoReliableIntervalBroadcastDeliverEvent) Class
							.forName(info.deliverEventClassName).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				event = new PseudoReliableIntervalBroadcastDeliverEvent();
			}
			// System.out.println("DELIVER EVENT " + event.getClass()
			// + " TRIGGERED IN "
			// + registry.getRingMaintainerComponent().getMyDKSRef());
			event.setInfo(new IntervalBroadcastInfo(info));
			trigger(event);
		}
		// System.out.println("Ahmad: Value of send is " + send);
		// System.out.println("#### Delivered Set ###");
		// for (CompactSet set : deliveredSet.values()) {
		// System.out.println(set);
		// }
		return send;
	}

}
