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
//import dks.niche.Niche;
//
///**
// * The <code>InterfacePresentation</code> class
// *
// * @author Joel
// * @version $Id: NameBasedCommunicationExample.java 294 2006-05-05 17:14:14Z joel $
// */
//public class NameBasedCommunicationExample {
//
//	public final static int sendToIdTestPort = 23001;
//	
//	public final static int pauseBeforeAction = 20000;
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
//		} else {
//			myNiche.join();
//		}
//		
//		id = myNiche.getId();
//		NameBasedCommunicationExample myself = new NameBasedCommunicationExample();
//		
//				
//		/*
//		 * Register one-to-one sending-related handlers
//		 */
//		
//		myNiche.setDefaultSendReceiver(myself, "receiveMethodDefault");
//		myNiche.registerReceiver(myself, "receiveMethodOne");
//		myNiche.registerReceiver(myself, "receiveMethodTwo");
//		
//		
//		
//		//If you are a passive node, this is all you do.
//		
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
//		}
//	}
//	
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
//	
//	
//}
