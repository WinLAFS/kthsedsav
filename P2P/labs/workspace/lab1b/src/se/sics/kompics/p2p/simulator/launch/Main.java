package se.sics.kompics.p2p.simulator.launch;

public class Main {
	public static void main(String[] args) throws Throwable {
		Configuration configuration = new Configuration();
		configuration.set();
		
		Scenario scenario = new SampleScenario();
		scenario.setSeed(System.currentTimeMillis());
		scenario.simulate();
	}
}
