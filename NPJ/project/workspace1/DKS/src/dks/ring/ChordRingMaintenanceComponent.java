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

import static dks.arch.HooksNumberTable.HOOK_LEAVE_AFTER_DONE;
import static dks.ring.RingMaintenanceConstants.JOIN_RETRY_TIMEOUT;
import static dks.ring.RingMaintenanceConstants.STABILIZATION_TIMER;
import static dks.ring.RingMaintenanceConstants.STAB_RPC_TIMEOUT;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.HooksRegistry;
import dks.arch.Scheduler;
import dks.boot.DKSWebCacheManager;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.comm.mina.events.SetConnectionStatusEvent;
import dks.fd.FailureDetectorMonitor;
import dks.fd.events.ReceivePongEvent;
import dks.fd.events.ReviseSuspicionEvent;
import dks.fd.events.StartMonitoringNodeEvent;
import dks.fd.events.StopMonitoringNodeEvent;
import dks.fd.events.SuspectEvent;
import dks.messages.JoinRequestMessage;
import dks.messages.JoinResponseMessage;
import dks.messages.Message;
import dks.messages.PredecessorLeaveMessage;
import dks.messages.RemEntryMessage;
import dks.messages.RingIdentifierAlreadyTakenMessage;
import dks.messages.StabGetPredecessorReqMessage;
import dks.messages.StabGetPredecessorRespMessage;
import dks.messages.StabGetSuccListReqMessage;
import dks.messages.StabGetSuccListRespMessage;
import dks.messages.StabNotifyMessage;
import dks.messages.SuccessorLeaveMessage;
import dks.ring.events.PredecessorChanged;
import dks.ring.events.RingJoinEvent;
import dks.ring.events.RingLastNodeEvent;
import dks.ring.events.RingLeaveDoneEvent;
import dks.ring.events.RingLeaveEvent;
import dks.ring.events.RingNodeJoinedEvent;
import dks.ring.events.RingRetryJoinEvent;
import dks.ring.events.RingStabRemoteException;
import dks.ring.events.RingStabilizeEvent;
import dks.router.events.InitRoutingTableEvent;
import dks.timer.TimerComponent;
import dks.utils.RingIntervals;
import dks.utils.RingIntervals.Bounds;

/**
 * The <code>RingMaintenanceComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingMaintenanceComponent.java 405 2007-08-17 16:39:54Z roberto $
 */
public class ChordRingMaintenanceComponent extends CommunicatingComponent
		implements RingMaintenanceComponentInt {

	/*#%*/ private static Logger log = Logger.getLogger(ChordRingMaintenanceComponent.class);

	private HooksRegistry hooksRegistry;

	private RingState ringState;

	private DKSRef n;

	private BigInteger nID;

	private TimerComponent timerComponent;

	private DKSParameters dksParameters;

	private DKSWebCacheManager dksWebCacheManager;

	/* TIMERS */

	// TimerID of the stabilization timer for stabilization scheduling
	private long stabilizationtimerID;

	// TimerID of the stabilization timer for RPC calls
	private long stabilizationRPCtimerID;

	private long retryJoinTimerId;

	// private int stabRetryCounter = 0;

	private boolean isStabilizationScheduled = false;

	private boolean isStabilizationActivated = true;

	// private OperationNumber currentOp;

	private long operationCounter;

	private List<DKSRef> ringNeighbors;

	private List<DKSRef> neighbors;

	private Set<DKSRef> permanentConnectionsToNeightbors;

	private Map<DKSRef, Set<OperationNumber>> seenJoinRequest;

	public ComponentRegistry registryRef;
	
	private long lastKnownFailTime;

	public ChordRingMaintenanceComponent(Scheduler scheduler,
			ComponentRegistry registry, DKSRef myDKSRef,
			DKSWebCacheManager dksCacheManager) {
		super(scheduler, registry);

		/*#%*/ log.debug("Ring Maintainer started for " + myDKSRef);

		super.getComponentRegistry().registerRingMaintainer(this);
		// My DKSRef
		this.n = myDKSRef;

		this.registryRef = registry;

		// MY ID
		this.nID = myDKSRef.getId();

		this.isStabilizationScheduled = false;

		// WebCache Address
		this.dksWebCacheManager = dksCacheManager;

		// Get the DKSParameters
		this.dksParameters = ComponentRegistry.getDksParameters();

		// Get the HooksRegistry
		this.hooksRegistry = registry.getHooksRegistry();

		// Initialize the status of the Ring
		this.ringState = new RingState(this, n);

		// Get the TimerComponent
		this.timerComponent = ComponentRegistry.getInstance()
				.getTimerComponent();

		this.ringNeighbors = new LinkedList<DKSRef>();

		this.neighbors = Collections.synchronizedList(new LinkedList<DKSRef>());

		// ringLeaveDoneInterceptorAckCounter = 0;

		// Register EventConsumers
		registerConsumers();

		// Register Subscription for the events
		registerForEvents();

		// // Register for RPC calls and Lookup operations
		// registerOperations();

		// Counter for the operations
		this.operationCounter = 0;

		this.permanentConnectionsToNeightbors = new HashSet<DKSRef>();

		this.seenJoinRequest = new HashMap<DKSRef, Set<OperationNumber>>();

	}

	protected void registerForEvents() {
		register(RingJoinEvent.class, "handleJoin", true);

		register(RingRetryJoinEvent.class, "handleRingRetryJoinEvent", true);
		register(RingStabilizeEvent.class, "handleStabStabilize", true);
		// register(CommConnBecomeTemporaryEvent.class,
		// "handleConnectionBecomeTemporary", true);
		register(RingStabRemoteException.class, "handleStabRemoteException",
				true);
		register(SuspectEvent.class, "handlePeerSuspectedEvent", true);
		register(ReviseSuspicionEvent.class, "handleReviseSuspicionEvent", true);

		// register(StopMonitoringNodeEvent.class,
		// "handleStopMonitoringNodeEvent");

	}

	/**
	 * Registers the EventConsumers in the EventConsumersRegistry
	 */
	private void registerConsumers() {

		// log.debug("Ring Maintainer of " + n + " - Registering consumers");

		/*
		 * Ring Maintenance messages
		 */
		registerConsumer("handleJoinReq", JoinRequestMessage.class);

		registerConsumer("handleJoinRes", JoinResponseMessage.class);

		registerConsumer("handleStabGetPredReq",
				StabGetPredecessorReqMessage.class);

		registerConsumer("handleStabGetPredResp",
				StabGetPredecessorRespMessage.class);

		registerConsumer("handleStabGetSuccListReq",
				StabGetSuccListReqMessage.class);

		registerConsumer("handleStabGetSuccListResp",
				StabGetSuccListRespMessage.class);

		registerConsumer("handleStabNotify", StabNotifyMessage.class);

		registerConsumer("handleSuccessorLeave", SuccessorLeaveMessage.class);

		registerConsumer("handlePredecessorLeave",
				PredecessorLeaveMessage.class);

		/*
		 * Exception messages
		 */
		registerConsumer("handleIdentifierAlreadyTaken",
				RingIdentifierAlreadyTakenMessage.class);

	}

	/*
	 * When a suspicion is revised the node notifies itself because the revised
	 * node could be its predecessor
	 */
	public void handleReviseSuspicionEvent(ReviseSuspicionEvent event) {
		DKSRef rectifiedPeer = event.getRectifiedPeer();
		/*#%*/ log.debug("handleReviseSuspicionEvent says: Peer with id "
		/*#%*/ 		+ rectifiedPeer.getId() 
		/*#%*/ 		+ "is rectified. My current predecessor is "
		/*#%*/ 		+ ringState.predecessor);

		
		if ( (ringState.predecessor == null && rectifiedPeer.equals(ringState.oldPredecessor) 
			
				||
				
			(ringState.predecessor != null && RingIntervals.belongsTo(
												rectifiedPeer.getId(),
												ringState.predecessor.getId(),
												nID,
												dksParameters.N,
												RingIntervals.Bounds.OPEN_CLOSED)
												))) {
			// The predecessor must be changed only if the lock is free
			if (!rectifiedPeer.equals(ringState.predecessor)) {
				updatePredecessor(rectifiedPeer); // TODO: check! the
				// rectified peer might be
				// on the other side of the
				// ring :@ !!!!!
			}/*#%*/  else {
			/*#%*/ 	log.debug("Cannot update the predecessor");
			/*#%*/ }
		}

		if (ringState.successor != null
				&& RingIntervals.belongsTo(rectifiedPeer.getId(), nID,
						ringState.successor.getId(), dksParameters.N,
						RingIntervals.Bounds.OPEN_CLOSED)) {
			ringState.successor = rectifiedPeer;
			/*#%*/ log
			/*#%*/ 		.debug("$$$$$ handleReviseSuspicionEvent: Updating successor: ringState.successor = "
			/*#%*/ 				+ ringState.successor);
			ringState.successorList.addNode(ringState.successor);
			ringState.successorList.truncate();
		}
		if (!permanentConnectionsToNeightbors.contains(rectifiedPeer)) {
			makePermanent(rectifiedPeer); // added by Joel & Ahmad
		}

	}

	public void handlePeerSuspectedEvent(SuspectEvent event) {

		String logMessage = "Node " + event.getSuspectedPeer() + " suspected";
		/*#%*/ log.info(logMessage);
		System.err.println(logMessage);

		DKSRef peerToRemove = event.getSuspectedPeer();
		remNeighbor(peerToRemove);
		registry.getRouterComponent().remNeighbor(peerToRemove); // Added by
		// joel

		// CheckPred Procedure
		if (peerToRemove.equals(ringState.predecessor)) {
			ringState.predecessor = null;
			/*#%*/ log.info("Predecessor died - Set the Predecessor to null");
		}
		/*
		 * Removing the disconnected peer from the successor's list, getting a
		 * new successor from the successor's list if the peer was my successor
		 */
		if (ringState.successorList.contains(peerToRemove)) {

			/*#%*/ log.info("Removing " + peerToRemove + " from successor list");

			ringState.successorList.remove(peerToRemove);

			/*#%*/ log.info(ringState.successorList.getSuccessorsList());

			// remNeighbor(peerToRemove);

			if (peerToRemove.equals(ringState.successor)) {
				DKSRef newSucc = getFirstAliveNode(ringState.successorList);

				/*#%*/ log.debug("Removing my successor, but how?? succ-list is:");

				if (newSucc != null) {
					/*
					 * "My successor will be notified that his predecessor has
					 * changed with the stabilization process"
					 * 
					 * - sure, but we need to speed this up!
					 */
					ringState.oldSuccessor = ringState.successor;
					ringState.successor = newSucc;
					//taking timestamp
					lastKnownFailTime = System.currentTimeMillis();
					/*#%*/ 	log.debug("$$$$$ peerSuspect: Updating successor from "
					/*#%*/ 		+ ringState.oldSuccessor + " to "
					/*#%*/ 		+ ringState.successor + " from successor list");
					
					//send(new StabGetPredecessorReqMessage(), ringState.successor); ?? maybe

				} else {
					/*
					 * The successorList is empty - it'll wait for a d to join
					 */
					// Reestablishing initial state
					ringState.successor = n;
					/*#%*/ log
					/*#%*/ 		.debug("$$$$$ peerSuspect: Updating successor: ringState.successor = Yourself!");

					/*#%*/ 	log.debug("SETTING SUCCESSOR TO MYSELF - PEER SUSPECTED");

					// Canceling scheduling of the stabilization
					timerComponent.cancelTimer(stabilizationtimerID);
					isStabilizationScheduled = false;

					// Cancelling timer of RPC calls
					timerComponent.cancelTimer(stabilizationRPCtimerID);

					/*#%*/ log
					/*#%*/ 		.info("Successor list empty - waiting for a successor to join the ring");
				}
			}
		} // end if (ringState.successorList.contains(peerToRemove))

		// remNeighbor(peerToRemove);
		permanentConnectionsToNeightbors.remove(peerToRemove);

	}

	/* Consumers event handlers */

	/* Handlers for the JOIN Algorithms */

	public void handleJoin(RingJoinEvent event) {

		if (ringState.status == RingStatus.INSIDE)
			return;

		DKSRef e;
		if (event.getNodeRef() != null) {
			// Request coming from the application
			e = event.getNodeRef();
		} else {
			// Request coming from the JoinRetry mechanism
			// (Resending JoinReq to the d which Retry is received from)
			e = (DKSRef) event.getAttachment();
		}

		if (e == null) {
			ringState.lockTaken = false;
			ringState.predecessor = null;
			ringState.successor = n;
			/*#%*/ log.debug("$$$$$ handleJoin: Updating successor: ringState.successor = "
			/*#%*/ 		+ ringState.successor);
			ringState.status = RingStatus.INSIDE;

			/*#%*/ log.info("Status set to " + RingStatus.INSIDE);

			// Resetting and updating the WEBCACHE
			dksWebCacheManager.reset();
			dksWebCacheManager.publishDKSRef(n);

			/*#%*/ log.info("First peer of the ring started");

			RingNodeJoinedEvent nodeJoinedEvent = new RingNodeJoinedEvent();
			trigger(nodeJoinedEvent);

		} else {

			// OperationCarrier number for the current Join OperationCarrier
			OperationNumber opNum = getNewOpNum();
			// currentOp = opNum;

			// ringState.lockTaken = true;
			ringState.predecessor = null;
			ringState.successor = null;
			/*#%*/ log.debug("$$$$$ handleJoin: Updating successor: ringState.successor = NULL");
			ringState.status = RingStatus.JOIN_REQ;

			/*#%*/ log.debug("Status set to: " + RingStatus.JOIN_REQ);

			/*#%*/ log.debug("Sending join request to " + e + " with q " + n
			/*#%*/ 		+ " and opNum " + opNum);

			retryJoinTimerId = timerComponent.registerTimer(
					RingRetryJoinEvent.class, null, JOIN_RETRY_TIMEOUT);

			// sendTo e.JoinReq(nID);
			JoinRequestMessage joinRequestMessage = new JoinRequestMessage(
					opNum, n);
			send(joinRequestMessage, e);

		}
	}

	public void handleJoinReq(DeliverMessageEvent event) {

		JoinRequestMessage joinReqMessage = (JoinRequestMessage) event
				.getMessage();

		DKSRef d = joinReqMessage.getNodeDKSRef();

		OperationNumber opNum = joinReqMessage.getOpNum();

		BigInteger dID = joinReqMessage.getNodeDKSRef().getId();

		/* Avoid bouncing of join requests */
		if (seenJoinRequest.containsKey(d)) {

			Set<OperationNumber> set = seenJoinRequest.get(d);

			if (set.contains(opNum)) {

				/* Discarding, the peer joining will retry */
				return;

			} else {

				set.add(opNum);

			}
		} else {

			Set<OperationNumber> set = new HashSet<OperationNumber>();

			set.add(opNum);

			seenJoinRequest.put(d, set);

		}

		/*
		 * s In the case the peer that wants to join has the same identifier of
		 * this peer, notify
		 */
		if (dID.equals(nID)) {
			if (d.equals(n)) {
				/*#%*/ log.debug("Received joinRequest from myself, ignoring");
				return;
			}
			// else:
			/*#%*/ log.debug("Received joinRequest with duplicate Id, reply that node cannot join");
			RingIdentifierAlreadyTakenMessage message = new RingIdentifierAlreadyTakenMessage();
			send(message, d);
			return;

		}

		/*#%*/ log.debug("JoinRequest with opnum " + opNum + " with d: " + d
		/*#%*/ 		+ " from " + event.getMessageInfo().getSource());

		/* If between me and my successor reply with my successor */
		if (ringState.successor != null
				&& RingIntervals.belongsTo(dID, nID, ringState.successor
						.getId(), dksParameters.N,
						RingIntervals.Bounds.OPEN_OPEN)) {

			JoinResponseMessage joinResponseMessage = new JoinResponseMessage(
					opNum, ringState.successor);

			/*#%*/ log.debug("Sending joinResponse message to " + d);

			send(joinResponseMessage, d);

			if (ringState.successor.equals(n)) {
				runStabilization();
				InitRoutingTableEvent initEvent = new InitRoutingTableEvent();
				trigger(initEvent);
			}

		}

		else {

			/*#%*/ log.debug("Forwarding Request if not for this peer,sending to "
			/*#%*/ 		+ ringState.successor);

			// sendTo succ.JoinReq(q)
			send(joinReqMessage, ringState.successor);

		}
	}

	public void handleJoinRes(DeliverMessageEvent event) {

		timerComponent.cancelTimer(retryJoinTimerId);

		if (ringState.status != RingStatus.INSIDE) {

			JoinResponseMessage joinResponseMessage = (JoinResponseMessage) event
					.getMessage();

			DKSRef d = joinResponseMessage.getNodeDKSRef();

			OperationNumber opNum = joinResponseMessage.getOpNum();

			/*#%*/ log.debug("JoinResponse with opnum " + opNum + " with d: " + d
			/*#%*/ 		+ " from " + event.getMessageInfo().getSource());

			ringState.successor = d;
			/*#%*/ log.debug("$$$$$ handleJoinRes: Updating successor: ringState.successor = "
			/*#%*/ 		+ ringState.successor);
			ringState.status = RingStatus.INSIDE;

			addNeighbor(ringState.successor);

			ringState.successorList.addNode(ringState.successor); // added by
			// J
			ringState.successorList.truncate(); // added by J

			// hooksRegistry.callHook(HOOK_JOIN_AFTER_POINT, new DKSRef[] { d
			// });

			RingNodeJoinedEvent nodeJoinedEvent = new RingNodeJoinedEvent();
			trigger(nodeJoinedEvent);

			dksWebCacheManager.publishDKSRef(n);

			runStabilization();

			InitRoutingTableEvent initEvent = new InitRoutingTableEvent();
			trigger(initEvent);
		} /*#%*/ else {
		/*#%*/ 	log.debug("JoinResponse from " + event.getMessageInfo().getSource()
		/*#%*/ 			+ " DUPLICATED, ignoring");
		/*#%*/ }
	}

	public void handleRingRetryJoinEvent(RingRetryJoinEvent event) {

		String rawDKSRef = dksWebCacheManager.getFirstDKSRef();
		DKSRef dksRef = null;

		try {
			dksRef = new DKSRef(rawDKSRef);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		RingJoinEvent ringJoinEvent = new RingJoinEvent(dksRef);

		trigger(ringJoinEvent);
	}

	/*
	 * Handler for the exception message received when an identifier is already
	 * taken
	 */
	public void handleIdentifierAlreadyTaken(DeliverMessageEvent event) {

		try {
			throw new IdentifierAlreadyTakenException("Identifier " + this.nID
					+ " already taken");
		} catch (IdentifierAlreadyTakenException e) {

			e.printStackTrace();

			/*#%*/ log.info("Stopping system due to duplicated identifier ");
			/*
			 * Shutting down everything
			 */
			registry.getScheduler().shutdown();
		}

	}

	/* Handlers for leave algorithm */

	public void handleLeave(RingLeaveEvent event) {

		OperationNumber opNum = getNewOpNum();
		// currentOp = opNum;

		ringState.status = RingStatus.LEAVE_REQ;

		/*#%*/ log.debug("handleLeave Status set to: " + RingStatus.LEAVE_REQ);

		if (ringState.successor.equals(ringState.predecessor)
				&& (ringState.successor.equals(n))) {

			/*#%*/ 	log.debug("handleReadyToLeave Last d: leaving");

		} else {

			// sendTo successor.PredecessorLeave(p)
			PredecessorLeaveMessage predecessorLeaveMessage = new PredecessorLeaveMessage(
					opNum, ringState.predecessor);
			send(predecessorLeaveMessage, ringState.successor);

			// sendTo predecessor.SuccessorLeave(s)
			SuccessorLeaveMessage successorLeaveMessage = new SuccessorLeaveMessage(
					opNum, ringState.successor);
			send(successorLeaveMessage, ringState.predecessor);

		}

		for (DKSRef ref : registry.getRouterComponent().getBackList()) {

			send(new RemEntryMessage(), ref);

		}

		RingLeaveDoneEvent leaveDoneEvent = new RingLeaveDoneEvent();
		trigger(leaveDoneEvent);

	}

	public void handleSuccessorLeave(DeliverMessageEvent event) {

		SuccessorLeaveMessage successorLeaveMessage = (SuccessorLeaveMessage) event
				.getMessage();

		/* Message sender */
		DKSRef sender = event.getMessageInfo().getSource();

		/* Message sender's successor */
		DKSRef s = successorLeaveMessage.getSuccessorDKSRef();

		// Putting the sender's connection temporary
		remNeighbor(sender);

		ringState.successorList.remove(sender);

		/* if sender was successor */
		if (ringState.successor.equals(sender)) {

			ringState.successor = s;
			/*#%*/ log.debug("$$$$$ handleSuccessorLeave: Updating successor: ringState.successor = "
			/*#%*/ 		+ ringState.successor);
			addNeighbor(ringState.successor);

		} else {

			/* If not, check if the sender's successor is a better successor */

			if (RingIntervals.belongsTo(s.getId(), nID, ringState.successor
					.getId(), dksParameters.N, RingIntervals.Bounds.OPEN_OPEN)) {

				ringState.successor = s;
				/*#%*/ 	log.debug("$$$$$ handleSuccessorLeave: Updating successor: ringState.successor = "
				/*#%*/ 			+ ringState.successor);
				// Putting the new successor's connection permanent
				addNeighbor(s);

			}

		}

		hooksRegistry.callHook(HOOK_LEAVE_AFTER_DONE, sender);

		/*#%*/ log.debug("Status set to: " + RingStatus.PRED_LEAVING);
	}

	public void handlePredecessorLeave(DeliverMessageEvent event) {

		PredecessorLeaveMessage successorLeaveMessage = (PredecessorLeaveMessage) event
				.getMessage();

		/* Message sender */
		DKSRef sender = event.getMessageInfo().getSource();

		/* Message sender's successor */
		DKSRef p = successorLeaveMessage.getPredecessorDKSRef();

		// Putting the sender's connection temporary
		remNeighbor(sender);

		ringState.successorList.remove(sender);

		/* if sender was predecessor */
		if (ringState.predecessor == null
				|| ringState.predecessor.equals(sender)) {

			updatePredecessor(p);

		} else {

			/* If not, check if the sender's predecessor is a better predecessor */

			if (RingIntervals.belongsTo(p.getId(), ringState.predecessor
					.getId(), nID, dksParameters.N,
					RingIntervals.Bounds.OPEN_OPEN)) {

				updatePredecessor(p);

				// Putting the new predecessor's connection permanent
				addNeighbor(p);

			}

		}

		hooksRegistry.callHook(HOOK_LEAVE_AFTER_DONE, sender);

		/*#%*/ log.debug("Status set to: " + RingStatus.PRED_LEAVING);
	}

	/* Handlers for the Stabilization algorithm */

	public void handleStabStabilize(RingStabilizeEvent event) {

		isStabilizationScheduled = false;

		// Setting Timer for RPC call

		/*#%*/ log.debug("Setting the timer for the RPC call");
		stabilizationRPCtimerID = timerComponent.registerTimer(
				RingStabRemoteException.class, ringState.successor,
				STAB_RPC_TIMEOUT);

		// succ.GetPredecessor()
		StabGetPredecessorReqMessage stabGetPredecessorReqMessage = new StabGetPredecessorReqMessage();
		send(stabGetPredecessorReqMessage, ringState.successor);
	}

	public void handleStabGetPredReq(DeliverMessageEvent event) {

		StabGetPredecessorRespMessage stabGetPredecessorRespMessage = new StabGetPredecessorRespMessage(
				ringState.predecessor);

		/*#%*/ log.debug("Get predecessor RPC call received - sending "
		/*#%*/ 		+ ringState.predecessor);

		send(stabGetPredecessorRespMessage, event.getMessageInfo().getSource());

	}

	public void handleStabGetPredResp(DeliverMessageEvent event) {
		// Cancel the timer for the RPC Call
		timerComponent.cancelTimer(stabilizationRPCtimerID);

		Message message = event.getMessage();
		StabGetPredecessorRespMessage predecessorRespMessage = (StabGetPredecessorRespMessage) message;

		DKSRef p = predecessorRespMessage.getPredecesor();

		/*#%*/ log.debug("RPC call response received with pred " + p);

		trigger(new ReceivePongEvent(message.getSource()));

		if (p != null && !p.equals(ringState.successor)) {

			if (RingIntervals.belongsTo(p.getId(), nID,
							ringState.successor.getId(), dksParameters.N,
							RingIntervals.Bounds.OPEN_CLOSED)) {

				boolean ignore = false;
				if (p.equals(ringState.oldSuccessor)) {
					long currentWait = System.currentTimeMillis() - lastKnownFailTime;
					if ( currentWait < STAB_RPC_TIMEOUT ) {
						/*#%*/ log.debug("$$$$$ handleStabGetPredResp: ignoring "
						/*#%*/ 		+ p
						/*#%*/ 		+ " as potentially crached peer for "
						/*#%*/ 		+ currentWait
						/*#%*/ 		+ " more ms"
						/*#%*/ );
						ignore = true;

					} else {
						ringState.oldSuccessor = null;
					}
				}
				
				if(!ignore) {
					/*#%*/ log.debug("$$$$$ handleStabGetPredResp: Updating successor from "
					/*#%*/ 				+ ringState.successor + " to " + p);
					ringState.successor = p;

					addNeighbor(ringState.successor);

					ringState.successorList.addNode(ringState.successor);
					ringState.successorList.truncate();

				}
				StabGetPredecessorReqMessage stabGetPredecessorReqMessage = new StabGetPredecessorReqMessage();
				// Added by A & J
				send(stabGetPredecessorReqMessage, ringState.successor);

			}
		}

		// Setting Timer for RPC call
		stabilizationRPCtimerID = timerComponent.registerTimer(
				RingStabRemoteException.class, ringState.successor,
				STAB_RPC_TIMEOUT);

		// succ.GetSuccList()
		StabGetSuccListReqMessage stabGetSuccListReqMessage = new StabGetSuccListReqMessage();
		send(stabGetSuccListReqMessage, ringState.successor);

	}

	public void handleStabGetSuccListReq(DeliverMessageEvent event) {

		StabGetSuccListRespMessage stabGetSuccListRespMessage;
		// return the alive nodes only

		stabGetSuccListRespMessage = new StabGetSuccListRespMessage(n,
				ringState.successorList.getSuccessorsList());

		send(stabGetSuccListRespMessage, event.getMessageInfo().getSource());

	}

	public void handleStabGetSuccListResp(DeliverMessageEvent event) {
		// Cancel the timer for the RPC Call
		timerComponent.cancelTimer(stabilizationRPCtimerID);

		StabGetSuccListRespMessage getSuccListRespMessage = (StabGetSuccListRespMessage) event
				.getMessage();
		List<DKSRef> slist = getSuccListRespMessage.getSuccessorsList();

		/*#%*/ log.debug("RPC call response received with SuccList "
		/*#%*/ 		+ slist.toString());

		// slist = succ + slist
		// tries to avoid putting dead guys back...
		// ringState.successorList.addAllBigger(slist, nID, dksParameters.N);
		ringState.successorList.addAll(slist);

		// succlist=trunc(slist,K)
		ringState.successorList.truncate();

		DKSRef e = null;
		if (ringState.successorList.getSuccessorsList().size() > 0) {

			e = ringState.successorList.getSuccessorsList().get(0);

			if ((!n.equals(ringState.successor))
					&& RingIntervals.belongsTo(e.getId(), ringState.successor
							.getId(), nID, dksParameters.N, Bounds.OPEN_OPEN)) {
				// ringState.successor = e;

				// addNeighbor(ringState.successor); //?
				/*#%*/ log.debug("It did happen");
			}
		}

		// succ.Notify(n)
		StabNotifyMessage stabNotifyMessage = new StabNotifyMessage(n);
		send(stabNotifyMessage, ringState.successor);

		/* Building the neighbors list */

		List<DKSRef> routingNeighbors = registry.getRouterComponent()
				.getRoutingNeighbors();

		neighbors.clear();

		synchronized (routingNeighbors) {

			neighbors.addAll(routingNeighbors);
			// log.debug("handleStabGetSuccListResp says: " +
			// routingNeighbors.toString())
			// stabRetryCounter = 0;
		}

		for (DKSRef neighbor : ringNeighbors) {
			if (!neighbors.contains(neighbor))
				neighbors.add(neighbor);
		}

		// ArrayList l1; // list with duplicates
		// ArrayList l2 = new ArrayList(new HashSet(l1)); // list without
		// duplicates

		/* Making connections with new neighbors permanent */

		for (DKSRef ref : neighbors) {

			if (!permanentConnectionsToNeightbors.contains(ref)) {

				makePermanent(ref);
				/* Start to monitor the neighbor node */
				StartMonitoringNodeEvent startMonitoringNodeEvent = new StartMonitoringNodeEvent(
						ref);
				trigger(startMonitoringNodeEvent);

			}

		}

		/* Making previous permanent connections temporary */
		Set<DKSRef> toBeTemporary = new HashSet<DKSRef>(
				permanentConnectionsToNeightbors);
		toBeTemporary.removeAll(neighbors);

		for (DKSRef ref2 : toBeTemporary) {

			if (!ref2.equals(ringState.successor)
					&& !ref2.equals(ringState.predecessor)) {

				/* Put the connection temporary */
				SetConnectionStatusEvent setEvent = new SetConnectionStatusEvent(
						ref2, TransportProtocol.TCP, false);
				trigger(setEvent);

				/* Stop monitoring the neighbor node */
				StopMonitoringNodeEvent stopMonitoringNodeEvent = new StopMonitoringNodeEvent(
						ref2);
				trigger(stopMonitoringNodeEvent);

				/*#%*/ log.debug("TEMPORARY TO: " + ref2);
			}
		}

		/* Intersecting permanent connections set with neighbors set */
		/*#%*/ log.debug("Removing those in "
		/*#%*/ 		+ permanentConnectionsToNeightbors.toString() + " but not in "
		/*#%*/ 		+ neighbors.toString());
		permanentConnectionsToNeightbors.retainAll(neighbors);

		// Scheduling the stabilization when the peer has finished the
		// previous stabilization
		scheduleStabilization();

	}

	public void handleStabNotify(DeliverMessageEvent event) {
		StabNotifyMessage stabNotifyMessage = (StabNotifyMessage) event
				.getMessage();
		DKSRef p = stabNotifyMessage.getP();

		// if (!ringState.lockTaken
		// && ((ringState.predecessor == null) || RingIntervals.belongsTo(
		// p.getId(), ringState.predecessor.getId(), nID,
		// dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED))) {

		if ((ringState.predecessor == null)
				|| RingIntervals.belongsTo(p.getId(), ringState.predecessor
						.getId(), nID, dksParameters.N,
						RingIntervals.Bounds.OPEN_CLOSED)) {

			remNeighbor(ringState.predecessor);

			updatePredecessor(p);

			addNeighbor(ringState.predecessor);

			scheduleStabilization();

		} /*#%*/ else {
		/*#%*/ 	log.debug("No need to update predecessor since " + p.getId() + " is not between " + ringState.predecessor
		/*#%*/ 			.getId() + " and "+ nID);
		/*#%*/ }

		/*
		 * if only two nodes in the ring, I'm my predecessor, set the successor =
		 * to predecessor
		 */
		if (ringState.successor.equals(n)) {
			ringState.successor = ringState.predecessor;
			/*#%*/ log.debug("$$$$$ handleStabNotify: Updating successor: ringState.successor = "
			/*#%*/ 		+ ringState.successor);
		}

	}

	public void handleStabRemoteException(RingStabRemoteException event) {
		/*#%*/ log.debug("Handling RingStabException");
		DKSRef succ = null;

		// Removing the Successor from the Successors list
		ringState.successorList.remove(ringState.successor); // FIXME we are
		// not sure if
		// we must
		// remove the
		// successor or
		// not!! what do
		// you think??

		if ((succ = getFirstAliveNode(ringState.successorList)) == null) {
			/*#%*/ log.debug("No alive nodes in the SuccessorList");
			if (ringState.predecessor == null) {
				// If the predecessor is null then probably the d is the
				// last
				// of the network
				RingLastNodeEvent ringLastNodeEvent = new RingLastNodeEvent();
				trigger(ringLastNodeEvent);
				timerComponent.cancelTimer(stabilizationRPCtimerID);
			} else {
				ringState.successor = n;
				/*#%*/ log.debug("$$$$$ handleStabRemoteException: Updating successor: ringState.successor = "
				/*#%*/ 				+ ringState.successor);
				/*#%*/ log.debug("SETTING SUCCESSOR TO MYSELF - REMOTE EXCEPTION");
				// Otherwise don't stabilize until a new d joins after you
			}
		} else {

			// stabRetryCounter = 0;

			/* Resetting timers */

			// Cancelling scheduling of the stabilization
			timerComponent.cancelTimer(stabilizationtimerID);
			isStabilizationScheduled = false;

			// Cancelling timer of RPC calls
			timerComponent.cancelTimer(stabilizationRPCtimerID);

			/* Resetting state */
			
			/*#%*/ log.debug("$$$$$ handleStabRemoteException: Updating successor from timeouted " + ringState.successor + 
			/*#%*/ 		" to " + succ);
			
			ringState.successor = succ;
			
			// ringState.lockTaken = false;

			// Running stabilization
			handleStabStabilize(null);
		}

	}

	/**
	 * Schedules a stabilization only if it's not already scheduled
	 */

	private void scheduleStabilization() {

		if (isStabilizationActivated) {

			if (isStabilizationScheduled) {
				/*#%*/ log.debug("Stabilizer: stabilization already scheduled");
			} else {
				/*#%*/ log.info("Scheduling stabilization");
				// Starting the timer for the stabilization
				stabilizationtimerID = timerComponent.registerTimer(
						RingStabilizeEvent.class, "", STABILIZATION_TIMER);
				isStabilizationScheduled = true;
			}
		}
	}

	/**
	 * Starts the stabilization process
	 */
	private void runStabilization() {
		if (isStabilizationActivated) {
			if (isStabilizationScheduled) {
				timerComponent.cancelTimer(stabilizationtimerID);
				isStabilizationScheduled = false;
			}
			handleStabStabilize(null);
		}
	}

	/**
	 * Remove the peer from the ordered list of neighbors (RT + SUCCLIST)
	 * 
	 * @param ref
	 *            The {@link DKSRef} of the peer to be removed
	 */
	public void remNeighbor(DKSRef ref) {

		if (ref != null) {
			ringNeighbors.remove(ref);

		}

	}

	/**
	 * Add the peer to the ordered list of neighbors (RT + SUCCLIST)
	 * 
	 * @param ref
	 *            The {@link DKSRef} of the peer to be added
	 */

	private void addNeighbor(DKSRef ref) {

		if (!ringNeighbors.contains(ref)) {
			ringNeighbors.add(ref);
		}
	}

	/**
	 * Gets the first alive d in the successor List
	 * 
	 * @param successorList
	 * @return
	 */
	private DKSRef getFirstAliveNode(SuccessorList successorList) {

		if (!successorList.isEmpty()) {
			return successorList.getSuccessorsList().get(0);

		}

		// if (!successorList.isEmpty()
		// && successorList.getSuccessorsList().size() > stabRetryCounter) {
		// //TODO: Check more carefully. Was <= (sic!)
		//
		// DKSRef newSuccessorRef = successorList.getSuccessorsList().get(
		// stabRetryCounter);
		//
		// stabRetryCounter++;
		//
		// return newSuccessorRef;
		//
		// }

		// stabRetryCounter = 0;
		return n;
	}

	private void makePermanent(DKSRef ref) {

		/*#%*/ log.debug("Issueing connection permanent to=" + ref);

		permanentConnectionsToNeightbors.add(ref);

		/* Setting connection permanent */
		SetConnectionStatusEvent setConnectionStatusEvent = new SetConnectionStatusEvent(
				ref, TransportProtocol.TCP, true);
		trigger(setConnectionStatusEvent);

		/*#%*/ log.debug("PERMANENT TO: " + ref);

	}

	private void updatePredecessor(DKSRef newPred) {

		ringState.oldPredecessor = ringState.predecessor;
		ringState.predecessor = newPred;

		// ringState.successorList.addIfSmallNode(ringState.predecessor);
		// //added by j
		// ringState.successorList.truncate(); //added by j
		/*#%*/ log.debug("setting pred to " + newPred);
		Event event = new PredecessorChanged(ringState.oldPredecessor, newPred);
		trigger(event);

	}

	private void send(Message message, DKSRef destination) {
		super.send(message, n, destination);
	}

	// /**
	// * If a {@link CommConnBecomeTemporaryEvent} is received checks if the
	// * connection to the peer whose connection is become temporary is still
	// * needed permanent
	// *
	// * @param event
	// * The {@link CommConnBecomeTemporaryEvent} event
	// */
	//
	// public void handleConnectionBecomeTemporary(
	// CommConnBecomeTemporaryEvent event) {
	// DKSRef peerOfConn = event.getPeerRef();
	// if (ringState.successorList.contains(peerOfConn)
	// || ringState.predecessor != null
	// && ringState.predecessor.equals(peerOfConn)) {
	// addNeighbor(peerOfConn);
	// }
	// }

	/**
	 * @return Returns the ringState.
	 */
	public RingState getRingState() {
		return ringState;
	}

	/**
	 * Returns if the stabilization is running or not
	 * 
	 * @return
	 */
	public boolean isStabilizationRunning() {
		return isStabilizationScheduled;
	}

	/**
	 * @return Returns the dksParameters.
	 */
	public DKSParameters getDksParameters() {
		return dksParameters;
	}

	/**
	 * Creates a new OperationNumber
	 * 
	 * @return The {@link OperationNumber}
	 */
	private OperationNumber getNewOpNum() {
		operationCounter++;
		return new OperationNumber(nID, operationCounter);
	}

	/**
	 * @return Returns the n.
	 */
	public DKSRef getMyDKSRef() {
		return n;
	}

	/**
	 * @return Returns the neighbors.
	 */
	// public List<DKSRef> getNeighbors() {
	//
	// return neighbors;
	// }
	/**
	 * @return Returns the registryRef.
	 */
	public ComponentRegistry getRegistry() {
		return registryRef;
	}

	//	
	// public void handleStopMonitoringNodeEvent(StopMonitoringNodeEvent event)
	// {
	// //J&A added
	//		
	// DKSRef node = event.getNode();
	//
	// if(ringState.successorList.remove(node)) {
	// log.info("Removing node " + node + " from successorlist");
	// }
	//		
	// }
	//	

}
