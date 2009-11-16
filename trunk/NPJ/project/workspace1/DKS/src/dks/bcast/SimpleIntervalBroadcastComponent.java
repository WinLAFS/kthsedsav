/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.bcast.events.DirectIntervalAggregationDeliverEvent;
import dks.bcast.events.DirectIntervalAggregationMyValueEvent;
import dks.bcast.events.DirectIntervalAggregationTimeoutEvent;
import dks.bcast.events.SimpleIntervalBroadcastDeliverEvent;
import dks.bcast.events.SimpleIntervalBroadcastStartEvent;
import dks.bcast.messages.DirectIntervalAggregationSubTotalMessage;
import dks.bcast.messages.SimpleIntervalBroadcastMessage;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.fd.events.SuspectEvent;
import dks.fd.events.ReviseSuspicionEvent;
import dks.router.GenericRoutingTableInterface;
import dks.timer.TimerComponent;
import dks.utils.IntervalsList;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;

/**
 * The <code>SimpleIntervalBroadcastComponent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalBroadcastComponent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */


public class SimpleIntervalBroadcastComponent extends CommunicatingComponent {
	
	DKSRef myRef;
	
	static BigInteger msgID = BigInteger.ZERO;
	
	//Hashtable<BigInteger, CompactSet> deliveredSet;
	
	Hashtable<BigInteger, ArrayList<Object>> aggregationValues;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public SimpleIntervalBroadcastComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);
		myRef = registry.getRingMaintainerComponent().getMyDKSRef();
		aggregationValues = new Hashtable<BigInteger, ArrayList<Object>>();
//		deliveredSet = new Hashtable<BigInteger, CompactSet>();
				
		registerForEvents();

		registerConsumers();
	}

	/**
	 * 
	 */
	protected void registerConsumers() {
		registerConsumer("handleSimpleIntervalBroadcastMessage", SimpleIntervalBroadcastMessage.class);
		registerConsumer("handleDirectIntervalAggregationSubTotalMessage", DirectIntervalAggregationSubTotalMessage.class);
	}

	/* (non-Javadoc)
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		register(SuspectEvent.class, "handlePeerSuspectedEvent");
		register(ReviseSuspicionEvent.class, "handleRectifyPeerEvent");
		register(SimpleIntervalBroadcastStartEvent.class, "handleSimpleIntervalBroadcastStartEvent");
		register(DirectIntervalAggregationMyValueEvent.class, "handleDirectIntervalAggregationMyValueEvent");
		register(DirectIntervalAggregationTimeoutEvent.class, "handleDirectIntervalAggregationTimeoutEvent");
	}
	
	public void handleSimpleIntervalBroadcastMessage(DeliverMessageEvent event) {
		SimpleIntervalBroadcastMessage simpleMessage = (SimpleIntervalBroadcastMessage) event.getMessage();
		IntervalBroadcastInfo info = simpleMessage.getInfo();
		processSIB(info);
	}
	
	public void handleDirectIntervalAggregationSubTotalMessage(DeliverMessageEvent event) {
		DirectIntervalAggregationSubTotalMessage msg = (DirectIntervalAggregationSubTotalMessage) event.getMessage();
		if (msg.getInitiator().getId().equals(myRef.getId())
				//if the instance ID is in the hash table then it means that
				//the timeout has not passed yet
				&& aggregationValues.containsKey(msg.getInstanceId()) ) {
				
			ArrayList<Object> arr = aggregationValues.get(msg.getInstanceId());
			arr.addAll(msg.getValues());
		}
	}

	public void handlePeerSuspectedEvent(SuspectEvent event) {

	}

	public void handleRectifyPeerEvent (ReviseSuspicionEvent event) {

	}

	/**
	 * @param event
	 */
	public void handleSimpleIntervalBroadcastStartEvent(SimpleIntervalBroadcastStartEvent event) {
		IntervalBroadcastInfo info = event.getInfo();
		preprocess(info);
		processSIB(info);
		if(info.getAggregate()) {
			// Add an entry for this instance in the hash table
			aggregationValues.put(info.getInstanceId(), new ArrayList<Object>());
			TimerComponent timer = registry.getTimerComponent();
			timer.registerTimer(DirectIntervalAggregationTimeoutEvent.class, info, info.getAggregationTimeout());
		}
			
	}
	
	/**
	 * @param event
	 */
	public void handleDirectIntervalAggregationMyValueEvent(DirectIntervalAggregationMyValueEvent event) {
		DirectIntervalAggregationSubTotalMessage message = new DirectIntervalAggregationSubTotalMessage();
		message.setInitiator(event.getInitiator());
		message.setInstanceId(event.getInstanceId());
		message.setSrc(myRef);
		message.setValues(event.getValues());
		
		
		send(message, myRef, event.getInitiator());
		
//		MessageInfo messageInfo = new MessageInfo(myRef, event.getInitiator(), null,
//				null, 0);
//		MarshallMessageEvent marshallMessageEvent = new MarshallMessageEvent(
//				message, messageInfo);
//		trigger(marshallMessageEvent);
	}
	
	public void handleDirectIntervalAggregationTimeoutEvent (DirectIntervalAggregationTimeoutEvent event) {
		IntervalBroadcastInfo info = (IntervalBroadcastInfo) event.getAttachment();
		BigInteger id = info.instanceId; 
		
		ArrayList<Object> val;
		val = aggregationValues.get(id);
		if(val == null)
			val = new ArrayList<Object>(0);
		
		// remove the entry from the hashtable so no more values will be added after timeout
		//System.out.println("Ahmad: HT size: "+aggregationValues.size());
		aggregationValues.remove(id);
		
		DirectIntervalAggregationDeliverEvent deliver = null;
		
		if(info.getAckAggrEventClassName() != null) {
			try {
				deliver = (DirectIntervalAggregationDeliverEvent) Class.forName(info.getAckAggrEventClassName()).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			deliver = new DirectIntervalAggregationDeliverEvent();
		deliver.setValues(val);
		deliver.setInstanceId(id);
		trigger(deliver);
		
		
	}

	private void preprocess(IntervalBroadcastInfo info) {
		
		
		if(info.getInitiator() == null)
			info.setInitiator(myRef);
		if(info.getSource() == null)
			info.setSource(myRef);
		if(info.getDestination() == null)
			info.setDestination(myRef);
		if(info.getInstanceId() == null){
			info.setInstanceId(msgID);
			msgID = msgID.add(BigInteger.ONE);
		}
		if(info.getInterval() == null) {
			BigInteger N = registry.getRingMaintainerComponent().getDksParameters().N;
			try {
				info.setInterval(new IntervalsList(
						BigInteger.ZERO, N.subtract(BigInteger.ONE), Bounds.CLOSED_CLOSED, N ));
			} catch (SimpleIntervalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void processSIB(IntervalBroadcastInfo info){
		BigInteger myID = myRef.getId();
		BigInteger N = registry.getRingMaintainerComponent().getDksParameters().N;
		BigInteger limit;
		
		IntervalsList I = new IntervalsList(info.getInterval());
		ArrayList<DKSRef> u = getCurrentUniquePointers();
		
		if(I.contains(myID)) {
			deliver(info);
		}
		limit = myID;
		for (int i = u.size()-1; i >= 1; i--) {
			IntervalsList J;
			try {
				J = new IntervalsList(u.get(i).getId(), limit, Bounds.CLOSED_OPEN, N);
				if(I.intersects(J)) {
					IntervalBroadcastInfo tmpInfo = new IntervalBroadcastInfo(info);
					tmpInfo.setSource(myRef);
					tmpInfo.setDestination(u.get(i));
					tmpInfo.setInterval(IntervalsList.intersection(I, J));
					sendSIB(tmpInfo);
					I.subtractFromSelf(J);
					limit = u.get(i).getId();
				}
				
			} catch (SimpleIntervalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
}

	/**
	 * Forwards the responsibility of a sub interval to another node
	 * @param info
	 */
	private void sendSIB(IntervalBroadcastInfo info) {
		
		SimpleIntervalBroadcastMessage message = new SimpleIntervalBroadcastMessage();
		message.setInfo(info);
		
		send(message, myRef, info.getDestination());
		
//		MessageInfo messageInfo = new MessageInfo(myRef, info.getDestination(), null,
//				null, 0);
//		MarshallMessageEvent marshallMessageEvent = new MarshallMessageEvent(
//				message, messageInfo);
//		trigger(marshallMessageEvent);
	
		
	}

	/**
	 * Gets the unique fingers of a node. The element at 0 contains the self DKSRef.
	 * @return a <code>DKSRef</code> ArrayList that contains all unique fingers of a node.
	 */
	private ArrayList<DKSRef> getCurrentUniquePointers() {
		
		ArrayList<DKSRef> list = new ArrayList<DKSRef>();
		list.add(myRef);
		
		GenericRoutingTableInterface table = registry.getRouterComponent().getRoutingTable();
		for (long i = 0; i < table.getIntervalsNumber(); i++) {
			if(table.getRoutingTableEntry(i) != null && table.getRoutingTableEntry(i).getIntervalPointer() != null)
				list.add(table.getRoutingTableEntry(i).getIntervalPointer());
			
		}
		
		DKSRef pred = registry.getRingMaintainerComponent().getRingState().predecessor;
		if(!list.contains(pred))
			list.add(pred);
		
		return list;
	}

	/**
	 * Delivers the message to the application.
	 * @param msg The message to deliver to the application.
	 */
	private void deliver(IntervalBroadcastInfo info) {
		//if(!deliveredSet.contains(info.instanceId))
		{
			SimpleIntervalBroadcastDeliverEvent event = null;
			if(info.deliverEventClassName != null) {
				try {
					event = (SimpleIntervalBroadcastDeliverEvent) Class.forName(info.deliverEventClassName).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				event = new SimpleIntervalBroadcastDeliverEvent();
			
			event.setInfo(new IntervalBroadcastInfo(info));
			trigger(event);
		}
	}

}
