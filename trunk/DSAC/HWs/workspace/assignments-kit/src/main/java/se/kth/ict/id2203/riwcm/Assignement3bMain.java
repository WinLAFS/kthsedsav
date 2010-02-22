package se.kth.ict.id2203.riwcm;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import se.kth.ict.id2203.beb.BasicBroadcastInit;
import se.kth.ict.id2203.beb.components.BasicBroadcast;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.delay.DelayLink;
import se.kth.ict.id2203.pp2p.delay.DelayLinkInit;
import se.kth.ict.id2203.riwcm.components.Application3b;
import se.kth.ict.id2203.riwcm.components.ReadImposeWriteConsultMajority;
import se.kth.ict.id2203.riwcm.ports.AtomicRegister;
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

public class Assignement3bMain extends ComponentDefinition {
	private static final int numberOfRegister = 1;

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

		Kompics.createAndStart(Assignement3bMain.class);
	}

	/**
	 * Instantiates a new assignment0 group0.
	 */
	public Assignement3bMain() {
		// create components
		Component time = create(JavaTimer.class);
		Component network = create(MinaNetwork.class);
		Component pp2p = create(DelayLink.class);
//		Component flp2p = create(DelayDropLink.class);
		Component app = create(Application3b.class);
		Component beb = create(BasicBroadcast.class);
		Component riwcm = create(ReadImposeWriteConsultMajority.class);
		
		// handle possible faults in the components
		subscribe(handleFault, time.getControl());
		subscribe(handleFault, network.getControl());
		subscribe(handleFault, pp2p.getControl());
		subscribe(handleFault, app.getControl());
		subscribe(handleFault, beb.getControl());
		subscribe(handleFault, riwcm.getControl());

		// initialize the components
		Address self = topology.getSelfAddress();
		Set<Address> neighborSet = topology.getNeighbors(self);

		trigger(new MinaNetworkInit(self, 5), network.getControl());
		trigger(new DelayLinkInit(topology), pp2p.getControl());
		trigger(new BasicBroadcastInit(commandScript, neighborSet, self), beb.getControl());
		trigger(new Application3bInit(commandScript, neighborSet, self, numberOfRegister), app.getControl());
		trigger(new RIWCMInit(commandScript, neighborSet, self, numberOfRegister), riwcm.getControl()); 

		// connect the components
		connect(app.getNegative(AtomicRegister.class), riwcm.getPositive(AtomicRegister.class));
		connect(app.getNegative(Timer.class), time.getPositive(Timer.class));
		connect(riwcm.getNegative(BEBPort.class), beb.getPositive(BEBPort.class));
		connect(riwcm.getNegative(PerfectPointToPointLink.class), pp2p.getPositive(PerfectPointToPointLink.class));
		connect(beb.getNegative(PerfectPointToPointLink.class), pp2p.getPositive(PerfectPointToPointLink.class));

		connect(pp2p.getNegative(Network.class), network.getPositive(Network.class));
		connect(pp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		
	}

	Handler<Fault> handleFault = new Handler<Fault>() {
		public void handle(Fault fault) {
			fault.getFault().printStackTrace(System.err);
		}
	};
}
