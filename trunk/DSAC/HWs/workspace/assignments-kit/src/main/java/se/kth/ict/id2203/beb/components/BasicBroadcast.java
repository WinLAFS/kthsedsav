package se.kth.ict.id2203.beb.components;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.beb.BasicBroadcastInit;
import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class BasicBroadcast extends ComponentDefinition {
	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Negative<BEBPort> beb = negative(BEBPort.class);

	private static final Logger logger = LoggerFactory
			.getLogger(BasicBroadcast.class);

	private Set<Address> neighborSet;
	private Address self;

	/**
	 * Instantiates a new application0.
	 */
	public BasicBroadcast() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pMessage, pp2p);
		subscribe(handleUnreliabeBroadcast, beb);
	}

	Handler<BebBroadcast> handleUnreliabeBroadcast = new Handler<BebBroadcast>() {

		public void handle(BebBroadcast event) {
			BebMessage bebMessage = event.getBebMessage();
			for (Address neigbour : neighborSet) {
				trigger(new Pp2pSend(neigbour, bebMessage), pp2p); 
			}
			trigger(new Pp2pSend(self, bebMessage), pp2p);
		}
	};
	
	Handler<BasicBroadcastInit> handleInit = new Handler<BasicBroadcastInit>() {
		public void handle(BasicBroadcastInit event) {
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			logger.debug("bebBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};

	Handler<BebMessage> handlePp2pMessage = new Handler<BebMessage>() {
		public void handle(BebMessage event) {
			trigger(event.getBebDeliver(), beb);
		}
	};
}
