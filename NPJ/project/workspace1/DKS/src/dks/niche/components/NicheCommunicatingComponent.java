/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.components;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.DirectIntervalAggregationMyValueEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastAckEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastDeliverEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.bcast.events.RecursiveIntervalAggregationMyValueEvent;
import dks.bcast.events.SimpleIntervalBroadcastStartEvent;
import dks.comm.CommunicatingComponent;
import dks.comm.SendJob;
import dks.comm.mina.CommunicationComponent;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.CommFailedEvent;
import dks.comm.mina.events.CommSendEvent;
import dks.comm.mina.events.CommSentEvent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.messages.Message;
import dks.niche.hiddenEvents.DeliverToManagementEvent;
import dks.niche.hiddenEvents.DeliverToNodeEvent;
import dks.niche.hiddenEvents.ManagementEvent;
import dks.niche.hiddenEvents.NicheBEBroadcastEvent;
import dks.niche.hiddenEvents.NicheBEBroadcastResultEvent;
import dks.niche.hiddenEvents.NichePRBroadcastEvent;
import dks.niche.hiddenEvents.NichePRBroadcastResultEvent;
import dks.niche.hiddenEvents.ReplyFromManagementEvent;
import dks.niche.hiddenEvents.SendRequestEvent;
import dks.niche.hiddenEvents.SendToIdAckEvent;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.NicheResponseMessageInterface;
import dks.niche.messages.RequestIdMessage;
import dks.niche.messages.SendToIdBulkMessage;
import dks.niche.messages.SendToIdMessage;
import dks.niche.messages.SendToIdResponseMessage;
import dks.niche.messages.SendToNodeMessage;
import dks.niche.messages.SendToNodeResponseMessage;
import dks.niche.wrappers.BroadcastContent;
import dks.niche.wrappers.ClassWrapper;
import dks.niche.wrappers.NicheSendClass;
import dks.niche.wrappers.NicheWaitClass;
import dks.niche.wrappers.SimpleResourceManager;
import dks.router.Router.LookupStrategy;
import dks.router.events.RemoveNodeEvent;
import dks.utils.IntervalsList;

/**
 * The <code>NicheInterfaceComponent</code> class
 * 
 * @author Joel
 * @version $Id: NicheInterfaceComponent.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheCommunicatingComponent extends CommunicatingComponent {

	/*#%*/ protected static Logger log = Logger.getLogger(CommunicationComponent.class);


	NicheManagementInterface niche;
	/*
	 * final String synchronousGetIndictator = "_synchronousGetHandler"; final
	 * String synchronousRemoveIndictator = "_synchronousRemoveHandler";
	 * 
	 * final String synchronousSendIndictator = "_synchronousSendHandler";
	 * 
	 * final String synchronousBEBIndictator = "_synchronousBEBHandler"; final
	 * String synchronousPRBIndictator = "_synchronousPRBHandler";
	 */


	final String defaultSendAckReceiver = "_defaultSendAckReceiver";

	final String defaultSendReceiver = "_defaultSendReceiver";

	final String defaultBroadcastResultReceiver = "_defaultBroadcastResultReceiver";

	final String defaultBroadcastReceiver = "_defaultBroadcastReceiver";

	private Integer defaultBroadcastTimeout;

	// String applicationBroadcastReceiveHandler = "receive"; String
	// applicationReceiveHandler = "receive";

	// final String synchronousRemoveHandler;

	public static final String CACHE_FLAG = System
			.getProperty("niche.cache.mode");

	public static final boolean CACHE_ENABLED =
		CACHE_FLAG instanceof String ?
				CACHE_FLAG.equals("1")
			:
				false
	;

	public static final int LOOKUP_QUEUE = 
		System.getProperty("niche.lookupQueue") instanceof String ?
				Integer.parseInt(System.getProperty("niche.lookupQueue"))
			:
				1
	;
				
	public static final int MAX_CONCURRENT_OPERATIONS = NicheSendClass.MESSAGE_QUEUE;

	
	//public static final int NO_ACKNOWLEDGEMENT_WANTED = -1;

	int currentPutOperation;

	int currentGetOperation;

	int currentRemoveOperation;

	int currentLookupAndSendOperation;

	int currentBroadcastOperation;

	static private HashMap<BigInteger, DKSRef> myCache;

	// NAMING SERVICE; WHERE???
	BigInteger myId;

	// Object dhtUser; Object sender; Object receiver;
	// Object broadcastInitiator; Object broadcastReceiver;
	// ClassWrapper defaultBroadcastResultReceiver;
	ConcurrentHashMap<String, ClassWrapper> applicationEventReceivers;

	// Remember, SendToIdMessages also go here, since they are extensions of
	// RequestIdMessages
	ConcurrentHashMap<Event, Object> myPendingRequests;
	NicheSendClass myMessageMonitor;
	HashMap<IoSession, NicheWaitClass> mySessionMonitors;
	

	ClassWrapper managementEventReceiver;

	ArrayList<Object> waitForPutAck = new ArrayList(MAX_CONCURRENT_OPERATIONS);

	ArrayList<Object> waitForGetResponse = new ArrayList(
			MAX_CONCURRENT_OPERATIONS);

	ArrayList<Object> waitForRemoveAck = new ArrayList(
			MAX_CONCURRENT_OPERATIONS);

	ArrayList<Object> waitForSendResponse = new ArrayList(
			MAX_CONCURRENT_OPERATIONS);

	ArrayList<Object> waitForBroadcastDelivery = new ArrayList(
			MAX_CONCURRENT_OPERATIONS);

//	ArrayList<SendClassInterface> mySendNotifyReceivers = new ArrayList(
//			MAX_CONCURRENT_OPERATIONS);

	ConcurrentHashMap<BigInteger, Vector<SendJob>> pendingLookups;

	// HashMap<String, SendClassInterface> mySendNotifyReceivers;

	ExecutorService myThreadPool;

	SimpleResourceManager myRM;

	DKSRef myRef;

	public NicheCommunicatingComponent(NicheManagementInterface niche,
			Scheduler scheduler, ComponentRegistry registry,
			ExecutorService threadPool) {
		
		super(scheduler, registry);

		this.niche = niche;
		// registry.getMarshalerComponent().registerMessageTypesTable(NicheMessageTable.INTERVAL_STARTING,
		// NicheMessageTable.class);

		myCache = new HashMap<BigInteger, DKSRef>();

		applicationEventReceivers = new ConcurrentHashMap<String, ClassWrapper>();
		myPendingRequests = new ConcurrentHashMap<Event, Object>();

		for (int i = 0; i < MAX_CONCURRENT_OPERATIONS; i++) {
			// initialize op-counters to avoid null
			waitForPutAck.add("");
			waitForGetResponse.add("");
			waitForRemoveAck.add("");
			waitForSendResponse.add("");
			waitForBroadcastDelivery.add("");
			
			//mySendNotifyReceivers.add(null);
			// saveForFailureHandling.add("");
		}

		pendingLookups = new ConcurrentHashMap<BigInteger, Vector<SendJob>>();

		// If 0, some errors might go undetected
		currentPutOperation = currentGetOperation = currentRemoveOperation = currentLookupAndSendOperation = currentBroadcastOperation = 5;

		myRef = registry.getRingMaintainerComponent().getMyDKSRef();
		myId = myRef.getId();
		myThreadPool = threadPool;
		myRM = niche.getResourceManager();
		//myMessageMonitor must be started!
		myMessageMonitor = new NicheSendClass(niche.getNicheAsynchronousSupport(), this);
		myRM.addSupportThread(myMessageMonitor);
		
		mySessionMonitors = new HashMap<IoSession, NicheWaitClass>();
		registerForEvents();
		registerConsumers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {

		/*
		 * Broadcast-related events
		 */

		// You should register for this event if you want to receive the ack and
		// the result of the pr-aggregation
		register(NichePRBroadcastResultEvent.class, "prbroadcastResultHandler");

		// You should register for this event if you want to receive
		// be-broadcasted messages
		register(NicheBEBroadcastEvent.class, "bebroadcastDeliverEventHandler");
		// You should register for this event if you want to receive
		// pr-broadcasted messages
		register(NichePRBroadcastEvent.class, "prbroadcastDeliverEventHandler");

		register(PseudoReliableIntervalBroadcastDeliverEvent.class,
				"receiveBroadcastHandler");
		register(PseudoReliableIntervalBroadcastAckEvent.class,
				"receiveBroadcastResultHandler");

		register(ReplyFromManagementEvent.class, "sendResponseHandler");

		register(SendRequestEvent.class, "sendRequestEventHandler");

		register(CommSentEvent.class, "sendNotifyHandler");
		register(CommFailedEvent.class, "sendFailedHandler");

		//register(NicheCommSentEvent.class, "sendNotifyHandler");

	}

	public void registerConsumers() {
		/*
		 * Name-based point-to-point sending events
		 */

		registerConsumer("receiveHandler", SendToIdMessage.class);
		registerConsumer("receiveHandler", SendToNodeMessage.class);

		registerConsumer("receiveBulkHandler", SendToIdBulkMessage.class);

		registerConsumer("responseHandler", SendToIdResponseMessage.class);

		registerConsumer("responseHandler", SendToNodeResponseMessage.class);

	}

	public synchronized void sendRequestEventHandler(SendRequestEvent e) {

		// _All_ niche initiated send-request will go through this method,
		// so here is the proper place to add any filtering one might want to
		// do,
		// and to set operation-ids for resending, and so on

		// FIXME: is the dest-id properly added??
		
		/*#%*/ String logMessage = "CC-sender got a request of type " + e.getType() + " with content " + e.getAttachedObject().getClass().getSimpleName();

		boolean directSend = false;
		
		NicheMessageInterface message;
		ManagementEvent event;
		
		BigInteger destinationId = e.getDestinationId(); 
		// int thisLookupAndSendOperation = -1;
		int thisLookupAndSendOperation = myRM.getNextLookupAndSendOperationIndex();
		
		if ((SendRequestEvent.SEND_TO_ID & e.getType()) != 0 ) {
			/*#%*/ logMessage += ". Sending to id = " + e.getDestinationId();
			message =  new SendToIdMessage(); 
			
		} else if ((SendRequestEvent.REQUEST_ID & e.getType()) != 0 ) {
			
			/*#%*/ logMessage += ". Requesting a stable id closest to = " + e.getDestinationNode();
			message =  new RequestIdMessage(
					destinationId, //FIXME njoeoeo
					e.getInitiator()
			);
			
			
		}
		else {
			/*#%*/ logMessage += ". Sending directly to node = " + e.getDestinationNode();
			message =  new SendToNodeMessage();
			message.setDestinationNode(e.getDestinationNode());
			directSend = true;
			
		}
		
		message.setDestinationId(destinationId);
		
		if ((SendRequestEvent.SEND_TO_MANAGEMENT & e.getType()) != 0 ) {
			event = new DeliverToManagementEvent(e.getAttachedObject()); 			
		} else {
			event = new DeliverToNodeEvent(e.getAttachedObject());
		}

//		case SendRequestEvent.REQUEST_ID:
//			log.debug("CC-sender got a request to find the responsible for "
//					+ destinationId);
//
//			message = new RequestIdMessage(
//						thisLookupAndSendOperation,
//						destinationId,
//						e.getInitiator()
//					);
//
//			break;

//		case (SendRequestEvent.REQUEST_MESSAGE | SendRequestEvent.SEND_TO_ID_RANGE):
//			log
//					.debug("CC-sender got a request to range-cast a message to symmetric neighbours");
//
//		default:
//			log.debug("Error, the send request didn't match existing types");
//			message = new SendToIdMessage();
//		}

		
		
		//OBS below we register the handler for the respons-message. this should
		//be checked to always match what happens on respons-message event
		if( (e.getType() & SendRequestEvent.REQUEST_MESSAGE) != 0) { 
			waitForSendResponse.set(
					thisLookupAndSendOperation,
					e.getInitiator()
			);
			event.setRequest(true);
		}
		
		//directSend = (e.getType() & SendRequestEvent.SEND_TO_NODE) != 0 ;
		
		message.setEvent(event);
		// saveForFailureHandling.set(thisOperation, message);

		/*#%*/ log.debug(logMessage);
		
		if ( (e.getType() & SendRequestEvent.SEND_TO_ID_RANGE) != 0) {

			PseudoReliableIntervalBroadcastStartEvent pseudoReliableIntervalBroadcastStartEvent = new PseudoReliableIntervalBroadcastStartEvent();
			pseudoReliableIntervalBroadcastStartEvent.setInfo(e.getInfo());
			trigger(pseudoReliableIntervalBroadcastStartEvent);

		} else {
			message.setMessageId(thisLookupAndSendOperation);
			message.setSource(myRef);
			
			SendJob sendJob = new SendJob(
										message,
										destinationId,
										e.getDestinationNode(),
										directSend,
										e.getMessageManager()
			);
			if(directSend) {
				trigger(new CommSendEvent((Message)message, e.getDestinationNode(), TransportProtocol.TCP, sendJob));
			} else {
				addMessageToQueueAndTriggerLookup(sendJob, destinationId);
			}
		}		
		
	}

	/*
	 * *********************************************************************************************************
	 * 
	 * The asynchronous dht-requests handlers
	 * 
	 */


	/*
	 * *********************************************************************************************************
	 * 
	 * Below follow code to
	 * enable simple access to multicast functionality
	 * 
	 */

	/*
	 * *********************************************************************************************************
	 * 
	 * Name-based communication related setters and getters
	 * 
	 */

	/**
	 * @return Returns the receiver.
	 */
	// public Object getReceiver() { return applicationEventReceivers.get(de); }
	/**
	 * @param receiverClass
	 *            The class of the receive-handler
	 * @param handlerMethod
	 *            The event handler method.
	 */
	public void setDefaultSendReceiver(Object receiverObject,
			String handlerMethod) {
		// For now, the use of a single applicationEventReceivers-tabel is is a
		// security breach, since a broadcast can end up in a one-to-one
		// receiver,
		// or vice versa, if the broadcast initiator addresses the message
		// (in)properly
		ClassWrapper t = new ClassWrapper(receiverObject, handlerMethod);
		applicationEventReceivers.put(defaultSendReceiver, t);
		applicationEventReceivers.put(receiverObject.getClass().getName()
				+ handlerMethod, t);
	}

	/**
	 * @param receiverClass
	 *            The instance of the receive-handler class
	 * @param handlerMethod
	 *            The event handler method.
	 */
	public void registerReceiver(Object receiverObject, String handlerMethod) {
		applicationEventReceivers.put(receiverObject.getClass().getName()
				+ handlerMethod,
				new ClassWrapper(receiverObject, handlerMethod));
	}

	public void registerManagementEventReceiver(Object receiverObject,
			String handlerMethod) {
		managementEventReceiver = new ClassWrapper(receiverObject,
				handlerMethod);
	}

	/**
	 * @param receiverClass
	 *            The instance of the receive-handler class
	 * @param handlerMethod
	 *            The event handler method.
	 */
	public void unregisterReceiver(Object receiverObject, String handlerMethod) {
		applicationEventReceivers.remove(receiverObject.getClass().getName()
				+ handlerMethod);
	}

	/**
	 * @param sendAckReceiver
	 *            The Default SendAckReceiver to set.
	 */
	public void setDefaultSendAckReceiver(Object sendAckReceiver, String m) {
		applicationEventReceivers.put(defaultSendAckReceiver, new ClassWrapper(
				sendAckReceiver, m));
	}

	/**
	 * @param sender
	 *            The broadcastResultReceiver to set.
	 */
	public void setDefaultBroadcastResultReceiver(Object handlerObject,
			String handlerMethod) {
		ClassWrapper t = new ClassWrapper(handlerObject, handlerMethod);
		applicationEventReceivers.put(defaultBroadcastResultReceiver, t);
		// A broadcast initiator might either specify the receiver as "default
		// receiver" or as "receiverClassName+receiverMethodName",
		// and both should work
		applicationEventReceivers.put(handlerObject.getClass().getName()
				+ handlerMethod, t);
	}

	/**
	 * @param receiver
	 *            The defaultBroadcastReceiver to register.
	 */
	public void setDefaultBroadcastReceiver(Object receiverObject,
			String handlerMethod) {
		ClassWrapper t = new ClassWrapper(receiverObject, handlerMethod);
		applicationEventReceivers.put(defaultBroadcastReceiver, t);
		applicationEventReceivers.put(receiverObject.getClass().getName()
				+ handlerMethod, t);
	}

	/**
	 * @param receiver
	 *            The broadcastReceiver to register.
	 */
	public void registerBroadcastReceiver(Object receiverObject,
			String handlerMethod) {
		applicationEventReceivers.put(receiverObject.getClass().getName()
				+ handlerMethod,
				new ClassWrapper(receiverObject, handlerMethod));

	}

	/**
	 * @param receiver
	 *            The broadcastReceiver to remove.
	 */
	public void unregisterBroadcastReceiver(Object receiverObject,
			String handlerMethod) {
		applicationEventReceivers.remove(receiverObject.getClass().getName()
				+ handlerMethod);

	}

	public void setDefaultBroadcastTimeout(int timeout) {
		defaultBroadcastTimeout = timeout;
	}

	public int getDefaultBroadcastTimeout() {
		return defaultBroadcastTimeout;
	}

	public void publicTrigger(Event event) {
		trigger(event);
	}
	
	public void addMessageToQueueAndTriggerLookup(
			SendJob sendJob, BigInteger destination) {

		if (destination.equals(myId)) {

			ManagementEvent e = ((SendToIdMessage) sendJob.getMessage()).getEvent();

				// TODO checkme
				if (e.isRequest()) {
					myPendingRequests.put(e, sendJob.getMessage());
				}

				myPendingRequests.put(e, (NicheMessageInterface) sendJob.getMessage());

				/*#%*/ log.debug("CC-sender says: Message "
				/*#%*/ 		+ e.getMessage().getClass().getSimpleName()
				/*#%*/ 		+ " wrapped in event " + e.getClass().getSimpleName()
				/*#%*/ 		+ " should go to MYSELF!");
				trigger(e);
				// Deliver locally!
				return;
			
		}

		if(LOOKUP_QUEUE == 0) {

			synchronized (pendingLookups) {
				
				Vector<SendJob> at = pendingLookups.get(destination);
				if (at == null) {
					at = new Vector<SendJob>();
					at.add(sendJob);
					pendingLookups.put(destination, at);
				} else {
					at.add(sendJob);
				}
			}
			triggerNicheLookupRequest(destination);

		} else {
			synchronized (pendingLookups) {
	
				Vector<SendJob> at = pendingLookups.get(destination);
				if (at == null) {
					at = new Vector<SendJob>();
					at.add(sendJob);
					pendingLookups.put(destination, at);
					triggerNicheLookupRequest(destination);
				} else {
					at.add(sendJob);
				}
			}
		}
		// triggerNicheLookupRequest(destination);

	}

	private void triggerNicheLookupRequest(BigInteger destinationId) {

		// check if destinationId is in my cache
		DKSRef node = myCache.get(destinationId);

		if (node != null && CACHE_ENABLED) {
			// if destinationId in cache then return the DKSRef
			SendToIdAckEvent e = new SendToIdAckEvent(destinationId, node, null);
			trigger(e);
			/*#%*/ log.debug("CACHE hit for ID=" + destinationId + " and DKSRef="
			/*#%*/ 		+ node);
		} else { // else go out and try to look it up :)
			/*#%*/ if (CACHE_ENABLED) {
			/*#%*/ 	log.debug("CACHE MISS for ID=" + destinationId);
			/*#%*/ }
			triggerReliableLookupRequest(destinationId,
					LookupStrategy.TRANSITIVE, null, SendToIdAckEvent.class,
					"handleLookupResponse");
		}
	}

	public void receiveBulkHandler(DeliverMessageEvent e) {
		SendToIdBulkMessage sendToIdBulkMessage = (SendToIdBulkMessage) e
				.getMessage();

		if (checkDestinationCorrectness((NicheMessageInterface) sendToIdBulkMessage)) {

			ArrayList<SendToIdMessage> msgs = sendToIdBulkMessage.getMessages();
			DKSRef source = sendToIdBulkMessage.getSource();
			for (SendToIdMessage message : msgs) {
				message.setSource(source);
				receiveHandler(message);
			}

		} // else { -the else is already handled by
		// checkDestinationCorrectness
	}

	public void receiveHandler(DeliverMessageEvent e) {

		if (checkDestinationCorrectness((NicheMessageInterface) e.getMessage())) {
			receiveHandler((NicheMessageInterface) e.getMessage());
		} // else { -the else is already handled by
		// checkDestinationCorrectness

	}

	private boolean checkDestinationCorrectness(
			NicheMessageInterface wrappedMessage) {

		
		if (!myRM.belongsToMe(wrappedMessage.getDestinationId())) {

			/*#%*/ String logMsg = "CC-receiver says: Got message ";
			
			/*#%*/ if (wrappedMessage instanceof SendToIdBulkMessage) {

				/*#%*/ logMsg += "SendToIdBulkMessage" + " which belongs to "
				/*#%*/ 		+ wrappedMessage.getDestinationId()
				/*#%*/ 		+ " and not to me. Throw it back to "
				/*#%*/ 		+ wrappedMessage.getSource()
				/*#%*/ 		+ " using originalMessageId "
				/*#%*/ 		+ wrappedMessage.getMessageId();

			/*#%*/ } else {

				/*#%*/ logMsg += wrappedMessage.getEvent().getMessage().getClass()
				/*#%*/ 		.getSimpleName()
				/*#%*/ 		+ " which belongs to "
				/*#%*/ 		+ wrappedMessage.getDestinationId()
				/*#%*/ 		+ " and not to me. Throw it back to "
				/*#%*/ 		+ wrappedMessage.getSource()
				/*#%*/ 		+ " using originalMessageId "
				/*#%*/ 		+ wrappedMessage.getMessageId();

			/*#%*/ }
			/*#%*/ log.debug(logMsg);
			/*#%*/ System.out.print("Return " + wrappedMessage.getMessageId() + " ");
			
			// SendClass(int operation, Message message, BigInteger
			// destinationId, DKSRef destination, boolean directMessage,
			// MessageManagerInterface messageManager)
			//myThreadPool.
			int operationId = myRM.getNextLookupAndSendOperationIndex();
			SendJob sendJob =
				new SendJob(
						new SendToNodeResponseMessage(
								operationId,
								wrappedMessage,
								SendToIdResponseMessage.FAILED_LOOKUP
						),
					wrappedMessage.getSource().getId(),
					wrappedMessage.getSource(),
					true,
					null
			);

			trigger(new CommSendEvent((Message)sendJob.getMessage(), wrappedMessage.getSource(), TransportProtocol.TCP, sendJob));
			return false;
		} 
		
		/*#%*/ String logMsg = "CC-receiver says: Got message to " + wrappedMessage.getDestinationId() + " with id " + wrappedMessage.getMessageId();
		/*#%*/ log.debug(logMsg);
		
		return true;

	}

	private void receiveHandler(NicheMessageInterface wrappedMessage) {

		ManagementEvent attachedEvent = wrappedMessage.getEvent();

		/*#%*/ log.debug("CC-receiver says: Got event "
		/*#%*/ 		+ wrappedMessage.getEvent().getClass().getSimpleName()
		/*#%*/ 		+ " with message "
		/*#%*/ 		+ attachedEvent.getMessage().getClass().getSimpleName()
		/*#%*/ 		+ " and id "
		/*#%*/ 		+ wrappedMessage.getMessageId()
		/*#%*/ 		+ " to "
		/*#%*/ 		+ wrappedMessage.getDestinationId()
		/*#%*/ );

		attachedEvent.setOperationId(wrappedMessage.getMessageId());
		if (attachedEvent.isRequest()) {

			/*#%*/ log.debug("CC-receiver says: store it, since acc wanted");
			myPendingRequests.put(attachedEvent, wrappedMessage);
		}
		attachedEvent.setSource(wrappedMessage.getSource());
		// please don't ask me to fix the serialization issues with dksref
		
		trigger(attachedEvent);

	}

	public void receiveBroadcastHandler(
			PseudoReliableIntervalBroadcastDeliverEvent deliverEvent) {
		
		ManagementEvent event = new DeliverToManagementEvent(deliverEvent.getInfo());
		event.setBroadcast(true);
		trigger(event);
		
	}

	public void receiveBroadcastResultHandler(
			PseudoReliableIntervalBroadcastAckEvent resultEvent) {
		ArrayList<Object> invalues = resultEvent.getValues();

		/*#%*/ log.debug("Received accs from symmetric neighbours. If results are aggregated as replies, they are shown here: " + invalues.size());
		
		for (Object object : invalues) {
			
			trigger((DeliverToManagementEvent) ((Serializable[]) object)[0]); // by
																		// construction
																		// a
																		// DeliverToManagementEvent!
		}

	}

	public void sendResponseHandler(ReplyFromManagementEvent e) {

		Object initialRequest = myPendingRequests.get(e.getOriginalMessage());

		if (initialRequest instanceof NicheMessageInterface) {

			/*
			 * Reply to the initiator
			 */

			// SendClass(int operation, Message message, BigInteger
			// destinationId, DKSRef
			// destination, boolean directMessage, MessageManagerInterface
			// messageManager)
			// FIXME: for now we assume a static receiver...
			//myThreadPool.
			
			int operationId = myRM.getNextLookupAndSendOperationIndex();
			SendJob sendJob = new SendJob(
					new SendToNodeResponseMessage(
							operationId,
							(NicheMessageInterface) initialRequest,
							e.getMessage()
					),
					null,
					((NicheMessageInterface) initialRequest).getSource(),
					true,
					null
			);
			/*#%*/ log.debug("Triggering reply to request which had id " + ((NicheMessageInterface) initialRequest).getMessageId()); 
			trigger(new CommSendEvent((Message)sendJob.getMessage(), ((NicheMessageInterface) initialRequest).getSource(), TransportProtocol.TCP, sendJob));
			
		} else {
			System.err.println("Unknown request type: "
					+ initialRequest.getClass().getSimpleName()
					+ " please implement");
		}
	}

	public synchronized void handleLookupResponse(SendToIdAckEvent e) {

		//myThreadPool.
		execute(new HandleLookupResponseClass(e));
		
	}

	/*
	 * *********************************************************************************************************
	 * 
	 * Event handlers for the response to the send event on the initiator side
	 * 
	 * 
	 */

	public void responseHandler(DeliverMessageEvent e) {

		
		/*#%*/ String logMessage = "NicheCommunicatingComponent-responseHandler says: ";
		
		NicheResponseMessageInterface rm = (NicheResponseMessageInterface) e
				.getMessage();

		int originalMessageId = rm.getOriginalMessageId();

		if (rm.failedLookup()) {

			/*#%*/ logMessage += "Message with original message id "
			/*#%*/ 				+ originalMessageId
			/*#%*/ 				+ " failed, retrieve send-manager from cache and retry";

			/*#%*/ log.debug(logMessage);
			myMessageMonitor.reportError(NicheSendClass.LOOKUP_FAILURE, originalMessageId); //to tell the send-manager an id-error occured.
			// thereafter:
			// Do The Right Thing (TM) - the responsibility of the send-manager
			//myThreadPool.
			//execute(sendManager);
			
		} else {
			
			/*#%*/ logMessage += "Received acc for message with original message id "
			/*#%*/ 	+ originalMessageId
			/*#%*/ 	+ ". Give respons to reply handler";
	
			/*#%*/ log.debug(logMessage);
			
			Object localHandler = waitForSendResponse.get(originalMessageId);

			
			// an asynchronous call was made, invoke the correct handler
			if(localHandler instanceof String) {
				//Scream
				System.err.println("This is ERROR! local handler for id " + originalMessageId + " was null");
				/*#%*/ log.debug("This is ERROR! local handler for id " + originalMessageId + " was null");
				
			} else {
				
				/*#%*/ logMessage =
				/*#%*/ 	"Received acc for message with original message id "
				/*#%*/ 	+ originalMessageId
				/*#%*/ 	+ ". Give respons to reply handler which has id "
				/*#%*/ 	+ ((NicheNotifyInterface)localHandler).getId();
		
				/*#%*/ log.debug(logMessage);
		
				((NicheNotifyInterface)localHandler).notify(rm.getMessage());
			}
		}
		
		
		//System.out.println(logMessage);

	}

	/*
	 * *********************************************************************************************************
	 * 
	 * Event handlers for the broadcast events issued by the application
	 * 
	 * Asynchronous
	 * 
	 */
	public void asynchronousBroadcast(Object message, IntervalsList receivers,
			boolean reliable, boolean aggregate, Integer timeout,
			String remoteSideHandlerClass, String remoteSideHandlerMethod,
			Object localSideHandlerObject, String localSideHandlerMethod) {

		if (localSideHandlerObject != null) {
			String t = localSideHandlerObject.getClass().getName()
					+ localSideHandlerMethod;
			internalAsynchronousBroadcast(message, receivers, reliable,
					aggregate, timeout, remoteSideHandlerClass,
					remoteSideHandlerMethod, t, new ClassWrapper(
							localSideHandlerObject, localSideHandlerMethod));
		} else {
			internalAsynchronousBroadcast(message, receivers, reliable,
					aggregate, timeout, remoteSideHandlerClass,
					remoteSideHandlerMethod, null, null);
		}

	}

	private int internalAsynchronousBroadcast(Object message,
			IntervalsList receivers, boolean reliable, boolean aggregate,
			Integer timeout, String remoteSideHandlerClass,
			String remoteSideHandlerMethod, String localSideHandlerId,
			ClassWrapper localSideHandler) {

		int thisOperation = currentBroadcastOperation;
		currentBroadcastOperation = (currentBroadcastOperation + 1)
				% MAX_CONCURRENT_OPERATIONS;

		String remoteSideHandlerId = remoteSideHandlerClass == null ? defaultBroadcastReceiver
				: remoteSideHandlerClass + remoteSideHandlerMethod;

		String broadcastResultReceiverId = localSideHandlerId;

		if (null == localSideHandler) {
			if (null == localSideHandlerId) {
				broadcastResultReceiverId = defaultBroadcastResultReceiver;
			}
			// else => this is a sync.call, the response should be handled
			// directly by the caller,
			// as indicated by eventName == synchronousRemoveIndictator
		} else {
			applicationEventReceivers.put(broadcastResultReceiverId,
					localSideHandler);
		}

		waitForBroadcastDelivery.set(thisOperation, broadcastResultReceiverId);

		IntervalBroadcastInfo info = new IntervalBroadcastInfo();
		info.setInterval(receivers);
		info.setMessage(new BroadcastContent(thisOperation, message,
				remoteSideHandlerId));
		info.setAggregate(aggregate);

		if (reliable) {
			PseudoReliableIntervalBroadcastStartEvent event = new PseudoReliableIntervalBroadcastStartEvent();
			info
					.setDeliverEventClassName(NichePRBroadcastEvent.class
							.getName());
			info.setAckAggrEventClassName(NichePRBroadcastResultEvent.class
					.getName());
			event.setInfo(info);
			trigger(event);
		} else {
			if (timeout != null) {
				info.setAggregationTimeout(timeout);
			}
			SimpleIntervalBroadcastStartEvent event = new SimpleIntervalBroadcastStartEvent();
			info
					.setDeliverEventClassName(NicheBEBroadcastEvent.class
							.getName());
			info.setAckAggrEventClassName(NicheBEBroadcastResultEvent.class
					.getName());
			event.setInfo(info);
			trigger(event);
		}
		return thisOperation;

	}

	/*
	 * *********************************************************************************************************
	 * 
	 * Event handlers for the events delivered at each node in the multicast
	 * group
	 * 
	 */

	public void bebroadcastDeliverEventHandler(NicheBEBroadcastEvent e) {
		internalBroadcastDeliverEventHandler(e);
	}

	public void prbroadcastDeliverEventHandler(NichePRBroadcastEvent e) {
		internalBroadcastDeliverEventHandler(e);
	}

	private void internalBroadcastDeliverEventHandler(Event e) {

		boolean reliable;

		IntervalBroadcastInfo i;
		if (e instanceof NichePRBroadcastEvent) {
			i = ((NichePRBroadcastEvent) e).getInfo();
			reliable = true;
		} else {
			i = ((NicheBEBroadcastEvent) e).getInfo();
			reliable = false;
		}

		// logger.debug("NIC-Debug: Delivered broadcast: "+i);

		BroadcastContent c = (BroadcastContent) i.getMessage();

		ClassWrapper t = applicationEventReceivers.get(c.getHandlerId());
		// System.out.println("NIC-internalBroadcastDeliverEventHandler says:
		// Handler " + c.getHandlerId() + " corresponds to handler object "+ t);
		try {

			if (i.getAggregate()) {
				// Then we expect a return value
				Object returnValue;
				// returnValue
				returnValue = t.getObject().getClass().getMethod(t.getMethod(),
						new Class[] { Object.class }).invoke(t.getObject(),
						c.getContent());

				if (reliable) {

					RecursiveIntervalAggregationMyValueEvent aggr = new RecursiveIntervalAggregationMyValueEvent();
					aggr.addValue(new BroadcastContent(c, returnValue));
					aggr.setInitiator(i.getInitiator());
					aggr.setInstanceId(i.getInstanceId());
					trigger(aggr);

				} else {// = pr

					DirectIntervalAggregationMyValueEvent aggr = new DirectIntervalAggregationMyValueEvent();
					aggr.addValue(new BroadcastContent(c, returnValue));
					aggr.setInitiator(i.getInitiator());
					aggr.setInstanceId(i.getInstanceId());
					trigger(aggr);

				}
			} else {

				t.getObject().getClass().getMethod(t.getMethod(),
						new Class[] { Object.class }).invoke(t.getObject(),
						c.getContent());
				// TODO fix
				// as of now, we still need the return event
				/*
				 * if(reliable) {
				 * 
				 * RecursiveIntervalAggregationMyValueEvent aggr = new
				 * RecursiveIntervalAggregationMyValueEvent(); aggr.addValue(new
				 * BroadcastContent(c, new Boolean(true)));
				 * aggr.setInitiator(i.getInitiator());
				 * aggr.setInstanceID(i.getInstanceId()); trigger(aggr); } else
				 * {//= best effort
				 * 
				 * DirectIntervalAggregationMyValueEvent aggr = new
				 * DirectIntervalAggregationMyValueEvent(); aggr.addValue(new
				 * BroadcastContent(c, new Boolean(true)));
				 * aggr.setInitiator(i.getInitiator());
				 * aggr.setInstanceID(i.getInstanceId()); trigger(aggr); }
				 */
			}

		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/*
	 * *********************************************************************************************************
	 * 
	 * Event handlers for the events returned by the broadcast component
	 * 
	 */

	public void bebroadcastResultHandler(NicheBEBroadcastResultEvent e) {
		internalBroadcastResultHandler(e);
	}

	public void prbroadcastResultHandler(NichePRBroadcastResultEvent e) {
		if (e.getAggregate()) {
			internalBroadcastResultHandler(e);
		}
		// else Do NOTHING (for now, at least) maybe later flag as ready to use
	}

	private void internalBroadcastResultHandler(Event e) {

		ArrayList wrappedResults;

		if (e instanceof NichePRBroadcastResultEvent) {
			wrappedResults = ((NichePRBroadcastResultEvent) e).getValues();
		} else {
			wrappedResults = ((NicheBEBroadcastResultEvent) e).getValues();
		}

		ArrayList finalResults = new ArrayList(wrappedResults.size());

		for (Object o : wrappedResults) {
			finalResults.add(((BroadcastContent) o).getContent());
		}

		int opId = ((BroadcastContent) wrappedResults.get(0)).getOperationId();
		String handlerMethodId = (String) waitForBroadcastDelivery.get(opId);

		// an asynchronous call was made, invoke the method provided
		// by the user

		try {
			ClassWrapper t = applicationEventReceivers.get(handlerMethodId);
			if (t != null) { // if == null, the initiator didn't want a
				// reply
				t.getObject().getClass().getMethod(t.getMethod(),
						new Class[] { ArrayList.class }).invoke(t.getObject(),
						finalResults);
			}

		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/*
	 * *********************************************************************************************************
	 * 
	 * Utility methods
	 * 
	 */

//	private void send(NicheMessageInterface message, DKSRef ref,
//			SendClassInterface sendManager) {
//
//		mySendNotifyReceivers.set(message.getMessageId(), sendManager);
//		
//		send((Message)message, myRef, ref, message.getMessageId(), TransportProtocol.TCP);
//
//	}
	
	
//	private void send(int thisOperation, Message message, DKSRef ref,
//			SendClassInterface sendManager) {
//
//		mySendNotifyReceivers.set(thisOperation, sendManager);
//		send(message, myRef, ref, thisOperation, TransportProtocol.TCP);
//
//	}
	
	public void sendFailedHandler(CommFailedEvent e) {
		myMessageMonitor.addJob(NicheSendClass.CONNECTION_CLOSED_FAILURE, (SendJob)e.getAttachment());
	}

	public synchronized void sendNotifyHandler(CommSentEvent e) {

		//if() //TODO
		//myThreadPool.
		WriteFuture writeFuture;
		SendJob sendJob = null;
	
		sendJob = (SendJob)e.getAttachment();
		
		if(sendJob.isLocalOperation()) {
			
			myMessageMonitor.addJob(NicheSendClass.SEND_SUCCESS, sendJob);
			
		} else {
			
			writeFuture = sendJob.getWriteFuture();
			IoSession currentSession = writeFuture.getSession();
			NicheWaitClass waitForChannel = null;
			if(mySessionMonitors.containsKey(currentSession)) {
				
				waitForChannel = mySessionMonitors.get(currentSession);
				mySessionMonitors.put(currentSession, waitForChannel);
				waitForChannel.addWaitJob(sendJob, writeFuture);

			} else {
				
				if(currentSession != null) {
					SocketAddress currentAddress = currentSession.getRemoteAddress();
					if(currentAddress != null) {
						waitForChannel = new NicheWaitClass(
								niche.getNicheAsynchronousSupport(),
								currentSession.getRemoteAddress().toString(),
								myMessageMonitor
						);
						mySessionMonitors.put(currentSession, waitForChannel);
						waitForChannel.addWaitJob(sendJob, writeFuture);
						execute(waitForChannel);
					} /*#%*/ else {
						/*#%*/ log.debug(
						/*#%*/ "Current session is "
						/*#%*/ + currentSession
						/*#%*/ + " but current address is null, "
						/*#%*/ + (sendJob.getMessage() == null ?
						/*#%*/		" and so is the attached message to be processed"
								:
						/*#%*/		" so message with id "
						/*#%*/ 		+ sendJob.getMessage().getMessageId()
						/*#%*/ 		+ " will NOT be handled! "
						/*#%*/ )
						/*#%*/ + "ERROR"
						/*#%*/ );
						/*#%*/}
				} /*#%*/ else {
					/*#%*/ log.debug(
					/*#%*/ 		"Current session is null, message with id "
					/*#%*/ + sendJob.getMessage().getMessageId()
					/*#%*/ + " will NOT be handled! ERROR"
					/*#%*/ );
					/*#%*/}
			
			}
		}
	}



//	private synchronized int getNextLookupAndSendOperationIndex() {
//
//		int thisOperation = currentLookupAndSendOperation;
//		currentLookupAndSendOperation = ((currentLookupAndSendOperation + 1)
//				% MAX_CONCURRENT_OPERATIONS) + 1; //OBS important,
//		//should not be 0 in order not to be confused with
//		//non-supervised operations
//		return thisOperation;
//
//	}

	class LookupSendClass implements Runnable {

		WriteFuture writeFuture;

		DKSRef contactedNode;

		LookupSendClass(WriteFuture writeFuture, DKSRef contactedNode) {
			this.writeFuture = writeFuture;
			this.contactedNode = contactedNode;
		}

		public void run() {

			writeFuture.awaitUninterruptibly();
			IoSession session = writeFuture.getSession();
			if (writeFuture.isWritten()) {
				/*#%*/ 	log.debug("LookupSendClass says: message to "
				/*#%*/ 			+ session.getRemoteAddress() + " written correctly");
			} else {
				/*#%*/ log.debug("LookupSendClass says: message to " + contactedNode
				/*#%*/ 		+ " NOT written correctly, session should be closed.");
				trigger(new RemoveNodeEvent(contactedNode));
				// trigger(new SessionGarbageCollectionEvent(session));

			}

		}

	}

	
	 class HandleLookupResponseClass implements Runnable {
	
		int LOOKUP_RETRY_DELAY = 1000;
		SendToIdAckEvent e;
		
		HandleLookupResponseClass(SendToIdAckEvent e) {
			this.e = e;
		}
		
		public void run() {
			
	
		DKSRef destinationNode = e.getResponsible();

		// add address node to cache, regardless:
		BigInteger lookedUpId = e.getLookedUpId();
		myCache.put(lookedUpId, destinationNode);

		// int thisSendOperation = getNextLookupAndSendOperationIndex();
		
		/*#%*/ String logMessage = "CC-lookupresulthandler says: I made a lookup for "
		/*#%*/ 					+ lookedUpId
		/*#%*/ 					+ " and I got back "
		/*#%*/ 					+ destinationNode
		/*#%*/ 					;

		if (destinationNode.equals(myRef)) {

			// typical race conditions go here:
			// the pred. might know that you are resp. but u might not
			// know it yourself. then you should pause / wait
			// TODO

			/*#%*/ logMessage += " so I'm the responsible node, ";
			
			if (myRM.belongsToMe(lookedUpId)) {

				
				Vector<SendJob> at = null;
				synchronized (pendingLookups) {
					at = pendingLookups.remove(lookedUpId);
				}

				if (at != null && 0 < at.size()) {
					
					/*#%*/ logMessage += " and I will process " + at.size() + " outstanding messages\n";
					Message msg;
					for (SendJob sj : at) {
						
						msg = (Message)sj.getMessage();
						
						if (msg instanceof SendToIdMessage) {

							SendToIdMessage m = (SendToIdMessage) msg;
							m.setSource(myRef);

							if (m.getEvent().isRequest()) {
								myPendingRequests.put(m.getEvent(), m);
							}
							/*#%*/ logMessage += "Delivering Message "
							/*#%*/ 		+ m.getEvent().getMessage().getClass()
							/*#%*/ 				.getSimpleName()
							/*#%*/ 		+ " to MYSELF!\n";
							
							//OBS OBS OBS - this must always go the exact same way as any
							//ordinary message would have arrived to the node!!
							receiveHandler(m); 
							//(delta the correctness check, which is already done)
							
						} else if (msg instanceof RequestIdMessage) {
							RequestIdMessage m = (RequestIdMessage) msg;
							
							m.getInitiator().notify(
									m.getResponse(destinationNode.getId())
							);
							
						} else if (msg instanceof SendToIdBulkMessage) {
							// yes, it can happen when nodes have failed
							receiveBulkHandler(
									new DeliverMessageEvent(
											msg,
											null,
											null
									)
								);
						}
					} // end for
				} /*#%*/ else  { // end if empty
				/*#%*/ 	logMessage += " but I had no outstanding messages\n";
				/*#%*/ }

			} else { // update not yet propagated.
				//sleep? and
				// re-trigger lookup
				/*#%*/ log.debug(
				/*#%*/ 		logMessage
				/*#%*/ 		+ " but due to slowness in the system, "
				/*#%*/ 		+ lookedUpId + " does not yet belong to me. So sleep " + LOOKUP_RETRY_DELAY + " and retry");
				
				try {
					Thread.sleep(LOOKUP_RETRY_DELAY);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				triggerReliableLookupRequest(lookedUpId,
						LookupStrategy.TRANSITIVE, null,
						SendToIdAckEvent.class, "handleLookupResponse");

			}

		} else {

			Vector<SendJob> at = null;
			synchronized (pendingLookups) {
				at = pendingLookups.remove(lookedUpId);
			}

			if (at != null && 0 < at.size()) {

				ArrayList<SendToIdMessage> sendToIdMessages = new ArrayList<SendToIdMessage>(
						at.size());

				/*#%*/ logMessage += " and I send " + at.size() + " outstanding messages\n";
				
				Message msg;
				SendJob singleJob = null;
				
				for (SendJob sj : at) {
					
					msg = (Message)sj.getMessage();
					if (msg instanceof SendToIdMessage) {

						SendToIdMessage m = (SendToIdMessage) msg;
						m.setSource(myRef);

						/*#%*/ logMessage +=
						/*#%*/ 		"Adding Message "
						/*#%*/ 		+ m.getEvent().getMessage().getClass().getSimpleName()
						/*#%*/ 		+ "\n"
						/*#%*/ 		;
						
						sendToIdMessages.add(m);
						singleJob = sj;

					} else if (msg instanceof RequestIdMessage) {

						RequestIdMessage m = (RequestIdMessage) msg;
						
						m.getInitiator().notify(m.getResponse(destinationNode.getId()));

					} else if (msg instanceof SendToIdBulkMessage) { // if it
						// has been looping due to some previous
						// send error!

						//myThreadPool.
						/*#%*/ logMessage +=
						/*#%*/ 	"Triggering resend of SendToIdBulkMessage with id "
						/*#%*/ 	+ ((SendToIdBulkMessage)msg).getMessageId()
						/*#%*/ 	+ "\n"
						/*#%*/ 	;
						trigger(new CommSendEvent(msg, destinationNode, TransportProtocol.TCP, sj));
						

					} else { // unknown type, error!
						System.err.println("unknown message type, error!");
						/*#%*/ log.debug("unknown message type, error!");
					}

				}

				// SendClass(int operation, Message message, BigInteger
				// destinationId, DKSRef destination, boolean directMessage,
				// MessageManagerInterface messageManager) {
				if (1 < sendToIdMessages.size()) {

					int thisSendOperation = myRM.getNextLookupAndSendOperationIndex();

					SendJob sendJob =
						new SendJob(
							(NicheMessageInterface)
								new SendToIdBulkMessage(
									thisSendOperation,
									lookedUpId,
									sendToIdMessages
							).setSource(myRef),
							lookedUpId,
							destinationNode,
							false,
							null
					);
			
					trigger(new CommSendEvent((Message)sendJob.getMessage(), destinationNode, TransportProtocol.TCP, sendJob));
					/*#%*/ logMessage += " Triggering CommSendEvent with id " + thisSendOperation;
					
				} else if (sendToIdMessages.size() == 1) {
					//reuse the sendJob we have already created:
					//Question: should we modify it, ie add the destinationNode to the job..?
					trigger(new CommSendEvent((Message)singleJob.getMessage(), destinationNode, TransportProtocol.TCP, singleJob));

				}/*#%*/  else {
				/*#%*/ 	logMessage += " but I have nothing to process!";
				/*#%*/ }

			}
		}
		/*#%*/ log.debug(logMessage);
		//System.err.println(logMessage);
	}
	}

}
