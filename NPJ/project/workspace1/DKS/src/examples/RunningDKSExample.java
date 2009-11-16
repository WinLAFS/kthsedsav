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
//import java.io.InputStreamReader;
//import java.math.BigInteger;
//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.UnknownHostException;
//
//import org.apache.log4j.PropertyConfigurator;
//
//import dks.DKSParameters;
//import dks.addr.DKSRef;
//import dks.boot.DKSPropertyLoader;
//import dks.boot.DKSWebCacheManager;
//
///**
// * The <code>RunningDKSExample</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: RunningDKSExample.java 494 2007-12-14 15:09:00Z roberto $
// */
//public class RunningDKSExample {
//
//	InputStreamReader cin = new InputStreamReader(System.in);
//
//	public RunningDKSExample(String[] args) {
//
//		PropertyConfigurator.configure(System
//				.getProperty("org.apache.log4j.config.file"));
//
//		DKSPropertyLoader propertyLoader = new DKSPropertyLoader();
//
//		DKSParameters dksParameters = (propertyLoader).getDKSParameters();
//
//		if (args.length < 4) {
//			System.err
//					.println("Usage: Test <create|join> <id> <port> <bind_ip>");
//		}
//
//		for (int i = 0; i < args.length; i++) {
//			System.out.println(i + "=" + args[i]);
//		}
//
//		try {
//			boolean create = args[0].equals("create");
//			BigInteger id = new BigInteger(args[1]);
//			int port = Integer.parseInt(args[2]);
//			InetAddress ip = InetAddress.getByName(args[(args.length - 1)]);
//
//			if (create) {
//				System.out.println("First node. Creating a ring...");
//				DKSRef ref1 = new DKSRef(ip, port, id.abs());
//				ExampleDKSModifiedNode node1 = new ExampleDKSModifiedNode(ref1,
//						dksParameters, propertyLoader.getWebcacheAddress());
//				node1.getDksImplementation().create();
//				
//			} else {
//				try {
//					String webCacheAddres = propertyLoader.getWebcacheAddress();
//					
//					DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
//							webCacheAddres);
//
//					DKSRef ref2 = new DKSRef(ip, port, id.abs());
//
//					ExampleDKSModifiedNode node2 = new ExampleDKSModifiedNode(
//							ref2, dksParameters, webCacheAddres);
//
//					String rawDKSRef = dksCacheManager.getFirstDKSRef();
//
//					DKSRef dksRef = null;
//
//					dksRef = new DKSRef(rawDKSRef);
//
//					System.out.println("Joining ring using node " + dksRef
//							+ "...");
//					node2.getDksImplementation().join(dksRef);
//
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (NumberFormatException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//		} catch (UnknownHostException e) {
//			System.out.println(e.getMessage());
//		}
//
//	}
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		/*
//		 * + Follow these steps to run DKS: - Define this JVM properties in your
//		 * launcher: dks.propFile=./dksParam.prop
//		 * org.apache.log4j.config.file=log4j.config - For creating the first
//		 * node of the ring start this program with the following parameters:
//		 * create [id] [port] [IPAddr] - For starting any other node use the
//		 * following parameters: join [id] [port] [IPAddr]
//		 */
//
//		if (args.length < 1) {
//
//			System.out
//					.println("usage: examples.RunningDKSExample [id] [port] [IPAddr]");
//
//		} else {
//
//			new RunningDKSExample(args);
//		}
//	}
//
//}
