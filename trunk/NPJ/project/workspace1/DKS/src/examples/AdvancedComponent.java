///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package examples;
//
//import java.math.BigInteger;
//
//import dks.addr.DKSRef;
//import dks.arch.ComponentRegistry;
//import dks.arch.Scheduler;
//import dks.comm.CommunicatingComponent;
//import dks.operations.OperationAlreadyRegisteredException;
//import dks.operations.events.OperationResponseEvent;
//import dks.router.Router.LookupStrategy;
//import examples.events.ExampleOperationRequestEvent;
//import examples.events.ExampleOperationResponseEvent;
//import examples.messages.ComponentSpecificMessageTable;
//import examples.messages.ExampleLookupOperationRequestMessage;
//import examples.messages.ExampleLookupOperationResponseMessage;
//
///**
// * The <code>AdvancedComponent</code> class This is an example of an advanced
// * component, it is able to issue lookup operation requests, to route a message
// * to the responsible of an identifier and to handle lookup operations.
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: AdvancedComponent.java 641 2008-09-03 12:14:42Z joel $
// */
//public class AdvancedComponent extends CommunicatingComponent {
//
//	public AdvancedComponent(Scheduler scheduler, ComponentRegistry registry) {
//		super(scheduler, registry);
//
//		/*
//		 * If new types of messages must be added, create a table like the
//		 * "ComponentSpecificMessageTable" in the example and registers it as
//		 * following with the interval start of the message types assigned to
//		 * the component (for informations about which intervals are assigned to
//		 * who please look at the WIKI)
//		 */
//
//		registry.getMarshalerComponent().registerMessageTypesTable(9000,
//				ComponentSpecificMessageTable.class);
//
//		registerForEvents();
//
//		registerLookupOperations();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see dks.comm.CommunicatingComponent#registerForEvents()
//	 */
//	@Override
//	protected void registerForEvents() {
//
//	}
//
//	private void registerLookupOperations() {
//		try {
//
//			/*
//			 * A registration for receiving a lookup operation is done by
//			 * passing the class of the lookup operation message, the event that
//			 * will carry that operation and its handler (The registration for
//			 * the event is already done by the method), note that the
//			 * ExampleOperationRequestEvent must extend the abstract Event
//			 * OpearationRequestEvent
//			 */
//			registerLookupOperation(ExampleLookupOperationRequestMessage.class,
//					ExampleOperationRequestEvent.class,
//					"handleExampleOperationRequest");
//
//		} catch (OperationAlreadyRegisteredException e) {
//
//			/*
//			 * The exception is thrown when there already exists another
//			 * registration for the same Lookup operation
//			 */
//			e.printStackTrace();
//		}
//	}
//
//	public void handleExampleOperationRequest(
//			ExampleOperationRequestEvent requestEvent) {
//
//		ExampleLookupOperationRequestMessage operationMessage = (ExampleLookupOperationRequestMessage) requestEvent
//				.getOperationMessage();
//
//		/*
//		 * Informations that you might need
//		 */
//		@SuppressWarnings("unused")
//		DKSRef initiator = requestEvent.getSource();
//
//		/*
//		 * The operationId is unique in the system (RPC operations and Lookup
//		 * operations share the same operation id generator)
//		 */
//		@SuppressWarnings("unused")
//		long operationId = requestEvent.getOperationId();
//
//		// Process the operation request....
//
//		int operationValue = operationMessage.getOperationValue();
//
//		operationValue++;
//
//		/*
//		 * To reply to the initiator
//		 */
//
//		ExampleLookupOperationResponseMessage responseMessage = new ExampleLookupOperationResponseMessage(
//				operationValue);
//
//		/*
//		 * Trigger the LookupInternalOperationResponseEvent passing the response
//		 * message and the request event previously received
//		 */
//		OperationResponseEvent operationResponseEvent = new OperationResponseEvent(
//				requestEvent, responseMessage);
//
//		trigger(operationResponseEvent);
//
//		/* ------------------------------------------ */
//		/*
//		 * To initiate a lookup operation request for the responsible of a
//		 * specific id, the following steps must be done
//		 */
//
//		/* the destination identifier of the lookup operation */
//		BigInteger destinationId = BigInteger.ONE;
//
//		/* the operation message request */
//		ExampleLookupOperationRequestMessage request = new ExampleLookupOperationRequestMessage(
//				12);
//
//		/*
//		 * Then one of this events must be triggered. Both needs the destination
//		 * identifier, the strategy to use for the lookup (Currently only
//		 * TRANSITIVE and RECURSIVE are implemented), the operation message
//		 * request, the event to be issued when the operation response comes
//		 * back and the handler of the latter. Note that the
//		 * ExampleOperationResponseEvent must extend the Event LookupResultEvent
//		 */
//		/*
//		 * For an unreliable lookup request
//		 */
//
//		triggerUnreliableLookupRequest(destinationId,
//				LookupStrategy.TRANSITIVE, request,
//				ExampleOperationResponseEvent.class,
//				"handleExampleOperationResponse");
//
//		/*
//		 * For a reliable lookup request
//		 */
//
//		triggerReliableLookupRequest(destinationId, LookupStrategy.TRANSITIVE,
//				request, ExampleOperationResponseEvent.class,
//				"handleExampleOperationResponse");
//	}
//
//	public void handleExampleOperationResponse(
//			ExampleOperationResponseEvent event) {
//
//		ExampleLookupOperationResponseMessage responseMessage = (ExampleLookupOperationResponseMessage) event
//				.getOperationMessage();
//
//		@SuppressWarnings("unused")
//		int responseValue = responseMessage.getOperationValue();
//
//	}
//}
