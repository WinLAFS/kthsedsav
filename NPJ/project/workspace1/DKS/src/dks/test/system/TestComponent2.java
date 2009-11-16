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
 * The <code>TestComponent2</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestComponent2.java 270 2007-03-30 16:16:05Z Roberto $
 */
public class TestComponent2 extends Component {

	/**
	 * @param scheduler
	 * @param registry
	 */
	protected TestComponent2(Scheduler scheduler, ComponentRegistry registry) {
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
		// register(ConcurrentEvent2.class, "handleConcurrentEvent2");
		registerDependentEvent(ConcurrentEvent3.class,
				"handleConcurrentEvent3", new Class[] { TestComponent1.class });

	}

	public void handleConcurrentEvent1(ConcurrentEvent1 event) {
		System.out.println("TESTCOMPONENT2 : ConcurrentEvent1 processed");
	}

	public void handleConcurrentEvent2(ConcurrentEvent2 event) {
		System.out.println("TESTCOMPONENT2 : ConcurrentEvent2 processed");
	}

	public void handleConcurrentEvent3(ConcurrentEvent3 event) {
		System.out.println("TESTCOMPONENT2 : ConcurrentEvent3 processed");
	}

}
