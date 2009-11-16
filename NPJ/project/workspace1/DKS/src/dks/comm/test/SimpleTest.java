///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package dks.comm.test;
//
//import java.math.BigInteger;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.UnknownHostException;
//import java.util.List;
//import java.util.Random;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//
//import dks.DKSParameters;
//import dks.addr.DKSRef;
//import dks.boot.DKSNode;
//import dks.boot.DKSPropertyLoader;
//import dks.comm.DirectByteBuffer;
//import dks.comm.MessageInfo;
//import dks.comm.events.CommMakeConnPermanentEvent;
//import dks.comm.events.CommMessageDelivered;
//import dks.comm.events.CommMessageSent;
//import dks.comm.events.CommSendEvent;
//import dks.comm.events.CommStopRetryingEvent;
//import dks.marshall.MarshallComponent;
//import dks.messages.HelloMessage;
//
///**
// * The <code>EarlyTest</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: SimpleTest.java 494 2007-12-14 15:09:00Z roberto $
// */
//public class SimpleTest extends Thread {
//
//	private static Logger log = Logger.getLogger(SimpleTest.class);
//
//	private static int ID_SIZE = 16;
//
//	private static String thread_sel;
//
//	/**
//	 * 
//	 */
//	public SimpleTest() {
//		setName("TestThread");
//	}
//
//	@Override
//	public void run() {
//
//		Random random = new Random();
//
//		PropertyConfigurator.configure(System
//				.getProperty("org.apache.log4j.config.file"));
//
//		if (thread_sel == null) {
//			InetAddress ip;
//			try {
//				ip = InetAddress.getLocalHost();
//
//				// First Peer
//				BigInteger bigInt = new BigInteger(ID_SIZE, random);
//
//				DKSRef ref1 = new DKSRef(ip, 12345, bigInt);
//				
//				DKSParameters dksParameters = (new DKSPropertyLoader())
//				.getDKSParameters();
//				
//				new DKSNode(ref1,dksParameters,"");
//
//				log.debug("ref1=" + ref1);
//
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} else {
//
//			InetAddress ip;
//			try {
//				ip = InetAddress.getLocalHost();
//
//				DKSRef ref1 = new DKSRef(thread_sel);
//				// Second peer
//				BigInteger bigInt2 = new BigInteger(ID_SIZE, random);
//
//				DKSRef ref2 = new DKSRef(ip, 12346, bigInt2);
//				
//				DKSParameters dksParameters = (new DKSPropertyLoader())
//				.getDKSParameters();
//				
//				DKSNode node2 = new DKSNode(ref2,dksParameters,"");
//
//				log.debug("ref2=" + ref2);
//				log.debug("ref1=" + ref1);
//
//				HelloMessage hello = new HelloMessage(ref1);
//
//				MarshallComponent marshaler = node2.getComponentRegistry()
//						.getMarshalerComponent();
//
//				List<DirectByteBuffer> helloMarshaled = hello.marshall(marshaler);
//
//				MessageInfo messageInfo = new MessageInfo(ref2, ref1,
//						CommMessageSent.class, CommMessageDelivered.class, 0);
//				CommSendEvent event = new CommSendEvent(helloMarshaled,
//						messageInfo);
//
//				node2.getScheduler().dispatch(event);
//
//				CommMakeConnPermanentEvent commMakeConnPermanentEvent = new CommMakeConnPermanentEvent(
//						ref1);
//
//				node2.getScheduler().dispatch(commMakeConnPermanentEvent);
//				// node2.getScheduler().dispatch(event);
//				// node2.getScheduler().dispatch(event);
//				
//				CommStopRetryingEvent commStopRetryingEvent = new CommStopRetryingEvent(ref1);
//				node2.getScheduler().dispatch(commStopRetryingEvent);
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//
//		if (args.length != 0) {
//			thread_sel = args[0];
//		}
//		new SimpleTest().start();
//	}
//
//}
