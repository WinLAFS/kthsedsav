/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.niche;

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
 * @version $Id: TestRing.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class LookupTest extends TestCase{

	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//	new TestRing().testRing();

//	}
	public final static int HISTORY_LENGTH = 20;
	ArrayList<DKSRef> ring ;
//	ArrayList<BigInteger> ringIds;
//	ArrayList<BigInteger> ringIdsOld;
	ArrayList<BigInteger>[] ringIds; 
	ArrayList<Properties> ringProperties;
	
	HashMap<String, String> averageLookup;
	HashMap<String, String> averageTransfer;
	HashMap<String, Boolean> testDone;
	boolean done;
	
	private final static String CONTEXT = "lookup";
	int errorCount = 0;

	static final String webcachePath = System.getProperty("dks.webcache") instanceof String ?
			System.getProperty("dks.webcache")
			:"http://kalle.sics.se:7811/lookupWebcache/dksRefs";

	public static void main(String[] args) {

		junit.textui.TestRunner.run(LookupTest.class);


	}

	public void testLookup() {

		ring = new ArrayList<DKSRef>();
		ringIds = new ArrayList[HISTORY_LENGTH];
		averageLookup = new HashMap<String, String>();
		averageTransfer = new HashMap<String, String>();
		testDone = new HashMap<String, Boolean>();
		
		//for (int i = 0; i < ringIds.length; i++) {		}
	
		ringProperties = new ArrayList<Properties>();
		done = false;
		while(!done) {
			init();
			start();
			
			System.out.println("Lookup errors found = " + errorCount);
			System.out.println("================================================");
			System.out.println();
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

	}

	public void start() {

		DKSRef ref;
		for (int i = 0; i < ring.size(); i++) {
			
			ref = ring.get(i);
			Properties properties = new Properties();

			URL url = null;

			try {

				
				url = new URL("http:/" +ref.getIp() + ":" + (ref.getPort()+1) + "/"
						+ CONTEXT);

//				System.out.println("URL: " + url + "  " + ref.getIp());

				// Get an input stream for reading
				InputStream in = url.openStream();

				// System.out.println(bufIn);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
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

				properties.load(new ByteArrayInputStream(sb.toString().getBytes()));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
//				System.out.println("Connection reset: " + url);
				ringIds[0].remove(ref.getId());
				ring.remove(ref);
				i--;
			}

			ringProperties.add(properties);
			averageLookup.put(ref.getId().toString(),  (String)(properties.get("lookup")) );
			averageTransfer.put(ref.getId().toString(),  (String)(properties.get("transfer") ) );
			testDone.put(ref.getId().toString(), properties.get("done") == null ? false:true);
		}
		
		System.out.println("Number of nodes found = " + ring.size());
		System.out.println(ringIds[0]);
		
		float count = 0;
		float total = 0;
		String value;
		
		//print lookup average
		System.out.println("Average Lookup:");
		for (BigInteger id : ringIds[0]) {
			
			value = averageLookup.get(id.toString());
			if(value != null) {
				total += Float.parseFloat(value);
				count++;
			}
			System.out.print(value + "  ");
		}
		System.out.println(" --> " + (total / count));
		
		//print transfer average
		count = 0;
		total = 0;
		System.out.println("Average Transfer:");
		for (BigInteger id : ringIds[0]) {
			
			value = averageTransfer.get(id.toString());
			if(value != null) {
				float tmp = Float.parseFloat(value) /2;
				total += tmp;
				count++;
				System.out.print(tmp + "  ");
			} else {
				System.out.print(value + "  ");
			}
		}
		System.out.println(" --> " + (total / count));
		
//		print done
		done = true;
		System.out.println("Test Finished:");
		for (BigInteger id : ringIds[0]) {
			
			done = done & testDone.get(id.toString());
			System.out.print("" + testDone.get(id.toString()) + "  ");
			
			
		}
		System.out.println();
		
		if(done) {
			System.out.println("Test Finished.");
		}
		
		
		
		
		
		
		String Q ,A;
		
		for (Properties properties : ringProperties) {
			
			Set<Entry<Object, Object>> set = properties.entrySet();
			
			for (Entry<Object, Object> entry : set) {
				Q = (String)entry.getKey();
				A = (String)entry.getValue();
				if(!(Q.equals("lookup") || Q.equals("transfer") || Q.equals("done")) ){
					
					if(!A.startsWith("P")) {
	//					assertEquals(check(Q,A), true);
						if(!check(Q,A, 0)) {
							errorCount++;
						}
					} else {
//						System.out.println("WARNING: " + Q +" not answered (yet)! " + A);//+ ref);
					}
				}
			}		
		}
	}



	private boolean check(String q, String a, int history) {
		if(ringIds[history] == null) {
			System.out.println("ERROR: "+history+": Out of history");
			return false;
		}
		
		int index = a.indexOf(':');
		
		BigInteger Q = new BigInteger(q);
		BigInteger A = new BigInteger(a.substring(0,index));
		
		int i = Collections.binarySearch(ringIds[history], A);
		
		if(i > 0) {
			if(ringIds[history].get(i).compareTo(Q) >= 0   &&  ringIds[history].get(i-1).compareTo(Q) < 0) {
				return true;
			}
		} else {
			if(ringIds[history].get(0).compareTo(Q) >= 0   ||  ringIds[history].get(ringIds[history].size()-1).compareTo(Q) < 0) {
				return true;
			}
		}
		
		if(history<HISTORY_LENGTH-1) {
//			System.out.println("WARNING: "+history+": Q = " + q + " and A=" + a);
			return check( q, a, ++history);
		} 
		System.out.println("ERROR: "+history+": Q = " + q + " and A=" + a);
		return false;
		
	}


	void init() {
		
		// rotate
		for (int i = ringIds.length-1; i > 0; i--) {
			ringIds[i] = ringIds[i-1];
		}
		ringIds[0] = new ArrayList<BigInteger>();

		
		ring.clear();
		ringProperties.clear();

		URL url = null;
		URLConnection con = null;
		BufferedReader in;
		try {

			System.out.println("Trying to connect to webcache at " + webcachePath);
			url = new URL(webcachePath);

			con = url.openConnection();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				DKSRef ref =new DKSRef(line); 
				if(!ring.contains(ref)) {
					ring.add(ref);
					ringIds[0].add(ref.getId());
				}
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		System.out.println("Number of nodes found = " + ring.size());
		Collections.sort(ringIds[0]);
		
//		System.out.println(ringIds);
		
		
		}

}
