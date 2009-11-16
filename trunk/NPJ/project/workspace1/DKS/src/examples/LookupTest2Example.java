/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.boot.DKSNode;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.niche.hiddenEvents.SendToIdAckEvent;
import dks.niche.messages.SendToIdMessage;
import dks.niche.messages.SendToIdResponseMessage;
import dks.ring.RingState;
import dks.router.NewFingerRouterComponent;
import dks.router.Router.LookupStrategy;
import dks.test.niche.LookupTestServlet;
import dks.test.niche.NicheServlet;
import examples.messages.LookupTestMessage;

/**

 * @author Ahmad Al-Shishtawy
 * @version $Id: PseudoReliableIntervalBroadcastDKSExample.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class LookupTest2Example extends CommunicatingComponent {

	/*#%*/ private static Logger log = Logger.getLogger(LookupTest2Example.class);
	
	static int FIRST_PORT = 20000;
	static int PORT_DELTA = 10;
	
	static int PER_NODE_DELAY = System.getProperty("dks.test.lookupTestNodeDelay") instanceof String ?
			Integer.parseInt(System.getProperty("dks.test.lookupTestNodeDelay")) :
				20*1000;
	
	static final int TEST_SIZE = 100;
	static final int TEST_DELAY = System.getProperty("dks.test.lookupTestDelay") instanceof String ?
			Integer.parseInt(System.getProperty("dks.test.lookupTestDelay")) :
				20*1000;
	
	static final Object DATA_TO_SEND = null;
	
	static int TEST_INITIAL_DELAY  = System.getProperty("dks.test.initialLookupTestDelay") instanceof String ?
			Integer.parseInt(System.getProperty("dks.test.initialLookupTestDelay")) :
				5*60*1000;

	//The ID of the node that will do the lookups
	static int TEST_NODE_ID  = System.getProperty("dks.test.nodeid") != null ?
			Integer.parseInt(System.getProperty("dks.test.nodeid")) :
				-1;
	
	//The IDs of nodes to lookup
	static String TEST_LOOKUP_IDs  = System.getProperty("dks.test.lookupids");

//	static final int TEST_INITIAL_BOOT_DELAY  = System.getProperty("dks.test.initialBootLookupTestDelay") instanceof String ?
//					60*Integer.parseInt(System.getProperty("dks.test.initialBootLookupTestDelay")) :
//						5*60*1000;

	//	static final Object DATA_TO_SEND = new byte[512*1024];
	
	static DKSNode node;
	static DKSParameters dksParameters;
	static DKSRef myRef;
	static long messageId = 0;
	static int respMessageCounter = 0;
	static Object mutex = new Object();
	
	static float lookupTime=0;
	static float lookupCount=0;

	
	static float transferTime=0;
	static float transferCount=0;

	Random random;
	long littleN;
	Semaphore semaphore;
	ArrayList<LookupTestMessage> msgs;
	
	// global time measurements
	long globalLookupStart;
	long globalLookupEnd;
	long globalSendStart;
	long globalSendEnd;
	
//	HashMap<String, String> pendingLookups;
	static Properties lookups  = new Properties();
	
	static HashMap<String, TestStats> statistics = new HashMap<String, TestStats>();
	int issuedLookups = 0;
	
	/**
	 * @param scheduler
	 * @param registry
	 */
	public LookupTest2Example(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);
		registerForEvents();
		registerConsumers();
		random = new Random();
		littleN = Long.parseLong(""+dksParameters.N);
		semaphore = new Semaphore(0);
		msgs = new ArrayList<LookupTestMessage>();
		
//		pendingLookups = new HashMap<String, String>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("================ Lookup Test ================");
		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

		dksParameters = (propertyLoader).getDKSParameters();

		if (args.length < 5) {
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
			InetAddress ip = InetAddress.getByName(args[3]);

			LookupTestServlet lookupTestServlet = new LookupTestServlet(lookups);
			
			ArrayList<NicheServlet> servlets = new ArrayList<NicheServlet>();
			servlets.add(lookupTestServlet);
			
			if (create) {
				System.out.println("First node. Creating a ring...");
				myRef = new DKSRef(ip, port, id.abs());
				// node = new IntervalBroadcastDKSModifiedNode(myRef,
				//		dksParameters, propertyLoader.getWebcacheAddress());
				node = new DKSNode(myRef, dksParameters, propertyLoader.getWebcacheAddress(), servlets);

				node.getDksImplementation().create();

				int lastNodeNumber = Integer.parseInt(args[4]);
				int lastNodePort = FIRST_PORT + lastNodeNumber*PORT_DELTA;

				int myDelayDelta = ( (lastNodePort - port) / PORT_DELTA) * PER_NODE_DELAY;
				
				TEST_INITIAL_DELAY += myDelayDelta;
				
				System.out.println("Creating address based on\n"
						+ "ip: "
						+ ip
						+ "\nport: "
						+ port
						+ "\nid: "
						+ id.abs()
						+ "\n\nMy initial delay delta: "
						+ myDelayDelta
						+ "\nresulting initial delay: "
						+ TEST_INITIAL_DELAY 
				);


			} else {
				try {
					String webCacheAddres = propertyLoader.getWebcacheAddress();
					
					System.out.println("Trying to join using webCacheAddress: " + webCacheAddres);
					
					DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
							webCacheAddres);

					int lastNodeNumber = Integer.parseInt(args[4]);
					int lastNodePort = FIRST_PORT + lastNodeNumber*PORT_DELTA;
					
					int myDelayDelta = ( (lastNodePort - port) / PORT_DELTA) * PER_NODE_DELAY;
					
					TEST_INITIAL_DELAY += myDelayDelta;
					
					System.out.println("Creating address based on\n"
							+ "ip: "
							+ ip
							+ "\nport: "
							+ port
							+ "\nid: "
							+ id.abs()
							+ "\n\nI believe the number of nodes is "
							+ lastNodeNumber
							+ "\nTherefore the last node port is "
							+ lastNodePort
							+ "\n\nMy initial delay delta: "
							+ myDelayDelta
							+ "\nresulting initial delay: "
							+ TEST_INITIAL_DELAY 
					);
					
					myRef = new DKSRef(ip, port, id.abs());

					node = new DKSNode(
							myRef, dksParameters, webCacheAddres, servlets);

					String rawDKSRef = dksCacheManager.getFirstDKSRef();

					DKSRef dksRef = null;

					dksRef = new DKSRef(rawDKSRef);

					System.out.println("Joining ring using node " + dksRef
							+ "...");
					
					node.getDksImplementation().join(dksRef);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}

		LookupTest2Example app = new LookupTest2Example(node.getScheduler(), node.getComponentRegistry());

	
		try {
			Thread.sleep(TEST_INITIAL_DELAY);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Tesr node ID: " + TEST_NODE_ID);
		
		if(TEST_NODE_ID < 0 || TEST_NODE_ID == myRef.getId().intValue()) {
			System.out.println("I'm the one:) starting the lookup test...");
			app.lookup();
		}

	}
	
	
	/**
	 * The test starts here
	 */
	private void lookup() {
		HashSet<BigInteger> set = new HashSet<BigInteger>();
		
		
		if(TEST_LOOKUP_IDs != null){
			// use a fixed set of IDs
			String[] tmpIDs = TEST_LOOKUP_IDs.split(",");
			for(String idString : tmpIDs) {
				set.add(new BigInteger(idString));
			}
		} else {
			//generate unique random IDs
			while(set.size() < TEST_SIZE) {
				String idString = ""+(long)(littleN * random.nextDouble());
				set.add(new BigInteger(idString));
			}
		}
		
		////////////////////////////////////////////////////////////////////////////
		//////////////////////// Generate Lookups //////////////////////////////////
		////////////////////////////////////////////////////////////////////////////

		globalLookupStart = System.currentTimeMillis();

		boolean done = false;
		int lookupCounter = 0;
		while( !done) {
			for (BigInteger id : set) {
				
				synchronized (lookups) {
	//				pendingLookups.put(id.toString(), ""+issuedLookups);
	//				lookups.put(""+issuedLookups, registry.getRingMaintainerComponent().getRingState().successor.getId() + "#"+id.toString());
					
					lookups.put(id.toString(), "P:"+myRef.getId());
				}
				issuedLookups++;
				synchronized (statistics) {
					statistics.put(id.toString(), new TestStats(id.toString(),System.currentTimeMillis()));
					// this line is inside the sync to ensure correct time measurments 
					triggerReliableLookupRequest(id, LookupStrategy.TRANSITIVE, null , SendToIdAckEvent.class, "handleLookupResponse");
				}
				
				String logMsg = "Issued a request for id " + id.toString();
				/*#%*/ log.info(logMsg);
				System.out.println(logMsg);
				
				//Wait for the lookup result
				try {
					semaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//check if we are done
				lookupCounter++;
				if(lookupCounter == TEST_SIZE){
					done=true;
					break;
				}
				
			}
		}

		globalLookupEnd = System.currentTimeMillis();

		System.out.println("Average Lookup: " + ((double)(globalLookupEnd - globalLookupStart)/(double)TEST_SIZE));

		////////////////////////////////////////////////////////////////////////////
		//////////////////////////// Send Messages /////////////////////////////////
		////////////////////////////////////////////////////////////////////////////

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		globalSendStart = System.currentTimeMillis();
		
		// send messages
		
		for(LookupTestMessage msg : msgs) {
		    msg.setSendTime(System.currentTimeMillis());
			send(msg, myRef, msg.getDest());
			
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		globalSendEnd = System.currentTimeMillis();
		
		System.out.println("Average Lookup: " + ((double)(globalLookupEnd - globalLookupStart)/(double)TEST_SIZE));
		System.out.println("Average Send: " + ((double)(globalSendEnd - globalSendStart)/(double)(2*TEST_SIZE)));
		
	}


	public void handleLookupResponse(SendToIdAckEvent e) {
		DKSRef node = e.getResponsible();
		BigInteger question =  e.getLookedUpId();
		
		TestStats testStats = null;
		long time;
		
		//signals the start of the next lookup
		semaphore.release();
		
		synchronized (statistics) {
			testStats = statistics.get(question.toString());
			testStats.setResponsible(node.getId().toString());
			time = System.currentTimeMillis()-testStats.getLookupTimeStart();
			testStats.setLookupTime(time);
		}
		
				
		lookupTime += time;
		lookupCount++;
		
		String logMsg = "handleLookupResponse: "+question + " generated " +node.getId() + " using time " + time;
		/*#%*/ log.info(logMsg);
		System.out.println(logMsg);
		synchronized (lookups) {
//			String no = pendingLookups.remove(question.toString());
//			String tmp = lookups.getProperty(no);
//			lookups.put(no, tmp+":"+node.getId());
			lookups.put("lookup", Float.toString(lookupTime/lookupCount));
			lookups.put(question.toString(), node.getId().toString()+":"+myRef.getId());
		}
		
		
		//int serial = Integer.parseInt()
		
		
		
		
		LookupTestMessage msg = new LookupTestMessage(question.toString(), myRef.getId().toString()+":"+(messageId++), DATA_TO_SEND ,0);
		msg.setDest(node);
		msgs.add(msg);
		
		// we'll send them all later
		// send(msg, myRef, node);
		
		
	}
	
	
	public void lookupTestMessageHandler(DeliverMessageEvent e) {
		
		
		
		LookupTestMessage msg = (LookupTestMessage)e.getMessage();
		TestStats testStats = null;
		if (msg.isPing()) { //then I'm the receiver and should response
			msg.setPing(false);
			msg.setReceiveTime(System.currentTimeMillis());
			send(msg, myRef, msg.getSource());
			
		} else {  // I'm the sender and I got a response for my sent message :)
			
			long time;
			//signals the start of the next send msg
			semaphore.release();
			synchronized (statistics) {
				testStats = statistics.get(msg.getLookupId());
				time = System.currentTimeMillis() - msg.getSendTime();
				testStats.setTransferTime(time);
			}
			
			transferTime += time;
			transferCount++;
			
			synchronized (lookups) {
				lookups.put("transfer", Float.toString(transferTime / transferCount));
			}
			respMessageCounter++;
			
			String logMsg = "Got a resp " + respMessageCounter + " of " + TEST_SIZE + " in time " + time;
			/*#%*/ log.info(logMsg);
			System.err.println(logMsg);
			if (respMessageCounter == TEST_SIZE) {
				synchronized (lookups) {
					lookups.put("done", myRef.getId().toString());
				}
				System.out.println("Printing Statistics...");
				System.out.println(statistics.toString());
			}
		}
		
		
	}

	/* (non-Javadoc)
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		// TODO Auto-generated method stub

	}
	
	
	public void registerConsumers() {
	
		registerConsumer("lookupTestMessageHandler", LookupTestMessage.class);
		
	}
	
	class TestStats {
		String lookupId;
		String responsible;
		long lookupTimeStart;
		long lookupTime;
		long transferTimeStart;
		long transferTime; //for sending the message
		long receiverProcessingDelay;
		
		

		/**
		 * @param lookupId
		 */
		public TestStats(String lookupId, long lookupTimeStart) {
			super();
			this.lookupId = lookupId;
			this.lookupTimeStart = lookupTimeStart;
			lookupTime = -1;
			transferTime = -1;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return lookupId + "\t" + responsible  + "\t" + lookupTime  + "\t" + transferTime;
		}
		
		
		public long getReceiverProcessingDelay() {
			return receiverProcessingDelay;
		}
		public void setReceiverProcessingDelay(long receiverProcessingDelay) {
			this.receiverProcessingDelay = receiverProcessingDelay;
		}
		public String getLookupId() {
			return lookupId;
		}
		public void setLookupId(String lookupId) {
			this.lookupId = lookupId;
		}
		public long getLookupTime() {
			return lookupTime;
		}
		public void setLookupTime(long lookupTime) {
			this.lookupTime = lookupTime;
		}
		public String getResponsible() {
			return responsible;
		}
		public void setResponsible(String responsible) {
			this.responsible = responsible;
		}
		public long getTransferTime() {
			return transferTime;
		}
		public void setTransferTime(long transferTime) {
			this.transferTime = transferTime;
		}
		public long getLookupTimeStart() {
			return lookupTimeStart;
		}
		public void setLookupTimeStart(long lookupTimeStart) {
			this.lookupTimeStart = lookupTimeStart;
		}
		public long getTransferTimeStart() {
			return transferTimeStart;
		}
		public void setTransferTimeStart(long transferTimeStart) {
			this.transferTimeStart = transferTimeStart;
		}
		
		
		
	}

}
