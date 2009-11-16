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
import dks.messages.Message;
import dks.niche.events.ComponentFailEvent;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.SensorInterface;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.SensorSubscription;
import dks.ring.events.RingLeaveEvent;

/**
 * The <code>ResourceWatcherId</code> class
 *
 * @author Joel
 * @version $Id: ResourceWatcherId.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceFailSensor implements SensorInterface  {

	// Could we please say that the sensors are only instantiated at the destination, so we
	// don't have to deal with naming and moving?? 
	
	//NicheManagementContainerComponent niche;
	//int eventType;
	//DKSRef supervisedResource;
	
	String localResourceLeaveEvent = RingLeaveEvent.class.getName();
	String localResourceFailEvent = SuspectEvent.class.getName();
	
	//boolean hasLeft;
	
	public ResourceFailSensor(DKSRef node, NicheManagementInterface host) {
		//super(node, host);
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

	
	
	public void resourceFailEventHandler(Event e) {
		
//		//is this the node we're interested in?
//		DKSRef suspectedPeer = ((SuspectEvent)e).getSuspectedPeer();
//		String suspectedNodeId = suspectedPeer.getId().toString();
//		
//		niche.log("ResourceSensor at " + node.getId() + " says: Peer "+suspectedNodeId+ " is suspected");
//		
//		String logMessage = "ResourceSensor at " + node.getId() + " tells more about "+suspectedNodeId;
//		
//		//ArrayList<SensorSubscription> at = mySinks.get(suspectedNodeId);
//		
//		HashMap<String, SensorSubscription> allSubscriptionsForFailedNode
//			= mySinks.get(suspectedNodeId); 
//		
//		if(allSubscriptionsForFailedNode != null) {
//			//Remember, one of the subscripers should be the group(s) to which the component belonged!
//			Object[] allSubscriptionsForFailedNodeAsArray = allSubscriptionsForFailedNode.values().toArray();
//			
//			logMessage += ": I inform my "
//				+ allSubscriptionsForFailedNodeAsArray.length
//				+" subscribers about the failure\n";
//			
//			for (int i = 0; i < allSubscriptionsForFailedNodeAsArray.length; i++) {
//				
//				SensorSubscription subscription =(SensorSubscription)allSubscriptionsForFailedNodeAsArray[i];
//				logMessage +=
//					"Sending to "
//					+ subscription.getSinkId()
//					+ " about entity "
//					+ subscription.getSourceId()
//					+ "\n";
//				
//				niche.sendToManagement(
//						subscription.getSinkId(),
//						new DeliverEventMessage(
//								subscription.getSinkId(),
//								new ComponentFailEvent(
//										subscription.getSourceId(),
//										suspectedPeer
//								)
//						)
//				);
//				
//			}
//			 
//		} else {
//			logMessage += " But I have none to inform";
//		}
//		
//		niche.log(logMessage);
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#eventHandler(dks.arch.Event)
	 */
	public void eventHandler(Object event) {
		resourceFailEventHandler((Event)event);
	}

	public void messageHandler(Message message){
		if(message instanceof UpdateManagementElementMessage) {
			handleUpdateMEMessage((UpdateManagementElementMessage)message);
		}
	}

	
	public synchronized void handleUpdateMEMessage(UpdateManagementElementMessage m) {
	
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

	}

	@Override
	public void addSink(SensorSubscription myNewSubscription) {
		
//		String failSensorSignature =
//			getFailSensorSignature(myNewSubscription.getSourceId(), myNewSubscription.getSinkId());
//		
//		String nodeId = myNewSubscription.getNodeOfSource().getDKSRef().getId().toString();
//		HashMap<String, SensorSubscription> subscriptionsForCurrentNode = mySinks.get(nodeId);
//		
//		String logMessage =
//				"ResourceSensor at "
//				+ node.getId()
//				+" says: I got a subscribe-request from "
//				+ myNewSubscription.getSinkId().toString()
//				+ " to monitor "
//				+ myNewSubscription.getSourceId()
//				+ " at "
//				+ nodeId
//		;
//		
//		if(subscriptionsForCurrentNode == null) {
//			subscriptionsForCurrentNode = new HashMap<String, SensorSubscription>();
//			
//			subscriptionsForCurrentNode.put(failSensorSignature, myNewSubscription);
//			mySinks.put(nodeId, subscriptionsForCurrentNode);
//		} else {
//			if(subscriptionsForCurrentNode.containsKey(failSensorSignature)) {
//				logMessage += " but it's a duplicate, which I ignore";
//			} else {
//				subscriptionsForCurrentNode.put(failSensorSignature, myNewSubscription);
//			}
//		}
//		niche.log(logMessage);

	}
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#init(java.lang.Object[])
	 */
	public void init(Object[] parameters) {
//		
//		String logMessage = "";
//		
//		if (parameters[0] instanceof ArrayList) {
//			// init
//			this.mySinks = new HashMap();
//			ArrayList<SensorSubscription> initalSubscriptions = (ArrayList)parameters[0];
//
//			this.mySupervisedNode = initalSubscriptions.get(0).getNodeOfSource();
//			
//			for (SensorSubscription sensorSubscription : initalSubscriptions) {
//				addSink(sensorSubscription);
//			}
//			
//			logMessage =
//				"Created fail-sensor "
//				+ myId + ":" +replicaNumber
//				+ " with"
//				+ initalSubscriptions.size()
//				+ " subscriptions ";
//			
//		} else if (parameters[0] instanceof SensorSubscription) {
//				// init
//				this.mySinks = new HashMap();
//				this.mySupervisedNode = ((SensorSubscription) parameters[0]).getNodeOfSource();
//				
//				addSink((SensorSubscription) parameters[0]);
//				
//				logMessage =
//					"Created fail-sensor "
//					+ myId + ":" +replicaNumber;
//				
//		}
//		niche.log(logMessage);
//		
	}
//
	public void reinit(Object[] parameters) {
//		
//		this.mySupervisedNode = (NodeRef)parameters[0];
//		this.mySinks = (HashMap)parameters[1];
//		
//		niche.log(
//				"Re-created fail-watcher "
//				+ myId + ":" +replicaNumber
//				);
//
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SensorInterface#getEventNames()
	 */
	public String getEventName() {
		
		return localResourceFailEvent;
		//No, that was wrong
		//return mySinks.keySet().toArray();
	}


	/* (non-Javadoc)
	 * @see dks.niche.sensors.SensorId#storeState()
	 */
//	@Override
//	public void storeState() {
//		// TODO Auto-generated method stub
//		
//	}


	public void setReplicaNumber(int replicaNumber) {
		//this.replicaNumber = replicaNumber;
		
	}
	public boolean isReliable() {
		//return 0 <= replicaNumber;
		return false;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SensorInterface#addSinks(java.util.ArrayList)
	 */
	@Override
	public void addSinks(ArrayList<SensorSubscription> ws) {
		// TODO Auto-generated method stub
		
	}



}
		

