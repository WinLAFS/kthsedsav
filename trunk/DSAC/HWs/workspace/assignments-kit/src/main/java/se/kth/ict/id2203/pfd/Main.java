package se.kth.ict.id2203.pfd;

import se.kth.ict.id2203.pfd.components.PerfectFailureDetector;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;

public class Main extends ComponentDefinition{

	private Component pfd;

	public Main() {
		System.out.println("Main created.");
		pfd = create(PerfectFailureDetector.class);
//		connect(component1.getNegative(HelloWorld.class), 
//				component2.getPositive(HelloWorld.class));
	}
	public static void main(String[] args) {
		Kompics.createAndStart(Main.class, 1);
		Kompics.shutdown();
	}

}
