package se.kth.ict.id2203.pfd.components;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.Application0;
import se.kth.ict.id2203.application.Application0Init;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.pfd.events.CheckTimeoutEvent;
import se.kth.ict.id2203.pfd.events.HeartbeatMessage;
import se.kth.ict.id2203.pfd.events.HeartbeatTimeoutEvent;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class PFD extends ComponentDefinition {

	long Delta = 1000;
	long Gamma = 4000;

	private Set<Address> neighborSet;
	private Set<Address> aliveSet;
	private Set<Address> detectedSet = new HashSet<Address>();
	private Address self;

	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Positive<FairLossPointToPointLink> flp2p = positive(FairLossPointToPointLink.class);

	private static final Logger logger = LoggerFactory
			.getLogger(Application0.class);

	// private Positive<FailureDetectorPort> fpPositive =
	// positive(FailureDetectorPort.class);
	Positive<Timer> timer = positive(Timer.class);



	public PFD() {
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
			
			for (Address neighbor : neighborSet) {
				logger.info("Sending hb message {} to {}", message, neighbor);
				trigger(new HeartbeatMessage(self), pp2p);
			}
			
		}
	};

	Handler<CheckTimeoutEvent> ctHandler = new Handler<CheckTimeoutEvent>() {

		public void handle(CheckTimeoutEvent arg0) {
			 // scheduling a timeout
			
			logger.debug("ok, it works!!");
		}
	};

	Handler<Application0Init> handleInit = new Handler<Application0Init>() {
		public void handle(Application0Init event) {
			neighborSet = event.getNeighborSet();
			aliveSet = neighborSet;
			self = event.getSelf();
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
			ScheduleTimeout st = new ScheduleTimeout(Gamma);
			st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
			trigger(st, timer);
			ScheduleTimeout st2 = new ScheduleTimeout(Gamma + Delta);
			st2.setTimeoutEvent(new CheckTimeoutEvent(st2));
			trigger(st2, timer);
			logger.debug("p1 started!");
		}
	};

	Handler<HeartbeatMessage> handlePp2pHeartbeatMessage = new Handler<HeartbeatMessage>() {
		public void handle(HeartbeatMessage event) {
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
