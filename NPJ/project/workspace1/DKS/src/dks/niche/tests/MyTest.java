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
//import java.math.BigInteger;
//
//import dks.dht.DHTComponent.getFlavor;
//import dks.dht.DHTComponent.putFlavor;
//import dks.dht.DHTComponent.removeFlavor;
//import dks.dht.tests.LargeObject;
//import dks.niche.Niche;
//
///**
// * The <code>MyTest</code> class
// *
// * @author Joel
// * @version $Id: DHTTest.java 294 2006-05-05 17:14:14Z joel $
// */
//
//
///*
// * ...oooO..............
// * ...(...).....Oooo....
// * ....\.(......(...)...
// * .....\_)......)./....
// * .............(_/.....
// * ... I WAS ...........
// * .......... HERE .....
// */
//
//
//public class MyTest {
//
//    public final static int testPort = 22000;
//    public final static int leave1Port = 22100;
//    public final static int leave2Port = 21000;
//    public final static int leave3Port = 0;
//
//    static int port;
//    static BigInteger id;
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//
//	System.out.println("Starting, " + args[0] + " " + args[1] + " " + args[2]);
//
//	int mode = args[0].equals("create") ? Niche.BOOT : Niche.JOINING;
//	//int id = Integer.parseInt(args[1]);
//	port = Integer.parseInt(args[2]);
//
//	Niche myDKSInterface = new Niche(args[1], port, mode);
//	//NicheOSSupport myDKSPrime = new NicheOSSupport(port, mode);
//
//	if(mode == Niche.BOOT) {
//	    myDKSInterface.boot();
//	    //myDKSPrime.boot();
//	}
//	else {
//	    myDKSInterface.join();
//	    //myDKSPrime.join();
//	}
//	id = myDKSInterface.getId();
//	DHTTest myself = new DHTTest();
//
//	myDKSInterface.setDefaultDHTPutAckReceiver(myself, "handlePutAck");
//	myDKSInterface.setDefaultDHTGetReceiver(myself, "handleGetResponse");
//
//
//	myDKSInterface.setDefaultBroadcastResultReceiver(myself, "receive");
//	myDKSInterface.registerBroadcastReceiver(myself, "receive");
//
//	if(port == testPort) {
//
//
//
//	    //myDKSInterface.put("katt", "plattak", putFlavor.PUT_ADD);
//	    //myDKSInterface.asynchronousPut("katt", "plattak", putFlavor.PUT_ADD, "handlePutAck");
//
//	    for(int i = 0; i<200; i++) {
//		try {
//		    myDKSInterface.asynchronousPut("katt"+i, new LargeObject(i+"pl-ttak"+i) , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(40);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
////	    myDKSInterface.put("katt", "sirap", putFlavor.PUT_ADD);
////	    for(int i = 0; i<20; i++) {
////	    try {
////	    Thread.sleep(50);
////	    } catch (InterruptedException e) {
////	    // TODO Auto-generated catch block
////	    e.printStackTrace();
////	    }
////	    System.out.println("(done) processing: " + (20-i) );
////	    }
//
//
////	    myDKSInterface.asynchronousGet("katt", getFlavor.GET_ANY, "handleGetResponse");
//
//	    try {
//		Thread.sleep(3000);
//	    } catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//
//	    for(int i = 0; i<200; i++) {
//		myDKSInterface.asynchronousGet("katt"+i, getFlavor.GET_ANY, myself, "handleGetResponse");
//		try {
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//
//	    }
//
//	    for(int i = 0; i<200; i++) {
//		try {
//		    myDKSInterface.asynchronousPut("DOG"+i, new LargeObject(i+"Niche"+i) , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<200; i++) {
//		try {
//		    myDKSInterface.asynchronousPut("DOG"+i, new LargeObject(i+"Jade"+i) , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<200; i++) {
//		try {
//		    myDKSInterface.asynchronousPut("DOG"+i, new LargeObject(i+"DKS"+i) , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<200; i++) {
//		myDKSInterface.asynchronousGet("DOG"+i, getFlavor.GET_ANY, myself, "handleGetResponse");
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//
//	    }
//
//	    System.out.println("\t\tDHTTest: Starting to remove some keys!");
//	    for(int i = 0; i<100; i+=10) {
//		Object o = myDKSInterface.remove("DOG"+i, removeFlavor.REMOVE_FIRST);
//		System.out.println("\t\tDHTTest: syncRemove FIRST at node "+id+". result is "+ o);
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 100; i<200; i+=10) {
//		Object o = myDKSInterface.remove("DOG"+i, removeFlavor.REMOVE_ALL);
//		System.out.println("\t\tDHTTest: syncRemove ALL at node "+id+". result is "+ o);
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<200; i+=10) {
//		myDKSInterface.asynchronousGet("DOG"+i, getFlavor.GET_ALL, myself, "handleGetResponseAfterRemove");
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//
//	    }
//
//
////	    IntervalsList list = null;
////	    try {
////	    list = new IntervalsList(new BigInteger("0"),	new BigInteger("500000"), Bounds.CLOSED_CLOSED, new BigInteger("1000000") );
//
////	    } catch (SimpleIntervalException e) {
////	    e.printStackTrace();
////	    }
//
//	    //myDKSInterface.setBroadcastInitiator(this);
//
//	    //ArrayList result = (ArrayList)myDKSInterface.broadcast("Hello DKS",list , true, true);
//	    //for(Object o: result) {				System.out.println("Result: "+o);			}
//
//	    //public Object broadcast(Object message, IntervalsList receivers, boolean reliable, boolean aggregate) 
//	} else if (port == leave1Port) {
//	    try {
//		Thread.sleep(20000);
//	    } catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//	    myDKSInterface.leave();
//	} else if (port == leave2Port) {
//	    try {
//		Thread.sleep(30000);
//	    } catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//	    myDKSInterface.leave();
//	} else if (port == leave3Port) {
//	    try {
//		Thread.sleep(40000);
//	    } catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	    }
//	    myDKSInterface.leave();
//	}
//
//    }
//    public void handlePutAck(Object o) {
//
//    }
//
//    public void handleGetResponse(Object o) {
//	System.out.println("\t\tDHTTest: GET at node "+id+". result is "+ o);
//    }
//
//    public void handleGetResponseAfterRemove(Object o) {
//	System.out.println("\t\tDHTTest: GET after remove at node "+id+". result is "+ o);	    
//    }
//
//    public String receive(Object s) {
//	System.out.println("msg received at node with port "+port);
//	return "Node, id: "+id+"\tport: "+port;
//    }
//
//}
