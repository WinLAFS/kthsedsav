package se.kth.ict.id2203.uc;

import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import se.kth.ict.id2203.ac.RWAbortableConsensusInit;
import se.kth.ict.id2203.ac.components.RWAbortableConsensus;
import se.kth.ict.id2203.ac.ports.AbortableConsensus;
import se.kth.ict.id2203.beb.BasicBroadcastInit;
import se.kth.ict.id2203.beb.components.BasicBroadcast;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.eld.ELDInit;
import se.kth.ict.id2203.eld.components.ELD;
import se.kth.ict.id2203.pfd.ports.PerfectFailureDetector;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.delay.DelayLink;
import se.kth.ict.id2203.pp2p.delay.DelayLinkInit;
import se.kth.ict.id2203.riwc.Application3aInit;
import se.kth.ict.id2203.riwc.RIWCInit;
import se.kth.ict.id2203.riwc.ports.AtomicRegister;
import se.kth.ict.id2203.uc.components.Application4;
import se.kth.ict.id2203.uc.components.PaxosUniformConsensus;
import se.kth.ict.id2203.uc.ports.UniformConsensus;
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

public class Assignement4Main extends ComponentDefinition {

	private static long delta = 1000;
	private static long timeDelay = 2000;
	
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

		Kompics.createAndStart(Assignement4Main.class);
	}

	/**
	 * Instantiates a new assignment0 group0.
	 */
	public Assignement4Main() {
		// create components
		Component time = create(JavaTimer.class);
		Component network = create(MinaNetwork.class);
		Component pp2p = create(DelayLink.class);
		Component app = create(Application4.class);
		Component beb = create(BasicBroadcast.class);
		Component uc = create(PaxosUniformConsensus.class);
		Component ac = create(RWAbortableConsensus.class);
		Component eld = create(ELD.class);
		
		// handle possible faults in the components
		subscribe(handleFault, time.getControl());
		subscribe(handleFault, network.getControl());
		subscribe(handleFault, pp2p.getControl());
		subscribe(handleFault, app.getControl());
		subscribe(handleFault, beb.getControl());
		subscribe(handleFault, uc.getControl());
		subscribe(handleFault, ac.getControl());
		subscribe(handleFault, eld.getControl());

		// initialize the components
		Address self = topology.getSelfAddress();
		Set<Address> neighborSet = topology.getNeighbors(self);

		trigger(new MinaNetworkInit(self, 5), network.getControl());
		trigger(new DelayLinkInit(topology), pp2p.getControl());
		trigger(new Application4Init(commandScript, neighborSet, self), app.getControl());
		trigger(new BasicBroadcastInit(commandScript, neighborSet, self), beb.getControl());
		trigger(new UCInit(commandScript, neighborSet, self), uc.getControl());
		trigger(new RWAbortableConsensusInit(commandScript, neighborSet, self), ac.getControl());
		trigger(new ELDInit(commandScript, neighborSet, self, (int) delta, (int) timeDelay) , eld.getControl());

		// connect the components
		connect(app.getNegative(UniformConsensus.class), uc.getPositive(UniformConsensus.class));
		connect(app.getNegative(Timer.class), time.getPositive(Timer.class));
		
		connect(uc.getNegative(BEBPort.class), beb.getPositive(BEBPort.class));
		connect(uc.getNegative(AbortableConsensus.class), ac.getPositive(AbortableConsensus.class));
		connect(uc.getNegative(se.kth.ict.id2203.eld.ports.ELD.class), eld.getPositive(se.kth.ict.id2203.eld.ports.ELD.class));
		
		connect(ac.getNegative(BEBPort.class), beb.getPositive(BEBPort.class));
		connect(ac.getNegative(PerfectPointToPointLink.class), pp2p.getPositive(PerfectPointToPointLink.class));
		
		connect(beb.getNegative(PerfectPointToPointLink.class), pp2p.getPositive(PerfectPointToPointLink.class));
		
		connect(eld.getNegative(PerfectPointToPointLink.class), pp2p.getPositive(PerfectPointToPointLink.class));
		connect(eld.getNegative(Timer.class), time.getPositive(Timer.class));

		connect(pp2p.getNegative(Network.class), network.getPositive(Network.class));
		connect(pp2p.getNegative(Timer.class), time.getPositive(Timer.class));
		
	}

	Handler<Fault> handleFault = new Handler<Fault>() {
		public void handle(Fault fault) {
			fault.getFault().printStackTrace(System.err);
		}
	};
}
