package se.kth.ict.id2203.pfd;

import se.sics.kompics.launch.Scenario;
import se.sics.kompics.launch.Topology;

@SuppressWarnings("serial")
public final class Assignment1aExecutor {
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static final void main(String[] args) {
		Topology topology1 = new Topology() {
			{
				node(1, "127.0.0.1", 22031);
				node(2, "127.0.0.1", 22032);
				link(1, 2, 4000, 0.5).bidirectional();
//				link(1, 2, 0, 0.99).bidirectional();
			}
		};

		Topology topology2 = new Topology() {
			{
				node(1, "127.0.0.1", 22031);
				node(2, "127.0.0.1", 22032);
				node(3, "127.0.0.1", 22033);
				node(4, "127.0.0.1", 22034);
				defaultLinks(1000, 0);
			}
		};
		//link(1, 2, 3000, 0.5).bidirectional();
		// link(1, 2, 3000, 0.5);
		// link(2, 1, 3000, 0.5);
		// link(3, 2, 3000, 0.5);
		// link(4, 2, 3000, 0.5);

		Scenario scenario1 = new Scenario(Assignment1aMain.class) {
			{
//				command(1, "S5000:Labc");
//				command(2, "S500:Pdef");
				command(1, "S5000");
				command(2, "S500");
			}
		};
		Scenario scenario2 = new Scenario(Assignment1aMain.class) {
			{
				command(1, "S500:La1:S300:PA1:X");
				command(2, "S500:Pb2:S300:LB2");
				command(3, "S500:Lc3:S300:PC3");
				command(4, "S500:Pd4:S300:LD4");
			}
		};

		 scenario1.executeOn(topology1);
		// scenario1.executeOn(topology2);
//		 scenario2.executeOn(topology2);
		// scenario1.executeOnFullyConnected(topology1);
		// scenario1.executeOnFullyConnected(topology2);
		// scenario2.executeOnFullyConnected(topology1);
		// scenario2.executeOnFullyConnected(topology2);

		System.exit(0);
//		scenario2.executeOn(topology2);
//		// move one of the below scenario executions above the exit for
//		// execution
//
//		scenario1.executeOn(topology1);
//		scenario1.executeOn(topology2);
//		scenario2.executeOn(topology1);
//		scenario2.executeOn(topology2);
//		scenario1.executeOnFullyConnected(topology1);
//		scenario1.executeOnFullyConnected(topology2);
//		scenario2.executeOnFullyConnected(topology1);
//		scenario2.executeOnFullyConnected(topology2);
	}
}
