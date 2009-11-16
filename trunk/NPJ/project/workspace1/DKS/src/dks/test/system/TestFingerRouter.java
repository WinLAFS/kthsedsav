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
 * The <code>FingerRouterTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestFingerRouter.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class TestFingerRouter extends DKSSystemTestCase {
	
	
//	private String classToLaunch="dks.ring.tests.RingBootAndJoinTest"; 
	
	/**
	 * 
	 */
	public TestFingerRouter(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestFingerRouter.class);
	}
	

	public void testLookup() {
//		
//		System.out.println("Starting Tests");
//		
//		
//		startNode("dks.test.system.BootAndJoinTest", "create", "1000", 12345, "127.0.0.1");
//		
//		startNode("dks.test.system.BootAndJoinTest", "join", "1001", 12346, "127.0.0.1");
//		
//		
//		startNode("dks.test.system.BootAndJoinTest", "join","1002", 12347, "127.0.0.1");
//		
//		
//		startNode("dks.test.system.BootAndJoinTestst", "join", "1003", 12348, "127.0.0.1");
		
		
		
		/*startNode(12, 12333,"dks.ring.tests.RingBootAndJoinTest");

		sleep(2000);

		// Starting third d
		startNode(13, 12335,"dks.ring.tests.RingBootAndJoinTest");

		sleep(2000);

		startNode(1001, 12334,"dks.ring.tests.RingBootAndJoinTest ");

		sleep(10000);
*/
		// UnreliableLookupRequestEvent event = new
		// UnreliableLookupRequestEvent(
		// BigInteger.valueOf(11), LookupStrategy.TRANSITIVE);
		// trigger(event);

		
//		List<Parameter> params=new LinkedList<Parameter>();
//		
//		Parameter par1=new Parameter("doYou","work");
//		
//		params.add(par1);
//		
		System.out.println("before");
		
		asserT("5000", "successor", "dks://127.0.0.1:15462/190000",
				"ring");
	
//		asserT("1000","Iwork", "fine", "testme",params);
//		asserT("1001","Iwork", "fine", "testme",params);
//		
//		System.out.println("First two tests fine");
//		asserT("1002","Iwork", "fine", "testme",params);
//		asserT("1003","Iwork", "fine", "testme",params);
//		
//		System.out.println("after");
		
		//asserT("RouterUsed", "dks.router.FingerRouterComponent", "router");

		

		// try {
		//
		// LookupResultEvent lookupResultEvent = (LookupResultEvent) eventQueue
		// .take();
		// assertEquals("12", lookupResultEvent.getResponsible().getId());
		//
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// stopExternalNode(12);
		// stopExternalNode(1001);

		//sleep(600000);

	}

//	public void createDummyComponent(TestCase testcase) {
//		// CREATING DUMMY COMPONENT
//		dummyComponent = new DummyRoutingComponent(node1.getScheduler(), node1
//				.getComponentRegistry(), this);
//	}

}
