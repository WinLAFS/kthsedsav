package se.sics.kompics.p2p.simulator.launch;

import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class Scenario1 extends Scenario {
private static SimulationScenario scenario = new SimulationScenario() {{
		
		StochasticProcess process1 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(1, Operations.peerJoin, uniform(Configuration.Log2Ring));
		}};

		StochasticProcess process2 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(499, Operations.peerJoin, uniform(Configuration.Log2Ring));
		}};


		StochasticProcess process3 = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
//			eventInterArrivalTime(exponential(500));
			raise(50, Operations.peerJoin, uniform(Configuration.Log2Ring));
			raise(50, Operations.peerFail, uniform(Configuration.Log2Ring));
		}};

		process1.start();
		process2.startAfterTerminationOf(2000, process1);
//		process3.startAfterTerminationOf(40000, process1);
	}};
	
//-------------------------------------------------------------------
	public Scenario1() {
		super(scenario);
	} 
}
