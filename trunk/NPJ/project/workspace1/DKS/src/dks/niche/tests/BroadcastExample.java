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
//import java.util.ArrayList;
//import java.util.concurrent.SynchronousQueue;
//
//import dks.dht.DHTComponent.getFlavor;
//import dks.dht.DHTComponent.putFlavor;
//import dks.niche.Niche;
//import dks.utils.IntervalsList;
//import dks.utils.SimpleIntervalException;
//import dks.utils.SimpleInterval.Bounds;
//
///**
// * The <code>InterfacePresentation</code> class
// *
// * @author Joel
// * @version $Id: BroaccastExample.java 294 2006-05-05 17:14:14Z joel $
// */
//public class BroadcastExample {
//
//	public final static int syncBroadcastTestPort = 24001;
//	public final static int asyncBroadcastTestPort = 24002;
//	
//	public final static int pauseBeforeAction = 10000;
//	
//	
//	static int port;
//	static BigInteger id;
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		System.out.println("Starting, " + args[0] + " " + args[1] + " " + args[2]);
//		
//		int mode = args[0].equals("create") ? Niche.BOOT : Niche.JOINING;
//		port = Integer.parseInt(args[2]);
//		
//		
//		Niche myNiche = new Niche(args[1], mode, port);
//		
//		if(mode == Niche.BOOT) {
//			myNiche.boot();
//		}
//		else {
//			myNiche.join();
//		}
//		
//		id = myNiche.getId();
//		BroadcastExample myself = new BroadcastExample();
//		
//	
//		
//		
//		/*
//		 * Register broadcast-related handlers
//		 */
//		
//		myNiche.setDefaultBroadcastReceiver(myself, "receive");
//		myNiche.registerBroadcastReceiver(myself, "receiveOne");
//		myNiche.registerBroadcastReceiver(myself, "receiveTwo");
//		
//		//If you are a passive node, this is all you do.
//		
//				
//		/*
//		 * Test Broadcast functionality
//		 * 
//		 */
//		if(port == syncBroadcastTestPort) {
//			
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//			e.printStackTrace();
//			}
//			
//			IntervalsList list = null;
//			
//			try {
//				list = new IntervalsList(new BigInteger("100000"),	new BigInteger("250000"), Bounds.CLOSED_CLOSED, myNiche.getN() );
//				list.addToSelf(new IntervalsList(new BigInteger("750000"),	new BigInteger("900000"), Bounds.CLOSED_CLOSED, myNiche.getN()));
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			
//			
//			System.out.println("BroadcastExample says: starting synchronous broadcast, to nodes 100000->250000 and 750000->900000");
//			ArrayList result = (ArrayList)myNiche.broadcast("'This is a synchronous broadcast message from node "+id+" to the default broadcast receiver'", list , false, true, null, null, null);
//			
//			System.out.println("The result of the broadcast is:");
//			for(Object o: result) {
//				System.out.println(o);
//			}
//		}
//		
//		if(port == asyncBroadcastTestPort) {
//			
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//			e.printStackTrace();
//			}
//			
//			IntervalsList list = null;
//			
//			
//			try {
//				list = new IntervalsList(new BigInteger("0"),	new BigInteger("500000"), Bounds.CLOSED_CLOSED, myNiche.getN() );
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			
//			System.out.println("BroadcastExample says: starting asynchronous broadcast one, to nodes 0->500000");
//			myNiche.asynchronousBroadcast("'This is a asynchronous broadcast message from node "+id+" to the broadcast receiver 'receiveOne''", list, false, true, null, myself.getClass().getName(), "receiveOne", myself, "handleResultsDefault");
//			
//			
//			System.out.println("BroadcastExample says: starting asynchronous broadcast two, to nodes 500000->999999");
//			try {
//				list = new IntervalsList(new BigInteger("500000"), new BigInteger("0"), Bounds.CLOSED_CLOSED, myNiche.getN() );
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			myNiche.asynchronousBroadcast("'This is a asynchronous broadcast message from node "+id+" to the broadcast receiver 'receiveTwo''", list, true, true, null, myself.getClass().getName(), "receiveTwo", myself, "handleResultsOne");
//		}
//	}
//	
//	
//	/*
//	 * Handlers for receiving broadcasts
//	 */
//	public String receive(Object s) {
//		String t = "This is a message given in reply to the following broadcast message: " + s + "\nThis message comes from node with id: "+id+", handler method 'receive'";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveOne(Object s) {
//		String t = "This is a message given in reply to the following broadcast message: " + s + "\nThis message comes from node with id: "+id+", handler method 'receiveOne'";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveTwo(Object s) {
//		String t = "This is a message given in return to the following broadcast message: " + s + "\nThis message comes from node with id: "+id+", handler method 'receiveTwo'";
//		System.out.println(t);
//		return t;		
//	}
//
//	/*
//	 * Handlers for the results
//	 */
//	public void handleResultsDefault(ArrayList results) {
//		System.out.println("DefaultBroadcastResultHandler says, the result of the broadcast is:");
//		for(Object o: results) {
//			System.out.println(o);
//		}
//	}
//	
//	public void handleResultsOne(ArrayList results) {
//		for(Object o: results) {
//			System.out.println("BroadcastResultHandlerOne says, the result of the broadcast is:");
//			System.out.println(o);
//		}
//	}
//
//	
//}
