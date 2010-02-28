package se.kth.ict.id2203.eld.components;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.eld.ELDInit;
import se.kth.ict.id2203.eld.events.Heartbeat;
import se.kth.ict.id2203.eld.events.Trust;
import se.kth.ict.id2203.pfd.events.HeartbeatTimeoutEvent;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class ELD extends ComponentDefinition {
	private Set<Address> neighborSet;
	private Set<Address> candidateSet;
	private Address self;
	private Address leader;
	private long delta;
	private long timeDelay;
	private long period;

	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Negative<se.kth.ict.id2203.eld.ports.ELD> eldPort = negative(se.kth.ict.id2203.eld.ports.ELD.class);
	
	private static final Logger logger = LoggerFactory
			.getLogger(ELD.class);

	// private Positive<FailureDetectorPort> fpPositive =
	// positive(FailureDetectorPort.class);
	Positive<Timer> timer = positive(Timer.class);



	public ELD() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pHeartbeatMessage, pp2p);
		subscribe(handleHeartBeatTimeout, timer);
	}

	Handler<HeartbeatTimeoutEvent> handleHeartBeatTimeout = new Handler<HeartbeatTimeoutEvent>() {

		public void handle(HeartbeatTimeoutEvent event) {
			/**
       startTimer(period);
			 */
			Address newLeader = selectLeader();
			if (!leader.equals(newLeader)) {
				leader = newLeader;
				period += delta;
				trigger(new Trust(leader), eldPort);
			}
			initializeHeartbeat();
		}
	};


	Handler<ELDInit> handleInit = new Handler<ELDInit>() {
		public void handle(ELDInit event) {
			logger.info("ELD Start running");
			timeDelay = event.getTimeDelay();
			delta = event.getDelta();
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			candidateSet = new HashSet<Address>();
			candidateSet.add(self);
			candidateSet.addAll(neighborSet);
			leader = selectLeader();
			trigger(new Trust(leader), eldPort);
			period = timeDelay;
			
			initializeHeartbeat();
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};

	Handler<Heartbeat> handlePp2pHeartbeatMessage = new Handler<Heartbeat>() {
		public void handle(Heartbeat event) {
			candidateSet.add(event.getSource());
		}
	};



	protected Address selectLeader() {
		Address curLeader = self;
		for (Address canditate : candidateSet) {
			if (canditate.getId() < curLeader.getId()) {
				curLeader = canditate;
			}
		}
		
		return curLeader;
	}



	/**
	 * send heatbeats, empties the canditateSet and renews the timer
	 */
	private void initializeHeartbeat() {
		for (Address neihbourAddress : neighborSet) {
			trigger(new Pp2pSend(neihbourAddress, new Heartbeat(self)), pp2p);
		}
		candidateSet = new HashSet<Address>();
		ScheduleTimeout st = new ScheduleTimeout(period);
		st.setTimeoutEvent(new HeartbeatTimeoutEvent(st));
		trigger(st, timer);
	}
}
