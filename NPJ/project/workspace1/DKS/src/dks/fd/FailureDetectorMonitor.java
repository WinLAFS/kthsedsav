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

import org.apache.log4j.Logger;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.fd.events.PingTimedOutEvent;
import dks.fd.events.ReviseSuspicionEvent;
import dks.fd.events.SendPingEvent;
import dks.fd.events.StopMonitoringNodeEvent;
import dks.fd.events.SuspectEvent;
import dks.fd.messsages.Ping;
import dks.timer.TimerComponent;

/**
 * The <code>FailureDetectorMonitor</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FailureDetectorMonitor.java 294 2006-05-05 17:14:14Z roberto $
 */
public class FailureDetectorMonitor {

	public enum States {
		INIT, REINIT, HSENT, HSUSPECT, UNKNOWN, STOPPED
	}

	/*#%*/ private static Logger log = Logger.getLogger(FailureDetectorMonitor.class);

	private TimerComponent timer;

	private boolean started = false;

	private States status;

	private DKSRef monitoredNode;

	private long intervalPingTimerId;

	private FailureDetectorComponent failureDetectorComponent;

	private long pingTimerId;

	private FailureDetectorStatistics stats;

	private long pingTimestamp;

	private int pingRetryCounter = 0;
	
	protected FailureDetectorMonitor(DKSRef monitoredNode,
			FailureDetectorComponent component) {

		this.monitoredNode = monitoredNode;

		this.failureDetectorComponent = component;

		this.timer = ComponentRegistry.getInstance().getTimerComponent();

		this.stats = new FailureDetectorStatistics();

	}

	public void start() {

		if (!started) {

			setPingTimer();

			status = States.INIT;

			/*#%*/ log.info("Failure Detector for node  " + monitoredNode	+ " started");

			started = true;

		}

	}
	public void restart() {

		timer.cancelTimer(pingTimerId);
		status = States.REINIT; //new state
		setPingTimer();
		pingRetryCounter = 0;
		/*#%*/ log.info("Failure Detector for node  " + monitoredNode + " REstarted");
		started = true;

	}

	public void ping() {

		if (started) {

			switch (status) {
			case INIT:

				// Setting timer for the receiving the Pong packet
				pingTimerId = timer
						.registerTimer(
								PingTimedOutEvent.class,
								monitoredNode.getId(),
								stats.getRTO()
										+ FailureDetectorConstants.FD_PONG_TIMEOUT_ADD_TO_RTO);

				// Sending Ping
				sendPing();

				/* Store the info about the Ping */
				pingTimestamp = System.currentTimeMillis();

				/*#%*/ log.info("FD for " + monitoredNode + ": Moving from State "
				/*#%*/ 		+ States.INIT + " to State:" + States.HSENT);

				status = States.HSENT;

				break;
				
			case REINIT:
				
				// Setting timer for the receiving the Pong packet
				pingTimerId = timer
						.registerTimer(
								PingTimedOutEvent.class,
								monitoredNode.getId(),
								stats.getRTO()
										+ FailureDetectorConstants.FD_PONG_TIMEOUT_ADD_TO_RTO);

				// Sending Ping
				sendPing();

				/* Store the info about the Ping */
				pingTimestamp = System.currentTimeMillis();

				/*#%*/ log.info("FD for " + monitoredNode + ": Moving from State "
				/*#%*/ 		+ States.REINIT + " to State:" + States.UNKNOWN + " until first pong is received");

				status = States.UNKNOWN;

				break;


			case HSUSPECT:

				/*
				 * When suspecting, the FD continues sending pings until a pong
				 * is received. When that happens, the RTO will be updated
				 * considering the time passed from the first ping sent and the
				 * first pong received.
				 */
				
				if(pingRetryCounter > FailureDetectorConstants.FD_PING_RETRY) {
					/*#%*/ log.info("FD for " + monitoredNode
					/*#%*/ 		+ ": I give up, stop monitoring this");
					
					failureDetectorComponent.trigger(new StopMonitoringNodeEvent(monitoredNode));
					
				} else {
					/*#%*/ log.info("FD for " + monitoredNode
					/*#%*/ 		+ ": Received no pong, sending another ping...");
					
					pingRetryCounter++;
					sendPing();
					setPingTimer();
				}
				break;

				/*#%*/ default:
				/*#%*/ 	log.debug("FD for " + monitoredNode + ": says Wrong state, should have been INIT, REINIT or HSUSPECT, was "+ status);
				/*#%*/ 	break;

			}
		}
	}

	public void pong() {

		if (started) {

			long RTT = System.currentTimeMillis() - pingTimestamp;
			/*#%*/ log.debug("RTT " + RTT);

			switch (status) {

			
			case HSENT:
			case UNKNOWN:
				
				/*#%*/ log.info("FD for " + monitoredNode + ": Moving from State "
				/*#%*/ 		+ status + " to State:" + States.INIT);

				timer.cancelTimer(pingTimerId);

				stats.updateRTO(RTT);

				setPingTimer();

				status = States.INIT;
				break;

			case HSUSPECT:
				/*#%*/ log.info("FD for " + monitoredNode + ": Moving from State "
				/*#%*/ 		+ States.HSUSPECT + " to State:" + States.INIT);

				stats.updateRTO(RTT);

				reviseSuspicion();

				setPingTimer();

				status = States.INIT;

				break;

				/*#%*/ default:
				/*#%*/ 	log.info("FD pong handler, exiting wrong state " + status);
				/*#%*/ 	break;
			}
			
		}
	}

	public void pingTimedOut() {
		if (started) {

			switch (status) {

			/*#%*/ case INIT:
				/*#%*/ log.debug("Ignoring ping timed out event");
			/*#%*/ break;
				
			case HSENT:
			case UNKNOWN:
				
				/*#%*/ log.info("FD for " + monitoredNode + ": Moving from State "
				/*#%*/ 		+ status + " to State:" + States.HSUSPECT);

				suspect();

				status = States.HSUSPECT;

				/*
				 * Continue sending pings until an answer is received
				 */
				setPingTimer();

				break;

			case HSUSPECT:

				/*#%*/ log.info("FD for " + monitoredNode
				/*#%*/ 		+ ": Received no pong, sending again ping");
				setPingTimer();

				break;
				/*#%*/ default:
				/*#%*/ log.debug("pingTimedOut(): Wrong State, should have been INIT or HSUSPECT, was " + status);
				/*#%*/ break;
			}

		}

	}

	/**
	 * Sets the timer for sending the ping challenge
	 */
	private void setPingTimer() {
		intervalPingTimerId = timer.registerTimer(SendPingEvent.class,
				monitoredNode.getId(),
				FailureDetectorConstants.FD_PING_INTERVAL_TIMER);
	}

	private void sendPing() {
		failureDetectorComponent.send(new Ping(), monitoredNode);
	}

	/**
	 * Issues a suspicion event for the peer associated with this instance of
	 * the Failure Detector
	 */
	private void suspect() {

		SuspectEvent commPeerSuspectedEvent = new SuspectEvent(monitoredNode);
		failureDetectorComponent.trigger(commPeerSuspectedEvent);

	}

	/**
	 * Issue a rectify event for the peer associated with the failure detector
	 */
	private void reviseSuspicion() {
		// Rectifying previous Suspicion
		pingRetryCounter = 0;
		ReviseSuspicionEvent commRectifyEvent = new ReviseSuspicionEvent(
				monitoredNode);
		failureDetectorComponent.trigger(commRectifyEvent);
	}

	/**
	 * Stopping the FailureDetector
	 */
	public void stop() {
		timer.cancelTimer(intervalPingTimerId);
		timer.cancelTimer(pingTimerId);

		status = States.STOPPED;
		started = false;
	}

}
