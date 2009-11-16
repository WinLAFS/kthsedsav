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
 * The <code>GodsDksRingAndJoinTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: GodsDksRingAndJoinTest.java 294 2006-05-05 17:14:14Z ozair $
 */
public class GodsRingBootAndJoinTest {
	/**
	 * 
	 */
	public GodsRingBootAndJoinTest(String[] args) {

	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {

		if (args.length < 1) {

			System.out
					.println("usage: dks.ring.tests.GodsRingBootAndJoinTest [id] [port] [IPAddr]");

		} else {

			PropertyConfigurator.configure(System
					.getProperty("org.apache.log4j.config.file"));

			DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

			DKSParameters dksParameters = propertyLoader.getDKSParameters();

			if (args.length < 4) {
				System.err
						.println("Usage: Test <create|join> <id> <port> <bind_ip>");
			}

			for (int i = 0; i < args.length; i++) {
				System.out.println(i + "=" + args[i]);
			}

			try {
				boolean create = args[0].equals("create");
				BigInteger id = new BigInteger(args[1]);
				int port = Integer.parseInt(args[2]);
				InetAddress ip = InetAddress.getByName(args[(args.length - 1)]);

				if (create) {
					System.out.println("First node. Creating a ring...");
					DKSRef ref1 = new DKSRef(ip, port, id.abs());
					DKSNode node1 = new DKSNode(ref1, dksParameters,
							propertyLoader.getWebcacheAddress());
					node1.getDksImplementation().create();
				} else {
					try {
						String webCacheAddres = propertyLoader
								.getWebcacheAddress();
						DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
								webCacheAddres);

						DKSRef ref2 = new DKSRef(ip, port, id.abs());

						DKSNode node2 = new DKSNode(ref2, dksParameters,
								webCacheAddres);

						String rawDKSRef = dksCacheManager.getFirstDKSRef();
						DKSRef dksRef = null;

						// Thread.currentThread().sleep(Long.parseLong(args[3]));

						dksRef = new DKSRef(rawDKSRef);

						System.out.println("Joining ring using node " + dksRef
								+ "...");
						node2.getDksImplementation().join(dksRef);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
			}

		}

	}
}
