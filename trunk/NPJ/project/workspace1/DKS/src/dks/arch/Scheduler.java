/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import examples.events.MyPseudoDeliverEvent;

/**
 * The <code>Scheduler</code> class
 * 
 * @author Roberto Roverso
 * @version $Id: Scheduler.java 600 2008-04-23 13:30:13Z ahmad $
 */
public class Scheduler extends Thread {

	/*#%*/ private static Logger log = Logger.getLogger(Scheduler.class);

	/*
	 * Constants necessary for the pool
	 * 
	 */

	// TODO Verify the correctness of the parameters

	/* Fairness rate of teh event priority queue */
	private static int fairnessRate = 3;

	public Executor poolExec;
	public Executor nicheExecutor;
	//public ThreadPoolExecutor poolExec;

	/**
	 * Queue of "Works" to be run
	 */
	//private LinkedBlockingQueue<Runnable> worksQueue;

	/**
	 * Queue of events waiting to be handled
	 */
	// public PriorityBlockingQueue<Event> eventsQueue;
	public FairEventPriorityQueue eventsQueue;

	private boolean running = true;

	private ComponentRegistry compRegistry;

	private boolean testing = false;

//	private Set<WorkingPair> runningFIFOpairs;
//
//	private EventsRegistry eventsRegistry;

	//
	// /* Concurrency-related attributes */
	//
	// private Set<WorkingPair> runningSet;

	// private LinkedBlockingQueue<Work> waitingQueue;

//	private Object lock;

	/**
	 * Constructs the Scheduler
	 * 
	 * @param reg -
	 *            the Component Registry
	 */
	public Scheduler(ComponentRegistry registry, Executor executor, Executor nicheExecutor) {
		this.compRegistry = registry;
		
		//worksQueue = new LinkedBlockingQueue<Runnable>();

		// eventsQueue = new PriorityBlockingQueue<Event>(10,
		// new PriorityEventComparator());

		eventsQueue = new FairEventPriorityQueue(fairnessRate);

		// Starting the poolExecutor
		//System.out.println("Scheduler param: " + CORE_POOL_SIZE + " " + MAX_POOL_SIZE + " " + KEEP_ALIVE_TIME);
		
		poolExec = executor;
		this.nicheExecutor = nicheExecutor;
//		poolExec = new ThreadPoolExecutor(
//				CORE_POOL_SIZE,
//				MAX_POOL_SIZE,
//				KEEP_ALIVE_TIME,
//				TimeUnit.MICROSECONDS,
//				null,
//				new RejectedExecution()
//		);
		
//(BlockingQueue<Runnable>) worksQueue
		// runningSet = new HashSet<WorkingPair>();

		// waitingQueue = new LinkedBlockingQueue<Work>();

//		eventsRegistry = registry.getEventsRegistry();
//
//		runningFIFOpairs = new HashSet<WorkingPair>();
//
//		lock = new Object();

		registry.registerScheduler(this);

		this.setName("Scheduler");
		this.start();

	}

	@Override
	public void run() {
		while (running) {
			try {
				/*
				 * Blocking until an event is received
				 */
				// log.debug("Taking event from queue");
				Event event = eventsQueue.take();
				// log.debug("Event " + event.getClass()
				// + " taken from queue, scheduling..");

				// log.debug("scheduling event " + event.getClass());

				scheduleEvent(event);

			} catch (InterruptedException e) {
				/*#%*/ log.error("The Scheduler has been interrupted");
				e.printStackTrace();
			}
		}
	}

	private void scheduleEvent(Event event) throws InterruptedException {

		boolean consumers = true;
		/* Processing consumers */
		if (event.hasConsumers() && !testing) {

			for (EventConsumer consumer : event.getConsumers()) {

				// WorkingPair processedPair = new WorkingPair(event.getClass(),
				// consumer.getComponent().getClass());

				/* Generating Work */
				Work work = new Work(event, consumer.getComponent(), consumer
						.getHandler(), null);

				// runningFIFOpairs.add(processedPair);
				// execute(processedPair, work);

				execute(null, work);

			}

			// /*
			// * Blocks until all the works for this event have been executed,
			// * this enables the FIFO processing of the messages from the
			// * marshaler to the components
			// */
			// synchronized (lock) {
			// if (!runningFIFOpairs.isEmpty()) {
			//
			// // log.debug("Consumer FIFO event: BLOCKING Scheduler");
			// lock.wait();
			// } else {
			// // log
			// // .debug("Consumer FIFO event: NO NEED TO BLOCK Scheduler,
			// // consumer FIFO event already processed");
			// }
			// }

			return;
		} else {
			consumers = false;
			//log.debug("The event " + event.getClass() + " has no consumers!!");
		}

		/*
		 * Processing subscriptions
		 */
		Class eventClass = event.getClass();
		List<Subscription> subscriptions = compRegistry
				.getSubscriptions(eventClass);

		if (subscriptions != null) {

			/*
			 * Creating a "Work" for every subscription and then give it to the
			 * poolExecutor
			 */
			// log.debug("I have " + subscriptions.size() + " subs for event: "
			// + eventClass);
			for (Subscription subscription : subscriptions) {

				if (eventClass.equals(MyPseudoDeliverEvent.class)) {

					System.out.println("SUB="
							+ subscription.getComponent().getClass());
				}

				// WorkingPair processedPair = new WorkingPair(event.getClass(),
				// subscription.getComponent().getClass());

				Work work = new Work(event, subscription.getComponent(),
						subscription.getMethod(), null);

				/**
				 * If FIFO event, keep track of the execution
				 */
				/**
				 * No need of synchronizing because before execution of work
				 */
				// if (eventsRegistry.getFIFOevents().contains(
				// processedPair.getEventClass())) {
				// runningFIFOpairs.add(processedPair);
				// } else {
				// // log.debug("Executing non-FIFO event:"
				// // + processedPair.getEventClass());
				// }
				// // log.debug("Calling Execute Work ");
				/**
				 * Executing Work
				 */
				execute(null, work);

			}

			// /**
			// * Guarantee FIFO for events
			// */
			// /**
			// * Synchronizing on lock to protect the Structure runningFIFOpairs
			// * (the Work might finish and unlock before the Scheduler blocks)
			// */
			// synchronized (lock) {
			// if (eventsRegistry.getFIFOevents().contains(eventClass)
			// && !runningFIFOpairs.isEmpty()) {
			//
			// // log.debug("Thread: " + Thread.currentThread().getName());
			// // log.debug("FIFO event instance of class "
			// // + eventClass.getCanonicalName()
			// // + ": BLOCKING Scheduler");
			// lock.wait();
			// }
			// if (eventsRegistry.getFIFOevents().contains(eventClass)
			// && runningFIFOpairs.isEmpty()) {
			// // log
			// // .debug("FIFO event: NO NEED TO BLOCK Scheduler, FIFO
			// // event already processed");
			// }
			// }

			/* Blocks until all the works for this event have been executed */
			// try {
			// wait();
			// } catch (InterruptedException e) {
			// log.debug("Scheduler Interrupted");
			// e.printStackTrace();
			// }
		} /*#%*/ else if(!consumers) {
		/*#%*/ log.debug("The event " + event.getClass() + " has no consumers!!");
		/*#%*/ }
	}
	
	public void execute(Runnable task) {
		poolExec.execute(task);
	}

	/**
	 * Executes a {@link Work} obeying concurrency dependencies
	 */
	private void execute(WorkingPair processedPair, Work work) {

		/*
		 * Set<WorkingPair> dependenciesSet = eventsRegistry
		 * .getDependenciesSet(processedPair);
		 * 
		 * if (dependenciesSet != null && !dependenciesSet.isEmpty()) {
		 * 
		 * Getting dependencies' list Set<WorkingPair> dependenciesSetCopy =
		 * new HashSet<WorkingPair>( dependenciesSet);
		 * 
		 * Intersecting dependencies and running sets
		 * dependenciesSetCopy.retainAll(runningSet);
		 * 
		 * if (dependenciesSetCopy.isEmpty()) {
		 * 
		 * Adding to the running set runningSet.add(processedPair);
		 * 
		 * If it's not in the queue nothing bad happens
		 * waitingQueue.remove(work);
		 * 
		 * poolExec.execute(work); } else { try {
		 * 
		 * log.debug("Work of pair=" + work.getProcessedPair() + " blocked");
		 * 
		 * waitingQueue.put(work); } catch (InterruptedException e) {
		 * log.debug("Scheduler interrupted"); e.printStackTrace(); } } } else {
		 * 
		 * Adding to the running set runningSet.add(processedPair);
		 * 
		 * If it's not in the queue nothing bad happens
		 * waitingQueue.remove(work);
		 */

		/**
		 * if FIFO events keeps track
		 */

		// log.debug("Executing Work ");
		poolExec.execute(work);
		// }

	}

	synchronized void finishedWork(WorkingPair processedPair, Work work) {

		// /**
		// * Synchronizing on lock to protect the Structure runningFIFOpairs
		// */
		// synchronized (lock) {
		// if (runningFIFOpairs.contains(processedPair)) {
		// runningFIFOpairs.remove(processedPair);
		//
		// /**
		// * IF all the consumers for a FIFO event have been processed
		// * notify
		// */
		// if (runningFIFOpairs.isEmpty()) {
		// // log
		// // .debug("FIFO event processed by all components: RELEASING
		// // Scheduler");
		//
		// lock.notify();
		//
		// }
		// }
		//
		// // else {
		// //
		// // log.debug("Finished pair for NON FIFO event instance of class:"
		// // + processedPair.getEventClass());
		// // }
		// }

		/**
		 * Concurrency
		 */
		// runningSet.remove(processedPair);
		//
		// if (waitingQueue.isEmpty()) {
		//
		// synchronized (lock) {
		//
		// lock.notify();
		// }
		//
		// return;
		//
		// } else {
		//
		// /*
		// * Wakes up as many Works as possible, there can be works in the
		// * queue that have no dependencies
		// */
		//
		// for (Work wakenUpwork : waitingQueue) {
		//
		// log.debug("Work of pair=" + wakenUpwork.getProcessedPair()
		// + " unblocked");
		//
		// execute(wakenUpwork.getProcessedPair(), wakenUpwork);
		// }
		//
		// }
	}

	/**
	 * dispathes a new Event to be scheduled
	 * 
	 * @param event -
	 *            the new event to be scheduled
	 */
	public void dispatch(Event event) {
		/*#%*/ log.debug("dispatch event:" + event.getClass());
		eventsQueue.add(event);
	}

	/**
	 * use this to shutdown the scheduler
	 */
	public void shutdown() {
		// ComponentRegistry.getInstance().getCommunicatorComponent().shutdown();
		ComponentRegistry.getInstance().getTimerComponent().stopAll();
		this.running = false;
		//poolExec. shutdownNow();
		System.exit(0);
	}

	public void setDontProcessConsumers() {
		this.testing = true;
	}
	public Executor getNicheExecutor() {
		return nicheExecutor;
	}
	
	class RejectedExecution implements RejectedExecutionHandler {

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			/*#%*/ log.debug("############# the executer rejected a task!! #############" );
			
		}
		
	}
}
