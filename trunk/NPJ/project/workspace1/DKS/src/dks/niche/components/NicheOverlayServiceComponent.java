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

// import jade.JadeDeploymentInterface;
// import jade.JadeResourceEnquiryInterface;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
//import org.objectweb.fractal.deployment.local.api.PackageDescription;
//import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.bcast.events.RecursiveIntervalAggregationMyValueEvent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.messages.Message;
import dks.niche.hiddenEvents.AllocateRequestEvent;
import dks.niche.hiddenEvents.DeliverToNodeEvent;
import dks.niche.hiddenEvents.DeployRequestEvent;
import dks.niche.hiddenEvents.DiscoverRequestEvent;
import dks.niche.hiddenEvents.ExternalDiscoverRequestEvent;
import dks.niche.hiddenEvents.ExternalDiscoverResponseEvent;
import dks.niche.ids.BindElement;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentElement;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.messages.AllocateRequestMessage;
import dks.niche.messages.AllocateResponseMessage;
import dks.niche.messages.BindRequestMessage;
import dks.niche.messages.BindResponseMessage;
import dks.niche.messages.DCMSCacheUpdateMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeployRequestMessage;
import dks.niche.messages.DeployResponseMessage;
import dks.niche.messages.RespondThroughBindingMessage;
import dks.niche.messages.SendThroughBindingMessage;
import dks.niche.messages.StartComponentMessage;
import dks.niche.wrappers.BindSendClass;
import dks.niche.wrappers.BroadcastContent;
import dks.niche.wrappers.BulkSendContent;
import dks.niche.wrappers.ClassWrapper;
import dks.niche.wrappers.ClientSideBindStub;
import dks.niche.wrappers.DeployWrapper;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.NicheNotifyType;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;
import dks.niche.wrappers.SimpleResourceManager;
import dks.utils.IntervalsList;

/**
 * The <code>NicheOSSInterfaceComponent</code> class
 * 
 * @author Joel
 * @version $Id: NicheOSSInterfaceComponent.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheOverlayServiceComponent extends Component {

	final String waitForSynchronousReturnValue = "_isSynchronous";

	public static final String BIND_FAILURE = "bind failure";

	public static final String PARTIAL_BIND_FAILURE = "partial bind failure";

	public static final String BIND_SUCCESS = "bind success";

	public static final int MAX_CONCURRENT_OPERATIONS = 5000;

	// final int MAX_CONCURRENT_DEPLOY_REQUESTS = 10;

	ClassWrapper resourceEnquiryHandler;

	ClassWrapper allocationHandler;

	ClassWrapper deploymentHandler;

	ClassWrapper bindHandler;

	ClassWrapper deliverHandler;

	Object[] waitForDiscoverResults;

	Object[] waitForAllocateResults;

	Object[] waitForDeployResults;

	String[] waitForBindResultHandlers;

	BindElement[] waitForBindResults;

	NicheNotifyInterface[] waitForSendResultHandlers;

	DKSRef myDKSRef;

	// Random myRandom;

	// NicheCommunicatingComponent myCommunicator;
	NicheAsynchronousInterface myCommunicator;

	NicheManagementInterface myNicheManagementInterface;

	ExecutorService myThreadPool;

	SimpleResourceManager myRM;
	int replicationFactor; 

	HashMap<String, ClassWrapper> applicationEventRecivers;

	// HashMap<String, NicheNotifyInterface> applicationResultRecivers;

	// HashMap<Object, ComponentId> componentsLocalToGlobal;
	// HashMap<String, Object> componentsGlobalToLocal;

	// HashMap<String, Object> bindingsGlobalToLocal;
	// HashMap<Object, BindElement> bindingsLocalToGlobal;

	private int currentDiscoverOperation;

	private int currentDeployOperation;

	private int currentAllocateOperation;

	private int currentBindOperation;


	// To know on return how many results that are expected.
	private int[] sizeOfDeployRequest;

	private int[] sizeOfAllocateRequest;

	// int discoverTimeout = 5000;

	// private long operationCounter = 0;
	// InputStreamReader cin = new InputStreamReader(System.in); //Only for
	// testing

	BigInteger id, N;

	private String defaultDiscoverResultHandlerId = "not fully used";

	private String defaultAllocateResultHandlerId = "not fully used";

	private String defaultDeployResultHandlerId = "not fully used";

	private String defaultBindResultHandlerId = "not fully used";

	private String defaultBindHandlerId = "not fully used";

	ArrayList[] deployResults;

	ArrayList[] allocateResults;

	HashMap myBindNotifyReceivers = new HashMap();

	HashMap<String, Message> myPendingDeliverOperations;

	public static final boolean EXPLICIT_INSTANTIATION =
		System.getProperty("niche.stableId.explicitInstantiation") instanceof String ?
				System.getProperty("niche.stableId.explicitInstantiation").equals("1")
			:
				false;
	
	static final boolean NICHE_DEPLOYMENT = 
		System.getProperty("niche.deployment.mode") instanceof String ?
				System.getProperty("niche.deployment.mode").equals("1")
			:
				false;
				
	/*#%*/ private static Logger log = Logger.getLogger(NicheOverlayServiceComponent.class);

	public NicheOverlayServiceComponent(NicheManagementInterface niche,
			NicheCommunicatingComponent communicator, Scheduler scheduler,
			ComponentRegistry registry, ExecutorService threadPool, DKSRef myRef) {

		super(scheduler, registry);

		// registry.getMarshalerComponent().registerMessageTypesTable(NicheMessageTable.INTERVAL_STARTING,
		// NicheMessageTable.class);

		id = registry.getRingMaintainerComponent().getMyDKSRef().getId();
		N = registry.getRingMaintainerComponent().getDksParameters().N;

		if (true) { // Was: if(receiver)// for instance, the boot node might not
					// want to
			// register for broadcast messages
			registerForEvents();
			registerConsumers();
			// registerLookupOperations();
			// componentsLocalToGlobal = new HashMap();
			// componentsGlobalToLocal = new HashMap();
			// bindingsGlobalToLocal = new HashMap();
			// bindingsLocalToGlobal= new HashMap();

		}
		// anyone can do management tasks, and should therefore be able to
		// receive the result events
		registerForResultEvents();

		myCommunicator = niche.getNicheAsynchronousSupport();
		// myCommunicator.setDefaultBroadcastReceiver(this,
		// "handleSendThroughBindingBroadcastMessage");

		myNicheManagementInterface = niche;

		myThreadPool = threadPool;

		myRM = niche.getResourceManager(); // This will null

		replicationFactor = myRM.getReplicationFactor();
		this.myDKSRef = myRef;
		// myRandom = new Random();

		waitForDiscoverResults = new Object[MAX_CONCURRENT_OPERATIONS];
		waitForAllocateResults = new Object[MAX_CONCURRENT_OPERATIONS];
		waitForDeployResults = new Object[MAX_CONCURRENT_OPERATIONS];
		waitForBindResultHandlers = new String[MAX_CONCURRENT_OPERATIONS];
		waitForBindResults = new BindElement[MAX_CONCURRENT_OPERATIONS];
		waitForSendResultHandlers = new NicheNotifyInterface[MAX_CONCURRENT_OPERATIONS];

		deployResults = new ArrayList[MAX_CONCURRENT_OPERATIONS];
		sizeOfDeployRequest = new int[MAX_CONCURRENT_OPERATIONS];

		allocateResults = new ArrayList[MAX_CONCURRENT_OPERATIONS];
		sizeOfAllocateRequest = new int[MAX_CONCURRENT_OPERATIONS];

		applicationEventRecivers = new HashMap();
		myPendingDeliverOperations = new HashMap<String, Message>();
		//

		for (int i = 0; i < MAX_CONCURRENT_OPERATIONS; i++) {
			// initialize all to !null
			waitForDiscoverResults[i] = "";
			waitForAllocateResults[i] = "";
			waitForDeployResults[i] = "";
			waitForBindResultHandlers[i] = "";
		}

		//currentDiscoverOperation = currentAllocateOperation = currentDeployOperation = currentBindOperation = 5;
		// catches some potential errors

	}

	@Override
	protected void registerForEvents() {

		// You should register for this event if you want to receive any
		// broadcasted message
		register(ExternalDiscoverRequestEvent.class,
				"handleResourceEnquiryRequestEvent");

		register(DiscoverRequestEvent.class, "discover");
		register(AllocateRequestEvent.class, "allocate");
		register(DeployRequestEvent.class, "deploy");

		register(DeliverToNodeEvent.class, "handleDeliverToNodeEvent");

	}

	protected void registerConsumers() {

		registerConsumer("handleAllocateRequestMessage",
				AllocateRequestMessage.class);
		registerConsumer("handleAllocateResponseMessage",
				AllocateResponseMessage.class);
		registerConsumer("handleDeployRequestMessage",
				DeployRequestMessage.class);
		registerConsumer("handleDeployResponseMessage",
				DeployResponseMessage.class);
		registerConsumer("handleBindRequestMessages", BindRequestMessage.class);
		registerConsumer("handleBindResponseMessage", BindResponseMessage.class);

		registerConsumer("handleStartComponentMessage",
				StartComponentMessage.class);

		registerConsumer("handleSendThroughBindingMessage",
				SendThroughBindingMessage.class);

		registerConsumer("handleRespondThroughBindingMessage",
				RespondThroughBindingMessage.class);

		registerConsumer("handleDCMSCacheUpdateMessage",
				DCMSCacheUpdateMessage.class);

	}

	protected void registerForResultEvents() {

		// You should register for this event if you want to receive the result
		// of the aggregation
		register(ExternalDiscoverResponseEvent.class,
				"handleDiscoverResponseEvent");

		// You should register for this event if you want to receive the ack and
		// the result of the aggregation
		// register(ExternalDeployResponseEvent.class,
		// "handleDeployResponseEvent");
		// Now: replaced with 1-1 communication

	}

	public void registerReceiver(Object receiverObject, String handlerMethod) {
		applicationEventRecivers.put(receiverObject.getClass().getName()
				+ handlerMethod,
				new ClassWrapper(receiverObject, handlerMethod));
		// applicationEventRecivers.put(defaultBindResultHandlerId, new
		// ClassWrapper(receiverObject, handlerMethod));
	}

	public void registerResourceEnquiryHandler(Object resourceEnquiryHandler) {
		this.resourceEnquiryHandler = new ClassWrapper(resourceEnquiryHandler,
				"resourceEnquiry");
	}

	public void registerResourceEnquiryHandler(Object resourceEnquiryHandler,
			String methodName) {
		this.resourceEnquiryHandler = new ClassWrapper(resourceEnquiryHandler,
				methodName);
	}

	public void registerAllocationHandler(Object allocationHandler) {
		this.allocationHandler = new ClassWrapper(allocationHandler, "allocate");
	}

	public void registerAllocationHandler(Object allocationHandler,
			String methodName) {
		this.allocationHandler = new ClassWrapper(allocationHandler, methodName);
	}

	public void registerDeploymentHandler(Object deploymentHandler) {
		this.deploymentHandler = new ClassWrapper(deploymentHandler, "deploy");
	}

	public void registerDeploymentHandler(Object deploymentHandler,
			String methodName) {
		this.deploymentHandler = new ClassWrapper(deploymentHandler, methodName);
	}

	public void registerBindHandler(Object bindHandler) {
		this.bindHandler = new ClassWrapper(bindHandler, "bind");
	}

	public void registerBindHandler(Object bindHandler, String methodName) {
		this.bindHandler = new ClassWrapper(bindHandler, methodName);
	}

	public void registerDeliverHandler(Object deliverHandler) {
		this.deliverHandler = new ClassWrapper(deliverHandler, "deliver");
	}

	public void registerDeliverHandler(Object deliverHandler, String methodName) {
		this.deliverHandler = new ClassWrapper(deliverHandler, methodName);
	}

	public void setResourceManager(SimpleResourceManager rm) {
		this.myRM = rm;
	}

	public void publicTrigger(Event e) {
		trigger(e);
	}

	public void handleDeliverToNodeEvent(DeliverToNodeEvent event) {

		//if it is given to the NicheOverlayServiceComponent, the attachment is supposed to be a message 
		
		Object chunk = event.getMessage();
		if(chunk instanceof ArrayList) {
			ArrayList<Message> list = (ArrayList)chunk;
			for (Message message : list) {
				handleDeliverToNodeSingleEvent(message);
			}
		} else {
			handleDeliverToNodeSingleEvent((Message) chunk);
		}
		
	}
	
	private void handleDeliverToNodeSingleEvent(Message message) {
		
		/*#%*/ log.debug("DeliverToNodeEvent has message " + message.getClass().getSimpleName());
		
		if (message instanceof AllocateRequestMessage) {
			handleAllocateRequestMessage(new DeliverMessageEvent(message, null,
					null));
		} else if (message instanceof AllocateResponseMessage) {
			handleAllocateResponseMessage(new DeliverMessageEvent(message,
					null, null));
		} else if (message instanceof DeployRequestMessage) {
			handleDeployRequestMessage(new DeliverMessageEvent(message, null,
					null));
		} else if (message instanceof DeployResponseMessage) {
			handleDeployResponseMessage(new DeliverMessageEvent(message, null,
					null));
		} else if (message instanceof BindRequestMessage) {
			handleBindRequestMessages(new DeliverMessageEvent(message, null,
					null));
		} else if (message instanceof BindResponseMessage) {
			System.err.println("It seems handleBindResponseMessage "
					+ " _was_ needed after all");
//			handleBindResponseMessage(new DeliverMessageEvent(message, null,
//					null));
		} else if (message instanceof SendThroughBindingMessage) {
			handleSendThroughBindingMessage(new DeliverMessageEvent(message,
					null, null));
		} else if (message instanceof RespondThroughBindingMessage) {
			handleRespondThroughBindingMessage(new DeliverMessageEvent(message,
					null, null));
		} else if (message instanceof StartComponentMessage) {
			handleStartComponentMessage(new DeliverMessageEvent(message, null, null));
		} else {
			String errMsg = "OBS: A message of type " + message.getClass().getSimpleName() + " was delivered but was not processed, please handle!";
			System.err.println(errMsg);
			/*#%*/ log.debug(errMsg);
		}
	}

	/*
	 * *********************************************************************************************
	 * Discover Discover Discover Discover
	 * 
	 * Here follows:
	 * 
	 * 1. The methods dealing with requests from the application 2. The method
	 * dealing with incoming requests from the management/other node(s) 3. The
	 * method dealing with the return event containing the results of the
	 * operation
	 * 
	 */

	/*
	 * *********************************************************************************************
	 * 
	 * Discover - syncronous & asynchronous
	 * 
	 * Handlers for the requests coming from the application
	 * 
	 */

	public void discover(DiscoverRequestEvent ev) {

		Object unwrappedContent = ev.getRequirements();
		NicheNotifyInterface initiator = ev.getInitiator();
		internalAsynchronousDiscover(unwrappedContent, null, null, initiator
				.getId().toString(), new ClassWrapper(initiator,
				NicheNotifyType.METHOD_NAME));

	}

	public void asynchronousDiscover(Object unwrappedContent,
			IntervalsList receivers, String destinationHandlerId,
			Object localSideHandlerObject, String localSideHandlerMethod) {
		String t = localSideHandlerObject.getClass().getName()
				+ localSideHandlerMethod;
		internalAsynchronousDiscover(unwrappedContent, receivers,
				destinationHandlerId, t, new ClassWrapper(
						localSideHandlerObject, localSideHandlerMethod));
	}

	private int internalAsynchronousDiscover(Object unwrappedContent,
			IntervalsList receivers, String destinationHandlerId,
			String localSideHandlerId, ClassWrapper localSideHandler) {

		int thisOperation = currentDiscoverOperation;
		currentDiscoverOperation = (currentDiscoverOperation + 1)
				% MAX_CONCURRENT_OPERATIONS;

		// System.out.println("JIC says: Do discover-broadcast...");

		String discoverResultHandlerId = localSideHandlerId;

		if (null == localSideHandler) {
			if (null == localSideHandlerId) {
				discoverResultHandlerId = defaultDiscoverResultHandlerId;
			}
			// else => this is a sync.call, the response should be handled
			// directly by the caller,
			// as indicated by localSideHandlerId ==
			// synchronousSendAckIndictator
		} else {
			applicationEventRecivers.put(discoverResultHandlerId,
					localSideHandler);
		}

		waitForDiscoverResults[thisOperation] = discoverResultHandlerId;

		// SimpleIntervalBroadcastStartEvent event = new
		// SimpleIntervalBroadcastStartEvent();
		PseudoReliableIntervalBroadcastStartEvent event = new PseudoReliableIntervalBroadcastStartEvent();
		IntervalBroadcastInfo info = new IntervalBroadcastInfo();

		info.setMessage(new BroadcastContent(thisOperation, unwrappedContent,
				destinationHandlerId));

		info.setInterval(receivers);
		info.setAggregate(true); // we do want the recepients to send their
		// data back

		info.setDeliverEventClassName(ExternalDiscoverRequestEvent.class
				.getName()); // JIC-specific events
		info.setAckAggrEventClassName(ExternalDiscoverResponseEvent.class
				.getName());// JIC-specific events

		event.setInfo(info);

		// System.out.println("NicheOSSInterfaceComponent says: do discovery -
		// i'm now triggering b-cast");
		trigger(event);
		// System.out.println("NicheOSSInterfaceComponent says: do discovery -
		// has triggered b-cast");

		return thisOperation;
	}

	/*
	 * *********************************************************************************************
	 * 
	 * Discover
	 * 
	 * Handlers for the requests coming from the network
	 * 
	 */

	/**
	 * This is the event-handler for the resourceEnquiryRequest on the JadeNode
	 * side
	 * 
	 * @param
	 */

	public void handleResourceEnquiryRequestEvent(
			ExternalDiscoverRequestEvent event) {
		ResourceEnquiry resourceEnquiry = new ResourceEnquiry(event);
		//myThreadPool.
		execute(resourceEnquiry);

	}

	/*
	 * *********************************************************************************************
	 * 
	 * Discover
	 * 
	 * Handlers for the results returned from the network
	 * 
	 */

	public void handleDiscoverResponseEvent(ExternalDiscoverResponseEvent event) {
		/*#%*/ log.debug("handleDiscoverResponseEvent: Broadcast results received");

		ArrayList<Object> invalues = event.getValues();
		ArrayList<Object[]> nRefArr = new ArrayList();
		Object[] oa;
		// Find those that didn't reply with null
		for (Object object : invalues) {
			oa = (Object[]) object;
			if (oa[0] != null) {
				// System.out.println("adding "+ oa[0]+" " + oa[1]);
				nRefArr.add(oa);
			}
		}

		// get from any object, why not 0, they all contain the same opId
		int thisOperation = ((NodeRef) ((Object[]) (invalues.get(0)))[1])
				.getOperationId();

		String localHandler = (String) waitForDiscoverResults[thisOperation];

		// an asynchronous call was made, invoke the correct handler
		ClassWrapper discoverResultHandler = applicationEventRecivers
				.get(localHandler);
		Object receiver = discoverResultHandler.getObject();

		if (receiver instanceof NicheNotifyInterface) {
			/*#%*/ log.debug("handleDiscoverResponseEvent: Notifying FORK");

			((NicheNotifyInterface) receiver).notify(nRefArr);

		}

		else {

			try {

				receiver.getClass().getMethod(
						discoverResultHandler.getMethod(),
						new Class[] { Object.class }).invoke(receiver, nRefArr);

			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				/*#%*/ log.error(e1.getMessage());
			} catch (SecurityException e1) {
				e1.printStackTrace();
				/*#%*/ log.error(e1.getMessage());
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				/*#%*/ log.error(e1.getMessage());
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				/*#%*/ log.error(e1.getMessage());
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				/*#%*/ log.error(e1.getMessage());
			}
		}

	}

	/*
	 * *********************************************************************************************
	 * *********************************************************************************************
	 * *********************************************************************************************
	 * 
	 * Allocate - asynchronous
	 * 
	 * Here follows:
	 * 
	 * 1. The methods dealing with requests from the application 2. The method
	 * dealing with incoming requests from the management/other node(s) 3. The
	 * method dealing with the return event containing the results of the
	 * operation
	 * 
	 */

	/*
	 * **********************************************************************************************
	 * 
	 * Handlers for the requests from the application
	 * 
	 */

	public void allocate(AllocateRequestEvent ev) {

		/*#%*/ log.debug("Allocation-event processing");
		Object destinationObject = ev.getDestinationObject();
		Object descriptionObject = ev.getDescriptionObject();
		NicheNotifyInterface initiator = ev.getInitiator();

		ArrayList<NodeRef> destinations;
		ArrayList descriptions;

		if (destinationObject instanceof ArrayList) {
			destinations = (ArrayList<NodeRef>) destinationObject;
		} else {
			destinations = new ArrayList<NodeRef>(1);
			destinations.add((NodeRef)destinationObject);
		}

		if (descriptionObject instanceof ArrayList) {
			descriptions = (ArrayList) descriptionObject;
		} else {
			descriptions = new ArrayList(1);
			descriptions.add(descriptionObject);
		}

		int[] positions = generateSinglePosition(destinations.size());
		String localSideHandlerId = "identifier";
		ClassWrapper localSideHandler =
			new ClassWrapper(initiator, NicheNotifyType.METHOD_NAME);
		
//		internalAsynchronousAllocate(destinations, descriptions,
//				generateSinglePosition(destinations.size()), null,
//				"identifier", new ClassWrapper(initiator,
//						NicheNotifyType.METHOD_NAME));

		
//		private int internalAsynchronousAllocate(
//				ArrayList<NodeRef> destinationNodeRefs,
//				ArrayList<Object> descriptions, int[] positions,
//				String destinationHandlerId, String localSideHandlerId,
//				ClassWrapper localSideHandler) {

			int thisOperation = currentAllocateOperation;
			currentAllocateOperation = (currentAllocateOperation + 1)
					% MAX_CONCURRENT_OPERATIONS;

			String allocateResultHandlerId = localSideHandlerId;

			if (null == localSideHandler) {
				if (null == localSideHandlerId) {
					allocateResultHandlerId = defaultAllocateResultHandlerId;
				}
				// else => this is a sync.call, the response should be handled
				// directly by the caller,
				// as indicated by localSideHandlerId ==
				// synchronousSendAckIndictator
			} else {
				applicationEventRecivers.put(allocateResultHandlerId,
						localSideHandler);
			}

			waitForAllocateResults[thisOperation] = allocateResultHandlerId;

			int numberOfDestinations = destinations.size();
			sizeOfAllocateRequest[thisOperation] = numberOfDestinations;
			allocateResults[thisOperation] = new ArrayList();

			// There should be only one case, given that the 'positions' array was
			// correctly generated:
			// the description for ResourceIds.get(i) is at
			// descriptions.get(positions[i])

			int i = 0;
			/*#%*/ log.debug("Number of destinations for allocation request: "
			/*#%*/ 		+ numberOfDestinations);

			for (NodeRef nodeRef : destinations) {
				// Should be physical node!
				myCommunicator.sendToNode(nodeRef.getDKSRef(),
						new AllocateRequestMessage(
								thisOperation,
								nodeRef,
								descriptions.get(positions[i++]),
								ev.getOwner()
						)
				);
			}

	}

//	public void asynchronousAllocate(ArrayList<NodeRef> destinations,
//			ArrayList<Object> descriptions, String destinationHandlerId,
//			Object localSideHandlerObject, String localSideHandlerMethod) {
//		String t = localSideHandlerObject.getClass().getName()
//				+ localSideHandlerMethod;
//		internalAsynchronousAllocate(destinations, descriptions,
//				generatePositions(destinations.size()), destinationHandlerId,
//				t, new ClassWrapper(localSideHandlerObject,
//						localSideHandlerMethod));
//	}

	/*
	 * 
	 * 
	 * 
	 * 
	 */

	/*
	 * **********************************************************************************************
	 * Allocate Allocate Allocate Allocate Allocate Allocate Allocate Allocate
	 * 
	 * Handlers for the requests from the network
	 * 
	 */

	/**
	 * This is the event-handler for the allocateRequest on the JadeNode side
	 * 
	 * @param
	 */

	public void handleAllocateRequestMessage(DeliverMessageEvent event) {

		AllocateRequest allocateRequest = new AllocateRequest(event);
		//myThreadPool.
		execute(allocateRequest);

	}

	/*
	 * **********************************************************************************************
	 * Allocate Allocate Allocate Allocate Allocate Allocate
	 * 
	 * Handlers for the result message from the network
	 * 
	 */

	// The 1-1-message-version
	public void handleAllocateResponseMessage(DeliverMessageEvent event) {

		AllocateResponseMessage m = (AllocateResponseMessage) event
				.getMessage();

		// The returnvalue contained in the response is an
		// ArrayList<Object[]{Object, ComponentId}>

		ResourceRef partialResult = m.getResourceRef();
		int thisOperation = m.getOperationId();

		allocateResults[thisOperation].add(partialResult);

		if (allocateResults[thisOperation].size() == sizeOfAllocateRequest[thisOperation]) {

			String localHandler = (String) waitForAllocateResults[thisOperation];
			ClassWrapper allocateResultHandler = applicationEventRecivers
					.get(localHandler);
			Object handlerObject = allocateResultHandler.getObject();

			if (handlerObject instanceof NicheNotifyInterface) {
				((NicheNotifyInterface) handlerObject)
						.notify(allocateResults[thisOperation]);
				// NicheNotifyInterface nn = // nn.notify(nn.getOperationId(),
				// allocateResults[thisOperation]);
			} else {
				try {
					handlerObject.getClass().getMethod(
							allocateResultHandler.getMethod(),
							new Class[] { Object.class }).invoke(handlerObject,
							allocateResults[thisOperation]);
				} catch (IllegalArgumentException e1) {
					/*#%*/ log.error(e1.getMessage());
					e1.printStackTrace();
				} catch (SecurityException e1) {
					/*#%*/ log.error(e1.getMessage());
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					/*#%*/ log.error(e1.getMessage());
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					/*#%*/ log.error(e1.getMessage());
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					/*#%*/ log.error(e1.getMessage());
					e1.printStackTrace();
				}

			} // endif size == size
		}
	}

	/*
	 * *********************************************************************************************
	 * *********************************************************************************************
	 * *********************************************************************************************
	 * 
	 * Deploy - asynchronous
	 * 
	 * Here follows:
	 * 
	 * 1. The methods dealing with requests from the application 2. The method
	 * dealing with incoming requests from the management/other node(s) 3. The
	 * method dealing with the return event containing the results of the
	 * operation
	 * 
	 */

	/*
	 * **********************************************************************************************
	 * 
	 * Handlers for the requests from the application
	 * 
	 */

	public void deploy(DeployRequestEvent ev) {
		
	
		Object destinationObject = ev.getDestinationObject();
		Object descriptionObject = ev.getDescriptionObject();
		NicheNotifyInterface initiator = ev.getInitiator();

		if (ev.isManagementDeployment()) {
			managementDeploy(destinationObject, descriptionObject);
			System.err.println("Who calls me!?!?!?!");
			System.out.println(Thread.currentThread().getStackTrace().toString());
			return;
		}

		ArrayList uncheckedDestinations;
		ArrayList<ResourceRef> destinations;
		ArrayList descriptions;
		
		if (destinationObject instanceof ArrayList) {
			uncheckedDestinations = (ArrayList) destinationObject;
			// FIXME: temp hack
			if (uncheckedDestinations.get(0) instanceof NodeRef) {
				destinations = new ArrayList<ResourceRef>();
				for (Object object : uncheckedDestinations) {
					destinations.add(new ResourceRef((NodeRef) object, ev.getOwner()));
				}
				
			} else { //if (uncheckedDestinations.get(0) instanceof ResourceRef){
				//here we go if they are already in the proper format
				//- that is resourceRefs and not node-refs!
				
				destinations = uncheckedDestinations;
			}
		} else {
			
			destinations = new ArrayList(1);
			// FIXME Hack
			if (destinationObject instanceof NodeRef) {
				destinations.add(
						new ResourceRef(
								(NodeRef) destinationObject,
								ev.getOwner()
							)
						);
			} else {
				//here we go if it is already in the proper format
				//- that is a resourceRef and not node-ref!
				destinations.add((ResourceRef)destinationObject);
			}
		}
		
		if (descriptionObject instanceof ArrayList) {
			descriptions = (ArrayList<Object>) descriptionObject;
		} else {
			
			descriptions = new ArrayList<Object>(1);
			descriptions.add(descriptionObject);
		}
		
//			internalAsynchronousDeploy(destinations, descriptions,
//					generatePositions(destinations.size()), false, null,
//					"identifier", new ClassWrapper(initiator,
//							NicheNotifyType.METHOD_NAME));
		
//			private int internalAsynchronousDeploy(
//					ArrayList<ResourceRef> destinationRefs,
//					ArrayList<Object> descriptions, int[] positions,
//					boolean sharedComponent, String destinationHandlerId,
//					String localSideHandlerId, ClassWrapper localSideHandler) {
	/*
	 * Statements made for now: - The string representing the GlobalComponentId
	 * for each component to be deployed is generated here at the initiator side
	 * 
	 */

		//int[] positions = generatePositions(destinations.size());
		
		ClassWrapper localSideHandler = new ClassWrapper(
				initiator,
				NicheNotifyType.METHOD_NAME
		);
		
		int thisOperation = currentDeployOperation;
		currentDeployOperation = (currentDeployOperation + 1)
				% MAX_CONCURRENT_OPERATIONS;

		// System.out.println("JIC says: Do deploy-broadcast...");

		String deployResultHandlerId = "identifer";

		applicationEventRecivers.put(
				deployResultHandlerId,
				localSideHandler
		);		

		waitForDeployResults[thisOperation] = deployResultHandlerId;

		int numberOfDestinations = destinations.size();
		sizeOfDeployRequest[thisOperation] = numberOfDestinations;

		deployResults[thisOperation] = new ArrayList();

		BigInteger[] destIds = new BigInteger[numberOfDestinations];
		DeployWrapper[] deployDestinationInfo = new DeployWrapper[numberOfDestinations];
		DeployWrapper[] deployContents = new DeployWrapper[numberOfDestinations];

		// IntervalBroadcastInfo info = new IntervalBroadcastInfo();
		// info.setAggregate(true); // we do want the recepients to send their
		// CId data back

		BigInteger tempId = null;
		ResourceRef tempRef;
		// ComponentId tempCId;

		DeployWrapper tempWrapper;

		/*
		 * Build the lists of destinations
		 */
		// IntervalsList destinations = new IntervalsList(N);
		/*
		 * All destinations must be treated individually since they all have
		 * different recourceIds
		 */
		for (int i = 0; i < numberOfDestinations; i++) {

			tempRef = destinations.get(i);
			tempId = tempRef.getDKSRef().getId();
			// System.out.println("JIC says: deploy-request to this id:
			// "+tempId);

			tempWrapper = new DeployWrapper(tempRef, i);

			// destinations.addToSelf(tempId);
			// System.out.println("NicheOSSInterfaceComponent says: this is the
			// partial list of destinations: "+destinations);
			destIds[i] = tempId;

			deployDestinationInfo[i] = tempWrapper;

		}
			
		ResourceRef currentRef;
		for (int i = 0; i < destinations.size(); i++) {
		
			currentRef = destinations.get(i);
			
			NicheId t = myRM.getNicheId(
						currentRef.getDKSRef().getId().toString(),
						currentRef.getOwner(),
						NicheId.TYPE_COMPONENT_ID,
						false
			);
			
			deployContents[i] = new DeployWrapper(t, descriptions.get(0)); // FIXME,
			// now
			// hardcoding
			// one
			// description
			// for
			// all
		
		}

		BulkSendContent tempB =
			new BulkSendContent(
					thisOperation,
					null, //destinationHandlerId,
					destIds,
					deployDestinationInfo,
					deployContents
		);

		// (int operationId, String eventName, BigInteger[]ids, Object[]
		// destinationInfo, Object[] content, int[]positions)
		// DeployWrapper dw = ((DeployWrapper)tempB.getContent(tempId));

		/*#%*/ log.debug("Final list of deploy destinations: "
		/*#%*/ 		+ destinations.toString());

		for (ResourceRef rid : destinations) {
			
			// System.out.println("trying to send to " + rid.getDKSRef());
			myCommunicator.sendToNode(
					rid.getDKSRef(),
					new DeployRequestMessage(
							tempB,
							myDKSRef,
							destinations.size()
						)
			);
		}

		// System.out.println("NicheOSSInterfaceComponent says: deploy-requests
		// sent");
		// info.setInterval(destinations);
		//				 
		// info.setMessage(tempB);
		//		
		// info.setDeliverEventClassName(ExternalDeployRequestEvent.class.getName());
		// //JIC-specific events
		// info.setAckAggrEventClassName(ExternalDeployResponseEvent.class.getName());//JIC-specific
		// events
		//		
		// //System.out.println("this is the content to send: " +
		// dw.getContent());
		//		
		// PseudoReliableIntervalBroadcastStartEvent myPRIBSE = new
		// PseudoReliableIntervalBroadcastStartEvent();
		// myPRIBSE.setInfo(info);
		//		
		// trigger(myPRIBSE);

		// System.out.println("ok, so now we check if we are good to go:
		// "+destIds.get(0)+" in "+theList.contains((BigInteger)destIds.get(0))
		// + " and "+destIds.get(1) + " in
		// "+theList.contains((BigInteger)destIds.get(1)) + " and
		// "+destIds.get(2) + " in
		// "+theList.contains((BigInteger)destIds.get(2)));
		//return thisOperation;

	}


	private void managementDeploy(Object destination, Object description) {
		myCommunicator.sendToNode((DKSRef) destination,
				new DeployRequestMessage(description, myDKSRef));
	}

	/*
	 * **********************************************************************************************
	 * Deploy Deploy Deploy Deploy Deploy Deploy
	 * 
	 * Handlers for the requests from the network
	 * 
	 */

	/**
	 * This is the event-handler for the deployRequest on the JadeNode side
	 * 
	 * @param
	 */

	public void handleDeployRequestMessage(DeliverMessageEvent event) {

		DeployRequestMessage m = (DeployRequestMessage) event.getMessage();

		/*#%*/ log.debug("Deploy request received");

		if (deploymentHandler == null) {
			return; // FIXME JadeBoot should not get this event
		}

		Runnable d = new DeployClass(m);

		// if (m.getManagementDeployParam() != null) {
		// System.err.println("ERRROR"); //d = new ManagementDeployClass(m);
		// }
		//myThreadPool.
		execute(d);

	}

	/*
	 * **********************************************************************************************
	 * Deploy Deploy Deploy Deploy Deploy
	 * 
	 * Handlers for the result message from the network
	 * 
	 */

	// The 1-1-message-version
	public void handleDeployResponseMessage(DeliverMessageEvent event) {

		DeployResponseMessage m = (DeployResponseMessage) event.getMessage();

		// The returnvalue contained in the response is an
		// ArrayList<Object[]{Object, ComponentId}>
		Object[] results = m.getResults();

		ComponentId tempCId = (ComponentId) results[1];
		int thisOperation = tempCId.getOperationId();

		deployResults[thisOperation].add(results);

		if (deployResults[thisOperation].size() == sizeOfDeployRequest[thisOperation]) {

			String localHandler = (String) waitForDeployResults[thisOperation];
			ClassWrapper deployResultHandler = applicationEventRecivers
					.get(localHandler);
			Object handlerObject = deployResultHandler.getObject();
			if (handlerObject instanceof NicheNotifyInterface) {

				((NicheNotifyInterface) handlerObject)
						.notify(deployResults[thisOperation]);
				// NicheNotifyInterface nn =
				// nn.notify(nn.getOperationId(), deployResults[thisOperation]);

			} else {
				try {

					handlerObject.getClass().getMethod(
							deployResultHandler.getMethod(),
							new Class[] { Object.class }).invoke(handlerObject,
							deployResults[thisOperation]);

				} catch (IllegalArgumentException e1) {
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

			} // END ELSE
		} // END IF-ELSE
	}

	/*
	 * *********************************************************************************************
	 * 
	 * Bind
	 * 
	 * Handlers for the requests coming from the network
	 * 
	 */

	/**
	 * This is the event-handler for the bindRequest on the JadeNode side
	 * 
	 * @param
	 */

	public synchronized void handleBindRequestMessages(DeliverMessageEvent event) {

		if (bindHandler == null) {
			System.err.println("Error, no bind receiver registered");
			return; // FIXME JadeBoot should not get this event
		}
		BindRequestMessage brms = (BindRequestMessage)event.getMessage();
		if(brms.isBulk()) {
			ArrayList<BindRequestMessage> bindings = brms.getBindings();
			for (BindRequestMessage message : bindings) {
				handleBindRequestMessage(message);
			}
		} else {
			handleBindRequestMessage(brms);
		}
		
		
	}
	private synchronized void handleBindRequestMessage(BindRequestMessage brm) {

		NicheId destinationComponentId = brm.getDestinationId();
		BindId bindId = brm.getBindInfo();
		
		String description = brm.isSender() ? (String)bindId.getSenderSideInterfaceDescription() : (String)bindId.getReceiverSideInterfaceDescription();
		
		description += ":" + destinationComponentId.toString();
		// System.out.println("NicheOSSINterfaceCOmponent says: bind request
		// received");

		/*
		 * this assumes we already have a mapping between a cid and a lcid!
		 */

		Object localComponent = myRM
				.getComponentBindReceiver(destinationComponentId);

		/*
		 * now call jade-owner
		 */
		Object[] result = null;
		// BindbroadcastWrapper = (BroadcastContent)info.getMessage();

		try {
			// "lbid:bind(lcid, description = BDS)"
			// the invoked method should be bind((whatever we agree on local id
			// type) localComponentId, (whatever we agree on description type)
			// description)
			result = (Object[]) bindHandler.getObject().getClass().getMethod(
					bindHandler.getMethod(),
					new Class[] { Object.class, Object.class }).invoke(
					bindHandler.getObject(), localComponent, description);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// result += " " + registry.getRingMaintainerComponent().getMyDKSRef();

		/*
		 * reply back to sender, if error
		 */

		if (result == null) { // major failure
			

			BindResponseMessage responseMessage = new BindResponseMessage(
					brm.getOperationId(),
					new Object[]{NicheOverlayServiceComponent.BIND_FAILURE}
				);
			
			myCommunicator.sendToNode(brm.getSource(),
					(Message) responseMessage);

		} else if (result[0] == null) {

			result[0] = NicheOverlayServiceComponent.BIND_FAILURE;

			BindResponseMessage responseMessage = new BindResponseMessage(brm
					.getOperationId(), result);
			myCommunicator.sendToNode(brm.getSource(),
					(Message) responseMessage);

		}
		if (brm.isSender()) {

			myRM.addSenderSideBinding(
					new ClientSideBindStub(
							myNicheManagementInterface.getNicheAsynchronousSupport(),
							bindId							
						),
						result[1]
					);
			// needed on downcall

			/*#%*/ log.debug("Bind has established a stub for sending side of bindId "
			/*#%*/ 		+ bindId.getId() + ", localId is: " + result[1] + " receiver is " + bindId.getReceiver().getId());

			NicheNotifyInterface handler = brm.getInitiator();

			if (handler != null) {

				/*#%*/ log.debug("Trying to notify on bind-completion");

				((NicheNotifyInterface) handler).notify("null");
				// NicheNotifyInterface nn = nn.notify(nn.getOperationId(),
				// "null");

			}/*#%*/  else {
			/*#%*/ 	log.debug("None to notify on bind-completion");
			/*#%*/ }

		} else {
			/*#%*/ log.debug("Bind is on receiving side of bindId "
			/*#%*/ 		+ bindId.getId()
			/*#%*/ 		+ ", Check for outstanding messages!");

			String bindReceiverDescription = myRM.addReceiverSideBinding(brm.getDestinationId(), bindId, result[1]);
			// needed on upcall
			if (myPendingDeliverOperations.containsKey(bindReceiverDescription)) {

				/*#%*/ log.debug("Delivering delayed reqest on new bindId");

				internalDeliver((SendThroughBindingMessage) myPendingDeliverOperations.get(bindReceiverDescription));
			}
		}

	}

	/*
	 * *********************************************************************************************
	 * 
	 * Bind
	 * 
	 * Handlers for the results returned from the network
	 * 
	 */

	//Is this really used ...?
	
//	public synchronized void handleBindResponseMessage(DeliverMessageEvent event) {
//		BindResponseMessage resultMessage = (BindResponseMessage) event
//				.getMessage();
//		Object[] results = (Object[]) resultMessage.getResponse();
//		// get the operationId;
//		int thisOperation = resultMessage.getOperationId();
//		// BigInteger id = event.getInstanceId();
//
//		// System.out.println("JIC says: A bind-response has been returned.
//		// thisOperation = "+thisOperation);
//		BindElement currentBindId = waitForBindResults[thisOperation];
//		if (results[0].equals(NicheOverlayServiceComponent.BIND_FAILURE)) {
//			currentBindId.addFailure();
//		}
//		if (currentBindId.decreaseOutstandingReplies()) {
//			// = all replies received
//
//			/*
//			 * Now store the results for further reference
//			 */
//			// FIXME - this should only be done if no unhandled errors
//			// myCommunicator.asynchronousPut(currentBindId.getGlobalBindId(),
//			// currentBindId, putFlavor.PUT_OVERWRITE, null, null);
//			String localHandler = (String) waitForBindResultHandlers[thisOperation];
//			// an asynchronous call was made, invoke the correct handler and
//			// report the "result"
//			try {
//				ClassWrapper bindResultHandler = applicationEventRecivers
//						.get(localHandler);
//				// FIXME what should be the return value to the (jade
//				// management) application?
//				bindResultHandler.getObject().getClass().getMethod(
//						bindResultHandler.getMethod(),
//						new Class[] { Object.class }).invoke(
//						bindResultHandler.getObject(),
//						currentBindId.getResult());
//
//			} catch (IllegalArgumentException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (SecurityException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (IllegalAccessException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (InvocationTargetException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (NoSuchMethodException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//		}
//	}

	public void handleStartComponentMessage(DeliverMessageEvent e) {
		StartComponentMessage m = (StartComponentMessage) e.getMessage();
		/*#%*/ log.debug("handleStartComponentMessage says: time to start the component with id "
		/*#%*/ 				+ m.getComponentId().getId());
		myRM.startLocalFractalComponent((ComponentId) m.getComponentId());
	}

	/*
	 * ****************************************************************************************************
	 * Send Send Send Send Send Send Send Send Send Send Send
	 * 
	 * When a bindId is established, components can communicate through the
	 * niche send-method
	 * 
	 */
	public void send(Object localBindId, Object message) {
		send(localBindId, message, null, null);

	}

	public void send(Object localBindId, Object message, ComponentId receiver) {

		send(localBindId, message, receiver, null);
	}

	public synchronized void send(Object localBindId, Object message,
			ComponentId receiver, NicheNotifyInterface exceptionHandlerAndReplyReceiver) {

		ClientSideBindStub bindStub = myRM.getSenderSideBinding(localBindId);
		
		/*#%*/ log.debug("localBindId " + localBindId + " gave globalBindId "
		/*#%*/ 		+ bindStub.getId() + " used for sending");

		int thisOperation = myRM.getNextLookupAndSendOperationIndex();
		
		if( (bindStub.getType() & JadeBindInterface.WITH_RETURN_VALUE) != 0) { 
		
			// remember, this is the op-no on the overlay service layer,
			// below the com-component also uses op-nos for its own
			// business			
			
			waitForSendResultHandlers[thisOperation] = exceptionHandlerAndReplyReceiver;
			
					
		} // End switch I

		//myThreadPool.
		bindStub.prepareSending(message, thisOperation, exceptionHandlerAndReplyReceiver, receiver);

		execute(bindStub); //
//				new BindSendClass(
//						thisOperation,
//						myCommunicator, bindStub, message, receiver,
//				replyReceiver));
		// new Thread(sendInstance).start();

	}

	public void handleRespondThroughBindingMessage(DeliverMessageEvent event) {
		RespondThroughBindingMessage m = (RespondThroughBindingMessage) event
				.getMessage();

		/*#%*/ log.debug("Notifying NotifyHandler no " + m.getOperationId());
				
		NicheNotifyInterface localHandler = waitForSendResultHandlers[
		                                          m.getOperationId()
		                                    ];

		localHandler.notify(m.getResponseMessage());
	}

	public void handleSendThroughBindingMessage(DeliverMessageEvent event) {
		deliver((SendThroughBindingMessage) event.getMessage());
	}

	/*
	 * This method will receive the one-to-many messages that are being
	 * broadcasted
	 */
	public void handleSendThroughBindingBroadcastMessage(Object msg) {
		deliver((SendThroughBindingMessage) msg);
	}

	private synchronized void deliver(SendThroughBindingMessage message) {

		/*
		 * The agreed plan is as follows: - If there is a localBindId stored in
		 * the Global2LocalBindId-table, use it. - If not, the bind call did not
		 * establish a specific bindId on the receiver side, in which case the
		 * localCId will be used
		 * 
		 */

		NicheId finalDestination = message.getDestination();
		BindId bindId = message.getBindInfo();
		
		Object localId = myRM.getLocalReceiverSideBindReference(finalDestination, bindId);
		
		if (localId == null) {

			/*#%*/ log.debug("Storing request on bindId " + bindId.getId().toString() + " which resolves to " + myRM.getBindReceiverDescription(finalDestination, bindId));
			// Remember: _TOSTRING()_!!
			
			myPendingDeliverOperations.put(
					myRM.getBindReceiverDescription(finalDestination, bindId),
					message
			);
		} else {
			/*#%*/ log.debug("Delivering request on bindId " + bindId.getId()
			/*#%*/ 		+ " corresponding to " + localId);

			BindDeliverClass c = new BindDeliverClass(
					deliverHandler,
					bindId,
					message
			);
			//myThreadPool.
			execute(c);
			// new Thread(c).start();
		}

		// if(localId == null) {
		// localId =
		// componentsGlobalToLocal.get(message.getBindId().getReceiverGlobalComponentId(id));
		// }

	}

	private void internalDeliver(SendThroughBindingMessage message) {

		BindDeliverClass c = new BindDeliverClass(
				deliverHandler,
				message.getBindInfo(),
				message
		);
		// new Thread(c).start();
		//myThreadPool.
		execute(c);

	}

	public void handleDCMSCacheUpdateMessage(DCMSCacheUpdateMessage message) {
		myRM.dcmsCachePut(message.getDCMSId(), message.getRingId());
	}

	/*
	 * ************************************************************************************************
	 * 
	 * Utility methods
	 * 
	 */

	private int[] generatePositions(int s) {
		int[] result = new int[s];
		for (int i = 0; i < s; i++) {
			result[i] = i;
		}
		return result;
	}

	private int[] generateSinglePosition(int s) {
		int[] result = new int[s];
		for (int i = 0; i < s; i++) {
			result[i] = 0;
		}
		return result;
	}

	// /**
	// * @param id2
	// * @param handerObject
	// */
	// public void registerBindNotifyHandler(ComponentId cid, Object
	// handlerObject) {
	// myBindNotifyReceivers.put(cid.getId().toString(), handlerObject);
	//		
	// }

	class ResourceEnquiry implements Runnable {

		ExternalDiscoverRequestEvent event;

		ResourceEnquiry(ExternalDiscoverRequestEvent event) {
			this.event = event;
		}

		public void run() {

			IntervalBroadcastInfo info = event.getInfo();
			BroadcastContent broadcastWrapper = (BroadcastContent) info
					.getMessage();

			Object[] result = null;

			if (resourceEnquiryHandler != null) {

				//System.out.println("JIC: resource enquiry delivered!");

				/*
				 * now call jade-owner
				 */

				// Object result = ((JadeResourceEnquiryInterface)
				// resourceEnquiryHandler).resourceEnquiry(info.getMessage());
				try {
					result = (Object[]) resourceEnquiryHandler.getObject()
							.getClass().getMethod(
									resourceEnquiryHandler.getMethod(),
									new Class[] { Object.class }).invoke(
									resourceEnquiryHandler.getObject(),
									broadcastWrapper.getContent());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// System.out.println("JIC: resource enquiry NOT delivered!");
				// test broadcastWrapper.getContent(), if u want

				result = new Object[] { null, 0 };
				// FIXME fake

			}

			/*
			 * reply back to sender, IF/now regardless of whether- the result is
			 * different from null!
			 */

			NodeRef nRef = myRM.getNodeRef(); // FIXME should be NodeRef

			nRef.setJadeNode(result[0]); // Can be null - will then be sorted
			// out at aggregating node

			if (result[0] != null) {

				nRef.setSize((Integer) result[1]);

			} // end is != null

			nRef.setOperationId(broadcastWrapper.getOperationId());

			// IF switching back to simple broadcast, CHANGE!!!
			RecursiveIntervalAggregationMyValueEvent aggr = new RecursiveIntervalAggregationMyValueEvent();

			aggr.addValue(new Object[] { result[0], nRef });

			/*#%*/ String logMessage = "handleResourceEnquiry: result: " + result[1]
			/*#%*/      					+ " is returned from " + myDKSRef.getId() + " to: "
			/*#%*/ 			                                    + info.getInitiator().getId();
			/*#%*/ log.debug(logMessage);
			//System.out.println(logMessage);

			aggr.setInitiator(info.getInitiator());
			aggr.setInstanceId(info.getInstanceId());
			trigger(aggr);

		}// END of run()
	}

	class AllocateRequest implements Runnable {

		AllocateRequestMessage m;

		AllocateRequest(DeliverMessageEvent event) {
			//System.out.println("Allocate request received");
			m = (AllocateRequestMessage) event.getMessage();
		}

		public void run() {

			if (allocationHandler == null)
				return; // FIXME JadeBoot should not get this event

			Object nodeId = m.getNodeRef().getJadeNode(); // TODO, maybe

			Object[] result = null;
			/*
			 * now call jade-owner
			 */
			try {

				// FIXME
				if (null == allocationHandler) {
					result = new Object[] { "", "" };
				} else {
					/*
					 * {LRID, result} allocate(Object requirements) !!
					 * 
					 * 
					 * 
					 */

					result = (Object[]) allocationHandler.getObject()
							.getClass().getMethod(
									allocationHandler.getMethod(),
									new Class[] { Object.class }).invoke(
									allocationHandler.getObject(),
									m.getDescription());

				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			myRM.addAllocatedResource(result[0]);

			// Should be physical node!
		
			myCommunicator
					.sendToNode(
							m.getSource(),
							new AllocateResponseMessage(
									m.getOperationId(),
									(new ResourceRef(
											myRM.getNodeRef(),
											result[0],
											m.getOwner()
										).setSize(
											m.getNodeRef().getTotalStorage()
										)
											
									)
							)
					);

		} // end of run

	}

	class DeployClass implements Runnable {

		DeployRequestMessage message;

		DeployClass(DeployRequestMessage message) {
			this.message = message;
		}

		public void run() {

			// use our node-id to retrieve the message directed to us
			BulkSendContent allContent = (BulkSendContent) message.getContent();
			DeployWrapper contentForThisNode = allContent.getDeployWrapper(id);
			Object componentDescription = contentForThisNode
					.getComponentDescription();
			//String cName = myRM.getComponentName(componentDescription);
			
			DeploymentParams params = myRM.getDeploymentParams(componentDescription);
			String cName = params.name;
		
			Object[] result = null;
			org.objectweb.fractal.api.Component newComponent = null;
			
			boolean error = false;
			//System.out.println("Niche deployment is " + NICHE_DEPLOYMENT);
			
			if(NICHE_DEPLOYMENT) {
			
				Object deploymentLock = myRM.getDeploymentLock();
				synchronized (deploymentLock) {

					System.out.println("stats:\n"
							+ params.definition
							+ "\n"
							+ params.name
							+ "\n"
							+ params.contentDesc
							+ "\n"
							+ params.controllerDesc
							+ "\n"
							+ params.packageDesc
							+ "\n"
							+ params.type
							);
//					doDeploy(params.type, params.name, params.definition,
//							params.controllerDesc, params.contentDesc, params.packageDesc);
//					
//					doDeploy(Object type, String name, String definition,
//							Object controllerDesc, Object contentDesc, Object[] packageDesc)
//							
//					res = physicalNodeFactoryItf.newFcInstance(fType,
//						controllerDesc, contentDesc,
//						(PackageDescription) packageDesc[0]);
//					
					
					
//					ArrayList<ComponentType> types = params.getTypes();
//					ArrayList<Map> contexts = params.getContext();

					ContentController cc = myRM.getContentController();

					Object[] deploy = params.packageDesc;
					
					//Map context = contexts.get(i);
					// //////////////////////////////////////////////////////////////////
					// //// create the new component
					// //////////////////////////////////////////////////////////////////
						// myNicheManagementInterface.getNicheAsynchronousSupport().log("creating
					// comp: " + deploy[0] + " "+ deploy[1] );
					/*#%*/ String logMessage = "New Component " + params.contentDesc + " named "
					/*#%*/ 	+ params.name ;
						
						// old
					newComponent = myRM.localJavaDeploy((String)params.contentDesc, (String) params.controllerDesc, (ComponentType) params.type);
					//newComponent = myRM.localADLDeploy((String)deploy[0], null);
					
					/*#%*/ logMessage += " generated using locally processed ADL";
				
					/*#%*/ log.debug(logMessage);
					/*#%*/ System.err.println(logMessage);

					
						// //////////////////////////////////////////////////////////////////
						// //// name it
						// //////////////////////////////////////////////////////////////////
						
					try {
							Fractal.getNameController(newComponent).setFcName(
										params.name);
					} catch (NoSuchInterfaceException e1) {
							e1.printStackTrace();
						}
					

							// //////////////////////////////////////////////////////////////////
							// //// add the new component, seems to be needed also for replicas..
							// //////////////////////////////////////////////////////////////////

							try {

								cc.addFcSubComponent(newComponent);
								
							} catch (IllegalContentException e) {
								e.printStackTrace();
							} catch (IllegalLifeCycleException e) {
								e.printStackTrace();
							}
						}

					

					// //////////////////////////////////////////////////////////////////
					// //// bindings
					// //////////////////////////////////////////////////////////////////

					// - could go here
					
					// //////////////////////////////////////////////////////////////////
					// //// print for testing
					// //////////////////////////////////////////////////////////////////

					// for (int i = 0; i < subComponents.length; i++) {
					// try {
					// logger.log(
					// "Sub component of managed_resources: "
					// + Fractal.getNameController(subComponents[i])
					// .getFcName());
					// } catch (NoSuchInterfaceException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// }

					// //////////////////////////////////////////////////////////////////
					// //// set attributes
					// //////////////////////////////////////////////////////////////////

					// - could go here
					
					// //////////////////////////////////////////////////////////////////
					// //// start components
					// //////////////////////////////////////////////////////////////////
					
					// - could go here
					
					// //////////////////////////////////////////////////////////////////
					// //// stop components
					// //////////////////////////////////////////////////////////////////
					
					// - could go here
					
				
				result = new Object[]{null, 1, newComponent}; //{ ref, 1, c };
				
				} // end synch on RM
			 else  //NOT niche-deployment, use good old jade...
			{
			/*
			 * now call jade-owner
			 */
				 
			try {

				/*
				 * OBS: for now the eventName is not used, the call is directed
				 * towards the single predefined deploymentHandler nor is the
				 * resourceId used...
				 * 
				 * object[0] = result of deploy operation: null, failure
				 * description et cetera
				 * 
				 * object[1] = localComponentId
				 * 
				 */
				result = (Object[]) deploymentHandler.
					getObject().
						getClass().
							getMethod(
									deploymentHandler.getMethod(),
									new Class[] {
										Object.class
									}
							).invoke(
								deploymentHandler.getObject(),
								componentDescription
							);

			} catch (IllegalArgumentException e) {
				/*#%*/ log.error(e.getMessage());
				e.printStackTrace();
				error = true;
			} catch (SecurityException e) {
				/*#%*/ log.error(e.getMessage());
				e.printStackTrace();
				error = true;
			} catch (IllegalAccessException e) {
				/*#%*/ log.error(e.getMessage());
				e.printStackTrace();
				error = true;
			} catch (InvocationTargetException e) {
				/*#%*/ log.error(e.getMessage());
				e.printStackTrace();
				error = true;
			} catch (NoSuchMethodException e) {
				/*#%*/ log.error(e.getMessage());
				e.printStackTrace();
				error = true;
			}
			
			} //end else = end of old deployment style
			
			
			/*
			 * decrease size, if the result was different from null
			 */
			// int allocatedSize = myRM.decreaseFreeSpace(componentDescription);
			myRM.decreaseFreeSpace(contentForThisNode
					.getDestinationResourceRef());
			/*
			 * reply back to sender, regardless of whether the result is
			 * different from null
			 */

			ComponentId deployedComponent =
				new ComponentId(
						contentForThisNode.getComponentToBeDeployedId(),
						cName
					);
			// FIXME

			deployedComponent.setDKSInfo(
					allContent.getOperationId(),
					contentForThisNode.getDestinationResourceRef(),
					contentForThisNode.getPosition()
			);

			deployedComponent
					.setSerializedDeployParameters(componentDescription);

			myRM.addComponentBindReceiver(
					deployedComponent,
					result[2],
					componentDescription
			);


			Object[] returnValue;
			
			if(message.getDeploySource().equals(myDKSRef)) {
				returnValue =
					new Object[] {
						(Serializable)result[0],
						deployedComponent,
						result[2]
					};
				
			} else {
				returnValue =
					new Serializable[] {
							(Serializable)result[0],
							deployedComponent
						};
			}						
			myCommunicator.sendToNode(message.getDeploySource(),
					new DeployResponseMessage(id, returnValue));
			
			myRM.addComponentBindReceiver(deployedComponent, result[2],
					componentDescription);

			
			if (result != null && EXPLICIT_INSTANTIATION) {
				// FIXME this 'instansiate SNE-request' should really be to
				// 'replica' number of recipents, not just the local node
				/*#%*/ log.debug("Activating deployed component = creating the corresponding SNRElement");
				
				DelegationRequestMessage message = new DelegationRequestMessage(
														deployedComponent.getId(),
														ComponentElement.class.getName(),
														new Serializable[]{deployedComponent}
													);
				
				//FIXME: no replication of ComponentId-SNRs
				myCommunicator.sendToManagement(deployedComponent.getId(), message, false);
				//return
				
			}


		}

	}

	class BindDeliverClass implements Runnable {
		ClassWrapper handler;

		BindId bindId;

		SendThroughBindingMessage message;

		BindDeliverClass(ClassWrapper handler, BindId bindId,
				SendThroughBindingMessage message) {
			this.handler = handler;
			this.bindId = bindId;
			this.message = message;
		}

		public void run() {

			Object localId = myRM.getLocalReceiverSideBindReference(message.getDestination(), bindId);
			Serializable returnValue = null;
			
			/*#%*/ log.debug("Before method invokation on component " + bindId.getId().toString() + " operation with opId with opid " + message.getOperationId() + " from " + message.getSource());
			
			try {
				// //This is the upcall!
				// //deliver(localId, message)
				// System.out.println("Delivering message sent over bindId
				// "+message.getBindId().toString());
				
				if (0 < (bindId.getType() & JadeBindInterface.WITH_RETURN_VALUE)) {
					
					returnValue = (Serializable)handler.getObject().getClass().getMethod(
							handler.getMethod(),
							new Class[] { Object.class, Object.class,
									Boolean.class }).invoke(
							handler.getObject(), localId, message, true);
				} else {
					handler.getObject().getClass().getMethod(
							handler.getMethod(),
							new Class[] { Object.class, Object.class }).invoke(
							handler.getObject(), localId, message);
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
			if (0 < (bindId.getType() & JadeBindInterface.WITH_RETURN_VALUE)) {
				// if(returnValue != null) {
				myCommunicator.sendToNode(
						message.getSource(),
						new RespondThroughBindingMessage(
								bindId,
								returnValue,
								message.getOperationId()
								)
						);
				/*#%*/ log.debug("Method invokation with return value done, reply is " + returnValue);

			} /*#%*/ else {
			/*#%*/ 	log.debug("Method invokation done with no return value");
			/*#%*/ }
		}
	}

//	class BindNotifyClass implements Runnable {
//		Object handler;
//
//		BindNotifyClass(Object handler) {
//			this.handler = handler;
//		}
//
//		public void run() {
//
//			String methodName = "bindCompleted";
//			try {
//				// "lbid:bind(lcid, description = BDS)"
//				// the invoked method should be bind((whatever we agree on local
//				// id type) localComponentId, (whatever we agree on description
//				// type) description)
//				handler.getClass().getMethod(methodName, new Class[] {})
//						.invoke(handler);
//
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NoSuchMethodException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}


}
