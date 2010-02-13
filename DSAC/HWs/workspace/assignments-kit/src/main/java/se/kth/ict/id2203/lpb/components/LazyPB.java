package se.kth.ict.id2203.lpb.components;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.Flp2pMessage;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.lpb.lazyPBInit;
import se.kth.ict.id2203.lpb.events.GossipTimeoutEvent;
import se.kth.ict.id2203.lpb.events.pbBroadcast;
import se.kth.ict.id2203.lpb.ports.ProbabilisticBroadcast;
import se.kth.ict.id2203.unb.components.SimpleUnreliableBroadcast;
import se.kth.ict.id2203.unb.events.unDeliver;
import se.kth.ict.id2203.unb.ports.UnreliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.Timer;

public class LazyPB extends ComponentDefinition {
	Positive<FairLossPointToPointLink> flp2p = positive(FairLossPointToPointLink.class);
	Positive<UnreliableBroadcast> ub = positive(UnreliableBroadcast.class);
	Positive<Timer> timer = positive(Timer.class);
	Negative<ProbabilisticBroadcast> pb = negative(ProbabilisticBroadcast.class);

	private static final Logger logger = LoggerFactory
			.getLogger(SimpleUnreliableBroadcast.class);

	private Set<Address> neighborSet;
	private Address self;
	
	private int[] delivered;
	private Set<unDeliver> pending;
	private Set<unDeliver> stored;
	private double storetreshold;
	private int fanouts;
	private int ttl;

	public LazyPB() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleFlp2pMessage, flp2p);
		subscribe(handleUNDeliver, ub);
		subscribe(handlePBMessage, pb);
		subscribe(gtHandler, timer);
	}
	
	Handler<lazyPBInit> handleInit = new Handler<lazyPBInit>() {
		public void handle(lazyPBInit event) {
			neighborSet = event.getNeighborSet();
			delivered = new int[neighborSet.size()];
			self = event.getSelf();
			pending = new HashSet<unDeliver>();
			stored = new HashSet<unDeliver>();
			storetreshold = event.getStoreTreshold();
			fanouts = event.getFanouts();
			ttl = event.getTtl();
			logger.debug("lazyPBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	Handler<GossipTimeoutEvent> gtHandler = new Handler<GossipTimeoutEvent>() {
		public void handle(GossipTimeoutEvent arg0) {
			//TODO ADD LOGIC
		}
	};
	
	Handler<unDeliver> handleUNDeliver = new Handler<unDeliver>() {
		public void handle(unDeliver arg0) {
			// TODO ADD LOGIC
			
		}
	};
	
	Handler<Flp2pMessage> handleFlp2pMessage = new Handler<Flp2pMessage>() {
		public void handle(Flp2pMessage event) {
			// TODO ADD LOGIC
		}
	};
	
	Handler<pbBroadcast> handlePBMessage = new Handler<pbBroadcast>() {
		public void handle(pbBroadcast event) {
			// TODO ADD LOGIC
		}
	};
	
}
