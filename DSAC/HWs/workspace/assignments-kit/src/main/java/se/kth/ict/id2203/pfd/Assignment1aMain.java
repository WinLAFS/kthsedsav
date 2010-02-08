package se.kth.ict.id2203.pfd;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import se.kth.ict.id2203.Assignment0Group0Main;
import se.kth.ict.id2203.application.Application0;
import se.kth.ict.id2203.application.Application0Init;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.flp2p.delay.DelayDropLink;
import se.kth.ict.id2203.flp2p.delay.DelayDropLinkInit;
import se.kth.ict.id2203.pfd.components.Application1a;
import se.kth.ict.id2203.pfd.components.PFD;
import se.kth.ict.id2203.pfd.ports.PerfectFailureDetector;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.delay.DelayLink;
import se.kth.ict.id2203.pp2p.delay.DelayLinkInit;
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

public class Assignment1aMain extends ComponentDefinition {

	static {
		PropertyConfigurator.configureAndWatch("log4j.properties");
	}
	private static int selfId;
	private static String commandScript;
	Topology topology = Topology.load(System.getProperty("topology"), selfId);
	
	private static long GAMMA = 4000;
	private static long DELTA = 1000;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		selfId = Integer.parseInt(args[0]);
		commandScript = args[1];

		Kompics.createAndStart(Assignment1aMain.class);
	}

	/**
	 * Instantiates a new assignment0 group0.
	 */
	public Assignment1aMain() {
		// create components
		Component time = create(JavaTimer.class);
		Component network = create(MinaNetwork.class);
		Component pp2p = create(DelayLink.class);
		Component flp2p = create(DelayDropLink.class);
		Component app = create(Application1a.class);
		Component pfd = create(PFD.class);

		// handle possible faults in the components
		subscribe(handleFault, time.getControl());
		subscribe(handleFault, network.getControl());
		subscribe(handleFault, pp2p.getControl());
		subscribe(handleFault, flp2p.getControl());
		subscribe(handleFault, pfd.getControl());
		subscribe(handleFault, app.getControl());

		// initialize the components
		Address self = topology.getSelfAddress();
		Set<Address> neighborSet = topology.getNeighbors(self);

		trigger(new MinaNetworkInit(self, 5), network.getControl());
		trigger(new DelayLinkInit(topology), pp2p.getControl());
		trigger(new DelayDropLinkInit(topology, 0), flp2p.getControl());
		trigger(new Application1Init(commandScript, neighborSet, self, this.GAMMA, this.DELTA), pfd
				.getControl());
		trigger(new Application1Init(commandScript, neighborSet, self, this.GAMMA, this.DELTA), app
				.getControl());

		// connect the components
		connect(pfd.getNegative(PerfectPointToPointLink.class), pp2p
				.getPositive(PerfectPointToPointLink.class));
		connect(pfd.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(pp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(pp2p.getNegative(Network.class), network
				.getPositive(Network.class));

		connect(flp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(flp2p.getNegative(Network.class), network
				.getPositive(Network.class));
		connect(pfd.getNegative(PerfectFailureDetector.class), app.getPositive(PerfectFailureDetector.class));
	}

	Handler<Fault> handleFault = new Handler<Fault>() {
		public void handle(Fault fault) {
			fault.getFault().printStackTrace(System.err);
		}
	};

}
