///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package dks.niche.tests;
//
//import dks.DKSParameters;
//import dks.addr.DKSRef;
//import dks.arch.ComponentRegistry;
//import dks.arch.Scheduler;
////import dks.bcast.TestComponent;
//import dks.niche.NicheOSSupport;
//import dks.niche.components.NicheManagementContainerComponent;
//import dks.niche.ids.ResourceId;
//
///**
// * The <code>NicheTestNode</code> class
// *
// * @author Joel
// * @version $Id: NicheTestNode.java 294 2006-05-05 17:14:14Z joel $
// */
//public class NicheTestNode {
//
//	
//	protected ComponentRegistry registry;
//
//	protected Scheduler scheduler;
//
//
//	
//	public NicheTestNode(JadeTest niche, DKSRef ref, DKSParameters dksParameters, String webcacheAddress, boolean receiver) { //(NicheOSSupport niche, DKSRef myRef, DKSParameters dksParameters) {
//
//		/* Starting the ComponentRegistry */
//		registry = ComponentRegistry.init(dksParameters); //new ComponentRegistry(""); //ComponentRegistry.init(dksParameters);
//
//		/* Creating Scheduler */
//		scheduler = new Scheduler(registry);
//
//		/* Creating the TimerComponent */
//		//new TimerComponent(registry, scheduler);
//
//		// DirectByteBufferPool bufferPool = new
//		// DirectByteBufferPool(scheduler,registry);
//		//
//		// /* Creating and Registering the Communicator */
//		// @SuppressWarnings("unused")
//		// CommunicationComponent communicatorComponent = CommunicationComponent
//		// .newInstance(scheduler, registry, myRef.getIp(), myRef
//		// .getPort(), myRef, bufferPool);
//		//new TestComponent(scheduler, registry); 
//
//		System.out.println("NicheTestNode says: Am I started, am I?");
//		
//		
//	}
//
//
//
//	/**
//	 * @return
//	 */
//	public ResourceId oneShotDiscoverResource() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
