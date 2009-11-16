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

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.SensorInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.Subscription;

/**
 * The <code>SensorId</code> class
 * 
 * @author Joel
 * @version $Id: SensorId.java 294 2006-05-05 17:14:14Z joel $
 */
public abstract class SensorId implements SensorInterface,
		ManagementElementInterface {

	protected NicheAsynchronousInterface niche;

	//protected NicheId myId;

	// Object[]applicationParameters;

	// This is the order of the infrastructure parameters:
	// 0
	// the first element of:
	
	//Explanation: HashMap<NodeIdAsString, HashMap<(ComponentOfInterestIdAsString+SinkIdAsString, SensorSubscription>>
	
	protected HashMap<String, HashMap<String, SensorSubscription>> mySinks;
	protected DKSRef node;
	// - can be either <RingId, ListOfFailSubscribers>
	// - <RingId, ListOfLeaveSubscribers>
	// - <ComponentId, ListOfGroupCreationSubscribers>

	// 1
	// protected HashMap<String, ArrayList<Subscription>> mySupervisedResources;
	// //this can be just the 'empty' component representing the resource itself
	// ArrayList<Subscription> mySinks;

	NodeRef mySupervisedNode;
	//int replicaNumber;

	public SensorId(DKSRef node, NicheManagementInterface host) {
		
		this.node = node;
		this.niche = host.getNicheAsynchronousSupport();
		this.mySinks = new HashMap<String, HashMap<String, SensorSubscription>>();

	}

	// public SensorId(NicheId id, NicheManagementContainerComponent host,
	// Object[]infrastructureParameters) {
	// this.myId = id;
	// this.myHost = host;
	// this.mySinks = new ArrayList();
	// this.mySinks.add((NicheId) infrastructureParameters[0]); //Source =
	// (NicheId)
	// infrastructureParameters[0];
	// this.myComponentId = (ComponentId) infrastructureParameters[1];
	// //this.mySinks = (ArrayList<Subscription> ) infrastructureParameters[1];
	//		
	// }

	public void connect(NicheId id, int replicaNumber, NicheManagementInterface host) {
		
//		this.myId = id;
//		this.replicaNumber = replicaNumber;
		this.niche = host.getNicheAsynchronousSupport();

	}

//	public void trigger(String initiatorId, Event e) {
//
//		ArrayList<SensorSubscription> ta = mySinks.get(initiatorId);
//
//		if(ta != null) {
//			for (Subscription s : ta) {
//				niche.sendToManagement(s.getSinkId(),
//						new DeliverEventMessage(s.getSinkId(), e));
//	
//			}
//		}
//
//	}

	public abstract void addSink(SensorSubscription s);

	// {
	// ArrayList<Subscription> ta = mySinks.get(ehs.getEventName());
	// if(ta == null) {
	// (ta = new ArrayList()).add(ehs);
	// mySinks.put(ehs.getEventName(), ta);
	// }
	// else {
	// ta.add(ehs);
	// }
	//		
	// }

	

	//	
//	public ArrayList<SensorSubscription> getSinks() {
//		Object[] allKeys = mySinks.keySet().toArray();
//		ArrayList<SensorSubscription> ta = new ArrayList();
//		ArrayList tat = new ArrayList();
//		for (int i = 0; i < allKeys.length; i++) {
//			tat = getSinks((String) allKeys[i]);
//			ta.addAll(tat);
//		}
//		return ta;
//	}

//	public ArrayList<SensorSubscription> getSinks(String initiatorId) {
//		return mySinks.get(initiatorId);
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.CommonWatcherInterface#removeSink(dks.niche.wrappers.Subscription)
	 */
	public void removeSink(Subscription ehs) {
		// TODO Auto-generated method stub

	}

	public abstract void messageHandler(Message message);

	//public abstract void storeState();

	public DelegationRequestMessage transfer(int mode) {
		System.err.println("This should not happen!");
		return null;
//		return new DelegationRequestMessage(myId,
//				DelegationRequestMessage.TYPE_SENSOR,
//				this.getClass().getName(), new Object[] {mySupervisedNode, mySinks});
	}

	public NicheId getId() {
		return null;
	}

	public String getTargetNodeId() {
		return mySupervisedNode.getDKSRef().getId().toString();
	}
	
	public int getReplicaNumber() {
		//return this.replicaNumber;
		return -Integer.MAX_VALUE;
	}

	protected String getFailSensorSignature(NicheId sourceId, NicheId sinkId) {
		return sourceId.getLocation() + sinkId;
	}
	// The id of the supervised object!

	// public NicheId getTargetId() {
	// return myComponentId.getId();
	// }
	 public DKSRef getDKSRef() {
		 return mySupervisedNode.getDKSRef();
	 }

}
