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
// * @version $Id: InterfacePresentation.java 294 2006-05-05 17:14:14Z joel $
// */
//public class InterfacePresentation {
//
//	public final static int dhtTestPort = 22001;
//	public final static int sendToIdTestPort = 23001;
//	
//	public final static int syncBroadcastTestPort = 24001;
//	public final static int asyncBroadcastTestPort = 24002;
//	
//	public final static int pauseBeforeAction = 20000;
//	
//	public final static int pauseBetweenPutAndGet = 50000;
//	
//	static final String TEST_KEY_ONE = "The first key";
//	static final String TEST_VALUE_ONE = "The first value";
//	static final String TEST_KEY_TWO = "The second key";
//	static final String TEST_VALUE_TWO = "The second value";
//	
//	static final int ITEMS = 400;
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
//		//NicheOSSupport myDKSPrime = new NicheOSSupport(port, mode);
//		
//		if(mode == Niche.BOOT) {
//			myNiche.boot();
//			//myDKSPrime.boot();
//		}
//		else {
//			myNiche.join();
//			//myDKSPrime.join();
//		}
//		
//		id = myNiche.getId();
//		InterfacePresentation myself = new InterfacePresentation();
//		
//		/*
//		 * Register dht-related handlers
//		 */
//		myNiche.setDefaultDHTPutAckReceiver(myself, "handlePutAck");
//		myNiche.setDefaultDHTGetReceiver(myself, "handleGetResponse");
//		
//		
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
//		//If you are a passive node, this is all you do.
//		
//		/*
//		 * Test DHT functionality
//		 * 
//		 */
//		if(port == dhtTestPort) {
//		
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			/*
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					//myCommunicator.put("katt"+i, i+"plattak"+i, putFlavor.PUT_ADD);
//					myCommunicator.asynchronousPut(TEST_KEY_ONE+i, i+TEST_VALUE_ONE+i, putFlavor.PUT_ADD, myself, "handlePutAck");
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				}
//			}
//			*/
//			System.out.println("InterfacePresentation says: Time to send put "+ITEMS+" items in the DHT");
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					//myCommunicator.put("katt"+i, i+"plattak"+i, putFlavor.PUT_ADD);
//					myNiche.put(TEST_KEY_TWO+i, i+TEST_VALUE_TWO+i, putFlavor.PUT_ADD);
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				}
//			}
//
//			
//			try {
//				Thread.sleep(pauseBetweenPutAndGet);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			
//			/*
//			for(int i = 0; i<ITEMS; i++) {
//				System.out.print("InterfacePresentation says: I asked for "+TEST_KEY_ONE+i+ " and got ");
//				System.out.println(myCommunicator.get(TEST_KEY_ONE+i, getFlavor.GET_ANY));
//				
//			}
//			*/
//			System.out.println("InterfacePresentation says: Time to send gut "+ITEMS+" items from the DHT");
//			for(int i = 0; i<ITEMS; i++) {
//					myNiche.asynchronousGet(TEST_KEY_TWO+i, getFlavor.GET_ANY, myself, "handleGetResponse");
//				try {
//					Thread.sleep(5);
//				}
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		
//		/*
//		 * Test One-to-one sending functionality
//		 * 
//		 */
//		
//		if(port == sendToIdTestPort) {
//			
//			BigInteger rec;
//			String reply;
//						
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			//Start by sending to myself
//			System.out.println("InterfacePresentation says: Time to send one-to-one messages");
//			System.out.println("InterfacePresentation says: Sending synchronously to id: " + id+ ", that is myself");
//			
//			myNiche.send(id, "This is a synchronous message without reply from id " + id +" to myself, to the default handler", null, null);
//			
//			rec = new BigInteger("1");
//			System.out.println("InterfacePresentation says: Sending synchronously to id: " + rec);
//			myNiche.send(rec, "This is a synchronous message without reply from id " + id +" to id "+rec+", to the default handler ", null, null);
//			
//			rec = new BigInteger("250000");
//			System.out.println("InterfacePresentation says: Sending synchronously to id: " + rec);
//			reply = (String)myNiche.send(rec, "This is a synchronous message with reply from id " + id +" to id "+rec+", to the handler 'receiveMethodOne'", myself.getClass().getName(), "receiveMethodOne");
//
//			System.out.println("InterfacePresentation says: This will not be printed until the synchronous operations above all have finished.\nThe result of the last operation is:\n"+reply);
//			
//			rec = new BigInteger("999999");
//			System.out.println("InterfacePresentation says: Sending asynchronously to id: " + rec);
//			
//			myNiche.asynchronousSend(rec, "This is an asynchronous message with reply from id " + id +" to id "+rec+", to the handler 'receiveMethodTwo'", myself.getClass().getName(), "receiveMethodTwo", myself, "receiveAsynchronousRepliesMethod");
//			
//			System.out.println("InterfacePresentation says: This will printed be as soon as the asynchronous operation above is issued");
//			
////			Sending to someone elses default handler
//			//BigInteger rec = myCommunicator.getRandomId();
//			//Object reply = myCommunicator.send(rec, "This is a sync. message to " + rec + " to the default handler", null, null);
//			//System.out.println(reply);
//			
//
//			
//			
//			
//			
//			
//			//reply = (String)myCommunicator.send(rec, "This is a message to " + rec + " to the method one", myself.getClass().getName(), "receiveMethodOne");
//			
//			//System.out.println("InterfacePresentation says: id: " + rec+ " gave the following reply: "+reply);
//			/*
//			rec = new BigInteger("123456");
//			total = "\n"+myCommunicator.send(rec, "This is a message to " + rec + " to the method two", myself.getClass().getName(), "receiveMethodTwo");
//	System.out.println(total);
//
//			rec = new BigInteger("250000");
//			total += "\n"+myCommunicator.send(rec, "This is a message to " + rec + " to the method one", myself.getClass().getName(), "receiveMethodOne");
//			
//			rec = new BigInteger("999999");
//			total += "\n"+myCommunicator.send(rec, "This is a message to " + rec + " to the method two", myself.getClass().getName(), "receiveMethodTwo");
//			*/
//		}
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
//			// TODO Auto-generated catch block
//			e.printStackTrace();
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
//			
//			System.out.println("InterfacePresentation says: starting broadcast three, to nodes 100000->250000 and 750000->900000");
//			ArrayList result = (ArrayList)myNiche.broadcast("Hello DKS",list , false, true, null, null, null);
//			
//			for(Object o: result) {
//				System.out.println("Result of last broadcast: "+o);
//			}
//
//			//
//		}
//		
//		if(port == asyncBroadcastTestPort) {
//			
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
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
//			System.out.println("InterfacePresentation says: starting broadcast one, to nodes 0->500000");
//			myNiche.asynchronousBroadcast("Hello Method One", list, false, true, null, myself.getClass().getName(), "receiveOne", myself, "handleResultsDefault");
//			
//			
//			System.out.println("InterfacePresentation says: starting broadcast two, to nodes 500000->999999");
//			try {
//				list = new IntervalsList(new BigInteger("500000"), new BigInteger("0"), Bounds.CLOSED_CLOSED, new BigInteger("1000000") );
//					
//			} catch (SimpleIntervalException e) {
//				e.printStackTrace();
//			}
//			myNiche.asynchronousBroadcast("Hello Method Two", list, true, true, null, myself.getClass().getName(), "receiveTwo", myself, "handleResultsOne");
//			
//			
//			//
//		}
//
//	}
//	
//	/*
//	 * DHT-testing helper methods
//	 */
//	public void handlePutAck(Object o) {
//		
//	}
//	
//	public void handleSendAck(Object o) {
//		System.out.println("Ack on one-to-one sending");
//	}
//	
//	public void handleGetResponse(Object o) {
//		System.out.println("InterfacePresentation says: GET at node "+id+". result is "+ o);
//	}
//	
//	/*
//	 * One-to-one-testing helper methods
//	 */
//	
//	public String receiveMethodDefault(Object o) {
//		String r = "Node "+id+" received the following msg in method default: "+o+ "\nIn the case the sender requested a reply, this text-string will be sent back to the initiator\n";
//		System.out.println(r);
//		return r;
//	}
//
//	public String receiveMethodOne(Object o) {
//		String r = "Node "+id+" received the following msg in method one: "+o+ "\nIn the case the sender requested a reply, this text-string will be sent back to the initiator\n";
//		System.out.println(r);
//		return r;
//
//	}
//	
//	public String receiveMethodTwo(Object o) {
//		String r = "Node "+id+" received the following msg in method two: "+o+ "\nIn the case the sender requested a reply, this text-string will be sent back to the initiator\n";
//		System.out.println(r);
//		return r;
//
//	}
//	
//	public void receiveAsynchronousRepliesMethod(Object o) {
//		
//		System.out.println("receiveAsynchronousRepliesMethod says: The result of the operation is:\n"+o+"\n");
//	}
//	/*
//	 * Broadcast-testing helper methods
//	 */
//	public String receive(Object s) {
//		String t = "This is a broadcast return message from node, id: "+id+"\tport: "+port+" handler method default";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveOne(Object s) {
//		String t = "This is a broadcast return message in answer to " + s + " from node, id: "+id+"\tport: "+port+" handler method one";
//		System.out.println(t);
//		return t;		
//	}
//	
//	public String receiveTwo(Object s) {
//		String t = "This is a broadcast return message in answer to " + s + " from node, id: "+id+"\tport: "+port+" handler method two";
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
