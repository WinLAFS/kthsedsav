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

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;

/**
 * The <code>ConcurrencyTestNode</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ConcurrencyTestNode.java 270 2007-03-30 16:16:05Z Roberto $
 */
public class ConcurrencyTestNode extends DKSNode {

	public TestComponent1 component1;

	public TestComponent2 component2;

	public TestComponent3 component3;

	/**
	 * @param myRef
	 * @param dksParameters
	 * @param webcacheAddress
	 */
	public ConcurrencyTestNode(DKSRef myRef, DKSParameters dksParameters,
			String webcacheAddress) {
		super(myRef, dksParameters, webcacheAddress);

		component1 = new TestComponent1(scheduler, registry);

		component2 = new TestComponent2(scheduler, registry);

		component3 = new TestComponent3(scheduler, registry);

	}
	
	
	
}
