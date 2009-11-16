///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//
//package examples;
//
//import java.math.BigInteger;
//import java.util.ArrayList;
//
//import dks.dht.DHTComponent.*;
//import dks.niche.Niche;
//import dks.utils.IntervalsList;
//import dks.utils.SimpleIntervalException;
//import dks.utils.SimpleInterval.Bounds;
//
///**
// * The <code>NicheInterfaceExample</code> class
// *
// * @author Joel
// * @version $Id: NicheInterfaceExample.java 294 2006-05-05 17:14:14Z joel $
// */
//public class NicheInterfaceExample {
//
//	public final static int dhtPort = 22001;
//	public final static int sendToIdPort = 23001;
//	
//	public final static int syncBroadcastPort = 24001;
//	public final static int asyncBroadcastPort = 24002;
//	
//	public final static int initialDelay = 5000;
//	
//	public final static int pauseBetweenPutAndGet = 5000;
//	
//	static final String TEST_KEY_ONE = "The first key";
//	static final String TEST_VALUE_ONE = "The first value";
//	static final String TEST_KEY_TWO = "The second key";
//	static final String TEST_VALUE_TWO = "The second value";
//	
//	static final int ITEMS = 100;
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
//		
//		port = Integer.parseInt(args[2]);
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
//		
//		NicheInterfaceExample myself = new NicheInterfaceExample();
//		
//		/*
//		 * Register dht-related handlers
//		 */
//		myNiche.setDefaultDHTPutAckReceiver(myself, "handlePutAck");
//		myNiche.setDefaultDHTGetReceiver(myself, "handleGetResponse");
//		
//		/*
//		 * Register one-to-one sending-related handlers
//		 */
//		
//		myNiche.setDefaultSendAckReceiver(myself, "handleSendAck");
//		myNiche.setDefaultSendReceiver(myself, "receiveMethodDefault");
//		myNiche.registerReceiver(myself, "receiveMethodOne");
//		myNiche.registerReceiver(myself, "receiveMethodTwo");
//		
//		
//		/*
//		 * Register broadcast-related handlers
//		 */
//		myNiche.setDefaultBroadcastResultReceiver(myself, "handleResultsDefault");
//		myNiche.setDefaultBroadcastReceiver(myself, "receive");
//		myNiche.registerBroadcastReceiver(myself, "receiveOne");
//		myNiche.registerBroadcastReceiver(myself, "receiveTwo");
//		
//		/*
//		 * Test DHT functionality
//		 */
//		if(port == dhtPort) {
//		
//			try {
//				Thread.sleep(initialDelay);
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					myNiche.asynchronousPut(TEST_KEY_ONE+i, i+TEST_VALUE_ONE+i, putFlavor.PUT_ADD);
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					myNiche.put(TEST_KEY_TWO+i, i+TEST_VALUE_TWO+i, putFlavor.PUT_ADD);
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//
//			
//			try {
//				Thread.sleep(pauseBetweenPutAndGet);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			
//			for(int i = 0; i<ITEMS; i++) {
//				System.out.print("InterfaceTest says: I asked for "+TEST_KEY_ONE+i+ " and got ");
//				System.out.println(myNiche.get(TEST_KEY_ONE+i, getFlavor.GET_ANY));
//				
//			}
//			
//			
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					myNiche.asynchronousGet(TEST_KEY_TWO+i, getFlavor.GET_ANY, myself, "handleGetResponse");
//					Thread.sleep(5);
//				}
//				catch (InterruptedException e) {
//			
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		
//		/*
//		 * One-to-one sending functionality
//		 * 
//		 */
//		
//		if(port == sendToIdPort) {
//			
//			try {
//				Thread.sleep(initialDelay);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			//Sending to myself
//			System.out.println("Illustrating one-to-one sending");
//			myNiche.send(id, "This is a message to myself", null, null);
//			
////			Sending to someone elses default handler
//			BigInteger rec = myNiche.getRandomId();
//			Object reply = myNiche.send(rec, "This is a sync. message to " + rec + " to the default handler", null, null);
//
//			System.out.println("The received reply was: "+reply);
//						
//			String total = "After sending to four different ids, this are the replies:\n";
//			
//			rec = new BigInteger("1");
//			total += (String)myNiche.send(rec, "This is a message to " + rec + " to the method one", myself.getClass().getName(), "receiveMethodOne");
//			
//			rec = new BigInteger("123456");
//			total += "\n"+myNiche.send(rec, "This is a message to " + rec + " to the method two", myself.getClass().getName(), "receiveMethodTwo");
//
//			rec = new BigInteger("250000");
//			total += "\n"+myNiche.send(rec, "This is a message to " + rec + " to the method one", myself.getClass().getName(), "receiveMethodOne");
//			
//			rec = new BigInteger("999999");
//			total += "\n"+myNiche.send(rec, "This is a message to " + rec + " to the method two", myself.getClass().getName(), "receiveMethodTwo");
//			
//			System.out.println(total);
//			
//		}
//		
//		/*
//		 * Broadcast functionality
//		 * 
//		 */
//		if(port == syncBroadcastPort) {
//			
//			try {
//				Thread.sleep(initialDelay);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			IntervalsList list = null;
//			
//			try {
//				list = new IntervalsList(new BigInteger("100000"),	new BigInteger("250000"), Bounds.CLOSED_CLOSED, new BigInteger("1000000") );
//				list.addToSelf(new IntervalsList(new BigInteger("750000"),	new BigInteger("900000"), Bounds.CLOSED_CLOSED, new BigInteger("1000000")));
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			
//			int myTimeout = 2500;
//			
//			System.out.println("InterfaceTest says: starting broadcast three, to the default handler in nodes 100000->250000 and 750000->900000");
//			ArrayList result = (ArrayList)myNiche.broadcast("Hello DKS",list , false, true, myTimeout, null, null);
//			
//			for(Object o: result) {
//				System.out.println("Result of last broadcast: "+o);
//			}
//
//			//
//		}
//		
//		if(port == asyncBroadcastPort) {
//			
//			try {
//				Thread.sleep(initialDelay);
//			} 
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			IntervalsList list = null;
//			
//			
//			try {
//				list = new IntervalsList(new BigInteger("0"),	new BigInteger("500000"), Bounds.CLOSED_CLOSED, new BigInteger("1000000") );
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			
//			System.out.println("NicheInterfaceExample says: starting broadcast one, to nodes 0->500000");
//			myNiche.asynchronousBroadcast("Hello Method One", list, false, true, null, myself.getClass().getName(), "receiveOne", myself, "handleResultsDefault");
//			
//			
//			System.out.println("NicheInterfaceExample says: starting broadcast two, to nodes 500000->999999");
//			try {
//				list = new IntervalsList(new BigInteger("500000"), new BigInteger("0"), Bounds.CLOSED_CLOSED, new BigInteger("1000000") );
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			myNiche.asynchronousBroadcast("Hello Method Two", list, true, true, null, myself.getClass().getName(), "receiveTwo", myself, "handleResultsOne");
//			
//			
//		}
//
//	}
//	
//	/*
//	 * DHT-testing helper methods
//	 */
//	public void handlePutAck(Object o) {
//		//Silently ignore		
//	}
//	
//	public void handleSendAck(Object o) {
//		System.out.println("Ack on one-to-one sending");
//	}
//	
//	public void handleGetResponse(Object o) {
//		System.out.println("InterfaceTest says: GET at node "+id+". result is "+ o);
//	}
//	
//	/*
//	 * One-to-one-testing helper methods
//	 */
//	
//	public String receiveMethodDefault(Object o) {
//		String r = "Node "+id+" received the following msg in method default: "+o;
//		System.out.println(r);
//		return r;
//	}
//
//	public String receiveMethodOne(Object o) {
//		String r = "Node "+id+" received the following msg in method one: "+o;
//		System.out.println(r);
//		return r;
//
//	}
//	
//	public String receiveMethodTwo(Object o) {
//		String r = "Node "+id+" received the following msg in method two: "+o;
//		System.out.println(r);
//		return r;
//
//	}
//	
//	/*
//	 * Broadcast-testing helper methods
//	 */
//	public String receive(Object s) {
//		String t = "Broadcast return message from node, id: "+id+"\tport: "+port+" handler method default";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveOne(Object s) {
//		String t = "Broadcast return message in answer to " + s + " from node, id: "+id+"\tport: "+port+" handler method one";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveTwo(Object s) {
//		String t = "Broadcast return message in answer to " + s + " from node, id: "+id+"\tport: "+port+" handler method two";
//		System.out.println(t);
//		return t;		
//	}
//
//	public void handleResultsDefault(ArrayList results) {
//		for(Object o: results) {
//			System.out.println("DefaultHandler says, result of broadcast: "+o);
//		}
//	}
//	
//	public void handleResultsOne(ArrayList results) {
//		for(Object o: results) {
//			System.out.println("HandlerOne says, result of broadcast: "+o);
//		}
//	}
//
//	
//}
