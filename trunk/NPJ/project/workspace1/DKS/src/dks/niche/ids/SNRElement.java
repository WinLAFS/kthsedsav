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

import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ConfigurationEvent;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.interfaces.ExecutorInterface;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.WatcherInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.sensors.CreateGroupSensor;
import dks.niche.sensors.ResourceLeaveSensor;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.ResourceRef;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;

/**
 * The <code>SNRElement</code> class
 * 
 * @author Joel
 * @version $Id: SNRElement.java 294 2006-05-05 17:14:14Z joel $
 */
public class SNRElement implements Serializable, IdentifierInterface {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -5705451284574337707L;

	public static final int TYPE_SINGLE = 0;

	public static final int TYPE_GROUP = 1;

	int type;

	// only for caching:
	protected ResourceRef currentComponentLocation;

	protected Object[] referenceHolder;

	// referenceHolder is the ArrayList of SNRs, or the

	//Subscription myResourceFailSubscription;
	boolean replicateFailSensors = true;
	
	ArrayList<WatcherInterface> mySinks;

	ArrayList<Subscription> mySinksForSNREvents;
	
	ArrayList<ExecutorInterface> mySources;

//	ArrayList<Subscription> mySourcesForSNREvents;

	HashMap<String, BindId> myClientSideBindingsADLToGlobal;

	HashMap<String, BindId> myClientSideBindingsGlobalToCache;

	HashMap<String, BindId> myServerSideBindingsADLToStub;
	
	
	protected transient NicheAsynchronousInterface niche;

	// transient NicheMESupportInterface niche;

	protected NicheId myId;

	protected int replicaNumber;

	public SNRElement() {
	}

	protected void internalConnect(NicheId id, int replicaNumber, NicheManagementInterface myHost) {
		this.myId = id;
		this.replicaNumber = replicaNumber;
		this.niche = myHost.getNicheAsynchronousSupport();
	}
	
	protected void addSink(SensorSubscription s) {

		addInfrastructureSensor(s);

	}

	protected void trigger(ConfigurationEvent e) {

		/*#%*/ String logMessage = "";
		String notifyMessage = "";
		
		String type = e.getClass().getName();
		
		boolean match = false;
		
		if (replicaNumber < 1) {

			// DeliverEventMessage message;

			for (Subscription sink : mySinksForSNREvents) {
				
				if (sink.getEventName().equals(type)) {
					
					match = true;
					
					NicheId destination = sink.getSinkId();
					notifyMessage = destination.toString();
					
					niche.sendToManagement(
							destination,
							new DeliverEventMessage(destination, (Serializable)e),
							destination.isReliable()
					);
				}
			}
		}
		
		/*#%*/ if(!match) {
			
			/*#%*/ if(replicaNumber < 1) {
				/*#%*/ logMessage = "SNRElement " + myId + ":" + replicaNumber
				/*#%*/ 			+ " isn't informing anyone about "
				/*#%*/ 			+ type
				/*#%*/ 			+ " since none of my "
				/*#%*/ 			+ mySinksForSNREvents.size()
				/*#%*/ 			+ " subscribers were interested"; 
			/*#%*/ } else {
				/*#%*/ logMessage = "SNRElement " + myId + ":" + replicaNumber
				/*#%*/ + " is a replica, and is therefore not informing anyone about "
				/*#%*/ + type; 				
			/*#%*/ }
			/*#%*/ } else {
			
			/*#%*/ logMessage = "SNRElement " + myId + ":" + replicaNumber
			/*#%*/ + " informed "
			/*#%*/ + notifyMessage
			/*#%*/ + " about "
			/*#%*/ + type;
			/*#%*/ }
		
		/*#%*/ niche.log(logMessage);
		
	}

	protected void addInfrastructureSensor(Subscription subscription) {
		System.err
				.println("SNRElement says: This is wrong, should be overwritten by Group-or-Component-Id");
		mySinksForSNREvents.add(subscription);
	}

	// public BindElement addBinding(BindElement b) {
	// BindElement existingBinding = myBindings.get(b.interfaceDescription);
	// if(existingBinding != null) {
	// return existingBinding;
	// }
	// b.setId(niche.getCollocatedId(myRingId));
	// myBindings.put(b.getInterfaceDescription(), b);
	// return b;
	// }

	public SNR getAny(int i) {
		//Must be overwritten!!
		System.err.println("Must be overwritten!!");
		return null;
		
		
	}

	public Serializable getAll() { // Ugly, but convenient -
		return new Serializable[] { this };
	}

	public ResourceRef getResourceRef() {
		return currentComponentLocation;
	}

	public SNRElement setResourceRef(ResourceRef resourceRef) {
		currentComponentLocation = resourceRef;
		return this;
	}

	// you have to know or introspect whether you asked a single-sNRElement or a group


	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	public NicheId getId() {
		return myId;
	}


	public HashMap getPredefinedReceiverBindings() {
		return myServerSideBindingsADLToStub;
	}
	
	protected void addClientBinding(BindId bindId) {
		System.err.println("MUST BE OVERWRITTEN");
	}
	
	public void addServerBinding(String receiverSideInterfaceDescription, int type) {

		BindId cachedBindInfo = new BindId();
		receiverSideInterfaceDescription = (niche.getResourceManager().wrapInterfaceDescriptions("", receiverSideInterfaceDescription, type))[1];
		cachedBindInfo.setReceiverSideInterfaceDescription(receiverSideInterfaceDescription);
		myServerSideBindingsADLToStub.put(receiverSideInterfaceDescription, cachedBindInfo);

	}

	public void removeBinding(NicheId id) {
		System.err.println("MUST BE OVERWRITTEN");
	}

	public DelegationRequestMessage transfer(int mode) {
		System.err.println("MUST BE OVERWRITTEN");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#eventHandler(java.lang.Object)
	 */
	public void eventHandler(Object event) {
		System.err.println("MUST BE OVERWRITTEN");
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#setReplicaNumber(int)
	 */
	public void setReplicaNumber(int replicaNumber) {
		System.err.println("MUST BE OVERWRITTEN");

	}

	public int getReplicaNumber() {
		return this.replicaNumber;
	}

	public boolean isReliable() {
		return 0 <= replicaNumber;
	}

	protected void activateInfrastructureSensors(
			IdentifierInterface thing,
			NodeRef nodeRef,
			ArrayList<DelegationRequestMessage> bulk) {

		//OBS nodeRef might be null
		
	NicheId destination;
	Serializable[] params;
	
	ArrayList<SensorSubscription> failSensorSubscriptions = new ArrayList<SensorSubscription>();
	
	// Always add the fail-subscription : fail-sensors
	
	//TODO
	//nodeRef is null, if the activate-method wasn't given a cached Ref!
	
	failSensorSubscriptions.add(
			//always include a fail-sensor subscription!
		new SensorSubscription(
				thing.getId(),
				myId,
				myId,
				ComponentFailEvent.class.getName(),
				nodeRef
			)
	);
	
	DelegationRequestMessage drm;
	
	for (Subscription subscription : mySinksForSNREvents) {
	
		if (subscription.getEventName().equals(
				ResourceLeaveEvent.class.getName())
				|| subscription.getEventName().equals(
						CreateGroupEvent.class.getName())) {
	
			String sensorClassName;
			if (subscription.getEventName().equals(
					ResourceLeaveEvent.class.getName())) {
				sensorClassName = ResourceLeaveSensor.class.getName();
			} else {
				sensorClassName = CreateGroupSensor.class.getName();
			}
			
			destination = niche.getResourceManager().getContainterId(thing.getId().getLocation()); // getUniqueCollocatedId(thing.getId());
			// Remember, the source is the _real_ component that we are
			// interested in, therefore source = thing.getId()
			params = new Serializable[] { 
					new SensorSubscription(
							thing.getId(),
							myId,
							subscription.getSinkId(),
							subscription.getEventName(),
							nodeRef
						)
					};
			
			drm = new DelegationRequestMessage(destination,
					DelegationRequestMessage.TYPE_SENSOR,
					params);
			
			bulk.add(drm);
			
		} else if (subscription.getEventName().equals(ComponentFailEvent.class.getName())) {
			
			//this is if we have some other component listening for
			//failures through us!
			
			failSensorSubscriptions.add(
					new SensorSubscription(
							thing.getId(),
							subscription.getSinkId(),
							subscription.getSinkId(),
							subscription.getEventName(),
							nodeRef
						)
				);
		}
		
		
	}
	
	// Always add the fail-subscription : fail-sensors
	
	destination = niche.getResourceManager().getSuccessorNodeContainerId(((SNR)thing).getResourceRef().getDKSRef().getId().toString()); //getCloseNodeId(((SNR)thing).getResourceRef().getDKSRef(), myId, NicheId.TYPE_SUBSCRIPTION, false);
	// Remember, the source is the _real_ component that we are interested
	// in, therefore source = thing.getId()
	params = new Serializable[] {
				failSensorSubscriptions
			};
	
	drm = new DelegationRequestMessage(
			destination,
			DelegationRequestMessage.TYPE_SENSOR,
			params
	);
	
	if (replicaNumber < 1) {
		/*#%*/ niche.log(
		/*#%*/ 		"SNRElement "
		/*#%*/ 		+ myId + ":" + replicaNumber 
		/*#%*/ 		+ " says: sending fail-sensor to node responsible for "
		/*#%*/ 		+ destination
		/*#%*/ 		+ " with replication set to "
		/*#%*/ 		+ replicateFailSensors
		/*#%*/ 		//+ (-1 < replicaNumber)
		/*#%*/ );
		niche.sendToManagement(destination, drm, replicateFailSensors);
	}
	//failSensors.put(thing.getId().toString(), destination);

}

	
}
