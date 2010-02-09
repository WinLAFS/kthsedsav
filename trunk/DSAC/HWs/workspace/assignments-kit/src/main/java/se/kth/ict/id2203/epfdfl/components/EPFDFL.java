package se.kth.ict.id2203.epfdfl.components;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.epfdfl.Application1Init;
import se.kth.ict.id2203.epfdfl.events.CheckTimeoutEvent;
import se.kth.ict.id2203.epfdfl.events.HeartbeatMessage;
import se.kth.ict.id2203.epfdfl.events.HeartbeatTimeoutEvent;
import se.kth.ict.id2203.epfdfl.events.RestoreEvent;
import se.kth.ict.id2203.epfdfl.events.SuspectEvent;
import se.kth.ict.id2203.epfdfl.ports.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.flp2p.Flp2pSend;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class EPFDFL extends ComponentDefinition {

	long delta;
	long timeDelay;
	long period;
	private Set<Address> neighborSet;
	private Set<Address> aliveSet;
	private Set<Address> suspectedSet;
	private Address self;

//	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Positive<FairLossPointToPointLink> flp2p = positive(FairLossPointToPointLink.class);
	Positive<EventuallyPerfectFailureDetector> epfdPort = positive(EventuallyPerfectFailureDetector.class);
	
	private static final Logger logger = LoggerFactory
			.getLogger(EPFDFL.class);

	// private Positive<FailureDetectorPort> fpPositive =
	// positive(FailureDetectorPort.class);
	Positive<Timer> timer = positive(Timer.class);



	public EPFDFL() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pHeartbeatMessage, flp2p);
		subscribe(handleHeartbeat, timer);
		subscribe(handleCheck, timer);
	}

	Handler<HeartbeatTimeoutEvent> handleHeartbeat = new Handler<HeartbeatTimeoutEvent>() {

		public void handle(HeartbeatTimeoutEvent arg0) {
			logger.info("Sending hb to all alive to neighboors");
			String message = "heartbeat";
			
			//9 - 12
			for (Address neighbor : neighborSet) {
//				logger.info("Sending hb message {} to {}", message, neighbor);
				HeartbeatMessage hbMessage = new HeartbeatMessage(self);
				trigger(new Flp2pSend(neighbor, hbMessage), flp2p);
			}
			
			ScheduleTimeout st = new ScheduleTimeout(timeDelay);
			st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
			trigger(st, timer);
		}
	};

	Handler<CheckTimeoutEvent> handleCheck = new Handler<CheckTimeoutEvent>() {

		public void handle(CheckTimeoutEvent arg0) {
			 //15 - 28
			logger.info("Checking for dead neighbours");
			Set<Address> checkIntersection = new HashSet<Address>();
			checkIntersection.addAll(aliveSet);
			checkIntersection.retainAll(suspectedSet);
			if (!checkIntersection.isEmpty()) {
				period += delta;
				logger.info("Increasing period, false alarm! New period: " + period);
			}
			
			for (Address neighbour : neighborSet) {
				if (!(aliveSet.contains(neighbour)) && !(suspectedSet.contains(neighbour))) {
					logger.info("Suspected: " + neighbour.toString());
					suspectedSet.add(neighbour);
					trigger(new SuspectEvent(neighbour), epfdPort);
				}
				else if ((aliveSet.contains(neighbour)) && (suspectedSet.contains(neighbour))) {
					logger.info("Restored: " + neighbour.toString());
					suspectedSet.remove(neighbour);
					trigger(new RestoreEvent(neighbour), epfdPort);
				}
			}
			
			aliveSet = new HashSet<Address>();
			
			ScheduleTimeout st2 = new ScheduleTimeout(period);
			st2.setTimeoutEvent(new CheckTimeoutEvent(st2));
			trigger(st2, timer);
		}
	};

	Handler<Application1Init> handleInit = new Handler<Application1Init>() {
		public void handle(Application1Init event) {
			//1-7
			logger.info("Algorithm Start running");
			suspectedSet = new HashSet<Address>();
			neighborSet = event.getNeighborSet();
			aliveSet = neighborSet;
			self = event.getSelf();
			
			delta = event.getDelta();
			timeDelay = event.getGamma();
			period = timeDelay;
			
			ScheduleTimeout st = new ScheduleTimeout(timeDelay);
			st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
			trigger(st, timer);
			ScheduleTimeout st2 = new ScheduleTimeout(period);
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
			//31
			logger.info("Received hb {}", event.getSource().toString());
			aliveSet.add(event.getSource());
		}
	};
	
	private void doShutdown() { // TODO close when application is closing
		System.out.close();
		System.err.close();
		System.exit(0);
	}

}
