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

import dks.messages.Message;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.events.SNRUpdatedEvent;
import dks.niche.interfaces.ExecutorInterface;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.WatcherInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;
import dks.niche.sensors.CreateGroupSensor;
import dks.niche.sensors.ResourceLeaveSensor;
import dks.niche.wrappers.ExecutorInfo;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;
import dks.niche.wrappers.WatcherInfo;

/**
 * The <code>ComponentId</code> class
 *
 * @author Joel
 * @author Ahmad
 * @version $Id: ResourceId.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class ComponentElement
		extends SNRElement
		implements
			Serializable,
			IdentifierInterface,
			ManagementElementInterface {
	
	
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 1134115633056027865L;

	ComponentId componentId;

	//Remember the empty constructor...
	public ComponentElement() {
		
	}
	
	
	

//	
//	public void removeClientBinding(NicheId id) {
//		BindId b = myClientSideBindingsGlobalToCache.get(id.toString());
//		DKSRef nodeOfRef;		
//		nodeOfRef = currentComponentLocation.getDKSRef();
//		niche.log("ComponentId-removeBinding says: Sending a remove request to remove "+ b.getId() +" from " + componentId.getRealComponentId() );
//		niche.sendToNode(nodeOfRef, new UpdateManagementElementMessage(componentId.getRealComponentId(), UpdateManagementElementMessage.TYPE_REMOVE_BINDING, b));
// 
//		
//		
//	}

	
	/* (non-Javadoc)
	 * @see dks.niche.wrappers.SNR#getAll()
	 */
	@Override
	public SNRElement[] getAll() {
		return new SNRElement[]{this};
	}
	/* (non-Javadoc)
	 * @see dks.niche.wrappers.SNR#getAny()
	 */
	@Override
	public SNR getAny(int i) {
		return componentId;
	}
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#eventHandler(dks.arch.Event)
	 */
	public void eventHandler(Serializable event) {
		// TODO Auto-generated method stub
		
	}
	public void eventHandler(Serializable event, int flag) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#messageHandler(dks.messages.Message)
	 */
	public void messageHandler(Message message) {
		if(message instanceof UpdateSNRRequestMessage) {
			UpdateSNRRequestMessage m = (UpdateSNRRequestMessage) message;
			switch (m.getType()) {

			case UpdateSNRRequestMessage.TYPE_ADD_WATCHER:
				
				addSink(new WatcherInfo(m.getReference().getId(), m.getTouchpointClassName(), m.getTouchpointParameters()));
				
				break;
				
			case UpdateSNRRequestMessage.TYPE_ADD_EXECUTOR:
				
				addSource(new ExecutorInfo(m.getReference().getId(), m.getTouchpointClassName(), m.getTouchpointParameters()));
				
				break;
				
			case UpdateSNRRequestMessage.TYPE_REMOVE_WATCHER:
				
				break;
				
			case UpdateSNRRequestMessage.TYPE_ADD_CLIENT_BINDING:
				addClientBinding((BindId)m.getReference());
				break;
				
			case UpdateSNRRequestMessage.TYPE_REMOVE_BINDING:
				removeBinding(m.getReference().getId());
				break;

			case UpdateSNRRequestMessage.TYPE_UNDEPLOY_COMPONENT:
				//FIXME: undeployComponent();
				break;
				/*#%*/ default:
				/*#%*/ niche.log("ComponentId says, ERROR, cannot handle update message of type "+m.getType());
				/*#%*/ break;
			}
		}
		else if (message instanceof UpdateManagementElementMessage ){
			UpdateManagementElementMessage m = (UpdateManagementElementMessage ) message;
			switch (m.getType()) {

			case UpdateManagementElementMessage.TYPE_ADD_SINK:
				/*#%*/ niche.log("ComponentId "+myId+" says: I'm adding a watcher listening for infrastructure events");
				addInfrastructureSensor((Subscription)m.getReference());
				
				break;
				
				/*#%*/ case UpdateManagementElementMessage.TYPE_REMOVE_SINK:
				/*#%*/ niche.log("ComponentId says, ERROR, REMOVE_SINK not implemented");
				/*#%*/ break;
				
			
				/*#%*/ default:
				/*#%*/ niche.log("GroupId says, ERROR, cannot handle update message of type "+m.getType());
				/*#%*/ break;
		}

		}
		/*#%*/ else {
		/*#%*/ 	niche.log("GroupId says, ERROR, cannot handle message of type "+message.getClass().getName());
		/*#%*/ }
	}
	
	private void addSink(WatcherInterface ws) {
		mySinks.add(ws);

		//DelegationRequestMessage m = ws.getSensorMessage();
			
		//Send out sensors!!
		NicheId idOfCollocatedSensor;
		
		/*#%*/ niche.log("ComponentId-addSink says: I'm adding a new watcher with id: "+ws.getId() + " to my reference, and sending a sensor-install-message");

		idOfCollocatedSensor = niche.getNicheId(componentId.getRealComponentId().getId(), myId.getOwner(), NicheId.TYPE_SENSOR, false);
		//here, the interesting thing _is_ the physical node, not the id. we do not want another layer of indirection
		niche.sendToManagement(currentComponentLocation.getDKSRef(), idOfCollocatedSensor, ws.getSensorMessage(idOfCollocatedSensor, componentId, componentId.getComponentName()));
 	}
	
	private void addSource(ExecutorInterface es) {
		mySources.add(es);

		//DelegationRequestMessage m = ws.getSensorMessage();
			
		//Send out actuator!!
		NicheId idOfCollocatedActuator;
		
		/*#%*/ niche.log("ComponentId-addSource says: I'm adding a new executor with id: "+es.getId() + " to my reference, and sending an actuator-install-message");

		idOfCollocatedActuator = niche.getNicheId(componentId.getRealComponentId().getId(), myId.getOwner(), NicheId.TYPE_ACTUATOR, false);
		//here, the interesting thing _is_ the physical node, not the id. we do not want another layer of indirection
		niche.sendToManagement(currentComponentLocation.getDKSRef(), idOfCollocatedActuator, es.getActuatorMessage(idOfCollocatedActuator, componentId, componentId.getComponentName()));
 	}


	//NOT Provided by the sNRElement!
	@Override
	protected void addInfrastructureSensor(Subscription subscription) {
		
		mySinksForSNREvents.add(subscription);
		
		DelegationRequestMessage drm;
		/*#%*/ niche.log("ComponentId-addSink says: I'm adding a new infrastructure watcher listening to "+subscription.getEventName()+" with id: "+subscription.getSinkId() + " to my reference");
		SensorSubscription s;
		
		if(subscription.getEventName().equals(ComponentFailEvent.class.getName())) {
			
			NicheId destination = niche.getResourceManager().getSuccessorNodeContainerId(componentId.getId().getLocation()); //niche.getCloseNodeId(currentComponentLocation.getDKSRef(), myId, NicheId.TYPE_SUBSCRIPTION, false);
			//Remember, the source is the _real_ component that we are interested in, therefore source = thing.getId()
			 s = new SensorSubscription(
					 componentId.getRealComponentId().getId(),
					 myId,
					 subscription.getSinkId(),
					 subscription.getEventName(),
					 currentComponentLocation.getNodeRef()
			);
			
			drm = new DelegationRequestMessage(destination, DelegationRequestMessage.TYPE_SENSOR, s);
			
			/*#%*/ 	niche.log("ComponentId-addInfrastructureSensor says: The parameters are, node of c being watched: "+ s.getNodeOfSource() + " sink: "+s.getSinkId());
			niche.sendToManagement(destination, drm, true); //do replicate fail-subscriptions
									
		} else {
			
			/*#%*/ niche.log("ComponentId-addInfrastructureSensor says: Type of subscription: "+subscription.getEventName()+" vs "+ CreateGroupEvent.class.getName() +" => "+subscription.getEventName().equals(CreateGroupEvent.class.getName()));
			if (subscription.getEventName().equals(ResourceLeaveEvent.class.getName()) || subscription.getEventName().equals(CreateGroupEvent.class.getName())) {

				String sensorClassName;
				if(subscription.getEventName().equals(ResourceLeaveEvent.class.getName())) {
					sensorClassName = ResourceLeaveSensor.class.getName();
				} else	{
					sensorClassName = CreateGroupSensor.class.getName();
				} 
				/*#%*/ 	niche.log("ComponentId-addInfrastructureSensor says: sensorClassName: "+sensorClassName);
				
				NicheId destination = niche.getNicheId(componentId.getRealComponentId().getId(), myId.getOwner(), NicheId.TYPE_SENSOR, false);
				//Remember, the source is the _real_ component that we are interested in, therefore source = thing.getId()
				s = new SensorSubscription(componentId.getRealComponentId(), myId, subscription.getSinkId(), subscription.getEventName(), currentComponentLocation.getNodeRef());
					
				drm = new DelegationRequestMessage(destination, DelegationRequestMessage.TYPE_SENSOR, s);
					
				/*#%*/ niche.log("ComponentId-addInfrastructureSensor says: The parameters are, node of c being watched: "+ currentComponentLocation.getDKSRef().getId() );
				niche.sendToManagement(currentComponentLocation.getDKSRef(), componentId.getRealComponentId(), drm);
			
			} /*#%*/ else if (subscription.getEventName().equals(SNRUpdatedEvent.class.getName())) {
			/*#%*/ niche.log("ComponentId-addInfrastructureSensor says: added watcher for SNRChanged-event! Sink is "+subscription.getSinkId());
			/*#%*/ }
			else {
				System.err.println("ComponentId-addSink says: Error! unsupported watcher");
			}
		}
			
		
	}
	
	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#connect(dks.niche.wrappers.NicheId, dks.niche.interfaces.NicheActuatorInterface, java.lang.Serializable[])
	 */
	public void connect(NicheId id, int replicaNumber, NicheManagementInterface myHost, NicheNotifyInterface initiator) {
		internalConnect(id, replicaNumber, myHost);
		
	}
	
	public void init(Serializable[] applicationParameters) {
		//addReferenceInternal
		componentId = (ComponentId)applicationParameters[0];
		currentComponentLocation = componentId.currentAllocatedResource;
		/*#%*/ niche.log("ComponentId-SNRElement created, with "+currentComponentLocation +" and " + componentId.getRealComponentId() );
		
		
	}
	public void reinit(Serializable[] applicationParameters) {
		//addReferenceInternal
		componentId = (ComponentId)applicationParameters[0];
		/*#%*/ niche.log("ComponentId-SNRElement re-created. Which now means that " +
		/*#%*/ 		"this is only a placeholder for a component that once was, since " +
		/*#%*/ 		"the current functional components are immobile..."
		/*#%*/ );
		
		
	}
	
	//Do we want this:
	public DelegationRequestMessage transfer(int mode) {

		Serializable[] applicationParameters = new Serializable[] {
				componentId,
				myServerSideBindingsADLToStub,
				myClientSideBindingsADLToGlobal,
				myClientSideBindingsGlobalToCache,
				mySinks,
				mySinksForSNREvents,
				mySources
				
		};

		return new DelegationRequestMessage(
							myId,
							DelegationRequestMessage.TYPE_COMPONENT_ID,
							this.getClass().getName(),
							applicationParameters
					);
	}

	

}
