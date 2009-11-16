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
 * The <code>TestRingMaintainerBasic</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TestRingMaintainerBasicJunit.java 155 2007-01-25 11:00:42Z
 *          Roberto $
 */
public class TestRingMaintainerBasicJunit extends DKSSystemTestCase {

	public TestRingMaintainerBasicJunit() {
	}

	/**
	 * 
	 */
	public TestRingMaintainerBasicJunit(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestRingMaintainerBasicJunit.class);
	}

	public void testThreeNodesRings() {
		
		
		/*startExternalNode(12,12000);
		
		startExternalNode(1001,12002);
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		 /* asserT("Predecessor", "12","ring",null);
		  asserT("Successor", "1001","ring",null);*/
//		  asserT("SuccessorList", "1001,12","ring");
		
	
		stopAllNodes();
	}

}
