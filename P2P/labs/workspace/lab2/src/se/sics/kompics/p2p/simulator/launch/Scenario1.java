package se.sics.kompics.p2p.simulator.launch;

import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class Scenario1 extends Scenario {
	private static SimulationScenario scenario = new SimulationScenario() {{
		StochasticProcess process1 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(1, Operations.trackerJoin, constant(Configuration.TRACKER_ID));
		}};
		StochasticProcess process2 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(1, Operations.peerJoin(PeerType.SEED), uniform(13));
		}};

		StochasticProcess process3 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(199, Operations.peerJoin(PeerType.LEECHER), uniform(13));
		}};

		process1.start();
		process2.startAfterTerminationOf(2000, process1);
		process3.startAfterTerminationOf(2000, process2);
	}};
	
//-------------------------------------------------------------------
	public Scenario1() {
		super(scenario);
	} 
}
