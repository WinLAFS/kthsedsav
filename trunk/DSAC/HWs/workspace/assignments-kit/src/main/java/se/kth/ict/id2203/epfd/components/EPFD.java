package se.kth.ict.id2203.epfd.components;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.epfd.Application1Init;
import se.kth.ict.id2203.epfd.events.CheckTimeoutEvent;
import se.kth.ict.id2203.epfd.events.HeartbeatMessage;
import se.kth.ict.id2203.epfd.events.HeartbeatTimeoutEvent;
import se.kth.ict.id2203.epfd.events.CrashEvent;
import se.kth.ict.id2203.epfd.ports.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class EPFD extends ComponentDefinition {

	long delta;
	long gamma;
	private Set<Address> neighborSet;
	private Set<Address> aliveSet;
	private Set<Address> detectedSet;
	private Address self;

	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Positive<EventuallyPerfectFailureDetector> epfdPort = positive(EventuallyPerfectFailureDetector.class);
	
	private static final Logger logger = LoggerFactory
			.getLogger(EPFD.class);

	// private Positive<FailureDetectorPort> fpPositive =
	// positive(FailureDetectorPort.class);
	Positive<Timer> timer = positive(Timer.class);



	public EPFD() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pHeartbeatMessage, pp2p);
		subscribe(hbHandler, timer);
		subscribe(ctHandler, timer);
	}

	Handler<HeartbeatTimeoutEvent> hbHandler = new Handler<HeartbeatTimeoutEvent>() {

		public void handle(HeartbeatTimeoutEvent arg0) {
			logger.debug("Sending hb to all alive to neighboors");
			String message = "heartbeat";
			
			//8 - 10
			for (Address neighbor : neighborSet) {
				logger.info("Sending hb message {} to {}", message, neighbor);
				HeartbeatMessage hbMessage = new HeartbeatMessage(self);
				trigger(new Pp2pSend(neighbor, hbMessage), pp2p);
			}
			
			//11
			ScheduleTimeout st = new ScheduleTimeout(gamma);
			st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
			trigger(st, timer);
		}
	};

	Handler<CheckTimeoutEvent> ctHandler = new Handler<CheckTimeoutEvent>() {

		public void handle(CheckTimeoutEvent arg0) {
			 //14 - 21
			logger.info("Checking for dead neighbours");
			for (Address neighbour : neighborSet) {
				if ((!aliveSet.contains(neighbour)) && (!detectedSet.contains(neighbour))) {
					detectedSet.add(neighbour);
					trigger(new CrashEvent(neighbour), epfdPort);
					logger.info("DEAD : : " + neighbour.toString());
				}
			}
			
			aliveSet = new HashSet<Address>();
			ScheduleTimeout st2 = new ScheduleTimeout(gamma + delta);
			st2.setTimeoutEvent(new CheckTimeoutEvent(st2));
			trigger(st2, timer);
		}
	};

	Handler<Application1Init> handleInit = new Handler<Application1Init>() {
		public void handle(Application1Init event) {
			//2 - 5
			logger.info("Algorithm Start running");
			detectedSet = new HashSet<Address>();
			neighborSet = event.getNeighborSet();
			aliveSet = neighborSet;
			self = event.getSelf();
			
			delta = event.getDelta();
			gamma = event.getGamma();
			
			ScheduleTimeout st = new ScheduleTimeout(gamma);
			st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
			trigger(st, timer);
			ScheduleTimeout st2 = new ScheduleTimeout(gamma + delta);
			st2.setTimeoutEvent(new CheckTimeoutEvent(st2));
			trigger(st2, timer);
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};

	Handler<HeartbeatMessage> handlePp2pHeartbeatMessage = new Handler<HeartbeatMessage>() {
		public void handle(HeartbeatMessage event) {
			//24
			logger.info("Received hb message from {}", event.getSource().toString());
			aliveSet.add(event.getSource());
		}
	};
	
	private void doShutdown() { // TODO close when application is closing
		System.out.close();
		System.err.close();
		System.exit(0);
	}

}
