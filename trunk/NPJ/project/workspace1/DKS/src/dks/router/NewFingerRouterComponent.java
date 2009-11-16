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

import static dks.router.RoutingConstants.PERIODIC_MAINTENANCE_TIMER;
import static dks.router.RoutingConstants.TOPOLOGY_MAINTENANCE_ACTIVATED;
import static dks.router.RoutingConstants.TOPOLOGY_MAINTENANCE_LOOKUP_STRATEGY;
import static dks.router.RoutingConstants.TRANSITIVE_RELIABLE_LOOKUP_TIMER;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.HooksNumberTable;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.fd.events.ReviseSuspicionEvent;
import dks.fd.events.SuspectEvent;
import dks.messages.AddBackListEntryMessage;
import dks.messages.LookupOperationRequestMessage;
import dks.messages.LookupOperationResponseMessage;
import dks.messages.Message;
import dks.messages.RecursiveLookupOperationRequestMessage;
import dks.messages.RemEntryMessage;
import dks.messages.TransitiveLookupOperationRequestMessage;
import dks.messages.TransitiveLookupOperationResponseMessage;
import dks.operations.events.OperationRequestEvent;
import dks.ring.RingMaintenanceComponentInt;
import dks.ring.RingState;
import dks.router.events.DiscoveredNodesEvent;
import dks.router.events.InitRoutingTableEvent;
import dks.router.events.LookupRequestEvent;
import dks.router.events.LookupResultEvent;
import dks.router.events.ReliableLookupRequestEvent;
import dks.router.events.RemoveNodeEvent;
import dks.router.events.TopologyMaintenanceLookupResultEvent;
import dks.router.events.TopologyMaintenanceTimerExpiredEvent;
import dks.router.events.TransitiveReliableLookupExpiredEvent;
import dks.router.events.UnreliableLookupRequestEvent;
import dks.router.events.UpdateEntryEvent;
import dks.stats.NodeStatistics;
import dks.timer.TimerComponent;
import dks.utils.LongSequenceGenerator;
import dks.utils.RingIntervals;
import dks.utils.RingIntervals.Bounds;

/**
 * The <code>GenericRouterComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: GenericRouterComponent.java 443 2007-11-22 00:03:38Z Roberto $
 */
public class NewFingerRouterComponent extends CommunicatingComponent implements
		Router {

	/*#%*/ private static Logger log = Logger.getLogger(NewFingerRouterComponent.class);

	protected DKSRef myDKSRef;

	protected BigInteger myId;

	protected DKSParameters dksParameters;

	protected RingMaintenanceComponentInt ringMaintainer;

	protected RingState ringState;

	protected GenericRoutingTableInterface routingTable;

	protected Object lock = null;

	/*
	 * Map containing all the pending operations, the request is stored until
	 * the operation has been performed
	 */
	protected HashMap<Long, LookupOperationRequestMessage> pendingOperations;

	/*
	 * Map used to keep track of the reliable lookup requests
	 */
	protected ConcurrentHashMap<Long, LookupOperationRequestMessage> reliablePendingLookups;

	/*
	 * Map that contains all the reliable recursive lookups that have been sent
	 * to a node and that are expected to come back. It contains the
	 * {@link DKSRef} of the peer to which the lookup request is sent as a key
	 * and the Set of request messages sent to that node as a value. (The
	 * initiator and all nodes on the path to the responsible will store the
	 * requests, then when a peer in the path will become suspected,all the
	 * requests will be sent again)
	 */
	// protected HashMap<DKSRef, Set<RecursiveLookupOperationRequestMessage>>
	// sentReliableRecursiveLookupRequests;
	/*
	 * List containing all the nodes that have a pointer to this node in the
	 * routing table
	 */
	protected List<DKSRef> backList;

	/*
	 * HashMap containing the peers to which has been sent the
	 * AddBackListEntryMessage and the corresponding Routing Entry
	 */
	protected HashMap<DKSRef, RoutingTableEntry> pendingAccountingRequests;

	/*
	 * Map containing all the specific events that have to be issued when a
	 * lookup result is received
	 */
	protected HashMap<Long, Class> specificLookupResultEvent = new HashMap<Long, Class>();

	/*
	 * Sequence generator shared between this component and the
	 * OperationManagerComponent to give uniform unique identifiers to all
	 * operations
	 */
	protected LongSequenceGenerator operationSequenceGenerator;

	/*
	 * Map containing the lookups already seen by this router.
	 */
	protected HashMap<DKSRef, Set<Long>> seenLookups;

	/*
	 * Sequence generator used to generate lookup ids for the lookup requests
	 * issued by this peer
	 */
	protected LongSequenceGenerator lookupIdentifierSequenceGenerator;

	protected TimerComponent timerComponent;

	private long periodicMaintenanceTimerId;

	protected List<DKSRef> routingNeighbors;

	public NewFingerRouterComponent(Scheduler scheduler,
			ComponentRegistry registry, DKSRef myDKSRef,
			LongSequenceGenerator operationSequenceGenerator) {
		super(scheduler, registry);

		// Registering Router in the ComponentRegistry
		registry.registerRouter(this);
		// Joel moved this up from the end of the constructor - otherwise
		// FingerRoutingTable yields null

		this.dksParameters = ComponentRegistry.getDksParameters();

		this.ringMaintainer = registry.getRingMaintainerComponent();

		this.timerComponent = registry.getTimerComponent();

		this.operationSequenceGenerator = operationSequenceGenerator;

		this.lookupIdentifierSequenceGenerator = new LongSequenceGenerator(0);

		this.reliablePendingLookups = new ConcurrentHashMap<Long, LookupOperationRequestMessage>();

		// this.unreliablePendingLookups = new HashMap<Long,
		// LookupOperationRequestMessage>();

		this.pendingOperations = new HashMap<Long, LookupOperationRequestMessage>();

		// this.sentReliableRecursiveLookupRequests = new HashMap<DKSRef,
		// Set<RecursiveLookupOperationRequestMessage>>();

		this.specificLookupResultEvent = new HashMap<Long, Class>();

		this.backList = new LinkedList<DKSRef>();

		this.pendingAccountingRequests = new HashMap<DKSRef, RoutingTableEntry>();

		this.routingNeighbors = Collections
				.synchronizedList(new LinkedList<DKSRef>());

		// Getting a reference to the States of the Ring
		this.ringState = ringMaintainer.getRingState();

		// Creating Routing Table
		this.routingTable = new FingerRoutingTable(myDKSRef.getId(),
				ringMaintainer);

		this.myDKSRef = myDKSRef;
		this.myId = myDKSRef.getId();

		this.seenLookups = new HashMap<DKSRef, Set<Long>>();

		registerEvents();

		registerConsumers();

		registerHooks(this);

		// timerComponent.registerTimer(CleanDuplicateLookupsTimerEvent.class,
		// null, CLEAN_DUPLICATE_LOOKUPS_TIMER);
	}

	private void registerConsumers() {

		registerConsumer("handleTransitiveLookupRequest",
				TransitiveLookupOperationRequestMessage.class);
		registerConsumer("handleLookupResponse",
				TransitiveLookupOperationResponseMessage.class);

		// registerConsumer("handleRecursiveLookupRequest",
		// RecursiveLookupOperationRequestMessage.getStaticMessageType());

		// registerConsumer("handleLookupResponse",
		// RecursiveLookupOperationResponseMessage.getStaticMessageType());

		registerConsumer("handleAddBackListEntry",
				AddBackListEntryMessage.class);

		registerConsumer("handleRemEntry", RemEntryMessage.class);

	}

	private void registerEvents() {

		/* Hooks events */
		register(DiscoveredNodesEvent.class, "handleDiscoveredNodes", true);
		register(RemoveNodeEvent.class, "handleRemovedNode", true);

		register(InitRoutingTableEvent.class, "handleInitRoutingTable", true);
		register(UpdateEntryEvent.class, "handleUpdateEntry", true);

		/*
		 * the handlers of this events will be implemented in the extention of
		 * the GenericRouter
		 */
		register(UnreliableLookupRequestEvent.class,
				"handleUnreliableLookupRequest", true);

		register(ReliableLookupRequestEvent.class,
				"handleReliableLookupRequest", true);

		register(TransitiveReliableLookupExpiredEvent.class,
				"handleTransitiveReliableLookupExpired", true);

		// register(LookupInternalOperationResponseEvent.class,
		// "handleLookupInternalOperationResponse", true);

		/*
		 * FD handlers
		 */
		register(SuspectEvent.class, "handleSuspectedPeer", true);

		register(ReviseSuspicionEvent.class, "handleRectifyPeer", true);

		/*
		 * Generic Routers events
		 */
		register(TopologyMaintenanceLookupResultEvent.class,
				"handleTopologyMaintenanceLookupResult", true);

		register(TopologyMaintenanceTimerExpiredEvent.class,
				"handleTopologyMaintenanceTimerExpired", true);

		// register(CleanDuplicateLookupsTimerEvent.class,
		// "handleCleanDuplicateLookupsTimerEvent");
	}

	protected void registerHooks(Component component) {

		/*
		 * Hook of the Joining d for adding its new predecessor and successor to
		 * the routing table
		 */
		registerHook(HooksNumberTable.HOOK_JOIN_AFTER_POINT, component,
				"handleHookNewNodes");

		/*
		 * Hook for the d which has received a LeaveRequest for removing its
		 * predecessor form the routing table
		 */
		registerHook(HooksNumberTable.HOOK_LEAVE_AFTER_DONE, component,
				"handleHookNodeLeft");

		/*
		 * Hook for adding the nodes discovered during the Ring Maintenance
		 * procedure
		 */
		registerHook(HooksNumberTable.HOOK_STAB_AFTER_SUCC_LIST_RESP,
				component, "handleHookNewNodes");

		registerHook(HooksNumberTable.HOOK_LEAVE_AFTER_POINT, component,
				"handleHookAccountingLeaving");
	}

	/* Hook Handlers */

	/*
	 * Hooks are called directly from the thread executing the RingMaintainer,
	 * events are generated to update the Routing Entries so that another thread
	 * is scheduled to handle them
	 */

	public void handleHookNewNode(Object object) {
		DKSRef newNode = (DKSRef) object;
		DiscoveredNodesEvent discoveredNodesEvent = new DiscoveredNodesEvent(
				new DKSRef[] { newNode });
		trigger(discoveredNodesEvent);
	}

	public void handleHookNewNodes(Object object) {
		DKSRef[] newNodes = (DKSRef[]) object;
		DiscoveredNodesEvent discoveredNodesEvent = new DiscoveredNodesEvent(
				newNodes);
		trigger(discoveredNodesEvent);
	}

	public void handleHookNodeLeft(Object object) {
		DKSRef removedNode = (DKSRef) object;
		RemoveNodeEvent removedNodeEvent = new RemoveNodeEvent(removedNode);
		trigger(removedNodeEvent);
	}

	/* Event Handlers */

	/*
	 * Handlers for the events generated by the hooks
	 */

	public void handleDiscoveredNodes(DiscoveredNodesEvent event) {
		DKSRef[] discoveredNodes = event.getDiscoveredNodes();
		for (DKSRef ref : discoveredNodes) {

			RoutingTableEntry entry = routingTable.getContainingEntry(ref
					.getId());

			if (entry.isAbetterResponsibleId(ref.getId())) {
				/*#%*/ log.debug(ref.getId() + " is a better responsible than "
				/*#%*/ 		+ entry.getIntervalPointer());
				addRoutingTablePointer(ref, entry);
			}
		}
	}

	public void handleRemovedNode(RemoveNodeEvent event) {
		DKSRef nodeToRemove = event.getNodeToRemove();
		routingTable.removePeerIfResponsible(nodeToRemove);
	}

	/*
	 * Failure Detector notifications handlers
	 */

	public void handleSuspectedPeer(SuspectEvent event) {

		DKSRef suspectedPeer = event.getSuspectedPeer();

		/* Removing the peer (if present) from the routing table */

		// synchronized (this) {
		RoutingTableEntry entry = routingTable.getContainingEntry(suspectedPeer
				.getId());

		if (entry.getIntervalPointer() != null
				&& entry.getIntervalPointer().equals(suspectedPeer)) {

			/*#%*/ log.debug("SET Interval Pointer of f("
			/*#%*/ 			+ entry.getIntervalNumber()
			/*#%*/ 			+ ")="
			/*#%*/ 			+ entry.getIntervalStartId()
			/*#%*/ 			+ " to null, and wait for periodic stabilization to update the entry");

			entry.setIntervalPointer(null);

//			if (ringState.predecessor != null && ringState.successor != null) {
//
//				// Shaky, the chordMC might be updating these at the same time!
//
//				/*
//				 * Update the entry (eager approach)
//				 */
//				UpdateEntryEvent updateEntry = new UpdateEntryEvent(entry
//						.getIntervalNumber());
//				trigger(updateEntry);
//			}

		}

		// }

		if (backList.contains(suspectedPeer)) {
			backList.remove(suspectedPeer);
		}

	}

	public void handleRectifyPeer(ReviseSuspicionEvent event) {

		DKSRef rectifiedPeer = event.getRectifiedPeer();

		RoutingTableEntry entry = routingTable.getContainingEntry(rectifiedPeer
				.getId());

		if (entry.isAbetterResponsibleId(rectifiedPeer.getId())) {

			/*
			 * If the rectified peer must be the responsible for the interval
			 */
			addRoutingTablePointer(rectifiedPeer, entry);

		}
	}

	/*
	 * Ring Maintenance Events handlers
	 */

	/*
	 * Handlers for Topology maintenance
	 */

	/* Routing table initialization handlers */

	public void handleInitRoutingTable(InitRoutingTableEvent event) {

		if (TOPOLOGY_MAINTENANCE_ACTIVATED) {
			timerComponent.cancelTimer(periodicMaintenanceTimerId);

			// Scheduling next maintenance
			periodicMaintenanceTimerId = timerComponent.registerTimer(
					TopologyMaintenanceTimerExpiredEvent.class, "",
					PERIODIC_MAINTENANCE_TIMER);

		}
	}

	public void handleUpdateEntry(UpdateEntryEvent event) {
		long i = event.getI();
		BigInteger startingId = routingTable.getStartingId(i);
		//System.out.println("StartingId: "+startingId);
		/*#%*/ log.debug("Will trigger lookup for interval starting at " + startingId);
		ReliableLookupRequestEvent reliableLookupRequestEvent = new ReliableLookupRequestEvent(
				startingId, TOPOLOGY_MAINTENANCE_LOOKUP_STRATEGY,
				TopologyMaintenanceLookupResultEvent.class);

		trigger(reliableLookupRequestEvent);

	}

	public void handleTopologyMaintenanceLookupResult(
			TopologyMaintenanceLookupResultEvent resultEvent) {

		DKSRef responsible = resultEvent.getResponsible();

		/*
		 * Getting routing table entry corresponding to the f(i) looked up
		 */
		RoutingTableEntry entry = routingTable.getContainingEntry(resultEvent
				.getLookedUpId());

		// System.out.println("Resp :" + responsible.getId());

		if (entry.isInRange(responsible.getId())) {

			addRoutingTablePointer(responsible, entry); //moved by J, ggrrgrgaah
			
//			if (entry.isAbetterResponsibleId(responsible.getId())) {
//
//				addRoutingTablePointer(responsible, entry);
//
//			} else {
//				log.debug("Not Better responsible for f(i) "
//						+ entry.getIntervalStartId() + " - Ignoring");
//				// Ignore
//			}
		} /*#%*/ else {
		/*#%*/ 	log.debug("No responsible for f(i) " + entry.getIntervalStartId()
		/*#%*/ 			+ " - Ignoring");
		/*#%*/ }
	}

	/**
	 * Checks every finite amount of time if all the routing Table entries are
	 * !=null and call UpdateEntry if some (or all) of them are
	 */
	public void handleTopologyMaintenanceTimerExpired(
			TopologyMaintenanceTimerExpiredEvent event) {

		BigInteger nextStartingId;
		RoutingTableEntry entry; // = routingTable.getRoutingTableEntry(1);
		// RoutingTableEntry biggerEntry;
		for (long i = 1; i <= routingTable.getIntervalsNumber(); i++) { // TODO
																		// check
			entry = routingTable.getRoutingTableEntry(i);

			if (entry.getIntervalPointer() == null) {
				// only update if interval limit is > successor
				nextStartingId = routingTable.getStartingId(i + 1);
				if (nextStartingId != null
						&& !RingIntervals.belongsTo(nextStartingId, myId,
								ringState.successor.getId(), dksParameters.N,
								Bounds.OPEN_CLOSED)) {
				
					UpdateEntryEvent updateEvent = new UpdateEntryEvent(i);
					handleUpdateEntry(updateEvent);
				}

			} else {
				
				/*#%*/ log.debug("Time to check entry no " + i);
				UpdateEntryEvent updateEvent = new UpdateEntryEvent(i);
				handleUpdateEntry(updateEvent);

				//addNeighbor(entry.getIntervalPointer());
			}
		}

		// Scheduling next maintenance
		periodicMaintenanceTimerId = timerComponent.registerTimer(
				TopologyMaintenanceTimerExpiredEvent.class, "",
				PERIODIC_MAINTENANCE_TIMER);

	}

	/* Back list handler algorithms */

	public void addRoutingTablePointer(DKSRef pointer, RoutingTableEntry entry) {

		// log.debug("RT: " + routingTable.printRoutingTable());

		/*#%*/ log.debug("ADD POINTER for f(" + entry.getIntervalStartId() + ")="
		/*#%*/ 		+ pointer);

		if (entry.getIntervalPointer() != null) {
			/* If a better entry has been found */

			/* Remove from the list of neighbors */
			remNeighbor(entry.getIntervalPointer());

			/* Sending the REmEntry message to the old pointer */
			RemEntryMessage remEntry = new RemEntryMessage(false);
			send(remEntry, entry.getIntervalPointer());
		}

		/*#%*/ log.debug("SET Interval Pointer of f(" + entry.getIntervalNumber()
		/*#%*/ 		+ ")=" + entry.getIntervalStartId() + " to " + pointer);

		// synchronized (this) {
		/* Setting interval pointer */
		entry.setIntervalPointer(pointer);

		// }

		/* Adding to set of neighbors */
		addNeighbor(pointer);

		//System.out.println("Add back list pointer = " + pointer);

		/* Sending Request */
		AddBackListEntryMessage message = new AddBackListEntryMessage();
		send(message, pointer);

	}

	public void handleAddBackListEntry(DeliverMessageEvent event) {

		DKSRef source = event.getMessageInfo().getSource();

		//System.out.println("Add back list pointer received from = " + source);
		/*#%*/ log.debug("Add back list pointer received from = " + source);
		
		if (!backList.contains(source)) {
			backList.add(source);
		}

	}

	public void handleRemEntry(DeliverMessageEvent event) {

		DKSRef source = event.getMessageInfo().getSource();

		/* Removing from the backlist */
		backList.remove(source);

		/*#%*/ log.debug("RemEntry: removing " + source.getId() + " from backlist at "
		/*#%*/ 		+ myDKSRef.getId());

		if (!ringState.successorList.contains(source)
				&& !source.equals(ringState.successor))
			ringMaintainer.remNeighbor(source);

	}

	/*
	 * Lookup-related event handlers
	 */

	/**
	 * Handler for a reliable lookup request coming from the system
	 */
	public void handleReliableLookupRequest(ReliableLookupRequestEvent event) {

		processLookupRequestEvent(event, true);

	}

	/**
	 * Handler for an unreliable lookup request coming from the system
	 */
	public void handleUnreliableLookupRequest(UnreliableLookupRequestEvent event) {

		processLookupRequestEvent(event, false);

	}

	/**
	 * Processes the lookup request event according to the fact that is reliable
	 * or not
	 * 
	 * @param event
	 *            The {@link LookupRequestEvent} received by the component
	 * @param reliable
	 *            true if the lookup is reliable,false otherwise
	 */
	private void processLookupRequestEvent(LookupRequestEvent event,
			boolean reliable) {

		/*
		 * Generating identifier of the lookup, the identifier is unique with
		 * respect to the initiator
		 */
		long lookupId = lookupIdentifierSequenceGenerator
				.getNextSequenceNumber();

		BigInteger lookedUpId = event.getId();

		LookupStrategy strategy = event.getStrategy();

		Message operationMessage = event.getOperationRequestMessage();

		LookupOperationRequestMessage lookupRequestMessage = null;

		/*#%*/ log.debug("Received lookup EVENT request  with lookedup id "
		/*#%*/ 		+ lookedUpId + " and initiator " + myDKSRef);

		switch (strategy) {

		/* Constructing Lookup Request */

		case TRANSITIVE:

			if (event.hasOperation()) {

				lookupRequestMessage = new TransitiveLookupOperationRequestMessage(
						lookupId, lookedUpId, strategy, reliable, myDKSRef,
						operationMessage);

			} else {

				lookupRequestMessage = new TransitiveLookupOperationRequestMessage(
						lookupId, lookedUpId, strategy, reliable, myDKSRef);
			}

			if (reliable) {

				long timerId = timerComponent.registerTimer(
						TransitiveReliableLookupExpiredEvent.class, lookupId,
						TRANSITIVE_RELIABLE_LOOKUP_TIMER);

				/* The timerId is stored directly in the LookupRequest */
				lookupRequestMessage.setLookupTimerId(timerId);
			}

			break;

			/*#%*/ default:
			/*#%*/ 	log.debug("Strategy not implemented");
			/*#%*/ return;
		}

		// Keeping track of the lookup
		if (reliable) {

			/*
			 * The lookup request is stored in the reliable requests map until
			 * the reply comes
			 */
			reliablePendingLookups.put(lookupId, lookupRequestMessage);

		}

		/* If a specific event for carrying the result it's needed, store it */
		if (event.hasEventToIssue()) {

			specificLookupResultEvent.put(lookupId, event.getEventToIssue());

		}

		/* If I'm the only node in the ring */
		if (myDKSRef.equals(ringState.successor)) {

			respond(lookupId, lookedUpId, strategy, reliable, myDKSRef,
					myDKSRef);

		} else {

			if (RingIntervals.belongsTo(lookedUpId, myId, ringState.successor
					.getId(), dksParameters.N, Bounds.OPEN_CLOSED)) {

				respond(lookupId, lookedUpId, strategy, reliable,
						ringState.successor, myDKSRef);

			} else {

				DKSRef nextHopRef = routingTable.nextHop(event.getId());

				if (nextHopRef != null) {

					// // TODO: check more carefully, and add the missing bit to
					// // the table instead of just
					// // sneaking around it!
					// // Bugfix: it seems it only affects the last node on the
					// // ring:
					// if
					// (ringState.successor.getId().compareTo(myDKSRef.getId())
					// < 1) {
					// if (ringState.successor.getId().compareTo(
					// nextHopRef.getId()) < 1) {
					// // System.out.println(myRingId +" says: I'm changing the
					// // nextHop from "+nextHopRef.getId()+" to
					// // "+ringState.successor.getId());
					// nextHopRef = ringState.successor;
					// }
					// }
					/*#%*/ log.debug("Sending Lookup Request for " + event.getId()
					/*#%*/ 		+ " with lookupid " + lookupId + " to "
					/*#%*/ 		+ nextHopRef);

					send(lookupRequestMessage, nextHopRef);

				} else {

					if (ringState.successor != null) {

						/*#%*/ log.debug("Sending lookup with lookupid "
						/*#%*/ 		+ lookupRequestMessage.getLookupId()
						/*#%*/ 		+ " and initiator "
						/*#%*/ 		+ lookupRequestMessage.getInitiator()
						/*#%*/ 		+ " to my successor " + ringState.successor);
						/*
						 * The routing table might be empty because not yet
						 * initialized - route through my successor
						 */
						send(lookupRequestMessage, ringState.successor);

					} else {

						respond(lookupId, lookedUpId, strategy, reliable,
								myDKSRef, myDKSRef);

					}

				}

			}

		}

	}

	private void respond(long lookupId, BigInteger lookedUpId,
			LookupStrategy strategy, boolean reliable, DKSRef responsible,
			DKSRef initiator) {

		/* Build response */
		TransitiveLookupOperationResponseMessage response = new TransitiveLookupOperationResponseMessage(
				lookupId, lookedUpId, strategy, reliable, initiator,
				responsible);

		send(response, initiator);

	}

	/**
	 * Processes the {@link TransitiveLookupOperationRequestMessage}
	 * 
	 * @param event
	 *            The {@link DeliverMessageEvent} containing the
	 *            {@link LookupOperationRequestMessage}
	 */
	public void handleTransitiveLookupRequest(DeliverMessageEvent event) {

		LookupOperationRequestMessage request = (LookupOperationRequestMessage) event
				.getMessage();

		DKSRef initiator = request.getInitiator();
		long lookupId = request.getLookupId();
		BigInteger lookedUpId = request.getDestinationId();
		boolean reliable = request.isReliable();
		LookupStrategy strategy = request.getStrategy();

		/*#%*/ log.debug("Lookup request received with destID " + lookedUpId
		/*#%*/ 		+ " lookupid " + lookupId + " and initiator " + initiator);

		/* Get rid of duplicate lookup requests */
		if (seenLookups.containsKey(initiator)
				&& seenLookups.get(initiator).contains(lookupId)) {

			/*#%*/ log.debug("DUPLICATE LOOKUP " + lookupId + " FROM=" + initiator);

			NodeStatistics.duplicateLookups.incrementAndGet();
			return;

		}

		/* Keeping track of the seen lookups */
		addSeenLookup(request.getInitiator(), request.getLookupId());

		if (RingIntervals.belongsTo(lookedUpId, myId, ringState.successor
				.getId(), dksParameters.N, Bounds.OPEN_CLOSED)) {

			/*#%*/ log.debug("For my successor " + ringState.successor);

			/* My successor is the responsible */
			respond(lookupId, lookedUpId, strategy, reliable,
					ringState.successor, initiator);

		} else {

			DKSRef nextHopRef = routingTable.nextHop(lookedUpId);

			if (nextHopRef != null) {

//				if (ringState.successor.getId().compareTo(myDKSRef.getId()) < 1) { ?????
//					
//					if (ringState.successor.getId().compareTo(
//							nextHopRef.getId()) < 1) {
//						// System.out.println(myRingId +" says: I'm changing the
//						// nextHop from "+nextHopRef.getId()+" to
//						// "+ringState.successor.getId());
//						nextHopRef = ringState.successor;
//						// System.out.println("BUGFIX for "+lookupId);
//					}
//				}

				/*#%*/ log.debug("Sending Lookup Request with lookupid " + lookupId
				/*#%*/ 		+ " to " + nextHopRef);

				send(request, nextHopRef);


			} else {

				if (ringState.successor != null) {

					/*#%*/ log.debug("Sending lookup with lookupid " + lookupId
					/*#%*/ 		+ " and initiator " + initiator
					/*#%*/ 		+ " to my successor " + ringState.successor);
					/*
					 * The routing table might be empty because not yet
					 * initialized - route through my successor
					 */
					send(request, ringState.successor);

				} else {

					/*
					 * If no peers in the routing table
					 */
					respond(lookupId, lookedUpId, strategy, reliable, myDKSRef,
							initiator);

				}

			}

		}

	}

	/* ----------------------- */

	/**
	 * Handler for all the Lookup Responses
	 */
	public void handleLookupResponse(DeliverMessageEvent event) {

		LookupOperationResponseMessage response = (LookupOperationResponseMessage) event
				.getMessage();

		// System.out.println("GOT lookup response with responsible="
		// + response.getResponsibleNode() + " for identifier: "
		// + response.getId());

		long lookupId = response.getLookupId();

		LookupStrategy strategy = response.getStrategy();

		DKSRef initiator = response.getInitiator();

		if (reliablePendingLookups.containsKey(lookupId)) {

			/* At this point the response is arrived to the initiator */
			LookupOperationRequestMessage request = reliablePendingLookups
					.remove(lookupId);

			// Stats
			NodeStatistics.lookupSucceeded.incrementAndGet();

			switch (strategy) {
			case TRANSITIVE:

				if (initiator.equals(myDKSRef)) {

					/*#%*/ log.debug("Reliable transistive slookup response with lookup id="
					/*#%*/ 				+ response.getLookupId());

					/* Canceling timer upon receipt of the response */
					long timerId = request.getLookupTimerId();

					timerComponent.cancelTimer(timerId);

				} else {

					/* Response not for me, ignoring */
					return;

				}
				break;

			default:
				break;
			}
		}

		NodeStatistics.lookupSucceeded.incrementAndGet();

		/* Generation of a specific result event if needed */
		Event lookupResultEvent = null;

		if (specificLookupResultEvent.containsKey(lookupId)) {

			lookupResultEvent = createSpecificEvent(specificLookupResultEvent
					.get(lookupId), response.getId(), response
					.getResponsibleNode(), response.getOperationResultMessage());

		} else {

			lookupResultEvent = new LookupResultEvent(response.getId(),
					response.getResponsibleNode(), response
							.getOperationResultMessage());

		}

		trigger(lookupResultEvent);

	}

	/**
	 * Creates the specific Lookup result event previously given by the
	 * requester
	 * 
	 */
	private Event createSpecificEvent(Class eventClass, BigInteger id,
			DKSRef responsibleNode, Message operationReplyMessage) {

		Event event = null;
		try {

			Constructor<OperationRequestEvent> opEventConstructor = (eventClass
					.getConstructor(new Class[] { BigInteger.class,
							DKSRef.class, Message.class }));

			event = opEventConstructor.newInstance(new Object[] { id,
					responsibleNode, operationReplyMessage });

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return event;
	}

	/**
	 * Handles the expiration of a lookup, the old request is discarded and a
	 * new one is sent, the response for the old one will be ignored now on
	 * 
	 */

	public void handleTransitiveReliableLookupExpired(
			TransitiveReliableLookupExpiredEvent event) {

		long lookupId = (Long) event.getAttachment();

		/*
		 * Removing the lookup from the pending ones, if it comes back it will
		 * be ignored
		 */
		LookupOperationRequestMessage oldRequest = reliablePendingLookups
				.remove(lookupId);

		/*
		 * ignore timeout since ACK has removed the old outstanding request
		 */
		if (oldRequest == null)
			return;

		/*#%*/ String logMessage = "Lookup " + lookupId + " expired, resending.. ";
		//System.out.println(logMessage);
		/*#%*/ log.info(logMessage);

		/*
		 * A transitive lookup is always resent with a different lookuid when
		 * the timer expires
		 */
		ReliableLookupRequestEvent newRequest = new ReliableLookupRequestEvent(
				oldRequest.getDestinationId(), oldRequest.getStrategy(),
				oldRequest.getOperationMsg(), specificLookupResultEvent
						.remove(lookupId));

		newRequest.setResent(true);

		trigger(newRequest);
	}

	// public void handleCleanDuplicateLookupsTimerEvent(
	// CleanDuplicateLookupsTimerEvent event) {
	//
	// seenLookups.clear();
	//
	// timerComponent.registerTimer(CleanDuplicateLookupsTimerEvent.class,
	// null, CLEAN_DUPLICATE_LOOKUPS_TIMER);
	// }

	/**
	 * @param routingTable
	 */
	protected void registerRoutingTable(FingerRoutingTable routingTable) {
		this.routingTable = routingTable;

	}

	public String getRoutingTableStringRepresentation() {
		return routingTable.printRoutingTable();
	}

	public GenericRoutingTableInterface getRoutingTable() {
		return routingTable;
	}

	/* Helper functions */

	protected void send(Message message, DKSRef destination) {
		super.send(message, myDKSRef, destination);
	}

	protected boolean isRecursiveAndReliable(
			LookupOperationRequestMessage message) {
		if (message.getClass().equals(
				RecursiveLookupOperationRequestMessage.class)
				&& ((RecursiveLookupOperationRequestMessage) message)
						.isReliable())
			return true;
		return false;
	}

	protected boolean isRecursive(LookupOperationRequestMessage message) {
		return message.getClass().equals(
				RecursiveLookupOperationRequestMessage.class);
	}

	protected void push(LookupOperationRequestMessage request, DKSRef dksref) {
		((RecursiveLookupOperationRequestMessage) request).push(dksref);
	}

	/**
	 * @return Returns the backList.
	 */
	public List<DKSRef> getBackList() {
		return backList;
	}

	public void remNeighbor(DKSRef ref) {
		routingNeighbors.remove(ref);
	}

	public void addNeighbor(DKSRef ref) {
		if (!routingNeighbors.contains(ref)) {
			routingNeighbors.add(ref);
		}
	}

	/**
	 * @return Returns the routingNeighbors.
	 */
	public List<DKSRef> getRoutingNeighbors() {
		return routingNeighbors;
	}

	// public List<DKSRef> getNeighbors() {
	// return ringMaintainer.getNeighbors();
	// }

	public boolean isTopoloMaintenanceRunning() {
		return TOPOLOGY_MAINTENANCE_ACTIVATED;
	}

	public int getPendingReliableLookupsNumber() {
		return reliablePendingLookups.size();
	}

	public int getTraversingReliableRecursiveLookupsNumber() {
		// return sentReliableRecursiveLookupRequests.size()
		return reliablePendingLookups.size();
	}

	public int getPendingOperationsNumber() {
		return pendingOperations.size();
	}

	public void addSeenLookup(DKSRef initiator, long lookupId) {

		if (seenLookups.containsKey(initiator)) {

			seenLookups.get(initiator).add(lookupId);

		} else {

			Set<Long> set = new HashSet<Long>();
			set.add(lookupId);
			seenLookups.put(initiator, set);
		}

	}

}