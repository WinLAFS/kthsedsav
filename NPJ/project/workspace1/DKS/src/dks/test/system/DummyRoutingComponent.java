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

import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.router.events.LookupResultEvent;
import dks.test.unit.DKSUnitTestCase;
import dks.test.unit.DummyComponent;

/**
 * The <code>DummyComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DummyRoutingComponent.java 222 2007-03-05 18:28:55Z Roberto $
 */
public class DummyRoutingComponent extends DummyComponent{


	/**
	 * @param scheduler
	 * @param registry
	 * @param marshaler
	 * @param marshaler
	 * @param structures
	 */
	public DummyRoutingComponent(Scheduler scheduler,
			ComponentRegistry registry, DKSUnitTestCase testCase) {
		super(scheduler, registry, testCase);
		registrerForEvents();
	}

	private void registrerForEvents() {

		register(LookupResultEvent.class, "handleEvent");

	}

	public void handleEvent(LookupResultEvent event) {
		testCase.eventQueue.add(event);
	}

}
