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
//import dks.niche.Niche;
//
///**
// * The <code>DHTTest</code> class
// *
// * @author Ahmad Al-Shishtawy
// * @version $Id: DHTTest.java 294 2006-05-05 17:14:14Z alshishtawy $
// */
//public class DHTTest {
//    public final static int putPort = 22100;
//    public final static int get1Port = 22400;
//    public final static int remPort = 22500;
//    public final static int get2Port = 22600;
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
//	String strID = args[1];
//	port = Integer.parseInt(args[2]);
//
//	Niche myNicheInterface = new Niche(strID, mode, port);
//
//	if(mode == Niche.BOOT) {
//	    myNicheInterface.boot();
//	}
//	else {
//	    myNicheInterface.join();
//	}
//	id = myNicheInterface.getId();
//	DHTTest myself = new DHTTest();
//
//
//	try {
//	    Thread.sleep(100);
//	} catch (InterruptedException e) {
//	    e.printStackTrace();
//	}
//	
//
//	if(port == putPort) {
//	    for(int i = 0; i<10; i++) {
//		boolean res = myNicheInterface.put("key"+i, "KTH_"+i, putFlavor.PUT_ADD);
//		System.out.println("Synchronous Put at node "+id+". Ack = "+ res);
//		try {
//		    Thread.sleep(40);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<10; i++) {
//		try {
//		    myNicheInterface.asynchronousPut("aKey"+i, "SICS_"+i , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<10; i++) {
//		try {
//		    myNicheInterface.asynchronousPut("aKey"+i, "Stockholm_"+i , putFlavor.PUT_ADD, myself, "handlePutAck");
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//
//	} else if (port == get1Port || port == get2Port) {
//
//
//	    for(int i = 0; i<10; i++) {
//		Object o = myNicheInterface.get("key"+i, getFlavor.GET_FIRST);
//		System.out.println("Synchronous Get at node "+id+". result is "+ o);		
//		try {
//		    Thread.sleep(20);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//
//	    for(int i = 0; i<10; i++) {
//		myNicheInterface.asynchronousGet("aKey"+i, getFlavor.GET_ANY, myself, "handleGetResponse");
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//	} else if(port == remPort) {
//
//	    for(int i = 0; i<10; i+=2) {
//		Object o = myNicheInterface.remove("key"+i, removeFlavor.REMOVE_FIRST);
//		System.out.println("Synchronous Remove at node "+id+". result is "+ o);
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//	    for(int i = 0; i<10; i+=2) {
//		//Object o = myNicheInterface.remove("aKey"+i, removeFlavor.REMOVE_ALL);
//		myNicheInterface.asynchronousRemove("aKey"+i, removeFlavor.REMOVE_ALL, myself, "handleRemoveAck");
//		//System.out.println("Synchronous Remove at node "+id+". result is "+ o);
//		try {
//		    Thread.sleep(10);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//	}
//    }
//
//    public void handlePutAck(Object o) {
//    	System.out.println("Asynchronous Put at node "+id+". Ack = "+ o);
//        }
//    
//    public void handleRemoveAck(Object o) {
//        	System.out.println("Asynchronous Remove at node "+id+". Result = "+ o);
//        }
//
//    public void handleGetResponse(Object o) {
//	System.out.println("Asynchronous Get at node "+id+". Result = "+ o);
//    }
//}
