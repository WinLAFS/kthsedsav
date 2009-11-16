/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.tests;

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
import java.util.Random;
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
public class GenerateIds {

	/**
	 * @param args
	 */
	// public static void main(String[] args) {
	// new TestRing().testRing();
	// }
	/**
	 * 
	 */
	
	public static int NUMBER_OF_IDS = System
			.getProperty("dks.test.numberOfIds") instanceof String ? Integer
			.parseInt(System.getProperty("dks.test.numberOfIds"))
			: 512;
	static final long N = System
			.getProperty("dks.test.N") instanceof String ? Long
			.parseLong(System.getProperty("dks.test.N"))
			: (long)Math.pow(4, 21);

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

	long largestDelay = 0;

	public static void main(String[] args) {

		int SEED = 1;
		Random myRandom;
		
		if(0 < SEED) {// = new Random();
			myRandom = new Random(SEED);
		} else {
			myRandom = new Random();
		}
		ArrayList<Double> idSeeds = new ArrayList<Double>(NUMBER_OF_IDS);
		for(int i = 0; i < NUMBER_OF_IDS; i++) {
			idSeeds.add(0d);
		}
		long[] ids = new long[NUMBER_OF_IDS];
		int generatedIds = 0;
		double temp;
		while(generatedIds < NUMBER_OF_IDS) {
			temp = myRandom.nextDouble();
			//System.out.println(idSeeds.s);
			if(idSeeds.get((int)(temp*NUMBER_OF_IDS)) == 0) {
				idSeeds.set((int)(temp*NUMBER_OF_IDS), temp);
				generatedIds++;
				//System.out.println(generatedIds + " " + (int)(temp*NUMBER_OF_IDS) + " " + temp);
			}
		}
		int nextSeedIndex = 0;
		int nextTargetIndex = 0;
		while(0 < idSeeds.size()) {
			ids[nextTargetIndex++] = (long)(idSeeds.remove(nextSeedIndex) * N);
			if(idSeeds.size() == 0) break;
			ids[nextTargetIndex++] = (long)(idSeeds.remove( (nextSeedIndex + (idSeeds.size() / 2)) % idSeeds.size()) * N);
			if(idSeeds.size() == 0) break;
			ids[nextTargetIndex++] = (long)(idSeeds.remove( (nextSeedIndex + (idSeeds.size() / 4)) % idSeeds.size()) * N);
			if(idSeeds.size() == 0) break;
			ids[nextTargetIndex++] = (long)(idSeeds.remove( (nextSeedIndex + (3*idSeeds.size() / 4)) % idSeeds.size()) * N);
			if(idSeeds.size() == 0) break;
			nextSeedIndex = myRandom.nextInt(idSeeds.size());
		}
		for(int i = 0; i < NUMBER_OF_IDS; i++) {
			System.out.println(ids[i]+"=1000001");
		}
		
		

	}

}
