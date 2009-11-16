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
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.SensorInterface;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.wrappers.SensorSubscription;
import dks.ring.events.RingLeaveEvent;

/**
 * The <code>ResourceWatcherId</code> class
 *
 * @author Joel
 * @version $Id: ResourceWatcherId.java 294 2006-05-05 17:14:14Z joel $
 */
public class ResourceLeaveSensor { //implements SensorInterface  {

	// Could we please say that the sensors are only instantiated at the destination, so we
	// don't have to deal with naming and moving?? 

	//NicheManagementContainerComponent niche;
	//int eventType;
	//DKSRef supervisedResource;
	
	String localResourceLeaveEvent = RingLeaveEvent.class.getName();
	String localResourceFailEvent = SuspectEvent.class.getName();
	HashMap  mySinks;
	protected NicheAsynchronousInterface niche;
	protected DKSRef node;

	//boolean hasLeft;
	
	public ResourceLeaveSensor(DKSRef node, NicheManagementInterface host) {
		this.node = node;
		this.niche = host.getNicheAsynchronousSupport();
		this.mySinks = new HashMap<String, ArrayList<SensorSubscription>>();

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

	
	public void resourceLeaveEventHandler(Event e) {
		/*
		 * potentially inform the mother-watcher, but now just trigger the correct new event
		 */
		System.err.println("Sensor says: NOT IMPLEMENTED!");
		
	}
	


	public void eventHandler(Object event) {
		resourceLeaveEventHandler((Event)event);
	}

	public void messageHandler(Message message){
		if(message instanceof UpdateManagementElementMessage) {
			handleUpdateMEMessage((UpdateManagementElementMessage)message);
		}
	}

	
	public void handleUpdateMEMessage(UpdateManagementElementMessage m) {
	
		if (m.getType() == UpdateManagementElementMessage.TYPE_ADD_SINK) {
			
			SensorSubscription myNewSubscription = (SensorSubscription)m.getReference();
			addSink(myNewSubscription);
		}
		
		else if (m.getType() == UpdateManagementElementMessage.TYPE_REMOVE_SINK) {
		
		}

	}

	
	public void addSink(SensorSubscription myNewSubscription) {
		
		String nodeId = myNewSubscription.getNodeOfSource().getDKSRef().getId().toString();
		ArrayList<SensorSubscription> at = (ArrayList<SensorSubscription>)mySinks.get(nodeId);
		/*#%*/ niche.log("ResourceSensor at "+node.getId() +" says: I'm adding a sink for node "+nodeId);
		if(at == null) {
			at = new ArrayList<SensorSubscription>();
			at.add(myNewSubscription);
			mySinks.put(nodeId, at);
		} else {
			at.add(myNewSubscription);
		}

	}

}
		

