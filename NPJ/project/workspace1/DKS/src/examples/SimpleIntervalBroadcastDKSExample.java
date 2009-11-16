/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.DirectIntervalAggregationDeliverEvent;
import dks.bcast.events.DirectIntervalAggregationMyValueEvent;
import dks.bcast.events.SimpleIntervalBroadcastDeliverEvent;
import dks.bcast.events.SimpleIntervalBroadcastStartEvent;
import dks.boot.DKSNode;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;
import dks.utils.IntervalsList;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;
import examples.events.MySimpleAggregateEvent;
import examples.events.MySimpleDeliverEvent;

/**
 * The <code>SimpleIntervalBroadcastDKSExample</code> class
 * 
 * <p>This example uses node 30 to do the following IntervalBroadcast examples:
 * 	1 - Broadcasting to all nodes in the interval [200, 700] with aggregation & short timeout
 * 	2 - Broadcasting to all nodes in the interval [900, 100] without aggregation
 * 	3 - Broadcasting to all nodes in the ring with aggregation
 * 	4 - Broadcasting to only one node in the ring with aggregation
 * 	5 - Multicasting to all nodes in the intervals [100, 300] [680, 680] [800, 900] [1000, 50]</p>
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalBroadcastDKSExample.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class SimpleIntervalBroadcastDKSExample extends Component {

    static DKSNode node;
    static DKSParameters dksParameters;
    static DKSRef myRef;
    /**
     * @param scheduler
     * @param registry
     */
    public SimpleIntervalBroadcastDKSExample(Scheduler scheduler, ComponentRegistry registry) {
	super(scheduler, registry);
	registerForEvents();
    }

    /**
     * @param args
     */
    /**
     * @param args
     */
    public static void main(String[] args) {

	/*
	 * + Follow these steps to run DKS: - Define this JVM properties in your
	 * launcher: dks.propFile=./dksParam.prop
	 * org.apache.log4j.config.file=log4j.config - For creating the first
	 * node of the ring start this program with the following parameters:
	 * create [id] [port] [IPAddr] - For starting any other node use the
	 * following parameters: join [id] [port] [IPAddr]
	 */

	System.out.println("Broadcasting Example");
	PropertyConfigurator.configure(System
		.getProperty("org.apache.log4j.config.file"));

	DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

	dksParameters = (propertyLoader).getDKSParameters();

	if (args.length < 4) {
	    System.err
	    .println("Usage: Test <create|join> <id> <port> <bind_ip>");
	}

	for (int i = 0; i < args.length; i++) {
	    System.out.println(i + "=" + args[i]);
	}

	try {
	    boolean create = args[0].equals("create");
	    BigInteger id = new BigInteger(args[1]);
	    int port = Integer.parseInt(args[2]);
	    InetAddress ip = InetAddress.getByName(args[(args.length - 1)]);

	    if (create) {
		System.out.println("First node. Creating a ring...");
		myRef = new DKSRef(ip, port, id.abs());
		node = new DKSNode(myRef,
			dksParameters, propertyLoader.getWebcacheAddress());
		node.getDksImplementation().create();

	    } else {
		try {
		    String webCacheAddres = propertyLoader.getWebcacheAddress();
		    DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
			    webCacheAddres);

		    myRef = new DKSRef(ip, port, id.abs());

		    node = new DKSNode(
			    myRef, dksParameters, webCacheAddres);

		    String rawDKSRef = dksCacheManager.getFirstDKSRef();

		    DKSRef dksRef = null;

		    dksRef = new DKSRef(rawDKSRef);

		    System.out.println("Joining ring using node " + dksRef
			    + "...");
		    node.getDksImplementation().join(dksRef);

		} catch (MalformedURLException e) {
		    e.printStackTrace();
		} catch (NumberFormatException e) {
		    e.printStackTrace();
		}
	    }

	} catch (UnknownHostException e) {
	    System.out.println(e.getMessage());
	}

	SimpleIntervalBroadcastDKSExample app = new SimpleIntervalBroadcastDKSExample(node.getScheduler(), node.getComponentRegistry());

	// if I'm node 30 then I'll broadcast to others
	if(args[1].equals("030")) {
	    System.out.println("DEBUG: I am node 30 and I'll broadcast to others!");

	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    //	1 - Broadcasting to all nodes in the interval [200, 700] with aggregation & short timeout
	    app.broadcast("Hello World!", "200", "700", true, 100, false);

	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    //	2 - Broadcasting to all nodes in the interval [900, 100] without aggregation
	    app.broadcast("Hi Broadcast!", "900", "100", false, null, false);

	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    //	3 - Broadcasting to all nodes in the ring with aggregation
	    app.broadcast("Broadcast to all!", null, null, true, null, true);

	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    //	4 - Broadcasting to only one node in the ring with aggregation
	    app.broadcast("Only one node!", "720", "720", true, null, false);

	    try {
		Thread.sleep(3000);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    //	5 - Multicasting to all nodes in the intervals [100, 300] [680, 680] [800, 900] [1000, 50]  
	    IntervalsList list = null;
	    try {
		list = new IntervalsList(new BigInteger("100"),	new BigInteger("300"), Bounds.CLOSED_CLOSED, dksParameters.N );
		list.addToSelf(new IntervalsList(new BigInteger("680"),	new BigInteger("680"), Bounds.CLOSED_CLOSED, dksParameters.N ));
		list.addToSelf(new IntervalsList(new BigInteger("800"),	new BigInteger("900"), Bounds.CLOSED_CLOSED, dksParameters.N ));
		list.addToSelf(new IntervalsList(new BigInteger("1000"),	new BigInteger("50"), Bounds.CLOSED_CLOSED, dksParameters.N ));

	    } catch (SimpleIntervalException e) {
		e.printStackTrace();
	    }

	    app.broadcast("Multicast!!!",list, true, null, false);

	}

    }


    /**
     * @param string
     * @param list
     * @param aggr
     * @param timeout
     * @param overrideDefault
     */
    private void broadcast(String string, IntervalsList list, boolean aggr, Integer timeout, boolean overrideDefault) {
	/*
	 * To broadcast a message you should create an IntervalBroadcastInfo object and set at least the message
	 * you may also set the intervals if you want interval broadcast
	 * you may also set the aggregate to true if you want aggregation
	 * you may also set the aggregation timeout if you want to override default value
	 * you should NOT set the initiator, source, destination, instanceID unless you know what you are doing!!
	 * 
	 * Then you should trigger a SimpleIntervalBroadcastStartEvent event containing the info you just created
	 */
	System.out.println("DEBUG: Broadcasting...");
	SimpleIntervalBroadcastStartEvent event = new SimpleIntervalBroadcastStartEvent();
	IntervalBroadcastInfo info = new IntervalBroadcastInfo();
	info.setInterval(list);
	info.setMessage(string);
	info.setAggregate(aggr);
	if(timeout != null)
	    info.setAggregationTimeout(timeout);
	if(overrideDefault) {
	    info.setDeliverEventClassName(MySimpleDeliverEvent.name);
	    info.setAckAggrEventClassName(MySimpleAggregateEvent.name);
	}
	event.setInfo(info);
	trigger(event);

    }


    /**
     * @param string
     * @param from
     * @param to
     * @param aggr
     * @param timeout
     * @param overrideDefault
     */
    private void broadcast(String string, String from, String to, boolean aggr, Integer timeout, boolean overrideDefault) {

	/*
	 * To broadcast a message you should create an IntervalBroadcastInfo object and set at least the message
	 * you may also set the intervals if you want interval broadcast
	 * you may also set the aggregate to true if you want aggregation
	 * you may also set the aggregation timeout if you want to override default value
	 * you should NOT set the initiator, source, destination, instanceID unless you know what you are doing!!
	 * 
	 * Then you should trigger a SimpleIntervalBroadcastStartEvent event containing the info you just created
	 */

	System.out.println("DEBUG: Broadcasting...");
	SimpleIntervalBroadcastStartEvent event = new SimpleIntervalBroadcastStartEvent();
	IntervalBroadcastInfo info = new IntervalBroadcastInfo();
	if(from != null && to != null){
	    try {
		info.setInterval(
			new IntervalsList(
				new BigInteger(from),
				new BigInteger(to),
				Bounds.CLOSED_CLOSED,
				dksParameters.N ));


	    } catch (SimpleIntervalException e) {
		e.printStackTrace();
	    }
	}
	
	info.setMessage(string);
	info.setAggregate(aggr);
	if(timeout != null)
	    info.setAggregationTimeout(timeout);
	if(overrideDefault) {
	    info.setDeliverEventClassName(MySimpleDeliverEvent.class.getName());
	    info.setAckAggrEventClassName(MySimpleAggregateEvent.class.getName());
	}
	event.setInfo(info);
	trigger(event);
    }

    /* (non-Javadoc)
     * @see dks.arch.Component#registerForEvents()
     */
    @Override
    protected void registerForEvents() {
	// You should register for this event if you want to receive any broadcasted message
	register(SimpleIntervalBroadcastDeliverEvent.class, "handleSimpleIntervalBroadcastDeliverEvent");

	// You should register for this event if you want to receive the result of the aggregation 
	register(DirectIntervalAggregationDeliverEvent.class, "handleDirectIntervalAggregationDeliverEvent");

	// Customized events
	register(MySimpleDeliverEvent.class,"handleMySimpleDeliverEvent");
	register(MySimpleAggregateEvent.class,"handleMySimpleAggregateEvent");
    }

    public void handleSimpleIntervalBroadcastDeliverEvent(SimpleIntervalBroadcastDeliverEvent event) {
	IntervalBroadcastInfo info = event.getInfo();
	System.out.println(String.format("DEBUG: Delivered at node %5s! %s", myRef.getId(), info));

	if(info.getAggregate()){
	    DirectIntervalAggregationMyValueEvent aggr = new DirectIntervalAggregationMyValueEvent();
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 1");
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 2");
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 3");
	    aggr.setInitiator(info.getInitiator());
	    aggr.setInstanceId(info.getInstanceId());
	    trigger(aggr);
	}

    }

    public void handleDirectIntervalAggregationDeliverEvent(DirectIntervalAggregationDeliverEvent event) {
	ArrayList<Object> arr;
	arr = event.getValues();
	BigInteger id = event.getInstanceId();
	System.out.println(String.format("DEBUG: DIA at node %5s for instance %3s!", myRef.getId(), id));
	for (Object o : arr) {
	    System.out.println(String.format("DEBUG: DIA value for instance %3s: %s",id, o));
	}

    }

    /////////////////customized event handeling
    public void handleMySimpleDeliverEvent(MySimpleDeliverEvent event) {
	IntervalBroadcastInfo info = event.getInfo();
	System.out.println("DEBUG: ##### handleMySimpleDeliverEvent #####");
	System.out.println(String.format("DEBUG: Delivered at node %5s! %s", myRef.getId(), info));

	if(info.getAggregate()){
	    DirectIntervalAggregationMyValueEvent aggr = new DirectIntervalAggregationMyValueEvent();
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 1");
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 2");
	    aggr.addValue(myRef.getId() + ":" + info.getMessage() + ":Val 3");
	    aggr.setInitiator(info.getInitiator());
	    aggr.setInstanceId(info.getInstanceId());
	    trigger(aggr);
	}

    }

    public void handleMySimpleAggregateEvent(MySimpleAggregateEvent event) {
	ArrayList<Object> arr;
	arr = event.getValues();
	BigInteger id = event.getInstanceId();
	System.out.println("DEBUG: ##### handleMySimpleAggregateEvent #####");
	System.out.println(String.format("DEBUG: DIA at node %5s for instance %3s!", myRef.getId(), id));
	for (Object o : arr) {
	    System.out.println(String.format("DEBUG: DIA value for instance %3s: %s",id, o));
	}

    }

}
