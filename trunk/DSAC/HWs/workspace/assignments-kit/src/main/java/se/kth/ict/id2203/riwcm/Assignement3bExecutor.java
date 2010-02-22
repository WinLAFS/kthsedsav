package se.kth.ict.id2203.riwcm;

import se.sics.kompics.launch.Scenario;
import se.sics.kompics.launch.Topology;

public final class Assignement3bExecutor {
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
				node(3, "127.0.0.1", 22033);
				node(4, "127.0.0.1", 22034);
				node(5, "127.0.0.1", 22035);
				node(6, "127.0.0.1", 22036);
				node(7, "127.0.0.1", 22037);
				node(8, "127.0.0.1", 22038);
				link(1, 2, 1000, 0.5).bidirectional();
				defaultLinks(1234, 0.5);
				
			}
		};

		Topology topology2 = new Topology() {
			{
				node(1, "127.0.0.1", 22031);
				node(2, "127.0.0.1", 22032);
				node(3, "127.0.0.1", 22033);
				node(4, "127.0.0.1", 22034);
//				link(1, 2, 3213, 0).bidirectional();
				defaultLinks(1234, 0.4);
			}
		};

		Topology topology3 = new Topology() {
			{
				node(0, "127.0.0.1", 22031);
				node(1, "127.0.0.1", 22032);
				node(2, "127.0.0.1", 22033);
				defaultLinks(1000, 0);
			}
		};

		Scenario scenario1 = new Scenario(Assignement3bMain.class) {
			{
//				command(1, "S5000:Labc");
//				command(2, "S500:Pdef");
				command(1, "S500:B1:B2:B3:B4:B5:B6");
				command(2, "");
				command(3, "");
				command(4, "");
				command(5, "");
				command(6, "");
				command(7, "");
				command(8, "");
			}
		};
		
		Scenario scenario3 = new Scenario(Assignement3bMain.class) {
			{
				command(1, "S500:Lmsg1:S6000:X").recover("R:S500:Pmsg3:S500:X",
						5000);
				command(2, "S500");
			}
		};
		
		Scenario scenario2 = new Scenario(Assignement3bMain.class) {
			{
				command(1, "B1");
				command(2, "S1");
				command(3, "S1");
				command(4, "S1");
//				command(3, "S500:Lc3:S300:PC3");
//				command(4, "S500:Pd4:S300:LD4");
			}
		};

		Scenario scenario4 = new Scenario(Assignement3bMain.class) {
			{
				command(0, "S30000");
				command(1, "S500:W4:S25000");
				command(2, "S10000:R");
				
//				command(0, "S3");
//				command(1, "S5");
//				command(2, "S1");
			}
		};

//		 scenario1.executeOn(topology1);
//		scenario3.executeOn(topology1);
		 scenario4.executeOn(topology3);
		// scenario2.executeOn(topology1);
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
