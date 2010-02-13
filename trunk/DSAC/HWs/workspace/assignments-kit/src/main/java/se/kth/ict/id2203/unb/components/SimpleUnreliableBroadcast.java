package se.kth.ict.id2203.unb.components;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.Flp2pMessage;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.flp2p.Flp2pSend;
import se.kth.ict.id2203.unb.SimpleUnreliableBroadcastInit;
import se.kth.ict.id2203.unb.events.unBroadcast;
import se.kth.ict.id2203.unb.events.unDeliver;
import se.kth.ict.id2203.unb.ports.UnreliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class SimpleUnreliableBroadcast extends ComponentDefinition {
	Positive<FairLossPointToPointLink> flp2p = positive(FairLossPointToPointLink.class);
	Negative<UnreliableBroadcast> unb = negative(UnreliableBroadcast.class);

	private static final Logger logger = LoggerFactory
			.getLogger(SimpleUnreliableBroadcast.class);

	private Set<Address> neighborSet;
	private Address self;

	/**
	 * Instantiates a new application0.
	 */
	public SimpleUnreliableBroadcast() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleFlp2pMessage, flp2p);
		subscribe(handleUnreliabeBroadcast, unb);
	}

	Handler<unBroadcast> handleUnreliabeBroadcast = new Handler<unBroadcast>() {

		public void handle(unBroadcast event) {
			unDeliver und = event.getUnDeliver();
			for (Address neigbour : neighborSet) {
				trigger(new Flp2pSend(neigbour, new Flp2pMessage(und.getSource(), und.getMessage())), flp2p);
			}
		}
	};
	
	Handler<SimpleUnreliableBroadcastInit> handleInit = new Handler<SimpleUnreliableBroadcastInit>() {
		public void handle(SimpleUnreliableBroadcastInit event) {
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			logger.debug("unBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};

	Handler<Flp2pMessage> handleFlp2pMessage = new Handler<Flp2pMessage>() {
		public void handle(Flp2pMessage event) {
			logger.info("Received broadcast message message {}", event.getMessage());
			trigger(new unDeliver(event.getSource(), event.getMessage()), unb);
		}
	};
}
