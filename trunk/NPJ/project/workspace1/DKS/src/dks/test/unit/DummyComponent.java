/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.unit;

import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;

/**
 * The <code>DummyComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DummyComponent.java 232 2007-03-08 14:36:07Z Roberto $
 */
public class DummyComponent extends Component {

	protected DKSUnitTestCase testCase;

	/**
	 * @param scheduler
	 * @param registry
	 * @param marshaler
	 * @param marshaler
	 * @param structures
	 */
	public DummyComponent(Scheduler scheduler, ComponentRegistry registry,
			DKSUnitTestCase testCase) {
		super(scheduler, registry);
		this.testCase = testCase;
	}

//	/**
//	 * @param scheduler
//	 * @param componentRegistry
//	 * @param case1
//	 */
//	public DummyComponent(Scheduler scheduler,
//			ComponentRegistry componentRegistry, DKSSystemTestCase case1) {
//		super(scheduler, componentRegistry);
//		registrerForEvents();
//		this.testCase = case1;
//	}

	
	public void triggerEvent(Event event) {
		this.trigger(event);

	}

	/* (non-Javadoc)
	 * @see dks.arch.Component#registerEvents()
	 */
	@Override
	protected void registerForEvents() {
		// TODO Auto-generated method stub
		
	}

}
