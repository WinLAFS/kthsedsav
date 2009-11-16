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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.jasmine.jade.util.Invocation;

import dks.arch.Event;
import dks.messages.Message;
import dks.niche.events.CreateGroupEvent;
import dks.niche.exceptions.DestinationUnreachableException;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.hiddenEvents.AllocateRequestEvent;
import dks.niche.hiddenEvents.DeployRequestEvent;
import dks.niche.hiddenEvents.DiscoverRequestEvent;
import dks.niche.ids.BindElement;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupElement;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.ids.ResourceId;
import dks.niche.ids.SNR;
import dks.niche.ids.SNRElement;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.LoggerInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.OperationManagerInterface;
import dks.niche.interfaces.ReliableInterface;
import dks.niche.interfaces.ResourceManagementInterface;
import dks.niche.messages.DelegateSubscriptionMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.GetReferenceMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;

/**
 * The <code>NicheOSSupportFork</code> class
 * 
 * @author Joel
 * @version $Id: NicheOSSupportFork.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheOSSupportFork implements NicheActuatorInterface, OperationManagerInterface {


	private static final int MAX_CONCURRENT_OPERATIONS = 500;

	transient NicheAsynchronousInterface myNiche;

	final String waitForSynchronousReturnValue = "_isSynchronous";

	Object[] waitForResults = new Object[MAX_CONCURRENT_OPERATIONS];

	protected int operationId = 0;

	Object[] synchronizedObjects = new Object[MAX_CONCURRENT_OPERATIONS];

	// For testing:
	int resourceIndex = 0;

	transient ArrayList<NodeRef> knownResources;
	transient SimpleResourceManager myRM;
	transient Random myRandom;

	String nodeId;

	NicheId myId;

	boolean blocking;
	boolean replicate;
	//private final Map<Integer, Boolean> notified = new HashMap<Integer, Boolean>(); 
	//IdentifierInterface owner;
	
	private HashMap<Object, ClientSideBindStub> bindings;

	//private boolean ignoreProxyErrors;
	
	public static final boolean randomizeDiscover =
		System.getProperty("niche.randomizeDiscover") instanceof String ? 
			Integer.parseInt(System.getProperty("niche.randomizeDiscover")) == 1 ?
			true : false
		: false; 

	public static final boolean STABLE_ID =
		System.getProperty("niche.stableid.mode") instanceof String ?
				System.getProperty("niche.stableid.mode").equals("1")
			:
				false
		;

	public static final	int	OPERATION_TIMEOUT =
		System.getProperty("niche.operationTimeout") instanceof String ?
				Integer.parseInt(System.getProperty("niche.operationTimeout"))
			:
				100000 //close to inf
		;	
	

	public static final	int	BIND_OPERATION_TIMEOUT =
		System.getProperty("niche.bindOperationTimeout") instanceof String ?
			Integer.parseInt(System.getProperty("niche.bindOperationTimeout"))
		:
			100000 //close to inf
	;

	public static final	int	BIND_SEND_TIMEOUT =
		System.getProperty("niche.bindSendTimeout") instanceof String ?
			Integer.parseInt(System.getProperty("niche.bindSendTimeout"))
		:
			10000 //
		;
								
			
	// CONSTRUCTOR(S)

	// public NicheOSSupportFork(NicheAsynchronousInterface niche, boolean
	// blocking) {
	// this.myNiche = niche;
	// nodeId = niche.getLocalId().getLocation();
	// synchronizedObject = "waitHandler";
	// knownResources = new ArrayList();
	// this.blocking = blocking;
	// }

	public NicheOSSupportFork(
			NicheAsynchronousInterface niche,
			NicheId id,
			String owner,
			boolean blocking
		) {
		
		this.myNiche = niche;
		
		//nodeId = niche.getLocalId().getLocation();
		// synchronizedObject = new Object();
		myRM = niche.getResourceManager();
		nodeId = myRM.getDKSRef().getId().toString();
		myRandom = new Random(myRM.getRandomSeed(this));
		
		knownResources = new ArrayList();
		this.blocking = blocking;
		this.replicate = 1 < myRM.getReplicationFactor();

//		if(id == null || id.getLocation() == null) {
//			
//			this.myId = myRM.getContainterId(nodeId);
//			this.ignoreProxyErrors = true;
//			myNiche.log(logMessage);
//		} else {
//			
//		}
		//it is not allowed for both id & owner to be null at init!
		
		if(id == null) {
			if(owner == null) {				
				/*#%*/ myNiche.log("ERROR: you must specify either an id or an owner when creating the actuator-instance!");
				System.exit(1);
			}
			myId = myRM.getContainterId(nodeId);
			myId.setOwner(owner);
			
		}else {
			this.myId = id;
		}

		
	}

	// DISCOVER, ALLOCATE & DEPLOY

	// DISCOVER

	public NodeRef oneShotDiscoverResource(Serializable description) throws OperationTimedOutException {
		/*#%*/ myNiche.log("The actuator " + myId + " says: Time for discover!");
		ArrayList<Object[]> at = discover(description);
		if (null == at || at.size() == 0) {
			/*#%*/ myNiche.log("Could not find any suitable resource!");
			return null;
		}
		return (NodeRef) at.get(0)[1];

		// if(resourceIndex >= knownResources.size()) {
		// ArrayList<Object[]> at = discover(description);
		// resourceIndex = 0;
		// for (Object [] o : at) {
		// knownResources.add((NodeRef)o[1]);
		// }
		// }
		// if(null == knownResources || knownResources.size() == 0) {
		// System.out.println("Could not find any suitable resource!");
		// return null;
		// }
		//		
		// return knownResources.get(resourceIndex++);
	}

	public ArrayList discover(Serializable description) throws OperationTimedOutException {

		synchronized (this) {
			prepareWait();
			Event event = new DiscoverRequestEvent(description,
					new NicheNotify(this, operationId));
			
			myNiche.trigger(event);
			
			myWait(operationId, OPERATION_TIMEOUT);
		}
		/*#%*/ myNiche.log("Discover done!");
		// System.out.println("Discover done!");
		ArrayList unbalancedList = (ArrayList) waitForResults[operationId];
		ArrayList resultList = new ArrayList();
		if(randomizeDiscover) {
			while(0 < unbalancedList.size()) {
				resultList.add(unbalancedList.remove(myRandom.nextInt(unbalancedList.size())));
			}
		} else {
			resultList = unbalancedList;
		}
		return resultList;

	}

	// ALLOCATE

	// Public

	public ArrayList allocate(Serializable resources, Object description) throws OperationTimedOutException {

		/*#%*/ myNiche.log("Starting allocation");

		System.out.println("Starting allocation using owner named " + myId.getOwner());
		synchronized (this) {
			prepareWait();
			Event event = new AllocateRequestEvent(
					resources,
					description,
					new NicheNotify(this, operationId),
					myId.getOwner()
			);

			myNiche.trigger(event);
			
			// myNiche.allocate(resources, description, this);
			myWait(operationId, OPERATION_TIMEOUT);
		}

		/*#%*/ myNiche.log("Done with allocation!");
		return (ArrayList) waitForResults[operationId];
	}

	public void deallocate(ResourceId resource) {
		System.out.println("Starting deallocation");

		// operationId = (operationId + 1) % MAX_CONCURRENT_OPERATIONS;
		// waitForResults[operationId] = waitForSynchronousReturnValue;

		Event event = new AllocateRequestEvent(
				resource,
				new NicheNotify(this, operationId),
				myId.getOwner()
		);
		
		myNiche.trigger(event);
		
		// myWait();
		System.out.println("Done with deallocation!");
		// return (ArrayList)waitForResults;
	}

	// Private upcall handler

	// DEPLOY

	// public ArrayList<Object[]> deploy(ArrayList<ResourceId>
	// rIds,ArrayList<Object> s) {
	// waitForResults = waitForSynchronousReturnValue;
	// myNiche.trigger(new DeployRequestEvent(rIds, s, this)); //deploy(rIds, s,
	// this);
	// myWait();
	// return (ArrayList<Object[]> )waitForResults;
	// }

	public ArrayList deploy(Serializable destinations, Serializable descriptions) throws OperationTimedOutException {

		synchronized (this) {
			prepareWait();
			Event event = new DeployRequestEvent(
					destinations,
					descriptions,
					new NicheNotify(this, operationId),
					myId.getOwner()
			);
			
			myNiche.trigger(event);
			
			myWait(operationId, OPERATION_TIMEOUT);
		}
		return (ArrayList) waitForResults[operationId];
	}

	public NicheId deployManagementElement(ManagementDeployParameters description,
			IdentifierInterface destination) {
		
		
		NicheId newId = requestId(true, destination, myId.getOwner(), description.getType(), description.isReliable());

		//I'm the startmanager, I deploy regardless
		DelegationRequestMessage message = new DelegationRequestMessage(
				newId,
				DelegationRequestMessage.TYPE_FRACTAL_MANAGER,
				description
		);
		myNiche.sendToManagement(newId, message, description.isReliable());
		
		//OBS, "newer" events are only of interest to the proxy, and can be ignored if no 
		//proxy is available

		return newId;
	}
	
	
	public void redeployManagementElement(ManagementDeployParameters description,
			IdentifierInterface oldId) {
		
		DelegationRequestMessage message = new DelegationRequestMessage(
													oldId.getId(),
													DelegationRequestMessage.TYPE_FRACTAL_MANAGER,
													description
											);
		
		myNiche.sendToManagement(oldId.getId(), message, description.isReliable());

	}

	//This is the local bind op offered to ordinary components
	public void bind(String clientInterface, Object server,
			String serverInterface, int type) {
		//TODO, fixme
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.NicheActuatorInterface#bind(java.lang.Object,
	 *      java.lang.String, java.lang.Object, java.lang.String)
	 */
	public BindId bind(Object client, String clientInterface, Object server,
			String serverInterface) {
		int type;
		if (server instanceof GroupId) {
			// GO AND FETCH WHATEVER THE TYPE SHOULD BE
			// FIXME
			type = JadeBindInterface.ONE_TO_ANY;
		} else {
			type = JadeBindInterface.ONE_TO_ONE;
		}
		return bind(client, clientInterface, server, serverInterface, type);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.NicheActuatorInterface#bind(java.lang.Object,
	 *      java.lang.String, java.lang.Object, java.lang.String, int)
	 */
	public BindId bind(Object sender, String senderInterface, Object receiver,
			String receiverInterface, int type) {

		operationId = (operationId + 1) % MAX_CONCURRENT_OPERATIONS;
		waitForResults[operationId] = waitForSynchronousReturnValue;

		String wrappedSenderInterface, wrappedReceiverInterface;

		// OBS OBS OBS This assumes it is the sender who does the bind
		// operation.
		// This will not work if the bind is initiatied by someone else,
		//since we grab the location/the node-ref of the node where this
		//command is issued
				
		String[] wrappedInterfaces = myRM.wrapInterfaceDescriptions(senderInterface, receiverInterface, type);
		
		wrappedSenderInterface = wrappedInterfaces[0];
		wrappedReceiverInterface = wrappedInterfaces[1];
		
		BindId bindId = new BindId(
				requestId(true, null, myId.getOwner(), NicheId.TYPE_BINDING, false),
				(IdentifierInterface)sender,
				(IdentifierInterface)receiver,
				wrappedSenderInterface,
				wrappedReceiverInterface,
				type
		);
		
		/*#%*/ myNiche.log(
		/*#%*/ 		"Fork says: Activate bindId "
		/*#%*/ 		+ bindId.getId().toString()
		/*#%*/ 		+ "for "
		/*#%*/ 		+ wrappedSenderInterface
		/*#%*/ 		+ " <-> "
		/*#%*/ 		+ wrappedReceiverInterface
		/*#%*/ 		+".  The receiver is at "
		/*#%*/ 		+ ((IdentifierInterface) receiver).getId().getLocation());
		
		DelegationRequestMessage message = new DelegationRequestMessage(
			bindId.getId(),
			DelegationRequestMessage.TYPE_BINDING,
			BindElement.class.getName(),
			new Serializable[]{}
		);
		
		message.setParameters(new Serializable[]{bindId});
		if (((IdentifierInterface)sender).getId().equals(myId)) {
			
			/*#%*/ myNiche.log("Fork says: BLOCK!");
			synchronized (this) {
				prepareWait();
				NicheNotify initiator = new NicheNotify(this, operationId);
				message.setInitiator(initiator); //initiator/NicheNotify is _NOT_ serializable
				myNiche.requestFromManagement(myId, message, initiator);
				myWait(operationId, BIND_OPERATION_TIMEOUT);
			}
		} else {
			//System.out.println("Is " + ((IdentifierInterface)sender).getId() + " different from " + myId);
			
			myNiche.sendToManagement(myId, message, false); //FIXME: check when this is used
		}
		return bindId;
	}

	public void unbind(IdentifierInterface binding) {
		// TODO: test
		myNiche.sendToManagement(
				binding.getId(),
				new UpdateManagementElementMessage(
						binding.getId(),
						UpdateManagementElementMessage.TYPE_REMOVE_BINDING,
						binding
				),
				binding.getId().isReliable()
		);
	}

	protected void myWait(int operationId, int delay) throws OperationTimedOutException {

		long startMillis = System.currentTimeMillis(), waitMillis = 0;
		synchronized (synchronizedObjects[operationId]) {
			
			/*#%*/ myNiche.log("Fork says: Entering critical section: " + operationId);
			
		while (waitForResults[operationId]
				.equals(waitForSynchronousReturnValue)
				&&
				waitMillis < delay
				) {
			
				try {
					synchronizedObjects[operationId].wait(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				waitMillis = System.currentTimeMillis() - startMillis;
			} // 
							
			if(waitForResults[operationId]
								.equals(waitForSynchronousReturnValue)) {
								
					throw new OperationTimedOutException();
			} 			
				
			/*#%*/ myNiche.log("Fork says: Exiting critical section: " + operationId);
		}
		// }
	}

//	private boolean notified(int operationId) {
//		if (notified.get(operationId) == null
//				|| notified.get(operationId) == false) {
//			return false;
//		} else {
//			return true;
//		}
//	}

	// Upcall! Not for the management elements to call, only for the
	// infrastructure

	public void notify(int operationId, Object result) {

		/*#%*/ myNiche.log("Fork says: notify is trying to enter critical section: "
		/*#%*/ 		+ operationId);
		synchronized (synchronizedObjects[operationId]) {
			/*#%*/ myNiche.log("Fork says: notify is entering critical section: "
			/*#%*/ 		+ operationId + " mySyncObj: "
			/*#%*/ 		+ synchronizedObjects[operationId]);
			//notified.put(operationId, true);
			waitForResults[operationId] = result;
			synchronizedObjects[operationId].notify();
			/*#%*/ myNiche.log("Fork says: notify is exiting critical section: "
			/*#%*/ 		+ operationId);

		}
	}

	// Subscribe

	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName) {
		return subscribe(source, sink, eventName, null);
	}
	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName, Serializable tag) {
		
		Object[] subscriptionAndMessage = myRM.getSubscriptionAndMessage(
													source,
													sink, 
													eventName,
													tag
											);
		
		boolean reliable =
			source instanceof GroupId ? 
					true
				:
					source instanceof ReliableInterface ?
							((ReliableInterface)source).isReliable()
						:
							false;
							
		//wiuwqehoeghoiwqe
		/*#%*/ myNiche.log("Fork says: sending subscription to " + source.getId() + " with replication set to " + reliable) ;
		myNiche.sendToManagement(
				source.getId(),
				(Message)subscriptionAndMessage[1],
				reliable
		);
		return (Subscription)subscriptionAndMessage[0];
	}

	public Subscription subscribe(IdentifierInterface source, String sinkName,
			String eventName, IdentifierInterface sinkLocation) throws OperationTimedOutException {

		synchronized (this) {
			prepareWait();
			/*#%*/ myNiche.log("Fork says: Request from management & block! My id is: "
			/*#%*/ 				+ this.getId());
			myNiche.requestFromManagement(sinkLocation.getId(),
					new DelegateSubscriptionMessage(source, sinkName,
							eventName, sinkLocation), new NicheNotify(this,
							operationId));
			myWait(operationId, OPERATION_TIMEOUT);
		}
		/*#%*/ myNiche.log("Fork says: The result of requesting from management was: "
		/*#%*/ 		+ waitForResults);
		if (waitForResults[operationId] instanceof Subscription) {
			return (Subscription) (waitForResults[operationId]);
		}
		return null;
	}

	public boolean unsubscribe(Subscription subscription) {
		myNiche.sendToManagement(
				subscription.getSourceId(),
				new UpdateManagementElementMessage(
						subscription.getSourceId(),
						UpdateManagementElementMessage.TYPE_REMOVE_SINK,
						subscription
				),
				subscription.getSourceId().isReliable()
		);
		return false;
	}

	// Sending things

	

	//
	// public void sendToManagement(DKSRef destination, Message message) {
	// myNiche.sendToManagement(destination, message);
	//	
	// }
	//
	// public void sendToNode(DKSRef destination, Message message) {
	// myNiche.sendToNode(destination, message);
	//
	// }
	//
	// public Object requestFromManagement(NicheId destination, Message
	// requestMessage) {
	// waitForResults = waitForSynchronousReturnValue;
	// myNiche.requestFromManagement(destination, requestMessage, this);
	// myWait();
	// System.out.println("Done with request!");
	// return waitForResults;
	//
	// }

	// Sending things - Components

//	public void send(Object localBindId, Object message) {
//		myNiche.send(localBindId, message);
//	}
//
//	public void send(Object localBindId, Object message, ComponentId destination) {
//		myNiche.send(localBindId, message, destination);
//	}

	// public void registerBindNotifyHandler(Object receiverId, Object
	// handlerObject) {
	// //FIXME
	// }
	// GETTERS

	// ID-GETTERS

//	public NicheId getUniqueCollocatedId(NicheId id) {
//		return myNiche.getUniqueCollocatedId(id);
//	}
//
//	public NicheId getUniqueId() {
//		return myNiche.getUniqueId();
//	}
//
//	public NicheId getCloseNode(DKSRef nodeOfRef) {
//		return myNiche.getCloseNode(nodeOfRef);
//	}

	// OTHER GETTERS

	public SimpleResourceManager getResourceManager() {

		return myRM;
	}

	
	public ComponentType getComponentType(String adlName) {
		return myRM.getComponentType(adlName);
	}
	// TIMERS

	public long registerTimer(EventHandlerInterface me, Class name, int period) {
		return myNiche.registerTimer(me, name, period);

	}

	public void cancelTimer(long timerId) {
		myNiche.cancelTimer(timerId);

	}

	// GROUPING

	public GroupId createGroup(ArrayList items) {
	
	return createGroup(new String(), items);
}
	
	public GroupId createGroup(String templateName, ArrayList items) {
	
		return createGroup(myRM.getSNRTemplate(templateName), items);
	}
	
	public GroupId createGroup(SNR template, ArrayList items) {
		
		NicheId newId = requestId(true, null, myId.getOwner(), NicheId.TYPE_GROUP_ID, true); //groups are reliable. period
		
		GroupId newGroup = new GroupId(newId, template);
		
		/*#%*/ myNiche.log("Fork says: sending to destination: " + newId + " with replication set to " + replicate);

		DelegationRequestMessage message = new DelegationRequestMessage(
													newId,
													GroupElement.class.getName(),
													new Serializable[]{newGroup, items}
											);

		myNiche.sendToManagement(newId, message, replicate);

		// System.out.println("Triggering new create group event!");
		myNiche.trigger(new CreateGroupEvent(newGroup, myId ));
		return newGroup;
	}

	public void removeGroup(GroupId gid) {
		update(gid, null, NicheComponentSupportInterface.REMOVE_GROUP);
	}

	public GroupId getGroupTemplate() {
		return new GroupId();
	}
	
	public boolean registerGroupTemplate(String templateName, SNR template) {
		return myRM.registerSNRTemplate(templateName, template);
	}
	// BindElement

	// public void dynamicBind(Object sender, Object senderInterface, Object
	// receiver, Object receiverInterface) {
	// int type;
	// if(receiver instanceof GroupId) {
	// //GO AND FETCH WHATEVER THE TYPE SHOULD BE
	// type = JadeBindInterface.ONE_TO_ANY;
	// } else {
	// type = JadeBindInterface.ONE_TO_ONE;
	// }
	// dynamicBind(sender, senderInterface, receiver, receiverInterface, type);
	// }
	//	
	// public void dynamicBind(Object sender, Object senderInterface, Object
	// receiver, Object receiverInterface, int type) {
	// waitForResults = waitForSynchronousReturnValue;
	// SNRElement wrappedSender;
	// if(sender instanceof SNRElement) {
	// wrappedSender = (SNRElement)sender;
	// } else {
	// wrappedSender = new SNRElement(((IdentifierInterface)sender).getId(),
	// myRM.getNodeRef());
	// }
	// BindElement newBinding = new BindElement(myNiche, wrappedSender, (SNRElement) receiver,
	// senderInterface, receiverInterface, type, myNiche.getLocalId());
	// System.out.println("Fork at "+nodeId +" : Activate bindId! The receiver
	// is at "+((SNRElement) receiver).getId().getLocation());
	// newBinding.activate(this);
	// System.out.println("Fork at "+nodeId+" : Wait on bindId");
	// myWait();
	// System.out.println("Done with bindId!");
	//		
	// }

	public void addToGroup(Object itemToAdd, Object groupId) {
		update(groupId, itemToAdd, NicheComponentSupportInterface.ADD_TO_GROUP);
	}

	public void removeFromGroup(Object itemToRemove, Object groupId) {
		update(groupId, itemToRemove,
				NicheComponentSupportInterface.REMOVE_FROM_GROUP);
	}

	public void update(Object objectToBeUpdated, Object argument, int type) {
		NicheId idOfDestination = ((IdentifierInterface) objectToBeUpdated)
				.getId();
		UpdateSNRRequestMessage message;
		
		boolean reliable = objectToBeUpdated instanceof ReliableInterface || objectToBeUpdated instanceof GroupId;
		//FIXME...
		
		switch (type) {
		case NicheComponentSupportInterface.ADD_TO_GROUP:
			message = new UpdateSNRRequestMessage(idOfDestination,
					UpdateSNRRequestMessage.TYPE_ADD_REFERENCE,
					(IdentifierInterface) argument);
			myNiche.sendToManagement(idOfDestination, message, reliable);

			break;
		case NicheComponentSupportInterface.ADD_TO_GROUP_AND_START:
			message = new UpdateSNRRequestMessage(idOfDestination,
					UpdateSNRRequestMessage.TYPE_ADD_REFERENCE_AND_START,
					(IdentifierInterface) argument);
			myNiche.sendToManagement(idOfDestination, message, reliable);

			break;

		case NicheComponentSupportInterface.REMOVE_FROM_GROUP:
			System.out.println("Fork says: warning: REMOVE_FROM_GROUP not tested yet!");
			/*#%*/ myNiche.log("Fork says: warning: REMOVE_FROM_GROUP not tested yet!");
			message = new UpdateSNRRequestMessage(
					idOfDestination,
					UpdateSNRRequestMessage.TYPE_REMOVE_REFERENCE,
					(IdentifierInterface) argument
			);
			myNiche.sendToManagement(idOfDestination, message, reliable);

			break;
		case NicheComponentSupportInterface.REMOVE_GROUP:
			message = new UpdateSNRRequestMessage(idOfDestination,
					UpdateSNRRequestMessage.TYPE_REMOVE_GROUP, new NicheId());
			myNiche.sendToManagement(idOfDestination, message, reliable);

			break;

		}
		/*#%*/ myNiche.log("NicheOSSupportFork says: Sending update message to " + idOfDestination.toString() + " with reliability set to " + reliable);
		
	}
	
	public synchronized Object query(IdentifierInterface queryObject, int queryType) {
		
		
		prepareWait();
		/*#%*/ myNiche.log("Fork says: Make a query of type "
		/*#%*/	+ queryType
		/*#%*/	+ " & block! My id is: "
		/*#%*/ 	+ this.getId());
		
		Message queryMessage;
		
		switch(queryType) {
		case NicheComponentSupportInterface.GET_CURRENT_MEMBERS:
			queryMessage =
				new GetReferenceMessage(
					queryObject.getId(),
					GetReferenceMessage.GET_ALL
				);
			break;
			default:
				queryMessage = null;
			
		}
		myNiche.requestFromManagement(queryObject.getId(), queryMessage, new NicheNotify(this, operationId));
		myWait(operationId, OPERATION_TIMEOUT);
	
	
	
		switch(queryType) {
		case NicheComponentSupportInterface.GET_CURRENT_MEMBERS:
			
				return ((HashMap<String, IdentifierInterface>)waitForResults[operationId]).values().toArray();
			
			default:
				return null;
			
		}
		
	}
	
	public ArrayList<SNRElement> getCurrentMembers(SNRElement snrName) {
		return null;
	}
	
	public synchronized Object sendWithReply(Object localBindId, Serializable invocation) {
		
		 
		prepareWait();
		/*#%*/ myNiche.log("Fork says: Request from server component & block! My id is: "
		/*#%*/ 				+ this.getId());
		myNiche.sendWithReply(localBindId, invocation, null, new NicheNotify(this, operationId));
		myWait(operationId, OPERATION_TIMEOUT);
	
	
		/*#%*/ myNiche.log("Fork says: The result of requesting from a server component was: "
		/*#%*/ 		+ waitForResults);
	
	return (waitForResults[operationId]);
	
	
}
	public synchronized Object sendOnBinding(Object localBindId, Invocation invocation, ComponentId shortcut) throws DestinationUnreachableException  {
		
		 
		prepareWait();
		/*#%*/ myNiche.log("Fork says: bla bla bla & block! My id is: "
		/*#%*/ 				+ this.getId());
		myNiche.sendWithSendAck(localBindId, invocation, shortcut, new NicheNotify(this, operationId));
		myWait(operationId, BIND_SEND_TIMEOUT);
	
		if(waitForResults[operationId]
							instanceof DestinationUnreachableException) {
			throw (DestinationUnreachableException)waitForResults[operationId];
		} 
		
		/*#%*/ myNiche.log("Fork says: The result of requesting from a server component was: "
		/*#%*/ 		+ waitForResults);
	
	return (waitForResults[operationId]);	
	
}

	public void setOwner(IdentifierInterface owner) {
		//this.owner = owner;
		if (myId == null) {
			myId = owner.getId();
		}
	}

	public NicheId getId() {
		if (myId == null) {
			System.err.println("FORK SAYS ERROR, NO ID SET");
		}
		return myId;
	}

	public NicheAsynchronousInterface testingOnly() {
		return myNiche;
	}
	public LoggerInterface getLogger() {
		return myNiche;
	}

	protected NicheId requestId(boolean unique, IdentifierInterface locationId, String owner, int type, boolean reliable) {
		NicheId newId;
		
		if(unique) {
			
			//getNicheId(NicheId locationId, String owner, int type, boolean reliable)
			newId = myRM.getNicheId(locationId, owner, type, reliable);
		} else {
			newId = myRM.getContainterId(nodeId);
		}
		return newId;
		
//		do {
//
//			if(!stableId) {
//				return newId;
//			} 
//			
//			BigInteger ringDestination = new BigInteger(newId.getLocation());
//
//			SendRequestEvent sre;
//
//			sre = new SendRequestEvent(ringDestination, newId,
//					SendRequestEvent.REQUEST_ID);
//
//			NicheId checkedId;
//			synchronized (this) {
//				prepareWait();
//				sre.setInitiator(new NicheNotify(this, operationId));
//				myNiche.trigger(sre);
//				myWait(operationId);
//				checkedId = (NicheId) waitForResults[operationId];
//			}
//			if (!newId.isCollocated(checkedId)) {
//				isStable = false;
//				/*#%*/ myNiche.log("Fork says: chosen stable node not present, retrying a different one");
//			} else {
//				isStable = true;
//				newId = checkedId;
//			}
//		} while (!isStable);
//		
//		return newId;

	}
	
	protected void prepareWait() {
		operationId = (operationId + 1) % MAX_CONCURRENT_OPERATIONS;
		waitForResults[operationId] = waitForSynchronousReturnValue;
		//notified.put(operationId, false);
		synchronizedObjects[operationId] = new Object();
	}

}
