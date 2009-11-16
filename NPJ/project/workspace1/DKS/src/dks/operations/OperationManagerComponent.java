/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.operations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.messages.Message;
import dks.operations.events.LookupInternalOperationRequestEvent;
import dks.operations.events.LookupInternalOperationResponseEvent;
import dks.operations.events.OperationRequestEvent;
import dks.operations.events.OperationResponseEvent;
import dks.utils.LongSequenceGenerator;

/**
 * The <code>OperationManagerComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationManagerComponent.java 564 2008-02-27 17:07:12Z joel $
 */
public class OperationManagerComponent extends CommunicatingComponent {

	static final public boolean RPC_OPERATION = true;

	static final public boolean LOOKUP_OPERATION = false;

	/*#%*/ private static Logger log = Logger.getLogger(OperationManagerComponent.class);

	/*
	 * Map containing the operation's subscriptions. A subscription consists of
	 * a pair of Classes, one identifying the class of the operation message and
	 * the second identifying which event has to be issued for the operation to
	 * be performed.
	 */
	public HashMap<Class, Class> operationSubscriptions;

	/*
	 * Map containing the operations that have been triggered but not yet
	 * completed. A pending operation is identified by a number and a flag that
	 * indicates if the operation is a Lookup or an RPC operation
	 */
	public HashMap<Long, Boolean> pendingOperations;

	/*
	 * This sequence generator is used to assign unique identifiers to RPC calls
	 */
	private LongSequenceGenerator sequenceGenerator;

	private DKSRef myDKSref;

	/**
	 * Constructs the {@link OperationManagerComponent} which will manage all
	 * lookup operations and RPC calls of the system
	 * 
	 * @param scheduler
	 *            The instance of the {@link Scheduler}
	 * @param registry
	 *            The instance of the {@link ComponentRegistry}
	 * @param sequenceGenerator
	 */
	public OperationManagerComponent(Scheduler scheduler,
			ComponentRegistry registry, DKSRef myDKSRef,
			LongSequenceGenerator sequenceGenerator) {
		super(scheduler, registry);

		this.myDKSref = myDKSRef;

		operationSubscriptions = new HashMap<Class, Class>();

		pendingOperations = new HashMap<Long, Boolean>();

		this.sequenceGenerator = sequenceGenerator;

		registerForEvents();

		super.getComponentRegistry().registerOperationManager(this);

	}

	protected void registerForEvents() {
		register(LookupInternalOperationRequestEvent.class,
				"handleLookupOperationRequest");
		register(OperationResponseEvent.class, "handleOperationResponse");
	}

	/**
	 * Registers a lookup operation that has to be handled by the operation
	 * manager
	 * 
	 * @param lookupOperationMessageClass
	 *            The class of the operation message to subscribe to
	 * 
	 * @param eventToIssue
	 *            The event to issue when the operation message has been
	 *            received
	 * @throws OperationAlreadyRegisteredException
	 */
	public void registerLookupOperation(Class lookupOperationMessageClass,
			Class eventToIssue) throws OperationAlreadyRegisteredException {

		if (!operationSubscriptions.containsKey(lookupOperationMessageClass)) {
			operationSubscriptions.put(lookupOperationMessageClass,
					eventToIssue);
		} else {
			Class previousEventToIssue = operationSubscriptions
					.get(lookupOperationMessageClass);
			/**
			 * If it's the same subscription it's ok because probably means that
			 * an RPC registration has been already made for the same operation
			 */
			if (!previousEventToIssue.equals(eventToIssue))
				throw new OperationAlreadyRegisteredException(
						"The subscription for this operation message has already been made");
		}
	}

	/**
	 * Registers for an RPC operation request that has to be handled by the
	 * OperationManager. The subscription consists in registering a Consumer for
	 * the message associated with the RPC call. An event, specified in the
	 * subscription and to which the interested component must subscribe, will
	 * be issued when the message is received.
	 * 
	 * @param operationMessageClass
	 *            The Class of the RPC operation
	 * @param eventToIssue
	 *            The Event to issue when the message is received
	 * @throws OperationAlreadyRegisteredException
	 */
	public void registerRPCOperation(Class rpcOperationMessageClass,
			Class eventToIssue) throws OperationAlreadyRegisteredException {

		// int messageTypeInt = -1;
		// if ((messageTypeInt = MessageTypeTable
		// .getMessageType(rpcOperationMessageClass)) == -1) {
		// log
		// .debug("Message type cannot be resolved for this OperationCarrier
		// Message");
		// }

		registerConsumer("handleRPCOperation", rpcOperationMessageClass);

		if (!operationSubscriptions.containsKey(rpcOperationMessageClass)) {
			operationSubscriptions.put(rpcOperationMessageClass, eventToIssue);
		} else {
			Class previousEventToIssue = operationSubscriptions
					.get(rpcOperationMessageClass);
			/**
			 * If it's the same subscription it's ok because probably means that
			 * an RPC registration has been already made for the same operation
			 */
			if (!previousEventToIssue.equals(eventToIssue))
				throw new OperationAlreadyRegisteredException(
						"The subscription for this operation message has already been made");
		}

	}

	/**
	 * Handler of the RPC operation requests. An RPC operation message request
	 * is received and the corresponding event, for passing it to the right
	 * component, is issued.
	 * 
	 */
	public void handleRPCOperation(DeliverMessageEvent deliverEvent) {

		Class operationMessageClass = deliverEvent.getMessage().getClass();

		if (operationSubscriptions.containsKey(operationMessageClass)) {

			/*
			 * For uniquely identifying an operation in the system we use a long
			 * value
			 */
			long operationId = sequenceGenerator.getNextSequenceNumber();

			// Keeping track of the operation
			pendingOperations.put(operationId, RPC_OPERATION);

			DKSRef operationSource = deliverEvent.getMessageInfo().getSource();

			Message operationMessage = deliverEvent.getMessage();

			/*
			 * The event registered by the application requesting the operation
			 * is created with the right parameters
			 */
			Event event = createEvent(operationMessageClass, operationId,
					operationSource, operationMessage);

			trigger(event);
		}/*#%*/  else {
		/*#%*/ log.info("No subscription for this RPC operation request");
		/*#%*/ }

	}

	/**
	 * Creates the event to deliver the operation to the component that
	 * subscribed for it
	 * 
	 */
	private Event createEvent(Class operationMessageClass, long operationId,
			DKSRef operationSource, Message operationMessage) {
		Class eventClass = operationSubscriptions.get(operationMessageClass);

		Event event = null;
		try {

			Constructor<OperationRequestEvent> opEventConstructor = (eventClass
					.getConstructor(new Class[] { DKSRef.class, Long.TYPE,
							Message.class }));

			event = opEventConstructor.newInstance(new Object[] {
					operationSource, operationId, operationMessage });

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return event;
	}

	/**
	 * Handler for the Lookup operation requests. Same mechanism of the RPC
	 * handler
	 * 
	 */
	public void handleLookupOperationRequest(
			LookupInternalOperationRequestEvent lookupInternalOperationRequestEvent) {

		Class operationMessageClass = lookupInternalOperationRequestEvent
				.getOperationMessage().getClass();
		// sSystem.out.println(lookupInternalOperationRequestEvent.getOperationMessage().getClass());

		if (operationSubscriptions.containsKey(operationMessageClass)) {

			/*
			 * For uniquely identifying an operation in the system we use the
			 * long value generated in the Router
			 */
			long operationId = lookupInternalOperationRequestEvent
					.getOperationId();

			// Keeping track of the operation
			pendingOperations.put(operationId, LOOKUP_OPERATION);

			/*
			 * The event registered by the application requesting the operation
			 * is created and an OperationCarrier object is attached to it
			 */
			Event event = createEvent(operationMessageClass, operationId,
					lookupInternalOperationRequestEvent.getInitiator(),
					lookupInternalOperationRequestEvent.getOperationMessage());

			trigger(event);
		} /*#%*/ else {
		/*#%*/ log.info("No subscription for this Lookup operation request");
		/*#%*/ }

	}

	public void handleOperationResponse(
			OperationResponseEvent operationResponseEvent) {

		long operationId = operationResponseEvent.getRequestEvent()
				.getOperationId();

		if (pendingOperations.containsKey(operationId)) {

			boolean isRPCCall = pendingOperations.get(operationId);

			pendingOperations.remove(operationId);

			if (isRPCCall) {

				DKSRef source = operationResponseEvent.getRequestEvent()
						.getSource();

				Message operationReplyMessage = operationResponseEvent
						.getOperationReplyMessage();
				/*
				 * If it's a reply to a RPC call send it directly to the peer
				 * that requested it
				 */

				super.send(operationReplyMessage, myDKSref, source);

			} else {
				// TODO
				/*
				 * If it's a lookup operation, send the response to the
				 * initiator (Create an event with the operation result and give
				 * it to the router)
				 */

				LookupInternalOperationResponseEvent event = new LookupInternalOperationResponseEvent(
						operationResponseEvent);

				trigger(event);

			}

		} /*#%*/ else {
		/*#%*/ log.debug("The operation doesn't exist");
		/*#%*/ }

	}

}

// class PendingOperationEntry{
//	
// PendingOperationEntry(long ){
//		
// }
// }
