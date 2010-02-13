package se.kth.ict.id2203.lpb;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import se.kth.ict.id2203.epfd.Application1Init;
import se.kth.ict.id2203.epfd.Assignment1bMain;
import se.kth.ict.id2203.epfd.ports.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.flp2p.delay.DelayDropLink;
import se.kth.ict.id2203.flp2p.delay.DelayDropLinkInit;
import se.kth.ict.id2203.lpb.components.Application2;
import se.kth.ict.id2203.lpb.components.LazyPB;
import se.kth.ict.id2203.lpb.ports.ProbabilisticBroadcast;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.delay.DelayLink;
import se.kth.ict.id2203.pp2p.delay.DelayLinkInit;
import se.kth.ict.id2203.unb.components.SimpleUnreliableBroadcast;
import se.kth.ict.id2203.unb.ports.UnreliableBroadcast;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.address.Address;
import se.sics.kompics.launch.Topology;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.network.mina.MinaNetworkInit;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class Assignement2Main extends ComponentDefinition {

	static {
		PropertyConfigurator.configureAndWatch("log4j.properties");
	}
	private static int selfId;
	private static String commandScript;
	Topology topology = Topology.load(System.getProperty("topology"), selfId);
	

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		selfId = Integer.parseInt(args[0]);
		commandScript = args[1];

		Kompics.createAndStart(Assignment1bMain.class);
	}

	/**
	 * Instantiates a new assignment0 group0.
	 */
	public Assignement2Main() {
		// create components
		Component time = create(JavaTimer.class);
		Component network = create(MinaNetwork.class);
		Component pp2p = create(DelayLink.class);
		Component flp2p = create(DelayDropLink.class);
		Component app = create(Application2.class);
		Component lpb = create(LazyPB.class);
		Component unb = create(SimpleUnreliableBroadcast.class);

		// handle possible faults in the components
		subscribe(handleFault, time.getControl());
		subscribe(handleFault, network.getControl());
		subscribe(handleFault, pp2p.getControl());
		subscribe(handleFault, flp2p.getControl());
		subscribe(handleFault, app.getControl());
		subscribe(handleFault, lpb.getControl());
		subscribe(handleFault, unb.getControl());

		// initialize the components
		Address self = topology.getSelfAddress();
		Set<Address> neighborSet = topology.getNeighbors(self);

		trigger(new MinaNetworkInit(self, 5), network.getControl());
		trigger(new DelayLinkInit(topology), pp2p.getControl());
		trigger(new DelayDropLinkInit(topology, 0), flp2p.getControl());
//		trigger(new Application1Init(commandScript, neighborSet, self, this.TIMEDELAY, this.DELTA), epfd
//				.getControl());
//		trigger(new Application1Init(commandScript, neighborSet, self), app
//				.getControl()); TODO

		// connect the components
		connect(app.getNegative(ProbabilisticBroadcast.class), lpb.getPositive(ProbabilisticBroadcast.class));
		connect(lpb.getNegative(UnreliableBroadcast.class), unb.getPositive(UnreliableBroadcast.class));
		connect(lpb.getNegative(FairLossPointToPointLink.class), flp2p.getPositive(FairLossPointToPointLink.class));
		connect(unb.getNegative(FairLossPointToPointLink.class), flp2p.getPositive(FairLossPointToPointLink.class));
		connect(pp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(lpb.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(app.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(pp2p.getNegative(Network.class), network
				.getPositive(Network.class));

		connect(flp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(flp2p.getNegative(Network.class), network
				.getPositive(Network.class));
	}

	Handler<Fault> handleFault = new Handler<Fault>() {
		public void handle(Fault fault) {
			fault.getFault().printStackTrace(System.err);
		}
	};

}
