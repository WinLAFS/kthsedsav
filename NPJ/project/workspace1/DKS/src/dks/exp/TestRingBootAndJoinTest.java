/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;

/**
 * The <code>GodsDksRingAndJoinTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: GodsDksRingAndJoinTest.java 294 2006-05-05 17:14:14Z ozair $
 */
public class TestRingBootAndJoinTest {

	public final static int MESSAGES = 1;

	public final static int LOOKUPS = 2;

	public final static int EVENTS = 3;

	public final static int BROADCAST = 4;

	public final static int WHICH_TEST = LOOKUPS;

	public TestRingBootAndJoinTest(String[] args) {
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
			boolean join2 = args[0].equals("join2");
			BigInteger id = new BigInteger(args[1]);
			int port = Integer.parseInt(args[2]);
			InetAddress ip = InetAddress.getByName(args[(args.length - 1)]);

			if (create) {
				System.out.println("First node. Creating a ring...");
				DKSRef ref1 = new DKSRef(ip, port, id.abs());
				final DKSExpNode node1 = new DKSExpNode(ref1, dksParameters,
						propertyLoader.getWebcacheAddress());
				node1.getDksImplementation().create();

				BufferedReader cin = new BufferedReader(new InputStreamReader(
						System.in));

				 System.out
						.println("Pausing - press enter to start measurement");
				
				try {
					cin.read();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				switch (WHICH_TEST) {

				case MESSAGES:
					boolean started = false;
					int cnt = 1000;
					int sleep = 1000;
					int size = 1000;
					while (true) {
						if (started == false) {
							System.out
									.println("Pausing  - press enter to start measurement");
							try {
								String[] line = cin.readLine().split(":");
								cnt = Integer.parseInt(line[0]);
								sleep = Integer.parseInt(line[1]);
								size = Integer.parseInt(line[2]);
								started = !started;
							} catch (Exception e) {
								cnt = 1000;
								sleep = 1000;
								size = 1000;
							}
							node1.getMessageThroughputComponent().setCnt(cnt);
							node1.getMessageThroughputComponent().setSleep(
									sleep);
							node1.getMessageThroughputComponent().setSize(size);
							(new Thread() {
								public void run() {
									node1.getMessageThroughputComponent()
											.start();
								}
							}).start();
						} else {
							System.out
									.println("Pausing  - press enter to stop measurement");
							try {
								cin.readLine();
								started = !started;
							} catch (IOException e) {
								e.printStackTrace();
							}
							System.out.println("Stopping.");
							node1.getMessageThroughputComponent().stop();
						}
					}

				case LOOKUPS:

					node1.getLookupTcomp().start();

					break;

				case EVENTS:

					node1.getSenderTcomp().start();

					break;

				case BROADCAST:

					node1.getBroadcastTcomp().start();

					break;
				default:
					break;
				}

			} else {
				try {
					String webCacheAddres = propertyLoader.getWebcacheAddress();
					DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
							webCacheAddres);

					DKSRef ref2 = new DKSRef(ip, port, id.abs());

					DKSExpNode node2 = new DKSExpNode(ref2, dksParameters,
							webCacheAddres);

					String rawDKSRef = dksCacheManager.getFirstDKSRef();
					DKSRef dksRef = null;

					// Thread.currentThread().sleep(Long.parseLong(args[3]));

					dksRef = new DKSRef(rawDKSRef);

					System.out.println("Joining ring using node " + dksRef
							+ "...");
					node2.getDksImplementation().join(dksRef);

					if (join2) {

						node2.getMessageThroughputComponent().setIsSink(join2);

					}

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

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {

		if (args.length < 1) {

			System.out
					.println("usage: dks.ring.tests.GodsRingBootAndJoinTest [id] [port] [IPAddr]");

		} else {

			new TestRingBootAndJoinTest(args);
		}

	}
}
