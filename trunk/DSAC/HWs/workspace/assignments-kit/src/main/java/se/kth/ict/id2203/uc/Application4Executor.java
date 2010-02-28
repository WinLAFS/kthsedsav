package se.kth.ict.id2203.uc;

import se.sics.kompics.launch.Scenario;
import se.sics.kompics.launch.Topology;

public final class Application4Executor {
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static final void main(String[] args) {

		Topology topologyEx3 = new Topology() {
			{
				node(1, "127.0.0.1", 22031);
				node(2, "127.0.0.1", 22032);
				node(3, "127.0.0.1", 22033);
				defaultLinks(1000, 0);
			}
		};
		
		Scenario scenarioEx3_1 = new Scenario(Assignement4Main.class) {
			{;
				command(1, "D2200:P1-1");
				command(2, "D2000:P1-2");
				command(3, "D2000:P1-3");
			}
		};
		
		Scenario scenarioEx3_2 = new Scenario(Assignement4Main.class) {
			{;
				command(1, "D2000:P1-1");
				command(2, "D2200:P1-2");
				command(3, "D2200:P1-3");
			}
		};
		Scenario scenarioEx3_21 = new Scenario(Assignement4Main.class) {
			{;
			command(1, "P1-1:P2-2:D12341:P3-3:P4-4");
			command(2, "S1");
			command(3, "S1");
			}
		};
		
		Scenario scenarioEmpty = new Scenario(Assignement4Main.class) {
			{;
				command(1, "S1");
				command(2, "S1");
				command(3, "S1");
			}
		};

		
//		scenarioEmpty.executeOn(topologyEx3);
//		scenarioEx3_1.executeOn(topologyEx3);
		scenarioEx3_21.executeOn(topologyEx3);

		System.exit(0);
	}
}
