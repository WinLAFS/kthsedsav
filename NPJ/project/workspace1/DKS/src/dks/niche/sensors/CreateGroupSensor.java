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

import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.SysexMessage;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.ComponentFailEvent;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.SensorInterface;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;

/**
 * The <code>CreateGroupSensor</code> class
 *
 * @author Joel
 * @version $Id: CreateGroupSensor.java 294 2006-05-05 17:14:14Z joel $
 */
public class CreateGroupSensor implements SensorInterface {

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SensorInterface#eventHandler(dks.arch.Event)
	 */
	
	protected NicheAsynchronousInterface niche;
	protected HashMap<String, ArrayList<SensorSubscription>> mySinks;
	DKSRef node;
	
	String localCreateGroupEvent = CreateGroupEvent.class.getName();
	
	public CreateGroupSensor(DKSRef node, NicheManagementInterface host) {
		
		this.node = node;
		this.niche = host.getNicheAsynchronousSupport();
		this.mySinks = new HashMap<String, ArrayList<SensorSubscription>>();

	}
	
	
	public void eventHandler(Object e) {
		createGroupEventHandler((CreateGroupEvent) e);		
	}

	private void createGroupEventHandler(CreateGroupEvent e) {
		
		ArrayList<SensorSubscription> interestedSubscribers = mySinks.get(e.getInitiator().getId().toString());
		if(interestedSubscribers != null) {
			/*#%*/ niche.log(
			/*#%*/ 		"Sensor says: now I'm triggering a CreateGroupEvent to the "
			/*#%*/ 		+ interestedSubscribers.size()
			/*#%*/ 		+ " interested Subscribers"
			/*#%*/ );
			
			for (SensorSubscription subscription : interestedSubscribers) {
				niche.sendToManagement(
						subscription.getSinkId(),
						new DeliverEventMessage(
								subscription.getSinkId(),
								e
						),
						subscription.getSinkId().isReliable()
				);

			}
		} /*#%*/ else {
			//do nuffin
		/*#%*/ niche.log(
		/*#%*/ 		"Sensor says: none was interested in the CreateGroupEvent from " 
		/*#%*/ 	);

		/*#%*/ }
		
	}
	
	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SensorInterface#getEventNames()
	 */
	public String getEventName() {
		return localCreateGroupEvent;
	}


	/* (non-Javadoc)
	 * @see dks.niche.sensors.SensorId#messageHandler(dks.messages.Message)
	 */

	public void messageHandler(Message message) {
		if(message instanceof UpdateManagementElementMessage) {
			handleUpdateMEMessage((UpdateManagementElementMessage)message);
		}
	}
	
	public void handleUpdateMEMessage(UpdateManagementElementMessage m) {
	
		if (m.getType() == UpdateManagementElementMessage.TYPE_ADD_SINK) {
			
			//Subscription myNewSubscription = (SensorSubscription)m.getReference();
			addSink((SensorSubscription)m.getReference());
		}
		
		else if (m.getType() == UpdateManagementElementMessage.TYPE_REMOVE_SINK) {
		
		}

	}

	public void addSinks(ArrayList<SensorSubscription> ehs) {
		for (SensorSubscription subscription : ehs) {
			addSink(subscription);
		}
	}
	
	public void addSink(SensorSubscription myNewSubscription) {
		
		String componentId = myNewSubscription.getSourceId().toString();
		ArrayList<SensorSubscription> at = mySinks.get(componentId);
		if(at == null) {
			at = new ArrayList<SensorSubscription>();
			at.add(myNewSubscription);
			mySinks.put(componentId, at);
		} else {
			at.add(myNewSubscription);
		}

	}

	
}
