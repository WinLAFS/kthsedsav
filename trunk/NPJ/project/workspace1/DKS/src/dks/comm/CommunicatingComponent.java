/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm;

import java.math.BigInteger;

//import org.apache.mina.common.WriteFuture;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.CommSendEvent;
import dks.messages.DeliverMessage;
import dks.messages.Message;
import dks.operations.OperationAlreadyRegisteredException;
import dks.operations.OperationManagerComponent;
import dks.router.Router;
import dks.router.Router.LookupStrategy;
import dks.router.events.ReliableLookupRequestEvent;
import dks.router.events.UnreliableLookupRequestEvent;

/**
 * The <code>CommunicatingComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CommunicatingComponent.java 642 2008-09-05 12:57:27Z joel $
 */
public class CommunicatingComponent extends Component {

	public CommunicatingComponent(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerEvents()
	 */
	@Override
	protected void registerForEvents() {
	}

	protected void send(Message message, DKSRef source, DKSRef destination) {

		message.setSource(source);

		CommSendEvent commSendEvent = new CommSendEvent(message,destination,TransportProtocol.TCP);
		trigger(commSendEvent);
	}
	
//	protected void send(Message message, DKSRef source, DKSRef destination, int operationId, TransportProtocol protocol) {
//
//		message.setSource(source);
//
//		CommSendEvent commSendEvent = new CommSendEvent(message,destination, operationId, protocol);
//		trigger(commSendEvent);
//	}
	
	protected void send(Message message, DKSRef source, DKSRef destination, TransportProtocol protocol) {

		message.setSource(source);

		CommSendEvent commSendEvent = new CommSendEvent(message,destination, protocol);
		trigger(commSendEvent);
	}

	/**
	 * Routes a message to the responsible of the passed identifier
	 * 
	 * @param destinationId
	 *            The destination identifier
	 * @param message
	 *            The message to route
	 */
	protected void route(BigInteger destinationId, LookupStrategy strategy,
			Message message) {
		if (Router.routerUsed == Router.RouterType.FINGER_ROUTER) {
			DeliverMessage deliverMessage = new DeliverMessage(message);
			UnreliableLookupRequestEvent event = new UnreliableLookupRequestEvent(
					destinationId, strategy, deliverMessage);
			super.trigger(event);
		}
	}

	/**
	 * This method subscribes to an operation request in the
	 * {@link OperationManagerComponent} and to the event that will be generated
	 * when it's received
	 * 
	 * @param operationMessageClass
	 *            The {@link Class} of the operation message in which the
	 *            component is interested
	 * @param eventToIssue
	 *            The event to issue when the operation request is received.
	 *            (for passing it to the interested component)
	 * @param method
	 *            The handler of the Operation event
	 * @throws OperationAlreadyRegisteredException
	 *             thrown when more than an operation request is registered for
	 *             the same operation message
	 */

	protected void registerLookupOperation(Class operationMessageClass,
			Class eventToIssue, String method)
			throws OperationAlreadyRegisteredException {

		registry.getOperationManager().registerLookupOperation(
				operationMessageClass, eventToIssue);

		register(eventToIssue, method);
	}

	/**
	 * This method subscribes to an RPC operation request in the
	 * {@link OperationManagerComponent} and to the event that will be generated
	 * when it's received
	 * 
	 * @param operationMessageClass
	 *            The {@link Class} of the operation message in which the
	 *            component is interested
	 * @param eventToIssue
	 *            The event to issue when the operation request is received.
	 *            (for passing it to the interested component)
	 * @param handler
	 *            The handler of the Operation event
	 * @throws OperationAlreadyRegisteredException
	 *             thrown when more than an operation request is registered for
	 *             the same operation message
	 */

	protected void registerRPCOperation(Class operationMessageClass,
			Class eventToIssue, String handler)
			throws OperationAlreadyRegisteredException {

		// Register the handler of the event specified
		register(eventToIssue, handler);

		registry.getOperationManager().registerRPCOperation(
				operationMessageClass, eventToIssue);

	}

	/**
	 * This method initiates an unreliable lookup request operation for the
	 * responsible of the passed destination identifier.
	 * 
	 * @param destinationId
	 *            The destination identifier
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * @param requestMessage
	 *            The operation message request
	 * @param eventToIssue
	 *            The event to issue when the response for the operation is
	 *            received
	 * @param handler
	 *            The handler of the event carrying the response message
	 */
	protected void triggerUnreliableLookupRequest(BigInteger destinationId,
			LookupStrategy strategy, Message requestMessage,
			Class eventToIssue, String handler) {

		register(eventToIssue, handler);

		UnreliableLookupRequestEvent unreliableLookupRequestEvent = new UnreliableLookupRequestEvent(
				destinationId, strategy, requestMessage, eventToIssue);

		trigger(unreliableLookupRequestEvent);
	}

	/**
	 * This method initiates a reliable lookup request operation for the
	 * responsible of the passed destination identifier.
	 * 
	 * @param destinationId
	 *            The destination identifier
	 * @param strategy
	 *            The {@link LookupStrategy} used
	 * @param requestMessage
	 *            The operation message request
	 * @param eventToIssue
	 *            The event to issue when the response for the opearion is
	 *            received
	 * @param handler
	 *            The handler of the event carrying the response message
	 */
	protected void triggerReliableLookupRequest(BigInteger destinationId,
			LookupStrategy strategy, Message requestMessage,
			Class eventToIssue, String handler) {

		register(eventToIssue, handler);

		ReliableLookupRequestEvent reliableLookupRequestEvent = new ReliableLookupRequestEvent(
				destinationId, strategy, requestMessage, eventToIssue);

		trigger(reliableLookupRequestEvent);
	}
}
