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


/**
 * The <code>TestFingerRouterLookups</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestFingerRouterLookups.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class TestFingerRouterLookups extends DKSSystemTestCase {

	/**
	 * 
	 */
	public TestFingerRouterLookups() {
	}

	/**
	 * @param arg0
	 */
	public TestFingerRouterLookups(String arg0) {
		super(arg0);
	}

	public void setUp() {
		super.setUp();

		// CREATING DUMMY COMPONENT
		dummyComponent = new DummyRoutingComponent(node1.getScheduler(), node1
				.getComponentRegistry(), this);

	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestFingerRouterLookups.class);
	}

	public void testLookups() {

		/*startExternalNode(100, 12000);

		startExternalNode(105, 12001);

		startExternalNode(110, 12002);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		asserT("RouterUsed", "dks.router.FingerRouterComponent", "router");

		try {

			// Unreliable transitive
			UnreliableLookupRequestEvent unreliableTransitiveLookupRequest = new UnreliableLookupRequestEvent(
					BigInteger.valueOf(109), LookupStrategy.TRANSITIVE,
					new StabGetSuccListReqMessage());

			dummyComponent.triggerEvent(unreliableTransitiveLookupRequest);

			LookupResultEvent lookupResultEvent = null;

			lookupResultEvent = (LookupResultEvent) eventQueue.take();

			assertEquals(lookupResultEvent.getResponsible().getId().toString(),
					"110");

			// Unreliable recursive
			UnreliableLookupRequestEvent unreliableRecursiveLookupRequest = new UnreliableLookupRequestEvent(
					BigInteger.valueOf(109), LookupStrategy.RECURSIVE,
					new StabGetSuccListReqMessage());

			dummyComponent.triggerEvent(unreliableRecursiveLookupRequest);

			lookupResultEvent = (LookupResultEvent) eventQueue.take();

			assertEquals(lookupResultEvent.getResponsible().getId().toString(),
					"110");

			// Reliable transitive
			ReliableLookupRequestEvent reliableTransitiveLookupRequest = new ReliableLookupRequestEvent(
					BigInteger.valueOf(109), LookupStrategy.TRANSITIVE,
					new StabGetSuccListReqMessage());

			dummyComponent.triggerEvent(reliableTransitiveLookupRequest);

			lookupResultEvent = (LookupResultEvent) eventQueue.take();

			assertEquals(lookupResultEvent.getResponsible().getId().toString(),
					"110");

			// Reliable recursive
			ReliableLookupRequestEvent reliableRecursiveLookupRequest = new ReliableLookupRequestEvent(
					BigInteger.valueOf(109), LookupStrategy.RECURSIVE,
					new StabGetSuccListReqMessage());

			dummyComponent.triggerEvent(reliableRecursiveLookupRequest);

			lookupResultEvent = (LookupResultEvent) eventQueue.take();

			assertEquals(lookupResultEvent.getResponsible().getId().toString(),
					"110");*/

		/*} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}
}
