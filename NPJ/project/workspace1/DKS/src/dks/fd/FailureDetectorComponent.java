/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.fd.events.PingTimedOutEvent;
import dks.fd.events.ReceivePongEvent;
import dks.fd.events.SendPingEvent;
import dks.fd.events.StartMonitoringNodeEvent;
import dks.fd.events.StopMonitoringNodeEvent;
import dks.fd.messsages.Ping;
import dks.fd.messsages.Pong;
import dks.messages.Message;

/**
 * The <code>FailureDetectorOLD</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FailureDetectorComponent.java 627 2008-07-11 23:17:56Z joel $
 */
public class FailureDetectorComponent extends CommunicatingComponent {

	/*#%*/ private static Logger log = Logger.getLogger(FailureDetectorComponent.class);

	private ConcurrentHashMap<BigInteger, FailureDetectorMonitor> monitoredNodes;

	private DKSRef myDKSRef;

	/**
	 * Constructs the Failure Detector
	 * 
	 * @param scheduler
	 * @param registry
	 */
	public FailureDetectorComponent(Scheduler scheduler,
			ComponentRegistry registry, DKSRef myDkRef) {
		super(scheduler, registry);

		this.myDKSRef = myDkRef;

		monitoredNodes = new ConcurrentHashMap<BigInteger, FailureDetectorMonitor>();

		registerForEvents();
		
		registerForConsumers();

		// register the component in the Component Registry
		registry.registerFailureDetectorComponent(this);

	}

	protected void registerForEvents() {
		register(StartMonitoringNodeEvent.class,
				"handleStartMonitoringNodeEvent");

		register(StopMonitoringNodeEvent.class, "handleStopMonitoringNodeEvent");

		register(SendPingEvent.class, "handleSendPingEvent");
		
		register(PingTimedOutEvent.class,"handlePingTimedoutEvent");
		
		register(ReceivePongEvent.class,"handlePong");

	}

	protected void registerForConsumers() {

		registerConsumer("handlePing", Ping.class);
		registerConsumer("handlePong", Pong.class);

	}

	public void handleStartMonitoringNodeEvent(StartMonitoringNodeEvent event) {

		DKSRef node = event.getNode();
		FailureDetectorMonitor monitor = monitoredNodes.get(node.getId());
		
		if (monitor == null) {

			monitor = new FailureDetectorMonitor(node,
					this);

			/*#%*/ log.info("Start monitoring node " + node);

			monitor.start();

			monitoredNodes.put(node.getId(), monitor);

		} else {

			/*#%*/ log.info("Node " + node + " already under monitoring");
			
			//FIXME: document why this is needed...!
			monitor.restart();
			

		}

	}

	public void handleStopMonitoringNodeEvent(StopMonitoringNodeEvent event) {

		DKSRef node = event.getNode();

		if (!monitoredNodes.containsKey(node.getId())) {

			FailureDetectorMonitor monitor = monitoredNodes.get(node.getId());

			/*#%*/ log.info("Stop monitoring node " + node);

			monitor.stop();

			monitoredNodes.remove(node.getId());

		}/*#%*/  else {

		/*#%*/ 	log.info("Node " + node + " not under monitoring");

		/*#%*/ }

	}

	public void handleSendPingEvent(SendPingEvent event) {

		BigInteger nodeId = (BigInteger) event.getAttachment();

		if (monitoredNodes.containsKey(nodeId)) {

			monitoredNodes.get(nodeId).ping();

		} /*#%*/ else {

		/*#%*/ 	log.info("Node " + nodeId + " not under monitoring");

		/*#%*/ }

	}

	public void handlePingTimedoutEvent(PingTimedOutEvent event) {

		BigInteger id = (BigInteger) event.getAttachment();

		if (monitoredNodes.containsKey(id)) {

			monitoredNodes.get(id).pingTimedOut();

		} /*#%*/ else {

		/*#%*/ 	log.info("Node " + id + " not under monitoring");

		/*#%*/ }

	}

	public void handlePing(DeliverMessageEvent event) {
		
		/*#%*/ log.info("Received ping from: "+event.getMessageInfo().getSource());
		
		sendPong(event.getMessageInfo().getSource());

	}

	public void handlePong(DeliverMessageEvent event) {

		BigInteger id = event.getMessageInfo().getSource().getId();

		if (monitoredNodes.containsKey(id)) {

			monitoredNodes.get(id).pong();

		} /*#%*/ else {

		/*#%*/ log.info("Node " + id + " not under monitoring");

		/*#%*/ }

	}
	public void handlePong(ReceivePongEvent event) {

		BigInteger id = event.getSource().getId();

		if (monitoredNodes.containsKey(id)) {

			monitoredNodes.get(id).pong();

		}/*#%*/  else {

		/*#%*/ 	log.info("Node " + id + " not under monitoring");

		/*#%*/ }

	}

	public void trigger(Event event) {
		super.trigger(event);
	}

	private void sendPong(DKSRef sourceNodeRef) {
		send(new Pong(), sourceNodeRef);
	}

	public void send(Message msg, DKSRef destination) {
		//send(msg, myDKSRef, destination, TransportProtocol.UDP);
		send(msg, myDKSRef, destination, TransportProtocol.TCP); //better safe then sorry?
	}
}
