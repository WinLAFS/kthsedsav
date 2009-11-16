/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.tests;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;

/**
 * The <code>RingBootAndJoinTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingBootJoinAndLeaveTest.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class RingBootJoinAndLeaveTest {

//	private static Logger log = Logger.getLogger(RingBootJoinAndLeaveTest.class);

	/**
	 * 
	 */
	public RingBootJoinAndLeaveTest(String[] n) {

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

		DKSParameters dksParameters = (propertyLoader).getDKSParameters();

		if (n == null) {
			InetAddress ip = null;
			try {
				ip = propertyLoader.getIP();
			} catch (UnknownHostException e) {
				// TODO Handle the exception
			}
			int port = propertyLoader.getPort();

			// byte[] hash = AddressHashing.hash(ip, port);

			BigInteger bigInt2 = new BigInteger("1233");

			DKSRef ref2 = new DKSRef(ip, port, bigInt2.abs());

			DKSNode node1 = new DKSNode(ref2, dksParameters, propertyLoader
					.getWebcacheAddress());

			node1.getDksImplementation().create();
			
		} else {
			if(n[0].equalsIgnoreCase("10")){
				InetAddress ip = null;
				try {
					ip = propertyLoader.getIP();
				} catch (UnknownHostException e) {
					// TODO Handle the exception
				}
				// int port = propertyLoader.getPort();

				// byte[] hash = AddressHashing.hash(ip, port);

				String webCacheAddres = propertyLoader.getWebcacheAddress();

				DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
						webCacheAddres);

				BigInteger bigInt2 = new BigInteger(n[0]);

				DKSRef ref2 = new DKSRef(ip, Integer.parseInt(n[1]), bigInt2.abs());

				DKSNode node1 = new DKSNode(ref2, dksParameters, webCacheAddres);

				String rawDKSRef = dksCacheManager.getFirstDKSRef();
				DKSRef dksRef = null;

				try {
					dksRef = new DKSRef(rawDKSRef);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				node1.getDksImplementation().join(dksRef);
				
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				node1.getDksImplementation().leave();
				
			}else{
				InetAddress ip = null;
				try {
					ip = propertyLoader.getIP();
				} catch (UnknownHostException e) {
					// TODO Handle the exception
				}
				// int port = propertyLoader.getPort();

				// byte[] hash = AddressHashing.hash(ip, port);

				String webCacheAddres = propertyLoader.getWebcacheAddress();

				DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
						webCacheAddres);

				BigInteger bigInt2 = new BigInteger(n[0]);

				DKSRef ref2 = new DKSRef(ip, Integer.parseInt(n[1]), bigInt2.abs());

				DKSNode node1 = new DKSNode(ref2, dksParameters, webCacheAddres);

				String rawDKSRef = dksCacheManager.getFirstDKSRef();
				DKSRef dksRef = null;

				try {
					dksRef = new DKSRef(rawDKSRef);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				node1.getDksImplementation().join(dksRef);
				
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				node1.getDksImplementation().leave();
				
			}
			

		}

		/*
		 * The property "dks.propFile" must be set for the loader to function
		 */

	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		if (args.length == 0) {
			new RingBootJoinAndLeaveTest(null);
		} else {
			if (args.length == 2) {
				new RingBootJoinAndLeaveTest(args);
			} 
			else {

//				String[] param = new String[2];
//
//				new RingBootAndJoinTest(null);
//
//				Thread.currentThread().sleep(2000);
//
//				param[0] = "40";
//				param[1] = "12348";
//				new RingBootAndJoinTest(param);

//				Thread.currentThread().sleep(1000);
//
//				param[0] = "11";
//				param[1] = "12342";
//				new RingBootAndJoinTest(param);
//
//				Thread.currentThread().sleep(1000);
//
//				param[0] = "14";
//				param[1] = "12343";
//				new RingBootAndJoinTest(param);
//
//				Thread.currentThread().sleep(1000);
//
//				param[0] = "5";
//				param[1] = "12344";
//				new RingBootAndJoinTest(param);
			}

		}
	}

}
