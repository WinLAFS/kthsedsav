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
import dks.comm.mina.events.DeliverMessageEvent;
import dks.test.events.ConcurrentEvent1;
import dks.test.events.ConcurrentEvent2;
import dks.test.events.ConcurrentEvent3;

/**
 * The <code>TestComponent2</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestComponent3.java 496 2007-12-20 15:39:02Z roberto $
 */
public class TestComponent3 extends Component {

	/**
	 * @param scheduler
	 * @param registry
	 */
	protected TestComponent3(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);

		registerForEvents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		register(ConcurrentEvent1.class, "handleConcurrentEvent1");
		register(ConcurrentEvent2.class, "handleConcurrentEvent2");
		// registerDependentEvent(ConcurrentEvent3.class,
		// "handleConcurrentEvent3", new Class[] { TestComponent1.class });

	}

	public void handleConcurrentEvent1(ConcurrentEvent1 event) {
		System.out.println("TESTCOMPONENT3 : ConcurrentEvent1 processed");
	}

	public void handleConcurrentEvent2(ConcurrentEvent2 event) {
		System.out.println("TESTCOMPONENT3 : ConcurrentEvent2 processed");
	}

	public void handleConcurrentEvent3(ConcurrentEvent3 event) {
		System.out.println("TESTCOMPONENT3 : ConcurrentEvent3 processed");
	}
	
	public void handleSimpleMessage(DeliverMessageEvent event) {
		System.out.println("Simple message processed");
	}

}
