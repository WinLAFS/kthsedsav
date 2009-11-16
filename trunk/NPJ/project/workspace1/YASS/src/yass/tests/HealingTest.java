/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import dks.addr.DKSRef;

import junit.framework.TestCase;

/**
 * The <code>TestRing</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @author Joel H
 * @version $Id: TestRing.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class HealingTest extends TestCase {

	/**
	 * @param args
	 */
	// public static void main(String[] args) {
	// new TestRing().testRing();
	// }
	/**
	 * 
	 */
	public static String HEALING_TEST_NAME = "yass_healing";
	public static String CURRENT_LOCAL_TIME = "currentLocalTime";
	public static String LATEST_LOCAL_STORE_TIME = "latestLocalStoreTime";
	public static String LATEST_ADJUSTED_STORE_TIME = "latestAdjustedStoreTime";

	public static String WEB_CACHE_ADDRESS = System
			.getProperty("dks.properties.webCacheAddress") instanceof String ? System
			.getProperty("dks.properties.webCacheAddress")
			: "http://www.sics.se/~joel/DKSPrime/";

	public static String TEST_FILE_NAME_PREFIX = "test";

	public static int NUMBER_OF_TEST_FILES = System
			.getProperty("yass.test.numberOfTestFiles") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.numberOfTestFiles"))
			: 50;
	static final int DEFAULT_REPLICATION_DEGREE = System
			.getProperty("yass.test.defaultReplicationDegree") instanceof String ? Integer
			.parseInt(System.getProperty("yass.test.defaultReplicationDegree"))
			: 2;

	public static int FAIL_SLEEP_DELAY = 30000;

	public final static int HISTORY_LENGTH = 20;
	ArrayList<DKSRef> ring;
	// ArrayList<BigInteger> ringIds;
	// ArrayList<BigInteger> ringIdsOld;
	// ArrayList<BigInteger>[] ringIds;
	ArrayList<Properties> allProperties;

	// HashMap<String, String> averageLookup;
	// HashMap<String, String> averageTransfer;
	HashMap<String, Boolean> testDone;
	HashMap<String, Integer> properReplicaDegree;

	boolean done;

	private final static String CONTEXT = HEALING_TEST_NAME;
	long largestDelay = 0;

	public static void main(String[] args) {

		junit.textui.TestRunner.run(HealingTest.class);

	}

	public void testHealing() {

		long localStartTime = System.currentTimeMillis();
		System.out.println("The starttime = the killing-time is " + localStartTime);
		ring = new ArrayList<DKSRef>();
		// HashMap<String, Integer> properReplicaDegree;

		// ringIds = new ArrayList[HISTORY_LENGTH];
		testDone = new HashMap<String, Boolean>();

		// for (int i = 0; i < ringIds.length; i++) { }

		allProperties = new ArrayList<Properties>();

		done = false;
		
		boolean firstTime = true;
		
		while (!done) {
			init();
		
			if(firstTime) {
				checkDeltas();
				firstTime = false;
			}
			start();

			System.out.println("Largest restore-time found so far = "
					+ largestDelay + " ms corresponding to " + (largestDelay - localStartTime) + " = " +((largestDelay - localStartTime) / 1000) +" s difference to start time");
			System.out
					.println("================================================");
			System.out.println();
			try {
				Thread.sleep(FAIL_SLEEP_DELAY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Largest final restore-time = " + largestDelay);
		System.out.println("Diff vs local start time = " + (largestDelay - localStartTime) + " ms = " + ((largestDelay - localStartTime) / 1000) + " s");

	}

	
	public void checkDeltas() {

		DKSRef ref;
		
		System.out.println("ringSize is " + ring.size());
		
		for (int i = 0; i < ring.size(); i++) {

			ref = ring.get(i);
			Properties properties = new Properties();

			URL url = null;
			long beforeAsking, afterAsking, meanTime, delta, localRemoteTime;
			String latestLocalStoreTime;
			try {

				beforeAsking = System.currentTimeMillis();
				url = new URL("http:/" + ref.getIp() + ":"
						+ (ref.getPort() + 1) + "/" + CONTEXT);

				// System.out.println("URL: " + url + " " + ref.getIp());

				// Get an input stream for reading
				InputStream in = url.openStream();

				// System.out.println(bufIn);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				StringBuffer sb = new StringBuffer();
				String line = null;
				do {
					line = br.readLine();
					if (line != null)
						if (!line.equalsIgnoreCase("STOP"))
							sb.append(line + "\n");
						else
							break;
					// System.out.println(sb.toString());
				} while (line != null);
				afterAsking = System.currentTimeMillis();
				
				properties.load(new ByteArrayInputStream(sb.toString()
						.getBytes()));
				
				//System.out.println("Fetched properties are: "+ properties.toString());
				
				latestLocalStoreTime = (String) properties
						.get(LATEST_LOCAL_STORE_TIME);
				if (latestLocalStoreTime != null) { // not all nodes host
													// storageComponents!!
					meanTime = (beforeAsking + afterAsking) / 2;
					

					delta = meanTime - Long.parseLong((String) properties
							.get(CURRENT_LOCAL_TIME));
					
					System.out.println("Delta for node " + ref + " is " + delta);
				} 
	

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
				// System.out.println("Connection reset: " + url);
				// ringIds[0].remove(ref.getId());
			}

			// ringProperties.add(properties);
		}

	}

	
	public void start() {

		DKSRef ref;
		properReplicaDegree = new HashMap<String, Integer>(); // start all
																// over, every
																// iteration.
																// fill with new
																// values
		for (int i = 0; i < NUMBER_OF_TEST_FILES; i++) {

			properReplicaDegree.put(TEST_FILE_NAME_PREFIX + i, 0);
		}
		properReplicaDegree.put(HealingTest.CURRENT_LOCAL_TIME, 0); //avoid NULL
		properReplicaDegree.put(HealingTest.LATEST_LOCAL_STORE_TIME, 0); //avoid NULL
		properReplicaDegree.put(HealingTest.LATEST_ADJUSTED_STORE_TIME, 0); //avoid NULL
		
		System.out.println("ringSize is " + ring.size());
		
		for (int i = 0; i < ring.size(); i++) {

			ref = ring.get(i);
			Properties properties = new Properties();

			URL url = null;
			long beforeAsking, afterAsking, meanTime, delta, localRemoteTime;
			String latestLocalStoreTime;
			try {

				beforeAsking = System.currentTimeMillis();
				url = new URL("http:/" + ref.getIp() + ":"
						+ (ref.getPort() + 1) + "/" + CONTEXT);

				// System.out.println("URL: " + url + " " + ref.getIp());

				// Get an input stream for reading
				InputStream in = url.openStream();

				// System.out.println(bufIn);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				StringBuffer sb = new StringBuffer();
				String line = null;
				do {
					line = br.readLine();
					if (line != null)
						if (!line.equalsIgnoreCase("STOP"))
							sb.append(line + "\n");
						else
							break;
					// System.out.println(sb.toString());
				} while (line != null);
				afterAsking = System.currentTimeMillis();
				
				properties.load(new ByteArrayInputStream(sb.toString()
						.getBytes()));
				
				//System.out.println("Fetched properties are: "+ properties.toString());
				
				latestLocalStoreTime = (String) properties
						.get(LATEST_LOCAL_STORE_TIME);
				if (latestLocalStoreTime != null) { // not all nodes host
													// storageComponents!!
					meanTime = (beforeAsking + afterAsking) / 2;
					

					delta = meanTime - Long.parseLong((String) properties
							.get(CURRENT_LOCAL_TIME));
					
					properties.put(LATEST_ADJUSTED_STORE_TIME, Long.parseLong(latestLocalStoreTime) + delta);

					allProperties.add(properties);
				} else {
					System.out.println(ref + " did not have a storageComponent");
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
				// System.out.println("Connection reset: " + url);
				// ringIds[0].remove(ref.getId());
				ring.remove(ref);
				i--;
			}

			// ringProperties.add(properties);
		}

		System.out.println("Number of nodes found = " + ring.size());
		// System.out.println(ringIds[0]);

		String currentFile;
		int seenReplicas;
		long largestTimeSoFar;

		// Loop, loopetie loop loop
		for (Properties properties : allProperties) {
			Object[] storedFiles = properties.keySet().toArray();
			// The above will also give you the 3 time-values, but that doesn't
			// harm
			for (int j = 0; j < storedFiles.length; j++) {
				currentFile = (String) storedFiles[j];
				//System.out.println("which file: "+currentFile);
				seenReplicas = properReplicaDegree.get(currentFile);
				seenReplicas++;
				properReplicaDegree.put(currentFile, seenReplicas);
			}
			largestTimeSoFar = (Long) properties
					.get(LATEST_ADJUSTED_STORE_TIME);
			if (largestDelay < largestTimeSoFar) {
				largestDelay = largestTimeSoFar;
			}
		}

		done = true;
		int missingConter = 0;
		for (int i = 0; i < NUMBER_OF_TEST_FILES; i++) {

			seenReplicas = properReplicaDegree.get(TEST_FILE_NAME_PREFIX + i);
			if (seenReplicas < DEFAULT_REPLICATION_DEGREE) {
				missingConter++;
				System.out.println(missingConter + ": File " + TEST_FILE_NAME_PREFIX + i
						+ " not yet restored. Current number is " + seenReplicas);
				done = false;
			}
		}

	}

	// private boolean check(String q, String a, int history) {
	// if(ringIds[history] == null) {
	// System.out.println("ERROR: "+history+": Out of history");
	// return false;
	// }
	//		
	// int index = a.indexOf(':');
	//		
	// BigInteger Q = new BigInteger(q);
	// BigInteger A = new BigInteger(a.substring(0,index));
	//		
	// int i = Collections.binarySearch(ringIds[history], A);
	//		
	// if(i > 0) {
	// if(ringIds[history].get(i).compareTo(Q) >= 0 &&
	// ringIds[history].get(i-1).compareTo(Q) < 0) {
	// return true;
	// }
	// } else {
	// if(ringIds[history].get(0).compareTo(Q) >= 0 ||
	// ringIds[history].get(ringIds[history].size()-1).compareTo(Q) < 0) {
	// return true;
	// }
	// }
	//		
	// if(history<HISTORY_LENGTH-1) {
	// System.out.println("WARNING: "+history+": Q = " + q + " and A=" + a);
	// return check( q, a, ++history);
	// }
	// System.out.println("ERROR: "+history+": Q = " + q + " and A=" + a);
	// return false;
	//		
	// }

	void init() {

		// rotate
		// for (int i = ringIds.length-1; i > 0; i--) {
		// ringIds[i] = ringIds[i-1];
		// }
		// ringIds[0] = new ArrayList<BigInteger>();

		ring.clear();
		allProperties.clear();

		URL url = null;
		URLConnection con = null;
		BufferedReader in;
		System.out.println("WEB_CACHE_ADDRESS: " + WEB_CACHE_ADDRESS);
		try {
			url = new URL(WEB_CACHE_ADDRESS + "dksRefs");
			con = url.openConnection();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				DKSRef ref = new DKSRef(line);
				if (!ring.contains(ref)) {
					ring.add(ref);
					System.out.println("adding " + ref);
					// ringIds[0].add(ref.getId());
				}
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// System.out.println("Number of nodes found = " + ring.size());
		// Collections.sort(ringIds[0]);

		// System.out.println(ringIds);

	}

}
