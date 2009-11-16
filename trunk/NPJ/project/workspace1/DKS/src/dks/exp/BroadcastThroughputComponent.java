/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.PseudoReliableIntervalBroadcastAckEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastDeliverEvent;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.bcast.events.RecursiveIntervalAggregationMyValueEvent;
import dks.utils.IntervalsList;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;
import examples.events.MyPseudoAckAggrEvent;
import examples.events.MyPseudoDeliverEvent;

/**
 * The <code>MessageThroughputComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageThroughputComponent.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class BroadcastThroughputComponent extends Component {

//	private TimerComponent timerComponent;

	private long startTimestamp;

	private DKSRef myRef;

//	private int size = 0;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public BroadcastThroughputComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);

//		this.timerComponent = registry.getTimerComponent();

		this.myRef = registry.getRingMaintainerComponent().getMyDKSRef();

		registerForEvents();
		// registerConsumer();
	}

	public void start() {

		// while (true) {

		BufferedReader cin = new BufferedReader(
				new InputStreamReader(System.in));

		String line = null;
		try {
			System.out.println("Choose size of message to broadcast:");
			line = cin.readLine();
		} catch (IOException e) {
			line = "1000";
		}

		int size = Integer.parseInt(line);

		/*-----------------STARTING MEASUREMENS------------------*/
		System.out.println("Broadcasting");

		startTimestamp = System.currentTimeMillis();

		// 3 - Broadcasting to all nodes in the ring with aggregation
		broadcast("Broadcast to all!", null, null, true, true, size);

		// }

	}

	@Override
	protected void registerForEvents() {
		// You should register for this event if you want to receive any
		// broadcasted message
		register(PseudoReliableIntervalBroadcastDeliverEvent.class,
				"handlePseudoReliableIntervalBroadcastDeliverEvent");

		// You should register for this event if you want to receive the ack and
		// the result of the aggregation
		register(PseudoReliableIntervalBroadcastAckEvent.class,
				"handlePseudoReliableIntervalBroadcastAckEvent");

		// //Customized events
		register(MyPseudoDeliverEvent.class, "handleMyPseudoDeliverEvent");
		register(MyPseudoAckAggrEvent.class, "handleMyPseudoAckAggrEvent");

	}

	public void handlePseudoReliableIntervalBroadcastDeliverEvent(
			PseudoReliableIntervalBroadcastDeliverEvent event) {
		IntervalBroadcastInfo info = event.getInfo();

		System.out.println("DELIVERED at node :"
				+ registry.getRingMaintainerComponent().getMyDKSRef());

		// Ahmad Debug
		// if(myRef.getId().equals(new BigInteger("850")) ) {
		// System.out.println("Suicide :S");
		// System.exit(123);
		// }

		if (info.getAggregate()) {
			// System.out.println("Ahmad: Start Aggr at " + myRef.getId());
			RecursiveIntervalAggregationMyValueEvent aggr = new RecursiveIntervalAggregationMyValueEvent();
			aggr.addValue("OK "
					+ registry.getRingMaintainerComponent().getMyDKSRef());
			aggr.setInitiator(info.getInitiator());
			aggr.setInstanceId(info.getInstanceId());
			trigger(aggr);
		}

	}

	public void handlePseudoReliableIntervalBroadcastAckEvent(
			PseudoReliableIntervalBroadcastAckEvent event) {
		// System.out.println("Ahmad: ACK for " + event.getUniqueID());

		System.out.println("Time for broadcasting to all nodes="
				+ (System.currentTimeMillis() - startTimestamp));

		if (event.getAggregate()) {
			ArrayList<Object> arr;
			arr = event.getValues();
//			BigInteger id = event.getInstanceId();
			// System.out.println(String.format("Ahmad: ACK & Aggr. at node %5s
			// for instance %3s!", myRef.getId(), id));
			for (Object o : arr) {
				System.out.println(o);
			}
		}
	}

	/**
	 * @param string
	 */
	private void broadcast(String string, String from, String to, boolean aggr,
			boolean overrideDefault, int size) {

		/*
		 * To broadcast a message you should create an IntervalBroadcastInfo
		 * object and set at least the message you may also set the intervals if
		 * you want interval broadcast you may also set the aggregate to true if
		 * you want aggregation you may also set the aggregation timeout if you
		 * want to override default value you should NOT set the initiator,
		 * source, destination, InstanceId unless you know what you are doing!!
		 * 
		 * Then you should trigger a PseudoReliableIntervalBroadcastStartEvent
		 * event containing the info you just created
		 */

		// System.out.println("Ahmad: Broadcasting...");
		PseudoReliableIntervalBroadcastStartEvent event = new PseudoReliableIntervalBroadcastStartEvent();
		IntervalBroadcastInfo info = new IntervalBroadcastInfo();
		if (from != null && to != null) {
			try {
				info.setInterval(new IntervalsList(new BigInteger(from),
						new BigInteger(to), Bounds.CLOSED_CLOSED, registry
								.getRingMaintainerComponent()
								.getDksParameters().N));

			} catch (SimpleIntervalException e) {
				e.printStackTrace();
			}
		}
		// info.setMessage(new ThroughputMessage(size));
		info.setMessage(string);
		info.setAggregate(aggr);

		if (overrideDefault) {
			info.setDeliverEventClassName(MyPseudoDeliverEvent.class.getName());
			info.setAckAggrEventClassName(MyPseudoAckAggrEvent.class.getName());
		}

		event.setInfo(info);
		trigger(event);
	}

	public void handleMyPseudoDeliverEvent(MyPseudoDeliverEvent event) {
		IntervalBroadcastInfo info = event.getInfo();
		System.out.println("Ahmad: ##### handleMyPseudoDeliverEvent #####");
		System.out.println(String.format("Ahmad: Delivered at node %5s! %s",
				myRef.getId(), info));

		// Ahmad Debug
		// if(myRef.getId().equals(new BigInteger("850")) ) {
		// System.out.println("Suicide :S");
		// System.exit(123);
		// }

		if (info.getAggregate()) {
			System.out.println("Ahmad: Start Aggr at " + myRef.getId());
			RecursiveIntervalAggregationMyValueEvent aggr = new RecursiveIntervalAggregationMyValueEvent();
			aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 1");
			aggr.setInitiator(info.getInitiator());
			aggr.setInstanceId(info.getInstanceId());
			trigger(aggr);
		}
	}

	public void handleMyPseudoAckAggrEvent(MyPseudoAckAggrEvent event) {
		System.out.println("Ahmad: ##### handleMyPseudoAckAggrEvent #####");
		System.out.println("Ahmad: ACK for " + event.getUniqueId());

		if (event.getAggregate()) {
			ArrayList<Object> arr;
			arr = event.getValues();
			BigInteger id = event.getInstanceId();
			System.out.println(String.format(
					"Ahmad: ACK & Aggr. at node %5s for instance %3s!", myRef
							.getId(), id));
			for (Object o : arr) {
				System.out
						.println(String
								.format(
										"Ahmad: ACK & Aggr. value for instance %3s: %s",
										id, o));
			}
		}
	}

}
