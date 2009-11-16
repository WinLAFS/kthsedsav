/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.sensors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.fd.events.SuspectEvent;
import dks.niche.events.ConfigurationEvent;
import dks.niche.events.ComponentFailEvent;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.wrappers.SensorSubscription;
import dks.ring.events.RingLeaveEvent;

/**
 * The <code>ResourceWatcherId</code> class
 *
 * @author Joel
 * @version $Id: ResourceWatcherId.java 294 2006-05-05 17:14:14Z joel $
 */
public class SystemSensor  {

	// Could we please say that the sensors are only instantiated at the destination, so we
	// don't have to deal with naming and moving?? 
	
	//NicheManagementContainerComponent niche;
	//int eventType;
	//DKSRef supervisedResource;
	
	String localResourceLeaveEvent = RingLeaveEvent.class.getName();
	String localResourceFailEvent = SuspectEvent.class.getName();
	
	
	protected NicheAsynchronousInterface niche;
	
	//Explanation: HashMap<EventName, HashMap<ComponentOfInterestIdAsString, ArrayList<SensorSubscription>>>
	protected HashMap<String, HashMap<String, ArrayList<SensorSubscription>>> sinks;
	
	//Explanation: HashMap<NodeIdAsString, HashMap<(ComponentOfInterestIdAsString+SinkIdAsString), SensorSubscription>>	
	protected HashMap<String, HashMap<String, SensorSubscription>> resourceFailSinks;
	
	DKSRef node;
	
	public SystemSensor(DKSRef node, NicheManagementInterface host) {

		this.node = node;
		this.niche = host.getNicheAsynchronousSupport();
		this.sinks = new HashMap<String, HashMap<String,ArrayList<SensorSubscription>>>();
		this.resourceFailSinks = new HashMap<String, HashMap<String,SensorSubscription>>();

	}
	
	
//	public NicheManagementContainerComponent getNicheEventHandlerComponent() {
//		return niche;
//	}
//
//
//	public void setNicheEventHandlerComponent(NicheManagementContainerComponent niche) {
//		this.myHost = niche;
//	}

//
//	public ResourceId getResourceId() {
//		return myComponentId.getResourceId();
//	}
//
//
//	public void setResourceId(ResourceId resourceId) {
//		this.myComponentId.setResourceId(resourceId);
//	}

	
	public void eventHandler(Object event) {
		if(event.getClass().getName().equals(localResourceFailEvent)) {
			resourceFailEventHandler((Event)event);
		} else {
			genericEventHandler((ConfigurationEvent)event);
		} 
	}

	private void resourceFailEventHandler(Event e) {
		
		//is this the node we're interested in?
		DKSRef suspectedPeer = ((SuspectEvent)e).getSuspectedPeer();
		String suspectedNodeId = suspectedPeer.getId().toString();
		
		/*#%*/ niche.log("ResourceSensor says: Peer "+suspectedNodeId+ " is suspected");
		
		/*#%*/ String logMessage = "ResourceSensor tells more about "+suspectedNodeId;
		
		//ArrayList<SensorSubscription> at = mySinks.get(suspectedNodeId);
		
		HashMap<String, SensorSubscription> allSubscriptionsForFailedNode
			= resourceFailSinks.get(suspectedNodeId); 
		
		if(allSubscriptionsForFailedNode != null) {
			//Remember, one of the subscribers should be the group(s) to which the component belonged!
			Object[] allSubscriptionsForFailedNodeAsArray = allSubscriptionsForFailedNode.values().toArray();
			
			/*#%*/ logMessage += ": I inform my "
			/*#%*/ 	+ allSubscriptionsForFailedNodeAsArray.length
			/*#%*/ 	+" subscribers about the failure\n";
			
			NicheId sinkId;
			for (int i = 0; i < allSubscriptionsForFailedNodeAsArray.length; i++) {
				
				SensorSubscription subscription =(SensorSubscription)allSubscriptionsForFailedNodeAsArray[i];
				sinkId = subscription.getSinkId();
				
				/*#%*/ logMessage +=
				/*#%*/ 	"Sending to all copies of "
				/*#%*/ 	+ sinkId
				/*#%*/ 	+ " about entity "
				/*#%*/ 	+ subscription.getSourceId()
				/*#%*/ 	+ "\n";
				
				niche.sendToManagement(
						subscription.getSinkId(),
						new DeliverEventMessage(
								subscription.getSinkId(),
								new ComponentFailEvent(
										subscription.getSourceId(),
										subscription.getBroker().getId(),
										suspectedPeer
								)
						),
						sinkId.isReliable() || sinkId.getType() == NicheId.TYPE_GROUP_ID //replication!!
				);
				
			}
			 
		} /*#%*/ else {
		/*#%*/ 	logMessage += " But I have none to inform";
		/*#%*/ }
		
		/*#%*/ niche.log(logMessage);
	}

	private void genericEventHandler(ConfigurationEvent event) {
		
		String type = event.getClass().getName();
		/*#%*/ niche.log("SystemSensor says: got event of type "+ type);
		
		/*#%*/ String logMessage = "SystemSensor tells more about handling of the event: ";
		
		//ArrayList<SensorSubscription> at = mySinks.get(suspectedNodeId);
		
		 HashMap<String, ArrayList<SensorSubscription>> allSubscriptionsForCurrentEventType =
			 sinks.get(type);
		 
		 if(allSubscriptionsForCurrentEventType != null) {
			 
			ArrayList<SensorSubscription> allSubscriptionsForCurrentEvent =
				allSubscriptionsForCurrentEventType.get(event.getSource());
		 
			if(allSubscriptionsForCurrentEvent != null) {
			
				/*#%*/ logMessage += ": I inform my "
				/*#%*/ + allSubscriptionsForCurrentEvent.size()
				/*#%*/ +" subscribers about the event\n";
			
			for (SensorSubscription subscription : allSubscriptionsForCurrentEvent) {
				
				/*#%*/ logMessage +=
				/*#%*/ 	"Sending to "
				/*#%*/ 	+ subscription.getSinkId()
				/*#%*/ 	+ " about entity "
				/*#%*/ 	+ subscription.getSourceId()
				/*#%*/ 	+ "\n";
				
				niche.sendToManagement(
						subscription.getSinkId(),
						new DeliverEventMessage(
								subscription.getSinkId(),
								(Serializable)event.setBroker(subscription.getBroker().getId())
						),
						subscription.getSinkId().isReliable()
				);
				
			}
			 
		} /*#%*/ else {
			/*#%*/ logMessage += " But I have none to inform - none cared about " + event.getSource();
			/*#%*/ }
		} /*#%*/ else {
		/*#%*/ logMessage += " But I have none to inform - none cared about " + event.getSource();
		/*#%*/ }
		
		/*#%*/ niche.log(logMessage);
		
	}
	
	
//	public void messageHandler(Message message){
//		if(message instanceof UpdateManagementElementMessage) {
//			handleUpdateMEMessage((UpdateManagementElementMessage)message);
//		}
//	}

	
//	public synchronized void handleUpdateMEMessage(UpdateManagementElementMessage m) {
//	
//		if (m.getType() == UpdateManagementElementMessage.TYPE_ADD_SINK) {
//			
//			SensorSubscription myNewSubscription = (SensorSubscription)m.getReference();
//			addSink(myNewSubscription);
//		}
//		
//		else if (m.getType() == UpdateManagementElementMessage.TYPE_REMOVE_SINK) {
//			//parameters are: new Object[] { nodeRef, myId }
//			Object[] params = (Object[])m.getParameters();
//			SensorSubscription sensorSubscription = (SensorSubscription)params[0];
//			//FIXME! this is not what the group is currently sending
//			String nodeId = sensorSubscription.getNodeOfSource().getDKSRef().toString();
//				
//			String failSensorSignature =
//				getFailSensorSignature(sensorSubscription.getSourceId(), sensorSubscription.getSinkId());
//
//			mySinks.get(nodeId).remove(failSensorSignature);
//			
//			
//		}
//
//	}
	
	public void addSinks(ArrayList<SensorSubscription> sensorSubscriptions) {
		for (SensorSubscription sensorSubscription : sensorSubscriptions) {
			addSink(sensorSubscription);
		}
	}

	public void addSink(SensorSubscription newSubscription) {
		if(newSubscription.getEventName().equals(ComponentFailEvent.class.getName())) {
			addResourceFailSink(newSubscription);
		} else {
			addNormalSink(newSubscription);
		}
	}
	
	private void addNormalSink(SensorSubscription myNewSubscription) {

		String infrastructureSensorSignature =
			getInfrastructureSensorSignature(myNewSubscription.getSourceId(), myNewSubscription.getSinkId());
		
		String eventType = myNewSubscription.getEventName();
		HashMap<String, ArrayList<SensorSubscription>> subscriptionsForCurrentEventType = sinks.get(eventType);
		
		/*#%*/ String logMessage =
		/*#%*/ 		"SystemSensor says: I got a subscribe-request from "
		/*#%*/ 		+ myNewSubscription.getSinkId().toString()
		/*#%*/ 		+ " to monitor "
		/*#%*/ 		+ eventType
		/*#%*/ 		+ "s from "
		/*#%*/ 		+ myNewSubscription.getSourceId()
		/*#%*/ ;
		
		ArrayList<SensorSubscription> at;
		if(subscriptionsForCurrentEventType == null) {
			subscriptionsForCurrentEventType = new HashMap<String, ArrayList<SensorSubscription>>();
			at = new ArrayList<SensorSubscription>();
			at.add(myNewSubscription);
			subscriptionsForCurrentEventType.put(infrastructureSensorSignature, at);
			sinks.put(eventType, subscriptionsForCurrentEventType);
		} else {
			
			at = subscriptionsForCurrentEventType.get(infrastructureSensorSignature);
			
			if(at == null) {

				at = new ArrayList<SensorSubscription>();
				at.add(myNewSubscription);
				subscriptionsForCurrentEventType.put(infrastructureSensorSignature, at);

			} else {
				at.add(myNewSubscription);
			}
		}
		/*#%*/ niche.log(logMessage);

	}
	private void addResourceFailSink(SensorSubscription myNewSubscription) {
		
		String failSensorSignature =
			getFailSensorSignature(myNewSubscription.getSourceId(), myNewSubscription.getSinkId());
		
		String nodeId = myNewSubscription.getNodeOfSource().getDKSRef().getId().toString();
		HashMap<String, SensorSubscription> subscriptionsForCurrentNode = resourceFailSinks.get(nodeId);
		
		/*#%*/ String logMessage =
		/*#%*/ 		"ResourceSensor at "
		/*#%*/ 		+ node.getId()
		/*#%*/ 		+" says: I got a subscribe-request from "
		/*#%*/ 		+ myNewSubscription.getSinkId().toString()
		/*#%*/ 		+ " to monitor "
		/*#%*/ 		+ myNewSubscription.getSourceId()
		/*#%*/ 		+ " at "
		/*#%*/ 		+ nodeId
		/*#%*/ ;
		
		if(subscriptionsForCurrentNode == null) {
			subscriptionsForCurrentNode = new HashMap<String, SensorSubscription>();
			
			subscriptionsForCurrentNode.put(failSensorSignature, myNewSubscription);
			resourceFailSinks.put(nodeId, subscriptionsForCurrentNode);
		} else {
			if(subscriptionsForCurrentNode.containsKey(failSensorSignature)) {
				/*#%*/ logMessage += " but it's a duplicate, which I ignore";
			} else {
				subscriptionsForCurrentNode.put(failSensorSignature, myNewSubscription);
			}
		}
		/*#%*/ niche.log(logMessage);

	}

//UTILITY
	
	private String getInfrastructureSensorSignature(NicheId sourceId, NicheId sinkId) {
		return sourceId.toString();
	}
	private String getFailSensorSignature(NicheId sourceId, NicheId sinkId) {
		return sourceId.getLocation() + sinkId;
	}

}
		

