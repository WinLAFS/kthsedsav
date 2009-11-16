/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.boot;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
//import dks.arch.Scheduler.RejectedExecution;
import dks.bcast.PseudoReliableIntervalBroadcastComponent;
import dks.bcast.SimpleIntervalBroadcastComponent;
import dks.comm.mina.CommunicationComponent;
import dks.dht.DHTComponent;
import dks.fd.FailureDetectorComponent;
import dks.niche.wrappers.SimpleResourceManager;
import dks.ring.ChordRingMaintenanceComponent;
import dks.ring.RingMaintenanceComponentInt;
import dks.ring.RingServlet;
import dks.router.NewFingerRouterComponent;
import dks.test.niche.NicheServlet;
import dks.timer.TimerComponent;
import dks.utils.LongSequenceGenerator;
import dks.web.jetty.DKSInfoServlet;
import dks.web.jetty.JettyServer;

/**
 * The <code>DKSNode</code> class
 * 
 * Dummy Class for generating a DKS d
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSNode.java 622 2008-07-09 09:26:34Z joel $
 */
public class DKSNode {

	protected ComponentRegistry registry;

	protected Scheduler scheduler;

	private DKSImplementation dksImplementation;

	private static int CORE_POOL_SIZE = 
		System.getProperty("dks.scheduler.corePoolSize") instanceof String ?
				Integer.parseInt(System.getProperty("dks.scheduler.corePoolSize"))
				: 10;

	private static int MAX_POOL_SIZE = 	
		System.getProperty("dks.scheduler.maxPoolSize") instanceof String ?
			Integer.parseInt(System.getProperty("dks.scheduler.maxPoolSize"))
			: 10;

	private static int KEEP_ALIVE_TIME = 
		System.getProperty("dks.scheduler.keepAliveTime") instanceof String ?
				Integer.parseInt(System.getProperty("dks.scheduler.keepAliveTime"))
				: 100;

	
	protected ExecutorService executor;
	/**
	 * Generates all the components needed fo the d
	 * 
	 * @param myRef
	 *            The DKSRef of the Peer
	 * @param string
	 */
	public DKSNode(DKSRef myRef, DKSParameters dksParameters, String webcacheAddress)  
	{
		this(myRef, dksParameters,webcacheAddress, null, null);
	}
	
	public DKSNode(DKSRef myRef, DKSParameters dksParameters, String webcacheAddress, ArrayList<NicheServlet> testServlets) {
		
		this(myRef, dksParameters,webcacheAddress, null, testServlets);
	}
	
	public DKSNode(DKSRef myRef, DKSParameters dksParameters,
			String webcacheAddress, SimpleResourceManager rm, ArrayList<NicheServlet> testServlets) {

		//executor = Executors.newFixedThreadPool()// newCachedThreadPool();
		LinkedBlockingQueue worksQueue = new LinkedBlockingQueue<Runnable>();
		executor = Executors.newCachedThreadPool();
			
//			new ThreadPoolExecutor(
//				CORE_POOL_SIZE,
//				MAX_POOL_SIZE,
//				KEEP_ALIVE_TIME,
//				TimeUnit.MICROSECONDS,
//				(BlockingQueue<Runnable>) worksQueue,
//				new RejectedExecution()
//		);
		Executor nicheExecutor = Executors.newCachedThreadPool();
		/* Starting the ComponentRegistry */
		registry = ComponentRegistry.init(dksParameters);

		/* Creating Scheduler */
		scheduler = new Scheduler(registry, executor, nicheExecutor);

		/* Creating the TimerComponent */
		new TimerComponent(registry, scheduler);

		// DirectByteBufferPool bufferPool = new
		// DirectByteBufferPool(scheduler,registry);
		//
		// /* Creating and Registering the Communicator */
		// @SuppressWarnings("unused")
		// CommunicationComponent communicatorComponent = CommunicationComponent
		// .newInstance(scheduler, registry, myRef.getIp(), myRef
		// .getPort(), myRef, bufferPool);

		new CommunicationComponent(scheduler, registry, executor, myRef);

		DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
				webcacheAddress);

		/* Creating and Registering the FailureDetectorComponentr */
		new FailureDetectorComponent(scheduler, registry, myRef);

		/*
		 * Creating Sequence generator shared between the OperationManager and
		 * the Router (The Lookup and RPC operation identifier's number should
		 * be unique)
		 */
		LongSequenceGenerator sequenceGenerator = new LongSequenceGenerator(0);
		//
		// @SuppressWarnings("unused")
		// OperationManagerComponent operationManagerComponent = 
//		new OperationManagerComponent(scheduler, registry, myRef, sequenceGenerator);

		/* Creating and Registering the RingMaintainer */
		// @SuppressWarnings("unused")
		// DKSRingMaintenanceComponent ringMaintenanceComponent = new
		// DKSRingMaintenanceComponent(
		// scheduler, registry, myRef, dksCacheManager);
		RingMaintenanceComponentInt ringMaintainer=new ChordRingMaintenanceComponent(scheduler, registry, myRef,
				dksCacheManager);

		RingServlet ringServlet=new RingServlet(ringMaintainer.getRingState());
		
		/* Creating and Registering the FingerRouter */
		NewFingerRouterComponent fingerRouter = new NewFingerRouterComponent(
				scheduler, registry, myRef, sequenceGenerator);

		/* Creating the DKSImplementation */
		dksImplementation = new DKSImplementation(scheduler, registry);

		// /* Creating and Registering the RingMaintainer */
		// @SuppressWarnings("unused")
		// WebServerComponent webServerComponent = new WebServerComponent(
		// scheduler, registry, chordRingMaintenanceComponent, myRef);


		JettyServer jettyServer = new JettyServer(myRef);
		jettyServer.addServlet(new DKSInfoServlet(myRef, dksParameters,
				fingerRouter), "/info");
		jettyServer.addServlet(ringServlet, "/ring");
		
		if(testServlets != null) {
			for (NicheServlet servlet : testServlets) {
				jettyServer.addServlet(servlet, servlet.getContext());
				System.out.println("Adding " + servlet.getContext());
			}
		}
		if(rm != null) {
			rm.setJettyServer(jettyServer);
		}
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new SimpleIntervalBroadcastComponent(scheduler, registry);

		new PseudoReliableIntervalBroadcastComponent(scheduler, registry);

		// /* Creating and Registering the DHTComponent */
		@SuppressWarnings("unused")
		DHTComponent dhtComponent = new DHTComponent(scheduler, registry);
	}

	public ComponentRegistry getComponentRegistry() {
		return registry;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return Returns the dksImplementation.
	 */
	public DKSImplementation getDksImplementation() {
		return dksImplementation;
	}

	class RejectedExecution implements RejectedExecutionHandler {

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			//log.debug("############# the executer rejected a task!! #############" );
			
		}
		
	}
}
