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
//import java.util.ArrayList;
//
//import dks.niche.NicheOSSupport;
//import dks.niche.ids.BindElement;
//import dks.niche.ids.ComponentId;
//import dks.niche.ids.ResourceId;
//import dks.niche.interfaces.JadeBindInterface;
//import dks.niche.interfaces.NicheActuatorInterface;
//import dks.niche.interfaces.NicheManagementInterface;
//import dks.niche.wrappers.BundleDescription;
//import dks.niche.wrappers.ResourceDescription;
//
///**
// * The <code>JadeTest</code> class
// *
// * @author Joel
// * @version $Id: JadeTest.java 294 2006-05-05 17:14:14Z joel $
// */
//public class JadeTest {
//
//	
//	public final static int pauseBeforeAction = 50000;
//	
//	
//	static final String TEST_KEY_ONE = "The first key";
//	static final String TEST_VALUE_ONE = "The first value";
//	static final String TEST_KEY_TWO = "The second key";
//	static final String TEST_VALUE_TWO = "The second value";
//	
//	static final int TEST_ONE_TO_ONE_INDICATOR = 1;
//	static final int TEST_ONE_TO_MANY_INDICATOR = 2;
//	static final int TEST_ONE_TO_ANY_INDICATOR = 3;
//	
//	
//	static final int ITEMS = 400;
//	
//	static int port;
//	static BigInteger id;
//	
//	private int bindCounter = 0;
//
//	private static int dhtTestPort;
//
//
//	private static long pauseBetweenPutAndGet;
//
//
//	private static int testDiscoverAndDeploy = 17001;
//	
//	private static int testBind = 18001;
//	
//	private static int testScript= 19001;
//	
//	private static int testWait = 20001;
//	
//	private static String [] initialStorageComponents = {"190000", "420000"};
//	private static String leavingNode = "190500";
//	private static String [] replacementNodes = {"830000", "770000"};
//	
//	static JadeTest myself;
//	static NicheOSSupport myStaticNiche;
//	static NicheActuatorInterface myNiche;
//	static NicheManagementInterface myManagement;
//	//static NicheOSTest myWaitTest;
//	
//	int SMALL_STORAGE = 600;
//	int BIG_STORAGE = 1700;
//	int myStorage = 0;
//	int numberOfDiscoveries = 0;
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		System.out.println("Starting, " + args[0] + " " + args[1] + " " + args[2]);
//		
//		int mode;
//		if(args[0].equals("create")) {
//			mode = NicheOSSupport.BOOT;
//		}
//		else if(args[0].equals("join")) {
//			mode = NicheOSSupport.JOINING;
//		}
//		else {
//			mode = 42;
//		}
//		
//		//int id = Integer.parseInt(args[1]);
//		port = Integer.parseInt(args[2]);
//		
//		
//		//Niche myCommunicator = new Niche(args[1], port, mode);
//		
//		
//		
//		
//		myself = new JadeTest();
//		
//		if(mode == NicheOSSupport.BOOT) {
//			
//			myStaticNiche = new NicheOSSupport(args[1], port, mode);
//			myStaticNiche.boot();
//			
//		}
//		else if (mode == NicheOSSupport.JOINING){
//			
//			myStaticNiche = new NicheOSSupport(args[1], port, mode);
//			myStaticNiche.join();
//			
//			//myDKSPrime.join();
//		}
//		else {
//			//myWaitTest = new NicheOSTest(args[1], port, mode);	
//		}
//		
//		
//		myStaticNiche.registerResourceEnquiryHandler(myself);
//		myStaticNiche.registerAllocationHandler(myself);
//		myStaticNiche.registerDeploymentHandler(myself);
//		myStaticNiche.registerBindHandler(myself);
//		//myNiche.registerReceiver(myself, "handleBindAck");
//
////		myNiche.registerDeliverHandler(myself, "deliver1");
////		myNiche.registerDeliverHandler(myself, "deliver2");
////		myNiche.registerDeliverHandler(myself, "deliver3");
//		
//		//myNiche.registerDelegateHandler(myself, "delegate");
//		
//		
//		id = myStaticNiche.getId();
//				
//		myNiche = myStaticNiche.getNicheActuator();
//		myManagement = myStaticNiche;
//		/*
//		 * Test DHT functionality
//		 * 
//		 */
//		if(port == dhtTestPort) {
//		
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			/*
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					//myCommunicator.put("katt"+i, i+"plattak"+i, putFlavor.PUT_ADD);
//					myCommunicator.asynchronousPut(TEST_KEY_ONE+i, i+TEST_VALUE_ONE+i, putFlavor.PUT_ADD, myself, "handlePutAck");
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				}
//			}
//			*/
//			
//			for(int i = 0; i<ITEMS; i++) {
//				try {
//					//myCommunicator.put(TEST_KEY_TWO+i, i+TEST_VALUE_TWO+i, putFlavor.PUT_ADD);
//					Thread.sleep(5);
//				} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				}
//			}
//
//			
//			try {
//				Thread.sleep(pauseBetweenPutAndGet);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			
//			/*
//			for(int i = 0; i<ITEMS; i++) {
//				System.out.print("InterfaceTest says: I asked for "+TEST_KEY_ONE+i+ " and got ");
//				System.out.println(myCommunicator.get(TEST_KEY_ONE+i, getFlavor.GET_ANY));
//				
//			}
//			*/
//			
//			for(int i = 0; i<ITEMS; i++) {
//					//myCommunicator.asynchronousGet(TEST_KEY_TWO+i, getFlavor.GET_ANY, this, "handleGetResponse");
//				try {
//					Thread.sleep(5);
//				}
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		
//				
//		/*
//		 * Test Discovery & Deploy functionality
//		 * 
//		 */
//		if(port == testDiscoverAndDeploy) {
//			
//			
//			
//			/* 
//			 * This was a simple test to compare the efficiency of method invocation through a known interface as 
//			 * compared with invocation through a known object+known method name 
//			 *   
//			 *
//			JadeInterfaceTest myIC1 = new JadeInterfaceTest();
//			JadeInterfaceTest myIC2 = new JadeInterfaceTest();
//			
//			JadeDirectTest myDC1 = new JadeDirectTest();
//			JadeDirectTest myDC2 = new JadeDirectTest();
//			
//			ClassWrapper s = new ClassWrapper(myDC1, "resourceEnquiry");
//			ClassWrapper t = new ClassWrapper(myDC2, "deploy");
//			
//			
//			boolean interfaceTest = false;
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			
//			System.out.println("Starting test");
//			int innerLoops = 1000;
//			int outerLoops = 1000;
//			int modulo = 1000;
//			Object[] array= new Object[modulo+1];
//			String request1 = "En ganska laang strang anvand for att testa i vilken utstrackning en granssnittsklass" +
//					" kan uppvisa battre prestanda jamfort med en dynamisk invokering av motsvarande metod tillhandahallen " +
//					"utan ett granssnitt";
//			
//			String request2 = "Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, " +
//					"eller hur? Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, eller hur? Dito, " +
//					"eller hur? Dito, eller hur? Dito, eller hur? ";
//			
//			long start, stop;
//					
//			if(interfaceTest) {
//				start = System.currentTimeMillis();
//				
//				for(int middleLoop = 0; middleLoop < outerLoops; middleLoop++) {
//				
//					for(int i = 0; i < innerLoops; i++) {
//						array[i % modulo] = ((JadeResourceEnquiryInterface) myIC1).resourceEnquiry(request1);
//						
//						array[1 + i % modulo] = ((JadeDeploymentInterface) myIC2).deploy(request2);
//					}
//					System.out.println(middleLoop);
//				}
//				
//				stop = System.currentTimeMillis();
//			}
//			else {
//				start = System.currentTimeMillis();
//			
//				for(int middleLoop = 0; middleLoop < outerLoops; middleLoop++) {
//			
//				for(int i = 0; i < innerLoops; i++) {
//					try {
//						array[i % modulo] = s.getObject().getClass().getMethod(s.getMethod(), new Class[]{Object.class}).invoke(s.getObject(), request1);
//						
//						array[1 + i % modulo] = t.getObject().getClass().getMethod(t.getMethod(), new Class[]{Object.class}).invoke(t.getObject(), request2);
//						
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (NoSuchMethodException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				System.out.println(middleLoop);
//				}
//			}
//			
//			stop = System.currentTimeMillis();
//		
//				
//			System.out.println("The time was "+(stop-start)+" ms");
//			
//			
//			/*
//			 * Results:
//			 * 
//			 * Interface: for running inside Eclipse without dks and noOfLoops = 1000, the time was 26312
//			 * 			  for running inside Eclipse without dks and outerLoops = 1000
//			 * 				 										 innerLoops = 10000, the time was 201256
//			 * 37326
//			 * 
//			 *  
//			 * Invocation: for running inside Eclipse without dks and noOfLoops = 1000, the time was 28597
//			 * 			   for running inside Eclipse without dks and outerLoops = 1000
//			 * 														  innerLoops = 10000, the time was 230127
//			 * 39203
//			 * 
//			 *  0.92009651, 
//			 *  0.87454319
//			 *  0.95212101
//			 */
//			
//			
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			
////			ResourceId myTemp1 = null, myTemp2 = null, myTemp3 = null;
////			try {
////				myTemp1 = new ResourceId("190000");
////				myTemp1.setDKSinfo(2, new DKSRef(InetAddress.getLocalHost(), 10000, new BigInteger("190000")));
////				
////				myTemp2 = new ResourceId("420000");
////				myTemp2.setDKSinfo(2, new DKSRef(InetAddress.getLocalHost(), 10000, new BigInteger("420000")));
////				
////				myTemp3 = new ResourceId("830000");
////				myTemp3.setDKSinfo(2, new DKSRef(InetAddress.getLocalHost(), 10000, id));
////				
////			} catch (UnknownHostException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//			ArrayList results = myNiche.discover("these are the descriptions"); // ArrayList(3);
//			//results.add(myTemp1);results.add(myTemp2);results.add(myTemp3);
//			
//			ArrayList msgs = new ArrayList<String>();
//			//msgs.add("Msg to node 1"); msgs.add("Msg to node 2"); msgs.add("Msg to node 3");
//			
//			for (Object object : results) {
//				ResourceId tempRID = ((ResourceId)object);
//				System.out.println("These was the results: "+ tempRID.getJadeNode());
//				msgs.add("Msg to node " + tempRID.getDKSRef().getId());
//			}
//			
//			System.out.println("Doin' deployment");
//			ArrayList depResults = (ArrayList) myNiche.deploy(results, msgs);
//			for (Object object : depResults) {
//				System.out.println("These was the dep.results: "+ ((ComponentId)object).getId());
//			}
//			//
//		}
//		
//	if(port == testBind) {
//			
//			
//			try {
//				Thread.sleep(pauseBeforeAction);
//			} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			}
//			
//			ArrayList<Object[]> results = myNiche.discover("these are the descriptions"); // ArrayList(3);
//			//results.add(myTemp1);results.add(myTemp2);results.add(myTemp3);
//			
//			ArrayList msgs = new ArrayList<String>();
//			//msgs.add("Msg to node 1"); msgs.add("Msg to node 2"); msgs.add("Msg to node 3");
//			ArrayList<ResourceId> inputToDeploy = new ArrayList<ResourceId>();
//			
//		
//			for (Object[] object : results) {
//				ResourceId tempRId = (ResourceId)object[1];
//				System.out.println("These was the results: "+ object[0] + " and " + tempRId.getJadeNode());
//				inputToDeploy.add(tempRId);
//				msgs.add("Msg to node " + tempRId.getDKSRef().getId());
//			}
//			
//			System.out.println("Doin' deployment");
//			results = (ArrayList)myNiche.deploy(inputToDeploy, msgs);
//			ComponentId callerSideCId = (ComponentId)(results.get(1))[1];
//			ArrayList<ComponentId> inputToBind = new ArrayList<ComponentId>(results.size());
//		
//			for (Object[] objects : results) {
//				if(objects == null) {
//					System.out.println("The deploy-operation failed partially");
//				}
//				else {
//					ComponentId tempCId = (ComponentId)objects[1]; 
//					System.out.println("These was the results: "+ objects[0] + " and " + tempCId.getGlobalComponentId());
//					inputToBind.add(tempCId);
//				}
//			}
//			//Testing synchronous
//			
//			//System.out.println("\n\n\n\nDoin' bindId");
//			Object[] bindResult = null; // myNiche.bind(callerSideCId, inputToBind, "This is non-null", null, 23);
//			System.out.println("Jadetest says: Doing it synch: "+((BindElement)(bindResult[1])).getOutstandingReplies()+"\n\n\n\n");
//			
//			//Testing asynchronous
//			callerSideCId = (ComponentId)(results.get(2))[1];
//			BindElement moreBindResult = null; //myNiche.asynchronousBind(callerSideCId, inputToBind, new Integer(TEST_ONE_TO_ANY_INDICATOR), "Now with work on both sides...", JadeBindInterface.ONE_TO_ANY, null, null,null);
//			
//			System.out.println("Jadetest says: Doing it asynch I: "+moreBindResult.getOutstandingReplies());
//			callerSideCId = (ComponentId)(results.get(3))[1];
//			BindElement evenMoreBindResult = null; //myNiche.asynchronousBind(callerSideCId, inputToBind, new Integer(TEST_ONE_TO_MANY_INDICATOR), "Now with work on both sides...", JadeBindInterface.ONE_TO_MANY, null, null,null);
//			
//			System.out.println("Jadetest says: Doing it asynch II: "+evenMoreBindResult.getOutstandingReplies());
//			
//			//evenMoreBindResult.doWait();
//			//moreBindResult.doWait();
//			
//			//System.out.println("After wait: "+moreBindResult.getOutstandingReplies());
//			//System.out.println("After wait: "+evenMoreBindResult.getOutstandingReplies());
//			//String a = "Tom"; 			dummy(a);			System.out.println("This is my dummy result: " +a);
//		}
//	
//	if(port == testScript) {
//		
//	
//		// testing in-parameters
//		
//		int yassGlobalStorageParameter = 1999; //="total storage"
//		int yassNodeStorageParameter = 200; //="minimum per node"
//		
//		if(args.length > 3) {
//			yassGlobalStorageParameter = Integer.parseInt(args[3]);
//		}
//		if(args.length > 4) {
//			yassNodeStorageParameter = Integer.parseInt(args[4]);
//		}
//		
//		int maximumAllocatedStorage = (int)(1.2 * yassGlobalStorageParameter);
//		int minimumAllocatedStorage = (int)(0.95 * yassGlobalStorageParameter);
//		double upperLoadThreshold = 0.90;
//		double lowerLoadThreshold = 0.50;
//
//		String preferences = "500";
//		
//		try {
//			Thread.sleep(pauseBeforeAction);
//		} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		
//		BundleDescription totalRequirements = new BundleDescription();		
//		
//		totalRequirements.describeResource(ResourceDescription.TYPE_STORAGE, yassGlobalStorageParameter);
//		
//		BundleDescription partialRequirements = new BundleDescription();
//		partialRequirements.describeResource(ResourceDescription.TYPE_STORAGE, yassNodeStorageParameter);
//		
//		//Startscript testink = null;
//		
//		
//			/*
//			 EventHandlerIllustrator(NicheOSSupport nicheInstance, BundleDescription totalRequirements, BundleDescription partialRequirements,
//					String preferences, URL storageComponentURL,
//					int maximumAllocatedStorage,
//					int minimumAllocatedStorage,
//					double upperLoadThreshold,
//					double lowerLoadThreshold)
//			*/
////			 testink = new EventHandlerIllustrator(myManagement, totalRequirements, partialRequirements,
////					 preferences, "http://error.in.fr",
////						maximumAllocatedStorage,
////						minimumAllocatedStorage,
////						upperLoadThreshold,
////						lowerLoadThreshold);
//		
//		
//	}
//	if(port == testWait) {
//		System.out.println("What should I test??");
//	}
//	
//		
//
//	}
//
//	
//	
//	/*
//	 * DHT-testing helper methods
//	 */
//	public void handlePutAck(Object o) {
//		
//	}
//	
//	public void handleSendAck(Object o) {
//		System.out.println("Ack on one-to-one sending");
//	}
//	
//	public void handleGetResponse(Object o) {
//		System.out.println("JadeTest says: GET at node "+id+". result is "+ o);
//	}
//	
//	/*
//	 * discover testing methods
//	 */
//	
//	public Object[] resourceEnquiry(Object o) {
//		
//		if(o instanceof BundleDescription) {
//			
//		
//			BundleDescription requirements = (BundleDescription ) o;
//		
//			BundleDescription myNode = new BundleDescription();
//		}
//		
//		String myNode = "MyNameIs:"+id;
//		
//		if(numberOfDiscoveries == 0) { 
//		
//			if(id.equals(new BigInteger(leavingNode))) {
//				myStorage = BIG_STORAGE; // an offer they cannot refuse
//			}
//			else {
//				for(int i = 0; i < initialStorageComponents.length; i++) {
//					if (id.equals(new BigInteger(initialStorageComponents[i]))) {
//						myStorage = SMALL_STORAGE;
//					}
//				}
//				if(myStorage < 0) {
//					myStorage = 0;
//				}
//			}
//			
//		}
//		else if (numberOfDiscoveries == 1) {
//			
//			for(int i = 0; i < replacementNodes.length; i++) {
//				if (id.equals(new BigInteger(replacementNodes[i]))) {
//					myStorage = SMALL_STORAGE;
//				}
//			}
//		}
//		//else {} // do nuffin
//		
//		//myNode.describeResource(ResourceDescription.TYPE_STORAGE, myStorage);
//		//ResourceDescription myResource = new ResourceDescription(ResourceDescription.TYPE_STORAGE, myStorage);
//		
//		String r = "Node "+id+" received the following msg in method resourceEnquiry: "+o+" I will respond with stating my storage: "+myStorage;
//		System.out.println(r);
//		
//		numberOfDiscoveries++;
//		return new Object[]{myNode, myStorage};
//	}
//	
//	/*
//	 * allocate testing methods
//	 */
//	
//	public Object[] allocate(Object o) {
//		System.out.println("Node "+id+" received an allocation request");
//		return new Object[]{"localARID"+id, 42};
//	}
//	
//	/*
//	 * deploy testing methods
//	 */
//	
//	public Object[] deploy(Object o) { //FIXME test(Object o)
//		
//		myStorage = 0;
//		String r = "Node "+id+" received a  msg in method deploy. I will no longer have any free storage";
//		myStorage = 0;
//		
//		if(id.equals(new BigInteger(leavingNode))) {
//			
//				r += " Time to start the demo!";
//					
//			int delayBeforeAction = pauseBeforeAction;
//			String message = null;		
//			
//			System.out.println("NicheTest says: starting demo-thread!");
//			//Sigh. make sure ur using the correct handler when testing...
//			TestManagement p = new TestManagement(myNiche, delayBeforeAction);
//			new Thread(p).start();
//		}
//		System.out.println(r);
//		return new Object[]{r, "local cid", "component reference"};
//	}
//	
//	/*
//	 * bind testing methods
//	 */
//
//	public Object[] bind(Object localComponentID, Object description) { //FIXME test(Object o)
//		bindCounter++;
//		String r = "Node "+id+" received the following msg in method bind, localComponentId: "+ localComponentID + ", with description: "+description;
//		System.out.println("JadeTest bind-method says: "+ r);
//		
//		//Testing some response pattern
//		if(description instanceof Integer) {
//			System.out.println("JadeTest bind-method says: Im an integer");
//			boolean startThread = true;
//			int delayBeforeAction = 1000;
//			String message = null;
//			
//			switch (((Integer)description).intValue()) {
//				case TEST_ONE_TO_ONE_INDICATOR:
//					delayBeforeAction = 7000;
//					message = "One to one";
//					break;
//				case TEST_ONE_TO_MANY_INDICATOR:
//					delayBeforeAction = 7000;
//					message = "One to many";
//					break;
//				case TEST_ONE_TO_ANY_INDICATOR:
//					delayBeforeAction = 7000;
//					message = "One to any";
//					break;
//				default:
//					startThread = false;
//					
//			}
//			
//			if(startThread) {
//				System.out.println("JadeTest says: starting thread!");
//				//Sigh. make sure ur using the correct handler when testing...
//				TestCommunication p = new TestCommunication((JadeBindInterface)myNiche, r+bindCounter, delayBeforeAction, message);
//				new Thread(p).start();				 
//			}
//		}
//		
//		return new Object[]{r, r+bindCounter};
//	}
//	
//	public void handleBindAck(Object o) {
//		System.out.println("A bindId has been established");
//	}
//	
//	public void deliver1(Object localBindId, Object message) {
//		System.out.println("JadeTest says: message "+ message +" was delivered in method test one-to-one");
//	}
//
//	public void deliver2(Object localBindId, Object message) {
//		System.out.println("JadeTest says: message "+ message +" was delivered in method test one-to-many");
//	}
//
//	public void deliver3(Object localBindId, Object message) {
//		System.out.println("JadeTest says: message "+ message +" was delivered in method test one-to-any");
//	}
//	
//	public void delegate() {
//		System.out.println("JadeTest says: Sleep 10 sek ");
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("then call 'Trigger' NOT");
//		//myCommunicator.trigger();
//		
//	}
//	
//
//	class TestCommunication implements Runnable {
//        JadeBindInterface subNiche;
//		Object handler;
//		int delayBeforeAction;
//        String message;
//        
//        TestCommunication(JadeBindInterface subNiche, Object handler, int delayBeforeAction, String message) {
//        	this.subNiche = subNiche;
//            this.handler = handler;
//            this.delayBeforeAction = delayBeforeAction;
//            this.message = message;
//        }
//
//        public void run() {
//        	System.out.println("JadeTest-TestCommunication says: waiting " + delayBeforeAction +" ms");
//        	try {
//				Thread.sleep(delayBeforeAction);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("JadeTest-TestCommunication says: sending the following message: " + message);
//			subNiche.send(handler, message);
//        }
//    }
//
//	
//	class TestManagement implements Runnable {
//		
//		NicheActuatorInterface subNiche;
//		int delayBeforeAction;
//                
//        TestManagement(NicheActuatorInterface subNiche, int delayBeforeAction) {
//        	this.subNiche = subNiche;
//            this.delayBeforeAction = delayBeforeAction;
//        }
//
//        public void run() {
//        	System.out.println("NicheTest-TestManagement says: waiting " + delayBeforeAction +" ms before initiating leave.");
//        	try {
//				Thread.sleep(delayBeforeAction);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("NicheTest-TestManagement says: I'm starting to leave!!");
//			//subNiche.leave();
//			System.out.println("NicheTest-TestManagement says: I'm gone");
//			System.exit(0);
//        }
//    }
//
//	
//}
//
//
//
//
///*
// * 
// * 
// * 
// *  Testing leftovers:
// *  
// *  
// *  
//TestTest myString = new TestTest();
//
////Serializable myMigratingEvent = (Serializable)myString.getClass();
//
//ByteArrayOutputStream baos = new ByteArrayOutputStream();
//ObjectOutputStream oos = null;
//try {
//	oos = new ObjectOutputStream( baos);
//	oos.writeObject(myString.getClass());
//	oos.flush();
//	//baos.flush();
//	byte myBuffer [] = baos.toByteArray();
//	System.out.println("the bytebuffer size is: "+myBuffer.length+"\nThe content is:\n");
//	for(int i = 0; i < myBuffer.length; i++) {
//		System.out.print(myBuffer[i]);
//	}
//	System.out.println("\nreverse!\n");
//	
//	
//	MigrationClassLoader myClassLoader = new MigrationClassLoader(); //
//	System.out.println("the bytebuffer size is: "+myBuffer.length+"\nThe content is:\n");
//	for(int i = 0; i < myBuffer.length; i++) {
//		System.out.print(myBuffer[i]);
//	}
//	System.out.println(" ");
//	
//	Object newClass = myClassLoader.getClass(myString.getClass().getName(), myBuffer);
//
//	
//} catch (IOException e) {
//	e.printStackTrace();
//}		
//*/
//
