/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ConfigurationEvent;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.MemberAddedEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.interfaces.ExecutorInterface;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.WatcherInterface;
import dks.niche.messages.BindRequestMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.StartComponentMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;
import dks.niche.wrappers.ExecutorInfo;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.NodeSendClass;
import dks.niche.wrappers.ResourceRef;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;
import dks.niche.wrappers.WatcherInfo;

/**
 * The <code>GroupId</code> class
 * 
 * @author Joel
 * @version $Id: GroupId.java 294 2006-05-05 17:14:14Z joel $
 */
public class GroupElement extends SNRElement implements Serializable, IdentifierInterface,
		ManagementElementInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 3019040436563170626L;

	/**
	 * @serialVersionUID - 
	 */
	

	/**
	 * @serialVersionUID -
	 */
	
	GroupId groupId;

	// ArrayList<IdentifierInterface> things;
	HashMap<String, IdentifierInterface> myReferences;

	//HashMap<String, NicheId> myFailSensors;

	// private NicheAsynchronousInterface myPrivateHost;

	transient private NicheActuatorInterface myNiche;

	// Hmm, one solution to 'can a watcher listen to many events'(yes)
	// would be to assume that such a watcher subscribes itself as many times
	// as there are events

	// To Ahmad: I think I expect this class to be expanded by us... this was
	// supposed to be for testing only...

	final boolean failureEventForwarding = false;
	/*
	 * Let's agree on an ordering of things concerning ME and SNRs:
	 * 
	 * 1 - constructors 2 - activation, sending itself to receiver side 3 -
	 * connection, connecting itself to MEContainer on receiver side 4 - saved
	 * for migration, store/restore state! 5 - add/remove sources 6 - add/remove
	 * sinks 7 - other helper methods n - getters and setters, last
	 * 
	 */

	// Remember the empty constructor
	public GroupElement() {

	}

	// this is for the active case, for the node hosting the group
	// public GroupId(NicheId id, NicheAsynchronousInterface host,
	// ArrayList<SNRElement> things) {
	public void connect(NicheId id, int replicaNumber,
			NicheManagementInterface host,
			NicheNotifyInterface callBack) {
				
		this.niche = host.getNicheAsynchronousSupport();
		this.replicaNumber = replicaNumber;
		
	}

	public void init(Serializable[] parameters) {

		mySinks = new ArrayList<WatcherInterface>();
		mySources = new ArrayList<ExecutorInterface>();
		mySinksForSNREvents = new ArrayList<Subscription>();
		myClientSideBindingsADLToGlobal = new HashMap<String, BindId>();
		myClientSideBindingsGlobalToCache = new HashMap<String, BindId>();

		myReferences = new HashMap<String, IdentifierInterface>();

		// Create the fail-subscription which all groups should have by
		// default:
		
		groupId = (GroupId)parameters[0];
		myId = groupId.getId();
		
		ArrayList<IdentifierInterface> things = (ArrayList<IdentifierInterface>)parameters[1]; 
		myServerSideBindingsADLToStub = groupId.getPredefinedReceiverBindings();
		if(myServerSideBindingsADLToStub == null) {
			myServerSideBindingsADLToStub = new HashMap<String, BindId>();
		}

		for (IdentifierInterface thing : things) {
			addReferenceInternal(thing, false);
			// myReferences.put(thing.getId().toString(), thing);
		}
			
		// this.myNiche = niche.getOSSupport();
		/*#%*/ niche.log("Group-SNRElement created with id " + myId.toString() + ":" +replicaNumber + ", " + myReferences.size() + " components and " + myServerSideBindingsADLToStub.size() + " predefined server bindings");
	
			
	}

	public void reinit(Serializable[] parameters) {
		
//		groupId,
//		myReferences,
//		myServerSideBindingsADLToStub,
//		myClientSideBindingsADLToGlobal,
//		myClientSideBindingsGlobalToCache,
//		myFailSensors,
//		myResourceFailSubscription,
//		mySinks,
//		mySinksForSNREvents


		groupId = (GroupId) parameters[0];
		myId = groupId.getId();

		myReferences = (HashMap<String, IdentifierInterface>) parameters[1];

		myServerSideBindingsADLToStub  = (HashMap<String, BindId>) parameters[2];
		myClientSideBindingsADLToGlobal = (HashMap<String, BindId>) parameters[3];
		myClientSideBindingsGlobalToCache = (HashMap<String, BindId>) parameters[4];

		mySinks = (ArrayList<WatcherInterface>) parameters[5];
		mySinksForSNREvents = (ArrayList<Subscription>) parameters[6];
		mySources = (ArrayList<ExecutorInterface>) parameters[7]; //FIXME check

		/*#%*/ niche.log("Group-SNRElement re-created with id " + myId.toString() + ":" +replicaNumber);
		
	}
	
	public void messageHandler(Message message) {
		if (message instanceof UpdateSNRRequestMessage) {
			UpdateSNRRequestMessage m = (UpdateSNRRequestMessage) message;
			switch (m.getType()) {
			case UpdateSNRRequestMessage.TYPE_ADD_REFERENCE:
				addReferenceInternal((SNR) m.getReference(), false);
				break;
			case UpdateSNRRequestMessage.TYPE_ADD_REFERENCE_AND_START:
				addReferenceInternal((SNR) m.getReference(), true);
				break;
			case UpdateSNRRequestMessage.TYPE_REMOVE_REFERENCE:
				removeReference(m.getReference());

				break;

			case UpdateSNRRequestMessage.TYPE_ADD_WATCHER:

				addSink(
						new WatcherInfo(
								m.getReference().getId(),
								m.getTouchpointClassName(),
								m.getTouchpointParameters()
							)
						);

				break;
				
			case UpdateSNRRequestMessage.TYPE_ADD_EXECUTOR:

				addSource(
						new ExecutorInfo(
								m.getReference().getId(),
								m.getTouchpointClassName(),
								m.getTouchpointParameters()
							)
						);

			break;

			case UpdateSNRRequestMessage.TYPE_REMOVE_WATCHER:

				break;

			case UpdateSNRRequestMessage.TYPE_ADD_CLIENT_BINDING:
				addClientBinding((BindId)m.getReference());
				break;

			case UpdateSNRRequestMessage.TYPE_REMOVE_BINDING:
				removeBinding(m.getReference().getId());
				break;

			case UpdateSNRRequestMessage.TYPE_REMOVE_GROUP:
				removeGroup();
				break;
				/*#%*/ default:
				/*#%*/ niche.log("GroupElement says, ERROR, cannot handle update message of type "+ m.getType());
				/*#%*/ break;
			}
		} else if (message instanceof UpdateManagementElementMessage) {
			UpdateManagementElementMessage m = (UpdateManagementElementMessage) message;
			switch (m.getType()) {

			case UpdateManagementElementMessage.TYPE_ADD_SINK:
				/*#%*/ niche.log("Group-id "
				/*#%*/ 		+ myId
				/*#%*/ 		+ ":" +replicaNumber
				/*#%*/ 		+ " says: I'm adding a watcher listening for infrastructure events"
				/*#%*/ );
				addInfrastructureSensor((Subscription) m.getReference());

				break;

			case UpdateManagementElementMessage.TYPE_REMOVE_SINK:
				/*#%*/ niche.log("GroupElement says, ERROR, REMOVE_SINK not implemented");
				break;

				/*#%*/ default:
				/*#%*/ niche
				/*#%*/ 		.log("GroupElement says, ERROR, cannot handle update message of type "
				/*#%*/ 				+ m.getType());
				/*#%*/ break;
			}

		} else {
			/*#%*/ niche.log("GroupElement says, ERROR, cannot handle message of type "
			/*#%*/ 		+ message.getClass().getName());
			System.err.println("GroupElement says, ERROR, cannot handle message of type "
					+ message.getClass().getName());
		}
	}


//	/*
//	 * 
//	 * add = add reference!! not a sink, that you have to do through addSink
//	 * 
//	 */
//
//	public void add(IdentifierInterface thing) {
//
//		if (myReferences == null) {
//			System.out
//					.println("ERROR, ERROR, ERROR, ERROR, ERROR, use the DCMService.createGroup(xx) instead");
//			// send the item to the SNRElement host!
//			// FIXME
//			UpdateSNRRequestMessage message = new UpdateSNRRequestMessage(myId,
//					UpdateSNRRequestMessage.TYPE_ADD_REFERENCE, thing);
//			niche.sendToManagement(myId, message);
//		}/*#%*/  else {
//		/*#%*/ 	niche.log("GroupElement says error, use the message-handler instead");
//		/*#%*/ }
//
//	}


	private void addReferenceInternal(IdentifierInterface thing, boolean activate) {

		// 1. Add it to the list...
		// 2. Trigger "MemberAddedEvent
		// 3. Add the bindings 'to' the new reference
		// 4. Dito sensors

		/*#%*/ 	String logMessage =		
		/*#%*/ 		"GroupElement-addReference "
		/*#%*/ 		+ myId.toString() + ":" +replicaNumber
		/*#%*/ 		+ " is adding component "
		/*#%*/ 		+ thing.getId().toString()
		/*#%*/		+ " which"
		/*#%*/		+ ( replicaNumber < 1 ?
		/*#%*/				" will get the "
		/*#%*/			:
		/*#%*/				", hadn't this been a replica, would have gotten the "
		/*#%*/		)
		/*#%*/		+ myClientSideBindingsADLToGlobal.size()
		/*#%*/		+ " client bindings, the "
		/*#%*/		+ myServerSideBindingsADLToStub.size()
		/*#%*/		+ " server bindings, the " 
		/*#%*/		+ mySinksForSNREvents.size()
		/*#%*/		+ " system sensors and the "
		/*#%*/		+ mySinks.size()
		/*#%*/		+ " user provided sensors that I have";
		
		/*#%*/ niche.log(logMessage);
		/*#%*/ //System.out.println(logMessage);
		
		myReferences.put(thing.getId().toString(), thing);
		trigger(new MemberAddedEvent(thing).setBroker(myId));

		//Now we must start being more careful: 
		// since this might be a group of MEs, they wont have any node-refs then,
		// in which case we must send to id(s).
		// but for the normal, "old" case we can retrieve dksRefs
		
		boolean sendToId = false;
		NodeRef nodeRef = null;
		
		if(thing instanceof SNR) {
			ResourceRef resourceRef = ((SNR)thing).getResourceRef();
			if(resourceRef == null) {
				sendToId = true;
			} else {
				nodeRef = resourceRef.getNodeRef(); 
			}
		} else {
			sendToId = true;
		}

		// For all bindings: send the stored bind-info to the node of the new
		// thing...

		ArrayList<Message> bindingsToSend = new ArrayList<Message>();
		
		Object[] /* of BindInfos */ clientSideBindings = myClientSideBindingsADLToGlobal.values().toArray();
		BindId bindInfo;

		

		if (replicaNumber < 1) {
			
			for (int i = 0; i < clientSideBindings.length; i++) {
	
				bindInfo = (BindId) clientSideBindings[i];
	

				/*#%*/ niche.log("GroupElement-addReference "
				/*#%*/ 		+ myId.toString() + ":" +replicaNumber
				/*#%*/ 		+ " Interface "
				/*#%*/ 		+ (String) bindInfo.getSenderSideInterfaceDescription()
				/*#%*/ 		+ " on component " + thing.getId() + " on node "
				/*#%*/ 		+ nodeRef.getDKSRef() +
				/*#%*/ 		"is being bound to "
				/*#%*/ 		+ bindInfo.getReceiver().getId());
	
				// can be bulked, they all go to the same destination
				bindingsToSend.add(
						new BindRequestMessage(
								thing,
								bindInfo,
								1 /* IS sender */
						)
				);	

			}
			
			if(0 < myServerSideBindingsADLToStub.size()) {
				
				Object[] serverSideBindings = myServerSideBindingsADLToStub.values().toArray();
				String receiverSideInterfaceDescription;

				/*#%*/ logMessage = "";
				
				for (int i = 0; i < serverSideBindings.length; i++) {
					
					bindInfo = (BindId) serverSideBindings[i];
					receiverSideInterfaceDescription = (String)bindInfo.getReceiverSideInterfaceDescription();
					receiverSideInterfaceDescription = (niche.getResourceManager().wrapInterfaceDescriptions("", receiverSideInterfaceDescription, type))[1];
					bindInfo.setReceiverSideInterfaceDescription(receiverSideInterfaceDescription);
					
					bindInfo.setReceiver(groupId);
					
					/*#%*/ logMessage +=
					/*#%*/ 	
					/*#%*/ 	"GroupElement-addReference "
					/*#%*/ 	+ myId.toString() + ":" +replicaNumber
					/*#%*/ 	+ " creates a stub for Interface "
					/*#%*/ 	+ bindInfo.getReceiverSideInterfaceDescription()
					/*#%*/ 	+ " on component "
					/*#%*/ 	+ thing.getId()
					/*#%*/ 	+ " on node "
					/*#%*/ 	+ nodeRef.getDKSRef();
					
		
					// they can be bulked, they all go to the same destination
		
					bindingsToSend.add(
							new BindRequestMessage(
									thing,
									bindInfo,
									0 /* is NOT sender */
							)
						);
				}
				/*#%*/ niche.log(logMessage);
			} //endif (0 < myServerSideBindingsADLToStub.size)
			
			
			if(0 < bindingsToSend.size()) {
				if(sendToId) {
					
					niche.sendToManagement(
							thing.getId(),
							new BindRequestMessage(
									thing,
									bindingsToSend,
									-1 /* both sender and receiver... */
							),
							thing.getId().isReliable()
						);
					}
					
				 else {
			
					niche.sendToNode(
							nodeRef.getDKSRef(),
							new BindRequestMessage(
									thing,
									bindingsToSend,
									-1 /* both sender and receiver... */
							)
						);
					}
			}

		} // else {	niche.log("GroupElement-addReference: Not sending, I'm a replica"); 	}
		
		// if(0 < bulk.size()) {
		//			
		// }

		ArrayList<DelegationRequestMessage> bulk = new ArrayList<DelegationRequestMessage>();


		// For all watchers: get the sensors and send then to the node of the
		// new thing...
		for (WatcherInterface sink : mySinks) {
			// SensorInterface
			// DelegationRequestMessage m = sink.getSensorMessage();
			bulk.add(
					sink.getSensorMessage(
							niche.getNicheId(thing.getId(), thing.getId().getOwner(), NicheId.TYPE_SENSOR, false),
							//sensors are not reliable...
							(ComponentId) thing
					)
			); // FIXME

		}
		
		// For all executors: get the actuators and send then to the node of the
		// new thing...
		for (ExecutorInterface source : mySources) {
			// SensorInterface
			// DelegationRequestMessage m = sink.getSensorMessage();
			bulk.add(
					source.getActuatorMessage(
							niche.getNicheId(thing.getId(), thing.getId().getOwner(), NicheId.TYPE_ACTUATOR, false),
							//sensors are not reliable...
							(ComponentId) thing
					)
			); // FIXME

		}

		// For all infrastructure watchers: construct the sensors and send then
		// to the node of the new thing...
		//OBS, nodeRef might be null
		//TODO
		activateInfrastructureSensors(thing, nodeRef, bulk);

		if (replicaNumber < 1) {
			
			if(sendToId) {
				
				niche.sendToManagement(
						thing.getId(),
						new DelegationRequestMessage(thing.getId(), bulk),
						thing.getId().isReliable()
				);// TODO check
	
				if (activate) { // Start the component
					/*#%*/ niche.log(
					/*#%*/ 		"GroupElement-addReference "
					/*#%*/ 		+ myId
					/*#%*/ 		+ " says: sending a start-command to "
					/*#%*/ 		+ thing.getId());
					niche.sendToManagement(
							thing.getId(),
							new StartComponentMessage(thing),
							thing.getId().isReliable()
					);
				}
				
			} else { /* send directly to node */
				
				niche.sendToManagement(
						nodeRef.getDKSRef(),
						thing.getId(),
						new DelegationRequestMessage(thing.getId(), bulk)
				);// TODO check
	
				if (activate) { // Start the component
					/*#%*/ niche.log(
					/*#%*/ 		"GroupElement-addReference "
					/*#%*/ 		+ myId
					/*#%*/ 		+ " says: sending a start-command to "
					/*#%*/ 		+ thing.getId());
					niche.sendToNode(nodeRef.getDKSRef(),
							new StartComponentMessage(thing));
				}

			}
			
		} else {
		
			/*#%*/ niche.log(
			/*#%*/ 		"GroupElement-addReference "
			/*#%*/ 		+ myId
			/*#%*/ 		+ ":" + replicaNumber
			/*#%*/ 		+" Not sending, I'm a replica"
			/*#%*/ );
		}
		

	}

	private synchronized boolean removeReference(IdentifierInterface ii) {

		Object removal = myReferences.remove(ii.getId().toString());
		if (removal != null) {
			/*#%*/ niche.log(
			/*#%*/ 		"Group "
			/*#%*/ 		+ myId
			/*#%*/ 		+ ":" + replicaNumber
			/*#%*/ 		+ " says: removed "
			/*#%*/ 		+ ii.getId()
			/*#%*/ 		+ " from my "
			/*#%*/ 		+ (myReferences.size() + 1)
			/*#%*/ 		+ " references"
			/*#%*/ );
			return true;
		}
		/*#%*/ niche.log(
		/*#%*/ 		"Group "
		/*#%*/ 		+ myId
		/*#%*/ 		+ ":" + replicaNumber
		/*#%*/ 		+ " says: Could not remove "
		/*#%*/ 		+ ii.getId()
		/*#%*/ 		+ " - not present in "
		/*#%*/ 		+ myReferences
		/*#%*/ );
		return false;
		// other book-keeping to do; revoke relevant sensors //FIXME
	}

	private void addSink(WatcherInterface ws) {
		mySinks.add(ws);

		// DelegationRequestMessage m = ws.getSensorMessage();

		if (replicaNumber < 1) {

			// Send out sensors!!
			DKSRef nodeOfRef;
			NicheId idOfCollocatedSensor;

			Object[] references = myReferences.values().toArray();
			// SNRElement thing;
			ComponentId componentId;
			String componentName;

			/*#%*/ niche.log("GroupElement-addSink "
			/*#%*/ 		+ myId
			/*#%*/ 		+ ":" + replicaNumber
			/*#%*/ 		+ " says: I'm adding a new watcher with id: "
			/*#%*/ 		+ ws.getId()
			/*#%*/ 		+ " to my "
			/*#%*/ 		+ references.length
			/*#%*/ 		+ " references"
			/*#%*/ );

			for (int i = 0; i < references.length; i++) {

				/*#%*/ niche.log("Adding sensor to ref no " + i);
				componentId = (ComponentId) references[i];
				nodeOfRef = componentId.getResourceRef().getDKSRef();
				componentName = componentId.getComponentName();
				//sensors are not reliable
				idOfCollocatedSensor = niche.getNicheId(componentId.getId(), componentId.getId().getOwner(), NicheId.TYPE_SENSOR, false);

				/*#%*/ niche.log("GroupElement-addSink-Collocated says: The parameters are "
				/*#%*/ 				+ nodeOfRef
				/*#%*/ 				+ ":"
				/*#%*/ 				+ idOfCollocatedSensor
				/*#%*/ 				+ " "
				/*#%*/ 				+ componentName + " " + componentId.getId());
				niche.sendToManagement(
						nodeOfRef,
						componentId.getId(),
						ws.getSensorMessage(
								idOfCollocatedSensor,
								componentId,
								componentName
								)
						);

			}
		} /*#%*/ else {
		/*#%*/ niche.log("GroupElement-addSink says: Not deploying sensors, I'm a replica");
		/*#%*/ }

	}

	
	
	private void addSource(ExecutorInterface es) {
		mySources.add(es);

		// DelegationRequestMessage m = ws.getSensorMessage();

		if (replicaNumber < 1) {

			// Send out actuators!!
			DKSRef nodeOfRef;
			NicheId idOfCollocatedActuator;

			Object[] references = myReferences.values().toArray();
			// SNRElement thing;
			ComponentId componentId;
			String componentName;

			/*#%*/ niche.log("GroupElement-addSource "
			/*#%*/ 		+ myId
			/*#%*/ 		+ ":" + replicaNumber
			/*#%*/ 		+ " says: I'm adding a new executor with id: "
			/*#%*/ 		+ es.getId()
			/*#%*/ 		+ " to my "
			/*#%*/ 		+ references.length
			/*#%*/ 		+ " references"
			/*#%*/ );

			for (int i = 0; i < references.length; i++) {

				/*#%*/ niche.log("Adding executor to ref no " + i);
				componentId = (ComponentId) references[i];
				nodeOfRef = componentId.getResourceRef().getDKSRef();
				componentName = componentId.getComponentName();
				//sensors are not reliable
				idOfCollocatedActuator = niche.getNicheId(componentId.getId(), componentId.getId().getOwner(), NicheId.TYPE_ACTUATOR, false);

				/*#%*/ niche.log("GroupElement-addSink-Collocated says: The parameters are "
				/*#%*/ 				+ nodeOfRef
				/*#%*/ 				+ ":"
				/*#%*/ 				+ idOfCollocatedActuator
				/*#%*/ 				+ " "
				/*#%*/ 				+ componentName + " " + componentId.getId());
				niche.sendToManagement(
						nodeOfRef,
						componentId.getId(),
						es.getActuatorMessage(
								idOfCollocatedActuator,
								componentId,
								componentName
								)
						);

			}
		} /*#%*/ else {
		/*#%*/ niche.log("GroupElement-addSink says: Not deploying sensors, I'm a replica");
		/*#%*/ }

	}
	@Override
	protected void addInfrastructureSensor(Subscription subscription) {

		mySinksForSNREvents.add(subscription);
		//Do add the subscription even if it is a resource fail event,
		//otherwise new components added to the groups will not get
		//that added subscription forwarded
		
		Object[] references = myReferences.values().toArray();
		
		/*#%*/ String logMessage = 
		/*#%*/ 		"GroupElement-addInfrastructureSensor at "
		/*#%*/ 		+ myId
		/*#%*/ 		+ ":"
		/*#%*/ 		+ replicaNumber
		/*#%*/ 		+ " says: I'm adding a new infrastructure watcher listening to "
		/*#%*/ 		+ subscription.getEventName()
		/*#%*/ 		+ " with id: "
		/*#%*/ 		+ subscription.getSinkId()
		/*#%*/ 		+ " to my "
		/*#%*/ 		+ references.length
		/*#%*/ 		+ " references\n";

		if (replicaNumber < 1) {

			SNR thing;
			NodeRef nodeRef;
			NicheId destination;
			DelegationRequestMessage drm;

			if (subscription.getEventName().equals(
					ComponentFailEvent.class.getName())) {

				// The group is already monitoring its resources, so let's add
				// new subscribers to the existing fail-sensors!

				DelegationRequestMessage dm;
				
				 SensorSubscription s;
				 for (int i = 0; i < references.length; i++) {								
						
					 thing = (SNR)references [i];
					 //TODO: replace with id?
					 destination = niche.getResourceManager().getSuccessorNodeContainerId(thing.getId().getLocation()); //niche.getCloseNodeId(thing.getResourceRef().getDKSRef(), myId, NicheId.TYPE_SUBSCRIPTION, false);
					 //Remember, the source is the _real_ component that we are
					 //interested in, therefore source = thing.getId()
					 s = new SensorSubscription(
							 thing.getId(),
							 groupId.getId(), //thing.getId() or subscription.getSinkId(), if doing the brokering style...
							 subscription.getSinkId(),
							 subscription.getEventName(),
							 thing.getResourceRef().getNodeRef()
					);
									
					 
					dm = new DelegationRequestMessage(				
						destination,
						DelegationRequestMessage.TYPE_SENSOR,
						new Serializable[] {s}
						);
				 
		
					/*#%*/ logMessage +=
					/*#%*/ 		 " The parameters are, node of c being watched: "
					/*#%*/ 		 + s.getNodeOfSource()
					/*#%*/ 		 + " sink: "
					/*#%*/ 		 + s.getSinkId()
					/*#%*/ 		 + "\n";
					 
					 niche.sendToManagement(destination, dm, (-1 < replicaNumber));								
		 
				 }

			} else {

				if (subscription.getEventName().equals(ResourceLeaveEvent.class.getName())
					||
					subscription.getEventName().equals(CreateGroupEvent.class.getName())) {
				
					for (int i = 0; i < references.length; i++) {

						// System.out.println("Adding sensor to ref no "+i);
						thing = (SNR) references[i];
						nodeRef = thing.getResourceRef().getNodeRef();

						destination = niche.getNicheId(
								thing.getId(),
								thing.getId().getOwner(),
								NicheId.TYPE_SUBSCRIPTION,
								false								
						);
						
						// Remember, the source is the _real_
						// component that we are
						// interested in,
						// therefore source = thing.getId()
						Serializable[] params = new Serializable[] {
								new SensorSubscription(
										thing.getId(),
										myId,
										subscription.getSinkId(),
										subscription.getEventName(),
										nodeRef
								)
						};

						drm = new DelegationRequestMessage(
								destination,
								DelegationRequestMessage.TYPE_SENSOR,
								params
						);

						/*#%*/ logMessage +=
						/*#%*/ 		"GroupElement-addInfrastructureSensor "
						/*#%*/ 		+" says: The parameters are, node of c being watched: "
						/*#%*/ 		+ nodeRef.getDKSRef().getId()
						/*#%*/ 		+"\n";
						
						niche.sendToNode(
								nodeRef.getDKSRef(),
								thing.getId(),
								drm,
								null,
								new NodeSendClass(
										niche,
										nodeRef.getDKSRef(),
										drm,
										null
								) 
						);
						
					}
				} else if (subscription.getEventName().equals(
														MemberAddedEvent.class.getName()
														)
						  ) {
					/*#%*/ logMessage += "GroupElement-addInfrastructureSensor says: added watcher for MemberAddedEvent! Sink is "
					/*#%*/ 				+ subscription.getSinkId();
					
				} else {
					/*#%*/ logMessage += "GroupElement-addSink says: Error! unsupported watcher for event of type "
					/*#%*/ 	+ subscription.getEventName();
					
					System.err.println(
							"GroupElement-addSink says: Error! unsupported watcher for event of type "
							+ subscription.getEventName()
					);
				}
			}
		}/*#%*/  else {
		/*#%*/ 	logMessage += "GroupElement-addInfrastructureSensor says: Not really adding. I'm a replica";

		/*#%*/ }
		
		/*#%*/ niche.log(logMessage);
	}

	protected void addClientBinding(BindId b) {
		// TODO - is it always ok to cast to string..?
		myClientSideBindingsADLToGlobal.put((String) b.getSenderSideInterfaceDescription(), b);
		myClientSideBindingsGlobalToCache.put(b.getId().toString(), b);

		if (0 < replicaNumber) {
			/*#%*/ niche.log("GroupElement-addClientSideBinding: "
			/*#%*/ 		+ myId + ":" + replicaNumber
			/*#%*/ 		+ " void, since I'm a replica"
			/*#%*/ );

		} else {
			/*#%*/ String logMessage = "GroupElement-addClientSideBinding "
			/*#%*/ 					+ myId + ":" + replicaNumber
			/*#%*/ 					+ " is adding binding with id "
			/*#%*/ 					+ b.getId()
			/*#%*/ 					+ "\n";

			// Send out sensors!!
			DKSRef nodeOfRef;

			Object[] references = myReferences.values().toArray();
			SNR thing;

			for (int i = 0; i < references.length; i++) {

				thing = (SNR) references[i];

				// FIXME
				// nodeOfRef = thing.getDKSRef();
				// IdentifierInterface thing = things.get(0);
				nodeOfRef = thing.getResourceRef().getDKSRef();
				/*#%*/ logMessage += 
				/*#%*/ 		"Interface "
				/*#%*/ 		+ (String) b.getSenderSideInterfaceDescription()
				/*#%*/ 		+ " on component "
				/*#%*/ 		+ thing.getId()
				/*#%*/ 		+ " on node "
				/*#%*/ 		+ nodeOfRef
				/*#%*/ 		+ " is being bound to "
				/*#%*/ 		+ b.getReceiver().getId()
				/*#%*/ 		+"\n";
				
				niche.sendToNode(
						nodeOfRef,
						new BindRequestMessage(
								thing,
								b,
								1 /* IS sender */
							)
				);

			}
			/*#%*/ niche.log(logMessage);
			//System.out.println(logMessage);
			
		} // end else=prime replica
	}

	public void removeBinding(NicheId id) {
		BindId b = myClientSideBindingsGlobalToCache.remove(id.toString());

		if (replicaNumber < 1) {
			DKSRef nodeOfRef;

			Object[] references = myReferences.values().toArray();
			SNR thing;

			for (int i = 0; i < references.length; i++) {

				thing = (SNR) references[i];

				// FIXME, test
				nodeOfRef = thing.getResourceRef().getDKSRef();
				/*#%*/ niche.log(
				/*#%*/ 		"GroupElement-removeBinding"
				/*#%*/ 		+ myId + ":" + replicaNumber
				/*#%*/ 		+ "says: Sending a remove request to remove "
				/*#%*/ 		+ b.getId()
				/*#%*/ 		+ " from "
				/*#%*/ 		+ thing.getId()
				/*#%*/ );
				// Should really be to the SNRs / the SNRs should be resolved
				niche
						.sendToNode(
								nodeOfRef,
								new UpdateManagementElementMessage(
										thing.getId(),
										UpdateManagementElementMessage.TYPE_REMOVE_BINDING,
										b
								)
						);

			}
		}
	}

	private void removeGroup() {
		Object[] references = myReferences.values().toArray();
		// SNRElement thing;
		ComponentId componentId;
		NodeRef nodeRef;
		NicheId destination;
		/*#%*/ niche.log("GroupElement-removeGroup "
		/*#%*/ 		+ myId + ":" + replicaNumber
		/*#%*/ 		+ " says: I'm removing myself!!"
		/*#%*/ );

		//Garbage collection - do this regardless whether u are the prime replica or not... 
		for (int i = 0; i < references.length; i++) {

			/*#%*/ niche.log("Removing _fail_ sensor from ref no " + i
			/*#%*/ 		+ " (remove more later-FIXME)"); // FIXME
			componentId = (ComponentId) references[i];
			nodeRef = componentId.getResourceRef().getNodeRef();
			
			destination = niche.getResourceManager().getSuccessorNodeContainerId(componentId.getId().getLocation()); //niche.getCloseNodeId(nodeRef.getDKSRef(), myId, NicheId.TYPE_SUBSCRIPTION, false);
			// Warning - normally msg.destination == destination, now
			// msg.destination == groupId
			niche.sendToManagement(
					destination,
					//FIXME:
					//Sorry, out of date: must be parsed at receiver side
					new UpdateManagementElementMessage(
							destination,
							UpdateManagementElementMessage.TYPE_REMOVE_SINK,
							new Object[] { nodeRef, myId }
					),
					false //this is only sending to functional components on nodes
			);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	public NicheId getId() {
		return myId;
	}

	public Object[] getReferences() {
		return myReferences.entrySet().toArray();
	}

	public SNR getAny() {
		// Testing only..
		return (SNR) (myReferences.values().toArray())[1]; // FIXME
	}
	
	public SNR getAny(int index) {
		// Testing only..
		return (SNR) (myReferences.values().toArray())[index % myReferences.size()]; // FIXME
	}

	public HashMap getAll() {
		return (HashMap) myReferences.clone(); // TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#eventHandler(dks.arch.Event)
	 */
	public void eventHandler(Serializable event) {
		eventHandler(event, 0);

	}
	public void eventHandler(Serializable event, int flag) {
		
		if (event instanceof ComponentFailEvent) {
			boolean relevant = removeReference(((ComponentFailEvent) event)
					.getFailedComponentId());
			if (relevant && failureEventForwarding) {
				trigger((ConfigurationEvent) event); // inform potential
				// listeners!
			}
			// if(myReferences.isEmpty()) {
			// cleenUp
			// }
		}
	}

	public DelegationRequestMessage transfer(int mode) {

		Serializable[] applicationParameters = new Serializable[] {
				groupId,
				myReferences,
				myServerSideBindingsADLToStub,
				myClientSideBindingsADLToGlobal,
				myClientSideBindingsGlobalToCache,
				mySinks,
				mySinksForSNREvents,
				mySources
				
		};

		return new DelegationRequestMessage(
							myId,
							DelegationRequestMessage.TYPE_GROUP_ID,
							this.getClass().getName(),
							applicationParameters
					);
	}

	// Below is garbage:

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.IdentifierInterface#getReferencedNodeId()
	 */
	public DKSRef getDKSRef() {
		// TODO Auto-generated method stub
		return null;
	}

}


