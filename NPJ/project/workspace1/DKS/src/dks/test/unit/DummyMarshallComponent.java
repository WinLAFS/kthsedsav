///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package dks.test.unit;
//
//import dks.arch.ComponentRegistry;
//import dks.arch.Scheduler;
//import dks.comm.mina.events.DeliverMessageEvent;
//import dks.marshall.events.MessageMarshalledEvent;
//
///**
// * The <code>DummyMarshallComponent</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: DummyMarshallComponent.java 496 2007-12-20 15:39:02Z roberto $
// */
//public class DummyMarshallComponent extends DummyComponent {
//
//
//	public DummyMarshallComponent(Scheduler scheduler,
//			ComponentRegistry registry, DKSUnitTestCase testCase) {
//		super(scheduler, registry, testCase);
//		registrerForEvents();
//	}
//
//	private void registrerForEvents() {
//
//		register(MessageMarshalledEvent.class, "handleMEvent");
//		register(DeliverMessageEvent.class, "handleEvent");
//
//	}
//
//	public void handleMEvent(MessageMarshalledEvent event) {
//		testCase.eventQueue.add(event);
//	}
//
//	public void handleEvent(DeliverMessageEvent event) {
//		System.out.println("DeliverMessageEvent received");
//		testCase.eventQueue.add(event);
//	}
//
//}
