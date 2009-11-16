///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package examples;
//
//import dks.DKSParameters;
//import dks.addr.DKSRef;
//import dks.boot.DKSNode;
//
///**
// * The <code>ExampleDKSModifiedNode</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: ExampleDKSModifiedNode.java 494 2007-12-14 15:09:00Z roberto $
// */
//public class ExampleDKSModifiedNode extends DKSNode {
//
//	/**
//	 * Your modified node constructor
//	 * 
//	 * @param myRef
//	 *            The DKSRef of the peer
//	 * @param dksParameters
//	 *            The dksParameters read from the property file
//	 * @param webcacheAddress
//	 *            The address of the webcache
//	 */
//	public ExampleDKSModifiedNode(DKSRef myRef, DKSParameters dksParameters,
//			String webcacheAddress) {
//
//		super(myRef, dksParameters, webcacheAddress);
//
//		/*
//		 * Here you can add all the components that you implemented. The only
//		 * fact of extending the Class Component gives you the capability to
//		 * register for events and then receive them.
//		 */
//		@SuppressWarnings("unused")
//		SimpleComponent simpleComponent = new SimpleComponent(scheduler,
//				registry);
//		
//		@SuppressWarnings("unused")
//		PublishingComponent testComponent= new PublishingComponent(scheduler,registry);
//
//	}
//
//}
