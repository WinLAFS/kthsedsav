/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.system;

import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.test.events.ConcurrentEvent1;
import dks.test.events.ConcurrentEvent2;
import dks.test.events.ConcurrentEvent3;

/**
 * The <code>TestComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestComponent1.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class TestComponent1 extends Component {

	/**
	 * @param scheduler
	 * @param registry
	 */
	public TestComponent1(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);

		registerForEvents();

		registerForConsumers();
	}

	/**
	 * 
	 */
	private void registerForConsumers() {
		// registerConsumer(method, SimpleMessage.getStaticMessageType());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {

		registerDependentEvent(ConcurrentEvent1.class,
				"handleConcurrentEvent1", new Class[] { TestComponent2.class });
		register(ConcurrentEvent2.class, "handleConcurrentEvent2");
		register(ConcurrentEvent3.class, "handleConcurrentEvent3");
	}

	public void handleConcurrentEvent1(ConcurrentEvent1 event) {
		System.out
				.println("TESTCOMPONENT1 : ConcurrentEvent1 processed, waiting...");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleConcurrentEvent2(ConcurrentEvent2 event) {
		System.out.println("TESTCOMPONENT1 : ConcurrentEvent2 processed");
	}

	public void handleConcurrentEvent3(ConcurrentEvent3 event) {
		System.out.println("TESTCOMPONENT1 : ConcurrentEvent3 processed");
	}

	
}
